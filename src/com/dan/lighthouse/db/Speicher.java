package com.dan.lighthouse.db;

import java.util.List;

/**
 * Wohin die App speichert: in die Datenbank ({@link DbSpeicher}) oder, ohne Datenbank, in Dateien im Ordner
 * {@code daten/} ({@link DateiSpeicher}, F40 A). Beide können dasselbe; die Oberfläche kennt nur diese Schnittstelle.
 * <p>
 * Namen sind eindeutig je Art. Speichern unter einem vorhandenen Namen ersetzt den Eintrag; ein mitgelieferter Eintrag
 * wird dabei nicht überschrieben, sondern abgelehnt (der Einrichter stellt ihn sonst beim nächsten Lauf wieder her).
 * Fehler kommen als {@link Fehler} mit einer lesbaren Meldung.
 */
public interface Speicher {

    /** Ein Fehler beim Lesen oder Schreiben, mit lesbarer Meldung. */
    final class Fehler extends Exception {
        public Fehler(String m, Throwable cause) { super(m, cause); }
        public Fehler(String m) { super(m); }
    }

    /** „Datenbank LIGHTHOUSE auf PDBORCL“ oder „Dateien in …/daten“. */
    String wo();

    boolean datenbank();

    Daten.Stand stand() throws Fehler;

    List<Daten.Turm> tuerme() throws Fehler;

    List<Daten.Wetterlage> wetterlagen() throws Fehler;
    void speichern(Daten.Wetterlage w) throws Fehler;
    void loeschenWetterlage(String name) throws Fehler;

    List<Daten.Regiebuch> regiebuecher() throws Fehler;
    void speichern(Daten.Regiebuch r) throws Fehler;
    void loeschenRegiebuch(String name) throws Fehler;

    List<Daten.Fahrt> fahrten() throws Fehler;
    void speichern(Daten.Fahrt f) throws Fehler;
    void loeschenFahrt(String name) throws Fehler;

    /** Ansichten ohne Zustand und Vorschau (für Listen), neueste zuerst. */
    List<Daten.Ansicht> ansichten() throws Fehler;
    /** Eine Ansicht ganz, mit Zustand und Vorschau. */
    Daten.Ansicht ansicht(long id) throws Fehler;
    /** Speichert eine neue Ansicht und gibt ihre Nummer zurück. */
    long speichern(Daten.Ansicht a, Long sitzung) throws Fehler;
    void loeschenAnsicht(long id) throws Fehler;

    /** Presets eines Package (paket = „com.dan.ocean“, klasse = „Ocean“); null für alle. */
    List<Daten.PaketPreset> presets(String paket, String klasse) throws Fehler;
    void speichern(Daten.PaketPreset p) throws Fehler;
    void loeschenPreset(String paket, String klasse, String name) throws Fehler;

    /** Neue Sitzung; gibt ihre Nummer zurück. */
    long sitzungBeginnen(String version, String gpu, String qualitaet) throws Fehler;
    /** Zustand der laufenden Sitzung sichern (F39: alle zwei Minuten und beim Ende). */
    void sitzungSichern(long id, String zustand, long szenenzeitMs, String turm, String regiebuch, double regieStunde) throws Fehler;
    void sitzungBeenden(long id) throws Fehler;
    /** Die letzte Sitzung mit Zustand vor der laufenden (oder null). */
    Daten.Sitzung letzteSitzung(long ausser) throws Fehler;

    void protokoll(List<Daten.LogZeile> zeilen, Long sitzung) throws Fehler;
    List<Daten.LogZeile> letzteZeilen(int n) throws Fehler;

    void schliessen();
}
