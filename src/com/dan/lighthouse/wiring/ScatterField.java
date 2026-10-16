package com.dan.lighthouse.wiring;

/**
 * Streudichte der Luft am Ort: Regen, Nebel, Gischt. Gibt: rain, fog, spray. Nimmt: {@code beacon} (sichtbarer
 * Strahl nur im Streumedium). Die App addiert die Felder aller Geber.
 */
@FunctionalInterface
public interface ScatterField {
    /** Streukoeffizient in 1/m am Ort (x, y, z). */
    double density(double x, double y, double z);
}
