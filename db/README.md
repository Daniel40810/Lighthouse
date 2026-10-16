# db – Datenbank von Lighthouse

Schema `LIGHTHOUSE` auf PDBORCL (angelegt mit `USER.sql`, Rolle DB_CONNECT), Präfix `LH_`, Spatial mit SRID 8307. Der Zugang steht in `db.properties` (Vorlage `db.properties.example`, nie ins Git). Ohne Datenbank läuft die App weiter und speichert in den Ordner `daten/` des Projekts.

## Einrichten

Die App bringt einen Einrichter mit: Startbild oder Block DATEN, Knopf **Einrichten**. Er führt die Skripte in `sql/` der Reihe nach aus, trägt fehlende Spalten nach und prüft danach alles. Am Ende steht „alles in Ordnung“ oder genau, was fehlt. Er löscht nie etwas und lässt sich beliebig oft wiederholen.

| Skript | Inhalt |
|---|---|
| `sql/01_tabellen.sql` | 13 Tabellen, 12 Sequenzen, 11 Trigger (Sequence + Trigger, nie IDENTITY), Indizes |
| `sql/02_spatial.sql` | Metadaten für `LH_LIGHTHOUSE.LAGE` und der räumliche Index |
| `sql/03_sichten.sql` | `LH_LIGHTHOUSE_V` (Breite und Länge als Zahl), `LH_NACHBARN_V` (Türme bis 40 km, räumlich gerechnet), `LH_LETZTE_SITZUNG_V` |
| `sql/04_startdaten.sql` | Neun Türme mit Sektoren, zehn Wetterlagen, drei Regiebücher, zwei Kamerafahrten, 40 Presets der Packages. Erzeugt aus `com.dan.lighthouse.db.Startdaten` mit `tools.StartdatenSql`, nicht von Hand ändern |
| `sql/99_entfernen.sql` | Entfernt alle `LH_`-Objekte samt Inhalt. Nur von Hand für einen Neuanfang, die App führt es nie aus |

Dieselben Skripte lassen sich in SQL Developer als Skript ausführen (F5). Bei einem zweiten Lauf meldet Oracle ORA-00955 (Name schon vergeben); das ist harmlos.

Meldet der räumliche Index ORA-01031 oder ORA-13249, fehlt dem Schema ein direkt erteiltes Recht (eine Rolle reicht dafür nicht). Als DBA: `GRANT CREATE TABLE, CREATE SEQUENCE TO LIGHTHOUSE;`, dann noch einmal einrichten.

## Prüfen

`test/com/dan/lighthouse/test/DbPruefung.java` in NetBeans mit „Run File“ starten. Sie richtet ein, läuft den Einrichter ein zweites Mal, prüft mit ihm, speichert und lädt jede Art von Daten unter Namen „~Prüfung …“, vergleicht die Abstände aus `LH_NACHBARN_V` mit der eigenen Rechnung, übernimmt eine Datei aus `daten/` und räumt danach auf. Ende: „Alles in Ordnung.“

## Tabellen

| Tabelle | Inhalt |
|---|---|
| `LH_SCHEMA` | Ausbaustufe (Phase 15 = 1) |
| `LH_LIGHTHOUSE`, `LH_SECTOR` | Leuchttürme mit Lage, Kennung, Feuerhöhe, Tragweite, Quelle; Sektoren als Peilung von See |
| `LH_WEATHER_PRESET` | Wetterlagen, je Größe der Regie eine Spalte |
| `LH_SCENARIO`, `LH_SCENARIO_STEP` | Regiebücher; leere Wetterspalte = Größe bleibt |
| `LH_CAMERA_PATH`, `LH_CAMERA_POINT` | Kamerafahrten mit Stützpunkten in Szenenmetern |
| `LH_SNAPSHOT` | Gemerkte Ansichten (Taste V) mit Zustand (CLOB) und Vorschau (PNG im BLOB) |
| `LH_PACKAGE_PRESET`, `LH_PACKAGE_SETTING` | Presets je Package, ein öffentliches Feld je Zeile. Spalte `PAKET`, weil PACKAGE in Oracle ein Schlüsselwort ist |
| `LH_SESSION` | Sitzungen; der Zustand wird alle zwei Minuten und beim Ende gesichert („Weiter wie zuletzt“) |
| `LH_LOG` | Protokoll mit Szenenzeit und Ort; älter als 90 Tage räumt die App beim Start weg |

Mitgelieferte Zeilen (`HERKUNFT = 'MITGELIEFERT'`) stellt der Einrichter bei jedem Lauf wieder her; eigene Zeilen (`EIGEN`) fasst er nie an. Die App überschreibt mitgelieferte Einträge nicht, eine Abwandlung bekommt einen eigenen Namen.
