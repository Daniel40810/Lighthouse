# Lighthouse – Quellen

Was im Projekt auf einer Vorlage, einer Vorschrift oder einer Veröffentlichung beruht, steht hier. Was gestaltet oder geschätzt ist, trägt im Werkbuch und in den READMEs das Zeichen [G]. Die Liste ist nach dem Werkbuch (Abschnitt 13) geführt.

## Vorbild, Ort, Physik (Phasen 1 bis 13)

- [Leuchtturm Roter Sand (Wikipedia, deutsch)](https://de.wikipedia.org/wiki/Leuchtturm_Roter_Sand): Bauzeit, Höhen, Geschosse, Erker, Feuer und Sektoren 1985, Sanierungen
- [Roter Sand Lighthouse (Wikipedia, englisch)](https://en.wikipedia.org/wiki/Roter_Sand_Lighthouse): Maße, Lage, Außerdienststellung 1986
- [Gutachten Sanierung 2019 (Deutsche Stiftung Denkmalschutz)](https://www.denkmalschutz.de/fileadmin/media/Bilder/Denkmal/Leuchtturm_Roter_Sand/Gutachten_Sanierung_Juni2019.pdf): Zustand des Caissons
- Beaufort- und Douglas-Skala nach WMO und DWD, Wellenhöhen als wahrscheinliche signifikante Höhen auf offener See
- Himmel: Hillaire, „A Scalable and Production Ready Sky and Atmosphere Rendering Technique“ (EGSR 2020); Bruneton, „A Qualitative and Quantitative Evaluation of 8 Clear Sky Models“ (2017); Meeus, „Astronomical Algorithms“ (über FMoon); Standish, „Keplerian Elements for Approximate Positions of the Major Planets“ (JPL); HYG-Sternkatalog v4.1 (astronexus); Milchstraße aus d3-celestial (Olaf Frohn); CIE-Normhimmel S 011
- [Alte Weser Lighthouse (Wikipedia, englisch)](https://en.wikipedia.org/wiki/Alte_Weser_Lighthouse): Lage auf die Bogenminute, 38 m hoch, Feuer auf 33 m, rot mit zwei weißen Bändern, grüne Laterne
- [Gezeitentafel Bremerhaven Alter Leuchtturm (tide-forecast.com)](https://tide-forecast.com/tide/Bremerhaven-Alter-Leuchtturm-Germany/sea-conditions): Hochwasser 17.10.2026 um 05:17, Spring Ende Oktober; daran ist der Takt der Gezeiten geeicht
- [Leuchtturm Tegeler Plate (deutsche-leuchtfeuer.de)](https://www.deutsche-leuchtfeuer.de/binnen/weser/tegeler-plate.html): Kennung Oc(3) WRG 12s mit Takt (1)+2+(1)+2+(1)+5, Feuerhöhe 20,9 m über MHW, Tragweiten 21/18/17 sm, Sektoren, Lage
- Leuchtfeuer: IALA-Empfehlung E-200-2 (Nenn- und Lichttragweite, 0,2 µlx, Durchlässigkeit 0,74 je sm), Blondel-Rey (wirksame Stärke eines Blitzes), Kulla und Fajardo, „Importance Sampling Techniques for Path Tracing in Participating Media“ (2012, gleichwinklige Abtastung), Stiles-Holladay (Streulicht im Auge), Henyey-Greenstein (Streufunktion)
- Blitz: Berger, Anderson und Kröninger, „Parameters of lightning flashes“ (Electra 41, 1975) und CIGRE-Broschüre 549 (2013) für Ströme, Zahl und Abstand der Schläge und Nachstrom; Rakov und Uman, „Lightning: Physics and Effects“ (2003) für Leitblitz, Windung des Kanals, Wolken- und Spinnenblitze und Donner; Eriksson, „An improved electrogeometric model for transmission line shielding analysis“ (IEEE 1987) für den Anziehungsradius; Schlagweite r = 10 · I^0,65 nach IEEE Std 1243
- Treppe: DIN 18065 (Gebäudetreppen; lichte Durchgangshöhe 2,0 m) und die Schrittmaßregel nach Blondel für die Steigung; Lichtstrom einer 60-W-Glühlampe rund 710 lm; Form, Farben und Ausstattung nach der Vorlage Innen2. Rückstrahlung 0,45 für Putz und Stein zusammen und die Leiterneigung sind geschätzt [G]
- Wetter: WMO-Beaufortskala und Böenfaktor, WMO-Code der Luftdrucktendenz über 3 Stunden, Taupunkt nach Magnus (Koeffizienten 17,62 und 243,12 °C). Zielwerte der Lagen, Trägheiten, Vorlauf und Verweildauern sind typische Werte für Herbsttiefs über der Deutschen Bucht, geschätzt [G]
- Turmdurchmesser, Maße der Manschette, die Stunde Versatz zu Bremerhaven, die Teiltiden-Amplituden, Budgetzahlen, Phasen-Größen, das Drehfeuer-Preset, die Tragweiten von Sturmnacht (20 sm), Fl(3) (18 sm) und dem Schwachlicht (3 sm), die Sektorgrenzen der Alten Weser, Brennweite und Ringzahl der Linse, die Lampengröße, die Stärke der Lichtglocken, die Lichtstärke je Meter Kanal (5000 cd/m), die Trägheit des Auges (35 ms), die Diffusionslänge in der Wolke (1,3 km) und die Pegel des Donners sind gestaltet [G], nicht belegt

## Leuchttürme (Phase 15, Startdaten in `db/sql/04_startdaten.sql`)

Je Turm stehen die Quellen auch in der Spalte `QUELLE` von `LH_LIGHTHOUSE`.

- [deutsche-leuchtfeuer.de](https://www.deutsche-leuchtfeuer.de/): Kennungen, Sektoren, Tragweiten und Feuerhöhen von Roter Sand, Alte Weser, Hohe Weg, Tegeler Plate, Westerheversand, Kiel, Bülk, Flügge und Dahmeshöved (Stand 2024/25)
- [baken-net.de](https://www.baken-net.de/): Bekanntmachungen für Seefahrer zu Alte Weser (BfS 36/19), Hohe Weg, Westerheversand (BfS 74/18) und Tegeler Plate
- [leuchtturm-atlas.de](https://www.leuchtturm-atlas.de/): Baujahre, Höhen, Geschichte
- [Lighthouse Directory (ibiblio)](https://www.ibiblio.org/lighthouse/debrh.htm) und die [Ostsee-Seite](https://www.ibiblio.org/lighthouse/deesh.htm): Anstriche, Laternenfarben, Lage
- Wikipedia, deutsch und englisch, zu jedem Turm (Fließtext)
- Unsicher und so gekennzeichnet ([G] in der Beschreibung): der Sektor 56–71° von Kiel, der Sichtbereich von Dahmeshöved, die Feuerhöhen der Ostseetürme über Mittelwasser

## Klang (Phase 14)

- Strouhal-Zahl 0,2 für Äolstöne an Stäben und Seilen; Schallgeschwindigkeit 331,3 · √(1 + T/273,15) m/s
- KVR Anlage III (Schallsignalanlagen): Grundfrequenzen und Hörweiten von Typhon und Nebelhorn nach Schiffslänge
- Schroeder (1962) und Moorer (1979): künstlicher Hall aus Kammfiltern und Allpässen
- Few (1969): Donner als Summe der N-Wellen eines gewundenen Kanals
- Silbermöwe: Rufe nach Tonaufnahmen aus dem Gedächtnis gestaltet [G]

## Werkzeuge und Vorbilder

- JOGL 2.6.0 (OpenGL 4.3 mit Compute-Shadern), FStyle (Fenster und Bedienelemente), Oracle 21c mit Spatial (SRID 8307), ojdbc11
- GL-Engine, Werkbuch-Form und Phasenplan aus den Vorgängern Kneiphof, Geyser und Kīlauea
- Vorlagenbilder Aussen1–3 und Innen1–3 in `templates/` (gesichtet am 15.10.2026, nicht im Git)
