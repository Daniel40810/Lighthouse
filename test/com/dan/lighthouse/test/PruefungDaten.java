package com.dan.lighthouse.test;

import com.dan.beacon.Beacon;
import com.dan.beacon.Characteristic;
import com.dan.beacon.SectorTable;
import com.dan.lighthouse.camera.Aufstieg;
import com.dan.lighthouse.db.Daten;
import com.dan.lighthouse.db.DateiSpeicher;
import com.dan.lighthouse.db.Einrichter;
import com.dan.lighthouse.db.Felder;
import com.dan.lighthouse.db.Skript;
import com.dan.lighthouse.db.Speicher;
import com.dan.lighthouse.db.Startdaten;
import com.dan.lighthouse.db.TurmWahl;
import com.dan.lighthouse.db.Zustand;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.scene.Site;
import com.dan.lighthouse.wiring.Wiring;
import com.dan.ocean.Ocean;
import com.dan.weather.Scenario;
import com.dan.weather.Weather;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.dan.lighthouse.test.Pruefung.check;

/**
 * Phase 15: Prüfungen der Datenseite ohne Datenbank: Skripte, Startdaten, Türme, Felder und Textformat, Zustand,
 * Turmauswahl und der ganze Kreislauf Speichern und Laden mit Dateien. Was nur mit Oracle geht, prüft {@link DbPruefung}.
 */
final class PruefungDaten {
    private PruefungDaten() { }

    static void alles() throws Exception {
        skripte();
        startdaten();
        felder();
        zustand();
        turmwahl();
        dateien();
    }

    static String lies(String f) throws Exception { return Files.readString(new File(f).toPath(), StandardCharsets.UTF_8); }

