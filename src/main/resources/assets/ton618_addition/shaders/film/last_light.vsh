#version 150

// TA-01 Last Light's film (client/film): everything the film draws itself goes through this one program.
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
