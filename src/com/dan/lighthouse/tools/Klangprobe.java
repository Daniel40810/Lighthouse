package com.dan.lighthouse.tools;

import com.dan.cloud.CloudType;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.scene.Turm;
import com.dan.lighthouse.wiring.Wiring;
import com.dan.soundscape.Soundscape;
import com.dan.soundscape.Wav;
import com.dan.soundscape.demo.Analysis;
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
import java.util.Locale;

/**
 * Hörproben für Phase 14 ohne Lautsprecher, mit der ganzen App: Je Wetterlage, draußen und drinnen, läuft die Szene
 * 15 s in Schritten von 1/30 s, und der Klang wird im selben Takt mitgerechnet (so wie ihn der Faden des
 * Lautsprechers abholt). Jede Probe wird als WAV geschrieben; das Statusbild zeigt je Probe ein Bild der Szene, das
 * Spektrogramm und die Pegel.
 * <pre>
 * Klangprobe [bild.png] [ordner für WAV] [breite]
 * </pre>
 */
public final class Klangprobe {
    private Klangprobe() { }

    static final LocalDate DAY = LocalDate.of(2026, 10, 16);
    static final double LEN = 15;

    interface Script { void at(double t, Renderer r, Wiring w); }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase14_claude.png");
        File wavDir = new File(a.length > 1 ? a[1] : "docs/status/klang");
        int width = a.length > 2 ? Integer.parseInt(a[2]) : 1800;
        int tw = 400, th = 225, rowH = th + 44;
        String[] titles = new String[6];
        BufferedImage img = new BufferedImage(width, rowH * 6 + 36, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(11, 17, 24));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, tw, th);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock(DAY, 11);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.setQuality(Quality.HOCH);
        wiring.tide.on = false;
        wiring.tide.fixed = 0.5;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }
        r.awaitInterior();
        wiring.soundLive = true;
        Soundscape s = r.soundscape;
        double gal = Math.toRadians(250);
        double[] galerie = {Math.sin(gal) * 3.9, Turm.GALLERY_TOP + 1.6, -Math.cos(gal) * 3.9};

        int row = 0;
        // 1 klar, Bft 2, Galerie, Möwen
        weather(wiring, 2, 250, CloudType.CUMULUS, 2, 0, 0);
        titles[row] = "1 · Klar, Bft 2, auf der Galerie: leise See, Brandung am Sockel, Möwen";
        probe(d, r, clock, 11, galerie, 250, 2, g, row++, rowH, tw, th, width, wavDir, "1_klar_Bft2_Galerie", titles, (t, rr, w) -> {
            if (Math.abs(t - 2) < 0.02 || Math.abs(t - 8) < 0.02) s.event(Soundscape.Kind.MOEWE, galerie[0] - 12, 27, galerie[2] - 8, 1);
        });
        // 2 Bft 5, Galerie
        weather(wiring, 5, 250, CloudType.CUMULUS, 4, 0, 0);
        titles[row] = "2 · Frische Brise, Bft 5, auf der Galerie: Wind, Handlauf und Stäbe singen, Brandung";
        probe(d, r, clock, 13, galerie, 250, 2, g, row++, rowH, tw, th, width, wavDir, "2_Bft5_Galerie", titles, (t, rr, w) -> { });
        // 3 Sturm draußen
        weather(wiring, 9, 250, CloudType.NIMBOSTRATUS, 8, 6, 0);
        titles[row] = "3 · Sturm, Bft 9, Regen 6 mm/h, auf der Galerie: Brecher am Sockel, Gischt";
        probe(d, r, clock, 15, galerie, 250, 2, g, row++, rowH, tw, th, width, wavDir, "3_Sturm_Bft9_Galerie", titles, (t, rr, w) -> {
            if (Math.abs(t - 4) < 0.02 || Math.abs(t - 10) < 0.02) rr.breaker();
        });
        // 4 Sturm drinnen
        titles[row] = "4 · Derselbe Sturm im Treppenhaus: dumpf, hallig, die Brecher als Dröhnen";
        probe(d, r, clock, 15, new double[]{0.4, 13.5, 0.9}, 200, 25, g, row++, rowH, tw, th, width, wavDir, "4_Sturm_Bft9_Treppenhaus", titles, (t, rr, w) -> {
            if (Math.abs(t - 4) < 0.02 || Math.abs(t - 10) < 0.02) rr.breaker();
        });
        // 5 Gewitter im Laternenraum, nachts
        weather(wiring, 7, 240, CloudType.CUMULONIMBUS, 8, 10, 0);
        double in = Math.toRadians(240 + 192);
        titles[row] = "5 · Gewitter in der Nacht, im Laternenraum: Regen auf Scheiben und Kuppel, Donner in 1,5 km";
        probe(d, r, clock, 22, new double[]{Math.sin(in) * 1.4, Turm.GALLERY_TOP + 1.7, -Math.cos(in) * 1.4}, 245, 5, g, row++, rowH, tw, th, width, wavDir,
                "5_Gewitter_Laternenraum", titles, (t, rr, w) -> {
                    if (Math.abs(t - 1) < 0.02) {
                        w.feed(rr.lightning, rr.clouds.snapshot(), rr.cloudsRunning(), rr.camera.eye);
                        double az = Math.toRadians(245);
                        rr.lightning.strike(Math.sin(az) * 1500, -Math.cos(az) * 1500);
                    }
                });
        // 6 Seenebel auf der Brücke
        weather(wiring, 2, 200, CloudType.STRATUS, 8, 0, 0.9);
        r.buoyancy.ships.clear();
        titles[row] = "6 · Seenebel, auf der Brücke eines Kümos: Diesel, das eigene Typhon, das Nebelhorn des Turms";
        probeBridge(d, r, clock, g, row++, rowH, tw, th, width, wavDir, titles);

        g.setColor(new Color(150, 165, 178));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.drawString("Spektrogramme je 15 s, 30 Hz bis 16 kHz logarithmisch, hell = laut; gerechnet mit der ganzen App im Takt von 1/30 s, ohne Lautsprecher. "
                + "Die Hörproben liegen als WAV daneben.", 10, rowH * 6 + 24);
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }

    static void weather(Wiring w, int bft, double from, CloudType ct, int oktas, double rain, double fog) {
        w.regie = false;
        w.wind.speed = com.dan.lighthouse.wiring.Beaufort.mid(bft);
        w.wind.fromDeg = from;
        w.cloudType = ct;
        w.oktas = oktas;
        w.rainRate = rain;
        w.visibilityKm = rain > 0 ? 15 : 30;
        w.fogAmount = fog;
    }

    static void probe(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, double hour, double[] eye, double yaw, double pitch, Graphics2D g, int row, int rowH,
                      int tw, int th, int width, File wavDir, String file, String[] titles, Script script) throws Exception {
        clock.set(DAY, hour);
        r.ocean.rebuildNow();
        r.resettleWeathering();
        r.zoom = 1;
        r.camera.place(eye[0], eye[1], eye[2], yaw, pitch);
        // einschwingen: Wetter, Meer, Gischt, Ohr (ohne Ton)
        for (int k = 0; k < 90; k++) r.step(1 / 30.0);
        r.beacon.snapOn(Renderer.daylightLux(r.skyState(), r.cloudShade()) < r.beacon.onLux);
        record(d, r, g, row, rowH, tw, th, width, wavDir, file, titles, script);
    }

    static void probeBridge(GLOffscreenAutoDrawable d, Renderer r, SceneClock clock, Graphics2D g, int row, int rowH, int tw, int th, int width, File wavDir,
                            String[] titles) throws Exception {
        clock.set(DAY, 9);
        r.ocean.rebuildNow();
        r.toBridge();
        for (int k = 0; k < 90; k++) r.step(1 / 30.0);
        com.dan.buoyancy.Ship sh = r.onBridge();
        record(d, r, g, row, rowH, tw, th, width, wavDir, "6_Nebel_Bruecke", titles, (t, rr, w) -> {
            if (sh == null) return;
            double[] p = sh.toWorld(sh.length * 0.2, 0, sh.freeboard + 9);
            if (Math.abs(t - 3) < 0.02) rr.soundscape.horn(Soundscape.Kind.TYFON, p[0], p[1], p[2], 1, 5);
            if (Math.abs(t - 1) < 0.02 || Math.abs(t - 11) < 0.02) rr.soundscape.horn(Soundscape.Kind.NEBELHORN, 0, Turm.LENS_Y + 2.5, 0, 1, 1);
        });
    }

    static void record(GLOffscreenAutoDrawable d, Renderer r, Graphics2D g, int row, int rowH, int tw, int th, int width, File wavDir, String file, String[] titles,
                       Script script) throws Exception {
        Soundscape s = r.soundscape;
        // das Ohr hat sich nach 4 s angepasst
        s.render(4);
        int sr = s.sampleRate(), steps = (int) (LEN * 30), per = sr / 30;
        // die Uhr läuft mit (Blitz und Donner rechnen in Szenenzeit)
        r.clock.lapse = 1;
        float[] audio = new float[2 * per * steps];
        float[] buf = new float[2 * per];
        for (int k = 0; k < steps; k++) {
            script.at(k / 30.0, r, r.wiring());
            r.step(1 / 30.0);
            s.render(buf, per);
            System.arraycopy(buf, 0, audio, 2 * per * k, 2 * per);
        }
        r.clock.lapse = 0;
        String lv = s.describe();
        Wav.write(new File(wavDir, file + ".wav"), audio, sr);
        r.readapt();
        for (int k = 0; k < 4; k++) d.display();
        final BufferedImage[] shot = new BufferedImage[1];
        d.invoke(true, x -> { shot[0] = r.grab(x); return true; });
        int y = row * rowH;
        g.setColor(new Color(230, 234, 238));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.drawString(titles[row], 10, y + 18);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(new Color(150, 165, 178));
        g.drawString(lv, 10, y + 35);
        g.drawImage(shot[0], 10, y + 42, null);
        Analysis.spectrogram(g, audio, sr, tw + 20, y + 42, width - tw - 30, th, -40);
        System.out.println(String.format(Locale.GERMAN, "%s%n   %s · WAV %s", titles[row], lv, file));
    }
}
