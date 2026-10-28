package com.dan.lighthouse.scene;

import com.dan.surf.BreakerMesh;
import com.dan.surf.Surf;
import com.dan.surf.WaveTrain;

import java.util.Locale;

/**
 * Phase 17 (Sylt-Werkbuch Abschnitt 12, F22 A): die Brandung am Caisson und an der Steinschüttung aus {@code com.dan.surf}
 * ({@link Surf#sockel()}) statt des Brandungsrings aus {@code ocean} (der bleibt für Tonnen, Pfahl und Schiffe).
 * <p>
 * Ein Rechteck um den Turm, längs der Laufrichtung der See: {@link #VOR} m auf der Wetterseite bis {@link #LEE} m im Lee,
 * {@link #HALB} m zu jeder Seite. Darin läuft ein {@link WaveTrain} mit geraden Kämmen; jede Reihe (eine Linie in
 * Laufrichtung) hat ihren eigenen Grund ({@link WaveTrain#rowProfile}): den Schnitt durch Seegrund, Böschung,
 * Steinschüttung, Manschette und Sockel an genau dieser Stelle. So brechen die Wellen auf der Steinschüttung rundum, wo
 * sie flach wird – auf der Wetterseite zuerst, an den Flanken etwas später, im Lee als Walze, die sich wieder legt –,
 * und laufen an der Manschette und am Sockel auf. Im Sockel ruht das Wasser unsichtbar. Die See ({@code ocean}) lässt
 * im Rechteck ein Loch mit weichem Rand; dort zeichnet das Netz ({@link BreakerMesh}).
 * <p>
 * Ändern sich Wasserstand, Seegang oder Richtung deutlich, wird ein neues Stück im eigenen Faden gebaut und
 * eingeschwungen ({@link #EINSCHWINGEN} s Szenenzeit); das alte läuft so lange weiter.
 * <p>
 * Annahmen [G]: Seegrund {@link #SEEGRUND} m unter dem Mittelwasser der Szene, die Böschung der Schüttung 1:2 bis zum
 * Grund, H_s am Rand des Stücks höchstens {@link #HS_TIEFE}·Tiefe (tiefenbegrenzt; draußen darf die See höher sein).
 * <p>
 * Naht: das Netz trägt die See ({@link #see}, auf einem groben Raster abgefragt); in den äußeren {@link #NAHT} m ist es
 * ganz die See, dort blendet {@code ocean} auch das Loch weich aus – so gibt es keine Kante zwischen beiden Wassern.
 */
public final class Sockelbrandung {
    /** Seegrund (m, Szene: Mittelwasser 0) [G], Ausdehnung des Stücks (m) und Einschwingzeit (s). */
    public static final double SEEGRUND = -7.0, VOR = 30, LEE = 16, HALB = 15, EINSCHWINGEN = 20;
    /** Höchstens so hoch ist H_s am Rand des Stücks im Verhältnis zur Tiefe (tiefenbegrenzte See) [G]. */
    public static final double HS_TIEFE = 0.55;
    /** Böschung der Schüttung bis zum Grund (Höhe : Länge) [G]. */
    public static final double BOESCHUNG = 0.5;
    /** Raster des Grunds je Reihe (m), Breite der Naht zur See (m), Raster der See in der Naht (m). */
    static final double ZELLE = 0.25, NAHT = 10, SEE_RASTER = 2.5;
    /** Wie weit die See im Netz über dem Grund ausschlagen darf (Anteil der Tiefe) [G]. */
    static final double SEE_TIEFE = 0.45;
    /** Über der trockenen Manschette (Grund höher als das) zeichnet nur das Netz; sonst taucht ruhiges Wasser so tief unter die See (m). */
    static final double UEBER_PLATTE = -0.5, UNTER = 0.6;

    /** Höhe der See (ocean) an einem Punkt der Szene, für die Naht. null: keine Naht. */
    @FunctionalInterface
    public interface See { double hoehe(double x, double z); }

    public volatile See see;

