// tower_shadow.vert — Schattenkarte des Turms aus Richtung des Gestirns (nur Tiefe). Glas wirft keinen Schatten.
layout(location = 0) in vec3 aPos;
layout(location = 3) in vec4 aMat;
uniform mat4 uLightVP;
flat out int vKind;
void main() { vKind = int(aMat.x + 0.5); gl_Position = uLightVP * vec4(aPos, 1.0); }
