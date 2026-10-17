-- =====================================================================================================
-- Lighthouse · Datenbank · 04_startdaten.sql
-- ERZEUGT aus com.dan.lighthouse.db.Startdaten (tools.StartdatenSql) – nicht von Hand ändern.
-- Mitgelieferte Zeilen (HERKUNFT = 'MITGELIEFERT') werden angelegt oder wiederhergestellt; eigene Zeilen
-- bleiben unberührt. Beliebig oft wiederholbar.
-- =====================================================================================================

-- Die Quellen enthalten „&“: in SQL Developer keine Ersetzungsvariablen
SET DEFINE OFF

-- ----------------------------------------------------------------------------------------- Leuchttürme
MERGE INTO LH_LIGHTHOUSE t
USING (SELECT 'Roter Sand' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.REVIER = 'NORDSEE', t.LAGE = MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.07917, 53.855, NULL), NULL, NULL),
    t.FEUERHOEHE_M = 24, t.TURMHOEHE_M = 28, t.TRAGWEITE_SM = 9, t.KENNUNG = 'F WRG', t.OPTIK = 'DRUM', t.BAUJAHR = 1885, t.AUSSER_BETRIEB = 1964, t.STAND_JAHR = 1985,
    t.FARBEN = 'rot-weiß gebändert (Weiß-Rot-Weiß-Rot-Weiß), ca. 8 m hoher schwarzer Sockel, schwarze Laterne/Dach (Kupferkuppel), drei Erker',
    t.BESCHREIBUNG = '1880–1885 nach Entwurf von Carl Friedrich Hanckes auf einem Senkkasten in der Außenweser errichtet; gilt als erstes Offshore-Bauwerk der Welt. Heute Tagessichtzeichen und Denkmal. Kennung und Sektoren nach dem Verzeichnis von 1985 (F21); Lage wie in der Szene.',
    t.QUELLE = 'https://www.deutsche-leuchtfeuer.de/binnen/weser/roter-sand.html · https://de.wikipedia.org/wiki/Leuchtturm_Roter_Sand · https://en.wikipedia.org/wiki/Roter_Sand_Lighthouse · https://www.leuchtturm-atlas.de/HTML/RoSandT.html · https://www.ibiblio.org/lighthouse/debrh.htm'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, REVIER, LAGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT)
  VALUES ('Roter Sand', 'NORDSEE', MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.07917, 53.855, NULL), NULL, NULL), 24, 28, 9, 'F WRG', 'DRUM', 1885, 1964, 1985, 'rot-weiß gebändert (Weiß-Rot-Weiß-Rot-Weiß), ca. 8 m hoher schwarzer Sockel, schwarze Laterne/Dach (Kupferkuppel), drei Erker', '1880–1885 nach Entwurf von Carl Friedrich Hanckes auf einem Senkkasten in der Außenweser errichtet; gilt als erstes Offshore-Bauwerk der Welt. Heute Tagessichtzeichen und Denkmal. Kennung und Sektoren nach dem Verzeichnis von 1985 (F21); Lage wie in der Szene.', 'https://www.deutsche-leuchtfeuer.de/binnen/weser/roter-sand.html · https://de.wikipedia.org/wiki/Leuchtturm_Roter_Sand · https://en.wikipedia.org/wiki/Roter_Sand_Lighthouse · https://www.leuchtturm-atlas.de/HTML/RoSandT.html · https://www.ibiblio.org/lighthouse/debrh.htm', 'MITGELIEFERT');
DELETE FROM LH_SECTOR WHERE LIGHTHOUSE_ID IN (SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = 'Roter Sand' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 1, 167.5, 185, 'W', 'SEKTOR', 9, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Roter Sand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 2, 185, 225, 'R', 'SEKTOR', 7, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Roter Sand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 3, 225, 254.5, 'W', 'SEKTOR', 9, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Roter Sand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 4, 254.5, 293.5, 'R', 'SEKTOR', 7, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Roter Sand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 5, 293.5, 312, 'W', 'SEKTOR', 9, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Roter Sand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 6, 312, 324, 'R', 'SEKTOR', 7, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Roter Sand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 7, 324, 328, 'W', 'LEITSEKTOR', 9, 'Leitsektor' FROM LH_LIGHTHOUSE WHERE NAME = 'Roter Sand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 8, 328, 341, 'G', 'SEKTOR', 6, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Roter Sand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 9, 341, 167.5, 'W', 'SCHWACH', 3, 'Schwachlicht' FROM LH_LIGHTHOUSE WHERE NAME = 'Roter Sand' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_LIGHTHOUSE t
USING (SELECT 'Alte Weser' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.REVIER = 'NORDSEE', t.LAGE = MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.12758, 53.86333, NULL), NULL, NULL),
    t.FEUERHOEHE_M = 33.1, t.TURMHOEHE_M = 38.13, t.TRAGWEITE_SM = 23, t.KENNUNG = 'F WRG', t.OPTIK = 'DRUM', t.BAUJAHR = 1964, t.AUSSER_BETRIEB = NULL, t.STAND_JAHR = 2026,
    t.FARBEN = 'roter Turm mit zwei weißen Bändern auf schwarzem Sockel, weiß/rote Aufbauten (Wohn-/Maschinendeck), grüne Laterne mit Radarantenne',
    t.BESCHREIBUNG = '1961–1964 als Ersatz für Roter Sand errichteter Leuchtturm in der Außenweser, in Betrieb seit 1. September 1964; Leit- und Quermarkenfeuer mit Nebelhorn.',
    t.QUELLE = 'https://www.deutsche-leuchtfeuer.de/binnen/weser/alte-weser.html · https://www.baken-net.de/alte-weser.htm · https://www.leuchtturm-atlas.de/HTML/AlWeseT.html · https://en.wikipedia.org/wiki/Alte_Weser_Lighthouse · https://www.ibiblio.org/lighthouse/debrh.htm'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, REVIER, LAGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT)
  VALUES ('Alte Weser', 'NORDSEE', MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.12758, 53.86333, NULL), NULL, NULL), 33.1, 38.13, 23, 'F WRG', 'DRUM', 1964, NULL, 2026, 'roter Turm mit zwei weißen Bändern auf schwarzem Sockel, weiß/rote Aufbauten (Wohn-/Maschinendeck), grüne Laterne mit Radarantenne', '1961–1964 als Ersatz für Roter Sand errichteter Leuchtturm in der Außenweser, in Betrieb seit 1. September 1964; Leit- und Quermarkenfeuer mit Nebelhorn.', 'https://www.deutsche-leuchtfeuer.de/binnen/weser/alte-weser.html · https://www.baken-net.de/alte-weser.htm · https://www.leuchtturm-atlas.de/HTML/AlWeseT.html · https://en.wikipedia.org/wiki/Alte_Weser_Lighthouse · https://www.ibiblio.org/lighthouse/debrh.htm', 'MITGELIEFERT');
DELETE FROM LH_SECTOR WHERE LIGHTHOUSE_ID IN (SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 1, 3, 17, 'W', 'SEKTOR', 23, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 2, 17, 45, 'G', 'SEKTOR', 18, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 3, 45, 74, 'W', 'SEKTOR', 23, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 4, 74, 121, 'G', 'SEKTOR', 18, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 5, 121, 126, 'W', 'SEKTOR', 23, 'Leitsektor?' FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 6, 126, 140, 'R', 'SEKTOR', 19, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 7, 140, 175, 'G', 'SEKTOR', 18, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 8, 175, 183, 'W', 'SEKTOR', 23, 'Leitsektor?' FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 9, 183, 196, 'R', 'SEKTOR', 19, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 10, 196, 238, 'W', 'SEKTOR', 23, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 11, 288, 352, 'W', 'SEKTOR', 23, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 12, 352, 3, 'R', 'SEKTOR', 19, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Alte Weser' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_LIGHTHOUSE t
USING (SELECT 'Hohe Weg' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.REVIER = 'NORDSEE', t.LAGE = MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.24322, 53.71233, NULL), NULL, NULL),
    t.FEUERHOEHE_M = 29, t.TURMHOEHE_M = 36, t.TRAGWEITE_SM = 17, t.KENNUNG = 'F WRG', t.OPTIK = 'DRUM', t.BAUJAHR = 1856, t.AUSSER_BETRIEB = NULL, t.STAND_JAHR = 2026,
    t.FARBEN = 'roter achteckiger Turm (seit 1996 rot lackierte Aluminiumverkleidung), weiße Absetzungen, zwei Galerien, grüne Laterne mit Radarantenne',
    t.BESCHREIBUNG = '1854–1856 erbauter achteckiger Ziegelturm auf der Sandbank Hohe Weg in der Außenweser, ältestes feststehendes Leuchtfeuer der deutschen Nordsee; seit 1973 automatisiert.',
    t.QUELLE = 'https://www.deutsche-leuchtfeuer.de/binnen/weser/hohe-weg.html · https://www.baken-net.de/hohe-weg.htm · https://www.leuchtturm-atlas.de/HTML/HoWeg_T.html · https://en.wikipedia.org/wiki/Hohe_Weg_Lighthouse · https://de.wikipedia.org/wiki/Leuchtturm_Hohe_Weg'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, REVIER, LAGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT)
  VALUES ('Hohe Weg', 'NORDSEE', MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.24322, 53.71233, NULL), NULL, NULL), 29, 36, 17, 'F WRG', 'DRUM', 1856, NULL, 2026, 'roter achteckiger Turm (seit 1996 rot lackierte Aluminiumverkleidung), weiße Absetzungen, zwei Galerien, grüne Laterne mit Radarantenne', '1854–1856 erbauter achteckiger Ziegelturm auf der Sandbank Hohe Weg in der Außenweser, ältestes feststehendes Leuchtfeuer der deutschen Nordsee; seit 1973 automatisiert.', 'https://www.deutsche-leuchtfeuer.de/binnen/weser/hohe-weg.html · https://www.baken-net.de/hohe-weg.htm · https://www.leuchtturm-atlas.de/HTML/HoWeg_T.html · https://en.wikipedia.org/wiki/Hohe_Weg_Lighthouse · https://de.wikipedia.org/wiki/Leuchtturm_Hohe_Weg', 'MITGELIEFERT');