    static void skripte() throws Exception {
        System.out.println("Datenbank: Skripte in db/sql");
        List<Skript.Anweisung> t01 = Skript.zerlegen(lies("db/sql/01_tabellen.sql"));
        Set<String> tab = new HashSet<>(), seq = new HashSet<>(), trg = new HashSet<>();
        for (Skript.Anweisung a : t01) {
            String o = Skript.objekt(a.sql());
            if (o == null) continue;
            if (o.startsWith("TABLE ")) tab.add(o.substring(6));
            if (o.startsWith("SEQUENCE ")) seq.add(o.substring(9));
            if (o.startsWith("TRIGGER ")) trg.add(o.substring(8));
        }
        String[] soll = {"LH_SCHEMA", "LH_LIGHTHOUSE", "LH_SECTOR", "LH_WEATHER_PRESET", "LH_SCENARIO", "LH_SCENARIO_STEP", "LH_CAMERA_PATH", "LH_CAMERA_POINT",
                "LH_SNAPSHOT", "LH_PACKAGE_PRESET", "LH_PACKAGE_SETTING", "LH_SESSION", "LH_LOG"};
        check(tab.equals(Set.of(soll)), "01_tabellen.sql legt die 13 Tabellen aus Abschnitt 8 an (" + tab.size() + ")");
        check(seq.size() == 12 && trg.size() == 11, "12 Sequenzen und 11 Trigger (LH_SCHEMA ohne, LH_LOG mit Sequence, ohne Trigger): " + seq.size() + "/" + trg.size());
        boolean jeTrigger = true;
        for (String t : trg) jeTrigger &= seq.contains(t.replace("_BIU", "_SEQ")) && tab.contains(t.replace("_BIU", ""));
        check(jeTrigger, "Jeder Trigger hat seine Tabelle und Sequence (Sequence + Trigger, nie IDENTITY)");
        check(t01.stream().noneMatch(a -> a.sql().toUpperCase(Locale.ROOT).contains("IDENTITY")), "Keine Anweisung mit GENERATED AS IDENTITY");
        List<Skript.Spalte> sp = Skript.spalten(t01);
        Map<String, Integer> jeTab = new HashMap<>();
        for (Skript.Spalte s : sp) jeTab.merge(s.tabelle(), 1, Integer::sum);
        check(sp.size() > 150 && jeTab.size() == 13, "Spalten aus den CREATE TABLE gelesen: " + sp.size() + " in " + jeTab.size() + " Tabellen");
        boolean ids = true;
        for (String t : soll) if (!t.equals("LH_SCHEMA")) ids &= sp.stream().anyMatch(s -> s.tabelle().equals(t) && s.name().equals("ID"));
        check(ids, "Jede Tabelle außer LH_SCHEMA hat die Spalte ID");
        boolean wetter = true;
        for (String t : new String[]{"LH_WEATHER_PRESET", "LH_SCENARIO_STEP"})
            for (String c : Startdaten.SPALTEN) wetter &= sp.stream().anyMatch(s -> s.tabelle().equals(t) && s.name().equals(c));
        check(wetter, "LH_WEATHER_PRESET und LH_SCENARIO_STEP haben alle 14 Wetterspalten (Weather.Var)");
        check(sp.stream().anyMatch(s -> s.tabelle().equals("LH_LIGHTHOUSE") && s.name().equals("LAGE") && Einrichterhilfe.basis(s.typ()).equals("SDO_GEOMETRY")),
                "LH_LIGHTHOUSE.LAGE ist SDO_GEOMETRY");
        List<Skript.Anweisung> t02 = Skript.zerlegen(lies("db/sql/02_spatial.sql")), t03 = Skript.zerlegen(lies("db/sql/03_sichten.sql")),
                t04 = Skript.zerlegen(lies("db/sql/04_startdaten.sql"));
        check(t02.size() == 2 && t02.get(0).plsql() && t02.get(1).sql().contains("SPATIAL_INDEX_V2") && t02.get(0).sql().contains("8307"),
                "02_spatial.sql: Metadaten als PL/SQL-Block (SRID 8307), dann der räumliche Index");
        check(t03.size() == 3 && t03.stream().allMatch(a -> a.sql().startsWith("CREATE OR REPLACE VIEW")), "03_sichten.sql: drei Sichten, wiederholbar");
        check(t04.size() > 800 && t04.stream().noneMatch(Skript.Anweisung::plsql), "04_startdaten.sql: " + t04.size() + " Anweisungen, ohne PL/SQL");
        boolean semi = true;
        for (List<Skript.Anweisung> l : List.of(t01, t02, t03, t04)) for (Skript.Anweisung a : l) if (!a.plsql() && a.sql().endsWith(";")) semi = false;
        check(semi, "Zerlegen: Strichpunkt am Ende abgelöst, PL/SQL-Blöcke bis zur Zeile /");
        check(Einrichter.SKRIPTE.length == 4 && !String.join(",", Einrichter.SKRIPTE).contains("99"), "Der Einrichter führt 01 bis 04 aus, nie 99_entfernen.sql");
        String gen = Startdaten.sql(), datei = lies("db/sql/04_startdaten.sql");
        check(gen.equals(datei), "04_startdaten.sql ist aktuell (erzeugt aus Startdaten; sonst tools.StartdatenSql laufen lassen)");
        // Längen: jeder mitgelieferte Text passt in seine Spalte
        Map<String, Integer> len = new HashMap<>();
        for (Skript.Spalte s : sp) len.put(s.tabelle() + "." + s.name(), Skript.laenge(s));
        List<String> zuLang = new ArrayList<>();
        for (Daten.Turm t : Startdaten.tuerme()) {
            pass(zuLang, len, "LH_LIGHTHOUSE.NAME", t.name());
            pass(zuLang, len, "LH_LIGHTHOUSE.KENNUNG", t.kennung());
            pass(zuLang, len, "LH_LIGHTHOUSE.FARBEN", t.farben());
            pass(zuLang, len, "LH_LIGHTHOUSE.BESCHREIBUNG", t.beschreibung());
            pass(zuLang, len, "LH_LIGHTHOUSE.QUELLE", t.quelle());
            for (Daten.Sektor x : t.sektoren()) pass(zuLang, len, "LH_SECTOR.BEZEICHNUNG", x.bezeichnung());
        }
        for (Daten.Regiebuch r : Startdaten.regiebuecher()) {
            pass(zuLang, len, "LH_SCENARIO.BESCHREIBUNG", r.beschreibung());
            for (Scenario.Step s : Scenario.parse(r.text()).steps()) { pass(zuLang, len, "LH_SCENARIO_STEP.TEXT", s.text()); pass(zuLang, len, "LH_SCENARIO_STEP.KAMERA", s.camera()); }
        }
        for (Daten.PaketPreset p : Startdaten.presets()) {
            pass(zuLang, len, "LH_PACKAGE_PRESET.BESCHREIBUNG", p.beschreibung());
            for (Map.Entry<String, String> e : p.felder().entrySet()) { pass(zuLang, len, "LH_PACKAGE_SETTING.FELD", e.getKey()); pass(zuLang, len, "LH_PACKAGE_SETTING.WERT", e.getValue()); }
        }
        check(zuLang.isEmpty(), "Alle mitgelieferten Texte passen in ihre Spalten" + (zuLang.isEmpty() ? "" : ": " + zuLang));
        String sqlAll = lies("db/sql/01_tabellen.sql") + lies("db/sql/02_spatial.sql") + lies("db/sql/03_sichten.sql");
        check(!sqlAll.contains("&") && datei.contains("SET DEFINE OFF"), "Kein & in den Skripten außer den Quellen, und dort SET DEFINE OFF");
    }

