package com.dan.lighthouse.wiring;

/** Das Leuchtfeuer als Lichtquelle. Gibt: {@code beacon} (Phase 9). Nimmt: cloud, rain, spray, fog, ocean, wetglass. */
public interface SpotLight {
    double[] position();

    /** Mittelachse des Strahls (Einheitsvektor) und halber Öffnungswinkel in Grad; 180 = Rundumlicht. */
    double[] direction();
    double halfAngleDeg();

    /** Farbe (linear, RGB) und Lichtstärke in Candela, beide in diesem Augenblick (Kennung eingerechnet). */
    double[] color();
    double candela();
}
