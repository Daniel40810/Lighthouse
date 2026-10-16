// rail_wet.frag — der runde Handlauf der Galerie, nass (Phase 8b): dieselbe Simulation wie auf den Scheiben, auf einem
// liegenden Zylinder (com.dan.wetglass, F10). Der Handlauf ist schon gezeichnet; hier kommen Tropfen, nasser Lack und
// Salz darüber. Der Ort wird abgewickelt: x um das Rohr herum (oben 0, wachsend zur Seite, wohin der Wind weht), y
// entlang des Rings. Auf Lack bricht ein Tropfen nichts, er spiegelt Himmel und See und glänzt.
#include "common.glsl"
#include "/com/dan/wetglass/gl/wetglass.glsl"
in vec3 vPos;
in vec3 vNormal;
in vec3 vColor;
flat in vec4 vMat;
in vec4 vSurf;
in float vAo;
out vec4 frag;
uniform vec4 uRail;        // Halbmesser des Rings, Höhe der Achse, Halbmesser des Rohrs, Wind (Grad, woher)
uniform float uStrength;
void main() {
    vec3 P = vPos;
    float phi = atan(P.x, -P.z);
    vec3 radial = vec3(sin(phi), 0.0, -cos(phi)), along = vec3(cos(phi), 0.0, sin(phi)), upv = vec3(0.0, 1.0, 0.0);
    float rho = length(P.xz) - uRail.x, up = P.y - uRail.y;
    // auf der Windseite weht der Wind zum Turm hin, im Lee vom Turm weg
    bool luv = cos(phi - radians(uRail.w)) >= 0.0;
    vec3 sdir = luv ? -radial : radial;
    float th = atan(luv ? -rho : rho, up);
    vec2 p = vec2(mod(th, 6.2831853) * uRail.z, (phi + 3.14159265) * uRail.x);
    vec3 T = cos(th) * sdir - sin(th) * upv, B = along, N = sin(th) * sdir + cos(th) * upv;
    vec4 s = wgSample(p);
    s.x *= uStrength;
    float cov = wgCover(s), rim = wgRim(s);
    vec3 n = wgNormal(s, T, B, N);
    vec3 V = normalize(uCam - P);
    vec3 sunC = sunLight();
    vec3 skyAmb = mix(skyColor(vec3(0.0, 1.0, 0.0)), skyColor(normalize(vec3(n.x, 0.08, n.z) + vec3(0.0, 1e-4, 0.0))), 0.5) * 1.1;
    vec3 seaAmb = skyColor(vec3(0.0, 1.0, 0.0)) * 0.25;
    // Wasser über dem Lack: was darunter liegt, beleuchtet über die Normale des Tropfens, dazu Spiegelung und Glanz
    vec3 amb = mix(seaAmb, skyAmb, smoothstep(-0.7, 0.5, n.y)) * vAo;
    vec3 under = vColor * (sunC * max(dot(n, uSun), 0.0) / 3.14159265 + amb);
    float fw = 0.02 + 0.98 * pow(1.0 - max(dot(n, V), 0.0), 5.0);
    vec3 drop = mix(under, skyColor(reflect(-V, n)), clamp(fw * 1.4, 0.0, 1.0));
    drop += sunC * pow(max(dot(reflect(-uSun, n), V), 0.0), 300.0) * 0.4;
    // der Rand bricht die dunklere Umgebung (See, Turm) zum Auge: dunkel
    drop *= 1.0 - 0.65 * smoothstep(0.45, 1.0, rim) * wgRimDetail(p);
    // nasser Lack: dunkler, glänzender (als Abdunklung über dem fertigen Handlauf)
    float film = s.y * uStrength;
    vec3 col = drop;
    float a = cov;
    float dark = (1.0 - cov) * film * 0.25;
    col = (col * a) / max(a + dark, 1e-4);
    a = a + dark;
    // Salz: weiße, matte Kruste
    float sa = wgSalt(p) * (1.0 - cov);
    vec3 crust = vec3(0.8, 0.8, 0.78) * (sunC * max(dot(N, uSun), 0.0) / 3.14159265 + mix(seaAmb, skyAmb, smoothstep(-0.7, 0.5, N.y)));
    col = mix(col, crust, sa / max(a + sa - a * sa, 1e-4));
    a = a + sa - a * sa;
    float dist = length(P - uCam);
    frag = vec4(aerial(col, (P - uCam) / max(dist, 1e-3), dist), a);
}
