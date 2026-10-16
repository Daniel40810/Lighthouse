// glass.frag — die nasse Scheibe von innen (Phase 8): Hinter dem Glas liegt das fertige Bild der Szene (eine Kopie).
// Tropfen aus com.dan.wetglass brechen es und zeigen die Welt kopfüber, ihr Rand wirft das Licht zurück, Sonne und
// Himmel glänzen auf ihnen; Feintropfen machen die Scheibe milchig, der Film der Spuren weich. Dazu ein schwacher
// Widerschein des Raums im Glas. Außen kann Salz krusten, innen beschlägt das Glas (Phase 8b).
#include "common.glsl"
#include "/com/dan/wetglass/gl/wetglass.glsl"
#include "/com/dan/lightning/gl/lightning.glsl"
in vec3 vWorld;
in vec2 vTile;
in vec2 vPane;
out vec4 frag;
uniform sampler2D uScene;      // Kopie des HDR-Bilds mit Mipmaps
uniform vec2 uViewport;
uniform vec3 uPaneN;           // Normale der Scheibe nach außen
uniform vec3 uPaneT;           // +x der Kachel in der Welt (mit Spiegelung)
uniform float uLens;
uniform float uStrength;       // 0..1 (Regler)
uniform vec3 uRoom;            // Widerschein des Raums (vorbelichtet)
uniform float uFogStrength;    // 0 = Beschlag aus (Schalter), 1 = an
uniform float uLampGlow;       // Licht der Lampe im Raum (vorbelichtet): der Beschlag leuchtet warm
// Phase 13: Scheibenwischer der Schiffsbrücke. Drehpunkt (Ort auf der Scheibe in m), Blatt von r0 bis r1 (z = 0: keiner);
// halber Schwenkwinkel und Winkel des Blatts jetzt (rad, 0 = senkrecht); wie nass das Gewischte wieder ist (0..1)
uniform vec4 uWiper;
uniform vec2 uWiperArc;
uniform float uWiperWet;
void main() {
    vec2 suv = gl_FragCoord.xy / uViewport;
    vec4 s = wgSample(vTile);
    s.x *= uStrength;
    float wiped = 1.0;
    if (uWiper.w > 0.0) {
        vec2 pw = vPane - uWiper.xy;
        float rw = length(pw), aw = atan(pw.x, pw.y);
        if (rw > uWiper.z && rw < uWiper.w && abs(aw) < uWiperArc.x + 0.02) {
            // hinter dem Blatt frisch gewischt, je weiter weg, desto länger her (bis zur Nässe des letzten Strichs)
            float since = clamp(abs(aw - uWiperArc.y) / max(1e-3, 2.0 * uWiperArc.x), 0.0, 1.0);
            wiped = uWiperWet * smoothstep(0.0, 0.8, since) + 0.04;
        }
    }
    s.x *= wiped;
    s.y *= wiped;
    vec2 ax, ay;
    wgScreenAxes(vTile, uViewport, ax, ay);
    vec3 behind = textureLod(uScene, suv, 0.0).rgb;
    float cov = wgCover(s), rim = wgRim(s);
    vec2 duv = clamp(suv + wgOffset(s, ax, ay, uLens), vec2(0.001), vec2(0.999));
    vec3 img = textureLod(uScene, duv, 1.0).rgb;
    // Wasseroberfläche nach innen zum Auge: N_in = -uPaneN
    vec3 T = uPaneT, B = vec3(0.0, 1.0, 0.0), Nin = -uPaneN;
    vec3 n = wgNormal(s, T, B, Nin);
    vec3 V = normalize(uCam - vWorld);
    // Rand: Totalreflexion, dunkel, mit etwas Himmel
    vec3 sky = uSkyMode == 1 ? panoSample(vec3(0.0, 1.0, 0.0)) * 0.5 + panoSample(normalize(vec3(uPaneN.x, 0.15, uPaneN.z))) * 0.5 : vec3(0.5);
    vec3 drop = mix(img * 0.94, sky * 0.12 + img * 0.08, smoothstep(0.55, 0.95, rim) * (0.25 + 0.75 * wgRimDetail(vTile)));
    // Glanz der Sonne, die durch den Tropfen scheint (Gegenlicht), und ein Lichtpunkt vom Himmel
    vec3 L = uSun;
    float through = pow(max(dot(refract(-V, n, 0.75), L), 0.0), 60.0);
    drop += sunLight() * through * 0.4 * cov + sky * pow(1.0 - abs(dot(n, V)), 3.0) * 0.25;
    vec3 col = mix(behind, drop, cov);
    // Film und Feintropfen: weicher, milchiger
    col = mix(col, textureLod(uScene, suv, 3.0).rgb * 1.04, s.y * 0.25 * (1.0 - cov) * uStrength);
    col = mix(col, textureLod(uScene, suv, 4.0).rgb * 1.08 + sky * 0.04, wgHaze(vTile) * 0.7 * uStrength * wiped);
    // Salz außen: eine weiße, matte Kruste, vom Himmel und der Sonne beleuchtet (davor das Glas)
    vec3 crust = sky * 0.8 + sunLight() * max(dot(uPaneN, uSun), 0.0) * 0.08 + uRoom * 0.4 + ltgIrradiance(vWorld, uPaneN) * 0.6 / LTG_PI;
    col = mix(col, crust, wgSalt(vTile) * uStrength);
    // Beschlag innen: davor; das Bild verschwimmt, die Tröpfchen streuen Licht von draußen und aus dem Raum
    float fo = wgFog(vTile) * uFogStrength;
    vec3 veil = textureLod(uScene, suv, 6.0).rgb * 0.55 + sky * 0.3 + uRoom * 2.5 + vec3(1.0, 0.78, 0.45) * uLampGlow * 0.5
              + ltgIrradiance(vWorld) * 0.3 / LTG_PI;
    col = mix(col, veil, fo * 0.9);
    // Glas: ein schwacher Widerschein des Raums
    float ndv = abs(dot(V, uPaneN));
    col += uRoom * (0.04 + 0.96 * pow(1.0 - ndv, 5.0)) * 0.6;
    frag = vec4(col, 1.0);
}
