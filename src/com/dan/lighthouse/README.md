# com.dan.lighthouse – die App

Die App „Roter Sand bei jedem Wetter“ stellt die Element-Packages unter `com.dan` zu einer Szene zusammen. Sie ist der erste Anwendungsfall der Packages und bringt selbst nichts mit, was ein Package könnte: Sie verdrahtet, zeichnet, bedient und speichert. Jedes Package bleibt für sich übersetzbar (`check-packages`, Paketvertrag Punkt 9).

| Teil | Aufgabe |
|---|---|
| `app/LighthouseApp` | Einstieg: Startbild, FFrame, Szene links, Bedienfeld rechts, Statuszeile; Daten und Sitzung beim Start, Sichern beim Ende. Schalter: `-Dlighthouse.splash=false`, `-Dlighthouse.sound=false`, `-Dlighthouse.gljpanel=true`, `-Dlighthouse.shaders=<ordner>`, `-Dlighthouse.db.dir`, `-Dlighthouse.daten.dir` |
| `scene/` | Ort, Uhr, Sonne, Gezeiten, der Turm außen und innen ([README](scene/README.md)) |
| `wiring/` | Die Schnittstellen zwischen den Packages und die Verdrahtung; nur hier kennt ein Package ein anderes ([README](wiring/README.md)) |
| `gl/` | `Renderer` (ein `GLEventListener` mit allen Durchgängen), `Quality` (die Stufe jedes Packages), `SceneView` (Canvas, Maus und Tasten), `HudOverlay`, `ComputeProbe`, `Shader`, `Mat4` und 17 eigene Shader in `gl/shader` |
| `camera/` | Kamera (Kreisen, Frei, Fahrt, Folgen) und Fahrten aus Stützpunkten (`Aufstieg`, Rundflug, eigene) |
| `ui/` | `ControlPanel` (acht Klappblöcke), `DatenSteuerung`, `RegieDialog`, `Startbild`, `AppIcon`, `Theme` |
| `db/` | `Speicher` mit `DbSpeicher` (Oracle) und `DateiSpeicher` (`daten/`), `Einrichter`, `Startdaten`, `Felder`, `Zustand`, `Dienst`, `Protokoll` ([README](../../../../db/README.md)) |
| `person/` | Seit Avatar Phase 15: der Wärter auf der Galerie aus `com.dan.avatar` und `com.dan.hair` ([README](person/README.md)) |
| `tools/` | Statusbilder ohne Fenster je Phase, `Klangprobe`, `StartdatenSql`, `Symbol`, `Dauerlauf` |

## Der Bildlauf

Je Bild: Uhr und Regie einen Schritt weiter → Verdrahtung reicht Wind, Licht, Wasserstand, Feuer und Nebel an die Packages → die Packages rechnen ihren Teil (Meer auf der Grafikkarte, Gischt und Schwimmkörper in eigenen Fäden) → der Renderer zeichnet in der Reihenfolge Schatten, Himmel, Meer, Turm, Innenraum, Gischt, Regen, Nebel, Feuer, Blitz, Scheiben, Nachbearbeitung, HUD. Die Zeit jedes Durchgangs steht im HUD (Zeitstempel der Grafikkarte, gleitendes Mittel).

## Qualität und Auflösung (Phase 16)

`Quality` hat vier Stufen von Niedrig bis Ultra und nennt für jedes Package dessen eigene Stufe (`Quality.summary()` sagt, was sie einstellt). Die Auflösung regelt sich von selbst: Hält die Grafikkarte das Ziel von 60 Bildern/s nicht, sinkt der Anteil der Bildgröße, in dem gerechnet wird, in Schritten von 5 % bis auf 50 %; hat sie Luft, steigt er wieder bis zur Stufe. Geregelt wird höchstens alle 1,5 s (`Renderer.autoScale`, `targetFps`, `renderScale`). Im Block BILD lässt sich die Auflösung auch fest setzen.

## Prüfen

- `test/…/Pruefung` prüft die App ohne Grafikkarte und Datenbank (Ant-Target `pruefung`); sie endet mit „ERGEBNIS: alles in Ordnung“.
- `test/…/DbPruefung` prüft gegen die Datenbank (Run File in NetBeans).
- `tools.Dauerlauf [Minuten] [Breite]` lässt die Szene ohne Fenster laufen und endet mit „Dauerlauf bestanden“; das Protokoll steht in `docs/status/Dauerlauf_claude.txt`.
- `tools/PaketPruefung.java` (Ant-Target `check-packages`, läuft nach jedem Übersetzen) übersetzt jedes Package für sich.
