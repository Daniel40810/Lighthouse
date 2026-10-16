# com.dan.beacon – Leuchtfeuer

Das Paket macht aus einer Kennung in Seezeichen-Notation ein Leuchtfeuer, das man sieht: die Lichtstärke in jede Richtung, die Blitze einer Drehlinse, den Strahl im Regen, den Lichtpunkt am Horizont und die Lichtflecken der Linse im Laternenraum. Es rechnet mit echten Größen. Die Lichtstärke folgt aus der Nenntragweite nach IALA, die Verteilung der Linse aus einer Strahlverfolgung durch ihre Prismenringe, der Strahl aus der Einfachstreuung in Regen, Gischt und Dunst.

Der Kern braucht nur Java 21. Der Ordner `gl/` zeichnet mit JOGL (GL 4.3). Das Paket kennt weder Meer noch Regen noch Turm: Wie stark die Luft streut, kommt über die Schnittstelle `Scatter`, und die Lichtverteilung geht als Rundumbild hinaus, das jeder Shader lesen kann. Zum Herauskopieren reicht `src/com/dan/beacon/`; nur `demo/BeaconDemo` braucht zusätzlich `FStyle.jar`.

## Was es kann

- **Kennungen:** `Characteristic.parse("Fl(3) W 15s")` liest die IALA-Notation mit F, Fl, LFl, Q, VQ, UQ, IQ, Iso, Oc, Mo(x), FFl, Gruppen wie `Fl(2+1)`, zusammengesetzte Kennungen wie `Q(6)+LFl`, Wechselfeuer `Al.Fl WR 4s` und Sektorenfeuer `F WRG`. Deutsche Kürzel gehen auch: Blz, Ubr, Glt, Fkl, SFkl, Fest. Fehler kommen mit Stelle und verständlicher Meldung („Wiederkehr fehlt, z. B. „5s““). Was die Notation offen lässt, füllt ein Standardtakt: Blitz 0,5 s, Pause 1 s, langer Blitz 2 s, Funkel 60/min, Unterbrechung 1 s mit 2 s Licht dazwischen. Damit kommt für `Oc(3) WRG 12s` genau der Takt der Tegeler Plate aus dem Verzeichnis heraus: (1)+2+(1)+2+(1)+5. `level(t)` gibt die Helligkeit mit der Trägheit der Lampe.
- **Sektoren:** `SectorTable` nimmt Farbe, Grenzen als Peilung von See zum Feuer (wie im Verzeichnis) und Nenntragweite je Sektor. Ein Schiff im Azimut 20° vom Turm peilt 200° und sieht die Farbe dieses Sektors. An den Grenzen gehen die Farben über 0,5° ineinander über.
- **Tragweiten:** `Range` rechnet nach IALA E-200-2: Lichtstärke ↔ Nenntragweite (835 cd ↔ 9 sm), Lichttragweite bei jeder Sicht, geografische Tragweite aus Feuer- und Augenhöhe (2,075 · (√h₁ + √h₂)) und die wirksame Stärke eines Blitzes nach Blondel-Rey.
- **Linse:** `Lens.drum()` ist eine Gürtellinse mit 25 Prismenringen, deren Außenflächen so gekrümmt sind, dass Licht aus dem Brennpunkt waagerecht austritt (mittlere Abweichung 0,17°). `trace` verfolgt 200 000 Strahlen von einer leuchtenden Kugel durch das Glas, mit Brechung, Fresnel-Verlusten und Totalreflexion, an Messingrahmen, Sockel und Deckplatte vorbei. Heraus kommen die Verteilung über die Höhe (Strahlbreite 2·atan(r/f), bei 32 mm Glühfaden 4,7°) und das Nahfeld: wie hell jeder Ort im Laternenraum angestrahlt wird, mit den Streifen der Prismenringe, dem Lichtring auf den Dielen aus dem Spalt zwischen Linse und Sockel und dem Fleck an der Kuppel.
- **Zwei Optiken:** Die Trommel (`Optic.DRUM`) leuchtet rundum, die Kennung entsteht durch Schalten der Lampe, die Farben durch Farbscheiben. Die Drehlinse (`Optic.ROTATING`) baut aus der Kennung ihre Ringlinsen: Lage und Zahl aus den Blitzen, Drehzeit nahe 20 s, Blitzdauer aus Strahlbreite und Drehgeschwindigkeit. Ein Beobachter sieht die Blitze genau zur Zeit der Kennung (± 0,003 s).
- **Strahl:** Einfachstreuung längs jedes Blicks mit gleichwinkliger Abtastung um das Feuer (Kulla und Fajardo), bei der Drehlinse für jeden Strahl eigens auf dem Stück des Blicks in seinem Keil. Streudichte aus einem Kugelgitter um das Feuer (fein am Feuer, grob in der Ferne), dazu der Dunst aus der Sicht, Streufunktion aus zwei Henyey-Greenstein-Keulen. In klarer Luft ist der Strahl schwach, im Regen von 5 mm/h gut viermal heller. Trifft der Blick die Wolkenuntergrenze, leuchtet sie, wo der Strahl sie erreicht.
- **Lichtpunkt:** Kern in einem Bildpunkt plus Schleier aus Streulicht nach Stiles-Holladay, für dieses und weitere Feuer (Nachbartürme), verdeckt durch die Szene.
- **Ausgänge:** `Snapshot.intensity(w)` (cd je Richtung), `nearIrradiance(p, n)` (lx im Laternenraum), `lensLuminance(w)`, `illuminanceAt(eye)`, `beamRadiance(...)`. Auf der Grafikkarte das Rundumbild der Lichtstärke (`patternTexture()`) und der Baustein `beacon.glsl` für fremde Shader.
- **Dämmerungsschalter:** an unter 150 lx, aus über 300 lx; Glühlicht braucht knapp eine Sekunde bis zur vollen Helligkeit.
- **Für die Augenanpassung:** `BeaconGl.beamMeanLuminance()` gibt die mittlere Leuchtdichte des Strahls über das ganze Bild (Mipmap des Strahlbilds, ein Bild später ohne Warten zurückgelesen). Schmale Strahlen einer Drehlinse fallen so nicht zwischen einige Probeblicke (Nachtrag in Phase 10).
- **Stufen:** `LOW`, `MEDIUM`, `HIGH`, `ULTRA` wählen die Schritte im Strahl (24 bis 64), die Größe des Rundumbilds und des Streugitters.