    /** Einrichter.basis ist paketintern; hier dieselbe Regel. */
    static final class Einrichterhilfe {
        static String basis(String t) {
            String s = t.toUpperCase(Locale.ROOT);
            int i = s.indexOf('(');
            if (i > 0) s = s.substring(0, i);
            return s.startsWith("MDSYS.") ? s.substring(6) : s;
        }
    }

    private static void pass(List<String> l, Map<String, Integer> len, String spalte, String wert) {
        Integer n = len.get(spalte);
        if (wert != null && n != null && n > 0 && wert.length() > n) l.add(spalte + " " + wert.length() + " > " + n);
    }

    static void startdaten() {
        System.out.println("Datenbank: Startdaten");
        List<Daten.Turm> t = Startdaten.tuerme();
        check(t.size() == 9, "Neun Türme: " + Startdaten.turmNamen());
        boolean kennung = true, sektoren = true;
        StringBuilder fail = new StringBuilder();
        for (Daten.Turm x : t) {
            try { Characteristic.parse(x.kennung()); } catch (Exception e) { kennung = false; fail.append(x.name()).append(": ").append(e.getMessage()).append(' '); }
            try { SectorTable.parse(x.sektorenText(), 10); TurmWahl.feuer(x); } catch (RuntimeException e) { sektoren = false; fail.append(x.name()).append(": ").append(e.getMessage()).append(' '); }
        }
        check(kennung, "Jede Kennung liest com.dan.beacon" + (kennung ? "" : ": " + fail));
        check(sektoren, "Jede Sektortafel liest com.dan.beacon, jedes Feuer lässt sich bauen" + (sektoren ? "" : ": " + fail));
        boolean quellen = t.stream().allMatch(x -> x.quelle() != null && x.quelle().startsWith("https://"));
        check(quellen, "Jeder Turm hat seine Quellen (F41)");
        boolean lage = t.stream().allMatch(x -> x.breite() > 53.5 && x.breite() < 54.6 && x.laenge() > 7.9 && x.laenge() < 11.2);
        check(lage, "Alle Lagen in der Deutschen Bucht und der westlichen Ostsee");
        Daten.Turm rs = t.get(0);
        Beacon a = TurmWahl.feuer(rs), b = Beacon.roterSand1985();
        boolean gleich = a.characteristic().text.equals(b.characteristic().text) && a.sectors().sectors().size() == b.sectors().sectors().size();
        for (double brg = 0; brg < 360 && gleich; brg += 0.5) gleich = a.sectors().at(brg).colour() == b.sectors().at(brg).colour()
                && Math.abs(a.sectors().at(brg).rangeNm() - b.sectors().at(brg).rangeNm()) < 1e-9;
        check(gleich, "Roter Sand aus den Startdaten = Beacon.roterSand1985 (Kennung, alle Sektoren je halbes Grad)");
        check(Math.abs(rs.breite() - Site.LAT) < 1e-5 && Math.abs(rs.laenge() - Site.LON) < 1e-5, "Roter Sand liegt in den Daten, wo er in der Szene steht");
        Daten.Turm kiel = t.stream().filter(x -> x.name().equals("Kiel")).findFirst().orElseThrow();
        Daten.Turm buelk = t.stream().filter(x -> x.name().equals("Bülk")).findFirst().orElseThrow();
        Daten.Turm aw = t.stream().filter(x -> x.name().equals("Alte Weser")).findFirst().orElseThrow();
        check(Math.abs(rs.abstandKm(aw) - 3.4) < 0.3, String.format(Locale.GERMAN, "Roter Sand – Alte Weser %.2f km (Karte rund 3,4 km)", rs.abstandKm(aw)));
        check(kiel.abstandKm(buelk) > 5 && kiel.abstandKm(buelk) < 9, String.format(Locale.GERMAN, "Kiel – Bülk %.2f km (quer über die Außenförde)", kiel.abstandKm(buelk)));
        check(t.stream().filter(Daten.Turm::ostsee).count() == 4, "Vier Türme in der Ostsee, fünf in der Nordsee");
        List<Daten.Wetterlage> w = Startdaten.wetterlagen();
        check(w.size() == 10 && w.stream().allMatch(x -> x.werte().length == Weather.Var.values().length), "Zehn Wetterlagen, je 14 Größen");
        check(w.get(3).name().equals("Sturm") && w.get(3).beaufort() == 9, "„Sturm“ ist Bft 9 wie die Lage STURM der Regie");
        boolean regie = true;
        for (Daten.Regiebuch r : Startdaten.regiebuecher()) regie &= Scenario.parse(r.text()).toText().equals(r.text());
        check(Startdaten.regiebuecher().size() == 3 && regie, "Drei Regiebücher, Text hin und zurück gleich");
        check(Startdaten.presets().size() == 40, "Presets calm, breeze, gale, storm von zehn Packages: " + Startdaten.presets().size());
        int felder = Startdaten.presets().stream().mapToInt(p -> p.felder().size()).sum();
        check(felder > 300, "Felder der Presets: " + felder);
        // Fahrten: hin und zurück, Schnitte bleiben
        Aufstieg auf = new Aufstieg(), zur = Startdaten.fahrt(Startdaten.fahrt(auf, "AUFSTIEG", ""));
        double err = 0;
        for (double tt = 0; tt < auf.duration(); tt += 0.37) {
            double[] p = auf.sample(tt), q = zur.sample(tt);
            for (int i = 0; i < 5; i++) err = Math.max(err, Math.abs(p[i] - q[i]));
        }
        check(err < 0.02, String.format(Locale.GERMAN, "Aufstieg als Datensatz und zurück: größte Abweichung %.3f (m oder Grad), Schnitte bleiben", err));
        Aufstieg rf = Aufstieg.rundflug(140, 34, 120);
        double[] p0 = rf.sample(0), p1 = rf.sample(60);
        check(rf.loop && Math.abs(Math.hypot(p0[0], p0[2]) - 140 * 1) < 20 && Math.abs(Math.hypot(p1[0], p1[2])) > 110, "Rundflug kreist um den Turm und läuft im Kreis");
    }

