package com.dan.flock.demo;

import com.dan.flock.Flock;
import com.dan.flock.GullMesh;
import com.dan.flock.gl.FlockGl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Prüfung des Packages ohne Fenster (Paketvertrag Punkt 10). Endet mit „Alles in Ordnung.“. Geprüft werden das
 * Aufwindfeld, Stehen im Aufwind, Grenzen der Geschwindigkeit, nie im Turm und nie im Wasser, die Schlagfrequenz,
 * Landen und Sitzen mit dem Kopf im Wind, Nacht, Lee im Sturm, Wegfliegen im Orkan, Rufe und Reproduzierbarkeit.
 */
public final class FlockCheck {
    private final List<String> lines = new ArrayList<>();
    private int bad;

    public static void main(String[] args) {
        for (String l : new FlockCheck().run()) System.out.println(l);
    }

    private void check(boolean ok, String what) {
        lines.add((ok ? "  ok    " : "  FEHLER") + " " + what);
        if (!ok) bad++;
    }

    private static String f(String fmt, Object... a) { return String.format(Locale.GERMAN, fmt, a); }

    static Flock scene(int bft, long seed) {
        Flock f = Flock.beaufort(bft);
        f.seed = seed;
        f.windFromDeg = 270;
        f.obstacles.add(new Flock.Obstacle(0, 0, FlockScene.TOWER_R, -2, FlockScene.TOWER_H));
        for (int i = 0; i < 10; i++) {
            double a = 2 * Math.PI * i / 10;
            f.perches.add(new Flock.Perch("Geländer " + i, Math.sin(a) * FlockScene.GALLERY_R, FlockScene.RAIL_Y, -Math.cos(a) * FlockScene.GALLERY_R));
        }
        f.homeRadius = 140;
        return f;
    }

