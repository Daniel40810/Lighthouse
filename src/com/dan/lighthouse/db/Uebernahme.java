package com.dan.lighthouse.db;

/**
 * „In die Datenbank übernehmen“ (F40 A): Was ohne Datenbank in {@code daten/} gespeichert wurde, kommt in die
 * Tabellen. Übernommen werden die eigenen Einträge (Wetterlagen, Regiebücher, Kamerafahrten, Presets) und alle
 * Ansichten; die Dateien bleiben liegen. Gleichnamige eigene Einträge in der Datenbank werden ersetzt.
 */
public final class Uebernahme {
    private Uebernahme() { }

    /** Gibt eine Zeile mit den Zahlen zurück. */
    public static String dateienInDb(DateiSpeicher von, Speicher nach) throws Speicher.Fehler {
        int w = 0, r = 0, f = 0, p = 0, a = 0;
        for (Daten.Wetterlage x : von.wetterlagen()) if (Daten.EIGEN.equals(x.herkunft())) { nach.speichern(x); w++; }
        for (Daten.Regiebuch x : von.regiebuecher()) if (Daten.EIGEN.equals(x.herkunft())) { nach.speichern(x); r++; }
        for (Daten.Fahrt x : von.fahrten()) if (Daten.EIGEN.equals(x.herkunft())) { nach.speichern(x); f++; }
        for (Daten.PaketPreset x : von.presets(null, null)) if (Daten.EIGEN.equals(x.herkunft())) { nach.speichern(x); p++; }
        for (Daten.Ansicht x : von.ansichten()) { nach.speichern(von.ansicht(x.id()), null); a++; }
        return w + " Wetterlagen, " + r + " Regiebücher, " + f + " Kamerafahrten, " + p + " Presets und " + a + " Ansichten übernommen";
    }
}
