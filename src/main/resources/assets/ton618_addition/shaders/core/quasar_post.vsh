#version 330

// TA-02 Quasar Jets' film: the passes that turn the space scene's high-range picture into what is shown (JetPost).
in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 uv;

void main() {
    // the quad already covers the picture, corner to corner (-1..1)
    gl_Position = vec4(Position.xy, 0.0, 1.0);
    uv = UV0;
}
