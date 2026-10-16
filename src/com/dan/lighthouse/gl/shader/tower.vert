// tower.vert — der Leuchtturm (Phase 5) und was um ihn steht. Ecken: Lage, Normale, Grundfarbe, Material (Art,
// Rostanfälligkeit, Rauheit, Porosität), Fläche (s, t, Kante oben, Plattenhöhe), Umgebungslicht.
#include "common.glsl"
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec3 aColor;
layout(location = 3) in vec4 aMat;
layout(location = 4) in vec4 aSurf;
layout(location = 5) in float aAo;
layout(location = 6) in vec4 aVisA;   // Phase 11, nur im Innenraum: Sichtbarkeit der Lampen 0..3
layout(location = 7) in vec4 aVisB;   // Lampen 4..6, Anteil des Fensterlichts
out vec3 vPos;
out vec3 vNormal;
out vec3 vColor;
flat out vec4 vMat;
out vec4 vSurf;
out float vAo;
out vec4 vVisA;
out vec4 vVisB;
void main() {
    vPos = aPos;
    vNormal = aNormal;
    vColor = aColor;
    vMat = aMat;
    vSurf = aSurf;
    vAo = aAo;
    vVisA = aVisA;
    vVisB = aVisB;
    gl_Position = uVP * vec4(aPos, 1.0);
}
