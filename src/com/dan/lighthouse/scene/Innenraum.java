package com.dan.lighthouse.scene;

import java.util.List;

/**
 * Das Innere des Turms (Phase 11, nach Innen2): Maße der Wendeltreppe, des Dienstraums und der Luke zum Laternenraum,
 * die Lampen und Fenster als Lichtquellen und eine kleine Lichtrechnung ohne Grafikkarte.
 * <p>
 * <b>Treppe:</b> 59 Steinstufen von der Tür (8,05 m) bis zum Boden des Dienstraums (19,75 m), 18 je Umlauf
 * (20°), Steigung 19,8 cm, im Uhrzeigersinn aufwärts ab Azimut 257,5°, links von der Tür. So liegt keines der acht
 * Bullaugen hinter einer Stufe und die Tür bleibt frei (mindestens 15 cm Luft; gesucht über Beginn, Stufenzahl und
 * Stufen je Umlauf). Die Stufen
 * kragen aus der Innenwand (Halbmesser 2,6 m) bis an das offene Auge in der Mitte (0,75 m); dort steht auf jeder
 * Stufe ein Geländerstab mit gelbem Knauf unter dem Handlauf aus Holz. Im Auge hängt eine Leiter mit Kabeln und Seilen.
 * <p>
 * <b>Dienstraum:</b> Er beginnt, wo die Treppe ankommt. Weil ein Umlauf nur 3,6 m hoch ist, braucht man über den
 * letzten 12 Stufen 2 m Kopfhöhe: Der Boden ist dort offen, nur zwischen 357,5° und 117,5° trägt er, mit einem
 * Geländer zur Öffnung. Von dort führt eine steile Schiffsleiter durch eine Luke in den Laternenraum.
 * <p>
 * <b>Licht:</b> 8 Bullaugen und 3 Bogenfenster lassen Himmel und Sonne herein, 7 Glühlampen brennen auf Wunsch. Für
 * jede Ecke des Innenraums wird einmal ausgerechnet, welche Lampen sie sehen und wie viel vom Fensterlicht die Stufen
 * verdecken ({@link #visibility}); das Licht selbst rechnet der Shader je Bildpunkt. Das zurückgeworfene Licht kommt
 * aus einer Bilanz über zwei Räume (Schaft, Dienstraum), die auch die Augenanpassung drinnen bestimmt.
 */
public final class Innenraum {
    private Innenraum() { }

    // ------------------------------------------------------------------ Maße in m
    public static final double WALL_R = 2.6, WELL_R = 0.75, FLOOR_Y = Turm.SOCKEL_TOP + 0.05, ROOM_FLOOR = 19.75, SLAB = 0.22;
    public static final double ROOM_WALL_R = 2.92, CEIL_Y = 22.58, LANTERN_FLOOR = Turm.GALLERY_TOP + 0.02;
    public static final int STEPS = 59, PER_TURN = 18;
    public static final double RISE = (ROOM_FLOOR - FLOOR_Y) / STEPS, STEP_DEG = 360.0 / PER_TURN, START_AZ = 257.5, THICK = 0.22;
    /** Handlauf: Halbmesser und Höhe über der Stufenvorderkante. */
    public static final double RAIL_R = 0.82, RAIL_H = 0.9;
    /** Der tragende Teil des Dienstraumbodens (Rest offen über der Treppe); erste Stufe unter der Öffnung. */
    public static final int OPEN_STEP = 47;
    public static final double SOLID_AZ0 = norm(START_AZ + STEP_DEG * STEPS), SOLID_AZ1 = norm(START_AZ + STEP_DEG * OPEN_STEP);
    /** Luke zum Laternenraum: Azimut, halbe Breite in Grad, Halbmesser innen und außen; die Leiter steht darunter. */
    public static final double HATCH_AZ = 60, HATCH_HALF = 18, HATCH_R0 = 0.62, HATCH_R1 = 1.36, LADDER_FOOT_R = 1.55;
    /** Tür (Türblatt innen), Breite und Höhe. */
    public static final double DOOR_W = 0.9, DOOR_H = 2.0;
    /** Bullaugen: Halbmesser der Öffnung (wie das Glas außen). */
    public static final double PORT_R = 0.15;
    /** Rückstrahlung der Wände und Stufen (Kalkputz, Sandstein, gemittelt). */
    public static final double RHO = 0.45;
    /** Glühlampe: 60 W, 710 lm, rund 2700 K; Halbmesser der Milchglaskugel. */
    public static final double LAMP_LM = 710, BULB_R = 0.075;
    public static final double[] LAMP_RGB = {1.0, 0.80, 0.60};