## Benutzung

```java
Beacon b = Beacon.roterSand1985();          // stormNight(), flash3(), alteWeser(), tegelerPlate()
b.y = 24.3;                                 // Linsenmitte
String fehler = b.setCharacteristic("Fl(3) W 15s");   // null = in Ordnung
b.optic = Beacon.Optic.ROTATING;

// je Bild
b.ambientLux = himmel.lux();  b.hazePerM = 3.0 / sichtMeter;
b.step(dt);
gitter.update((x, y, z) -> regen.sigma(x, y, z) + gischt.sigma(x, y, z), b.x, b.y, b.z, 1.0);  // höchstens 1 ms

// vor der Szene: Rundumbild rechnen, dann darf jeder Shader es lesen
BeaconGl gl = new BeaconGl();
gl.init(gl4);
gl.update(gl4, b.snapshot(), gitter);
meer.punktlicht(gl.patternTexture(), b.x, b.y, b.z);
gl.bind(gl4, turmShader, 10, vorbelichtung);          // beacon.glsl: bcnIntensity, bcnNearIrradiance, bcnLensLuminance

// nach der Szene, mit einer Kopie ihrer Tiefe
BeaconGl.View v = new BeaconGl.View();      // viewProj, invViewProj, eye, depthTexture, targetFbo, preExposure, ceiling…
gl.drawBeam(gl4, v);
gl.drawGlare(gl4, v, List.of(new BeaconGl.Glare(x, y, z, lux, 1, 2.5)));
```

