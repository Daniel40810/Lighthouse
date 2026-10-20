package com.dan.lighthouse.db;

import com.dan.lighthouse.camera.Aufstieg;
import com.dan.lighthouse.camera.Camera;
import com.dan.weather.Scenario;
import com.dan.weather.Weather;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Die mitgelieferten Daten (HERKUNFT = MITGELIEFERT): neun Leuchttürme mit Sektoren, zehn Wetterlagen, drei
 * Regiebücher, zwei Kamerafahrten und die Presets calm, breeze, gale und storm von zehn Packages.
 * <p>
 * Das ist die eine Quelle: {@link #sql()} schreibt daraus {@code db/sql/04_startdaten.sql}, und der
 * {@link DateiSpeicher} liest dieselben Daten ohne Datenbank. Die Prüfung vergleicht die Datei mit dem, was hier steht.
 */
public final class Startdaten {
    private Startdaten() { }

    /** Die Größen der Regie in der Reihenfolge von {@code Weather.Var} (Schlüssel im Textformat). */
    public static final String[] VARS;
    /** Die Spalten dazu in LH_WEATHER_PRESET und LH_SCENARIO_STEP, gleiche Reihenfolge. */
    public static final String[] SPALTEN = {"WIND_MS", "RICHTUNG_GRAD", "BOEIGKEIT", "BEDECKUNG_ACHTEL", "HOHE_SCHICHT", "WOLKENART",
            "REGEN_MMH", "SICHT_KM", "NEBEL", "LUFTDRUCK_HPA", "TEMPERATUR_C", "FEUCHTE_PROZ", "DUENUNG_M", "GEWITTER"};
    /** Lage der Wolkenart in den Wetterspalten (als Name gespeichert, in der Regie als Zahl). */
    public static final int WOLKE = Weather.Var.CLOUD.ordinal();

    static {
        Weather.Var[] v = Weather.Var.values();
        VARS = new String[v.length];
        for (int i = 0; i < v.length; i++) VARS[i] = v[i].name();
        if (v.length != SPALTEN.length) throw new IllegalStateException("Weather.Var und die Wetterspalten passen nicht zusammen");
    }

    // ------------------------------------------------------------------ Leuchttürme

    private static List<Daten.Turm> tuerme;

    /**
     * Die neun Türme. Je Turm eine Zeile T (Name | Revier | Breite | Länge | Feuerhöhe | Turmhöhe | Tragweite | Kennung |
     * Optik | Baujahr | außer Betrieb | Stand | Farben | Beschreibung | Quelle), darunter je Sektor eine Zeile S
     * (Farbe | von | bis | Tragweite | Art | Bezeichnung). Leere Felder = unbekannt.
     */
    static final String TUERME = TuermeText.TEXT;

    public static synchronized List<Daten.Turm> tuerme() {
        if (tuerme == null) tuerme = Collections.unmodifiableList(leseTuerme(TUERME));
        return tuerme;
    }

    static List<Daten.Turm> leseTuerme(String text) {
        List<Daten.Turm> l = new ArrayList<>();
        String[] t = null;
        List<Daten.Sektor> s = new ArrayList<>();
        int no = 0;
        for (String z : text.split("\\R")) {
            no++;
            if (z.isBlank() || z.strip().startsWith("#")) continue;
            String[] f = z.strip().split("\\s*\\|\\s*", -1);
            if (f[0].equals("T")) {
                if (t != null) l.add(turm(t, s));
                if (f.length != 16) throw new IllegalArgumentException("Zeile " + no + ": 16 Felder erwartet, " + f.length + " gefunden");
                t = f;
                s = new ArrayList<>();
            } else if (f[0].equals("S")) {
                if (f.length != 7) throw new IllegalArgumentException("Zeile " + no + ": 7 Felder erwartet");
                s.add(new Daten.Sektor(s.size() + 1, Zahl.lies(f[2]), Zahl.lies(f[3]), f[1], f[5].isEmpty() ? "SEKTOR" : f[5],
                        f[4].isEmpty() ? null : Zahl.lies(f[4]), f[6].isEmpty() ? null : f[6]));
            } else throw new IllegalArgumentException("Zeile " + no + ": T oder S erwartet");
        }
        if (t != null) l.add(turm(t, s));
        return l;
    }

    private static Daten.Turm turm(String[] f, List<Daten.Sektor> s) {
        return new Daten.Turm(f[1], f[2], Zahl.lies(f[3]), Zahl.lies(f[4]), Zahl.lies(f[5]), opt(f[6]), opt(f[7]), f[8], f[9],
                optInt(f[10]), optInt(f[11]), Integer.parseInt(f[12]), f[13], f[14], f[15], Daten.MITGELIEFERT, s);
    }

    private static Double opt(String s) { return s.isEmpty() ? null : Zahl.lies(s); }
    private static Integer optInt(String s) { return s.isEmpty() ? null : Integer.parseInt(s); }

    // ------------------------------------------------------------------ Wetterlagen

    /**
     * Die acht Lagen der Regie mit ihren Zielwerten aus {@code com.dan.weather} und zwei Ränder dazu: Windstille und
     * Orkan (gestaltet [G]).
     */
    public static List<Daten.Wetterlage> wetterlagen() {
        List<Daten.Wetterlage> l = new ArrayList<>();
        String[] namen = {"Klar", "Aufziehend", "Front", "Sturm", "Gewitter", "Abklingend", "Aufklarend", "Seenebel"};
        String[] text = {"Hoch über der Deutschen Bucht, leichte Brise, Schönwetterwolken",
                "Ein Tief nähert sich: Cirren, rückdrehender Wind, Dünung kommt vor dem Wind",
                "Die Front: geschlossene Regenschicht, Wind frischt auf",
                "Sturm aus Südwest bis West, Bft 9, Schauer",
                "Gewitter in der Kaltluft, Böen und Starkregen",
                "Der Sturm flaut ab, Schauer, aufklarender Himmel",
                "Rückseite: klare Luft, weite Sicht, Wind auf Nordwest",
                "Schwacher Wind, feuchte Luft über kühlerem Wasser, dichter Nebel"};
        Weather.Phase[] ph = Weather.Phase.values();
        for (int i = 0; i < Math.min(ph.length, namen.length); i++) {   // ohne ORKAN aus Rungholt (Rückfluss Phase 17)
            double[] w = Weather.target(ph[i]);
            l.add(new Daten.Wetterlage(namen[i], (double) Weather.beaufort(w[0]), ph[i].name(), w, text[i], Daten.MITGELIEFERT));
        }
        //                          Wind Richt Böig Bed hoch Wolke Regen Sicht Nebel Druck Temp Feuchte Dünung Gewitter
        l.add(new Daten.Wetterlage("Windstille", 0.0, "KLAR", new double[]{0.2, 180, 0.05, 0, 0.1, 0, 0, 45, 0, 1032, 16, 70, 0.15, 0},
                "Spiegelglatte See, wolkenlos, Hochdruck [G]", Daten.MITGELIEFERT));
        l.add(new Daten.Wetterlage("Orkan", 12.0, "STURM", new double[]{34, 280, 0.28, 8, 0, 3, 8, 3, 0, 962, 9, 95, 7.5, 0},
                "Orkan aus West, Bft 12, Gischt bis über die Galerie [G]", Daten.MITGELIEFERT));
        return l;
    }

    // ------------------------------------------------------------------ Regiebücher

    public static List<Daten.Regiebuch> regiebuecher() {
        return List.of(
                new Daten.Regiebuch(Scenario.stormDay().name(), Scenario.stormDay().toText(),
                        "Morgenstille, Front, Sturm Bft 9, Gewitter in der Dämmerung, Sturmnacht, Aufklaren mit Regenbogen; 28 Stunden in gut 4 Minuten",
                        Daten.MITGELIEFERT),
                new Daten.Regiebuch(Scenario.fogDay().name(), Scenario.fogDay().toText(),
                        "Klare Dämmerung, Nebelbank, dichter Seenebel mit Nebelhorn und Typhon, flacher Nebel, Aufklaren; 14 Stunden in gut 4 Minuten",
                        Daten.MITGELIEFERT),
                new Daten.Regiebuch(Scenario.calmDays().name(), Scenario.calmDays().toText(),
                        "Zwei Tage Hochdruck mit Seenebel am Morgen", Daten.MITGELIEFERT));
    }

    // ------------------------------------------------------------------ Kamerafahrten

    public static List<Daten.Fahrt> fahrten() {
        return List.of(
                fahrt(Aufstieg.rundflug(140, 34, 120), "RUNDFLUG", "Einmal um den Turm in zwei Minuten, Höhe und Abstand atmen sacht"),
                fahrt(new Aufstieg(), "AUFSTIEG", "Von der Tür die 59 Stufen hinauf, durch die Luke in den Laternenraum"));
    }

    /** Eine Kamerafahrt als Datensatz; ein Schnitt wird als zweiter Punkt 1 ms später gespeichert. */
    public static Daten.Fahrt fahrt(Aufstieg a, String art, String beschreibung) {
        List<Daten.Stuetzpunkt> l = new ArrayList<>();
        int nr = 0;
        for (Aufstieg.Key k : a.keys())
            l.add(new Daten.Stuetzpunkt(++nr, Math.round(k.t() * 1000) / 1000.0, r2(k.x()), r2(k.y()), r2(k.z()), r2(k.yaw()), r2(k.pitch()), Camera.FOVY));
        return new Daten.Fahrt(a.name, art, a.loop, beschreibung, Daten.MITGELIEFERT, l);
    }

    /** Aus dem Datensatz wieder eine Fahrt; zwei Punkte unter 0,01 s Abstand sind ein Schnitt. */
    public static Aufstieg fahrt(Daten.Fahrt f) {
        List<Aufstieg.Key> l = new ArrayList<>();
        double prev = Double.NEGATIVE_INFINITY;
        for (Daten.Stuetzpunkt p : f.punkte()) {
            l.add(new Aufstieg.Key(p.zeitS(), p.x(), p.y(), p.z(), p.gier(), p.nick(), p.zeitS() - prev < 0.01));
            prev = p.zeitS();
        }
        return new Aufstieg(f.name(), l, f.schleife());
    }

    private static double r2(double v) { return Math.round(v * 100) / 100.0; }

    // ------------------------------------------------------------------ Presets der Packages

    /** Die Package-Klassen mit den Presets aus dem Paketvertrag (Punkt 3). */
    public static final String[] PAKETE = {"com.dan.ocean.Ocean", "com.dan.spray.Spray", "com.dan.rain.Rain", "com.dan.wetglass.Wetglass",
            "com.dan.weathering.Weathering", "com.dan.lightning.Lightning", "com.dan.fog.Fog", "com.dan.buoyancy.Buoyancy",
            "com.dan.flock.Flock", "com.dan.soundscape.Soundscape"};
    public static final String[] PRESETS = {"calm", "breeze", "gale", "storm"};
    private static final Map<String, String> PRESET_TEXT = Map.of("calm", "ruhig, Bft 2", "breeze", "frische Brise, Bft 5",
            "gale", "stürmisch, Bft 8", "storm", "schwerer Sturm, Bft 10");

    private static List<Daten.PaketPreset> presets;

    public static synchronized List<Daten.PaketPreset> presets() {
        if (presets != null) return presets;
        List<Daten.PaketPreset> l = new ArrayList<>();
        for (String k : PAKETE) {
            try {
                Class<?> c = Class.forName(k);
                for (String p : PRESETS) {
                    Method m = c.getMethod(p);
                    Object o = m.invoke(null);
                    l.add(new Daten.PaketPreset(c.getPackageName(), c.getSimpleName(), p, "Preset " + p + "() aus dem Package: " + PRESET_TEXT.get(p),
                            Daten.MITGELIEFERT, Felder.lesen(o)));
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Preset von " + k + " nicht lesbar: " + e, e);
            }
        }
        presets = Collections.unmodifiableList(l);
        return presets;
    }

    // ------------------------------------------------------------------ SQL

    /** Schreibt alle mitgelieferten Daten als SQL (MERGE je Eintrag; Kinder werden neu angelegt). */
    public static String sql() {
        StringBuilder b = new StringBuilder();
        b.append("""
                -- =====================================================================================================
                -- Lighthouse · Datenbank · 04_startdaten.sql
                -- ERZEUGT aus com.dan.lighthouse.db.Startdaten (tools.StartdatenSql) – nicht von Hand ändern.
                -- Mitgelieferte Zeilen (HERKUNFT = 'MITGELIEFERT') werden angelegt oder wiederhergestellt; eigene Zeilen
                -- bleiben unberührt. Beliebig oft wiederholbar.
                -- =====================================================================================================

                -- Die Quellen enthalten „&“: in SQL Developer keine Ersetzungsvariablen
                SET DEFINE OFF

                """);
        b.append("-- ----------------------------------------------------------------------------------------- Leuchttürme\n");
        for (Daten.Turm t : tuerme()) {
            String set = "t.REVIER = " + s(t.revier()) + ", t.LAGE = " + lage(t) + ",\n    t.FEUERHOEHE_M = " + n(t.feuerhoeheM()) + ", t.TURMHOEHE_M = " + n(t.turmhoeheM())
                    + ", t.TRAGWEITE_SM = " + n(t.tragweiteSm()) + ", t.KENNUNG = " + s(t.kennung()) + ", t.OPTIK = " + s(t.optik()) + ", t.BAUJAHR = " + n(t.baujahr())
                    + ", t.AUSSER_BETRIEB = " + n(t.ausserBetrieb()) + ", t.STAND_JAHR = " + t.standJahr() + ",\n    t.FARBEN = " + s(t.farben())
                    + ",\n    t.BESCHREIBUNG = " + s(t.beschreibung()) + ",\n    t.QUELLE = " + s(t.quelle());
            b.append("MERGE INTO LH_LIGHTHOUSE t\nUSING (SELECT ").append(s(t.name())).append(" AS NAME FROM DUAL) q\nON (t.NAME = q.NAME)\n")
                    .append("WHEN MATCHED THEN UPDATE SET ").append(set).append("\n  WHERE t.HERKUNFT = 'MITGELIEFERT'\n")
                    .append("WHEN NOT MATCHED THEN INSERT (NAME, REVIER, LAGE, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, KENNUNG, OPTIK, BAUJAHR, AUSSER_BETRIEB, STAND_JAHR, FARBEN, BESCHREIBUNG, QUELLE, HERKUNFT)\n")
                    .append("  VALUES (").append(s(t.name())).append(", ").append(s(t.revier())).append(", ").append(lage(t)).append(", ").append(n(t.feuerhoeheM()))
                    .append(", ").append(n(t.turmhoeheM())).append(", ").append(n(t.tragweiteSm())).append(", ").append(s(t.kennung())).append(", ").append(s(t.optik()))
                    .append(", ").append(n(t.baujahr())).append(", ").append(n(t.ausserBetrieb())).append(", ").append(t.standJahr()).append(", ").append(s(t.farben()))
                    .append(", ").append(s(t.beschreibung())).append(", ").append(s(t.quelle())).append(", 'MITGELIEFERT');\n");
            String eltern = "FROM LH_LIGHTHOUSE WHERE NAME = " + s(t.name()) + " AND HERKUNFT = 'MITGELIEFERT'";
            b.append("DELETE FROM LH_SECTOR WHERE LIGHTHOUSE_ID IN (SELECT ID ").append(eltern).append(");\n");
            for (Daten.Sektor x : t.sektoren())
                b.append("INSERT INTO LH_SECTOR (LIGHTHOUSE_ID, NR, VON_GRAD, BIS_GRAD, FARBE, ART, TRAGWEITE_SM, BEZEICHNUNG) SELECT ID, ").append(x.nr()).append(", ")
                        .append(n(x.von())).append(", ").append(n(x.bis())).append(", ").append(s(x.farbe())).append(", ").append(s(x.art())).append(", ")
                        .append(n(x.tragweiteSm())).append(", ").append(s(x.bezeichnung())).append(' ').append(eltern).append(";\n");
            b.append('\n');
        }
        b.append("-- ----------------------------------------------------------------------------------------- Wetterlagen\n");
        for (Daten.Wetterlage w : wetterlagen()) {
            StringBuilder cols = new StringBuilder("NAME, BEAUFORT, PHASE"), vals = new StringBuilder(s(w.name()) + ", " + n(w.beaufort()) + ", " + s(w.phase()));
            StringBuilder set = new StringBuilder("t.BEAUFORT = " + n(w.beaufort()) + ", t.PHASE = " + s(w.phase()));
            for (int i = 0; i < SPALTEN.length; i++) {
                String v = wert(i, w.werte()[i]);
                cols.append(", ").append(SPALTEN[i]);
                vals.append(", ").append(v);
                set.append(", t.").append(SPALTEN[i]).append(" = ").append(v);
            }
            set.append(", t.BESCHREIBUNG = ").append(s(w.beschreibung()));
            b.append("MERGE INTO LH_WEATHER_PRESET t\nUSING (SELECT ").append(s(w.name())).append(" AS NAME FROM DUAL) q\nON (t.NAME = q.NAME)\n")
                    .append("WHEN MATCHED THEN UPDATE SET ").append(set).append("\n  WHERE t.HERKUNFT = 'MITGELIEFERT'\n")
                    .append("WHEN NOT MATCHED THEN INSERT (").append(cols).append(", BESCHREIBUNG, HERKUNFT)\n  VALUES (").append(vals).append(", ")
                    .append(s(w.beschreibung())).append(", 'MITGELIEFERT');\n");
        }
        b.append('\n');
        b.append("-- ----------------------------------------------------------------------------------------- Regiebücher\n");
        for (Daten.Regiebuch r : regiebuecher()) {
            Scenario sc = Scenario.parse(r.text());
            b.append("MERGE INTO LH_SCENARIO t\nUSING (SELECT ").append(s(r.name())).append(" AS NAME FROM DUAL) q\nON (t.NAME = q.NAME)\n")
                    .append("WHEN MATCHED THEN UPDATE SET t.BEGINN_STUNDE = ").append(n(sc.startHour())).append(", t.ZEITRAFFER = ").append(n(sc.lapse()))
                    .append(", t.BESCHREIBUNG = ").append(s(r.beschreibung())).append("\n  WHERE t.HERKUNFT = 'MITGELIEFERT'\n")
                    .append("WHEN NOT MATCHED THEN INSERT (NAME, BEGINN_STUNDE, ZEITRAFFER, BESCHREIBUNG, HERKUNFT)\n  VALUES (").append(s(r.name())).append(", ")
                    .append(n(sc.startHour())).append(", ").append(n(sc.lapse())).append(", ").append(s(r.beschreibung())).append(", 'MITGELIEFERT');\n");
            String eltern = "FROM LH_SCENARIO WHERE NAME = " + s(r.name()) + " AND HERKUNFT = 'MITGELIEFERT'";
            b.append("DELETE FROM LH_SCENARIO_STEP WHERE SCENARIO_ID IN (SELECT ID ").append(eltern).append(");\n");
            int nr = 0;
            for (Scenario.Step st : sc.steps()) {
                StringBuilder cols = new StringBuilder("SCENARIO_ID, NR, DAUER_H, PHASE, TEXT, KAMERA"),
                        vals = new StringBuilder("ID, " + (++nr) + ", " + n(st.hours()) + ", " + s(st.phase().name()) + ", " + s(st.text()) + ", " + s(st.camera()));
                for (int i = 0; i < SPALTEN.length; i++) {
                    double v = st.values()[i];
                    if (Double.isNaN(v)) continue;
                    cols.append(", ").append(SPALTEN[i]);
                    vals.append(", ").append(wert(i, v));
                }
                b.append("INSERT INTO LH_SCENARIO_STEP (").append(cols).append(") SELECT ").append(vals).append(' ').append(eltern).append(";\n");
            }
            b.append('\n');
        }
        b.append("-- ----------------------------------------------------------------------------------------- Kamerafahrten\n");
        for (Daten.Fahrt f : fahrten()) {
            b.append("MERGE INTO LH_CAMERA_PATH t\nUSING (SELECT ").append(s(f.name())).append(" AS NAME FROM DUAL) q\nON (t.NAME = q.NAME)\n")
                    .append("WHEN MATCHED THEN UPDATE SET t.ART = ").append(s(f.art())).append(", t.SCHLEIFE = ").append(f.schleife() ? 1 : 0)
                    .append(", t.BESCHREIBUNG = ").append(s(f.beschreibung())).append("\n  WHERE t.HERKUNFT = 'MITGELIEFERT'\n")
                    .append("WHEN NOT MATCHED THEN INSERT (NAME, ART, SCHLEIFE, BESCHREIBUNG, HERKUNFT)\n  VALUES (").append(s(f.name())).append(", ").append(s(f.art()))
                    .append(", ").append(f.schleife() ? 1 : 0).append(", ").append(s(f.beschreibung())).append(", 'MITGELIEFERT');\n");
            String eltern = "FROM LH_CAMERA_PATH WHERE NAME = " + s(f.name()) + " AND HERKUNFT = 'MITGELIEFERT'";
            b.append("DELETE FROM LH_CAMERA_POINT WHERE CAMERA_PATH_ID IN (SELECT ID ").append(eltern).append(");\n");
            for (Daten.Stuetzpunkt p : f.punkte())
                b.append("INSERT INTO LH_CAMERA_POINT (CAMERA_PATH_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, KAM_FOV_GRAD) SELECT ID, ")
                        .append(p.nr()).append(", ").append(n(p.zeitS())).append(", ").append(n(p.x())).append(", ").append(n(p.y())).append(", ").append(n(p.z()))
                        .append(", ").append(n(p.gier())).append(", ").append(n(p.nick())).append(", ").append(n(p.fov())).append(' ').append(eltern).append(";\n");
            b.append('\n');
        }
        b.append("-- ----------------------------------------------------------------------------------------- Presets der Packages\n");
        for (Daten.PaketPreset p : presets()) {
            String key = "PAKET = " + s(p.paket()) + " AND KLASSE = " + s(p.klasse()) + " AND NAME = " + s(p.name());
            b.append("MERGE INTO LH_PACKAGE_PRESET t\nUSING (SELECT ").append(s(p.paket())).append(" AS PAKET, ").append(s(p.klasse())).append(" AS KLASSE, ")
                    .append(s(p.name())).append(" AS NAME FROM DUAL) q\nON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)\n")
                    .append("WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = ").append(s(p.beschreibung())).append("\n  WHERE t.HERKUNFT = 'MITGELIEFERT'\n")
                    .append("WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)\n  VALUES (").append(s(p.paket())).append(", ")
                    .append(s(p.klasse())).append(", ").append(s(p.name())).append(", ").append(s(p.beschreibung())).append(", 'MITGELIEFERT');\n");
            String eltern = "FROM LH_PACKAGE_PRESET WHERE " + key + " AND HERKUNFT = 'MITGELIEFERT'";
            b.append("DELETE FROM LH_PACKAGE_SETTING WHERE PACKAGE_PRESET_ID IN (SELECT ID ").append(eltern).append(");\n");
            Map<String, String> typen = typen(p);
            for (Map.Entry<String, String> e : p.felder().entrySet())
                b.append("INSERT INTO LH_PACKAGE_SETTING (PACKAGE_PRESET_ID, FELD, TYP, WERT) SELECT ID, ").append(s(e.getKey())).append(", ")
                        .append(s(typen.getOrDefault(e.getKey(), "STRING"))).append(", ").append(s(e.getValue())).append(' ').append(eltern).append(";\n");
            b.append('\n');
        }
        b.append("-- ----------------------------------------------------------------------------------------- Stand\n");
        b.append("MERGE INTO LH_SCHEMA t\nUSING (SELECT 1 AS VERSION FROM DUAL) q\nON (t.VERSION = q.VERSION)\n")
                .append("WHEN MATCHED THEN UPDATE SET t.APP_VERSION = 'Phase 15'\n")
                .append("WHEN NOT MATCHED THEN INSERT (VERSION, APP_VERSION, TEXT)\n  VALUES (1, 'Phase 15', ")
                .append(s("13 Tabellen, 3 Sichten, Spatial, Startdaten: " + tuerme().size() + " Türme, " + wetterlagen().size() + " Wetterlagen, "
                        + regiebuecher().size() + " Regiebücher, " + fahrten().size() + " Kamerafahrten, " + presets().size() + " Presets")).append(");\n\n");
        b.append("COMMIT;\n\n-- Ende 04_startdaten.sql\n");
        return b.toString();
    }

    /** Die Feldarten der Klasse eines Presets (für LH_PACKAGE_SETTING.TYP). */
    static Map<String, String> typen(Daten.PaketPreset p) {
        try {
            return Felder.typen(Class.forName(p.paket() + "." + p.klasse()));
        } catch (ClassNotFoundException e) {
            return new LinkedHashMap<>();
        }
    }

    /** Wert einer Wetterspalte als SQL: die Wolkenart als Name, sonst Zahl; NaN = NULL. */
    static String wert(int i, double v) {
        if (Double.isNaN(v)) return "NULL";
        if (i == WOLKE) return s(Weather.CloudKind.values()[(int) Math.max(0, Math.min(4, Math.round(v)))].name());
        return n(v);
    }

    static String lage(Daten.Turm t) {
        return "MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(" + n(t.laenge()) + ", " + n(t.breite()) + ", NULL), NULL, NULL)";
    }

    /** Text als SQL-Literal; null = NULL. Zeichen außerhalb von Latin-1 bleiben, die Skripte sind UTF-8. */
    static String s(String v) { return v == null ? "NULL" : "'" + v.replace("'", "''") + "'"; }

    static String n(Number v) {
        if (v == null) return "NULL";
        double d = v.doubleValue();
        return Double.isNaN(d) ? "NULL" : Zahl.text(d);
    }

    /** Für Listen: Namen der Türme. */
    public static List<String> turmNamen() {
        List<String> l = new ArrayList<>();
        for (Daten.Turm t : tuerme()) l.add(t.name());
        return l;
    }

    static String fmt(double v) { return String.format(Locale.ROOT, "%.4f", v); }

    static List<String> zeilen(String s) { return Arrays.asList(s.split("\\R")); }
}
