package com.dan.lighthouse.test;

import com.dan.lighthouse.camera.Camera;
import com.dan.lighthouse.db.Db;
import com.dan.lighthouse.gl.ComputeProbe;
import com.dan.lighthouse.gl.Mat4;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Shader;
import com.dan.lighthouse.scene.Tide;
import com.dan.lighthouse.scene.Innenraum;
import com.dan.lighthouse.scene.Turm;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.scene.Site;
import com.dan.lighthouse.scene.Sun;
import com.dan.lighthouse.ui.AppIcon;
import com.dan.lighthouse.wiring.Beaufort;
import com.dan.lighthouse.wiring.Collider;
import com.dan.lighthouse.wiring.Wind;
import com.dan.lighthouse.wiring.Wiring;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Prüfprogramm der App ohne Grafikkarte und ohne Datenbank (F27 = A). In NetBeans: Rechtsklick → „Run File“.
 * Endet mit „ERGEBNIS: alles in Ordnung“ oder mit der Liste der Verstöße (Rückgabewert 1). Wächst mit jeder Phase.
 * Die Packages haben zusätzlich je ihre eigene Prüfklasse in {@code .demo}.
 */
public final class Pruefung {
    private static int ok, bad;

    public static void main(String[] args) throws Exception {
        long t0 = System.nanoTime();
        mathe();
        ort();
        sonne();
        uhr();
        kamera();
        turm();
        gezeiten();
        compute();
        shader();
        verdrahtung();
        meer();
        himmel();
        wolken();
        verwitterung();
        gischt();
        regen();
        scheiben();
        leuchtfeuer();
        gewitter();
        innenraum();
        wetter();
        leben();
        klang();
        isolation();
        datenbank();
        PruefungDaten.alles();
        PruefungSchliff.alles();
        sonstiges();
        System.out.printf(Locale.GERMAN, "%n%d Prüfungen, %d Verstöße, %.1f s%n", ok + bad, bad, (System.nanoTime() - t0) / 1e9);
        System.out.println(bad == 0 ? "ERGEBNIS: alles in Ordnung" : "ERGEBNIS: " + bad + " Verstöße");
        System.exit(bad == 0 ? 0 : 1);
    }

    static void check(boolean cond, String what) {
        if (cond) ok++; else bad++;
        System.out.println((cond ? "  ok    " : "  FEHLER ") + what);
    }

    static void mathe() {
        System.out.println("Matrizen");
        float[] v = Mat4.lookAt(10, 20, 30, 0, 14, 0);
        float[] p = Mat4.perspective(55, 16 / 9.0, 0.2, 60000, true);
        float[] vp = Mat4.mul(p, v), id = Mat4.mul(vp, Mat4.invert(vp));
        double err = 0;
        for (int i = 0; i < 16; i++) err = Math.max(err, Math.abs(id[i] - (i % 5 == 0 ? 1 : 0)));
        check(err < 1e-4, String.format(Locale.GERMAN, "Projektion·Blick mal Inverse = Einheit (Fehler %.1e)", err));
        float[] look = Mat4.lookAt(0, 0, 0, 0, 0, -1);
        double[] c = Mat4.apply(Mat4.mul(p, look), 0, 0, -100);
        check(Math.abs(c[2] / c[3] - 0.2 / 100) < 1e-6, "Umgekehrte Tiefe: Punkt in 100 m hat Tiefe Nahebene/100");
        double[] far = Mat4.apply(Mat4.mul(p, look), 0, 0, -16000);
        check(far[2] / far[3] > 0 && far[2] / far[3] < 1e-4, "Kimm in 16 km liegt zwischen 0 und Nahebene, vor dem Unendlichen");
    }

    static void ort() {
        System.out.println("Ort und Maßstab");
        check(Math.abs(Site.curvatureDrop(100_000) - 784.8) < 1, "Erdkrümmung: 100 km → 785 m Absenkung");
        check(Math.abs(Site.horizon(20) / 1000 - 17.1) < 0.2, String.format(Locale.GERMAN, "Kimm aus 20 m Augenhöhe in %.1f km (mit Brechung rund 17,1 km)", Site.horizon(20) / 1000));
        double range = Site.geographicRangeNm(Site.LIGHT, 5);
        check(Math.abs(range - 14.8) < 0.1, String.format(Locale.GERMAN, "Feuerhöhe 24 m, Auge 5 m: geografische Tragweite %.1f sm", range));
        check(Site.dms(Site.LAT, Site.LON).equals("53°51′18″ N · 8°04′45″ E"), "Lage zurück in Grad, Minuten, Sekunden: " + Site.dms(Site.LAT, Site.LON));
        double x = 1234, z = -5678;
        check(Math.abs(Site.x(Site.lon(x)) - x) < 1e-6 && Math.abs(Site.z(Site.lat(z)) - z) < 1e-6, "Szene ↔ Breite/Länge hin und zurück");
        check(Site.lat(-1000) > Site.LAT && Site.lon(1000) > Site.LON, "Nord ist −z, Ost ist +x");
        check(Math.abs(Site.dipDeg(20) - 0.1435) < 0.002, String.format(Locale.GERMAN, "Kimmtiefe aus 20 m: %.3f°", Site.dipDeg(20)));
    }

    static void sonne() {
        System.out.println("Sonne am Roten Sand");
        LocalDate d = LocalDate.of(2026, 10, 15);
        double day0 = d.atStartOfDay(SceneClock.ZONE).toInstant().toEpochMilli();
        double up = Sun.crossing(day0, Site.LAT, Site.LON, true), down = Sun.crossing(day0, Site.LAT, Site.LON, false);
        double upH = hourOf(up), downH = hourOf(down), len = (down - up) / 3.6e6;
        check(Math.abs(upH - (7 + 56 / 60.0)) < 0.15, "15.10.2026 Aufgang " + hm(upH) + " MESZ (erwartet rund 07:56)");
        check(Math.abs(downH - (18 + 31 / 60.0)) < 0.15, "15.10.2026 Untergang " + hm(downH) + " MESZ (erwartet rund 18:31)");
        check(len > 10.4 && len < 10.75, String.format(Locale.GERMAN, "Tageslänge %.2f h (54° N Mitte Oktober: rund 10,6 h)", len));
        double noon = (up + down) / 2;
        check(Math.abs(hourOf(noon) - (13 + 13.5 / 60)) < 0.1, "Wahrer Mittag " + hm(hourOf(noon)) + " MESZ (Länge 8,08° E, Zeitgleichung +14 min)");
        double eNoon = Sun.position(noon, Site.LAT, Site.LON)[0];
        check(Math.abs(eNoon - (90 - Site.LAT - 8.6)) < 0.5, String.format(Locale.GERMAN, "Mittagshöhe %.1f° (90° − Breite − 8,6° Deklination)", eNoon));
        SceneClock c = new SceneClock(d, 20.0);
        double e20 = c.sun()[0];
        check(e20 < -8 && e20 > -18, String.format(Locale.GERMAN, "20:00 Uhr: Sonne %.1f° unter der Kimm (Dämmerung, Feuer an)", e20));
        double sol = ZonedDateTime.of(2026, 6, 21, 13, 40, 0, 0, SceneClock.ZONE).toInstant().toEpochMilli();
        double best = -90;
        for (int m = -60; m <= 60; m++) best = Math.max(best, Sun.position(sol + m * 60_000, Site.LAT, Site.LON)[0]);
        check(Math.abs(best - (90 - Site.LAT + 23.44)) < 0.3, String.format(Locale.GERMAN, "Sommeranfang: Mittagshöhe %.1f°", best));
        double[] dir = Sun.direction(0, 90);
        check(Math.abs(dir[0] - 1) < 1e-9 && Math.abs(dir[1]) < 1e-9, "Azimut 90° zeigt nach Osten (+x)");
        boolean mono = true;
        for (double e = -30; e < 40; e += 0.5) if (Sun.skyBrightness(e + 0.5) < Sun.skyBrightness(e)) mono = false;
        check(mono && Sun.skyBrightness(40) == 1 && Math.abs(Sun.skyBrightness(0) - 0.1) < 1e-9 && Sun.skyBrightness(-25) < 1e-4,
                "Himmelshelligkeit: Tag 1, Sonnenuntergang 0,1, Nacht 0,00005, steigt mit der Sonne");
    }

    static double hourOf(double epochMs) {
        ZonedDateTime z = Instant.ofEpochMilli((long) epochMs).atZone(SceneClock.ZONE);
        return z.getHour() + z.getMinute() / 60.0 + z.getSecond() / 3600.0;
    }

    static String hm(double h) { return String.format("%02d:%02d", (int) h, (int) Math.round((h - (int) h) * 60) % 60); }

    static void uhr() {
        System.out.println("Uhr der Szene");
        SceneClock c = new SceneClock();
        check(c.date().equals(LocalDate.now(SceneClock.ZONE)) && Math.abs(c.hour() - 20) < 1e-6, "Start: heute um 20:00 Uhr Ortszeit (F26)");
        c = new SceneClock(LocalDate.of(2026, 10, 15), 20.0);
        check(c.label().equals("15.10.2026 20:00 MESZ"), "Anzeige: " + c.label());
        c.lapse = 600;
        for (int i = 0; i < 600; i++) c.step(1 / 60.0);
        check(Math.abs(c.hour() - (20 + 100 / 60.0)) < 1e-3, "Zeitraffer 600-fach: 10 s → 100 Minuten");
        check(Math.abs(c.t() - 10) < 1e-9, "Laufzeit zählt echte Sekunden");
        c.lapse = 0;
        double h = c.hour();
        c.step(1);
        check(c.hour() == h, "Uhr steht bei Zeitraffer 0, die Laufzeit läuft weiter");
        c.set(LocalDate.of(2026, 12, 1), 8.5);
        check(c.label().equals("1.12.2026 08:30 MEZ"), "Winterzeit: " + c.label());
        c.lapse = 1e6;
        c.step(1);
        check(Math.abs(c.hour() - (8.5 + SceneClock.MAX_LAPSE / 3600)) < 1e-6, "Zeitraffer ist auf 600-fach begrenzt");
    }

    static void kamera() {
        System.out.println("Kamera");
        Camera c = new Camera();
        check(c.eye[1] >= Camera.MIN_EYE && c.eye[1] < 25 && Math.abs(c.towerDistance() - c.dist * Math.cos(Math.toRadians(c.pitch))) < 1,
                String.format(Locale.GERMAN, "Grundansicht: Auge %.1f m hoch, %.0f m vom Turm", c.eye[1], c.towerDistance()));
        check(Math.abs(c.heading() - 250) < 0.01, "Blick nach Westsüdwest (250°) in die Abenddämmerung");
        Random rnd = new Random(7);
        boolean dry = true;
        for (int i = 0; i < 4000; i++) {
            switch (rnd.nextInt(4)) {
                case 0 -> c.orbit(rnd.nextGaussian() * 40, rnd.nextGaussian() * 40);
                case 1 -> c.zoom(rnd.nextGaussian() * 6);
                case 2 -> c.pan(rnd.nextGaussian(), rnd.nextGaussian());
                default -> { c.ty = rnd.nextGaussian() * 30; c.orbit(0, 0); }
            }
            c.step(1 / 30.0);
            if (c.eye[1] < Camera.MIN_EYE - 1e-6) dry = false;
        }
        check(dry, "Kreisen: 4 000 zufällige Bewegungen, das Auge bleibt über dem Wasser");
        c.home();
        c.snap();
        double[] e0 = c.eye.clone(), f0 = c.fwd.clone();
        c.setMode(Camera.Mode.FREI);
        c.step(1 / 60.0);
        check(dist(e0, c.eye) < 0.05 && dist(f0, c.fwd) < 1e-3, "Wechsel zu Frei: Ansicht springt nicht");
        c.kDown = true;
        for (int i = 0; i < 600; i++) c.step(1 / 30.0);
        c.kDown = false;
        check(c.eye[1] >= Camera.MIN_EYE - 1e-6, String.format(Locale.GERMAN, "Frei abwärts: bleibt bei %.2f m über dem Wasser", c.eye[1]));
        c.kFwd = true;
        double[] before = c.eye.clone();
        for (int i = 0; i < 30; i++) c.step(1 / 30.0);
        c.kFwd = false;
        check(dist(before, c.eye) > 3, "Frei: W fliegt vorwärts");
        c.setMode(Camera.Mode.KREISEN);
        c.step(1 / 60.0);
        check(c.eye[1] >= Camera.MIN_EYE - 1e-6, "Zurück zu Kreisen: Drehpunkt vor dem Auge, nicht unter Wasser");
        c.home();
        c.autoOrbit = true;
        double h0 = c.heading();
        for (int i = 0; i < 300; i++) c.step(1 / 30.0);
        check(Math.abs(c.heading() - h0) > 20, "R: kreist von selbst um den Turm");
        c.waterLevel = 1.5;
        c.home();
        c.lookAt(0, 0, 0, 0, -30, 50);
        c.snap();
        check(c.eye[1] >= 1.5 + Camera.MIN_EYE - 1e-6, "Hochwasser +1,5 m: das Auge steigt mit");
    }

