package com.dan.lighthouse.tools;

import com.dan.cloud.CloudType;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.wiring.Beaufort;
import com.dan.lighthouse.wiring.Wiring;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;

/**
 * Phase 17: das Bild vorher/nachher der Brandung am Sockel, ohne Fenster. Vier feste Ansichten (gleicher Tag, gleiche
 * Uhrzeit, gleicher Wind aus 240°, gleiche Kamera auf der Wetterseite), damit ein Lauf mit dem Brandungsring aus
 * {@code ocean} (wie bis Phase 16, Schalter {@code ring}) und einer mit der Brandung aus {@code surf} nebeneinander
 * passen: nah am Sockel bei Bft 6, Sturm Bft 9, Niedrigwasser mit freier Steinschüttung bei Bft 6 und von oben bei
 * Bft 7. Jede Ansicht läuft 25 s Szenenzeit vor und wartet dann (höchstens 30 s), bis vor dem Turm eine Welle gerade
 * bricht oder als Walze läuft.
 * <pre>
 * Brandungsbild datei.png [breite] [MITTEL|HOCH|…] [Beschriftung] [ring]
 * </pre>
 */
public final class Brandungsbild {
    private Brandungsbild() { }

    /** Ansicht: Name, Uhrzeit, Wasserstand, Bft, Wolkenart, Achtel, Sicht km, Kamera (Zielhöhe, Gier, Neigung, Abstand). */
    record View(String label, double hour, double level, int bft, CloudType type, double oktas, double vis, double ty, double yaw, double pitch, double dist) { }

    static final View[] VIEWS = {
            new View("Nah am Sockel, Wetterseite, Bft 6", 13.0, 0.0, 6, CloudType.STRATOCUMULUS, 5, 30, 1, 60, 8, 40),
            new View("Sturm Bft 9", 15.0, 0.6, 9, CloudType.NIMBOSTRATUS, 8, 15, 3, 60, 5, 60),
            new View("Niedrigwasser, Steinschüttung frei, Bft 6", 12.0, -1.3, 6, CloudType.CUMULUS, 4, 35, 0, 60, 12, 30),
            new View("Von oben auf die Wetterseite, Bft 7", 14.0, 0.3, 7, CloudType.STRATOCUMULUS, 6, 25, 0, 60, 50, 32)};

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Brandung_Sockel_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1600;
        Quality q = a.length > 2 ? Quality.valueOf(a[2]) : Quality.HOCH;
        String titel = a.length > 3 ? a[3] : "";
        int w = width / 2, h = w * 9 / 16, top = titel.isEmpty() ? 0 : 34;
        BufferedImage img = new BufferedImage(w * 2, h * 2 + top, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(40, 46, 52));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        if (!titel.isEmpty()) {
            g.setColor(new Color(235, 225, 200));
            g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 20f));
            g.drawString(titel, 12, 24);
        }

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, w, h);
        d.setAutoSwapBufferMode(false);
        LocalDate day = LocalDate.of(2026, 10, 15);
        SceneClock clock = new SceneClock(day, 13);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        wiring.wind.fromDeg = 240;
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.setQuality(q);
        r.brandung.an = !(a.length > 4 && a[4].equals("ring"));
        wiring.tide.on = false;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }
        for (int i = 0; i < VIEWS.length; i++) {
            View v = VIEWS[i];
            int x = (i % 2) * w, y = top + (i / 2) * h;
            g.drawImage(render(d, r, clock, wiring, v, day), x, y, null);
            Turmbild.label(g, v.label() + " · " + r.surfDescribe(), x, y);
            System.out.println(v.label() + " · " + r.surfDescribe());
        }
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }

    /** Bricht in der Mitte des Stücks gerade eine Welle (Fortschritt 0,3 … 1,3)? Vor Phase 17: nie. */
    static boolean bricht(Renderer r) {
        com.dan.lighthouse.scene.Sockelbrandung.Stueck s = r.brandung.stueck();
        if (s == null) return true;     // mit dem Ring: nicht warten
        int mid = s.zug.rows / 2;
        for (com.dan.surf.WaveTrain.Wave w : s.zug.waves) {
            double p = s.zug.progress(w, mid);
            if (p >= 0.6 && p <= 1.6) return true;
        }
        return false;
    }

    static BufferedImage render(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, Wiring wiring, View v, LocalDate day) {
        clock.set(day, v.hour());
        wiring.tide.fixed = v.level();
        wiring.wind.speed = Beaufort.mid(v.bft());
        wiring.cloudType = v.type();
        wiring.oktas = v.oktas();
        wiring.visibilityKm = v.vis();
        r.ocean.rebuildNow();
        r.resettleWeathering();
        r.spray.clear();
        r.camera.lookAt(0, v.ty(), 0, v.yaw(), v.pitch(), v.dist());
        r.camera.snap();
        r.step(1 / 30.0);
        r.brandungJetzt();
        for (int i = 0; i < 30 * 25; i++) r.step(1 / 30.0);
        // seit Phase 17: warten (höchstens 30 s), bis auf der Wetterseite eine Welle gerade bricht
        for (int i = 0; i < 30 * 30 && !bricht(r); i++) r.step(1 / 30.0);
        r.camera.snap();
        for (int i = 0; i < 24; i++) d.display();
        final BufferedImage[] img = new BufferedImage[1];
        d.invoke(true, x -> { img[0] = r.grab(x); return true; });
        return img[0];
    }
}
