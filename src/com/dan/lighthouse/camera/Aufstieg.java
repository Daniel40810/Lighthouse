package com.dan.lighthouse.camera;

import com.dan.lighthouse.scene.Innenraum;
import com.dan.lighthouse.scene.Turm;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Kamerafahrt „Aufstieg“ (Phase 11): vom Podest vor der Tür hinein, ein Blick hinauf ins Treppenauge, dann die
 * 59 Stufen im Uhrzeigersinn hinauf (in Augenhöhe 1,65 m über der Stufe, in der Mitte der Laufbreite), im Dienstraum
 * zur Schiffsleiter, durch die Luke in den Laternenraum und dort zur Linse. Die Fahrt besteht aus Stützpunkten
 * (Zeit, Ort, Blickrichtung, Neigung), zwischen denen ein Catmull-Rom-Spline weich verbindet; an der Tür gibt es einen
 * harten Schnitt.
 */
public final class Aufstieg {
    /** Ein Stützpunkt; cut = hier beginnt eine neue Einstellung (kein Übergang vom vorigen Punkt). */
    public record Key(double t, double x, double y, double z, double yaw, double pitch, boolean cut) { }

    public static final double EYE = 1.65, WALK_R = 1.68;
    private final List<Key> keys = new ArrayList<>();
    /** Phase 15: Name der Fahrt und ob sie im Kreis läuft (Rundflug). */
    public final String name;
    public final boolean loop;

    public Aufstieg() { name = "Aufstieg"; loop = false; build(); }

    /** Phase 15: eine Fahrt aus gespeicherten Stützpunkten (LH_CAMERA_POINT); mindestens zwei. */
    public Aufstieg(String name, List<Key> keys, boolean loop) {
        if (keys.size() < 2) throw new IllegalArgumentException("Eine Kamerafahrt braucht mindestens zwei Stützpunkte.");
        this.name = name;
        this.loop = loop;
        this.keys.addAll(keys);
    }

    /**
     * Phase 15: Rundflug um den Turm, 36 Stützpunkte auf einem Kreis mit dem Blick zum Turm, gegen den Uhrzeigersinn
     * gesehen von oben, Höhe und Abstand atmen sacht. Eine Runde in {@code seconds}.
     */
    public static Aufstieg rundflug(double radius, double height, double seconds) {
        List<Key> l = new ArrayList<>();
        int n = 36;
        for (int i = 0; i <= n; i++) {
            double f = i / (double) n, az = 360 * f;
            double r = radius * (1 + 0.12 * Math.sin(f * 4 * Math.PI)), y = height * (1 + 0.25 * Math.sin(f * 2 * Math.PI + 1));
            double[] p = polar(r, az, y);
            double look = az + 180;                 // zum Turm
            double pitch = Math.toDegrees(Math.atan2(y - 16, r));
            l.add(new Key(seconds * f, p[0], p[1], p[2], look, pitch, false));
        }
        return new Aufstieg("Rundflug", l, true);
    }

    public double duration() { return keys.get(keys.size() - 1).t(); }

    public List<Key> keys() { return keys; }

    /** Auf der Treppe: Ort beim Fortschritt s (Stufen, unaufgewickelt), Blick die Treppe hinauf und etwas ins Auge. */
    static double[] onStair(double s, double look) {
        double az = Innenraum.START_AZ + Innenraum.STEP_DEG * (s + 0.5);
        double a = Math.toRadians(az);
        double y = Innenraum.FLOOR_Y + Innenraum.RISE * (s + 1) + EYE;
        return new double[]{Math.sin(a) * WALK_R, y, -Math.cos(a) * WALK_R, az + 90 + look};
    }

    private void add(double t, double[] p, double yaw, double pitch, boolean cut) { keys.add(new Key(t, p[0], p[1], p[2], yaw, pitch, cut)); }

    private static double[] polar(double r, double az, double y) {
        double a = Math.toRadians(az);
        return new double[]{Math.sin(a) * r, y, -Math.cos(a) * r};
    }

