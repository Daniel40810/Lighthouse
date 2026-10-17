// ocean_common.glsl — gemeinsame Werte der Oberflächen-Shader: Kaskaden, Dünung, Ringwellen, Rauschen, Himmel.
// Formeln wie in com.dan.ocean (Swell, Ripples, Ocean.streaks/surfRing), damit Bild und Abfragen übereinstimmen.
uniform mat4 uVP;
uniform vec3 uCam;
uniform vec2 uCenter;          // Mittelpunkt des Ringgitters (auf 0,25 m gerastert)
uniform float uTime;           // Zeit der See modulo Wiederholzeit
uniform float uLevel;          // Wasserstand
uniform int uCascades;
uniform float uLength[4];
uniform sampler2DArray uDisp;  // Dx, Dy, Dz, Schaum
uniform sampler2DArray uDeriv; // ∂h/∂x, ∂h/∂z, ∂Dx/∂x, ∂Dz/∂z
uniform vec4 uSwellA[4];       // Richtung x, z, k, ω
uniform vec4 uSwellB[4];       // A, Q, φ₀, –
uniform int uReversed;
uniform sampler2D uBed;        // seit Haithabu: Höhe des Grunds, optional
uniform vec4 uBedRect;         // x, z der Nordwestecke, Kantenlänge (m), an (1) / aus (0)
uniform vec3 uBedMap;          // Höhe = Wert · x + y; z: Trübung je m

// Tiefe des Wassers über dem Grund am Ort p (groß, wenn kein Grund gesetzt; −1, wenn außerhalb des Rechtecks)
float bedDepth(vec2 p) {
    if (uBedRect.w <= 0.0) return 1.0e4;
    vec2 uv = (p - uBedRect.xy) / uBedRect.z;
    if (any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)))) return -1.0;
    return uLevel - (textureLod(uBed, uv, 0.0).r * uBedMap.x + uBedMap.y);
}
const float EARTH_R = 6371000.0;

// Dünung am Ruhepunkt p: Auslenkung (xyz) und Steigungen dy/dx0, dy/dz0, Jacobi-Anteil
void swell(vec2 p, out vec3 d, out vec3 slope) {
    d = vec3(0.0);
    slope = vec3(0.0);
    for (int i = 0; i < 4; i++) {
        float a = uSwellB[i].x;
        if (a == 0.0) continue;
        vec2 dir = uSwellA[i].xy;
        float k = uSwellA[i].z, w = uSwellA[i].w, q = uSwellB[i].y;
        float ph = k * dot(dir, p) - w * uTime + uSwellB[i].z;
        float s = sin(ph), c = cos(ph);
        d += vec3(-q * a * dir.x * s, a * c, -q * a * dir.y * s);
        slope += vec3(-a * k * dir.x * s, -a * k * dir.y * s, -q * a * k * c);
    }
}

float hash21(vec2 p) { return fract(sin(p.x * 127.1 + p.y * 311.7) * 43758.5453); }

float vnoise(vec2 p) {
    vec2 i = floor(p), f = p - i;
    f = f * f * (3.0 - 2.0 * f);
    float a = hash21(i), b = hash21(i + vec2(1.0, 0.0)), c = hash21(i + vec2(0.0, 1.0)), d = hash21(i + vec2(1.0, 1.0));
    return a + (b - a) * f.x + (c - a) * f.y + (a - b - c + d) * f.x * f.y;
}
