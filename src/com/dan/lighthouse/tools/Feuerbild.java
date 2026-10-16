package com.dan.lighthouse.tools;

import com.dan.beacon.Beacon;
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
 * Vergleichsbild für Phase 9 ohne Fenster, gerechnet mit den Shadern der App: oben die Vorlage Aussen3 neben Fl(3) W 15s
 * in klarer Nacht und im Landregen; in der Mitte der Rote Sand 1985 an der Grenze Weiß/Rot, die Sturmnacht mit Gischt
 * und die Nachbarfeuer von der Galerie; unten der Laternenraum mit den wandernden Lichtflecken der Drehlinse (zwei
 * Augenblicke) und mit der Trommellinse des Roten Sands.
 * <pre>
 * Feuerbild datei.png [breite] [MITTEL|HOCH|…]
 * </pre>
 */
public final class Feuerbild {
    private Feuerbild() { }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase9_claude.png");
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
        // Vollmond am 26.10.2026: der Turm steht im Mondlicht, wie in Aussen3
        LocalDate day = LocalDate.of(2026, 10, 26);
        SceneClock clock = new SceneClock(day, 22);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.setQuality(q);
        wiring.tide.on = false;
        wiring.tide.fixed = 0.2;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }
        if (System.getenv("NOBEAM") != null) r.showBeam = false;
        if (System.getenv("NONB") != null) r.showNeighbours = false;

        Turmbild.drawTemplate(g, new File("templates/Leuchturm_Aussen3.jpg"), 0, 0, tw, th, "Vorlage Aussen3");
        // 1 Fl(3) W 15s in klarer Nacht und im Landregen, gleicher Blick, ein Strahl streicht schräg am Auge vorbei
        wiring.wind.fromDeg = 250;
        wiring.wind.speed = Beaufort.mid(4);
        wiring.cloudType = CloudType.STRATOCUMULUS;
        wiring.oktas = 3;
        wiring.visibilityKm = 40;
        wiring.rainRate = 0;
        r.beacon.copyLight(Beacon.flash3());
        double camAz = 200;
        // erst der Regen; die klare Nacht dann mit derselben Belichtung, damit der Unterschied im Strahl sichtbar bleibt
        wiring.cloudType = CloudType.NIMBOSTRATUS;
        wiring.oktas = 8;
        wiring.visibilityKm = 15;
        wiring.rainRate = 4;
        // vier Blenden knapper als die Anpassung, damit der Strahl im Regen nicht ausbrennt
        biasNext = -4;
        shoot(d, r, clock, day, 22.5, g, tw * 2, 0, tw, th, camAz, 380, 8, 4.0, camAz + 22);
        double rainExposure = r.exposure();
        r.exposureBias = 0;
        Turmbild.label(g, "dieselbe Nacht im Landregen, 4 mm/h (Belichtung −4 Blenden)", tw * 2, 0);
        wiring.cloudType = CloudType.CUMULUS;
        wiring.oktas = 1;
        wiring.visibilityKm = 40;
        wiring.rainRate = 0;
        lockExposure = rainExposure;
        shoot(d, r, clock, day, 22.5, g, tw, 0, tw, th, camAz, 380, 8, 4.0, camAz + 22);
        lockExposure = 0;
        r.exposureBias = 0;
        Turmbild.label(g, "Fl(3) W 15s · klare Nacht, Sicht 40 km (gleiche Belichtung)", tw, 0);
        // 2 Roter Sand 1985 an der Grenze Weiß/Rot (Peilung 185°: Schiff im Azimut 5°)
        r.beacon.copyLight(Beacon.roterSand1985());
        wiring.rainRate = 2;
        wiring.visibilityKm = 12;
        shoot(d, r, clock, day, 22.5, g, 0, th, tw, th, 5, 1800, 5, 1.0, 0);
        Turmbild.label(g, "Roter Sand 1985 im Regen, Peilung 185°: Weiß | Rot", 0, th);
        // 3 Sturmnacht: Fl W 5s, Bft 10, Gewitterwolken, Regen und Gischt
        r.beacon.copyLight(Beacon.stormNight());
        wiring.wind.speed = Beaufort.mid(10);
        wiring.cloudType = CloudType.CUMULONIMBUS;
        wiring.oktas = 7;
        wiring.rainRate = 8;
        wiring.visibilityKm = 10;
        shoot(d, r, clock, day, 22.5, g, tw, th, tw, th, 250, 260, 8, 6.0, 250 + 30);
        Turmbild.label(g, "Sturmnacht Fl W 5s · Bft 10, Regen 8 mm/h, Gischt", tw, th);
        // 4 Nachbarfeuer von der Galerie: Alte Weser und Tegeler Plate in klarer Nacht
        r.beacon.copyLight(Beacon.roterSand1985());
        wiring.wind.speed = Beaufort.mid(3);
        wiring.cloudType = CloudType.CUMULUS;
        wiring.oktas = 1;
        wiring.rainRate = 0;
        wiring.visibilityKm = 40;
        double[] aw = Turm.alteWeserPos();
        double awAz = Math.toDegrees(Math.atan2(aw[0], -aw[1]));
        double ga = Math.toRadians(100 - 25);
        r.zoom = 1;
        shootFree(d, r, clock, day, 22.5, g, tw * 2, th, tw, th, Math.sin(ga) * 3.9, Turm.GALLERY_TOP + 1.6, -Math.cos(ga) * 3.9, 100, 0.5, 0);
        r.zoom = 1;
        Turmbild.label(g, "Von der Galerie: Alte Weser und Tegeler Plate", tw * 2, th);
        // 5 Laternenraum mit der Trommellinse des Roten Sands: Streifen der Prismenringe, Lichtring auf den Dielen
        double inR = Math.toRadians(120);
        r.lampOn = false;
        shootInside(d, r, clock, day, 22.5, g, 0, th * 2, tw, th, Math.sin(inR) * 1.4, Turm.GALLERY_TOP + 1.9, -Math.cos(inR) * 1.4, 120 + 180 - 30, 35, 0);
        Turmbild.label(g, "Laternenraum, Roter Sand 1985: Trommellinse, Lichtring auf den Dielen", 0, th * 2);
        // 6, 7 Laternenraum: die Drehlinse, zwei Augenblicke eine Sekunde auseinander
        r.beacon.copyLight(Beacon.stormNight());
        double in = Math.toRadians(250);
        for (int k = 0; k < 2; k++) {
            r.lampOn = false;
            shootInside(d, r, clock, day, 22.5, g, tw + k * tw, th * 2, tw, th, Math.sin(in) * 1.35, Turm.GALLERY_TOP + 1.9, -Math.cos(in) * 1.35, 250 + 180 - 25, 30, 100 + 18 * k);
            Turmbild.label(g, k == 0 ? "Laternenraum, Drehlinse: Lichtflecken auf Dielen, Reling und Glas" : "eine Sekunde später: die Flecken sind weitergewandert", tw + k * tw, th * 2);
        }
        r.lampOn = true;
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println(r.beaconCheck().isEmpty() ? "Feuer: Abgleich nicht gelaufen" : r.beaconCheck());
        System.out.println(r.beaconLine());
        System.out.println(r.beacon.describe());
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }

    /** Von See auf den Turm: Auge im Azimut az, Abstand dist, Höhe h, Neigung pitch; die Linse auf rot gedreht. */
    static void shoot(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, LocalDate day, double hour, Graphics2D g, int x, int y, int w, int h,
                      double az, double dist, double eyeH, double pitch, double rot) {
        double ar = Math.toRadians(az);
        double ex = Math.sin(ar) * dist, ez = -Math.cos(ar) * dist;
        double tilt = Math.toDegrees(Math.atan2(Turm.LENS_Y - 6 - eyeH, dist));
        shootFree(d, r, clock, day, hour, g, x, y, w, h, ex, eyeH, ez, az + 180, tilt + pitch, rot);
    }

    static void shootFree(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, LocalDate day, double hour, Graphics2D g, int x, int y, int w, int h,
                          double ex, double ey, double ez, double yaw, double pitch, double rot) {
        d.setSurfaceSize(w, h);
        clock.set(day, hour);
        r.ocean.rebuildNow();
        r.resettleWeathering();
        r.camera.place(ex, ey, ez, yaw, pitch);
        for (int i = 0; i < 40; i++) r.step(1 / 30.0);
        r.camera.place(ex, ey, ez, yaw, pitch);
        r.beacon.snapOn(true);
        for (Beacon n : r.neighbours) n.snapOn(true);
        r.beacon.setTime(0.3, rot);
        r.fillBeaconGrid();
        for (Beacon n : r.neighbours) n.setTime(2.0, 0);
        r.readapt();
        r.exposureBias = biasNext;
        biasNext = 0;
        if (lockExposure > 0) r.exposureBias += Math.log(lockExposure / r.exposure()) / Math.log(2);
        for (int i = 0; i < 12; i++) d.display();
        final BufferedImage[] img = new BufferedImage[1];
        d.invoke(true, x2 -> { img[0] = r.grab(x2); return true; });
        g.drawImage(img[0], x, y, null);
        if (System.getenv("FEUER_DEBUG") != null) System.out.println(f("Belichtung %.3g · Feuer im Blick %.3g cd/m² · %s", r.exposure(), r.beaconLum(), r.beaconLine()));
        if (System.getenv("FEUER_DEBUG") != null) {
            double mx = 0, mean = 0; int nn = 0;
            for (int i = -40; i <= 40; i++) for (int j = -15; j <= 15; j++) {
                double yr = Math.toRadians(yaw + i * 0.8), pr = Math.toRadians(-pitch + j * 0.8);
                double[] dd = {Math.sin(yr) * Math.cos(pr), Math.sin(pr), -Math.cos(yr) * Math.cos(pr)};
                double tm = dd[1] < 0 ? Math.min(20000, ey / -dd[1]) : 20000;
                double L = com.dan.beacon.LightColour.luminance(r.beacon.snapshot().beamRadiance(new double[]{ex, ey, ez}, dd, tm, r.beaconGrid(), 48, 0.5));
                mx = Math.max(mx, L); mean += L; nn++;
            }
            System.out.println(f("Strahl im Bild: größte %.3g cd/m², Mittel %.3g", mx, mean / nn));
        }
        if (System.getenv("FEUER_DEBUG") != null) try { java.lang.reflect.Method m = Renderer.class.getDeclaredMethod("skyLine"); m.setAccessible(true); System.out.println(m.invoke(r)); } catch (Exception e) { System.out.println(e); }
    }

    static void shootInside(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, LocalDate day, double hour, Graphics2D g, int x, int y, int w, int h,
                            double ex, double ey, double ez, double yaw, double pitch, double rot) {
        shootFree(d, r, clock, day, hour, g, x, y, w, h, ex, ey, ez, yaw, pitch, rot);
    }

    /** Belichtung festhalten (> 0) für den Vergleich klar gegen Regen. */
    static double lockExposure, biasNext;

    static String f(String fmt, Object... a) { return String.format(Locale.GERMAN, fmt, a); }
}
