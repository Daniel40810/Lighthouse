package com.dan.lighthouse.wiring;

import com.dan.cloud.CloudType;
import com.dan.lighthouse.scene.Tide;
import com.dan.lighthouse.scene.Innenraum;
import com.dan.weather.Weather;
import com.dan.lighthouse.scene.Turm;
import com.dan.cloud.Clouds;
import com.dan.ocean.Ocean;
import com.dan.rain.Rain;
import com.dan.sky.Sky;
import com.dan.spray.Body;
import com.dan.spray.Spray;
import com.dan.weathering.Weathering;
import com.dan.wetglass.Wetglass;
import com.dan.fog.Fog;
import com.dan.fog.Signal;
import com.dan.buoyancy.Buoyancy;
import com.dan.buoyancy.Ship;
import com.dan.flock.Flock;
import com.dan.soundscape.Aeolian;
import com.dan.soundscape.Hum;
import com.dan.soundscape.Room;
import com.dan.soundscape.Soundscape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Die Verdrahtung der App (Paketvertrag Punkt 4): Hier und nur hier reden die Packages miteinander. Jedes Package
 * bietet Eingänge an und meldet Ereignisse über die kleinen Schnittstellen dieses Pakets; die Phasen 2 bis 14
 * hängen ihre Verbindungen hier ein. Wo ein Package eine eigene Kopie einer Schnittstelle mitbringt (gleicher Name,
 * gleiche Form), übersetzt {@code wiring} mit einer Zeile.
 * <p>
 * Phase 1: Uhr, Hand-Wind und fester Wasserstand. Phase 2: das Meer ({@code com.dan.ocean}) bekommt Wind und
 * Wasserstand, der App-Himmel spiegelt sich darin, und die Kamera fragt die Welle unter sich. Phase 3: der Himmel
 * ({@code com.dan.sky}) bekommt Zeit, Ort, Augenhöhe, Sicht und Bedeckung und gibt das Licht für Meer, Turm und
 * Belichtung. Phase 4: die Wolken. Phase 5: Gezeiten, Turm als Hindernis und Schattenwerfer, Verwitterung. Phase 6:
 * Gischt an Sockel, Schaft und Peilpfahl; sie macht die Wand nass und wirft Ringwellen. Phase 7: Regen, nur unter
 * den dichten Stellen der Wolken; er macht den Turm nass und legt Ringe aufs Meer. Phase 8: Tropfen auf den Scheiben
 * des Laternenraums. Phase 9: das Leuchtfeuer, sein Strahl im Regen und in der Gischt, sein Glitzer auf der See. Phase 10:
 * Gewitter unter den Wolken; Blitze schlagen ins Wasser oder in den Mast, leuchten in Wolken, Meer, Turm und Scheiben,
 * und der Donner kommt mit seiner Laufzeit. Phase 11: das Innere des Turms, Licht durch die Fenster, Barometer nach dem
 * Wind, kein Regen drinnen, die Fahrt „Aufstieg“. Phase 12: die Wetterregie ({@code com.dan.weather}) setzt alle
 * Eingänge, die bis dahin von Hand kamen; Handregler überschreiben einzelne Größen. Phase 13: Seenebel aus der Regie
 * ({@code com.dan.fog}) mit Nebelhorn am Turm, Tonnen und ein Schiff auf dem Meer ({@code com.dan.buoyancy}), Möwen
 * um den Turm ({@code com.dan.flock}). Die Liste {@link #links()} zeigt, was verbunden ist, und landet im
 * Bedienfeld und in der Prüfung.
 */
public final class Wiring {
    /** Eine Verbindung: von wem, an wen, über welche Schnittstelle, seit welcher Phase. */
    public record Link(String from, String to, String via, int phase) {
        @Override public String toString() { return from + " → " + to + " (" + via + ", Phase " + phase + ")"; }
    }

    /** Der Wind, wie ihn die Wetterregie (Phase 12) setzt; Handregler gehen über {@link #hand}. */
    public static final class ManualWind implements Wind {
        public volatile double speed = Beaufort.mid(4), fromDeg = 240, gust = 0.2;
        @Override public double speed() { return speed; }
        @Override public double fromDeg() { return fromDeg; }
        @Override public double gustiness() { return gust; }
    }

    public final Clock clock;
    public final ManualWind wind = new ManualWind();
    /**
     * Phase 12: die Wetterregie ({@code com.dan.weather}). Sie setzt je Bild Wind, Böen, Bedeckung, Wolkenart, hohe
     * Schicht, Regen, Sicht, Gewitter, Temperatur, Luftdruck und Dünung; Handregler überschreiben einzelne Größen.
     */
    public final Weather weather = new Weather();
    /**
     * Phase 12: Ob die Regie die Eingänge setzt. Die App schaltet sie ein; Standbild-Werkzeuge und Prüfungen lassen sie
     * aus und setzen die Felder selbst wie bisher.
     */
    public volatile boolean regie;
    /** Phase 12: Wind in der laufenden Böe (m/s), Luftdruck (hPa) und Tendenz (hPa je 3 h) für Barometer und HUD. */
    public volatile double gustNow = Beaufort.mid(4), pressureHPa = 1013, pressureTendency;
    /** Phase 12: Dünung von Hand (Periode, Richtung); NaN = aus der Regie. */
    public volatile double swellPeriodHand = Double.NaN, swellDirHand = Double.NaN;
    /** Phase 12: Dünung aus der Regie (Höhe, Periode, Richtung), vor dem Meer gerundet. */
    public volatile double swellHeight = 0.6, swellPeriod = 9.5, swellFromDeg = 290;
    /** Phase 12: Regenbogen von Hand (0..1); negativ = aus der Regie (nach einem Schauer mit tiefer Sonne). */
    public volatile double rainbowHand = -1;
    /** Phase 12: Lage für einen Regenbogen nach einem Schauer (0..1), aus der Regie. */
    public volatile double showerEnd;
    /** Phase 5: die Gezeiten am Roten Sand (Mond und Sonne aus dem FMoon-Kern); abschaltbar auf einen festen Stand. */
    public final Tide tide;
    /** Wasserstand; seit Phase 5 die Gezeit. */
    public volatile WaterLevel waterLevel;
    /** Sicht in km von Hand, bis die Wetterregie (Phase 12) übernimmt. */
    public volatile double visibilityKm = 40;
    /** Phase 7: Regen von Hand in mm/h unter den dichtesten Wolken (0 = trocken), bis die Wetterregie übernimmt. */
    public volatile double rainRate = 0;
    /** Phase 4: Wolken von Hand – Bedeckung in Achteln, Art, hohe Schicht 0..1. */
    public volatile double oktas = 3, highCover = 0;
    public volatile CloudType cloudType = CloudType.CUMULUS;
    /** Phase 10: Stärke des Gewitters von Hand, bis die Wetterregie (Phase 12) übernimmt. */
    public volatile com.dan.lightning.Lightning.Level thunderstorm = com.dan.lightning.Lightning.Level.OFF;
    /** Phase 10: Donner, der das Ohr erreicht (für {@code com.dan.soundscape} ab Phase 14; bis dahin HUD). */
    public volatile SoundEvent sound;
    /** Zeichnet die App echte Wolken? Dann bekommt der Himmel keinen Schleier mehr. */
    public volatile boolean cloudsDrawn;
    private final List<Link> links = new ArrayList<>();

    public Wiring(Clock clock) {
        this.clock = clock;
        this.tide = new Tide(clock::epochMillis);
        this.waterLevel = tide;
        link("App (SceneClock)", "Renderer, Kamera, HUD", "Clock", 1);
        link("App (Hand-Wind)", "HUD", "Wind", 1);
        link("App (Wasserstand)", "Kamera, Platzhalter-See", "WaterLevel", 1);
        link("Wiring (Hand-Wind)", "com.dan.ocean", "Wind → windSpeed, windFromDeg", 2);
        link("App (Wasserstand)", "com.dan.ocean", "WaterLevel → waterLevel", 2);
        link("com.dan.ocean", "Kamera", "heightAt → Mindesthöhe über der Welle", 2);
        link("App (SceneClock)", "com.dan.sky", "Clock → epochMillis, Breite, Länge", 3);
        link("Kamera", "com.dan.sky", "Augenhöhe → eyeHeight (Kimm, Dunstweg)", 3);
        link("Wiring (Hand-Sicht)", "com.dan.sky", "visibilityKm (bis Phase 12)", 3);
        link("com.dan.sky", "com.dan.ocean.gl", "Lighting → Light (Sonne, Mond, Rundumbild 256 × 128, Dunst)", 3);
        link("com.dan.sky", "Turm, Platzhalter-See", "Lighting → Sonne, Mond, Rundumbild, Dunst", 3);
        link("com.dan.sky", "Nachbearbeitung", "Anpassungsleuchtdichte, Stäbchensehen", 3);
        link("Wiring (Hand-Wolken)", "com.dan.cloud", "Achtel, Wolkenart, hohe Schicht (bis Phase 12)", 4);
        link("Wiring (Hand-Wind)", "com.dan.cloud", "Wind → Zug in Wolkenhöhe (1,6-fach, 20° rechtsdrehend)", 4);
        link("com.dan.sky", "com.dan.cloud.gl", "Lighting → Licht in Wolkenhöhe, Himmels- und Meerlicht, Dunst", 4);
        link("com.dan.cloud.gl", "com.dan.ocean.gl", "Schattenkarte 256² über 24 km", 4);
        link("com.dan.cloud", "Turm, Belichtung", "Schatten am Turm, Helligkeit des bedeckten Himmels", 4);
        link("Gezeiten (FMoon-Kern)", "com.dan.ocean, com.dan.weathering, Kamera", "WaterLevel → ±1,5 m, Hochwasser 1 h vor Bremerhaven", 5);
        link("Turm", "com.dan.ocean", "Collider → Obstacle (Sockel r 5,25 m, bei Ebbe die ovale Manschette)", 5);
        link("Wiring (Wasserstand, Wind)", "com.dan.weathering", "WaterLevel, MHW/MNW ±1,45 m, Wind", 5);
        link("com.dan.ocean", "com.dan.weathering", "Hs → Auflauf und Spritzzone", 5);
        link("com.dan.cloud, com.dan.sky", "com.dan.weathering", "Sonne am Turm → Trocknung", 5);
        link("com.dan.weathering.gl", "Turm-Shader", "weathering.glsl, Nässekarte, Masken", 5);
        link("Turm", "com.dan.ocean.gl", "Schatten des Turms aufs Wasser (Körperschatten 1024²)", 5);
        link("com.dan.ocean", "com.dan.spray", "Sea ← heightAt, verticalVelocityAt, Wasserstand (Abtastung der Wände 20-mal je s)", 6);
        link("Turm", "com.dan.spray", "Collider → Body (Sockel, Schaft, Peilpfahl; bei Ebbe die Manschette)", 6);
        link("Wiring (Hand-Wind), com.dan.ocean", "com.dan.spray", "Wind, H_s", 6);
        link("com.dan.spray", "com.dan.weathering", "SurfaceHit → wet(u, v): Spritzwasser und Nebel machen die Wand nass", 6);
        link("com.dan.spray", "com.dan.ocean", "Wiedereintritt → addImpulse (Ringwellen)", 6);
        link("com.dan.sky, com.dan.cloud", "com.dan.spray.gl", "Lighting → Sonne, Mond, Himmel, Dunst (vorbelichtet)", 6);
        link("com.dan.spray", "(Phase 9: beacon, Phase 14: soundscape)", "ScatterField, SoundEvent", 6);
        link("Wiring (Hand-Regen, Hand-Wind)", "com.dan.rain", "Regen in mm/h, Wind, Böen (bis Phase 12)", 7);
        link("Wiring (Hand-Regen)", "com.dan.cloud", "Niederschlag 0..1 (dunklere Unterseiten, Regenfeld)", 7);
        link("com.dan.cloud", "com.dan.rain", "Curtain ← Clouds.rain (Regen nur unter dichten Stellen), Wolkenuntergrenze", 7);
        link("Gezeiten, Kamera", "com.dan.rain", "Wasserspiegel, Auge (Tropfenkasten, Karte 16 km)", 7);
        link("com.dan.rain", "com.dan.weathering", "Regen am Turm → rainRate (Wetterseite nass, Salz wird abgespült)", 7);
        link("com.dan.rain.gl", "com.dan.ocean.gl", "Ringkarte → detailTexture (Ringe auf dem Wasser um das Auge)", 7);
        link("com.dan.sky", "com.dan.rain.gl", "Lighting → Sonne, Mond, Himmel, Dunst (vorbelichtet)", 7);
        link("com.dan.rain", "(Phase 9: beacon, 14: soundscape)", "ScatterField, loudness", 7);
        link("com.dan.rain", "com.dan.wetglass", "expectedHits je Scheibe (Luv, Seite, Lee) → hitRate, Λ der Tropfengrößen", 8);
        link("Wiring (Hand-Wind)", "com.dan.wetglass", "Wind über der Scheibe → windX (schiebt die Tropfen)", 8);
        link("Turm (Laterne)", "com.dan.rain", "Unterstand: im Laternenraum regnet es nicht", 8);
        link("com.dan.wetglass.gl", "Scheiben-Shader", "wetglass.glsl, Karte, Feintropfen → Brechung, Tropfen kopfüber", 8);
        link("com.dan.sky", "Scheiben-Shader", "Sonne und Himmel → Glanz und Rand der Tropfen", 8);
        link("Wiring (Luft, Raum, Lampe)", "com.dan.wetglass", "Taupunkt im Raum gegen Glastemperatur → condensation (Beschlag innen)", 8);
        link("com.dan.spray", "com.dan.wetglass", "Seesalz im Wind und Sprühnebel an der Laterne → saltRate; Schwall über die Galerie → flood", 8);
        link("com.dan.rain", "com.dan.wetglass (Handlauf)", "Regen quer zum Rohr → rainAcross, Wind → Cylinder.wind", 8);
        link("com.dan.wetglass.gl", "Handlauf-Shader", "derselbe Baustein auf einem liegenden Zylinder (F10)", 8);
        link("com.dan.rain, com.dan.spray", "com.dan.beacon", "ScatterField → Scatter (Regen 3/km je Einheit, Gischt bis 0,08 /m, im Kugelgitter um das Feuer)", 9);
        link("Wiring (Hand-Sicht)", "com.dan.beacon", "Dunst 3/V → hazePerM (sichtbarer Strahl in klarer Luft)", 9);
        link("com.dan.sky", "com.dan.beacon", "Beleuchtungsstärke am Turm → Dämmerungsschalter (an unter 150 lx)", 9);
        link("com.dan.beacon.gl", "com.dan.ocean.gl", "Rundumbild der Lichtstärke → Punktlicht (Glitzer des Feuers auf der See)", 9);
        link("com.dan.beacon.gl", "Turm-Shader", "beacon.glsl: Linse von außen, Nahfeld im Laternenraum, Lampe", 9);
        link("com.dan.cloud.gl", "com.dan.beacon.gl", "Wolkenuntergrenze und Schattenkarte → der Strahl streift die Unterseite", 9);
        link("com.dan.beacon", "Wiring", "SpotLight (Ort, Richtung, Farbe, Candela in Blickrichtung)", 9);
        link("Turm (Alte Weser), Site (Tegeler Plate)", "com.dan.beacon.gl", "Nachbarfeuer als Lichtpunkte mit ihren Kennungen, hinter der Kimm verborgen", 9);
        link("Wiring (Hand-Gewitter)", "com.dan.lightning", "Stärke aus, Wetterleuchten, mäßig, kräftig, schwer (bis Phase 12)", 10);
        link("com.dan.cloud", "com.dan.lightning", "CellField ← Regenfeld und Bedeckung (Blitze unter den dichten Stellen), Wolkenuntergrenze", 10);
        link("com.dan.cloud", "com.dan.lightning", "Occlusion ← Bedeckung über dem Kanal (in Lücken sieht man ihn auch über der Untergrenze)", 10);
        link("Turm (Mast 30,7 m), Alte Weser", "com.dan.lightning", "Attractor: Fangpunkte über die Schlagweite", 10);
        link("Gezeiten, Kamera", "com.dan.lightning", "Wasserstand, Ohr und Auge (Donner, Sichtbarkeit)", 10);
        link("com.dan.lightning.gl", "com.dan.cloud.gl", "Flash → Lichtpunkte in der Wolke, beim Auflegen ohne zeitliches Nachziehen", 10);
        link("com.dan.lightning.gl", "com.dan.ocean.gl", "Flash → Punktlichter auf Wasser und Schaum; der Kanal im Rundumbild spiegelt sich", 10);
        link("com.dan.lightning.gl", "Turm-Shader, Scheiben-Shader", "lightning.glsl: Licht und Glanz, Salz und Beschlag leuchten auf", 10);
        link("com.dan.lightning", "com.dan.rain.gl, com.dan.spray.gl", "Flash → stärkstes Licht statt des Monds (Tropfen und Gischt blitzen auf)", 10);
        link("com.dan.lightning", "Belichtung", "Leuchtdichte im Blitz → Belichtung gibt nach, erholt sich in 0,25 s", 10);
        link("com.dan.lightning", "(Phase 14: soundscape), HUD", "SoundEvent DONNER mit Laufzeit (343 m/s), Pegel und Knall", 10);
        link("com.dan.sky", "Innenraum (Turm-Shader)", "Strahldichte vor jedem Bullauge und Bogenfenster, Sonne durch die Laibungen (Sonnenflecken)", 11);
        link("Innenraum", "Belichtung", "mittlere Leuchtdichte von Schaft und Dienstraum aus der Lichtbilanz; drinnen passt sich das Auge fast ganz an", 11);
        link("Wind (Beaufort)", "Barometer", "Luftdruck 1022 − 3,2 · Bft hPa, bis die Wetterregie (Phase 12) echte Werte gibt", 11);
        link("Innenraum", "com.dan.rain", "Unterstand: im Schaft und im Dienstraum fällt kein Regen", 11);
        link("Kamera", "Innenraum", "Fahrt „Aufstieg“: Tür, 59 Stufen, Dienstraum, Luke, Laternenraum", 11);
        link("Uhr (Zeitraffer)", "com.dan.weather", "Szenenzeit; Böen in Echtzeit", 12);
        link("com.dan.weather", "Wind (Wiring)", "Mittelwind, Richtung, Böigkeit; Böe im Augenblick für Regen und HUD", 12);
        link("com.dan.weather", "com.dan.ocean", "Wind und Dünung in Stufen (1 m/s, 10°, 0,25 m), damit die See nicht dauernd neu baut; die Dünung läuft dem Wind 3 h voraus", 12);
        link("com.dan.weather", "com.dan.cloud", "Bedeckung, Wolkenart (CloudKind → CloudType über den Namen), hohe Schicht; Cirren 2 h vor der Front", 12);
        link("com.dan.weather", "com.dan.rain, com.dan.sky", "Regenrate; Sicht mit Nebel; Regenbogen nach einem Schauer bei tiefer Sonne", 12);
        link("com.dan.weather", "com.dan.lightning", "Gewitterstärke 0..4 → Lightning.Level", 12);
        link("com.dan.weather", "Barometer, Beschlag, HUD", "Luftdruck mit Tendenz (fällt 5 h vor dem Sturm), Temperatur", 12);
        link("Bedienfeld", "com.dan.weather", "Handregler überschreiben einzelne Größen; „Regie weiter“, Regie anhalten (Leertaste), „Ein Sturmtag“", 12);
        link("com.dan.weather", "com.dan.fog", "Nebel 0..1 → Menge, Sicht in der Bank 4 km bis 60 m, Bedeckung, Obergrenze 18 bis 88 m; Wind (ab 6 m/s reißt er auf), Wasserstand; Lage Seenebel und „Ein Nebeltag“", 13);
        link("com.dan.weather", "com.dan.sky", "Sicht nur noch mit Dunst (den Nebel zeichnet com.dan.fog)", 13);
        link("com.dan.fog", "com.dan.beacon", "ScatterField → Streugitter: der Strahl leuchtet im Nebel", 13);
        link("com.dan.fog", "Lichtpunkte (Feuer, Tonnen, Schiff)", "Durchlässigkeit vom Licht zum Auge", 13);
        link("com.dan.fog (Signal)", "(Phase 14: soundscape), HUD", "SoundEvent NEBELHORN „Horn Mo(R) 30s“ am Turm, TYFON des Schiffs alle 2 min, nur bei Sicht unter 2 sm", 13);
        link("com.dan.ocean", "com.dan.buoyancy", "Sea ← heightAt, verticalVelocityAt, Wasserstand (Säulen der Tonnen und des Schiffs, 30-mal je s)", 13);
        link("com.dan.buoyancy", "com.dan.ocean", "Wake → addImpulse (der Bug wirft Ringwellen)", 13);
        link("Wiring (Wind), com.dan.sky", "com.dan.buoyancy", "Wind (Verankerung, Fahrt), Helligkeit in lx (Tonnenfeuer und Positionslichter an unter 200 lx)", 13);
        link("com.dan.buoyancy", "com.dan.beacon.gl", "Lichtpunkte der Tonnenfeuer und Positionslichter mit Schleier", 13);
        link("com.dan.buoyancy (Brücke)", "Kamera, com.dan.wetglass", "Auge mit Stampfen und Rollen; drei Frontscheiben nass aus dem Regen, Wischer wischt", 13);
        link("Turm, Peilpfahl", "com.dan.flock", "Obstacle (Schaft bis zur Kuppel, Pfahl), Aufwind an der Luvseite", 13);
        link("Turm, Peilpfahl, Manschette, com.dan.buoyancy", "com.dan.flock", "Perch: Handlauf der Galerie, Knauf, Pfahl, Platte bei Ebbe, Tonnen bei ruhiger See", 13);
        link("Wiring (Wind), com.dan.sky, com.dan.ocean", "com.dan.flock", "Wind, Helligkeit (nachts keine), Höhe der Wellen", 13);
        link("com.dan.flock", "(Phase 14: soundscape), HUD", "SoundEvent MOEWE", 13);
        link("com.dan.flock", "Kamera", "Möwenflug: Auge auf einer Möwe, mit Querneigung", 13);
        link("Wiring (Wind, Böigkeit, Regen, Temperatur), com.dan.ocean", "com.dan.soundscape", "Wind, Böigkeit, Regen am Ohr, Seegang (Hs, Tp), Wasserstand, Lufttemperatur (Schallgeschwindigkeit)", 14);
        link("Kamera, Turm, Laternenraum, Brücke", "com.dan.soundscape", "Ohr: Ort und Blickrichtung; drinnen Steinturm, Glashaus oder Steuerhaus", 14);
        link("Turm (Galerie, Mast)", "com.dan.soundscape", "Aeolian: Handlauf 70 mm, 60 Geländerstäbe 25 mm, Mast 60 mm singen im Wind; der Sockel als Brandung", 14);
        link("com.dan.wetglass, com.dan.rain", "com.dan.soundscape", "Tropfen je s auf den Scheiben (Laternenraum, Brücke) und auf der Kuppel", 14);
        link("com.dan.spray", "com.dan.soundscape", "Spray.onSound → Brecher am Sockel", 14);
        link("com.dan.fog (Signal)", "com.dan.soundscape", "Nebelhorn und Typhon mit der Dauer jedes Tons", 14);
        link("com.dan.flock", "com.dan.soundscape", "Möwenrufe am Ort der Möwe", 14);
        link("com.dan.lightning", "com.dan.soundscape", "Donner aus dem Kanal des Blitzes: Abstand jedes Stücks, Hauptkanal stärker, alle Schläge", 14);
        link("com.dan.buoyancy", "com.dan.soundscape", "Hum: Diesel jedes Schiffs (750 U/min, 6 Zylinder), an Bord im Steuerhaus", 14);
    }

    /**
     * Phase 10: Stärke, Wolken, Wasserstand und Ohr für das Gewitter (je Bild, vor {@code lightning.step}). Die
     * Blitze entstehen im Umkreis von 16 km um das Auge, mit gezeichneten Wolken nur unter ihren dichten Stellen
     * (Regenfeld, sonst Bedeckung); ohne Wolken gleichmäßig. Der Kanal über der Untergrenze ist in der Wolke verborgen,
     * außer wo sie eine Lücke hat.
     */
    public void feed(com.dan.lightning.Lightning l, Clouds.Snapshot cs, boolean cloudsShown, double[] eye) {
        if (l.level() != thunderstorm) l.setLevel(thunderstorm);
        l.cx = eye[0];
        l.cz = eye[2];
        l.radius = 16_000;
        l.listenerX = eye[0];
        l.listenerY = eye[1];
        l.listenerZ = eye[2];
        l.groundLevel = waterLevel.level();
        l.temperatureC = 15;
        if (cloudsShown && cs != null) {
            double base = cs.type().base;
            l.cloudBase = base;
            l.cells = (x, z) -> Math.max(Clouds.rain(cs, x, z) * 1.5, Clouds.coverage(cs, x, z) * 0.35);
            double top = base + cs.type().maxThickness;
            l.occlusion = (px, py, pz, ex, ey, ez) -> cloudTransmittance(cs, px, py, pz, ex, ey, ez, base, top);
        } else {
            l.cloudBase = 1200;
            l.cells = null;
            l.occlusion = null;
        }
    }

    /**
     * Durchlässigkeit der Wolken zwischen einem Punkt des Kanals und dem Auge: die Dichte aus {@link Clouds#density}
     * (ohne feines Rauschen) an zehn Stellen des Wegs innerhalb der Wolkenschicht. Etwas schwächer als die volle
     * Auslöschung (Faktor 0,5), weil Licht in der Wolke auch nach vorn gestreut ankommt.
     */
    static double cloudTransmittance(Clouds.Snapshot cs, double px, double py, double pz, double ex, double ey, double ez, double base, double top) {
        double dx = ex - px, dy = ey - py, dz = ez - pz, len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1) return 1;
        // Stück des Wegs in der Schicht base..top
        double t0 = 0, t1 = 1;
        if (Math.abs(dy) > 1e-6) {
            double ta = (base - py) / dy, tb = (top - py) / dy;
            t0 = Math.max(0, Math.min(ta, tb));
            t1 = Math.min(1, Math.max(ta, tb));
        } else if (py < base || py > top) return 1;
        if (t1 <= t0) return 1;
        double tau = 0, step = (t1 - t0) / 10;
        for (int i = 0; i < 10; i++) {
            double t = t0 + (i + 0.5) * step;
            tau += Clouds.density(cs, px + dx * t, py + dy * t, pz + dz * t, false) * step * len;
            if (tau > 20) break;
        }
        return Math.exp(-0.5 * tau);
    }

    /** Phase 10: die Fangpunkte (einmal): die Spitze des Masts auf der Galerie und die Laterne der Alten Weser. */
    public static void attractors(com.dan.lightning.Lightning l, double[] alteWeserTop) {
        l.attractors.clear();
        double a = Math.toRadians(Turm.MAST_AZ), r = Turm.GALLERY_R - 0.35;
        l.attractors.add(new com.dan.lightning.Lightning.Attractor(Math.sin(a) * r, Turm.MAST_TOP, -Math.cos(a) * r, "Roter Sand"));
        if (alteWeserTop != null) l.attractors.add(new com.dan.lightning.Lightning.Attractor(alteWeserTop[0], alteWeserTop[1], alteWeserTop[2], "Alte Weser"));
    }

    /** Phase 10: Donner als SoundEvent (Ort des nächsten Kanalpunkts, Stärke = Schalldruckpegel / 130 dB). */
    public static com.dan.lightning.Lightning.ThunderListener thunderTo(SoundEvent s) {
        return t -> { if (s != null) s.sound(SoundEvent.Kind.DONNER, t.x(), t.y(), t.z(), t.levelDb() / 130.0); };
    }

    /**
     * Phase 7: Regen, Wind, Wolkenuntergrenze, Wasserspiegel und Auge für den Regen (je Bild, vor {@code rain.step}).
     * Mit gezeichneten Wolken regnet es nur unter ihren dichten Stellen ({@link Clouds#rain}, auf 0..1 bezogen);
     * ohne Wolken überall gleich. Gibt den Wolken den Niederschlag zurück.
     */
    public void feed(Rain r, Clouds c, Clouds.Snapshot cs, boolean cloudsShown, double[] eye) {
        double rate = Math.max(0, rainRate);
        c.precipitation = rate > 0 ? Math.min(1, 0.35 + rate / 25) : 0;
        r.rate = rate;
        // Phase 12: der Regen treibt mit der Böe des Augenblicks
        r.windSpeed = regie ? gustNow : wind.speed();
        r.windFromDeg = wind.fromDeg();
        r.gust = wind.gustiness();
        r.waterLevel = waterLevel.level();
        r.cloudBase = cs.type().base;
        r.eyeX = eye[0];
        r.eyeY = eye[1];
        r.eyeZ = eye[2];
        // Phase 11: auch im Turm (Schaft und Dienstraum) regnet es nicht
        r.shelter = new double[]{0, 0, Turm.LANTERN_R + 0.05, Turm.DOME_TOP, 0, 0, Innenraum.ROOM_WALL_R + 0.03, Innenraum.CEIL_Y + 0.04};
        if (!cloudsShown || rate <= 0) { r.curtain = null; return; }
        double p = Math.max(1e-3, cs.precipitation());
        r.curtain = (x, z) -> curtain(cs, x, z, p);
    }

    /**
     * Wie stark es unter den Wolken regnet (0..1): {@link Clouds#rain} auf den Niederschlag bezogen und verdoppelt, denn
     * dessen Feld erreicht nur in den dichtesten Kernen 1. So regnet es unter geschlossenem Nimbostratus überall voll,
     * unter Gewitterwolken 7/8 in Schauern auf einem Teil der Fläche, unter 5/8 nur selten.
     */
    public static double curtain(Clouds.Snapshot cs, double x, double z, double precipitation) {
        return Math.min(1, Clouds.rain(cs, x, z) / precipitation * RAIN_GAIN);
    }

    public static final double RAIN_GAIN = 2;

    /** Richtungen der drei Scheiben-Simulationen relativ zum Wind: Luv, Seite, Lee (Grad). */
    public static final double[] GLASS_SIDE = {0, 90, 180};

    /**
     * Phase 8: Regen und Wind für die drei Scheiben des Laternenraums, die stellvertretend für alle zwölf rechnen:
     * Luv (dem Wind zugewandt), Seite und Lee. Die Einschläge kommen aus dem Regen für eine senkrechte Fläche mit
     * dieser Ausrichtung; auf der Seite mindestens ein Viertel, im Lee ein Zwanzigstel von Luv (Wirbel am Turm). Der Wind schiebt die Tropfen auf der Seite am
     * stärksten. Nach dem Regen trocknen die Scheiben schneller.
     */
    public void feed(Wetglass[] g, Rain r) {
        double from = wind.fromDeg(), sp = wind.speed();
        double rate = r.rateAt(0, 0);
        double luv = 0;
        for (int k = 0; k < g.length && k < 3; k++) {
            double az = Math.toRadians(from + GLASS_SIDE[k]);
            double hits = r.expectedHits(0, 0, 1, Math.sin(az), 0, -Math.cos(az), 0.3, 1);
            if (k == 0) luv = hits;
            // auf der Seite und im Lee trifft es auch etwas (Wirbel am Turm, wechselnder Wind)
            if (k == 1) hits = Math.max(hits, 0.25 * luv);
            if (k == 2) hits = Math.max(hits, 0.05 * luv);
            g[k].hitRate = hits;
            g[k].hitLambda = Rain.lambda(Math.max(0.05, rate));
            g[k].windX = sp * (k == 0 ? 0.15 : k == 1 ? 0.6 : 0.05);
            g[k].slopeX = 0;
            g[k].slopeY = -1;
            g[k].drainLength = (Turm.GLASS_TOP - Turm.GLASS_BOTTOM) / 2;
            g[k].evaporation = rate > 0.05 ? 2e-8 : 2.5e-7;
        }
    }

    /** Lufttemperatur draußen in °C (Oktober an der Außenweser) und wie viel feuchter die Luft im Raum ist (K Taupunkt). */
    public volatile double airTempC = 11, roomDewExcess = 2;
    /** Beschlag innen rechnen (Schalter im Bedienfeld). */
    public volatile boolean fogOn = true;

    /** Taupunkt in °C nach Magnus aus Temperatur und relativer Feuchte (0..1). */
    public static double dewPoint(double tC, double rh) {
        double a = 17.62, b = 243.12, g = Math.log(Math.max(1e-3, rh)) + a * tC / (b + tC);
        return b * g / (a - g);
    }

    /**
     * Phase 8b: Beschlag innen. Das Glas ist kälter als der Raum: draußen die Luft, im Regen rund 1,5 K kühler (der Regen
     * ist kälter und verdunstet), in klarer Nacht 1 K (es strahlt in den Himmel), dazu ein Viertel des Unterschieds zum
     * Raum. Der Raum ist mit Lampe 6 K wärmer als draußen, sonst 1 K, plus Sonne durchs Glas; seine Luft ist feuchter
     * als draußen ({@link #roomDewExcess}). Liegt der Taupunkt im Raum über der Glastemperatur, kondensiert Wasser
     * (2,5 nm/s je K), sonst trocknet es ab. Gibt den Unterschied Taupunkt − Glas in K zurück.
     */
    public double feedFog(Wetglass[] g, Rain r, boolean lamp, double sunAltDeg) {
        double rate = r.rateAt(0, 0);
        double rh = rate > 0.05 ? 0.95 : 0.8;
        double sunGain = Math.max(0, Math.sin(Math.toRadians(sunAltDeg))) * (oktas < 6 ? 4 : 1);
        double room = airTempC + (lamp ? 6 : 1) + sunGain;
        double glassT = airTempC - (rate > 0.05 ? 1.5 : 0) - (sunAltDeg < 0 && oktas < 4 ? 1 : 0) + 0.25 * (room - airTempC);
        double dew = dewPoint(airTempC, rh) + roomDewExcess;
        double diff = dew - glassT;
        double c = diff > 0 ? 2.5e-9 * diff : 3.5e-9 * diff;
        for (Wetglass w : g) w.condensation = fogOn ? c : -1e-7;
        return diff;
    }

    /**
     * Phase 8b: Salz an der Laterne. Ab stürmischem Wind trägt die Luft Seesalz (bei 20 m/s eine volle Kruste in knapp drei Stunden), dazu der
     * Sprühnebel der Gischt, wo er die Laterne auf der Wetterseite erreicht. Die Seite bekommt ein Drittel, das Lee ein
     * Zwanzigstel, der Handlauf so viel wie Luv.
     */
    public void feedSalt(Wetglass[] g, Wetglass rail, Spray sp) {
        double from = Math.toRadians(wind.fromDeg()), sp0 = wind.speed();
        double aero = 1e-5 * Math.pow(Math.max(0, sp0 - 10) / 10, 2) * 10;
        double mist = 0;
        if (sp != null) mist = sp.scatter(Math.sin(from) * (Turm.LANTERN_R + 0.6), (Turm.GLASS_BOTTOM + Turm.GLASS_TOP) / 2, -Math.cos(from) * (Turm.LANTERN_R + 0.6));
        double luv = aero + 0.03 * mist;
        double[] share = {1, 1 / 3.0, 0.05};
        for (int k = 0; k < g.length && k < 3; k++) g[k].saltRate = luv * share[k];
        if (rail != null) rail.saltRate = luv;
    }

    private long lastSplashes = -1;

    /**
     * Phase 8b: Überspülung. Steigt ein neuer Schwall der Gischt über die Galerie (Wasserstand + Kamm + Strahl über
     * {@link Turm#GALLERY_TOP}), läuft Seewasser über Handlauf und Scheiben der Wetterseite (0,5 mm, je Meter darüber
     * 0,15 mm mehr, höchstens 2 mm), die Seitenscheibe bekommt die Hälfte. Gibt die Dicke zurück (0 = nichts).
     */
    public double feedFlood(Spray sp, Wetglass[] g, Wetglass rail, double crest) {
        if (sp == null) return 0;
        long n = sp.totals()[0];
        boolean fresh = lastSplashes >= 0 && n > lastSplashes;
        lastSplashes = n;
        if (!fresh) return 0;
        double top = waterLevel.level() + crest + sp.lastApex();
        if (top < Turm.GALLERY_TOP) return 0;
        double mm = Math.min(2, 0.5 + 0.15 * (top - Turm.GALLERY_TOP));
        if (g.length > 0) g[0].flood(mm, true);
        if (g.length > 1) g[1].flood(mm * 0.5, true);
        if (rail != null) rail.flood(mm, true);
        return mm;
    }

    /**
     * Phase 8b: der Handlauf der Galerie als liegender Zylinder. Der Regen kommt quer zum Rohr (Einschläge auf eine Fläche
     * quer zum fallenden Regen), der Wind umströmt es mit dem Anteil quer zum Ring (gemittelt zwei Drittel).
     */
    public void feedRail(Wetglass rail, Wetglass.Cylinder c, Rain r) {
        double rate = r.rateAt(Math.sin(Math.toRadians(wind.fromDeg())) * Turm.HANDRAIL_RING, -Math.cos(Math.toRadians(wind.fromDeg())) * Turm.HANDRAIL_RING);
        c.wind = wind.speed() * 0.66;
        rail.rainAcross(rate, wind.speed());
        rail.evaporation = rate > 0.05 ? 2e-8 : 2.5e-7;
    }

    /** Sichtbarer Strahl nur, so weit die Gischt reicht: Sprühnebel zählt bis 80 m um den Turm. */
    public static final double SPRAY_SCATTER_RANGE = 80;
    /** Streukoeffizient 1/m für Sprühnebel der Dichte 1 (Sicht rund 40 m). */
    public static final double SPRAY_SIGMA = 0.08;

    /**
     * Phase 9: die Streudichte der Luft für den Strahl (ScatterField in 1/m): Regen aus {@link Rain#scatter} (0..1 =
     * Auslöschung / 3 km⁻¹, also mal 3·10⁻³ /m) und Sprühnebel aus {@link Spray#scatter} (0..1, mal
     * {@link #SPRAY_SIGMA}) bis {@link #SPRAY_SCATTER_RANGE} um den Turm. Der gleichmäßige Dunst kommt getrennt
     * ({@link #hazePerM()}). Nebel ({@code com.dan.fog}) kommt mit Phase 13 dazu.
     */
    public static ScatterField scatter(Rain r, Spray sp) { return scatter(r, sp, null); }

    /** Phase 13: dazu der Seenebel ({@link Fog#scatter}, schon in 1/m): Der Strahl leuchtet in der Nebelbank. */
    public static ScatterField scatter(Rain r, Spray sp, Fog fog) {
        return (x, y, z) -> {
            double s = r != null ? r.scatter(x, y, z) * 3e-3 : 0;
            if (sp != null && x * x + z * z < SPRAY_SCATTER_RANGE * SPRAY_SCATTER_RANGE && y < 40) s += sp.scatter(x, y, z) * SPRAY_SIGMA;
            if (fog != null) s += fog.scatter(x, y, z);
            return s;
        };
    }

    /** Dunst in 1/m aus der Sicht von Hand (Koschmieder mit 5 %: 3/V). */
    public double hazePerM() { return 2.996 / (Math.max(0.5, visibilityKm) * 1000); }

    /**
     * Phase 9: gibt dem Feuer Ort, Dunst und die Helligkeit am Turm (für den Dämmerungsschalter): Sonne auf eine
     * waagerechte Fläche plus Himmel, in lx.
     */
    public void feed(com.dan.beacon.Beacon b, double lux) {
        b.x = 0;
        b.z = 0;
        b.y = Turm.LENS_Y;
        b.hazePerM = hazePerM();
        b.ambientLux = lux;
    }

    /** Das Feuer als {@link SpotLight} für andere Packages (Rundumlicht, Stärke und Farbe in Richtung des Auges). */
    public static SpotLight spotLight(com.dan.beacon.Beacon b, double[] eye) {
        return new SpotLight() {
            com.dan.beacon.Beacon.Snapshot s() { return b.snapshot(); }
            double[] w() {
                com.dan.beacon.Beacon.Snapshot s = s();
                double dx = eye[0] - s.x(), dy = eye[1] - s.y(), dz = eye[2] - s.z(), l = Math.max(1e-6, Math.sqrt(dx * dx + dy * dy + dz * dz));
                return new double[]{dx / l, dy / l, dz / l};
            }
            @Override public double[] position() { return new double[]{s().x(), s().y(), s().z()}; }
            @Override public double[] direction() { return w(); }
            @Override public double halfAngleDeg() { return s().optic() == com.dan.beacon.Beacon.Optic.ROTATING ? s().wedgeDeg() : 180; }
            @Override public double[] color() {
                double[] w = w(), i = s().intensity(w[0], w[1], w[2]);
                double l = Math.max(1e-9, com.dan.beacon.LightColour.luminance(i));
                return new double[]{i[0] / l, i[1] / l, i[2] / l};
            }
            @Override public double candela() { double[] w = w(); return com.dan.beacon.LightColour.luminance(s().intensity(w[0], w[1], w[2])); }
        };
    }

    /** Phase 6: Wind und Seegang für die Gischt (je Bild, vor {@code spray.step}). */
    public void feed(Spray s, double hs) {
        s.windSpeed = wind.speed();
        s.windFromDeg = wind.fromDeg();
        s.waveHeight = hs;
    }

    /** Phase 6: die Körper, an denen Wellen anschlagen; die Manschette nur bei Ebbe. Gibt zurück, ob sie mitzählt. */
    public boolean feedBodies(Spray s, boolean plateBefore, boolean first) {
        boolean plate = waterLevel.level() < Turm.PLATE_TOP - 0.1;
        if (plate == plateBefore && !first) return plate;
        s.clearBodies();
        if (plate) for (double[] c : Turm.plateCircles()) s.addBody(Body.cylinder(c[0], c[1], c[2], Turm.PLATE_BOTTOM - 0.6, Turm.PLATE_TOP));
        s.addBody(new Body(0, 0, Turm.PLATE_BOTTOM - 0.5, Turm.SOCKEL_TOP, Turm.SOCKEL_R0, Turm.SOCKEL_R1));
        s.addBody(new Body(0, 0, Turm.SOCKEL_TOP, Turm.SHAFT_TOP, Turm.SHAFT_R0, Turm.SHAFT_R1));
        s.addBody(Body.cylinder(Math.sin(Math.toRadians(Turm.PFAHL_AZ)) * Turm.PFAHL_DIST, -Math.cos(Math.toRadians(Turm.PFAHL_AZ)) * Turm.PFAHL_DIST, 0.35, -5, 3.45));
        return plate;
    }

    /** Mittleres Hoch- und Niedrigwasser am Roten Sand über MSL (aus den Teiltiden: M2 + M4, im Mittel über Spring und Nipp). */
    public static final double MHW = 1.45, MLW = -1.45;

    /**
     * Phase 5: gibt der Verwitterung Wasserstand, Gezeitenband, Seegang, Wind und Sonne am Turm (je Bild, vor
     * {@code weathering.step}). Regen kommt mit Phase 7, Spritzwasser auf die Nässekarte mit Phase 6.
     */
    public void feed(Weathering w, double hs, double sunAtTower) {
        w.waterLevel = waterLevel.level();
        w.meanHighWater = MHW;
        w.meanLowWater = MLW;
        w.waveHeight = hs;
        w.windSpeed = wind.speed();
        w.windFromDeg = wind.fromDeg();
        w.sun = Math.max(0, Math.min(1, sunAtTower));
    }

    /**
     * Phase 5: die Körper im Wasser für das Meer. Der Sockel immer; die ovale Manschette nur, solange sie aus dem
     * Wasser ragt (sonst laufen die Wellen über sie hinweg und brechen am Sockel). Gibt zurück, ob sie mitzählt.
     */
    public boolean feedObstacles(Ocean ocean, boolean plateBefore) { return feedObstacles(ocean, plateBefore, true); }

    /**
     * Wie {@link #feedObstacles(Ocean, boolean)}; seit Phase 17 mit ring = false für Sockel und Manschette: dort kommt
     * die Brandung aus {@code com.dan.surf} (Brandung am Sockel), der Ring aus {@code ocean} bleibt für den Peilpfahl,
     * die Tonnen und die Schiffe.
     */
    public boolean feedObstacles(Ocean ocean, boolean plateBefore, boolean ring) {
        boolean plate = waterLevel.level() < Turm.PLATE_TOP - 0.1;
        java.util.List<com.dan.ocean.Obstacle> obs = ocean.obstacles();
        if (plate == plateBefore && !obs.isEmpty() && obs.get(0).ring() == ring) return plate;
        ocean.clearObstacles();
        ocean.addObstacle(new com.dan.ocean.Obstacle(0, 0, Turm.SOCKEL_R0, ring));
        if (plate) for (double[] c : Turm.plateCircles()) ocean.addObstacle(new com.dan.ocean.Obstacle(c[0], c[1], c[2], ring));
        ocean.addObstacle(new com.dan.ocean.Obstacle(Math.sin(Math.toRadians(Turm.PFAHL_AZ)) * Turm.PFAHL_DIST,
                -Math.cos(Math.toRadians(Turm.PFAHL_AZ)) * Turm.PFAHL_DIST, 0.35));
        return plate;
    }

    /** Phase 2: gibt dem Meer Wind und Wasserstand (je Bild, vor {@code ocean.step}). */
    public void feed(Ocean ocean) {
        // Phase 12: Das Meer baut sein Spektrum bei jeder Änderung neu (die See springt dabei einmal). Deshalb gehen
        // Wind und Dünung nur in Stufen weiter: Wind 1 m/s, Richtung 10°, Dünung 0,25 m, 0,5 s und 10°.
        double ws = wind.speed(), wd = wind.fromDeg();
        if (Math.abs(ws - ocean.windSpeed) >= 1.0 || (ws < 0.5) != (ocean.windSpeed < 0.5)) ocean.windSpeed = Math.round(ws * 2) / 2.0;
        if (Math.abs(((wd - ocean.windFromDeg) % 360 + 540) % 360 - 180) >= 10) ocean.windFromDeg = Math.round(wd / 5) * 5.0 % 360;
        if (Math.abs(swellHeight - ocean.swellHeight) >= 0.25) ocean.swellHeight = Math.round(swellHeight * 10) / 10.0;
        if (Math.abs(swellPeriod - ocean.swellPeriod) >= 0.5) ocean.swellPeriod = Math.round(swellPeriod * 10) / 10.0;
        if (Math.abs(((swellFromDeg - ocean.swellFromDeg) % 360 + 540) % 360 - 180) >= 10) ocean.swellFromDeg = Math.round(swellFromDeg / 5) * 5.0 % 360;
        ocean.waterLevel = waterLevel.level();
    }

    /**
     * Phase 12: die Wetterregie einen Schritt weiter (Szenenzeit aus der Uhr, Böen in Echtzeit) und ihre Werte in die
     * Eingänge der App übernehmen. Je Bild, vor allen anderen {@code feed}.
     */
    public Weather.Snapshot feedWeather(double realDt) {
        Weather.Snapshot s = weather.update(clock.epochMillis() / 1000.0, realDt);
        apply(s);
        return s;
    }

    /** Phase 12: Handregler: eine Größe der Regie sofort setzen und halten, bis „Regie weiter“. */
    public void hand(Weather.Var v, double value) {
        weather.set(v, value);
        Weather.Snapshot s = weather.snapshot();
        if (s != null) apply(s);
    }

    /** Phase 12: „Regie weiter“: alle Handregler lösen, auch Dünung und Regenbogen von Hand. */
    public void resumeRegie() {
        weather.release();
        swellPeriodHand = swellDirHand = Double.NaN;
        rainbowHand = -1;
    }

    private void apply(Weather.Snapshot s) {
        if (s == null) return;
        wind.speed = s.wind();
        wind.fromDeg = s.fromDeg();
        wind.gust = s.gustiness();
        gustNow = s.gustNow();
        oktas = s.oktas();
        highCover = s.high();
        cloudType = CloudType.valueOf(s.cloud().name());
        rainRate = s.rain();
        // Phase 13: den Nebel zeichnet com.dan.fog; der Himmel bekommt nur noch den Dunst
        visibilityKm = Math.max(0.2, s.visibilityKm());
        fogAmount = s.fog();
        thunderstorm = com.dan.lightning.Lightning.Level.values()[Math.max(0, Math.min(4, s.thunder()))];
        airTempC = s.temperature();
        pressureHPa = s.pressure();
        pressureTendency = s.tendency();
        swellHeight = s.swellHeight();
        swellPeriod = Double.isNaN(swellPeriodHand) ? s.swellPeriod() : swellPeriodHand;
        swellFromDeg = Double.isNaN(swellDirHand) ? s.swellFromDeg() : swellDirHand;
        showerEnd = s.showerEnd();
    }

    /** Phase 3: gibt dem Himmel Zeit, Ort, Augenhöhe, Sicht und Bedeckung (je Bild, vor {@code sky.step}). */
    public void feed(Sky sky, double eyeHeight) {
        sky.epochMillis = clock.epochMillis();
        sky.latDeg = clock.latDeg();
        sky.lonDeg = clock.lonDeg();
        sky.eyeHeight = Math.max(1, eyeHeight);
        sky.visibilityKm = visibilityKm;
        // Mit echten Wolken übernimmt com.dan.cloud Bedeckung, Schatten und Helligkeit; der Himmel bleibt klar
        sky.cloudCover = cloudsDrawn ? 0 : oktas / 8;
        sky.cloudVeil = !cloudsDrawn;
    }

    /**
     * Phase 4: gibt den Wolken Bedeckung, Art, hohe Schicht und den Wind in Wolkenhöhe. Über See weht er dort rund
     * 1,6-mal so stark und um 20° rechtsdrehend (Reibung und Ekman-Spirale), aber mindestens 3 m/s.
     */
    public void feed(Clouds c) {
        c.oktas = oktas;
        c.type = cloudType;
        c.highCover = highCover;
        c.windSpeed = Math.max(3, wind.speed() * 1.6);
        c.windFromDeg = (wind.fromDeg() + 20) % 360;
    }

    // ------------------------------------------------------------------ Phase 13: Nebel, Schwimmkörper, Möwen

    /** Nebel 0..1 aus der Regie ({@code Weather.Var.FOG}); Seenebel zeichnen (Schalter im Bedienfeld). */
    public volatile double fogAmount;
    public volatile boolean seaFogOn = true;
    /** Nebelsignal am Turm (gestaltet, der Rote Sand hatte keines im Verzeichnis von 1985). */
    public static final String HORN = "Horn Mo(R) 30s";
    public final Signal towerHorn = new Signal("Roter Sand", HORN, 0, Turm.LENS_Y + 2.5, 0);

    /**
     * Phase 13: der Seenebel aus der Regie. Nebel n (0..1) setzt die Menge (ab 0,05 bis 0,35 voll), die Sicht in der
     * Bank 4 km · (60/4000)^(n^0,6), die Bedeckung 0,3 + 0,8 n und die Obergrenze 18 + 70 n² m. Wind über 6 m/s reißt den
     * Nebel auf (bei 12 m/s ist er fort).
     */
    public void feed(Fog f) {
        double n = Math.max(0, Math.min(1, fogAmount));
        double windCut = 1 - smooth(6, 12, wind.speed());
        f.amount = seaFogOn ? smooth(0.05, 0.35, n) * windCut : 0;
        f.visibilityM = fogVisibilityM(n);
        f.coverage = Math.min(1, 0.3 + 0.8 * n);
        f.top = 18 + 70 * n * n;
        f.windSpeed = wind.speed();
        f.windFromDeg = wind.fromDeg();
        f.waterLevel = waterLevel.level();
        f.hazeVisibilityM = visibilityKm * 1000;
    }

    /** Sicht in der Nebelbank (m) bei Nebel n: 4 km bei 0, 60 m bei 1; schon flacher Nebel ist dicht (Exponent 0,6). */
    public static double fogVisibilityM(double n) {
        return 4000 * Math.pow(60 / 4000.0, Math.pow(Math.max(0, Math.min(1, n)), 0.6));
    }

    /** Das Nebelhorn am Turm (einmal) und sein Ereignis als SoundEvent. */
    public void signals(Fog f) {
        if (!f.signals.contains(towerHorn)) f.signals.add(towerHorn);
        f.onHorn = (x, y, z, strength, seconds, sig) -> {
            Soundscape sc = soundscape;
            boolean ty = sig.kind == Signal.Kind.TYFON;
            // Phase 14: der Klang bekommt die Dauer des Tons; andere Abnehmer das Ereignis
            if (sc != null && soundLive) sc.horn(ty ? Soundscape.Kind.TYFON : Soundscape.Kind.NEBELHORN, x, y, z, strength, seconds);
            else {
                SoundEvent s = sound;
                if (s != null) s.sound(ty ? SoundEvent.Kind.TYFON : SoundEvent.Kind.NEBELHORN, x, y, z, strength);
            }
            lastHorn = sig.name + " " + sig.text;
            lastHornT = f.time();
        };
    }

    /** Zuletzt gehörtes Nebelsignal und seine Zeit (Nebelzeit in s), für HUD und Bedienfeld. */
    public volatile String lastHorn = "";
    public volatile double lastHornT = -1e9;

    /** Das Fahrwasser: 1,5 km östlich auf dem Weg zur Alten Weser, von Nordwest nach Südost (gestaltet). */
    public static final double FAIRWAY_BEARING = 65, FAIRWAY_DIST = 1500, FAIRWAY_DEG = 315;

    /** Phase 13: das Meer als Wasser für die Schwimmkörper, das Fahrwasser mit acht Tonnen, Bugwellen ins Meer. */
    public void setup(Buoyancy b, Ocean ocean) {
        b.sea = new com.dan.buoyancy.Sea() {
            @Override public double height(double x, double z) { return ocean.heightAt(x, z); }
            // nur die senkrechte Geschwindigkeit, die braucht die Dämpfung; sie kommt mit der Höhe ohne weitere Rechnung
            @Override public double[] velocity(double x, double z) { return new double[]{0, ocean.verticalVelocityAt(x, z), 0}; }
            @Override public double level() { return waterLevel.level(); }
        };
        double a = Math.toRadians(FAIRWAY_BEARING);
        b.fairwayX = Math.sin(a) * FAIRWAY_DIST;
        b.fairwayZ = -Math.cos(a) * FAIRWAY_DIST;
        b.fairwayDeg = FAIRWAY_DEG;
        b.fairwayHalf = 7000;
        b.layFairway(260, 1100, 3);
        b.onWake = (x, z, s) -> ocean.addImpulse(x, z, 0.3 + 0.5 * s);
    }

    /** Phase 13: Wind und Helligkeit für Tonnen und Schiff (je Bild); das Typhon wandert mit dem Schiff. */
    public void feed(Buoyancy b, double lux, Fog fog) {
        b.windSpeed = wind.speed();
        b.windFromDeg = wind.fromDeg();
        b.ambientLux = lux;
        if (fog == null) return;
        for (Ship s : b.ships) {
            Signal t = typhons.computeIfAbsent(s, k -> { Signal x = new Signal(k.name, "Tyfon 120s", 0, 0, 0); x.phase = 37; return x; });
            double[] p = s.toWorld(s.length * 0.2, 0, s.freeboard + 9);
            t.x = p[0];
            t.y = p[1];
            t.z = p[2];
            if (!fog.signals.contains(t)) fog.signals.add(t);
        }
        typhons.entrySet().removeIf(e -> {
            if (b.ships.contains(e.getKey())) return false;
            fog.signals.remove(e.getValue());
            return true;
        });
    }

    private final java.util.Map<Ship, Signal> typhons = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Phase 13: Revier der Möwen: der Turm als Hindernis (Schaft bis zur Kuppel, der Pfahl), Sitzplätze auf dem Handlauf
     * der Galerie (zwölf), auf dem Knauf, auf dem Peilpfahl, auf der Platte (nur bei Ebbe) und auf den Tonnen (nur bis
     * Bft 5), Rufe als SoundEvent.
     */
    public void setup(Flock f, Buoyancy b) {
        f.obstacles.clear();
        f.obstacles.add(new Flock.Obstacle(0, 0, (Turm.SHAFT_R0 + Turm.SOCKEL_R1) / 2, -3, Turm.DOME_TOP));
        double pa = Math.toRadians(Turm.PFAHL_AZ), px = Math.sin(pa) * Turm.PFAHL_DIST, pz = -Math.cos(pa) * Turm.PFAHL_DIST;
        f.obstacles.add(new Flock.Obstacle(px, pz, 0.4, -3, 3.45));
        f.perches.clear();
        for (int i = 0; i < 12; i++) {
            double a = 2 * Math.PI * (i + 0.5) / 12;
            f.perches.add(new Flock.Perch("Handlauf " + (i + 1), Math.sin(a) * Turm.HANDRAIL_RING, Turm.HANDRAIL_Y + Turm.HANDRAIL_T + 0.02, -Math.cos(a) * Turm.HANDRAIL_RING));
        }
        f.perches.add(new Flock.Perch("Knauf", 0, Turm.KNOB_TOP + 0.5, 0));
        f.perches.add(new Flock.Perch("Peilpfahl", px, 3.5, pz));
        for (int i = 0; i < 4; i++) {
            double a = Math.toRadians(Turm.PLATE_ROT + 90 * i + 30), ra = Turm.SOCKEL_R0 + 1.2;
            double x = Math.sin(a) * ra, z = -Math.cos(a) * ra;
            f.perches.add(new Flock.Perch("Platte " + (i + 1), new Flock.Spot() {
                @Override public double[] position() { return new double[]{x, Turm.PLATE_TOP + 0.02, z}; }
                @Override public boolean usable() { return waterLevel.level() < Turm.PLATE_TOP - 0.3; }
            }));
        }
        if (b != null)
            for (com.dan.buoyancy.Buoy by : b.buoys) {
                f.perches.add(new Flock.Perch(by.name, new Flock.Spot() {
                    @Override public double[] position() { return by.toWorld(0.35, 0.0, com.dan.buoyancy.Buoy.FREEBOARD + 0.3); }
                    @Override public boolean usable() { return wind.speed() < 10.8; }
                }));
            }
        f.homeX = 0;
        f.homeZ = 0;
        f.homeRadius = 180;
        f.coastDeg = 150;
        f.onCall = (x, y, z, s, g) -> { SoundEvent e = sound; if (e != null) e.sound(SoundEvent.Kind.MOEWE, x, y, z, s); };
    }

    /** Phase 13: Wind, Helligkeit und die Höhe der Wellenkämme für die Möwen (je Bild). */
    public void feed(Flock f, double lux, double hs) {
        f.windSpeed = wind.speed();
        f.windFromDeg = wind.fromDeg();
        f.ambientLux = lux;
        double lv = waterLevel.level(), crest = 0.6 * hs;
        f.ground = (x, z) -> lv + crest;
    }

    // ------------------------------------------------------------------ Phase 14: Klang

    /** Der Klang (com.dan.soundscape) und ob er gerade läuft (Lautsprecher oder Mitschnitt); sonst gehen keine Ereignisse hinein. */
    public volatile Soundscape soundscape;
    public volatile boolean soundLive;

    /** Ereignisse der App an den Klang (Brecher, Möwe, Donner ohne Kanal, Hörner mit Standarddauer). */
    public SoundEvent soundTo(Soundscape s) {
        return (kind, x, y, z, strength) -> {
            if (!soundLive) return;
            switch (kind) {
                case BRECHER -> s.event(Soundscape.Kind.BRECHER, x, y, z, strength);
                case MOEWE -> s.event(Soundscape.Kind.MOEWE, x, y, z, strength);
                case NEBELHORN -> s.horn(Soundscape.Kind.NEBELHORN, x, y, z, strength, 1.0);
                case TYFON -> s.horn(Soundscape.Kind.TYFON, x, y, z, strength, 6.0);
                case DONNER -> s.thunder(x, y, z, 70 + 60 * strength, null, null, null, true);
                case REGEN_GLAS -> { }
            }
        };
    }

    /**
     * Donner aus dem Kanal eines Blitzes für den Klang: jedes Stück mit seinem Abstand zum Ohr, der Hauptkanal (Ordnung
     * 0) voll, Äste schwächer, alle Schläge; der Blitz hat die Laufzeit schon abgewartet.
     */
    public void thunder(Soundscape s, com.dan.lightning.Lightning.Thunder t, double[] ear) {
        if (s == null || !soundLive) return;
        com.dan.lightning.Bolt b = t.bolt();
        double[] d, w, strokes;
        if (b == null || b.n == 0) {
            d = null;
            w = null;
            strokes = null;
        } else {
            int n = b.n;
            d = new double[n];
            w = new double[n];
            for (int i = 0; i < n; i++) {
                double mx = (b.ax[i] + b.bx[i]) / 2.0, my = (b.ay[i] + b.by[i]) / 2.0, mz = (b.az[i] + b.bz[i]) / 2.0;
                d[i] = Math.sqrt((mx - ear[0]) * (mx - ear[0]) + (my - ear[1]) * (my - ear[1]) + (mz - ear[2]) * (mz - ear[2]));
                double len = Math.sqrt((b.bx[i] - b.ax[i]) * (b.bx[i] - b.ax[i]) + (b.by[i] - b.ay[i]) * (b.by[i] - b.ay[i]) + (b.bz[i] - b.az[i]) * (b.bz[i] - b.az[i]));
                w[i] = len * (b.order[i] == 0 ? 1 : 0.4);
            }
            strokes = b.strokeTime.clone();
            for (int k = strokes.length - 1; k >= 0; k--) strokes[k] -= strokes[0];
        }
        s.thunder(t.x(), t.y(), t.z(), t.levelDb(), d, w, strokes, true);
    }

    /** Die singenden Körper des Turms: Handlauf und Geländerstäbe der Galerie, der Mast; Ort je Bild nahe am Ohr. */
    static final double RAIL_BAR_D = 0.025, MAST_D = 0.06;

    /**
     * Phase 14: Eingänge des Klangs je Bild. ear: Auge, yawDeg: Blickrichtung; room und inside: wo das Ohr steht;
     * glassDrops und roofDrops: Tropfen je s auf Scheiben und Kuppel; rainAtEar: Regen am Ohr (mm/h).
     */
    public void feed(Soundscape s, double[] ear, double yawDeg, double hs, double tp, double rainAtEar, Room room, double inside, double glassDrops, double roofDrops) {
        s.windSpeed = wind.speed();
        s.windFromDeg = wind.fromDeg();
        s.gustiness = wind.gustiness();
        s.rainRate = rainAtEar;
        s.waveHeight = hs;
        s.wavePeriod = tp;
        s.waterLevel = waterLevel.level();
        s.temperatureC = airTempC;
        s.earX = ear[0];
        s.earY = ear[1];
        s.earZ = ear[2];
        s.earYawDeg = yawDeg;
        s.room = room;
        s.inside = inside;
        s.glassDrops = glassDrops;
        s.roofDrops = roofDrops;
        s.surfX = 0;
        s.surfZ = 0;
        s.surfRadius = Turm.SOCKEL_R0;
        // die Brandung schlägt an Sockel und Manschette, solange das Wasser sie erreicht
        s.surfStrength = smooth(-3.5, -1.0, waterLevel.level() + 0.5 * hs);
        // Handlauf und Stäbe: der Punkt des Rings, der dem Ohr am nächsten liegt (dort hört man sie)
        double a = Math.atan2(ear[0], -ear[2]);
        double rx = Math.sin(a), rz = -Math.cos(a);
        double ma = Math.toRadians(Turm.MAST_AZ);
        List<Aeolian> want = List.of(
                new Aeolian("Handlauf", rx * Turm.HANDRAIL_RING, Turm.HANDRAIL_Y, rz * Turm.HANDRAIL_RING, 2 * Turm.HANDRAIL_T, 2 * Math.PI * Turm.HANDRAIL_RING),
                new Aeolian("Geländerstäbe", rx * Turm.HANDRAIL_RING, Turm.GALLERY_TOP + 0.5, rz * Turm.HANDRAIL_RING, RAIL_BAR_D, 60 * 1.0),
                new Aeolian("Mast", Math.sin(ma) * (Turm.LANTERN_R + 0.3), (Turm.DOME_TOP + Turm.MAST_TOP) / 2, -Math.cos(ma) * (Turm.LANTERN_R + 0.3), MAST_D, Math.max(1, Turm.MAST_TOP - Turm.DOME_TOP)));
        if (s.bodies.size() != want.size()) { s.bodies.clear(); s.bodies.addAll(want); }
        else for (int i = 0; i < want.size(); i++) s.bodies.set(i, want.get(i));
    }

    private final java.util.Map<Ship, Hum> diesels = new java.util.concurrent.ConcurrentHashMap<>();

    /** Phase 14: der Diesel jedes Schiffs (achtern, 2 m über dem Wasser), an Bord, wenn das Auge auf seiner Brücke steht. */
    public void feedHums(Soundscape s, Buoyancy b, Ship onBridge) {
        for (Ship sh : b.ships) {
            Hum h = diesels.computeIfAbsent(sh, k -> {
                Hum x = new Hum(k.name, k.x, k.y, k.z);
                x.firingHz = Hum.firing(750, 6);
                s.hums.add(x);
                return x;
            });
            double[] p = sh.toWorld(-0.3 * sh.length, 0, 2);
            h.x = p[0];
            h.y = p[1];
            h.z = p[2];
            h.aboard = sh == onBridge;
        }
        diesels.entrySet().removeIf(e -> {
            if (b.ships.contains(e.getKey())) return false;
            s.hums.remove(e.getValue());
            return true;
        });
    }

    static double smooth(double a, double b, double x) {
        double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    /**
     * Das Licht des Himmels als {@link Lighting} für Packages, die es brauchen (Phase 4 ff.). Liest den jeweils
     * letzten Zustand; Farben ohne Vorbelichtung, Umgebungslicht als Strahldichte (Bestrahlung / π).
     */
    public static Lighting lighting(Sky sky) {
        return new Lighting() {
            Sky.State st() { Sky.State s = sky.state(); return s != null ? s : sky.update(); }
            @Override public double[] sunDir() { return st().sunDir.clone(); }
            @Override public double[] moonDir() { return st().moonDir.clone(); }
            @Override public double[] sunColor() { return st().sunColor.clone(); }
            @Override public double[] moonColor() { return st().moonColor.clone(); }
            @Override public double[] skyAmbient() {
                double[] e = st().skyIrradiance;
                return new double[]{e[0] / Math.PI, e[1] / Math.PI, e[2] / Math.PI};
            }
            @Override public double haze() { return st().groundExtinction; }
        };
    }

    /** Ein Blitz (Phase 10) erreicht den Himmel über dieselbe Form. */
    public static Flash flashTo(Sky sky) { return sky::flash; }

    public synchronized void link(String from, String to, String via, int phase) { links.add(new Link(from, to, via, phase)); }

    public synchronized List<Link> links() { return Collections.unmodifiableList(new ArrayList<>(links)); }

    /** Alle Schnittstellen dieses Pakets (für die Prüfung und das Werkbuch). */
    public static final Class<?>[] INTERFACES = {Wind.class, Clock.class, Lighting.class, SpotLight.class, ScatterField.class,
            SurfaceHit.class, Collider.class, WaterLevel.class, Flash.class, SoundEvent.class};
}
