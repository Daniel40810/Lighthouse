// sea.frag — die leere See als Kugeloberfläche bis zur Kimm (Phase 1, Platzhalter bis com.dan.ocean in Phase 2).
// Der Sichtstrahl wird mit dem Meeresspiegel geschnitten, der um d²/2R unter die Waagerechte fällt; so liegt die Kimm
// an der richtigen Stelle (bei 20 m Augenhöhe 16 km entfernt, 0,14° unter der Waagerechten). Die Rechnung bleibt in
// kleinen Zahlen (kein Erdradius im Quadrat), damit float reicht. Wellen: wenige Sinuszüge in Windrichtung, nach
// Entfernung ausgeblendet, damit nichts flimmert. Optik: Fresnel, Himmelsspiegelung, Sonnenglitzer, Wasserfarbe
// der Nordsee, Dunst. Die Tiefe wird aus dem Schnittpunkt geschrieben (umgekehrte Tiefe), damit der Messpfahl richtig
// ins Wasser taucht.
#include "common.glsl"
in vec2 vUV;
out vec4 frag;
uniform vec2 uWindTo;      // wohin der Wind weht (x, z), Einheitsvektor
uniform float uWindSpeed;  // m/s
uniform int uOctaves;

void main() {
    vec3 dir = viewRay(vUV);
    float h = max(0.05, uCam.y - uLevel);
    float dy = dir.y;
    float dh2 = dot(dir.xz, dir.xz);
    float disc = dy * dy - 2.0 * h * dh2 / EARTH_R;
    if (dy >= 0.0 || disc < 0.0) discard;
    float t = 2.0 * h / (-dy + sqrt(disc));
    vec3 p = uCam + dir * t;

    // Grundnormale der Kugel (Mittelpunkt unter dem Turm)
    vec3 n = normalize(vec3(p.x / EARTH_R, 1.0, p.z / EARTH_R));
    // Platzhalter-Wellen: Steigung aus Sinuszügen, nach dem goldenen Winkel um die Windrichtung gefächert (±70°),
    // jeder Zug wird ausgeblendet, sobald er kleiner als ein paar Bildpunkte wird
    float sea = clamp(uWindSpeed / 12.0, 0.05, 1.3);
    vec2 dx = vec2(0.0);
    float footprint = t * 0.0015 / max(0.08, -dy);   // Größe eines Bildpunkts auf dem Wasser, längs gestreckt
    float L = 34.0 * (0.35 + sea);
    for (int i = 0; i < 16; i++) {
        if (i >= uOctaves * 2) break;
        float k = 6.2831853 / L;
        float ang = sin(float(i) * 2.39996) * 1.22;
        vec2 wd = mat2(cos(ang), -sin(ang), sin(ang), cos(ang)) * uWindTo;
        float omega = sqrt(9.81 * k);
        float A = L * 0.0075 * sea;
        float fade = 1.0 - smoothstep(L * 0.08, L * 0.35, footprint);
        float ph = k * dot(wd, p.xz) - omega * uTime + float(i) * 2.17;
        dx += wd * (A * k * cos(ph)) * fade;
        L *= 0.72;
    }
    n = normalize(n + vec3(-dx.x, 0.0, -dx.y));

    vec3 V = -dir;
    float ndv = max(dot(n, V), 0.0);
    float F = 0.02 + 0.98 * pow(1.0 - ndv, 5.0);
    vec3 r = reflect(dir, n);
    r.y = abs(r.y);
    vec3 refl = skyColor(r);
    vec3 tint = vec3(0.030, 0.105, 0.095);
    vec3 body = tint * (skyColor(vec3(0.0, 1.0, 0.0)) * 1.4 + sunLight() * max(uSun.y, 0.0) * 0.30);
    vec3 col = mix(body, refl, F);
    col += sunLight() * 8.0 * pow(max(dot(r, uSun), 0.0), 700.0) * F;
    // zur Kimm hin: Kanten weich, Dunst
    col = aerial(col, dir, t);
    frag = vec4(col, 1.0);
    gl_FragDepth = depthOf(p);
}
