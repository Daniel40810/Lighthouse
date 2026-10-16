package com.dan.lighthouse.scene;

import com.dan.lighthouse.wiring.WaterLevel;
import com.dan.sky.astro.FAstroTime;
import com.dan.sky.astro.FMoonPosition;
import com.dan.sky.astro.FSun;

/**
 * Die Gezeiten am Roten Sand (F13 = A, Phase 5): Wasserstand in m über mittlerem Meeresspiegel aus dem Stand von Mond
 * und Sonne, gerechnet mit dem Kern aus FMoon (über {@code com.dan.sky.astro}). Drei Teiltiden reichen für das, was
 * man am Sockel sieht:
 * <ul>
 * <li>M2 (Mond, halbtägig): 1,32 m Amplitude. Hochwasser folgt dem Durchgang des Mondes mit dem Mondflutintervall.</li>
 * <li>S2 (Sonne, halbtägig): 0,34 m. Mit 54° Phasenvorsprung kommen die Springtiden gut zwei Tage nach Voll- und
 * Neumond (das „Alter der Gezeit“ der Deutschen Bucht).</li>
 * <li>M4 (Flachwasser): 0,12 m. Macht die Kurve unsymmetrisch: Das Hochwasser ist flacher als das Niedrigwasser,
 * die Ebbe dauert gut sechs Stunden.</li>
 * </ul>
 * Tidenhub damit 2,0 m bei Nipp, 3,3 m bei Spring, im Mittel rund 3 m (Werkbuch, Abschnitt 3). Das Mondflutintervall
 * ist so geeicht, dass das Hochwasser eine Stunde vor Bremerhaven Alter Leuchtturm liegt [G]: am 17.10.2026 um
 * 04:15 MESZ statt 05:17. Ein Gezeitenrechner für die Schifffahrt ist das nicht; für Algenlinie, nasse Zone und
 * Caisson-Plattform, die bei Niedrigwasser rund 1,5 m aus dem Wasser ragt, reicht es.
 */
public final class Tide implements WaterLevel {
    public static final double A_M2 = 1.32, A_S2 = 0.34, A_M4 = 0.12;
    /** Phasen in Grad (gegen den Durchgang am Ort); G_M2 aus der Eichung, G_S2 = G_M2 + 54°. */
    public static final double G_M2 = 316.0, G_S2 = G_M2 + 54.0, G_M4 = 2 * G_M2 + 150.0;

    private final SceneClockSource clock;
    /** Aus: Wasserstand steht auf {@link #fixed}. */
    public volatile boolean on = true;
    public volatile double fixed = 0;
    private volatile double cacheMs = Double.NaN, cacheLevel;

    /** Woher die Zeit kommt (ms seit 1970, UTC). */
    @FunctionalInterface
    public interface SceneClockSource { double epochMillis(); }

    public Tide(SceneClockSource clock) { this.clock = clock; }

    @Override
    public double level() {
        if (!on) return fixed;
        double ms = clock.epochMillis();
        // Mond und Sonne ändern sich in 20 s Szenenzeit um weniger als 0,1 mm Wasserstand
        if (Math.abs(ms - cacheMs) < 20_000) return cacheLevel;
        cacheLevel = at(ms);
        cacheMs = ms;
        return cacheLevel;
    }

    /** Wasserstand in m über MSL zu einer Zeit (ms seit 1970, UTC). */
    public static double at(double epochMs) {
        double jdUt = FAstroTime.julianDayFromMillis((long) epochMs);
        double jde = jdUt + FAstroTime.deltaTDays(jdUt);
        double lst = FAstroTime.meanSiderealTime(jdUt) + Site.LON;
        double hm = Math.toRadians(lst - FMoonPosition.at(jde).rightAscension);
        double hs = Math.toRadians(lst - FSun.at(jde).rightAscension);
        return A_M2 * Math.cos(2 * hm - Math.toRadians(G_M2))
                + A_S2 * Math.cos(2 * hs - Math.toRadians(G_S2))
                + A_M4 * Math.cos(4 * hm - Math.toRadians(G_M4));
    }

    /** Nächstes Hoch- oder Niedrigwasser ab epochMs: {Zeit in ms, Stand in m}; high = Hochwasser. */
    public static double[] next(double epochMs, boolean high) {
        double step = 5 * 60_000;
        double t = epochMs, prev = at(t), cur = at(t + step);
        for (int i = 0; i < 400; i++) {
            double nx = at(t + 2 * step);
            boolean ext = high ? (cur >= prev && cur > nx) : (cur <= prev && cur < nx);
            if (ext) {
                // auf die Minute verfeinern (Parabel durch drei Punkte)
                double d = (prev - nx) / (2 * (prev - 2 * cur + nx));
                double tm = t + step + d * step;
                return new double[]{tm, at(tm)};
            }
            t += step;
            prev = cur;
            cur = nx;
        }
        return new double[]{Double.NaN, Double.NaN};
    }
}
