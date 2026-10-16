package com.dan.lighthouse.db;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Das Protokoll (F43 A): Fehler und Warnungen mit Stapel, dazu Bedienschritte (Sitzung, Laden, Speichern,
 * Turmauswahl, Presets, Regieschritte, Einrichter). Jede Zeile trägt Szenenzeit und Ort. Gesammelt wird im Speicher
 * des Programms; ein Faden schreibt alle fünf Sekunden in einem Zug in den {@link Dienst#speicher() Speicher}
 * (LH_LOG oder daten/protokoll.txt). Ereignisse der Szene (Blitz, Brecher, Möwe) kommen nicht hinein.
 */
public final class Protokoll {
    private Protokoll() { }

    public static final String FEHLER = "FEHLER", WARNUNG = "WARNUNG", INFO = "INFO";
    static final ConcurrentLinkedQueue<Daten.LogZeile> WARTET = new ConcurrentLinkedQueue<>();
    /** Die letzten Zeilen für die Anzeige im Block DATEN (auch wenn das Schreiben scheitert). */
    static final List<Daten.LogZeile> ZULETZT = new ArrayList<>();
    static volatile Supplier<Long> szenenzeit = () -> null;
    static volatile Supplier<String> ort = () -> null;

    /** Woher Szenenzeit und Ort kommen (die App setzt das, sobald die Szene steht). */
    public static void kontext(Supplier<Long> zeit, Supplier<String> wo) {
        szenenzeit = zeit;
        ort = wo;
    }

    public static void info(String quelle, String text) { add(INFO, quelle, text, null); }
    public static void warnung(String quelle, String text) { add(WARNUNG, quelle, text, null); }
    public static void fehler(String quelle, String text, Throwable t) { add(FEHLER, quelle, text, t); }

    static void add(String stufe, String quelle, String text, Throwable t) {
        Long sz = null;
        String o = null;
        try { sz = szenenzeit.get(); o = ort.get(); } catch (RuntimeException e) { /* ohne Kontext */ }
        String det = null;
        if (t != null) {
            StringWriter w = new StringWriter();
            t.printStackTrace(new PrintWriter(w));
            det = w.toString();
        }
        Daten.LogZeile z = new Daten.LogZeile(System.currentTimeMillis(), stufe, quelle, text, sz, o, det);
        WARTET.add(z);
        synchronized (ZULETZT) {
            ZULETZT.add(z);
            while (ZULETZT.size() > 200) ZULETZT.remove(0);
        }
        if (!INFO.equals(stufe)) System.err.println("Lighthouse · " + stufe + " · " + quelle + ": " + text);
    }

    /** Die letzten n Zeilen dieser Sitzung. */
    public static List<Daten.LogZeile> zuletzt(int n) {
        synchronized (ZULETZT) {
            return new ArrayList<>(ZULETZT.subList(Math.max(0, ZULETZT.size() - n), ZULETZT.size()));
        }
    }

    /** Alles Wartende holen (für den schreibenden Faden). */
    static List<Daten.LogZeile> abholen() {
        List<Daten.LogZeile> l = new ArrayList<>();
        Daten.LogZeile z;
        while ((z = WARTET.poll()) != null) l.add(z);
        return l;
    }

    /** Eine Zeile zum Anzeigen: „20:15 · WARNUNG · Einrichter: …“. */
    public static String zeile(Daten.LogZeile z) {
        java.time.LocalTime t = java.time.Instant.ofEpochMilli(z.zeitMs()).atZone(java.time.ZoneId.systemDefault()).toLocalTime();
        return String.format("%02d:%02d:%02d · %s · %s: %s", t.getHour(), t.getMinute(), t.getSecond(), z.stufe(), z.quelle(), z.text());
    }
}
