// sky.frag — Himmel als Verlauf (Phase 1, Platzhalter bis com.dan.sky in Phase 3).
// Eingaben: common.glsl. Raum: Sichtstrahl je Bildpunkt. Ausgabe: HDR-Farbe linear. Tiefe: keine (zuerst gezeichnet).
#include "common.glsl"
in vec2 vUV;
out vec4 frag;
void main() {
    vec3 d = viewRay(vUV);
    frag = vec4(skyColor(d), 1.0);
}
