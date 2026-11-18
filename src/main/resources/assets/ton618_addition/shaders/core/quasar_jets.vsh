#version 330

// TA-02 Quasar Jets' film (client/quasarfilm): everything the film draws itself goes through this one program.
#moj_import <minecraft:projection.glsl>
// Minecraft 1.21.11 (Fabric 1.21.11 branch): the projection comes in Minecraft's Projection block, the rest in ours
// (client/quasarfilm/JetGl).
layout(std140) uniform JetParams {
    mat4 ModelViewMat;
    vec4 ModeVec;
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