    private void build() {
        double door = Turm.DOOR_AZ, pod = Turm.SOCKEL_TOP + EYE;
        // 1 draußen auf dem Podest, auf die Tür zu
        add(0, polar(13, door, pod + 1.2), door + 180, -8, false);
        add(2, polar(8.5, door, pod + 0.4), door + 180, -4, false);
        add(4, polar(4.7, door, pod), door + 180, 0, false);
        // 2 Schnitt: drinnen hinter der Tür, Blick hinauf ins Treppenauge
        double in0 = Innenraum.FLOOR_Y + EYE;
        add(4.001, polar(2.15, door, in0), door + 180, -5, true);
        add(6.0, polar(1.5, door + 2, in0), door + 180 - 4, -50, false);
        add(8.0, polar(1.0, door + 6, in0), door + 180 - 8, -86, false);
        add(10.0, polar(1.5, door + 16, in0 + 0.05), door + 180 - 45, -25, false);
        // 3 die Treppe hinauf: 59 Stufen in 40 s; der Blick schwingt sacht zwischen Auge und Wand
        double t0 = 12, t1 = 52;
        double[] st = onStair(0, 20);
        add(t0, st, st[3], -2, false);
        int n = 80;
        for (int i = 1; i <= n; i++) {
            double f = i / (double) n, s = (Innenraum.STEPS - 0.5) * f;
            double look = 28 + 16 * Math.sin(f * Math.PI * 5);
            double pitch = 4 + 8 * Math.sin(f * Math.PI * 3 + 1);
            double[] q = onStair(s, look);
            add(t0 + (t1 - t0) * f, q, q[3], pitch, false);
        }
        // 4 im Dienstraum: von der letzten Stufe auf den Boden, zur Schiffsleiter umdrehen
        double last = keys.get(keys.size() - 1).yaw();
        double room = Innenraum.ROOM_FLOOR + EYE, ha = Innenraum.HATCH_AZ;
        double face = ha + 180;                      // zur Achse hin
        while (face < last - 180) face += 360;
        while (face > last + 180) face -= 360;
        double[] q = onStair(Innenraum.STEPS + 0.6, 10);
        q[1] = room;
        add(t1 + 1.5, q, last + 10, -10, false);
        add(t1 + 3.5, polar(2.15, ha - 10, room), (last + face) / 2, -20, false);
        add(t1 + 5.0, polar(2.2, ha, room), face, -50, false);
        // 5 die Leiter hinauf: das Auge 0,4 m hinter den Tritten, durch die Luke
        double t = t1 + 6.0;
        for (int i = 0; i <= 6; i++) {
            double feet = Innenraum.ROOM_FLOOR + (Innenraum.LANTERN_FLOOR - Innenraum.ROOM_FLOOR) * i / 6.0;
            double ye = feet + EYE - 0.05, r = Math.max(Innenraum.HATCH_R0 + 0.42, ladderR(Math.min(ye - 0.25, Innenraum.LANTERN_FLOOR)) + 0.42);
            add(t + 1.2 * i, polar(r, ha, ye), face, -55 + 57 * i / 6.0, false);
        }
        // 6 im Laternenraum: zur Linse umsehen
        t += 7.2 + 1.5;
        double lr = Innenraum.HATCH_R0 + 0.45, ly = Innenraum.LANTERN_FLOOR + EYE;
        add(t, polar(lr + 0.05, ha + 18, ly), face + 40, 4, false);
        add(t + 3, polar(lr + 0.15, ha + 40, ly), face + 110, 6, false);
        add(t + 6, polar(lr + 0.25, ha + 60, ly), face + 200, 2, false);
        add(t + 9, polar(lr + 0.3, ha + 70, ly), face + 235, 1, false);
    }

    static double ladderR(double y) {
        return Innenraum.LADDER_FOOT_R + (Innenraum.HATCH_R0 + 0.04 - Innenraum.LADDER_FOOT_R) * (y - Innenraum.ROOM_FLOOR) / (Innenraum.LANTERN_FLOOR - Innenraum.ROOM_FLOOR);
    }

    /** Stand zur Zeit t: {x, y, z, yaw, pitch} (Catmull-Rom innerhalb einer Einstellung). */
    public double[] sample(double t) {
        t = Math.max(0, Math.min(duration(), t));
        int i = 0;
        while (i + 1 < keys.size() - 1 && keys.get(i + 1).t() <= t) i++;
        Key k1 = keys.get(i), k2 = keys.get(Math.min(keys.size() - 1, i + 1));
        if (k2.cut() && t < k2.t()) { /* noch in der alten Einstellung */ }
        Key k0 = i > 0 && !k1.cut() ? keys.get(i - 1) : k1;
        Key k3 = i + 2 < keys.size() && !keys.get(i + 2).cut() ? keys.get(i + 2) : k2;
        if (k2.cut()) { k2 = k1; k3 = k1; }
        double u = k2.t() > k1.t() ? (t - k1.t()) / (k2.t() - k1.t()) : 0;
        double[][] v = {vec(k0), vec(k1), vec(k2), vec(k3)};
        double[] out = new double[5];
        for (int c = 0; c < 5; c++) out[c] = cr(v[0][c], v[1][c], v[2][c], v[3][c], u);
        return out;
    }

    private static double[] vec(Key k) { return new double[]{k.x(), k.y(), k.z(), k.yaw(), k.pitch()}; }

    private static double cr(double p0, double p1, double p2, double p3, double u) {
        double u2 = u * u, u3 = u2 * u;
        return 0.5 * (2 * p1 + (-p0 + p2) * u + (2 * p0 - 5 * p1 + 4 * p2 - p3) * u2 + (-p0 + 3 * p1 - 3 * p2 + p3) * u3);
    }
}