Das Rundumbild hat u = Azimut von Nord / 360° und v = ½ + ½ · sign(h) · √(|h| / 90°). Strahl und Lichtpunkt kommen linear und vorbelichtet und werden mit ONE, ONE addiert.

## Aufbau

| Klasse | Aufgabe |
|---|---|
| `Characteristic` | Parser der Notation, Lichtzeiten, Helligkeit und Farbe zur Zeit t |
| `SectorTable` | Sektoren als Peilung von See, weiche Grenzen, Lichtstärke je Sektor |
| `LightColour` | Signalfarben als Farbort und lineares RGB mit Leuchtdichte 1 |
| `Range` | Nenn-, Licht- und geografische Tragweite, Blondel-Rey |
| `Lens` | Gürtellinse im Schnitt, Strahlverfolgung, Fern- und Nahfeld |
| `Beacon` | Eingänge, Optik, Drehung, Dämmerungsschalter, Voreinstellungen, `Snapshot` mit allen Abfragen |
| `ScatterGrid`, `Scatter` | Streudichte als Kugelgitter um das Feuer, in Portionen nachgefüllt |
| `gl/BeaconGl` | Rundumbild, Strahl, Lichtpunkte, Selbsttest |
| `gl/beacon.glsl` | Baustein für fremde Shader |
| `gl/pattern.frag`, `gl/beam.frag`, `gl/composite.frag`, `gl/glare.*` | die Durchgänge |

## Demo und Selbsttest

- `com.dan.beacon.demo.BeaconDemo` zeigt einen Turm auf See bei Nacht. Rechts: Voreinstellungen, ein Textfeld für die Kennung mit der Rückmeldung des Parsers, Regen, Sicht, Strahl, Blick aus dem Laternenraum, Linse an oder aus. Ziehen dreht, das Rad zoomt.
- Ohne Fenster rechnet `BeaconDemo --bild datei.png [breite]` klare Nacht gegen Regen, die Sektorgrenze Weiß/Rot, die Sturmnacht, den Laternenraum, den Schnitt durch die Linse mit verfolgten Strahlen und die Kennungen als Lichtkurven. Dabei vergleicht es Grafikkarte und Prozessor.
- `com.dan.beacon.demo.BeaconCheck` prüft ohne Fenster und ohne Grafikkarte, mit 33 Prüfungen; Ende „Alles in Ordnung.“: Parser (gegen die Tegeler Plate), Sektoren, Tragweiten, Linse (Bündelung, Strahlbreite, Energie), Blitze einer Drehlinse aus Sicht eines Beobachters, Nahfeld, Streugitter, Strahl gegen eine Rechnung mit 20 000 Schritten, Dämmerungsschalter.
- `BeaconGl.selfCheck` vergleicht das Rundumbild an Texelmitten und 64 Blicke durch den Strahl mit dem Prozessor.

## Grenzen

- Die Linse ist im Meridianschnitt gerechnet. Für die Trommel ist das genau; schräge Strahlen und die Ringlinsen einer Drehlinse sind als Produkt zweier Schnitte genähert.
- Der Strahl streut einmal. Mehrfachstreuung in dichtem Nebel (der „Lichtdom“) fehlt; Nebel selbst kommt mit `com.dan.fog`.
- Die Durchlässigkeit längs eines Wegs nimmt die Streudichte am Ort für den ganzen Weg. Bei Gischt, die nur am Turm hängt, wird der Strahl dahinter darum etwas zu hell.
- Farbscheiben stehen als Zylinder mit festem Halbmesser um die Linse; innerhalb ist das Licht weiß.
- Kaustiken des Sonnenlichts durch die Linse am Tag gibt es nicht.
