# Lighthouse — Planungsprompt für das Werkbuch

> **Zweck dieser Datei:** Grundlage, aus der Claude das **„Lighthouse Werkbuch“** erstellt.
> Noch **kein Code, keine Skripte**. Erst planen: Ausgangspunkt klären, Themen und Struktur festlegen, offene Fragen stellen. Programmiert wird erst nach der Freigabe von Phase 1.
>
> Stand: 15.10.2026 · NetBeans-Projekt `C:\Users\Dan40810\Documents\NetBeansProjects\Lighthouse`

---

## 1. Auftrag

Eine animierte 3D-Echtzeitszene eines Leuchtturms auf offener See, von ruhiger Sommerbrise bis zum schweren Sturm, mit Gewitter, Nacht und Leuchtfeuer.

**Die Besonderheit dieses Projekts:** Die Naturelemente werden **nicht** fest in die Leuchtturm-App eingebaut. Jedes wird ein **eigenständiges, wiederverwendbares Package**:

- Wellen / Meer
- Gischt / Brandung
- Wolken
- Regen
- Tropfen auf Scheiben und nassen Flächen
- weitere Elemente, siehe Package-Katalog

Die Leuchtturm-App ist am Ende nur **ein Anwendungsfall** dieser Packages. Spätere Projekte, etwa ein Schiff, ein Hafen, eine Strandszene oder ein Autofenster, sollen sie unverändert übernehmen können.

**Detailtreue ist ausdrücklich gewünscht.** Ein Beispiel: Regentropfen, die an der Scheibe hängen bleiben, sich sammeln, ruckartig herunterlaufen und eine Spur hinterlassen. „Zeig richtig, was du kannst“. Eigene Ideen sind erwünscht.

---

## 2. Ausgangslage im Projekt (bereits vorhanden)

| Bereich | Stand |
|---|---|
| JDK | `platform.active=JDK_21`, `javac.source/target=21` |
| Bibliotheken `lib/` | `FStyle.jar`, `jogl-all.jar` + natives (win-amd64), `gluegen-rt.jar` + natives, `ojdbc11.jar`, `sdoapi.jar`, `sdogr.jar`, `sdoutl.jar` |
| Vorhandene Packages `src/com/dan/` | `forest`, `ground`, `river`, `road` (aus dem Geyser-Projekt, je mit `README.md`) |
| Datenbank | `db/USER.sql`: `manage_user('Lighthouse', 'li')`, Rolle `DB_CONNECT`, Service `PDBORCL`; `db/db.properties(.example)`: Passwort leer → Startbild fragt und speichert, alternativ Umgebungsvariable `Lighthouse_DB_PASSWORD`; SQL-Developer-Berichte vom 15.09. liegen in `db/` |
| Programmsymbol | Ordner `icon/` existiert, noch leer |
| Verknüpfung | `create-shortcut-files.bat` vorhanden |
| Vorlagen | `templates/` mit 6 Bildern (siehe Abschnitt 3) |
| `main.class` | noch nicht gesetzt |

**Vor dem Werkbuch prüfen:** die READMEs der vier vorhandenen Packages lesen. Sie legen den **Paketstil** fest, den die neuen Packages übernehmen (siehe Abschnitt 4). Außerdem klären, ob `river` als Basis für das Meer taugt oder ob `com.dan.ocean` eigenständig sein soll.

---

## 3. Vorlagen (`templates/`)

