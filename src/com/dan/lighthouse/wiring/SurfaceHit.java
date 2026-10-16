package com.dan.lighthouse.wiring;

/**
 * Ein Einschlag auf einer Fläche: Regentropfen, Gischt. Gibt: rain, spray. Nimmt: {@code ocean.addImpulse},
 * {@code wetglass.hit}, {@code weathering.wet}. Die App verteilt nach dem, was getroffen wurde.
 */
@FunctionalInterface
public interface SurfaceHit {
    enum Kind { TROPFEN, GISCHT, BRECHER }

    /** Ort, Masse in kg, Geschwindigkeit in m/s, Art. */
    void hit(double x, double y, double z, double massKg, double vx, double vy, double vz, Kind kind);
}