    static void felder() {
        System.out.println("Datenbank: Felder und Textformat");
        Ocean g = Ocean.gale();
        LinkedHashMap<String, String> m = Felder.lesen(g);
        Ocean o = new Ocean();
        List<String> f = Felder.schreiben(o, m);
        check(f.isEmpty() && Felder.lesen(o).equals(m), "Ocean.gale(): " + m.size() + " Felder gelesen, in ein neues Ocean geschrieben, gleich");
        Map<String, String> falsch = new LinkedHashMap<>();
        falsch.put("gibtsNicht", "1");
        falsch.put("windSpeed", "viel");
        check(Felder.schreiben(new Ocean(), falsch).size() == 2, "Unbekanntes Feld und falscher Wert werden gemeldet, nicht geschrieben");
        LinkedHashMap<String, String> t = new LinkedHashMap<>();
        t.put("a", "eins\nzwei");
        t.put("b", "C:\\Pfad\\n");
        t.put("c", "x=y");
        t.put("d", "");
        String text = Felder.alsText(t, "Kopf\nzweite Zeile");
        check(Felder.ausText(text).equals(t), "Textformat: Zeilenumbruch, Rückstrich und = im Wert hin und zurück");
        check(Felder.typ(double.class).equals("DOUBLE") && Felder.typ(boolean.class).equals("BOOLEAN") && Felder.typ(Weather.Phase.class).equals("ENUM")
                && Felder.typ(int[].class) == null, "Feldarten wie in LH_PACKAGE_SETTING.TYP; Arrays bleiben außen vor");
    }