| Datei | Inhalt | Was daraus übernommen wird |
|---|---|---|
| `Leuchturm_Aussen1.jpg` | **Roter Sand** (Wesermündung), Seitenansicht bei Wind: rot-weiß, dunkler Caisson-Sockel, drei Erker-Ausbauten mit Konsolen, Galerie mit Geländer, schwarze Kuppel mit Knauf, Mast/Leiter, Solarpaneele; Schaumkranz um das Fundament; in der Ferne ein zweiter Turm | Bauform, Proportionen, Farben, Seegang Bft 5–6 mit Schaumköpfen |
| `Leuchturm_Aussen2.jpg` | Roter Sand **von oben** (Drohne): grün-türkises Wasser mit Licht im Wellenkamm, Brandungsring um das Fundament, Rost am Caisson, **grüner Algenstreifen** an der Wasserlinie, Steinschüttung, Peilpfahl | Wasserfarbe/Transluzenz, Brandungsring, Verwitterung, Kamera-Preset „Drohne“ |
| `Leuchturm_Aussen3.jpg` | Roter Turm auf Fels, Nacht, Gewitter, **Lichtstrahl** in den Wolken, Blitze, haushohe Brecher | Stimmung „Sturmnacht“, volumetrischer Leuchtfeuerstrahl, Blitzverästelung |
| `Leuchturm_Aussen4.jpg` | Rot-weißer Turm, Sturm, gewaltige Gischtwand, dunkle Wolkentürme, **Möwen** | Gischt-Maßstab, Wolkendramatik, Seevögel |
| `Leuchturm_Innen1.jpg` | **Laternenraum**: Rundum-Verglasung mit Sprossen, Holzboden, Linse/Laterne in der Mitte, Blick aufs Meer | Innenraum oben; **Hauptbühne für Tropfen auf der Scheibe** |
| `Leuchturm_Innen2.jpg` | **Wendeltreppe** von oben: Steinstufen, Messing-Handlauf mit gelben Knäufen, Leiter, Kabel, Glühlampe, Eimer und Besen | Treppenhaus, Kamerafahrt „Aufstieg“, Kleinkram für Detailtreue |

---

## 4. Kernprinzip: der Paketvertrag

Jedes Element-Package muss **alle** folgenden Punkte erfüllen. Das Werkbuch übernimmt diese Liste als verbindliche Abnahme-Checkliste.

1. **Unabhängig:** Es braucht nur Java 21 (und für die GL-Teile JOGL). Es hängt **nicht** von `com.dan.lighthouse` und **nicht** von einem anderen Element-Package ab.
2. **Zum Herauskopieren:** `src/com/dan/<paket>/` kopieren reicht. Shader (`.glsl`) liegen als Ressourcen **im** Package.
3. **Kern und Darstellung getrennt** (Vorschlag, siehe Frage F4):
   - `com.dan.<paket>` ist reines Java: Physik, Zustand und Abfragen. Ein Beispiel ist `ocean.heightAt(x, z, t)` für Bojen oder Schiffe, ganz ohne GPU.
   - `com.dan.<paket>.gl` enthält den JOGL-Renderer und die Shader.
   - `com.dan.<paket>.demo` ist eine eigene kleine Vorführung, nur sie darf `FStyle.jar` nutzen.
4. **Kopplung nur über schmale Schnittstellen:** Pakete reden nicht direkt miteinander. Sie bieten Eingänge an (Wind als Vektor, Zeit, Sonnenrichtung, Treffer-Callbacks). Die App verdrahtet sie in `com.dan.lighthouse.wiring`. Beispiel: `rain` meldet über `SurfaceHit` einen Einschlag, die App leitet ihn an `ocean.addImpulse(x, z, s)` weiter. Das ist dasselbe Muster wie `LeafFall.Water` zwischen `forest` und `river`.
5. **Voreinstellungen + offene Werte:** Presets wie `calm()`, `breeze()`, `gale()` und `storm()`, möglichst an der **Beaufort-Skala** ausgerichtet. Alle Werte sind öffentlich und zur Laufzeit änderbar.
6. **Reproduzierbar:** Gleicher `seed` ergibt das gleiche Bild.
7. **Qualitätsstufen und LOD:** niedrig / mittel / hoch / ultra, damit es auf der integrierten Radeon (2 GB) flüssig läuft.
8. **README.md** im Stil der vorhandenen Packages: kurze Einleitung, „Was es kann“, „Benutzung“ mit Codebeispiel, Grenzen. Klares Deutsch, keine Marketingsprache.
9. **Isolationsprüfung im Build:** Ein Ant-Target `check-packages` kompiliert jedes Package **einzeln**, nur gegen JDK und JOGL. Schlägt das fehl, ist das Package nicht fertig. So wird die Wiederverwendbarkeit geprüft statt nur behauptet.
10. **Abnahme je Package:** Die eigene Demo läuft **ohne** Leuchtturm-Code. Dazu gibt es ein Statusbild.

