#version 150

// TA-02 Quasar Jets' world effects (client/quasar/QuasarRender). All our own. Every material writes colour that is
// already multiplied by its alpha: alpha 0 adds light to what is there, alpha above 0 also covers it (smoke, the
// lobe's skin against a day sky). A material per Mode:
//  1 a named star in the night sky (uv -1..1 on a camera-facing quad; Color.rgb its colour, Color.a how strong;
//    Param: seconds, its own seed, locked 0-1, how much it twinkles)
//  2 the second sun: TON618 blazing as a quasar (uv -1..1; Color.a how strong; Param.x seconds)
//  3 the jet, a band turned to the eye (uv.x -1..1 across it, uv.y blocks along it from the mark; Color.rgb the
//    sheath's colour, Color.a how strong; Param: ticks, where its head is (blocks from the mark), the core's width as
//    a share of the band, where it fades out far off; Param2.x: 1 once it stands on the ground)
//  4 the radio lobe, a dome (uv.x 0-1 round it, uv.y 0 at the ground to 1 at the top; Color.rgb, Color.a how squarely
//    this bit of its skin faces the eye; Param: ticks, how strong, its radius in blocks, how white (the pop))
//  5 a shock wave running over the ground, a flat ring (uv.x 0 at its inner edge to 1 at its front, uv.y 0-1 round
//    it; Color; Param: ticks, how strong, how many ripples round it)
//  6 a wall of dust behind the shock wave, a standing strip (uv.x 0-1 round, uv.y 0 at the ground to 1 at its top;
//    Color.rgb the dust; Param: ticks, how strong, how many billows round it)
//  7 the fireball of the landing, a dome (uv as 4; Color.rgb the star's colour, Color.a facing; Param: ticks, its age
//    0-1, its radius in blocks, how strong)
//  8 the mark on the ground (uv -1..1 on a flat quad; Color; Param: ticks, how strong, the first pulse 0-1)
//  9 a round glow with a hot middle (uv -1..1; Color; Param.x how tight the middle is, Param.y how much wide haze)
// Bake 1: painted onto a texture for a shader pack (client/quasar/QuasarAtlas), see the end of main; 2: the same, as
// its square root (for the pack's sky program, which squares it).
uniform float Mode;
uniform float Bake;
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

vec4 star() {
    float r = length(uv);
    float T = Param.x;
    float seed = Param.y;
    // the shimmer of the air: its brightness and the length of its rays never sit still
    float tw = 1.0 + Param.w * (0.55 * sin(T * 7.3 + seed * 11.0) * sin(T * 3.1 + seed * 5.0) + 0.3 * sin(T * 13.7 + seed * 3.0));
    float tw2 = 1.0 + Param.w * 1.4 * sin(T * 5.1 + seed * 7.0) * sin(T * 2.3 + seed);
    float core = exp(-r * r * 1100.0);
    float glow = exp(-r * r * 85.0) * 0.5;
    float halo = exp(-r * 5.5) * 0.16;
    vec2 a = abs(uv);
    float spikes = exp(-a.y * 110.0) * exp(-a.x * 3.4 / tw2) + exp(-a.x * 110.0) * exp(-a.y * 3.4 / tw2);
    vec2 d = abs(vec2(uv.x + uv.y, uv.x - uv.y)) * 0.7071;
    float cross2 = (exp(-d.y * 150.0) * exp(-d.x * 7.0) + exp(-d.x * 150.0) * exp(-d.y * 7.0)) * 0.3;
    vec3 c = vec3(1.0) * core * 1.3 + mix(vec3(1.0), color.rgb, 0.6) * glow + color.rgb * (halo + (spikes + cross2) * 0.55);
    c *= tw;
    // locked: a dashed ring turning round it, a thin one inside
    float lock = Param.z;
    if (lock > 0.001) {
        float ang = atan(uv.y, uv.x) / 6.2832;
        float dash = step(0.38, fract(ang * 10.0 + T * 0.12));
        float ring = gs((r - 0.34) / 0.012) * dash + gs((r - 0.27) / 0.006) * 0.5;
        c += mix(color.rgb, vec3(1.0), 0.3) * ring * lock * 0.9;
    }
    c *= (1.0 - smoothstep(0.75, 1.0, r)) * color.a;
    return vec4(c, 0.0);
}

vec4 sun() {
    float r = length(uv);
    float T = Param.x;
    float ang = atan(uv.y, uv.x);
    float disc = 1.0 - smoothstep(0.016, 0.026, r);
    float glow = exp(-r * r * 260.0);
    float corona = exp(-r * 11.0) * 0.55;
    float wide = exp(-r * 3.2) * 0.2;
    float n = 0.5 + 0.5 * sin(ang * 13.0 + T * 0.21 + sin(ang * 5.0 - T * 0.1) * 1.5);
    float m = 0.5 + 0.5 * sin(ang * 7.0 - T * 0.13);
    float rays = pow(n * m, 2.2) * exp(-r * 5.0) * 0.9;
    float streak = exp(-abs(uv.y) * 85.0) * exp(-abs(uv.x) * 2.4) * 0.85 + exp(-abs(uv.x) * 140.0) * exp(-abs(uv.y) * 7.0) * 0.3;
    vec3 c = vec3(1.0) * (disc * 1.5 + glow) + vec3(0.72, 0.86, 1.0) * (corona + rays + streak) + vec3(0.35, 0.55, 1.0) * wide;
    c *= (1.0 - smoothstep(0.8, 1.0, r)) * color.a;
    return vec4(c, 0.0);
}

