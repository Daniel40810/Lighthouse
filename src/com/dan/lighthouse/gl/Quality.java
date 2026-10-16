package com.dan.lighthouse.gl;

import com.dan.cloud.Clouds;
import com.dan.ocean.Ocean;
import com.dan.weathering.Weathering;
import com.dan.rain.Rain;
import com.dan.spray.Spray;
import com.dan.wetglass.Wetglass;
import com.dan.beacon.Beacon;
import com.dan.lightning.Lightning;
import com.dan.fog.Fog;
import com.dan.buoyancy.Buoyancy;
import com.dan.flock.Flock;
import com.dan.soundscape.Soundscape;

import java.util.Locale;

/**
 * Qualitätsstufen der App. Jede Package-Phase hängt ihre eigenen Werte an (Proben, Partikel, Kartengrößen); in
 * Phase 2 regelt die Stufe die Rechenauflösung und das Meer (FFT-Größe, Kacheln), ab Phase 5 auch Turmschatten,
 * Nieten und die Nässekarte der Verwitterung. Die Packages haben
 * ihre eigene Kopie dieser Aufzählung (Paketvertrag Punkt 7); die App-Stufe nennt die Stufe jedes Packages.
 * Ziel (F16): „Mittel“ hält 60 Bilder/s in Full HD, „Hoch“ mindestens 30.
 */
public enum Quality {
    NIEDRIG("Niedrig", 0.67, Ocean.Quality.LOW, Clouds.Quality.FLAT, 1024, false, Weathering.Quality.LOW, Spray.Quality.LOW, Rain.Quality.LOW, Wetglass.Quality.LOW, Beacon.Quality.LOW, Lightning.Quality.LOW, Fog.Quality.LOW, Buoyancy.Quality.LOW, Flock.Quality.LOW, Soundscape.Quality.LOW),
    MITTEL("Mittel", 0.85, Ocean.Quality.MEDIUM, Clouds.Quality.MEDIUM, 2048, true, Weathering.Quality.MEDIUM, Spray.Quality.MEDIUM, Rain.Quality.MEDIUM, Wetglass.Quality.MEDIUM, Beacon.Quality.MEDIUM, Lightning.Quality.MEDIUM, Fog.Quality.MEDIUM, Buoyancy.Quality.MEDIUM, Flock.Quality.MEDIUM, Soundscape.Quality.MEDIUM),
    HOCH("Hoch", 1.0, Ocean.Quality.HIGH, Clouds.Quality.HIGH, 2048, true, Weathering.Quality.HIGH, Spray.Quality.HIGH, Rain.Quality.HIGH, Wetglass.Quality.HIGH, Beacon.Quality.HIGH, Lightning.Quality.HIGH, Fog.Quality.HIGH, Buoyancy.Quality.HIGH, Flock.Quality.HIGH, Soundscape.Quality.HIGH),
    ULTRA("Ultra", 1.0, Ocean.Quality.ULTRA, Clouds.Quality.ULTRA, 4096, true, Weathering.Quality.ULTRA, Spray.Quality.ULTRA, Rain.Quality.ULTRA, Wetglass.Quality.ULTRA, Beacon.Quality.ULTRA, Lightning.Quality.ULTRA, Fog.Quality.ULTRA, Buoyancy.Quality.ULTRA, Flock.Quality.ULTRA, Soundscape.Quality.ULTRA);

    public final String label;
    /** Anteil der Bildgröße, in dem gerechnet wird. */
    public final double renderScale;
    /** Stufe des Meeres (FFT-Größe und Zahl der Kacheln, Phase 2). */
    public final Ocean.Quality ocean;
    /** Stufe der Wolken (Phase 4): Niedrig nur 2.5D, Mittel viertel Auflösung, Hoch und Ultra halbe. */
    public final Clouds.Quality clouds;
    /** Phase 5: Kantenlänge der Schattenkarte des Turms, Nieten und Plattenstöße an/aus, Stufe der Verwitterung. */
    public final int towerShadow;
    public final boolean towerDetail;
    public final Weathering.Quality weathering;
    /** Phase 6: Tropfen je Bündel und höchstens gleichzeitige Bündel der Gischt. */
    public final Spray.Quality spray;
    /** Phase 7: Stufe des Regens. */
    public final Rain.Quality rain;
    /** Phase 8: Stufe der Tropfen auf den Scheiben. */
    public final Wetglass.Quality wetglass;
    /** Phase 9: Stufe des Leuchtfeuers (Schritte im Strahl, Rundumbild, Streugitter). */
    public final Beacon.Quality beacon;
    /** Phase 10: Stufe der Blitze (Länge der Stufen, Ordnung der Äste, Stücke je Blitz). */
    public final Lightning.Quality lightning;
    /** Phase 13: Stufe des Seenebels (Schritte, Karte), der Schwimmkörper (Rechenrate, Schiffe) und der Möwen (Zahl). */
    public final Fog.Quality fog;
    public final Buoyancy.Quality buoyancy;
    public final Flock.Quality flock;
    /** Phase 14: Stufe des Klangs (Abtastrate, Stimmen, Äolskörper, Tropfen, Hall). */
    public final Soundscape.Quality sound;

    Quality(String label, double renderScale, Ocean.Quality ocean, Clouds.Quality clouds, int towerShadow, boolean towerDetail, Weathering.Quality weathering, Spray.Quality spray, Rain.Quality rain, Wetglass.Quality wetglass, Beacon.Quality beacon, Lightning.Quality lightning, Fog.Quality fog, Buoyancy.Quality buoyancy, Flock.Quality flock, Soundscape.Quality sound) {
        this.label = label;
        this.renderScale = renderScale;
        this.ocean = ocean;
        this.clouds = clouds;
        this.towerShadow = towerShadow;
        this.towerDetail = towerDetail;
        this.weathering = weathering;
        this.spray = spray;
        this.rain = rain;
        this.wetglass = wetglass;
        this.beacon = beacon;
        this.lightning = lightning;
        this.fog = fog;
        this.buoyancy = buoyancy;
        this.flock = flock;
        this.sound = sound;
    }

    /** Was die Stufe einstellt, als eine Zeile (Bedienfeld, README). */
    public String summary() {
        return String.format(Locale.GERMAN, "%s: %d %% der Auflösung, Meer %d × %d², Wolken %s, Turmschatten %d²%s", label, Math.round(renderScale * 100), ocean.cascades, ocean.n,
                clouds == Clouds.Quality.FLAT ? "2.5D" : "1/" + clouds.divisor + " mit bis zu " + clouds.maxSteps + " Schritten", towerShadow,
                towerDetail ? ", Nieten" : "") + String.format(Locale.GERMAN, ", Gischt %d Tropfen je Schwall, Regen %,d Tropfen, Scheiben %,d Tropfen, Strahl %d Schritte, Blitz bis %,d Stücke", spray.splash, rain.drops, wetglass.maxDrops, beacon.beamSteps, lightning.maxSegments)
                + String.format(Locale.GERMAN, ", Nebel %d Schritte, %d Möwen, Klang %.1f kHz", fog.steps, flock.birds, sound.sampleRate / 1000.0);
    }

    @Override public String toString() { return label; }
}