---

## 5. Package-Katalog (Vorschlag, im Werkbuch zu bestätigen)

### 5.1 `com.dan.ocean` — Meer und Wellen
- Wellenspektrum: FFT-Ozean (Phillips/JONSWAP) für Windsee **plus** Gerstner-Wellen für lange Dünung, getrennt regelbar
- Seegang nach Beaufort/Douglas: Wellenhöhe, Periode, Steilheit und Schaumanteil laut Tabelle
- **Schaumköpfe** aus der Jacobi-Determinante der Verschiebung (Wellen, die sich überschlagen), mit Nachleuchten: Schaum zerfällt langsam statt zu flackern
- **Schaumstraßen** in Windrichtung ab Bft 7
- Optik: Fresnel, **Licht im Wellenkamm** (Subsurface, das Türkis aus Aussen2), Himmelsspiegelung, Sonnen- und **Mondglitzerpfad**, Tiefenfärbung
- Flachwasser und Hindernisse: Wellen laufen auf, brechen und bilden einen **Brandungsring** um beliebige Zylinder- oder Kegelkörper
- Störimpulse von außen, etwa Regentropfen, Gischt, die zurückfällt, oder ein Schiffsrumpf: `addImpulse(...)`
- CPU-Abfragen: `heightAt`, `normalAt` und `velocityAt` für Bojen, Schiffe und die Kamera

### 5.2 `com.dan.spray` — Gischt und Brandung
- Aufprall-Gischt an **beliebigen Kollisionskörpern** über die Schnittstelle `Collider` (Zylinder, Kegel, Box, Höhenfeld). Damit funktioniert es auch an Mole, Fels oder Bug.
- Drei Arten: **Spritzwasser** (große Tropfen, ballistisch), **Sprühnebel** (fein, treibt mit dem Wind, schwebt lange), **Gischtfahnen** (Sturm reißt Kämme ab, ab Bft 8)
- Fallende Tropfen melden ihren Wiedereintritt ans Meer
- Gischt, die auf Flächen trifft, macht diese nass (Callback an `wetglass`)
- GPU-Partikel mit weichen Partikeln (Soft Particles), Licht von hinten (Gegenlicht), Beleuchtung durch das Leuchtfeuer

### 5.3 `com.dan.cloud` — Wolken
- Volumetrisch per Raymarching im Shader: Cumulus, Stratus, Altostratus, Stratocumulus, **Cumulonimbus** mit Amboss
- Licht: Silberrand, Powder-Effekt, Abendrot, **Blitze leuchten im Wolkeninneren auf**, der **Leuchtfeuerstrahl streift die Wolkenunterseite**
- Drift und Formwandel mit dem Wind, Wolkenbedeckung 0–8 Achtel als Regler
- Regenvorhänge und Virga unter Schauerwolken, im Austausch mit `rain`
- Sparsame Ausweichstufe: geschichtete 2.5D-Wolken für niedrige Qualität

### 5.4 `com.dan.rain` — Regen
- Regenstreifen mit Bewegungsunschärfe, Schräglage nach Wind und Stärke (Niesel bis Wolkenbruch)
- **Regenvorhänge** in der Ferne (sichtbare Schauerwand, die über die See zieht)
- Aufprall: Spritzer auf festen Flächen, **Ringwellen und Krönchen auf dem Wasser**
- Nur Partikel um die Kamera (Kamera-Volumen), damit es überall billig bleibt
- Schnittstelle `SurfaceHit` für Ozean, nasse Flächen und Scheiben

