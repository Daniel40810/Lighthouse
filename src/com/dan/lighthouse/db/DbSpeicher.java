package com.dan.lighthouse.db;

import com.dan.weather.Scenario;
import com.dan.weather.Weather;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Speichern in der Datenbank (Schema LIGHTHOUSE, Tabellen aus Abschnitt 8). Eine Verbindung für alles; jede
 * Methode ist eine Transaktion (bei einem Fehler wird zurückgerollt). Bricht die Verbindung ab, wird einmal neu
 * verbunden. Die Lage der Türme kommt über die Sicht {@code LH_LIGHTHOUSE_V} als zwei Zahlen (F35 A).
 */
public final class DbSpeicher implements Speicher {
    private final Db.Konfig konfig;
    private Connection c;
    private final String wo;

    public DbSpeicher(Db.Konfig k) throws Fehler {
        konfig = k;
        try {
            c = oeffnen();
            String w;
            try { w = Db.pruefen(k); } catch (SQLException e) { w = k.kurz(); }
            wo = "Datenbank " + w.replaceAll(" · Oracle.*", "");
        } catch (SQLException e) {
            throw new Fehler("Keine Verbindung: " + Einrichter.erste(e), e);
        }
    }

    /** Für Prüfungen: eine schon offene Verbindung (wird beim Schließen mit geschlossen). */
    public DbSpeicher(Connection c, String wo) {
        konfig = null;
        this.c = c;
        this.wo = wo;
    }

    private Connection oeffnen() throws SQLException {
        Connection x = Db.oeffnen(konfig);
        x.setAutoCommit(false);
        return x;
    }

    /** Die Verbindung, bei Bedarf neu geöffnet. */
    public synchronized Connection verbindung() throws Fehler {
        try {
            if (c == null || (konfig != null && !c.isValid(3))) {
                if (c != null) try { c.close(); } catch (SQLException e) { /* weg */ }
                c = oeffnen();
            }
            return c;
        } catch (SQLException e) {
            throw new Fehler("Keine Verbindung zur Datenbank: " + Einrichter.erste(e), e);
        }
    }

    @Override public String wo() { return wo; }
    @Override public boolean datenbank() { return true; }

    /** Eine Arbeit in einer Transaktion. */
    private interface Arbeit<T> { T tun(Connection c) throws SQLException, Fehler; }

    private synchronized <T> T tx(String was, Arbeit<T> a) throws Fehler {
        Connection x = verbindung();
        try {
            T r = a.tun(x);
            x.commit();
            return r;
        } catch (SQLException e) {
            try { x.rollback(); } catch (SQLException e2) { /* weg */ }
            String m = Einrichter.erste(e);
            if (e.getErrorCode() == 942) m += " · Tabellen fehlen: bitte im Block DATEN „Einrichten“";
            if (e.getErrorCode() == 1) m = "Den Namen gibt es schon";
            throw new Fehler(was + ": " + m, e);
        } catch (Fehler | RuntimeException e) {
            try { x.rollback(); } catch (SQLException e2) { /* weg */ }
            throw e;
        }
    }

    private static Long id(Connection c, String sql, Object... p) throws SQLException {
        try (PreparedStatement st = c.prepareStatement(sql)) {
            bind(st, p);
            try (ResultSet r = st.executeQuery()) { return r.next() ? r.getLong(1) : null; }
        }
    }

    private static int upd(Connection c, String sql, Object... p) throws SQLException {
        try (PreparedStatement st = c.prepareStatement(sql)) {
            bind(st, p);
            return st.executeUpdate();
        }
    }

