package com.dan.flock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Silbermöwen um einen Turm auf See.
 * <p>
 * <b>Flug:</b> Jede Möwe fliegt mit 8 bis 15 m/s gegen die Luft. Ohne Flügelschlag sinkt sie im Gleitflug um 0,6 m/s,
 * Aufwind trägt sie, Flügelschlag (3,1 Hz) bringt bis 2 m/s Steigen. Sie steuert nach Zielen und nach drei Schwarmregeln
 * (Abstand, Richtung, Zusammenhalt), weicht Hindernissen aus und bleibt über den Wellen.
 * <p>
 * <b>Aufwind am Turm:</b> Wind, der auf einen senkrechten Zylinder trifft, steigt an dessen Luvseite auf
 * ({@link #updraft}): am stärksten dicht vor der Wand, nach außen und über der Oberkante abklingend, im Lee nichts.
 * Ab gut 5 m/s Wind stellen sich Möwen dort in den Wind und stehen fast still in der Luft.
 * <p>
 * <b>Landen:</b> Sitzplätze ({@link Perch}, auch bewegte wie Tonnen) werden gegen den Wind angeflogen; die Möwe bremst,
 * fängt ab, sitzt eine Weile mit dem Kopf im Wind und fliegt wieder ab.
 * <p>
 * <b>Wetter und Tag:</b> Nachts (unter {@link #nightLux}) fliegt keine: Wer keinen Platz hat, fliegt zur Küste. Ab
 * Bft 8 suchen sie das Lee hinter dem Turm und fliegen tief, ab Bft 10 fliegen alle weg. Rufe kommen als Ereignis
 * ({@link #onCall}).
 * <p>
 * Nur Java 21; Koordinaten wie überall: x Ost, y oben, z Süd.
 */
public final class Flock {

    /** Stufen: Zahl der Möwen. */
    public enum Quality {
        LOW(6), MEDIUM(10), HIGH(16), ULTRA(28);
        public final int birds;
        Quality(int b) { birds = b; }
    }

    /** Was eine Möwe gerade tut. */
    public enum State { FLY, SOAR, LAND, PERCH, TAKEOFF, LEAVE, AWAY }

    /** Ein Ruf: Ort, Stärke 0..1, die Möwe. */
    @FunctionalInterface public interface Call { void call(double x, double y, double z, double strength, Gull g); }

    /** Die Oberfläche darunter (Wellen), eigene kleine Schnittstelle. */
    @FunctionalInterface public interface Ground { double height(double x, double z); }

    /** Ein Ort zum Sitzen; kann sich bewegen (Tonne) oder zeitweise fehlen (Platte bei Flut). */
    public interface Spot {
        double[] position();
        default boolean usable() { return true; }
    }

    /** Ein Sitzplatz mit Namen. */
    public static final class Perch {
        public final String name;
        public final Spot spot;
        volatile Gull by;
        public Perch(String name, Spot spot) { this.name = name; this.spot = spot; }
        public Perch(String name, double x, double y, double z) { this(name, () -> new double[]{x, y, z}); }
        public boolean taken() { return by != null; }
    }

    /** Ein senkrechter Zylinder (Turm, Pfahl): Mitte, Halbmesser, Unter- und Oberkante. */
    public record Obstacle(double x, double z, double r, double y0, double y1) { }

    /** Eine Möwe. */
    public static final class Gull {
        public final int id;
        public final double[] pos = new double[3], vel = new double[3];
        public volatile State state = State.FLY;
        /** Flügel: Phase des Schlags (rad), wie stark sie schlägt 0..1, Querneigung (rad), Faltung beim Sitzen 0..1. */
        public double wingPhase, flap, bank, fold;
        /** Geschwindigkeit gegen die Luft im letzten Schritt (m/s). */
        public double airspeed;
        /** Blickrichtung (Grad von Nord) beim Sitzen. */
        public double facing;
        double timer, nextCall, wanderX, wanderY, wanderZ, slotSide, slotDist, slotUp;
        /** Anflug: 0 = zum Punkt im Lee des Platzes, 1 = Endanflug gegen den Wind. */
        int approach;
        Perch perch;
        final Random rnd;
        Gull(int id, long seed) { this.id = id; rnd = new Random(seed * 7919 + id * 104729L); }
        public Perch perch() { return perch; }
        /** Geschwindigkeit über Grund (m/s). */
        public double groundSpeed() { return Math.sqrt(vel[0] * vel[0] + vel[1] * vel[1] + vel[2] * vel[2]); }
    }

    // ------------------------------------------------------------------ Eingänge
    public volatile long seed = 1;
    public volatile double windSpeed = 6, windFromDeg = 240;
    /** Helligkeit in lx; darunter ist Nacht. */
    public volatile double ambientLux = 20_000, nightLux = 30;
    /** Mitte des Reviers und sein Halbmesser (m). */
    public volatile double homeX = 0, homeZ = 0, homeRadius = 160;
    /** Richtung zur Küste (Grad), wohin sie abends und im Sturm fliegen. */
    public volatile double coastDeg = 150;
    public volatile Ground ground = (x, z) -> 0;
    public final List<Obstacle> obstacles = new CopyOnWriteArrayList<>();
    public final List<Perch> perches = new CopyOnWriteArrayList<>();
    public volatile Quality quality = Quality.HIGH;
    public volatile Call onCall;

    // ------------------------------------------------------------------ Zustand
    private final List<Gull> gulls = new CopyOnWriteArrayList<>();
    private double time;
    private long calls;
    private boolean started;

    /** Grenzen des Flugs gegen die Luft (m/s), Sinken im Gleitflug, Steigen mit Flügelschlag, Schlagfrequenz (Hz). */
    public static final double V_MIN = 8, V_CRUISE = 10.5, V_MAX = 17, SINK = 0.6, CLIMB = 2.0, FLAP_HZ = 3.1;
    /** Ab diesem Wind (m/s) stehen sie im Aufwind; ab Bft 8 suchen sie Lee, ab Bft 10 fliegen sie weg. */
    public static final int MAX_SOAR = 5;
    public static final double SOAR_WIND = 5.5, LEE_WIND = 17.2, AWAY_WIND = 24.5;

    // ------------------------------------------------------------------ Presets
    public static Flock calm() { return beaufort(1); }
    public static Flock breeze() { return beaufort(4); }
    public static Flock gale() { return beaufort(8); }
    public static Flock storm() { return beaufort(10); }

    public static Flock beaufort(int bft) {
        Flock f = new Flock();
        f.windSpeed = BFT_MID[Math.max(0, Math.min(12, bft))];
        return f;
    }

    static final double[] BFT_MID = {0.1, 0.9, 2.5, 4.4, 6.7, 9.4, 12.3, 15.5, 19.0, 22.6, 26.5, 30.6, 35.0};

    public List<Gull> gulls() { return gulls; }
    public double time() { return time; }
    public long calls() { return calls; }
    public boolean night() { return ambientLux < nightLux; }

    // ------------------------------------------------------------------ Wind

    /** Wind am Ort in der Höhe y über dem Wasser (Vektor, wohin er weht), mit dem logarithmischen Profil über See. */
    public double[] windAt(double y) {
        double a = Math.toRadians(windFromDeg + 180), h = Math.max(0.5, y);
        double f = Math.log(h / 2e-4) / Math.log(10 / 2e-4);
        return new double[]{Math.sin(a) * windSpeed * f, 0, -Math.cos(a) * windSpeed * f};
    }

    /**
     * Wind am Ort mit dem Windschatten der Hindernisse: hinter einem Zylinder bis gut zehn Halbmesser weit
     * schwächer (in der Mitte des Nachlaufs auf ein Fünftel), so hoch wie das Hindernis. Dort halten Möwen im Sturm aus.
     */
    public double[] windAt(double x, double y, double z) {
        double[] w = windAt(y - 0);
        double a = Math.toRadians(windFromDeg + 180), dx0 = Math.sin(a), dz0 = -Math.cos(a);
        double k = 1;
        for (Obstacle o : obstacles) {
            if (y > o.y1 + o.r || y < o.y0) continue;
            double rx = x - o.x, rz = z - o.z, along = rx * dx0 + rz * dz0, lat = Math.abs(rx * dz0 - rz * dx0);
            if (along <= 0) continue;
            double width = o.r * (1.2 + along / (o.r * 8));
            double s = Math.exp(-Math.pow(lat / width, 2)) * Math.exp(-along / (o.r * 12)) * (y > o.y1 ? Math.exp(-(y - o.y1) / o.r) : 1);
            k = Math.min(k, 1 - 0.8 * s);
        }
        return new double[]{w[0] * k, 0, w[2] * k};
    }

    /**
     * Aufwind (m/s) am Ort aus allen Hindernissen: vor der Luvseite eines Zylinders
     * w = 0,9 · U · cos^1,5(Winkel zur Luv) · exp(−Abstand / 1,5 R), bis zur Oberkante voll, darüber mit 0,8 R
     * abklingend; im Lee und seitlich nichts. Eine Näherung, kein Strömungsfeld.
     */
    public double updraft(double x, double y, double z) {
        double w = 0;
        double a = Math.toRadians(windFromDeg), ux = Math.sin(a), uz = -Math.cos(a);   // zur Luv
        for (Obstacle o : obstacles) {
            double dx = x - o.x, dz = z - o.z, d = Math.hypot(dx, dz);
            if (d < o.r * 0.5) continue;
            double cosA = (dx * ux + dz * uz) / d;
            if (cosA <= 0) continue;
            double gap = Math.max(0, d - o.r);
            double hf = y < o.y0 ? 0 : y < o.y1 ? Math.min(1, (y - o.y0) / (0.25 * (o.y1 - o.y0) + 1e-6)) : Math.exp(-(y - o.y1) / (0.8 * o.r));
            w += 0.9 * windSpeed * Math.pow(cosA, 1.5) * Math.exp(-gap / (1.5 * o.r)) * hf;
        }
        return w;
    }

    // ------------------------------------------------------------------ Zeit

    /** Möwen ins Revier setzen (verteilt in der Luft); geschieht beim ersten Schritt von selbst. */
    public void populate() {
        gulls.clear();
        for (Perch p : perches) p.by = null;
        Random r = new Random(seed);
        int n = quality.birds;
        for (int i = 0; i < n; i++) {
            Gull g = new Gull(i, seed);
            double a = r.nextDouble() * 2 * Math.PI, rr = homeRadius * (0.3 + 0.6 * r.nextDouble());
            g.pos[0] = homeX + Math.sin(a) * rr;
            g.pos[2] = homeZ - Math.cos(a) * rr;
            g.pos[1] = 12 + r.nextDouble() * 30;
            double h = r.nextDouble() * 2 * Math.PI;
            g.vel[0] = Math.sin(h) * V_CRUISE;
            g.vel[2] = -Math.cos(h) * V_CRUISE;
            g.wingPhase = r.nextDouble() * 6.28;
            g.nextCall = 5 + r.nextDouble() * 40;
            pickWander(g);
            gulls.add(g);
        }
        started = true;
    }

    /** Um dt Sekunden weiter (Szenenzeit; große Schritte werden in Stücke von 1/30 s geteilt, höchstens 3 s je Aufruf). */
    public void step(double dt) {
        if (!started) populate();
        if (dt <= 0) return;
        if (gulls.size() != quality.birds) adjustCount();
        double left = Math.min(dt, 3);
        while (left > 1e-9) {
            double h = Math.min(1 / 30.0, left);
            left -= h;
            time += h;
            for (Gull g : gulls) stepGull(g, h);
        }
    }

    private void adjustCount() {
        while (gulls.size() > quality.birds) {
            Gull g = gulls.remove(gulls.size() - 1);
            if (g.perch != null) g.perch.by = null;
        }
        while (gulls.size() < quality.birds) {
            Gull g = new Gull(gulls.size(), seed);
            g.state = State.AWAY;
            g.timer = 5 + g.rnd.nextDouble() * 60;
            gulls.add(g);
        }
    }

    private void stepGull(Gull g, double h) {
        boolean night = night(), lee = windSpeed >= LEE_WIND, away = windSpeed >= AWAY_WIND;
        g.timer -= h;
        switch (g.state) {
            case AWAY -> {
                // draußen bei der Küste; kommt am Tag bei mäßigem Wind zurück
                if (!night && !away && !lee && g.timer <= 0) {
                    double a = Math.toRadians(coastDeg);
                    g.pos[0] = homeX + Math.sin(a) * homeRadius * 4;
                    g.pos[2] = homeZ - Math.cos(a) * homeRadius * 4;
                    g.pos[1] = 25;
                    g.vel[0] = -Math.sin(a) * V_CRUISE;
                    g.vel[1] = 0;
                    g.vel[2] = Math.cos(a) * V_CRUISE;
                    g.state = State.FLY;
                    pickWander(g);
                } else return;
            }
            case PERCH -> {
                double[] p = g.perch.spot.position();
                g.pos[0] = p[0]; g.pos[1] = p[1]; g.pos[2] = p[2];
                g.vel[0] = g.vel[1] = g.vel[2] = 0;
                g.flap = 0;
                g.fold = Math.min(1, g.fold + h * 1.5);
                g.facing = turnToward(g.facing, windFromDeg, 40 * h);
                g.bank = 0;
                if (!g.perch.spot.usable() || away || (g.timer <= 0 && !night && !lee) || lee && !inLee(g.perch.spot.position())) takeoff(g);
                if (!night) maybeCall(g, h, 1 / 45.0);
                return;
            }
            default -> { }
        }
        // ------------------------------------------------ Ziele je Zustand
        double[] wind = windAt(g.pos[0], g.pos[1] - groundAt(g), g.pos[2]);
        double[] want = new double[3];      // gewünschte Geschwindigkeit über Grund
        double wantY = g.pos[1];
        if (g.state == State.FLY) {
            if ((night || lee) && !away) {
                // abends auf einen Platz am Turm, im Sturm auf einen im Lee; sonst zur Küste
                Perch p = freePerch(g, lee);
                if (p != null) { g.state = State.LAND; g.perch = p; p.by = g; g.timer = 90; g.approach = 0; }
                else g.state = State.LEAVE;
            } else if (away) { g.state = State.LEAVE; }
            else {
                double dx = g.wanderX - g.pos[0], dz = g.wanderZ - g.pos[2];
                if (Math.hypot(dx, dz) < 25 || g.timer <= 0) pickWander(g);
                want = toward(g, g.wanderX, g.wanderZ, V_CRUISE, wind);
                wantY = g.wanderY;
                if (g.timer <= 0) g.timer = 15 + g.rnd.nextDouble() * 30;
                // Entscheidungen
                if (g.rnd.nextDouble() < h / 25 && !lee) {
                    Perch p = freePerch(g, false);
                    if (p != null) { g.state = State.LAND; g.perch = p; p.by = g; g.timer = 60; g.approach = 0; }
                } else if (windSpeed >= SOAR_WIND && !lee && !obstacles.isEmpty() && g.rnd.nextDouble() < h / 40 && census()[State.SOAR.ordinal()] < MAX_SOAR) {
                    g.state = State.SOAR;
                    g.timer = 25 + g.rnd.nextDouble() * 50;
                    g.slotSide = (g.rnd.nextDouble() - 0.5) * 1.4;
                    g.slotDist = 0.9 + g.rnd.nextDouble() * 1.2;
                    g.slotUp = (g.rnd.nextDouble() - 0.4) * 10;
                }
            }
        }
        if (g.state == State.SOAR) {
            Obstacle o = obstacles.get(0);
            g.facing = turnToward(g.facing, windFromDeg, 90 * h);
            if (g.timer <= 0 || windSpeed < SOAR_WIND * 0.9 || lee || night) { g.state = State.FLY; pickWander(g); }
            else {
                double a = Math.toRadians(windFromDeg), ux = Math.sin(a), uz = -Math.cos(a);
                double side = g.slotSide, dist = o.r * (1 + g.slotDist);
                double sx = o.x + (ux * Math.cos(side) - uz * Math.sin(side)) * dist, sz = o.z + (uz * Math.cos(side) + ux * Math.sin(side)) * dist;
                double dx = sx - g.pos[0], dz = sz - g.pos[2];
                // Stehen: gegen Grund fast still, Nase im Wind
                want = new double[]{dx * 0.5, 0, dz * 0.5};
                double l = Math.hypot(want[0], want[2]);
                if (l > 6) { want[0] *= 6 / l; want[2] *= 6 / l; }
                wantY = o.y1 + g.slotUp + Math.sin(time * 0.7 + g.id) * 1.5;
            }
        }
        if (g.state == State.LAND) {
            double[] p = g.perch.spot.position();
            double dd0 = dist(g, p);
            if (!g.perch.spot.usable() || lee && !inLee(p) || g.timer <= 0) { release(g); g.state = State.FLY; pickWander(g); }
            else if (dd0 < 3) {
                // Abfangen: schlagend auf den Platz, langsam und gerade
                double sp = Math.min(1.8, dd0 * 2 + 0.3), step = Math.min(dd0, sp * h);
                for (int k = 0; k < 3; k++) { g.vel[k] = (p[k] - g.pos[k]) / Math.max(1e-6, dd0) * sp; g.pos[k] += (p[k] - g.pos[k]) / Math.max(1e-6, dd0) * step; }
                g.flap += (1 - g.flap) * Math.min(1, h * 4);
                g.wingPhase = (g.wingPhase + 2 * Math.PI * FLAP_HZ * h) % (2 * Math.PI);
                g.bank *= 0.9;
                if (dd0 - step < 0.05) {
                    g.state = State.PERCH;
                    g.timer = 40 + g.rnd.nextDouble() * 160;
                    g.facing = Math.toDegrees(Math.atan2(g.vel[0], -g.vel[2]));
                    g.pos[0] = p[0]; g.pos[1] = p[1]; g.pos[2] = p[2];
                    g.vel[0] = g.vel[1] = g.vel[2] = 0;
                    callNow(g);
                }
                return;
            }
            else {
                // gegen den Wind anfliegen: Ziel zuerst 12 m im Lee des Platzes, dann der Platz selbst
                double a = Math.toRadians(windFromDeg + 180), lx = Math.sin(a), lz = -Math.cos(a);
                double dx = p[0] - g.pos[0], dz = p[2] - g.pos[2], d = Math.hypot(dx, dz);
                double tx = p[0], tz = p[2];
                // erst zu einem Punkt 14 m im Lee des Platzes, von dort im Endanflug gegen den Wind (mit Gedächtnis, kein Pendeln)
                double qx = p[0] + lx * 14, qz = p[2] + lz * 14;
                if (g.approach == 0 && Math.hypot(qx - g.pos[0], qz - g.pos[2]) < 5) g.approach = 1;
                if (g.approach == 1 && d > 40) g.approach = 0;
                if (g.approach == 0) { tx = qx; tz = qz; }
                if (d > homeRadius * 6) { release(g); g.state = State.LEAVE; return; }
                double sp = Math.max(1.0, Math.min(V_CRUISE, d * 0.55));
                want = toward(g, tx, tz, sp, null);
                wantY = p[1] + Math.min(6, d * 0.15);
            }
        }
        if (g.state == State.TAKEOFF) {
            double a = Math.toRadians(windFromDeg + 180);
            want = new double[]{Math.sin(a) * 4, 0, -Math.cos(a) * 4};
            wantY = g.pos[1] + 6;
            g.fold = Math.max(0, g.fold - h * 4);
            if (g.timer <= 0) { g.state = State.FLY; pickWander(g); }
        }
        if (g.state == State.LEAVE) {
            double a = Math.toRadians(coastDeg);
            want = new double[]{Math.sin(a) * 12, 0, -Math.cos(a) * 12};
            wantY = lee ? 8 : 30;
            if (Math.hypot(g.pos[0] - homeX, g.pos[2] - homeZ) > homeRadius * 6) {
                g.state = State.AWAY;
                g.timer = 120 + g.rnd.nextDouble() * 600;
                return;
            }
            if (!night && !away && !lee) { g.state = State.FLY; pickWander(g); }
        }
        fly(g, want, wantY, wind, h);
        if (g.state != State.PERCH && g.state != State.AWAY) maybeCall(g, h, g.state == State.LAND ? 1 / 10.0 : 1 / 70.0);
    }

    /** Fliegen: gewünschte Geschwindigkeit plus Schwarm und Ausweichen, Grenzen gegen die Luft, Steigen und Sinken. */
    private void fly(Gull g, double[] want, double wantY, double[] wind, double h) {
        double[] steer = {want[0] - g.vel[0], 0, want[2] - g.vel[2]};
        // Schwarm: Abstand (4 m), Richtung und Zusammenhalt (25 m)
        double ax = 0, az = 0, cx = 0, cz = 0, sx = 0, sz = 0, sy = 0;
        int n = 0;
        for (Gull o : gulls) {
            if (o == g || o.state == State.AWAY || o.state == State.PERCH) continue;
            double dx = o.pos[0] - g.pos[0], dy = o.pos[1] - g.pos[1], dz = o.pos[2] - g.pos[2], d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d < 4 && d > 1e-6) { sx -= dx / d * (4 - d); sz -= dz / d * (4 - d); sy -= dy / d * (4 - d); }
            if (d < 25) { ax += o.vel[0]; az += o.vel[2]; cx += o.pos[0]; cz += o.pos[2]; n++; }
        }
        double flockW = g.state == State.FLY ? 1 : 0.2;
        if (n > 0) {
            steer[0] += flockW * (0.15 * (ax / n - g.vel[0]) + 0.02 * (cx / n - g.pos[0]));
            steer[2] += flockW * (0.15 * (az / n - g.vel[2]) + 0.02 * (cz / n - g.pos[2]));
        }
        steer[0] += sx * 1.5;
        steer[2] += sz * 1.5;
        // Hindernisse: weg von der Wand, wenn nah und darunter
        for (Obstacle o : obstacles) {
            double dx = g.pos[0] - o.x, dz = g.pos[2] - o.z, d = Math.hypot(dx, dz);
            double clear = g.state == State.LAND || g.state == State.TAKEOFF ? 0.8 : 5;
            boolean nearPerch = g.state == State.LAND && g.perch != null && dist(g, g.perch.spot.position()) < 25 || g.state == State.TAKEOFF;
            if (!nearPerch && g.pos[1] < o.y1 + 3 && d < o.r + clear + 8 && d > 1e-6) {
                double push = (o.r + clear + 8 - d) * 1.2;
                steer[0] += dx / d * push;
                steer[2] += dz / d * push;
            }
        }
        // Ins Revier zurück
        double hx = g.pos[0] - homeX, hz = g.pos[2] - homeZ, hd = Math.hypot(hx, hz);
        if (g.state == State.FLY && hd > homeRadius) { steer[0] -= hx / hd * (hd - homeRadius) * 0.05; steer[2] -= hz / hd * (hd - homeRadius) * 0.05; }
        double maxA = g.state == State.LAND || g.state == State.SOAR ? 6 : 4;
        double sl = Math.hypot(steer[0], steer[2]);
        if (sl > maxA) { steer[0] *= maxA / sl; steer[2] *= maxA / sl; }
        double ox = g.vel[0], oz = g.vel[2];
        g.vel[0] += steer[0] * h;
        g.vel[2] += steer[2] * h;
        // gegen die Luft zwischen V_MIN und V_MAX (beim Landen und Stehen darf sie langsamer werden: sie hängt im Wind)
        double vx = g.vel[0] - wind[0], vz = g.vel[2] - wind[2], va = Math.hypot(vx, vz);
        double vmin = g.state == State.LAND || g.state == State.TAKEOFF ? 2 : g.state == State.SOAR ? Math.min(V_MIN, windSpeed * 0.9) : V_MIN;
        if (va < vmin) {
            double k = va < 1e-6 ? 0 : vmin / va;
            if (va < 1e-6) { vx = -wind[0]; vz = -wind[2]; va = Math.hypot(vx, vz); k = va < 1e-6 ? 0 : vmin / va; if (va < 1e-6) { vx = vmin; vz = 0; k = 1; } }
            g.vel[0] = wind[0] + vx * k;
            g.vel[2] = wind[2] + vz * k;
        } else if (va > V_MAX) {
            g.vel[0] = wind[0] + vx * V_MAX / va;
            g.vel[2] = wind[2] + vz * V_MAX / va;
        }
        g.airspeed = Math.hypot(g.vel[0] - wind[0], g.vel[2] - wind[2]);
        // senkrecht: Aufwind und Sinken, Flügelschlag nach Bedarf
        double up = updraft(g.pos[0], g.pos[1], g.pos[2]);
        double ground = groundAt(g);
        wantY = Math.max(wantY, ground + (g.state == State.LAND ? -10 : 2.5));
        double needVy = Math.max(-3, Math.min(3, (wantY - g.pos[1]) * 0.6));
        double free = up - SINK * (g.state == State.SOAR ? 0.8 : 1);
        double flapNeed = (needVy - free) / CLIMB;
        double flap = Math.max(0, Math.min(1, flapNeed));
        if (g.state == State.TAKEOFF || g.state == State.LAND && Math.hypot(g.vel[0], g.vel[2]) < 4) flap = Math.max(flap, 0.8);
        g.flap += (flap - g.flap) * Math.min(1, h * 3);
        double vy = Math.min(needVy, free + g.flap * CLIMB);
        if (needVy < free) vy = needVy;              // Höhe abbauen geht immer (Bremsen, Gleiten)
        g.vel[1] += (vy - g.vel[1]) * Math.min(1, h * 2.5);
        g.pos[0] += g.vel[0] * h;
        g.pos[1] += g.vel[1] * h;
        g.pos[2] += g.vel[2] * h;
        if (g.pos[1] < ground + 0.8 && g.state != State.LAND) { g.pos[1] = ground + 0.8; g.vel[1] = Math.max(0, g.vel[1]); }
        // nie in ein Hindernis
        for (Obstacle o : obstacles) {
            double dx = g.pos[0] - o.x, dz = g.pos[2] - o.z, d = Math.hypot(dx, dz);
            if (d < o.r + 0.3 && g.pos[1] > o.y0 && g.pos[1] < o.y1 && d > 1e-6) {
                g.pos[0] = o.x + dx / d * (o.r + 0.3);
                g.pos[2] = o.z + dz / d * (o.r + 0.3);
            }
        }
        // Flügel und Querneigung
        if (g.state != State.SOAR && Math.hypot(g.vel[0], g.vel[2]) > 0.5) g.facing = Math.toDegrees(Math.atan2(g.vel[0], -g.vel[2]));
        // Schlag mit 3,1 Hz, solange sie schlägt; im Gleiten bleibt die Phase stehen
        if (g.flap > 0.05) g.wingPhase = (g.wingPhase + 2 * Math.PI * FLAP_HZ * h) % (2 * Math.PI);
        double lat = ((g.vel[0] - ox) * -g.vel[2] + (g.vel[2] - oz) * g.vel[0]) / Math.max(1e-6, Math.hypot(g.vel[0], g.vel[2])) / h;
        double bank = Math.atan(Math.max(-15, Math.min(15, lat)) / 9.81);
        g.bank += (bank - g.bank) * Math.min(1, h * 3);
        g.fold = Math.max(0, g.fold - h * 3);
    }

    static double dist(Gull g, double[] p) { return Math.sqrt((g.pos[0] - p[0]) * (g.pos[0] - p[0]) + (g.pos[1] - p[1]) * (g.pos[1] - p[1]) + (g.pos[2] - p[2]) * (g.pos[2] - p[2])); }

    private double groundAt(Gull g) { Ground gr = ground; return gr == null ? 0 : gr.height(g.pos[0], g.pos[2]); }

    private double[] toward(Gull g, double tx, double tz, double speed, double[] wind) {
        double dx = tx - g.pos[0], dz = tz - g.pos[2], d = Math.max(1e-6, Math.hypot(dx, dz));
        return new double[]{dx / d * speed + (wind != null ? wind[0] * 0.6 : 0), 0, dz / d * speed + (wind != null ? wind[2] * 0.6 : 0)};
    }

    private void pickWander(Gull g) {
        boolean lee = windSpeed >= LEE_WIND;
        double a, r;
        if (lee && !obstacles.isEmpty()) {
            Obstacle o = obstacles.get(0);
            double wa = Math.toRadians(windFromDeg + 180), along = o.r + 6 + g.rnd.nextDouble() * 45, lat = (g.rnd.nextDouble() - 0.5) * o.r * 2.4;
            g.wanderX = o.x + Math.sin(wa) * along + Math.cos(wa) * lat;
            g.wanderZ = o.z - Math.cos(wa) * along + Math.sin(wa) * lat;
            g.wanderY = 3 + g.rnd.nextDouble() * 10;
        } else {
            a = g.rnd.nextDouble() * 2 * Math.PI;
            r = homeRadius * Math.sqrt(g.rnd.nextDouble());
            g.wanderX = homeX + Math.sin(a) * r;
            g.wanderZ = homeZ - Math.cos(a) * r;
            g.wanderY = g.rnd.nextDouble() < 0.25 ? 3 + g.rnd.nextDouble() * 4 : 10 + g.rnd.nextDouble() * 45;
        }
        g.timer = 15 + g.rnd.nextDouble() * 30;
    }

    /** Liegt der Ort im Lee des ersten Hindernisses (hinter ihm, vom Wind aus gesehen)? */
    boolean inLee(double[] p) {
        if (obstacles.isEmpty()) return true;
        Obstacle o = obstacles.get(0);
        double a = Math.toRadians(windFromDeg + 180);
        return (p[0] - o.x) * Math.sin(a) - (p[2] - o.z) * Math.cos(a) > o.r * 0.3;
    }

    private Perch freePerch(Gull g, boolean leeOnly) {
        List<Perch> free = new ArrayList<>();
        for (Perch p : perches) if (p.by == null && p.spot.usable() && (!leeOnly || inLee(p.spot.position()))) free.add(p);
        if (free.isEmpty()) return null;
        // nahe Plätze bevorzugen
        Perch best = null;
        double bd = 1e18;
        for (int k = 0; k < 3; k++) {
            Perch p = free.get(g.rnd.nextInt(free.size()));
            double[] q = p.spot.position();
            double d = Math.hypot(q[0] - g.pos[0], q[2] - g.pos[2]);
            if (d < bd) { bd = d; best = p; }
        }
        return best;
    }

    private void takeoff(Gull g) {
        release(g);
        g.state = State.TAKEOFF;
        g.timer = 2.5;
        double a = Math.toRadians(windFromDeg);
        // springt gegen den Wind ab
        g.vel[0] = Math.sin(a) * 2;
        g.vel[2] = -Math.cos(a) * 2;
        g.vel[1] = 2;
        callNow(g);
    }

    private void release(Gull g) {
        if (g.perch != null && g.perch.by == g) g.perch.by = null;
        g.perch = null;
    }

    /** Rufe kommen zufällig mit der Rate (je s), aber nie kurz nacheinander. */
    private void maybeCall(Gull g, double h, double rate) {
        if (night()) return;
        g.nextCall -= h;
        if (g.nextCall > 0) return;
        g.nextCall = 4 - Math.log(1 - g.rnd.nextDouble()) / Math.max(1e-3, rate);
        fire(g);
    }

    /** Ein Ruf jetzt (beim Landen und Abfliegen), wenn der letzte mindestens 2 s her ist. */
    private void callNow(Gull g) {
        if (night()) return;
        fire(g);
        g.nextCall = Math.max(g.nextCall, 6);
    }

    private void fire(Gull g) {
        calls++;
        Call c = onCall;
        if (c != null) c.call(g.pos[0], g.pos[1], g.pos[2], 0.6 + 0.4 * g.rnd.nextDouble(), g);
    }

    static double turnToward(double from, double to, double maxStep) {
        double d = ((to - from) % 360 + 540) % 360 - 180;
        return (from + Math.max(-maxStep, Math.min(maxStep, d)) + 360) % 360;
    }

    // ------------------------------------------------------------------ Anzeige

    /** Zahl der Möwen je Zustand. */
    public int[] census() {
        int[] c = new int[State.values().length];
        for (Gull g : gulls) c[g.state.ordinal()]++;
        return c;
    }

    /** „16 Möwen: 9 fliegen, 3 stehen im Aufwind, 2 sitzen, 2 weg · 41 Rufe“ */
    public String describe() {
        int[] c = census();
        int fly = c[State.FLY.ordinal()] + c[State.LAND.ordinal()] + c[State.TAKEOFF.ordinal()] + c[State.LEAVE.ordinal()];
        return String.format(Locale.GERMAN, "%d Möwen: %d fliegen, %d stehen im Aufwind, %d sitzen, %d weg · %d Rufe", gulls.size(), fly, c[State.SOAR.ordinal()],
                c[State.PERCH.ordinal()], c[State.AWAY.ordinal()], calls);
    }
}
