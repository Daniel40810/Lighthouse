# com.dan.soundscape – Klang

Erzeugter Ton ohne Audiodateien: Wind mit Äolstönen an Stäben und Seilen, See und Brandung, Regen auf dem Wasser, auf Glas und auf einem Dach, Brecher, Nebelhorn und Typhon, Möwen, Donner aus dem Kanal eines Blitzes und der Diesel eines Schiffs. Alles gilt für ein Ohr mit Ort und Blickrichtung: Abstand, Laufzeit, Luftdämpfung, Richtung. Drinnen dämpft die Wand und der Raum hallt. Der Kern braucht nur Java 21; die Ausgabe auf den Lautsprecher nimmt `javax.sound.sampled` aus dem JDK. Ein `gl/` gibt es nicht. Zum Herauskopieren reicht `src/com/dan/soundscape/`; nur `demo/SoundscapeDemo` braucht zusätzlich `FStyle.jar`.

## Was es kann

- **Pegel in dB SPL:** Jede Quelle hat einen Pegel in 1 m oder am Ohr. Ein Effektivwert von 1 im Signal sind 104 dB (`REF_DB`). Abstand nach 1/r, Laufzeit nach Abstand / c mit c aus der Temperatur (331,3 · √(1 + T/273,15) m/s), die Ferne verliert die Höhen (Tiefpass nach Abstand).
- **Richtung:** Stereo nach dem Winkel zur Blickrichtung des Ohrs, das abgewandte Ohr hört gut 12 dB leiser und bis 0,66 ms später (Kopf). Quellen hinter dem Ohr klingen etwas dumpfer.
- **Wind:** rosa Rauschen in einem Band, das mit dem Wind steigt, dazu das Brausen am Ohr; Böen als Ornstein-Uhlenbeck-Vorgang in Echtzeit (3 s). Pegel 22 + 50 · lg U dB, mit dem logarithmischen Profil über See für die Höhe des Ohrs. Gemessen: Bft 2 34 dB, Bft 6 69 dB, Bft 10 86 dB.
- **Äolstöne:** Jeder Körper in `bodies` (Stab, Rohr, Seil: Ort, Durchmesser, Länge) singt mit f = 0,2 · U / d (Strouhal), mit dem Wind in seiner Höhe; der Ton wandert mit den Böen. Ein Geländerstab von 25 mm singt bei 15 m/s mit 120 Hz.
- **See und Brandung:** Rauschen der Wellen und Schaumkronen, das mit der Periode atmet und in Gruppen kommt. An einem Bauwerk (`surfX`, `surfZ`, `surfRadius`) klatscht jede Welle und läuft ab, im Takt der Periode.
- **Regen:** auf dem Wasser Zischen und einzelne Tropfen (1,3 bis 5 kHz); auf Glas helle Ticks (`glassDrops` je s), auf einem Blechdach tiefer und länger (`roofDrops`). Glas und Dach klingen drinnen, ohne die Wand.
- **Ereignisse:** `event(Kind, x, y, z, Stärke)` für BRECHER, MOEWE, NEBELHORN, TYFON, DONNER; sie erreichen das Ohr nach ihrer Laufzeit.
  - **Brecher:** dumpfer Schlag (60 → 35 Hz), Krachen, dann das zurückfallende Wasser; bis 120 dB in 1 m.
  - **Nebelhorn:** `horn(Kind.NEBELHORN, …, Dauer)`, Diaphon mit 175 Hz und Obertönen, 132 dB in 1 m [G]; am Ende fällt der Ton um ein Fünftel („Grunzen“).
  - **Typhon:** 220 Hz, 138 dB in 1 m (KVR Anlage III: 130 bis 350 Hz für 75 bis 200 m Länge).
  - **Möwe:** Silbermöwe, kurzes „kjau“, Warnruf „kek-kek“ oder langer Ruf aus sechs bis neun Tönen; Grundton 800 bis 1100 Hz, näselnd, rau.
  - **Donner:** `thunder(x, y, z, Pegel am Ohr, Abstände, Gewichte, Schläge, schon angekommen)`: Jedes Stück des Kanals sendet eine N-Welle (Few), die Summe rollt so lange, wie die Abstände auseinanderliegen; fern länger und dumpfer, nah ein Knall. Ohne Kanal rechnet das Paket einen gewundenen Kanal mit drei Ästen.
