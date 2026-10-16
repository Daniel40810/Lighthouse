package com.dan.lighthouse.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Das Programmsymbol. Phase 16 legt die PNGs aus {@code icon/lighthouse.ico} (mit Goldkante wie heidelberg.ico, F19) ins
 * Jar unter {@code com/dan/lighthouse/ui/icon/lighthouse_NN.png}. Bis dahin malt die App ein vorläufiges Symbol
 * selbst: der rot-weiße Turm vor Nachtblau mit Strahl und Goldkante.
 */
public final class AppIcon {
    public static final int[] SIZES = {16, 24, 32, 48, 64, 128, 256};

    private AppIcon() { }

    public static List<Image> images() {
        List<Image> l = new ArrayList<>();
        for (int s : SIZES) l.add(load(s));
        return l;
    }

    public static BufferedImage load(int size) {
        try (InputStream in = AppIcon.class.getResourceAsStream("icon/lighthouse_" + size + ".png")) {
            if (in != null) {
                BufferedImage b = javax.imageio.ImageIO.read(in);
                if (b != null) return b;
            }
        } catch (java.io.IOException e) { /* gemaltes Symbol */ }
        return paint(size);
    }

    /** Das vorläufige Symbol, in jeder Größe neu gemalt. */
    public static BufferedImage paint(int s) {
        BufferedImage im = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double r = s * 0.18, inset = Math.max(1, s * 0.035);
        RoundRectangle2D outer = new RoundRectangle2D.Double(0, 0, s, s, r * 2, r * 2);
        g.setPaint(new GradientPaint(0, 0, new Color(240, 210, 120), s, s, new Color(150, 105, 30)));
        g.fill(outer);
        RoundRectangle2D inner = new RoundRectangle2D.Double(inset, inset, s - 2 * inset, s - 2 * inset, r * 1.7, r * 1.7);
        g.setPaint(new GradientPaint(0, 0, new Color(16, 30, 58), 0, s, new Color(6, 10, 20)));
        g.fill(inner);
        g.setClip(inner);
        double cx = s * 0.5, top = s * 0.24, base = s * 0.80, wTop = s * 0.11, wBase = s * 0.17;
        // Strahl
        Path2D beam = new Path2D.Double();
        beam.moveTo(cx, top + s * 0.06);
        beam.lineTo(s * 1.05, top - s * 0.10);
        beam.lineTo(s * 1.05, top + s * 0.20);
        beam.closePath();
        g.setPaint(new GradientPaint((float) cx, 0, new Color(255, 236, 180, 200), s, 0, new Color(255, 236, 180, 0)));
        g.fill(beam);
        // Schaft in Bändern
        int bands = 5;
        for (int i = 0; i < bands; i++) {
            double y0 = top + s * 0.08 + (base - top - s * 0.08) * i / bands, y1 = top + s * 0.08 + (base - top - s * 0.08) * (i + 1) / bands;
            double w0 = wTop + (wBase - wTop) * (y0 - top) / (base - top), w1 = wTop + (wBase - wTop) * (y1 - top) / (base - top);
            Path2D p = new Path2D.Double();
            p.moveTo(cx - w0, y0); p.lineTo(cx + w0, y0); p.lineTo(cx + w1, y1); p.lineTo(cx - w1, y1); p.closePath();
            g.setColor(i == bands - 1 ? new Color(25, 25, 30) : i % 2 == 0 ? new Color(236, 232, 224) : new Color(214, 54, 38));
            g.fill(p);
        }
        // Laterne und Kuppel
        g.setColor(new Color(255, 230, 160));
        g.fill(new java.awt.geom.Rectangle2D.Double(cx - wTop * 0.75, top, wTop * 1.5, s * 0.08));
        g.setColor(new Color(20, 22, 28));
        g.fill(new java.awt.geom.Arc2D.Double(cx - wTop * 0.85, top - s * 0.07, wTop * 1.7, s * 0.14, 0, 180, java.awt.geom.Arc2D.CHORD));
        // See
        g.setPaint(new GradientPaint(0, (float) base, new Color(20, 70, 80), 0, s, new Color(8, 30, 40)));
        g.fill(new java.awt.geom.Rectangle2D.Double(0, base - s * 0.02, s, s));
        if (s >= 32) {
            g.setColor(new Color(215, 227, 234, 170));
            g.setStroke(new BasicStroke((float) Math.max(1, s * 0.02)));
            g.draw(new java.awt.geom.Line2D.Double(cx - wBase * 1.6, base + s * 0.015, cx + wBase * 1.6, base + s * 0.015));
        }
        g.dispose();
        return im;
    }

    /** Setzt das Symbol am Fenster und, wo unterstützt, in der Taskleiste. */
    public static void install(java.awt.Window w) {
        List<Image> l = images();
        w.setIconImages(l);
        try {
            if (java.awt.Taskbar.isTaskbarSupported()) {
                java.awt.Taskbar tb = java.awt.Taskbar.getTaskbar();
                if (tb.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) tb.setIconImage(l.get(l.size() - 1));
            }
        } catch (Exception | Error ignored) {
            // Windows nimmt ohnehin die Fenstersymbole
        }
    }
}
