package com.dan.lighthouse.tools;

import com.dan.cloud.CloudType;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.ui.AppIcon;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Statusbild für Phase 16 ohne Fenster: dieselbe Ansicht (Bft 7, Nachmittag, Aussen1) in den vier Qualitätsstufen mit
 * der Zeit der Grafikkarte, darunter die Regelung der Auflösung (100 %, 75 %, 50 % derselben Stufe) und das
 * Protokoll des Dauerlaufs, dazu das Programmsymbol.
 * <pre>
 * Schliffbild [datei.png] [breite]
 * </pre>
 */
public final class Schliffbild {
    private Schliffbild() { }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase16_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1800;
        int tw = width / 4, th = tw * 9 / 16;
        File dl = new File("docs/status/Dauerlauf_claude.txt");
        List<String> l = dl.isFile() ? Files.readAllLines(dl.toPath(), StandardCharsets.UTF_8) : List.of("kein Protokoll");
        int textH = 40 + 15 * l.size();
        BufferedImage img = new BufferedImage(tw * 4, th * 2 + textH + 48, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(new Color(24, 28, 34));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, tw, th);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock(LocalDate.of(2026, 10, 16), 15.5);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.autoScale = false;
        wiring.tide.on = false;
        wiring.tide.fixed = 0.4;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }
        r.awaitInterior();
        StringBuilder log = new StringBuilder();
        Lebensbild.weather(r, wiring, 7, 250, CloudType.CUMULUS, 5, 0, 0);
        r.setQuality(Quality.MITTEL);
        Lebensbild.settle(r, clock, 15.5, 40);
        r.applyView("Aussen1");
        r.camera.snap();
        // Reihe 1: die vier Stufen bei voller Auflösung
        Quality[] qs = Quality.values();
        double[] ms = new double[4];
        for (int i = 0; i < 4; i++) {
            r.setQuality(qs[i]);
            r.renderScale = 1;
            r.ocean.rebuildNow();
            for (int k = 0; k < 90; k++) { r.step(1 / 30.0); if (k % 10 == 0) d.display(); }
            for (int k = 0; k < 30; k++) d.display();
            ms[i] = r.gpuMs();
            Lebensbild.shoot(d, r, g, tw * i, 0, log, String.format(Locale.GERMAN, "%s · %.0f ms · %s", qs[i].label, ms[i], kurz(qs[i])));
        }
        // Reihe 2: Hoch bei 100, 75 und 50 % Auflösung, und das Symbol mit Text
        r.setQuality(Quality.HOCH);
        double[] sc = {1.0, 0.75, 0.5};
        for (int i = 0; i < 3; i++) {
            r.renderScale = sc[i];
            for (int k = 0; k < 30; k++) d.display();
            Lebensbild.shoot(d, r, g, tw * i, th, log, String.format(Locale.GERMAN, "Hoch, Auflösung %.0f %% (%d×%d) · %.0f ms", sc[i] * 100,
                    (int) Math.round(tw * sc[i]), (int) Math.round(th * sc[i]), r.gpuMs()));
            // die Zeit der Grafikkarte ist ein gleitendes Mittel: vor der nächsten Stufe ausschwingen lassen
            for (int k = 0; k < 20; k++) d.display();
        }
        // Symbol und Erklärung
        int x0 = tw * 3, y0 = th;
        g.setColor(new Color(16, 22, 32));
        g.fillRect(x0, y0, tw, th);
        BufferedImage ic = AppIcon.load(256);
        int is = Math.min(th - 60, 200);
        g.drawImage(ic, x0 + 24, y0 + (th - is) / 2, is, is, null);
        g.setColor(new Color(235, 228, 210));
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Phase 16 · Schliff", x0 + is + 40, y0 + 46);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String[] zeilen = {"Symbol mit Goldkante:", "icon/lighthouse.ico, PNGs im Jar", "Verknuepfung.bat → Desktop", "Auflösung regelt sich selbst", "(50–100 %, Ziel 60 Bilder/s)",
                "Vier Stufen in zwölf Packages", "README je Package, GitHub-", "README, docs/QUELLEN.md", "Dauerlauf: siehe unten"};
        for (int i = 0; i < zeilen.length; i++) g.drawString(zeilen[i], x0 + is + 40, y0 + 72 + i * 18);
        // Dauerlauf
        int y = th * 2 + 36;
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Dauerlauf (tools.Dauerlauf, docs/status/Dauerlauf_claude.txt)", 16, y);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        int yy = y + 24;
        int maxChars = (tw * 4 - 32) / 7;
        for (String z : l) {
            if (yy > img.getHeight() - 6) break;
            String s = z.replace(" gleichbleibend", "").replaceAll(" · \\d+ Werte$", "");
            if (s.length() > maxChars) s = s.substring(0, maxChars - 2) + " …";
            g.setColor(s.startsWith("FEHLER") ? new Color(240, 150, 90) : s.startsWith("Dauerlauf bestanden") ? new Color(150, 220, 160) : new Color(200, 205, 212));
            g.drawString(s, 16, yy);
            yy += 15;
        }
        g.dispose();
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("Statusbild: " + out);
        System.out.println(log);
        System.exit(0);
    }

    static String kurz(Quality q) {
        return String.format(Locale.GERMAN, "Meer %d×%d², Wolken %s, Klang %.1f kHz", q.ocean.cascades, q.ocean.n,
                q.clouds == com.dan.cloud.Clouds.Quality.FLAT ? "2.5D" : "1/" + q.clouds.divisor, q.sound.sampleRate / 1000.0);
    }
}
