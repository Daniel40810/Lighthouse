package com.dan.lighthouse.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Der Einrichter (F34 A): führt die Skripte aus {@code db/sql} der Reihe nach aus, trägt fehlende Spalten nach und
 * prüft danach jede Tabelle, Spalte, Sequence, jeden Trigger, Index, die Sichten, die Spatial-Metadaten und die
 * Startdaten. Er löscht nie etwas. Am Ende meldet er „alles in Ordnung“ oder genau, was fehlt.
 * <p>
 * Was schon da ist, meldet Oracle beim zweiten Lauf mit einer bekannten Nummer (ORA-00955 Name vergeben, ORA-01430
 * Spalte vorhanden, ORA-02260/02261/02275 Schlüssel vorhanden, ORA-01408 Index vorhanden); diese Meldungen sind kein
 * Fehler. Jede andere Meldung steht mit Skript und Zeile im Ergebnis.
 */
public final class Einrichter {
    private Einrichter() { }

    /** Die Skripte in der Reihenfolge, in der sie laufen. {@code 99_entfernen.sql} gehört nie dazu. */
    public static final String[] SKRIPTE = {"01_tabellen.sql", "02_spatial.sql", "03_sichten.sql", "04_startdaten.sql"};
    /** Die Ausbaustufe, die diese App erwartet (LH_SCHEMA.VERSION). */
    public static final int VERSION = 1;
    /** Oracle-Nummern, die beim Wiederholen kommen und nichts bedeuten. */
    static final Set<Integer> HARMLOS = Set.of(955, 1430, 2260, 2261, 2264, 2275, 1408, 29879);
    static final String[] SICHTEN = {"LH_LIGHTHOUSE_V", "LH_NACHBARN_V", "LH_LETZTE_SITZUNG_V"};

    /** Fortschritt für die Oberfläche: Text und Anteil 0..1. */
    public interface Fortschritt { void melde(String text, double anteil); }

    /** Ergebnis: ok, Zeilen zum Anzeigen (je Befund eine), Zähler. */
    public record Ergebnis(boolean ok, List<String> zeilen, int ausgefuehrt, int vorhanden, int fehler) {
        public String kurz() {
            return ok ? "alles in Ordnung" : fehler + (fehler == 1 ? " Befund" : " Befunde") + ": " + zeilen.stream().filter(z -> z.startsWith("FEHLT") || z.startsWith("FEHLER")).findFirst().orElse("");
        }
    }

    /** Der Ordner der Skripte: db/sql. */
    public static File ordner() { return new File(Db.ordner(), "sql"); }

    // ------------------------------------------------------------------ einrichten

    public static Ergebnis einrichten(Connection c, File dir, Fortschritt f) throws IOException, SQLException {
        if (f == null) f = (t, a) -> { };
        List<String> zeilen = new ArrayList<>();
        int run = 0, da = 0, err = 0;
        List<List<Skript.Anweisung>> alle = new ArrayList<>();
        int gesamt = 0;
        for (String s : SKRIPTE) {
            List<Skript.Anweisung> as = Skript.zerlegen(Files.readString(new File(dir, s).toPath(), StandardCharsets.UTF_8));
            alle.add(as);
            gesamt += as.size();
        }
        boolean auto = c.getAutoCommit();
        c.setAutoCommit(true);
        int k = 0;
        try (Statement st = c.createStatement()) {
            for (int i = 0; i < SKRIPTE.length; i++) {
                for (Skript.Anweisung a : alle.get(i)) {
                    k++;
                    if (k % 10 == 1 || a.plsql()) f.melde(SKRIPTE[i] + " · " + a.kurz(), k / (double) gesamt);
                    if (a.sql().equalsIgnoreCase("COMMIT")) continue;
                    try {
                        st.execute(a.sql());
                        run++;
                    } catch (SQLException e) {
                        if (HARMLOS.contains(e.getErrorCode())) { da++; continue; }
                        err++;
                        zeilen.add("FEHLER " + SKRIPTE[i] + " Zeile " + a.zeile() + ": " + erste(e) + " · " + a.kurz());
                    }
                }
                if (i == 0) {
                    int n = spaltenNachtragen(c, alle.get(0), zeilen);
                    if (n > 0) zeilen.add("ok " + n + (n == 1 ? " Spalte" : " Spalten") + " nachgetragen");
                    kompilieren(c, zeilen);
                }
            }
            kompilieren(c, zeilen);
        } finally {
            c.setAutoCommit(auto);
        }
        f.melde("Prüfen …", 1);
        Ergebnis p = pruefen(c, dir);
        List<String> z = new ArrayList<>();
        z.add(String.format(Locale.GERMAN, "ok %d Anweisungen ausgeführt, %d schon vorhanden", run, da));
        z.addAll(zeilen);
        z.addAll(p.zeilen());
        return new Ergebnis(err == 0 && p.ok(), z, run, da, err + p.fehler());
    }