    static Renderer renderer() {
        SceneClock c = new SceneClock(LocalDate.of(2026, 10, 16), 20.0);
        Wiring w = new Wiring(c);
        Renderer r = new Renderer(c, w);
        w.weather.startFree(c.epochMillis() / 1000.0, Weather.Phase.KLAR);
        return r;
    }

    static void zustand() {
        System.out.println("Datenbank: Zustand der Szene");
        Renderer a = renderer();
        a.ocean.choppiness = 1.234;
        a.spray.strength = 1.7;
        a.rainStrength = 0.4;
        a.showGulls = false;
        a.beamStrength = 2.5;
        a.clock.lapse = 60;
        a.clock.setHour(14.5);
        a.camera.place(30, 12, -50, 123, 7);
        a.camera.snap();
        a.wiring.hand(Weather.Var.WIND, 18);
        a.wiring.hand(Weather.Var.FOG, 0.6);
        a.beacon.setCharacteristic("Fl(3) W 15s");
        Scenario sc = Scenario.stormDay();
        a.wiring.weather.start(sc, a.clock.epochMillis() / 1000.0 - 7200);
        a.wiring.weather.update(a.clock.epochMillis() / 1000.0, 0);
        LinkedHashMap<String, String> z = Zustand.erfassen(a, false);
        check(z.size() > 200, "Zustand erfasst: " + z.size() + " Werte");
        String text = Felder.alsText(z, "Prüfung");
        Renderer b = renderer();
        List<String> f = Zustand.anwenden(b, Felder.ausText(text));
        check(f.isEmpty(), "Zustand auf eine frische Szene angewandt, nichts abgelehnt" + (f.isEmpty() ? "" : ": " + f.subList(0, Math.min(5, f.size()))));
        b.wiring.weather.update(b.clock.epochMillis() / 1000.0, 0);
        LinkedHashMap<String, String> z2 = Zustand.erfassen(b, false);
        List<String> diff = new ArrayList<>();
        for (Map.Entry<String, String> e : z.entrySet()) if (!e.getValue().equals(z2.get(e.getKey()))) diff.add(e.getKey() + " " + e.getValue() + " ≠ " + z2.get(e.getKey()));
        check(diff.isEmpty(), "Zweimal erfasst, gleich: Szene, Uhr, Kamera, Regiebuch mit Stelle, Handregler, Kennung" + (diff.isEmpty() ? "" : " – " + diff.subList(0, Math.min(6, diff.size()))));
        check(b.ocean.choppiness == 1.234 && b.spray.strength == 1.7 && !b.showGulls && b.clock.lapse == 60, "Einzelne Werte stimmen (Meer, Gischt, Möwen, Zeitraffer)");
        check(b.wiring.weather.scenario() != null && b.wiring.weather.scenario().name().equals("Ein Sturmtag")
                && Math.abs(Zustand.regieStunde(z2) - 2) < 1e-6, "Das Regiebuch läuft weiter, an derselben Stelle (2 Stunden nach Beginn)");
        check(b.beacon.characteristic().text.equals("Fl(3) W 15s"), "Die Kennung kommt mit");
        double[] k = Zustand.kamera(b);
        check(Math.abs(k[0] - 30) < 0.05 && Math.abs(k[1] - 12) < 0.05 && Math.abs(k[2] + 50) < 0.05 && Math.abs(k[3] - 123) < 0.1, "Die Kamera steht wieder am selben Ort mit demselben Blick");
    }

