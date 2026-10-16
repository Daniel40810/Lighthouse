package com.dan.lighthouse.tools;

import com.dan.cloud.CloudType;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.scene.Turm;
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
 * Vergleichsbild für Phase 8 ohne Fenster: oben die Vorlage Innen1 neben dem Laternenraum im Regen, darunter die
 * Nahaufnahme der Scheibe auf der Wetterseite (die Welt kopfüber in jedem Tropfen), die Seitenscheibe, über die der
 * Wind die Läufer schräg zieht, und der Abend mit beschlagenen Scheiben; unten ein Brecher über die Galerie, das Salz,
 * das er zurücklässt, und derselbe Rechner auf dem runden Handlauf der Galerie (Zylinder, F10).
 * <pre>
 * Scheibenbild datei.png [breite] [MITTEL|HOCH|…]
 * </pre>
 */
public final class Scheibenbild {
    private Scheibenbild() { }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase8_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1800;
        Quality q = a.length > 2 ? Quality.valueOf(a[2]) : Quality.HOCH;
        int tw = width / 3, th = tw * 3 / 4, bw = width / 3, bh = bw * 3 / 4;
        BufferedImage img = new BufferedImage(width, th + bh * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(40, 46, 52));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, tw * 2, th);
        d.setAutoSwapBufferMode(false);
        LocalDate day = LocalDate.of(2026, 10, 16);
        SceneClock clock = new SceneClock(day, 14);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.setQuality(q);
        wiring.tide.on = false;
        wiring.tide.fixed = 0.3;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }
        if (System.getenv("NOGLASS") != null) r.showGlass = false;

        // Wind aus West: der Blick zur Wetterseite ist frei (Mast im Osten, Erker unter der Galerie)
        double from = 270;
        wiring.wind.fromDeg = from;
        wiring.wind.speed = Beaufort.mid(6);
        wiring.cloudType = CloudType.NIMBOSTRATUS;
        wiring.oktas = 8;
        wiring.visibilityKm = 20;
        wiring.rainRate = 4;
        double eyeY = Turm.GALLERY_TOP + 1.55;

        Turmbild.drawTemplate(g, new File("templates/Leuchturm_Innen1.jpg"), 0, 0, tw, th, "Vorlage Innen1");
        // 1 Laternenraum im Regen: hinter der Linse, Blick zur Wetterseite (wie Innen1)
        double side = Math.toRadians(from + 180 + 12);
        shoot(d, r, clock, day, 14.2, g, tw, 0, tw * 2, th, Math.sin(side) * 1.4, eyeY + 0.15, -Math.cos(side) * 1.4, from + 5, 9, 40);
        Turmbild.label(g, "Laternenraum im Landregen, 4 mm/h, Bft 6 · " + r.glassLine().replace("Scheiben ", "").replaceAll(" · Beschlag.*", ""), tw, 0);
        // 2 Nahaufnahme der Scheibe auf der Wetterseite
        double fr = Math.toRadians(from);
        r.zoom = 3;
        shoot(d, r, clock, day, 14.2, g, 0, th, bw, bh, Math.sin(fr) * 1.53, eyeY - 0.25, -Math.cos(fr) * 1.53, from + 6, 10, 0);
        r.zoom = 1;
        Turmbild.label(g, "Nah an der Wetterscheibe: Geländer und See kopfüber in jedem Tropfen", 0, th);
        // 3 Seitenscheibe: der Wind zieht die Läufer schräg
        double sr = Math.toRadians(from + 90);
        wiring.wind.speed = Beaufort.mid(8);
        wiring.rainRate = 8;
        shoot(d, r, clock, day, 14.2, g, bw, th, bw, bh, Math.sin(sr) * 1.35, eyeY - 0.3, -Math.cos(sr) * 1.35, from + 90 + 10, 0, 40);
        Turmbild.label(g, "Seitenscheibe bei Bft 8: der Wind zieht die Läufer schräg", bw, th);
        // 4 Abend: Lampe an, drinnen wärmer und feuchter als das kalte Glas, die Scheiben beschlagen
        wiring.wind.speed = Beaufort.mid(5);
        wiring.rainRate = 3;
        shoot(d, r, clock, day, 18.6, g, bw * 2, th, bw, bh, Math.sin(side) * 1.2, eyeY, -Math.cos(side) * 1.2, from - 20, 6, 240);
        Turmbild.label(g, f("Abend: Lampe an, die Scheiben beschlagen innen (%.0f %%)", r.glass[0].fogMean() * 100), bw * 2, th);
        // 5 Brecher über die Galerie: Seewasser läuft als Haut über die Wetterscheibe
        wiring.fogOn = false;
        r.wipeFog();
        wiring.wind.speed = Beaufort.mid(11);
        wiring.rainRate = 6;
        wiring.cloudType = CloudType.CUMULONIMBUS;
        wiring.oktas = 7;
        r.zoom = 2;
        shoot(d, r, clock, day, 14.4, g, 0, th + bh, bw, bh, Math.sin(fr) * 1.4, eyeY - 0.2, -Math.cos(fr) * 1.4, from + 4, 6, 20, 1.6, 0.9);
        Turmbild.label(g, f("Bft 11, ein Brecher über die Galerie: Seewasser läuft ab (%.2f mm)", r.glass[0].sheetMm()), 0, th + bh);
        // 6 danach: das Seewasser trocknet, Salz bleibt
        wiring.rainRate = 0;
        wiring.wind.speed = Beaufort.mid(7);
        wiring.cloudType = CloudType.STRATOCUMULUS;
        wiring.oktas = 5;
        shoot(d, r, clock, day, 15.2, g, bw, th + bh, bw, bh, Math.sin(fr) * 1.4, eyeY - 0.2, -Math.cos(fr) * 1.4, from + 4, 6, 600);
        r.zoom = 1;
        Turmbild.label(g, f("eine Stunde nach dem Sturm, trocken: Salz %.1f g/m² auf der Wetterscheibe", r.glass[0].saltMean() * com.dan.wetglass.Wetglass.SALT_FULL), bw, th + bh);
        // 7 derselbe Rechner auf einem Zylinder: der Handlauf der Galerie im Regen
        r.wipeRail();
        wiring.wind.speed = Beaufort.mid(6);
        wiring.rainRate = 8;
        wiring.cloudType = CloudType.NIMBOSTRATUS;
        wiring.oktas = 8;
        shootRail(d, r, clock, day, 14.2, g, bw * 2, th + bh, bw, bh, from, 90);
        Turmbild.label(g, f("Handlauf der Galerie, 8 mm/h: oben liegen, unten hängen (%,d abgefallen)", r.rail.totals()[4]), bw * 2, th + bh);
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println(r.glassCheck().isEmpty() ? "Scheiben: Abgleich nicht gelaufen" : r.glassCheck());
        System.out.println(r.glassLine());
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }

    static void shoot(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, LocalDate day, double hour, Graphics2D g, int x, int y, int w, int h,
                      double ex, double ey, double ez, double yaw, double pitch, double warm) {
        shoot(d, r, clock, day, hour, g, x, y, w, h, ex, ey, ez, yaw, pitch, warm, 0, 0);
    }

    /** Auf die Galerie an den Handlauf (wie der Knopf im Bedienfeld), Regen und Handlauf vorlaufen lassen. */
    static void shootRail(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, LocalDate day, double hour, Graphics2D g, int x, int y, int w, int h,
                          double from, double warm) {
        d.setSurfaceSize(w, h);
        clock.set(day, hour);
        r.ocean.rebuildNow();
        r.resettleWeathering();
        r.toRail(from);
        for (int i = 0; i < 30; i++) r.step(1 / 30.0);
        r.toRail(from);
        for (int i = 0; i < (int) warm; i++) r.rail.step(1.0);
        for (int i = 0; i < 12; i++) d.display();
        final BufferedImage[] img = new BufferedImage[1];
        d.invoke(true, x2 -> { img[0] = r.grab(x2); return true; });
        g.drawImage(img[0], x, y, null);
        r.zoom = 1;
    }

    /**
     * Auge frei in den Raum stellen, Regen und Scheiben vorlaufen lassen, Bild ins Gesamtbild. Mit floodMm > 0 kommt
     * nach dem Vorlauf ein Brecher über die Galerie, das Bild folgt floodAfter s später.
     */
    static void shoot(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, LocalDate day, double hour, Graphics2D g, int x, int y, int w, int h,
                      double ex, double ey, double ez, double yaw, double pitch, double warm, double floodMm, double floodAfter) {
        shoot(d, r, clock, day, hour, g, x, y, w, h, ex, ey, ez, yaw, pitch, warm, floodMm, floodAfter, false);
    }

    static void shoot(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, LocalDate day, double hour, Graphics2D g, int x, int y, int w, int h,
                      double ex, double ey, double ez, double yaw, double pitch, double warm, double floodMm, double floodAfter, boolean wipeBand) {
        d.setSurfaceSize(w, h);
        clock.set(day, hour);
        r.ocean.rebuildNow();
        r.resettleWeathering();
        r.camera.place(ex, ey, ez, yaw, pitch);
        // Scheiben vorlaufen lassen (die Wolken stehen)
        for (int i = 0; i < 30; i++) r.step(1 / 30.0);
        r.camera.place(ex, ey, ez, yaw, pitch);
        for (int i = 0; i < (int) warm; i++) for (com.dan.wetglass.Wetglass gl : r.glass) gl.step(1.0);
        if (wipeBand) {
            // mit der Hand ein Band in den Beschlag gewischt, 20 s später
            com.dan.wetglass.Wetglass gw = r.glass[0];
            for (double xx = 0; xx < gw.width; xx += 0.008) gw.wipeInside(xx, 0.2 + 0.02 * Math.sin(xx * 40), 0.035);
            for (int i = 0; i < 20; i++) for (com.dan.wetglass.Wetglass gl : r.glass) gl.step(1.0);
        }
        if (floodMm > 0) {
            r.floodByHand(floodMm);
            for (double t = 0; t < floodAfter; t += 0.1) for (com.dan.wetglass.Wetglass gl : r.glass) gl.step(0.1);
        }
        for (int i = 0; i < 12; i++) d.display();
        final BufferedImage[] img = new BufferedImage[1];
        d.invoke(true, x2 -> { img[0] = r.grab(x2); return true; });
        g.drawImage(img[0], x, y, null);
    }

    static String f(String fmt, Object... a) { return String.format(Locale.GERMAN, fmt, a); }
}
