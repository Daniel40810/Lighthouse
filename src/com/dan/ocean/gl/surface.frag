// surface.frag — Optik der See: Normale aus den Ableitungen aller Kaskaden und der Dünung, Fresnel mit Spiegelung des
// Himmels (Panorama-Textur), Streulicht im Wasser in Nordsee-Grün, Licht im Wellenkamm (durchscheinend gegen die
// Sonne), Sonnen- und Mondglitzer, Schaum aus Überschlag (mit Nachleuchten), Schaumstraßen ab Bft 7, Brandungsringe
// um Hindernisse, Ringwellen nach Einschlägen, Dunst zur Kimm, Wolkenschatten, Schatten eines Körpers und eine feine Störung wie Regenringe (alle
// optional). Ausgabe: HDR-Farbe, linear.
#include "ocean_common.glsl"
in vec2 vRest;
in vec3 vWorld;
in float vHeight;
in float vDist;
out vec4 frag;
uniform sampler2D uSky;        // Himmel als Panorama (Azimut von Nord, Höhe), linear
uniform float uSkyGain;
uniform vec3 uSunDir, uSunColor, uMoonDir, uMoonColor;
uniform vec3 uWaterColor, uSssColor;
uniform float uHs;
uniform float uFogDist;
uniform float uWindTo;
uniform float uWindSpeed;
uniform vec4 uObst[8];         // x, z, Radius
uniform int uObstCount;
uniform vec4 uRipple[32];      // x, z, Alter, Stärke
uniform int uRippleCount;
uniform float uFoamGain;
uniform sampler2D uCloudShadow;  // Anteil direkten Lichts von oben (Wolken), optional
uniform vec4 uShadowRect;       // x, z der Ecke, Kantenlänge in m, an (1) / aus (0)
uniform sampler2D uBodyShadow;   // Schatten eines Körpers (Turm, Schiff) auf dem Wasser, optional
uniform vec4 uBodyRect;         // wie uShadowRect
uniform sampler2D uDetail;      // feine Störung (Regenringe): Steigung in RG, kachelbar, optional
uniform vec4 uDetailP;          // Kachel in m, Stärke, Reichweite in m, an (1) / aus (0)
uniform sampler2D uPointTex;    // Punktlicht: Lichtstärke cd je Richtung vom Licht (Rundumbild), optional
uniform vec4 uPointPos;         // Ort des Punktlichts, Faktor (0 = aus)
uniform float uPointSigma;      // Auslöschung der Luft auf dem Weg zum Wasser (1/m)
uniform int uFlashN;            // einfache Punktlichter (Blitze): Zahl
uniform vec4 uFlashPos[8];      // Ort, Größe der Quelle (m)
uniform vec4 uFlashRgb[8];      // Lichtstärke RGB in cd · Vorbelichtung

// Lichtstärke des Punktlichts in Richtung w (vom Licht weg), mal Faktor
vec3 pointIntensity(vec3 w) {
    float az = atan(w.x, -w.z) / 6.28318530718;
    float el = degrees(asin(clamp(w.y, -1.0, 1.0)));
    return textureLod(uPointTex, vec2(fract(az + 1.0), 0.5 + 0.5 * sign(el) * sqrt(abs(el) / 90.0)), 0.0).rgb * uPointPos.w;
}

vec3 sky(vec3 d) {
    float az = atan(d.x, -d.z) / 6.28318530718 + 0.5;
    float el = asin(clamp(d.y, -1.0, 1.0)) / 3.14159265359 + 0.5;
    return textureLod(uSky, vec2(az, el), 0.0).rgb * uSkyGain;
}

float smoothr(float a, float b, float x) { return smoothstep(a, b, x); }

// Ringwellen: Steigung (dh/dx, dh/dz) aller Einschläge, Formel wie Ripples.ring()
vec2 rippleSlope(vec2 p) {
    vec2 g = vec2(0.0);
    for (int i = 0; i < 32; i++) {
        if (i >= uRippleCount) break;
        vec4 r = uRipple[i];
        vec2 dv = p - r.xy;
        float rr = length(dv), tau = r.z;
        if (rr > 0.9 * tau + 1.0 || rr < 1e-4) continue;
        float front = rr - 0.9 * tau, w = 0.15 + 0.12 * tau;
        float env = exp(-front * front / (w * w)) * exp(-tau / 1.4) / (1.0 + rr * 1.5);
        float k = 6.28318530718 / 0.35;
        float dh = r.w * 0.06 * env * (-k * sin(k * front) - 2.0 * front / (w * w) * cos(k * front));
        g += dh * dv / rr;
    }
    return g;
}

