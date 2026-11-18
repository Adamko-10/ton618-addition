#version 330
#extension GL_ARB_separate_shader_objects : require

// TA-02 Quasar Jets' film: the passes that turn the space scene's high-range picture into what is shown (JetPost).
layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;

layout(location = 0) out vec2 uv;

void main() {
    // the quad already covers the picture, corner to corner (-1..1)
    gl_Position = vec4(Position.xy, 0.0, 1.0);
    uv = UV0;
}
