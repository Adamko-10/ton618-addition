#version 150

// TA-02 Quasar Jets' film: the passes that turn the space scene's high-range picture into what is shown (JetPost).
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 uv;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    uv = UV0;
}
