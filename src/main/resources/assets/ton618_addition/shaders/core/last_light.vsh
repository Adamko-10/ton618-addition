#version 330
#extension GL_ARB_separate_shader_objects : require

// TA-01 Last Light's film (client/film): everything the film draws itself goes through this one program.
#include <minecraft:projection.glsl>
// Minecraft 26.3 (Fabric 26.3 branch): the projection comes in Minecraft's Projection block, the rest in ours.
layout(std140) uniform FilmParams {
    mat4 ModelViewMat;
    vec4 ModeVec;
    vec4 Param;
    vec4 Param2;
};

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;

layout(location = 0) out vec2 uv;
layout(location = 1) out vec4 color;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    uv = UV0;
    color = Color;
}
