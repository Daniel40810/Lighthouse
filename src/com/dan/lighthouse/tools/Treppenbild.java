package com.dan.lighthouse.tools;

import com.dan.cloud.CloudType;
import com.dan.lighthouse.camera.Aufstieg;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.Innenraum;
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
import java.util.Locale;

/**
 * Vergleichsbild für Phase 11 ohne Fenster, mit den Shadern der App: oben die Vorlage Innen2 neben demselben Blick
 * ins Treppenauge am Nachmittag und abends mit brennenden Lampen; darunter die Fahrt „Aufstieg“ als Bildfolge in
 * sechs Bildern (Podest, Blick hinauf, zwei Bilder von der Treppe, Leiter und Luke, Laternenraum).
 * <pre>
 * Treppenbild datei.png [breite] [MITTEL|HOCH|…]
 * </pre>
 */
public final class Treppenbild {
    private Treppenbild() { }

    static final LocalDate DAY = LocalDate.of(2026, 10, 16);

    public static void main(String[] a) throws Exception {
        if (a.length > 0 && a[0].equals("--blick")) { single(a); return; }
        if (a.length > 0 && a[0].equals("--folge")) { sequence(a); return; }
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase11_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1800;
        Quality q = a.length > 2 ? Quality.valueOf(a[2]) : Quality.HOCH;
        int tw = width / 3, th = tw * 2 / 3;
        BufferedImage img = new BufferedImage(tw * 3, th * 3, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(40, 46, 52));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, tw, th);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock(DAY, 14.5);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.setQuality(q);
        wiring.tide.on = false;
        wiring.tide.fixed = 0.3;
        wiring.wind.fromDeg = 250;
        wiring.wind.speed = Beaufort.mid(4);
        wiring.cloudType = CloudType.CUMULUS;
        wiring.oktas = 2;
        wiring.rainRate = 0;
        wiring.visibilityKm = 30;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }

        Turmbild.drawTemplate(g, new File("templates/Leuchturm_Innen2.jpg"), 0, 0, tw, th, "Vorlage Innen2");
        double[] v = Innenraum.INNEN2;
        StringBuilder log = new StringBuilder();
        shootAt(d, r, clock, g, tw, 0, tw, th, 14.5, v, log);
        Turmbild.label(g, "Innen2 nachgestellt: Blick ins Treppenauge, 14:30", tw, 0);
        shootAt(d, r, clock, g, tw * 2, 0, tw, th, 19.5, v, log);
        Turmbild.label(g, "derselbe Blick um 19:30, die Lampen brennen", tw * 2, 0);
        Aufstieg ride = new Aufstieg();
        double[] times = {3, 8.5, 24, 44, 60.6, ride.duration() - 4};
        String[] names = {"Aufstieg 1: vor der Tür", "2: Blick hinauf ins Auge", "3: auf der Treppe, Barometer und Bullauge",
                "4: die letzte Windung", "5: auf der Leiter durch die Luke", "6: im Laternenraum an der Linse"};
        for (int i = 0; i < times.length; i++) {
            double[] k = ride.sample(times[i]);
            int x = (i % 3) * tw, y = (1 + i / 3) * th;
            shootAt(d, r, clock, g, x, y, tw, th, 14.5, new double[]{k[0], k[1], k[2], k[3], k[4], 1}, log);
            Turmbild.label(g, String.format(Locale.GERMAN, "%s (%.0f s)", names[i], times[i]), x, y);
        }
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.print(log);
        System.out.println(r.interiorLine());
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }

    /** Die ganze Fahrt als Bildfolge: --folge datei.png [breite] [bilder]; 4 Spalten, alle duration/(n−1) s ein Bild. */
    static void sequence(String[] a) throws Exception {
        int width = a.length > 2 ? Integer.parseInt(a[2]) : 1600, n = a.length > 3 ? Integer.parseInt(a[3]) : 16;
        int cols = 4, rows = (n + cols - 1) / cols, tw = width / cols, th = tw * 2 / 3;
        Renderer[] rr = new Renderer[1];
        GLOffscreenAutoDrawable d = setup(tw, th, rr);
        Renderer r = rr[0];
        BufferedImage img = new BufferedImage(tw * cols, th * rows, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        Aufstieg ride = new Aufstieg();
        StringBuilder log = new StringBuilder();
        for (int i = 0; i < n; i++) {
            double t = Math.min(ride.duration() - 0.3, 0.5 + (ride.duration() - 0.5) * i / (n - 1));
            double[] k = ride.sample(t);
            int x = (i % cols) * tw, y = (i / cols) * th;
            shootAt(d, r, r.clock, g, x, y, tw, th, 14.5, new double[]{k[0], k[1], k[2], k[3], k[4], 1}, log);
            Turmbild.label(g, String.format(Locale.GERMAN, "%.1f s", t), x, y);
        }
        g.dispose();
        ImageIO.write(img, "png", new File(a[1]));
        System.out.print(log);
        d.destroy();
        System.exit(0);
    }

    static GLOffscreenAutoDrawable setup(int w, int h, Renderer[] out) {
        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, w, h);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock(DAY, 14.5);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.setQuality(Quality.HOCH);
        wiring.tide.on = false;
        wiring.tide.fixed = 0.3;
        wiring.wind.fromDeg = 250;
        wiring.wind.speed = Beaufort.mid(4);
        wiring.cloudType = CloudType.CUMULUS;
        wiring.oktas = 2;
        wiring.visibilityKm = 30;
        d.addGLEventListener(r);
        d.display();
        out[0] = r;
        return d;
    }

    /** Ein einzelnes Bild zum Nachsehen: --blick datei.png breite höhe stunde x y z blick neigung vergrößerung */
    static void single(String[] a) throws Exception {
        int w = Integer.parseInt(a[2]), h = Integer.parseInt(a[3]);
        double hour = Double.parseDouble(a[4]);
        double[] c = new double[6];
        for (int i = 0; i < 6; i++) c[i] = Double.parseDouble(a[5 + i]);
        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, w, h);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock(DAY, hour);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.setQuality(Quality.HOCH);
        wiring.tide.on = false;
        wiring.tide.fixed = 0.3;
        wiring.oktas = 2;
        r.stairLight = System.getenv("LICHT_AUS") == null;
        d.addGLEventListener(r);
        d.display();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        StringBuilder log = new StringBuilder();
        shootAt(d, r, clock, g, 0, 0, w, h, hour, c, log);
        g.dispose();
        ImageIO.write(img, "png", new File(a[1]));
        System.out.print(log);
        System.out.println(r.interiorLine());
        d.destroy();
        System.exit(0);
    }

    /** Ein Bild: Uhrzeit, Kamera {x, y, z, Blick, Neigung, Vergrößerung}. */
    static void shootAt(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, Graphics2D g, int x, int y, int w, int h, double hour, double[] c, StringBuilder log) {
        d.setSurfaceSize(w, h);
        r.awaitInterior();
        clock.set(DAY, hour);
        r.zoom = c[5];
        r.camera.place(c[0], c[1], c[2], c[3], c[4]);
        for (int i = 0; i < 6; i++) { r.step(1 / 30.0); d.display(); }
        r.sky.updateNow();
        r.camera.place(c[0], c[1], c[2], c[3], c[4]);
        r.beacon.snapOn(hour >= 18.5 || hour < 7.5);
        r.readapt();
        for (int i = 0; i < 4; i++) { r.step(1 / 30.0); r.camera.place(c[0], c[1], c[2], c[3], c[4]); d.display(); }
        final BufferedImage[] img = new BufferedImage[1];
        d.invoke(true, x2 -> { img[0] = r.grab(x2); return true; });
        g.drawImage(img[0], x, y, null);
        log.append(String.format(Locale.GERMAN, "%5.1f h  Auge %.2f %.2f %.2f  Belichtung %.3g  im Turm %s%n", hour, c[0], c[1], c[2], r.exposure(), r.insideTower()));
    }
}
