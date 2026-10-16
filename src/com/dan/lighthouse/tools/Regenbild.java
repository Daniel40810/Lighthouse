package com.dan.lighthouse.tools;

import com.dan.cloud.CloudType;
import com.dan.cloud.Clouds;
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
import java.util.Locale;

/**
 * Vergleichsbild für Phase 7 ohne Fenster: oben ein Schauer aus echten Gewitterwolken, der über den Roten Sand zieht
 * (Blick gegen den Wind, vier Augenblicke), darunter derselbe Turm im Landregen von Niesel bis Wolkenbruch, unten Ringe und
 * Krönchen an der Manschette und der Turm im Wolkenbruch aus 260 m.
 * <pre>
 * Regenbild datei.png [breite] [MITTEL|HOCH|…]
 * </pre>
 */
public final class Regenbild {
    private Regenbild() { }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase7_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1800;
        Quality q = a.length > 2 ? Quality.valueOf(a[2]) : Quality.HOCH;
        int qw = width / 4, qh = qw * 3 / 4, fw = width / 5, fh = fw * 4 / 5, hw = width / 2, hh = hw * 9 / 20;
        BufferedImage img = new BufferedImage(width, qh + fh + hh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(40, 46, 52));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, qw, qh);
        d.setAutoSwapBufferMode(false);
        LocalDate day = LocalDate.of(2026, 10, 16);
        SceneClock clock = new SceneClock(day, 14);
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

        // 1 Schauer: Gewitterwolken 7/8, Bft 6. Gesucht wird der dichteste Regenvorhang, der in sechs Stunden über den Turm zieht.
        clock.set(day, 14.0);
        wiring.tide.fixed = 0.2;
        wiring.wind.speed = Beaufort.mid(6);
        wiring.cloudType = CloudType.CUMULONIMBUS;
        wiring.oktas = 7;
        wiring.visibilityKm = 30;
        wiring.rainRate = 18;
        wiring.feed(r.clouds);
        r.clouds.precipitation = Math.min(1, 0.35 + wiring.rainRate / 25);
        double[] times = showerTimes(r.clouds);
        String[] what = {"Schauer zieht heran", "erreicht den Turm", "über dem Turm", "zieht ab (Blick mit dem Wind)"};
        d.setSurfaceSize(qw, qh);
        for (int i = 0; i < 4; i++) {
            r.clouds.setTime(times[i]);
            // zuletzt mit dem Wind schauen: der Vorhang zieht davon
            double here = shoot(d, r, g, i * qw, 0, i == 3 ? 60 : 240, 2, 150, 14, 2.5);
            Turmbild.label(g, String.format(Locale.GERMAN, "%s · hier %.1f mm/h", what[i], here), i * qw, 0);
        }
        // 2 Landregen von Niesel bis Wolkenbruch
        double[] rates = {0.3, 2, 5, 15, 60};
        String[] names = {"Niesel", "Leicht", "Mäßig", "Stark", "Wolkenbruch"};
        wiring.cloudType = CloudType.NIMBOSTRATUS;
        wiring.oktas = 8;
        wiring.wind.speed = Beaufort.mid(5);
        d.setSurfaceSize(fw, fh);
        for (int i = 0; i < rates.length; i++) {
            wiring.rainRate = rates[i];
            r.clouds.setTime(600);
            shoot(d, r, g, i * fw, qh, 230, 3, 95, 12, 2.5);
            Turmbild.label(g, String.format(Locale.GERMAN, "%s · %s mm/h", names[i], rates[i] < 1 ? "0,3" : String.format(Locale.GERMAN, "%.0f", rates[i])), i * fw, qh);
        }
        // 3 Nahaufnahmen: Ringe und Krönchen an der Manschette bei Ebbe; die nasse Wetterseite
        d.setSurfaceSize(hw, hh);
        wiring.rainRate = 10;
        wiring.tide.fixed = -1.0;
        wiring.wind.speed = Beaufort.mid(4);
        shoot(d, r, g, 0, qh + fh, 200, 14, 22, 0, 2.5);
        Turmbild.label(g, "Nah: Ringe und Krönchen an der Manschette, 10 mm/h, Ebbe", 0, qh + fh);
        wiring.tide.fixed = 0.5;
        wiring.wind.speed = Beaufort.mid(6);
        wiring.rainRate = 60;
        shoot(d, r, g, hw, qh + fh, 230, 1, 260, 10, 2.5);
        Turmbild.label(g, "Wolkenbruch 60 mm/h aus 260 m: Sicht 1,2 km, die Alte Weser ist verschwunden", hw, qh + fh);
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println(r.rainCheck().isEmpty() ? "Regen: Abgleich nicht gelaufen" : r.rainCheck());
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }

    /**
     * Sucht in den Wolken einen Schauer, der über den Turm zieht: die Zeit, zu der der Vorhang am Turm von trocken auf
     * voll springt. Gibt vier Zeiten zurück: vorher (Vorhang gut 3 km entfernt), am Rand, mitten darin, danach.
     */
    static double[] showerTimes(Clouds c) {
        double p = Math.max(1e-3, c.precipitation), best = -1, tBest = 0;
        for (double t = 0; t < 6 * 3600; t += 10) {
            c.setTime(t);
            double v = Wiring.curtain(c.snapshot(), 0, 0, p);
            if (v > best) { best = v; tBest = t; }
        }
        double edge = tBest, after = tBest;
        for (double u = tBest; u > tBest - 1800; u -= 5) {
            c.setTime(u);
            if (Wiring.curtain(c.snapshot(), 0, 0, p) < 0.25 * best) { edge = u; break; }
        }
        for (double u = tBest; u < tBest + 1800; u += 5) {
            c.setTime(u);
            if (Wiring.curtain(c.snapshot(), 0, 0, p) < 0.05) { after = u; break; }
        }
        double speed = c.windSpeed;
        System.out.printf(Locale.GERMAN, "Schauer: am Turm bis %.0f %% bei t = %.0f s, Rand %.0f s vorher, trocken %.0f s danach%n", best * 100, tBest, tBest - edge, after - tBest);
        return new double[]{edge - 2500 / speed, edge + 15, tBest, after + 1200 / speed};
    }

    /** Blick auf den Turm, Vorlauf (Regen, Wolken, Nässe), Bild in das Gesamtbild. Gibt den Regen am Turm zurück. */
    static double shoot(GLOffscreenAutoDrawable d, Renderer r, Graphics2D g, int x, int y, double yaw, double pitch, double dist, double ty, double warm) {
        double cloudT = cloudTime(r.clouds);
        r.ocean.rebuildNow();
        r.resettleWeathering();
        r.spray.clear();
        r.rain.reset();
        r.camera.lookAt(0, ty, 0, yaw, pitch, dist);
        r.camera.snap();
        for (int i = 0; i < (int) (warm * 30); i++) {
            r.step(1 / 30.0);
            r.clouds.setTime(cloudT);
        }
        r.camera.snap();
        for (int i = 0; i < 20; i++) d.display();
        final BufferedImage[] img = new BufferedImage[1];
        d.invoke(true, x2 -> { img[0] = r.grab(x2); return true; });
        g.drawImage(img[0], x, y, null);
        return r.rain.rateAt(0, 0);
    }

    /** Die Laufzeit der Wolken (für das Festhalten während des Vorlaufs). */
    static double cloudTime(Clouds c) { return c.snapshot().time(); }
}