vec4 beam() {
    float x = uv.x;
    float y = uv.y;
    float head = Param.y;
    if (y < head) {
        discard;
    }
    float T = Param.x;
    float ax = abs(x);
    float cw = max(Param.z, 0.02);
    float core = exp(-ax * ax / (cw * cw));
    float inner = exp(-ax * ax / (cw * cw * 9.0)) * 0.45;
    float sheath = exp(-ax * ax * 6.5);
    // knots racing down it toward the mark
    float knots = 0.86 + 0.14 * sin(y * 0.55 + T * 2.6) + 0.1 * sin(y * 1.7 + T * 4.1 + 1.0);
    // the field wound round it: two threads that cross and cross again
    float wind = 0.55 * sin(y * 0.33 + T * 1.3);
    float threads = gs((x - wind) / 0.09) + gs((x + wind) / 0.09);
    float turb = fbm(vec2(x * 3.0, y * 0.35 + T * 1.6));
    // its head burns brightest, and so does its foot once it stands on the ground
    float tip = 1.0 + 2.2 * exp(-max(y - head, 0.0) * 0.22) * (1.0 - Param2.x) + Param2.x * 1.2 * exp(-max(y, 0.0) * 0.12);
    float far = 1.0 - smoothstep(Param.w * 0.55, Param.w, y);
    vec3 c = vec3(0.92, 0.97, 1.0) * (core * 1.25 + inner * 0.45) * knots * tip
        + color.rgb * sheath * (0.55 * (0.5 + turb) + threads * 0.22) * knots * (0.7 + 0.3 * tip);
    float shown = smoothstep(1.0, 0.72, ax) * far * color.a;
    // the core is a solid line of light (it covers the sky behind it); the sheath round it is only added
    return vec4(c * shown, clamp(core * 0.8 * shown, 0.0, 1.0));
}

vec4 dome() {
    float f = clamp(color.a, 0.0, 1.0);
    float T = Param.x;
    float R = Param.z;
    float rim = pow(1.0 - f, 2.4);
    float cells = max(6.0, floor(R * 1.2));
    float yy = uv.y * R * 0.3;
    float n = ringFbm(uv.x + T * 0.0016, yy - T * 0.035, cells);
    float n2 = ringFbm(uv.x - T * 0.0023, yy * 1.7 + T * 0.02, cells * 2.0);
    // threads of lightning crawling over its skin
    float top = smoothstep(0.78, 1.0, uv.y);
    n = mix(n, 0.5, top);
    n2 = mix(n2, 0.5, top);
    float arcs = pow(1.0 - abs(2.0 * ringFbm(uv.x + T * 0.004, yy * 0.8 + T * 0.05, cells) - 1.0), 14.0) * (1.0 - top);
    // (the threads come and go in patches, not all over it at once)
    arcs *= smoothstep(0.45, 0.7, n2);
    float foot = exp(-uv.y * 6.0) * 0.5;
    float white = Param.w;
    vec3 tint = mix(color.rgb, vec3(1.0), white);
    float a = Param.y;
    // A skin of coloured light that also covers what is behind it: thin where we look straight through the bubble,
    // thick at its edge. (Light only added would wash out to white against a day sky.)
    float cover = clamp(a * (0.2 + 0.14 * n + 0.6 * rim + 0.2 * foot), 0.0, 0.92) * (1.0 - white);
    vec3 skin = tint * (0.62 + 0.45 * rim + 0.3 * n * n2);
    vec3 light = vec3(1.0) * (arcs * 0.8 + rim * rim * 0.35) + tint * white * (0.4 + rim);
    return vec4(skin * cover + light * a, cover);
}

vec4 shock() {
    float T = Param.x;
    float front = pow(uv.x, 3.0) * (1.0 - smoothstep(0.9, 1.0, uv.x));
    float lip = gs((uv.x - 0.9) / 0.05);
    float n = 0.55 + 0.9 * ringFbm(uv.y, uv.x * 3.0 - T * 0.15, Param.z);
    vec3 c = mix(color.rgb, vec3(1.0), 0.35 + 0.5 * lip) * (front * 0.7 + lip) * n * Param.y * color.a;
    return vec4(c, 0.0);
}