    /** Ein Fenster: innere Mitte, Normale nach außen (waagerecht), Tiefe der Laibung, Form (Kreis: a = Halbmesser; sonst Rechteck a × b halb). */
    public record Window(String name, double x, double y, double z, double nx, double nz, double depth, boolean round, double a, double b, boolean room) {
        public double area() { return round ? Math.PI * a * a : 4 * a * b; }
        /** Mitte außen. */
        public double[] outer() { return new double[]{x + nx * depth, y, z + nz * depth}; }
    }

    /** Eine Lampe: Ort des Kolbens; room = im Dienstraum. */
    public record Lamp(String name, double x, double y, double z, boolean room) { }

    public static final List<Window> WINDOWS = windows();
    /** Ansicht wie Innen2: über dem Handlauf der obersten Windung, Blick hinab ins Auge (x, y, z, Blick, Neigung, Vergrößerung). */
    public static final double[] INNEN2 = innen2();

    private static double[] innen2() {
        double az = 300, r = 0.55, a = Math.toRadians(az);
        return new double[]{Math.sin(a) * r, 20.6, -Math.cos(a) * r, norm(az + 180), 84, 0.55};
    }
    public static final List<Lamp> LAMPS = lamps();

    // ------------------------------------------------------------------ Treppe

    public static double norm(double az) { double a = az % 360; return a < 0 ? a + 360 : a; }

    /** Azimut, an dem Stufe i beginnt (unaufgewickelt). */
    public static double stepAz(int i) { return START_AZ + STEP_DEG * i; }

    /** Oberkante der Stufe i. */
    public static double tread(int i) { return FLOOR_Y + RISE * (i + 1); }

    /** Liegt der Azimut az (Grad) im Bereich der Stufe i? */
    public static boolean onStep(double az, int i) { return norm(az - stepAz(i)) < STEP_DEG; }

    /** Oberkante der Treppe unter (az, y): die höchste Stufe an diesem Azimut, deren Oberkante höchstens y + 0,05 ist; sonst der Boden. */
    public static double floorBelow(double az, double y) {
        double best = FLOOR_Y;
        int i0 = (int) Math.floor(norm(az - START_AZ) / STEP_DEG);
        for (int i = i0; i < STEPS; i += PER_TURN) if (tread(i) <= y + 0.05) best = tread(i);
        return best;
    }

    /** Liegt (az, r) über dem tragenden Teil des Dienstraumbodens? */
    public static boolean solidRoomFloor(double az, double r) {
        if (r < WELL_R || r > ROOM_WALL_R) return false;
        if (r > WALL_R) return true;
        return norm(az - SOLID_AZ0) < norm(SOLID_AZ1 - SOLID_AZ0);
    }

    /** Azimut eines Punkts (Grad von Nord im Uhrzeigersinn). */
    public static double azOf(double x, double z) { return norm(Math.toDegrees(Math.atan2(x, -z))); }

    /** Steht der Punkt im Innenraum (Schaft oder Dienstraum)? */
    public static boolean inside(double x, double y, double z) {
        double r = Math.hypot(x, z);
        return y > FLOOR_Y - 0.05 && y < CEIL_Y && r < (y < ROOM_FLOOR ? WALL_R : ROOM_WALL_R);
    }

