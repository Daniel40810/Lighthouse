package com.dan.lighthouse.wiring;

/** Ein Blitz. Gibt: {@code lightning} (Phase 10). Nimmt: cloud, ocean, wetglass, weathering, sky, soundscape. */
@FunctionalInterface
public interface Flash {
    /** Ort des Kanals (Mitte), Stärke 0..1, Szenenzeit in s, Dauer in s. */
    void flash(double x, double y, double z, double strength, double time, double duration);
}