    /** Ein Aufschlag in der Szene: Ort, Wasser (m³), Tempo (m/s). */
    @FunctionalInterface
    public interface Aufschlag { void aufschlag(double x, double y, double z, double volumen, double tempo); }

    /** Ein Brecher beginnt (Szene): Ort, Höhe (m), Art. */
    @FunctionalInterface
    public interface Brecher { void brecher(double x, double z, double hoehe, Surf.Breaker art); }

    /** Ein Stück: Wellenzug, Netz und Lage in der Szene. */
    public static final class Stueck {
        public final WaveTrain zug;
        public final BreakerMesh netz = new BreakerMesh();
        /** Laufrichtung (ux, uz), quer (ax, az), Ursprung (Rand zur See, Reihe 0) in der Szene. */
        public final double ux, uz, ax, az, ox, oz;
        /** Woher die See kommt (°), H_s draußen und am Rand, T_p, Wasserstand beim Bau. */
        public final double von, hs, hsRand, tp, pegel, laenge;
        final float[] grund;
        final int nx;
        public int brecher, aufschlaege;

        Stueck(WaveTrain zug, double von, double hs, double hsRand, double tp, double pegel) {
            this.zug = zug;
            this.von = von;
            this.hs = hs;
            this.hsRand = hsRand;
            this.tp = tp;
            this.pegel = pegel;
            double a = Math.toRadians(von);
            ux = -Math.sin(a);
            uz = Math.cos(a);
            ax = -uz;
            az = ux;
            ox = -VOR * ux - HALB * ax;
            oz = -VOR * uz - HALB * az;
            laenge = VOR + LEE;
            nx = (int) Math.ceil(laenge / ZELLE) + 3;
            grund = new float[zug.rows * nx];
        }

        /** Szenenpunkt {x, z} aus lokal (x in Laufrichtung, y quer). */
        public double[] welt(double x, double y) { return new double[]{ox + x * ux + y * ax, oz + x * uz + y * az}; }

        /** Lokal {x, y} aus der Szene. */
        public double[] lokal(double wx, double wz) {
            double dx = wx - ox, dz = wz - oz;
            return new double[]{dx * ux + dz * uz, dx * ax + dz * az};
        }

        /** Matrix lokal → Szene (Spalten zuerst): x in Laufrichtung, y nach oben, z quer. */
        public float[] modell() {
            return new float[]{(float) ux, 0, (float) uz, 0, 0, 1, 0, 0, (float) ax, 0, (float) az, 0, (float) ox, 0, (float) oz, 1};
        }

        /** Grund der Reihe r am Ort x (aus dem Raster). */
        double grund(double x, int r) {
            double u = Math.max(0, Math.min(nx - 1.001, x / ZELLE));
            int i = (int) u, o = r * nx;
            return grund[o + i] + (grund[o + i + 1] - grund[o + i]) * (u - i);
        }

        /** Liegt der Szenenpunkt im Stück? */
        public boolean innen(double wx, double wz) {
            double[] l = lokal(wx, wz);
            return l[0] >= 0 && l[0] <= laenge && l[1] >= 0 && l[1] <= 2 * HALB;
        }
    }

    public final Surf surf = Surf.sockel();
    private volatile Stueck aktiv;
    private volatile Thread bau;
    /** Reihen quer (nach Qualität) und ob die Brandung gezeigt wird. */
    public volatile int reihen = 40;
    public volatile boolean an = true;
    public volatile String meldung = "noch kein Stück";
    public volatile double schrittMs, netzMs;
    public volatile Aufschlag onAufschlag;
    public volatile Brecher onBrecher;

    public Stueck stueck() { return an ? aktiv : null; }

    public boolean baut() { return bau != null; }

