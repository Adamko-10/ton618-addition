#version 150

// TA-02 Quasar Jets' film (client/quasarfilm). One program, a material per Mode (all our own, nothing of TON 618's or
// SS-21 Quasar's):
//  0 plain colour (Color)
//  1 a soft round dot (uv -1..1 across it; Color.rgb its colour, Color.a how strong, Param.x a gain past 1), added
//  2 our black hole on one camera-facing quad (uv -1..1): a thin disc, its far side bent up over the shadow, a photon
//    ring, the black shadow (Param: the disc's turning, how bright the disc is, how far it is tipped toward us
//    0.2 edge-on to 1 face-on, how blue-white it burns 0-1). Premultiplied.
//  3 the star: a lit ball on one quad (uv -1..1; Color.rgb its colour, Color.a how solid; Param.x its surface's slow
//    churn). Premultiplied.
//  4 the star engine's mirror, a cap of a sphere (uv.x 0 at its middle to 1 at its rim, uv.y round it 0-1;
//    Color.rgb the star's colour, Color.a 1 on its dark outer face and 0 on its lit inner face; Param: how far it has
//    unfolded 0-1, how lit 0-1, how solid). Premultiplied.
//  6 a far galaxy's smudge (uv already turned and squashed; Color.a how strong; Param.x: spiral arms 0-1), added
//  7 a wide glow with a hot core (uv -1..1; Color; Param.x a gain), added
//  8 a streak (uv.x -1..1 across it; Color, which fades along it), added
//  9 a soft halo (uv -1..1; Color.rgb, Color.a how strong; Param.x a gain), added
// 10 a jet of plasma, a tube (uv.x 1 where its surface faces us, uv.y 0 at its foot to 1 at its end; Color.rgb the
//    sheath's colour; Param: the knots' travel, how far its head has got 0-1, where it starts 0-1, how many knots;
//    Param2: how much it fades along its length, how white its core is, a gain), added
// 11 the star's tail of light, a tube (uv as 10; Color.rgb; Param.x how strong, Param.y its shimmer's travel), added
uniform float Mode;
uniform vec4 Param;
uniform vec4 Param2;

in vec2 uv;
in vec4 color;

out vec4 fragColor;

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
    float inner = clamp(1.0 - (rr - rin) / (rout - rin), 0.0, 1.0);
    float dop = 1.0 + 0.55 * (-q.x / r) * (1.0 - tilt * 0.8);
    vec3 dc = diskCol(inner, blue) * band * (0.3 + 1.1 * sw) * dop * (0.35 + 1.7 * inner * inner);
    float shadow = 1.0 - smoothstep(0.97, 1.03, arcR);
    float front = 1.0 - smoothstep(-0.01, 0.01, p.y);
    float diskVis = mix(1.0 - shadow, 1.0, front * (1.0 - smoothstep(0.6, 0.95, tilt)));
    float arc = smoothstep(1.02, 1.14, arcR) * (1.0 - smoothstep(1.25, 1.95, arcR));
    float topw = 0.5 + 0.5 * smoothstep(-0.4, 0.6, p.y / d);
    float a3 = atan(p.y, p.x) + Param.x * 0.6;
    float an = fbm(vec2(cos(a3), sin(a3)) * 2.3 + vec2(arcR * 2.0));
    vec3 ac = diskCol(0.8, blue) * arc * topw * (0.45 + 0.9 * an) * (1.0 + 0.4 * (-p.x / d)) * (1.0 - front * shadow);
    float ring = gs((arcR - 1.04) / 0.025);
    vec3 col = (dc * diskVis + ac + mix(vec3(1.0, 0.86, 0.62), vec3(0.85, 0.94, 1.0), blue) * ring * 0.9) * Param.y;
    col += mix(vec3(1.0, 0.5, 0.15), vec3(0.5, 0.7, 1.0), blue) * exp(-d * d / (R * R * 26.0)) * 0.05 * Param.y * (1.0 - shadow);
    col *= 1.0 - smoothstep(0.8, 1.0, d);
    float a = max(shadow, clamp(max(col.r, max(col.g, col.b)), 0.0, 1.0));
    return vec4(col, a);
}

vec4 star(vec2 p) {
    float r2 = dot(p, p);
    float mu = sqrt(max(1.0 - r2, 0.0));
    vec3 P = vec3(p, mu);
    float T = Param.x;
    float g = sin(P.x * 9.0 + T + sin(P.z * 6.0)) * sin(P.y * 11.0 - T * 0.7 + sin(P.x * 7.0)) * sin(P.z * 10.0 + T * 0.4);
    vec3 c = color.rgb * (0.5 + 0.65 * pow(mu, 0.6)) + vec3(1.0) * pow(mu, 3.0) * 0.3 + color.rgb * g * 0.10;
    float m = (1.0 - smoothstep(0.94, 1.0, sqrt(r2))) * color.a;
    return vec4(c * m, m);
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
    float line = max(smoothstep(0.42, 0.5, abs(gr - 0.5)), smoothstep(0.44, 0.5, abs(gp - 0.5)));
    float edge = smoothstep(lim - 0.05, lim, th);
    float lit = Param.y;
    vec3 c;
    if (color.a > 0.5) {
        // the outer face: dark panels, their seams, the rim catching the light
        c = vec3(0.035, 0.045, 0.07) + vec3(0.10, 0.15, 0.24) * line + color.rgb * edge * 0.7 * lit;
    } else {
        // the inner face: the star's own light thrown back
        c = color.rgb * (0.28 + 0.85 * lit) * (0.72 + 0.3 * cos(th * 2.4)) * (1.0 - 0.4 * line)
            + vec3(1.0) * 0.22 * lit * pow(1.0 - uv.x, 2.0) + color.rgb * edge * 0.5;
    }
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
    return vec4(c, a);
}

float soft(float g) {
    float a = g < 0.12 ? mix(1.0, 0.6, g / 0.12) : g < 0.4 ? mix(0.6, 0.13, (g - 0.12) / 0.28) : mix(0.13, 0.0, clamp((g - 0.4) / 0.6, 0.0, 1.0));
    return a;
}

void main() {
    int mode = int(Mode + 0.5);
    float g = length(uv);
    if (mode == 0) {
        fragColor = color;
    } else if (mode == 1) {
        float a = smoothstep(1.0, 0.0, g);
        fragColor = vec4(color.rgb * (color.a * a * a * Param.x), 1.0);
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
        float knots = 0.62 + 0.38 * sin(s * Param.w - Param.x) + 0.22 * sin(s * Param.w * 2.7 - Param.x * 1.9 + 1.0);
        float tip = 1.0 + 2.2 * smoothstep(Param.y - 0.03, Param.y, s) * step(Param.y, 0.999);
        vec3 c = mix(color.rgb, vec3(0.82, 0.92, 1.0), (0.3 + 0.7 * core) * Param2.y) * (0.14 + core) * knots * mix(1.0, Param2.x, s) * tip * Param2.z;
        fragColor = vec4(c, 1.0);
    } else {
        float s = uv.y;
        float a = pow(max(1.0 - s, 0.0), 1.7) * (0.2 + 0.8 * uv.x * uv.x) * Param.x * (0.82 + 0.18 * sin(s * 46.0 - Param.y * 2.0));
        fragColor = vec4(color.rgb * a, 1.0);
    }
}