    static double dist(double[] a, double[] b) { return Math.sqrt((a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]) + (a[2] - b[2]) * (a[2] - b[2])); }

    static void turm() {
        System.out.println("Turm Roter Sand (Phase 5)");
        Turm t = Turm.build();
        check(t.wrongWinding() == 0, String.format(Locale.GERMAN, "Wicklung: alle %,d Dreiecke außen gegen den Uhrzeigersinn (Lehre aus RayPhong)", t.triangles()));
        boolean norm = true, inRange = true;
        for (int i = 0; i < t.verts.length; i += Turm.STRIDE) {
            double n = Math.sqrt(t.verts[i + 3] * t.verts[i + 3] + t.verts[i + 4] * t.verts[i + 4] + t.verts[i + 5] * t.verts[i + 5]);
            if (Math.abs(n - 1) > 1e-4) norm = false;
        }
        for (int i : t.idx) if (i < 0 || i >= t.vertexCount()) inRange = false;
        check(norm && inRange, "Normalen normiert, alle Indizes zeigen auf Ecken");
        check(Math.abs(t.maxY("Turm") - Site.TOP) < 1e-4, String.format(Locale.GERMAN, "Mastspitze auf %.1f m (Roter Sand)", t.maxY("Turm")));
        check(Turm.GALLERY_TOP == Site.GALLERY && Turm.GLASS_BOTTOM < Site.LIGHT && Turm.GLASS_TOP > Site.LIGHT,
                "Galerie auf 23 m, Feuerhöhe 24 m liegt in der Verglasung der Laterne");
        check(Turm.SOCKEL_TOP == Site.PLINTH_TOP && Math.abs(Turm.shaftR(Site.PLINTH_TOP) - Turm.SHAFT_R0) < 1e-9 && Math.abs(Turm.shaftR(Turm.SHAFT_TOP) - Turm.SHAFT_R1) < 1e-9
                && Turm.shaftR(14) < Turm.shaftR(10), "Schaft verjüngt sich vom Sockel (7,5 m) zum Dienstraum (5,9 m)");
        double room = t.maxR("Turm", 20.0, 21.5), gal = t.maxR("Turm", 22.8, 23.0);
        check(Math.abs(room - (Turm.ERKER_D + Turm.ERKER_R)) < 0.1 && Math.abs(gal - Turm.GALLERY_R) < 0.01,
                String.format(Locale.GERMAN, "Dienstraum mit Erkern %.1f m breit, Galerie %.1f m", 2 * room, 2 * gal));
        check(java.util.Arrays.equals(Turm.ERKER_AZ, new double[]{315, 180, 45}), "Erker nach Nordwest, Süd und Nordost (der nordöstliche höher, mit Kuppel)");
        check(t.maxY("Manschette") == Turm.PLATE_TOP && t.maxY("Steinschüttung") < Turm.PLATE_TOP - 0.5,
                "Manschette auf mittlerem Meeresspiegel, Steinschüttung darunter (bei Niedrigwasser frei)");
        boolean inside = true;
        for (double[] c : Turm.plateCircles())
            for (int k = 0; k < 36; k++) {
                double a = k * Math.PI / 18;
                if (!Turm.onPlate(c[0] + Math.cos(a) * c[2] * 0.999, c[1] + Math.sin(a) * c[2] * 0.999)) inside = false;
            }
        check(inside, "Die Kreise für das Meer liegen ganz in der ovalen Manschette (kein Wasser auf der Platte, aber auch keins weggeschnitten)");
        double[] aw = Turm.alteWeserPos();
        double awAz = Math.toDegrees(Math.atan2(aw[0], -aw[1])), awKm = Math.hypot(aw[0], aw[1]) / 1000;
        check(awKm > 3 && awKm < 4.5 && awAz > 55 && awAz < 85 && t.maxY("Alte Weser") > 35 - aw[2],
                String.format(Locale.GERMAN, "Alte Weser in %.1f km, Richtung %03.0f°, Fuß %.1f m unter der Kimmlinie der Erdkrümmung", awKm, awAz, aw[2]));
        Wiring w = new Wiring(new SceneClock(LocalDate.of(2026, 10, 15), 12));
        com.dan.ocean.Ocean o = new com.dan.ocean.Ocean();
        w.tide.on = false;
        w.tide.fixed = -1.5;
        boolean dry = w.feedObstacles(o, false);
        int low = o.obstacles().size();
        w.tide.fixed = 1.5;
        boolean wetP = w.feedObstacles(o, dry);
        check(dry && !wetP && low == 5 && o.obstacles().size() == 2, "Meer: bei Ebbe brechen die Wellen an der Manschette, bei Flut am Sockel (Peilpfahl immer)");
    }

    static void gezeiten() {
        System.out.println("Gezeiten am Roten Sand");
        double t0 = ZonedDateTime.of(2026, 10, 17, 0, 0, 0, 0, SceneClock.ZONE).toInstant().toEpochMilli();
        double[] hw = Tide.next(t0, true);
        double hwH = hourOf(hw[0]);
        check(Math.abs(hwH - (4 + 15 / 60.0)) < 0.25, "17.10.2026 Hochwasser " + hm(hwH) + " MESZ (Bremerhaven 05:17, eine Stunde früher [G])");
        double[] nw = Tide.next(hw[0], false), hw2 = Tide.next(nw[0], true);
        double ebb = (nw[0] - hw[0]) / 3.6e6, period = (hw2[0] - hw[0]) / 3.6e6;
        check(Math.abs(period - 12.42) < 0.4 && ebb > 5.8 && ebb < 6.6, String.format(Locale.GERMAN, "Hochwasser zu Hochwasser %.2f h, Ebbe %.1f h", period, ebb));
        double maxR = 0, minR = 99;
        for (int d = 0; d < 30; d++) {
            double s = t0 + d * 86_400_000.0;
            double[] h1 = Tide.next(s, true), n1 = Tide.next(h1[0], false);
            double range = h1[1] - n1[1];
            maxR = Math.max(maxR, range);
            minR = Math.min(minR, range);
        }
        check(maxR > 3.0 && maxR < 3.6 && minR > 1.8 && minR < 2.4, String.format(Locale.GERMAN, "Tidenhub über einen Monat %.1f bis %.1f m (Nipp und Spring, im Mittel rund 3 m)", minR, maxR));
        // Springtide gut zwei Tage nach Vollmond (26.10.2026)
        double best = 0, bestDay = 0;
        for (int d = 0; d < 14; d++) {
            double s = ZonedDateTime.of(2026, 10, 20, 0, 0, 0, 0, SceneClock.ZONE).toInstant().toEpochMilli() + d * 86_400_000.0;
            double[] h1 = Tide.next(s, true);
            double r = h1[1] - Tide.next(h1[0], false)[1];
            if (r > best) { best = r; bestDay = 20 + d; }
        }
        check(bestDay >= 26 && bestDay <= 30, String.format(Locale.GERMAN, "Springtide am %.0f.10. (Vollmond 26.10., Alter der Gezeit rund zwei Tage)", bestDay));
        SceneClock c = new SceneClock(LocalDate.of(2026, 10, 17), 4.25);
        Wiring w = new Wiring(c);
        check(Math.abs(w.waterLevel.level() - Tide.at(c.epochMillis())) < 1e-9 && w.waterLevel.level() > 1.0, "Die Verdrahtung gibt die Gezeit als Wasserstand weiter");
        w.tide.on = false;
        w.tide.fixed = -0.7;
        check(w.waterLevel.level() == -0.7, "Gezeit abschaltbar: Wasserstand von Hand");
    }

    static void compute() {
        System.out.println("Compute-Test (Rechnung auf dem Prozessor)");
        check(ComputeProbe.expected(0, 63) == 64L * 65 / 2, "Gruppe 0, letzter Wert = 1 + … + 64 = 2080");
        check(ComputeProbe.expected(1, 0) == 65, "Gruppe 1, erster Wert = 65");
        long s = 0;
        for (int i = 0; i <= 10; i++) s += 3L * 64 + i + 1;
        check(ComputeProbe.expected(3, 10) == s, "Gruppe 3, Stelle 10 stimmt mit der Summe überein");
    }

    static void shader() {
        System.out.println("Shader-Dateien");
        String[][] progs = {{"fullscreen.vert", "sky.frag"}, {"fullscreen.vert", "sea.frag"}, {"fullscreen.vert", "skypano.frag"}, {"tower.vert", "tower.frag"},
                {"tower_shadow.vert", "tower_shadow.frag"}, {"tower_cast.vert", "tower_cast.frag"},
                {"fullscreen.vert", "post.frag"}, {"fullscreen.vert", "overlay.frag"}, {"probe.comp"}};
        for (String[] pr : progs) {
            boolean good = true;
            String msg = "";
            for (String f : pr) {
                try {
                    String src = Shader.source(f, null).text;
                    if (!src.startsWith("#version 430 core")) { good = false; msg = f + " ohne Kopf"; }
                    if (src.contains("#include")) { good = false; msg = f + ": #include nicht aufgelöst"; }
                    int open = src.length() - src.replace("{", "").length(), close = src.length() - src.replace("}", "").length();
                    if (open != close) { good = false; msg = f + ": Klammern { } ungleich"; }
                } catch (RuntimeException e) {
                    good = false;
                    msg = e.getMessage();
                }
            }
            check(good, String.join(" + ", pr) + (good ? " vollständig" : ": " + msg));
        }
        String sea = Shader.source("sea.frag", null).text;
        check(sea.contains("gl_FragDepth") && sea.contains("EARTH_R"), "Platzhalter-See (Rückfall ohne Compute) schreibt ihre Tiefe und rechnet mit der Erdkrümmung");
        String pano = Shader.source("skypano.frag", null).text;
        check(pano.contains("atan") || pano.contains("sin(az)"), "Himmelspanorama nutzt dieselbe Abbildung wie das Meer (Azimut von Nord, Höhe ±90°)");
    }

    static void verdrahtung() throws Exception {
        System.out.println("Verdrahtung");
        check(Beaufort.of(0.2) == 0 && Beaufort.of(0.3) == 1 && Beaufort.of(10.79) == 5 && Beaufort.of(10.8) == 6 && Beaufort.of(40) == 12,
                "Beaufort-Grenzen nach WMO (0,3 · 10,8 · 32,7 m/s)");
        boolean mids = true;
        for (int b = 0; b <= 12; b++) if (Beaufort.of(Beaufort.mid(b)) != b) mids = false;
        check(mids, "Beaufort.mid(n) liegt für alle 13 Stufen in der Stufe n");
        Wind west = new Wind() {
            public double speed() { return 10; }
            public double fromDeg() { return 270; }
        };
        double[] to = west.toVector();
        check(to[0] > 0.999 && Math.abs(to[2]) < 1e-9, "Westwind weht nach Osten (+x)");
        Collider cyl = Collider.cylinder(0, 0, 4.5, -10, 8);
        check(cyl.distance(5.5, 0, 0) > 0.99 && cyl.distance(5.5, 0, 0) < 1.01 && cyl.distance(0, 0, 0) < 0 && cyl.distance(0, 10, 0) > 1.99,
                "Collider-Zylinder: Abstand außen, innen negativ, über der Kappe");
        Wiring w = new Wiring(new SceneClock());
        check(w.links().size() >= 18, "Verbindungen eingetragen: " + w.links().size() + " (Phase 1 bis 4)");
        com.dan.ocean.Ocean o = new com.dan.ocean.Ocean();
        w.wind.speed = 15.5;
        w.wind.fromDeg = 300;
        w.waterLevel = () -> 1.2;
        w.feed(o);
        check(o.windSpeed == 15.5 && o.windFromDeg == 300 && o.waterLevel == 1.2, "Wind und Wasserstand gehen über die Verdrahtung ins Meer");
        File dir = new File("src/com/dan/lighthouse/wiring");
        if (!dir.isDirectory()) { check(false, "Ordner " + dir + " nicht gefunden (Arbeitsverzeichnis = Projektordner?)"); return; }
        for (Class<?> c : Wiring.INTERFACES) {
            File f = new File(dir, c.getSimpleName() + ".java");
            List<String> bad = foreignImports(f, "com.dan.lighthouse.wiring", false, false);
            check(f.isFile() && bad.isEmpty(), c.getSimpleName() + ": nur Zahlen und java.*" + (bad.isEmpty() ? "" : " – " + bad));
        }
    }

    /** Phase 2: die Prüfung des Meeres (com.dan.ocean.demo.OceanCheck) läuft mit, dazu die Brücken in der App. */
    static void meer() {
        System.out.println("Meer (com.dan.ocean)");
        com.dan.ocean.demo.OceanCheck oc = new com.dan.ocean.demo.OceanCheck();
        for (String l : oc.run()) {
            boolean good = l.startsWith("  ok");
            check(good, l.substring(8).trim());
        }
        boolean map = true;
        for (Quality q : Quality.values()) if (q.ocean == null) map = false;
        check(map && Quality.HOCH.ocean.cascades == 4 && Quality.NIEDRIG.ocean.n == 128, "Qualitätsstufen der App wählen die Stufe des Meeres");
        double[] noon = com.dan.lighthouse.gl.Renderer.sunLight(30), dusk = com.dan.lighthouse.gl.Renderer.sunLight(2), night = com.dan.lighthouse.gl.Renderer.sunLight(-5);
        check(noon[0] > 2.5 && noon[2] > 2 && dusk[2] < dusk[0] * 0.6 && night[0] == 0, "Sonnenlicht fürs Meer: weiß am Tag, golden tief, aus in der Nacht");
    }

    static void himmel() {
        System.out.println("Himmel (com.dan.sky)");
        com.dan.sky.demo.SkyCheck sc = new com.dan.sky.demo.SkyCheck();
        for (String l : sc.run()) check(l.startsWith("  ok"), l.substring(8).trim());
        // Verdrahtung: Zeit, Ort, Augenhöhe, Sicht und Bedeckung gehen in den Himmel, das Licht kommt zurück
        SceneClock c = new SceneClock(LocalDate.of(2026, 10, 15), 13.0);
        Wiring w = new Wiring(c);
        w.visibilityKm = 25;
        w.oktas = 2.4;
        com.dan.sky.Sky sky = new com.dan.sky.Sky();
        w.feed(sky, 18.5);
        check(sky.epochMillis == c.epochMillis() && sky.latDeg == Site.LAT && sky.lonDeg == Site.LON && sky.eyeHeight == 18.5
                && sky.visibilityKm == 25 && sky.cloudCover == 0.3, "Uhr, Ort, Augenhöhe, Sicht und Bedeckung gehen über die Verdrahtung in den Himmel");
        sky.cloudCover = 0;
        com.dan.sky.Sky.State st = sky.rebuildNow();
        com.dan.lighthouse.wiring.Lighting li = Wiring.lighting(sky);
        check(li.sunDir()[1] > 0.4 && Math.abs(li.sunColor()[1] - st.sunColor[1]) < 1e-12 && li.haze() > 3.912 / 26_000 && li.haze() < 3.912 / 24_000,
                String.format(Locale.GERMAN, "Lighting aus dem Himmel: Sonne %.1f° hoch, Dunst %.2e je m (Sicht 25 km)", Math.toDegrees(Math.asin(li.sunDir()[1])), li.haze()));
        double[] sp = c.sun();
        check(Math.abs(sp[0] + com.dan.sky.Celestial.refraction(sp[0]) - st.sunAlt) < 0.02 && Math.abs(sp[1] - st.sunAz) < 0.05,
                String.format(Locale.GERMAN, "Sonne der App-Uhr (Phase 1) und des Himmels stimmen überein (%.3f° gegen %.3f° mit Brechung)", sp[0], st.sunAlt));
        // Lichtglocken: Bremerhaven liegt im Südosten, rund 48 km entfernt
        Site.Town bhv = Site.TOWNS[0];
        check(Math.abs(bhv.azimuthDeg() - 137) < 3 && Math.abs(bhv.distanceKm() - 48) < 3,
                String.format(Locale.GERMAN, "Lichtglocke Bremerhaven: %03.0f°, %.0f km", bhv.azimuthDeg(), bhv.distanceKm()));
        String common = Shader.source("common.glsl", null).text, post = Shader.source("post.frag", null).text;
        check(common.contains("uSkyMode") && common.contains("panoSample") && post.contains("uScotopic"),
                "App-Shader lesen Himmel und Licht aus com.dan.sky (Rundumbild, Vorbelichtung, Stäbchensehen)");
    }

    /** Phase 4: die Prüfung der Wolken (com.dan.cloud.demo.CloudCheck) läuft mit, dazu die Brücken in der App. */
    static void wolken() throws Exception {
        System.out.println("Wolken (com.dan.cloud)");
        com.dan.cloud.demo.CloudCheck cc = new com.dan.cloud.demo.CloudCheck();
        for (String l : cc.run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        Wiring w = new Wiring(new SceneClock(LocalDate.of(2026, 10, 15), 13.0));
        w.oktas = 5;
        w.cloudType = com.dan.cloud.CloudType.STRATOCUMULUS;
        w.highCover = 0.3;
        w.wind.speed = 12;
        w.wind.fromDeg = 240;
        com.dan.cloud.Clouds c = new com.dan.cloud.Clouds();
        w.feed(c);
        check(c.oktas == 5 && c.type == com.dan.cloud.CloudType.STRATOCUMULUS && c.highCover == 0.3 && Math.abs(c.windSpeed - 19.2) < 1e-9
                && c.windFromDeg == 260, "Bedeckung, Art, hohe Schicht und Wind (in Wolkenhöhe 1,6-fach, 20° rechts gedreht) gehen in die Wolken");
        com.dan.sky.Sky sky = new com.dan.sky.Sky();
        w.cloudsDrawn = false;
        w.feed(sky, 10);
        boolean veil = sky.cloudCover == 5 / 8.0 && sky.cloudVeil;
        w.cloudsDrawn = true;
        w.feed(sky, 10);
        check(veil && sky.cloudCover == 0 && !sky.cloudVeil, "Ohne Wolken-Paket graut der Schleier den Himmel, mit Wolken nicht mehr");
        boolean map = true;
        for (Quality q : Quality.values()) if (q.clouds == null) map = false;
        check(map && Quality.NIEDRIG.clouds == com.dan.cloud.Clouds.Quality.FLAT && Quality.MITTEL.clouds == com.dan.cloud.Clouds.Quality.MEDIUM,
                "Qualitätsstufen der App wählen die Stufe der Wolken (Niedrig = 2.5D, Mittel = 1/4 Auflösung)");
        java.lang.reflect.Field pf = com.dan.lighthouse.gl.Renderer.class.getDeclaredField("PASSES");
        pf.setAccessible(true);
        check(java.util.Arrays.asList((String[]) pf.get(null)).contains("Wolken"), "Zeitmessung der Durchgänge enthält „Wolken“");
        String surf = Files.readString(new File("src/com/dan/ocean/gl/surface.frag").toPath(), StandardCharsets.UTF_8);
        check(surf.contains("uCloudShadow") && surf.contains("uShadowRect"), "Das Meer nimmt die Schattenkarte der Wolken (ohne com.dan.cloud zu kennen)");
    }

    /** Phase 5: die Prüfung der Verwitterung (com.dan.weathering.demo.WeatheringCheck) läuft mit, dazu die Brücken. */
    static void verwitterung() throws Exception {
        System.out.println("Verwitterung (com.dan.weathering)");
        for (String l : new com.dan.weathering.demo.WeatheringCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        Wiring w = new Wiring(new SceneClock(LocalDate.of(2026, 10, 15), 13.0));
        w.tide.on = false;
        w.tide.fixed = 0.8;
        w.wind.speed = 12;
        w.wind.fromDeg = 250;
        com.dan.weathering.Weathering we = new com.dan.weathering.Weathering();
        w.feed(we, 2.5, 0.6);
        check(we.waterLevel == 0.8 && we.meanHighWater == Wiring.MHW && we.meanLowWater == Wiring.MLW && we.waveHeight == 2.5 && we.windSpeed == 12
                && we.windFromDeg == 250 && we.sun == 0.6, "Wasserstand, Gezeitenband, Seegang, Wind und Sonne gehen in die Verwitterung");
        String frag = Shader.source("tower.frag", null).text;
        check(frag.contains("wthApply") && frag.contains("uWthMap") && !frag.contains("#include"), "Der Turm-Shader bindet weathering.glsl aus dem Klassenpfad ein (/com/dan/weathering/gl/)");
        String surf = Files.readString(new File("src/com/dan/ocean/gl/surface.frag").toPath(), StandardCharsets.UTF_8);
        check(surf.contains("uBodyShadow") && surf.contains("uBodyRect"), "Das Meer nimmt den Schatten eines Körpers (Turm) als zweite Schattenkarte, ohne den Turm zu kennen");
        java.lang.reflect.Field pf = com.dan.lighthouse.gl.Renderer.class.getDeclaredField("PASSES");
        pf.setAccessible(true);
        java.util.List<String> passes = java.util.Arrays.asList((String[]) pf.get(null));
        check(passes.contains("Schatten") && passes.contains("Turm") && passes.indexOf("Schatten") < passes.indexOf("Meer"),
                "Zeitmessung: „Schatten“ vor dem Meer (das den Turmschatten liest), dann „Turm“");
        boolean q = true;
        for (Quality x : Quality.values()) if (x.weathering == null || x.towerShadow < 1024) q = false;
        check(q && Quality.NIEDRIG.towerShadow == 1024 && !Quality.NIEDRIG.towerDetail && Quality.MITTEL.towerShadow == 2048,
                "Qualitätsstufen wählen Turmschatten (1024² bis 4096²), Nieten und die Nässekarte");
    }

    /** Phase 6: die Prüfung der Gischt (com.dan.spray.demo.SprayCheck) läuft mit, dazu die Brücken. */
    static void gischt() throws Exception {
        System.out.println("Gischt (com.dan.spray)");
        for (String l : new com.dan.spray.demo.SprayCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        Wiring w = new Wiring(new SceneClock(LocalDate.of(2026, 10, 15), 13.0));
        w.tide.on = false;
        w.wind.speed = 21;
        w.wind.fromDeg = 250;
        com.dan.spray.Spray sp = new com.dan.spray.Spray();
        w.feed(sp, 5.5);
        check(sp.windSpeed == 21 && sp.windFromDeg == 250 && sp.waveHeight == 5.5, "Wind und Seegang gehen in die Gischt");
        w.tide.fixed = -2.5;
        boolean low = w.feedBodies(sp, false, true);
        int nLow = sp.bodies().size();
        w.tide.fixed = 1.5;
        boolean high = w.feedBodies(sp, low, false);
        int nHigh = sp.bodies().size();
        check(low && !high && nLow > nHigh && nHigh == 3,
                String.format(Locale.GERMAN, "Körper der Gischt: bei Ebbe mit Manschette (%d), bei Flut Sockel, Schaft und Peilpfahl (%d)", nLow, nHigh));
        java.lang.reflect.Field pf = com.dan.lighthouse.gl.Renderer.class.getDeclaredField("PASSES");
        pf.setAccessible(true);
        java.util.List<String> passes = java.util.Arrays.asList((String[]) pf.get(null));
        check(passes.indexOf("Gischt") > passes.indexOf("Turm") && passes.indexOf("Gischt") < passes.indexOf("Nachbearbeitung"),
                "Zeitmessung: „Gischt“ nach dem Turm (liest dessen Tiefe), vor der Nachbearbeitung");
        boolean q = true;
        for (Quality x : Quality.values()) if (x.spray == null) q = false;
        check(q && Quality.NIEDRIG.spray == com.dan.spray.Spray.Quality.LOW && Quality.ULTRA.spray == com.dan.spray.Spray.Quality.ULTRA,
                "Qualitätsstufen der App wählen die Stufe der Gischt");
    }

    /** Phase 7: die Prüfung des Regens (com.dan.rain.demo.RainCheck) läuft mit, dazu die Brücken. */
    static void regen() throws Exception {
        System.out.println("Regen (com.dan.rain)");
        for (String l : new com.dan.rain.demo.RainCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        Wiring w = new Wiring(new SceneClock(LocalDate.of(2026, 10, 16), 13.0));
        w.tide.on = false;
        w.tide.fixed = -0.7;
        w.wind.speed = 14;
        w.wind.fromDeg = 250;
        w.rainRate = 12;
        com.dan.cloud.Clouds c = com.dan.cloud.Clouds.of(com.dan.cloud.CloudType.NIMBOSTRATUS, 8);
        com.dan.rain.Rain rn = new com.dan.rain.Rain();
        w.feed(rn, c, c.snapshot(), true, new double[]{30, 12, -40});
        com.dan.cloud.Clouds.Snapshot cs = c.snapshot();
        w.feed(rn, c, cs, true, new double[]{30, 12, -40});
        check(rn.rate == 12 && rn.windSpeed == 14 && rn.windFromDeg == 250 && rn.waterLevel == -0.7 && rn.eyeX == 30 && rn.cloudBase == cs.type().base
                && c.precipitation > 0, "Regen, Wind, Wasserstand, Auge und Wolkenuntergrenze gehen in den Regen, der Niederschlag in die Wolken");
        rn.step(0.6);
        double ns = rn.rateAt(0, 0);
        com.dan.cloud.Clouds cb = com.dan.cloud.Clouds.of(com.dan.cloud.CloudType.CUMULONIMBUS, 7);
        cb.precipitation = 0.8;
        com.dan.cloud.Clouds.Snapshot bs = cb.snapshot();
        int wet = 0, n = 0;
        for (int i = 0; i < 60; i++)
            for (int j = 0; j < 60; j++) { n++; if (Wiring.curtain(bs, i * 250 - 7500, j * 250 - 7500, 0.8) > 0.2) wet++; }
        check(ns > 9 && wet > n * 0.1 && wet < n * 0.6,
                String.format(Locale.GERMAN, "Landregen (Nimbostratus 8/8) am Turm %.1f von 12 mm/h; unter Gewitterwolken 7/8 regnet es auf %.0f %% der Fläche (Schauer)", ns, 100.0 * wet / n));
        w.feed(rn, c, cs, false, new double[]{0, 10, 0});
        check(rn.curtain == null, "ohne gezeichnete Wolken regnet es überall gleich");
        String surf = Files.readString(new File("src/com/dan/ocean/gl/surface.frag").toPath(), StandardCharsets.UTF_8);
        check(surf.contains("uDetail") && !surf.contains("rain"), "Das Meer nimmt eine feine Störung (die Ringkarte des Regens) als Textur, ohne den Regen zu kennen");
        java.lang.reflect.Field pf = com.dan.lighthouse.gl.Renderer.class.getDeclaredField("PASSES");
        pf.setAccessible(true);
        java.util.List<String> passes = java.util.Arrays.asList((String[]) pf.get(null));
        check(passes.indexOf("Regen") == passes.indexOf("Gischt") + 1 && passes.indexOf("Regen") < passes.indexOf("Nachbearbeitung"),
                "Zeitmessung: „Regen“ nach der Gischt, vor der Nachbearbeitung");
        boolean q = true;
        for (Quality x : Quality.values()) if (x.rain == null) q = false;
        check(q && Quality.NIEDRIG.rain == com.dan.rain.Rain.Quality.LOW && Quality.ULTRA.rain == com.dan.rain.Rain.Quality.ULTRA,
                "Qualitätsstufen der App wählen die Stufe des Regens");
    }

    static void scheiben() throws Exception {
        System.out.println("Nasse Scheiben (com.dan.wetglass, Phase 8a)");
        for (String l : new com.dan.wetglass.demo.WetglassCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        Wiring w = new Wiring(new SceneClock(LocalDate.of(2026, 10, 16), 13.0));
        w.wind.speed = 14;
        w.wind.fromDeg = 270;
        com.dan.rain.Rain rn = new com.dan.rain.Rain();
        rn.rate = 6;
        rn.windSpeed = 14;
        rn.windFromDeg = 270;
        rn.step(0.6);
        com.dan.wetglass.Wetglass[] g = {new com.dan.wetglass.Wetglass(), new com.dan.wetglass.Wetglass(), new com.dan.wetglass.Wetglass()};
        w.feed(g, rn);
        check(g[0].hitRate > 0 && g[0].hitRate > g[1].hitRate && g[1].hitRate >= 0.25 * g[0].hitRate - 1e-9 && g[2].hitRate >= 0.05 * g[0].hitRate - 1e-9
                        && g[2].hitRate < g[1].hitRate,
                String.format(Locale.GERMAN, "Regen auf die Scheiben: Luv %.0f, Seite %.0f, Lee %.0f Einschläge je m²s (Seite mindestens ¼, Lee mindestens ¹⁄₂₀ von Luv)",
                        g[0].hitRate, g[1].hitRate, g[2].hitRate));
        check(g[1].windX > g[0].windX && g[1].windX > g[2].windX && Math.abs(g[0].drainLength - (Turm.GLASS_TOP - Turm.GLASS_BOTTOM) / 2) < 1e-9
                        && g[0].hitLambda == com.dan.rain.Rain.lambda(6),
                "Wind schiebt die Läufer auf der Seitenscheibe am stärksten; Tropfengröße und Länge der Scheibe aus Regen und Turm");
        rn.rate = 0;
        rn.step(0.6);
        w.feed(g, rn);
        check(g[0].hitRate == 0 && g[0].evaporation > 1e-7, "nach dem Regen keine Einschläge mehr, die Scheiben trocknen schneller");
        Turm t = Turm.build();
        Turm.Part lr = t.part("Laternenraum");
        check(lr != null && lr.count() > 1000 && t.wrongWinding() == 0,
                String.format(Locale.GERMAN, "Turm: Teil „Laternenraum“ mit %,d Dreiecken (Dielen, Brüstung, Kuppel, Reling, Linse), Wicklung stimmt", lr == null ? 0 : lr.count() / 3));
        rn.rate = 6;
        w.feed(rn, com.dan.cloud.Clouds.of(com.dan.cloud.CloudType.NIMBOSTRATUS, 8), com.dan.cloud.Clouds.of(com.dan.cloud.CloudType.NIMBOSTRATUS, 8).snapshot(), false, new double[]{0, 25, 0});
        check(rn.shelter != null && rn.shelter.length >= 4 && rn.shelter[2] >= Turm.LANTERN_R && rn.shelter[3] >= Turm.DOME_TOP - 1e-9,
                "Unter der Kuppel regnet es nicht: der Laternenraum ist ein Unterstand für den Regen");
        java.lang.reflect.Field pf = com.dan.lighthouse.gl.Renderer.class.getDeclaredField("PASSES");
        pf.setAccessible(true);
        java.util.List<String> passes = java.util.Arrays.asList((String[]) pf.get(null));
        check(passes.indexOf("Scheiben") > passes.indexOf("Regen") && passes.indexOf("Scheiben") < passes.indexOf("Nachbearbeitung"),
                "Zeitmessung: „Scheiben“ nach dem Regen (und dem Feuer), vor der Nachbearbeitung");
        boolean q = true;
        for (Quality x : Quality.values()) if (x.wetglass == null) q = false;
        check(q && Quality.NIEDRIG.wetglass == com.dan.wetglass.Wetglass.Quality.LOW && Quality.ULTRA.wetglass == com.dan.wetglass.Wetglass.Quality.ULTRA,
                "Qualitätsstufen der App wählen die Stufe der Scheiben");
        String frag = Files.readString(new File("src/com/dan/lighthouse/gl/shader/glass.frag").toPath(), StandardCharsets.UTF_8);
        String block = Files.readString(new File("src/com/dan/wetglass/gl/wetglass.glsl").toPath(), StandardCharsets.UTF_8);
        check(frag.contains("#include \"/com/dan/wetglass/gl/wetglass.glsl\"") && frag.contains("wgOffset") && !block.contains("lighthouse") && !block.contains("uCam"),
                "Die Scheibe der App nimmt den Baustein wetglass.glsl; der Baustein kennt die App nicht");
        String sh = Files.readString(new File("src/com/dan/lighthouse/gl/shader/tower_shadow.frag").toPath(), StandardCharsets.UTF_8);
        check(sh.contains("discard"), "Glas wirft keinen Schatten (sonst läge der Laternenraum im Dunkeln)");
        // Phase 8b: Beschlag, Salz, Überspülung, Handlauf
        System.out.println("Phase 8b: Beschlag, Salz, Überspülung, Handlauf");
        check(Math.abs(Wiring.dewPoint(20, 0.5) - 9.3) < 0.2 && Math.abs(Wiring.dewPoint(11, 1.0) - 11) < 0.05,
                String.format(Locale.GERMAN, "Taupunkt nach Magnus: 20 °C bei 50 %% → %.1f °C, gesättigt gleich der Temperatur", Wiring.dewPoint(20, 0.5)));
        com.dan.rain.Rain wet = new com.dan.rain.Rain();
        wet.rate = 4;
        wet.step(0.6);
        com.dan.rain.Rain dry = new com.dan.rain.Rain();
        dry.rate = 0;
        dry.step(0.6);
        w.oktas = 8;
        double dRain = w.feedFog(g, wet, true, -5);
        double cRain = g[0].condensation;
        w.oktas = 2;
        double dSun = w.feedFog(g, dry, false, 40);
        double cSun = g[0].condensation;
        w.fogOn = false;
        w.feedFog(g, wet, true, -5);
        double cOff = g[0].condensation;
        w.fogOn = true;
        check(dRain > 0 && cRain > 0 && dSun < 0 && cSun < 0 && cOff < 0,
                String.format(Locale.GERMAN, "Beschlag: abends im Regen mit Lampe liegt der Taupunkt im Raum %.1f K über dem Glas (es beschlägt), mittags in der Sonne %.1f K darunter (es trocknet); ausgeschaltet trocknet es", dRain, -dSun));
        com.dan.wetglass.Wetglass rl = new com.dan.wetglass.Wetglass();
        w.wind.speed = 25;
        w.feedSalt(g, rl, null);
        double s25 = g[0].saltRate;
        boolean shares = Math.abs(g[1].saltRate - s25 / 3) < 1e-12 && Math.abs(g[2].saltRate - s25 * 0.05) < 1e-12 && rl.saltRate == s25;
        w.wind.speed = 8;
        w.feedSalt(g, rl, null);
        check(s25 > 1e-5 && shares && g[0].saltRate == 0,
                String.format(Locale.GERMAN, "Salz: bei 25 m/s setzt sich Seesalz ab (volle Kruste in %.1f h), Seite ein Drittel, Lee ein Zwanzigstel; bei 8 m/s ohne Gischt nichts", 1 / s25 / 3600));
        com.dan.spray.Spray sp = new com.dan.spray.Spray();
        sp.waveHeight = 9;
        com.dan.spray.Body tb = com.dan.spray.Body.cylinder(0, 0, 4, -10, 20);
        com.dan.wetglass.Wetglass[] fg = {new com.dan.wetglass.Wetglass(), new com.dan.wetglass.Wetglass(), new com.dan.wetglass.Wetglass()};
        com.dan.wetglass.Wetglass fr = new com.dan.wetglass.Wetglass();
        double none0 = w.feedFlood(sp, fg, fr, 2);
        sp.splash(tb, 0, 1, 8);
        double low = w.feedFlood(sp, fg, fr, 2);
        sp.splash(tb, 0, 1, 26);
        double high = w.feedFlood(sp, fg, fr, 2);
        check(none0 == 0 && low == 0 && high > 0.5 && fg[0].sheetMm() >= high - 1e-9 && fg[1].sheetMm() > 0 && fg[2].sheetMm() == 0 && fr.sheetMm() > 0,
                String.format(Locale.GERMAN, "Überspülung: ein Schwall 8 m über den Kamm bleibt unter der Galerie, einer von 26 m legt %.1f mm Seewasser über Wetterscheibe und Handlauf, die Seite halb, das Lee nichts", high));
        com.dan.wetglass.Wetglass.Cylinder cy = new com.dan.wetglass.Wetglass.Cylinder(Turm.HANDRAIL_T, true);
        w.wind.speed = 12;
        w.feedRail(rl, cy, wet);
        check(rl.hitRate > 0 && Math.abs(cy.wind - 12 * 0.66) < 1e-9, "Handlauf: Regen quer zum Rohr und der Wind quer zum Ring gehen in den liegenden Zylinder");
        Turm.Part hr = t.part("Handlauf");
        check(hr != null && hr.count() > 1000 && t.wrongWinding() == 0 && Math.abs(Turm.HANDRAIL_Y - Turm.GALLERY_TOP - 1.05) < 1e-9,
                String.format(Locale.GERMAN, "Turm: runder Handlauf der Galerie als eigener Teil (%,d Dreiecke, Rohr %.0f mm), Wicklung stimmt", hr == null ? 0 : hr.count() / 3, Turm.HANDRAIL_T * 2000));
        String railFrag = Files.readString(new File("src/com/dan/lighthouse/gl/shader/rail_wet.frag").toPath(), StandardCharsets.UTF_8);
        check(railFrag.contains("#include \"/com/dan/wetglass/gl/wetglass.glsl\"") && frag.contains("wgFog") && frag.contains("wgSalt") && block.contains("wgSheet"),
                "Derselbe Baustein auf Scheibe und Handlauf; die Scheibe zeigt Beschlag, Salz und die Wasserhaut");
        check(new com.dan.lighthouse.gl.Renderer(new SceneClock(LocalDate.of(2026, 10, 16), 13.0), w).simThreaded,
                "Die Tropfen rechnen in einem eigenen Faden (Standbilder schalten ihn ab)");
    }

    /** Die Element-Packages: alles unter src/com/dan außer der App. */
    static List<File> elementPackages() {
        List<File> l = new ArrayList<>();
        File[] all = new File("src/com/dan").listFiles(File::isDirectory);
        if (all != null) for (File f : all) if (!f.getName().equals("lighthouse")) l.add(f);
        l.sort(java.util.Comparator.comparing(File::getName));
        return l;
    }

    static final Pattern IMPORT = Pattern.compile("^\\s*import\\s+(static\\s+)?([\\w.]+)", Pattern.MULTILINE),
            FQN = Pattern.compile("\\bcom\\.dan\\.[\\w.]*\\w");

    /** Packages aus lib/FStyle.jar (seit Phase 13 gelesen: com.dan.fog und com.dan.flock beginnen auch mit „com.dan.f“). */
    static final java.util.Set<String> FSTYLE = new java.util.HashSet<>();

    static boolean fstyle(String name) {
        if (FSTYLE.isEmpty()) {
            try (java.util.zip.ZipFile z = new java.util.zip.ZipFile("lib/FStyle.jar")) {
                z.stream().map(java.util.zip.ZipEntry::getName).filter(n -> n.endsWith(".class") && n.contains("/"))
                        .forEach(n -> FSTYLE.add(n.substring(0, n.lastIndexOf('/')).replace('/', '.')));
            } catch (Exception e) {
                FSTYLE.add("com.dan.fstyle");
            }
        }
        for (String p = name; p.contains("."); p = p.substring(0, p.lastIndexOf('.'))) if (FSTYLE.contains(p)) return true;
        return false;
    }

    /** Importe und voll qualifizierte Namen, die nicht erlaubt sind: alles außer java.*, javax.*, dem eigenen Package (und JOGL, FStyle, wo erlaubt). */
    static List<String> foreignImports(File f, String own, boolean jogl, boolean fstyle) throws Exception {
        List<String> bad = new ArrayList<>();
        String src = Files.readString(f.toPath(), StandardCharsets.UTF_8);
        Matcher m = IMPORT.matcher(src);
        while (m.find()) {
            String imp = m.group(2);
            if (imp.startsWith("java.") || imp.startsWith("javax.") || imp.equals(own) || imp.startsWith(own + ".")) continue;
            if (jogl && imp.startsWith("com.jogamp.")) continue;
            if (fstyle && fstyle(imp)) continue;
            bad.add(imp);
        }
        String code = src.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\n]*", "").replaceAll("(?m)^\\s*(package|import)\\s.*$", "");
        Matcher q = FQN.matcher(code);
        while (q.find()) {
            String n = q.group();
            if (n.equals(own) || n.startsWith(own + ".") || (fstyle && fstyle(n))) continue;
            bad.add(n);
        }
        return bad;
    }

    /** Phase 9: die Prüfung des Leuchtfeuers (com.dan.beacon.demo.BeaconCheck) läuft mit, dazu die Brücken in der App. */
    static void leuchtfeuer() throws Exception {
        System.out.println("Leuchtfeuer (com.dan.beacon, Phase 9)");
        for (String l : new com.dan.beacon.demo.BeaconCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        System.out.println("Phase 9 in der App");
        Wiring w = new Wiring(new SceneClock());
        long n9 = w.links().stream().filter(l -> l.phase() == 9).count();
        check(n9 >= 6, "Verbindungen der Phase 9 eingetragen: " + n9);
        com.dan.beacon.Beacon b = com.dan.beacon.Beacon.roterSand1985();
        w.visibilityKm = 12;
        w.feed(b, 20);
        check(Math.abs(b.y - Turm.LENS_Y) < 1e-9 && Math.abs(b.hazePerM - 2.996 / 12000) < 1e-9 && b.ambientLux == 20,
                String.format(Locale.GERMAN, "Feuer in der Linsenmitte (%.2f m), Dunst aus der Sicht, Helligkeit am Turm für den Dämmerungsschalter", Turm.LENS_Y));
        com.dan.rain.Rain rr = com.dan.rain.Rain.moderate();
        rr.step(0);
        double sig = Wiring.scatter(rr, null).density(100, 10, 50);
        check(Math.abs(sig - com.dan.rain.Rain.extinctionPerKm(5) / 1000) < 1e-6, String.format(Locale.GERMAN, "Streudichte aus dem Regen: 5 mm/h → %.2e /m (Auslöschung %.2f /km)", sig, com.dan.rain.Rain.extinctionPerKm(5)));
        double[] eye = {0, 5, -1000};
        b.snapOn(true);
        com.dan.lighthouse.wiring.SpotLight sl = Wiring.spotLight(b, eye);
        check(sl.candela() > 500 && sl.halfAngleDeg() == 180 && Math.abs(sl.position()[1] - Turm.LENS_Y) < 1e-9,
                String.format(Locale.GERMAN, "SpotLight: Rundumlicht, %.0f cd zum Auge im Norden (Peilung 180°, weißer Sektor)", sl.candela()));
        // die Linse im Modell ist die gerechnete Linse
        java.util.List<double[]> pr = com.dan.beacon.Lens.drum().prisms();
        check(pr.size() == 25 && Turm.build().part("Laternenraum").count() > 0, "Gürtellinse im Modell aus den 25 Prismenringen von com.dan.beacon.Lens");
        String frag = Shader.source("tower.frag", null).text;
        check(frag.contains("bcnNearIrradiance") && frag.contains("bcnLensLuminance") && !frag.contains("#include"), "Der Turm-Shader bindet beacon.glsl ein: Linse von außen, Lichtflecken im Laternenraum");
        String sea = new String(com.dan.ocean.gl.OceanGl.class.getResourceAsStream("surface.frag").readAllBytes(), StandardCharsets.UTF_8);
        check(sea.contains("uPointTex") && sea.contains("pointIntensity"), "Das Meer kennt ein Punktlicht mit Rundum-Verteilung (Glitzer des Feuers), ohne das Feuer zu kennen");
        check(Quality.NIEDRIG.beacon == com.dan.beacon.Beacon.Quality.LOW && Quality.ULTRA.beacon == com.dan.beacon.Beacon.Quality.ULTRA, "Qualitätsstufen reichen bis ins Leuchtfeuer");
        java.lang.reflect.Field pf = com.dan.lighthouse.gl.Renderer.class.getDeclaredField("PASSES");
        pf.setAccessible(true);
        java.util.List<String> passes = java.util.Arrays.asList((String[]) pf.get(null));
        check(passes.indexOf("Feuer") > passes.indexOf("Regen") && passes.indexOf("Feuer") < passes.indexOf("Scheiben"), "Zeitmessung: „Feuer“ nach dem Regen (seit Phase 13 mit dem Nebel dazwischen), vor den Scheiben");
    }

    /** Phase 10: die Prüfung des Gewitters (com.dan.lightning.demo.LightningCheck) läuft mit, dazu die Brücken in der App. */
    static void gewitter() throws Exception {
        System.out.println("Gewitter (com.dan.lightning, Phase 10)");
        for (String l : new com.dan.lightning.demo.LightningCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        System.out.println("Phase 10 in der App");
        Wiring w = new Wiring(new SceneClock());
        long n10 = w.links().stream().filter(l -> l.phase() == 10).count();
        check(n10 >= 8, "Verbindungen der Phase 10 eingetragen: " + n10);
        com.dan.lightning.Lightning L = new com.dan.lightning.Lightning();
        Wiring.attractors(L, new double[]{9000, 37, -2000});
        com.dan.lightning.Lightning.Attractor top = L.attractors.get(0);
        check(Math.abs(top.y() - Turm.MAST_TOP) < 1e-9 && Math.hypot(top.x(), top.z()) < Turm.GALLERY_R, String.format(Locale.GERMAN,
                "Fangpunkt: die Spitze des Masts auf der Galerie (%.1f m), dazu die Alte Weser", top.y()));
        com.dan.cloud.Clouds c = com.dan.cloud.Clouds.of(com.dan.cloud.CloudType.CUMULONIMBUS, 7);
        com.dan.cloud.Noise.weather();
        c.step(0.1);
        com.dan.cloud.Clouds.Snapshot cs = c.snapshot();
        w.thunderstorm = com.dan.lightning.Lightning.Level.STRONG;
        double[] eye = {0, 10, 300};
        w.feed(L, cs, true, eye);
        check(L.level() == com.dan.lightning.Lightning.Level.STRONG && L.cloudBase == cs.type().base && L.listenerZ == 300 && L.cells != null && L.occlusion != null,
                "Stärke von Hand, Wolkenuntergrenze, Zellen aus dem Regenfeld, Verdeckung durch die Wolken, Ohr am Auge");
        // Verdeckung: unter der Untergrenze frei, tief in einer dichten Stelle verborgen
        double base = cs.type().base, bestX = 0, bestZ = 0, bestD = 0;
        for (int i = 0; i < 400; i++) {
            double x = (i % 20 - 10) * 600, z = (i / 20 - 10) * 600;
            double d = com.dan.cloud.Clouds.density(cs, x, base + 800, z, false);
            if (d > bestD) { bestD = d; bestX = x; bestZ = z; }
        }
        double below = L.occlusion.transmittance(bestX, base - 100, bestZ, eye[0], eye[1], eye[2]);
        double inside = L.occlusion.transmittance(bestX, base + 1500, bestZ, eye[0], eye[1], eye[2]);
        check(below > 0.999 && inside < 0.2, String.format(Locale.GERMAN, "Kanal unter der Wolke frei (%.3f), 1,5 km tief in einer dichten Stelle verborgen (%.3f)", below, inside));
        // Donner als SoundEvent
        java.util.List<Double> heard = new java.util.ArrayList<>();
        w.sound = (kind, x, y, z, strength) -> { if (kind == com.dan.lighthouse.wiring.SoundEvent.Kind.DONNER) heard.add(strength); };
        L.onThunder = Wiring.thunderTo(w.sound);
        L.setLevel(com.dan.lightning.Lightning.Level.OFF);
        w.thunderstorm = com.dan.lightning.Lightning.Level.OFF;
        L.strikeAttractor(0);
        for (int i = 0; i < 300; i++) L.step(0.01);
        check(heard.size() == 1 && heard.get(0) > 0.8, String.format(Locale.GERMAN, "Einschlag in den Turm: Donner als SoundEvent (Stärke %.2f)", heard.isEmpty() ? 0 : heard.get(0)));
        String frag = Shader.source("tower.frag", null).text, glass = Shader.source("glass.frag", null).text;
        check(frag.contains("ltgIrradiance") && frag.contains("ltgSpecular") && glass.contains("ltgIrradiance") && !frag.contains("#include"),
                "Turm- und Scheiben-Shader binden lightning.glsl ein (Licht, Glanz, Salz und Beschlag leuchten auf)");
        String sea = new String(com.dan.ocean.gl.OceanGl.class.getResourceAsStream("surface.frag").readAllBytes(), StandardCharsets.UTF_8);
        String cloud = new String(com.dan.cloud.gl.CloudGl.class.getResourceAsStream("composite.frag").readAllBytes(), StandardCharsets.UTF_8);
        check(sea.contains("uFlashPos") && cloud.contains("uFlashPos") && !sea.contains("lightning") && !cloud.contains("lightning"),
                "Meer und Wolken nehmen einfache Punktlichter, ohne das Gewitter zu kennen");
        check(Quality.NIEDRIG.lightning == com.dan.lightning.Lightning.Quality.LOW && Quality.ULTRA.lightning == com.dan.lightning.Lightning.Quality.ULTRA, "Qualitätsstufen reichen bis in die Blitze");
        java.lang.reflect.Field pf = com.dan.lighthouse.gl.Renderer.class.getDeclaredField("PASSES");
        pf.setAccessible(true);
        java.util.List<String> passes = java.util.Arrays.asList((String[]) pf.get(null));
        check(passes.indexOf("Blitz") == passes.indexOf("Feuer") + 1 && passes.indexOf("Blitz") < passes.indexOf("Scheiben"), "Zeitmessung: „Blitz“ nach dem Feuer, vor den Scheiben");
    }

    /** Phase 11: Wendeltreppe und Innenräume nach Innen2, die Fahrt „Aufstieg“. */
    static void innenraum() throws Exception {
        System.out.println("Treppenhaus und Innenräume (Phase 11)");
        double rise = Innenraum.RISE;
        check(Math.abs(Innenraum.tread(Innenraum.STEPS - 1) - Innenraum.ROOM_FLOOR) < 1e-9 && rise > 0.17 && rise < 0.21 && Innenraum.FLOOR_Y == Turm.SOCKEL_TOP + 0.05,
                String.format(Locale.GERMAN, "%d Stufen von der Tür (%.2f m) in den Dienstraum (%.2f m), Steigung %.1f cm, %d je Umlauf", Innenraum.STEPS,
                        Innenraum.FLOOR_Y, Innenraum.ROOM_FLOOR, rise * 100, Innenraum.PER_TURN));
        // Fenster frei: unter keinem Bullauge reicht eine Stufe (mit Wange) heran, über keinem hängt eine
        double worst = 9;
        for (Innenraum.Window w : Innenraum.WINDOWS) {
            if (w.room()) continue;
            double az = Innenraum.azOf(w.x(), w.z()), half = Math.toDegrees(0.24 / Innenraum.WALL_R);
            for (int i = 0; i < Innenraum.STEPS; i++)
                for (double da = -half; da <= half; da += half / 4)
                    if (Innenraum.onStep(az + da, i)) {
                        double t = Innenraum.tread(i);
                        worst = Math.min(worst, Math.max((w.y() - 0.24) - (t + 0.20), (t - Innenraum.THICK - 0.12) - (w.y() + 0.24)));
                    }
        }
        double door = 9;
        for (int i = 0; i < Innenraum.STEPS; i++)
            for (double da = -11; da <= 11; da += 1)
                if (Innenraum.onStep(Turm.DOOR_AZ + da, i)) door = Math.min(door, Innenraum.tread(i) - Innenraum.THICK - 0.12 - (Innenraum.FLOOR_Y + Innenraum.DOOR_H));
        check(worst > 0.1 && door > 0.1, String.format(Locale.GERMAN, "Bullaugen und Tür frei: mindestens %.0f cm Luft zu Stufen und Wangen, %.0f cm über der Tür",
                worst * 100, door * 100));
        // Kopfhöhe unter dem Dienstraum: wo der Boden trägt, bleiben über jeder Stufe 2 m
        double head = 9;
        for (int i = 0; i < Innenraum.STEPS; i++)
            for (double f = 0.05; f < 1; f += 0.1) {
                double az = Innenraum.stepAz(i) + Innenraum.STEP_DEG * f;
                for (double r = Innenraum.WELL_R + 0.05; r < Innenraum.WALL_R; r += 0.3)
                    if (Innenraum.solidRoomFloor(az, r)) head = Math.min(head, Innenraum.ROOM_FLOOR - Innenraum.SLAB - Innenraum.tread(i));
            }
        check(head >= 2.0, String.format(Locale.GERMAN, "Kopfhöhe unter dem Boden des Dienstraums mindestens %.2f m; er trägt von %.1f° bis %.1f°",
                head, Innenraum.SOLID_AZ0, Innenraum.SOLID_AZ1));
        // Lampen: 2 m über der Stufe darunter, nicht vor einem Bullauge
        boolean lampsOk = true;
        for (Innenraum.Lamp l : Innenraum.LAMPS) {
            if (l.room()) { lampsOk &= l.y() < Innenraum.CEIL_Y && Innenraum.solidRoomFloor(Innenraum.azOf(l.x(), l.z()), Math.hypot(l.x(), l.z())); continue; }
            double az = Innenraum.azOf(l.x(), l.z()), r = Math.hypot(l.x(), l.z());
            if (r > Innenraum.WELL_R) lampsOk &= l.y() - Innenraum.floorBelow(az, l.y()) > 1.9;
            for (Innenraum.Window w : Innenraum.WINDOWS) lampsOk &= Math.sqrt(Math.pow(l.x() - w.x(), 2) + Math.pow(l.y() - w.y(), 2) + Math.pow(l.z() - w.z(), 2)) > 0.45;
        }
        check(lampsOk && Innenraum.LAMPS.size() == 7, "7 Glühlampen: je 2 m über der Stufe, keine vor einem Fenster, die Deckenlampe über festem Boden");
        // Luke zwischen Linsensockel und Reling, Leiterfuß auf festem Boden
        check(Innenraum.HATCH_R0 > 0.42 + 0.15 && Innenraum.HATCH_R1 < Turm.LANTERN_R - 0.45 && Innenraum.solidRoomFloor(Innenraum.HATCH_AZ, Innenraum.LADDER_FOOT_R)
                        && Innenraum.solidRoomFloor(Innenraum.HATCH_AZ - Innenraum.HATCH_HALF, 1.0) && Innenraum.solidRoomFloor(Innenraum.HATCH_AZ + Innenraum.HATCH_HALF, 1.0),
                String.format(Locale.GERMAN, "Luke %.2f bis %.2f m von der Achse (Linsensockel 0,42 m, Reling %.2f m), Leiter auf festem Boden",
                        Innenraum.HATCH_R0, Innenraum.HATCH_R1, Turm.LANTERN_R - 0.45));
        // Verdeckung: unter einer Stufe sieht man eine Lampe darüber nicht, im Auge schon
        double az0 = Innenraum.stepAz(30) + 10, a0 = Math.toRadians(az0);
        double[] under = {Math.sin(a0) * 1.8, Innenraum.tread(30) - Innenraum.THICK - 0.5, -Math.cos(a0) * 1.8};
        double[] over = {Math.sin(a0) * 1.8, Innenraum.tread(30) + 1.0, -Math.cos(a0) * 1.8};
        double[] wellLo = {0.1, 10, 0.1}, wellHi = {0.1, 18, 0.1};
        check(Innenraum.blocked(under, over) && !Innenraum.blocked(wellLo, wellHi) && !Innenraum.blocked(under, new double[]{under[0] * 0.3, under[1] - 1, under[2] * 0.3}),
                "Verdeckung: die Stufe darüber verdeckt die Lampe, im Treppenauge ist der Blick frei");
        // Netz
        Turm in = Turm.interior();
        boolean norm = true, vis = true;
        for (int i = 0; i < in.vertexCount(); i++) {
            int o = i * Turm.STRIDE;
            double n = Math.sqrt(in.verts[o + 3] * in.verts[o + 3] + in.verts[o + 4] * in.verts[o + 4] + in.verts[o + 5] * in.verts[o + 5]);
            if (Math.abs(n - 1) > 1e-4) norm = false;
            for (int k = 0; k < Turm.VIS; k++) { float v = in.vis[i * Turm.VIS + k]; if (!(v >= 0 && v <= 1)) vis = false; }
        }
        double shadowed = 0;
        for (int i = 0; i < in.vertexCount(); i++) shadowed += in.vis[i * Turm.VIS] < 0.5 ? 1 : 0;
        check(in.wrongWinding() == 0 && norm && vis && in.interleaved().length == in.vertexCount() * (Turm.STRIDE + Turm.VIS),
                String.format(Locale.GERMAN, "Innenraum: %,d Dreiecke zum Raum hin gewunden, Sichtbarkeit je Ecke 0..1 (%.0f %% der Ecken sehen die unterste Lampe nicht)",
                        in.triangles(), 100 * shadowed / in.vertexCount()));
        boolean inside = true;
        for (int i = 0; i < in.vertexCount(); i++) {
            int o = i * Turm.STRIDE;
            double r = Math.hypot(in.verts[o], in.verts[o + 2]), y = in.verts[o + 1];
            if (y < Innenraum.FLOOR_Y - 1e-3 || y > Innenraum.LANTERN_FLOOR + 1e-3 || r > (y < Turm.SHAFT_TOP ? Turm.shaftR(y) : Turm.ROOM_R) + 1e-3) inside = false;
        }
        check(inside, "Alles Innere liegt in der Außenhaut (Laibungen enden an ihr)");
        // Lichtbilanz
        SceneClock c = new SceneClock(LocalDate.of(2026, 10, 16), 14.5);
        com.dan.sky.Sky sky = new com.dan.sky.Sky();
        Wiring w = new Wiring(c);
        w.feed(sky, 15);
        sky.cloudCover = 0;
        com.dan.sky.Sky.State day = sky.rebuildNow();
        double u = 1 / com.dan.sky.Sky.CD_PER_UNIT;
        double[][] wl = new double[Innenraum.WINDOWS.size()][];
        for (int i = 0; i < wl.length; i++) {
            Innenraum.Window win = Innenraum.WINDOWS.get(i);
            wl[i] = day.radiance(new double[]{win.nx() * 0.94, 0.34, win.nz() * 0.94});
        }
        double[][] lit = Innenraum.balance(wl, day.sunColor, day.sunDir, true, u), dark = Innenraum.balance(wl, day.sunColor, day.sunDir, false, u);
        double[][] night = Innenraum.balance(new double[wl.length][3], new double[3], new double[]{0, -1, 0}, true, u);
        double ls = lit[2][0] / u, lr = lit[3][0] / u, ln = night[2][0] / u;
        check(ls > 2 && ls < 200 && lr > ls && lit[2][0] > dark[2][0] && ln > 0.3 && ln < 20,
                String.format(Locale.GERMAN, "Lichtbilanz am Nachmittag: Schaft %.1f cd/m², Dienstraum %.1f cd/m² (Bogenfenster); nachts nur Lampen %.1f cd/m²", ls, lr, ln));
        double luxDay = com.dan.lighthouse.gl.Renderer.daylightLux(day, 1);
        com.dan.beacon.Beacon b = new com.dan.beacon.Beacon();
        check(luxDay > 5_000 && luxDay > b.offLux, String.format(Locale.GERMAN, "Dämmerungsschalter in lx: nachmittags %,.0f lx, das Feuer bleibt aus (bis Phase 10: Einheiten der Szene)", luxDay));
        check(Math.abs(Innenraum.needle(Innenraum.pressure(0)) - Innenraum.needle(1022)) < 1e-9 && Innenraum.needle(Innenraum.pressure(10)) < Innenraum.needle(Innenraum.pressure(2))
                && Math.abs(Innenraum.needle(1010)) < 10, String.format(Locale.GERMAN, "Barometer: %.0f hPa bei Bft 3, Zeiger %.0f°, bei Sturm tiefer", Innenraum.pressure(3), Innenraum.needle(Innenraum.pressure(3))));
        // Fahrt „Aufstieg“
        com.dan.lighthouse.camera.Aufstieg ride = new com.dan.lighthouse.camera.Aufstieg();
        double maxJump = 0, minClear = 9, minWall = 9;
        boolean hatchOk = true, cut = false;
        double[] prev = ride.sample(0);
        for (double t = 0.05; t <= ride.duration(); t += 0.05) {
            double[] k = ride.sample(t);
            double j = Math.sqrt(Math.pow(k[0] - prev[0], 2) + Math.pow(k[1] - prev[1], 2) + Math.pow(k[2] - prev[2], 2));
            if (j > 1) cut = true; else maxJump = Math.max(maxJump, j);
            prev = k;
            double r = Math.hypot(k[0], k[2]), az = Innenraum.azOf(k[0], k[2]);
            if (Innenraum.inside(k[0], k[1], k[2])) {
                if (r > Innenraum.WELL_R) minClear = Math.min(minClear, k[1] - Innenraum.floorBelow(az, k[1] - 1.0));
                minWall = Math.min(minWall, (k[1] < Innenraum.ROOM_FLOOR ? Innenraum.WALL_R : Innenraum.ROOM_WALL_R) - r);
            }
            if (k[1] > Innenraum.CEIL_Y - 0.05 && k[1] < Innenraum.LANTERN_FLOOR + 0.1 && r < 2)
                hatchOk &= r > Innenraum.HATCH_R0 + 0.05 && r < Innenraum.HATCH_R1 - 0.05 && Math.abs(Innenraum.norm(az - Innenraum.HATCH_AZ + 180) - 180) < Innenraum.HATCH_HALF - 3;
        }
        double[] end = ride.sample(ride.duration());
        check(cut && maxJump < 0.25 && minClear > 1.3 && minWall > 0.4 && hatchOk && Math.hypot(end[0], end[2]) < Turm.LANTERN_R - 0.45 && end[1] > Turm.GALLERY_TOP,
                String.format(Locale.GERMAN, "Aufstieg %.0f s: ein Schnitt an der Tür, sonst höchstens %.0f cm je 1/20 s, Auge %.2f m über der Stufe, %.2f m von der Wand, durch die Luke, Ende im Laternenraum",
                        ride.duration(), maxJump * 100, minClear, minWall));
        Camera cam = new Camera();
        cam.startRide(ride);
        for (int i = 0; i < 30; i++) cam.step(1 / 30.0);
        boolean riding = cam.mode == Camera.Mode.FAHRT && Math.abs(cam.rideTime() - 1) < 1e-6;
        cam.kFwd = true;
        cam.step(1 / 30.0);
        cam.kFwd = false;
        boolean stopped = cam.mode == Camera.Mode.FREI;
        cam.startRide(ride);
        for (int i = 0; i < (int) (ride.duration() * 10) + 20; i++) cam.step(0.1);
        check(riding && stopped && cam.mode == Camera.Mode.FREI && Math.abs(cam.eye[1] - end[1]) < 0.05,
                "Kamera: Fahrt läuft in Echtzeit, eine Taste beendet sie, am Ende steht sie frei im Laternenraum");
        // Verdrahtung
        com.dan.rain.Rain rain = new com.dan.rain.Rain();
        com.dan.cloud.Clouds cl = com.dan.cloud.Clouds.of(com.dan.cloud.CloudType.NIMBOSTRATUS, 8);
        cl.step(0.1);
        w.rainRate = 5;
        w.feed(rain, cl, cl.snapshot(), true, new double[]{0, 15, 1});
        rain.step(0.1);
        check(rain.snapshot().sheltered(0.5, 15, 1) && rain.snapshot().sheltered(1, 22, 0.3) && !rain.snapshot().sheltered(5, 15, 0),
                "Regen: im Schaft und im Dienstraum fällt keiner, draußen schon");
        long n11 = w.links().stream().filter(l -> l.phase() == 11).count();
        check(n11 >= 4, "Verbindungen der Phase 11 eingetragen: " + n11);
        String tv = Shader.source("tower.vert", null).text, tf = Shader.source("tower.frag", null).text, pf = Shader.source("post.frag", null).text;
        check(tv.contains("aVisA") && tf.contains("stairBlocked") && tf.contains("sunThroughWindows") && tf.contains("uInterior") && pf.contains("uWhite"),
                "Shader: Sichtbarkeit je Ecke, Verdeckung durch die Stufen und Sonnenflecken wie in Java, Weißabgleich drinnen");
    }

    /** Phase 12: die Prüfung der Wetterregie (com.dan.weather.demo.WeatherCheck) und ihre Verdrahtung in der App. */
    static void wetter() {
        System.out.println("Wetterregie (com.dan.weather, Phase 12)");
        for (String l : new com.dan.weather.demo.WeatherCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        System.out.println("Phase 12 in der App");
        SceneClock c = new SceneClock(LocalDate.of(2026, 10, 16), 6.0);
        Wiring w = new Wiring(c);
        com.dan.weather.Scenario sc = com.dan.weather.Scenario.stormDay();
        w.regie = true;
        w.weather.start(sc, c.epochMillis() / 1000.0);
        c.lapse = sc.lapse();
        com.dan.ocean.Ocean o = new com.dan.ocean.Ocean();
        com.dan.lightning.Lightning.Level maxStorm = com.dan.lightning.Lightning.Level.OFF;
        double maxRain = 0, minP = 2000, maxWind = 0;
        int changes = 0;
        String last = "";
        boolean typesOk = true;
        for (int i = 0; i < (int) Math.ceil(2 * sc.hours() * 3600 / sc.lapse()) + 2; i++) {
            c.step(0.5);
            w.feedWeather(0.5);
            w.feed(o);
            String now = o.windSpeed + "/" + o.windFromDeg + "/" + o.swellHeight + "/" + o.swellPeriod + "/" + o.swellFromDeg;
            if (!now.equals(last)) changes++;
            last = now;
            if (w.thunderstorm.ordinal() > maxStorm.ordinal()) maxStorm = w.thunderstorm;
            maxRain = Math.max(maxRain, w.rainRate);
            minP = Math.min(minP, w.pressureHPa);
            maxWind = Math.max(maxWind, w.wind.speed());
            typesOk &= w.cloudType.name().equals(w.weather.snapshot().cloud().name());
        }
        check(maxStorm == com.dan.lightning.Lightning.Level.STRONG && maxRain > 10 && minP < 992 && Beaufort.of(maxWind) >= 9 && typesOk,
                String.format(Locale.GERMAN, "Sturmtag im Zeitraffer 420 : 1 (4 min): alle Eingänge folgen; Bft %d, Regen bis %.0f mm/h, %.0f hPa, Gewitter „%s“, Wolkenart über den Namen",
                        Beaufort.of(maxWind), maxRain, minP, maxStorm));
        check(changes < 160, String.format(Locale.GERMAN, "Das Meer baut in den 4 Minuten nur %d-mal neu (Wind und Dünung in Stufen)", changes));
        w.hand(com.dan.weather.Weather.Var.WIND, 25);
        w.feedWeather(0.1);
        boolean held = Math.abs(w.wind.speed() - 25) < 1e-9 && w.weather.overrides() == 1;
        w.rainbowHand = 0.5;
        w.swellDirHand = 100;
        w.feedWeather(0.1);
        boolean hand = Math.abs(w.swellFromDeg - 100) < 1e-9;
        w.resumeRegie();
        w.feedWeather(0.1);
        check(held && hand && w.weather.overrides() == 0 && w.rainbowHand < 0 && Double.isNaN(w.swellDirHand),
                "Handregler überschreiben (Wind, Dünung, Regenbogen); „Regie weiter“ löst alle");
        Wiring off = new Wiring(new SceneClock(LocalDate.of(2026, 10, 16), 12));
        off.rainRate = 7;
        check(!off.regie && off.rainRate == 7, "Ohne Regie (Werkzeuge, Prüfungen) bleiben die Felder von Hand");
        long n12 = w.links().stream().filter(l -> l.phase() == 12).count();
        check(n12 >= 6, "Verbindungen der Phase 12 eingetragen: " + n12);
        check(com.dan.lighthouse.gl.Renderer.VERSION.matches("Phase 1[2-9]"), "Version: " + com.dan.lighthouse.gl.Renderer.VERSION);
    }

    /** Phase 13: Nebel, Schwimmkörper, Möwen (ihre drei Prüfungen) und ihre Verdrahtung in der App. */
    static void leben() throws Exception {
        System.out.println("Seenebel (com.dan.fog, Phase 13)");
        for (String l : new com.dan.fog.demo.FogCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        System.out.println("Tonnen und Schiffe (com.dan.buoyancy, Phase 13)");
        for (String l : new com.dan.buoyancy.demo.BuoyancyCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        System.out.println("Möwen (com.dan.flock, Phase 13)");
        for (String l : new com.dan.flock.demo.FlockCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        System.out.println("Phase 13 in der App");
        SceneClock c = new SceneClock(LocalDate.of(2026, 10, 16), 11);
        Wiring w = new Wiring(c);
        com.dan.fog.Fog fog = new com.dan.fog.Fog();
        w.wind.speed = 3;
        w.fogAmount = 0;
        w.feed(fog);
        double clear = fog.amount;
        w.fogAmount = 1;
        w.feed(fog);
        double full = fog.amount, vis = fog.visibilityM, top = fog.top, cov = fog.coverage;
        w.wind.speed = 13;
        w.feed(fog);
        double windy = fog.amount;
        check(clear == 0 && full == 1 && Math.abs(vis - 60) < 1e-6 && Math.abs(top - 88) < 1e-6 && cov == 1 && windy == 0,
                String.format(Locale.GERMAN, "Nebel aus der Regie: 0 → keiner; 1 → Sicht %.0f m, bis %.0f m, Bedeckung %.0f %%; bei 13 m/s Wind reißt er auf", vis, top, cov * 100));
        check(Math.abs(Wiring.fogVisibilityM(0.42) - 330) < 15 && Wiring.fogVisibilityM(0.2) > 700 && Wiring.fogVisibilityM(0) == 4000,
                String.format(Locale.GERMAN, "Sicht in der Bank: Nebel 0,2 → %.0f m, 0,42 (flach) → %.0f m", Wiring.fogVisibilityM(0.2), Wiring.fogVisibilityM(0.42)));
        // Nebelhorn: im dichten Nebel ruft es, im klaren nicht
        w.wind.speed = 3;
        w.fogAmount = 1;
        java.util.List<String> heard = new java.util.ArrayList<>();
        w.sound = (k, x, y, z, st) -> heard.add(k + String.format(Locale.ROOT, "@%.0f", y));
        w.feed(fog);
        w.signals(fog);
        w.signals(fog);
        for (int i = 0; i < 30 * 70; i++) fog.step(1 / 30.0);
        int horns = (int) heard.stream().filter(h -> h.startsWith("NEBELHORN")).count();
        w.fogAmount = 0;
        w.feed(fog);
        heard.clear();
        for (int i = 0; i < 30 * 70; i++) fog.step(1 / 30.0);
        check(fog.signals.size() == 1 && horns >= 5 && heard.isEmpty(),
                String.format(Locale.GERMAN, "Nebelhorn „%s“ am Turm (einmal eingetragen): in 70 s dichtem Nebel %d Töne als SoundEvent, im klaren keiner", Wiring.HORN, horns));
        w.fogAmount = 1;
        w.feed(fog);
        fog.step(0.01);
        double sc = Wiring.scatter(null, null, fog).density(0, 5, 0), sc0 = Wiring.scatter(null, null, null).density(0, 5, 0);
        check(sc > 0.01 && sc0 == 0, String.format(Locale.GERMAN, "Streugitter des Strahls nimmt den Nebel: %.3f /m in der Bank (vorher 0)", sc));
        // Schwimmkörper auf dem Meer der App
        com.dan.ocean.Ocean o = new com.dan.ocean.Ocean();
        w.wind.speed = Beaufort.mid(6);
        w.feed(o);
        o.rebuildNow();
        o.step(0.03);
        com.dan.buoyancy.Buoyancy b = new com.dan.buoyancy.Buoyancy();
        w.setup(b, o);
        double dv = Math.abs(b.sea.height(300, -200) - o.heightAt(300, -200)) + Math.abs(b.sea.velocity(300, -200)[1] - o.velocityAt(300, -200)[1]);
        double fx = Math.sin(Math.toRadians(Wiring.FAIRWAY_BEARING)) * Wiring.FAIRWAY_DIST;
        check(b.buoys.size() == 8 && dv < 1e-9 && Math.abs(b.fairwayX - fx) < 1e-9,
                String.format(Locale.GERMAN, "Fahrwasser %.1f km in %03.0f° mit %d Tonnen; das Wasser ist das Meer der App (Höhe und senkrechte Geschwindigkeit, ± %.0e)", Wiring.FAIRWAY_DIST / 1000, Wiring.FAIRWAY_BEARING, b.buoys.size(), dv));
        com.dan.buoyancy.Ship ship = b.spawn(true, 3000);
        double maxRoll = 0, maxDev = 0;
        w.feed(b, 20000, fog);
        for (int i = 0; i < 30 * 40; i++) {
            o.step(1 / 30.0);
            b.step(1 / 30.0);
            maxRoll = Math.max(maxRoll, Math.abs(ship.roll));
            if (i > 300) for (com.dan.buoyancy.Buoy by : b.buoys) maxDev = Math.max(maxDev, Math.abs(by.y - o.heightAt(by.x, by.z)));
        }
        check(maxDev < 1.5 && Math.toDegrees(maxRoll) > 0.5 && Math.toDegrees(maxRoll) < 25,
                String.format(Locale.GERMAN, "Bft 6 auf dem Meer der App: Tonnen folgen den Wellen (Wasserlinie höchstens %.2f m neben der Oberfläche), Kümo rollt bis %.1f°", maxDev, Math.toDegrees(maxRoll)));
        // Typhon wandert mit dem Schiff und verschwindet mit ihm
        w.feed(b, 20000, fog);
        boolean ty = fog.signals.size() == 2;
        b.ships.clear();
        w.feed(b, 20000, fog);
        check(ty && fog.signals.size() == 1, "Typhon „Tyfon 120s“ je Schiff als Nebelsignal, mit dem Schiff wieder fort");
        // Möwen am Turm
        com.dan.flock.Flock f = new com.dan.flock.Flock();
        w.setup(f, b);
        long hand = f.perches.stream().filter(pp -> pp.name.startsWith("Handlauf")).count();
        check(f.obstacles.size() == 2 && hand == 12 && f.perches.size() == 12 + 1 + 1 + 4 + b.buoys.size(),
                String.format(Locale.GERMAN, "Revier der Möwen: Turm und Pfahl als Hindernis, %d Sitzplätze (12 auf dem Handlauf, Knauf, Pfahl, Platte, Tonnen)", f.perches.size()));
        w.wind.speed = Beaufort.mid(5);
        w.fogAmount = 0;
        double minWall = 1e9;
        for (int i = 0; i < 30 * 180; i++) {
            w.feed(f, 20000, 1.0);
            f.step(1 / 30.0);
            for (com.dan.flock.Flock.Gull g : f.gulls()) {
                if (g.state == com.dan.flock.Flock.State.PERCH || g.state == com.dan.flock.Flock.State.AWAY) continue;
                if (g.pos[1] < Turm.SHAFT_TOP) minWall = Math.min(minWall, Math.hypot(g.pos[0], g.pos[2]) - Turm.SHAFT_R0);
            }
        }
        int perched = f.census()[com.dan.flock.Flock.State.PERCH.ordinal()];
        check(minWall > 0 && perched >= 1, String.format(Locale.GERMAN, "Bft 5, 3 min: keine Möwe im Schaft (kleinster Abstand %.1f m), %d sitzen", minWall, perched));
        check(Quality.NIEDRIG.fog == com.dan.fog.Fog.Quality.LOW && Quality.ULTRA.flock == com.dan.flock.Flock.Quality.ULTRA && Quality.HOCH.buoyancy == com.dan.buoyancy.Buoyancy.Quality.HIGH,
                "Qualitätsstufen reichen bis in Nebel, Schwimmkörper und Möwen");
        java.lang.reflect.Field pf = com.dan.lighthouse.gl.Renderer.class.getDeclaredField("PASSES");
        pf.setAccessible(true);
        java.util.List<String> passes = java.util.Arrays.asList((String[]) pf.get(null));
        check(passes.indexOf("Leben") == passes.indexOf("Turm") + 1 && passes.indexOf("Nebel") == passes.indexOf("Regen") + 1 && passes.indexOf("Feuer") == passes.indexOf("Nebel") + 1,
                "Zeitmessung: „Leben“ nach dem Turm, „Nebel“ nach dem Regen und vor dem Feuer");
        // Kamera folgt mit eigenem Oben (Querneigung) und lässt bei einer Taste los
        com.dan.lighthouse.camera.Camera cam = new com.dan.lighthouse.camera.Camera();
        double bank = Math.toRadians(20);
        cam.follow("Probe", () -> new double[][]{{10, 20, 30}, {0, 0, -1}, {Math.sin(bank), Math.cos(bank), 0}});
        float[] v = cam.view();
        double upx = v[1], upy = v[5];
        boolean rolled = Math.abs(Math.toDegrees(Math.atan2(upx, upy)) - 20) < 1e-3 && cam.eye[1] == 20;
        cam.kFwd = true;
        cam.step(0.1);
        cam.kFwd = false;
        check(rolled && cam.mode == com.dan.lighthouse.camera.Camera.Mode.FREI && cam.up[1] == 1,
                "Kamera folgt (Möwenflug, Brücke) mit Querneigung 20°; eine Taste gibt sie frei");
        String rs = Files.readString(new File("src/com/dan/lighthouse/gl/Renderer.java").toPath(), StandardCharsets.UTF_8);
        com.dan.weather.Scenario fd = com.dan.weather.Scenario.fogDay();
        boolean views = true;
        for (com.dan.weather.Scenario.Step st : fd.steps()) if (st.camera() != null && !st.camera().isEmpty()) views &= rs.contains("case \"" + st.camera() + "\"");
        check(views && rs.contains("case \"Möwenflug\"") && rs.contains("case \"Schiffsbrücke\""), "Kamerawünsche des Nebeltags (Möwenflug, Schiffsbrücke, …) kennt die App");
        String sv = new String(com.dan.spray.gl.SprayGl.class.getResourceAsStream("spray.vert").readAllBytes(), StandardCharsets.UTF_8);
        check(sv.contains("uLodPix") && new com.dan.spray.gl.SprayGl.View().lodPixels > 0 && rs.contains("sprayLodPixels"),
                "Befund Phase 12 (Gischt 9 ms bei Bft 10): große Nebel- und Weißwasserbälle werden im Bild ausgedünnt");
        com.dan.wetglass.Wetglass g1 = com.dan.wetglass.Wetglass.gale(), g2 = com.dan.wetglass.Wetglass.gale();
        for (com.dan.wetglass.Wetglass g : new com.dan.wetglass.Wetglass[]{g1, g2}) { g.seed = 5; g.reset(); for (int i = 0; i < 300; i++) g.step(1 / 30.0); }
        double[] pts = {0.1, 0.1, 0.15, 0.12, 0.2, 0.15, 0.25, 0.2};
        for (int i = 0; i < pts.length; i += 2) g1.wipe(pts[i], pts[i + 1], 0.03);
        g2.wipeStroke(pts, 0.03);
        check(g1.count() == g2.count() && g1.count() > 0, "Wischerstrich (wipeStroke) wischt wie einzelne Kreise: " + g2.count() + " Tropfen bleiben");
        long n13 = w.links().stream().filter(l -> l.phase() == 13).count();
        check(n13 >= 12, "Verbindungen der Phase 13 eingetragen: " + n13);
        check(com.dan.lighthouse.gl.Renderer.VERSION.matches("Phase 1[3-9]"), "Version: " + com.dan.lighthouse.gl.Renderer.VERSION);
    }

    /** Phase 14: der Klang (SoundscapeCheck) und seine Verdrahtung in der App. */
    static void klang() throws Exception {
        System.out.println("Klang (com.dan.soundscape, Phase 14)");
        for (String l : new com.dan.soundscape.demo.SoundscapeCheck().run()) if (l.startsWith("  ")) check(l.startsWith("  ok"), l.substring(8).trim());
        System.out.println("Phase 14 in der App");
        com.dan.soundscape.Soundscape sc = new com.dan.soundscape.Soundscape();
        sc.autoGain = false;
        SceneClock c = new SceneClock(LocalDate.of(2026, 10, 16), 12);
        Wiring w = new Wiring(c);
        w.wind.speed = 17;
        w.wind.fromDeg = 250;
        double[] ear = {-3.6, Turm.GALLERY_TOP + 1.6, 1.3};
        w.feed(sc, ear, 250, 4.0, 8.5, 3, com.dan.soundscape.Room.OUTDOORS, 0, 0, 0);
        com.dan.soundscape.Aeolian rail = sc.bodies.get(0);
        double ringDist = Math.hypot(rail.x() - ear[0], rail.z() - ear[2]);
        check(sc.bodies.size() == 3 && Math.abs(rail.diameter() - 0.07) < 1e-9 && ringDist < 0.6 && sc.surfRadius == Turm.SOCKEL_R0 && sc.windSpeed == 17
                        && sc.earX == ear[0] && sc.waveHeight == 4.0 && sc.rainRate == 3,
                String.format(Locale.GERMAN, "Wind, Seegang, Regen, Ohr an den Klang; Handlauf (70 mm), Stäbe, Mast singen, der nächste Punkt des Handlaufs liegt %.2f m neben dem Ohr; Brandung am Sockel", ringDist));
        double fRail = rail.frequency(sc.windAt(rail.y()));
        check(fRail > 40 && fRail < 120, String.format(Locale.GERMAN, "Handlauf bei Bft 8 singt mit %.0f Hz (0,2 · U / 0,07 m)", fRail));
        com.dan.lighthouse.wiring.SoundEvent ev = w.soundTo(sc);
        w.soundLive = false;
        ev.sound(com.dan.lighthouse.wiring.SoundEvent.Kind.BRECHER, -5, 2, 0, 1);
        sc.render(1);
        double quiet = sc.levels()[7];
        w.soundLive = true;
        ev.sound(com.dan.lighthouse.wiring.SoundEvent.Kind.BRECHER, -5, 2, 0, 1);
        sc.render(1.5);
        double loud = sc.levels()[7];
        check(quiet < 20 && loud > 80, String.format(Locale.GERMAN, "Ereignisse gehen nur in den laufenden Klang: still %.0f dB, Brecher am Sockel %.0f dB", quiet, loud));
        // Donner aus dem Kanal eines echten Blitzes
        com.dan.lightning.Lightning L = new com.dan.lightning.Lightning();
        L.seed = 5;
        L.listenerX = ear[0];
        L.listenerY = ear[1];
        L.listenerZ = ear[2];
        com.dan.lightning.Lightning.Thunder[] got = new com.dan.lightning.Lightning.Thunder[1];
        L.onThunder = t -> got[0] = t;
        L.strike(-1500, -1200);
        for (int i = 0; i < 30 * 20 && got[0] == null; i++) L.step(1 / 30.0);
        boolean th = false;
        String tht = "kein Donner";
        if (got[0] != null) {
            com.dan.soundscape.Soundscape ts = new com.dan.soundscape.Soundscape();
            ts.autoGain = false;
            ts.earX = ear[0]; ts.earY = ear[1]; ts.earZ = ear[2];
            ts.windSpeed = 0; ts.waveHeight = 0;
            w.thunder(ts, got[0], ear);
            float[] o = ts.render(got[0].duration() + 4);
            double on = com.dan.soundscape.demo.Analysis.onset(o, ts.sampleRate(), 0, com.dan.soundscape.Dsp.amp(got[0].levelDb() - 25));
            double dur = 0;
            for (double t = on; t < o.length / 2.0 / ts.sampleRate() - 0.3; t += 0.1)
                if (com.dan.soundscape.demo.Analysis.rms(o, ts.sampleRate(), t, 0.3) > com.dan.soundscape.Dsp.amp(got[0].levelDb() - 30)) dur = t + 0.3 - on;
            th = on < 0.1 && dur > 0.4 * got[0].duration() && dur < 2.5 * got[0].duration() + 2;
            tht = String.format(Locale.GERMAN, "Blitz in %.1f km mit %d Stücken: der Donner rollt %.1f s (der Blitz meldet %.1f s), %.0f dB", got[0].distance() / 1000, got[0].bolt().n, dur, got[0].duration(), got[0].levelDb());
        }
        check(th, "Donner aus dem Kanal: " + tht);
        // Diesel der Schiffe
        com.dan.ocean.Ocean o = new com.dan.ocean.Ocean();
        com.dan.buoyancy.Buoyancy b = new com.dan.buoyancy.Buoyancy();
        w.setup(b, o);
        com.dan.buoyancy.Ship ship = b.spawn(true, 3000);
        w.feedHums(sc, b, ship);
        boolean hum = sc.hums.size() == 1 && sc.hums.get(0).aboard && Math.abs(sc.hums.get(0).firingHz - 37.5) < 1e-9;
        b.ships.clear();
        w.feedHums(sc, b, null);
        check(hum && sc.hums.isEmpty(), "Diesel je Schiff (37,5 Hz), an Bord im Steuerhaus; mit dem Schiff wieder fort");
        String rs = Files.readString(new File("src/com/dan/lighthouse/gl/Renderer.java").toPath(), StandardCharsets.UTF_8);
        check(rs.contains("Room.STONE_TOWER") && rs.contains("Room.GLASS_LANTERN") && rs.contains("Room.WHEELHOUSE") && rs.contains("glassDrops") && rs.contains("roofDrops"),
                "Ohr drinnen: Turm (Steinturm), Laternenraum (Glashaus, Tropfen auf Scheiben und Kuppel), Brücke (Steuerhaus)");
        check(Quality.NIEDRIG.sound == com.dan.soundscape.Soundscape.Quality.LOW && Quality.ULTRA.sound == com.dan.soundscape.Soundscape.Quality.ULTRA,
                "Qualitätsstufen reichen bis in den Klang");
        long n14 = w.links().stream().filter(l -> l.phase() == 14).count();
        check(n14 >= 8, "Verbindungen der Phase 14 eingetragen: " + n14);
        check(com.dan.lighthouse.gl.Renderer.VERSION.matches("Phase 1[4-9]"), "Version: " + com.dan.lighthouse.gl.Renderer.VERSION);
    }

    static void isolation() throws Exception {
        System.out.println("Isolation der Packages (Kurzform von check-packages: nur Importe)");
        for (File pkg : elementPackages()) {
            String name = pkg.getName();
            List<String> bad = new ArrayList<>();
            int files = 0;
            try (Stream<Path> s = Files.walk(pkg.toPath())) {
                for (Path p : (Iterable<Path>) s.filter(x -> x.toString().endsWith(".java"))::iterator) {
                    files++;
                    String rel = pkg.toPath().relativize(p).toString().replace('\\', '/');
                    boolean gl = rel.startsWith("gl/"), demo = rel.startsWith("demo/");
                    for (String b : foreignImports(p.toFile(), "com.dan." + name, gl || demo, demo)) bad.add(rel + ": " + b);
                }
            }
            check(bad.isEmpty(), "com.dan." + name + " (" + files + " Dateien) hält den Paketvertrag ein (Importe)" + (bad.isEmpty() ? "" : " – " + bad));
        }
    }

    static void datenbank() throws Exception {
        System.out.println("Datenbank-Zugang (ohne Verbindung)");
        check(new File("db/db.properties.example").isFile(), "Vorlage db/db.properties.example vorhanden");
        File tmp = File.createTempFile("lighthouse", ".properties");
        tmp.deleteOnExit();
        Files.writeString(tmp.toPath(), "# Kommentar bleibt\nurl=jdbc:oracle:thin:@//localhost:1521/PDBORCL\nuser=Lighthouse\npassword=\n", StandardCharsets.UTF_8);
        Db.Konfig k = Db.konfig(tmp);
        check(k.user.equals("Lighthouse") && k.kurz().equals("Lighthouse@localhost:1521/PDBORCL"), "Konfig gelesen: " + k.kurz());
        Db.passwortSpeichern(k, "geheim".toCharArray());
        String after = Files.readString(tmp.toPath(), StandardCharsets.UTF_8);
        check(after.contains("# Kommentar bleibt") && after.contains("password=geheim") && Db.konfig(tmp).hatPasswort(),
                "Passwort gespeichert, Kommentare bleiben");
        File gi = new File(".gitignore");
        check(gi.isFile() && Files.readString(gi.toPath()).contains("db/db.properties"), ".gitignore schützt db/db.properties");
    }

    static void sonstiges() {
        System.out.println("Sonstiges");
        boolean rising = true;
        Quality[] q = Quality.values();
        for (int i = 1; i < q.length; i++) if (q[i].renderScale < q[i - 1].renderScale || q[i].ocean.ordinal() <= q[i - 1].ocean.ordinal()) rising = false;
        check(rising, "Qualitätsstufen steigen von Niedrig bis Ultra");
        java.awt.image.BufferedImage ic = AppIcon.paint(64);
        int edge = ic.getRGB(32, 1), mid = ic.getRGB(4, 32);
        check(((edge >> 16) & 255) > 150 && ((edge >> 8) & 255) > 100, "Programmsymbol mit Goldkante");
        check((mid >>> 24) > 0, "Symbol deckend gemalt");
        check(new File("tools/PaketPruefung.java").isFile(), "Isolationsprüfung tools/PaketPruefung.java vorhanden (Ant-Target check-packages)");
        check(new File("src/com/dan/muster/README.md").isFile(), "Muster-Package mit README als Vorlage für neue Packages");
    }

    private Pruefung() { }
}
