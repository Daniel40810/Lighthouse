// glass.vert — eine Scheibe des Laternenraums (Phase 8): Ecke in der Welt und Ort auf der Scheibe in m. Der Ort wird
// hier auf die Kachel der Tropfen gelegt (Versatz, links-rechts gespiegelt, je Scheibe anders).
#include "common.glsl"
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 aPane;
uniform vec2 uOff;
uniform float uFlip;
out vec3 vWorld;
out vec2 vTile;
out vec2 vPane;
void main() {
    vWorld = aPos;
    vPane = aPane;
    vTile = uOff + vec2(uFlip * aPane.x, aPane.y);
    gl_Position = uVP * vec4(aPos, 1.0);
}
