package com.dan.lighthouse.db;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Hält den laufenden {@link Speicher}, die Sitzung und die beiden Hintergrundarbeiten: das Protokoll alle fünf
 * Sekunden schreiben (F43) und den Zustand alle zwei Minuten sichern (F39). Ein eigener Faden, damit die Szene nie
 * auf die Datenbank wartet.
 */
public final class Dienst {
    private Dienst() { }

    /** Abstände in Sekunden. */
    public static final int PROTOKOLL_S = 5, SICHERN_S = 120;

    private static volatile Speicher speicher = new DateiSpeicher(DateiSpeicher.ordner());
    private static volatile long sitzung = -1;
    private static volatile Supplier<String[]> zustand;
    private static volatile long gesichert;
    private static volatile String letzterFehler = "";
    private static ScheduledExecutorService exec;

    public static Speicher speicher() { return speicher; }
    public static long sitzung() { return sitzung; }
    /** Wann zuletzt gesichert wurde (ms seit 1970, 0 = noch nie). */
    public static long gesichert() { return gesichert; }
    public static String letzterFehler() { return letzterFehler; }

    /** Speicher setzen (beim Start oder nach dem Einrichten); die laufende Sitzung zieht mit um. */
    public static synchronized void setzen(Speicher s) {
        Speicher alt = speicher;
        speicher = s;
        if (alt != s && alt != null) {
            if (sitzung >= 0) sitzung = -1;
            alt.schliessen();
        }
    }

    /**
     * Sitzung beginnen und die Hintergrundarbeiten starten. erfassen liefert {Zustand als Text, Szenenzeit ms, Turm,
     * Regiebuch, Stunde im Regiebuch}; es läuft im Faden des Dienstes und liest nur volatile Felder.
     */
    public static synchronized void starten(String version, String gpu, String qualitaet, Supplier<String[]> erfassen) {
        zustand = erfassen;
        beginnen(version, gpu, qualitaet);
        if (exec != null) return;
        exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Lighthouse-Daten");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        exec.scheduleWithFixedDelay(Dienst::protokollSchreiben, PROTOKOLL_S, PROTOKOLL_S, TimeUnit.SECONDS);
        exec.scheduleWithFixedDelay(() -> sichern(false), SICHERN_S, SICHERN_S, TimeUnit.SECONDS);
    }

    private static String version = "", gpu = "", qualitaet = "";

    static synchronized void beginnen(String v, String g, String q) {
        version = v; gpu = g; qualitaet = q;
        try {
            sitzung = speicher.sitzungBeginnen(v, g, q);
            Protokoll.info("Sitzung", "Sitzung " + sitzung + " begonnen · " + speicher.wo());
        } catch (Speicher.Fehler e) {
            sitzung = -1;
            Protokoll.fehler("Sitzung", "Sitzung nicht begonnen: " + e.getMessage(), null);
        }
    }

    /** Nach einem Wechsel des Speichers (Einrichten, Verbinden) eine neue Sitzung dort beginnen. */
    public static synchronized void neuBeginnen() {
        if (zustand != null) beginnen(version, gpu, qualitaet);
    }

    /** Protokoll schreiben; scheitert die Datenbank, gehen die Zeilen in die Datei. */
    public static void protokollSchreiben() {
        List<Daten.LogZeile> l = Protokoll.abholen();
        if (l.isEmpty()) return;
        Speicher s = speicher;
        try {
            s.protokoll(l, sitzung >= 0 ? sitzung : null);
        } catch (Speicher.Fehler e) {
            letzterFehler = e.getMessage();
            System.err.println("Lighthouse: Protokoll nicht geschrieben: " + e.getMessage());
            if (s.datenbank()) {
                try { new DateiSpeicher(DateiSpeicher.ordner()).protokoll(l, null); } catch (Speicher.Fehler e2) { /* dann eben nicht */ }
            }
        }
    }

    /** Zustand sichern (alle zwei Minuten und beim Ende); mit ende wird die Sitzung abgeschlossen. */
    public static void sichern(boolean ende) {
        Supplier<String[]> z = zustand;
        if (z == null || sitzung < 0) return;
        try {
            String[] v = z.get();
            speicher.sitzungSichern(sitzung, v[0], Long.parseLong(v[1]), v[2], v[3], Zahl.lies(v[4]));
            gesichert = System.currentTimeMillis();
            if (ende) speicher.sitzungBeenden(sitzung);
        } catch (Speicher.Fehler | RuntimeException e) {
            letzterFehler = e.getMessage();
            Protokoll.fehler("Sitzung", "Zustand nicht gesichert: " + e.getMessage(), e instanceof RuntimeException ? e : null);
        }
    }

    /** Beim Beenden der App: letzter Stand, Protokoll, Verbindung zu. */
    public static synchronized void beenden() {
        Protokoll.info("Sitzung", "Sitzung " + sitzung + " geordnet beendet");
        sichern(true);
        protokollSchreiben();
        if (exec != null) exec.shutdownNow();
        speicher.schliessen();
    }

    /** Eine Arbeit im Faden des Dienstes (Laden, Speichern aus der Oberfläche). */
    public static void spaeter(Runnable r) {
        ScheduledExecutorService e = exec;
        if (e != null) e.execute(r);
        else {
            Thread t = new Thread(r, "Lighthouse-Daten-einmal");
            t.setDaemon(true);
            t.start();
        }
    }
}