DELETE FROM LH_SECTOR WHERE LIGHTHOUSE_ID IN (SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = 'Hohe Weg' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 1, 102, 138.5, 'W', 'SEKTOR', 17, 'Orientierungsfeuer' FROM LH_LIGHTHOUSE WHERE NAME = 'Hohe Weg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 2, 138.5, 142.5, 'G', 'SEKTOR', 13, 'Stb-Warnsektor' FROM LH_LIGHTHOUSE WHERE NAME = 'Hohe Weg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 3, 142.5, 145.5, 'W', 'LEITSEKTOR', 17, 'Leitsektor' FROM LH_LIGHTHOUSE WHERE NAME = 'Hohe Weg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 4, 145.5, 184, 'R', 'SEKTOR', 14, 'Bb-Warn- und Quermarkensektor' FROM LH_LIGHTHOUSE WHERE NAME = 'Hohe Weg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 5, 184, 278.5, 'W', 'SEKTOR', 17, 'Orientierungsfeuer' FROM LH_LIGHTHOUSE WHERE NAME = 'Hohe Weg' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_LIGHTHOUSE t
USING (SELECT 'Westerheversand' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.REVIER = 'NORDSEE', t.LAGE = MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.63992, 54.37336, NULL), NULL, NULL),
    t.FEUERHOEHE_M = 41, t.TURMHOEHE_M = 40, t.TRAGWEITE_SM = 21, t.KENNUNG = 'Oc(3) WRG 15s', t.OPTIK = 'DRUM', t.BAUJAHR = 1906, t.AUSSER_BETRIEB = NULL, t.STAND_JAHR = 2026,
    t.FARBEN = 'rot mit zwei weißen Bändern, zwei Galerien, schwarze Laterne und Galerie',
    t.BESCHREIBUNG = '1906/07 aus gusseisernen Tübbings auf einer Warft vor Westerhever (Eiderstedt) errichtet, Feuer seit 26. Mai 1908; See-, Quermarken- und Leitfeuer und Wahrzeichen Eiderstedts.',
    t.QUELLE = 'https://www.deutsche-leuchtfeuer.de/nordsee/westerheversand.html · https://www.baken-net.de/westerhever.htm · https://www.leuchtturm-atlas.de/HTML/WerhevT.html · https://en.wikipedia.org/wiki/Westerheversand_Lighthouse · https://de.wikipedia.org/wiki/Leuchtturm_Westerheversand'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, REVIER, LAGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT)
  VALUES ('Westerheversand', 'NORDSEE', MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.63992, 54.37336, NULL), NULL, NULL), 41, 40, 21, 'Oc(3) WRG 15s', 'DRUM', 1906, NULL, 2026, 'rot mit zwei weißen Bändern, zwei Galerien, schwarze Laterne und Galerie', '1906/07 aus gusseisernen Tübbings auf einer Warft vor Westerhever (Eiderstedt) errichtet, Feuer seit 26. Mai 1908; See-, Quermarken- und Leitfeuer und Wahrzeichen Eiderstedts.', 'https://www.deutsche-leuchtfeuer.de/nordsee/westerheversand.html · https://www.baken-net.de/westerhever.htm · https://www.leuchtturm-atlas.de/HTML/WerhevT.html · https://en.wikipedia.org/wiki/Westerheversand_Lighthouse · https://de.wikipedia.org/wiki/Leuchtturm_Westerheversand', 'MITGELIEFERT');
DELETE FROM LH_SECTOR WHERE LIGHTHOUSE_ID IN (SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = 'Westerheversand' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 1, 12.2, 69, 'W', 'SEKTOR', 21, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Westerheversand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 2, 69, 85, 'G', 'SEKTOR', 16, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Westerheversand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 3, 85, 86, 'W', 'LEITSEKTOR', 21, 'Leitsektor (Mittelhever)' FROM LH_LIGHTHOUSE WHERE NAME = 'Westerheversand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 4, 86, 107, 'R', 'SEKTOR', 17, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Westerheversand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 5, 107, 150, 'W', 'SEKTOR', 21, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Westerheversand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 6, 150, 207, 'R', 'SEKTOR', 17, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Westerheversand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 7, 207, 233, 'W', 'SEKTOR', 21, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Westerheversand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 8, 233, 248, 'R', 'SEKTOR', 17, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Westerheversand' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_LIGHTHOUSE t
USING (SELECT 'Kiel' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.REVIER = 'OSTSEE', t.LAGE = MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(10.27361, 54.5, NULL), NULL, NULL),
    t.FEUERHOEHE_M = 29.25, t.TURMHOEHE_M = 33.5, t.TRAGWEITE_SM = 17, t.KENNUNG = 'Iso WRG 6s', t.OPTIK = 'DRUM', t.BAUJAHR = 1967, t.AUSSER_BETRIEB = NULL, t.STAND_JAHR = 2026,
    t.FARBEN = 'roter runder Aluminiumturm mit weißem Mittelband, auf drei Stahlbeton-Fundamentkörpern',
    t.BESCHREIBUNG = '1964–1967 auf der Position des Feuerschiffs Kiel errichteter Offshore-Leuchtturm in der Kieler Außenförde, in Betrieb seit 5. Juli 1967; Leit- und Orientierungsfeuer und Lotsenstation. Sektoren teils geschätzt [G]. Feuerhöhe über Mittelwasser angenommen.',
    t.QUELLE = 'https://www.deutsche-leuchtfeuer.de/ostsee/kiel.html · https://www.leuchtturm-atlas.de/HTML/Kiel__T.html · https://de.wikipedia.org/wiki/Leuchtturm_Kiel · https://cdn.marinetraffic.com/ro/ais/details/lights/1000006217 · https://www.ibiblio.org/lighthouse/deesh.htm · https://www.kiel.de/en/english_Kopie/outer_fjord.php'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, REVIER, LAGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT)
  VALUES ('Kiel', 'OSTSEE', MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(10.27361, 54.5, NULL), NULL, NULL), 29.25, 33.5, 17, 'Iso WRG 6s', 'DRUM', 1967, NULL, 2026, 'roter runder Aluminiumturm mit weißem Mittelband, auf drei Stahlbeton-Fundamentkörpern', '1964–1967 auf der Position des Feuerschiffs Kiel errichteter Offshore-Leuchtturm in der Kieler Außenförde, in Betrieb seit 5. Juli 1967; Leit- und Orientierungsfeuer und Lotsenstation. Sektoren teils geschätzt [G]. Feuerhöhe über Mittelwasser angenommen.', 'https://www.deutsche-leuchtfeuer.de/ostsee/kiel.html · https://www.leuchtturm-atlas.de/HTML/Kiel__T.html · https://de.wikipedia.org/wiki/Leuchtturm_Kiel · https://cdn.marinetraffic.com/ro/ais/details/lights/1000006217 · https://www.ibiblio.org/lighthouse/deesh.htm · https://www.kiel.de/en/english_Kopie/outer_fjord.php', 'MITGELIEFERT');
DELETE FROM LH_SECTOR WHERE LIGHTHOUSE_ID IN (SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 1, 148.5, 220, 'W', 'SEKTOR', 17, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 2, 220, 246.5, 'R', 'SEKTOR', 14, 'Warnsektor' FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 3, 246.5, 295, 'W', 'SEKTOR', 17, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 4, 295, 358, 'R', 'SEKTOR', 14, 'Warnsektor' FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 5, 358, 25.5, 'W', 'SEKTOR', 17, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 6, 25.5, 56, 'G', 'SEKTOR', 13, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 7, 56, 71, 'R', 'SEKTOR', 14, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 8, 71, 88, 'R', 'SEKTOR', 14, 'Warnsektor' FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 9, 88, 91.3, 'W', 'SEKTOR', 17, 'Leitsektor?' FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 10, 91.3, 148.5, 'G', 'SEKTOR', 13, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Kiel' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_LIGHTHOUSE t
USING (SELECT 'Bülk' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.REVIER = 'OSTSEE', t.LAGE = MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(10.19728, 54.45522, NULL), NULL, NULL),
    t.FEUERHOEHE_M = 29.5, t.TURMHOEHE_M = 25.6, t.TRAGWEITE_SM = 14, t.KENNUNG = 'Fl WRG 3s', t.OPTIK = 'DRUM', t.BAUJAHR = 1865, t.AUSSER_BETRIEB = NULL, t.STAND_JAHR = 2026,
    t.FARBEN = 'weißer runder Turm mit zwei grünen Bändern (seit 2020/21, vorher schwarzes Band), grüne untere Galerie, gelber Backsteinsockel, weiße Laterne',
    t.BESCHREIBUNG = '1862/63 unter dänischer Verwaltung begonnener, 1865 von Preußen vollendeter Turm an der Bülker Huk, ältester Leuchtturm der Kieler Förde; Orientierungsfeuer mit Aussichtsplattform. Feuerhöhe über Mittelwasser angenommen.',
    t.QUELLE = 'https://www.deutsche-leuchtfeuer.de/ostsee/buelk.html · https://www.leuchtturm-atlas.de/HTML/Buelk_T.html · https://de.wikipedia.org/wiki/Leuchtturm_B%C3%BClk · https://en.wikipedia.org/wiki/B%C3%BClk_Lighthouse · https://www.ibiblio.org/lighthouse/deesh.htm'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, REVIER, LAGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT)
  VALUES ('Bülk', 'OSTSEE', MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(10.19728, 54.45522, NULL), NULL, NULL), 29.5, 25.6, 14, 'Fl WRG 3s', 'DRUM', 1865, NULL, 2026, 'weißer runder Turm mit zwei grünen Bändern (seit 2020/21, vorher schwarzes Band), grüne untere Galerie, gelber Backsteinsockel, weiße Laterne', '1862/63 unter dänischer Verwaltung begonnener, 1865 von Preußen vollendeter Turm an der Bülker Huk, ältester Leuchtturm der Kieler Förde; Orientierungsfeuer mit Aussichtsplattform. Feuerhöhe über Mittelwasser angenommen.', 'https://www.deutsche-leuchtfeuer.de/ostsee/buelk.html · https://www.leuchtturm-atlas.de/HTML/Buelk_T.html · https://de.wikipedia.org/wiki/Leuchtturm_B%C3%BClk · https://en.wikipedia.org/wiki/B%C3%BClk_Lighthouse · https://www.ibiblio.org/lighthouse/deesh.htm', 'MITGELIEFERT');
