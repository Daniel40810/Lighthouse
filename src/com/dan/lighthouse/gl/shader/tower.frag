// tower.frag — Leuchtturm, Manschette, Steinschüttung, Peilpfahl, Alte Weser (Phase 5).
// Licht: Sonne oder Mond (das hellere wirft Schatten aus der Schattenkarte), Himmel als Umgebungslicht aus dem
// Rundumbild, von unten das Meer. Glanz nach GGX, Spiegelung des Himmels; nasse Flächen werden dunkler und glatter
// (com.dan.weathering), dadurch glänzen sie.
// Nieten und Plattenstöße sind keine Geometrie: Sie entstehen hier aus den Flächenkoordinaten und verblassen mit der
// Entfernung, bevor sie flimmern. Darüber legt com.dan.weathering Nässe, Rost, Algen und Salz.
#include "common.glsl"
#include "/com/dan/weathering/gl/weathering.glsl"
#include "/com/dan/beacon/gl/beacon.glsl"
#include "/com/dan/lightning/gl/lightning.glsl"
in vec3 vPos;
in vec3 vNormal;
in vec3 vColor;
flat in vec4 vMat;
in vec4 vSurf;
in float vAo;
in vec4 vVisA;
in vec4 vVisB;
out vec4 frag;
uniform sampler2DShadow uShadowMap;
uniform mat4 uLightVP;
uniform int uShadowOn;      // 1 = Schattenkarte gültig
uniform int uZeroOne;       // Tiefenbereich 0..1 (glClipControl)
uniform float uKeyIsSun;    // 1 = Sonne wirft den Schatten, 0 = Mond
uniform float uLampGlow;    // Leuchtdichte der Raumlampe im Laternenraum, vorbelichtet
uniform float uBeaconOn;    // 1 = das Feuer (com.dan.beacon) ist gebunden
uniform vec3 uMapScale;     // Umfang, Höhe und Unterkante der Nässekarte in m
uniform float uDetail;      // 1 = Nieten und Stöße zeichnen

const float PI = 3.14159265;

// ---------------------------------------------------------------- Phase 11: der Innenraum
uniform int uInterior;          // 1 = das Netz des Innenraums
uniform vec4 uStairA;           // Boden, Steigung, Azimut der ersten Stufe, Grad je Stufe
uniform vec4 uStairB;           // Auge, Innenwand, Stufendicke, Zahl der Stufen
uniform vec4 uStairC;           // Boden Dienstraum, Plattendicke, Wand Dienstraum, Decke
uniform vec2 uStairD;           // tragender Boden: Azimut von, Spanne
uniform int uInWinCount;
uniform vec4 uInWin[12];        // innere Mitte, rund (1) oder Rechteck (0)
uniform vec4 uInWinAx[12];      // Normale nach außen, Tiefe der Laibung
uniform vec4 uInWinS[12];       // Halbmesser bzw. halbe Breite, halbe Höhe, Fläche
uniform vec3 uInWinL[12];       // Strahldichte durchs Fenster (vorbelichtet)
uniform vec4 uInLamp[7];        // Ort der Glühlampen, Art (0 frei, 1 Wandleuchte, 2 Pendel)
uniform vec3 uInLampI;          // Lichtstärke je Lampe (vorbelichtet, 0 = aus)
uniform vec3 uInAmbS, uInAmbR;  // zurückgeworfenes Licht E/π in Schaft und Dienstraum (vorbelichtet)
uniform vec3 uInBulb;           // Leuchtdichte eines brennenden Kolbens (vorbelichtet)
uniform float uBaro;            // Zeigerwinkel des Barometers in Grad

bool inVolume(vec3 p) {
    float r = length(p.xz);
    // 2 cm über die Innenwand hinaus: so verschwindet auch der Streifen, wo ein Erker die Wand schneidet
    return p.y > uStairA.x - 0.04 && p.y < uStairC.w && r < (p.y < uStairC.x ? uStairB.y : uStairC.z) + 0.02;
}