    static String erste(SQLException e) {
        String m = String.valueOf(e.getMessage());
        return m.split("\\R")[0].strip();
    }

    /** Fehlende Spalten aus 01_tabellen.sql als ALTER TABLE … ADD nachtragen (ohne NOT NULL, die Tabelle kann Zeilen haben). */
    static int spaltenNachtragen(Connection c, List<Skript.Anweisung> as, List<String> zeilen) throws SQLException {
        Map<String, Set<String>> da = vorhandeneSpalten(c);
        int n = 0;
        try (Statement st = c.createStatement()) {
            for (Skript.Spalte s : Skript.spalten(as)) {
                Set<String> cols = da.get(s.tabelle());
                if (cols == null || cols.contains(s.name())) continue;
                String def = s.definition().replaceAll("(?i)\\s+NOT\\s+NULL", "");
                try {
                    st.execute("ALTER TABLE " + s.tabelle() + " ADD (" + s.name() + " " + def + ")");
                    n++;
                } catch (SQLException e) {
                    if (!HARMLOS.contains(e.getErrorCode())) zeilen.add("FEHLER Spalte " + s.tabelle() + "." + s.name() + " nicht nachgetragen: " + erste(e));
                }
            }
        }
        return n;
    }

    static Map<String, Set<String>> vorhandeneSpalten(Connection c) throws SQLException {
        Map<String, Set<String>> m = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT TABLE_NAME, COLUMN_NAME FROM USER_TAB_COLUMNS WHERE TABLE_NAME LIKE 'LH\\_%' ESCAPE '\\'")) {
            while (r.next()) m.computeIfAbsent(r.getString(1), x -> new HashSet<>()).add(r.getString(2));
        }
        return m;
    }

    /** Ungültige Trigger und Sichten neu übersetzen (etwa nach einer nachgetragenen Spalte). */
    static void kompilieren(Connection c, List<String> zeilen) throws SQLException {
        List<String[]> l = new ArrayList<>();
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT OBJECT_TYPE, OBJECT_NAME FROM USER_OBJECTS WHERE OBJECT_NAME LIKE 'LH\\_%' ESCAPE '\\' "
                     + "AND STATUS = 'INVALID' AND OBJECT_TYPE IN ('TRIGGER', 'VIEW')")) {
            while (r.next()) l.add(new String[]{r.getString(1), r.getString(2)});
        }
        try (Statement st = c.createStatement()) {
            for (String[] o : l) {
                try { st.execute("ALTER " + o[0] + " " + o[1] + " COMPILE"); } catch (SQLException e) { /* die Prüfung meldet es */ }
            }
        }
    }

    // ------------------------------------------------------------------ prüfen

    /** Prüft alles, was die Skripte anlegen; schreibt nichts. */
    public static Ergebnis pruefen(Connection c, File dir) throws IOException, SQLException {
        List<String> z = new ArrayList<>();
        int fehler = 0;
        List<Skript.Anweisung> t01 = Skript.zerlegen(Files.readString(new File(dir, SKRIPTE[0]).toPath(), StandardCharsets.UTF_8));
        Set<String> tabellen = new LinkedHashSet<>(), sequenzen = new LinkedHashSet<>(), trigger = new LinkedHashSet<>(), indizes = new LinkedHashSet<>();
        for (Skript.Anweisung a : t01) {
            String o = Skript.objekt(a.sql());
            if (o == null) continue;
            String[] p = o.split(" ");
            switch (p[0]) {
                case "TABLE" -> tabellen.add(p[1]);
                case "SEQUENCE" -> sequenzen.add(p[1]);
                case "TRIGGER" -> trigger.add(p[1]);
                case "INDEX" -> indizes.add(p[1]);
                default -> { }
            }
        }
        indizes.add("LH_LIGHTHOUSE_SX");
        Set<String> daT = namen(c, "SELECT TABLE_NAME FROM USER_TABLES"), daS = namen(c, "SELECT SEQUENCE_NAME FROM USER_SEQUENCES"),
                daI = namen(c, "SELECT INDEX_NAME FROM USER_INDEXES");
        int fT = 0;
        for (String t : tabellen) if (!daT.contains(t)) { z.add("FEHLT Tabelle " + t); fT++; }
        fehler += fT;
        if (fT == 0) z.add("ok " + tabellen.size() + " Tabellen");
        // Spalten mit Typ
        Map<String, String> typ = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE FROM USER_TAB_COLUMNS WHERE TABLE_NAME LIKE 'LH\\_%' ESCAPE '\\'")) {
            while (r.next()) typ.put(r.getString(1) + "." + r.getString(2), r.getString(3));
        }
        int fS = 0, nS = 0;
        for (Skript.Spalte s : Skript.spalten(t01)) {
            nS++;
            String ist = typ.get(s.tabelle() + "." + s.name());
            String soll = basis(s.typ());
            if (ist == null) { if (daT.contains(s.tabelle())) { z.add("FEHLT Spalte " + s.tabelle() + "." + s.name()); fS++; } }
            else if (!basis(ist).equals(soll)) { z.add("FEHLT Spalte " + s.tabelle() + "." + s.name() + " hat " + ist + " statt " + soll); fS++; }
        }
        fehler += fS;
        if (fS == 0 && fT == 0) z.add("ok " + nS + " Spalten mit passendem Typ");
        int f2 = 0;
        for (String s : sequenzen) if (!daS.contains(s)) { z.add("FEHLT Sequence " + s); f2++; }
        if (f2 == 0) z.add("ok " + sequenzen.size() + " Sequenzen");
        fehler += f2;
        // Trigger: da, eingeschaltet, gültig
        Map<String, String> trg = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT t.TRIGGER_NAME, t.STATUS, o.STATUS FROM USER_TRIGGERS t JOIN USER_OBJECTS o "
                     + "ON o.OBJECT_NAME = t.TRIGGER_NAME AND o.OBJECT_TYPE = 'TRIGGER'")) {
            while (r.next()) trg.put(r.getString(1), r.getString(2) + "/" + r.getString(3));
        }
        int f3 = 0;
        for (String t : trigger) {
            String s = trg.get(t);
            if (s == null) { z.add("FEHLT Trigger " + t); f3++; }
            else if (!s.equals("ENABLED/VALID")) { z.add("FEHLT Trigger " + t + " ist " + s); f3++; }
        }
        if (f3 == 0) z.add("ok " + trigger.size() + " Trigger eingeschaltet und gültig");
        fehler += f3;
        int f4 = 0;
        for (String i : indizes) if (!daI.contains(i)) { z.add("FEHLT Index " + i + (i.endsWith("_SX") ? " (räumlich; Rechte siehe 02_spatial.sql)" : "")); f4++; }
        fehler += f4;
        if (f4 == 0) {
            String sx = einWert(c, "SELECT DOMIDX_STATUS || '/' || DOMIDX_OPSTATUS FROM USER_INDEXES WHERE INDEX_NAME = 'LH_LIGHTHOUSE_SX'");
            if (!"VALID/VALID".equals(sx)) { z.add("FEHLT räumlicher Index LH_LIGHTHOUSE_SX ist " + sx); fehler++; }
            else z.add("ok " + indizes.size() + " Indizes, der räumliche gültig");
        }
        String srid = einWert(c, "SELECT TO_CHAR(SRID) FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'LH_LIGHTHOUSE' AND COLUMN_NAME = 'LAGE'");
        if (!"8307".equals(srid)) { z.add("FEHLT Spatial-Metadaten für LH_LIGHTHOUSE.LAGE (SRID 8307), gefunden: " + srid); fehler++; }
        else z.add("ok Spatial-Metadaten mit SRID 8307");
        Map<String, String> views = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT OBJECT_NAME, STATUS FROM USER_OBJECTS WHERE OBJECT_TYPE = 'VIEW'")) {
            while (r.next()) views.put(r.getString(1), r.getString(2));
        }
        int f5 = 0;
        for (String v : SICHTEN) {
            if (!"VALID".equals(views.get(v))) { z.add("FEHLT Sicht " + v + (views.containsKey(v) ? " ist " + views.get(v) : "")); f5++; }
        }
        fehler += f5;
        if (f5 == 0) z.add("ok " + SICHTEN.length + " Sichten gültig");
        if (fT > 0) return new Ergebnis(false, z, 0, 0, fehler);
        // Stand und Startdaten
        String v = einWert(c, "SELECT TO_CHAR(MAX(VERSION)) FROM LH_SCHEMA");
        if (v == null || Integer.parseInt(v) < VERSION) { z.add("FEHLT LH_SCHEMA: Stufe " + v + ", erwartet " + VERSION); fehler++; }
        else z.add("ok Ausbaustufe " + v);
        fehler += zaehle(c, z, "Türme", "SELECT COUNT(*) FROM LH_LIGHTHOUSE WHERE HERKUNFT = 'MITGELIEFERT'", Startdaten.tuerme().size());
        int sek = 0;
        for (Daten.Turm t : Startdaten.tuerme()) sek += t.sektoren().size();
        fehler += zaehle(c, z, "Sektoren", "SELECT COUNT(*) FROM LH_SECTOR s JOIN LH_LIGHTHOUSE t ON t.ID = s.LIGHTHOUSE_ID WHERE t.HERKUNFT = 'MITGELIEFERT'", sek);
        fehler += zaehle(c, z, "Wetterlagen", "SELECT COUNT(*) FROM LH_WEATHER_PRESET WHERE HERKUNFT = 'MITGELIEFERT'", Startdaten.wetterlagen().size());
        fehler += zaehle(c, z, "Regiebücher", "SELECT COUNT(*) FROM LH_SCENARIO WHERE HERKUNFT = 'MITGELIEFERT'", Startdaten.regiebuecher().size());
        fehler += zaehle(c, z, "Kamerafahrten", "SELECT COUNT(*) FROM LH_CAMERA_PATH WHERE HERKUNFT = 'MITGELIEFERT'", Startdaten.fahrten().size());
        fehler += zaehle(c, z, "Presets der Packages", "SELECT COUNT(*) FROM LH_PACKAGE_PRESET WHERE HERKUNFT = 'MITGELIEFERT'", Startdaten.presets().size());
        int felder = 0;
        for (Daten.PaketPreset p : Startdaten.presets()) felder += p.felder().size();
        fehler += zaehle(c, z, "Felder der Presets", "SELECT COUNT(*) FROM LH_PACKAGE_SETTING s JOIN LH_PACKAGE_PRESET p ON p.ID = s.PACKAGE_PRESET_ID WHERE p.HERKUNFT = 'MITGELIEFERT'", felder);
        // die räumliche Sicht rechnet
        try {
            String n = einWert(c, "SELECT TO_CHAR(COUNT(*)) FROM LH_NACHBARN_V");
            z.add("ok Nachbarn im Umkreis von 40 km: " + n + " Paare (räumlich gerechnet)");
        } catch (SQLException e) {
            z.add("FEHLER LH_NACHBARN_V rechnet nicht: " + erste(e));
            fehler++;
        }
        if (fehler == 0) z.add("alles in Ordnung");
        return new Ergebnis(fehler == 0, z, 0, 0, fehler);
    }

    private static int zaehle(Connection c, List<String> z, String was, String sql, int soll) throws SQLException {
        String ist = einWert(c, sql);
        int n = ist == null ? 0 : Integer.parseInt(ist);
        if (n != soll) { z.add("FEHLT " + was + ": " + n + " statt " + soll + " mitgeliefert"); return 1; }
        z.add("ok " + n + " " + was);
        return 0;
    }

    static String basis(String t) {
        String s = t.toUpperCase(Locale.ROOT);
        int i = s.indexOf('(');
        if (i > 0) s = s.substring(0, i);
        if (s.startsWith("MDSYS.")) s = s.substring(6);
        return s;
    }

    static Set<String> namen(Connection c, String sql) throws SQLException {
        Set<String> s = new HashSet<>();
        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(sql)) {
            while (r.next()) s.add(r.getString(1));
        }
        return s;
    }

    static String einWert(Connection c, String sql) throws SQLException {
        try (PreparedStatement st = c.prepareStatement(sql); ResultSet r = st.executeQuery()) {
            return r.next() ? r.getString(1) : null;
        }
    }

    /** Für das Startbild: Stufe des Schemas (0 = keine LH_-Tabellen, -1 = LH_SCHEMA fehlt, sonst VERSION). */
    public static int stufe(Connection c) throws SQLException {
        String n = einWert(c, "SELECT TO_CHAR(COUNT(*)) FROM USER_TABLES WHERE TABLE_NAME LIKE 'LH\\_%' ESCAPE '\\'");
        if (n == null || Integer.parseInt(n) == 0) return 0;
        try {
            String v = einWert(c, "SELECT TO_CHAR(MAX(VERSION)) FROM LH_SCHEMA");
            return v == null ? -1 : Integer.parseInt(v);
        } catch (SQLException e) {
            return -1;
        }
    }
}
