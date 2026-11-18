#version 330
#extension GL_ARB_separate_shader_objects : require

// TA-02 Quasar Jets' film (client/quasarfilm). One program, a material per Mode (all our own, nothing of TON 618's or
// SS-21 Quasar's). The scene is drawn into the film's own high-range picture (JetPost), so colours may go well past 1:
// the brighter, the more it glows once quasar_post has had it.
//  0 plain colour (Color)
//  1 a soft round dot (uv -1..1 across it; Color.rgb its colour, Color.a how strong, Param.x a gain past 1,
//    Param.y how much of a four-pointed glint it has), added
//  2 our black hole on one camera-facing quad (uv -1..1): a thin disc, its far side bent up over the shadow, a photon
//    ring, the black shadow (Param: the disc's turning, how bright the disc is, how far it is tipped toward us
//    0.2 edge-on to 1 face-on, how blue-white it burns 0-1). Premultiplied.
//  3 the star: a lit, boiling ball on one quad (uv -1..1; Color.rgb its colour, Color.a how solid; Param.x its
//    surface's slow churn, Param.y how bright). Premultiplied.
//  4 the star engine's mirror, a cap of a sphere (uv.x 0 at its middle to 1 at its rim, uv.y round it 0-1;
//    Color.rgb the star's colour, Color.a 1 on its dark outer face and 0 on its lit inner face; Param: how far it has
//    unfolded 0-1, how lit 0-1, how solid, a clock for the glints). Premultiplied.
//  6 a far galaxy's smudge (uv already turned and squashed; Color.a how strong; Param.x: spiral arms 0-1), added
//  7 a wide glow with a hot core (uv -1..1; Color; Param.x a gain), added
//  8 a streak (uv.x -1..1 across it; Color, which fades along it), added
//  9 a soft halo (uv -1..1; Color.rgb, Color.a how strong; Param.x a gain), added
// 10 a jet of plasma, a tube (uv.x 1 where its surface faces us, uv.y 0 at its foot to 1 at its end; Color.rgb the
//    sheath's colour; Param: the knots' travel, how far its head has got 0-1, where it starts 0-1, how many knots;
//    Param2: how much it fades along its length, how white its core is, a gain), added
// 11 the star's tail of light, a tube (uv as 10; Color.rgb; Param.x how strong, Param.y its shimmer's travel), added
// 12 the star's corona: streamers round it (uv -1..1 on a quad centred on the star; Color.rgb, Color.a how strong;
//    Param.x a clock, Param.y the star's radius as a share of the quad's half), added
// 13 the fast-forward's dial round the star (uv -1..1; Color; Param: where its hand is (turns), how smeared the hand
//    is 0-1, how far the ticks have turned, a pulse's radius 0-1 or 0 for none), added
// 14 deep space behind everything (uv -1..1 over the screen; Param: how far the view has turned right and up, the
//    picture's width over its height, how bright)
// Minecraft 26.3 (Fabric 26.3 branch): the projection comes in Minecraft's Projection block, the rest in ours
// (client/quasarfilm/JetGl).
layout(std140) uniform JetParams {
    mat4 ModelViewMat;
    vec4 ModeVec;
    vec4 Param;
    vec4 Param2;
};
#define Mode (ModeVec.x)

layout(location = 0) in vec2 uv;
layout(location = 1) in vec4 color;

layout(location = 0) out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x), mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}

float fbm(vec2 p) {
    float a = 0.5;
    float s = 0.0;
    for (int i = 0; i < 4; i++) {
        s += a * noise(p);
        p = p * 2.03 + vec2(17.0, 9.0);
        a *= 0.5;
    }
    return s;
}

// noise that joins up round a circle: x is 0-1 round it, cells how many cells in a turn
float ringNoise(float x, float y, float cells) {
    float f = fract(x) * cells;
    float i = floor(f);
    float u = f - i;
    u = u * u * (3.0 - 2.0 * u);
    float j = floor(y);
    float v = y - j;
    v = v * v * (3.0 - 2.0 * v);
    float i1 = mod(i + 1.0, cells);
    return mix(mix(hash(vec2(i, j)), hash(vec2(i1, j)), u), mix(hash(vec2(i, j + 1.0)), hash(vec2(i1, j + 1.0)), u), v);
}