// Verdecken Stufen oder der Boden des Dienstraums die Strecke a–b? (wie Innenraum.blocked in Java)
float stairBlocked(vec3 a, vec3 b) {
    float dy = b.y - a.y;
    if (abs(dy) < 1e-5) return 0.0;
    float y0 = min(a.y, b.y), y1 = max(a.y, b.y);
    for (int pass = 0; pass < 2; pass++) {
        float off = pass == 0 ? 0.0 : -uStairB.z;
        int j0 = max(0, int(ceil((y0 + 0.01 - uStairA.x - off) / uStairA.y)) - 1);
        int j1 = min(int(uStairB.w) - 1, int(floor((y1 - 0.01 - uStairA.x - off) / uStairA.y)) - 1);
        for (int j = j0; j <= j1 && j < j0 + 70; j++) {
            float t = (uStairA.x + uStairA.y * float(j + 1) + off - a.y) / dy;
            if (t <= 0.0 || t >= 1.0) continue;
            vec3 q = mix(a, b, t);
            float r = length(q.xz);
            if (r < uStairB.x || r > uStairB.y) continue;
            if (mod(degrees(atan(q.x, -q.z)) - uStairA.z - uStairA.w * float(j), 360.0) < uStairA.w) return 1.0;
        }
    }
    for (int k = 0; k < 2; k++) {
        float t = (uStairC.x - uStairC.y * float(k) - a.y) / dy;
        if (t <= 0.001 || t >= 0.999) continue;
        vec3 q = mix(a, b, t);
        float r = length(q.xz);
        if (r < uStairB.x || r > uStairC.z) continue;
        if (r > uStairB.y || mod(degrees(atan(q.x, -q.z)) - uStairD.x, 360.0) < uStairD.y) return 1.0;
    }
    return 0.0;
}

// Durchlass einer Laibung (Tiefe d, Halbmesser a) unter dem Winkel mit cos c zur Fensterachse
float tubePass(float c, float d, float a) {
    if (c <= 0.0) return 0.0;
    return clamp(1.0 - d * sqrt(max(0.0, 1.0 - c * c)) / c / (2.0 * a), 0.0, 1.0);
}

// Sonnenflecken: Fällt Sonne durch eines der Fenster auf p? (beide Ebenen der Laibung, weicher Rand der Sonnenscheibe)
vec3 sunThroughWindows(vec3 p, vec3 N) {
    vec3 s = uSun;
    float ndl = max(dot(N, s), 0.0);
    if (s.y <= 0.0 || ndl <= 0.0) return vec3(0.0);
    float acc = 0.0;
    for (int w = 0; w < uInWinCount; w++) {
        vec3 n = uInWinAx[w].xyz;
        float c = dot(n, s);
        if (c < 0.02) continue;
        vec3 ci = uInWin[w].xyz, co = ci + n * uInWinAx[w].w;
        if (dot(n, co - p) <= 0.0) continue;
        vec3 tg = vec3(-n.z, 0.0, n.x);
        float cover = 1.0;
        for (int k = 0; k < 2; k++) {
            vec3 cc = k == 0 ? ci : co;
            float t = dot(n, cc - p) / c;
            if (t <= 0.0) continue;
            vec3 q = p + s * t - cc;
            float u = dot(q, tg), v = q.y, soft = 0.004 + 0.0047 * t;
            vec4 S = uInWinS[w];
            cover *= uInWin[w].w > 0.5 ? 1.0 - smoothstep(S.x - soft, S.x + soft, length(vec2(u, v)))
                    : (1.0 - smoothstep(S.x - soft, S.x + soft, abs(u))) * (1.0 - smoothstep(S.y - soft, S.y + soft, abs(v)));
        }
        if (cover < 0.002) continue;
        float ti = max(dot(n, ci - p) / c, 0.0);
        if (ti > 0.03 && stairBlocked(p + N * 0.02, p + s * ti) > 0.5) continue;
        acc += cover;
    }
    return sunLight() * ndl * min(acc, 1.0);
}

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}