DELETE FROM LH_SECTOR WHERE LIGHTHOUSE_ID IN (SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = 'Bülk' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 1, 127, 146, 'W', 'SEKTOR', 14, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Bülk' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 2, 146, 213, 'R', 'SEKTOR', 11, 'Warnsektor Stollergrund (Zuordnung abgeleitet)' FROM LH_LIGHTHOUSE WHERE NAME = 'Bülk' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 3, 213, 228, 'G', 'SEKTOR', 10, 'Kabeltrasse zum Leuchtturm Kiel' FROM LH_LIGHTHOUSE WHERE NAME = 'Bülk' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 4, 228, 235.5, 'W', 'SEKTOR', 14, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Bülk' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 5, 235.5, 238.5, 'R', 'SEKTOR', 11, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Bülk' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 6, 238.5, 262, 'W', 'SEKTOR', 14, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Bülk' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 7, 262, 43, 'G', 'SEKTOR', 10, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Bülk' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_LIGHTHOUSE t
USING (SELECT 'Flügge' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.REVIER = 'OSTSEE', t.LAGE = MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(11.01769, 54.44092, NULL), NULL, NULL),
    t.FEUERHOEHE_M = 38, t.TURMHOEHE_M = 37, t.TRAGWEITE_SM = 17, t.KENNUNG = 'Oc(4) W 20s', t.OPTIK = 'DRUM', t.BAUJAHR = 1916, t.AUSSER_BETRIEB = NULL, t.STAND_JAHR = 2026,
    t.FARBEN = 'unverputztes gelb-rotes Backsteinmauerwerk (seit 2010 wieder, 1977–2009 rot-weiß verkleidet), rote Laterne, roter Wachraum und Galerien',
    t.BESCHREIBUNG = '1914/15 erbauter, 1916 in Betrieb genommener achteckiger Backsteinturm im Südwesten Fehmarns; Seefeuer und zugleich Oberfeuer der Richtfeuerlinie Strukkamphuk zur Fehmarnsundbrücke. Feuerhöhe über Mittelwasser angenommen.',
    t.QUELLE = 'https://www.deutsche-leuchtfeuer.de/ostsee/fluegge.html · https://www.leuchtturm-atlas.de/HTML/FlueggT.html · https://de.wikipedia.org/wiki/Leuchtturm_Fl%C3%BCgge · https://www.ibiblio.org/lighthouse/deesh.htm'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, REVIER, LAGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT)
  VALUES ('Flügge', 'OSTSEE', MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(11.01769, 54.44092, NULL), NULL, NULL), 38, 37, 17, 'Oc(4) W 20s', 'DRUM', 1916, NULL, 2026, 'unverputztes gelb-rotes Backsteinmauerwerk (seit 2010 wieder, 1977–2009 rot-weiß verkleidet), rote Laterne, roter Wachraum und Galerien', '1914/15 erbauter, 1916 in Betrieb genommener achteckiger Backsteinturm im Südwesten Fehmarns; Seefeuer und zugleich Oberfeuer der Richtfeuerlinie Strukkamphuk zur Fehmarnsundbrücke. Feuerhöhe über Mittelwasser angenommen.', 'https://www.deutsche-leuchtfeuer.de/ostsee/fluegge.html · https://www.leuchtturm-atlas.de/HTML/FlueggT.html · https://de.wikipedia.org/wiki/Leuchtturm_Fl%C3%BCgge · https://www.ibiblio.org/lighthouse/deesh.htm', 'MITGELIEFERT');
DELETE FROM LH_SECTOR WHERE LIGHTHOUSE_ID IN (SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = 'Flügge' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 1, 245, 180, 'W', 'SEKTOR', 17, 'Seefeuer (Sichtbereich)' FROM LH_LIGHTHOUSE WHERE NAME = 'Flügge' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_LIGHTHOUSE t
USING (SELECT 'Dahmeshöved' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.REVIER = 'OSTSEE', t.LAGE = MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(11.09025, 54.20178, NULL), NULL, NULL),
    t.FEUERHOEHE_M = 33.7, t.TURMHOEHE_M = 28.8, t.TRAGWEITE_SM = 23, t.KENNUNG = 'Fl(3) W 12s', t.OPTIK = 'ROTATING', t.BAUJAHR = 1879, t.AUSSER_BETRIEB = NULL, t.STAND_JAHR = 2026,
    t.FARBEN = 'rotbraunes Ziegelmauerwerk (seit 1982 ohne Anstrich; vorher rot mit weißem Band), rote Laterne und Wachraum',
    t.BESCHREIBUNG = '1878/79 nach Plänen von Ludwig Alexander Veitmeyer erbauter achteckiger Ziegelturm südlich von Dahme, Feuer seit 1. Februar 1880; Seefeuer für Lübecker und Mecklenburger Bucht. Sektoren teils geschätzt [G]. Sichtbereich nicht belegt, als Rundumfeuer eingetragen [G]. Feuerhöhe über Mittelwasser angenommen.',
    t.QUELLE = 'https://www.deutsche-leuchtfeuer.de/ostsee/dahmeshoeved.html · https://www.leuchtturm-atlas.de/HTML/DahmesT.html · https://de.wikipedia.org/wiki/Leuchtturm_Dahmesh%C3%B6ved · https://www.ibiblio.org/lighthouse/deesh.htm'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, REVIER, LAGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT)
  VALUES ('Dahmeshöved', 'OSTSEE', MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(11.09025, 54.20178, NULL), NULL, NULL), 33.7, 28.8, 23, 'Fl(3) W 12s', 'ROTATING', 1879, NULL, 2026, 'rotbraunes Ziegelmauerwerk (seit 1982 ohne Anstrich; vorher rot mit weißem Band), rote Laterne und Wachraum', '1878/79 nach Plänen von Ludwig Alexander Veitmeyer erbauter achteckiger Ziegelturm südlich von Dahme, Feuer seit 1. Februar 1880; Seefeuer für Lübecker und Mecklenburger Bucht. Sektoren teils geschätzt [G]. Sichtbereich nicht belegt, als Rundumfeuer eingetragen [G]. Feuerhöhe über Mittelwasser angenommen.', 'https://www.deutsche-leuchtfeuer.de/ostsee/dahmeshoeved.html · https://www.leuchtturm-atlas.de/HTML/DahmesT.html · https://de.wikipedia.org/wiki/Leuchtturm_Dahmesh%C3%B6ved · https://www.ibiblio.org/lighthouse/deesh.htm', 'MITGELIEFERT');
DELETE FROM LH_SECTOR WHERE LIGHTHOUSE_ID IN (SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = 'Dahmeshöved' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 1, 0, 360, 'W', 'SEKTOR', 23, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Dahmeshöved' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_LIGHTHOUSE t
USING (SELECT 'Tegeler Plate' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.REVIER = 'NORDSEE', t.LAGE = MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.19111, 53.79722, NULL), NULL, NULL),
    t.FEUERHOEHE_M = 20.9, t.TURMHOEHE_M = 24.5, t.TRAGWEITE_SM = 21, t.KENNUNG = 'Oc(3) WRG 12s', t.OPTIK = 'DRUM', t.BAUJAHR = 1965, t.AUSSER_BETRIEB = NULL, t.STAND_JAHR = 2026,
    t.FARBEN = 'roter runder Stahlturm mit auskragendem Geschoss, weiße Laterne, rotes Dach',
    t.BESCHREIBUNG = 'In einer Wilhelmshavener Werft komplett gefertigter Stahlturm, im Juli 1965 an das Nordwestende der Tegeler Plate gebracht; ersetzte das Feuerschiff Weser auf Station Bremen; Leit- und Quermarkenfeuer. Lage nach deutsche-leuchtfeuer.de wie seit Phase 9 in der Szene; baken-net.de nennt eine rund 230 m entfernte Lage.',
    t.QUELLE = 'https://www.deutsche-leuchtfeuer.de/binnen/weser/tegeler-plate.html · https://www.baken-net.de/tegeler-plate.htm · https://www.leuchtturm-atlas.de/HTML/TegeleT.html · https://www.leuchttuerme.net/index.php?nav=1000001&id=32&lang=1&action=portrait · https://www.ibiblio.org/lighthouse/debrh.htm'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, REVIER, LAGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT)
  VALUES ('Tegeler Plate', 'NORDSEE', MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.19111, 53.79722, NULL), NULL, NULL), 20.9, 24.5, 21, 'Oc(3) WRG 12s', 'DRUM', 1965, NULL, 2026, 'roter runder Stahlturm mit auskragendem Geschoss, weiße Laterne, rotes Dach', 'In einer Wilhelmshavener Werft komplett gefertigter Stahlturm, im Juli 1965 an das Nordwestende der Tegeler Plate gebracht; ersetzte das Feuerschiff Weser auf Station Bremen; Leit- und Quermarkenfeuer. Lage nach deutsche-leuchtfeuer.de wie seit Phase 9 in der Szene; baken-net.de nennt eine rund 230 m entfernte Lage.', 'https://www.deutsche-leuchtfeuer.de/binnen/weser/tegeler-plate.html · https://www.baken-net.de/tegeler-plate.htm · https://www.leuchtturm-atlas.de/HTML/TegeleT.html · https://www.leuchttuerme.net/index.php?nav=1000001&id=32&lang=1&action=portrait · https://www.ibiblio.org/lighthouse/debrh.htm', 'MITGELIEFERT');