float ringFbm(float x, float y, float cells) {
    return 0.5 * ringNoise(x, y, cells) + 0.3 * ringNoise(x + 0.37, y * 2.0 + 5.0, cells * 2.0)
        + 0.2 * ringNoise(x + 0.71, y * 4.0 + 11.0, cells * 4.0);
}

float gs(float x) {
    return exp(-x * x);
}

// the disc's colour from its cool rim (0) to its hot inner edge (1): orange while it starves, blue-white once fed
vec3 diskCol(float x, float blue) {
    vec3 warm = mix(vec3(0.85, 0.2, 0.03), vec3(1.0, 0.6, 0.2), smoothstep(0.0, 0.6, x)) + vec3(1.0, 0.9, 0.75) * smoothstep(0.55, 1.0, x) * 0.9;
    vec3 cold = mix(vec3(0.16, 0.34, 1.0), vec3(0.62, 0.82, 1.0), smoothstep(0.0, 0.6, x)) + vec3(1.0) * smoothstep(0.5, 1.0, x) * 0.9;
    return mix(warm, cold, blue);
}

vec4 hole(vec2 p) {
    float R = 0.16;
    float tilt = max(Param.z, 0.02);
    float blue = Param.w;
    float d = length(p) + 0.00001;
    float arcR = d / R;
    vec2 q = vec2(p.x, p.y / tilt);
    float r = length(q) + 0.00001;
    float rr = r / R;
    float rin = 1.9;
    float rout = 5.2;
    float band = smoothstep(rin, rin * 1.18, rr) * (1.0 - smoothstep(rout * 0.5, rout, rr));
    float a2 = atan(q.y, q.x) + Param.x * (0.6 + 0.8 / rr);
    float sw = fbm(vec2(cos(a2), sin(a2)) * rr * 1.3 + vec2(rr * 2.2, 0.0));
    // finer streaks wound round with the flow, so the disc reads as gas in orbit and not as a smooth plate
    float fine = fbm(vec2(cos(a2 * 2.0), sin(a2 * 2.0)) * 2.4 + vec2(rr * 9.0, 3.0));
    float inner = clamp(1.0 - (rr - rin) / (rout - rin), 0.0, 1.0);
    float dop = 1.0 + 0.7 * (-q.x / r) * (1.0 - tilt * 0.8);
    vec3 dc = diskCol(inner, blue) * band * (0.22 + 0.85 * sw + 0.5 * fine * fine) * dop * dop * (0.35 + 1.9 * inner * inner);
    float shadow = 1.0 - smoothstep(0.97, 1.03, arcR);
    float front = 1.0 - smoothstep(-0.01, 0.01, p.y);
    float diskVis = mix(1.0 - shadow, 1.0, front * (1.0 - smoothstep(0.6, 0.95, tilt)));
    float arc = smoothstep(1.02, 1.14, arcR) * (1.0 - smoothstep(1.25, 1.95, arcR));
    float topw = 0.5 + 0.5 * smoothstep(-0.4, 0.6, p.y / d);
    float a3 = atan(p.y, p.x) + Param.x * 0.6;
    float an = fbm(vec2(cos(a3), sin(a3)) * 2.3 + vec2(arcR * 2.0));
    vec3 ac = diskCol(0.8, blue) * arc * topw * (0.45 + 0.9 * an) * (1.0 + 0.4 * (-p.x / d)) * (1.0 - front * shadow);
    // the photon ring: a hair of light hugging the shadow, and a fainter one just outside it
    float ring = gs((arcR - 1.04) / 0.02) + 0.35 * gs((arcR - 1.1) / 0.012);
    vec3 col = (dc * diskVis + ac + mix(vec3(1.0, 0.86, 0.62), vec3(0.85, 0.94, 1.0), blue) * ring * 1.3) * Param.y;
    col += mix(vec3(1.0, 0.5, 0.15), vec3(0.5, 0.7, 1.0), blue) * exp(-d * d / (R * R * 26.0)) * 0.05 * Param.y * (1.0 - shadow);
    col *= 1.0 - smoothstep(0.8, 1.0, d);
    float a = max(shadow, clamp(max(col.r, max(col.g, col.b)), 0.0, 1.0));
    return vec4(col, a);
}

