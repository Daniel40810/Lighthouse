// tower_cast.vert — Schatten des Turms auf dem Wasser: jede Ecke entlang des Lichts auf die Wasserfläche gelegt und
// ins Rechteck der Schattenkarte gesetzt (von oben gesehen). Teile unter Wasser fallen auf ihren Grundriss.
layout(location = 0) in vec3 aPos;
uniform vec3 uLightDir;   // zum Gestirn
uniform float uLevel;
uniform vec4 uRect;       // x, z der Ecke, Kantenlänge in m
uniform int uZeroOne;     // Tiefenbereich 0..1 (glClipControl)
void main() {
    float h = max(aPos.y - uLevel, 0.0);
    vec2 xz = aPos.xz - uLightDir.xz * (h / max(uLightDir.y, 0.03));
    vec2 uv = (xz - uRect.xy) / uRect.z;
    gl_Position = vec4(uv * 2.0 - 1.0, uZeroOne == 1 ? 0.5 : 0.0, 1.0);
}
