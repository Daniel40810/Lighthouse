package com.dan.lighthouse.scene;

import java.util.ArrayList;
import java.util.List;

import static com.dan.lighthouse.scene.Innenraum.*;

/**
 * Baut das Innere des Turms (Phase 11) in ein eigenes Netz: Innenwand mit Laibungen für Bullaugen, Tür und
 * Bogenfenster, die Wendeltreppe aus kragenden Steinstufen mit Wandwangen, Geländerstäben, gelben Knäufen und dem
 * Handlauf aus Holz, die Leiter mit Kabeln und Seilen im Auge, Lampen, Barometer, Eimer und Besen, den Boden des
 * Dienstraums mit Geländer, die Decke mit der Luke und die Schiffsleiter dorthin. Alle Flächen zeigen in den Raum.
 * <p>
 * Die Wand ist ein Raster (2,25° × 0,25 m). Wo ein Fenster sitzt, fallen die Zellen eines Rechtecks weg; ein Ring
 * verbindet genau dessen Rasterpunkte mit dem Umriss des Fensters (Kreis, Rechteck oder Rundbogen), so entstehen keine
 * Risse. Vom Umriss läuft die Laibung bis an die Außenhaut; durch sie sieht man hinaus.
 */
final class Treppenhaus {
    private Treppenhaus() { }

    static final double[] PLASTER = {0.74, 0.73, 0.69}, STEP_STONE = {0.30, 0.30, 0.29}, CHEEK = {0.30, 0.295, 0.28}, JOINT = {0.035, 0.033, 0.03}, RAIL_WOOD = {0.30, 0.12, 0.05},
            IRON = {0.035, 0.035, 0.035}, YELLOW = {0.80, 0.55, 0.02}, LADDER_WOOD = {0.24, 0.13, 0.06}, CABLE = {0.02, 0.02, 0.02},
            ROPE = {0.70, 0.70, 0.66}, BUCKET = {0.72, 0.72, 0.70}, BLUE = {0.05, 0.12, 0.40}, DOOR = {0.40, 0.44, 0.40}, FLOOR_STONE = {0.30, 0.29, 0.27};
    static final Turm.Mat M_PLASTER = new Turm.Mat(Turm.K_PLASTER, 0, 0.85, 0.3, PLASTER), M_STEP = new Turm.Mat(Turm.K_STEP, 0, 0.7, 0.4, STEP_STONE),
            M_CHEEK = new Turm.Mat(Turm.K_STEP, 0, 0.75, 0.4, CHEEK), M_JOINT = new Turm.Mat(Turm.K_STEP, 0, 0.9, 0.4, JOINT), M_FLOOR = new Turm.Mat(Turm.K_STEP, 0, 0.75, 0.4, FLOOR_STONE),
            M_RAIL = new Turm.Mat(Turm.K_WOOD, 0, 0.32, 0, RAIL_WOOD), M_IRON = new Turm.Mat(Turm.K_STEEL, 0, 0.45, 0, IRON),
            M_KNOB = new Turm.Mat(Turm.K_PLASTIC, 0, 0.35, 0, YELLOW), M_LADDER = new Turm.Mat(Turm.K_WOOD, 0, 0.6, 0, LADDER_WOOD),
            M_CABLE = new Turm.Mat(Turm.K_PLASTIC, 0, 0.5, 0, CABLE), M_ROPE = new Turm.Mat(Turm.K_PLASTIC, 0, 0.9, 0, ROPE),
            M_BULB = new Turm.Mat(Turm.K_BULB, 0, 0.1, 0, new double[]{1, 1, 1}), M_DIAL = new Turm.Mat(Turm.K_DIAL, 0, 0.5, 0, new double[]{0.75, 0.72, 0.62}),
            M_BUCKET = new Turm.Mat(Turm.K_PLASTIC, 0, 0.45, 0, BUCKET), M_BLUE = new Turm.Mat(Turm.K_PLASTIC, 0, 0.45, 0, BLUE),
            M_DOOR = new Turm.Mat(Turm.K_STEEL, 0, 0.5, 0, DOOR), M_PAINT = new Turm.Mat(Turm.K_STEEL, 0, 0.55, 0, Turm.PAINT_IN),
            M_GREY_IN = new Turm.Mat(Turm.K_STEEL, 0, 0.5, 0, new double[]{0.20, 0.21, 0.21});

    static void build(Turm.B b) {
        new Treppenhaus.Run(b).all();
    }

    /** Ein Fenster in einer Wand: Mitte (Azimut, Höhe), Umriss (Kreis a; Rechteck a × unten c, oben d; Bogen: Rechteck bis archV, darüber Halbkreis a). */
    record Hole(double az, double y, boolean round, double a, double c, double d, double archV, Turm.Mat frame) {
        /** Abstand des Umrisses von der Mitte in Richtung (cu, cv) (Einheitsvektor in der Wand, m). */
        double reach(double cu, double cv) {
            if (round) return a;
            double t = Math.abs(cu) > 1e-9 ? a / Math.abs(cu) : 1e9;
            if (cv < 0) t = Math.min(t, c / -cv);
            else if (cv > 0) {
                if (Double.isNaN(archV)) t = Math.min(t, d / cv);
                else if (t * cv > archV) t = cv * archV + Math.sqrt(Math.max(0, cv * cv * archV * archV - archV * archV + a * a));
            }
            return t;
        }

        double top() { return round ? a : Double.isNaN(archV) ? d : archV + a; }
        double bottom() { return round ? a : c; }
    }

    static final class Run {
        final Turm.B b;

        Run(Turm.B b) { this.b = b; }