vec4 star(vec2 p) {
    // pulled out of shape by the tide (Param.z), its ends come to points: a spindle, not a pill
    float w = pow(max(1.0 - abs(p.x), 0.0001), 0.75 * Param.z);
    p.y /= max(w, 0.04);
    float r2 = dot(p, p);
    float mu = sqrt(max(1.0 - r2, 0.0));
    vec3 P = vec3(p, mu);
    float T = Param.x;
    // its surface, turning slowly: boiling cells, brighter where hot gas wells up, a few dark spots
    vec2 q = vec2(atan(P.x, P.z) + T * 0.05, asin(clamp(P.y, -1.0, 1.0))) * 2.6;
    float warp = fbm(q * 1.6 + vec2(T * 0.11, -T * 0.07));
    float cells = fbm(q * 4.2 + vec2(warp * 2.2, warp * 1.3 + T * 0.16));
    float grain = noise(q * 17.0 + vec2(T * 0.5, 0.0));
    float spots = smoothstep(0.7, 0.82, fbm(q * 0.9 + vec2(40.0, T * 0.02)));
    vec3 hot = mix(color.rgb, vec3(1.0), 0.45);
    // (its cooler gas is a deeper shade of its own colour: a blue giant's is dark blue, not brown)
    vec3 cool = color.rgb * color.rgb * 0.62;
    vec3 surface = mix(cool, hot, smoothstep(0.25, 0.72, cells + (grain - 0.5) * 0.2));
    surface *= 1.0 - 0.6 * spots;
    // darker and redder toward its edge, as real stars are
    float limb = 0.3 + 0.7 * pow(mu, 0.55);
    vec3 c = surface * limb * mix(vec3(1.0, 0.82, 0.7), vec3(1.0), mu) * (0.92 + Param.y);
    float m = (1.0 - smoothstep(0.95, 1.0, sqrt(r2))) * color.a;
    return vec4(c * m, m);
}

vec4 corona() {
    float r = length(uv);
    float x = r / max(Param.y, 0.001);
    float ang = atan(uv.y, uv.x) / 6.2832;
    float T = Param.x;
    float fall = exp(-max(x - 1.0, 0.0) * 1.7);
    float streamers = ringFbm(ang, x * 0.7 - T * 0.12, 9.0);
    float fine = ringFbm(ang + 0.2, x * 1.8 - T * 0.3, 30.0);
    float glow = fall * (0.3 + 1.7 * streamers * streamers + 0.35 * fine) * smoothstep(0.86, 1.02, x);
    // loops of hot gas standing off its edge
    float loops = gs((x - 1.07 - 0.07 * ringNoise(ang, T * 0.05, 14.0)) / 0.035) * smoothstep(0.55, 0.8, ringNoise(ang + 0.5, 3.0, 14.0));
    vec3 c = mix(color.rgb, vec3(1.0), 0.4 * exp(-max(x - 1.0, 0.0) * 4.0)) * (glow + loops * 0.9);
    return vec4(c * color.a * (1.0 - smoothstep(0.8, 1.0, r)), 1.0);
}

