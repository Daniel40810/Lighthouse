package com.dan.lighthouse.db;

import com.dan.beacon.Beacon;
import com.dan.beacon.LightColour;
import com.dan.beacon.SectorTable;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.Site;
import com.dan.lighthouse.wiring.Wiring;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 15: die Leuchtturm-Auswahl (F36 A). Aus einem Datensatz von LH_LIGHTHOUSE wird ein Feuer für
 * {@code com.dan.beacon}; die Nachbarn bis 40 km leuchten am Horizont, an ihrer Lage relativ zum gewählten Turm. Der
 * Turm selbst bleibt der Rote Sand, das Feuer brennt in seiner Laterne.
 */
public final class TurmWahl {
    private TurmWahl() { }

    /** Bis hierher sind Nachbarn zu sehen (km). */
    public static final double NACHBAR_KM = 40;

    /** Das Feuer eines Turms; wirft IllegalArgumentException, wenn Kennung oder Sektoren nicht lesbar sind. */
    public static Beacon feuer(Daten.Turm t) {
        Beacon b = new Beacon();
        b.name = t.name();
        b.optic = "ROTATING".equals(t.optik()) ? Beacon.Optic.ROTATING : Beacon.Optic.DRUM;
        b.lampRadius = 0.016;
        double range = t.tragweiteSm() != null ? t.tragweiteSm() : 10;
        SectorTable st = t.sektoren().isEmpty() ? SectorTable.all(LightColour.W, range) : SectorTable.parse(t.sektorenText(), range);
        b.set(t.kennung(), st);
        double p = b.characteristic().period;
        if (b.optic == Beacon.Optic.ROTATING && p > 0) b.rotationTarget = p;
        return b;
    }

    /** Die Nachbarn eines Turms bis {@link #NACHBAR_KM}, mit Lage in Metern relativ zu ihm. */
    public static List<Daten.Turm> nachbarn(Daten.Turm t, List<Daten.Turm> alle) {
        List<Daten.Turm> l = new ArrayList<>();
        for (Daten.Turm o : alle) if (!o.name().equals(t.name()) && t.abstandKm(o) <= NACHBAR_KM) l.add(o);
        l.sort((a, b) -> Double.compare(t.abstandKm(a), t.abstandKm(b)));
        return l;
    }

    /** Lage von o relativ zu t in Metern {x Ost, z Süd} (örtliche Ebene, für unter 40 km genau genug). */
    public static double[] lage(Daten.Turm t, Daten.Turm o) {
        double x = Math.toRadians(o.laenge() - t.laenge()) * Site.EARTH_R * Math.cos(Math.toRadians(t.breite()));
        double z = -Math.toRadians(o.breite() - t.breite()) * Site.EARTH_R;
        return new double[]{x, z};
    }

    /** Ob das der Rote Sand selbst ist (dann gilt die Szene aus Phase 9 mit der Alten Weser am Horizont). */
    public static boolean heim(Daten.Turm t) { return t.name().equals("Roter Sand"); }

    /** Wählt den Turm in der Szene. Gibt eine Zeile für Protokoll und HUD zurück. */
    public static String waehlen(Renderer r, Daten.Turm t, List<Daten.Turm> alle) {
        Beacon light = feuer(t);
        List<Beacon> nb = new ArrayList<>();
        for (Daten.Turm o : nachbarn(t, alle)) {
            try {
                Beacon b = feuer(o);
                double[] p = lage(t, o);
                b.x = p[0];
                b.z = p[1];
                b.y = o.feuerhoeheM() + (t.ostsee() ? 0 : Wiring.MHW) - Site.curvatureDrop(Math.hypot(p[0], p[1]));
                nb.add(b);
            } catch (RuntimeException e) {
                // ein Nachbar mit unlesbarer Kennung bleibt dunkel
            }
        }
        boolean home = heim(t);
        r.setSite(t.name(), t.breite(), t.laenge(), !t.ostsee(), home, light, nb.toArray(new Beacon[0]));
        return home ? "Roter Sand mit seinem Feuer von 1985" : "Feuer von " + t.name() + " (" + t.kennung() + ") am Turm Roter Sand · "
                + (t.ostsee() ? "Ostsee, ohne Gezeiten" : "Nordsee") + " · " + nb.size() + (nb.size() == 1 ? " Nachbar" : " Nachbarn") + " bis 40 km";
    }
}
