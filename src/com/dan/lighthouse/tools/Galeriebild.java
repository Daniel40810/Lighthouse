package com.dan.lighthouse.tools;

import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.person.Figur;
import com.dan.lighthouse.person.Personen;
import com.dan.lighthouse.scene.SceneClock;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Statusbild der Avatar-Phase 15 ohne Fenster, mit den Shadern der App: das Regiebuch „Sturmnacht auf der Galerie“ läuft
 * mit fester Bildzeit (1/30 s) durch; nach jedem Wechsel der Kamera wird ein Bild genommen. Dazu misst es Bilder/s und
 * GPU-Zeit derselben Ansicht mit und ohne Person (Budget von Lighthouse) und schreibt die Zahlen darunter.
 * <pre>
 * Galeriebild datei.png [breite] [NIEDRIG|MITTEL|HOCH|ULTRA]
 * Galeriebild --blick datei.png [breite] [Einstellung] [winkel]     (ein Bild: Wärter an der Reling im Luv)
 * </pre>
 */
public final class Galeriebild {
    private Galeriebild() { }

    static final LocalDate DAY = LocalDate.of(2026, 10, 22);

    public static void main(String[] a) throws Exception {
        if (a.length > 0 && a[0].equals("--blick")) { blick(a); return; }
        File out = new File(a.length > 0 ? a[0] : "docs/status/Avatar_Galerie_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1920;
        Quality q = a.length > 2 ? Quality.valueOf(a[2]) : Quality.MITTEL;
        int tw = width / 3, th = tw * 9 / 16;
        Renderer[] rr = new Renderer[1];
        GLOffscreenAutoDrawable d = setup(tw, th, q, rr);
        Renderer r = rr[0];
        Personen ps = r.personen;
        long t0 = System.nanoTime();
        ps.warten();
        double loadS = (System.nanoTime() - t0) / 1e9;

        com.dan.avatar.Regie rg = com.dan.avatar.Regie.resource(Personen.class, "galerie_sturmnacht.txt");
        List<String> shots = new ArrayList<>();
        List<Double> due = new ArrayList<>();
        double[] clockT = {0};
        // im Kino wie in der App (Vorspann, Balken, Untertitel, Schnitte, Abspann)
        r.kinoPersonen(rg);
        r.frozen = true;   // die Zeit gibt dieses Werkzeug vor (sonst rechnet display() mit der Wanduhr noch einmal)
        java.util.function.Consumer<com.dan.avatar.Regie.Line> prev = rg.onLine;
        rg.onLine = l -> {
            prev.accept(l);
            if ((l.verb().equals("kamera") || l.verb().equals("ansicht")) && due.size() < 12) { due.add(clockT[0] + 2.5); shots.add(l.args()); }
        };
        List<BufferedImage> imgs = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        double dt = 1 / 30.0;
        int next = 0, frames = 0;
        StringBuilder log = new StringBuilder();
        boolean abspann = false;
        while (frames < 30 * 160 && !(rg.finished() && next >= due.size() && !r.kino.laeuft())) {
            abspann |= r.kino.laeuft() && r.kino.lage() == com.dan.kino.Kino.Lage.ABSPANN;
            r.step(dt);
            d.display();
            clockT[0] += dt;
            frames++;
            if (next < due.size() && clockT[0] >= due.get(next)) {
                BufferedImage[] im = new BufferedImage[1];
                d.invoke(true, x -> { im[0] = r.grab(x); return true; });
                imgs.add(im[0]);
                String txt = rg.onText == null ? "" : ps.text;
                labels.add(String.format(Locale.GERMAN, "%.0f s · %s", clockT[0], shots.get(next)));
                double[] hk = ps.kopf(), ey = r.camera.eye;
                log.append(String.format(Locale.GERMAN, "%.1f s %s · %s · Auge %.2f %.2f %.2f (r %.2f) · Kopf %s · %s%n", clockT[0], shots.get(next), ps.zeile(),
                        ey[0], ey[1], ey[2], Math.hypot(ey[0], ey[2]), hk == null ? "-" : String.format(Locale.GERMAN, "%.2f %.2f %.2f (r %.2f)", hk[0], hk[1], hk[2], Math.hypot(hk[0], hk[2])), r.camera.mode));
                next++;
            }
            if (r.error() != null || ps.error() != null) { System.err.println("FEHLER: " + r.error() + " / " + ps.error()); break; }
        }
        Figur f = ps.erste();
        String person = f == null ? "keine Person" : f.person.name + " · " + f.outfit.name + " · " + ps.herkunft;
        String wet = f == null ? "" : String.format(Locale.GERMAN, "Nässe: Gesicht %.0f %%, Schultern %.0f %%, Haar %.0f %% · %d Schritte",
                f.wetness.wet(com.dan.avatar.Wetness.FACE) * 100, f.wetness.wet(com.dan.avatar.Wetness.SHOULDERS) * 100, f.wetness.wet(com.dan.avatar.Wetness.HAIR) * 100,
                f.motion == null ? 0 : f.motion.steps.size());
        String regie = String.format(Locale.GERMAN, "Regiebuch „%s“ im Kino: %d Zeilen, %.1f s, Fehler: %s; Vorspann, %d Schnitte, Abspann %s, Kino zu Ende nach %.1f s", rg.title, rg.lines.size(), rg.time(),
                rg.errors.isEmpty() ? "keine" : rg.errors, shots.size(), abspann ? "gezeigt" : "FEHLT", frames / 30.0);
        d.destroy();

        // Budget: dieselbe Ansicht (Halbtotale auf die Galerie) mit und ohne Person, 1920 × 1080, feste Auflösung
        boolean nurFilm = System.getenv("NUR_FILM") != null;
        double[] mit = nurFilm ? new double[3] : budget(q, true), ohne = nurFilm ? new double[]{1, 0, 0} : budget(q, false);

        int rows = (imgs.size() + 2) / 3;
        BufferedImage img = new BufferedImage(tw * 3, th * rows + 150, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(12, 16, 22));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        for (int i = 0; i < imgs.size(); i++) {
            int x = (i % 3) * tw, y = (i / 3) * th;
            g.drawImage(imgs.get(i), x, y, tw, th, null);
            Turmbild.label(g, labels.get(i), x, y);
        }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int y0 = th * rows + 24;
        g.setColor(new Color(80, 200, 200));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        g.drawString("AVATAR · PHASE 15 · DER WÄRTER AUF DER GALERIE DES ROTEN SANDS (LIGHTHOUSE)", 14, y0);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        g.setColor(new Color(220, 226, 232));
        String[] lines = {
                person,
                regie,
                wet + String.format(Locale.GERMAN, " · Grundkörper geladen in %.1f s", loadS),
                String.format(Locale.GERMAN, "Budget %s 1920 × 1080, Halbtotale auf die Galerie: ohne Person %.1f ms (%.0f Bilder/s), mit Person %.1f ms (%.0f Bilder/s), Person GPU %.2f ms, Prozessor %.1f ms",
                        q.label, ohne[0], 1000 / ohne[0], mit[0], 1000 / mit[0], mit[1], mit[2]),
                r.glInfo()};
        for (int i = 0; i < lines.length; i++) g.drawString(lines[i], 14, y0 + 24 + i * 20);
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.print(log);
        for (String l : lines) System.out.println(l);
        System.out.println("Bild: " + out.getPath());
        System.exit(0);
    }

    /** Mittlere Bildzeit (ms) bis glFinish, GPU- und Prozessorzeit der Person (ms): 1920 × 1080, feste Auflösung. */
    static double[] budget(Quality q, boolean person) throws Exception {
        Renderer[] rr = new Renderer[1];
        GLOffscreenAutoDrawable d = setup(1920, 1080, q, rr);
        Renderer r = rr[0];
        r.autoScale = false;
        if (person) {
            r.personen.warten();
            r.startPersonen(new com.dan.avatar.Regie("probe", "uhr 22:40\nwetter Sturm wind=19 regen=6\nperson hauke Regenjacke seed=19 ort=Reling_Luv dreh=See nass=5\nkamera Halbtotale hauke winkel=20\n"));
        } else {
            r.startPersonen(new com.dan.avatar.Regie("probe", "uhr 22:40\nwetter Sturm wind=19 regen=6\nperson hauke Regenjacke seed=19 ort=Reling_Luv dreh=See nass=5\nkamera Halbtotale hauke winkel=20\n"));
        }
        for (int i = 0; i < 40; i++) { r.step(1 / 60.0); d.display(); }
        if (!person) r.personen.an = false;
        for (int i = 0; i < 20; i++) { r.step(1 / 60.0); d.display(); }
        double sum = 0;
        int n = 90;
        for (int i = 0; i < n; i++) {
            long t0 = System.nanoTime();
            r.step(1 / 60.0);
            d.display();
            d.getContext().makeCurrent();
            d.getGL().glFinish();
            d.getContext().release();
            sum += (System.nanoTime() - t0) / 1e6;
        }
        double[] res = {sum / n, r.personen.gpuMs, r.personen.cpuMs};
        d.destroy();
        return res;
    }

    static GLOffscreenAutoDrawable setup(int w, int h, Quality q, Renderer[] out) {
        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, w, h);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock(DAY, 22.66);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        wiring.regie = true;
        wiring.weather.startFree(clock.epochMillis() / 1000.0, com.dan.weather.Weather.Phase.STURM);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.setQuality(q);
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }
        out[0] = r;
        return d;
    }

    /** Ein Bild: Wärter an der Reling im Luv, Blick auf die See, Kamera nach Einstellung und Winkel. */
    static void blick(String[] a) throws Exception {
        File out = new File(a.length > 1 ? a[1] : "bilder/galerie.png");
        int w = a.length > 2 ? Integer.parseInt(a[2]) : 1280, h = w * 9 / 16;
        String shot = a.length > 3 ? a[3] : "Halbnah";
        String ang = a.length > 4 ? a[4] : "10";
        String hour = a.length > 5 ? a[5] : "22:40";
        Renderer[] rr = new Renderer[1];
        GLOffscreenAutoDrawable d = setup(w, h, Quality.MITTEL, rr);
        Renderer r = rr[0];
        r.personen.warten();
        String eigen = System.getenv("BLICK_REGIE");
        r.startPersonen(new com.dan.avatar.Regie("blick", eigen != null ? eigen.replace("|", "\n")
                : "uhr " + hour + "\nwetter Sturm wind=19 regen=6\nperson hauke Regenjacke seed=19 ort=Reling_Luv dreh=See nass=10\nkamera " + shot + " hauke winkel=" + ang + "\n"));
        int n = System.getenv("BLICK_BILDER") != null ? Integer.parseInt(System.getenv("BLICK_BILDER")) : 90;
        for (int i = 0; i < n; i++) { r.step(1 / 30.0); d.display(); }
        BufferedImage[] im = new BufferedImage[1];
        d.invoke(true, x -> { im[0] = r.grab(x); return true; });
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(im[0], "png", out);
        System.out.println(r.personen.zeile());
        System.out.println(r.personen.herkunft);
        System.out.println("Kamera: " + r.camera.mode + " " + r.camera.followName + " Auge " + java.util.Arrays.toString(r.camera.eye) + " Kopf " + java.util.Arrays.toString(r.personen.kopf()) + " Regiefehler " + (r.personen.regie == null ? "-" : r.personen.regie.errors));
        com.dan.avatar.gl.AvatarGl.Light pl = r.personen.licht;
        if (pl != null) System.out.println("Licht: Führung " + java.util.Arrays.toString(pl.keyCol) + " Kante " + java.util.Arrays.toString(pl.rimCol) + " Aufhellung " + java.util.Arrays.toString(pl.fillCol) + " Belichtung " + r.exposure());
        System.out.println("Fehler: " + r.error() + " / " + r.personen.error());
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }
}