    /**
     * Grund an der Stelle (x, z) der Szene (m über dem Mittelwasser): Sockel {@link Turm#SOCKEL_TOP}, Manschette
     * {@link Turm#PLATE_TOP}, Steinschüttung wie in {@code Turm.riprap} (Oberkante der Brocken etwas abgezogen), dann die
     * Böschung bis zum Seegrund.
     */
    public static double grund(double x, double z) {
        // im Sockel: die Platte (das Wasser dort ruht unsichtbar im Mauerwerk, ein senkrechter Vorhang stünde vor dem
        // verjüngten Sockel)
        if (Math.hypot(x, z) < Turm.SOCKEL_R0) return Turm.PLATE_TOP;
        double c = Math.cos(Math.toRadians(Turm.PLATE_ROT)), s = Math.sin(Math.toRadians(Turm.PLATE_ROT));
        double u = x * c - z * s, w = x * s + z * c;
        double k = Math.sqrt(u * u / (Turm.PLATE_A * Turm.PLATE_A) + w * w / (Turm.PLATE_B * Turm.PLATE_B));
        if (k <= 1) return Turm.PLATE_TOP;
        // Steinschüttung: k 1,04 … 1,54 mit Oberkanten −0,95 … −2,25 m, gemittelt 0,3 m tiefer
        if (k <= 1.54) return -1.25 - Math.max(0, k - 1.04) / 0.5 * 1.3;
        // Böschung: Abstand vom Fuß der Schüttung in Metern (k über den mittleren Halbmesser der Manschette)
        double meter = (k - 1.54) * Math.sqrt(Turm.PLATE_A * Turm.PLATE_B);
        return Math.max(SEEGRUND, -2.55 - meter * BOESCHUNG);
    }

    /** H_s am Rand des Stücks: die See draußen, in der Tiefe dort begrenzt. */
    public static double hsRand(double hs, double pegel) { return Math.max(0, Math.min(hs, HS_TIEFE * (pegel - SEEGRUND))); }

    /**
     * Je Bild: neu bauen, wenn sich etwas deutlich geändert hat, und das Stück um dt weiterrechnen.
     *
     * @param hs  H_s der See draußen (m), tp ihre Periode (s), von ihre Richtung (woher, °)
     */
    public void schritt(double dt, double pegel, double hs, double tp, double von) {
        if (!an) { meldung = "Brandung am Sockel aus"; return; }
        Stueck s = aktiv;
        double dv = s == null ? 0 : Math.abs(((von - s.von) % 360 + 540) % 360 - 180);
        boolean neu = s == null || Math.abs(pegel - s.pegel) > 0.4 || Math.abs(hs - s.hs) > 0.15 * Math.max(0.3, s.hs)
                || Math.abs(tp - s.tp) > 0.9 || dv > 12 || s.zug.rows != reihen;
        if (neu && bau == null) {
            final int rows = reihen;
            Thread th = new Thread(() -> {
                try {
                    aktiv = baue(pegel, hs, tp, von, rows);
                } catch (RuntimeException e) {
                    meldung = "Brandung am Sockel: " + e.getMessage();
                } finally { bau = null; }
            }, "brandung-sockel");
            th.setDaemon(true);
            bau = th;
            th.start();
        }
        if (s == null) return;
        long t0 = System.nanoTime();
        double h = Math.min(0.05, Math.max(0, dt));
        s.zug.level = pegel;
        if (h > 0) s.zug.step(h);
        long t1 = System.nanoTime();
        s.netz.build(s.zug, s.laenge);
        naht(s, see);
        netzMs = netzMs * 0.9 + (System.nanoTime() - t1) / 1e6 * 0.1;
        schrittMs = schrittMs * 0.9 + (t1 - t0) / 1e6 * 0.1;
        meldung = beschreibe(s);
    }

