-- =====================================================================================================
-- Lighthouse · Datenbank · 03_sichten.sql
-- Drei Sichten: Türme mit Länge und Breite als Zahl, Nachbarn im Umkreis von 40 km (räumlich gerechnet),
-- und die letzte Sitzung für „Weiter wie zuletzt“. CREATE OR REPLACE: beliebig oft wiederholbar.
-- =====================================================================================================

-- Türme ohne Spatial-Typ: so lesen SQL Developer und die App die Lage als zwei Zahlen.
CREATE OR REPLACE VIEW LH_LIGHTHOUSE_V AS
SELECT t.ID,
       t.NAME,
       t.REVIER,
       t.LAGE.SDO_POINT.Y AS BREITE,
       t.LAGE.SDO_POINT.X AS LAENGE,
       t.FEUERHOEHE_M,
       t.TURMHOEHE_M,
       t.TRAGWEITE_SM,
       t.KENNUNG,
       t.OPTIK,
       t.BAUJAHR,
       t.AUSSER_BETRIEB,
       t.STAND_JAHR,
       t.FARBEN,
       t.BESCHREIBUNG,
       t.QUELLE,
       t.HERKUNFT,
       (SELECT COUNT(*) FROM LH_SECTOR s WHERE s.LIGHTHOUSE_ID = t.ID) AS SEKTOREN
FROM LH_LIGHTHOUSE t;

-- Nachbarn: alle Türme bis 40 km, Abstand in km auf dem Ellipsoid (SDO_GEOM, Toleranz 5 cm).
-- Die App nimmt daraus die Nachbarfeuer am Horizont; die Prüfung vergleicht mit ihrer eigenen Rechnung.
CREATE OR REPLACE VIEW LH_NACHBARN_V AS
SELECT a.NAME AS TURM,
       b.NAME AS NACHBAR,
       ROUND(MDSYS.SDO_GEOM.SDO_DISTANCE(a.LAGE, b.LAGE, 0.05, 'unit=KM'), 3) AS KM
FROM LH_LIGHTHOUSE a,
     LH_LIGHTHOUSE b
WHERE a.ID <> b.ID
  AND MDSYS.SDO_WITHIN_DISTANCE(b.LAGE, a.LAGE, 'distance=40 unit=KM') = 'TRUE';

-- Die jüngste Sitzung mit gesichertem Zustand, ob sauber beendet oder abgebrochen.
CREATE OR REPLACE VIEW LH_LETZTE_SITZUNG_V AS
SELECT s.ID,
       s.BEGINN,
       s.ENDE,
       s.SAUBER_BEENDET,
       s.ZUSTAND_ZEIT,
       s.SZENENZEIT,
       l.NAME AS TURM,
       c.NAME AS REGIEBUCH,
       s.SCENARIO_STUNDE
FROM LH_SESSION s
     LEFT JOIN LH_LIGHTHOUSE l ON l.ID = s.LIGHTHOUSE_ID
     LEFT JOIN LH_SCENARIO c ON c.ID = s.SCENARIO_ID
WHERE s.ZUSTAND IS NOT NULL
ORDER BY s.ID DESC
FETCH FIRST 1 ROWS ONLY;

-- Ende 03_sichten.sql