### 5.5 `com.dan.wetglass` — Tropfen auf Scheiben und nassen Flächen *(Prunkstück)*
Wunsch des Auftraggebers. Bewusst **allgemein** gehalten, also für jede Fläche, nicht nur für Fenster:
- **Tropfenphysik:** Tropfen haben Masse und **haften**, bis eine Schwelle überschritten ist. Dann laufen sie **ruckartig und im Zickzack** herunter, **verschmelzen**, ziehen eine **Spur** und zerfallen in kleine **Resttröpfchen**. Kleine Tropfen bleiben stehen.
- **Lichtbrechung:** Jeder Tropfen wirkt als kleine Linse und zeigt die Welt **auf dem Kopf**. Am Rand liegen Glanzlicht und dunkle Kontur.
- **Beschlag/Kondensat** innen, **Salzkruste** außen nach Gischt; die Scheibe wird nach einem Gischttreffer flächig überspült
- **Interaktion:** Mit der Maus über die beschlagene Scheibe **wischen**, die Spur beschlägt langsam wieder
- Wind drückt die Tropfen seitwärts. Auf **gekrümmten Flächen** (Turmwand, Kuppel) laufen **Rinnsale** dem Gefälle nach.
- Technik: Tropfen-Simulation auf der CPU, daraus eine Höhen-/Normalenkarte als Textur, im Shader Brechung und Unschärfe dahinter
- Spätere Nutzung: Autofenster, Schiffsbrücke mit Scheibenwischer, Gewächshaus, Fenster in jeder Szene

### 5.6 `com.dan.lightning` — Gewitter
- Verästelte Blitze (Dielectric Breakdown oder L-System): Wolke-Wolke, Wolke-Wasser, Wetterleuchten
- Nachleuchten, Mehrfachentladung (Flackern), Beleuchtung der Szene und **Spiegelung auf nassen Flächen**
- **Donner verzögert nach Entfernung** (Schallgeschwindigkeit)
- Prüfen, ob die Blitze aus dem **Projekt Thunder** übernommen und zum Package gemacht werden können (Frage F9)

### 5.7 `com.dan.beacon` — Leuchtfeuer
- **Kennung als echte Seezeichen-Notation**, mit Parser: `Fl(3) W 15s`, `Oc WRG 6s`, `Iso`, `LFl`, `Q` … Damit lässt sich jeder reale Leuchtturm einstellen.
- **Sektorenfeuer** Weiß/Rot/Grün, wie es Roter Sand hatte
- Drehende **Fresnel-Linse** (Gürtellinse mit Prismenringen): Kaustiken und Lichtflecken wandern durch den Laternenraum
- **Volumetrischer Strahl**, der nur dort sichtbar ist, wo Streumedium in der Luft liegt (Regen, Nebel, Gischt). So ist es physikalisch richtig: In klarer Nacht sieht man kaum Strahl, im Regen einen kräftigen.
- Tragweite und Kimm (Erdkrümmung) als Werte

### 5.8 `com.dan.sky` — Himmel und Atmosphäre
- Atmosphärische Streuung (Rayleigh/Mie), Sonnen- und Mondstand nach Datum, Uhrzeit und Ort
- **Sterne aus [Sternbild]**, Mondphase aus **[FMoon]**
- Dunst und Fernsicht nach Wetterlage, **Regenbogen** nach dem Schauer (Erfahrung aus Geyser übernehmen)

### 5.9 `com.dan.fog` — Seenebel
- Höhennebel und Nebelbänke, die über die See ziehen, mit Lichtstreuung für Leuchtfeuer und Sonne
- Nebelhorn-Ereignis für `soundscape`

### 5.10 `com.dan.weather` — Wetterregie
- Zustandsautomat für den Wetterverlauf: klar → aufziehend → Front → Sturm → Gewitter → abklingend → aufklarend
- Liefert **nur Zahlen** (Wind, Bft, Bedeckung, Regenrate, Nebeldichte, Luftdruck), keine Grafik. Die App verteilt sie an die Packages.
- Weiche Übergänge, Böen, Barometer, das **vor** dem Sturm fällt
- Szenario „**Ein Sturmtag**“ als Zeitraffer

