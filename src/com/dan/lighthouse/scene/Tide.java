package com.dan.lighthouse.scene;

import com.dan.lighthouse.wiring.WaterLevel;

/**
 * Die Gezeiten am Roten Sand: Wasserstand in m über dem Mittelwasser. Seit Rungholt Phase 17 aus {@code com.dan.tide}
 * (Rückfluss): zehn Partialtiden des Pegels Bremerhaven Alter Leuchtturm, angepasst an 112 Hoch- und Niedrigwasser
 * (20.10. bis 17.11.2026, nicht amtlich; im Mittel 10 min genau), eine Stunde früher und im Hub auf 3,0 m im Mittel
 * gestreckt ({@code Tide.roterSand()}, [G]). Damit Spring und Nipp, die tägliche Ungleichheit und die ungleich langen
 * Flut- und Ebbzeiten wie an der Weser: Nipp rund 2,0 m, Spring bis 3,8 m. Vorher drei Teiltiden aus dem Stand von
 * Mond und Sonne (M2, S2, M4; Hub 2,0 bis 3,3 m).
 * <p>
 * Die Schnittstelle bleibt: {@link #level()} für die Verdrahtung, {@link #at} und {@link #next} statisch, {@link #on}
 * und {@link #fixed} für den Wasserstand von Hand.
 */
public final class Tide implements WaterLevel {
    /** Der Pegel aus {@code com.dan.tide}. */
    public static final com.dan.tide.Tide PEGEL = com.dan.tide.Tide.roterSand();

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
        // in 20 s Szenenzeit ändert sich der Pegel um weniger als 1 mm
        if (Math.abs(ms - cacheMs) < 20_000) return cacheLevel;
        cacheLevel = at(ms);
        cacheMs = ms;
        return cacheLevel;
    }

    /** Wasserstand in m über dem Mittelwasser zu einer Zeit (ms seit 1970, UTC). */
    public static double at(double epochMs) { return PEGEL.astronomical(epochMs / 3_600_000.0); }

    /** Nächstes Hoch- oder Niedrigwasser ab epochMs: {Zeit in ms, Stand in m}; high = Hochwasser. */
    public static double[] next(double epochMs, boolean high) {
        com.dan.tide.Tide.Turn t = PEGEL.next(epochMs / 3_600_000.0, high);
        return t == null ? new double[]{Double.NaN, Double.NaN} : new double[]{t.hours() * 3_600_000.0, t.level()};
    }
}
