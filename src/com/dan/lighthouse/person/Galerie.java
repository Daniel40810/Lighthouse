package com.dan.lighthouse.person;

import com.dan.avatar.Climate;
import com.dan.avatar.World;
import com.dan.lighthouse.scene.Turm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Die Galerie des Roten Sands als Umwelt für Personen (Avatar Phase 15, F22 A): ein Ring um die Laterne in 23 m Höhe.
 * Boden, Orte und das Klima am Ort; die Kamera setzt {@link Personen}. Richtungen wie im ganzen Projekt: Azimut von
 * Nord im Uhrzeigersinn, Nord = −z, Ost = +x.
 *
 * <ul>
 * <li><b>Boden:</b> die Galerie auf {@link Turm#GALLERY_TOP} zwischen der Sockelwand der Laterne und dem Geländer.</li>
 * <li><b>Weg:</b> ein Kreis mit {@link #WEG_R} = 2,6 m; Orte {@code G0} … {@code G330} alle 30°. Zwischen zwei Orten mit
 * höchstens 40° Abstand geht die Person gerade, ohne die Laterne (2,0 m) zu streifen; die Solarpaneele (150°–210°,
 * ab 3,0 m) und der Mast (75°, 3,95 m) liegen außerhalb.</li>
 * <li><b>Reling:</b> {@code Reling_<Azimut>} 3,8 m von der Achse (nicht über den Paneelen und nicht am Mast).</li>
 * <li><b>Nach dem Wind:</b> {@code Luv} und {@code Lee} auf dem Weg, {@code Reling_Luv}, {@code See} (300 m hinaus in den
 * Wind, auf Augenhöhe), {@code Feuer} (die Linse), {@code Kimm}.</li>
 * <li><b>Hoher Erker:</b> der Erker im Nordosten (45°) ragt mit seiner Kuppel 1,4 m über die Galerie; um ihn herum führt kein
 * Weg. Orte auf dem Weg, die in ihn fielen, rücken an seinen Rand; {@code rund} geht auf der anderen Seite herum.</li>
 * <li><b>Klima:</b> Wind und Regen der Szene; im Lee der Laterne nur gut ein Drittel davon (Windschatten [G]).</li>
 * </ul>
 */
public final class Galerie implements World {

    /** Halbmesser des Wegs um die Laterne (m). */
    public static final double WEG_R = 2.6, RELING_R = 3.8;
    /** Sockelwand der Laterne (m), dazu eine Schulterbreite. */
    public static final double WAND_R = Turm.LANTERN_R + 0.15;

    /** Der hohe Erker (mit eigener Kuppel bis 24,4 m): Azimut, Abstand der Mitte von der Achse, Halbmesser (m). */
    public static final double ERKER_AZ = Turm.ERKER_AZ[2], ERKER_D = Turm.ERKER_D, ERKER_R = Turm.ERKER_R, ERKER_TOP = Turm.ERKER_HIGH_TOP;

    /** Liegt der Punkt neben dem hohen Erker (mit einer Schulterbreite Luft)? */
    public static boolean frei(double x, double z) {
        double[] e = at(ERKER_AZ, ERKER_D, 0);
        return Math.hypot(x - e[0], z - e[2]) > ERKER_R + 0.35;
    }

    /** Nächster Azimut auf dem Kreis r, der neben dem Erker liegt (2°-Schritte vom Wunsch weg). */
    public static double freiAz(double az, double r) {
        for (int d = 0; d <= 180; d += 2) {
            double[] p = at(az + d, r, 0), q = at(az - d, r, 0);
            if (frei(p[0], p[2])) return az + d;
            if (frei(q[0], q[2])) return az - d;
        }
        return az;
    }

    /** Wind der Szene: Richtung, aus der er kommt (Grad), Geschwindigkeit (m/s), Böen 0..1. */
    public volatile double windFromDeg = 250, windSpeed = 8;
    /** Regen (mm/h), Temperatur (°C), Feuchte 0..1. */
    public volatile double rain, tempC = 10, humidity = 0.85;
    /** Auge der Kamera (Welt) oder null. */
    public volatile double[] eye;

    @Override public double ground(double x, double z) { return Turm.GALLERY_TOP; }

    @Override public double[] camera() { double[] e = eye; return e == null ? null : e.clone(); }

    /** Punkt in Azimut {@code az} (Grad) und Abstand {@code r} von der Achse, Höhe {@code y}. */
    public static double[] at(double az, double r, double y) {
        double a = Math.toRadians(az);
        return new double[]{Math.sin(a) * r, y, -Math.cos(a) * r};
    }

    /** Azimut (Grad 0..360) eines Punkts. */
    public static double azimuth(double x, double z) {
        double a = Math.toDegrees(Math.atan2(x, -z));
        return a < 0 ? a + 360 : a;
    }

    /** Darf an diesem Azimut an der Reling gestanden werden (nicht über den Paneelen, nicht am Mast)? */
    public static boolean relingFrei(double az) {
        double a = ((az % 360) + 360) % 360;
        double[] p = at(a, RELING_R, 0);
        return !(a > 135 && a < 225) && Math.abs(a - Turm.MAST_AZ) > 14 && frei(p[0], p[2]);
    }

    @Override public double[] place(String name) {
        if (name == null) return null;
        String n = name.strip();
        String k = n.toLowerCase(Locale.ROOT);
        double y = Turm.GALLERY_TOP;
        if (k.matches("g\\d{1,3}")) return at(freiAz(Double.parseDouble(n.substring(1)), WEG_R), WEG_R, y);
        if (k.startsWith("reling_") && k.substring(7).matches("\\d{1,3}")) {
            double az = Double.parseDouble(k.substring(7));
            return relingFrei(az) ? at(az, RELING_R, y) : null;
        }
        return switch (k) {
            case "luv" -> at(freiAz(windFromDeg, WEG_R), WEG_R, y);
            case "lee" -> at(freiAz(windFromDeg + 180, WEG_R), WEG_R, y);
            case "reling_luv" -> at(relingNah(windFromDeg), RELING_R, y);
            case "see" -> at(windFromDeg, 300, y + 1.0);
            case "kimm" -> at(windFromDeg, 15_000, 0);
            case "feuer", "linse" -> new double[]{0, Turm.LENS_Y, 0};
            case "turm" -> new double[]{0, y + 1.5, 0};
            default -> null;
        };
    }

    /** Nächster Azimut an der Reling, der frei ist (in 5°-Schritten vom Wunsch weg). */
    static double relingNah(double az) {
        for (int d = 0; d <= 180; d += 5) {
            if (relingFrei(az + d)) return az + d;
            if (relingFrei(az - d)) return az - d;
        }
        return az;
    }

    @Override public List<String> places() {
        List<String> l = new ArrayList<>();
        for (int a = 0; a < 360; a += 30) l.add("G" + a);
        l.addAll(List.of("Luv", "Lee", "Reling_Luv", "See", "Kimm", "Feuer", "Turm"));
        for (int a = 0; a < 360; a += 30) if (relingFrei(a)) l.add("Reling_" + a);
        return l;
    }

    /**
     * Liegt der Weg von a nach b frei (Abstand der Strecke zur Achse über der Laternenwand plus 0,25 m Schulter,
     * außerhalb der Paneele)?
     */
    public static boolean wegFrei(double[] a, double[] b) {
        double dx = b[0] - a[0], dz = b[2] - a[2], l2 = dx * dx + dz * dz;
        double t = l2 < 1e-9 ? 0 : Math.max(0, Math.min(1, -(a[0] * dx + a[2] * dz) / l2));
        double px = a[0] + t * dx, pz = a[2] + t * dz;
        double[] e = at(ERKER_AZ, ERKER_D, 0);
        double ex = e[0] - a[0], ez = e[2] - a[2], te = l2 < 1e-9 ? 0 : Math.max(0, Math.min(1, (ex * dx + ez * dz) / l2));
        boolean erker = frei(a[0] + te * dx, a[2] + te * dz);
        return erker && Math.hypot(px, pz) >= WAND_R + 0.22 && Math.hypot(a[0], a[2]) <= Turm.GALLERY_R - 0.3 && Math.hypot(b[0], b[2]) <= Turm.GALLERY_R - 0.3;
    }

    /**
     * Klima am Ort (Wind in Weltrichtung, die Szene rechnet ihn für die Person um): Wind der Szene um die Höhe
     * verstärkt (Galerie 23 m statt 10 m, Potenzgesetz mit 0,11 über See), Regen; im Lee der Laterne Schutz.
     */
    @Override public Climate climateAt(double x, double y, double z) {
        Climate c = new Climate();
        double lee = lee(x, z);
        double v = windSpeed * Math.pow(Math.max(1, y) / 10.0, 0.11) * (1 - 0.62 * lee);
        double a = Math.toRadians(windFromDeg + 180);
        c.wind = new double[]{Math.sin(a) * v, 0, -Math.cos(a) * v};
        c.rain = rain * (1 - 0.55 * lee);
        c.tempC = tempC;
        c.humidity = humidity;
        return c;
    }

    /** Wie sehr der Ort im Windschatten der Laterne liegt (0 Luv … 1 genau Lee, weich über ±60°) [G]. */
    public double lee(double x, double z) {
        double r = Math.hypot(x, z);
        if (r < 1e-6 || r > Turm.GALLERY_R + 0.5) return 0;
        double d = Math.abs(((azimuth(x, z) - (windFromDeg + 180)) % 360 + 540) % 360 - 180);
        double t = Math.max(0, Math.min(1, (75 - d) / 60));
        return t * t * (3 - 2 * t) * Math.max(0, Math.min(1, (4.6 - r) / 1.6));
    }

    /** Für Bedienfeld und Prüfung. */
    public Map<String, String> beschreibung() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("Boden", String.format(Locale.GERMANY, "%.1f m", Turm.GALLERY_TOP));
        m.put("Weg", String.format(Locale.GERMANY, "Kreis %.1f m um die Laterne", WEG_R));
        m.put("Wind", String.format(Locale.GERMANY, "aus %.0f° mit %.1f m/s", windFromDeg, windSpeed));
        return m;
    }
}