### 5.11 Weitere Kandidaten (Zugaben, im Werkbuch einzeln abfragen)
- `com.dan.weathering`: Materialzusätze für beliebige Oberflächen: **nass** (dunkler und glänzender), Rostfahnen, **Algenstreifen** an der Wasserlinie, Salzausblühung, Moos
- `com.dan.flock`: **Möwen** mit Schwarmverhalten. Sie nutzen den Aufwind am Turm, stehen im Wind und kreischen.
- `com.dan.buoyancy`: Schwimmkörper für Bojen, Tonnen und Schiffe auf `ocean.heightAt`, mit Rollen und Stampfen
- `com.dan.soundscape`: **erzeugter Ton ohne Audiodateien** (wie in Thunder): Brandung, Regen auf Glas, Wind, der um den Turm heult, Donner, Nebelhorn, Möwen
- **Gezeiten** über FMoon: Wasserstand steigt und fällt, nasse und trockene Sockelzonen, Algenlinie

---

## 6. Die Szene (projektspezifisch, `com.dan.lighthouse`)

- **Leuchtturm** nach Vorbild Roter Sand (siehe Frage F1): Caisson-Sockel mit Steinschüttung, Schaft rot-weiß, Erker mit Konsolen, Galerie, Laterne, Kuppel und Mast
- **Innenräume:** Laternenraum (Innen1) und Wendeltreppe (Innen2), gegebenenfalls auch Wachstube oder Wohnraum in den Erkern
- **Umgebung:** offene See, am Horizont ein zweites Seezeichen (Aussen1), Tonnen, vorbeifahrende Schiffe mit Positionslichtern
- **Detailtreue:** Nieten und Plattenstöße am Schaft, Bullaugen, Rostläufer unter Kanten, Solarpaneele, Antennen, Leiter am Caisson, Messinghandlauf mit gelben Knäufen, Glühlampe, Eimer und Besen, Holzdielen im Laternenraum

### Schaustücke (Kamera-Presets und Regie)
1. **Laternenraum im Sturm:** Blick nach draußen durch die Scheiben. Regentropfen laufen herunter, Gischt klatscht gegen das Glas, hinter der Kamera dreht sich die Linse und ihr Strahl schneidet durch den Regen.
2. **Drohne** von oben wie Aussen2: Brandungsring, Algenstreifen, Wasserfarbe
3. **Möwenflug:** Die Kamera reitet mit einer Möwe um den Turm
4. **Aufstieg:** die Wendeltreppe hinauf bis in den Laternenraum (Innen2 → Innen1)
5. **Sturmnacht** wie Aussen3/4: Brecher am Sockel, Blitze, Strahl in den Wolken
6. **Von der Schiffsbrücke:** schwankendes Deck, Scheibenwischer, Tropfen auf der Brückenscheibe. Das zeigt nebenbei, dass `wetglass` wiederverwendbar ist.
7. **Ein Sturmtag:** Zeitraffer von der Morgenstille bis zum Aufklaren mit Regenbogen
8. Freie Kamera: Orbit, Zoom, Rundumblick

---

## 7. Erfahrungen aus bisherigen Werkbüchern (übernehmen)

Aus Geyser, Semiramis, Caracalla, Talsperre, Heidelberg, Kneiphof, Kīlauea und Thunder:

