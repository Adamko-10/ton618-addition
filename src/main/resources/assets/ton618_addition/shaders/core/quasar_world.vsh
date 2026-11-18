#version 150

// TA-02 Quasar Jets' world effects (client/quasar): the named stars, the second sun, the mark, the jet, the landing
// blast, the radio lobe. One program, a material per Mode (quasar_world.fsh).
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

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