    private static long naechste(Connection c, String seq) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT " + seq + ".NEXTVAL FROM DUAL")) {
            r.next();
            return r.getLong(1);
        }
    }

    private static void bind(PreparedStatement st, Object... p) throws SQLException {
        for (int i = 0; i < p.length; i++) {
            Object o = p[i];
            if (o == null) st.setNull(i + 1, Types.VARCHAR);
            else if (o instanceof Double d) { if (Double.isNaN(d)) st.setNull(i + 1, Types.NUMERIC); else st.setDouble(i + 1, d); }
            else if (o instanceof Integer n) st.setInt(i + 1, n);
            else if (o instanceof Long n) st.setLong(i + 1, n);
            else if (o instanceof Boolean b) st.setInt(i + 1, b ? 1 : 0);
            else if (o instanceof Timestamp t) st.setTimestamp(i + 1, t);
            else if (o instanceof byte[] b) st.setBytes(i + 1, b);
            else if (o instanceof Clob s) st.setString(i + 1, s.text);
            else st.setString(i + 1, o.toString());
        }
    }

    /** Ein langer Text für eine CLOB-Spalte. */
    private record Clob(String text) { }

    private static Double dbl(ResultSet r, String col) throws SQLException {
        double v = r.getDouble(col);
        return r.wasNull() ? null : v;
    }

    private static Integer integer(ResultSet r, String col) throws SQLException {
        int v = r.getInt(col);
        return r.wasNull() ? null : v;
    }

    private static boolean mitgeliefert(Connection c, String tab, String where, Object... p) throws SQLException {
        try (PreparedStatement st = c.prepareStatement("SELECT HERKUNFT FROM " + tab + " WHERE " + where)) {
            bind(st, p);
            try (ResultSet r = st.executeQuery()) { return r.next() && Daten.MITGELIEFERT.equals(r.getString(1)); }
        }
    }

    private static void eigen(boolean mit, String was) throws Fehler {
        if (mit) throw new Fehler(was + " ist mitgeliefert und bleibt, wie sie ist. Bitte unter einem eigenen Namen speichern.");
    }

    private static int zahl(Connection c, String sql) throws SQLException {
        Long n = id(c, sql);
        return n == null ? 0 : n.intValue();
    }

    // ------------------------------------------------------------------ Stand

    @Override public Daten.Stand stand() throws Fehler {
        Daten.Sitzung s = letzteSitzung(-1);
        return tx("Stand lesen", c -> new Daten.Stand(zahl(c, "SELECT COUNT(*) FROM LH_LIGHTHOUSE"), zahl(c, "SELECT COUNT(*) FROM LH_WEATHER_PRESET"),
                zahl(c, "SELECT COUNT(*) FROM LH_SCENARIO"), zahl(c, "SELECT COUNT(*) FROM LH_CAMERA_PATH"), zahl(c, "SELECT COUNT(*) FROM LH_SNAPSHOT"),
                zahl(c, "SELECT COUNT(*) FROM LH_PACKAGE_PRESET"), s));
    }

    // ------------------------------------------------------------------ Türme

    @Override public List<Daten.Turm> tuerme() throws Fehler {
        return tx("Türme lesen", c -> {
            Map<Long, List<Daten.Sektor>> sek = new HashMap<>();
            try (Statement st = c.createStatement();
                 ResultSet r = st.executeQuery("SELECT LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG FROM LH_SECTOR ORDER BY LIGHTHOUSE_ID, NR")) {
                while (r.next()) sek.computeIfAbsent(r.getLong(1), x -> new ArrayList<>()).add(new Daten.Sektor(r.getInt(2), r.getDouble(3), r.getDouble(4),
                        r.getString(5), r.getString(6), dbl(r, "TRAGWEITE_SM"), r.getString(8)));
            }
            List<Daten.Turm> l = new ArrayList<>();
            try (Statement st = c.createStatement();
                 ResultSet r = st.executeQuery("SELECT ID, NAME, REVIER, BREITE, LAENGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, "
                         + "AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT FROM LH_LIGHTHOUSE_V ORDER BY ID")) {
                while (r.next()) l.add(new Daten.Turm(r.getString("NAME"), r.getString("REVIER"), r.getDouble("BREITE"), r.getDouble("LAENGE"),
                        r.getDouble("FEUERHOEHE_M"), dbl(r, "TURMHOEHE_M"), dbl(r, "TRAGWEITE_SM"), r.getString("KENNUNG"), r.getString("OPTIK"),
                        integer(r, "BAUJAHR"), integer(r, "AUSSER_BETRIEB"), r.getInt("STAND_JAHR"), r.getString("FARBEN"), r.getString("BESCHREIBUNG"),
                        r.getString("QUELLE"), r.getString("HERKUNFT"), sek.getOrDefault(r.getLong("ID"), List.of())));
            }
            return l;
        });
    }

    /** Die Nachbarn eines Turms bis 40 km aus der räumlichen Sicht: Name → km. */
    public Map<String, Double> nachbarn(String turm) throws Fehler {
        return tx("Nachbarn lesen", c -> {
            Map<String, Double> m = new LinkedHashMap<>();
            try (PreparedStatement st = c.prepareStatement("SELECT NACHBAR, KM FROM LH_NACHBARN_V WHERE TURM = ? ORDER BY KM")) {
                st.setString(1, turm);
                try (ResultSet r = st.executeQuery()) { while (r.next()) m.put(r.getString(1), r.getDouble(2)); }
            }
            return m;
        });
    }

    // ------------------------------------------------------------------ Wetterlagen

    private static final String WSP = String.join(", ", Startdaten.SPALTEN);

    private static Object wetterWert(int i, double v) {
        if (Double.isNaN(v)) return Double.NaN;
        if (i == Startdaten.WOLKE) return Weather.CloudKind.values()[(int) Math.max(0, Math.min(4, Math.round(v)))].name();
        return v;
    }

    private static double[] wetterLesen(ResultSet r) throws SQLException {
        double[] w = new double[Startdaten.SPALTEN.length];
        for (int i = 0; i < w.length; i++) {
            if (i == Startdaten.WOLKE) {
                String k = r.getString(Startdaten.SPALTEN[i]);
                w[i] = k == null ? Double.NaN : Weather.CloudKind.valueOf(k).ordinal();
            } else {
                double v = r.getDouble(Startdaten.SPALTEN[i]);
                w[i] = r.wasNull() ? Double.NaN : v;
            }
        }
        return w;
    }

    @Override public List<Daten.Wetterlage> wetterlagen() throws Fehler {
        return tx("Wetterlagen lesen", c -> {
            List<Daten.Wetterlage> l = new ArrayList<>();
            try (Statement st = c.createStatement();
                 ResultSet r = st.executeQuery("SELECT NAME, BEAUFORT, PHASE, " + WSP + ", BESCHREIBUNG, HERKUNFT FROM LH_WEATHER_PRESET ORDER BY ID")) {
                while (r.next()) l.add(new Daten.Wetterlage(r.getString("NAME"), dbl(r, "BEAUFORT"), r.getString("PHASE"), wetterLesen(r),
                        r.getString("BESCHREIBUNG"), r.getString("HERKUNFT")));
            }
            return l;
        });
    }

    @Override public void speichern(Daten.Wetterlage w) throws Fehler {
        tx("Wetterlage speichern", c -> {
            eigen(mitgeliefert(c, "LH_WEATHER_PRESET", "NAME = ?", w.name()), "Die Wetterlage „" + w.name() + "“");
            upd(c, "DELETE FROM LH_WEATHER_PRESET WHERE NAME = ? AND HERKUNFT = 'EIGEN'", w.name());
            Object[] p = new Object[4 + Startdaten.SPALTEN.length + 1];
            p[0] = w.name(); p[1] = w.beaufort(); p[2] = w.phase();
            for (int i = 0; i < Startdaten.SPALTEN.length; i++) p[3 + i] = wetterWert(i, w.werte()[i]);
            p[3 + Startdaten.SPALTEN.length] = w.beschreibung();
            p[4 + Startdaten.SPALTEN.length] = Daten.EIGEN;
            upd(c, "INSERT INTO LH_WEATHER_PRESET (NAME, BEAUFORT, PHASE, " + WSP + ", BESCHREIBUNG, HERKUNFT) VALUES (" + "?, ".repeat(p.length - 1) + "?)", p);
            return null;
        });
    }

    @Override public void loeschenWetterlage(String name) throws Fehler {
        tx("Wetterlage löschen", c -> {
            eigen(mitgeliefert(c, "LH_WEATHER_PRESET", "NAME = ?", name), "Die Wetterlage „" + name + "“");
            if (upd(c, "DELETE FROM LH_WEATHER_PRESET WHERE NAME = ? AND HERKUNFT = 'EIGEN'", name) == 0) throw new Fehler("Die Wetterlage „" + name + "“ gibt es nicht.");
            return null;
        });
    }

    // ------------------------------------------------------------------ Regiebücher

    @Override public List<Daten.Regiebuch> regiebuecher() throws Fehler {
        return tx("Regiebücher lesen", c -> {
            List<Daten.Regiebuch> l = new ArrayList<>();
            Map<Long, List<Scenario.Step>> steps = new HashMap<>();
            try (Statement st = c.createStatement();
                 ResultSet r = st.executeQuery("SELECT SCENARIO_ID, DAUER_H, PHASE, TEXT, KAMERA, " + WSP + " FROM LH_SCENARIO_STEP ORDER BY SCENARIO_ID, NR")) {
                while (r.next()) {
                    String t = r.getString("TEXT"), k = r.getString("KAMERA");
                    steps.computeIfAbsent(r.getLong(1), x -> new ArrayList<>()).add(new Scenario.Step(t == null ? "" : t, Weather.Phase.valueOf(r.getString("PHASE")),
                            r.getDouble("DAUER_H"), wetterLesen(r), k == null ? "" : k));
                }
            }
            try (Statement st = c.createStatement();
                 ResultSet r = st.executeQuery("SELECT ID, NAME, BEGINN_STUNDE, ZEITRAFFER, BESCHREIBUNG, HERKUNFT FROM LH_SCENARIO ORDER BY ID")) {
                while (r.next()) {
                    Scenario s = new Scenario(r.getString("NAME"), r.getDouble("BEGINN_STUNDE"), r.getDouble("ZEITRAFFER"), steps.getOrDefault(r.getLong("ID"), List.of()));
                    l.add(new Daten.Regiebuch(s.name(), s.toText(), r.getString("BESCHREIBUNG"), r.getString("HERKUNFT")));
                }
            }
            return l;
        });
    }

    @Override public void speichern(Daten.Regiebuch rb) throws Fehler {
        Scenario s;
        try { s = Scenario.parse(rb.text()); } catch (RuntimeException e) { throw new Fehler("Das Regiebuch lässt sich nicht lesen: " + e.getMessage(), e); }
        tx("Regiebuch speichern", c -> {
            eigen(mitgeliefert(c, "LH_SCENARIO", "NAME = ?", rb.name()), "Das Regiebuch „" + rb.name() + "“");
            upd(c, "DELETE FROM LH_SCENARIO WHERE NAME = ? AND HERKUNFT = 'EIGEN'", rb.name());
            long id = naechste(c, "LH_SCENARIO_SEQ");
            upd(c, "INSERT INTO LH_SCENARIO (ID, NAME, BEGINN_STUNDE, ZEITRAFFER, BESCHREIBUNG, HERKUNFT) VALUES (?, ?, ?, ?, ?, 'EIGEN')",
                    id, rb.name(), s.startHour(), s.lapse(), rb.beschreibung());
            int nr = 0;
            try (PreparedStatement st = c.prepareStatement("INSERT INTO LH_SCENARIO_STEP (SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA, " + WSP + ") VALUES ("
                    + "?, ".repeat(5 + Startdaten.SPALTEN.length) + "?)")) {
                for (Scenario.Step x : s.steps()) {
                    Object[] p = new Object[6 + Startdaten.SPALTEN.length];
                    p[0] = id; p[1] = ++nr; p[2] = x.hours(); p[3] = x.phase().name(); p[4] = x.text(); p[5] = x.camera();
                    for (int i = 0; i < Startdaten.SPALTEN.length; i++) p[6 + i] = wetterWert(i, x.values()[i]);
                    bind(st, p);
                    st.addBatch();
                }
                st.executeBatch();
            }
            return null;
        });
    }

    @Override public void loeschenRegiebuch(String name) throws Fehler {
        tx("Regiebuch löschen", c -> {
            eigen(mitgeliefert(c, "LH_SCENARIO", "NAME = ?", name), "Das Regiebuch „" + name + "“");
            if (upd(c, "DELETE FROM LH_SCENARIO WHERE NAME = ? AND HERKUNFT = 'EIGEN'", name) == 0) throw new Fehler("Das Regiebuch „" + name + "“ gibt es nicht.");
            return null;
        });
    }

    // ------------------------------------------------------------------ Fahrten

    @Override public List<Daten.Fahrt> fahrten() throws Fehler {
        return tx("Kamerafahrten lesen", c -> {
            Map<Long, List<Daten.Stuetzpunkt>> pts = new HashMap<>();
            try (Statement st = c.createStatement();
                 ResultSet r = st.executeQuery("SELECT CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD FROM LH_CAMERA_POINT ORDER BY CAMERA_PATH_ID, NR")) {
                while (r.next()) pts.computeIfAbsent(r.getLong(1), x -> new ArrayList<>()).add(new Daten.Stuetzpunkt(r.getInt(2), r.getDouble(3), r.getDouble(4),
                        r.getDouble(5), r.getDouble(6), r.getDouble(7), r.getDouble(8), r.getDouble(9)));
            }
            List<Daten.Fahrt> l = new ArrayList<>();
            try (Statement st = c.createStatement();
                 ResultSet r = st.executeQuery("SELECT ID, NAME, ART, SCHLEIFE, BESCHREIBUNG, HERKUNFT FROM LH_CAMERA_PATH ORDER BY ID")) {
                while (r.next()) l.add(new Daten.Fahrt(r.getString("NAME"), r.getString("ART"), r.getInt("SCHLEIFE") == 1, r.getString("BESCHREIBUNG"),
                        r.getString("HERKUNFT"), pts.getOrDefault(r.getLong("ID"), List.of())));
            }
            return l;
        });
    }

    @Override public void speichern(Daten.Fahrt f) throws Fehler {
        tx("Kamerafahrt speichern", c -> {
            eigen(mitgeliefert(c, "LH_CAMERA_PATH", "NAME = ?", f.name()), "Die Kamerafahrt „" + f.name() + "“");
            upd(c, "DELETE FROM LH_CAMERA_PATH WHERE NAME = ? AND HERKUNFT = 'EIGEN'", f.name());
            long id = naechste(c, "LH_CAMERA_PATH_SEQ");
            upd(c, "INSERT INTO LH_CAMERA_PATH (ID, NAME, ART, SCHLEIFE, BESCHREIBUNG, HERKUNFT) VALUES (?, ?, ?, ?, ?, 'EIGEN')", id, f.name(), f.art(), f.schleife(), f.beschreibung());
            try (PreparedStatement st = c.prepareStatement("INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int nr = 0;
                for (Daten.Stuetzpunkt p : f.punkte()) {
                    bind(st, id, ++nr, p.zeitS(), p.x(), p.y(), p.z(), p.gier(), p.nick(), p.fov());
                    st.addBatch();
                }
                st.executeBatch();
            }
            return null;
        });
    }

    @Override public void loeschenFahrt(String name) throws Fehler {
        tx("Kamerafahrt löschen", c -> {
            eigen(mitgeliefert(c, "LH_CAMERA_PATH", "NAME = ?", name), "Die Kamerafahrt „" + name + "“");
            if (upd(c, "DELETE FROM LH_CAMERA_PATH WHERE NAME = ? AND HERKUNFT = 'EIGEN'", name) == 0) throw new Fehler("Die Kamerafahrt „" + name + "“ gibt es nicht.");
            return null;
        });
    }

    // ------------------------------------------------------------------ Ansichten

    private static final String ANSICHT = "SELECT s.ID, s.NAME, s.SZENENZEIT, s.KAM_X, s.KAM_Y, s.KAM_Z, s.KAM_GIER_GRAD, s.KAM_NICK_GRAD, s.KAM_FOV_GRAD, l.NAME AS TURM";

    private static Daten.Ansicht ansicht(ResultSet r, String zustand, byte[] png) throws SQLException {
        Timestamp t = r.getTimestamp("SZENENZEIT");
        double[] k = new double[6];
        String[] cols = {"KAM_X", "KAM_Y", "KAM_Z", "KAM_GIER_GRAD", "KAM_NICK_GRAD", "KAM_FOV_GRAD"};
        for (int i = 0; i < 6; i++) { k[i] = r.getDouble(cols[i]); if (r.wasNull()) k[i] = Double.NaN; }
        return new Daten.Ansicht(r.getLong("ID"), r.getString("NAME"), t == null ? 0 : t.getTime(), k, r.getString("TURM"), zustand, png);
    }

    @Override public List<Daten.Ansicht> ansichten() throws Fehler {
        return tx("Ansichten lesen", c -> {
            List<Daten.Ansicht> l = new ArrayList<>();
            try (Statement st = c.createStatement();
                 ResultSet r = st.executeQuery(ANSICHT + " FROM LH_SNAPSHOT s LEFT JOIN LH_LIGHTHOUSE l ON l.ID = s.LIGHTHOUSE_ID ORDER BY s.ID DESC")) {
                while (r.next()) l.add(ansicht(r, null, null));
            }
            return l;
        });
    }

    @Override public Daten.Ansicht ansicht(long id) throws Fehler {
        return tx("Ansicht lesen", c -> {
            try (PreparedStatement st = c.prepareStatement(ANSICHT + ", s.ZUSTAND, s.VORSCHAU FROM LH_SNAPSHOT s LEFT JOIN LH_LIGHTHOUSE l ON l.ID = s.LIGHTHOUSE_ID WHERE s.ID = ?")) {
                st.setLong(1, id);
                try (ResultSet r = st.executeQuery()) {
                    if (!r.next()) throw new Fehler("Die Ansicht " + id + " gibt es nicht.");
                    return ansicht(r, r.getString("ZUSTAND"), r.getBytes("VORSCHAU"));
                }
            }
        });
    }

    @Override public long speichern(Daten.Ansicht a, Long sitzung) throws Fehler {
        return tx("Ansicht speichern", c -> {
            long id = naechste(c, "LH_SNAPSHOT_SEQ");
            Long turm = a.turm() == null ? null : id(c, "SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = ?", a.turm());
            double[] k = a.kamera();
            upd(c, "INSERT INTO LH_SNAPSHOT (ID, NAME, SESSION_ID, LIGHTHOUSE_ID, SZENENZEIT, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD, ZUSTAND, VORSCHAU) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", id, a.name(), sitzung, turm, new Timestamp(a.szenenzeitMs()),
                    k[0], k[1], k[2], k[3], k[4], k[5], new Clob(a.zustand()), a.vorschau());
            return id;
        });
    }

    @Override public void loeschenAnsicht(long id) throws Fehler {
        tx("Ansicht löschen", c -> {
            if (upd(c, "DELETE FROM LH_SNAPSHOT WHERE ID = ?", id) == 0) throw new Fehler("Die Ansicht " + id + " gibt es nicht.");
            return null;
        });
    }

    // ------------------------------------------------------------------ Presets

    @Override public List<Daten.PaketPreset> presets(String paket, String klasse) throws Fehler {
        return tx("Presets lesen", c -> {
            String w = " WHERE (? IS NULL OR p.PAKET = ?) AND (? IS NULL OR p.KLASSE = ?)";
            Map<Long, LinkedHashMap<String, String>> felder = new HashMap<>();
            try (PreparedStatement st = c.prepareStatement("SELECT s.PACKAGE_PRESET_ID, s.FELD, s.WERT FROM LH_PACKAGE_SETTING s JOIN LH_PACKAGE_PRESET p ON p.ID = s.PACKAGE_PRESET_ID"
                    + w + " ORDER BY s.ID")) {
                bind(st, paket, paket, klasse, klasse);
                try (ResultSet r = st.executeQuery()) {
                    while (r.next()) { String v = r.getString(3); felder.computeIfAbsent(r.getLong(1), x -> new LinkedHashMap<>()).put(r.getString(2), v == null ? "" : v); }
                }
            }
            List<Daten.PaketPreset> l = new ArrayList<>();
            try (PreparedStatement st = c.prepareStatement("SELECT p.ID, p.PAKET, p.KLASSE, p.NAME, p.BESCHREIBUNG, p.HERKUNFT FROM LH_PACKAGE_PRESET p" + w + " ORDER BY p.ID")) {
                bind(st, paket, paket, klasse, klasse);
                try (ResultSet r = st.executeQuery()) {
                    while (r.next()) l.add(new Daten.PaketPreset(r.getString(2), r.getString(3), r.getString(4), r.getString(5), r.getString(6),
                            felder.getOrDefault(r.getLong(1), new LinkedHashMap<>())));
                }
            }
            return l;
        });
    }

    @Override public void speichern(Daten.PaketPreset p) throws Fehler {
        Map<String, String> typen = Startdaten.typen(p);
        tx("Preset speichern", c -> {
            eigen(mitgeliefert(c, "LH_PACKAGE_PRESET", "PAKET = ? AND KLASSE = ? AND NAME = ?", p.paket(), p.klasse(), p.name()), "Das Preset „" + p.name() + "“");
            upd(c, "DELETE FROM LH_PACKAGE_PRESET WHERE PAKET = ? AND KLASSE = ? AND NAME = ? AND HERKUNFT = 'EIGEN'", p.paket(), p.klasse(), p.name());
            long id = naechste(c, "LH_PACKAGE_PRESET_SEQ");
            upd(c, "INSERT INTO LH_PACKAGE_PRESET (ID, PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT) VALUES (?, ?, ?, ?, ?, 'EIGEN')", id, p.paket(), p.klasse(), p.name(), p.beschreibung());
            try (PreparedStatement st = c.prepareStatement("INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) VALUES (?, ?, ?, ?)")) {
                for (Map.Entry<String, String> e : p.felder().entrySet()) {
                    bind(st, id, e.getKey(), typen.getOrDefault(e.getKey(), "STRING"), e.getValue());
                    st.addBatch();
                }
                st.executeBatch();
            }
            return null;
        });
    }

    @Override public void loeschenPreset(String paket, String klasse, String name) throws Fehler {
        tx("Preset löschen", c -> {
            eigen(mitgeliefert(c, "LH_PACKAGE_PRESET", "PAKET = ? AND KLASSE = ? AND NAME = ?", paket, klasse, name), "Das Preset „" + name + "“");
            if (upd(c, "DELETE FROM LH_PACKAGE_PRESET WHERE PAKET = ? AND KLASSE = ? AND NAME = ? AND HERKUNFT = 'EIGEN'", paket, klasse, name) == 0)
                throw new Fehler("Das Preset „" + name + "“ gibt es nicht.");
            return null;
        });
    }

    // ------------------------------------------------------------------ Sitzungen

    @Override public long sitzungBeginnen(String version, String gpu, String qualitaet) throws Fehler {
        return tx("Sitzung beginnen", c -> {
            long id = naechste(c, "LH_SESSION_SEQ");
            upd(c, "INSERT INTO LH_SESSION (ID, RECHNER, BENUTZER, APP_VERSION, GPU, QUALITAET) VALUES (?, ?, ?, ?, ?, ?)", id, DateiSpeicher.rechner(),
                    System.getProperty("user.name"), version, cut(gpu, 120), qualitaet);
            // Protokoll älter als 90 Tage aufräumen (kein Job nötig)
            upd(c, "DELETE FROM LH_LOG WHERE ZEIT < SYSTIMESTAMP - INTERVAL '90' DAY");
            return id;
        });
    }

    private static String cut(String s, int n) { return s == null || s.length() <= n ? s : s.substring(0, n); }

    @Override public void sitzungSichern(long id, String zustand, long szenenzeitMs, String turm, String regiebuch, double regieStunde) throws Fehler {
        tx("Sitzung sichern", c -> {
            upd(c, "UPDATE LH_SESSION SET ZUSTAND = ?, ZUSTAND_ZEIT = SYSTIMESTAMP, SZENENZEIT = ?, LIGHTHOUSE_ID = (SELECT ID FROM LH_LIGHTHOUSE WHERE NAME = ?), "
                    + "SCENARIO_ID = (SELECT ID FROM LH_SCENARIO WHERE NAME = ?), SCENARIO_STUNDE = ? WHERE ID = ?", new Clob(zustand), new Timestamp(szenenzeitMs), turm,
                    regiebuch, Double.isNaN(regieStunde) ? null : Math.round(regieStunde * 1000) / 1000.0, id);
            return null;
        });
    }

    @Override public void sitzungBeenden(long id) throws Fehler {
        tx("Sitzung beenden", c -> upd(c, "UPDATE LH_SESSION SET ENDE = SYSTIMESTAMP, SAUBER_BEENDET = 1 WHERE ID = ?", id));
    }

    @Override public Daten.Sitzung letzteSitzung(long ausser) throws Fehler {
        return tx("Letzte Sitzung lesen", c -> {
            try (PreparedStatement st = c.prepareStatement("SELECT s.ID, s.BEGINN, s.ENDE, s.SAUBER_BEENDET, s.ZUSTAND, s.ZUSTAND_ZEIT, l.NAME AS TURM FROM LH_SESSION s "
                    + "LEFT JOIN LH_LIGHTHOUSE l ON l.ID = s.LIGHTHOUSE_ID WHERE s.ZUSTAND IS NOT NULL AND s.ID <> ? ORDER BY s.ID DESC FETCH FIRST 1 ROWS ONLY")) {
                st.setLong(1, ausser);
                try (ResultSet r = st.executeQuery()) {
                    if (!r.next()) return null;
                    Timestamp e = r.getTimestamp("ENDE"), z = r.getTimestamp("ZUSTAND_ZEIT");
                    return new Daten.Sitzung(r.getLong("ID"), r.getTimestamp("BEGINN").getTime(), e == null ? null : e.getTime(), r.getInt("SAUBER_BEENDET") == 1,
                            r.getString("ZUSTAND"), z == null ? null : z.getTime(), r.getString("TURM"));
                }
            }
        });
    }

    // ------------------------------------------------------------------ Protokoll

    @Override public void protokoll(List<Daten.LogZeile> zeilen, Long sitzung) throws Fehler {
        if (zeilen.isEmpty()) return;
        tx("Protokoll schreiben", c -> {
            try (PreparedStatement st = c.prepareStatement("INSERT INTO LH_LOG (ID, SESSION_ID, ZEIT, STUFE, QUELLE, TEXT, SZENENZEIT, ORT, DETAIL) "
                    + "VALUES (LH_LOG_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (Daten.LogZeile z : zeilen) {
                    bind(st, sitzung, new Timestamp(z.zeitMs()), z.stufe(), cut(z.quelle(), 60), cut(z.text(), 1000),
                            z.szenenzeitMs() == null ? null : new Timestamp(z.szenenzeitMs()), cut(z.ort(), 40), z.detail() == null ? null : new Clob(z.detail()));
                    st.addBatch();
                }
                st.executeBatch();
            }
            return null;
        });
    }

    @Override public List<Daten.LogZeile> letzteZeilen(int n) throws Fehler {
        return tx("Protokoll lesen", c -> {
            List<Daten.LogZeile> l = new ArrayList<>();
            try (PreparedStatement st = c.prepareStatement("SELECT ZEIT, STUFE, QUELLE, TEXT, SZENENZEIT, ORT FROM LH_LOG ORDER BY ID DESC FETCH FIRST ? ROWS ONLY")) {
                st.setInt(1, n);
                try (ResultSet r = st.executeQuery()) {
                    while (r.next()) {
                        Timestamp sz = r.getTimestamp(5);
                        l.add(0, new Daten.LogZeile(r.getTimestamp(1).getTime(), r.getString(2), r.getString(3), r.getString(4), sz == null ? null : sz.getTime(), r.getString(6), null));
                    }
                }
            }
            return l;
        });
    }

    // ------------------------------------------------------------------ Einrichter

    /** Den Einrichter auf dieser Verbindung laufen lassen (F34 A). */
    public synchronized Einrichter.Ergebnis einrichten(Einrichter.Fortschritt f) throws Fehler {
        Connection x = verbindung();
        try {
            Einrichter.Ergebnis e = Einrichter.einrichten(x, Einrichter.ordner(), f);
            x.commit();
            return e;
        } catch (java.io.IOException e) {
            throw new Fehler("Skripte in " + Einrichter.ordner() + " nicht lesbar: " + e.getMessage(), e);
        } catch (SQLException e) {
            try { x.rollback(); } catch (SQLException e2) { /* weg */ }
            throw new Fehler("Einrichter: " + Einrichter.erste(e), e);
        }
    }

    /** Nur prüfen, nichts schreiben. */
    public synchronized Einrichter.Ergebnis pruefen() throws Fehler {
        try {
            Einrichter.Ergebnis e = Einrichter.pruefen(verbindung(), Einrichter.ordner());
            verbindung().rollback();
            return e;
        } catch (java.io.IOException e) {
            throw new Fehler("Skripte in " + Einrichter.ordner() + " nicht lesbar: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new Fehler("Prüfen: " + Einrichter.erste(e), e);
        }
    }

    /** Stufe des Schemas (0 = leer, −1 = unvollständig, sonst LH_SCHEMA.VERSION). */
    public synchronized int stufe() throws Fehler {
        try {
            int n = Einrichter.stufe(verbindung());
            verbindung().rollback();
            return n;
        } catch (SQLException e) {
            throw new Fehler("Stand des Schemas: " + Einrichter.erste(e), e);
        }
    }

    @Override public synchronized void schliessen() {
        if (c != null) try { c.close(); } catch (SQLException e) { /* weg */ }
        c = null;
    }
}
