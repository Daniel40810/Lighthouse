package com.dan.lighthouse.tools;

import com.dan.buoyancy.Ship;
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
 * Statusbild für Phase 13 ohne Fenster, mit den Shadern der App: Möwenflug, Möwen im Aufwind an der Galerie,
 * Schiffsbrücke im Seegang mit Regen und Wischer, Nebelbank mit Strahl, der Turm ragt aus flachem Nebel, ein Schiff
 * nachts mit seinen Lichtern im Fahrwasser.
 * <pre>
 * Lebensbild datei.png [breite] [MITTEL|HOCH|…]
 * </pre>
 */
public final class Lebensbild {
    private Lebensbild() { }

    static final LocalDate DAY = LocalDate.of(2026, 10, 16);

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase13_claude.png");
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1800;
        Quality q = a.length > 2 ? Quality.valueOf(a[2]) : Quality.HOCH;
        int tw = width / 3, th = tw * 2 / 3;
        BufferedImage img = new BufferedImage(tw * 3, th * 2, BufferedImage.TYPE_INT_RGB);
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
        SceneClock clock = new SceneClock(DAY, 11);
        clock.lapse = 0;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.frozen = true;
        r.showHud = false;
        r.simThreaded = false;
        r.setQuality(q);
        if (System.getenv("FEUER_EINHEIT") != null) r.beaconUnit = Double.parseDouble(System.getenv("FEUER_EINHEIT"));
        wiring.tide.on = false;
        wiring.tide.fixed = 0.5;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { System.err.println("FEHLER: " + r.error()); System.exit(2); }
        r.awaitInterior();
        StringBuilder log = new StringBuilder();

        // 1 Möwenflug: Bft 5 aus West, Vormittag, auf einer Möwe
        weather(r, wiring, 5, 260, CloudType.CUMULUS, 3, 0, 0);
        settle(r, clock, 10.5, 90);
        // eine Möwe, die gerade auf den Turm zuhält (höchstens 40 s suchen)
        boolean rode = false;
        for (int t = 0; t < 80; t++) {
            rode = r.rideGull();
            for (int k = 0; k < 15; k++) r.step(1 / 30.0);
            double[] e = r.camera.eye, f = r.camera.fwd;
            double dl = Math.hypot(e[0], e[2]);
            if (rode && dl > 30 && dl < 160 && -(e[0] * f[0] + e[2] * f[2]) / (dl * Math.hypot(f[0], f[2])) > 0.85) break;
        }
        if (on(PANEL++)) shoot(d, r, g, 0, 0, log, "Möwenflug" + (rode ? "" : " (keine fliegt)"));

        // 2 Möwen im Aufwind an der Luvseite der Galerie, von der Galerie aus
        r.camera.mode = com.dan.lighthouse.camera.Camera.Mode.KREISEN;
        double ga = Math.toRadians(260 + 70);
        r.lookFrom(Math.sin(ga) * 3.9, Turm.GALLERY_TOP + 1.6, -Math.cos(ga) * 3.9, Math.sin(Math.toRadians(260)) * 14, Turm.GALLERY_TOP - 1, -Math.cos(Math.toRadians(260)) * 14);
        for (int k = 0; k < 6; k++) r.step(1 / 30.0);
        if (on(PANEL++)) shoot(d, r, g, tw, 0, log, "Von der Galerie: Möwen im Aufwind an der Luvseite");

        // 3 Schiffsbrücke: Bft 7, Regen, auf der Brücke des einlaufenden Kümos, der Wischer läuft
        weather(r, wiring, 7, 250, CloudType.NIMBOSTRATUS, 8, 1.5, 0);
        settle(r, clock, 14, 30);
        Ship s = r.toBridge();
        r.wiperMode = System.getenv("WISCHER_AUS") != null ? 0 : 1;
        for (int k = 0; k < 30 * 25; k++) r.step(1 / 30.0);
        r.zoom = System.getenv("BRUECKE_ZOOM") != null ? Double.parseDouble(System.getenv("BRUECKE_ZOOM")) : 1.6;
        if (on(PANEL++)) shoot(d, r, g, tw * 2, 0, log, String.format(Locale.GERMAN, "Brücke %s im Seegang, Bft 7, Regen 1,5 mm/h, Wischer (Zoom 1,6×)", s.name));
        r.wiperMode = -1;
        r.zoom = 1;

