package com.dan.lighthouse.ui;

import com.dan.fstyle.FTheme;

import java.awt.Color;

/**
 * Die Farben der Oberfläche aus FTheme (Grundsatz: Bedienelemente in FTheme-Farben, die Szene hat ihren eigenen
 * Look). Liefert das Thema keine dunklen Farben, nimmt die App ihre Nordseenacht, damit der Rand um die Szene nicht
 * hell aufleuchtet. Alle Swing-Flächen sind deckend und haben eine ausdrückliche Hintergrundfarbe (Lehre aus RayPhong).
 */
public final class Theme {
    private Theme() { }

    public static final Color NACHT = new Color(11, 17, 24), ROT = new Color(226, 67, 47), GISCHT = new Color(215, 227, 234),
            MESSING = new Color(217, 180, 90), SEE = new Color(63, 178, 168);

    public static Color bg() { return pick(FTheme::getBackground, NACHT, Role.DARK); }
    public static Color surface() { return pick(FTheme::getSurface, new Color(17, 26, 36), Role.DARK); }
    public static Color ink() { return pick(FTheme::getText, new Color(230, 234, 238), Role.LIGHT); }
    public static Color muted() { return pick(FTheme::getTextMuted, new Color(147, 161, 175), Role.LIGHT); }
    public static Color accent() { return pick(FTheme::getAccent, ROT, Role.ANY); }
    public static Color status() { return new Color(7, 11, 16); }

    private enum Role { DARK, LIGHT, ANY }

    private static Color pick(java.util.function.Function<FTheme, Color> f, Color fallback, Role role) {
        try {
            Color c = f.apply(FTheme.getInstance());
            if (c == null) return fallback;
            int lum = (c.getRed() * 3 + c.getGreen() * 6 + c.getBlue()) / 10;
            if (role == Role.DARK && lum > 70) return fallback;     // helles Thema: Rand um die Szene bleibt dunkel
            if (role == Role.LIGHT && lum < 120) return fallback;   // Schrift muss auf Dunkel lesbar bleiben
            return c;
        } catch (RuntimeException | Error e) {
            return fallback;
        }
    }
}
