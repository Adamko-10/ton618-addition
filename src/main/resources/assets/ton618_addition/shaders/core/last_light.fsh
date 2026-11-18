#version 330

// TA-01 Last Light's film (client/film). One program, a material per Mode (all our own, nothing of TON 618's):
//  0 plain colour (Color)
//  1 a soft round dot (uv -1..1 across it; Color.rgb its colour, Color.a how strong, Param.x a gain past 1), added
//  2 our black hole on one camera-facing quad (uv -1..1): a thin disk seen nearly edge on, its far side bent up over
//    the shadow, a photon ring, the black shadow, and the Hawking glow (Param: the disk's turning, how bright the disk
//    is, the glow's heat 0-1, the glow's strength; Param2.x: how flat the disk looks). Premultiplied.
//  3 the time wave on the dark's floor (uv: blocks from its centre; Param: the wave's radius, how strong, how fast time
//    runs behind it 0-1, the dashes' travel; Param2.xy: the camera from the centre, Param2.z: the quad's half size), added
//  4 a wall of light (uv.y 0 at its foot to 1 at its top; Color), added
//  5 a thin ring (uv -1..1; Param.x: its inner edge 0-1; Param.y: a soft glow trailing inside it, Param.z: how far in
//    that reaches 0-1; Color), added
//  6 a far galaxy's smudge (uv already turned and squashed; Color.a how strong), added
//  7 a wide glow with a hot core (uv -1..1; Color; Param.x a gain), added
//  8 a streak (uv.x -1..1 across it; Color, which fades along it), added
//  9 a flare: a thin line of light across the quad and a shorter one up it (uv -1..1; Color; Param.x a gain), added
// Minecraft 1.21.11 (Fabric 1.21.11 branch): the parameters come in one uniform block (see client/film/FilmGl).
layout(std140) uniform FilmParams {
    mat4 ModelViewMat;
    vec4 ModeVec;
    vec4 Param;
    vec4 Param2;
};
#define Mode (ModeVec.x)

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

float h1(float n) {
    return fract(sin(n * 12.9898) * 43758.5453);
}

float gs(float x) {
    return exp(-x * x);
}

vec3 diskCol(float x) {
    return mix(vec3(0.85, 0.2, 0.03), vec3(1.0, 0.6, 0.2), smoothstep(0.0, 0.6, x)) + vec3(1.0, 0.9, 0.75) * smoothstep(0.55, 1.0, x) * 0.9;
}

vec3 heatCol(float h) {
    vec3 c = mix(vec3(0.9, 0.2, 0.05), vec3(1.0, 0.6, 0.2), smoothstep(0.0, 0.4, h));
    c = mix(c, vec3(1.0, 0.95, 0.85), smoothstep(0.4, 0.7, h));
    return mix(c, vec3(0.72, 0.86, 1.0), smoothstep(0.7, 1.0, h));
}

vec4 hole(vec2 p) {
    float R = 0.16;
    float d = length(p) + 0.00001;
    float arcR = d / R;
    vec2 q = vec2(p.x, p.y / Param2.x);
    float r = length(q) + 0.00001;
    float rr = r / R;
    float rin = 1.9;
    float rout = 5.2;
    float band = smoothstep(rin, rin * 1.18, rr) * (1.0 - smoothstep(rout * 0.5, rout, rr));
    float a2 = atan(q.y, q.x) + Param.x * (0.6 + 0.8 / rr);
    float sw = fbm(vec2(cos(a2), sin(a2)) * rr * 1.3 + vec2(rr * 2.2, 0.0));
    float inner = clamp(1.0 - (rr - rin) / (rout - rin), 0.0, 1.0);
    float dop = 1.0 + 0.55 * (-q.x / r);
    vec3 dc = diskCol(inner) * band * (0.3 + 1.1 * sw) * dop * (0.35 + 1.7 * inner * inner);
    float shadow = 1.0 - smoothstep(0.97, 1.03, arcR);
    float front = 1.0 - smoothstep(-0.01, 0.01, p.y);
    float diskVis = mix(1.0 - shadow, 1.0, front);
    float arc = smoothstep(1.02, 1.14, arcR) * (1.0 - smoothstep(1.25, 1.95, arcR));
    float topw = 0.5 + 0.5 * smoothstep(-0.4, 0.6, p.y / d);
    float a3 = atan(p.y, p.x) + Param.x * 0.6;
    float an = fbm(vec2(cos(a3), sin(a3)) * 2.3 + vec2(arcR * 2.0));
    vec3 ac = diskCol(0.8) * arc * topw * (0.45 + 0.9 * an) * (1.0 + 0.4 * (-p.x / d)) * (1.0 - front * shadow);
    float ring = gs((arcR - 1.04) / 0.025);
    vec3 col = (dc * diskVis + ac + vec3(1.0, 0.86, 0.62) * ring * 0.9) * Param.y;
    col += vec3(1.0, 0.5, 0.15) * exp(-d * d / (R * R * 26.0)) * 0.05 * Param.y * (1.0 - shadow);
    vec3 hot = heatCol(Param.z);
    float halo = exp(-max(arcR - 1.0, 0.0) / (0.3 + Param.z * 1.3));
    col += hot * (halo * (1.0 - shadow) * 0.7 + ring * 1.4) * Param.w;
    col *= 1.0 - smoothstep(0.8, 1.0, d);
    float a = max(shadow, clamp(max(col.r, max(col.g, col.b)), 0.0, 1.0));
    return vec4(col, a);
}

