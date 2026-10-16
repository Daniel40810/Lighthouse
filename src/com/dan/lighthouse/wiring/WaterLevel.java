package com.dan.lighthouse.wiring;

/** Wasserstand in m über mittlerem Meeresspiegel. Gibt: Gezeiten (FMoon) über die App. Nimmt: ocean, weathering, buoyancy, Kamera. */
@FunctionalInterface
public interface WaterLevel {
    double level();
}
