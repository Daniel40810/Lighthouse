package com.dan.lighthouse.tools;

import com.dan.lighthouse.app.LighthouseApp;
import com.dan.lighthouse.db.Daten;
import com.dan.lighthouse.db.DateiSpeicher;
import com.dan.lighthouse.db.Felder;
import com.dan.lighthouse.db.Zustand;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.ui.ControlPanel;
import com.dan.lighthouse.ui.Startbild;
import com.dan.lighthouse.wiring.Wiring;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;

/**
 * Statusbild für Phase 15 mit der ganzen App im Fenster (braucht einen Bildschirm): das Startbild mit dem Stand der
 * Daten und „Weiter wie zuletzt“, die Szene mit dem Feuer von Kiel und dem Nachbarn Bülk in der Nacht, das Bedienfeld
 * mit den Blöcken LEUCHTFEUER, KAMERA (gemerkte Ansicht mit Vorschau) und DATEN. Ohne Datenbank: gespeichert wird in
 * einen eigenen Ordner, der vorher eine abgebrochene Sitzung bekommt.
 * <pre>
 * Datenbild [datei.png]
 * </pre>
 */
public final class Datenbild {
    private Datenbild() { }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Status_Phase15_claude.png");
        File dir = Files.createTempDirectory("lighthouse-datenbild").toFile();
        System.setProperty("lighthouse.daten.dir", dir.getPath());
        System.setProperty("lighthouse.sound", "false");
        System.setProperty("lighthouse.db.dir", new File("db_gibt_es_nicht").getAbsolutePath());
        // eine abgebrochene Sitzung von gestern: Sturmabend am Roten Sand
        DateiSpeicher ds = new DateiSpeicher(dir);
        SceneClock c0 = new SceneClock(LocalDate.of(2026, 10, 15), 19.5);
        Renderer r0 = new Renderer(c0, new Wiring(c0));
        r0.wiring.weather.startFree(c0.epochMillis() / 1000.0, com.dan.weather.Weather.Phase.STURM);
        long sid = ds.sitzungBeginnen(Renderer.VERSION, "Prüfstand", "HOCH");
        ds.sitzungSichern(sid, Felder.alsText(Zustand.erfassen(r0, true), null), (long) c0.epochMillis(), "Roter Sand", null, Double.NaN);

        Robot robot = new Robot();
        LighthouseApp.main(new String[0]);
        // 1 das Startbild
        Window splash = null;
        for (int i = 0; i < 200 && splash == null; i++) {
            Thread.sleep(50);
            for (Window w : Window.getWindows()) if (w instanceof Startbild && w.isShowing()) splash = w;
        }
        for (int i = 0; i < 300 && (Startbild.lage() == Startbild.Lage.PRUEFT || Startbild.stand() == null); i++) Thread.sleep(50);
        Thread.sleep(500);
        BufferedImage start = splash != null && splash.isShowing() ? robot.createScreenCapture(splash.getBounds()) : null;
        if (start == null) System.err.println("Startbild nicht mehr zu sehen");
        while (LighthouseApp.controls == null) Thread.sleep(200);
        ControlPanel cp = LighthouseApp.controls;
        Renderer r = LighthouseApp.scene.renderer();
        Thread.sleep(3000);
        // 2 Hohe Weg bei Nacht, Blick nach Nordnordwest auf Tegeler Plate, Alte Weser und Roter Sand
        SwingUtilities.invokeAndWait(() -> {
            cp.daten().turmWaehlen("Hohe Weg");
            r.clock.lapse = 0;
            r.clock.set(LocalDate.of(2026, 10, 16), 22.0);
            r.showHud = false;
            r.camera.place(-88, 18, -180, 334, -0.2);
            r.camera.snap();
            cp.oeffnen("LEUCHTFEUER");
        });
        Thread.sleep(25000);
        Rectangle fb = LighthouseApp.frame.getBounds();
        BufferedImage szene = robot.createScreenCapture(fb);
        SwingUtilities.invokeAndWait(() -> cp.daten().ansichtMerken());
        Thread.sleep(10000);
        SwingUtilities.invokeAndWait(() -> { cp.daten().listenNeu(); cp.oeffnen("KAMERA"); });
        Thread.sleep(8000);
        BufferedImage kamera = robot.createScreenCapture(fb);
        SwingUtilities.invokeAndWait(() -> cp.oeffnen("DATEN"));
        Thread.sleep(5000);
        BufferedImage daten = robot.createScreenCapture(fb);

        // zusammensetzen: oben das Fenster, unten das Startbild und dreimal das Bedienfeld
        int W = 1800;
        double s = W / (double) szene.getWidth();
        int kh = (int) (szene.getHeight() * s);
        int pw = 336, top = 40, phh = Math.min(szene.getHeight() - top - 40, 1000);
        double ps = 0.82;
        int ph = (int) (phh * ps);
        BufferedImage img = new BufferedImage(W, kh + ph + 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(24, 28, 34));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.drawImage(szene, 0, 0, W, kh, null);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(new Color(235, 228, 210));
        g.drawString("Lighthouse · Phase 15 · Oberfläche und Daten (hier ohne Datenbank: Dateien in daten/)", 12, kh + 26);
        int y = kh + 44;
        int x = 12;
        if (start != null) {
            double ss = Math.min(1.0, 760.0 / start.getWidth());
            g.drawImage(start, x, y, (int) (start.getWidth() * ss), (int) (start.getHeight() * ss), null);
        }
        int px = szene.getWidth() - pw - 8;
        BufferedImage[] p3 = {szene, kamera, daten};
        for (int i = 0; i < 3; i++) {
            BufferedImage sub = p3[i].getSubimage(px, top, pw, phh);
            g.drawImage(sub, W - (3 - i) * ((int) (pw * ps) + 10), y, (int) (pw * ps), ph, null);
        }
        g.dispose();
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("Statusbild: " + out + " · " + r.siteName + " · " + r.neighbours.length + " Nachbarn · " + r.beaconLine());
        System.exit(0);
    }
}
