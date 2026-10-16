package com.dan.lighthouse.tools;

import com.dan.beacon.Beacon;
import com.dan.cloud.CloudType;
import com.dan.lightning.Bolt;
import com.dan.lightning.Lightning;
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
import java.util.function.Function;

/**
 * Vergleichsbild für Phase 10 ohne Fenster, gerechnet mit den Shadern der App: oben die Vorlage Aussen3 neben der
 * Sturmnacht mit einem Erdblitz hinter dem Turm und einem Einschlag in den Mast; in der Mitte ein Blitz, der sich in
 * der See spiegelt, ein Wolkenblitz in Gewitterwolken und ein Spinnenblitz unter der Untergrenze; unten der Blitz durch
 * die nassen Scheiben des Laternenraums, Wetterleuchten am Horizont und der Leitblitz in Zeitlupe.
 * <pre>
 * Blitzbild datei.png [breite] [MITTEL|HOCH|…]
 * </pre>
 */
public final class Blitzbild {
    private Blitzbild() { }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase10_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1800;
        Quality q = a.length > 2 ? Quality.valueOf(a[2]) : Quality.HOCH;
        int tw = width / 3, th = tw * 3 / 4;
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
        // Neumond-nahe Nacht im Herbst: 16.10.2026, 22:30
        LocalDate day = LocalDate.of(2026, 10, 16);
        SceneClock clock = new SceneClock(day, 22.5);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.setQuality(q);
        wiring.tide.on = false;
        wiring.tide.fixed = 0.6;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }
        Lightning L = r.lightning;

        Turmbild.drawTemplate(g, new File("templates/Leuchturm_Aussen3.jpg"), 0, 0, tw, th, "Vorlage Aussen3");
        // 1 Sturmnacht wie Aussen3: Gewitterwolken, Regen, das Drehfeuer, ein Erdblitz hinter dem Turm
        storm(wiring, 9, CloudType.CUMULONIMBUS, 7, 4, 12);
        r.beacon.copyLight(Beacon.stormNight());
        String check = shoot(d, r, clock, day, g, tw, 0, tw, th, 70, 300, 6, 9, 70 + 200,
                ll -> ll.strike(Math.sin(Math.toRadians(250)) * 3200, -Math.cos(Math.toRadians(250)) * 3200), 1, 0.004, 31);
        Turmbild.label(g, "Sturmnacht: Erdblitz in 3,5 km hinter dem Turm, Bft 9, Regen", tw, 0);
        // 2 Einschlag in den Mast auf der Galerie
        shoot(d, r, clock, day, g, tw * 2, 0, tw, th, 160, 220, 6, 14, 160 + 180, ll -> ll.strikeAttractor(0), 1, 0.006, 5);
        Turmbild.label(g, "Einschlag in den Mast (30,7 m), aus 220 m", tw * 2, 0);
        // 3 Blitz über der See, gespiegelt; ruhigere See, kein Regen am Auge
        storm(wiring, 5, CloudType.CUMULONIMBUS, 6, 0, 25);
        r.beacon.copyLight(Beacon.roterSand1985());
        shoot(d, r, clock, day, g, 0, th, tw, th, 200, 160, 3, 8, 0,
                ll -> ll.strike(Math.sin(Math.toRadians(25)) * 4200, -Math.cos(Math.toRadians(25)) * 4200), 0, 0.004, 31);
        Turmbild.label(g, "Erdblitz in 4 km, die See spiegelt ihn", 0, th);
        // 4 Wolkenblitz: die Gewitterwolken leuchten von innen
        shoot(d, r, clock, day, g, tw, th, tw, th, 200, 160, 3, 22, 0,
                ll -> ll.discharge(Bolt.Kind.INTRA_CLOUD, Math.sin(Math.toRadians(20)) * 6000, -Math.cos(Math.toRadians(20)) * 6000), 0, 0.004, 9);
        Turmbild.label(g, "Wolkenblitz: die Wolken leuchten von innen", tw, th);
        // 5 Spinnenblitz unter der Untergrenze
        shoot(d, r, clock, day, g, tw * 2, th, tw, th, 200, 160, 3, 30, 0,
                ll -> ll.discharge(Bolt.Kind.SPIDER, Math.sin(Math.toRadians(20)) * 2600, -Math.cos(Math.toRadians(20)) * 2600), 1, 0.005, 8);
        Turmbild.label(g, "Spinnenblitz unter der Wolkenuntergrenze", tw * 2, th);
        // 6 Laternenraum im Regen: der Blitz draußen durch die nassen Scheiben
        storm(wiring, 7, CloudType.CUMULONIMBUS, 7, 6, 12);
        r.beacon.copyLight(Beacon.roterSand1985());
        double from = wiring.wind.fromDeg, ai = Math.toRadians(from + 180 + 12);
        r.lampOn = false;
        shootInside(d, r, clock, day, g, 0, th * 2, tw, th, Math.sin(ai) * 1.4, Turm.GALLERY_TOP + 1.7, -Math.cos(ai) * 1.4, from + 5, 9,
                ll -> ll.strike(Math.sin(Math.toRadians(from + 5)) * 2600, -Math.cos(Math.toRadians(from + 5)) * 2600), 0.004, 31);
        r.lampOn = true;
        Turmbild.label(g, "Laternenraum im Regen: der Blitz durch die nassen Scheiben", 0, th * 2);
        // 7 Wetterleuchten am Horizont: klare Nacht über dem Turm, die Gewitter 50 km entfernt
        storm(wiring, 3, CloudType.CUMULUS, 2, 0, 40);
        r.beacon.copyLight(Beacon.roterSand1985());
        shoot(d, r, clock, day, g, tw, th * 2, tw, th, 70, 600, 8, 2, 0,
                ll -> ll.discharge(Bolt.Kind.SHEET, Math.sin(Math.toRadians(245)) * 35_000, -Math.cos(Math.toRadians(245)) * 35_000), 0, 0.004, 6);
        Turmbild.label(g, "Wetterleuchten in 35 km, kein Donner", tw, th * 2);
        // 8 Leitblitz in Zeitlupe
        storm(wiring, 6, CloudType.CUMULONIMBUS, 7, 0, 20);
        r.beacon.copyLight(Beacon.stormNight());
        shoot(d, r, clock, day, g, tw * 2, th * 2, tw, th, 70, 300, 6, 9, 70 + 200,
                ll -> ll.strike(Math.sin(Math.toRadians(250)) * 3200, -Math.cos(Math.toRadians(250)) * 3200), -1, 0.6, 31);
        Turmbild.label(g, "Zeitlupe: der Leitblitz kurz vor dem Einschlag", tw * 2, th * 2);
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println(check.isEmpty() ? "Blitz: Abgleich nicht gelaufen" : check);
        System.out.println(r.lightningLine());
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }

    static void storm(Wiring w, int bft, CloudType type, double oktas, double rain, double visKm) {
        w.wind.fromDeg = 250;
        w.wind.speed = Beaufort.mid(bft);
        w.cloudType = type;
        w.oktas = oktas;
        w.rainRate = rain;
        w.visibilityKm = visKm;
        w.thunderstorm = Lightning.Level.OFF;
    }

    /**
     * Von See auf den Turm: Auge im Azimut az, Abstand dist, Höhe h, Blick um yawOff gedreht und pitch geneigt, die
     * Linse gedreht; dann der Blitz aus make (mit eigenem seed), gezeigt stroke + after s nach seinem Start (stroke −1:
     * after als Anteil der Zeit des Leitblitzes).
     */
    static String shoot(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, LocalDate day, Graphics2D g, int x, int y, int w, int h,
                        double az, double dist, double eyeH, double pitch, double rot, Function<Lightning, Bolt> make, int stroke, double after, long seed) {
        double ar = Math.toRadians(az);
        double ex = Math.sin(ar) * dist, ez = -Math.cos(ar) * dist;
        return shootFree(d, r, clock, day, g, x, y, w, h, ex, eyeH, ez, az + 180 + yawOff, -pitch, rot, make, stroke, after, seed);
    }

    static double yawOff;

    static String shootInside(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, LocalDate day, Graphics2D g, int x, int y, int w, int h,
                              double ex, double ey, double ez, double yaw, double pitch, Function<Lightning, Bolt> make, double after, long seed) {
        return shootFree(d, r, clock, day, g, x, y, w, h, ex, ey, ez, yaw, pitch, 0, make, 0, after, seed);
    }

    static String shootFree(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, LocalDate day, Graphics2D g, int x, int y, int w, int h,
                            double ex, double ey, double ez, double yaw, double pitch, double rot, Function<Lightning, Bolt> make, int stroke, double after, long seed) {
        d.setSurfaceSize(w, h);
        clock.set(day, 22.5);
        r.ocean.rebuildNow();
        r.resettleWeathering();
        r.camera.place(ex, ey, ez, yaw, pitch);
        for (int i = 0; i < 40; i++) r.step(1 / 30.0);
        r.camera.place(ex, ey, ez, yaw, pitch);
        r.beacon.snapOn(true);
        for (Beacon n : r.neighbours) n.snapOn(true);
        r.beacon.setTime(0.3, rot);
        r.fillBeaconGrid();
        r.readapt();
        // erst ohne Blitz anpassen, dann den Blitz bauen und auf den Augenblick stellen
        for (int i = 0; i < 4; i++) d.display();
        Lightning L = r.lightning;
        r.wiring().feed(L, r.clouds.snapshot(), r.cloudsRunning(), r.camera.eye);
        L.seed = seed;
        L.restart();
        Bolt b = make.apply(L);
        double t = stroke < 0 ? b.leaderTime * after : b.strokeTime[Math.min(stroke, b.strokeTime.length - 1)] + after;
        L.setTime(b.t0 + t);
        r.step(0);
        for (int i = 0; i < 3; i++) d.display();
        final BufferedImage[] img = new BufferedImage[1];
        d.invoke(true, x2 -> { img[0] = r.grab(x2); return true; });
        g.drawImage(img[0], x, y, null);
        if (System.getenv("BLITZ_DEBUG") != null) System.out.println(String.format(Locale.GERMAN, "Belichtung %.3g · %s", r.exposure(), r.lightningLine()));
        String c = r.lightningCheck();
        L.restart();
        r.step(0);
        return c;
    }
}