vec4 mirror() {
    float cap = 1.08;
    float th = uv.x * cap;
    float lim = Param.x * cap;
    float A = Param.z;
    if (th > lim || A < 0.01) {
        discard;
    }
    float gr = fract(uv.x * 7.0);
    float gp = fract(uv.y * 24.0);
    vec2 id = vec2(floor(uv.x * 7.0), floor(uv.y * 24.0));
    float line = max(smoothstep(0.42, 0.5, abs(gr - 0.5)), smoothstep(0.44, 0.5, abs(gp - 0.5)));
    float edge = smoothstep(lim - 0.05, lim, th);
    float lit = Param.y;
    // no two panels quite alike: each a little brighter or duller, and now and then one catches the light
    float own = hash(id);
    float glint = pow(max(0.0, sin(Param.w * 0.35 + own * 40.0)), 24.0);
    vec3 c;
    if (color.a > 0.5) {
        // the outer face: dark panels, their seams, the rim catching the light
        c = vec3(0.03, 0.04, 0.065) * (0.7 + 0.6 * own) + vec3(0.12, 0.17, 0.26) * line + color.rgb * edge * 0.9 * lit
            + vec3(0.5, 0.6, 0.8) * glint * 0.25;
    } else {
        // the inner face: the star's own light thrown back, hottest at its middle
        c = color.rgb * (0.22 + 0.62 * lit) * (0.72 + 0.3 * cos(th * 2.4)) * (0.75 + 0.5 * own) * (1.0 - 0.55 * line)
            + vec3(1.0) * 0.22 * lit * pow(1.0 - uv.x, 2.0) + color.rgb * edge * 0.5 + vec3(1.0) * glint * 0.45 * lit;
    }
    // a panel just unfolded glows along its leading edge
    c += mix(color.rgb, vec3(1.0), 0.5) * edge * (1.0 - step(0.999, Param.x)) * 0.8;
    return vec4(c * A, A);
}

vec4 galaxy(vec2 p, float arms) {
    float d = length(p);
    vec3 c = mix(vec3(1.0, 0.94, 0.86), vec3(0.86, 0.78, 1.0), smoothstep(0.0, 0.18, d));
    c = mix(c, vec3(0.59, 0.67, 1.0), smoothstep(0.18, 0.6, d));
    float a = mix(0.9, 0.35, smoothstep(0.0, 0.18, d));
    a = mix(a, 0.08, smoothstep(0.18, 0.6, d));
    a = mix(a, 0.0, smoothstep(0.6, 1.0, d));
    float sp = 0.5 + 0.5 * sin(2.0 * atan(p.y, p.x) + 9.0 * sqrt(d));
    a *= mix(1.0, 0.4 + 1.5 * sp * sp, arms * smoothstep(0.08, 0.3, d));
    // dust lanes and knots of young stars along the arms
    a *= 0.75 + 0.5 * fbm(p * 9.0 + 3.0);
    return vec4(c, a);
}

float soft(float g) {
    float a = g < 0.12 ? mix(1.0, 0.6, g / 0.12) : g < 0.4 ? mix(0.6, 0.13, (g - 0.12) / 0.28) : mix(0.13, 0.0, clamp((g - 0.4) / 0.6, 0.0, 1.0));
    return a;
}

vec4 dial() {
    float r = length(uv);
    float ang = atan(uv.x, uv.y) / 6.2832;
    float ring = gs((r - 0.86) / 0.006) * 0.9 + gs((r - 0.93) / 0.004) * 0.45;
    float t60 = (1.0 - smoothstep(0.07, 0.11, abs(fract(ang * 60.0 + Param.z) - 0.5))) * step(0.815, r) * step(r, 0.845);
    float t12 = (1.0 - smoothstep(0.012, 0.02, abs(fract(ang * 12.0 + Param.z * 0.2) - 0.5))) * step(0.775, r) * step(r, 0.845);
    // the hand: a sharp line while time runs slowly, smeared into a band of light just inside the ring as it races
    float behind = fract(Param.x - ang);
    float smear = max(Param.y, 0.004);
    float inner = mix(0.1, 0.62, Param.y);
    float hand = exp(-behind / smear) * (1.0 - smoothstep(0.72, 0.78, r)) * smoothstep(inner - 0.06, inner + 0.06, r) * mix(1.0, 0.3, Param.y);
    float pulse = Param.w > 0.001 ? gs((r - Param.w) / 0.025) * (1.0 - Param.w) * 1.6 : 0.0;
    float a = ring + t60 * 0.55 + t12 * 0.9 + hand * 0.9 + pulse + 0.02 * (1.0 - smoothstep(0.84, 0.86, r));
    return vec4(mix(color.rgb, vec3(1.0), hand * 0.5) * a * color.a, 1.0);
}

