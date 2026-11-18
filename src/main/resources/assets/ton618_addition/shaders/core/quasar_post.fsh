#version 330

// TA-02 Quasar Jets' film: from the space scene's high-range picture to the screen (JetPost). What is brighter than
// white spills light round itself (bloom, built by halving the picture a few times and adding the halves back), then
// the whole range is brought into what a screen can show with a soft shoulder, so the quasar blazes without turning
// into a flat white patch. A pass per Mode; Param.xy is always one texel of Sampler0.
//  0 keep what is bright (Param.z: from how bright on), at half size
//  1 halve: a soft five-tap average
//  2 double: a soft nine-tap spread of the smaller picture (Sampler0, times Param.z) plus the larger one (Sampler1)
//  3 the final picture: the scene (Sampler0) plus its glow (Sampler1, times Param2.x), exposure Param2.y, darker
//    corners Param2.z, a breath of grain (Param2.w a clock)
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
// Minecraft 1.21.11 (Fabric 1.21.11 branch): the pass's values in one uniform block (client/quasarfilm/JetPost).
layout(std140) uniform PostParams {
    vec4 ModeVec;
    vec4 Param;
    vec4 Param2;
};
#define Mode (ModeVec.x)

in vec2 uv;

out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

void main() {
    int mode = int(Mode + 0.5);
    vec2 px = Param.xy;
    if (mode == 0) {
        vec3 c = (texture(Sampler0, uv + px * vec2(-0.5, -0.5)).rgb + texture(Sampler0, uv + px * vec2(0.5, -0.5)).rgb
            + texture(Sampler0, uv + px * vec2(-0.5, 0.5)).rgb + texture(Sampler0, uv + px * vec2(0.5, 0.5)).rgb) * 0.25;
        c = clamp(c, vec3(0.0), vec3(16.0));
        float l = max(c.r, max(c.g, c.b));
        fragColor = vec4(c * smoothstep(Param.z * 0.6, Param.z * 1.5, l), 1.0);
    } else if (mode == 1) {
        vec3 c = texture(Sampler0, uv).rgb * 4.0 + texture(Sampler0, uv + px * vec2(-1.0, -1.0)).rgb + texture(Sampler0, uv + px * vec2(1.0, -1.0)).rgb
            + texture(Sampler0, uv + px * vec2(-1.0, 1.0)).rgb + texture(Sampler0, uv + px * vec2(1.0, 1.0)).rgb;
        fragColor = vec4(c / 8.0, 1.0);
    } else if (mode == 2) {
        vec3 c = texture(Sampler0, uv + px * vec2(-2.0, 0.0)).rgb + texture(Sampler0, uv + px * vec2(2.0, 0.0)).rgb
            + texture(Sampler0, uv + px * vec2(0.0, -2.0)).rgb + texture(Sampler0, uv + px * vec2(0.0, 2.0)).rgb
            + (texture(Sampler0, uv + px * vec2(-1.0, -1.0)).rgb + texture(Sampler0, uv + px * vec2(1.0, -1.0)).rgb
            + texture(Sampler0, uv + px * vec2(-1.0, 1.0)).rgb + texture(Sampler0, uv + px * vec2(1.0, 1.0)).rgb) * 2.0;
        fragColor = vec4(c / 12.0 * Param.z + texture(Sampler1, uv).rgb, 1.0);
    } else {
        vec3 c = max(texture(Sampler0, uv).rgb, vec3(0.0)) + texture(Sampler1, uv).rgb * Param2.x;
        c = 1.0 - exp(-c * Param2.y);
        vec2 q = uv - 0.5;
        c *= 1.0 - Param2.z * dot(q, q);
        c += (hash(uv * 811.0 + fract(Param2.w) * 37.0) - 0.5) * 0.014;
        fragColor = vec4(c, 1.0);
    }
}