    // ------------------------------------------------------------------ Lichtquellen

    private static List<Window> windows() {
        List<Window> l = new java.util.ArrayList<>();
        double[][] port = {{10.9, 60}, {10.9, 300}, {14.4, 20}, {14.4, 110}, {14.4, 245}, {14.4, 335}, {18.0, 80}, {18.0, 260}};
        for (double[] p : port) {
            double a = Math.toRadians(p[1]), sx = Math.sin(a), sz = -Math.cos(a);
            l.add(new Window(String.format(java.util.Locale.ROOT, "Bullauge %.1f m %03.0f°", p[0], p[1]), sx * WALL_R, p[0], sz * WALL_R, sx, sz,
                    Turm.shaftR(p[0]) - WALL_R, true, PORT_R, PORT_R, false));
        }
        for (double az : new double[]{0, 112.5, 247.5}) {
            double a = Math.toRadians(az), sx = Math.sin(a), sz = -Math.cos(a);
            // Bogenfenster: 0,6 m breit, 20,375 bis 21,7 m (Rechteck mit Halbkreis), fürs Licht als Rechteck gleicher Fläche
            double h = (0.6 * 1.025 + Math.PI * 0.09 / 2) / 0.6;
            l.add(new Window(String.format(java.util.Locale.ROOT, "Bogenfenster %05.1f°", az), sx * ROOM_WALL_R, 20.375 + h / 2, sz * ROOM_WALL_R, sx, sz,
                    Turm.ROOM_R + 0.012 - ROOM_WALL_R, false, 0.3, h / 2, true));
        }
        return List.copyOf(l);
    }

    /** Lampen an der Wand je 2,1 m über der Stufe darunter, im Wechsel auf 350° und 170°; eine oben an der Leiter, eine im Dienstraum. */
    private static List<Lamp> lamps() {
        List<Lamp> l = new java.util.ArrayList<>();
        double rr = WALL_R - 0.16;
        double[][] at = {{350, 0}, {170, 0}, {350, 1}, {170, 1}, {350, 2}};
        for (double[] p : at) {
            int i = (int) Math.floor(norm(p[0] - START_AZ) / STEP_DEG) + PER_TURN * (int) p[1];
            double a = Math.toRadians(p[0]);
            l.add(new Lamp(String.format(java.util.Locale.ROOT, "Wandlampe %03.0f°", p[0]), Math.sin(a) * rr, tread(i) + 2.1, -Math.cos(a) * rr, false));
        }
        l.add(new Lamp("Lampe an der Leiter", 0.18, ROOM_FLOOR - 0.5, 0.0, false));
        double a = Math.toRadians(20);
        l.add(new Lamp("Deckenlampe Dienstraum", Math.sin(a) * 2.25, CEIL_Y - 0.32, -Math.cos(a) * 2.25, true));
        return List.copyOf(l);
    }

    /** Lichtstärke einer Lampe in cd (gleichmäßig in alle Richtungen). */
    public static double lampCandela() { return LAMP_LM / (4 * Math.PI); }

    // ------------------------------------------------------------------ Verdeckung

