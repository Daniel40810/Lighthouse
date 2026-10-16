package com.dan.lighthouse.tools;

import com.dan.cloud.CloudType;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.wiring.Beaufort;
import com.dan.lighthouse.wiring.Wiring;
import com.dan.spray.Parcel;
import com.dan.spray.Spray;
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
import java.util.Locale;

/**
 * Vergleichsbild für Phase 6 ohne Fenster: oben die Vorlage Aussen4 neben dem Sturm am Roten Sand, darunter derselbe
 * Blick bei Bft 5, 7, 9 und 11 und zwei weitere Ansichten (nasser Sockel nah, Gischtfahnen von oben). Jede Ansicht
 * wartet auf einen Schwall, der gerade steigt und zur Kamera zeigt, damit das Bild zeigt, wie hoch es spritzt.
 * <pre>
 * Gischtbild datei.png [breite] [MITTEL|HOCH|…]
 * </pre>
 */
public final class Gischtbild {
    private Gischtbild() { }

    /** Ansicht: Name, Uhrzeit, Wasserstand, Bft, Wolkenart, Achtel, Sicht km, Kamera, Mindesthöhe des Schwalls. */
    record View(String label, double hour, double level, int bft, CloudType type, double oktas, double vis, double ty, double yaw, double pitch,
                double dist, double minApex) { }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase6_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1800;
        Quality q = a.length > 2 ? Quality.valueOf(a[2]) : Quality.HOCH;
        int half = width / 4, th = half * 4 / 3, th2 = half * 3 / 4;
        // Blick gegen den Wind (Wind aus 240°): die Kamera steht auf der Wetterseite, der Schwall steigt zu ihr hin
        View storm = new View("Sturm Bft 10 · Gewitterwolken · 16:00", 16.0, 0.6, 10, CloudType.CUMULONIMBUS, 7, 15, 15, 60, 2, 110, 18);
        View[] row2 = {
                new View("Bft 5", 14.0, 0.3, 5, CloudType.CUMULUS, 3, 40, 10, 60, 1, 85, 2),
                new View("Bft 7", 14.0, 0.3, 7, CloudType.STRATOCUMULUS, 6, 30, 10, 60, 1, 85, 6),
                new View("Bft 9", 14.0, 0.3, 9, CloudType.NIMBOSTRATUS, 8, 20, 10, 60, 1, 85, 15),
                new View("Bft 11", 14.0, 0.3, 11, CloudType.CUMULONIMBUS, 7, 12, 14, 60, 3, 110, 24)};
        View[] row3 = {
                new View("Nah: der Sockel nach dem Schwall, Bft 8", 13.0, -0.4, 8, CloudType.STRATOCUMULUS, 6, 25, 4, 70, 4, 26, 5),
                new View("Von oben: Gischtfahnen bei Bft 11", 13.0, 0.3, 11, CloudType.NIMBOSTRATUS, 8, 15, 0, 60, 45, 160, 12)};
        BufferedImage img = new BufferedImage(half * 4, th + th2 * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(40, 46, 52));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, half, th);
        d.setAutoSwapBufferMode(false);
        LocalDate day = LocalDate.of(2026, 10, 15);
        SceneClock clock = new SceneClock(day, 15);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        wiring.wind.fromDeg = 240;
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.setQuality(q);
        wiring.tide.on = false;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }

        Turmbild.drawTemplate(g, new File("templates/Leuchturm_Aussen4.jpg"), 0, 0, half, th, "Vorlage Aussen4");
        d.setSurfaceSize(half * 3, th);
        g.drawImage(render(d, r, clock, wiring, storm, day), half, 0, null);
        Turmbild.label(g, storm.label() + " · " + r.spray.describe(), half, 0);
        d.setSurfaceSize(half, th2);
        for (int i = 0; i < row2.length; i++) {
            g.drawImage(render(d, r, clock, wiring, row2[i], day), half * i, th, null);
            Turmbild.label(g, row2[i].label() + String.format(Locale.GERMAN, " · Schwall %.0f m", r.spray.lastApex()), half * i, th);
        }
        d.setSurfaceSize(half * 2, th2);
        for (int i = 0; i < row3.length; i++) {
            g.drawImage(render(d, r, clock, wiring, row3[i], day), half * 2 * i, th + th2, null);
            Turmbild.label(g, row3[i].label(), half * 2 * i, th + th2);
        }
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println(r.sprayCheck().isEmpty() ? "Gischt: Abgleich nicht gelaufen" : r.sprayCheck());
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
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
        // Vorlauf: Gischt, Nässe und Wolken stellen sich ein
        for (int i = 0; i < 30 * 20; i++) r.step(1 / 30.0);
        // warten, bis ein großer Schwall auf der Seite der Kamera gerade steigt
        double[] eye = r.camera.eye;
        for (int i = 0; i < 30 * 90; i++) {
            boolean found = false;
            for (Parcel p : r.spray.parcels()) {
                double age = r.spray.time() - p.t0();
                if (p.kind() != Parcel.SPLASH || age < 0.9 || age > 1.4) continue;
                double tx = eye[0] - p.x(), tz = eye[2] - p.z(), l = Math.hypot(tx, tz);
                if ((p.nx() * tx + p.nz() * tz) / l > 0.4 && p.y() + Spray.apex(p.vy(), p.k()) > v.minApex()) { found = true; break; }
            }
            if (found) break;
            r.step(1 / 30.0);
        }
        r.camera.snap();
        for (int i = 0; i < 24; i++) d.display();
        final BufferedImage[] img = new BufferedImage[1];
        d.invoke(true, x -> { img[0] = r.grab(x); return true; });
        return img[0];
    }
}
