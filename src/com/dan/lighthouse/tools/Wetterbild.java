package com.dan.lighthouse.tools;

import com.dan.lightning.Bolt;
import com.dan.lightning.Lightning;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.wiring.Wiring;
import com.dan.weather.Scenario;
import com.dan.weather.Weather;
import com.dan.weather.demo.WeatherChart;
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
 * Statusbild für Phase 12 ohne Fenster, mit den Shadern der App: „Ein Sturmtag“ in acht Bildern, jedes mit dem
 * Wetter, das die Regie zu dieser Zeit setzt, und der Kamera aus dem Regiebuch; darunter die Barometerkurve mit Wind,
 * Regen und Bedeckung, die acht Zeitpunkte markiert.
 * <pre>
 * Wetterbild datei.png [breite] [MITTEL|HOCH|…]
 * </pre>
 */
public final class Wetterbild {
    private Wetterbild() { }

    static final LocalDate DAY = LocalDate.of(2026, 10, 16);

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase12_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1800;
        Quality q = a.length > 2 ? Quality.valueOf(a[2]) : Quality.HOCH;
        int tw = width / 4, th = tw * 2 / 3, ch = width / 2;
        BufferedImage img = new BufferedImage(width, th * 2 + ch, BufferedImage.TYPE_INT_RGB);
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
        Scenario sc = Scenario.stormDay();
        SceneClock clock = new SceneClock(DAY, sc.startHour());
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.setQuality(q);
        wiring.tide.on = true;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }
        r.awaitInterior();

        double t0 = clock.epochMillis() / 1000.0;
        wiring.regie = true;
        wiring.weather.start(sc, t0);
        // Stunden ab 06:00, Kamera aus dem Regiebuch (die Sturmnacht aus 1,2 km, damit das Feuer nicht blendet)
        double[] at = {1.5, 4.5, 7.5, 10.5, 13.4, 17.0, 25.85, 27.4};
        String[] cam = {"Aussen1", "Drohne", "Aussen1", "Brecher", "Aussen3", "Sturmnacht", "Aussen1", "Regenbogen"};
        double[] marks = new double[at.length];
        double done = t0;
        StringBuilder log = new StringBuilder();
        for (int i = 0; i < at.length; i++) {
            double t = t0 + at[i] * 3600;
            marks[i] = t;
            // die Regie bis dahin in 5-Minuten-Schritten durchrechnen (so wie sie in der App liefe)
            for (double s = done + 300; s < t; s += 300) wiring.weather.update(s, 0.05);
            done = t;
            double hh = sc.startHour() + at[i];
            clock.set(DAY.plusDays((long) Math.floor(hh / 24)), hh % 24);
            r.ocean.rebuildNow();
            Weather.Snapshot ws = wiring.feedWeather(0.05);
            r.applyView(cam[i]);
            r.camera.snap();
            for (int k = 0; k < 24; k++) { r.step(1 / 30.0); r.camera.snap(); d.display(); }
            r.ocean.rebuildNow();
            r.beacon.snapOn(Renderer.daylightLux(r.skyState(), r.cloudShade()) < r.beacon.onLux);
            r.readapt();
            if (ws.thunder() >= 2) {
                // ein Erdblitz hinter dem Turm, gezeigt beim ersten Schlag
                Lightning L = r.lightning;
                wiring.feed(L, r.clouds.snapshot(), r.cloudsRunning(), r.camera.eye);
                L.seed = 31;
                L.restart();
                double az = Math.toRadians(250);
                Bolt b = L.strike(Math.sin(az) * 3200, -Math.cos(az) * 3200);
                L.setTime(b.t0 + b.strokeTime[0] + 0.004);
            }
            for (int k = 0; k < 3; k++) { r.step(0); r.camera.snap(); d.display(); }
            final BufferedImage[] shot = new BufferedImage[1];
            d.invoke(true, x -> { shot[0] = r.grab(x); return true; });
            int x = (i % 4) * tw, y = (i / 4) * th;
            g.drawImage(shot[0], x, y, null);
            Turmbild.label(g, String.format(Locale.GERMAN, "%d · %s · %s · Bft %d · %.0f hPa", i + 1, clock.timeLabel(), ws.stepText(), ws.beaufort(), ws.pressure()), x, y);
            log.append(String.format(Locale.GERMAN, "%d %s %s Bft %d %.1f hPa %s Regen %.1f Sicht %.0f km, %.1f/8 %s, Gewitter %d, Regenbogen %.2f%n",
                    i + 1, clock.timeLabel(), ws.stepText(), ws.beaufort(), ws.pressure(), ws.tendencyText(), ws.rain(), ws.effectiveVisibilityKm(), ws.oktas(),
                    ws.cloud(), ws.thunder(), r.sky.rainbow));
            if (ws.thunder() >= 2) { r.lightning.restart(); r.step(0); }
        }
        WeatherChart.draw(g, 0, th * 2, width, ch, sc, Weather.run(sc, t0, sc.hours(), 300, wiring.weather.seed), Double.NaN, SceneClock.ZONE, marks);
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.print(log);
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }
}