    /**
     * Verdeckt etwas die Strecke a–b? Geprüft werden Ober- und Unterseiten aller Stufen und der Boden des Dienstraums
     * (dieselbe Rechnung macht der Shader für die Sonnenflecken). Die Endpunkte selbst zählen nicht (1 cm Abstand).
     */
    public static boolean blocked(double[] a, double[] b) {
        double y0 = Math.min(a[1], b[1]), y1 = Math.max(a[1], b[1]), dy = b[1] - a[1];
        if (Math.abs(dy) < 1e-6) return false;
        for (int pass = 0; pass < 2; pass++) {
            double off = pass == 0 ? 0 : -THICK;
            int j0 = Math.max(0, (int) Math.ceil((y0 + 0.01 - FLOOR_Y - off) / RISE) - 1), j1 = Math.min(STEPS - 1, (int) Math.floor((y1 - 0.01 - FLOOR_Y - off) / RISE) - 1);
            for (int j = j0; j <= j1; j++) {
                double y = tread(j) + off, t = (y - a[1]) / dy;
                if (t <= 0 || t >= 1) continue;
                double x = a[0] + (b[0] - a[0]) * t, z = a[2] + (b[2] - a[2]) * t, r = Math.hypot(x, z);
                if (r >= WELL_R && r <= WALL_R && onStep(azOf(x, z), j)) return true;
            }
        }
        for (double y : new double[]{ROOM_FLOOR, ROOM_FLOOR - SLAB}) {
            double t = (y - a[1]) / dy;
            if (t <= 0.001 || t >= 0.999) continue;
            double x = a[0] + (b[0] - a[0]) * t, z = a[2] + (b[2] - a[2]) * t;
            if (solidRoomFloor(azOf(x, z), Math.hypot(x, z))) return true;
        }
        return false;
    }

    /** Durchlass einer Laibung der Tiefe d und des Halbmessers a unter dem Winkel mit cos c zur Fensterachse. */
    public static double tube(double c, double d, double a) {
        if (c <= 0) return 0;
        double tan = Math.sqrt(Math.max(0, 1 - c * c)) / c;
        return Math.max(0, Math.min(1, 1 - d * tan / (2 * a)));
    }

    /**
     * Sichtbarkeit je Ecke: [0..6] die sieben Lampen (Mittel aus sechs Punkten auf der Kugel), [7] der unverdeckte
     * Anteil des Fensterlichts (nach Raumwinkel gewichtet). p Ort, n Normale.
     */
    public static float[] visibility(double[] p, double[] n) {
        float[] v = new float[8];
        double[] a = {p[0] + n[0] * 0.015, p[1] + n[1] * 0.015, p[2] + n[2] * 0.015};
        for (int i = 0; i < LAMPS.size() && i < 7; i++) {
            Lamp l = LAMPS.get(i);
            // sechs Punkte auf der Milchglaskugel: weiche Ränder der Schatten
            double s = 0;
            for (int k = 0; k < 6; k++) {
                double ph = k * Math.PI / 3, rr = BULB_R * 0.8;
                if (!blocked(a, new double[]{l.x() + Math.cos(ph) * rr, l.y() + ((k & 1) == 0 ? 0.5 : -0.5) * rr, l.z() + Math.sin(ph) * rr})) s += 1;
            }
            v[i] = (float) (s / 6);
        }
        double all = 0, seen = 0;
        for (Window w : WINDOWS) {
            double[] c = w.outer();
            double dx = c[0] - a[0], dy = c[1] - a[1], dz = c[2] - a[2], d2 = dx * dx + dy * dy + dz * dz, d = Math.sqrt(d2);
            double cw = (dx * w.nx() + dz * w.nz()) / d, cp = (dx * n[0] + dy * n[1] + dz * n[2]) / d;
            if (cw <= 0 || cp <= 0) continue;
            double g = w.area() * cw * cp / (d2 + w.area()) * tube(cw, w.depth(), Math.min(w.a(), w.b()));
            if (g <= 0) continue;
            all += g;
            // drei Punkte über die Öffnung verteilt
            double hits = 0;
            for (int k = -1; k <= 1; k++) {
                double[] q = {c[0] - w.nz() * k * w.a() * 0.6, c[1] + k * Math.min(w.b(), 0.12) * 0.5, c[2] + w.nx() * k * w.a() * 0.6};
                if (!blocked(a, q)) hits++;
            }
            seen += g * hits / 3;
        }
        v[7] = all > 0 ? (float) (seen / all) : 1f;
        return v;
    }

    // ------------------------------------------------------------------ Bilanz