- **Werkbuch zuerst**, als Artefakt „**Lighthouse Werkbuch**“, das über das ganze Projekt fortgeschrieben wird (Entscheidungen, Phasenstand, Statusbilder)
- **Fragen nummeriert**, je mit Optionen A/B/C. **A ist immer die echte Empfehlung**, denn die Antwort lautet erfahrungsgemäß oft „alle A, los!“. Jede Option bekommt eine Zeile Begründung.
- **Phasenweise freigeben.** Je Phase gibt es ein **Statusbild** `Status_PhaseN.png` im Projekt. Mehrere Phasen können gemeinsam freigegeben werden.
- **Oberfläche und Datenbank als eigene späte Phase** mit eigenem Fragenblock (wie Geyser Phase 9, Kneiphof Phase 11, Kīlauea Phase 16). Tabellenentwurf mit allen Spalten vorlegen und freigeben lassen.
- **Schliff** als letzte Phase: README für GitHub mit PNGs in `docs/bilder`, `Verknuepfung.bat/.ps1` wie in Heidelberg, Programmsymbol `.ico` **mit Goldkante als Rahmen wie `heidelberg.ico`**
- **GL-Engine aus Kneiphof/Kīlauea** übernehmen statt neu bauen (JOGL liegt schon in `lib/`)
- **FFrame** als Hülle, beim Start maximiert, Größe veränderbar; Bedienelemente aus FStyle in **FTheme-Farben**; die Szene hat ihren eigenen Look; Dialoge über **FOptionPane/FDialog**
- **Datenbank:** eigenes Schema **LIGHTHOUSE** (laut `USER.sql`) mit Präfix `LH_`; Autoinkrement mit **Sequence + Trigger, nie IDENTITY**; Verbindung über `db/db.properties` wie Heidelberg/Kneiphof. Das Passwort aus dem Startbild wird nach erfolgreichem Verbinden gespeichert.
- **Leistung von Anfang an messen:** FPS-/GPU-Zeit-Anzeige ab Phase 1, Qualitätsstufen, Ziel-Hardware Ryzen 7 7735HS mit integrierter Radeon (2 GB VRAM), 64 GB RAM
- **Selbst prüfen** statt nur behaupten: Pixelprüfung und Vorher/Nachher-Bilder, Normalen und Wicklung früh testen (Lehre aus RayPhong: invertierte Kugelwicklung blieb eine Phase lang unbemerkt), Swing-Panels deckend mit expliziter Hintergrundfarbe (Lehre aus RayPhong Phase 6)
- **Vorhandene Packages** (`forest`, `ground`, `river`, `road`) sind ein Vorbild für Stil und README. Für die Szene nur nutzen, wenn Land vorkommt (siehe F3).

---

## 8. Gliederung des Werkbuchs (zu erstellen)

1. Auftrag und Leitidee
2. Vorlagen und was aus ihnen folgt
3. Grundsatzentscheidungen (Engine, Vorbild, Paketvertrag)
4. **Package-Katalog** mit Paketvertrag, Schnittstellen und Abhängigkeitsbild (die App in der Mitte, die Packages ringsum ohne Querverbindungen)
5. Szene: Leuchtturm, Innenräume, Umgebung, Detailliste
6. Regie: Wetterverlauf, Tageszeit, Gezeiten, Kamera-Presets
7. Oberfläche (FFrame, Regler, Presets, Export)
8. Datenbank (später ausgearbeitet)
9. **Phasenplan** mit Lieferumfang und Abnahme je Phase
10. Leistungsbudget und Risiken
11. **Offene Fragen** (nummeriert, A/B/C)
12. Ideenspeicher und Zugaben
13. Fortschritt (Statusbilder, Freigaben, Datum)

---

## 9. Phasenplan — Entwurf (im Werkbuch verfeinern)