DELETE FROM LH_SECTOR WHERE LIGHTHOUSE_ID IN (SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = 'Tegeler Plate' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 1, 14, 100, 'W', 'SEKTOR', 21, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Tegeler Plate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 2, 100, 116, 'G', 'SEKTOR', 17, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Tegeler Plate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 3, 116, 119, 'W', 'LEITSEKTOR', 21, 'Leitsektor Neue Weser' FROM LH_LIGHTHOUSE WHERE NAME = 'Tegeler Plate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 4, 119, 123, 'R', 'SEKTOR', 18, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Tegeler Plate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 5, 123, 148.3, 'G', 'SEKTOR', 17, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Tegeler Plate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 6, 148.3, 150.3, 'W', 'LEITSEKTOR', 21, 'Leitsektor Alte Weser' FROM LH_LIGHTHOUSE WHERE NAME = 'Tegeler Plate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 7, 150.3, 264, 'R', 'SEKTOR', 18, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Tegeler Plate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 8, 329, 340, 'W', 'SEKTOR', 21, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Tegeler Plate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, 9, 340, 14, 'R', 'SEKTOR', 18, NULL FROM LH_LIGHTHOUSE WHERE NAME = 'Tegeler Plate' AND HERKUNFT = 'MITGELIEFERT';

-- ----------------------------------------------------------------------------------------- Wetterlagen
MERGE INTO LH_WEATHER_PRESET t
USING (SELECT 'Klar' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEAUFORT = 3, t.PHASE = 'KLAR', t.WIND_MS = 5, t.RICHTUNG_GRAD = 260, t.BOEIGKEIT = 0.14, t.BEDECKUNG_ACHTEL = 2.5, t.HOHE_SCHICHT = 0.05, t.WOLKENART = 'CUMULUS', t.REGEN_MMH = 0, t.SICHT_KM = 30, t.NEBEL = 0, t.LUFTDRUCK_HPA = 1024, t.TEMPERATUR_C = 13, t.FEUCHTE_PROZ = 72, t.DUENUNG_M = 0.5, t.GEWITTER = 0, t.BESCHREIBUNG = 'Hoch über der Deutschen Bucht, leichte Brise, Schönwetterwolken'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEAUFORT, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, LUFTDRUCK_HPA, TEMPERATUR_C, FEUCHTE_PROZ, DUENUNG_M, GEWITTER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Klar', 3, 'KLAR', 5, 260, 0.14, 2.5, 0.05, 'CUMULUS', 0, 30, 0, 1024, 13, 72, 0.5, 0, 'Hoch über der Deutschen Bucht, leichte Brise, Schönwetterwolken', 'MITGELIEFERT');
MERGE INTO LH_WEATHER_PRESET t
USING (SELECT 'Aufziehend' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEAUFORT = 4, t.PHASE = 'AUFZIEHEND', t.WIND_MS = 7.5, t.RICHTUNG_GRAD = 205, t.BOEIGKEIT = 0.16, t.BEDECKUNG_ACHTEL = 3.5, t.HOHE_SCHICHT = 0.75, t.WOLKENART = 'CUMULUS', t.REGEN_MMH = 0, t.SICHT_KM = 22, t.NEBEL = 0, t.LUFTDRUCK_HPA = 1013, t.TEMPERATUR_C = 13, t.FEUCHTE_PROZ = 78, t.DUENUNG_M = 1.4, t.GEWITTER = 0, t.BESCHREIBUNG = 'Ein Tief nähert sich: Cirren, rückdrehender Wind, Dünung kommt vor dem Wind'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEAUFORT, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, LUFTDRUCK_HPA, TEMPERATUR_C, FEUCHTE_PROZ, DUENUNG_M, GEWITTER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Aufziehend', 4, 'AUFZIEHEND', 7.5, 205, 0.16, 3.5, 0.75, 'CUMULUS', 0, 22, 0, 1013, 13, 78, 1.4, 0, 'Ein Tief nähert sich: Cirren, rückdrehender Wind, Dünung kommt vor dem Wind', 'MITGELIEFERT');
MERGE INTO LH_WEATHER_PRESET t
USING (SELECT 'Front' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEAUFORT = 6, t.PHASE = 'FRONT', t.WIND_MS = 13, t.RICHTUNG_GRAD = 225, t.BOEIGKEIT = 0.2, t.BEDECKUNG_ACHTEL = 8, t.HOHE_SCHICHT = 0.2, t.WOLKENART = 'NIMBOSTRATUS', t.REGEN_MMH = 4, t.SICHT_KM = 8, t.NEBEL = 0, t.LUFTDRUCK_HPA = 1000, t.TEMPERATUR_C = 12, t.FEUCHTE_PROZ = 95, t.DUENUNG_M = 2.4, t.GEWITTER = 0, t.BESCHREIBUNG = 'Die Front: geschlossene Regenschicht, Wind frischt auf'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEAUFORT, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, LUFTDRUCK_HPA, TEMPERATUR_C, FEUCHTE_PROZ, DUENUNG_M, GEWITTER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Front', 6, 'FRONT', 13, 225, 0.2, 8, 0.2, 'NIMBOSTRATUS', 4, 8, 0, 1000, 12, 95, 2.4, 0, 'Die Front: geschlossene Regenschicht, Wind frischt auf', 'MITGELIEFERT');
MERGE INTO LH_WEATHER_PRESET t
USING (SELECT 'Sturm' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEAUFORT = 9, t.PHASE = 'STURM', t.WIND_MS = 22, t.RICHTUNG_GRAD = 250, t.BOEIGKEIT = 0.22, t.BEDECKUNG_ACHTEL = 7.5, t.HOHE_SCHICHT = 0, t.WOLKENART = 'NIMBOSTRATUS', t.REGEN_MMH = 6, t.SICHT_KM = 6, t.NEBEL = 0, t.LUFTDRUCK_HPA = 987, t.TEMPERATUR_C = 11, t.FEUCHTE_PROZ = 92, t.DUENUNG_M = 3.4, t.GEWITTER = 1, t.BESCHREIBUNG = 'Sturm aus Südwest bis West, Bft 9, Schauer'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEAUFORT, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, LUFTDRUCK_HPA, TEMPERATUR_C, FEUCHTE_PROZ, DUENUNG_M, GEWITTER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Sturm', 9, 'STURM', 22, 250, 0.22, 7.5, 0, 'NIMBOSTRATUS', 6, 6, 0, 987, 11, 92, 3.4, 1, 'Sturm aus Südwest bis West, Bft 9, Schauer', 'MITGELIEFERT');
MERGE INTO LH_WEATHER_PRESET t
USING (SELECT 'Gewitter' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEAUFORT = 8, t.PHASE = 'GEWITTER', t.WIND_MS = 18, t.RICHTUNG_GRAD = 265, t.BOEIGKEIT = 0.3, t.BEDECKUNG_ACHTEL = 7, t.HOHE_SCHICHT = 0, t.WOLKENART = 'CUMULONIMBUS', t.REGEN_MMH = 14, t.SICHT_KM = 4, t.NEBEL = 0, t.LUFTDRUCK_HPA = 992, t.TEMPERATUR_C = 10, t.FEUCHTE_PROZ = 94, t.DUENUNG_M = 3, t.GEWITTER = 3, t.BESCHREIBUNG = 'Gewitter in der Kaltluft, Böen und Starkregen'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEAUFORT, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, LUFTDRUCK_HPA, TEMPERATUR_C, FEUCHTE_PROZ, DUENUNG_M, GEWITTER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Gewitter', 8, 'GEWITTER', 18, 265, 0.3, 7, 0, 'CUMULONIMBUS', 14, 4, 0, 992, 10, 94, 3, 3, 'Gewitter in der Kaltluft, Böen und Starkregen', 'MITGELIEFERT');
MERGE INTO LH_WEATHER_PRESET t
USING (SELECT 'Abklingend' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEAUFORT = 6, t.PHASE = 'ABKLINGEND', t.WIND_MS = 11, t.RICHTUNG_GRAD = 295, t.BOEIGKEIT = 0.18, t.BEDECKUNG_ACHTEL = 5, t.HOHE_SCHICHT = 0.1, t.WOLKENART = 'STRATOCUMULUS', t.REGEN_MMH = 0.8, t.SICHT_KM = 16, t.NEBEL = 0, t.LUFTDRUCK_HPA = 1004, t.TEMPERATUR_C = 9, t.FEUCHTE_PROZ = 85, t.DUENUNG_M = 2.4, t.GEWITTER = 0, t.BESCHREIBUNG = 'Der Sturm flaut ab, Schauer, aufklarender Himmel'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEAUFORT, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, LUFTDRUCK_HPA, TEMPERATUR_C, FEUCHTE_PROZ, DUENUNG_M, GEWITTER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Abklingend', 6, 'ABKLINGEND', 11, 295, 0.18, 5, 0.1, 'STRATOCUMULUS', 0.8, 16, 0, 1004, 9, 85, 2.4, 0, 'Der Sturm flaut ab, Schauer, aufklarender Himmel', 'MITGELIEFERT');
MERGE INTO LH_WEATHER_PRESET t
USING (SELECT 'Aufklarend' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEAUFORT = 4, t.PHASE = 'AUFKLAREND', t.WIND_MS = 6.5, t.RICHTUNG_GRAD = 310, t.BOEIGKEIT = 0.15, t.BEDECKUNG_ACHTEL = 3, t.HOHE_SCHICHT = 0.1, t.WOLKENART = 'CUMULUS', t.REGEN_MMH = 0, t.SICHT_KM = 40, t.NEBEL = 0, t.LUFTDRUCK_HPA = 1015, t.TEMPERATUR_C = 9, t.FEUCHTE_PROZ = 75, t.DUENUNG_M = 1.4, t.GEWITTER = 0, t.BESCHREIBUNG = 'Rückseite: klare Luft, weite Sicht, Wind auf Nordwest'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEAUFORT, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, LUFTDRUCK_HPA, TEMPERATUR_C, FEUCHTE_PROZ, DUENUNG_M, GEWITTER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Aufklarend', 4, 'AUFKLAREND', 6.5, 310, 0.15, 3, 0.1, 'CUMULUS', 0, 40, 0, 1015, 9, 75, 1.4, 0, 'Rückseite: klare Luft, weite Sicht, Wind auf Nordwest', 'MITGELIEFERT');
MERGE INTO LH_WEATHER_PRESET t
USING (SELECT 'Seenebel' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEAUFORT = 2, t.PHASE = 'NEBEL', t.WIND_MS = 2.5, t.RICHTUNG_GRAD = 200, t.BOEIGKEIT = 0.08, t.BEDECKUNG_ACHTEL = 1, t.HOHE_SCHICHT = 0.25, t.WOLKENART = 'CUMULUS', t.REGEN_MMH = 0, t.SICHT_KM = 14, t.NEBEL = 0.9, t.LUFTDRUCK_HPA = 1022, t.TEMPERATUR_C = 12, t.FEUCHTE_PROZ = 99, t.DUENUNG_M = 0.5, t.GEWITTER = 0, t.BESCHREIBUNG = 'Schwacher Wind, feuchte Luft über kühlerem Wasser, dichter Nebel'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEAUFORT, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, LUFTDRUCK_HPA, TEMPERATUR_C, FEUCHTE_PROZ, DUENUNG_M, GEWITTER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Seenebel', 2, 'NEBEL', 2.5, 200, 0.08, 1, 0.25, 'CUMULUS', 0, 14, 0.9, 1022, 12, 99, 0.5, 0, 'Schwacher Wind, feuchte Luft über kühlerem Wasser, dichter Nebel', 'MITGELIEFERT');
MERGE INTO LH_WEATHER_PRESET t
USING (SELECT 'Windstille' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEAUFORT = 0, t.PHASE = 'KLAR', t.WIND_MS = 0.2, t.RICHTUNG_GRAD = 180, t.BOEIGKEIT = 0.05, t.BEDECKUNG_ACHTEL = 0, t.HOHE_SCHICHT = 0.1, t.WOLKENART = 'CUMULUS', t.REGEN_MMH = 0, t.SICHT_KM = 45, t.NEBEL = 0, t.LUFTDRUCK_HPA = 1032, t.TEMPERATUR_C = 16, t.FEUCHTE_PROZ = 70, t.DUENUNG_M = 0.15, t.GEWITTER = 0, t.BESCHREIBUNG = 'Spiegelglatte See, wolkenlos, Hochdruck [G]'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEAUFORT, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, LUFTDRUCK_HPA, TEMPERATUR_C, FEUCHTE_PROZ, DUENUNG_M, GEWITTER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Windstille', 0, 'KLAR', 0.2, 180, 0.05, 0, 0.1, 'CUMULUS', 0, 45, 0, 1032, 16, 70, 0.15, 0, 'Spiegelglatte See, wolkenlos, Hochdruck [G]', 'MITGELIEFERT');
MERGE INTO LH_WEATHER_PRESET t
USING (SELECT 'Orkan' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEAUFORT = 12, t.PHASE = 'STURM', t.WIND_MS = 34, t.RICHTUNG_GRAD = 280, t.BOEIGKEIT = 0.28, t.BEDECKUNG_ACHTEL = 8, t.HOHE_SCHICHT = 0, t.WOLKENART = 'NIMBOSTRATUS', t.REGEN_MMH = 8, t.SICHT_KM = 3, t.NEBEL = 0, t.LUFTDRUCK_HPA = 962, t.TEMPERATUR_C = 9, t.FEUCHTE_PROZ = 95, t.DUENUNG_M = 7.5, t.GEWITTER = 0, t.BESCHREIBUNG = 'Orkan aus West, Bft 12, Gischt bis über die Galerie [G]'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEAUFORT, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, LUFTDRUCK_HPA, TEMPERATUR_C, FEUCHTE_PROZ, DUENUNG_M, GEWITTER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Orkan', 12, 'STURM', 34, 280, 0.28, 8, 0, 'NIMBOSTRATUS', 8, 3, 0, 962, 9, 95, 7.5, 0, 'Orkan aus West, Bft 12, Gischt bis über die Galerie [G]', 'MITGELIEFERT');

-- ----------------------------------------------------------------------------------------- Regiebücher
MERGE INTO LH_SCENARIO t
USING (SELECT 'Ein Sturmtag' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEGINN_STUNDE = 6, t.ZEITRAFFER = 420, t.BESCHREIBUNG = 'Morgenstille, Front, Sturm Bft 9, Gewitter in der Dämmerung, Sturmnacht, Aufklaren mit Regenbogen; 28 Stunden in gut 4 Minuten'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEGINN_STUNDE, ZEITRAFFER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Ein Sturmtag', 6, 420, 'Morgenstille, Front, Sturm Bft 9, Gewitter in der Dämmerung, Sturmnacht, Aufklaren mit Regenbogen; 28 Stunden in gut 4 Minuten', 'MITGELIEFERT');
DELETE FROM LH_SCENARIO_STEP WHERE SCENARIO_ID IN (SELECT ID FROM LH_SCENARIO WHERE NAME = 'Ein Sturmtag' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, RICHTUNG_GRAD, BEDECKUNG_ACHTEL, HOHE_SCHICHT, SICHT_KM, NEBEL, DUENUNG_M) SELECT ID, 1, 3, 'KLAR', 'Morgenstille mit Dunst', 'Aussen1', 1.2, 140, 1, 0, 7, 0.25, 0.4 FROM LH_SCENARIO WHERE NAME = 'Ein Sturmtag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, BEDECKUNG_ACHTEL, HOHE_SCHICHT) SELECT ID, 2, 3, 'AUFZIEHEND', 'Dünung und Cirren ziehen auf', 'Drohne', 5.5, 2.5, 0.85 FROM LH_SCENARIO WHERE NAME = 'Ein Sturmtag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA) SELECT ID, 3, 3, 'FRONT', 'Front mit Regenvorhang', 'Aussen1' FROM LH_SCENARIO WHERE NAME = 'Ein Sturmtag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS) SELECT ID, 4, 3.5, 'STURM', 'Sturm, Bft 9', 'Brecher', 23.5 FROM LH_SCENARIO WHERE NAME = 'Ein Sturmtag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WOLKENART, GEWITTER) SELECT ID, 5, 2, 'GEWITTER', 'Gewitter in der Dämmerung', 'Aussen3', 'CUMULONIMBUS', 3 FROM LH_SCENARIO WHERE NAME = 'Ein Sturmtag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, REGEN_MMH, GEWITTER) SELECT ID, 6, 6.5, 'STURM', 'Sturmnacht mit Leuchtfeuer', 'Laternenraum', 20, 4, 1 FROM LH_SCENARIO WHERE NAME = 'Ein Sturmtag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA) SELECT ID, 7, 5, 'ABKLINGEND', 'Der Sturm flaut ab', 'Aussen1' FROM LH_SCENARIO WHERE NAME = 'Ein Sturmtag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, BEDECKUNG_ACHTEL, WOLKENART, REGEN_MMH) SELECT ID, 8, 0.8, 'ABKLINGEND', 'Ein letzter Schauer', 'Aussen1', 9, 5.5, 'CUMULUS', 3 FROM LH_SCENARIO WHERE NAME = 'Ein Sturmtag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, BEDECKUNG_ACHTEL) SELECT ID, 9, 1.5, 'AUFKLAREND', 'Aufklaren mit Regenbogen', 'Regenbogen', 3.5 FROM LH_SCENARIO WHERE NAME = 'Ein Sturmtag' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_SCENARIO t
USING (SELECT 'Ein Nebeltag' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEGINN_STUNDE = 5.5, t.ZEITRAFFER = 200, t.BESCHREIBUNG = 'Klare Dämmerung, Nebelbank, dichter Seenebel mit Nebelhorn und Typhon, flacher Nebel, Aufklaren; 14 Stunden in gut 4 Minuten'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEGINN_STUNDE, ZEITRAFFER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Ein Nebeltag', 5.5, 200, 'Klare Dämmerung, Nebelbank, dichter Seenebel mit Nebelhorn und Typhon, flacher Nebel, Aufklaren; 14 Stunden in gut 4 Minuten', 'MITGELIEFERT');
DELETE FROM LH_SCENARIO_STEP WHERE SCENARIO_ID IN (SELECT ID FROM LH_SCENARIO WHERE NAME = 'Ein Nebeltag' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, RICHTUNG_GRAD, BEDECKUNG_ACHTEL, SICHT_KM, NEBEL) SELECT ID, 1, 2, 'KLAR', 'Klare Morgendämmerung', 'Aussen1', 3, 200, 1, 25, 0 FROM LH_SCENARIO WHERE NAME = 'Ein Nebeltag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, NEBEL) SELECT ID, 2, 1.5, 'NEBEL', 'Eine Nebelbank zieht auf', 'Drohne', 0.55 FROM LH_SCENARIO WHERE NAME = 'Ein Nebeltag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, NEBEL) SELECT ID, 3, 4, 'NEBEL', 'Dichter Seenebel, das Nebelhorn ruft', 'Schiffsbrücke', 2, 1 FROM LH_SCENARIO WHERE NAME = 'Ein Nebeltag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, NEBEL) SELECT ID, 4, 2.5, 'NEBEL', 'Flacher Nebel, der Turm ragt heraus', 'Drohne', 3, 0.42 FROM LH_SCENARIO WHERE NAME = 'Ein Nebeltag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, NEBEL, FEUCHTE_PROZ) SELECT ID, 5, 2, 'KLAR', 'Der Nebel lichtet sich', 'Möwenflug', 4, 0.12, 85 FROM LH_SCENARIO WHERE NAME = 'Ein Nebeltag' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, NEBEL) SELECT ID, 6, 2, 'KLAR', 'Klarer Nachmittag', 'Aussen1', 5, 0 FROM LH_SCENARIO WHERE NAME = 'Ein Nebeltag' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_SCENARIO t
USING (SELECT 'Ruhige Tage' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BEGINN_STUNDE = 5, t.ZEITRAFFER = 600, t.BESCHREIBUNG = 'Zwei Tage Hochdruck mit Seenebel am Morgen'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, BEGINN_STUNDE, ZEITRAFFER, BESCHREIBUNG, HERKUNFT)
  VALUES ('Ruhige Tage', 5, 600, 'Zwei Tage Hochdruck mit Seenebel am Morgen', 'MITGELIEFERT');
DELETE FROM LH_SCENARIO_STEP WHERE SCENARIO_ID IN (SELECT ID FROM LH_SCENARIO WHERE NAME = 'Ruhige Tage' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, NEBEL, FEUCHTE_PROZ) SELECT ID, 1, 4, 'KLAR', 'Seenebel am Morgen', 'Aussen1', 2, 0.8, 98 FROM LH_SCENARIO WHERE NAME = 'Ruhige Tage' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA) SELECT ID, 2, 20, 'KLAR', 'Hochdruck', 'Aussen1' FROM LH_SCENARIO WHERE NAME = 'Ruhige Tage' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, WIND_MS, NEBEL, FEUCHTE_PROZ) SELECT ID, 3, 4, 'KLAR', 'Seenebel am Morgen', 'Aussen1', 2.5, 0.7, 97 FROM LH_SCENARIO WHERE NAME = 'Ruhige Tage' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA) SELECT ID, 4, 20, 'KLAR', 'Hochdruck', 'Aussen1' FROM LH_SCENARIO WHERE NAME = 'Ruhige Tage' AND HERKUNFT = 'MITGELIEFERT';

-- ----------------------------------------------------------------------------------------- Kamerafahrten
MERGE INTO LH_CAMERA_PATH t
USING (SELECT 'Rundflug' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'RUNDFLUG', t.SCHLEIFE = 1, t.BESCHREIBUNG = 'Einmal um den Turm in zwei Minuten, Höhe und Abstand atmen sacht'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, SCHLEIFE, BESCHREIBUNG, HERKUNFT)
  VALUES ('Rundflug', 'RUNDFLUG', 1, 'Einmal um den Turm in zwei Minuten, Höhe und Abstand atmen sacht', 'MITGELIEFERT');
DELETE FROM LH_CAMERA_POINT WHERE CAMERA_PATH_ID IN (SELECT ID FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 1, 0, 0, 41.15, -140, 180, 10.19, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 2, 3.333, 25.31, 41.84, -143.53, 190, 10.05, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 3, 6.667, 51.58, 42.29, -141.7, 200, 9.89, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 4, 10, 77.27, 42.49, -133.84, 210, 9.73, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 5, 13.333, 100.63, 42.43, -119.92, 220, 9.58, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 6, 16.667, 119.92, 42.12, -100.63, 230, 9.47, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 7, 20, 133.84, 41.55, -77.27, 240, 9.39, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 8, 23.333, 141.7, 40.76, -51.58, 250, 9.33, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 9, 26.667, 143.53, 39.76, -25.31, 260, 9.26, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 10, 30, 140, 38.59, 0, 270, 9.17, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 11, 33.333, 132.21, 37.28, 23.31, 280, 9.01, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 12, 36.667, 121.41, 35.87, 44.19, 290, 8.74, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 13, 40, 108.64, 34.4, 62.73, 300, 8.34, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 14, 43.333, 94.57, 32.92, 79.36, 310, 7.8, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 15, 46.667, 79.36, 31.47, 94.57, 320, 7.14, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 16, 50, 62.73, 30.1, 108.64, 330, 6.41, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 17, 53.333, 44.19, 28.85, 121.41, 340, 5.68, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 18, 56.667, 23.31, 27.75, 132.21, 350, 5, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 19, 60, 0, 26.85, 140, 360, 4.43, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 20, 63.333, -25.31, 26.16, 143.53, 370, 3.99, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 21, 66.667, -51.58, 25.71, 141.7, 380, 3.68, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 22, 70, -77.27, 25.51, 133.84, 390, 3.52, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 23, 73.333, -100.63, 25.57, 119.92, 400, 3.5, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 24, 76.667, -119.92, 25.88, 100.63, 410, 3.61, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 25, 80, -133.84, 26.45, 77.27, 420, 3.87, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 26, 83.333, -141.7, 27.24, 51.58, 430, 4.26, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 27, 86.667, -143.53, 28.24, 25.31, 440, 4.8, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 28, 90, -140, 29.41, 0, 450, 5.47, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 29, 93.333, -132.21, 30.72, -23.31, 460, 6.26, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 30, 96.667, -121.41, 32.13, -44.19, 470, 7.12, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 31, 100, -108.64, 33.6, -62.73, 480, 7.99, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 32, 103.333, -94.57, 35.08, -79.36, 490, 8.79, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 33, 106.667, -79.36, 36.53, -94.57, 500, 9.44, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 34, 110, -62.73, 37.9, -108.64, 510, 9.9, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 35, 113.333, -44.19, 39.15, -121.41, 520, 10.16, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 36, 116.667, -23.31, 40.25, -132.21, 530, 10.24, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 37, 120, 0, 41.15, -140, 540, 10.19, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Rundflug' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_CAMERA_PATH t
USING (SELECT 'Aufstieg' AS NAME FROM DUAL) q
ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'AUFSTIEG', t.SCHLEIFE = 0, t.BESCHREIBUNG = 'Von der Tür die 59 Stufen hinauf, durch die Luke in den Laternenraum'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, SCHLEIFE, BESCHREIBUNG, HERKUNFT)
  VALUES ('Aufstieg', 'AUFSTIEG', 0, 'Von der Tür die 59 Stufen hinauf, durch die Luke in den Laternenraum', 'MITGELIEFERT');
DELETE FROM LH_CAMERA_POINT WHERE CAMERA_PATH_ID IN (SELECT ID FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 1, 0, -4.45, 10.85, 12.22, 380, -8, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 2, 2, -2.91, 10.05, 7.99, 380, -4, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 3, 4, -1.61, 9.65, 4.42, 380, 0, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 4, 4.001, -0.74, 9.7, 2.02, 380, -5, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 5, 6, -0.56, 9.7, 1.39, 376, -50, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 6, 8, -0.44, 9.7, 0.9, 372, -86, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 7, 10, -0.88, 9.75, 1.21, 335, -25, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 8, 12, -1.68, 9.9, 0.07, 377.5, -2, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 9, 12.5, -1.64, 10.04, -0.35, 403.25, 11.19, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 10, 13, -1.5, 10.19, -0.76, 420.87, 11.55, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 11, 13.5, -1.26, 10.33, -1.11, 438.26, 11.81, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 12, 14, -0.94, 10.48, -1.39, 455.31, 11.96, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 13, 14.5, -0.56, 10.62, -1.58, 471.93, 12, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 14, 15, -0.14, 10.77, -1.67, 488.03, 11.93, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 15, 15.5, 0.29, 10.91, -1.66, 503.57, 11.74, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 16, 16, 0.7, 11.06, -1.53, 518.5, 11.45, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 17, 16.5, 1.06, 11.2, -1.3, 532.82, 11.06, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 18, 17, 1.35, 11.35, -0.99, 546.53, 10.57, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 19, 17.5, 1.56, 11.49, -0.62, 559.68, 9.99, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 20, 18, 1.67, 11.64, -0.2, 572.31, 9.32, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 21, 18.5, 1.67, 11.78, 0.22, 584.51, 8.58, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 22, 19, 1.55, 11.93, 0.64, 596.37, 7.78, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 23, 19.5, 1.34, 12.07, 1.01, 608, 6.93, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 24, 20, 1.05, 12.22, 1.31, 619.5, 6.03, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 25, 20.5, 0.68, 12.36, 1.54, 631, 5.11, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 26, 21, 0.27, 12.51, 1.66, 642.63, 4.17, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 27, 21.5, -0.16, 12.65, 1.67, 654.49, 3.23, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 28, 22, -0.57, 12.8, 1.58, 666.69, 2.3, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 29, 22.5, -0.95, 12.94, 1.38, 679.32, 1.39, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 30, 23, -1.27, 13.09, 1.1, 692.47, 0.52, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 31, 23.5, -1.51, 13.23, 0.74, 706.18, -0.3, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 32, 24, -1.65, 13.38, 0.33, 720.5, -1.07, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 33, 24.5, -1.68, 13.52, -0.09, 735.43, -1.76, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 34, 25, -1.6, 13.67, -0.51, 750.97, -2.37, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 35, 25.5, -1.42, 13.81, -0.9, 767.07, -2.9, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 36, 26, -1.15, 13.96, -1.23, 783.69, -3.33, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 37, 26.5, -0.8, 14.1, -1.48, 800.74, -3.65, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 38, 27, -0.4, 14.25, -1.63, 818.13, -3.87, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 39, 27.5, 0.03, 14.39, -1.68, 835.75, -3.99, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 40, 28, 0.45, 14.54, -1.62, 853.5, -3.99, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 41, 28.5, 0.84, 14.68, -1.45, 871.25, -3.88, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 42, 29, 1.18, 14.83, -1.19, 888.87, -3.66, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 43, 29.5, 1.45, 14.97, -0.86, 906.26, -3.33, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 44, 30, 1.61, 15.12, -0.46, 923.31, -2.91, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 45, 30.5, 1.68, 15.26, -0.04, 939.93, -2.39, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 46, 31, 1.64, 15.41, 0.39, 956.03, -1.77, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 47, 31.5, 1.49, 15.55, 0.79, 971.57, -1.08, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 48, 32, 1.24, 15.7, 1.13, 986.5, -0.32, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 49, 32.5, 0.91, 15.84, 1.41, 1000.82, 0.5, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 50, 33, 0.53, 15.99, 1.6, 1014.53, 1.37, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 51, 33.5, 0.11, 16.13, 1.68, 1027.68, 2.27, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 52, 34, -0.32, 16.28, 1.65, 1040.31, 3.2, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 53, 34.5, -0.73, 16.42, 1.51, 1052.51, 4.15, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 54, 35, -1.09, 16.57, 1.28, 1064.37, 5.09, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 55, 35.5, -1.37, 16.71, 0.97, 1076, 6.01, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 56, 36, -1.57, 16.86, 0.59, 1087.5, 6.91, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 57, 36.5, -1.67, 17, 0.17, 1099, 7.76, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 58, 37, -1.66, 17.15, -0.26, 1110.63, 8.57, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 59, 37.5, -1.54, 17.29, -0.67, 1122.49, 9.31, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 60, 38, -1.32, 17.44, -1.03, 1134.69, 9.97, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 61, 38.5, -1.02, 17.58, -1.34, 1147.32, 10.56, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 62, 39, -0.65, 17.73, -1.55, 1160.47, 11.05, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 63, 39.5, -0.24, 17.87, -1.66, 1174.18, 11.45, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 64, 40, 0.19, 18.02, -1.67, 1188.5, 11.74, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 65, 40.5, 0.61, 18.16, -1.57, 1203.43, 11.92, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 66, 41, 0.98, 18.31, -1.36, 1218.97, 12, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 67, 41.5, 1.29, 18.45, -1.07, 1235.07, 11.96, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 68, 42, 1.52, 18.6, -0.71, 1251.69, 11.82, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 69, 42.5, 1.65, 18.74, -0.3, 1268.74, 11.56, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 70, 43, 1.68, 18.89, 0.12, 1286.13, 11.2, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 71, 43.5, 1.59, 19.03, 0.54, 1303.75, 10.74, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 72, 44, 1.4, 19.18, 0.93, 1321.5, 10.19, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 73, 44.5, 1.12, 19.32, 1.25, 1339.25, 9.55, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 74, 45, 0.77, 19.47, 1.49, 1356.87, 8.84, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 75, 45.5, 0.37, 19.61, 1.64, 1374.26, 8.05, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 76, 46, -0.06, 19.76, 1.68, 1391.31, 7.22, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 77, 46.5, -0.48, 19.9, 1.61, 1407.93, 6.33, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 78, 47, -0.87, 20.05, 1.44, 1424.03, 5.42, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 79, 47.5, -1.21, 20.19, 1.17, 1439.57, 4.48, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 80, 48, -1.46, 20.34, 0.83, 1454.5, 3.54, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 81, 48.5, -1.62, 20.48, 0.43, 1468.82, 2.6, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 82, 49, -1.68, 20.63, 0.01, 1482.53, 1.69, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 83, 49.5, -1.63, 20.77, -0.42, 1495.68, 0.8, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 84, 50, -1.47, 20.92, -0.81, 1508.31, -0.04, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 85, 50.5, -1.22, 21.06, -1.16, 1520.51, -0.82, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 86, 51, -0.88, 21.21, -1.43, 1532.37, -1.54, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 87, 51.5, -0.49, 21.35, -1.61, 1544, -2.18, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 88, 52, -0.07, 21.5, -1.68, 1555.5, -2.73, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 89, 53.5, 0.56, 21.4, -1.58, 1565.5, -10, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 90, 55.5, 1.65, 21.4, -1.38, 1617.75, -20, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 91, 57, 1.91, 21.4, -1.1, 1680, -50, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 92, 58, 1.39, 21.35, -0.8, 1680, -55, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 93, 59.2, 1.26, 21.9, -0.73, 1680, -45.5, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 94, 60.4, 1.13, 22.44, -0.65, 1680, -36, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 95, 61.6, 1, 22.98, -0.58, 1680, -26.5, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 96, 62.8, 0.94, 23.53, -0.54, 1680, -17, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 97, 64, 0.94, 24.08, -0.54, 1680, -7.5, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 98, 65.2, 0.94, 24.62, -0.54, 1680, 2, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 99, 66.7, 1.1, 24.67, -0.23, 1720, 4, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 100, 69.7, 1.2, 24.67, 0.21, 1790, 6, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 101, 72.7, 1.14, 24.67, 0.66, 1880, 2, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, 102, 75.7, 1.05, 24.67, 0.88, 1915, 1, 55 FROM LH_CAMERA_PATH WHERE NAME = 'Aufstieg' AND HERKUNFT = 'MITGELIEFERT';

-- ----------------------------------------------------------------------------------------- Presets der Packages
MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.ocean' AS PAKET, 'Ocean' AS KLASSE, 'calm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus dem Package: ruhig, Bft 2'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.ocean', 'Ocean', 'calm', 'Preset calm() aus dem Package: ruhig, Bft 2', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetch', 'DOUBLE', '150000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'depth', 'DOUBLE', '10000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'choppiness', 'DOUBLE', '0.85' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'heightOverride', 'DOUBLE', 'NaN' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetchLimited', 'BOOLEAN', 'false' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellHeight', 'DOUBLE', '0.318' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellPeriod', 'DOUBLE', '8.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellFromDeg', 'DOUBLE', '290' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamDecay', 'DOUBLE', '2.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamAmount', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsLow', 'DOUBLE', '0.15' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsHigh', 'DOUBLE', '1.6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.ocean' AS PAKET, 'Ocean' AS KLASSE, 'breeze' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus dem Package: frische Brise, Bft 5'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.ocean', 'Ocean', 'breeze', 'Preset breeze() aus dem Package: frische Brise, Bft 5', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '6.7' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetch', 'DOUBLE', '150000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'depth', 'DOUBLE', '10000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'choppiness', 'DOUBLE', '0.85' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'heightOverride', 'DOUBLE', 'NaN' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetchLimited', 'BOOLEAN', 'false' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellHeight', 'DOUBLE', '0.48' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellPeriod', 'DOUBLE', '9.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellFromDeg', 'DOUBLE', '290' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamDecay', 'DOUBLE', '2.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamAmount', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsLow', 'DOUBLE', '0.15' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsHigh', 'DOUBLE', '1.6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.ocean' AS PAKET, 'Ocean' AS KLASSE, 'gale' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus dem Package: stürmisch, Bft 8'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.ocean', 'Ocean', 'gale', 'Preset gale() aus dem Package: stürmisch, Bft 8', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '19' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetch', 'DOUBLE', '150000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'depth', 'DOUBLE', '10000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'choppiness', 'DOUBLE', '0.85' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'heightOverride', 'DOUBLE', 'NaN' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetchLimited', 'BOOLEAN', 'false' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellHeight', 'DOUBLE', '1.29' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellPeriod', 'DOUBLE', '10.8' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellFromDeg', 'DOUBLE', '290' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamDecay', 'DOUBLE', '2.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamAmount', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsLow', 'DOUBLE', '0.15' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsHigh', 'DOUBLE', '1.6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.ocean' AS PAKET, 'Ocean' AS KLASSE, 'storm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus dem Package: schwerer Sturm, Bft 10'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.ocean', 'Ocean', 'storm', 'Preset storm() aus dem Package: schwerer Sturm, Bft 10', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetch', 'DOUBLE', '150000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'depth', 'DOUBLE', '10000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'choppiness', 'DOUBLE', '0.85' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'heightOverride', 'DOUBLE', 'NaN' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetchLimited', 'BOOLEAN', 'false' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellHeight', 'DOUBLE', '1.92' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellPeriod', 'DOUBLE', '11.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellFromDeg', 'DOUBLE', '290' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamDecay', 'DOUBLE', '2.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamAmount', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsLow', 'DOUBLE', '0.15' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsHigh', 'DOUBLE', '1.6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.spray' AS PAKET, 'Spray' AS KLASSE, 'calm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus dem Package: ruhig, Bft 2'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.spray', 'Spray', 'calm', 'Preset calm() aus dem Package: ruhig, Bft 2', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '2.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '0.2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'strength', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusRadius', 'DOUBLE', '120' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'samplesPer10m', 'INT', '10' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sampleRate', 'DOUBLE', '20' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.spray' AS PAKET, 'Spray' AS KLASSE, 'breeze' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus dem Package: frische Brise, Bft 5'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.spray', 'Spray', 'breeze', 'Preset breeze() aus dem Package: frische Brise, Bft 5', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '9.3' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'strength', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusRadius', 'DOUBLE', '120' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'samplesPer10m', 'INT', '10' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sampleRate', 'DOUBLE', '20' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.spray' AS PAKET, 'Spray' AS KLASSE, 'gale' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus dem Package: stürmisch, Bft 8'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.spray', 'Spray', 'gale', 'Preset gale() aus dem Package: stürmisch, Bft 8', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '18.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '5.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'strength', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusRadius', 'DOUBLE', '120' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'samplesPer10m', 'INT', '10' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sampleRate', 'DOUBLE', '20' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.spray' AS PAKET, 'Spray' AS KLASSE, 'storm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus dem Package: schwerer Sturm, Bft 10'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.spray', 'Spray', 'storm', 'Preset storm() aus dem Package: schwerer Sturm, Bft 10', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'strength', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'focusRadius', 'DOUBLE', '120' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'samplesPer10m', 'INT', '10' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sampleRate', 'DOUBLE', '20' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.spray' AND KLASSE = 'Spray' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'calm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus dem Package: ruhig, Bft 2'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'calm', 'Preset calm() aus dem Package: ruhig, Bft 2', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '0.3' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.13' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'breeze' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus dem Package: frische Brise, Bft 5'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'breeze', 'Preset breeze() aus dem Package: frische Brise, Bft 5', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '9.3' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.25' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'gale' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus dem Package: stürmisch, Bft 8'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'gale', 'Preset gale() aus dem Package: stürmisch, Bft 8', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '8' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '18.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.34' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'storm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus dem Package: schwerer Sturm, Bft 10'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'storm', 'Preset storm() aus dem Package: schwerer Sturm, Bft 10', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '20' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.wetglass' AS PAKET, 'Wetglass' AS KLASSE, 'calm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus dem Package: ruhig, Bft 2'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.wetglass', 'Wetglass', 'calm', 'Preset calm() aus dem Package: ruhig, Bft 2', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'width', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'height', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'periodic', 'BOOLEAN', 'true' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'drainLength', 'DOUBLE', '1.05' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeY', 'DOUBLE', '-1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windX', 'DOUBLE', '0.315' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windY', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hitRate', 'DOUBLE', '82.074683' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hitLambda', 'DOUBLE', '5.279446' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'evaporation', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'filmLife', 'DOUBLE', '90' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'condensation', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'saltRate', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'stepRate', 'DOUBLE', '60' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.wetglass' AS PAKET, 'Wetglass' AS KLASSE, 'breeze' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus dem Package: frische Brise, Bft 5'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.wetglass', 'Wetglass', 'breeze', 'Preset breeze() aus dem Package: frische Brise, Bft 5', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'width', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'height', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'periodic', 'BOOLEAN', 'true' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'drainLength', 'DOUBLE', '1.05' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeY', 'DOUBLE', '-1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windX', 'DOUBLE', '3.255' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windY', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hitRate', 'DOUBLE', '1464.178639' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hitLambda', 'DOUBLE', '3.544603' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'evaporation', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'filmLife', 'DOUBLE', '90' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'condensation', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'saltRate', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'stepRate', 'DOUBLE', '60' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.wetglass' AS PAKET, 'Wetglass' AS KLASSE, 'gale' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus dem Package: stürmisch, Bft 8'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.wetglass', 'Wetglass', 'gale', 'Preset gale() aus dem Package: stürmisch, Bft 8', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'width', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'height', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'periodic', 'BOOLEAN', 'true' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'drainLength', 'DOUBLE', '1.05' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeY', 'DOUBLE', '-1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windX', 'DOUBLE', '6.615' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windY', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hitRate', 'DOUBLE', '3399.05997' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hitLambda', 'DOUBLE', '2.649323' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'evaporation', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'filmLife', 'DOUBLE', '90' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'condensation', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'saltRate', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'stepRate', 'DOUBLE', '60' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.wetglass' AS PAKET, 'Wetglass' AS KLASSE, 'storm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus dem Package: schwerer Sturm, Bft 10'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.wetglass', 'Wetglass', 'storm', 'Preset storm() aus dem Package: schwerer Sturm, Bft 10', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'width', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'height', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'periodic', 'BOOLEAN', 'true' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'drainLength', 'DOUBLE', '1.05' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeY', 'DOUBLE', '-1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windX', 'DOUBLE', '9.24' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windY', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hitRate', 'DOUBLE', '5350.165682' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hitLambda', 'DOUBLE', '2.185584' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'evaporation', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'filmLife', 'DOUBLE', '90' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'condensation', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'saltRate', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'stepRate', 'DOUBLE', '60' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.wetglass' AND KLASSE = 'Wetglass' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.weathering' AS PAKET, 'Weathering' AS KLASSE, 'calm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus dem Package: ruhig, Bft 2'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.weathering', 'Weathering', 'calm', 'Preset calm() aus dem Package: ruhig, Bft 2', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'meanHighWater', 'DOUBLE', '1.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'meanLowWater', 'DOUBLE', '-1.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '0.1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sun', 'DOUBLE', '0.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rust', 'DOUBLE', '0.6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'algae', 'DOUBLE', '0.8' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'salt', 'DOUBLE', '0.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'moss', 'DOUBLE', '0.1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapWidthM', 'DOUBLE', '30' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapHeightM', 'DOUBLE', '32' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapBottom', 'DOUBLE', '-2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.weathering' AS PAKET, 'Weathering' AS KLASSE, 'breeze' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus dem Package: frische Brise, Bft 5'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.weathering', 'Weathering', 'breeze', 'Preset breeze() aus dem Package: frische Brise, Bft 5', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'meanHighWater', 'DOUBLE', '1.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'meanLowWater', 'DOUBLE', '-1.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '9.3' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sun', 'DOUBLE', '0.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rust', 'DOUBLE', '0.6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'algae', 'DOUBLE', '0.8' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'salt', 'DOUBLE', '0.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'moss', 'DOUBLE', '0.1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapWidthM', 'DOUBLE', '30' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapHeightM', 'DOUBLE', '32' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapBottom', 'DOUBLE', '-2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.weathering' AS PAKET, 'Weathering' AS KLASSE, 'gale' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus dem Package: stürmisch, Bft 8'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.weathering', 'Weathering', 'gale', 'Preset gale() aus dem Package: stürmisch, Bft 8', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'meanHighWater', 'DOUBLE', '1.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'meanLowWater', 'DOUBLE', '-1.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '5.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '18.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sun', 'DOUBLE', '0.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rust', 'DOUBLE', '0.6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'algae', 'DOUBLE', '0.8' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'salt', 'DOUBLE', '0.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'moss', 'DOUBLE', '0.1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapWidthM', 'DOUBLE', '30' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapHeightM', 'DOUBLE', '32' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapBottom', 'DOUBLE', '-2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.weathering' AS PAKET, 'Weathering' AS KLASSE, 'storm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus dem Package: schwerer Sturm, Bft 10'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.weathering', 'Weathering', 'storm', 'Preset storm() aus dem Package: schwerer Sturm, Bft 10', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'meanHighWater', 'DOUBLE', '1.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'meanLowWater', 'DOUBLE', '-1.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '11.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '30.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sun', 'DOUBLE', '0.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rust', 'DOUBLE', '0.6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'algae', 'DOUBLE', '0.8' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'salt', 'DOUBLE', '0.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'moss', 'DOUBLE', '0.1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapWidthM', 'DOUBLE', '30' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapHeightM', 'DOUBLE', '32' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mapBottom', 'DOUBLE', '-2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.weathering' AND KLASSE = 'Weathering' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.lightning' AS PAKET, 'Lightning' AS KLASSE, 'calm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus dem Package: ruhig, Bft 2'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.lightning', 'Lightning', 'calm', 'Preset calm() aus dem Package: ruhig, Bft 2', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'perMinute', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cgFraction', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'spiderFraction', 'DOUBLE', '0.18' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cx', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cz', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'radius', 'DOUBLE', '12000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '1200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'chargeAbove', 'DOUBLE', '2500' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'groundLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerY', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '15' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'leaderSpeed', 'DOUBLE', '200000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sheetDistance', 'DOUBLE', '28000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'candelaPerMetre', 'DOUBLE', '5000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxBolts', 'INT', '6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.lightning' AS PAKET, 'Lightning' AS KLASSE, 'breeze' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus dem Package: frische Brise, Bft 5'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.lightning', 'Lightning', 'breeze', 'Preset breeze() aus dem Package: frische Brise, Bft 5', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'perMinute', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cgFraction', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'spiderFraction', 'DOUBLE', '0.18' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cx', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cz', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'radius', 'DOUBLE', '12000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '1200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'chargeAbove', 'DOUBLE', '2500' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'groundLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerY', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '15' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'leaderSpeed', 'DOUBLE', '200000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sheetDistance', 'DOUBLE', '28000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'candelaPerMetre', 'DOUBLE', '5000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxBolts', 'INT', '6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.lightning' AS PAKET, 'Lightning' AS KLASSE, 'gale' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus dem Package: stürmisch, Bft 8'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.lightning', 'Lightning', 'gale', 'Preset gale() aus dem Package: stürmisch, Bft 8', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'perMinute', 'DOUBLE', '5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cgFraction', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'spiderFraction', 'DOUBLE', '0.18' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cx', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cz', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'radius', 'DOUBLE', '12000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '1200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'chargeAbove', 'DOUBLE', '2500' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'groundLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerY', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '15' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'leaderSpeed', 'DOUBLE', '200000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sheetDistance', 'DOUBLE', '28000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'candelaPerMetre', 'DOUBLE', '5000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxBolts', 'INT', '6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.lightning' AS PAKET, 'Lightning' AS KLASSE, 'storm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus dem Package: schwerer Sturm, Bft 10'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.lightning', 'Lightning', 'storm', 'Preset storm() aus dem Package: schwerer Sturm, Bft 10', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'perMinute', 'DOUBLE', '8' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cgFraction', 'DOUBLE', '0.28' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'spiderFraction', 'DOUBLE', '0.18' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cx', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cz', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'radius', 'DOUBLE', '12000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '1200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'chargeAbove', 'DOUBLE', '2500' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'groundLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerY', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '15' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'leaderSpeed', 'DOUBLE', '200000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sheetDistance', 'DOUBLE', '28000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'candelaPerMetre', 'DOUBLE', '5000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxBolts', 'INT', '6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'calm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus dem Package: ruhig, Bft 2'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'calm', 'Preset calm() aus dem Package: ruhig, Bft 2', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '0.7' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '50' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'breeze' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus dem Package: frische Brise, Bft 5'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'breeze', 'Preset breeze() aus dem Package: frische Brise, Bft 5', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '0.55' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '0.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '6.7' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'gale' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus dem Package: stürmisch, Bft 8'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'gale', 'Preset gale() aus dem Package: stürmisch, Bft 8', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '0.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '19' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'storm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus dem Package: schwerer Sturm, Bft 10'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'storm', 'Preset storm() aus dem Package: schwerer Sturm, Bft 10', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '0.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.buoyancy' AS PAKET, 'Buoyancy' AS KLASSE, 'calm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus dem Package: ruhig, Bft 2'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.buoyancy', 'Buoyancy', 'calm', 'Preset calm() aus dem Package: ruhig, Bft 2', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ambientLux', 'DOUBLE', '10000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'lightsOnLux', 'DOUBLE', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shipEveryMin', 'DOUBLE', '25' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayX', 'DOUBLE', '900' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayDeg', 'DOUBLE', '330' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayHalf', 'DOUBLE', '7000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.buoyancy' AS PAKET, 'Buoyancy' AS KLASSE, 'breeze' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus dem Package: frische Brise, Bft 5'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.buoyancy', 'Buoyancy', 'breeze', 'Preset breeze() aus dem Package: frische Brise, Bft 5', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '6.7' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ambientLux', 'DOUBLE', '10000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'lightsOnLux', 'DOUBLE', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shipEveryMin', 'DOUBLE', '25' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayX', 'DOUBLE', '900' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayDeg', 'DOUBLE', '330' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayHalf', 'DOUBLE', '7000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.buoyancy' AS PAKET, 'Buoyancy' AS KLASSE, 'gale' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus dem Package: stürmisch, Bft 8'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.buoyancy', 'Buoyancy', 'gale', 'Preset gale() aus dem Package: stürmisch, Bft 8', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '19' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ambientLux', 'DOUBLE', '10000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'lightsOnLux', 'DOUBLE', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shipEveryMin', 'DOUBLE', '25' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayX', 'DOUBLE', '900' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayDeg', 'DOUBLE', '330' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayHalf', 'DOUBLE', '7000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.buoyancy' AS PAKET, 'Buoyancy' AS KLASSE, 'storm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus dem Package: schwerer Sturm, Bft 10'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.buoyancy', 'Buoyancy', 'storm', 'Preset storm() aus dem Package: schwerer Sturm, Bft 10', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ambientLux', 'DOUBLE', '10000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'lightsOnLux', 'DOUBLE', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shipEveryMin', 'DOUBLE', '50' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayX', 'DOUBLE', '900' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayDeg', 'DOUBLE', '330' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fairwayHalf', 'DOUBLE', '7000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.buoyancy' AND KLASSE = 'Buoyancy' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.flock' AS PAKET, 'Flock' AS KLASSE, 'calm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus dem Package: ruhig, Bft 2'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.flock', 'Flock', 'calm', 'Preset calm() aus dem Package: ruhig, Bft 2', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ambientLux', 'DOUBLE', '20000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'nightLux', 'DOUBLE', '30' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeRadius', 'DOUBLE', '160' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coastDeg', 'DOUBLE', '150' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'dayOfYear', 'INT', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.flock' AS PAKET, 'Flock' AS KLASSE, 'breeze' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus dem Package: frische Brise, Bft 5'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.flock', 'Flock', 'breeze', 'Preset breeze() aus dem Package: frische Brise, Bft 5', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '6.7' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ambientLux', 'DOUBLE', '20000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'nightLux', 'DOUBLE', '30' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeRadius', 'DOUBLE', '160' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coastDeg', 'DOUBLE', '150' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'dayOfYear', 'INT', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.flock' AS PAKET, 'Flock' AS KLASSE, 'gale' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus dem Package: stürmisch, Bft 8'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.flock', 'Flock', 'gale', 'Preset gale() aus dem Package: stürmisch, Bft 8', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '19' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ambientLux', 'DOUBLE', '20000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'nightLux', 'DOUBLE', '30' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeRadius', 'DOUBLE', '160' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coastDeg', 'DOUBLE', '150' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'dayOfYear', 'INT', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.flock' AS PAKET, 'Flock' AS KLASSE, 'storm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus dem Package: schwerer Sturm, Bft 10'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.flock', 'Flock', 'storm', 'Preset storm() aus dem Package: schwerer Sturm, Bft 10', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ambientLux', 'DOUBLE', '20000' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'nightLux', 'DOUBLE', '30' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'homeRadius', 'DOUBLE', '160' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coastDeg', 'DOUBLE', '150' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'dayOfYear', 'INT', '200' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.flock' AND KLASSE = 'Flock' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.soundscape' AS PAKET, 'Soundscape' AS KLASSE, 'calm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus dem Package: ruhig, Bft 2'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.soundscape', 'Soundscape', 'calm', 'Preset calm() aus dem Package: ruhig, Bft 2', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'volume', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGain', 'BOOLEAN', 'true' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.18' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '0.1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wavePeriod', 'DOUBLE', '3' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfRadius', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfStrength', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '11' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earY', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earYawDeg', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'inside', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'glassDrops', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roofDrops', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reedNear', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'thatchNear', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'obstruction', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roughness', 'DOUBLE', '0.0002' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.soundscape' AS PAKET, 'Soundscape' AS KLASSE, 'breeze' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus dem Package: frische Brise, Bft 5'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.soundscape', 'Soundscape', 'breeze', 'Preset breeze() aus dem Package: frische Brise, Bft 5', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'volume', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGain', 'BOOLEAN', 'true' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '6.7' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.18' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wavePeriod', 'DOUBLE', '5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfRadius', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfStrength', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '11' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earY', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earYawDeg', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'inside', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'glassDrops', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roofDrops', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reedNear', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'thatchNear', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'obstruction', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roughness', 'DOUBLE', '0.0002' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.soundscape' AS PAKET, 'Soundscape' AS KLASSE, 'gale' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus dem Package: stürmisch, Bft 8'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.soundscape', 'Soundscape', 'gale', 'Preset gale() aus dem Package: stürmisch, Bft 8', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'volume', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGain', 'BOOLEAN', 'true' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '18.9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.25' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '5.5' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wavePeriod', 'DOUBLE', '9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfRadius', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfStrength', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '11' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earY', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earYawDeg', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'inside', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'glassDrops', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roofDrops', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reedNear', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'thatchNear', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'obstruction', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roughness', 'DOUBLE', '0.0002' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';

MERGE INTO LH_PACKAGE_PRESET t
USING (SELECT 'com.dan.soundscape' AS PAKET, 'Soundscape' AS KLASSE, 'storm' AS NAME FROM DUAL) q
ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus dem Package: schwerer Sturm, Bft 10'
  WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.soundscape', 'Soundscape', 'storm', 'Preset storm() aus dem Package: schwerer Sturm, Bft 10', 'MITGELIEFERT');
DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'volume', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGain', 'BOOLEAN', 'true' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.4' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.25' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '6' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '9' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wavePeriod', 'DOUBLE', '11' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfRadius', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfStrength', 'DOUBLE', '1' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '11' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earX', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earY', 'DOUBLE', '2' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earZ', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earYawDeg', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'inside', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'glassDrops', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roofDrops', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reedNear', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'thatchNear', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'obstruction', 'DOUBLE', '0' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roughness', 'DOUBLE', '0.0002' FROM LH_PACKAGE_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';

-- ----------------------------------------------------------------------------------------- Stand
MERGE INTO LH_SCHEMA t
USING (SELECT 1 AS VERSION FROM DUAL) q
ON (t.VERSION = q.VERSION)
WHEN MATCHED THEN UPDATE SET t.APP_VERSION = 'Phase 15'
WHEN NOT MATCHED THEN INSERT (VERSION, APP_VERSION, TEXT)
  VALUES (1, 'Phase 15', '13 Tabellen, 3 Sichten, Spatial, Startdaten: 9 Türme, 10 Wetterlagen, 3 Regiebücher, 2 Kamerafahrten, 40 Presets');

COMMIT;

-- Ende 04_startdaten.sql
