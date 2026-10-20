# com.dan.lighthouse.scene – Turm, Ort, Uhr und das Innere

Dieser Teil der App beschreibt, was am Roten Sand steht: den Ort (`Site`), die Uhr (`SceneClock`, `Sun`), die Gezeiten (`Tide`, seit Rungholt Phase 17 aus `com.dan.tide`: Pegel Bremerhaven, eine Stunde früher [G]), den Turm von außen (`Turm`, Phase 5) und seit Phase 11 sein Inneres (`Innenraum`, `Treppenhaus`). Er braucht nur Java 21; gezeichnet wird in `gl/Renderer` mit `tower.vert` und `tower.frag`.

## Das Innere (Phase 11, nach Innen2)

- **Treppe:** 59 Steinstufen von der Tür (8,05 m) bis in den Dienstraum (19,75 m), 18 je Umlauf, Steigung 19,8 cm, im Uhrzeigersinn ab 257,5°. Die Stufen kragen aus der Innenwand (2,6 m) bis an das offene Treppenauge (0,75 m). Beginn, Zahl und Stufen je Umlauf sind so gewählt, dass keines der acht Bullaugen hinter einer Stufe liegt und die Tür frei bleibt (mindestens 16 cm Luft).
- **Wangen:** über jeder Stufe eine Steinplatte mit dunkler Fuge an der Wand, darunter ein Absatz. Zusammen ergeben sie den Zinnenrand, den man in Innen2 sieht.
- **Geländer:** auf jeder Stufe ein Eisenstab mit Messingmanschette und gelbem Knauf, darüber der Handlauf aus Holz als Wendel, oben weiter bis an das Geländer der Bodenöffnung.
- **Im Auge:** eine Holzleiter, drei schwarze Kabel, zwei weiße Seile, oben eine Lampe.
- **Wand:** Kalkputz mit Flecken. Für Bullaugen, Tür und Bogenfenster ist sie gelocht. Ein Ring verbindet genau die Rasterpunkte des ausgesparten Rechtecks mit dem Umriss, deshalb gibt es keine Risse. Die Laibung läuft bis an die Außenhaut, durch sie sieht man hinaus. Das Bullauge hat einen Messingrahmen, das Bogenfenster eine Fensterbank.
- **Dienstraum:** Ein Umlauf ist nur 3,6 m hoch. Damit über den letzten zwölf Stufen 2 m Kopfhöhe bleiben, trägt der Dielenboden nur zwischen 357,5° und 117,5°; zur Öffnung hin hat er ein Geländer. Unter der Decke hängt eine Pendellampe. Eimer und Besen stehen dort, wo die Treppe ankommt.
- **Luke:** eine steile Schiffsleiter durch die Decke in den Laternenraum (Luke 0,62 bis 1,36 m von der Achse, zwischen Linsensockel und Reling). Der Deckel steht offen.
- **Barometer:** ein Dosenbarometer an der Treppenwand. Der Zeiger folgt bis Phase 12 dem Wind (1022 − 3,2 · Bft hPa).

## Licht drinnen

- **Lampen:** sieben Glühlampen (60 W, 710 lm, warmweiß), abschaltbar.
- **Himmel:** durch acht Bullaugen und drei Bogenfenster. Je Fenster misst `Renderer.interiorBalance` die Strahldichte des Himmels draußen (über der Kimm, an der Kimm und das Meer).
- **Sonne:** durch dieselben Fenster. Der Shader prüft für jeden Bildpunkt, ob die Sonne durch beide Ebenen der Laibung fällt und keine Stufe dazwischen liegt. So entstehen Sonnenflecken mit weichem Rand.
- **Schatten:** Für jede Ecke des Innenraums wird einmal ausgerechnet, welche Lampen sie sieht und wie viel vom Fensterlicht die Stufen verdecken (`Innenraum.visibility`, 8 Werte je Ecke, auf allen Kernen in rund 0,8 s). Die Verdeckung prüft Ober- und Unterseite jeder Stufe und den Boden des Dienstraums (`Innenraum.blocked`). Der Shader macht dasselbe in `stairBlocked`.
- **Zurückgeworfenes Licht:** eine Bilanz über zwei Räume, Schaft und Dienstraum, die durch die Bodenöffnung gekoppelt sind (Rückstrahlung 0,45). Sie gibt das indirekte Licht und die mittlere Leuchtdichte. Nach ihr passt sich das Auge drinnen an: fast vollständig (Exponent 0,92), mit Weißabgleich und ohne Stäbchensehen.
- **Werte am 16.10. um 14:30:** Schaft rund 10 cd/m², Dienstraum rund 30 cd/m². Nachts mit Lampen brennen im Schaft rund 3 cd/m².

## Kamerafahrt „Aufstieg“

`camera/Aufstieg` legt Stützpunkte, die ein Catmull-Rom-Spline verbindet. An der Tür gibt es einen harten Schnitt. Die Fahrt dauert rund 76 s:

1. vom Podest vor der Tür hinein und ein Blick hinauf ins Treppenauge,
2. die 59 Stufen in 40 s, das Auge 1,65 m über der Stufe auf 1,68 m vom Mittelpunkt,
3. im Dienstraum zur Schiffsleiter,
4. die Leiter hinauf durch die Luke,
5. im Laternenraum an der Linse vorbei, mit dem Blick hinaus auf die See.

Die Kamera läuft dabei in der Art `FAHRT`. Jede Taste und jedes Ziehen beendet die Fahrt.

## Prüfen und Bilder

- `test/Pruefung`, Abschnitt „Treppenhaus und Innenräume“ prüft:
  - Maße und Fensterfreiheit, Kopfhöhe, Lampen und Luke
  - Verdeckung und Netz, dass alles in der Außenhaut liegt
  - Lichtbilanz und Dämmerungsschalter in lx
  - Barometer, Fahrt und Kamera
  - Regenschutz, Verbindungen und Shader
- `tools/Treppenbild` rechnet das Statusbild: Innen2 neben dem nachgestellten Blick am Nachmittag und am Abend, dazu sechs Bilder der Fahrt. `--folge datei.png [breite] [bilder]` gibt die ganze Fahrt als Bildfolge, `--blick …` ein einzelnes Bild.

## Grenzen

- Die Erker sind innen nicht ausgebaut: Die Wand des Dienstraums läuft glatt durch, Teile des Außennetzes, die in den Innenraum ragen, verwirft der Shader.
- Die Tür bleibt zu. Die Fahrt schneidet vom Podest nach drinnen.
- Die Sichtbarkeit liegt an den Ecken. Schatten der Stufen auf der Wand sind deshalb so weich wie das Raster (0,25 m). Handlauf, Leiter und Stäbe werfen keine Schatten.
- Durch die Fenster kommt kein Blitzlicht und kein Licht des Feuers.
