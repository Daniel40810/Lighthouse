package com.dan.lighthouse.wiring;

/**
 * Wind: Stärke, Richtung, Böigkeit. Gibt: {@code weather} (Phase 12). Nimmt: ocean, spray, cloud, rain, wetglass,
 * fog, flock, soundscape. Bis Phase 12 setzt die App ihn von Hand.
 */
public interface Wind {
    /** Mittlere Geschwindigkeit in m/s, 10 m über dem Wasser. */
    double speed();

    /** Richtung, aus der der Wind kommt, in Grad von Nord im Uhrzeigersinn (wie im Seewetterbericht). */
    double fromDeg();

    /** Böigkeit 0..1 (Anteil, um den Böen über dem Mittel liegen). */
    default double gustiness() { return 0.2; }

    /** Windstärke nach Beaufort (0..12) aus der Geschwindigkeit, WMO-Grenzen. */
    default int beaufort() { return Beaufort.of(speed()); }

    /** Wohin er weht, als Einheitsvektor der Szene (x Ost, z Süd). */
    default double[] toVector() {
        double a = Math.toRadians(fromDeg() + 180);
        return new double[]{Math.sin(a), 0, -Math.cos(a)};
    }
}
