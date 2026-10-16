# com.dan.buoyancy – Tonnen und Schiffe

Alles, was auf dem Wasser schwimmt: Leuchttonnen an einem Fahrwasser und gelegentlich ein Küstenmotorschiff, das es entlangfährt, mit Tauchen, Stampfen und Rollen auf einer beliebigen Wasseroberfläche. Das Package braucht nur Java 21, der Ordner `gl/` zusätzlich JOGL, der Ordner `demo/` zusätzlich `FStyle.jar`. Zum Herauskopieren reicht `src/com/dan/buoyancy/`.

## Was es kann

- **Schwimmen nach dem Streifenmodell:** Ein Rumpf ist eine Menge senkrechter Säulen. Jede trägt so viel, wie Wasser über ihrem Kiel steht (ρ g a d), gedämpft nach der Geschwindigkeit gegen das Wasser. Daraus kommen Tauchen, Stampfen und Rollen; die Wasserlinie liefert die Formstabilität, der Schwerpunkt die Gewichtsstabilität, Schlingerkiele eine quadratische Rolldämpfung. Lange Dünung reitet der Körper ab, kurze Wellen glättet er.
- **Geprüfte Hydrostatik:** Die Tonne (2,4 m, 5,1 t) schwimmt auf 1,10 m, taucht mit 2,6 s und rollt mit 3,2 s; der Kümo (82 × 12,5 m, 4 277 t) schwimmt auf 4,46 m ohne Trimm, taucht mit 5,8 s, stampft mit 5,4 s und rollt mit 12 s (GM 0,86 m). Alles auf ein paar Prozent wie von Hand gerechnet.
- **Tonnen nach IALA, Region A:** Backbord rot mit Stumpf-Toppzeichen (`Fl R 4s`), Steuerbord grün mit Kegel (`Fl G 4s`), Ansteuerungstonne rot-weiß mit Ball (`Iso 8s`), Kardinaltonne Nord (`Q`). An der Kette mit rund 40 s Eigenperiode, Strom und Wind ziehen sie ein paar Meter aus der Mitte. `layFairway` legt ein Fahrwasser aus.
- **Tonnenfeuer:** `Rhythm` liest kurze Kennungen: `F`, `Fl`, `Fl(n)`, `LFl`, `Q`, `VQ`, `Iso`, `Oc`, `Mo(X)` mit Farbe und Periode.
- **Schiff mit Positionslichtern nach KVR:** zwei Topplichter (das achtere höher) über 225°, Seitenlichter rot und grün über je 112,5°, Hecklicht über 135°, Sektoren mit 2° weichem Rand. Lichtstärken nach Anhang I: 94 cd für 6 sm, 12 cd für 3 sm.
- **Route und Verkehr:** Ein Schiff fährt seine Wegpunkte mit begrenzter Drehrate ab und giert im Seegang. Im Mittel alle 25 Minuten kommt eines (lognormal gestreut), im Sturm seltener. Am Bug meldet es Stöße ins Wasser (`onWake`) für Ringwellen.
- **Brücke als Kamera:** `bridgeView()` gibt Auge, vorn, oben und rechts mit Stampfen und Rollen; `windowWorld(i)` die drei Frontscheiben in der Welt; `Wiper` schwingt weich und gibt die überstrichenen Punkte zum Wegwischen von Tropfen.
- **Netze aus Code:** `Mesh` baut Rumpf mit Sprung und Wasserpass, Lukendeckel, Back, Deckshaus mit Fenstern, Brückennocken, Schornstein, Masten und Lampen, die Brücke von innen mit Pult und Anzeigen, den Wischer und die vier Tonnen mit Gittergerüst und Toppzeichen.
- **Presets:** `calm()`, `breeze()`, `gale()`, `storm()`, `beaufort(n)`. **Reproduzierbar:** gleicher `seed`, gleiche Schiffe. **Stufen:** `Quality.LOW` bis `ULTRA` regeln, wie oft das Meer abgefragt wird, und wie viele Schiffe gleichzeitig fahren.

## Benutzung

```java
Buoyancy b = Buoyancy.beaufort(5);
b.sea = new Sea() {                       // das eigene Meer hineingeben
    public double height(double x, double z) { return ocean.heightAt(x, z); }
    public double[] velocity(double x, double z) { return ocean.velocityAt(x, z); }
    public double level() { return tide; }
};
b.fairwayX = 900; b.fairwayDeg = 330;
b.layFairway(220, 900, 4);
b.onWake = (x, z, s) -> ocean.addImpulse(x, z, s);
b.step(dt);                                // je Bild
for (LightPoint l : b.lights(eyeX, eyeY, eyeZ)) drawGlare(l);

Ship s = b.spawn(true, 3000);              // ein Schiff von Hand
double[][] v = s.bridgeView();             // Auge, vorn, oben, rechts
s.wiper.on = true;
```

## Aufbau

| Klasse | Aufgabe |
|---|---|
| `Floater` | Streifenmodell: Säulen, Hydrostatik, Eigenperioden, Integration, Modellmatrix |
| `Buoy` | Leuchttonne: Säulen, Verankerung, Feuer |
| `Ship` | Kümo: Säulen aus Wasserlinie und Tiefgang, Route, Lichter nach KVR, Brücke, Frontscheiben |
| `Wiper` | Scheibenwischer: Winkel, Blatt, überstrichene Punkte |
| `Rhythm` | Kennung eines Tonnenfeuers |
| `Mesh` | Netze in Java |
| `Buoyancy` | Tonnen, Schiffe, Verkehr, Lichter, Presets |
| `Sea`, `LightPoint` | eigene kleine Schnittstellen |
| `gl/BuoyancyGl` | Netze hochladen und mit ihrer Modellmatrix zeichnen, Selbsttest |
| `demo/BuoyancyDemo` | Fenster mit Reglern; `--bild datei.png [breite]` schreibt fünf Lagen und die Bewegungen als Kurven |
| `demo/BuoyancyScene`, `DemoSea` | eigene See aus zwölf Wellenzügen, Himmel, Lichtpunkte |
| `demo/BuoyancyCheck` | Prüfung ohne Fenster, endet mit „Alles in Ordnung.“ |

## Demo und Selbsttest

- `com.dan.buoyancy.demo.BuoyancyDemo` zeigt das Fahrwasser im Seegang, nachts mit Feuern und Positionslichtern, und den Blick von der Brücke mit Wischer.
- `com.dan.buoyancy.demo.BuoyancyCheck` prüft Tiefgang, Stabilität, Eigenperioden, das Mitgehen in Dünung, die Verankerung bei Bft 10, die Route, die Lichtsektoren, die Kennungen, den Wischer, seed und Stabilität bei Bft 12.
- `BuoyancyGl.selfCheck` legt 64 Punkte mit der Modellmatrix auf der Grafikkarte in die Welt und vergleicht mit dem Prozessor.

## Grenzen

- Die Säulen tragen senkrecht; Wellenkräfte quer (Abdrift, Schlagen der See) und das Gieren aus Wellen sind nur genähert.
- Das Schiff fährt nach Route, nicht nach Ruder und Maschine; es weicht niemandem aus.
- Keine Bugwelle und kein Kielwasser als Netz; nur Stöße ins Wasser für ein Meer, das Ringwellen kann.
- Die Brücke von innen ist ein Rahmen mit Pult, kein ausgebautes Steuerhaus.
