package com.dan.lighthouse.db;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Die Datensätze, die {@link Speicher} liest und schreibt (Phase 15, Werkbuch Abschnitt 8). Je Tabelle ein Record mit
 * den Spalten, die die App braucht; ID, ANGELEGT und GEAENDERT führt die Datenbank selbst.
 */
public final class Daten {
    private Daten() { }

    /** HERKUNFT: mitgeliefert (stellt der Einrichter wieder her) oder eigen (fasst er nie an). */
    public static final String MITGELIEFERT = "MITGELIEFERT", EIGEN = "EIGEN";

    /** Ein Sektor (LH_SECTOR); von und bis als Peilung von See, im Uhrzeigersinn. */
    public record Sektor(int nr, double von, double bis, String farbe, String art, Double tragweiteSm, String bezeichnung) {
        /** In der Schreibweise von {@code SectorTable.parse}: „W 324-328 9 Leitsektor“. */
        public String alsText() {
            StringBuilder b = new StringBuilder(farbe).append(' ').append(Zahl.text(von)).append('-').append(Zahl.text(bis));
            if (tragweiteSm != null) b.append(' ').append(Zahl.text(tragweiteSm));
            String n = bezeichnung != null && !bezeichnung.isBlank() ? bezeichnung
                    : "LEITSEKTOR".equals(art) ? "Leitsektor" : "SCHWACH".equals(art) ? "Schwachlicht" : "";
            if (!n.isEmpty()) b.append(' ').append(n.replace(',', ' '));
            return b.toString();
        }
    }

    /** Ein Leuchtturm (LH_LIGHTHOUSE mit seinen Sektoren). */
    public record Turm(String name, String revier, double breite, double laenge, double feuerhoeheM, Double turmhoeheM,
                       Double tragweiteSm, String kennung, String optik, Integer baujahr, Integer ausserBetrieb, int standJahr,
                       String farben, String beschreibung, String quelle, String herkunft, List<Sektor> sektoren) {
        public Turm { sektoren = List.copyOf(sektoren); }
        public boolean ostsee() { return "OSTSEE".equals(revier); }
        /** Alle Sektoren in der Schreibweise von {@code SectorTable.parse}. */
        public String sektorenText() {
            StringBuilder b = new StringBuilder();
            for (Sektor s : sektoren) b.append(b.length() == 0 ? "" : ", ").append(s.alsText());
            return b.toString();
        }
        /** Abstand zu einem anderen Turm in km (Großkreis). */
        public double abstandKm(Turm o) { return Zahl.abstandKm(breite, laenge, o.breite, o.laenge); }
        @Override public String toString() { return name; }
    }

    /**
     * Eine Wetterlage (LH_WEATHER_PRESET): je {@code Weather.Var} ein Wert oder NaN. Die Wolkenart steht als Zahl wie
     * in der Regie (Ordinalzahl von {@code Weather.CloudKind}); in der Tabelle als Name.
     */
    public record Wetterlage(String name, Double beaufort, String phase, double[] werte, String beschreibung, String herkunft) {
        public Wetterlage { werte = werte.clone(); }
        @Override public String toString() { return name; }
    }

    /** Ein Regiebuch (LH_SCENARIO mit LH_SCENARIO_STEP) als Text im Format von {@code Scenario.toText}. */
    public record Regiebuch(String name, String text, String beschreibung, String herkunft) {
        @Override public String toString() { return name; }
    }

    /** Ein Stützpunkt einer Kamerafahrt (LH_CAMERA_POINT); Szenenmeter und Grad. */
    public record Stuetzpunkt(int nr, double zeitS, double x, double y, double z, double gier, double nick, double fov) { }

    /** Eine Kamerafahrt (LH_CAMERA_PATH). */
    public record Fahrt(String name, String art, boolean schleife, String beschreibung, String herkunft, List<Stuetzpunkt> punkte) {
        public Fahrt { punkte = List.copyOf(punkte); }
        @Override public String toString() { return name; }
    }

    /** Eine gemerkte Ansicht (LH_SNAPSHOT); vorschau ist ein PNG oder null. */
    public record Ansicht(long id, String name, long szenenzeitMs, double[] kamera, String turm, String zustand, byte[] vorschau) {
        @Override public String toString() { return name; }
    }

    /** Ein benanntes Preset eines Package (LH_PACKAGE_PRESET mit LH_PACKAGE_SETTING). */
    public record PaketPreset(String paket, String klasse, String name, String beschreibung, String herkunft, Map<String, String> felder) {
        public PaketPreset { felder = new LinkedHashMap<>(felder); }
        @Override public String toString() { return name; }
    }

    /** Die letzte Sitzung für „Weiter wie zuletzt“. */
    public record Sitzung(long id, long beginnMs, Long endeMs, boolean sauber, String zustand, Long zustandMs, String turm) { }

    /** Eine Zeile des Protokolls (LH_LOG). */
    public record LogZeile(long zeitMs, String stufe, String quelle, String text, Long szenenzeitMs, String ort, String detail) { }

    /** Was das Startbild über den Stand der Daten zeigt. */
    public record Stand(int tuerme, int wetterlagen, int regiebuecher, int fahrten, int ansichten, int presets, Sitzung letzte) { }
}