    public List<String> run() {
        lines.clear();
        bad = 0;
        // ------------------------------------------------ Aufwind
        Flock f = scene(5, 1);
        double upLuv = f.updraft(-6, 15, 0), upLee = f.updraft(6, 15, 0), upSide = f.updraft(0, 15, 6), upFar = f.updraft(-40, 15, 0), upHigh = f.updraft(-6, 45, 0);
        check(upLuv > 4 && upLee == 0 && upSide < 0.1 && upFar < 0.3 && upHigh < upLuv * 0.01,
                f("Aufwind bei %.1f m/s aus West: Luv 2,5 m vor der Wand %.1f m/s, Lee %.1f, querab %.2f, 36 m vor der Wand %.2f, 17 m über der Kuppel %.3f", f.windSpeed, upLuv, upLee, upSide, upFar, upHigh));

        // ------------------------------------------------ Bft 5: 10 min Flug
        f.step(0.01);
        double minAir = 1e9, maxAir = 0, minClear = 1e9, minAlt = 1e9;
        int soarSamples = 0, soarStill = 0, maxSoar = 0;
        long flapFrames = 0;
        double phaseAdvance = 0;
        for (int i = 0; i < 18000; i++) {
            double[] before = new double[f.gulls().size()];
            for (int k = 0; k < before.length; k++) before[k] = f.gulls().get(k).wingPhase;
            f.step(1 / 30.0);
            int soar = 0;
            for (int k = 0; k < f.gulls().size(); k++) {
                Flock.Gull g = f.gulls().get(k);
                if (g.state == Flock.State.AWAY || g.state == Flock.State.PERCH) continue;
                double air = g.airspeed;
                if (g.state == Flock.State.FLY) { minAir = Math.min(minAir, air); maxAir = Math.max(maxAir, air); }
                double d = Math.hypot(g.pos[0], g.pos[2]);
                if (g.pos[1] < FlockScene.TOWER_H) minClear = Math.min(minClear, d - FlockScene.TOWER_R);
                minAlt = Math.min(minAlt, g.pos[1]);
                if (g.state == Flock.State.SOAR) {
                    soar++;
                    soarSamples++;
                    if (g.groundSpeed() < 2.5) soarStill++;
                }
                if (g.flap > 0.3) {
                    flapFrames++;
                    double dp = g.wingPhase - before[k];
                    if (dp < 0) dp += 2 * Math.PI;
                    phaseAdvance += dp;
                }
            }
            maxSoar = Math.max(maxSoar, soar);
        }
        double hz = flapFrames > 0 ? phaseAdvance / (2 * Math.PI) / (flapFrames / 30.0) : 0;
        check(minAir >= Flock.V_MIN - 0.05 && maxAir <= Flock.V_MAX + 0.05, f("Fluggeschwindigkeit gegen die Luft %.1f bis %.1f m/s (Grenzen %.0f und %.0f)", minAir, maxAir, Flock.V_MIN, Flock.V_MAX));
        check(minClear >= 0.2 && minAlt >= 0.79, f("nie im Turm (kleinster Abstand zur Wand %.2f m) und nie im Wasser (tiefste Höhe %.2f m)", minClear, minAlt));
        check(maxSoar >= 2 && soarSamples > 0 && soarStill / (double) soarSamples > 0.5,
                f("im Aufwind stehen bis zu %d Möwen zugleich, %.0f %% der Zeit davon mit weniger als 2,5 m/s über Grund", maxSoar, 100.0 * soarStill / Math.max(1, soarSamples)));
        check(Math.abs(hz - Flock.FLAP_HZ) < 0.05, f("Flügelschlag %.2f Hz (Silbermöwe rund 3,1 Hz)", hz));
        int perched = f.census()[Flock.State.PERCH.ordinal()];
        boolean facing = true, onSpot = true;
        for (Flock.Gull g : f.gulls())
            if (g.state == Flock.State.PERCH) {
                double d = Math.abs(((g.facing - f.windFromDeg) % 360 + 540) % 360 - 180);
                facing &= d < 30;
                double[] p = g.perch().spot.position();
                onSpot &= Math.abs(g.pos[0] - p[0]) + Math.abs(g.pos[1] - p[1]) + Math.abs(g.pos[2] - p[2]) < 1e-9 && g.groundSpeed() == 0;
            }
        check(perched >= 1 && facing && onSpot && f.calls() > 20,
                f("nach 10 min sitzen %d auf dem Geländer, alle mit dem Kopf im Wind und still; %d Rufe", perched, f.calls()));

        // ------------------------------------------------ Nacht
        Flock n = scene(3, 2);
        n.step(0.01);
        for (int i = 0; i < 3000; i++) n.step(1 / 30.0);
        n.ambientLux = 5;
        long callsBefore = n.calls();
        for (int i = 0; i < 30 * 600; i++) n.step(1 / 30.0);
        int[] c = n.census();
        int flying = c[Flock.State.FLY.ordinal()] + c[Flock.State.SOAR.ordinal()] + c[Flock.State.LAND.ordinal()] + c[Flock.State.LEAVE.ordinal()] + c[Flock.State.TAKEOFF.ordinal()];
        check(flying == 0 && c[Flock.State.PERCH.ordinal()] >= 5 && n.calls() - callsBefore <= c[Flock.State.PERCH.ordinal()] + 2,
                f("nachts nach 10 min: %d fliegen, %d schlafen am Turm, %d sind zur Küste geflogen; danach still", flying, c[Flock.State.PERCH.ordinal()], c[Flock.State.AWAY.ordinal()]));

        // ------------------------------------------------ Sturm: Lee, Orkan: weg
        Flock s = scene(9, 3);
        s.step(0.01);
        int soarS = 0;
        for (int i = 0; i < 30 * 300; i++) {
            s.step(1 / 30.0);
            if (i > 30 * 60) for (Flock.Gull g : s.gulls()) if (g.state == Flock.State.SOAR) soarS++;
        }
        int[] cs = s.census();
        boolean allLee = true;
        for (Flock.Gull g : s.gulls()) if (g.state == Flock.State.PERCH) allLee &= g.pos[0] > 1;
        check(soarS == 0 && cs[Flock.State.PERCH.ordinal()] >= 2 && cs[Flock.State.PERCH.ordinal()] + cs[Flock.State.AWAY.ordinal()] == s.gulls().size() && allLee,
                f("Bft 9 aus West: nach 5 min sitzen %d im Lee auf dem Geländer (Ostseite), %d sind zur Küste, keine steht im Aufwind", cs[Flock.State.PERCH.ordinal()], cs[Flock.State.AWAY.ordinal()]));
        Flock o = scene(11, 4);
        o.step(0.01);
        for (int i = 0; i < 30 * 600; i++) o.step(1 / 30.0);
        int[] co = o.census();
        check(co[Flock.State.AWAY.ordinal()] == o.gulls().size(), f("Bft 11: nach 10 min sind alle %d weg", co[Flock.State.AWAY.ordinal()]));

        // ------------------------------------------------ seed
        Flock a = scene(5, 7), b = scene(5, 7), d = scene(5, 8);
        for (int i = 0; i < 900; i++) { a.step(1 / 30.0); b.step(1 / 30.0); d.step(1 / 30.0); }
        check(a.gulls().get(3).pos[0] == b.gulls().get(3).pos[0] && a.gulls().get(3).pos[0] != d.gulls().get(3).pos[0], "gleicher seed, gleicher Flug; anderer seed, anderer");

        // ------------------------------------------------ Flügel
        Flock.Gull g = a.gulls().get(0);
        g.fold = 1;
        double[] folded = FlockGl.tipCpu(GullMesh.wing(g));
        g.fold = 0;
        g.flap = 0;
        double[] spread = FlockGl.tipCpu(GullMesh.wing(g));
        check(spread[0] > 0.6 && Math.abs(folded[0]) < 0.25 && folded[2] > 0.3,
                f("Flügel: ausgebreitet reicht die Spitze %.2f m zur Seite (Spannweite gut 1,4 m), angelegt liegt sie %.2f m hinter der Schulter am Rumpf", spread[0], folded[2]));

        lines.add(bad == 0 ? "Alles in Ordnung." : bad + " Prüfungen fehlgeschlagen.");
        return lines;
    }

    public int failures() { return bad; }
}