float vnoise(vec3 p) {
    vec3 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash13(i), hash13(i + vec3(1, 0, 0)), f.x), mix(hash13(i + vec3(0, 1, 0)), hash13(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(hash13(i + vec3(0, 0, 1)), hash13(i + vec3(1, 0, 1)), f.x), mix(hash13(i + vec3(0, 1, 1)), hash13(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}

float fbm3(vec3 p) { return (0.5 * vnoise(p) + 0.25 * vnoise(p * 2.07) + 0.125 * vnoise(p * 4.13) + 0.0625 * vnoise(p * 8.3)) / 0.9375; }

// Zifferblatt des Barometers: (s, t) in m in der Scheibe, 11 cm Halbmesser
vec3 dial(vec2 st, vec3 base) {
    float r = length(st) / 0.11, th = degrees(atan(st.x, st.y));
    vec3 c = base;
    // Bögen: Regen (960–990 hPa), Veränderlich, Schön (1030–1060)
    float band = step(0.56, r) * step(r, 0.66) * step(abs(th), 135.0);
    float hp = 960.0 + (th + 135.0) / 2.7;
    c = mix(c, hp < 990.0 ? vec3(0.18, 0.25, 0.42) : hp > 1030.0 ? vec3(0.55, 0.38, 0.08) : vec3(0.35, 0.33, 0.3), band * 0.85);
    // Teilstriche je 2 hPa, lang je 10 hPa
    float fw = fwidth(th) + 1e-3;
    float minor = 1.0 - smoothstep(0.0, 0.25 + fw, abs(fract((th + 135.0) / 5.4 + 0.5) - 0.5) * 5.4);
    float major = 1.0 - smoothstep(0.0, 0.5 + fw, abs(fract((th + 135.0) / 27.0 + 0.5) - 0.5) * 27.0);
    float ring = step(abs(th), 136.0) * (minor * step(0.8, r) * step(r, 0.9) + major * step(0.72, r) * step(r, 0.92));
    c = mix(c, vec3(0.02), clamp(ring, 0.0, 1.0));
    // Zeiger und Mittelknopf
    float a = radians(uBaro);
    float along = st.x * sin(a) + st.y * cos(a), across = abs(st.x * cos(a) - st.y * sin(a));
    float w = mix(0.0022, 0.0008, clamp(along / 0.09, 0.0, 1.0));
    if (along > -0.022 && along < 0.092 && across < w) c = vec3(0.015);
    if (r < 0.08) c = vec3(0.45, 0.32, 0.12);
    return c;
}

float ggx(vec3 N, vec3 V, vec3 L, float rough, float F0);

// Licht im Innenraum: Glühlampen, Himmel und Sonne durch die Fenster, zurückgeworfenes Licht
vec3 interiorShade(int kind, vec3 N, vec3 V, vec3 albedo, float rough) {
    if (kind == 15) {
        float on = step(1e-12, dot(uInBulb, vec3(1.0)));
        return on > 0.5 ? uInBulb : albedo * 0.6 * (uInAmbS + uInAmbR);
    }
    float F0 = 0.04;
    vec3 tint = vec3(1.0);
    if (kind == 9) { F0 = 0.6; tint = albedo / max(max(albedo.r, albedo.g), 1e-3); albedo *= 0.25; }
    if (kind == 11) {
        float big = fbm3(vPos * 1.3), stain = smoothstep(0.58, 0.82, fbm3(vPos * vec3(0.7, 0.35, 0.7) + 7.0));
        albedo *= 0.82 + 0.25 * big;
        albedo = mix(albedo, albedo * vec3(0.78, 0.66, 0.48), stain * 0.55);
        albedo *= 0.94 + 0.08 * vnoise(vPos * 90.0);
    } else if (kind == 12) {
        albedo *= (0.82 + 0.3 * vnoise(vPos * 70.0)) * (0.88 + 0.24 * fbm3(vPos * 3.0));
    } else if (kind == 13) {
        albedo *= 0.78 + 0.45 * vnoise(vPos * vec3(9.0, 55.0, 9.0) + vec3(0.0, sin(vPos.x * 3.0) * 2.0, 0.0));
    } else if (kind == 16) {
        albedo = dial(vSurf.xy, albedo);
    }
    float yb = smoothstep(uStairC.x - 0.8, uStairC.x + 0.4, vPos.y);
    vec3 amb = mix(uInAmbS, uInAmbR, yb) * vAo;
    float vis[7] = float[7](vVisA.x, vVisA.y, vVisA.z, vVisA.w, vVisB.x, vVisB.y, vVisB.z);
    vec3 E = vec3(0.0), spec = vec3(0.0);
    for (int l = 0; l < 7; l++) {
        vec3 d = uInLamp[l].xyz - vPos;
        float d2 = dot(d, d);
        vec3 L = d * inversesqrt(d2);
        // Fassung und Wandplatte schirmen nach hinten ab (w = 1: Wandleuchte, 2: Pendel mit Schirm nach unten)
        float kind = uInLamp[l].w, shield = 1.0;
        if (kind > 0.5) {
            vec3 front = kind < 1.5 ? vec3(-normalize(uInLamp[l].xz).x, 0.0, -normalize(uInLamp[l].xz).y) : vec3(0.0, -1.0, 0.0);
            shield = mix(0.12, 1.0, smoothstep(-0.35, 0.45, dot(-L, front)));
        }
        float ndl = max(dot(N, L), 0.0), e = shield * vis[l] / (d2 + 0.0025);
        E += uInLampI * ndl * e;
        spec += uInLampI * (e * ndl) * ggx(N, V, L, max(rough, 0.12), F0);
    }
    vec3 Ew = vec3(0.0);
    for (int w = 0; w < uInWinCount; w++) {
        vec3 n = uInWinAx[w].xyz, co = uInWin[w].xyz + n * uInWinAx[w].w, d = co - vPos;
        float d2 = dot(d, d);
        vec3 L = d * inversesqrt(d2);
        float cw = dot(n, L), cp = dot(N, L);
        if (cw <= 0.0 || cp <= 0.0) continue;
        vec4 S = uInWinS[w];
        Ew += uInWinL[w] * S.z * cw * cp / (d2 + S.z) * tubePass(cw, uInWinAx[w].w, min(S.x, S.y));
    }
    E += Ew * vVisB.w + sunThroughWindows(vPos, N);
    return albedo * (E / PI + amb) + spec * tint;
}

float shadowAt(vec3 p, vec3 N, vec3 L) {
    if (uShadowOn == 0) return 1.0;
    float slope = 1.0 - max(dot(N, L), 0.0);
    vec4 lp = uLightVP * vec4(p + N * (0.03 + 0.06 * slope), 1.0);
    vec3 c = lp.xyz / lp.w;
    c.xy = c.xy * 0.5 + 0.5;
    if (uZeroOne == 0) c.z = c.z * 0.5 + 0.5;
    if (any(lessThan(c, vec3(0.0))) || any(greaterThan(c, vec3(1.0)))) return 1.0;
    vec2 texel = 1.0 / vec2(textureSize(uShadowMap, 0));
    float s = 0.0;
    for (int i = -1; i <= 1; i++)
        for (int j = -1; j <= 1; j++) s += texture(uShadowMap, vec3(c.xy + vec2(i, j) * texel * 1.2, c.z - 0.0004));
    return s / 9.0;
}

// Nieten und Plattenstöße: Platten 1,6 m breit, versetzt; zwei Nietreihen unter jedem waagerechten Stoß, eine Reihe
// neben jedem senkrechten. Gibt die gestörte Normale und eine leichte Verschattung in der Fuge zurück.
vec3 plates(vec3 N, vec2 st, float edge, float seam, float y, out float groove) {
    groove = 1.0;
    if (seam <= 0.0 || abs(N.y) > 0.85 || uDetail < 0.5) return N;
    float fw = max(fwidth(st.x), fwidth(y));
    float lod = 1.0 - smoothstep(0.006, 0.02, fw);       // Nieten
    float lodSeam = 1.0 - smoothstep(0.015, 0.07, fw);  // Stöße bleiben weiter sichtbar
    if (lodSeam <= 0.0) return N;
    vec3 T = normalize(cross(vec3(0.0, 1.0, 0.0), N));
    vec3 U = cross(N, T);
    float d = edge - y;
    float row = floor(d / seam);
    float dv = d - row * seam;
    float sx = st.x + row * 0.8;
    float ds = mod(sx, 1.6);
    vec3 n = N;
    // waagerechter Stoß: Unterkante der äußeren Platte, eine Stufe nach unten
    float hb = 1.0 - smoothstep(0.004, 0.012, dv);
    float hbw = 1.0 - smoothstep(0.004, 0.012 + fw, dv);
    n += -U * 0.9 * hb * lodSeam;
    groove *= 1.0 - 0.35 * hbw * lodSeam;
    // senkrechter Stoß
    float vb = 1.0 - smoothstep(0.004, 0.012, ds);
    float vbw = 1.0 - smoothstep(0.004, 0.012 + fw, ds);
    n += -T * 0.7 * vb * lodSeam;
    groove *= 1.0 - 0.25 * vbw * lodSeam;
    // Nieten: halbe Kugeln, Halbmesser 1,7 cm
    const float R = 0.017;
    vec2 q1 = vec2(mod(st.x, 0.14) - 0.07, -(dv - 0.04));
    vec2 q2 = vec2(mod(st.x + 0.07, 0.14) - 0.07, -(dv - 0.085));
    vec2 q3 = vec2(-(ds - 0.045), mod(y, 0.14) - 0.07);
    vec2 q = dot(q1, q1) < dot(q2, q2) ? q1 : q2;
    if (dot(q3, q3) < dot(q, q)) q = q3;
    float r2 = dot(q, q) / (R * R);
    if (r2 < 1.0 && lod > 0.0) {
        float h = sqrt(1.0 - r2);
        n = normalize(mix(N, normalize(N * h + T * q.x / R + U * q.y / R), lod));
        groove *= mix(1.0, 0.92 + 0.08 * h, lod);
    }
    return normalize(n);
}

// GGX mit Schlick-Fresnel und Smith-Sichtbarkeit; gibt das Glanzlicht (ohne Lichtfarbe) zurück.
float ggx(vec3 N, vec3 V, vec3 L, float rough, float F0) {
    vec3 H = normalize(L + V);
    float ndl = max(dot(N, L), 0.0), ndv = max(dot(N, V), 1e-3), nh = max(dot(N, H), 0.0), vh = max(dot(V, H), 0.0);
    float a = max(rough * rough, 0.002), a2 = a * a;
    float D = a2 / (PI * pow(nh * nh * (a2 - 1.0) + 1.0, 2.0));
    float k = a * 0.5;
    float G = ndl / (ndl * (1.0 - k) + k) * ndv / (ndv * (1.0 - k) + k);
    float F = F0 + (1.0 - F0) * pow(1.0 - vh, 5.0);
    return D * G * F / (4.0 * ndv) ;
}

// Himmel in Richtung r; unter der Kimm das dunkle Meer.
vec3 envAt(vec3 r) {
    if (r.y >= 0.0) return skyColor(r);
    vec3 up = skyColor(vec3(0.0, 1.0, 0.0));
    return mix(skyColor(normalize(vec3(r.x, 0.02, r.z))), up * 0.06, smoothstep(0.0, 0.25, -r.y));
}

void main() {
    int kind = int(vMat.x + 0.5);
    vec3 N = normalize(vNormal);
    if (!gl_FrontFacing) N = -N;
    vec3 V = normalize(uCam - vPos);
    float dist = length(uCam - vPos);
    vec3 albedo = vColor;
    float rough = vMat.z, porosity = vMat.w, sus = vMat.y;
    float groove = 1.0;
    // Phase 11: Teile des äußeren Netzes, die in den Innenraum ragen (Erker, Konsolen), und die Rückseiten der
    // Fensterscheiben verschwinden; das Innere zeichnet ein eigenes Netz.
    if (uInterior == 0 && inVolume(vPos)) discard;
    if (kind == 2 && vPos.y < uStairC.w + 0.04 && dot(vNormal.xz, vPos.xz) < 0.0 && dot(vPos.xz, vPos.xz) > 4.0 && dot(vPos.xz, vPos.xz) < 36.0) discard;
    if (uInterior == 1) {
        vec3 c = interiorShade(kind, N, V, albedo, rough);
        frag = vec4(aerial(c, -V, dist), 1.0);
        return;
    }
    if (kind == 0) N = plates(N, vSurf.xy, vSurf.z, vSurf.w, vPos.y, groove);
    // Laternenraum (Phase 8): innen trocken, Dielen aus Brettern, Messing glänzt
    bool inside = dot(vPos.xz, vPos.xz) < 1.83 * 1.83 && vPos.y > 22.9 && vPos.y < 27.8 && kind != 2;
    if (kind == 8) {
        float plank = floor(vPos.x / 0.12);
        float tone = fract(sin(plank * 12.9898) * 43758.55);
        float grain = sin(vPos.z * 40.0 + tone * 20.0 + sin(vPos.z * 3.0 + plank) * 2.0) * 0.5 + 0.5;
        albedo *= (0.75 + 0.45 * tone) * (0.9 + 0.2 * grain);
        albedo *= 1.0 - 0.5 * smoothstep(0.455, 0.49, abs(fract(vPos.x / 0.12) - 0.5));
    }
    // Verwitterung (nicht für die Alte Weser in 3,8 km)
    bool far = dot(vPos.xz, vPos.xz) > 1.0e4;
    float wet = 0.0;
    vec4 masks = vec4(0.0);
    if (!far) {
        float below = vSurf.w > 0.0 ? mod(vSurf.z - vPos.y, vSurf.w) : vSurf.z - vPos.y;
        bool onTower = dot(vPos.xz, vPos.xz) < 36.0 && kind != 4;
        vec2 mapUV = onTower ? vec2(atan(vPos.x, -vPos.z) / 6.28318530718 + 0.5, (vPos.y - uMapScale.z) / uMapScale.y) : vec2(-1.0);
        if (inside) wet = 0.0;
        else if (kind != 2 && kind != 7) wet = wthApply(albedo, rough, porosity, vPos, N, vSurf.xy, below, sus, mapUV, masks);
        else wet = wthWetness(vPos.y, N, vSurf.xy, wthMapAt(mapUV));
    }
    float F0 = kind == 2 ? 0.04 : kind == 7 ? 0.05 : 0.04;
    vec3 tint = vec3(1.0);
    if (kind == 9) { F0 = 0.55; tint = albedo / max(max(albedo.r, albedo.g), 1e-3); albedo *= 0.25; }
    if (kind == 10) F0 = 0.09;
    if (kind == 2) rough = mix(0.04, 0.02, wet);

    // Licht
    float keySun = uKeyIsSun;
    vec3 Lk = keySun > 0.5 ? uSun : uMoonDir;
    float sh = shadowAt(vPos, normalize(vNormal), Lk);
    vec3 sunC = sunLight() * (keySun > 0.5 ? sh : 1.0);
    vec3 moonC = moonLight() * (keySun > 0.5 ? 1.0 : sh);
    float ndlS = max(dot(N, uSun), 0.0), ndlM = max(dot(N, uMoonDir), 0.0);
    // Umgebung: Himmel von oben und von der Seite, Meer von unten
    vec3 skyAmb = mix(skyColor(vec3(0.0, 1.0, 0.0)), skyColor(normalize(vec3(N.x, 0.08, N.z) + vec3(0.0, 1e-4, 0.0))), 0.5) * 1.1;
    vec3 seaAmb = skyColor(vec3(0.0, 1.0, 0.0)) * 0.25 + (sunLight() * max(uSun.y, 0.0)) * 0.05;
    vec3 amb = mix(seaAmb, skyAmb, smoothstep(-0.7, 0.5, N.y)) * vAo;
    // Sonne und Mond kommen als Bestrahlung (wie im Meer), daher /π; der Himmel als mittlere Strahldichte
    vec3 col = albedo * groove * ((sunC * ndlS + moonC * ndlM) / PI + amb);
    // Glanz: Gestirne und Himmel
    col += (sunC * ndlS * ggx(N, V, uSun, rough, F0) + moonC * ndlM * ggx(N, V, uMoonDir, rough, F0)) * tint;
    // Phase 10: Blitze (lightning.glsl): Licht und Glanz ohne Schatten; im Laternenraum durch das Glas gedämpft
    if (ltgCount > 0) {
        float through = inside ? 0.75 : 1.0;
        col += (albedo * groove * ltgIrradiance(vPos, N) / PI + ltgSpecular(vPos, N, V, max(rough, 0.04)) * tint) * through;
    }
    vec3 R = reflect(-V, N);
    float ndv = max(dot(N, V), 0.0);
    float Fe = F0 + (1.0 - F0) * pow(1.0 - ndv, 5.0);
    col += envAt(R) * Fe * pow(1.0 - rough, 2.0) * vAo * (kind == 2 ? 1.0 : 0.8) * tint * (inside && kind != 9 ? 0.4 : 1.0);
    // Phase 9: das Feuer. Von außen durch das Glas: die Linse, wo der Blick sie trifft, sonst der Raum im Licht der
    // Linse; innen fällt das Nahfeld der Linse (Streifen der Prismenringe, Lichtring auf den Dielen, bei der Drehlinse
    // wandernde Flecken) auf jede Fläche, auch auf Handlauf und Geländer der Galerie.
    vec3 wEye = normalize(uCam - bcnPos.xyz);
    if (kind == 2) {
        bool lantern = vPos.y > 23.4 && vPos.y < 25.7 && dot(vPos.xz, vPos.xz) < 4.5;
        col += vec3(1.0, 0.78, 0.42) * uLampGlow * (lantern ? 1.0 : 0.15) * (1.0 - Fe);
        if (lantern && uBeaconOn > 0.5 && dot(uCam.xz, uCam.xz) > 3.6) {
            vec3 d = -V, o = vPos - bcnPos.xyz;
            float a = dot(d.xz, d.xz), b = dot(o.xz, d.xz), c = dot(o.xz, o.xz) - bcnLens.x * bcnLens.x, disc = b * b - a * c;
            float tl = disc > 0.0 && a > 1e-9 ? (-b - sqrt(disc)) / a : -1.0;
            float yl = o.y + d.y * tl;
            if (tl > 0.0 && abs(yl) < bcnLens.y) {
                float ring = 0.75 + 0.25 * cos(yl / (2.0 * bcnLens.y / 25.0) * 6.2831853);
                col += bcnLensLuminance(wEye) * ring * (1.0 - Fe);
            } else {
                col += bcnNearIrradiance(vPos - N * 0.03, N) * 0.05 / PI * (1.0 - Fe);
            }
        }
    }
    if (kind == 6) col += vec3(1.0, 0.78, 0.42) * uLampGlow + (uBeaconOn > 0.5 ? bcnTint.rgb * bcnLens.w * bcnPos.w / (4.0 * PI * PI * 0.0081) * bcnPre : vec3(0.0));
    if (kind == 10) {
        col += vec3(1.0, 0.8, 0.5) * uLampGlow * (0.35 + 0.65 * pow(abs(N.y), 0.5)) * 0.3;
        if (uBeaconOn > 0.5) {
            float ring = 0.75 + 0.25 * cos((vPos.y - bcnPos.y) / (2.0 * bcnLens.y / 25.0) * 6.2831853);
            col += bcnLensLuminance(wEye) * ring;
        }
    }
    if (uBeaconOn > 0.5 && kind != 2 && kind != 6 && kind != 10 && !far && dot(vPos.xz, vPos.xz) < 4.6 * 4.6 && abs(vPos.y - bcnPos.y) < 4.0)
        col += albedo * groove / PI * (bcnNearIrradiance(vPos, N)
                // im Laternenraum: das Licht, das von Wänden, Glas und Boden zurückgeworfen wird (15 % auf rund 35 m²)
                + (inside ? bcnTint.rgb * bcnLens.w * bcnPos.w * 0.15 / 35.0 * bcnPre : vec3(0.0)));
    vec3 dir = (vPos - uCam) / max(dist, 1e-3);
    frag = vec4(aerial(col, dir, dist), 1.0);
}