| Phase | Inhalt | Abnahme |
|---|---|---|
| 1 | Grundgerüst: FFrame, GL-Engine übernehmen, Kamera, HUD mit FPS, `wiring`-Schicht, Ant-Target `check-packages` | Fenster läuft, leere Szene mit Horizont, Isolationsprüfung grün |
| 2 | `ocean`: Dünung, Windsee, Optik, Schaumköpfe, Demo | Demo ohne Leuchtturm, Bft 2/6/10 im Vergleich |
| 3 | `sky`: Atmosphäre, Sonne, Mond, Sterne, Tageszeit | Morgen/Mittag/Abend/Nacht |
| 4 | `cloud`: volumetrische Wolken, Bedeckung, Drift | Schönwetter bis Cumulonimbus |
| 5 | Leuchtturm außen: Modell, Materialien, `weathering` (nass, Rost, Algen) | Ansicht wie Aussen1, Drohne wie Aussen2 |
| 6 | `spray`: Brandungsring, Aufprall-Gischt, Sprühnebel, Gischtfahnen | Sturmbild wie Aussen4 |
| 7 | `rain`: Streifen, Vorhänge, Ringwellen auf dem Meer | Schauer zieht über die See |
| 8 | `wetglass` + Laternenraum: Tropfen, Spuren, Beschlag, Wischen | Laternenraum im Regen, Nahaufnahme Scheibe |
| 9 | `beacon`: Linse, Kennung, Sektoren, volumetrischer Strahl, Kaustiken | Nacht klar gegen Nacht mit Regen |
| 10 | `lightning` + Donner | Sturmnacht wie Aussen3 |
| 11 | Wendeltreppe und weitere Innenräume, Kamerafahrt „Aufstieg“ | Innen2 nachgestellt |
| 12 | `weather`: Wetterregie, Übergänge, „Ein Sturmtag“ | Zeitraffer läuft durch |
| 13 | Leben: `flock` (Möwen), `buoyancy` (Tonnen, Schiffe), `fog` | Möwenflug, Schiffsbrücke |
| 14 | `soundscape` (erzeugter Ton) | Hörprobe je Wetterlage |
| 15 | Oberfläche + Datenbank (eigener Fragenblock) | Tabellen freigegeben, Speichern/Laden |
| 16 | Schliff: Qualitätsstufen, README je Package + GitHub-README, Icon, Verknüpfung | „fertig“ |

---

## 10. Fragen-Kandidaten für das Werkbuch

*(Claude formuliert sie im Werkbuch aus, ergänzt sie und setzt die Empfehlung jeweils als A.)*

| Nr. | Frage | A (Empfehlung) | B | C |
|---|---|---|---|---|
| F1 | Welcher Leuchtturm? | Roter Sand als Vorbild (Aussen1/2), Sturmstimmung aus Aussen3/4 | Frei gestalteter Turm auf Fels (Aussen3) | Ein Ostsee-Leuchtturm aus der Nähe |
| F2 | Maßstab und Lage | Echter Maßstab, echte Lage in der Wesermündung (SRID 8307) | Echter Maßstab, Lage frei | Frei |
| F3 | Land in Sicht? | Nur offene See, Küste als ferner Streifen | Felsinsel (`ground`/`forest` nutzen) | Küstenpanorama |
| F4 | Aufbau der Packages | Reiner Java-Kern + `.gl`-Unterpaket + `.demo` | Nur JOGL | Nur CPU wie `river`/`road` |
| F5 | Kopplung der Packages | Schmale Schnittstellen, Verdrahtung in der App | Gemeinsames Mini-Paket `com.dan.atmos` mit Wetterzustand | Direkte Abhängigkeiten |
| F6 | Engine | GL-Engine aus Kneiphof/Kīlauea übernehmen | Neu aufsetzen | — |
| F7 | Meerestechnik | FFT-Windsee + Gerstner-Dünung | Nur Gerstner | Nur FFT |
| F8 | Wolkentechnik | Volumetrisch mit 2.5D-Ausweichstufe | Nur volumetrisch | Nur 2.5D |
| F9 | Blitze | Aus Thunder übernehmen und zum Package ausbauen | Neu bauen | — |
| F10 | `wetglass` auch für gekrümmte Flächen (Turmwand, Kuppel)? | Ja, allgemein | Nur ebene Scheiben | — |
| F11 | Innenräume | Laternenraum + Wendeltreppe | Nur Laternenraum | Alle Etagen |
| F12 | Kennung des Leuchtfeuers | Historische Kennung von Roter Sand (recherchieren), wählbar per Notation | Frei wählbar | Fest |
| F13 | Gezeiten | Über FMoon, Wasserstand sichtbar am Sockel | Fester Wasserstand | — |
| F14 | Ton | Erzeugt, ohne Audiodateien (wie Thunder) | Kein Ton | Audiodateien |
| F15 | Lebewesen und Verkehr | Möwen + Tonnen + gelegentlich ein Schiff | Nur Möwen | Nichts |
| F16 | Leistungsziel | Hoch: ≥ 30 fps Full HD; Mittel: 60 fps | Nur 30 fps | Frei |
| F17 | Wetterwerte | Erzeugt durch die Wetterregie | Echte DWD-Daten importieren (DB) | — |
| F18 | Schema | LIGHTHOUSE laut `USER.sql`, Präfix `LH_` | DEMO wie früher | — |
| F19 | Programmsymbol | Neues `.ico` mit Goldkante wie `heidelberg.ico` | — | — |
| F20 | Vorhandene Packages `forest/ground/river/road` | Bleiben unangetastet, nur als Vorbild | Entfernen | In der Szene nutzen |

