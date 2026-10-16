package com.dan.lighthouse.tools;

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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;

/**
 * Vergleichsbild für Phase 5 ohne Fenster: oben die Vorlagen Aussen1 und Aussen2 jeweils neben der Szene aus
 * demselben Blickwinkel, darunter vier Lagen (Niedrigwasser, Hochwasser, Abend, Nacht) und zwei Nahaufnahmen.
 * <pre>
 * Turmbild datei.png [breite] [MITTEL|HOCH|…] [bilder je Ansicht, Vorgabe 70]
 * </pre>
 * Die Vorlagen werden aus {@code templates/} gelesen; fehlen sie, bleibt das Feld grau.
 */
public final class Turmbild {
    private Turmbild() { }

    static int frames = 70;

    /** Eine Ansicht: Name, Uhrzeit, Wasserstand, Bft, Achtel Cumulus, Kamera (Drehpunkt y, Blick, Neigung, Abstand). */
    record View(String label, double hour, double level, int bft, double oktas, double ty, double yaw, double pitch, double dist) { }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase5_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1800;
        Quality q = a.length > 2 ? Quality.valueOf(a[2]) : Quality.HOCH;
        frames = a.length > 3 ? Integer.parseInt(a[3]) : 70;
        int half = width / 4, th = half * 3 / 4;
        View aus1 = new View("Szene wie Aussen1 · 15:00, Wasser +0,4 m, Bft 5", 15.0, 0.4, 5, 2, 13.5, 62, 1.5, 58);
        View aus2 = new View("Drohne wie Aussen2 · 15:30, Wasser −0,6 m, Bft 5", 15.5, -0.6, 5, 0, 9, 325, 55, 48);
        View[] row2 = {
                new View("Niedrigwasser −1,5 m · Manschette frei", 12.5, -1.5, 3, 2, 2, 20, 12, 34),
                new View("Hochwasser +1,5 m · Platte überspült", 12.5, 1.5, 6, 3, 4, 20, 10, 40),
                new View("Abend 18:15 · Gegenlicht", 18.25, 0.2, 4, 3, 12, 250, 2, 110),
                new View("Nacht 20:30 · Laterne an", 20.5, -0.3, 4, 2, 14, 250, 2, 90)};
        View[] row3 = {
                new View("Nah: Tür, Leiter, Nieten", 13.0, -1.2, 3, 1, 6, 20, 6, 16),
                new View("Nah: Erker, Galerie, Laterne", 13.0, 0, 3, 1, 22, 20, -8, 18)};
        int rowH = th;
        BufferedImage img = new BufferedImage(half * 4, rowH * 3, BufferedImage.TYPE_INT_RGB);
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
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.setQuality(q);
        wiring.tide.on = false;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }

        drawTemplate(g, new File("templates/Leuchturm_Aussen1.jpg"), 0, 0, half, th, "Vorlage Aussen1");
        g.drawImage(render(d, r, clock, wiring, aus1, day), half, 0, null);
        label(g, aus1.label(), half, 0);
        drawTemplate(g, new File("templates/Leuchturm_Aussen2.jpg"), half * 2, 0, half, th, "Vorlage Aussen2");
        g.drawImage(render(d, r, clock, wiring, aus2, day), half * 3, 0, null);
        label(g, aus2.label(), half * 3, 0);
        for (int i = 0; i < row2.length; i++) {
            g.drawImage(render(d, r, clock, wiring, row2[i], day), half * i, rowH, null);
            label(g, row2[i].label(), half * i, rowH);
        }
        d.setSurfaceSize(half * 2, th);
        for (int i = 0; i < row3.length; i++) {
            g.drawImage(render(d, r, clock, wiring, row3[i], day), half * 2 * i, rowH * 2, null);
            label(g, row3[i].label(), half * 2 * i, rowH * 2);
        }
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println(r.weatheringCheck().isEmpty() ? "Verwitterung: Abgleich nicht gelaufen" : r.weatheringCheck());
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }

    static BufferedImage render(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, Wiring wiring, View v, LocalDate day) {
        clock.set(day, v.hour());
        wiring.tide.fixed = v.level();
        wiring.wind.speed = Beaufort.mid(v.bft());
        wiring.oktas = v.oktas();
        r.ocean.rebuildNow();
        r.resettleWeathering();
        r.camera.lookAt(0, v.ty(), 0, v.yaw(), v.pitch(), v.dist());
        r.camera.snap();
        for (int i = 0; i < frames; i++) { r.step(1 / 60.0); r.camera.snap(); d.display(); }
        final BufferedImage[] img = new BufferedImage[1];
        d.invoke(true, x -> { img[0] = r.grab(x); return true; });
        return img[0];
    }

    public static void drawTemplate(Graphics2D g, File f, int x, int y, int w, int h, String label) {
        try {
            BufferedImage t = ImageIO.read(f);
            double s = Math.max(w / (double) t.getWidth(), h / (double) t.getHeight());
            int sw = (int) Math.round(t.getWidth() * s), sh = (int) Math.round(t.getHeight() * s);
            g.setClip(x, y, w, h);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(t, x + (w - sw) / 2, y + (h - sh) / 2, sw, sh, null);
            g.setClip(null);
        } catch (Exception e) {
            // Vorlage fehlt: grau lassen
        }
        label(g, label, x, y);
    }

    public static void label(Graphics2D g, String t, int x, int y) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(new Color(0, 0, 0, 130));
        g.fillRect(x + 6, y + 6, g.getFontMetrics().stringWidth(t) + 12, 22);
        g.setColor(Color.WHITE);
        g.drawString(t, x + 12, y + 22);
    }
}
