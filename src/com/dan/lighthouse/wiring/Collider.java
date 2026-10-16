package com.dan.lighthouse.wiring;

/**
 * Ein fester Körper im Wasser oder in der Luft: Turm, Sockel, Schiff. Gibt: die App. Nimmt: spray, ocean
 * (Brandungsring), rain. Beschrieben als vorzeichenbehafteter Abstand: negativ innen, positiv außen.
 */
@FunctionalInterface
public interface Collider {
    double distance(double x, double y, double z);

    /** Senkrechter Zylinder um (cx, cz) mit Radius r von y0 bis y1. */
    static Collider cylinder(double cx, double cz, double r, double y0, double y1) {
        return (x, y, z) -> {
            double dr = Math.hypot(x - cx, z - cz) - r;
            double dy = Math.max(y0 - y, y - y1);
            if (dr <= 0 && dy <= 0) return Math.max(dr, dy);
            return Math.hypot(Math.max(dr, 0), Math.max(dy, 0));
        };
    }
}