    static void turmwahl() {
        System.out.println("Datenbank: Leuchtturm-Auswahl");
        Renderer r = renderer();
        List<Daten.Turm> t = Startdaten.tuerme();
        Daten.Turm kiel = t.stream().filter(x -> x.name().equals("Kiel")).findFirst().orElseThrow();
        String z = TurmWahl.waehlen(r, kiel, t);
        check(r.siteName.equals("Kiel") && !r.homeSite && r.beacon.characteristic().text.equals(kiel.kennung()), "Kiel gewählt: " + z);
        check(!r.wiring.tide.on && r.wiring.tide.fixed == 0, "Ostsee: keine Gezeiten");
        check(Math.abs(r.clock.lat - kiel.breite()) < 1e-9 && Math.abs(r.sky.latDeg - kiel.breite()) < 1e-9, "Sonne, Mond und Sterne für 54,5° N");
        check(r.neighbours.length >= 1 && r.sky.domes.isEmpty(), "Nachbarn aus den Daten (" + r.neighbours.length + "), keine Lichtglocken der Weserorte");
        boolean bulk = false;
        for (Beacon n : r.neighbours) if (n.name.equals("Bülk")) {
            double d = Math.hypot(n.x, n.z) / 1000;
            bulk = Math.abs(d - kiel.abstandKm(t.stream().filter(x -> x.name().equals("Bülk")).findFirst().orElseThrow())) < 0.05 && n.z > 0;
        }
        check(bulk, "Bülk liegt im richtigen Abstand südlich von Kiel");
        TurmWahl.waehlen(r, t.get(0), t);
        check(r.homeSite && r.neighbours.length == 2 && r.wiring.tide.on && r.sky.domes.size() == Site.TOWNS.length && r.beacon.characteristic().text.equals("F WRG"),
                "Zurück zum Roten Sand: Alte Weser und Tegeler Plate, Gezeiten, Lichtglocken, F WRG");
    }