    /** Flächen der beiden Räume (m²), die das Licht teilen: Schaft mit Treppe, Dienstraum mit Decke. */
    public static final double AREA_SHAFT = 2 * Math.PI * WALL_R * (ROOM_FLOOR - FLOOR_Y) + STEPS * 1.75 + Math.PI * WALL_R * WALL_R,
            AREA_ROOM = 2 * Math.PI * ROOM_WALL_R * (CEIL_Y - ROOM_FLOOR) + 2 * Math.PI * ROOM_WALL_R * ROOM_WALL_R * 0.6;
    /** Anteil, mit dem die Räume ihr Licht durch die große Bodenöffnung teilen. */
    public static final double COUPLING = 0.35;

    /**
     * Lichtbilanz: Lichtstrom in beide Räume aus Himmel (Strahldichte je Fenster, rgb), Sonne (Bestrahlungsstärke
     * senkrecht, rgb, Richtung sun) und Lampen (an/aus). Gibt {E_ind Schaft rgb, E_ind Dienstraum rgb, L Schaft, L Dienstraum}:
     * indirekte Bestrahlungsstärke in lx und mittlere Leuchtdichte der Flächen in cd/m².
     */
    public static double[][] balance(double[][] windowL, double[] sunE, double[] sun, boolean lampsOn) { return balance(windowL, sunE, sun, lampsOn, 1); }

    /** Wie oben; unit rechnet Lumen in die Lichteinheit der übrigen Größen um (com.dan.sky: 1 / {@code Sky.CD_PER_UNIT}). */
    public static double[][] balance(double[][] windowL, double[] sunE, double[] sun, boolean lampsOn, double unit) {
        double[] fs = new double[3], fr = new double[3];
        for (int i = 0; i < WINDOWS.size(); i++) {
            Window w = WINDOWS.get(i);
            double[] f = w.room() ? fr : fs;
            double tubeSky = w.depth() > 0.3 ? 0.55 : 0.9;   // tiefe Laibungen schneiden schräge Strahlen ab
            double cs = sun[0] * w.nx() + sun[2] * w.nz();
            double sunPart = sun[1] > 0 && cs > 0 ? cs * tube(cs, w.depth(), Math.min(w.a(), w.b())) * w.area() : 0;
            for (int c = 0; c < 3; c++) f[c] += Math.PI * windowL[i][c] * w.area() * tubeSky + sunE[c] * sunPart;
        }
        if (lampsOn) {
            double lm = LAMP_LM * unit;
            for (Lamp l : LAMPS) for (int c = 0; c < 3; c++) (l.room() ? fr : fs)[c] += lm * LAMP_RGB[c];
        }
        double[] es = new double[3], er = new double[3];
        double k = COUPLING, g = RHO / (1 - RHO);
        for (int c = 0; c < 3; c++) {
            es[c] = g * (fs[c] + k * fr[c]) / (AREA_SHAFT + k * AREA_ROOM);
            er[c] = g * (fr[c] + k * fs[c]) / (AREA_ROOM + k * AREA_SHAFT);
        }
        // mittlere Leuchtdichte: ρ · E_gesamt / π mit E_gesamt = E_ind / ρ
        double ls = lum(es) / Math.PI, lr = lum(er) / Math.PI;
        return new double[][]{es, er, {ls}, {lr}};
    }

    static double lum(double[] c) { return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]; }

    /** Luftdruck in hPa für das Barometer aus der Windstärke (bis die Wetterregie in Phase 12 echte Werte gibt). */
    public static double pressure(int beaufort) { return 1022 - 3.2 * beaufort; }

    /** Zeigerwinkel des Barometers in Grad (0 = oben, im Uhrzeigersinn): 960 bis 1060 hPa über 270°. */
    public static double needle(double hPa) { return -135 + 270 * Math.max(0, Math.min(1, (hPa - 960) / 100)); }
}