        // 4 Nebelbank mit Strahl: Nacht, Bft 2, Nebel 0,7
        r.camera.place(0, 20, 0, 0, 0);
        weather(r, wiring, 2, 200, CloudType.STRATUS, 8, 0, 0.5);
        settle(r, clock, 22, 30);
        r.camera.lookAt(0, 24, 0, 245, -1.5, 650);
        r.camera.mode = com.dan.lighthouse.camera.Camera.Mode.KREISEN;
        r.camera.snap();
        lightsOn(r);
        if (on(PANEL++)) shoot(d, r, g, 0, th, log, "Nachts in der Nebelbank: der Strahl im Nebel");

        // 5 Flacher Nebel am Morgen, der Turm ragt heraus (Drohne)
        weather(r, wiring, 2, 120, CloudType.CUMULUS, 1, 0, 0.42);
        settle(r, clock, 10, 20);
        r.applyView("Drohne");
        r.camera.lookAt(0, 12, 0, 300, 18, 160);
        r.camera.snap();
        if (on(PANEL++)) shoot(d, r, g, tw, th, log, "Flacher Seenebel am Morgen, der Turm ragt heraus");

        // 6 Ein Schiff nachts mit seinen Lichtern im Fahrwasser, von der Galerie
        weather(r, wiring, 3, 240, CloudType.STRATOCUMULUS, 3, 0, 0);
        settle(r, clock, 19.1, 10);
        r.buoyancy.ships.clear();
        Ship n = r.buoyancy.spawn(false, 6700);
        r.buoyancy.settleAll();
        for (int k = 0; k < 60; k++) r.step(1 / 30.0);
        // 300 m an Steuerbord voraus: Topplichter, das grüne Seitenlicht, der Rumpf vor dem Abendhimmel
        double[] fw = n.forward(), rt = n.right();
        double ex = n.x + rt[0] * 300 + fw[0] * 200, ez = n.z + rt[1] * 300 + fw[1] * 200;
        r.zoom = 1.6;
        r.lookFrom(ex, 6, ez, n.x, 5, n.z);
        lightsOn(r);
        if (on(PANEL++)) shoot(d, r, g, tw * 2, th, log, String.format(Locale.GERMAN, "%s in der Dämmerung, Lichter an, aus 360 m (Zoom 1,6×)", n.name));
        r.zoom = 1;

        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(img, "png", out);
        System.out.print(log);
        System.out.println("Bild: " + out.getPath());
        d.destroy();
        System.exit(0);
    }

    static int PANEL = 1;

    /** Mit der Umgebungsvariable NUR=1,4 nur diese Bilder (zum Prüfen). */
    static boolean on(int i) {
        String n = System.getenv("NUR");
        if (n == null || n.isBlank()) return true;
        for (String x : n.split(",")) if (x.trim().equals(String.valueOf(i))) return true;
        return false;
    }

    static void weather(Renderer r, Wiring w, int bft, double from, CloudType ct, int oktas, double rain, double fog) {
        w.regie = false;
        w.wind.speed = Beaufort.mid(bft);
        w.wind.fromDeg = from;
        w.cloudType = ct;
        w.oktas = oktas;
        w.rainRate = rain;
        w.visibilityKm = rain > 0 ? 18 : 30;
        w.fogAmount = fog;
    }

    /** Uhr setzen, Meer neu, Leben einschwingen lassen (Sekunden Szene). */
    static void settle(Renderer r, SceneClock clock, double hour, double seconds) {
        clock.set(DAY, hour);
        r.ocean.rebuildNow();
        r.resettleWeathering();
        for (int k = 0; k < seconds * 30; k++) r.step(1 / 30.0);
    }

    static void lightsOn(Renderer r) {
        r.beacon.snapOn(Renderer.daylightLux(r.skyState(), r.cloudShade()) < r.beacon.onLux);
        r.fillBeaconGrid();
    }

    static void shoot(GLOffscreenAutoDrawable d, Renderer r, Graphics2D g, int x, int y, StringBuilder log, String label) {
        r.readapt();
        for (int k = 0; k < 8; k++) { r.step(1 / 30.0); d.display(); }
        final BufferedImage[] shot = new BufferedImage[1];
        d.invoke(true, x2 -> { shot[0] = r.grab(x2); return true; });
        g.drawImage(shot[0], x, y, null);
        Turmbild.label(g, label, x, y);
        log.append(label).append(String.format(Locale.GERMAN, " · Kamera %s, Auge %.1f/%.1f/%.1f, Blick %.2f/%.2f/%.2f", r.camera.mode, r.camera.eye[0], r.camera.eye[1], r.camera.eye[2], r.camera.fwd[0], r.camera.fwd[1], r.camera.fwd[2])).append('\n').append("   ").append(r.lifeLine()).append('\n').append("   ").append(r.fogLine()).append('\n');
    }
}
