package com.dan.lighthouse.test;

import com.dan.lighthouse.db.Daten;
import com.dan.lighthouse.db.DateiSpeicher;
import com.dan.lighthouse.db.Db;
import com.dan.lighthouse.db.DbSpeicher;
import com.dan.lighthouse.db.Einrichter;
import com.dan.lighthouse.db.Felder;
import com.dan.lighthouse.db.Speicher;
import com.dan.lighthouse.db.Startdaten;
import com.dan.lighthouse.db.TurmWahl;
import com.dan.ocean.Ocean;
import com.dan.weather.Scenario;
import com.dan.weather.Weather;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Phase 15: die Prüfung gegen die echte Datenbank (PDBORCL, Schema aus db/db.properties). In NetBeans: Rechtsklick,
 * „Run File“. Sie richtet ein (wiederholbar), läuft den Einrichter ein zweites Mal (muss ohne Fehler durchgehen),
 * prüft alles mit dem Einrichter, speichert und lädt jede Art von Daten unter Namen „~Prüfung …“, vergleicht mit der
 * Datei-Variante und dem räumlich gerechneten Abstand, und räumt hinterher auf. Ende: „Alles in Ordnung.“
 * <p>
 * Die Ausgabe bitte als Text oder Bildschirmfoto schicken; jede Zeile mit FEHLER sagt, was nicht ging.
 */
public final class DbPruefung {
    private static int ok, bad;

    static void check(boolean c, String was) {
        if (c) ok++; else bad++;
        System.out.println((c ? "  ok     " : "  FEHLER ") + was);
    }