        int v(double[] p, double[] n, Turm.Mat m, double s, double t, double ao) {
            return b.vert(p[0], p[1], p[2], n[0], n[1], n[2], m, s, t, 0, 0, ao);
        }

        static double[] at(double r, double az, double y) {
            double a = Math.toRadians(az);
            return new double[]{Math.sin(a) * r, y, -Math.cos(a) * r};
        }

        static double[] radial(double az, double s) {
            double a = Math.toRadians(az);
            return new double[]{Math.sin(a) * s, 0, -Math.cos(a) * s};
        }

        static double[] tangent(double az) {
            double a = Math.toRadians(az);
            return new double[]{Math.cos(a), 0, Math.sin(a)};
        }

        static double[] add(double[] a, double[] b, double s) { return new double[]{a[0] + b[0] * s, a[1] + b[1] * s, a[2] + b[2] * s}; }

        static double[] sub(double[] a, double[] b) { return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]}; }

        static double[] cross(double[] a, double[] b) { return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]}; }

        static double[] unit(double[] a) {
            double l = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
            return l < 1e-12 ? new double[]{0, 1, 0} : new double[]{a[0] / l, a[1] / l, a[2] / l};
        }

        static double dot(double[] a, double[] b) { return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]; }

        /** Viereck aus vier Punkten mit einer Normale (Wicklung danach). */
        void quad(double[] p0, double[] p1, double[] p2, double[] p3, double[] n, Turm.Mat m, double ao) {
            int a = v(p0, n, m, p0[0] + p0[2], p0[1], ao), c1 = v(p1, n, m, p1[0] + p1[2], p1[1], ao), c2 = v(p2, n, m, p2[0] + p2[2], p2[1], ao), c3 = v(p3, n, m, p3[0] + p3[2], p3[1], ao);
            b.orient(a, c1, c2, n[0], n[1], n[2]);
            b.orient(a, c2, c3, n[0], n[1], n[2]);
        }

        /** Fläche aus einem Gitter f(i, j) → {Punkt, Normale}; die Normale am Rand der Zelle bestimmt die Wicklung. */
        interface Grid { double[][] at(double u, double v); }

        void patch(int nu, int nv, Grid g, Turm.Mat m, double ao) {
            int base = b.vs.size();
            for (int j = 0; j <= nv; j++)
                for (int i = 0; i <= nu; i++) {
                    double[][] q = g.at(i / (double) nu, j / (double) nv);
                    v(q[0], q[1], m, q[0][0] + q[0][2], q[0][1], ao);
                }
            for (int j = 0; j < nv; j++)
                for (int i = 0; i < nu; i++) {
                    int a = base + j * (nu + 1) + i, c = a + 1, d = a + nu + 1, e = d + 1;
                    float[] n = b.vs.get(a), n2 = b.vs.get(e);
                    double nx = n[3] + n2[3], ny = n[4] + n2[4], nz = n[5] + n2[5];
                    b.orient(a, c, e, nx, ny, nz);
                    b.orient(a, e, d, nx, ny, nz);
                }
        }

        /** Vierkantstab von p0 nach p1, Querschnitt w × d (d entlang side). */
        void rod(double[] p0, double[] p1, double w, double d, double[] side, Turm.Mat m, double ao) {
            double[] ax = unit(sub(p1, p0));
            double[] s = unit(sub(side, add(new double[3], ax, dot(side, ax))));
            double[] t = unit(cross(ax, s));
            double[][] c = new double[8][];
            for (int i = 0; i < 8; i++) {
                double[] e = (i & 4) == 0 ? p0 : p1;
                double sw = (i & 1) == 0 ? -w / 2 : w / 2, sd = (i & 2) == 0 ? -d / 2 : d / 2;
                c[i] = add(add(e, t, sw), s, sd);
            }
            double[] mt = add(new double[3], t, -1), ms = add(new double[3], s, -1), mx = add(new double[3], ax, -1);
            quad(c[1], c[3], c[7], c[5], t, m, ao);
            quad(c[0], c[2], c[6], c[4], mt, m, ao);
            quad(c[2], c[3], c[7], c[6], s, m, ao);
            quad(c[0], c[1], c[5], c[4], ms, m, ao);
            quad(c[4], c[5], c[7], c[6], ax, m, ao);
            quad(c[0], c[1], c[3], c[2], mx, m, ao);
        }

        /** Rohr entlang einer Linie aus Punkten, Halbmesser r, k Seiten; up gibt die Lage des Querschnitts. */
        void tube(List<double[]> path, double r, int k, Turm.Mat m, double ao, boolean caps) {
            int base = b.vs.size(), n = path.size();
            double[] s = null;
            for (int i = 0; i < n; i++) {
                double[] p = path.get(i), t = unit(sub(path.get(Math.min(n - 1, i + 1)), path.get(Math.max(0, i - 1))));
                // Querschnitt mitgeführt (paralleler Transport), damit sich das Rohr nicht verdreht
                if (s == null) s = unit(cross(t, Math.abs(t[1]) > 0.9 ? new double[]{1, 0, 0} : new double[]{0, 1, 0}));
                else s = unit(sub(s, add(new double[3], t, dot(s, t))));
                double[] u = cross(s, t);
                for (int j = 0; j <= k; j++) {
                    double a = 2 * Math.PI * j / k, ca = Math.cos(a), sa = Math.sin(a);
                    double[] nn = {s[0] * ca + u[0] * sa, s[1] * ca + u[1] * sa, s[2] * ca + u[2] * sa};
                    v(add(p, nn, r), nn, m, i * 0.05, a * r, ao);
                }
            }
            for (int i = 0; i + 1 < n; i++)
                for (int j = 0; j < k; j++) {
                    int a = base + i * (k + 1) + j, c = a + 1, d = a + k + 1, e = d + 1;
                    float[] q = b.vs.get(a), q2 = b.vs.get(e);
                    b.orient(a, c, e, q[3] + q2[3], q[4] + q2[4], q[5] + q2[5]);
                    b.orient(a, e, d, q[3] + q2[3], q[4] + q2[4], q[5] + q2[5]);
                }
            if (caps)
                for (int end = 0; end < 2; end++) {
                    int i = end == 0 ? 0 : n - 1;
                    double[] p = path.get(i), t = unit(sub(path.get(Math.min(n - 1, i + 1)), path.get(Math.max(0, i - 1))));
                    if (end == 0) t = add(new double[3], t, -1);
                    int c = v(p, t, m, 0, 0, ao);
                    for (int j = 0; j < k; j++) {
                        float[] q0 = b.vs.get(base + i * (k + 1) + j), q1 = b.vs.get(base + i * (k + 1) + j + 1);
                        int a0 = v(new double[]{q0[0], q0[1], q0[2]}, t, m, 0, 0, ao), a1 = v(new double[]{q1[0], q1[1], q1[2]}, t, m, 0, 0, ao);
                        b.orient(c, a0, a1, t[0], t[1], t[2]);
                    }
                }
        }

        /** Scheibe (r0 > 0: Ring) mit Mitte c, Normale n, „oben“ up in der Fläche; s, t = Lage in der Scheibe (für das Zifferblatt). */
        void disc(double[] c, double[] n, double[] up, double r0, double r1, int k, Turm.Mat m, double ao) {
            double[] rt = unit(cross(up, n));
            int base = b.vs.size();
            for (int j = 0; j <= k; j++) {
                double a = 2 * Math.PI * j / k, ca = Math.cos(a), sa = Math.sin(a);
                for (double rr : new double[]{r0, r1}) {
                    double[] p = add(add(c, rt, ca * rr), up, sa * rr);
                    b.vert(p[0], p[1], p[2], n[0], n[1], n[2], m, ca * rr, sa * rr, 0, 0, ao);
                }
            }
            for (int j = 0; j < k; j++) {
                int a = base + 2 * j;
                b.orient(a, a + 1, a + 3, n[0], n[1], n[2]);
                b.orient(a, a + 3, a + 2, n[0], n[1], n[2]);
            }
        }

        /** Mantel eines kurzen Zylinders entlang der Achse n (für Ränder): Halbmesser r, von c bis c + n·len, nach außen. */
        void rim(double[] c, double[] n, double[] up, double r, double len, int k, Turm.Mat m, double ao) {
            double[] rt = unit(cross(up, n));
            int base = b.vs.size();
            for (int j = 0; j <= k; j++) {
                double a = 2 * Math.PI * j / k;
                double[] e = add(add(new double[3], rt, Math.cos(a)), up, Math.sin(a));
                for (double l : new double[]{0, len}) b.vert(c[0] + e[0] * r + n[0] * l, c[1] + e[1] * r + n[1] * l, c[2] + e[2] * r + n[2] * l, e[0], e[1], e[2], m, a * r, l, 0, 0, ao);
            }
            for (int j = 0; j < k; j++) {
                int a = base + 2 * j;
                float[] q = b.vs.get(a), q2 = b.vs.get(a + 3);
                b.orient(a, a + 1, a + 3, q[3] + q2[3], q[4] + q2[4], q[5] + q2[5]);
                b.orient(a, a + 3, a + 2, q[3] + q2[3], q[4] + q2[4], q[5] + q2[5]);
            }
        }

        // ============================================================== Teile

        void all() {
            shaftWall();
            roomWall();
            floors();
            stairs();
            handrail();
            wellLadder();
            fittings();
            hatch();
        }

        /** Rasterwand mit Löchern. outerR(y): Halbmesser der Außenhaut für die Laibung. */
        void wall(double R, double y0, double y1, double azStart, List<Hole> holes, java.util.function.DoubleUnaryOperator outerR, Turm.Mat m) {
            int cols = 160, rows = (int) Math.round((y1 - y0) / 0.25);
            double dA = 360.0 / cols, dY = (y1 - y0) / rows;
            int[][] id = new int[rows + 1][cols + 1];
            for (int j = 0; j <= rows; j++)
                for (int i = 0; i <= cols; i++) {
                    double az = azStart + i * dA, y = y0 + j * dY;
                    id[j][i] = v(at(R, az, y), radial(az, -1), m, Math.toRadians(az) * R, y, 1);
                }
            boolean[][] gone = new boolean[rows][cols];
            List<int[]> rects = new ArrayList<>();
            for (Hole h : holes) {
                double du = Math.toDegrees(1) / R;   // Grad je Meter
                double half = (h.round() ? h.a() : h.a()) + 0.07;
                int i0 = (int) Math.floor((Innenraum.norm(h.az() - azStart) - half * du) / dA), i1 = (int) Math.ceil((Innenraum.norm(h.az() - azStart) + half * du) / dA);
                int j0 = Math.max(0, (int) Math.floor((h.y() - h.bottom() - 0.07 - y0) / dY)), j1 = Math.min(rows, (int) Math.ceil((h.y() + h.top() + 0.07 - y0) / dY));
                if (i0 < 0 || i1 > cols) throw new IllegalStateException("Fenster über der Naht des Rasters bei " + h.az());
                for (int j = j0; j < j1; j++) for (int i = i0; i < i1; i++) {
                    if (gone[j][i]) throw new IllegalStateException("Fenster überlappen bei " + h.az());
                    gone[j][i] = true;
                }
                rects.add(new int[]{i0, i1, j0, j1});
            }
            double[] in = new double[3];
            for (int j = 0; j < rows; j++)
                for (int i = 0; i < cols; i++) {
                    if (gone[j][i]) continue;
                    double az = azStart + (i + 0.5) * dA;
                    in = radial(az, -1);
                    b.orient(id[j][i], id[j][i + 1], id[j + 1][i + 1], in[0], 0, in[2]);
                    b.orient(id[j][i], id[j + 1][i + 1], id[j + 1][i], in[0], 0, in[2]);
                }
            for (int k = 0; k < holes.size(); k++) {
                Hole h = holes.get(k);
                int[] q = rects.get(k);
                // Rand des Rechtecks gegen den Uhrzeigersinn in (u, v): unten, rechts, oben, links
                List<int[]> ring = new ArrayList<>();
                for (int i = q[0]; i < q[1]; i++) ring.add(new int[]{i, q[2]});
                for (int j = q[2]; j < q[3]; j++) ring.add(new int[]{q[1], j});
                for (int i = q[1]; i > q[0]; i--) ring.add(new int[]{i, q[3]});
                for (int j = q[3]; j > q[2]; j--) ring.add(new int[]{q[0], j});
                int n = ring.size();
                int[] inner = new int[n], tIn = new int[n], tOut = new int[n];
                double[] nOut = radial(h.az(), 1);
                double[] centre = at(R, h.az(), h.y());
                for (int e = 0; e < n; e++) {
                    int[] g = ring.get(e);
                    double az = azStart + g[0] * dA, y = y0 + g[1] * dY;
                    double u = Math.toRadians(((az - h.az()) % 360 + 540) % 360 - 180) * R, vv = y - h.y(), l = Math.hypot(u, vv);
                    double cu = l > 1e-9 ? u / l : 0, cv = l > 1e-9 ? vv / l : 1;
                    double t = Math.min(l, h.reach(cu, cv));
                    double hu = cu * t, hv = cv * t;
                    double haz = h.az() + Math.toDegrees(hu / R), hy = h.y() + hv;
                    double[] p = at(R, haz, hy);
                    inner[e] = v(p, radial(haz, -1), m, Math.toRadians(haz) * R, hy, 1);
                    // Laibung: vom Umriss entlang der Fensterachse bis an die Außenhaut
                    double[] ax = add(centre, nOut, dot(sub(p, centre), nOut));
                    double[] nn = unit(sub(ax, p));
                    if (Math.abs(hv + h.bottom()) < 1e-6 && !h.round()) nn = new double[]{0, 1, 0};
                    double ro = outerR.applyAsDouble(hy) - 0.004, hn = p[0] * nOut[0] + p[2] * nOut[2], hh = p[0] * p[0] + p[2] * p[2];
                    double dd = -hn + Math.sqrt(Math.max(0, hn * hn - hh + ro * ro));
                    tIn[e] = v(p, nn, m, e * 0.05, 0, 0.8);
                    tOut[e] = v(add(p, nOut, dd), nn, m, e * 0.05, dd, 0.7);
                }
                for (int e = 0; e < n; e++) {
                    int f = (e + 1) % n;
                    int[] g = ring.get(e);
                    double[] nn = radial(azStart + g[0] * dA, -1);
                    b.orient(id[g[1]][g[0]], id[ring.get(f)[1]][ring.get(f)[0]], inner[f], nn[0], 0, nn[2]);
                    b.orient(id[g[1]][g[0]], inner[f], inner[e], nn[0], 0, nn[2]);
                    float[] a1 = b.vs.get(tIn[e]), a2 = b.vs.get(tIn[f]);
                    double sx = a1[3] + a2[3], sy = a1[4] + a2[4], sz = a1[5] + a2[5];
                    b.orient(tIn[e], tIn[f], tOut[f], sx, sy, sz);
                    b.orient(tIn[e], tOut[f], tOut[e], sx, sy, sz);
                }
                // Rahmen um die Bullaugen (Messing), Fensterbank unter den Bogenfenstern
                if (h.round() && h.frame() != null) {
                    double[] c = add(centre, nOut, -0.012);
                    disc(c, radial(h.az(), -1), new double[]{0, 1, 0}, h.a(), h.a() + 0.075, 24, h.frame(), 1);
                    rim(c, nOut, new double[]{0, 1, 0}, h.a() + 0.075, 0.012, 24, h.frame(), 1);
                } else if (!h.round() && !Double.isNaN(h.archV())) {
                    double[] s = at(R - 0.07, h.az(), h.y() - h.bottom() - 0.025);
                    b.box(s[0], s[1], s[2], Math.toRadians(h.az()), 2 * h.a() + 0.16, 0.05, 0.16, M_PAINT, 0.9);
                }
            }
        }

        void shaftWall() {
            List<Hole> holes = new ArrayList<>();
            for (Window w : WINDOWS) if (!w.room()) holes.add(new Hole(azOf(w.x(), w.z()), w.y(), true, PORT_R, 0, 0, Double.NaN, Turm.M_BRASS));
            holes.add(new Hole(Turm.DOOR_AZ, FLOOR_Y + DOOR_H / 2, false, DOOR_W / 2, DOOR_H / 2, DOOR_H / 2, Double.NaN, null));
            wall(WALL_R, FLOOR_Y, ROOM_FLOOR, 160, holes, Turm::shaftR, M_PLASTER);
            // Türblatt am Ende der Laibung, mit Griff
            double a = Math.toRadians(Turm.DOOR_AZ), r = Math.sqrt(Math.pow(Turm.shaftR(FLOOR_Y + DOOR_H), 2) - DOOR_W * DOOR_W / 4) - 0.05;
            b.box(Math.sin(a) * r, FLOOR_Y + DOOR_H / 2, -Math.cos(a) * r, a, DOOR_W, DOOR_H, 0.04, M_DOOR, 0.8);
            double[] t = tangent(Turm.DOOR_AZ), hp = add(at(r - 0.05, Turm.DOOR_AZ, FLOOR_Y + 1.0), t, 0.32);
            b.box(hp[0], hp[1], hp[2], a, 0.14, 0.025, 0.03, Turm.M_BRASS, 1);
        }

        void roomWall() {
            List<Hole> holes = new ArrayList<>();
            for (Window w : WINDOWS)
                if (w.room()) holes.add(new Hole(azOf(w.x(), w.z()), 20.9, false, 0.3, 20.9 - 20.375, 0, 21.4 - 20.9, null));
            wall(ROOM_WALL_R, ROOM_FLOOR, CEIL_Y, 180, holes, y -> Turm.ROOM_R, M_PLASTER);
        }

        /** Ringförmige Fläche als Raster in Polarkoordinaten; keep entscheidet je Zelle (r, az in der Mitte). */
        void polar(double y, double[] rings, double az0, int seg, double span, boolean up, Turm.Mat m, double ao, java.util.function.BiPredicate<Double, Double> keep) {
            int base = b.vs.size();
            double[] n = {0, up ? 1 : -1, 0};
            for (double r : rings)
                for (int k = 0; k <= seg; k++) {
                    double az = az0 + span * k / seg;
                    double[] p = at(r, az, y);
                    v(p, n, m, p[0], p[2], ao);
                }
            for (int j = 0; j + 1 < rings.length; j++)
                for (int k = 0; k < seg; k++) {
                    double rm = (rings[j] + rings[j + 1]) / 2, am = az0 + span * (k + 0.5) / seg;
                    if (!keep.test(rm, Innenraum.norm(am))) continue;
                    int a = base + j * (seg + 1) + k, c = a + 1, d = a + seg + 1, e = d + 1;
                    b.orient(a, c, e, 0, n[1], 0);
                    b.orient(a, e, d, 0, n[1], 0);
                }
        }

        /** Senkrechter Streifen auf einem Bogen (Kante einer Platte): Halbmesser r, Azimut a0..a1, Höhe y0..y1, Normale nach innen (in = true) oder außen. */
        void arcStrip(double r, double a0, double a1, double y0, double y1, boolean in, Turm.Mat m, double ao) {
            int seg = Math.max(2, (int) Math.ceil(Math.abs(a1 - a0) / 4));
            patch(seg, 1, (u, vv) -> {
                double az = a0 + (a1 - a0) * u;
                return new double[][]{at(r, az, y0 + (y1 - y0) * vv), radial(az, in ? -1 : 1)};
            }, m, ao);
        }

        /** Senkrechte Fläche entlang eines Radius (Azimut az, r0..r1, y0..y1) mit Normale in Richtung wachsender (dir = 1) oder fallender Azimute. */
        void radialStrip(double az, double r0, double r1, double y0, double y1, int dir, Turm.Mat m, double ao) {
            double[] t = add(new double[3], tangent(az), dir);
            quad(at(r0, az, y0), at(r1, az, y0), at(r1, az, y1), at(r0, az, y1), t, m, ao);
        }

        void floors() {
            // Boden des Schafts (Steinplatten), Boden des Dienstraums (Dielen) über dem tragenden Teil, Sims über der Öffnung
            polar(FLOOR_Y, new double[]{0, 0.75, 1.5, 2.1, WALL_R}, 0, 96, 360, true, M_FLOOR, 0.8, (r, a) -> true);
            double span = Innenraum.norm(SOLID_AZ1 - SOLID_AZ0);
            Turm.Mat planks = Turm.M_PLANKS;
            polar(ROOM_FLOOR, new double[]{WELL_R, 1.2, LADDER_FOOT_R + 0.3, 2.3, WALL_R, ROOM_WALL_R}, SOLID_AZ0, 30, span, true, planks, 0.85, (r, a) -> true);
            polar(ROOM_FLOOR, new double[]{WALL_R, ROOM_WALL_R}, SOLID_AZ1, 132, 360 - span, true, planks, 0.8, (r, a) -> true);
            polar(ROOM_FLOOR - SLAB, new double[]{WELL_R, 1.5, WALL_R}, SOLID_AZ0, 30, span, false, M_PLASTER, 0.75, (r, a) -> true);
            arcStrip(WELL_R, SOLID_AZ0, SOLID_AZ0 + span, ROOM_FLOOR - SLAB, ROOM_FLOOR, true, M_PAINT, 0.8);
            radialStrip(SOLID_AZ0 + span, WELL_R, WALL_R, ROOM_FLOOR - SLAB, ROOM_FLOOR, 1, M_PAINT, 0.8);
            radialStrip(SOLID_AZ0, WELL_R, WALL_R, ROOM_FLOOR - SLAB, ROOM_FLOOR - RISE + 0.001, -1, M_PAINT, 0.8);
            // Geländer an der Öffnung: entlang der Kante bei 237,5° und um das Auge
            List<double[]> topRail = new ArrayList<>();
            for (int k = 0; k <= 16; k++) topRail.add(at(WELL_R + 0.06, SOLID_AZ0 - 8 + (span + 8) * k / 16.0, ROOM_FLOOR + 0.95));
            for (int k = 1; k <= 8; k++) topRail.add(at(WELL_R + 0.06 + (ROOM_WALL_R - 0.02 - WELL_R - 0.06) * k / 8.0, SOLID_AZ1 + 1.5, ROOM_FLOOR + 0.95));
            tube(topRail, 0.03, 8, M_RAIL, 0.9, true);
            for (int k = 0; k <= 6; k++) baluster(at(WELL_R + 0.06, SOLID_AZ0 + span * k / 6.0, 0), ROOM_FLOOR, ROOM_FLOOR + 0.92);
            for (int k = 1; k <= 4; k++) baluster(at(WELL_R + 0.06 + (WALL_R + 0.25 - WELL_R) * k / 4.0, SOLID_AZ1 + 1.5, 0), ROOM_FLOOR, ROOM_FLOOR + 0.92);
            // Decke des Dienstraums mit der Luke
            polar(CEIL_Y, new double[]{0, 0.3, HATCH_R0, HATCH_R1, 2.1, ROOM_WALL_R}, 0, 120, 360, false, M_PLASTER, 0.75, (r, a) -> !hatchCell(r, a));
        }

        static boolean hatchCell(double r, double az) { return r > HATCH_R0 && r < HATCH_R1 && Math.abs(Innenraum.norm(az - HATCH_AZ + 180) - 180) < HATCH_HALF; }

        /** Geländerstab: Messingmanschette unten, Eisenstab, gelber Knauf unter dem Handlauf. */
        void baluster(double[] xz, double y0, double y1) {
            double x = xz[0], z = xz[2];
            b.latheAt(x, z, new double[][]{{0.022, y0}, {0.022, y0 + 0.04}, {0.012, y0 + 0.05}}, 8, Turm.M_BRASS, y0, 0, 0.9);
            b.box(x, (y0 + y1) / 2, z, Math.atan2(x, -z), 0.016, y1 - y0, 0.016, M_IRON, 0.9);
            double k0 = y1 - 0.16;
            b.latheAt(x, z, new double[][]{{0.012, k0}, {0.024, k0 + 0.02}, {0.03, k0 + 0.06}, {0.026, k0 + 0.1}, {0.018, k0 + 0.13}, {0.016, y1}}, 10, M_KNOB, y1, 0, 0.95);
        }

        void stairs() {
            for (int i = 0; i < STEPS; i++) {
                double a0 = stepAz(i), a1 = a0 + STEP_DEG, top = tread(i), bot = Math.max(FLOOR_Y + 0.001, top - THICK);
                int na = 4, nr = 5;
                // Oberseite mit leicht gerundeter Vorderkante (Nase 2 cm über die Setzstufe)
                patch(na, nr, (u, vv) -> new double[][]{at(WELL_R + (WALL_R - WELL_R) * vv, a0 - 0.6 + (a1 + 0.6 - a0) * u, top), {0, 1, 0}}, M_STEP, 1);
                patch(na, nr, (u, vv) -> new double[][]{at(WELL_R + (WALL_R - WELL_R) * vv, a1 - (a1 - a0 + 0.6) * u, bot), {0, -1, 0}}, M_STEP, 0.7);
                double[] fr = add(new double[3], tangent(a0 - 0.6), -1), bk = tangent(a1);
                patch(1, nr, (u, vv) -> new double[][]{at(WELL_R + (WALL_R - WELL_R) * vv, a0 - 0.6, bot + (top - bot) * u), fr}, M_STEP, 0.9);
                patch(1, nr, (u, vv) -> new double[][]{at(WELL_R + (WALL_R - WELL_R) * vv, a1, top - (top - bot) * u), bk}, M_STEP, 0.8);
                arcStrip(WELL_R, a0 - 0.6, a1, bot, top, true, M_STEP, 0.85);
                // Wandwange: Steinplatte über der Stufe an der Wand
                // Steinplatte mit dunkler Fuge rundum (die „Zinnen“ in Innen2): Fuge 3 cm, Platte 2 cm davor
                double ch = 0.17, r0 = WALL_R - 0.03, rj = WALL_R - 0.01, fa = Math.toDegrees(0.03 / WALL_R);
                arcStrip(rj, a0 - 0.6, a1, top, top + ch + 0.03, true, M_JOINT, 0.8);
                patch(3, 1, (u, vv) -> new double[][]{at(rj + 0.01 * vv, a0 - 0.6 + (a1 + 0.6 - a0) * u, top + ch + 0.03), {0, 1, 0}}, M_JOINT, 0.8);
                radialStrip(a0 - 0.6, rj, WALL_R, top, top + ch + 0.03, -1, M_JOINT, 0.8);
                double c0 = a0 - 0.6 + fa, c1 = a1 - fa;
                arcStrip(r0, c0, c1, top, top + ch, true, M_CHEEK, 0.85);
                patch(3, 1, (u, vv) -> new double[][]{at(r0 + 0.02 * vv, c0 + (c1 - c0) * u, top + ch), {0, 1, 0}}, M_CHEEK, 0.9);
                radialStrip(c0, r0, rj, top, top + ch, -1, M_CHEEK, 0.85);
                radialStrip(c1, r0, rj, top, top + ch, 1, M_CHEEK, 0.85);
                // unter der Stufe: Wange zur Wand hin (der Zinnenrand in Innen2)
                double cb = Math.max(FLOOR_Y + 0.001, bot - 0.12);
                if (bot - cb > 0.01) {
                    arcStrip(r0, a0 - 0.6, a1, cb, bot, true, M_CHEEK, 0.7);
                    patch(3, 1, (u, vv) -> new double[][]{at(r0 + 0.03 * vv, a1 - (a1 + 0.6 - a0) * u, cb), {0, -1, 0}}, M_CHEEK, 0.7);
                    radialStrip(a1, r0, WALL_R, cb, bot, 1, M_CHEEK, 0.7);
                }
                // Geländerstab auf jeder Stufe
                double am = a0 + STEP_DEG * 0.5;
                baluster(at(RAIL_R, am, 0), top, railY(am) - 0.025);
            }
        }

        /** Höhe des Handlaufs über dem unaufgewickelten Azimut. */
        static double railY(double az) { return FLOOR_Y + RISE * ((az - START_AZ) / STEP_DEG + 0.5) + RAIL_H; }

        void handrail() {
            List<double[]> p = new ArrayList<>();
            double a0 = stepAz(0) + 4, a1 = stepAz(STEPS - 1) + STEP_DEG;
            for (double az = a0; az <= a1 + 1e-9; az += 3) p.add(at(RAIL_R, az, railY(az)));
            // oben waagerecht bis an das Geländer der Öffnung
            p.add(at(RAIL_R - 0.02, a1 + 4, ROOM_FLOOR + 0.95));
            p.add(at(WELL_R + 0.06, a1 + 9, ROOM_FLOOR + 0.95));
            // Profil: rundes Holz, 6 cm
            tube(p, 0.032, 10, M_RAIL, 1, true);
        }

        void wellLadder() {
            double az = 115, top = ROOM_FLOOR - 0.35;
            double[] t = tangent(az), o = radial(az, 1);
            for (int s = -1; s <= 1; s += 2) {
                double[] c = add(new double[3], t, 0.2 * s);
                rod(new double[]{c[0], FLOOR_Y, c[2]}, new double[]{c[0], top, c[2]}, 0.06, 0.03, o, M_LADDER, 0.9);
            }
            for (double y = FLOOR_Y + 0.3; y < top - 0.05; y += 0.3) {
                double[] c0 = add(new double[]{0, y, 0}, t, -0.17), c1 = add(new double[]{0, y, 0}, t, 0.17);
                rod(c0, c1, 0.035, 0.022, o, M_LADDER, 0.9);
            }
            // drei schwarze Kabel und zwei weiße Seile entlang der Leiter bis unter den Dienstraum
            double[][] cab = {{0.26, 0.06}, {0.29, 0.03}, {0.27, -0.02}, {-0.30, 0.10}, {-0.33, -0.04}};
            for (int k = 0; k < cab.length; k++) {
                double[] c = add(add(new double[3], t, cab[k][0]), o, cab[k][1]);
                boolean rope = k >= 3;
                List<double[]> line = new ArrayList<>();
                for (double y = FLOOR_Y + (rope ? 0.6 : 0.02); y <= ROOM_FLOOR + 0.001 - (rope ? 0.3 : 0.25); y += 0.5)
                    line.add(new double[]{c[0] + 0.01 * Math.sin(y * 1.3 + k), y, c[2] + 0.01 * Math.cos(y * 1.1 + k)});
                tube(line, rope ? 0.007 : 0.009, 5, rope ? M_ROPE : M_CABLE, 0.9, false);
            }
        }

        void fittings() {
            for (int i = 0; i < LAMPS.size(); i++) {
                Lamp l = LAMPS.get(i);
                double[] p = {l.x(), l.y(), l.z()};
                if (l.room()) pendant(p);
                else if (i == 5) ladderLamp(p);
                else wallLamp(p);
            }
            barometer(290, tread((int) Math.floor(Innenraum.norm(290 - START_AZ) / STEP_DEG) + PER_TURN) + 1.55);
            // Eimer und Besen im Dienstraum, wo die Treppe ankommt
            double[] bk = at(2.45, 98, 0);
            b.latheAt(bk[0], bk[2], new double[][]{{0.0, ROOM_FLOOR + 0.004}, {0.125, ROOM_FLOOR + 0.004}, {0.14, ROOM_FLOOR + 0.3}, {0.148, ROOM_FLOOR + 0.31}}, 24, M_BUCKET, ROOM_FLOOR + 0.31, 0, 0.8);
            b.latheAt(bk[0], bk[2], new double[][]{{0.1405, ROOM_FLOOR + 0.24}, {0.1425, ROOM_FLOOR + 0.27}}, 24, M_BLUE, ROOM_FLOOR + 0.27, 0, 0.8);
            b.latheAt(bk[0], bk[2], new double[][]{{0.14, ROOM_FLOOR + 0.31}, {0.123, ROOM_FLOOR + 0.03}, {0.0, ROOM_FLOOR + 0.03}}, 24, M_BUCKET, ROOM_FLOOR + 0.31, 0, 0.55);
            double[] f = at(2.82, 106, ROOM_FLOOR + 0.02), h = at(2.86, 105.0, ROOM_FLOOR + 1.35);
            rod(f, h, 0.025, 0.025, radial(106, 1), M_LADDER, 0.85);
            rod(add(f, tangent(106), -0.15), add(f, tangent(106), 0.15), 0.06, 0.05, radial(106, 1), M_GREY_IN, 0.7);
            rod(add(at(2.7, 90, ROOM_FLOOR + 0.004), tangent(90), -0.13), add(at(2.7, 90, ROOM_FLOOR + 0.004), tangent(90), 0.13), 0.004, 0.25, radial(90, 1), M_GREY_IN, 0.7);
            // ein Paar Arbeitsschuhe auf dem Boden des Schafts neben der Tür
            for (int s = 0; s < 2; s++) {
                double[] c = at(2.25, 183 + s * 4, FLOOR_Y + 0.05);
                b.box(c[0], c[1], c[2], Math.toRadians(150 + s * 8), 0.11, 0.1, 0.28, new Turm.Mat(Turm.K_PLASTIC, 0, 0.6, 0, new double[]{0.05, 0.04, 0.035}), 0.7);
            }
        }

        void wallLamp(double[] p) {
            double az = azOf(p[0], p[2]);
            double[] w = at(WALL_R - 0.015, az, p[1] + BULB_R + 0.04);
            b.box(w[0], w[1], w[2], Math.toRadians(az), 0.1, 0.14, 0.03, M_GREY_IN, 0.9);
            // Milchglaskugel mit Fassung (Schiffsleuchte)
            b.sphereAt(p[0], p[1], p[2], BULB_R, M_BULB);
            b.latheAt(p[0], p[2], new double[][]{{0.03, p[1] + BULB_R - 0.01}, {0.045, p[1] + BULB_R + 0.01}, {0.045, p[1] + BULB_R + 0.05}, {0.0, p[1] + BULB_R + 0.06}}, 16, M_GREY_IN, p[1] + 0.14, 0, 0.9);
            double[] top = {p[0], p[1] + BULB_R + 0.04, p[2]};
            rod(top, add(at(WALL_R - 0.03, az, p[1] + BULB_R + 0.04), new double[3], 0), 0.025, 0.025, new double[]{0, 1, 0}, M_GREY_IN, 0.9);
        }

        void ladderLamp(double[] p) {
            b.sphereAt(p[0], p[1], p[2], BULB_R, M_BULB);
            b.latheAt(p[0], p[2], new double[][]{{0.03, p[1] + 0.05}, {0.04, p[1] + 0.12}, {0.0, p[1] + 0.13}}, 12, M_GREY_IN, p[1] + 0.13, 0, 0.9);
            rod(new double[]{p[0], p[1] + 0.12, p[2]}, new double[]{p[0] - 0.1, p[1] + 0.2, p[2]}, 0.02, 0.02, new double[]{0, 0, 1}, M_GREY_IN, 0.9);
        }

        void pendant(double[] p) {
            rod(new double[]{p[0], CEIL_Y, p[2]}, new double[]{p[0], p[1] + 0.12, p[2]}, 0.008, 0.008, new double[]{1, 0, 0}, M_CABLE, 0.9);
            b.latheAt(p[0], p[2], new double[][]{{0.02, p[1] + 0.13}, {0.06, p[1] + 0.1}, {0.18, p[1] - 0.02}, {0.19, p[1] - 0.04}}, 24, M_PAINT, p[1] + 0.13, 0, 0.9);
            b.latheAt(p[0], p[2], new double[][]{{0.188, p[1] - 0.035}, {0.175, p[1] - 0.02}, {0.058, p[1] + 0.09}, {0.0, p[1] + 0.1}}, 24, M_PAINT, p[1] + 0.1, 0, 1);
            b.sphereAt(p[0], p[1], p[2], BULB_R, M_BULB);
        }

        /** Dosenbarometer mit Messingring auf einem Holzbrett. */
        void barometer(double az, double y) {
            double[] in = radial(az, -1), up = {0, 1, 0};
            double[] board = at(WALL_R - 0.01, az, y);
            b.box(board[0], board[1], board[2], Math.toRadians(az), 0.32, 0.36, 0.02, M_LADDER, 0.9);
            double[] c = at(WALL_R - 0.055, az, y);
            disc(c, in, up, 0, 0.11, 40, M_DIAL, 1);
            disc(add(c, in, 0.012), in, up, 0.11, 0.13, 40, Turm.M_BRASS, 1);
            rim(add(c, in, -0.035), in, up, 0.13, 0.047, 40, Turm.M_BRASS, 1);
        }

        /** Luke: Laibung durch die Decke, Schiffsleiter vom Boden des Dienstraums. */
        void hatch() {
            double a0 = HATCH_AZ - HATCH_HALF, a1 = HATCH_AZ + HATCH_HALF;
            arcStrip(HATCH_R0, a0, a1, CEIL_Y, LANTERN_FLOOR, false, M_PAINT, 0.7);
            arcStrip(HATCH_R1, a0, a1, CEIL_Y, LANTERN_FLOOR, true, M_PAINT, 0.7);
            radialStrip(a0, HATCH_R0, HATCH_R1, CEIL_Y, LANTERN_FLOOR, 1, M_PAINT, 0.7);
            radialStrip(a1, HATCH_R0, HATCH_R1, CEIL_Y, LANTERN_FLOOR, -1, M_PAINT, 0.7);
            // Schiffsleiter: Wangen vom Fuß bis über die Luke, Tritte alle 25 cm
            double[] t = tangent(HATCH_AZ), side = radial(HATCH_AZ, 1);
            double yTop = LANTERN_FLOOR - 0.06;
            for (int s = -1; s <= 1; s += 2) {
                double[] f = add(at(LADDER_FOOT_R, HATCH_AZ, ROOM_FLOOR), t, 0.24 * s), h = add(at(ladderR(yTop), HATCH_AZ, yTop), t, 0.24 * s);
                rod(f, h, 0.04, 0.1, side, M_GREY_IN, 0.85);
            }
            for (double y = ROOM_FLOOR + 0.25; y < LANTERN_FLOOR - 0.05; y += 0.25) {
                double[] c = at(ladderR(y) - 0.0, HATCH_AZ, y);
                b.box(c[0], c[1], c[2], Math.toRadians(HATCH_AZ), 0.46, 0.03, 0.13, M_GREY_IN, 0.85);
            }
        }
    }

    /** Halbmesser der Schiffsleiter auf Höhe y: vom Fuß (1,55 m) zur Innenkante der Luke auf Höhe des Laternenbodens. */
    static double ladderR(double y) {
        return LADDER_FOOT_R + (HATCH_R0 + 0.04 - LADDER_FOOT_R) * (y - ROOM_FLOOR) / (LANTERN_FLOOR - ROOM_FLOOR);
    }
}