    /**
     * Die See im Netz: die Höhe von {@code ocean} auf einem Raster von {@link #SEE_RASTER} m (dazwischen bilinear) legt
     * sich unter das Netz. Am Rand (bis {@link #NAHT} m hinein) ist das Netz ganz die See (keine Kante zum Loch), innen
     * kommt der Wellenzug dazu, und die See darf dort nur so hoch ausschlagen, wie das Wasser über dem Grund tief ist
     * ({@link #SEE_TIEFE}·Tiefe, die Brandung übernimmt) [G: eine Mischung fürs Bild, keine gekoppelte Rechnung]. Danach
     * die Normalen neu.
     */
    static void naht(Stueck s, See see) {
        if (see == null) return;
        BreakerMesh m = s.netz;
        int gx = (int) Math.ceil(s.laenge / SEE_RASTER) + 1, gy = (int) Math.ceil(2 * HALB / SEE_RASTER) + 1;
        double[] g = new double[gx * gy];
        for (int j = 0; j < gy; j++)
            for (int i = 0; i < gx; i++) {
                double[] p = s.welt(i * SEE_RASTER, j * SEE_RASTER);
                g[j * gx + i] = see.hoehe(p[0], p[1]);
            }
        float[] v = m.vertices;
        int ns = m.ns, ny = m.ny;
        double level = s.zug.level;
        for (int r = 0; r < ny; r++)
            for (int k = 0; k < ns; k++) {
                int o = (r * ns + k) * BreakerMesh.STRIDE;
                double x = v[o], y = v[o + 2];
                double ed = Math.min(Math.min(x, s.laenge - x), Math.min(y, 2 * HALB - y));
                double w = Math.max(0, Math.min(1, ed / NAHT));
                w = w * w * (3 - 2 * w);
                double u = Math.max(0, Math.min(gx - 1.001, x / SEE_RASTER)), q = Math.max(0, Math.min(gy - 1.001, y / SEE_RASTER));
                int i = (int) u, j = (int) q;
                double fu = u - i, fq = q - j;
                double h = (g[j * gx + i] * (1 - fu) + g[j * gx + i + 1] * fu) * (1 - fq) + (g[(j + 1) * gx + i] * (1 - fu) + g[(j + 1) * gx + i + 1] * fu) * fq;
                double bed = v[o + 9], still = Math.max(0, level - bed);
                double lim = SEE_TIEFE * still / Math.max(1e-3, w) ;      // am Rand unbegrenzt
                double sea = Math.max(-lim, Math.min(lim, h - level));
                double yt = v[o + 1];
                if (yt < bed) continue;                                    // trocken: bleibt unter dem Grund
                double yn = level + sea + (yt - level) * w;
                if (bed < UEBER_PLATTE || level > Turm.PLATE_TOP - 0.1) {     // die See zeichnet hier selbst (über der Platte, wenn sie unter Wasser ist)
                    // über Steinschüttung und See: nur das weiße Wasser zeigt sich über der See, das ruhige taucht unter sie
                    double a = Math.max(0, Math.min(1, (Math.max(v[o + 6], v[o + 7]) * w - 0.12) / 0.33));
                    a = a * a * (3 - 2 * a);
                    // das weiße Wasser reitet auf der See: ihre Höhe plus das, was der Wellenzug über dem Ruhewasser hat
                    double top = h + Math.max(0, yt - level) * w + 0.04;
                    yn = (h - UNTER) * (1 - a) + top * a;
                }
                v[o + 1] = (float) Math.max(bed - 0.03, yn);
                v[o + 6] *= (float) w;     // Schaum und Lippe nur, wo der Wellenzug gilt
                v[o + 7] *= (float) w;
            }
        // Normalen wie in BreakerMesh (quer × längs)
        for (int r = 0; r < ny; r++) {
            for (int i = 0; i < ns; i++) {
                int i0 = Math.max(0, i - 1), i1 = Math.min(ns - 1, i + 1), r0 = Math.max(0, r - 1), r1 = Math.min(ny - 1, r + 1);
                int a = (r * ns + i0) * BreakerMesh.STRIDE, b = (r * ns + i1) * BreakerMesh.STRIDE, c = (r0 * ns + i) * BreakerMesh.STRIDE, d = (r1 * ns + i) * BreakerMesh.STRIDE;
                double sx = v[b] - v[a], sy = v[b + 1] - v[a + 1], sz = v[b + 2] - v[a + 2];
                double tx = v[d] - v[c], ty = v[d + 1] - v[c + 1], tz = v[d + 2] - v[c + 2];
                double nx = ty * sz - tz * sy, nyy = tz * sx - tx * sz, nz = tx * sy - ty * sx;
                double l = Math.sqrt(nx * nx + nyy * nyy + nz * nz);
                if (l < 1e-12) { nx = 0; nyy = 1; nz = 0; l = 1; }
                int o = (r * ns + i) * BreakerMesh.STRIDE;
                v[o + 3] = (float) (nx / l);
                v[o + 4] = (float) (nyy / l);
                v[o + 5] = (float) (nz / l);
            }
        }
    }

