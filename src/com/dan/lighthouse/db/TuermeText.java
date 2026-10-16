package com.dan.lighthouse.db;

/**
 * Die Daten der neun Türme für {@link Startdaten} (F41 A). Recherchiert am 16.10.2026 aus deutsche-leuchtfeuer.de,
 * baken-net.de (Bekanntmachungen für Seefahrer), leuchtturm-atlas.de, Wikipedia und dem Lighthouse Directory; die
 * Quellen stehen je Turm in der letzten Spalte. Grenzen als Peilung von See zum Feuer, rechtweisend. Unbefeuerte
 * Bereiche stehen nicht als Sektor da. Was unsicher ist, sagt die Beschreibung ([G]).
 */
final class TuermeText {
    private TuermeText() { }

    static final String TEXT = """
T | Roter Sand | NORDSEE | 53.855 | 8.07917 | 24 | 28 | 9 | F WRG | DRUM | 1885 | 1964 | 1985 | rot-weiß gebändert (Weiß-Rot-Weiß-Rot-Weiß), ca. 8 m hoher schwarzer Sockel, schwarze Laterne/Dach (Kupferkuppel), drei Erker | 1880–1885 nach Entwurf von Carl Friedrich Hanckes auf einem Senkkasten in der Außenweser errichtet; gilt als erstes Offshore-Bauwerk der Welt. Heute Tagessichtzeichen und Denkmal. Kennung und Sektoren nach dem Verzeichnis von 1985 (F21); Lage wie in der Szene. | https://www.deutsche-leuchtfeuer.de/binnen/weser/roter-sand.html · https://de.wikipedia.org/wiki/Leuchtturm_Roter_Sand · https://en.wikipedia.org/wiki/Roter_Sand_Lighthouse · https://www.leuchtturm-atlas.de/HTML/RoSandT.html · https://www.ibiblio.org/lighthouse/debrh.htm
S | W | 167.5 | 185 | 9 | SEKTOR | 
S | R | 185 | 225 | 7 | SEKTOR | 
S | W | 225 | 254.5 | 9 | SEKTOR | 
S | R | 254.5 | 293.5 | 7 | SEKTOR | 
S | W | 293.5 | 312 | 9 | SEKTOR | 
S | R | 312 | 324 | 7 | SEKTOR | 
S | W | 324 | 328 | 9 | LEITSEKTOR | Leitsektor
S | G | 328 | 341 | 6 | SEKTOR | 
S | W | 341 | 167.5 | 3 | SCHWACH | Schwachlicht
T | Alte Weser | NORDSEE | 53.86333 | 8.12758 | 33.1 | 38.13 | 23 | F WRG | DRUM | 1964 |  | 2026 | roter Turm mit zwei weißen Bändern auf schwarzem Sockel, weiß/rote Aufbauten (Wohn-/Maschinendeck), grüne Laterne mit Radarantenne | 1961–1964 als Ersatz für Roter Sand errichteter Leuchtturm in der Außenweser, in Betrieb seit 1. September 1964; Leit- und Quermarkenfeuer mit Nebelhorn. | https://www.deutsche-leuchtfeuer.de/binnen/weser/alte-weser.html · https://www.baken-net.de/alte-weser.htm · https://www.leuchtturm-atlas.de/HTML/AlWeseT.html · https://en.wikipedia.org/wiki/Alte_Weser_Lighthouse · https://www.ibiblio.org/lighthouse/debrh.htm
S | W | 3 | 17 | 23 | SEKTOR | 
S | G | 17 | 45 | 18 | SEKTOR | 
S | W | 45 | 74 | 23 | SEKTOR | 
S | G | 74 | 121 | 18 | SEKTOR | 
S | W | 121 | 126 | 23 | SEKTOR | Leitsektor?
S | R | 126 | 140 | 19 | SEKTOR | 
S | G | 140 | 175 | 18 | SEKTOR | 
S | W | 175 | 183 | 23 | SEKTOR | Leitsektor?
S | R | 183 | 196 | 19 | SEKTOR | 
S | W | 196 | 238 | 23 | SEKTOR | 
S | W | 288 | 352 | 23 | SEKTOR | 
S | R | 352 | 3 | 19 | SEKTOR | 
T | Hohe Weg | NORDSEE | 53.71233 | 8.24322 | 29 | 36 | 17 | F WRG | DRUM | 1856 |  | 2026 | roter achteckiger Turm (seit 1996 rot lackierte Aluminiumverkleidung), weiße Absetzungen, zwei Galerien, grüne Laterne mit Radarantenne | 1854–1856 erbauter achteckiger Ziegelturm auf der Sandbank Hohe Weg in der Außenweser, ältestes feststehendes Leuchtfeuer der deutschen Nordsee; seit 1973 automatisiert. | https://www.deutsche-leuchtfeuer.de/binnen/weser/hohe-weg.html · https://www.baken-net.de/hohe-weg.htm · https://www.leuchtturm-atlas.de/HTML/HoWeg_T.html · https://en.wikipedia.org/wiki/Hohe_Weg_Lighthouse · https://de.wikipedia.org/wiki/Leuchtturm_Hohe_Weg
S | W | 102 | 138.5 | 17 | SEKTOR | Orientierungsfeuer
S | G | 138.5 | 142.5 | 13 | SEKTOR | Stb-Warnsektor
S | W | 142.5 | 145.5 | 17 | LEITSEKTOR | Leitsektor
S | R | 145.5 | 184 | 14 | SEKTOR | Bb-Warn- und Quermarkensektor
S | W | 184 | 278.5 | 17 | SEKTOR | Orientierungsfeuer
T | Westerheversand | NORDSEE | 54.37336 | 8.63992 | 41 | 40 | 21 | Oc(3) WRG 15s | DRUM | 1906 |  | 2026 | rot mit zwei weißen Bändern, zwei Galerien, schwarze Laterne und Galerie | 1906/07 aus gusseisernen Tübbings auf einer Warft vor Westerhever (Eiderstedt) errichtet, Feuer seit 26. Mai 1908; See-, Quermarken- und Leitfeuer und Wahrzeichen Eiderstedts. | https://www.deutsche-leuchtfeuer.de/nordsee/westerheversand.html · https://www.baken-net.de/westerhever.htm · https://www.leuchtturm-atlas.de/HTML/WerhevT.html · https://en.wikipedia.org/wiki/Westerheversand_Lighthouse · https://de.wikipedia.org/wiki/Leuchtturm_Westerheversand
S | W | 12.2 | 69 | 21 | SEKTOR | 
S | G | 69 | 85 | 16 | SEKTOR | 
S | W | 85 | 86 | 21 | LEITSEKTOR | Leitsektor (Mittelhever)
S | R | 86 | 107 | 17 | SEKTOR | 
S | W | 107 | 150 | 21 | SEKTOR | 
S | R | 150 | 207 | 17 | SEKTOR | 
S | W | 207 | 233 | 21 | SEKTOR | 
S | R | 233 | 248 | 17 | SEKTOR | 
T | Kiel | OSTSEE | 54.5 | 10.27361 | 29.25 | 33.5 | 17 | Iso WRG 6s | DRUM | 1967 |  | 2026 | roter runder Aluminiumturm mit weißem Mittelband, auf drei Stahlbeton-Fundamentkörpern | 1964–1967 auf der Position des Feuerschiffs Kiel errichteter Offshore-Leuchtturm in der Kieler Außenförde, in Betrieb seit 5. Juli 1967; Leit- und Orientierungsfeuer und Lotsenstation. Sektoren teils geschätzt [G]. Feuerhöhe über Mittelwasser angenommen. | https://www.deutsche-leuchtfeuer.de/ostsee/kiel.html · https://www.leuchtturm-atlas.de/HTML/Kiel__T.html · https://de.wikipedia.org/wiki/Leuchtturm_Kiel · https://cdn.marinetraffic.com/ro/ais/details/lights/1000006217 · https://www.ibiblio.org/lighthouse/deesh.htm · https://www.kiel.de/en/english_Kopie/outer_fjord.php
S | W | 148.5 | 220 | 17 | SEKTOR | 
S | R | 220 | 246.5 | 14 | SEKTOR | Warnsektor
S | W | 246.5 | 295 | 17 | SEKTOR | 
S | R | 295 | 358 | 14 | SEKTOR | Warnsektor
S | W | 358 | 25.5 | 17 | SEKTOR | 
S | G | 25.5 | 56 | 13 | SEKTOR | 
S | R | 56 | 71 | 14 | SEKTOR | 
S | R | 71 | 88 | 14 | SEKTOR | Warnsektor
S | W | 88 | 91.3 | 17 | SEKTOR | Leitsektor?
S | G | 91.3 | 148.5 | 13 | SEKTOR | 
T | Bülk | OSTSEE | 54.45522 | 10.19728 | 29.5 | 25.6 | 14 | Fl WRG 3s | DRUM | 1865 |  | 2026 | weißer runder Turm mit zwei grünen Bändern (seit 2020/21, vorher schwarzes Band), grüne untere Galerie, gelber Backsteinsockel, weiße Laterne | 1862/63 unter dänischer Verwaltung begonnener, 1865 von Preußen vollendeter Turm an der Bülker Huk, ältester Leuchtturm der Kieler Förde; Orientierungsfeuer mit Aussichtsplattform. Feuerhöhe über Mittelwasser angenommen. | https://www.deutsche-leuchtfeuer.de/ostsee/buelk.html · https://www.leuchtturm-atlas.de/HTML/Buelk_T.html · https://de.wikipedia.org/wiki/Leuchtturm_B%C3%BClk · https://en.wikipedia.org/wiki/B%C3%BClk_Lighthouse · https://www.ibiblio.org/lighthouse/deesh.htm
S | W | 127 | 146 | 14 | SEKTOR | 
S | R | 146 | 213 | 11 | SEKTOR | Warnsektor Stollergrund (Zuordnung abgeleitet)
S | G | 213 | 228 | 10 | SEKTOR | Kabeltrasse zum Leuchtturm Kiel
S | W | 228 | 235.5 | 14 | SEKTOR | 
S | R | 235.5 | 238.5 | 11 | SEKTOR | 
S | W | 238.5 | 262 | 14 | SEKTOR | 
S | G | 262 | 43 | 10 | SEKTOR | 
T | Flügge | OSTSEE | 54.44092 | 11.01769 | 38 | 37 | 17 | Oc(4) W 20s | DRUM | 1916 |  | 2026 | unverputztes gelb-rotes Backsteinmauerwerk (seit 2010 wieder, 1977–2009 rot-weiß verkleidet), rote Laterne, roter Wachraum und Galerien | 1914/15 erbauter, 1916 in Betrieb genommener achteckiger Backsteinturm im Südwesten Fehmarns; Seefeuer und zugleich Oberfeuer der Richtfeuerlinie Strukkamphuk zur Fehmarnsundbrücke. Feuerhöhe über Mittelwasser angenommen. | https://www.deutsche-leuchtfeuer.de/ostsee/fluegge.html · https://www.leuchtturm-atlas.de/HTML/FlueggT.html · https://de.wikipedia.org/wiki/Leuchtturm_Fl%C3%BCgge · https://www.ibiblio.org/lighthouse/deesh.htm
S | W | 245 | 180 | 17 | SEKTOR | Seefeuer (Sichtbereich)
T | Dahmeshöved | OSTSEE | 54.20178 | 11.09025 | 33.7 | 28.8 | 23 | Fl(3) W 12s | ROTATING | 1879 |  | 2026 | rotbraunes Ziegelmauerwerk (seit 1982 ohne Anstrich; vorher rot mit weißem Band), rote Laterne und Wachraum | 1878/79 nach Plänen von Ludwig Alexander Veitmeyer erbauter achteckiger Ziegelturm südlich von Dahme, Feuer seit 1. Februar 1880; Seefeuer für Lübecker und Mecklenburger Bucht. Sektoren teils geschätzt [G]. Sichtbereich nicht belegt, als Rundumfeuer eingetragen [G]. Feuerhöhe über Mittelwasser angenommen. | https://www.deutsche-leuchtfeuer.de/ostsee/dahmeshoeved.html · https://www.leuchtturm-atlas.de/HTML/DahmesT.html · https://de.wikipedia.org/wiki/Leuchtturm_Dahmesh%C3%B6ved · https://www.ibiblio.org/lighthouse/deesh.htm
S | W | 0 | 360 | 23 | SEKTOR | 
T | Tegeler Plate | NORDSEE | 53.79722 | 8.19111 | 20.9 | 24.5 | 21 | Oc(3) WRG 12s | DRUM | 1965 |  | 2026 | roter runder Stahlturm mit auskragendem Geschoss, weiße Laterne, rotes Dach | In einer Wilhelmshavener Werft komplett gefertigter Stahlturm, im Juli 1965 an das Nordwestende der Tegeler Plate gebracht; ersetzte das Feuerschiff Weser auf Station Bremen; Leit- und Quermarkenfeuer. Lage nach deutsche-leuchtfeuer.de wie seit Phase 9 in der Szene; baken-net.de nennt eine rund 230 m entfernte Lage. | https://www.deutsche-leuchtfeuer.de/binnen/weser/tegeler-plate.html · https://www.baken-net.de/tegeler-plate.htm · https://www.leuchtturm-atlas.de/HTML/TegeleT.html · https://www.leuchttuerme.net/index.php?nav=1000001&id=32&lang=1&action=portrait · https://www.ibiblio.org/lighthouse/debrh.htm
S | W | 14 | 100 | 21 | SEKTOR | 
S | G | 100 | 116 | 17 | SEKTOR | 
S | W | 116 | 119 | 21 | LEITSEKTOR | Leitsektor Neue Weser
S | R | 119 | 123 | 18 | SEKTOR | 
S | G | 123 | 148.3 | 17 | SEKTOR | 
S | W | 148.3 | 150.3 | 21 | LEITSEKTOR | Leitsektor Alte Weser
S | R | 150.3 | 264 | 18 | SEKTOR | 
S | W | 329 | 340 | 21 | SEKTOR | 
S | R | 340 | 14 | 18 | SEKTOR | 
            """;
}