    public static void main(String[] args) throws Exception {
        long t0 = System.nanoTime();
        System.out.println("Lighthouse · Prüfung der Datenbank (Phase 15)");
        Db.Konfig k = Db.konfig();
        System.out.println("Verbindung " + k.kurz());
        DbSpeicher db = new DbSpeicher(k);
        System.out.println(db.wo());

        System.out.println("Einrichter");
        Einrichter.Ergebnis e1 = db.einrichten((t, a) -> { });
        for (String z : e1.zeilen()) if (!z.startsWith("ok")) System.out.println("    " + z);
        check(e1.ok(), "Erster Lauf: " + e1.kurz() + " (" + e1.ausgefuehrt() + " ausgeführt, " + e1.vorhanden() + " schon da)");
        Einrichter.Ergebnis e2 = db.einrichten((t, a) -> { });
        check(e2.ok() && e2.fehler() == 0, "Zweiter Lauf geht ohne Fehler durch (wiederholbar): " + e2.kurz());
        Einrichter.Ergebnis p = db.pruefen();
        for (String z : p.zeilen()) System.out.println("    " + z);
        check(p.ok() && p.zeilen().contains("alles in Ordnung"), "Einrichter meldet „alles in Ordnung“");
        check(db.stufe() == Einrichter.VERSION, "Ausbaustufe " + db.stufe());

        System.out.println("Trigger und Spatial");
        Connection c = db.verbindung();
        try (Statement st = c.createStatement()) {
            st.executeUpdate("INSERT INTO LH_WEATHER_PRESET (NAME, HERKUNFT) VALUES ('~Prüfung Trigger', 'EIGEN')");
            try (ResultSet r = st.executeQuery("SELECT ID, ANGELEGT, GEAENDERT FROM LH_WEATHER_PRESET WHERE NAME = '~Prüfung Trigger'")) {
                check(r.next() && r.getLong(1) > 0 && r.getTimestamp(2) != null && r.getTimestamp(3) != null, "Trigger setzt ID, ANGELEGT und GEAENDERT");
            }
            st.executeUpdate("DELETE FROM LH_WEATHER_PRESET WHERE NAME = '~Prüfung Trigger'");
            c.commit();
            try (ResultSet r = st.executeQuery("SELECT COUNT(*) FROM LH_LIGHTHOUSE_V WHERE BREITE IS NULL OR LAENGE IS NULL")) {
                check(r.next() && r.getInt(1) == 0, "LH_LIGHTHOUSE_V liest Breite und Länge aus SDO_GEOMETRY");
            }
            try (ResultSet r = st.executeQuery("SELECT t.NAME FROM LH_LIGHTHOUSE t WHERE SDO_WITHIN_DISTANCE(t.LAGE, "
                    + "SDO_GEOMETRY(2001, 8307, SDO_POINT_TYPE(8.07917, 53.855, NULL), NULL, NULL), 'distance=5 unit=KM') = 'TRUE' ORDER BY t.NAME")) {
                StringBuilder b = new StringBuilder();
                while (r.next()) b.append(b.length() == 0 ? "" : ", ").append(r.getString(1));
                check(b.toString().equals("Alte Weser, Roter Sand"), "Räumliche Abfrage über den Index: bis 5 km um den Roten Sand: " + b);
            }
            c.rollback();
        }
        List<Daten.Turm> tuerme = db.tuerme();
        double maxErr = 0;
        int paare = 0;
        for (Daten.Turm t : tuerme) {
            Map<String, Double> nb = db.nachbarn(t.name());
            List<Daten.Turm> java = TurmWahl.nachbarn(t, tuerme);
            check(nb.size() == java.size(), String.format(Locale.GERMAN, "%s: %d Nachbarn bis 40 km in der Datenbank, %d in Java", t.name(), nb.size(), java.size()));
            for (Daten.Turm o : java) {
                Double km = nb.get(o.name());
                if (km == null) continue;
                maxErr = Math.max(maxErr, Math.abs(km - t.abstandKm(o)) / t.abstandKm(o));
                paare++;
            }
        }
        check(paare > 0 && maxErr < 0.006, String.format(Locale.GERMAN, "Abstände SDO_GEOM (Ellipsoid) gegen Haversine (Kugel): %d Paare, größte Abweichung %.2f %%", paare, maxErr * 100));

        System.out.println("Gelesen gleich den Startdaten");
        check(tuerme.size() >= 9, "Türme: " + tuerme.size());
        boolean gleich = true;
        for (Daten.Turm s : Startdaten.tuerme()) {
            Daten.Turm d = tuerme.stream().filter(x -> x.name().equals(s.name())).findFirst().orElse(null);
            gleich &= d != null && d.kennung().equals(s.kennung()) && d.sektorenText().equals(s.sektorenText()) && Math.abs(d.breite() - s.breite()) < 1e-6
                    && Math.abs(d.laenge() - s.laenge()) < 1e-6 && Math.abs(d.feuerhoeheM() - s.feuerhoeheM()) < 1e-6 && d.standJahr() == s.standJahr();
        }
        check(gleich, "Alle neun Türme mit Lage, Kennung, Feuerhöhe und Sektoren wie in den Startdaten");
        List<Daten.Wetterlage> wl = db.wetterlagen();
        boolean wg = true;
        for (Daten.Wetterlage s : Startdaten.wetterlagen()) {
            Daten.Wetterlage d = wl.stream().filter(x -> x.name().equals(s.name())).findFirst().orElse(null);
            wg &= d != null && Arrays.equals(runde(d.werte()), runde(s.werte()));
        }
        check(wg, "Zehn Wetterlagen mit allen 14 Größen (die Wolkenart als Name hin und zurück)");
        boolean rg = true;
        for (Daten.Regiebuch s : Startdaten.regiebuecher()) rg &= db.regiebuecher().stream().anyMatch(x -> x.name().equals(s.name()) && x.text().equals(s.text()));
        check(rg, "Drei Regiebücher: Text aus der Datenbank gleich Scenario.toText");
        boolean fg = true;
        for (Daten.Fahrt s : Startdaten.fahrten()) fg &= db.fahrten().stream().anyMatch(x -> x.name().equals(s.name()) && x.punkte().equals(s.punkte()));
        check(fg, "Zwei Kamerafahrten mit allen Stützpunkten");
        boolean pg = true;
        for (Daten.PaketPreset s : Startdaten.presets())
            pg &= db.presets(s.paket(), s.klasse()).stream().anyMatch(x -> x.name().equals(s.name()) && x.felder().equals(s.felder()));
        check(pg, "40 Presets mit allen Feldern");

        System.out.println("Speichern und Laden");
        aufraeumen(db);
        double[] w = Weather.target(Weather.Phase.GEWITTER);
        w[Weather.Var.FOG.ordinal()] = Double.NaN;
        db.speichern(new Daten.Wetterlage("~Prüfung Gewitter", 7.0, "GEWITTER", w, "DbPruefung", Daten.EIGEN));
        Daten.Wetterlage x = db.wetterlagen().stream().filter(v -> v.name().equals("~Prüfung Gewitter")).findFirst().orElse(null);
        check(x != null && Arrays.equals(runde(x.werte()), runde(w)) && "GEWITTER".equals(x.phase()), "Wetterlage: gespeichert, gelesen, leere Größe bleibt leer");
        db.speichern(new Daten.Wetterlage("~Prüfung Gewitter", 8.0, "GEWITTER", w, "ersetzt", Daten.EIGEN));
        check(db.wetterlagen().stream().filter(v -> v.name().equals("~Prüfung Gewitter")).count() == 1, "Gleicher Name ersetzt den eigenen Eintrag");
        boolean abgelehnt = false;
        try { db.speichern(new Daten.Wetterlage("Sturm", 9.0, "STURM", w, "", Daten.EIGEN)); } catch (Speicher.Fehler ex) { abgelehnt = true; }
        check(abgelehnt, "Mitgelieferte Wetterlage wird nicht überschrieben");
        Scenario sc = new Scenario("~Prüfung Regie", 7.5, 300, Scenario.stormDay().steps());
        db.speichern(new Daten.Regiebuch(sc.name(), sc.toText(), "DbPruefung", Daten.EIGEN));
        check(db.regiebuecher().stream().anyMatch(v -> v.name().equals(sc.name()) && v.text().equals(sc.toText())), "Regiebuch mit neun Schritten: Text gleich");
        Daten.Fahrt f0 = Startdaten.fahrten().get(1);
        Daten.Fahrt fa = new Daten.Fahrt("~Prüfung Fahrt", "FREI", false, "", Daten.EIGEN, f0.punkte());
        db.speichern(fa);
        check(db.fahrten().stream().anyMatch(v -> v.name().equals(fa.name()) && v.punkte().equals(fa.punkte())), "Kamerafahrt mit " + fa.punkte().size() + " Stützpunkten gleich");
        String zs = Felder.alsText(Felder.lesen(Ocean.gale()), "Prüfung") + "lang=" + "x".repeat(40000) + "\n";
        byte[] png = new byte[5000];
        for (int i = 0; i < png.length; i++) png[i] = (byte) (i * 7);
        long sid = db.sitzungBeginnen("~Prüfung", "Prüfung", "HIGH");
        long aid = db.speichern(new Daten.Ansicht(0, "~Prüfung Ansicht", 1_792_180_800_000L, new double[]{1, 2, 3, 4, 5, 55}, "Kiel", zs, png), sid);
        Daten.Ansicht an = db.ansicht(aid);
        check(an.zustand().equals(zs) && Arrays.equals(an.vorschau(), png) && "Kiel".equals(an.turm()) && an.szenenzeitMs() == 1_792_180_800_000L,
                "Ansicht: Zustand (CLOB, 40 kB), Vorschau (BLOB), Turm und Szenenzeit gleich");
        Map<String, String> pf = Felder.lesen(Ocean.storm());
        db.speichern(new Daten.PaketPreset("com.dan.ocean", "Ocean", "~Prüfung", "", Daten.EIGEN, pf));
        check(db.presets("com.dan.ocean", "Ocean").stream().anyMatch(v -> v.name().equals("~Prüfung") && v.felder().equals(pf)), "Preset mit " + pf.size() + " Feldern gleich");
        db.sitzungSichern(sid, zs, 1_792_180_800_000L, "Kiel", "Ein Sturmtag", 2.5);
        long sid2 = db.sitzungBeginnen("~Prüfung", "Prüfung", "HIGH");
        Daten.Sitzung ls = db.letzteSitzung(sid2);
        check(ls != null && ls.id() == sid && !ls.sauber() && ls.zustand().equals(zs) && "Kiel".equals(ls.turm()), "Abgebrochene Sitzung: „Weiter wie zuletzt“ findet ihren Zustand");
        db.sitzungBeenden(sid);
        check(db.letzteSitzung(sid2).sauber(), "Beendete Sitzung ist als sauber vermerkt");
        db.protokoll(List.of(new Daten.LogZeile(System.currentTimeMillis(), "WARNUNG", "DbPruefung", "~Prüfung Protokoll", 1_792_180_800_000L, "Galerie", "Stapel")), sid);
        check(db.letzteZeilen(20).stream().anyMatch(z -> z.text().equals("~Prüfung Protokoll") && "Galerie".equals(z.ort())), "Protokoll geschrieben und gelesen");

        System.out.println("Dateien übernehmen");
        java.io.File tmp = java.nio.file.Files.createTempDirectory("lighthouse-uebernahme").toFile();
        DateiSpeicher ds = new DateiSpeicher(tmp);
        ds.speichern(new Daten.Wetterlage("~Prüfung aus Datei", 3.0, "KLAR", Weather.target(Weather.Phase.KLAR), "", Daten.EIGEN));
        String u = com.dan.lighthouse.db.Uebernahme.dateienInDb(ds, db);
        check(db.wetterlagen().stream().anyMatch(v -> v.name().equals("~Prüfung aus Datei")), "Aus daten/ übernommen: " + u);

        System.out.println("Aufräumen");
        aufraeumen(db);
        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(
                "SELECT (SELECT COUNT(*) FROM LH_WEATHER_PRESET WHERE NAME LIKE '~Prüfung%') + (SELECT COUNT(*) FROM LH_SCENARIO WHERE NAME LIKE '~Prüfung%') "
                        + "+ (SELECT COUNT(*) FROM LH_SESSION WHERE APP_VERSION = '~Prüfung') + (SELECT COUNT(*) FROM LH_SNAPSHOT WHERE NAME LIKE '~Prüfung%') FROM DUAL")) {
            r.next();
            check(r.getInt(1) == 0, "Alle Prüfzeilen wieder entfernt");
        }
        c.rollback();
        db.schliessen();
        System.out.printf(Locale.GERMAN, "%n%d Prüfungen, %d Fehler, %.1f s%n", ok + bad, bad, (System.nanoTime() - t0) / 1e9);
        System.out.println(bad == 0 ? "Alles in Ordnung." : "Nicht in Ordnung: " + bad + " Fehler.");
        System.exit(bad == 0 ? 0 : 1);
    }

    static double[] runde(double[] v) {
        double[] r = new double[v.length];
        for (int i = 0; i < v.length; i++) r[i] = Double.isNaN(v[i]) ? Double.NaN : Math.round(v[i] * 100) / 100.0;
        return r;
    }

    /** Entfernt alles, was diese Prüfung anlegt (Namen „~Prüfung …“, Sitzungen mit APP_VERSION ~Prüfung). */
    static void aufraeumen(DbSpeicher db) throws Exception {
        Connection c = db.verbindung();
        try (Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM LH_WEATHER_PRESET WHERE NAME LIKE '~Prüfung%' AND HERKUNFT = 'EIGEN'");
            st.executeUpdate("DELETE FROM LH_SCENARIO WHERE NAME LIKE '~Prüfung%' AND HERKUNFT = 'EIGEN'");
            st.executeUpdate("DELETE FROM LH_CAMERA_PATH WHERE NAME LIKE '~Prüfung%' AND HERKUNFT = 'EIGEN'");
            st.executeUpdate("DELETE FROM LH_PACKAGE_PRESET WHERE NAME LIKE '~Prüfung%' AND HERKUNFT = 'EIGEN'");
            st.executeUpdate("DELETE FROM LH_SNAPSHOT WHERE NAME LIKE '~Prüfung%'");
            st.executeUpdate("DELETE FROM LH_SESSION WHERE APP_VERSION = '~Prüfung'");
            st.executeUpdate("DELETE FROM LH_LOG WHERE QUELLE = 'DbPruefung'");
        }
        c.commit();
    }

    private DbPruefung() { }
}