    String beschreibe(Stueck s) {
        int[] b = s.zug.brokenByType;
        return String.format(Locale.GERMAN, "Brandung am Sockel: %d Reihen, See aus %.0f°, H_s %.1f m (am Rand %.1f m), T_p %.1f s · %d Wellen · gebrochen %d Schwall, %d Sturz, %d Reflexion · %d Aufschläge · Rechnen %.1f ms, Netz %.1f ms",
                s.zug.rows, s.von, s.hs, s.hsRand, s.tp, s.zug.waves.size(), b[Surf.Breaker.SPILLING.ordinal()], b[Surf.Breaker.PLUNGING.ordinal()],
                b[Surf.Breaker.COLLAPSING.ordinal()] + b[Surf.Breaker.SURGING.ordinal()], s.aufschlaege, schrittMs, netzMs);
    }

    /** Ein Stück bauen und einschwingen (im Faden des Aufrufers). */
    public Stueck baue(double pegel, double hs, double tp, double von, int rows) {
        surf.hs = hs;
        surf.tp = tp;
        surf.seaFromDeg = von;
        surf.level = t -> pegel;
        surf.bed = Sockelbrandung::grund;
        WaveTrain zug = new WaveTrain(rows, 0, VOR + LEE, 2 * HALB);
        double hr = hsRand(hs, pegel);
        Stueck st = new Stueck(zug, von, hs, hr, Math.max(3, tp), pegel);
        for (int r = 0; r < rows; r++)
            for (int i = 0; i < st.nx; i++) {
                double[] p = st.welt(i * ZELLE, zug.rowY(r));
                st.grund[r * st.nx + i] = (float) grund(p[0], p[1]);
            }
        zug.profile = x -> SEEGRUND;          // die Kämme laufen mit der See um das Bauwerk
        zug.rowProfile = st::grund;
        zug.level = pegel;
        zug.hs = hr;
        zug.tp = st.tp;
        zug.gamma = surf.gamma;
        zug.slopeBase = 3;                    // die Schüttung ist steil und kurz
        zug.crestLength = 12;
        zug.seed = 1885 + Math.round(von / 10);
        zug.onBreaking = (x, y, hb, art) -> {
            st.brecher++;
            Brecher b = onBrecher;
            if (b != null && st == aktiv) { double[] p = st.welt(x, y); b.brecher(p[0], p[1], hb, art); }
        };
        zug.onImpact = (x, y, z, v, sp) -> {
            st.aufschlaege++;
            Aufschlag a = onAufschlag;
            if (a != null && st == aktiv) { double[] p = st.welt(x, y); a.aufschlag(p[0], z, p[1], v, sp); }
        };
        for (double t = 0; t < EINSCHWINGEN; t += 1 / 30.0) zug.step(1 / 30.0);
        st.netz.build(zug, st.laenge);
        naht(st, see);
        meldung = beschreibe(st);
        return st;
    }

    /** Für Standbilder: das Stück jetzt bauen und einschwingen (im aufrufenden Faden). */
    public void jetzt(double pegel, double hs, double tp, double von) {
        if (an) aktiv = baue(pegel, hs, tp, von, reihen);
    }

    /** Das Loch für die See: Mitte {x, z}, Richtung {ux, uz}, halbe Länge, halbe Breite, weicher Rand (m); null ohne Stück. */
    public double[][] loch() {
        Stueck s = stueck();
        if (s == null) return null;
        // das Loch reicht bis 1 m vor den Rand, der weiche Rand der See liegt in der Naht
        double cx = s.laenge / 2;
        return new double[][]{s.welt(cx, HALB), {s.ux, s.uz}, {cx - 1, HALB - 1, NAHT - 2}};
    }
}