vec4 deepSpace() {
    vec2 p = vec2(uv.x * Param.z, uv.y) * 0.5 + Param.xy;
    float n1 = fbm(p * 0.9 + 3.0);
    float n2 = fbm(p * 1.7 + vec2(n1 * 1.5, 7.0));
    float cloud = smoothstep(0.42, 0.85, n2);
    vec3 c = mix(vec3(0.05, 0.02, 0.1), vec3(0.02, 0.07, 0.12), n1) * cloud * 0.9
        + vec3(0.1, 0.04, 0.03) * smoothstep(0.6, 0.95, fbm(p * 0.5 + 20.0)) * 0.5;
    // the dust of far stars
    vec2 g = p * 90.0;
    vec2 id = floor(g);
    vec2 f = fract(g) - 0.5 - (vec2(hash(id + 3.1), hash(id + 7.7)) - 0.5) * 0.6;
    float st = step(0.975, hash(id)) * exp(-dot(f, f) * 60.0) * (0.3 + 0.7 * hash(id + 1.3));
    c += vec3(0.8, 0.85, 1.0) * st * 0.55;
    return vec4(c * Param.w, 1.0);
}

void main() {
    int mode = int(Mode + 0.5);
    float g = length(uv);
    if (mode == 0) {
        fragColor = color;
    } else if (mode == 1) {
        float a = exp(-g * g * 4.5) * (1.0 - smoothstep(0.8, 1.0, g));
        a += Param.y * exp(-abs(uv.x * uv.y) * 90.0) * exp(-g * 3.0);
        fragColor = vec4(color.rgb * (color.a * a * Param.x), 1.0);
    } else if (mode == 2) {
        fragColor = hole(uv);
    } else if (mode == 3) {
        fragColor = star(uv);
    } else if (mode == 4) {
        fragColor = mirror();
    } else if (mode == 6) {
        vec4 s = galaxy(uv, Param.x);
        fragColor = vec4(s.rgb * (s.a * color.a), 1.0);
    } else if (mode == 7) {
        float a = (exp(-g * g * 9.0) * 0.6 + exp(-g * 26.0) * 0.9) * (1.0 - smoothstep(0.75, 1.0, g));
        fragColor = vec4(color.rgb * (color.a * a * Param.x), 1.0);
    } else if (mode == 8) {
        fragColor = vec4(color.rgb * (color.a * (1.0 - abs(uv.x))), 1.0);
    } else if (mode == 9) {
        fragColor = vec4(color.rgb * (color.a * soft(g) * Param.x), 1.0);
    } else if (mode == 10) {
        float s = uv.y;
        if (s > Param.y || s < Param.z) {
            discard;
        }
        float core = uv.x * uv.x;
        float knots = 0.8 + 0.2 * sin(s * Param.w - Param.x) + 0.13 * sin(s * Param.w * 2.7 - Param.x * 1.9 + 1.0);
        // the plasma is never smooth: it tears and billows as it goes
        float torn = fbm(vec2(uv.x * 4.0, s * Param.w * 0.6 - Param.x * 0.5));
        float tip = 1.0 + 2.2 * smoothstep(Param.y - 0.03, Param.y, s) * step(Param.y, 0.999);
        vec3 c = mix(color.rgb, vec3(0.82, 0.92, 1.0), (0.3 + 0.7 * core) * Param2.y) * (0.05 + core * core * core * 1.9) * knots * (0.55 + 0.9 * torn)
            * mix(1.0, Param2.x, s) * tip * Param2.z;
        fragColor = vec4(c, 1.0);
    } else if (mode == 11) {
        float s = uv.y;
        float rays = 0.6 + 0.8 * fbm(vec2(uv.x * 5.0, s * 3.0 - Param.y * 0.2));
        float a = pow(max(1.0 - s, 0.0), 1.7) * uv.x * uv.x * uv.x * Param.x * (0.82 + 0.18 * sin(s * 46.0 - Param.y * 2.0)) * rays;
        fragColor = vec4(color.rgb * a, 1.0);
    } else if (mode == 12) {
        fragColor = corona();
    } else if (mode == 13) {
        fragColor = dial();
    } else {
        fragColor = deepSpace();
    }
}
