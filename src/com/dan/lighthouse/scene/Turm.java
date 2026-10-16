package com.dan.lighthouse.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Der Leuchtturm Roter Sand, prozedural aus Profilkurven (F22 = A, Phase 5). Rotationskörper für Caisson, Sockel,
 * Schaft, Dienstraum, Galerie, Laterne und Kuppel; die drei Erker als angesetzte Zylinder mit spitzen Konsolen; dazu
 * Tür, Leiter, Bullaugen, Fenster, Geländer, Mast, Solarpaneele, die ovale Stahlmanschette von 1987, die
 * Steinschüttung, der Peilpfahl und am Horizont der Leuchtturm Alte Weser.
 * <p>
 * Maße: Höhen aus dem Werkbuch (Abschnitt 3, über mittlerem Meeresspiegel), Durchmesser aus Aussen1 abgemessen [G]:
 * Sockel unten 10,5 m, oben 8 m, Schaft 7,5 m auf 5,9 m verjüngt, Dienstraum mit Erkern 8 m breit, Galerie 8,6 m.
 * Die Plattform liegt auf mittlerem Meeresspiegel: Bei Niedrigwasser ragt sie 1,5 m heraus, bei Hochwasser steht sie
 * 1,5 m unter Wasser. Ihr Oval misst 16 × 13,2 m, die lange Achse quer zum Blick von Aussen1 (aus dem Foto abgemessen
 * [G]; die in der Recherche genannten 15 × 11 m ließen neben einem Sockel von 10,5 m kaum einen Rand). Die Erker zeigen nach Nordwest, Süd und Nordost; der nordöstliche ist höher und hat eine eigene
 * kleine Kuppel (Aussen2).
 * <p>
 * Ecken ({@link #STRIDE} Floats): Lage (3), Normale (3), Grundfarbe linear (3), Material (Art, Rostanfälligkeit,
 * Rauheit, Porosität), Fläche (s, t in m, Kante oben in m, Plattenhöhe in m; 0 = keine Stöße), Umgebungslicht (1).
 * Alle Dreiecke sind außen gegen den Uhrzeigersinn gewunden.
 */
public final class Turm {
    public static final int STRIDE = 18;

    /** Arten (Material.x im Shader). */
    public static final int K_RIVETED = 0, K_STEEL = 1, K_GLASS = 2, K_DOME = 3, K_STONE = 4, K_DECK = 5, K_LAMP = 6, K_SOLAR = 7, K_PLANKS = 8, K_BRASS = 9, K_LENS = 10,
            /** Phase 11, innen: Kalkputz, Steinstufe, Holz, Kunststoff, Glühlampe, Zifferblatt. */
            K_PLASTER = 11, K_STEP = 12, K_WOOD = 13, K_PLASTIC = 14, K_BULB = 15, K_DIAL = 16;

    // ------------------------------------------------------------------ Maße in m über MSL
    public static final double PLATE_TOP = 0.0, PLATE_BOTTOM = -1.2, PLATE_A = 8.0, PLATE_B = 6.6, PLATE_ROT = -62;
    public static final double SOCKEL_TOP = Site.PLINTH_TOP, SOCKEL_R0 = 5.25, SOCKEL_R1 = 4.0;
    public static final double SHAFT_R0 = 3.75, SHAFT_R1 = 2.95, SHAFT_TOP = 19.6;
    public static final double ROOM_R = 3.05, ROOM_TOP = 22.6, ERKER_R = 1.15, ERKER_D = 2.85, ERKER_HIGH_TOP = 24.4;
    public static final double[] ERKER_AZ = {315, 180, 45};
    public static final double GALLERY_R = 4.3, GALLERY_TOP = Site.GALLERY, LANTERN_R = 1.85, GLASS_BOTTOM = 23.5, GLASS_TOP = 25.6;
    /** Runder Handlauf des Galeriegeländers (Phase 8b, nass mit com.dan.wetglass): Ring, Höhe der Achse, Halbmesser des Rohrs. */
    public static final double HANDRAIL_RING = GALLERY_R - 0.1, HANDRAIL_Y = GALLERY_TOP + 1.05, HANDRAIL_T = 0.035;
    /** Mitte der Gürtellinse (Lichtschwerpunkt des Feuers, Phase 9). */
    public static final double LENS_Y = GALLERY_TOP + 0.02 + 0.84 + 0.475;
    public static final double DOME_TOP = 27.6, KNOB_TOP = 28.3, MAST_TOP = Site.TOP, MAST_AZ = 75;
    /** Farbringe: weiß, rot, weiß über dem Sockel, dann der rote Dienstraum. */
    public static final double BAND1 = 12.6, BAND2 = 16.4;
    public static final double DOOR_AZ = 200;
    /** Peilpfahl: Richtung und Abstand vom Turm (Aussen2). */
    public static final double PFAHL_AZ = 105, PFAHL_DIST = 27;
    /** Alte Weser: 53°52′ N, 8°08′ E (auf die Bogenminute [G]), 38 m hoch, Feuer auf 33 m, rot mit zwei weißen Bändern. */
    public static final double ALTE_WESER_LAT = 53 + 52 / 60.0, ALTE_WESER_LON = 8 + 8 / 60.0;

    // ------------------------------------------------------------------ Farben (linear)
    static final double[] BLACK = {0.022, 0.021, 0.022}, WHITE = {0.78, 0.78, 0.76}, RED = {0.58, 0.045, 0.03}, DARK = {0.028, 0.032, 0.036},
            GLASS = {0.012, 0.014, 0.015}, STONE = {0.10, 0.095, 0.085}, DECK = {0.06, 0.055, 0.05}, SOLAR = {0.02, 0.03, 0.07},
            GREY = {0.30, 0.31, 0.31}, GREEN_GLASS = {0.02, 0.06, 0.03}, ORANGE = {0.55, 0.12, 0.03},
            WOOD = {0.20, 0.115, 0.06}, LENS = {0.42, 0.50, 0.50}, BRASS = {0.62, 0.45, 0.18}, PAINT_IN = {0.55, 0.56, 0.52}, DOME_IN = {0.10, 0.12, 0.11};

    /** Ein Material: Art, Rostanfälligkeit, Rauheit, Porosität, Farbe. */
    record Mat(int kind, double sus, double rough, double por, double[] col) { }

    static final Mat M_BLACK = new Mat(K_RIVETED, 0.55, 0.55, 0.35, BLACK), M_WHITE = new Mat(K_RIVETED, 0.3, 0.42, 0.15, WHITE),
            M_RED = new Mat(K_RIVETED, 0.3, 0.42, 0.15, RED), M_WHITE_S = new Mat(K_STEEL, 0.25, 0.4, 0.15, WHITE),
            M_RED_S = new Mat(K_STEEL, 0.3, 0.42, 0.15, RED), M_DARK = new Mat(K_DOME, 0.15, 0.32, 0.1, DARK), M_GLASS = new Mat(K_GLASS, 0, 0.04, 0, GLASS),
            M_STONE = new Mat(K_STONE, 0, 0.9, 0.9, STONE), M_DECK = new Mat(K_DECK, 0.6, 0.75, 0.6, DECK), M_SOLAR = new Mat(K_SOLAR, 0, 0.12, 0, SOLAR),
            M_BLACK_S = new Mat(K_STEEL, 0.6, 0.55, 0.35, BLACK), M_GREY = new Mat(K_STEEL, 0.4, 0.5, 0.2, GREY), M_LAMP = new Mat(K_LAMP, 0, 0.3, 0, WHITE),
            M_ORANGE = new Mat(K_STEEL, 0.6, 0.5, 0.3, ORANGE),
            M_PLANKS = new Mat(K_PLANKS, 0, 0.6, 0.5, WOOD), M_BRASS = new Mat(K_BRASS, 0, 0.3, 0, BRASS), M_PAINT_IN = new Mat(K_STEEL, 0, 0.55, 0.1, PAINT_IN),
            M_DOME_IN = new Mat(K_STEEL, 0, 0.6, 0.1, DOME_IN), M_LENS = new Mat(K_LENS, 0, 0.05, 0, LENS);

    /** Teile des Netzes als Indexbereiche (erster Index, Anzahl). */
    public record Part(String name, int first, int count) { }

    public final float[] verts;
    public final int[] idx;
    public final List<Part> parts;
    /** Nur beim Innenraum (Phase 11): je Ecke 8 Werte Sichtbarkeit (7 Lampen, Anteil des Fensterlichts), sonst null. */
    public final float[] vis;

    private Turm(float[] v, int[] i, List<Part> p) { this(v, i, p, null); }

    private Turm(float[] v, int[] i, List<Part> p, float[] vis) { verts = v; idx = i; parts = p; this.vis = vis; }

    /** Zahl der Sichtbarkeitswerte je Ecke im Innenraum. */
    public static final int VIS = 8;

    /**
     * Phase 11: das Innere als eigenes Netz (Teil „Treppenhaus“), mit der Sichtbarkeit der Lampen und Fenster je Ecke
     * (einmal gerechnet, auf allen Kernen). Es wirft keine Schatten nach draußen und wird nur gezeichnet, wenn das Auge
     * im oder am Turm ist.
     */
    public static Turm interior() {
        B b = new B();
        Treppenhaus.build(b);
        float[] v = b.v();
        int n = v.length / STRIDE;
        float[] vis = new float[n * VIS];
        java.util.stream.IntStream.range(0, n).parallel().forEach(i -> {
            int o = i * STRIDE;
            float[] q = Innenraum.visibility(new double[]{v[o], v[o + 1], v[o + 2]}, new double[]{v[o + 3], v[o + 4], v[o + 5]});
            System.arraycopy(q, 0, vis, i * VIS, VIS);
        });
        int[] idx = b.i();
        return new Turm(v, idx, List.of(new Part("Treppenhaus", 0, idx.length)), vis);
    }

    /** Ecken mit Sichtbarkeit verschränkt (STRIDE + VIS Floats je Ecke) für die Grafikkarte. */
    public float[] interleaved() {
        if (vis == null) return verts;
        int n = vertexCount(), s = STRIDE + VIS;
        float[] f = new float[n * s];
        for (int i = 0; i < n; i++) {
            System.arraycopy(verts, i * STRIDE, f, i * s, STRIDE);
            System.arraycopy(vis, i * VIS, f, i * s + STRIDE, VIS);
        }
        return f;
    }

    public Part part(String name) {
        for (Part p : parts) if (p.name().equals(name)) return p;
        throw new IllegalArgumentException(name);
    }

    public int triangles() { return idx.length / 3; }
    public int vertexCount() { return verts.length / STRIDE; }

    // ================================================================== Bau

    /** Schaftradius auf Höhe y: leicht hohl ausgeschwungen wie auf Aussen1. */
    public static double shaftR(double y) {
        double f = Math.max(0, Math.min(1, (SHAFT_TOP - y) / (SHAFT_TOP - SOCKEL_TOP)));
        return SHAFT_R1 + (SHAFT_R0 - SHAFT_R1) * Math.pow(f, 1.35);
    }

    /** Ableitung dr/dy des Schafts. */
    static double shaftDr(double y) { return (shaftR(y + 0.01) - shaftR(y - 0.01)) / 0.02; }

    /** Sockelradius auf Höhe y (0..8 m). */
    public static double sockelR(double y) { return SOCKEL_R0 + (SOCKEL_R1 - SOCKEL_R0) * Math.max(0, Math.min(1, (y - 0.9) / (SOCKEL_TOP - 0.15 - 0.9))); }

    public static Turm build() {
        B b = new B();
        int seg = 96;

        // ---------- Turm (wirft Schatten aufs Wasser)
        int t0 = b.idx.size();
        // Sockel: unter der Platte beginnend, Fuß mit Wulst, verjüngt bis 7,85 m, Flansch, Absatz zum Schaft
        b.lathe(new double[][]{{SOCKEL_R0, PLATE_BOTTOM - 0.5}, {SOCKEL_R0, 0.6}}, seg, M_BLACK, 0.6, 1.3, 0.85);
        b.lathe(new double[][]{{SOCKEL_R0, 0.6}, {SOCKEL_R0 + 0.12, 0.62}, {SOCKEL_R0 + 0.12, 0.78}, {SOCKEL_R0, 0.9}}, seg, M_BLACK_S, 0.9, 0, 0.9);
        b.lathe(new double[][]{{SOCKEL_R0, 0.9}, {SOCKEL_R1, SOCKEL_TOP - 0.15}}, seg, M_BLACK, SOCKEL_TOP - 0.15, 1.3, 1.0);
        b.lathe(new double[][]{{SOCKEL_R1, SOCKEL_TOP - 0.15}, {SOCKEL_R1 + 0.15, SOCKEL_TOP - 0.15}, {SOCKEL_R1 + 0.15, SOCKEL_TOP + 0.05},
                {SHAFT_R0, SOCKEL_TOP + 0.05}}, seg, M_BLACK_S, SOCKEL_TOP + 0.05, 0, 1.0);
        // Schaft in Farbringen; Normalen aus der Ableitung, damit die Ringe ohne Kante ineinander übergehen
        double[][] bands = {{SOCKEL_TOP + 0.05, BAND1}, {BAND1, BAND2}, {BAND2, SHAFT_TOP}};
        Mat[] bandMat = {M_WHITE, M_RED, M_WHITE};
        for (int k = 0; k < 3; k++) {
            List<double[]> p = new ArrayList<>();
            int n = 14;
            for (int i = 0; i <= n; i++) {
                double y = bands[k][0] + (bands[k][1] - bands[k][0]) * i / n;
                double dr = shaftDr(y), l = Math.hypot(1, dr);
                p.add(new double[]{shaftR(y), y, 1 / l, -dr / l});
            }
            b.lathe(p.toArray(new double[0][]), seg, bandMat[k], bands[k][1], 1.25, 1.0);
        }
        // Dienstraum: Gesims unten, roter Mantel, Erker, Galerie
        b.lathe(new double[][]{{SHAFT_R1, SHAFT_TOP}, {ROOM_R + 0.08, SHAFT_TOP}, {ROOM_R + 0.08, SHAFT_TOP + 0.12}, {ROOM_R, SHAFT_TOP + 0.12}}, seg, M_RED_S, SHAFT_TOP + 0.12, 0, 0.8);
        b.lathe(new double[][]{{ROOM_R, SHAFT_TOP + 0.12}, {ROOM_R, ROOM_TOP}}, seg, M_RED, ROOM_TOP, 1.0, 0.85);
        for (int e = 0; e < 3; e++) b.erker(ERKER_AZ[e], e == 2);
        // Galerie: Gesims, Boden mit Unterseite, Geländer
        b.lathe(new double[][]{{ROOM_R, ROOM_TOP - 0.25}, {GALLERY_R - 0.25, ROOM_TOP - 0.25}, {GALLERY_R, ROOM_TOP}, {GALLERY_R, GALLERY_TOP},
                {LANTERN_R + 0.15, GALLERY_TOP}}, seg, M_WHITE_S, GALLERY_TOP, 0, 0.75);
        b.railing(GALLERY_R - 0.1, GALLERY_TOP, 1.05, 60, false);
        // Laterne: Sockelwand, Glas mit Sprossen, Dachring, Kuppel, Knauf, Blitzableiter
        b.lathe(new double[][]{{LANTERN_R + 0.15, GALLERY_TOP}, {LANTERN_R + 0.15, GLASS_BOTTOM}, {LANTERN_R, GLASS_BOTTOM}}, seg, M_WHITE_S, GLASS_BOTTOM, 0, 0.95);
        b.lathe(new double[][]{{LANTERN_R, GLASS_BOTTOM}, {LANTERN_R, GLASS_TOP}}, seg, M_GLASS, GLASS_TOP, 0, 1.0);
        b.mullions(LANTERN_R, GLASS_BOTTOM, GLASS_TOP, 12);
        b.lathe(new double[][]{{LANTERN_R, GLASS_TOP}, {LANTERN_R + 0.22, GLASS_TOP}, {LANTERN_R + 0.22, GLASS_TOP + 0.2}, {LANTERN_R + 0.05, GLASS_TOP + 0.2}}, seg, M_DARK, GLASS_TOP + 0.2, 0, 0.9);
        List<double[]> dome = new ArrayList<>();
        for (int i = 0; i <= 16; i++) {
            double a = Math.PI / 2 * i / 16;
            double r = (LANTERN_R + 0.05) * Math.cos(a), y = GLASS_TOP + 0.2 + (DOME_TOP - GLASS_TOP - 0.2) * Math.sin(a);
            // Normale einer Ellipse mit Halbachsen R, H
            double nr = Math.cos(a) / (LANTERN_R + 0.05), ny = Math.sin(a) / (DOME_TOP - GLASS_TOP - 0.2), l = Math.hypot(nr, ny);
            dome.add(new double[]{Math.max(r, 0.09), y, nr / l, ny / l});
        }
        b.lathe(dome.toArray(new double[0][]), seg, M_DARK, DOME_TOP, 0, 1.0);
        b.lathe(new double[][]{{0.09, DOME_TOP}, {0.09, DOME_TOP + 0.1}}, 16, M_DARK, DOME_TOP + 0.1, 0, 1.0);
        b.sphere(0, DOME_TOP + 0.36, 0, 0.26, M_DARK);
        b.lathe(new double[][]{{0.025, DOME_TOP + 0.6}, {0.025, KNOB_TOP + 0.4}, {0.0, KNOB_TOP + 0.5}}, 8, M_DARK, KNOB_TOP + 0.5, 0, 1.0);
        // Mast mit Leiter und Antennen, Solarpaneele auf der Galerie
        b.mast();
        for (double az : new double[]{150, 180, 210}) b.solar(az);
        // Tür, Leiter, Bullaugen, Fenster
        b.door();
        b.ladder();
        double[][] port = {{10.9, 60}, {10.9, 300}, {14.4, 20}, {14.4, 110}, {14.4, 245}, {14.4, 335}, {18.0, 80}, {18.0, 260}};
        for (double[] p : port) b.porthole(p[1], p[0]);
        for (double az : new double[]{0, 112.5, 247.5}) b.archWindow(az);
        int t1 = b.idx.size();

        // ---------- Stahlmanschette (oval, rostig, mit Algen; bei Hochwasser unter Wasser)
        int p0 = b.idx.size();
        b.plate();
        int p1 = b.idx.size();

        // ---------- Steinschüttung
        int r0 = b.idx.size();
        b.riprap(new Random(1885), 300);
        int r1 = b.idx.size();

        // ---------- Peilpfahl
        int f0 = b.idx.size();
        double pa = Math.toRadians(PFAHL_AZ), px = Math.sin(pa) * PFAHL_DIST, pz = -Math.cos(pa) * PFAHL_DIST;
        b.latheAt(px, pz, new double[][]{{0.35, -5}, {0.35, 2.4}}, 24, M_BLACK_S, 2.4, 0, 1.0);
        b.latheAt(px, pz, new double[][]{{0.35, 2.4}, {0.35, 3.4}}, 24, M_ORANGE, 3.4, 0, 1.0);
        b.latheAt(px, pz, new double[][]{{0.35, 3.4}, {0.0, 3.45}}, 24, M_BLACK_S, 3.45, 0, 1.0);
        int f1 = b.idx.size();

        // ---------- Laternenraum (Phase 8): Dielen, Brüstung, Kämpfer und Kuppel von innen, Reling, Linse
        int l0 = b.idx.size();
        b.lanternRoom();
        int l1 = b.idx.size();

        // ---------- Handlauf der Galerie (Phase 8b)
        int h0 = b.idx.size();
        b.handrail();
        int h1 = b.idx.size();

        // ---------- Alte Weser am Horizont (Erdkrümmung eingerechnet)
        int a0 = b.idx.size();
        b.alteWeser();
        int a1 = b.idx.size();

        List<Part> parts = List.of(new Part("Turm", t0, t1 - t0), new Part("Manschette", p0, p1 - p0), new Part("Steinschüttung", r0, r1 - r0),
                new Part("Peilpfahl", f0, f1 - f0), new Part("Laternenraum", l0, l1 - l0),
                new Part("Handlauf", h0, h1 - h0), new Part("Alte Weser", a0, a1 - a0));
        return new Turm(b.v(), b.i(), parts);
    }

    /** Lage der Alten Weser in der Szene {x, z, Absenkung durch die Erdkrümmung}. */
    public static double[] alteWeserPos() {
        double x = Site.x(ALTE_WESER_LON), z = Site.z(ALTE_WESER_LAT);
        return new double[]{x, z, Site.curvatureDrop(Math.hypot(x, z))};
    }

    /** Die ovale Manschette als Kreise für das Meer (Brandungsring, kein Wasser innen): {x, z, r} je Kreis. */
    public static double[][] plateCircles() {
        double c = Math.cos(Math.toRadians(PLATE_ROT)), s = Math.sin(Math.toRadians(PLATE_ROT));
        // größter Kreis im Oval um (u0, 0): b·√(1 − u0²/(a² − b²)), solange u0 ≤ (a² − b²)/a
        double u0 = 2.5, r0 = PLATE_B * Math.sqrt(1 - u0 * u0 / (PLATE_A * PLATE_A - PLATE_B * PLATE_B));
        double[][] l = {{0, 0, PLATE_B}, {u0, 0, r0}, {-u0, 0, r0}};
        double[][] out = new double[l.length][];
        for (int i = 0; i < l.length; i++) out[i] = new double[]{l[i][0] * c, -l[i][0] * s, l[i][2]};
        return out;
    }

    /** Liegt (x, z) auf der Manschette? */
    public static boolean onPlate(double x, double z) {
        double c = Math.cos(Math.toRadians(PLATE_ROT)), s = Math.sin(Math.toRadians(PLATE_ROT));
        double u = x * c - z * s, w = x * s + z * c;
        return (u * u) / (PLATE_A * PLATE_A) + (w * w) / (PLATE_B * PLATE_B) <= 1;
    }

    // ================================================================== Prüfhilfen

    /** Zahl der falsch gewundenen Dreiecke (Flächennormale gegen die Eckennormalen). */
    public int wrongWinding() {
        int bad = 0;
        for (int t = 0; t < idx.length; t += 3) {
            int a = idx[t] * STRIDE, b = idx[t + 1] * STRIDE, c = idx[t + 2] * STRIDE;
            double ux = verts[b] - verts[a], uy = verts[b + 1] - verts[a + 1], uz = verts[b + 2] - verts[a + 2];
            double wx = verts[c] - verts[a], wy = verts[c + 1] - verts[a + 1], wz = verts[c + 2] - verts[a + 2];
            double nx = uy * wz - uz * wy, ny = uz * wx - ux * wz, nz = ux * wy - uy * wx;
            double area = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (area < 1e-9) continue;
            double sx = verts[a + 3] + verts[b + 3] + verts[c + 3], sy = verts[a + 4] + verts[b + 4] + verts[c + 4], sz = verts[a + 5] + verts[b + 5] + verts[c + 5];
            if (nx * sx + ny * sy + nz * sz <= 0) bad++;
        }
        return bad;
    }

    /** Höchster Punkt eines Teils. */
    public double maxY(String part) {
        Part p = part(part);
        double m = -1e9;
        for (int i = p.first(); i < p.first() + p.count(); i++) m = Math.max(m, verts[idx[i] * STRIDE + 1]);
        return m;
    }

    /** Größter Abstand von der Achse eines Teils zwischen y0 und y1. */
    public double maxR(String part, double y0, double y1) {
        Part p = part(part);
        double m = 0;
        for (int i = p.first(); i < p.first() + p.count(); i++) {
            int o = idx[i] * STRIDE;
            if (verts[o + 1] >= y0 && verts[o + 1] <= y1) m = Math.max(m, Math.hypot(verts[o], verts[o + 2]));
        }
        return m;
    }

    // ================================================================== Netzbauer

    static final class B {
        final List<float[]> vs = new ArrayList<>();
        final List<Integer> idx = new ArrayList<>();

        int vert(double x, double y, double z, double nx, double ny, double nz, Mat m, double s, double t, double edge, double seam, double ao) {
            double l = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (l < 1e-12) { nx = 0; ny = 1; nz = 0; l = 1; }
            vs.add(new float[]{(float) x, (float) y, (float) z, (float) (nx / l), (float) (ny / l), (float) (nz / l),
                    (float) m.col()[0], (float) m.col()[1], (float) m.col()[2], m.kind(), (float) m.sus(), (float) m.rough(), (float) m.por(),
                    (float) s, (float) t, (float) edge, (float) seam, (float) ao});
            return vs.size() - 1;
        }

        void tri(int a, int b, int c) { idx.add(a); idx.add(b); idx.add(c); }

        void quad(int a, int b, int c, int d) { tri(a, b, c); tri(a, c, d); }

        float[] v() {
            float[] f = new float[vs.size() * STRIDE];
            for (int i = 0; i < vs.size(); i++) System.arraycopy(vs.get(i), 0, f, i * STRIDE, STRIDE);
            return f;
        }

        int[] i() { return idx.stream().mapToInt(Integer::intValue).toArray(); }

        /** Umgebungslicht am Turm: unter der Galerie und am Fuß der Erker dunkler. */
        static double aoAt(double y, double r, double base) {
            double ao = base;
            if (y < ROOM_TOP && y > ROOM_TOP - 1.4 && r < GALLERY_R + 0.05) ao *= 1 - 0.45 * Math.pow((y - (ROOM_TOP - 1.4)) / 1.4, 2);
            if (y < 0.9 && y > -0.2) ao *= 0.8 + 0.2 * (y + 0.2) / 1.1;
            return ao;
        }

        /** Rotationskörper um die Turmachse. Profilpunkte {r, y} (Normale aus den Nachbarn) oder {r, y, nr, ny}. */
        void lathe(double[][] p, int seg, Mat m, double edge, double seam, double ao) { latheAt(0, 0, p, seg, m, edge, seam, ao); }

        void latheAt(double cx, double cz, double[][] p, int seg, Mat m, double edge, double seam, double ao) {
            latheArc(cx, cz, p, seg, 0, 360, m, edge, seam, ao, 0);
        }

        /**
         * Rotationskörper um (cx, cz) von Azimut a0 bis a1 (Grad von Nord im Uhrzeigersinn, x = sin, z = −cos);
         * jede Kante zwischen zwei Profilpunkten bekommt eigene Ecken (harte Kanten), außer die Punkte bringen ihre
         * Normale mit. s läuft entlang des Umfangs in m (plus sOff).
         */
        void latheArc(double cx, double cz, double[][] p, int seg, double a0, double a1, Mat m, double edge, double seam, double ao, double sOff) {
            // s mit einem mittleren Halbmesser je Körper: so laufen Rostläufer auch auf Kegeln senkrecht
            double rRef = 0;
            for (double[] q : p) rRef += q[0];
            rRef = Math.max(0.05, rRef / p.length);
            for (int k = 0; k + 1 < p.length; k++) {
                double[] q0 = p[k], q1 = p[k + 1];
                double dr = q1[0] - q0[0], dy = q1[1] - q0[1], l = Math.hypot(dr, dy);
                if (l < 1e-9) continue;
                // Außennormale im Profil: (dy, −dr), siehe Klassenkommentar zur Wicklung
                double[] n0 = q0.length > 2 ? new double[]{q0[2], q0[3]} : new double[]{dy / l, -dr / l};
                double[] n1 = q1.length > 2 ? new double[]{q1[2], q1[3]} : new double[]{dy / l, -dr / l};
                int base = vs.size();
                for (int r = 0; r < 2; r++) {
                    double[] q = r == 0 ? q0 : q1, n = r == 0 ? n0 : n1;
                    for (int s = 0; s <= seg; s++) {
                        double az = Math.toRadians(a0 + (a1 - a0) * s / seg);
                        double sx = Math.sin(az), sz = -Math.cos(az);
                        double arc = sOff + rRef * az;
                        vert(cx + q[0] * sx, q[1], cz + q[0] * sz, n[0] * sx, n[1], n[0] * sz, m, arc, q[1], edge, seam, aoAt(q[1], Math.hypot(cx + q[0] * sx, cz + q[0] * sz), ao));
                    }
                }
                for (int s = 0; s < seg; s++) {
                    int b0 = base + s, b1 = b0 + 1, c0 = b0 + seg + 1, c1 = c0 + 1;
                    // Azimut im Uhrzeigersinn von oben: außen gegen den Uhrzeigersinn heißt b0, c0, c1 (siehe Prüfung)
                    tri(b0, c1, b1);
                    tri(b0, c0, c1);
                }
            }
        }

        /** Ein Erker: Zylinder auf spitzer Konsole, Dach unter der Galerie oder (der hohe) mit eigener Kuppel. */
        void erker(double azDeg, boolean high) {
            double a = Math.toRadians(azDeg), cx = Math.sin(a) * ERKER_D, cz = -Math.cos(a) * ERKER_D;
            int seg = 40;
            double top = high ? ERKER_HIGH_TOP : ROOM_TOP;
            // Konsole: hohl geschwungen bis zur Spitze, Zapfen darunter
            List<double[]> c = new ArrayList<>();
            c.add(new double[]{0.0, SHAFT_TOP - 2.45});
            c.add(new double[]{0.06, SHAFT_TOP - 2.4});
            c.add(new double[]{0.09, SHAFT_TOP - 2.3});
            c.add(new double[]{0.07, SHAFT_TOP - 2.2});
            for (int i = 0; i <= 10; i++) {
                double f = i / 10.0, y = SHAFT_TOP - 2.2 + 2.2 * f;
                c.add(new double[]{0.08 + (ERKER_R - 0.08) * Math.pow(f, 0.85), y});
            }
            latheArc(cx, cz, c.toArray(new double[0][]), seg, 0, 360, M_RED_S, SHAFT_TOP, 0, 0.8, azDeg * 10);
            // Mantel und Gesims
            latheArc(cx, cz, new double[][]{{ERKER_R, SHAFT_TOP}, {ERKER_R + 0.06, SHAFT_TOP}, {ERKER_R + 0.06, SHAFT_TOP + 0.15}, {ERKER_R, SHAFT_TOP + 0.15}},
                    seg, 0, 360, M_RED_S, SHAFT_TOP + 0.15, 0, 0.85, azDeg * 10);
            if (!high) {
                latheArc(cx, cz, new double[][]{{ERKER_R, SHAFT_TOP + 0.15}, {ERKER_R, ROOM_TOP}}, seg, 0, 360, M_RED, ROOM_TOP, 1.0, 0.85, azDeg * 10);
            } else {
                latheArc(cx, cz, new double[][]{{ERKER_R, SHAFT_TOP + 0.15}, {ERKER_R, GALLERY_TOP + 0.2}}, seg, 0, 360, M_RED, GALLERY_TOP + 0.2, 1.0, 0.85, azDeg * 10);
                // Nebenfeuer: Glasband, Dachring, kleine Kuppel
                latheArc(cx, cz, new double[][]{{ERKER_R, GALLERY_TOP + 0.2}, {ERKER_R, top}}, seg, 0, 360, M_GLASS, top, 0, 1.0, 0);
                for (int i = 0; i < 8; i++) {
                    double aa = Math.toRadians(i * 45);
                    box(cx + Math.sin(aa) * (ERKER_R + 0.01), (GALLERY_TOP + 0.2 + top) / 2, cz - Math.cos(aa) * (ERKER_R + 0.01), aa, 0.06, top - GALLERY_TOP - 0.2, 0.05, M_WHITE_S, 1);
                }
                latheArc(cx, cz, new double[][]{{ERKER_R, top}, {ERKER_R + 0.12, top}, {ERKER_R + 0.12, top + 0.12}, {ERKER_R, top + 0.12}}, seg, 0, 360, M_DARK, top + 0.12, 0, 0.9, 0);
                List<double[]> d = new ArrayList<>();
                for (int i = 0; i <= 10; i++) {
                    double t = Math.PI / 2 * i / 10, R = ERKER_R, H = 0.8;
                    double nr = Math.cos(t) / R, ny = Math.sin(t) / H, l = Math.hypot(nr, ny);
                    d.add(new double[]{Math.max(0.05, R * Math.cos(t)), top + 0.12 + H * Math.sin(t), nr / l, ny / l});
                }
                latheArc(cx, cz, d.toArray(new double[0][]), seg, 0, 360, M_DARK, top + 1, 0, 1.0, 0);
                sphereAt(cx, top + 1.0, cz, 0.12, M_DARK);
            }
            // zwei kleine Fenster je Erker, schräg nach außen
            for (int k = -1; k <= 1; k += 2) {
                double wa = a + Math.toRadians(32 * k);
                double fx = cx + Math.sin(wa) * (ERKER_R + 0.012), fz = cz - Math.cos(wa) * (ERKER_R + 0.012);
                box(fx, 21.1, fz, wa, 0.52, 0.9, 0.04, M_WHITE_S, 1);
                box(fx + Math.sin(wa) * 0.022, 21.1, fz - Math.cos(wa) * 0.022, wa, 0.38, 0.74, 0.02, M_GLASS, 1);
            }
        }

        /**
         * Der Laternenraum von innen (Innen1): Dielenboden auf Galeriehöhe, die weiße Brüstung bis zum Glas mit einer
         * Fensterbank, der Kämpfer und die Kuppel von innen, eine Reling 0,45 m vor den Scheiben, in der Mitte der
         * Linsensockel mit einer Gürtellinse aus Glasringen zwischen Messingringen und der Lampe (die echte Linse kommt
         * mit Phase 9). Alle Flächen zeigen nach innen; von außen sieht man davon nur, was durchs Glas fällt.
         */
        void lanternRoom() {
            double fy = GALLERY_TOP + 0.02, ri = LANTERN_R - 0.02, mid = (GLASS_BOTTOM + GLASS_TOP) / 2;
            int seg = 72;
            // Phase 11: Dielen mit der Luke zum Dienstraum (gleiches Raster wie dessen Decke), der Deckel steht offen
            new Treppenhaus.Run(this).polar(fy, new double[]{0, 0.3, Innenraum.HATCH_R0, Innenraum.HATCH_R1, ri}, 0, 120, 360, true, M_PLANKS, 0.55,
                    (r, a) -> !Treppenhaus.Run.hatchCell(r, a));
            double ha = Math.toRadians(Innenraum.HATCH_AZ), hr = Innenraum.HATCH_R1 - 0.04;
            box(Math.sin(ha) * hr, fy + 0.36, -Math.cos(ha) * hr, ha, 0.62, 0.72, 0.04, M_PLANKS, 0.6);
            lathe(new double[][]{{ri, GLASS_BOTTOM - 0.05}, {ri, fy}}, seg, M_PAINT_IN, GLASS_BOTTOM, 0, 0.5);
            lathe(new double[][]{{LANTERN_R - 0.005, GLASS_BOTTOM}, {ri - 0.1, GLASS_BOTTOM}, {ri - 0.1, GLASS_BOTTOM - 0.05}, {ri, GLASS_BOTTOM - 0.05}}, seg, M_PAINT_IN, GLASS_BOTTOM, 0, 0.6);
            lathe(new double[][]{{ri - 0.01, mid + 0.04}, {ri - 0.01, mid - 0.04}}, seg, M_PAINT_IN, mid + 0.04, 0, 0.6);
            lathe(new double[][]{{ri - 0.01, mid - 0.04}, {LANTERN_R, mid - 0.04}}, seg, M_PAINT_IN, mid, 0, 0.6);
            lathe(new double[][]{{LANTERN_R, mid + 0.04}, {ri - 0.01, mid + 0.04}}, seg, M_PAINT_IN, mid + 0.04, 0, 0.6);
            // Dachring und Kuppel von innen
            lathe(new double[][]{{ri - 0.06, GLASS_TOP + 0.18}, {ri - 0.06, GLASS_TOP}, {LANTERN_R - 0.005, GLASS_TOP}}, seg, M_PAINT_IN, GLASS_TOP + 0.18, 0, 0.45);
            List<double[]> dome = new ArrayList<>();
            double h = DOME_TOP - GLASS_TOP - 0.3, r0 = ri - 0.06;
            for (int i = 16; i >= 0; i--) {
                double a = Math.PI / 2 * i / 16;
                double rr = r0 * Math.cos(a), yy = GLASS_TOP + 0.18 + h * Math.sin(a);
                double nr = Math.cos(a) / r0, ny = Math.sin(a) / h, l = Math.hypot(nr, ny);
                dome.add(new double[]{Math.max(rr, 0.0), yy, -nr / l, -ny / l});
            }
            // Profil vom Scheitel nach außen: so zeigt die Normale nach innen
            lathe(dome.toArray(new double[0][]), seg, M_DOME_IN, DOME_TOP, 0, 0.35);
            // Reling vor den Scheiben
            railing(LANTERN_R - 0.45, fy, 0.95, 12, true);
            // Linsensockel, Lampe, Gürtellinse aus Glasringen
            lathe(new double[][]{{0.42, fy}, {0.42, fy + 0.08}, {0.30, fy + 0.12}, {0.30, fy + 0.72}, {0.40, fy + 0.76}, {0.40, fy + 0.82}, {0.0, fy + 0.82}}, 48, M_GREY, fy + 0.82, 0, 0.7);
            double l0 = fy + 0.84, lh = 0.95, lr = 0.48;
            lathe(new double[][]{{lr + 0.02, l0}, {lr + 0.02, l0 + 0.05}, {lr - 0.04, l0 + 0.05}}, 48, M_BRASS, l0 + 0.05, 0, 0.8);
            // Phase 9: die Gürtellinse genau so, wie com.dan.beacon sie rechnet (25 Prismenringe mit gekrümmter Außenfläche)
            double lc = LENS_Y;
            List<double[]> prism = new ArrayList<>();
            for (double[] q : com.dan.beacon.Lens.drum().prisms()) {
                // Außenkurve von unten nach oben, dann die Stufe zurück zur Innenkante
                for (int k = q.length / 2 - 1; k >= 2; k--) prism.add(new double[]{q[2 * k], lc + q[2 * k + 1]});
                prism.add(new double[]{q[2], lc + q[3]});
            }
            lathe(prism.toArray(new double[0][]), 48, M_LENS, l0 + lh, 0, 1.0);
            lathe(new double[][]{{lr - 0.04, l0 + lh - 0.05}, {lr + 0.02, l0 + lh - 0.05}, {lr + 0.02, l0 + lh}, {0.12, l0 + lh + 0.02}}, 48, M_BRASS, l0 + lh, 0, 0.8);
            sphere(0, l0 + lh * 0.5, 0, 0.09, M_LAMP);
            lathe(new double[][]{{0.03, l0 + 0.05}, {0.03, l0 + lh * 0.5 - 0.08}}, 12, M_BRASS, l0 + lh * 0.5, 0, 0.8);
        }

        /** Senkrechte Sprossen und ein Kämpfer in der Laterne. */
        void mullions(double r, double y0, double y1, int n) {
            for (int i = 0; i < n; i++) {
                double a = Math.toRadians(i * 360.0 / n + 15);
                box(Math.sin(a) * (r + 0.02), (y0 + y1) / 2, -Math.cos(a) * (r + 0.02), a, 0.07, y1 - y0, 0.06, M_WHITE_S, 1);
            }
            latheArc(0, 0, new double[][]{{r, (y0 + y1) / 2 - 0.04}, {r + 0.05, (y0 + y1) / 2 - 0.04}, {r + 0.05, (y0 + y1) / 2 + 0.04}, {r, (y0 + y1) / 2 + 0.04}},
                    96, 0, 360, M_WHITE_S, (y0 + y1) / 2, 0, 1, 0);
        }

        /** Geländer: Pfosten und zwei Handläufe. */
        void railing(double r, double y, double h, int posts, boolean top) {
            for (int i = 0; i < posts; i++) {
                double a = Math.toRadians(i * 360.0 / posts);
                box(Math.sin(a) * r, y + h / 2, -Math.cos(a) * r, a, 0.05, h, 0.05, M_WHITE_S, 1);
            }
            for (double yy : top ? new double[]{y + h, y + h * 0.5} : new double[]{y + h * 0.5}) {
                double t = yy == y + h ? 0.035 : 0.022;
                latheArc(0, 0, new double[][]{{r - t, yy - t}, {r + t, yy - t}, {r + t, yy + t}, {r - t, yy + t}, {r - t, yy - t}}, 120, 0, 360, M_WHITE_S, yy + t, 0, 1, 0);
            }
        }

        /** Der runde Handlauf der Galerie: ein Rohr um den Turm (eigener Teil, damit die Scheiben-Simulation ihn nass macht). */
        void handrail() {
            int k = 20;
            double[][] p = new double[k + 1][];
            for (int i = 0; i <= k; i++) {
                double a = 2 * Math.PI * i / k;
                p[i] = new double[]{HANDRAIL_RING + HANDRAIL_T * Math.cos(a), HANDRAIL_Y + HANDRAIL_T * Math.sin(a), Math.cos(a), Math.sin(a)};
            }
            latheArc(0, 0, p, 360, 0, 360, M_WHITE_S, HANDRAIL_Y + HANDRAIL_T, 0, 1.0, 0);
        }

        /** Quader mit Mitte (x, y, z), gedreht um den Azimut az (Breite quer, Tiefe radial nach außen). */
        void box(double x, double y, double z, double az, double w, double h, double d, Mat m, double ao) {
            double ox = Math.sin(az), oz = -Math.cos(az);   // nach außen
            double tx = Math.cos(az), tz = Math.sin(az);    // quer (im Uhrzeigersinn von oben)
            double[][] c = new double[8][];
            for (int i = 0; i < 8; i++) {
                double sw = (i & 1) == 0 ? -w / 2 : w / 2, sh = (i & 2) == 0 ? -h / 2 : h / 2, sd = (i & 4) == 0 ? -d / 2 : d / 2;
                c[i] = new double[]{x + tx * sw + ox * sd, y + sh, z + tz * sw + oz * sd};
            }
            // Flächen: Ecken gegen den Uhrzeigersinn von außen gesehen, mit Normale
            face(c[4], c[5], c[7], c[6], ox, 0, oz, m, ao, y + h / 2);      // außen
            face(c[1], c[0], c[2], c[3], -ox, 0, -oz, m, ao, y + h / 2);    // innen
            face(c[0], c[4], c[6], c[2], -tx, 0, -tz, m, ao, y + h / 2);    // links
            face(c[5], c[1], c[3], c[7], tx, 0, tz, m, ao, y + h / 2);      // rechts
            face(c[2], c[6], c[7], c[3], 0, 1, 0, m, ao, y + h / 2);        // oben
            face(c[0], c[1], c[5], c[4], 0, -1, 0, m, ao * 0.8, y + h / 2); // unten
        }

        /** Viereck; die Wicklung wird an der Normale ausgerichtet. */
        void face(double[] a, double[] b, double[] c, double[] d, double nx, double ny, double nz, Mat m, double ao, double edge) {
            double ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2], wx = c[0] - a[0], wy = c[1] - a[1], wz = c[2] - a[2];
            double fx = uy * wz - uz * wy, fy = uz * wx - ux * wz, fz = ux * wy - uy * wx;
            double[][] q = fx * nx + fy * ny + fz * nz >= 0 ? new double[][]{a, b, c, d} : new double[][]{a, d, c, b};
            int[] k = new int[4];
            for (int i = 0; i < 4; i++) {
                double s = q[i][0] * nz - q[i][2] * nx + q[i][2] * ny;
                k[i] = vert(q[i][0], q[i][1], q[i][2], nx, ny, nz, m, s, q[i][1], edge, 0, ao);
            }
            quad(k[0], k[1], k[2], k[3]);
        }

        void sphere(double x, double y, double z, double r, Mat m) { sphereAt(x, y, z, r, m); }

        void sphereAt(double x, double y, double z, double r, Mat m) {
            int st = 10, sl = 16;
            int base = vs.size();
            for (int i = 0; i <= st; i++) {
                double phi = Math.PI * i / st - Math.PI / 2;
                for (int j = 0; j <= sl; j++) {
                    double th = 2 * Math.PI * j / sl;
                    double nx = Math.cos(phi) * Math.sin(th), ny = Math.sin(phi), nz = -Math.cos(phi) * Math.cos(th);
                    vert(x + r * nx, y + r * ny, z + r * nz, nx, ny, nz, m, th * r, y + r * ny, y + r, 0, 1);
                }
            }
            for (int i = 0; i < st; i++)
                for (int j = 0; j < sl; j++) {
                    int a = base + i * (sl + 1) + j, b = a + 1, c = a + sl + 1, d = c + 1;
                    tri(a, d, b);
                    tri(a, c, d);
                }
        }

        /** Mast mit Leiter, Querträger und zwei Antennen am Rand der Galerie. */
        void mast() {
            double a = Math.toRadians(MAST_AZ), r = GALLERY_R - 0.35, x = Math.sin(a) * r, z = -Math.cos(a) * r;
            double h = MAST_TOP - GALLERY_TOP;
            box(x, GALLERY_TOP + h / 2, z, a, 0.1, h, 0.1, M_WHITE_S, 1);
            double tx = Math.cos(a), tz = Math.sin(a);
            for (int s = -1; s <= 1; s += 2) box(x + tx * 0.2 * s - Math.sin(a) * 0.12, GALLERY_TOP + (h - 1.2) / 2, z + tz * 0.2 * s + Math.cos(a) * 0.12, a, 0.03, h - 1.2, 0.03, M_WHITE_S, 1);
            for (double y = GALLERY_TOP + 0.4; y < MAST_TOP - 1.3; y += 0.35)
                box(x - Math.sin(a) * 0.12, y, z + Math.cos(a) * 0.12, a, 0.4, 0.025, 0.025, M_WHITE_S, 1);
            box(x, MAST_TOP - 1.0, z, a, 1.3, 0.05, 0.05, M_WHITE_S, 1);
            box(x + tx * 0.6, MAST_TOP - 0.7, z + tz * 0.6, a, 0.025, 1.4, 0.025, M_GREY, 1);
            box(x - tx * 0.6, MAST_TOP - 0.9, z - tz * 0.6, a, 0.025, 1.0, 0.025, M_GREY, 1);
        }

        /** Solarpaneel auf der Galerie, 35° geneigt, nach außen. */
        void solar(double azDeg) {
            double a = Math.toRadians(azDeg), r = 3.25, x = Math.sin(a) * r, z = -Math.cos(a) * r;
            double ox = Math.sin(a), oz = -Math.cos(a), tx = Math.cos(a), tz = Math.sin(a);
            double tilt = Math.toRadians(35), w = 1.0, l = 0.62;
            double y0 = GALLERY_TOP + 0.25;
            // Ecken: unten außen, oben innen (geneigt nach außen)
            double cy = Math.cos(tilt) * l / 2, cr = Math.sin(tilt) * l / 2;
            double[] p0 = {x - tx * w / 2 + ox * cr, y0 - cy, z - tz * w / 2 + oz * cr}, p1 = {x + tx * w / 2 + ox * cr, y0 - cy, z + tz * w / 2 + oz * cr},
                    p2 = {x + tx * w / 2 - ox * cr, y0 + cy, z + tz * w / 2 - oz * cr}, p3 = {x - tx * w / 2 - ox * cr, y0 + cy, z - tz * w / 2 - oz * cr};
            double nx = ox * Math.cos(tilt), ny = Math.sin(tilt), nz = oz * Math.cos(tilt);
            face(p0, p1, p2, p3, nx, ny, nz, M_SOLAR, 1, y0);
            face(p0, p3, p2, p1, -nx, -ny, -nz, M_GREY, 0.7, y0);
            box(x, GALLERY_TOP + 0.1, z, a, 0.05, 0.2, 0.05, M_GREY, 0.8);
        }

        /** Eingangstür unten im ersten weißen Ring mit Rahmen und kleinem Vordach. */
        void door() {
            double a = Math.toRadians(DOOR_AZ), y0 = SOCKEL_TOP + 0.05, h = 2.05;
            double r = shaftR(y0 + h / 2) + 0.03;
            double x = Math.sin(a) * r, z = -Math.cos(a) * r;
            box(x, y0 + h / 2, z, a, 1.15, h + 0.1, 0.06, M_WHITE_S, 0.95);
            box(x + Math.sin(a) * 0.035, y0 + h / 2 - 0.02, z - Math.cos(a) * 0.035, a, 0.9, h - 0.12, 0.02, new Mat(K_STEEL, 0.4, 0.45, 0.15, new double[]{0.70, 0.70, 0.68}), 0.9);
            box(x + Math.sin(a) * 0.1, y0 + h + 0.12, z - Math.cos(a) * 0.1, a, 1.4, 0.08, 0.3, M_WHITE_S, 0.9);
            // Podest vor der Tür auf dem Flansch
            box(Math.sin(a) * (SOCKEL_R1 + 0.45), y0 - 0.02, -Math.cos(a) * (SOCKEL_R1 + 0.45), a, 1.4, 0.06, 1.2, M_GREY, 0.9);
        }

        /** Senkrechte Leiter am Sockel von der Plattform bis zum Podest (folgt der Verjüngung). */
        void ladder() {
            double a = Math.toRadians(DOOR_AZ), tx = Math.cos(a), tz = Math.sin(a), ox = Math.sin(a), oz = -Math.cos(a);
            int n = 26;
            for (int s = -1; s <= 1; s += 2)
                for (int i = 0; i < n; i++) {
                    double y = PLATE_TOP + (SOCKEL_TOP - PLATE_TOP) * (i + 0.5) / n, r = sockelR(y) + 0.28;
                    box(ox * r + tx * 0.24 * s, y, oz * r + tz * 0.24 * s, a, 0.035, (SOCKEL_TOP - PLATE_TOP) / n + 0.01, 0.06, M_BLACK_S, 0.9);
                }
            for (double y = PLATE_TOP + 0.3; y < SOCKEL_TOP - 0.1; y += 0.3) {
                double r = sockelR(y) + 0.28;
                box(ox * r, y, oz * r, a, 0.5, 0.03, 0.03, M_BLACK_S, 0.9);
            }
        }

        /** Bullauge: Rahmen (Ring) und dunkles Glas, auf dem Schaft ausgerichtet. */
        void porthole(double azDeg, double y) {
            double a = Math.toRadians(azDeg);
            double r = shaftR(y), dr = shaftDr(y), l = Math.hypot(1, dr);
            double nr = 1 / l, ny = -dr / l;
            double nx = Math.sin(a) * nr, nz = -Math.cos(a) * nr;
            double tx = Math.cos(a), tz = Math.sin(a);
            // „oben“ in der Fläche: senkrecht zu n und t
            double ux = ny * tz - nz * 0, uy = nz * tx - nx * tz, uz = nx * 0 - ny * tx;
            double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
            ux /= ul; uy /= ul; uz /= ul;
            if (uy < 0) { ux = -ux; uy = -uy; uz = -uz; }
            double cx = Math.sin(a) * r, cz = -Math.cos(a) * r;
            int seg = 20;
            Mat frame = y < BAND1 || y > BAND2 ? M_WHITE_S : M_RED_S;
            double r0 = 0.15, r1 = 0.21, depth = 0.05;
            // Ring vorn, außen, Glas
            int base = vs.size();
            for (int i = 0; i <= seg; i++) {
                double t = 2 * Math.PI * i / seg, ct = Math.cos(t), st = Math.sin(t);
                for (double rr : new double[]{r0, r1}) {
                    double px = cx + nx * depth + (tx * ct + ux * st) * rr, py = y + ny * depth + uy * st * rr, pz = cz + nz * depth + (tz * ct + uz * st) * rr;
                    vert(px, py, pz, nx, ny, nz, frame, t * rr, py, y + r1, 0, 1);
                }
            }
            for (int i = 0; i < seg; i++) {
                int a0 = base + i * 2, a1 = a0 + 1, b0 = a0 + 2, b1 = a0 + 3;
                orient(a0, a1, b1, nx, ny, nz);
                orient(a0, b1, b0, nx, ny, nz);
            }
            base = vs.size();
            for (int i = 0; i <= seg; i++) {
                double t = 2 * Math.PI * i / seg, ct = Math.cos(t), st = Math.sin(t);
                double ex = tx * ct + ux * st, ey = uy * st, ez = tz * ct + uz * st;
                for (double dd : new double[]{-0.02, depth}) {
                    vert(cx + nx * dd + ex * r1, y + ny * dd + ey * r1, cz + nz * dd + ez * r1, ex, ey, ez, frame, t * r1, y + ey * r1, y + r1, 0, 1);
                }
            }
            for (int i = 0; i < seg; i++) {
                int a0 = base + i * 2, a1 = a0 + 1, b0 = a0 + 2, b1 = a0 + 3;
                double t = 2 * Math.PI * (i + 0.5) / seg;
                double ex = tx * Math.cos(t) + ux * Math.sin(t), ey = uy * Math.sin(t), ez = tz * Math.cos(t) + uz * Math.sin(t);
                orient(a0, a1, b1, ex, ey, ez);
                orient(a0, b1, b0, ex, ey, ez);
            }
            int c = vert(cx + nx * 0.02, y + ny * 0.02, cz + nz * 0.02, nx, ny, nz, M_GLASS, 0, y, y + r1, 0, 1);
            base = vs.size();
            for (int i = 0; i <= seg; i++) {
                double t = 2 * Math.PI * i / seg, ct = Math.cos(t), st = Math.sin(t);
                vert(cx + nx * 0.02 + (tx * ct + ux * st) * r0, y + ny * 0.02 + uy * st * r0, cz + nz * 0.02 + (tz * ct + uz * st) * r0, nx, ny, nz, M_GLASS, t * r0, y, y + r1, 0, 1);
            }
            for (int i = 0; i < seg; i++) orient(c, base + i, base + i + 1, nx, ny, nz);
        }

        /** Dreieck so gewunden, dass es zur Normale passt. */
        void orient(int a, int b, int c, double nx, double ny, double nz) {
            float[] A = vs.get(a), Bv = vs.get(b), C = vs.get(c);
            double ux = Bv[0] - A[0], uy = Bv[1] - A[1], uz = Bv[2] - A[2], wx = C[0] - A[0], wy = C[1] - A[1], wz = C[2] - A[2];
            double fx = uy * wz - uz * wy, fy = uz * wx - ux * wz, fz = ux * wy - uy * wx;
            if (fx * nx + fy * ny + fz * nz >= 0) tri(a, b, c); else tri(a, c, b);
        }

        /** Rundbogenfenster im Dienstraum zwischen zwei Erkern. */
        void archWindow(double azDeg) {
            double a = Math.toRadians(azDeg), r = ROOM_R + 0.012, x = Math.sin(a) * r, z = -Math.cos(a) * r;
            // Rahmen als vier Leisten (Phase 11: von innen sieht man durch die Scheibe hinaus)
            double tx0 = Math.cos(a), tz0 = Math.sin(a);
            for (int s = -1; s <= 1; s += 2) box(x + tx0 * 0.345 * s, 20.85, z + tz0 * 0.345 * s, a, 0.09, 1.1, 0.05, M_WHITE_S, 0.9);
            box(x, 20.3375, z, a, 0.6, 0.075, 0.05, M_WHITE_S, 0.9);
            box(x, 21.3625, z, a, 0.6, 0.075, 0.05, M_WHITE_S, 0.9);
            box(x + Math.sin(a) * 0.026, 20.85, z - Math.cos(a) * 0.026, a, 0.6, 0.95, 0.02, M_GLASS, 0.9);
            // Bogen: Halbkreis aus Fächern
            double ox = Math.sin(a), oz = -Math.cos(a), tx = Math.cos(a), tz = Math.sin(a);
            int seg = 12;
            for (int layer = 0; layer < 2; layer++) {
                double rr = layer == 0 ? 0.39 : 0.3, d = layer == 0 ? 0.025 : 0.037;
                Mat m = layer == 0 ? M_WHITE_S : M_GLASS;
                int c = vert(x + ox * d, 21.4, z + oz * d, ox, 0, oz, m, 0, 21.4, 22, 0, 0.9);
                int base = vs.size();
                for (int i = 0; i <= seg; i++) {
                    double t = Math.PI * i / seg;
                    vert(x + ox * d + tx * Math.cos(t) * rr, 21.4 + Math.sin(t) * rr, z + oz * d + tz * Math.cos(t) * rr, ox, 0, oz, m, t, 21.4, 22, 0, 0.9);
                }
                for (int i = 0; i < seg; i++) orient(c, base + i, base + i + 1, ox, 0, oz);
            }
        }

        /** Die ovale Stahlmanschette: Wand, Kante, Deck. */
        void plate() {
            double c = Math.cos(Math.toRadians(PLATE_ROT)), s = Math.sin(Math.toRadians(PLATE_ROT));
            int seg = 128;
            // Profil in Anteilen des Ovals: Wand außen, gerundete Kante, Deck bis an den Sockel
            double[][] p = {{1.0, PLATE_BOTTOM - 0.6}, {1.0, PLATE_TOP - 0.12}, {0.99, PLATE_TOP - 0.03}, {0.975, PLATE_TOP}, {SOCKEL_R0 / PLATE_B * 0.98, PLATE_TOP}};
            for (int k = 0; k + 1 < p.length; k++) {
                double[] q0 = p[k], q1 = p[k + 1];
                int base = vs.size();
                double dk = q1[0] - q0[0], dy = q1[1] - q0[1];
                for (int r = 0; r < 2; r++) {
                    double[] q = r == 0 ? q0 : q1;
                    for (int i = 0; i <= seg; i++) {
                        double az = 2 * Math.PI * i / seg, ca = Math.sin(az), sa = -Math.cos(az);
                        // Oval in eigenen Achsen (u entlang der langen Achse), dann gedreht
                        double ku = q[0] * PLATE_A * ca, kw = q[0] * PLATE_B * sa;
                        if (k == p.length - 2 && r == 1) { ku = SOCKEL_R0 * 0.98 * ca; kw = SOCKEL_R0 * 0.98 * sa; }
                        double x = ku * c + kw * s, z = -ku * s + kw * c;
                        double gu = ca / PLATE_A, gw = sa / PLATE_B, gl = Math.hypot(gu, gw);
                        double hx = (gu * c + gw * s) / gl, hz = (-gu * s + gw * c) / gl;
                        double hl = Math.hypot(dk * 6.5, dy);
                        double nr = dy / hl, ny = -dk * 6.5 / hl;
                        vert(x, q[1], z, hx * nr, ny, hz * nr, M_DECK, az * 6.5, k >= 3 ? Math.hypot(x, z) : q[1], PLATE_TOP + 0.2, 0, k == 3 ? 0.85 : 0.9);
                    }
                }
                for (int i = 0; i < seg; i++) {
                    int b0 = base + i, b1 = b0 + 1, c0 = b0 + seg + 1, c1 = c0 + 1;
                    float[] n = vs.get(b0);
                    orient(b0, c1, b1, n[3], n[4], n[5]);
                    orient(b0, c0, c1, n[3], n[4], n[5]);
                }
            }
        }

        /** Steinschüttung: unregelmäßige, flache Brocken rund um die Manschette, unter Wasser und bei Ebbe frei. */
        void riprap(Random rnd, int n) {
            double c = Math.cos(Math.toRadians(PLATE_ROT)), s = Math.sin(Math.toRadians(PLATE_ROT));
            for (int i = 0; i < n; i++) {
                double az = rnd.nextDouble() * 2 * Math.PI, k = 1.04 + Math.pow(rnd.nextDouble(), 1.3) * 0.5;
                double ku = k * PLATE_A * Math.sin(az), kw = -k * PLATE_B * Math.cos(az);
                double x = ku * c + kw * s, z = -ku * s + kw * c;
                double size = 0.55 + rnd.nextDouble() * 0.75;
                double top = -0.95 - (k - 1.04) * 2.6 - rnd.nextDouble() * 0.5;
                rock(rnd, x, top - size * 0.45, z, size, rnd.nextDouble() * Math.PI);
            }
        }

        /** Ein Brocken: verformtes Ikosaeder, einmal unterteilt, glatt schattiert. */
        void rock(Random rnd, double x, double y, double z, double size, double rot) {
            double t = (1 + Math.sqrt(5)) / 2;
            double[][] v = {{-1, t, 0}, {1, t, 0}, {-1, -t, 0}, {1, -t, 0}, {0, -1, t}, {0, 1, t}, {0, -1, -t}, {0, 1, -t}, {t, 0, -1}, {t, 0, 1}, {-t, 0, -1}, {-t, 0, 1}};
            int[][] f = {{0, 11, 5}, {0, 5, 1}, {0, 1, 7}, {0, 7, 10}, {0, 10, 11}, {1, 5, 9}, {5, 11, 4}, {11, 10, 2}, {10, 7, 6}, {7, 1, 8},
                    {3, 9, 4}, {3, 4, 2}, {3, 2, 6}, {3, 6, 8}, {3, 8, 9}, {4, 9, 5}, {2, 4, 11}, {6, 2, 10}, {8, 6, 7}, {9, 8, 1}};
            List<double[]> pts = new ArrayList<>();
            for (double[] p : v) { double l = Math.sqrt(p[0] * p[0] + p[1] * p[1] + p[2] * p[2]); pts.add(new double[]{p[0] / l, p[1] / l, p[2] / l}); }
            List<int[]> faces = new ArrayList<>();
            java.util.Map<Long, Integer> mid = new java.util.HashMap<>();
            for (int[] tri : f) {
                int a = midpoint(pts, mid, tri[0], tri[1]), b = midpoint(pts, mid, tri[1], tri[2]), cc = midpoint(pts, mid, tri[2], tri[0]);
                faces.add(new int[]{tri[0], a, cc});
                faces.add(new int[]{tri[1], b, a});
                faces.add(new int[]{tri[2], cc, b});
                faces.add(new int[]{a, b, cc});
            }
            double sx = size * (0.8 + rnd.nextDouble() * 0.5), sy = size * (0.45 + rnd.nextDouble() * 0.2), sz = size * (0.7 + rnd.nextDouble() * 0.4);
            double cr = Math.cos(rot), sr = Math.sin(rot);
            // geteilte Ecken mit glatten Normalen (Ellipsoid-Gradient), leicht verbeult
            int base = vs.size();
            double seedU = rnd.nextDouble() * 100, tint = 0.65 + rnd.nextDouble() * 0.7;
            Mat stone = new Mat(K_STONE, 0, 0.9, 0.9, new double[]{STONE[0] * tint, STONE[1] * tint, STONE[2] * tint * (0.9 + 0.2 * rnd.nextDouble())});
            for (double[] p : pts) {
                double d = 0.82 + 0.3 * (0.5 + 0.5 * Math.sin(p[0] * 5.1 + seedU) * Math.cos(p[2] * 4.3 + seedU * 0.7)) + rnd.nextDouble() * 0.06;
                double px = p[0] * sx * d, py = p[1] * sy * d, pz = p[2] * sz * d;
                if (py < -sy * 0.3) py = -sy * 0.3 - (py + sy * 0.3) * 0.2;
                double gx = p[0] / sx, gy = p[1] / sy, gz = p[2] / sz;
                double wx = x + px * cr - pz * sr, wy = y + py, wz = z + px * sr + pz * cr;
                double nx = gx * cr - gz * sr, nz = gx * sr + gz * cr;
                double ao = 0.5 + 0.5 * Math.max(0, Math.min(1, (py + sy * 0.3) / (sy * 1.3)));
                vert(wx, wy, wz, nx, gy, nz, stone, wx + wz * 0.7 + x * 3.1, wy + z * 1.7, y + sy, 0, ao);
            }
            for (int[] fa : faces) {
                float[] A = vs.get(base + fa[0]), Bv = vs.get(base + fa[1]), C = vs.get(base + fa[2]);
                orient(base + fa[0], base + fa[1], base + fa[2], A[3] + Bv[3] + C[3], A[4] + Bv[4] + C[4], A[5] + Bv[5] + C[5]);
            }
        }

        static int midpoint(List<double[]> pts, java.util.Map<Long, Integer> mid, int a, int b) {
            long key = a < b ? ((long) a << 32) | b : ((long) b << 32) | a;
            Integer m = mid.get(key);
            if (m != null) return m;
            double[] p = pts.get(a), q = pts.get(b);
            double x = p[0] + q[0], y = p[1] + q[1], z = p[2] + q[2], l = Math.sqrt(x * x + y * y + z * z);
            pts.add(new double[]{x / l, y / l, z / l});
            mid.put(key, pts.size() - 1);
            return pts.size() - 1;
        }

        /** Leuchtturm Alte Weser (1964): Schaft nach unten verjüngt, rot mit zwei weißen Bändern, zweistöckige Kanzel, grüne Laterne. */
        void alteWeser() {
            double[] p = alteWeserPos();
            double x = p[0], z = p[1], d = -p[2];
            int seg = 32;
            Mat red = new Mat(K_STEEL, 0.2, 0.45, 0.2, RED), white = new Mat(K_STEEL, 0.2, 0.45, 0.2, WHITE);
            latheAt(x, z, new double[][]{{6.0, d - 3}, {6.0, d + 2.5}, {2.3, d + 2.5}}, seg, M_BLACK_S, d + 2.5, 0, 1);
            double[][] rings = {{2.5, 9}, {9, 12}, {12, 18}, {18, 21}, {21, 26}};
            for (int i = 0; i < rings.length; i++) {
                double y0 = rings[i][0], y1 = rings[i][1];
                double r0 = 2.2 + (y0 - 2.5) * 0.04, r1 = 2.2 + (y1 - 2.5) * 0.04;
                latheAt(x, z, new double[][]{{r0, d + y0}, {r1, d + y1}}, seg, i % 2 == 1 ? white : red, d + y1, 0, 1);
            }
            latheAt(x, z, new double[][]{{3.15, d + 26}, {5.2, d + 26}, {5.2, d + 28.6}, {5.4, d + 28.6}, {5.4, d + 29}}, seg, white, d + 29, 0, 1);
            latheAt(x, z, new double[][]{{5.2, d + 29}, {5.2, d + 31.6}, {5.4, d + 31.6}, {5.4, d + 32}, {1.8, d + 32}}, seg, red, d + 32, 0, 1);
            latheAt(x, z, new double[][]{{5.25, d + 27}, {5.25, d + 28.2}}, seg, M_GLASS, d + 28.2, 0, 1);
            latheAt(x, z, new double[][]{{5.25, d + 29.6}, {5.25, d + 31.2}}, seg, M_GLASS, d + 31.2, 0, 1);
            latheAt(x, z, new double[][]{{1.6, d + 32}, {1.6, d + 34.6}}, seg, new Mat(K_GLASS, 0, 0.05, 0, GREEN_GLASS), d + 34.6, 0, 1);
            latheAt(x, z, new double[][]{{1.75, d + 34.6}, {1.75, d + 35}, {0.2, d + 36.6}, {0.0, d + 38}}, seg, new Mat(K_DOME, 0.1, 0.4, 0.1, new double[]{0.05, 0.12, 0.06}), d + 38, 0, 1);
        }
    }
}
