package com.dan.lighthouse.tools;

import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.wiring.Wiring;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;

import java.io.File;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Tempo-Probe und Standbild ohne Fenster (nach dem Tempo-Werkzeug aus Kīlauea). Rechnet die Szene unsichtbar in
 * fester Größe, misst die Wanduhr je Bild bis glFinish und die Zeit jedes Durchgangs auf der Grafikkarte und schreibt
 * auf Wunsch ein PNG. So lässt sich jede Phase vorher und nachher vergleichen und im Leistungsbudget (Werkbuch,
 * Abschnitt 10) eintragen.
 * <pre>
 * Tempo [bilder] [breite] [höhe] [png|-] [NIEDRIG|MITTEL|HOCH|ULTRA] [uhrzeit] [datum JJJJ-MM-TT] [beaufort]
 * Tempo 120 1920 1080 bilder/tempo.png HOCH 20.0 2026-10-15 6
 * Umgebung: OHNE_HUD, VORLAUF (s), ABSTAND (m), GISCHT_LOD (Bildpunkte), FEUER_EINHEIT (1 = Feuer wie bis Phase 12)
 * </pre>
 */
public final class Tempo {
    private Tempo() { }

    public static void main(String[] a) throws Exception {
        int frames = a.length > 0 ? Integer.parseInt(a[0]) : 120;
        int w = a.length > 1 ? Integer.parseInt(a[1]) : 1920, h = a.length > 2 ? Integer.parseInt(a[2]) : 1080;
        File png = a.length > 3 && !a[3].equals("-") ? new File(a[3]) : null;
        Quality q = a.length > 4 ? Quality.valueOf(a[4]) : Quality.HOCH;
        double hour = a.length > 5 ? Double.parseDouble(a[5].replace(',', '.')) : 20.0;
        LocalDate day = a.length > 6 && !a[6].equals("-") ? LocalDate.parse(a[6]) : LocalDate.now(SceneClock.ZONE);
        int bft = a.length > 7 ? Integer.parseInt(a[7]) : 4;

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, w, h);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock(day, hour);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        wiring.wind.speed = com.dan.lighthouse.wiring.Beaufort.mid(bft);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.setQuality(q);
        wiring.feed(r.ocean);
        r.ocean.rebuildNow();
        // Umgebungsvariablen für Vergleiche: GISCHT_LOD (Bildpunkte, 0 = aus), ABSTAND (m vom Turm)
        if (System.getenv("OHNE_HUD") != null) r.showHud = false;
        // FEUER_EINHEIT=1: das Feuer wie bis Phase 12 (ohne Umrechnung in Einheiten der Szene), zum Vergleich
        if (System.getenv("FEUER_EINHEIT") != null) r.beaconUnit = Double.parseDouble(System.getenv("FEUER_EINHEIT"));
        if (System.getenv("GISCHT_LOD") != null) r.sprayLodPixels = Double.parseDouble(System.getenv("GISCHT_LOD"));
        d.addGLEventListener(r);
        d.display();
        if (System.getenv("VORLAUF") != null) for (int i = 0, n = (int) (30 * Double.parseDouble(System.getenv("VORLAUF"))); i < n; i++) r.step(1 / 30.0);
        if (System.getenv("ABSTAND") != null) { r.camera.dist = Double.parseDouble(System.getenv("ABSTAND")); r.camera.snap(); }
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }
        for (int i = 0; i < 10; i++) { r.step(1 / 60.0); d.display(); }
        double best = Double.MAX_VALUE, sum = 0;
        for (int i = 0; i < frames; i++) {
            long t0 = System.nanoTime();
            r.step(1 / 60.0);
            d.display();
            d.getContext().makeCurrent();
            d.getGL().glFinish();
            d.getContext().release();
            double ms = (System.nanoTime() - t0) / 1e6;
            best = Math.min(best, ms);
            sum += ms;
        }
        System.out.println(r.glInfo());
        System.out.println(r.compute() == null ? "Compute-Test: nicht gelaufen" : r.compute().line());
        System.out.printf(Locale.GERMAN, "%d×%d %s, %s: bestes Bild %.2f ms, Mittel %.2f ms (%.0f Bilder/s)%n", w, h, q.label, clock.label(), best, sum / frames, 1000 / (sum / frames));
        System.out.println("Durchgänge (ms): " + r.passTimes());
        System.out.println("Meer: " + r.ocean.describe() + " · " + r.oceanInfo());
        System.out.println(r.oceanCheck().isEmpty() ? "Abgleich Grafikkarte/Prozessor: nicht gelaufen" : r.oceanCheck());
        if (png != null) {
            File dir = png.getAbsoluteFile().getParentFile();
            if (dir != null) dir.mkdirs();
            final java.awt.image.BufferedImage[] img = new java.awt.image.BufferedImage[1];
            d.invoke(true, x -> { img[0] = r.grab(x); return true; });
            javax.imageio.ImageIO.write(img[0], "png", png);
            System.out.println("Bild: " + png.getPath());
        }
        d.destroy();
        System.exit(0);
    }
}