- **Maschine:** `hums` (Ort, Zündfrequenz, Pegel); `Hum.firing(750, 6)` = 37,5 Hz. Mit `aboard` klingt sie gleichmäßig durch den Rumpf.
- **Raum:** `inside` 0..1 und `room`: `Room.OUTDOORS`, `STONE_TOWER` (−28 dB, bis 320 Hz, Hall 2,2 s), `GLASS_LANTERN` (−14 dB, bis 2,2 kHz, 0,7 s), `WHEELHOUSE` (−18 dB, 1,5 kHz, 0,4 s). Hall nach Schroeder und Moorer.
- **Ohr passt sich an:** `autoGain` zieht den Pegel über 4 s zu 84 dB, höchstens 18 dB hinauf und 12 dB hinab; Donner und Horn bleiben laut, eine stille Nacht wird hörbar. Ein Begrenzer mit 5 ms Vorschau fängt den Rest.
- **Presets:** `calm()`, `breeze()`, `gale()`, `storm()`, `beaufort(n)` (Wind, Seegang, Regen). **Reproduzierbar:** gleicher `seed`, gleicher Ton. **Stufen:** `Quality.LOW` bis `ULTRA` (22,05 bis 48 kHz, 12 bis 48 Stimmen, 2 bis 10 Äolskörper, Hall ab MEDIUM).

## Benutzung

```java
Soundscape s = Soundscape.beaufort(7);
s.bodies.add(new Aeolian("Handlauf", 0, 24.1, -4.2, 0.07, 26));
s.surfRadius = 5.5;
s.earX = …; s.earY = …; s.earZ = …; s.earYawDeg = …;   // je Bild
s.inside = 1; s.room = Room.GLASS_LANTERN;
AudioOut out = new AudioOut(s);
out.start();                                            // eigener Faden, Puffer gut 90 ms
s.event(Soundscape.Kind.MOEWE, x, y, z, 1);
s.horn(Soundscape.Kind.NEBELHORN, x, y, z, 1, 3.0);
```

Ohne Lautsprecher: `s.render(Sekunden)` gibt verschränktes Stereo, `Wav.write(datei, ton, s.sampleRate())` schreibt es.

## Aufbau

| Klasse | Aufgabe |
|---|---|
| `Soundscape` | Eingänge, Betten (Wind, Äolstöne, See, Brandung, Regen, Glas, Dach, Maschine), Ereignisse, Mischer mit Ort und Richtung, Wand, Hall, Anpassung, Begrenzer, Pegel |
| `Voice` | Ereignisse: Brecher, Horn, Möwe, vorgerechneter Klang |
| `Thunder` | Donner aus dem Kanal (N-Wellen, Luft, Echos) |
| `Dsp` | Rauschen, Biquads, Glättung, Hall, Begrenzer |
| `Aeolian`, `Hum`, `Room` | singende Körper, dauernde Quellen, Räume |
| `AudioOut`, `Wav` | Lautsprecher (eigener Faden), WAV-Datei |
| `demo/SoundscapeDemo` | Fenster mit Ton, Reglern und Pegeln; `--bild` sechs Hörszenen als Spektrogramme, `--wav` dieselben als WAV |
| `demo/SoundscapeCheck` | Prüfung ohne Lautsprecher, endet mit „Alles in Ordnung.“ |
| `demo/Analysis` | FFT, Spektrum, Spitze, Schwerpunkt, Einsatz, Spektrogramm |

## Demo und Selbsttest

- `com.dan.soundscape.demo.SoundscapeDemo` spielt einen gedachten Turm mit Galerie; Beaufort, Regen, Ort des Ohrs, Knöpfe für Nebelhorn, Typhon, Möwe, Brecher, Donner nah und fern.
- `com.dan.soundscape.demo.SoundscapeCheck` prüft mit FFT: Rauschfarben, Filter, Wind nach Beaufort, Äolston nach Strouhal, Nebelhorn und Typhon nach KVR, Laufzeit, Abstand, Richtung und Ohrenlaufzeit, Luftdämpfung, Wand, Nachhallzeit, Regen auf Wasser und Glas, Donner, Möwe, Diesel, Begrenzer, Anpassung, seed, Stufen und Rechenzeit.

## Grenzen

- Kein Doppler, keine Bodenreflexion, kein Wind, der den Schall verweht; Richtung nur in der Ebene.
- Die Pegel der Betten sind geschätzt und an Erfahrungswerte angelehnt, nicht gemessen; Hörner und Typhon folgen den Vorschriften.
- Die Möwe ist eine Silbermöwe; andere Arten fehlen. Das Nebelhorn ist ein Diaphon nach Gehör, kein bestimmtes Horn.
- Der Hall ist ein Schroeder-Hall, kein Abbild des Raums.
