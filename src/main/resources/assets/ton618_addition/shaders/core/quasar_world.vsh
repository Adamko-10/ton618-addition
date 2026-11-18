#version 330

// TA-02 Quasar Jets' world effects (client/quasar): the named stars, the second sun, the mark, the jet, the landing
// blast, the radio lobe. One program, a material per Mode (quasar_world.fsh).
#moj_import <minecraft:projection.glsl>
// Minecraft 1.21.11 (Fabric 1.21.11 branch): the projection comes in Minecraft's Projection block, the rest in ours
// (client/quasar/QuasarGl).
layout(std140) uniform QuasarParams {
    mat4 ModelViewMat;
    vec4 ModeBake;
    vec4 Param;
    vec4 Param2;
};

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 uv;
out vec4 color;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    uv = UV0;
    color = Color;
}
