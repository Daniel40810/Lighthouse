# com.dan.lighthouse.wiring – die Verdrahtung

Hier reden die Packages miteinander, und nur hier. Kein Element-Package (`ocean`, `spray`, `cloud`, `rain`, `wetglass`, `lightning`, `beacon`, `sky`, `fog`, `weather` und die Zugaben) kennt ein anderes oder die App. Jedes bietet Eingänge an und meldet Ereignisse; die App verbindet sie in `Wiring`.

## Die Schnittstellen

| Schnittstelle | Inhalt | Gibt | Nimmt |
|---|---|---|---|
| `Wind` | Geschwindigkeit, Richtung, Böigkeit, Bft | weather | ocean, spray, cloud, rain, wetglass, fog, flock, soundscape |
| `Clock` | Laufzeit, Schritt, Uhrzeit, Ort | App | alle |
| `Lighting` | Sonne, Mond, Himmel, Dunst | sky | ocean, cloud, spray, rain, Szene |
| `SpotLight` | Leuchtfeuer: Lage, Achse, Öffnung, Farbe, Stärke | beacon | cloud, rain, spray, fog, ocean, wetglass |
| `ScatterField` | Streudichte am Ort | rain, fog, spray | beacon |
| `SurfaceHit` | Einschlag: Ort, Masse, Geschwindigkeit, Art | rain, spray | ocean, wetglass, weathering |
| `Collider` | fester Körper als Abstandsfeld | App | spray, ocean, rain |
| `WaterLevel` | Wasserstand | Gezeiten (`scene/Tide`, seit Phase 5) | ocean, weathering, buoyancy, Kamera |
| `Flash` | Blitz | lightning | cloud, ocean, wetglass, weathering, sky, soundscape |
| `SoundEvent` | Klangereignis | spray, fog, flock, lightning | soundscape |

`Beaufort` gibt die gemeinsame Sprache für alle Presets: Grenzen nach WMO und die deutschen Bezeichnungen.

## Regeln

- Jede Schnittstelle besteht nur aus Zahlen, Aufzählungen und Funktionen. Keine Klassen aus anderen Packages.
- Braucht ein Package dieselbe Form, bringt es eine eigene Kopie mit gleichem Namen mit. `Wiring` übersetzt mit einer Zeile. Die Kopie ist gewollt: So bleibt das Package zum Herauskopieren.
- Jede neue Verbindung wird mit `link(von, an, über, Phase)` eingetragen. Das Bedienfeld zeigt die Liste.
- Die `feed(...)`-Methoden geben je Bild die Eingänge an ein Package: Meer, Himmel, Wolken, Verwitterung. `feedObstacles` setzt die Körper im Wasser nach dem Wasserstand (Sockel immer, die Manschette nur bei Ebbe).
