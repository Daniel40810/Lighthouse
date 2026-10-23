# com.dan.lighthouse.person – der Wärter auf der Galerie

Seit Avatar Phase 15 (Werkbuch Avatar, F22 A) steht ein Mensch auf dem Roten Sand: Hauke, der Wärter, auf der Galerie
in 23 m Höhe, nass vom Sturm, im Licht des Feuers. Die Packages des Avatars liegen als Kopie unter `src/com/dan/`
(`gltf`, `avatar`, `hair`, `portrait`; im Projekt Avatar mit `ant lighthouse-kopie` erneuert). Nur hier, in der App,
treffen `com.dan.avatar` und `com.dan.hair` aufeinander, wie alle Packages in `wiring/`.

| Teil | Aufgabe |
|---|---|
| `Personen` | Lädt den Grundkörper im Hintergrund (Abbild in `daten/cache`), holt die Person aus der Datenbank AVATAR (`AV_API.WUERFELN(19)`, Schema LIGHTHOUSE mit der Rolle AVATAR_USER; ohne Datenbank derselbe Würfel in Java), führt das Regiebuch, rechnet und zeichnet je Bild. |
| `Figur` | Eine Person: Körper, Skelett, Bewegung, Verhalten, Kleidung, Haare, Nässe, Tropfen, Atem (aus dem Avatar Studio übernommen, ohne Menge und ohne Bildraum-Streuung). |
| `HairRig` | Haarkarten, Fliegenhaare und Bart an die Person (aus dem Avatar Studio). |
| `Galerie` | Die Galerie als `World`: Boden auf 23 m, Weg 2,6 m um die Laterne, Reling 3,8 m, Orte `G0` … `G330`, `Luv`, `Lee`, `Reling_Luv`, `See`, `Feuer`; Windschatten der Laterne; der hohe Erker im Nordosten ist ein Hindernis. |
| `GalerieStage` | Die Szene der Regie: `person`, `kamera` (Einstellung oder Ansicht), `ansicht`, `rund` (um die Laterne, `warte rund`), `wetter`, `uhr`, `warte vorspann`. |
| `PersonKamera` | Kamera für Menschen (Halbtotale bis Groß) als Folge-Kamera von Lighthouse; weicht Laterne und Erker aus. |
| `galerie_sturmnacht.txt` | Das Regiebuch „Sturmnacht auf der Galerie“ (rund 53 s). |

## Im Bild

Der Renderer zeichnet die Personen nach dem Leben (Tonnen, Schiffe, Möwen) und vor Gischt, Regen und Nebel; sie
schreiben Tiefe (Durchgang 7). Licht, vorbelichtet wie der Turm (`Renderer.personLight`):

- **Führungslicht:** am Tag die Sonne; nachts das Feuer im Nahfeld der Linse (`Beacon.Snapshot.nearIrradiance` am Kopf:
  Lampe und vorbeiziehende Strahlen) und das leuchtende Glas der Laterne (rund 0,8 sr).
- **Aufhellung:** Himmel oben und an der Kimm, dazu die Rückstrahlung von Boden und Brüstung [G, am Bild abgestimmt].
- **Kante:** der Mond. Blitze hellen auf.
- **Auge:** Nah an der leuchtenden Laterne (unter 6 m vom Glas) passt es sich an Glas und Gesicht an und sieht mit den
  Zapfen (vorher war es nachts an den dunklen Himmel angepasst, das Glas brannte aus und alles war grau).
- **Schatten:** eine Schattenkarte des Führungslichts nur für die Person, mit Tiefe −1..1 (die Szene rechnet mit umgekehrter
  Tiefe 0..1; die Abbildung wird dafür kurz umgeschaltet). Die Person wirft keinen Schatten auf die Galerie.

## Bedienung

Im Bedienfeld unter LEBEN, „Wärter auf der Galerie“: Wärter zeigen, „Sturmnacht im Kino“, „Zum Wärter“.

## Prüfen

- `test/…/PruefungPersonen` (Teil von `ant pruefung`): Galerie, Wege und Erker, Windschatten, Kamera, Wärter, Regiebuch,
  Kreisweg, Packages, Durchgang im Renderer.
- `tools.Galeriebild datei.png [breite] [Stufe]`: das Regiebuch ohne Fenster, ein Bild nach jedem Schnitt, dazu Bildzeit
  mit und ohne Person. `--blick datei.png [breite] [Einstellung] [Winkel]` gibt ein Bild an der Reling im Luv.

## Grenzen

- Ein Weg nur auf der Galerie, gerade oder im Kreis um die Laterne; keine Treppe, keine Tür in die Laterne.
- Die Person wirft keinen Schatten auf Boden und Brüstung; Gischt und Regen treffen sie über das Klima am Ort, nicht über
  die Teilchen der Szene.
- Der Kapuzenrand nimmt höchstens 9 m/s Wind an (sonst schlägt er übers Gesicht).
- Keine Bildraum-Streuung der Haut (ein Ziel), vorab integrierte Streuung wie auf „Niedrig“ im Studio.