void main() {
    // Wolkenschatten (aus com.dan.cloud über die App): Anteil direkten Lichts an dieser Stelle
    float shade = 1.0;
    if (uShadowRect.w > 0.0) shade = textureLod(uCloudShadow, (vWorld.xz - uShadowRect.xy) / uShadowRect.z, 0.0).r;
    if (uBodyRect.w > 0.0) {
        vec2 bu = (vWorld.xz - uBodyRect.xy) / uBodyRect.z;
        if (all(greaterThan(bu, vec2(0.0))) && all(lessThan(bu, vec2(1.0)))) shade *= textureLod(uBodyShadow, bu, 0.0).r;
    }
    vec3 sunC = uSunColor * shade, moonC = uMoonColor * shade;
    // Hindernisse: innen kein Wasser, außen ein Brandungsring
    float ring = 0.0;
    for (int i = 0; i < 8; i++) {
        if (i >= uObstCount) break;
        float ds = length(vWorld.xz - uObst[i].xy) - uObst[i].z;
        if (ds < 0.0) discard;
        float width = 0.5 + 0.8 * uHs;
        float n = vnoise(vec2(vWorld.x * 0.8 + uTime * 0.35, vWorld.z * 0.8 - uTime * 0.2));
        ring = max(ring, smoothr(0.15, 1.6, uHs) * exp(-ds / width) * (0.45 + 0.55 * n));
    }
    // Ableitungen und Schaum aller Kaskaden
    float sx = 0.0, sz = 0.0, jx = 0.0, jz = 0.0, keep = 1.0;
    for (int c = 0; c < 4; c++) {
        if (c >= uCascades) break;
        vec3 uv = vec3(vRest / uLength[c], float(c));
        vec4 dv = texture(uDeriv, uv);
        sx += dv.x; sz += dv.y; jx += dv.z; jz += dv.w;
        keep *= 1.0 - texture(uDisp, uv).w;
    }
    vec3 swd, sws;
    swell(vRest, swd, sws);
    sx += sws.x; sz += sws.y;
    vec2 rs = rippleSlope(vWorld.xz);
    if (uDetailP.w > 0.0) rs += texture(uDetail, vWorld.xz / uDetailP.x).rg * uDetailP.y * exp(-vDist / uDetailP.z);
    vec3 n = normalize(vec3(-(sx / max(0.2, 1.0 + jx + 0.5 * sws.z) + rs.x), 1.0, -(sz / max(0.2, 1.0 + jz + 0.5 * sws.z) + rs.y)));

    vec3 V = normalize(uCam - vWorld);
    if (dot(n, V) < 0.02) n = normalize(n + V * (0.02 - dot(n, V)));
    float ndv = clamp(dot(n, V), 0.0, 1.0);
    float F = 0.02 + 0.98 * pow(1.0 - ndv, 5.0);
    vec3 r = reflect(-V, n);
    r.y = abs(r.y);
    vec3 refl = sky(r);

    // Wasserkörper: Streulicht aus Himmel und Sonne, im Kamm heller und türkis. Maßstab (Phase 3, physikalisches
    // Licht): Bestrahlung von oben E = Sonne · sin h + Himmel (≈ 3,5 · Zenit-Strahldichte); das Wasser gibt davon
    // rund 1 % je Steradiant zurück (Nordsee, grün), uWaterColor · 0,11.
    vec3 skyUp = sky(vec3(0.0, 1.0, 0.0));
    float sunUp = max(uSunDir.y, 0.0);
    vec3 eDown = skyUp * 3.5 + sunC * sunUp + moonC * max(uMoonDir.y, 0.0);
    vec3 body = uWaterColor * eDown * 0.11;
    float crest = smoothstep(0.25, 1.0, vHeight / max(0.4 * uHs, 0.05));
    vec3 Lh = normalize(vec3(uSunDir.x, 0.0, uSunDir.z) + vec3(0.0, 1e-4, 0.0));
    vec3 Vh = normalize(vec3(V.x, 0.0, V.z) + vec3(0.0, 1e-4, 0.0));
    float back = pow(clamp(dot(-Vh, Lh) * 0.5 + 0.5, 0.0, 1.0), 4.0);
    float grazing = pow(1.0 - clamp(V.y, 0.0, 1.0), 2.0);
    vec3 sss = uSssColor * crest * grazing * (sunC * back * sunUp * 0.9 + skyUp * 0.35) * 0.15;
    vec3 col = mix(body + sss, refl, F);

    // Glitzer: Sonne und Mond, rauer mit der Entfernung (die kleinen Wellen sind dort weggefiltert)
    float rough = clamp(0.012 + vDist * 0.00002, 0.012, 0.12);
    float a2 = rough * rough;
    vec3 Hs = normalize(uSunDir + V);
    float nh = max(dot(n, Hs), 0.0);
    float D = a2 / (3.14159265 * pow(nh * nh * (a2 - 1.0) + 1.0, 2.0));
    col += sunC * D * F * 0.25 * smoothstep(-0.02, 0.02, uSunDir.y);
    vec3 Hm = normalize(uMoonDir + V);
    float nm = max(dot(n, Hm), 0.0);
    float Dm = a2 / (3.14159265 * pow(nm * nm * (a2 - 1.0) + 1.0, 2.0));
    col += moonC * Dm * F * 0.25 * smoothstep(-0.02, 0.02, uMoonDir.y);
    // Punktlicht (Leuchtfeuer): Bestrahlung I/d² aus seiner Richtung, Glitzer wie Sonne und Mond
    vec3 pointE = vec3(0.0);
    vec3 Lp = vec3(0.0, 1.0, 0.0);
    if (uPointPos.w > 0.0) {
        vec3 tp = uPointPos.xyz - vWorld;
        float dp = max(length(tp), 0.5);
        Lp = tp / dp;
        pointE = pointIntensity(-Lp) / (dp * dp) * exp(-uPointSigma * dp);
        vec3 Hp = normalize(Lp + V);
        float np = max(dot(n, Hp), 0.0);
        float Dp = a2 / (3.14159265 * pow(np * np * (a2 - 1.0) + 1.0, 2.0));
        col += pointE * Dp * F * 0.25 + uWaterColor * pointE * max(Lp.y, 0.0) * 0.11;
    }
    // einfache Punktlichter (Blitze): Licht im Wasserkörper und auf dem Schaum; den Glanz zeigt die Spiegelung
    vec3 flashE = vec3(0.0);
    for (int i = 0; i < 8; i++) {
        if (i >= uFlashN) break;
        vec3 tf = uFlashPos[i].xyz - vWorld;
        float d2 = dot(tf, tf) + uFlashPos[i].w * uFlashPos[i].w;
        flashE += uFlashRgb[i].rgb * max(dot(n, normalize(tf)), 0.0) / d2 * exp(-uPointSigma * sqrt(dot(tf, tf)));
    }
    col += uWaterColor * flashE * 0.11;

    // Schaum: Überschlag aller Kaskaden, Straßen in Windrichtung, Brandungsringe; feine Struktur aus Rauschen
    float foam = (1.0 - keep) * uFoamGain;
    vec2 wd = vec2(sin(uWindTo), -cos(uWindTo));
    float along = dot(vRest, wd), across = dot(vRest, vec2(-wd.y, wd.x));
    float warp = vnoise(vec2(along / 23.0, across / 3.1)) * 1.6;
    float streak = smoothr(13.0, 17.5, uWindSpeed) * smoothr(0.58, 0.82, vnoise(vec2(along / 34.0 + warp, across / 0.9)) * 0.6 + vnoise(vec2(along / 9.0 - warp, across / 0.35)) * 0.4) * 0.75;
    foam = clamp(max(max(foam, streak), ring), 0.0, 1.0);
    // Struktur: in Windrichtung gestrecktes Rauschen; wenig Schaum zeigt nur die Spitzen (zerrissene Schaumköpfe),
    // viel Schaum deckt alles. In der Ferne wird die Struktur weich, damit nichts flimmert.
    vec2 fp = vec2(along * 0.55, across);
    float detail = vnoise(fp * 0.9) * 0.45 + vnoise(fp * 2.7 + 7.3) * 0.33 + vnoise(fp * 8.1 + 3.1) * 0.22;
    float soft = clamp(vDist / 400.0, 0.0, 1.0);
    float edge = mix(0.22, 0.6, soft);
    float level = 1.0 - min(foam, 0.8) * 0.9;
    float foamVis = mix(smoothstep(level, level + edge, detail), foam, soft * 0.7) * (0.45 + 0.45 * clamp(foam * 1.5, 0.0, 1.0));
    // Schaum als matte Fläche mit Albedo 0,6: Strahldichte = 0,6/π · Bestrahlung
    vec3 foamCol = vec3(0.93, 0.97, 1.0) * 0.6 / 3.14159265 * (sunC * max(dot(n, uSunDir), 0.0) + moonC * max(dot(n, uMoonDir), 0.0) + skyUp * 3.5 + pointE * max(dot(n, Lp), 0.0) + flashE);
    col = mix(col, foamCol, foamVis);

    // Dunst zur Kimm
    float f = 1.0 - exp(-vDist / uFogDist);
    vec3 fogCol = sky(normalize(vec3(-V.x, 0.0, -V.z) + vec3(0.0, 0.002, 0.0)));
    col = mix(col, fogCol, f);
    frag = vec4(col, 1.0);
}