vec3 wave(vec2 dv) {
    float dc = length(dv);
    float wf = dc - Param.x;
    float wd = 1.5 + Param.x * 0.02;
    float front = exp(-wf * wf / (wd * wd));
    // a white-hot line in the middle of the front, so it still reads over TON618's bright disk
    float hot = exp(-wf * wf / (wd * wd * 0.12));
    float tail = wf < 0.0 ? exp(wf / (5.0 + Param.x * 0.1)) : 0.0;
    vec3 c = (vec3(1.0, 0.85, 0.65) * front * 1.3 + vec3(1.0, 0.97, 0.9) * hot + vec3(1.0, 0.5, 0.18) * tail * 0.4) * Param.y;
    // behind the wave: dashes of light rushing outward along the floor
    float hh = h1(floor(atan(dv.y, dv.x) * 30.0));
    float dash = fract(dc * 0.035 - Param.w * (0.6 + hh) + hh * 7.0);
    float st = step(0.7, hh) * smoothstep(0.0, 0.1, dash) * (1.0 - smoothstep(0.1, 0.55, dash));
    float dist = length(dv - Param2.xy);
    c += vec3(1.0, 0.66, 0.34) * st * Param.z * 0.8 * step(dc, Param.x) * exp(-dist * 0.02) * smoothstep(1.5, 6.0, dc);
    return c * (1.0 - smoothstep(Param2.z * 0.85, Param2.z, dc));
}

vec4 galaxy(float d) {
    vec3 c = mix(vec3(1.0, 0.94, 0.86), vec3(0.86, 0.78, 1.0), smoothstep(0.0, 0.18, d));
    c = mix(c, vec3(0.59, 0.67, 1.0), smoothstep(0.18, 0.6, d));
    float a = mix(0.9, 0.35, smoothstep(0.0, 0.18, d));
    a = mix(a, 0.08, smoothstep(0.18, 0.6, d));
    a = mix(a, 0.0, smoothstep(0.6, 1.0, d));
    return vec4(c, a);
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
        fragColor = vec4(wave(uv), 1.0);
    } else if (mode == 4) {
        fragColor = vec4(color.rgb * (color.a * pow(clamp(1.0 - uv.y, 0.0, 1.0), 2.5)), 1.0);
    } else if (mode == 5) {
        float a = smoothstep(Param.x - 0.02, Param.x + 0.01, g) * (1.0 - smoothstep(0.96, 1.0, g));
        float inside = clamp((g - Param.x + Param.z) / max(Param.z, 0.001), 0.0, 1.0);
        a += Param.y * inside * inside * step(g, Param.x);
        fragColor = vec4(color.rgb * (color.a * a), 1.0);
    } else if (mode == 6) {
        vec4 s = galaxy(g);
        fragColor = vec4(s.rgb * (s.a * color.a), 1.0);
    } else if (mode == 7) {
        float a = (exp(-g * g * 9.0) * 0.6 + exp(-g * 26.0) * 0.9) * (1.0 - smoothstep(0.75, 1.0, g));
        fragColor = vec4(color.rgb * (color.a * a * Param.x), 1.0);
    } else if (mode == 8) {
        fragColor = vec4(color.rgb * (color.a * (1.0 - abs(uv.x))), 1.0);
    } else {
        float across = exp(-abs(uv.y) * 60.0) * pow(max(1.0 - abs(uv.x), 0.0), 2.0);
        float upward = exp(-abs(uv.x) * 90.0) * pow(max(1.0 - abs(uv.y) * 2.2, 0.0), 2.0);
        fragColor = vec4(color.rgb * (color.a * (across + 0.6 * upward) * Param.x), 1.0);
    }
}