    static void dateien() throws Exception {
        System.out.println("Datenbank: Speichern und Laden ohne Datenbank (daten/)");
        File dir = Files.createTempDirectory("lighthouse-daten").toFile();
        DateiSpeicher s = new DateiSpeicher(dir);
        Daten.Stand st = s.stand();
        check(st.tuerme() == 9 && st.wetterlagen() == 10 && st.regiebuecher() == 3 && st.fahrten() == 2 && st.presets() == 40 && st.letzte() == null,
                "Leerer Ordner: die mitgelieferten Daten sind da, keine Sitzung");
        double[] w = Weather.target(Weather.Phase.STURM);
        w[Weather.Var.FOG.ordinal()] = Double.NaN;
        s.speichern(new Daten.Wetterlage("~Prüfung Sturm", 9.0, "STURM", w, "von der Prüfung", Daten.EIGEN));
        Daten.Wetterlage wl = s.wetterlagen().stream().filter(x -> x.name().equals("~Prüfung Sturm")).findFirst().orElse(null);
        check(wl != null && Daten.EIGEN.equals(wl.herkunft()) && Double.isNaN(wl.werte()[Weather.Var.FOG.ordinal()])
                && wl.werte()[0] == w[0], "Wetterlage gespeichert und gelesen, leere Größe bleibt leer");
        boolean abgelehnt = false;
        try { s.speichern(new Daten.Wetterlage("Sturm", 9.0, "STURM", w, "", Daten.EIGEN)); } catch (Speicher.Fehler e) { abgelehnt = true; }
        check(abgelehnt, "Eine mitgelieferte Wetterlage wird nicht überschrieben");
        Scenario sc = new Scenario("~Prüfung Regie", 7.5, 300, Scenario.fogDay().steps());
        s.speichern(new Daten.Regiebuch(sc.name(), sc.toText(), "", Daten.EIGEN));
        Daten.Regiebuch rb = s.regiebuecher().stream().filter(x -> x.name().equals("~Prüfung Regie")).findFirst().orElse(null);
        check(rb != null && rb.text().equals(sc.toText()), "Regiebuch gespeichert und gelesen, Text gleich");
        Daten.Fahrt fa = Startdaten.fahrt(Aufstieg.rundflug(80, 20, 30), "FREI", "");
        fa = new Daten.Fahrt("~Prüfung Fahrt", "FREI", true, "", Daten.EIGEN, fa.punkte());
        s.speichern(fa);
        Daten.Fahrt fb = s.fahrten().stream().filter(x -> x.name().equals("~Prüfung Fahrt")).findFirst().orElse(null);
        check(fb != null && fb.punkte().equals(fa.punkte()) && fb.schleife(), "Kamerafahrt gespeichert und gelesen, " + fa.punkte().size() + " Stützpunkte gleich");
        Renderer r = renderer();
        String zs = Felder.alsText(Zustand.erfassen(r, true), null);
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};
        long id = s.speichern(new Daten.Ansicht(0, "~Prüfung Ansicht", 1_792_180_800_000L, new double[]{1, 2, 3, 4, 5, 55}, "Roter Sand", zs, png), null);
        Daten.Ansicht an = s.ansicht(id);
        check(an.zustand().equals(zs) && java.util.Arrays.equals(an.vorschau(), png) && an.kamera()[5] == 55 && s.ansichten().size() == 1,
                "Ansicht mit Zustand und Vorschau gespeichert und gelesen");
        Map<String, String> pf = Felder.lesen(Ocean.storm());
        s.speichern(new Daten.PaketPreset("com.dan.ocean", "Ocean", "~Prüfung", "", Daten.EIGEN, pf));
        Daten.PaketPreset pp = s.presets("com.dan.ocean", "Ocean").stream().filter(x -> x.name().equals("~Prüfung")).findFirst().orElse(null);
        check(pp != null && pp.felder().equals(pf) && s.presets("com.dan.ocean", "Ocean").size() == 5, "Preset gespeichert und gelesen (4 mitgelieferte + 1 eigenes)");
        long s1 = s.sitzungBeginnen("Phase 15", "Prüfung", "HIGH");
        s.sitzungSichern(s1, zs, 1_792_180_800_000L, "Kiel", "Ein Nebeltag", 1.5);
        long s2 = s.sitzungBeginnen("Phase 15", "Prüfung", "HIGH");
        Daten.Sitzung ls = s.letzteSitzung(s2);
        check(ls != null && ls.id() == s1 && !ls.sauber() && ls.zustand().equals(zs) && "Kiel".equals(ls.turm()),
                "Abgebrochene Sitzung: „Weiter wie zuletzt“ findet ihren Zustand");
        s.sitzungSichern(s2, "x=1", 0, "", "", Double.NaN);
        s.sitzungBeenden(s2);
        ls = s.letzteSitzung(-1);
        check(ls != null && ls.id() == s2 && ls.sauber(), "Geordnet beendete Sitzung ist die letzte");
        s.protokoll(List.of(new Daten.LogZeile(System.currentTimeMillis(), "WARNUNG", "Prüfung", "eine Warnung", 1_792_180_800_000L, "Galerie", "Stapel\nzweite Zeile")), s1);
        List<Daten.LogZeile> lz = s.letzteZeilen(5);
        check(lz.size() == 1 && lz.get(0).text().equals("eine Warnung") && lz.get(0).ort().equals("Galerie"), "Protokoll geschrieben und gelesen");
        s.loeschenWetterlage("~Prüfung Sturm");
        s.loeschenRegiebuch("~Prüfung Regie");
        s.loeschenFahrt("~Prüfung Fahrt");
        s.loeschenAnsicht(id);
        s.loeschenPreset("com.dan.ocean", "Ocean", "~Prüfung");
        Daten.Stand nach = s.stand();
        check(nach.wetterlagen() == 10 && nach.regiebuecher() == 3 && nach.fahrten() == 2 && nach.ansichten() == 0 && nach.presets() == 40, "Alles wieder gelöscht");
    }
}