vec4 dust() {
    float T = Param.x;
    float n = ringFbm(uv.x, uv.y * 2.4 - T * 0.06, Param.z);
    float n2 = ringFbm(uv.x + 0.5, uv.y * 5.0 - T * 0.11, Param.z * 2.0);
    float body = smoothstep(0.2 + 0.55 * uv.y, 0.75 + 0.2 * uv.y, n * 0.7 + n2 * 0.3 + (1.0 - uv.y) * 0.45);
    // (Color.a: thinned out near the eye, so the wave washes over the camera and doesn't slap it like a wall)
    float a = body * (1.0 - smoothstep(0.45, 1.0, uv.y)) * smoothstep(0.0, 0.04, uv.y) * Param.y * color.a;
    vec3 c = color.rgb * (0.6 + 0.6 * n2) * (0.8 + 0.4 * uv.y);
    return vec4(c * a, a * 0.9);
}

vec4 fireball() {
    float f = clamp(color.a, 0.0, 1.0);
    float T = Param.x;
    float age = Param.y;
    float R = Param.z;
    float cells = max(5.0, floor(R * 0.9));
    float yy = uv.y * R * 0.22;
    float n = ringFbm(uv.x, yy - T * 0.07 - age * 2.0, cells);
    float n2 = ringFbm(uv.x + 0.3, yy * 2.2 - T * 0.03, cells * 2.0);
    float top = smoothstep(0.78, 1.0, uv.y);
    n = mix(n, 0.5, top);
    n2 = mix(n2, 0.45, top);
    // it cools as it ages: white, the star's colour, orange, then smoke
    float heat = clamp(1.2 - age * 2.2 + (n - 0.5) * 1.1 + (f - 0.6) * 0.55, 0.0, 1.0);
    vec3 hot = mix(vec3(1.0, 0.36, 0.06), mix(color.rgb, vec3(1.0), 0.75), smoothstep(0.45, 1.0, heat));
    vec3 smoke = vec3(0.09, 0.08, 0.075) * (0.6 + 0.8 * n2);
    float glowing = smoothstep(0.0, 0.45, heat);
    // it breaks up into rags as it ages
    float body = smoothstep(0.0, 0.6, f) * smoothstep(age * 1.25 - 0.3, age * 1.25 + 0.2, n2 * 0.7 + n * 0.3 + 0.25);
    float a = body * Param.w;
    vec3 c = mix(smoke, hot * (0.5 + 0.7 * heat), glowing);
    return vec4(c * a, a * mix(0.85, 0.2, glowing));
}

vec4 mark() {
    float r = length(uv);
    float T = Param.x;
    float ang = atan(uv.y, uv.x) / 6.2832;
    float ring = gs((r - 0.6) / 0.03);
    float outer = gs((r - 0.86) / 0.016) * step(0.5, fract(ang * 24.0 + T * 0.004));
    float arcs = gs((r - 0.4) / 0.018) * step(0.55, fract(ang * 3.0 - T * 0.012));
    float ticks = (1.0 - smoothstep(0.012, 0.02, abs(fract(ang * 4.0 + 0.5) - 0.5))) * step(0.64, r) * step(r, 0.82);
    float dot2 = exp(-r * r * 500.0);
    float haze = exp(-r * r * 3.5) * 0.14;
    float pulse = gs((r - Param.z) / 0.05) * (1.0 - Param.z) * step(0.001, Param.z);
    float beat = 0.85 + 0.15 * sin(T * 0.3);
    vec3 c = color.rgb * (ring * beat + outer * 0.7 + arcs * 0.8 + ticks * 0.8 + haze) + vec3(1.0) * (dot2 + pulse * 1.3 + ring * 0.25);
    c *= (1.0 - smoothstep(0.92, 1.0, r)) * Param.y * color.a;
    return vec4(c, 0.0);
}

void main() {
    int mode = int(Mode + 0.5);
    if (mode == 1) {
        fragColor = star();
    } else if (mode == 2) {
        fragColor = sun();
    } else if (mode == 3) {
        fragColor = beam();
    } else if (mode == 4) {
        fragColor = dome();
    } else if (mode == 5) {
        fragColor = shock();
    } else if (mode == 6) {
        fragColor = dust();
    } else if (mode == 7) {
        fragColor = fireball();
    } else if (mode == 8) {
        fragColor = mark();
    } else if (mode == 9) {
        float r = length(uv);
        float a = (exp(-r * r * Param.x) + Param.y * exp(-r * 3.5)) * (1.0 - smoothstep(0.75, 1.0, r));
        fragColor = vec4(color.rgb * a * color.a, 0.0);
    } else {
        fragColor = vec4(color.rgb * color.a, 0.0);
    }
    if (Bake > 0.5) {
        // the light alone, softly clipped into 0..1 (a texture can't hold more), and an alpha of 1 wherever there is
        // any: shader packs throw away what is nearly see-through
        vec3 c = max(fragColor.rgb, vec3(0.0));
        vec3 baked = 1.0 - exp(-c * 1.25);
        if (Bake > 1.5) {
            baked = sqrt(baked);
        }
        fragColor = vec4(baked, clamp(max(c.r, max(c.g, c.b)) * 50.0, 0.0, 1.0));
    }
}