---

## 11. Datenbank-Ideen (für Phase 15, noch nicht ausarbeiten)

- `LH_LIGHTHOUSE`: deutsche Leuchttürme mit Lage (`SDO_GEOMETRY`, SRID 8307), Feuerhöhe, Tragweite, **Kennung** und Sektoren. Damit kann `beacon` per Auswahl jeden realen Turm darstellen. So zeigt sich die Wiederverwendbarkeit auch in den Daten.
- `LH_WEATHER_PRESET`: benannte Wetterlagen (Beaufort, Bedeckung, Regen, Nebel)
- `LH_SCENARIO` / `LH_SCENARIO_STEP`: Regiebücher wie „Ein Sturmtag“
- `LH_CAMERA_PATH`: Kamerafahrten mit Stützpunkten
- `LH_SNAPSHOT`: gespeicherte Ansichten mit Vorschaubild (BLOB)
- `LH_PACKAGE_SETTING`: Einstellungen je Package (Name/Wert), sodass Presets auch für spätere Projekte nutzbar sind

---

## 12. Ideenspeicher (eingebracht von Claude)

- Tropfen als Linsen: In jedem Tropfen steht der Leuchtturm **auf dem Kopf**
- Der Strahl ist in klarer Nacht fast unsichtbar und im Regen kräftig, gesteuert über die Streudichte
- Kaustiken der drehenden Linse wandern über Dielen und Sprossen im Laternenraum
- Ein Blitz spiegelt sich in jeder nassen Fläche und in jedem Tropfen auf der Scheibe
- Ein Brecher trifft die Laterne: Die ganze Scheibe wird überspült, danach laufen Rinnsale ab und es bleibt eine Salzkruste
- Ein Barometer im Turm fällt sichtbar, bevor der Sturm kommt
- Schaumstraßen und Gischtfahnen ab Bft 8, so wie es auf echten Seegangsfotos aussieht
- Möwen stehen im Aufwind an der Luvseite des Turms
- Mondglitzerpfad auf der See, Mondphase aus FMoon
- **Meeresleuchten** (Biolumineszenz) in warmen Sommernächten, wo Wellen brechen. Das ist eine leise Zugabe.
- Nebelhorn im Seenebel, Donner mit Laufzeitverzögerung
- Vorbeifahrendes Schiff mit richtigen Positionslichtern (rot/grün/Topplicht), das in den Wellen rollt
- Wischen mit der Maus über die beschlagene Scheibe
- PNG- und Video-Export der Schaustücke für das GitHub-README

---

## 13. Arbeitsanweisung an Claude

1. Projektordner sichten (Abschnitt 2), Vorlagenbilder ansehen (Abschnitt 3), READMEs der vorhandenen Packages lesen.
2. Kurz recherchieren: Roter Sand (Bauform, Maße, Baujahr 1885, historische Kennung und Sektoren), Beaufort- und Douglas-Tabelle.
3. Das **„Lighthouse Werkbuch“** als Artefakt anlegen, nach der Gliederung in Abschnitt 8, mit Paketvertrag, Package-Katalog, Phasenplan und **nummerierten Fragen** (A = Empfehlung).
4. **Keinen Code schreiben**, bevor die Fragen beantwortet sind und Phase 1 freigegeben ist.
5. Nach jeder Phase das Werkbuch fortschreiben und ein Statusbild ablegen.
