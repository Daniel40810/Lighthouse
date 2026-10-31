package com.dan.lighthouse.scene;

import com.dan.cloth.Cloth;
import com.dan.lamp.Lamp;
import com.dan.lamp.Lamps;

import java.util.Locale;

/**
 * Rückfluss aus Timmendorf (Phase 18, F29 A): die Flagge am Mast ({@code com.dan.cloth}) und die Leuchten außen am Turm
 * ({@code com.dan.lamp}). Der Rote Sand selbst hatte nur das Feuer und das Licht der Räume; die Leuchten sind gestaltet [G].
 * <ul>
 * <li><b>Arbeitsleuchte über der Tür</b> (Azimut {@value Turm#DOOR_AZ}°, 2,3 m über dem Podest): LED 4 000 K, nach unten,
 * 1 500 lm – sie leuchtet Tür, Podest, Leiter, Manschette und die See am Fuß aus.</li>
 * <li><b>Hindernisfeuer</b> an der Spitze des Masts: rot, rundum, rund 32 cd [G nach ICAO Anhang 14, Typ B: mindestens 32 cd].</li>
 * </ul>
 * Alle schalten mit dem Dämmerungsschalter aus {@code lamp} (an unter 35 lx, aus über 70 lx), das Licht fällt im Turm-Shader
 * über {@code lamp.glsl} auf Mauer und Stahl (ohne Schatten) und als Punktlicht auf die See.
 * <p>
 * <b>Flagge:</b> Bundesflagge 1,2 × 0,72 m (3 : 5) am Mast unter dem Querträger, Tuch nach XPBD, Wind 10 m über dem
 * Wasser mit dem Höhenprofil der See (Exponent 0,11) und Böen nach der Böigkeit [G].
 */
public final class Aussenlicht {
    public final Lamps lampen = new Lamps();
    public final Lamp tuer, hindernis;
    public final Cloth flagge;
    /** Schwarz, Rot, Gold (linear) [G nach den Farbangaben der Bundesregierung]. */
    public static final float[] DEUTSCHLAND = {0.010f, 0.010f, 0.010f, 0.62f, 0.012f, 0.010f, 0.88f, 0.60f, 0.0f};
    /** Fuß des Masts (x, z) und Höhe der Mastspitze. */
    public final double mastX, mastZ;
    private double windSpeed = 7, windFrom = 240, gust = 0.2, time;

    public Aussenlicht() {
        // Tür: außen am Schaft, etwas über dem Sturz
        double a = Math.toRadians(Turm.DOOR_AZ), r = Turm.SHAFT_R0 + 0.25;
        tuer = lampen.add(new Lamp(Math.sin(a) * r, Turm.SOCKEL_TOP + 2.3, -Math.cos(a) * r, Lamp.Type.LED_4000K, Lamp.Distribution.DOWN, 1500).facing(Turm.DOOR_AZ));
        tuer.area = 0.03;
        double m = Math.toRadians(Turm.MAST_AZ), rm = Turm.GALLERY_R - 0.35;
        mastX = Math.sin(m) * rm;
        mastZ = -Math.cos(m) * rm;
        hindernis = lampen.add(Lamp.point(mastX, Turm.MAST_TOP + 0.12, mastZ, 32, 1.0, 0.03, 0.02));
        hindernis.onLux = 35;   // Lamp.point ist sonst immer an: das Hindernisfeuer brennt nur bei Dunkelheit
        hindernis.offLux = 70;
        hindernis.area = 0.005;
        flagge = Cloth.flag(mastX, Turm.MAST_TOP - 1.15, mastZ, 1.2, 0.72);
        flagge.colors = DEUTSCHLAND.clone();
        flagge.seed = 1885;
        flagge.wind = (x, y, z, t, o) -> {
            double u = windSpeed * Math.pow(Math.max(1, y) / 10, 0.11) * (1 + gust * (0.6 * Math.sin(t * 0.9 + 0.3 * x) + 0.4 * Math.sin(t * 2.3 + z)));
            double to = Math.toRadians(windFrom + 180 + 6 * Math.sin(t * 0.41));
            o[0] = u * Math.sin(to);
            o[1] = 0;
            o[2] = -u * Math.cos(to);
        };
        flagge.ground = (x, z) -> Turm.GALLERY_TOP + 0.05;   // tiefer als die Galerie fällt das Tuch nie
    }

    /** Tuch weiter (Wind 10 m über dem Wasser, Richtung, Böigkeit, Nässe 0..1). */
    public void schritt(double dt, double ws, double fromDeg, double gustiness, double nass) {
        if (dt <= 0) return;
        windSpeed = ws;
        windFrom = fromDeg;
        gust = gustiness;
        time += dt;
        flagge.wetness = nass;
        flagge.step(dt);
    }

    /** Dämmerungsschalter (Umgebungslicht in lx, Uhrzeit). */
    public void schalten(double dt, double lux, double hour) { lampen.step(dt, lux, hour); }

    public boolean an() { return tuer.on(); }

    /** Wohin die freie Kante der Flagge zeigt (Grad von Nord). */
    public double flaggeNach() {
        double[] tip = flagge.point(flagge.nu / 2, flagge.nv - 1), base = flagge.point(flagge.nu / 2, 0);
        return (Math.toDegrees(Math.atan2(tip[0] - base[0], -(tip[2] - base[2]))) % 360 + 360) % 360;
    }

    public String zeile() {
        return String.format(Locale.GERMAN, "Rückfluss aus Timmendorf: Flagge weht nach %03.0f° (Wind aus %03.0f°, %.1f m/s) · Leuchten %s: Tür %.0f lm, Hindernisfeuer %.0f cd",
                flaggeNach(), windFrom, windSpeed, an() ? "an" : "aus", tuer.lumen, hindernis.candelaFull(0, 1, 0));
    }
}
