package com.dan.lighthouse.gl;

import com.dan.lighthouse.camera.Camera;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.scene.Site;
import com.dan.lighthouse.scene.Sun;
import com.dan.lighthouse.scene.Tide;
import com.dan.lighthouse.scene.Innenraum;
import com.dan.lighthouse.scene.Turm;
import com.dan.lighthouse.wiring.Beaufort;
import com.dan.lighthouse.wiring.Wiring;
import com.dan.lighthouse.wiring.SoundEvent;
import com.dan.beacon.Beacon;
import com.dan.beacon.LightColour;
import com.dan.beacon.ScatterGrid;
import com.dan.beacon.gl.BeaconGl;
import com.dan.cloud.Clouds;
import com.dan.cloud.Noise;
import com.dan.cloud.gl.CloudGl;
import com.dan.lightning.Bolt;
import com.dan.lightning.Lightning;
import com.dan.lightning.gl.LightningGl;
import com.dan.ocean.Obstacle;
import com.dan.ocean.Ocean;
import com.dan.ocean.gl.OceanGl;
import com.dan.rain.Rain;
import com.dan.rain.gl.RainGl;
import com.dan.sky.Sky;
import com.dan.sky.gl.SkyGl;
import com.dan.spray.Spray;
import com.dan.spray.gl.SprayGl;
import com.dan.weathering.Weathering;
import com.dan.weathering.gl.WeatheringGl;
import com.dan.wetglass.Wetglass;
import com.dan.wetglass.gl.WetglassGl;
import com.dan.fog.Fog;
import com.dan.fog.gl.FogGl;
import com.dan.buoyancy.Buoyancy;
import com.dan.buoyancy.LightPoint;
import com.dan.buoyancy.Ship;
import com.dan.buoyancy.gl.BuoyancyGl;
import com.dan.flock.Flock;
import com.dan.flock.gl.FlockGl;
import com.dan.soundscape.AudioOut;
import com.dan.soundscape.Room;
import com.dan.soundscape.Soundscape;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Die Engine auf der Grafikkarte, Grundlage aus Kīlauea (und davor Kneiphof). Je Bild:
 * <ol>
 * <li>Phase 3: der Himmel aus {@code com.dan.sky} (Streuung, Sonne, Mond, Sterne, Planeten, Milchstraße) in einen
 * HDR-Puffer (RGBA16F, umgekehrte 32F-Tiefe), dazu dasselbe als kleines Rundumbild für die Spiegelungen im Wasser.
 * Alle Helligkeiten tragen schon die Vorbelichtung: Die Augenanpassung (halb an den Himmel, halb ans Blickfeld, über
 * eine Sekunde geglättet) wird vorher auf dem Prozessor bestimmt, damit der Nachthimmel im Halbfloat-Puffer nicht
 * verschwindet. Startet {@code com.dan.sky} nicht, bleibt der Verlauf aus Phase 1.</li>
 * <li>Phase 4: die Wolken aus {@code com.dan.cloud} über den Himmel gelegt (Raymarching in halber Auflösung, mit dem
 * letzten Bild gemischt), dazu ins Rundumbild und als Schattenkarte fürs Meer. Ihr Licht kommt aus dem Himmel: die
 * Sonne in Wolkenhöhe (abends noch rot, wenn sie unten schon weg ist), nachts der Mond.</li>
 * <li>Phase 2: das Meer aus {@code com.dan.ocean} – erst rechnen (Compute-Shader), dann zeichnen. Fällt der
 * Compute-Test, bleibt die Platzhalter-See aus Phase 1 (F24 B).</li>
 * <li>Phase 5: der Leuchtturm Roter Sand ({@link Turm}) mit Manschette, Steinschüttung, Peilpfahl und der Alten Weser
 * am Horizont. Vor dem Meer: Schattenkarte aus Richtung des Gestirns und der Schatten des Turms auf dem Wasser (eine
 * Draufsicht, die das Meer als Körperschatten liest). Nach dem Meer der Turm selbst, mit Nieten und Plattenstößen aus
 * dem Shader und der Verwitterung aus {@code com.dan.weathering} (nass nach Gezeit und Seegang, Rost, Algen, Salz).</li>
 * <li>Phase 6: die Gischt aus {@code com.dan.spray}: Spritzwasser an Sockel und Peilpfahl, Sprühnebel, Gischtfahnen ab
 * Bft 7. Die Tiefe der Szene wird dafür kopiert (weiche Ränder). Treffer machen die Wand nass, zurückfallendes Wasser
 * wirft Ringwellen.</li>
 * <li>Phase 9: das Leuchtfeuer aus {@code com.dan.beacon}: vor dem Meer seine Lichtverteilung als Rundumbild (das
 * Meer glitzert darin, der Turm zeigt Linse und Lichtflecken im Laternenraum), nach dem Regen der sichtbare Strahl
 * im Streugitter aus Regen, Gischt und Dunst und die Lichtpunkte des Feuers und der Nachbartürme.</li>
 * <li>Nachbearbeitung: Belichtung, Filmkurve, Gamma; dann das HUD.</li>
 * </ol>
 * Gemessen wird ab dem ersten Bild: Bilder/s, Prozessorzeit und die Zeit jedes Durchgangs auf der Grafikkarte
 * (Zeitstempel, ein Bild versetzt gelesen). Beim Start läuft einmal der Compute-Test (F24).
 * Im Bildlauf werden keine Dateien gelesen und keine Datenbank gefragt.
 */
public final class Renderer implements GLEventListener {
    public static final int MIN_MAJOR = 4, MIN_MINOR = 3;
    public static final String VERSION = "Phase 17";
    static final String[] PASSES = {"Himmel", "Wolken", "Meer rechnen", "Schatten", "Meer", "Turm", "Leben", "Gischt", "Regen", "Nebel", "Feuer", "Blitz", "Scheiben", "Nachbearbeitung", "HUD"};
    private static final int MARKS = PASSES.length + 1;

    public final SceneClock clock;
    public final Wiring wiring;
    public final Camera camera = new Camera();
    /** Phase 2: das Meer (Kern ohne Grafikkarte; die Verdrahtung gibt ihm Wind und Wasserstand). */
    public final Ocean ocean = new Ocean();
    private final OceanGl oceanGl = new OceanGl();
    private volatile boolean oceanOk;
    /**
     * Phase 17: die Brandung am Caisson und an der Steinschüttung aus {@code com.dan.surf} (Surf.sockel()); sie ersetzt
     * den Brandungsring aus {@code ocean} am Turm. Aus: der Ring wie bis Phase 16.
     */
    public final com.dan.lighthouse.scene.Sockelbrandung brandung = new com.dan.lighthouse.scene.Sockelbrandung();
    private final com.dan.surf.gl.BreakerGl brandungGl = new com.dan.surf.gl.BreakerGl();
    private volatile boolean brandungOk;
    private volatile String oceanCheck = "";
    /** Phase 3: der Himmel (Kern ohne Grafikkarte; die Uhr gibt Zeit, die Kamera die Augenhöhe). */
    public final Sky sky = new Sky();
    private final SkyGl skyGl = new SkyGl();
    private volatile boolean skyOk;
    private volatile String skyCheck = "";
    /** Augenanpassung: geglättete Leuchtdichte, Leuchtdichte im Blickfeld, Zeit bis zur nächsten Messung. */
    private double adaptLum = -1, viewLum = -1, viewTimer, beaconLum = -1;
    /** Phase 4: die Wolken (Kern ohne Grafikkarte; die Verdrahtung gibt Bedeckung, Art und Wind in Wolkenhöhe). */
    public final Clouds clouds = new Clouds();
    private final CloudGl cloudGl = new CloudGl();
    private volatile boolean cloudsOk;
    private volatile String cloudCheck = "";
    private double cloudShade = 1;
    /** Anteil der Anpassung (1 = volle Anpassung, 0 = keine); 0,85 lässt die Nacht Nacht bleiben. */
    public volatile double adaptation = 0.85;
    private volatile Quality quality = Quality.HOCH;
    public volatile double renderScale = quality.renderScale;
    /**
     * Phase 16: die Auflösung regelt sich selbst. Liegt die Grafikkarte über dem Budget für {@link #targetFps}, sinkt
     * {@link #renderScale} bis auf {@link #MIN_SCALE}; hat sie Luft, steigt er wieder bis zur Stufe. Geregelt wird
     * höchstens alle 1,5 s in Schritten von 5 %, damit die Zielbilder nicht ständig neu angelegt werden.
     */
    public volatile boolean autoScale = true;
    public volatile double targetFps = 60;
    public static final double MIN_SCALE = 0.5;
    private long scaleNanos;
    private int scaleSteps;
    public volatile boolean showHud = true, showTower = true;
    /** Rückfluss aus Timmendorf (Phase 18): Flagge am Mast (cloth) und Leuchten außen (lamp); -Drueckfluss=aus schaltet ab. */
    public volatile boolean showRueckfluss = !"aus".equals(System.getProperty("rueckfluss"));
    public final com.dan.lighthouse.scene.Aussenlicht aussen = new com.dan.lighthouse.scene.Aussenlicht();
    private final com.dan.lamp.gl.LampGl lampGl = new com.dan.lamp.gl.LampGl();
    private final com.dan.cloth.gl.ClothGl flaggeGl = new com.dan.cloth.gl.ClothGl();
    private boolean lampOk, flaggeOk;
    private double lampK;
    /**
     * Phase 16: das HUD ist in der Regel kurz (drei Zeilen oben links: Tempo, Zeit und Wetter, Kamera) und verdeckt
     * die Szene nicht mehr; {@code hudFull} zeigt alle Zeilen wie bisher. H schaltet kurz → voll → aus → kurz. Alle
     * Zeilen stehen außerdem im Block TECHNIK des Bedienfelds.
     */
    public volatile boolean hudFull;
    /** Phase 5: Verwitterung (Kern ohne Grafikkarte; die Verdrahtung gibt Wasserstand, Seegang, Wind und Sonne). */
    public final Weathering weathering = new Weathering();
    private final WeatheringGl weatheringGl = new WeatheringGl();
    private volatile boolean weatheringOk;
    private volatile String weatheringCheck = "";
    private boolean plateDry, weatheringSettled;
    private double lastEpoch = Double.NaN;
    /** Phase 6: Gischt (Kern ohne Grafikkarte; die Verdrahtung gibt Wind, Seegang, Körper und das Meer als Quelle). */
    public final Spray spray = new Spray();
    private final SprayGl sprayGl = new SprayGl();
    private volatile boolean sprayOk;
    private volatile String sprayCheck = "";
    public volatile boolean showSpray = true;
    /**
     * Phase 13 (Befund zu F33): Einheit des Feuers. Das Feuer rechnet in cd, lx und cd/m², die Szene in Einheiten von
     * {@link Sky#CD_PER_UNIT} cd/m² (die Vorbelichtung gilt je Einheit). Bis Phase 12 ging das Feuer ohne Umrechnung in
     * Bild und Augenanpassung ein, also 43 966-mal zu hell: Der Strahl brannte aus, und weil sich das Auge an ihn
     * anpasste, versank die Nacht daneben. 1 stellt das alte Bild wieder her (zum Vergleich).
     */
    public volatile double beaconUnit = 1 / Sky.CD_PER_UNIT;
    /** Phase 13: Durchmesser im Bild, ab dem Nebel- und Weißwasserbälle der Gischt ausgedünnt werden (0 = aus). */
    public volatile double sprayLodPixels = 110;
    private boolean sprayPlate, sprayFirst = true;
    private int sprayDepthTex, sprayDepthFbo;
    private double sprayCpuMs;

    public final Rain rain = new Rain();
    private final RainGl rainGl = new RainGl();
    private volatile boolean rainOk;
    private volatile String rainCheck = "";
    public volatile boolean showRain = true;
    /** Deckkraft des Regens 0..2 (Bedienfeld). */
    public volatile double rainStrength = 1;
    private double rainCpuMs;

    /** Phase 8: drei Kacheln Tropfen für die zwölf Scheiben des Laternenraums (Luv, Seite, Lee). */
    public final Wetglass[] glass = {new Wetglass(), new Wetglass(), new Wetglass()};
    private final WetglassGl[] glassGl = {new WetglassGl(), new WetglassGl(), new WetglassGl()};
    public static final String[] GLASS_NAMES = {"Luv", "Seite", "Lee"};
    private volatile boolean glassOk;
    private volatile String glassCheck = "";
    public volatile boolean showGlass = true;
    /** Vergrößerung des Blicks (1 = normal; die Nahaufnahme der Scheibe nimmt 3). */
    public volatile double zoom = 1;

    /** Senkrechter Bildwinkel in Grad mit der Vergrößerung. */
    public double fovy() { return Camera.FOVY / Math.max(0.45, zoom); }
    /** Deckkraft der Tropfen 0..1 (Bedienfeld). */
    public volatile double glassStrength = 1;
    private boolean wasInside;
    private int glassWarm;
    private double glassCpuMs;
    private int glassCopyTex, glassCopyFbo, glassCopyW, glassCopyH, paneVao, paneVbo;
    /** Phase 8b: der runde Handlauf der Galerie, nass mit derselben Simulation auf einem liegenden Zylinder (F10). */
    public final Wetglass rail = new Wetglass();
    public final Wetglass.Cylinder railShape = new Wetglass.Cylinder(Turm.HANDRAIL_T, true);
    private final WetglassGl railGl = new WetglassGl();
    private boolean wasNear;
    /** Taupunkt im Raum minus Glastemperatur (K) und Dicke der letzten Überspülung (mm) mit ihrer Szenenzeit. */
    private volatile double fogDiff, lastFloodMm, lastFloodT = -1e9;
    /** Die Tropfen rechnen in einem eigenen Faden (Standbilder schalten das ab und rechnen der Reihe nach). */
    public volatile boolean simThreaded = true;
    private Thread simThread;
    private final Object simLock = new Object();
    private double simDt, simExtra;
    private boolean simGlass, simRail;
    private volatile double simMs;
    /** Je Scheibenhälfte: Azimut der Mitte (Grad), Versatz der Kachel x, y (m). */
    private final double[][] panes = new double[24][];
    /** Raumlampe im Laternenraum nachts (das Feuer selbst ist {@link #beacon}). */
    public volatile boolean lampOn = true;
    /** Phase 9: das Leuchtfeuer (Kern ohne Grafikkarte; die Verdrahtung gibt Ort, Dunst und Helligkeit am Turm). */
    public final Beacon beacon = Beacon.roterSand1985();
    private final BeaconGl beaconGl = new BeaconGl();
    private ScatterGrid beaconGrid;
    private volatile boolean beaconOk;
    private volatile String beaconCheck = "";
    /** Nachbarfeuer: Alte Weser und Tegeler Plate (Lichtpunkte mit ihren Kennungen). */
    public volatile Beacon[] neighbours = {Beacon.alteWeser(), Beacon.tegelerPlate()};
    /**
     * Phase 15: der gewählte Turm (Leuchtturm-Auswahl aus LH_LIGHTHOUSE, F36 A). Das Feuer, der Ort für Sonne, Mond und
     * Sterne, die Gezeiten und die Nachbarfeuer folgen ihm; der Turm selbst bleibt der Rote Sand. homeSite = der Rote
     * Sand selbst: nur dann stehen die Alte Weser am Horizont und die Lichtglocken der Weserorte.
     */
    public volatile String siteName = "Roter Sand";
    public volatile boolean homeSite = true;
    public volatile boolean showBeam = true, showNeighbours = true;
    /** Deckkraft des Strahls 0..3 (Bedienfeld), 1 = physikalisch. */
    public volatile double beamStrength = 1;
    private double beaconCpuMs, beaconLux;
    /** Phase 10: das Gewitter (Kern ohne Grafikkarte; die Verdrahtung gibt Stärke, Wolken, Wasserstand und Ohr). */
    public final Lightning lightning = new Lightning();
    private final LightningGl lightningGl = new LightningGl();
    private volatile boolean lightningOk;
    private volatile String lightningCheck = "";
    private Lightning.Snapshot lightningSnap;
    public volatile boolean showLightning = true;
    /** Zeitlupe der Blitze (1 = echt; das Gewitter läuft dann langsamer als die Szene). */
    public volatile double lightningSlow = 1;
    /** Wie weit die Belichtung im Blitz nachgibt (1 = gar nicht); erholt sich in einer Viertelsekunde. */
    private double flashGive = 1;
    private double lightningCpuMs;
    private double[] lightningSeenFrom;
    private volatile String lastBoltText = "";
    private final Gpu.Mesh towerMesh = new Gpu.Mesh();
    private Turm turm;
    /** Phase 11: das Innere (Treppenhaus, Dienstraum) als eigenes Netz mit der Sichtbarkeit der Lichtquellen je Ecke. */
    private final Gpu.Mesh innerMesh = new Gpu.Mesh();
    private Turm inner;
    private java.util.concurrent.CompletableFuture<Turm> innerFuture;
    /** Die Glühlampen im Treppenhaus brennen (Bedienfeld). */
    public volatile boolean stairLight = true;
    /** Lichtbilanz drinnen: Strahldichte je Fenster (cd/m², rgb), indirektes Licht Schaft/Dienstraum (lx), Leuchtdichten. */
    private double[][] inWinL = new double[Innenraum.WINDOWS.size()][3];
    private double[] inAmbS = new double[3], inAmbR = new double[3];
    private double inLumS, inLumR, inTimer, inBlend;
    /**
     * Avatar Phase 15: wie nah das Auge nachts an der leuchtenden Laterne steht (0 … 1, voll unter 6 m, nichts über
     * 11 m vom Glas). Dort passt sich das Auge an das helle Glas und das Gesicht davor an und sieht mit den Zapfen.
     */
    private double lanBlend;
    private int shadowTex, shadowFbo, shadowSize, castTex, castFbo, sprayDepthW, sprayDepthH;
    static final int CAST = 1024;
    private float[] lightVP;
    private boolean shadowValid;
    private double[] castRect = {0, 0, 1};
    /** Uhr und Kamera stehen (Standbilder schalten selbst weiter). */
    public volatile boolean frozen;
    /**
     * Kinomodus (com.dan.kino, Rückfluss aus Glacier Phase 16): ein Regiebuch im Kino, ohne Bedienfeld und HUD, Balken
     * 2,39 : 1, Vor- und Abspann, Untertitel aus den Texten der Schritte, Überblendung bei jedem Wechsel der Kamera,
     * Schwenk, Filmkorn.
     */
    public final com.dan.kino.Kino kino = new com.dan.kino.Kino();
    private com.dan.kino.gl.KinoGl kinoGl;
    /** Wechsel der Kamera, die auf das Merken des Bilds warten (kommen aus dem Bedienfeld, laufen im Bildlauf). */
    private final java.util.concurrent.ConcurrentLinkedQueue<Runnable> kinoSchnitte = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private double kinoYaw, kinoTonBasis = Double.NaN;
    private boolean kinoFensterAn;
    private int kinoSchritt = -2;
    private volatile com.dan.weather.Scenario kinoBuch;
    /** true, wenn das Kino beginnt (Bedienfeld weg, Vollbild), false, wenn es zu Ende ist; im Bildlauf gerufen. */
    public volatile java.util.function.Consumer<Boolean> kinoFenster = an -> { };
    /** Belichtungskorrektur in Blendenstufen. */
    public volatile double exposureBias;
    public volatile boolean photoRequest;
    /** Phase 15: das nächste fertige Bild an diesen Empfänger (im GL-Faden; für die Vorschau gemerkter Ansichten). */
    public volatile java.util.function.Consumer<BufferedImage> grabRequest;
    /** Ordner für Fotos (F12); ohne Angabe {@code bilder/} im Arbeitsverzeichnis. */
    public volatile File photoDir;

    // ------------------------------------------------------------------ Phase 13: Leben und Nebel
    /** Seenebel ({@code com.dan.fog}) mit dem Nebelhorn am Turm. */
    public final Fog fog = new Fog();
    private final FogGl fogGl = new FogGl();
    /** Tonnen im Fahrwasser und Schiffe ({@code com.dan.buoyancy}). */
    public final Buoyancy buoyancy = new Buoyancy();
    private final BuoyancyGl buoyancyGl = new BuoyancyGl();
    /** Möwen um den Turm ({@code com.dan.flock}). */
    public final Flock flock = new Flock();
    private final FlockGl flockGl = new FlockGl();
    private boolean fogOk, buoyancyOk, flockOk, bridgeGlassOk;
    private String fogCheck = "", buoyancyCheck = "", flockCheck = "";
    public volatile boolean showFog = true, showShips = true, showGulls = true;
    /** Wischer auf der Brücke: −1 von selbst (bei Regen oder nassen Scheiben), 0 aus, 1 an. */
    public volatile int wiperMode = -1;
    /** Die drei Frontscheiben der Brücke als eine Fläche nebeneinander (nicht periodisch). */
    public final Wetglass bridgeGlass = new Wetglass();
    private final WetglassGl bridgeGlassGl = new WetglassGl();
    private int bridgeVao, bridgeVbo;
    /** Maße der Frontscheiben (eines Kümos): links außen, Breite aller drei, Unterkante, Höhe (m, Schiffskoordinaten). */
    static final double[] BRIDGE_PANES = bridgePanes();
    private volatile Ship bridgeShip;
    private double wiperWet = 1;
    private volatile int rideGull = -1;
    private double[] rideFwd;
    private double lifeCpuMs, lifeLux;
    private boolean pendG, pendR;

    // ------------------------------------------------------------------ Phase 14: Klang
    /** Der Klang (com.dan.soundscape); er spielt erst nach {@link #startSound()} (die App, nicht die Werkzeuge). */
    public final Soundscape soundscape = new Soundscape();
    private AudioOut audio;
    private double soundCpuMs;
    private volatile String soundWhere = "draußen";
    private double pendExtra;

    private final Shader skyShader = new Shader("Himmel", null, "fullscreen.vert", "sky.frag"),
            sea = new Shader("Platzhalter-See", null, "fullscreen.vert", "sea.frag"),
            skyPanoShader = new Shader("Himmelspanorama", null, "fullscreen.vert", "skypano.frag"),
            tower = new Shader("Turm", null, "tower.vert", "tower.frag"),
            towerShadow = new Shader("Turmschatten", null, "tower_shadow.vert", "tower_shadow.frag"),
            towerCast = new Shader("Turmschatten auf dem Wasser", null, "tower_cast.vert", "tower_cast.frag"),
            post = new Shader("Nachbearbeitung", null, "fullscreen.vert", "post.frag"),
            overlay = new Shader("Überlagerung", null, "fullscreen.vert", "overlay.frag"),
            glassShader = new Shader("Scheiben", null, "glass.vert", "glass.frag"),
            railShader = new Shader("Handlauf", null, "tower.vert", "rail_wet.frag");
    private final Shader[] all = {skyShader, sea, skyPanoShader, tower, towerShadow, towerCast, post, overlay, glassShader, railShader};
    static final int PANO_W = 256, PANO_H = 128;
    private int panoTex, panoFbo;
    private final HudOverlay hud = new HudOverlay();
    private int emptyVao, hdrTex, depthTex, hdrFbo, hdrW, hdrH, surfW = 1, surfH = 1, defaultFbo;
    private boolean reversedZ, ready;
    private final int[][] stamp = new int[2][MARKS];
    private final double[] passMs = new double[MARKS - 1];
    private int frame;
    private long lastNanos, statsNanos;
    private int statsFrames;
    private double fps, gpuMs, cpuMs, worstMs;
    private volatile String glInfo = "", error;
    private volatile ComputeProbe.Result compute;
    private volatile boolean reloadShaders;
    private Consumer<String> statusListener = s -> { };
    private Consumer<String> errorListener = s -> { };
    private volatile String photoMsg = "";
    private volatile long photoMsgUntil;

    public Renderer(SceneClock clock, Wiring wiring) {
        this.clock = clock;
        this.wiring = wiring;
        sky.latDeg = Site.LAT;
        sky.lonDeg = Site.LON;
        sky.epochMillis = clock.epochMillis();
        for (Site.Town t : Site.TOWNS) sky.domes.add(new Sky.LightDome(t.name(), t.azimuthDeg(), t.distanceKm(), t.strength()));
        // das Rauschen der Wolken (8 MB, je nach Rechner 0,5 bis 3 s) schon jetzt im Hintergrund rechnen
        Thread t = new Thread(() -> { Noise.base(); Noise.detail(); Noise.weather(); }, "cloud-noise");
        t.setDaemon(true);
        t.start();
    }

    public Renderer() { this(new SceneClock(), null); }

    // ------------------------------------------------------------------ Zugriff
    public Wiring wiring() { return wiring; }
    public void setStatusListener(Consumer<String> l) { statusListener = l; }
    public void setErrorListener(Consumer<String> l) { errorListener = l; }
    public String glInfo() { return glInfo; }
    public String error() { return error; }
    public double fps() { return fps; }
    public double gpuMs() { return gpuMs; }
    public double cpuMs() { return cpuMs; }
    public boolean reversedDepth() { return reversedZ; }
    public ComputeProbe.Result compute() { return compute; }
    /** Ob das Meer aus com.dan.ocean läuft (sonst die Platzhalter-See). */
    public boolean oceanRunning() { return oceanOk; }
    /** Ergebnis des Abgleichs Grafikkarte gegen Prozessor (leer, bis er gelaufen ist). */
    public String oceanCheck() { return oceanCheck; }
    public String oceanInfo() { return oceanGl.info(); }
    public Quality quality() { return quality; }
    public void requestShaderReload() { reloadShaders = true; }
    public double[] lastPassTimes() { return passMs.clone(); }
    public long frames() { return frame; }
    public double takeWorstFrameMs() { double w = worstMs; worstMs = 0; return w; }

    public void setQuality(Quality q) {
        quality = q;
        renderScale = q.renderScale;
        ocean.quality = q.ocean;
        weathering.quality = q.weathering;
        spray.quality = q.spray;
        rain.quality = q.rain;
        for (Wetglass g : glass) g.quality = q.wetglass;
        rail.quality = q.wetglass;
        beacon.quality = q.beacon;
        fog.quality = q.fog;
        buoyancy.quality = q.buoyancy;
        flock.quality = q.flock;
        bridgeGlass.quality = q.wetglass;
        soundscape.quality = q.sound;
        brandung.reihen = BRANDUNG_REIHEN[q.ordinal()];
    }

    /** Phase 17: Reihen der Brandung am Sockel je Stufe (Niedrig bis Ultra). */
    static final int[] BRANDUNG_REIHEN = {24, 32, 40, 56};

    /**
     * Belichtung. Mit {@code com.dan.sky}: aus der geglätteten Anpassungsleuchtdichte (halb ganzer Himmel, halb
     * Blickfeld) mit nur teilweiser Anpassung (Exponent {@link #adaptation}), die Nacht wird aufgehellt, bleibt aber
     * Nacht. Ohne: aus der Helligkeitstabelle von Phase 1.
     */
    public double exposure() {
        // Phase 10: im Blitz gibt die Belichtung nach (wie ein Auge, das kurz geblendet wird) und erholt sich
        // Phase 11: drinnen passt sich das Auge fast ganz an (Exponent mindestens 0,92), draußen nur teilweise
        double p = adaptation + Math.max(0, 0.92 - adaptation) * inBlend;
        if (skyOk && adaptLum > 0) return Sky.State.exposureFor(adaptLum, p) * Math.pow(2, exposureBias) / flashGive;
        double b = Sun.skyBrightness(clock.sun()[0]);
        return 0.6 * Math.pow(b, -0.85) * Math.pow(2, exposureBias);
    }

    /** Ob die Wolken aus com.dan.cloud laufen. */
    public boolean cloudsRunning() { return cloudsOk; }
    public String cloudCheck() { return cloudCheck; }
    public String cloudInfo() { return cloudGl.info(); }
    /** Anteil direkten Sonnenlichts am Turm (Wolkenschatten). */
    public double cloudShade() { return cloudShade; }

    /** Ob der Himmel aus com.dan.sky läuft (sonst der Verlauf aus Phase 1). */
    public boolean skyRunning() { return skyOk; }
    /** Ergebnis des Abgleichs Grafikkarte gegen Prozessor für den Himmel (leer, bis er gelaufen ist). */
    public String skyCheck() { return skyCheck; }
    public String skyInfo() { return skyGl.info(); }
    /** Der aktuelle Himmelszustand (nie null nach dem ersten Schritt). */
    public Sky.State skyState() {
        Sky.State st = sky.state();
        return st != null ? st : sky.update();
    }

    // ------------------------------------------------------------------ Lebenslauf

    @Override
    public void init(GLAutoDrawable d) {
        GL4 gl = d.getGL().getGL4();
        int major = d.getContext().getGLVersionNumber().getMajor(), minor = d.getContext().getGLVersionNumber().getMinor();
        String renderer = gl.glGetString(GL.GL_RENDERER), vendor = gl.glGetString(GL.GL_VENDOR), version = gl.glGetString(GL.GL_VERSION);
        glInfo = renderer + " · OpenGL " + major + "." + minor;
        System.out.println("Lighthouse: " + vendor + " · " + renderer + " · " + version);
        if (major < MIN_MAJOR || (major == MIN_MAJOR && minor < MIN_MINOR)) {
            fail("Die Grafikkarte meldet OpenGL " + major + "." + minor + " (" + renderer + ", Treiber " + version
                    + "). Lighthouse braucht OpenGL " + MIN_MAJOR + "." + MIN_MINOR + ".\nBitte den Grafiktreiber aktualisieren.");
            return;
        }
        gl.setSwapInterval(1);
        defaultFbo = d.getContext().getDefaultDrawFramebuffer();
        reversedZ = (major > 4 || minor >= 5) || gl.isExtensionAvailable("GL_ARB_clip_control");
        if (reversedZ) gl.glClipControl(GL4.GL_LOWER_LEFT, GL4.GL_ZERO_TO_ONE);
        try {
            for (Shader s : all) s.build(gl);
        } catch (RuntimeException e) {
            fail(e.getMessage());
            return;
        }
        int[] v = new int[1];
        gl.glGenVertexArrays(1, v, 0);
        emptyVao = v[0];
        turm = Turm.build();
        towerMesh.upload(gl, turm.verts, turm.idx, new int[]{3, 3, 3, 4, 4, 1});
        // Phase 11: das Innere (mit der Sichtbarkeit je Ecke, gut eine Sekunde) im Hintergrund bauen
        innerFuture = java.util.concurrent.CompletableFuture.supplyAsync(Turm::interior);
        try {
            lampGl.init(gl);
            lampOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Leuchten (lamp) nicht verfügbar: " + e.getMessage());
        }
        try {
            flaggeGl.init(gl);
            flaggeGl.setCloths(gl, java.util.List.of(aussen.flagge));
            flaggeOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Flagge (cloth) nicht verfügbar: " + e.getMessage());
        }
        try {
            weatheringGl.init(gl);
            weatheringOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Verwitterung nicht verfügbar: " + e.getMessage());
            errorListener.accept("Die Verwitterung (com.dan.weathering) lässt sich nicht starten.\n" + e.getMessage());
        }
        try {
            sprayGl.init(gl);
            sprayOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Gischt nicht verfügbar: " + e.getMessage());
            errorListener.accept("Die Gischt (com.dan.spray) lässt sich nicht starten.\n" + e.getMessage());
        }
        initSpray();
        try {
            rainGl.init(gl);
            rainOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Regen nicht verfügbar: " + e.getMessage());
            errorListener.accept("Der Regen (com.dan.rain) lässt sich nicht starten.\n" + e.getMessage());
        }
        rain.quality = quality.rain;
        try {
            for (WetglassGl g : glassGl) g.init(gl);
            railGl.init(gl);
            buildPanes(gl);
            glassOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Scheiben nicht verfügbar: " + e.getMessage());
            errorListener.accept("Die Tropfen auf den Scheiben (com.dan.wetglass) lassen sich nicht starten.\n" + e.getMessage());
        }
        for (int k = 0; k < glass.length; k++) {
            glass[k].quality = quality.wetglass;
            glass[k].seed = 1885 + 97 * k;
            glass[k].stepRate = 30;
            glass[k].reset();
        }
        rail.surface = railShape;
        rail.width = railShape.circumference();
        // die Kachel passt 75-mal in den Ring, dann gibt es keine Naht
        rail.height = 2 * Math.PI * Turm.HANDRAIL_RING / 75;
        rail.drainLength = 1e9;
        rail.quality = quality.wetglass;
        rail.seed = 1885 + 300;
        rail.stepRate = 30;
        rail.reset();
        try {
            beaconGl.init(gl);
            beaconOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Leuchtfeuer nicht verfügbar: " + e.getMessage());
            errorListener.accept("Das Leuchtfeuer (com.dan.beacon) lässt sich nicht starten.\n" + e.getMessage());
        }
        beacon.quality = quality.beacon;
        placeNeighbours();
        try {
            lightningGl.init(gl);
            lightningOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Gewitter nicht verfügbar: " + e.getMessage());
            errorListener.accept("Das Gewitter (com.dan.lightning) lässt sich nicht starten.\n" + e.getMessage());
        }
        lightning.quality = quality.lightning;
        lightning.seed = 1885;
        Wiring.attractors(lightning, homeSite && neighbours.length > 0 ? new double[]{neighbours[0].x, neighbours[0].y + 4.5, neighbours[0].z} : null);
        // Phase 14: Donner mit dem ganzen Kanal an den Klang
        lightning.onThunder = t -> { if (wiring != null) wiring.thunder(soundscape, t, camera.eye.clone()); };
        castTex = Gpu.texture(gl, CAST, CAST, GL4.GL_R8, false);
        castFbo = Gpu.fbo(gl, castTex, 0);
        System.out.println(String.format(Locale.GERMAN, "Lighthouse: Turm %,d Dreiecke, %s", turm.triangles(), weatheringGl.info()));
        for (int[] s : stamp) gl.glGenQueries(MARKS, s, 0);
        compute = ComputeProbe.run(gl);
        System.out.println("Lighthouse: " + compute.line());
        try {
            skyGl.init(gl);
            skyOk = true;
            System.out.println("Lighthouse: Himmel " + skyGl.info());
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Himmel nicht verfügbar, Verlauf aus Phase 1: " + e.getMessage());
            errorListener.accept("Der Himmel (com.dan.sky) lässt sich nicht starten, es bleibt der Verlauf aus Phase 1.\n" + e.getMessage());
        }
        if (skyOk) {
            try {
                cloudGl.init(gl);
                cloudsOk = true;
                if (wiring != null) wiring.cloudsDrawn = true;
                System.out.println("Lighthouse: Wolken " + cloudGl.info());
            } catch (RuntimeException e) {
                System.err.println("Lighthouse: Wolken nicht verfügbar: " + e.getMessage());
                errorListener.accept("Die Wolken (com.dan.cloud) lassen sich nicht starten, es bleibt der Schleier aus Phase 3.\n" + e.getMessage());
            }
        }
        panoTex = Gpu.texture(gl, PANO_W, PANO_H, GL.GL_RGBA16F, false);
        gl.glBindTexture(GL.GL_TEXTURE_2D, panoTex);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        panoFbo = Gpu.fbo(gl, panoTex, 0);
        ocean.quality = quality.ocean;
        weathering.mapWidthM = 2 * Math.PI * 4.6;
        weathering.mapHeightM = 34;
        weathering.mapBottom = -3;
        weathering.rust = 0.5;
        weathering.salt = 0.2;
        if (wiring != null) plateDry = wiring.feedObstacles(ocean, plateDry, !brandung.an);
        else ocean.addObstacle(new Obstacle(0, 0, Turm.SOCKEL_R0));
        try {   // Phase 17: Brecher am Sockel
            brandungGl.init(gl);
            brandungOk = true;
            System.out.println("Lighthouse: Brandung am Sockel " + brandungGl.info());
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Brandung am Sockel nicht verfügbar, es bleibt der Brandungsring: " + e.getMessage());
        }
        if (compute.ok()) {
            try {
                oceanGl.init(gl);
                oceanOk = true;
                System.out.println("Lighthouse: Meer " + oceanGl.info());
            } catch (RuntimeException e) {
                System.err.println("Lighthouse: Meer nicht verfügbar, Platzhalter-See: " + e.getMessage());
                errorListener.accept("Das Meer (com.dan.ocean) lässt sich nicht starten, es bleibt die Platzhalter-See.\n" + e.getMessage());
            }
        }
        initLife(gl);
        try {   // Kinomodus
            com.dan.kino.KinoMaler km = new com.dan.kino.KinoMaler();
            km.kopfFarbe = new java.awt.Color(214, 92, 76);   // Rot des Turms
            kinoGl = new com.dan.kino.gl.KinoGl(kino, km);
            kinoGl.init(gl);
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Kino nicht verfügbar (ohne Balken und Blenden): " + e.getMessage());
            kinoGl = null;
        }
        lastNanos = System.nanoTime();
        statsNanos = lastNanos;
        camera.snap();
        ready = true;
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
        surfW = Math.max(1, d.getSurfaceWidth());
        surfH = Math.max(1, d.getSurfaceHeight());
    }

    /** Uhr und Kamera um dt weiterrechnen (vom Bildlauf aufgerufen; Standbilder rufen es mit festem dt). */
    public void step(double dt) {
        clock.step(dt);
        kinoSchritt(dt);   // Kinomodus
        // Phase 12: zuerst die Wetterregie, dann bekommen alle Packages ihre Eingänge
        if (wiring != null && wiring.regie) {
            wiring.feedWeather(dt);
            double[] sp = clock.sun();
            double low = smooth(1, 6, sp[0]) * (1 - smooth(36, 42, sp[0]));
            sky.rainbow = wiring.rainbowHand >= 0 ? wiring.rainbowHand : 0.85 * wiring.showerEnd * low * (0.35 + 0.65 * smooth(0.2, 0.6, cloudShade));
        }
        if (wiring != null) {
            wiring.feed(ocean);
            wiring.feed(sky, camera.eye[1] - wiring.waterLevel.level());
            wiring.feed(clouds);
        } else {
            sky.epochMillis = clock.epochMillis();
        }
        Sky.State st = sky.step(dt);
        clouds.quality = quality.clouds;
        clouds.step(dt);
        // Wolkenschatten am Turm (für Pfahl und später den Turm): wie die Schattenkarte, auf dem Prozessor
        cloudShade = cloudsOk ? Clouds.shadow(clouds.snapshot(), 0, 0, keyDir(st)) : 1;
        adapt(st, dt);
        if (wiring != null) plateDry = wiring.feedObstacles(ocean, plateDry, !brandungAn());
        ocean.step(dt);
        stepBrandung(dt);
        stepWeathering(st, dt);
        stepSpray(dt);
        stepRain(dt);
        stepGlass(dt);
        stepBeacon(st, dt);
        if (showRueckfluss && wiring != null)
            aussen.schritt(dt, wiring.regie ? wiring.gustNow : wiring.wind.speed(), wiring.wind.fromDeg(), wiring.wind.gustiness(),
                    rainOk && showRain ? Math.min(1, rain.rate / 4) : 0);
        stepLightning(dt);
        stepLife(st, dt);
        stepPersonen(dt);   // Avatar Phase 15
        stepSound();
        double level = wiring != null ? wiring.waterLevel.level() : 0;
        // die Kamera bleibt über der Welle unter ihr; gefragt wird nur, wenn ein Kamm sie erreichen könnte
        boolean low = camera.eye[1] - level < 2 * ocean.state().totalHs() + 3;
        camera.waterLevel = oceanOk && low ? Math.max(level, ocean.heightAt(camera.eye[0], camera.eye[2])) : level;
        camera.step(dt);
    }

    /**
     * Augenanpassung: alle 0,2 s die Leuchtdichte im Blickfeld messen (12 Strahlen auf dem Prozessor), mit der des
     * ganzen Himmels mitteln und über eine Sekunde glätten (logarithmisch, wie das Auge).
     */
    private void adapt(Sky.State st, double dt) {
        viewTimer -= dt;
        if (viewLum <= 0 || viewTimer <= 0) {
            double[] f = camera.fwd.clone();
            viewLum = st.viewLuminance(f, new double[]{0, 1, 0}, fovy(), surfW / (double) Math.max(1, surfH));
            viewTimer = 0.2;
        }
        // Phase 9: das Feuer blendet. Strahl und Lichtpunkt im Blickfeld (im Laternenraum das Licht der Linse auf den
        // Wänden) zählen zur Leuchtdichte, an die sich das Auge anpasst.
        if (viewTimer >= 0.2 - 1e-9 || beaconLum < 0) beaconLum = beaconLuminance();
        double bl = beaconLum * beaconUnit;
        double target = Math.sqrt(Math.max(1e-14, st.adaptation + 0.5 * bl) * Math.max(1e-14, viewLum + bl)) * (cloudsOk ? clouds.brightnessFactor() : 1);
        // Phase 11: im Turm zählt die mittlere Leuchtdichte von Wänden und Stufen (Lichtbilanz), nicht der Himmel
        inTimer -= dt;
        if (inTimer <= 0 || dt > 0.5) { interiorBalance(st); inTimer = 0.25; }
        boolean in = insideTower();
        if (in) {
            double yb = smooth(Innenraum.ROOM_FLOOR - 0.8, Innenraum.ROOM_FLOOR + 0.4, camera.eye[1]);
            target = Math.max(1e-6, inLumS + (inLumR - inLumS) * yb) * 1.2;
        }
        inBlend = frozen || dt > 0.5 ? (in ? 1 : 0) : inBlend + ((in ? 1 : 0) - inBlend) * Math.min(1, dt / 1.5);
        // Avatar Phase 15: nah an der leuchtenden Laterne (Galerie bei Nacht) zählt ihr Glas mit [G, am Bild abgestimmt]
        double sa = skyOk && st != null ? st.sunAlt : clock.sun()[0];
        double glowU = lampOn && !in && !insideLantern() ? 1.5e-3 * (1 - smooth(-1, 2, sa)) : 0;
        double dl = Math.hypot(Math.hypot(camera.eye[0], camera.eye[2]) - Turm.LANTERN_R, camera.eye[1] - (Turm.GLASS_BOTTOM + Turm.GLASS_TOP) / 2);
        double lb = glowU > 0 ? 1 - smooth(6, 11, dl) : 0;
        lanBlend = frozen || dt > 0.5 ? lb : lanBlend + (lb - lanBlend) * Math.min(1, dt / 1.5);
        if (lanBlend > 1e-3) target = Math.exp(Math.log(Math.max(1e-14, target)) * (1 - lanBlend) + Math.log(Math.max(target, 0.07 * glowU)) * lanBlend);
        if (adaptLum <= 0 || frozen || dt > 0.5) adaptLum = target;
        else adaptLum = Math.exp(Math.log(adaptLum) + (Math.log(target) - Math.log(adaptLum)) * Math.min(1, dt / 1.0));
    }

    /**
     * Mittlere Leuchtdichte des Feuers im Blickfeld in cd/m² (für die Augenanpassung): 15 Blicke durch den Strahl
     * (ein Raster von 5 × 3 Richtungen über das Bild, bis zum Wasser oder 20 km), dazu der Lichtpunkt, verteilt auf das
     * Blickfeld; im Laternenraum ein Viertel des Lampenlichts, verteilt auf rund 35 m² Wände und Boden.
     */
    private double beaconLuminance() {
        if (!beaconOk) return 0;
        Beacon.Snapshot s = beacon.snapshot();
        if (!s.lit()) return 0;
        if (insideLantern()) return s.lumen() * s.level() * 0.25 / (Math.PI * 35);
        double[] e = camera.eye.clone(), f = camera.fwd.clone();
        double fov = Math.toRadians(fovy()), aspect = surfW / (double) Math.max(1, surfH);
        // Strahl und Wolkenunterseite: Mittel über das ganze Strahlbild der Grafikkarte (ein Bild später gelesen).
        // Bis Phase 9 waren es 15 Probeblicke auf dem Prozessor; die schmalen Strahlen der Drehlinse fielen zwischen
        // sie, das Auge blieb auf die dunkle Nacht eingestellt und der Strahl brannte weiß aus (Status_Phase9.png).
        double gm = beaconGl != null && showBeam ? beaconGl.beamMeanLuminance() : 0;
        double lb = Double.isNaN(gm) ? 0 : gm;
        double dx = s.x() - e[0], dy = s.y() - e[1], dz = s.z() - e[2], dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double cosA = (dx * f[0] + dy * f[1] + dz * f[2]) / Math.max(1e-6, dl);
        if (cosA > Math.cos(fov / 2 * Math.max(1, aspect))) {
            double E = LightColour.luminance(s.illuminanceAt(e[0], e[1], e[2], beacon.hazePerM));
            lb += E / (fov * fov * aspect);
        }
        if (showNeighbours) for (Beacon n : neighbours) {
            Beacon.Snapshot ns = n.snapshot();
            if (!ns.lit()) continue;
            double nx = ns.x() - e[0], ny = ns.y() - e[1], nz = ns.z() - e[2], nl = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if ((nx * f[0] + ny * f[1] + nz * f[2]) / nl > Math.cos(fov / 2 * Math.max(1, aspect) * 1.3))
                lb += LightColour.luminance(ns.illuminanceAt(e[0], e[1], e[2], beacon.hazePerM)) / (fov * fov * aspect);
        }
        return lb;
    }

    /** Das Streugitter um das Feuer (für Werkzeuge). */
    public ScatterGrid beaconGrid() { return beaconGrid; }

    /** Mittlere Leuchtdichte des Strahlbilds (cd/m², Grafikkarte). */
    public double beaconGlMean() { return beaconGl != null ? beaconGl.beamMeanLuminance() : Double.NaN; }

    /** Leuchtdichte des Feuers im Blickfeld (letzte Messung, cd/m²). */
    public double beaconLum() { return beaconLum; }

    /** Beleuchtungsstärke auf einer waagerechten Fläche in lx (Himmel und Sonne, Wolkenschatten) für den Dämmerungsschalter. */
    public static double daylightLux(Sky.State st, double cloudShade) {
        if (st == null) return 0;
        return (LightColour.luminance(st.skyIrradiance) + LightColour.luminance(st.sunColor) * Math.max(0, st.sunDir[1]) * cloudShade) * Sky.CD_PER_UNIT;
    }

    /** Wartet, bis das Innere gebaut ist (Standbilder). */
    public void awaitInterior() { if (innerFuture != null) innerFuture.join(); }

    /** Steht das Auge im Turm (Treppenhaus oder Dienstraum)? */
    public boolean insideTower() {
        double[] e = camera.eye;
        return Innenraum.inside(e[0], e[1], e[2]);
    }

    /** Ist das Innere von hier zu sehen (im Turm, im Laternenraum durch die Luke, auf der Galerie durchs Glas)? */
    public boolean interiorVisible() {
        double[] e = camera.eye;
        return Math.hypot(e[0], e[2]) < 6 && e[1] > Innenraum.FLOOR_Y - 1 && e[1] < Turm.DOME_TOP + 3;
    }

    /**
     * Phase 11: Lichtbilanz im Turm alle 0,25 s: Strahldichte des Himmels vor jedem Fenster (halb Himmel über der
     * Kimm, halb Meer), Sonne durch die Fenster, die Glühlampen; daraus das zurückgeworfene Licht in Schaft und
     * Dienstraum und deren mittlere Leuchtdichte (für die Augenanpassung).
     */
    private void interiorBalance(Sky.State st) {
        double[] sunE, sunDir;
        if (skyOk && st != null) {
            double[] up = st.radiance(new double[]{0, 1, 0});
            for (int i = 0; i < Innenraum.WINDOWS.size(); i++) {
                Innenraum.Window w = Innenraum.WINDOWS.get(i);
                double[] sky = st.radiance(norm3(new double[]{w.nx(), 0.35, w.nz()})), hor = st.radiance(norm3(new double[]{w.nx(), 0.04, w.nz()}));
                for (int c = 0; c < 3; c++) inWinL[i][c] = 0.35 * sky[c] + 0.4 * hor[c] + 0.25 * 0.12 * up[c];
            }
            sunE = scale3(st.sunColor, cloudShade);
            sunDir = st.sunDir;
        } else {
            double[] sp = clock.sun();
            double b = Sun.skyBrightness(sp[0]);
            for (double[] l : inWinL) { l[0] = 0.75 * b; l[1] = 0.8 * b; l[2] = 0.9 * b; }
            sunE = sunLight(sp[0]);
            sunDir = Sun.direction(sp[0], sp[1]);
        }
        double[][] bal = Innenraum.balance(inWinL, sunE, sunDir, stairLight, lightUnit());
        inAmbS = bal[0];
        inAmbR = bal[1];
        inLumS = bal[2][0];
        inLumR = bal[3][0];
    }

    /**
     * Lichteinheit der Szene je cd (bzw. lm, lx): mit com.dan.sky eine Einheit = {@link Sky#CD_PER_UNIT} cd/m²; ohne
     * entspricht der Platzhalter-Himmel am Tag (1) etwa 0,1 Einheiten.
     */
    private double lightUnit() { return (skyOk ? 1 : 10) / Sky.CD_PER_UNIT; }

    /**
     * Phase 11: Weißabgleich drinnen. Das Auge stellt sich zu gut zwei Dritteln auf die Farbe des Lichts im Raum ein
     * (von Kries): Himmelslicht durch die Bullaugen wirkt nicht blau, Glühlampenlicht nicht orange.
     */
    public double[] whiteBalance() {
        if (inBlend <= 1e-3) return new double[]{1, 1, 1};
        double yb = smooth(Innenraum.ROOM_FLOOR - 0.8, Innenraum.ROOM_FLOOR + 0.4, camera.eye[1]);
        double[] e = new double[3];
        for (int c = 0; c < 3; c++) e[c] = Math.max(1e-12, inAmbS[c] + (inAmbR[c] - inAmbS[c]) * yb);
        double y = 0.2126 * e[0] + 0.7152 * e[1] + 0.0722 * e[2], k = 0.7 * inBlend;
        double[] w = new double[3];
        for (int c = 0; c < 3; c++) w[c] = Math.pow(y / e[c], k);
        return w;
    }

    static double[] scale3(double[] v, double k) { return new double[]{v[0] * k, v[1] * k, v[2] * k}; }

    /** Phase 12: Kamera nach dem Wunsch des Regiebuchs: Aussen1, Drohne, Brecher, Aussen3, Sturmnacht, Laternenraum, Regenbogen. */
    public void applyView(String name) {
        // Kinomodus: der Wechsel wartet auf das Merken des Bilds (Überblendung)
        if (kino.schnitt()) kinoSchnitte.add(() -> applyViewJetzt(name));
        else { applyViewJetzt(name); kinoYaw = 0; }
    }

    /** Kinomodus: ein Regiebuch (Szenario) im Kino; das Bedienfeld hat es schon gestartet. */
    public void kinoStart(com.dan.weather.Scenario sc) {
        frozen = false;
        kinoBuch = sc;
        kinoSchritt = -2;
        kino.beginnen("Lighthouse · Roter Sand bei jedem Wetter", sc.name(),
                String.format(Locale.GERMAN, "Der Leuchtturm Roter Sand in der Wesermündung: %d Bilder des Wetters in %.1f Stunden", sc.steps().size(), sc.hours()));
    }

    /** Kinomodus verlassen (Esc); das Regiebuch läuft im Fenster weiter. */
    public void kinoVerlassen() {
        frozen = false;
        kino.abbrechen();
    }

    /** Die Zeilen des Abspanns (erste Zeile groß). */
    java.util.List<String> abspann(com.dan.weather.Scenario sc) {
        java.util.List<String> z = new java.util.ArrayList<>();
        z.add(sc != null ? sc.name() : "Lighthouse");
        z.add("Ort: Leuchtturm Roter Sand in der Wesermündung");
        z.add("Szene: " + clock.label());
        z.add("Himmel, Wetter, See, Gischt, Feuer und Klang: com.dan");
        z.add("Lighthouse · " + VERSION);
        return z;
    }

    /** Kino weiter: Untertitel aus den Schritten, Abspann am Ende, Schwenk und Ton. */
    private void kinoSchritt(double dt) {
        if (kino.laeuft() && wiring != null) {
            com.dan.weather.Weather.Snapshot ws = wiring.weather.snapshot();
            int st = ws == null || kinoBuch == null || !kinoBuch.name().equals(ws.scenario()) ? -1 : ws.step();
            if (st >= 0 && st != kinoSchritt) { kinoSchritt = st; if (ws.stepText() != null) kino.untertitel(ws.stepText()); }
            com.dan.kino.Kino.Lage l = kino.lage();
            if (st < 0 && kinoSchritt >= 0 && (l == com.dan.kino.Kino.Lage.VORSPANN || l == com.dan.kino.Kino.Lage.FILM)) kino.beenden(abspann(kinoBuch));
        }
        kino.schritt(dt);
        if (!kino.laeuft() && !kinoSchnitte.isEmpty() && !kino.schnittFaellig()) schnitteJetzt();
        double sw = kino.schwenk();
        if (camera.followName.isEmpty() && kinoSchnitte.isEmpty()) {
            double dd = sw - kinoYaw;
            if (camera.mode == Camera.Mode.FREI) camera.fyaw += dd; else if (camera.mode == Camera.Mode.KREISEN) camera.yaw += dd;
        }
        kinoYaw = sw;
        if (kino.sichtbar()) {
            if (Double.isNaN(kinoTonBasis)) kinoTonBasis = soundscape.volume;
            soundscape.volume = kinoTonBasis * kino.ton();
        } else if (!Double.isNaN(kinoTonBasis)) { soundscape.volume = kinoTonBasis; kinoTonBasis = Double.NaN; }
    }

    private void schnitteJetzt() {
        Runnable x;
        while ((x = kinoSchnitte.poll()) != null) x.run();
        kinoYaw = 0;
    }

    /** Schnitt mit gemerktem Bild, dann Balken, Schwarz, Untertitel; Fenster auf und zu. */
    private void kinoBild(GL4 gl, long now) {
        if (kinoGl != null && kinoGl.bereit()) {
            try {
                if (kino.schnittFaellig()) {
                    kinoGl.merken(gl, defaultFbo, surfW, surfH);
                    schnitteJetzt();
                    kino.geschnitten();
                }
                kinoGl.zeichnen(gl, defaultFbo, surfW, surfH, now / 1e9);
            } catch (RuntimeException e) {
                System.err.println("Lighthouse: Kino: " + e);
                kinoGl = null;
            }
        } else if (kino.schnittFaellig()) { schnitteJetzt(); kino.geschnitten(); }
        boolean an = kino.laeuft();
        if (an != kinoFensterAn) {
            kinoFensterAn = an;
            try { kinoFenster.accept(an); } catch (RuntimeException e) { System.err.println("Lighthouse: Kinofenster: " + e); }
        }
    }

    /** Phase 12: Kamera nach dem Wunsch des Regiebuchs (ohne Kino gleich, im Kino nach dem Merken des Bilds). */
    private void applyViewJetzt(String name) {
        Camera c = camera;
        if (name == null || name.isEmpty() || c.rideTime() >= 0) return;
        zoom = 1;
        double from = (wiring != null ? wiring.wind.fromDeg() : 240);
        switch (name) {
            case "Aussen1" -> { c.mode = Camera.Mode.KREISEN; c.autoOrbit = false; c.lookAt(0, 13.5, 0, 62, 1.5, 70); }
            case "Drohne" -> { c.mode = Camera.Mode.KREISEN; c.autoOrbit = false; c.lookAt(0, 9, 0, 325, 55, 48); }
            case "Brecher" -> { c.mode = Camera.Mode.KREISEN; c.autoOrbit = false; c.lookAt(0, 6, 0, from + 180 + 35, 4, 55); }
            case "Sturmnacht" -> { c.mode = Camera.Mode.KREISEN; c.autoOrbit = false; c.lookAt(0, 20, 0, 250, 0.5, 1200); }
            case "Aussen3" -> { c.mode = Camera.Mode.KREISEN; c.autoOrbit = false; c.lookAt(0, 16, 0, 250, 3, 260); }
            case "Laternenraum" -> {
                double a = Math.toRadians(from + 180 + 12);
                c.place(Math.sin(a) * 1.4, com.dan.lighthouse.scene.Turm.GALLERY_TOP + 1.7, -Math.cos(a) * 1.4, from + 5, 9);
            }
            case "Regenbogen" -> {
                double[] sp = clock.sun();
                c.mode = Camera.Mode.KREISEN;
                c.autoOrbit = false;
                // Gegenpunkt der Sonne; der Bogen steht 42° um ihn, also weiter Winkel und etwas nach oben
                zoom = 0.75;
                c.lookAt(0, 14, 0, sp[1] + 180, -10, 160);
            }
            case "Möwenflug" -> { if (!rideGull()) { c.mode = Camera.Mode.KREISEN; c.autoOrbit = false; c.lookAt(0, 20, 0, from + 90, 6, 90); } }
            case "Schiffsbrücke" -> toBridge();
            default -> { }
        }
    }

    /** Phase 12: Zeile der Wetterregie für HUD und Bedienfeld. */
    public String weatherLine() {
        if (wiring == null) return "Wetter: ohne Verdrahtung";
        com.dan.weather.Weather.Snapshot s = wiring.weather.snapshot();
        if (s == null || !wiring.regie) return "Wetter: von Hand (Regie aus)";
        String sc = s.scenario().isEmpty() ? "frei" : String.format(Locale.GERMAN, "„%s“ %d/%d", s.scenario(), s.step() + 1, s.steps());
        return String.format(Locale.GERMAN, "Wetter %s · %s%s · Bft %d, Böe %.0f m/s · %.1f hPa %s · %.1f °C · nächste Lage %s in %.1f h%s",
                sc, s.stepText(), s.running() ? "" : " (angehalten)", s.beaufort(), s.gustNow(), s.pressure(), s.tendencyText(), s.temperature(),
                s.next().label, s.nextInH(), wiring.weather.overrides() > 0 ? String.format(Locale.GERMAN, " · %d von Hand", wiring.weather.overrides()) : "");
    }

    /** Zeile fürs Bedienfeld: Licht im Turm. */
    public String interiorLine() {
        if (inner == null) return "Innen: das Treppenhaus wird gebaut …";
        return String.format(Locale.GERMAN, "Innen: Schaft %.1f cd/m², Dienstraum %.1f cd/m² · Lampen %s · %,d Dreiecke · %s",
                inLumS / lightUnit(), inLumR / lightUnit(), stairLight ? "an" : "aus", inner.triangles(), insideTower() ? "Auge im Turm" : "Auge draußen");
    }

    /** Augenanpassung sofort neu messen (Standbilder, nach Umschalten des Feuers). */
    public void readapt() {
        viewTimer = 0;
        beaconLum = -1;
        adaptLum = -1;
        adapt(skyState(), 1);
    }

    static double[] norm3(double[] v) { double l = Math.max(1e-12, Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])); return new double[]{v[0] / l, v[1] / l, v[2] / l}; }

    static double[] cross(double[] a, double[] b) { return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]}; }

    /**
     * Verwitterung: Wasserstand, Seegang, Wind und Sonne hinein, dann in Szenenzeit weiter (der Zeitraffer trocknet
     * schneller). Beim Start und nach Zeitsprüngen wird die nasse Zone aus den letzten drei Stunden Gezeit gesetzt:
     * Was das Wasser oder der Auflauf damals erreicht hat, ist so weit noch nass, wie es seither nicht trocknen konnte.
     */
    private void stepWeathering(Sky.State st, double dt) {
        if (wiring == null) return;
        double hs = ocean.state().totalHs();
        wiring.feed(weathering, hs, cloudShade * Math.max(0, st.sunDir[1]) * 1.5);
        weathering.rainRate = showRain ? rain.rateAt(0, 0) : 0;
        double now = clock.epochMillis();
        double sceneDt = Double.isNaN(lastEpoch) ? 0 : (now - lastEpoch) / 1000;
        lastEpoch = now;
        if (!weatheringSettled || sceneDt < 0 || sceneDt > 3600) {
            weathering.settle();
            double rate = weathering.dryingRate(), run = Weathering.runup(hs), line = -1e9;
            for (int i = 0; i <= 36; i++) {
                double ago = i * 300.0;
                line = Math.max(line, Tide.at(now - ago * 1000) + run - rate * ago / 3600);
            }
            if (wiring.tide.on) weathering.setWetLine(line);
            weatheringSettled = true;
        } else {
            weathering.step(sceneDt);
        }
    }

    private volatile double[] nextTide;

    /** Phase 17: ob die Brandung am Sockel gezeigt wird (sonst der Brandungsring aus ocean). */
    boolean brandungAn() { return brandung.an && brandungOk; }

    /**
     * Phase 17: die Brandung am Sockel einen Schritt weiter. Seegang und Richtung kommen aus dem Meer, der Wasserstand
     * aus der Verdrahtung; die Lippen der Sturzbrecher werfen Gischt ({@code spray.impact}), jeder Brecher klingt.
     */
    private void stepBrandung(double dt) {
        if (!brandungAn()) return;
        if (brandung.onAufschlag == null) {
            brandung.see = (x, z) -> ocean.heightAt(x, z);
            brandung.onAufschlag = (x, y, z, v, sp) -> { if (showSpray) spray.impact(x, y, z, v, sp); };
            brandung.onBrecher = (x, z, hb, art) -> {
                SoundEvent e = wiring != null ? wiring.sound : null;
                if (e != null) e.sound(SoundEvent.Kind.BRECHER, x, Math.max(0, hb * 0.5), z, Math.min(1, 0.25 + hb / 4));
            };
        }
        Ocean.State os = ocean.state();
        double level = wiring != null ? wiring.waterLevel.level() : 0;
        brandung.schritt(frozen && dt > 1 ? 0 : dt, level, os.totalHs(), os.peakPeriod, ocean.windFromDeg);
    }

    /** Phase 17: für Standbilder das Stück der Brandung jetzt bauen (Seegang und Wasserstand von jetzt, im aufrufenden Faden). */
    public void brandungJetzt() {
        if (!brandungAn()) return;
        if (brandung.see == null) brandung.see = (x, z) -> ocean.heightAt(x, z);
        if (wiring != null) wiring.feed(ocean);
        Ocean.State os = ocean.state();
        brandung.jetzt(wiring != null ? wiring.waterLevel.level() : 0, os.totalHs(), os.peakPeriod, ocean.windFromDeg);
    }

    /** Phase 17: die Brandung am Sockel in einer Zeile (für Bilder und HUD). */
    public String surfDescribe() {
        if (!brandungAn()) return String.format(Locale.GERMAN, "Brandungsring aus ocean, H_s %.1f m", ocean.state().totalHs());
        return brandung.meldung;
    }

    /** Phase 17: Licht für die Brecher, vorbelichtet, lokal zum Stück gedreht (Sonne in seinen Achsen). */
    private com.dan.surf.gl.BreakerGl.Light brandungLicht(Sky.State st, double[] sun, double pre, com.dan.lighthouse.scene.Sockelbrandung.Stueck s) {
        com.dan.surf.gl.BreakerGl.Light l = new com.dan.surf.gl.BreakerGl.Light();
        double[][] L = lifeLight(st, sun, pre);
        boolean day = sun[1] > -0.02;
        double[] key = day ? sun : (skyOk ? st.moonDir : sun);
        l.sunDir = new double[]{key[0] * s.ux + key[2] * s.uz, key[1], key[0] * s.ax + key[2] * s.az};
        l.sunColor = day ? L[0] : L[1];
        l.skyTop = L[2];
        l.skyHorizon = L[3];
        l.fogColor = L[3];
        l.fogDistance = skyOk ? fogDistance(st) : 40_000;
        // Nordsee vor der Weser: grau-grün, über der Schüttung trüb [G]
        l.waterDeep = new double[]{0.018, 0.055, 0.060};
        l.waterShallow = new double[]{0.06, 0.15, 0.13};
        l.sand = new double[]{0.28, 0.26, 0.22};
        l.display = 0;
        return l;
    }

    /** Phase 6: das Meer als Quelle, Ereignisse in Ringwellen und Nässe übersetzen. */
    private void initSpray() {
        spray.quality = quality.spray;
        spray.focusRadius = 150;
        spray.samplesPer10m = 6;
        spray.sea = new com.dan.spray.Sea() {
            @Override public double height(double x, double z) { return ocean.heightAt(x, z); }
            @Override public double[] velocity(double x, double z) { return ocean.velocityAt(x, z); }
            @Override public double level() { return wiring != null ? wiring.waterLevel.level() : 0; }
        };
        spray.onReentry = (x, z, s) -> ocean.addImpulse(x, z, 0.3 + 0.6 * s);
        spray.onHit = (x, y, z, mass, speed, kind) -> {
            // nur der Turm hat eine Nässekarte (der Peilpfahl nicht)
            if (Math.hypot(x, z) > Turm.SOCKEL_R0 + 1) return;
            double u = Math.atan2(x, -z) / (2 * Math.PI) + 0.5, v = (y - weathering.mapBottom) / weathering.mapHeightM;
            if (kind == com.dan.spray.Parcel.MIST) weathering.wet(u, v, 2.5, 0.12);
            else if (kind == com.dan.spray.Parcel.WASH) weathering.wet(u, v, 3.0, 0.3);
            else weathering.wet(u, v, 1.0 + Math.min(2.5, mass * 0.04), Math.min(1, 0.25 + mass * 0.02));
        };
    }

    private void stepSpray(double dt) {
        if (wiring == null || !showSpray) return;
        long t0 = System.nanoTime();
        sprayPlate = wiring.feedBodies(spray, sprayPlate, sprayFirst);
        sprayFirst = false;
        wiring.feed(spray, ocean.state().totalHs());
        spray.step(dt);
        sprayCpuMs = sprayCpuMs * 0.95 + (System.nanoTime() - t0) / 1e6 * 0.05;
    }

    /** Phase 7: Regen aus Hand-Regen, Wind und Wolken; das Auge ist die Mitte des Tropfenkastens. */
    private void stepRain(double dt) {
        if (wiring == null) return;
        long t0 = System.nanoTime();
        wiring.feed(rain, clouds, clouds.snapshot(), cloudsOk, camera.eye.clone());
        if (!showRain) rain.rate = 0;
        rain.step(dt);
        rainCpuMs = rainCpuMs * 0.95 + (System.nanoTime() - t0) / 1e6 * 0.05;
    }

    public String rainCheck() { return rainCheck; }

    private boolean neighbourOn() {
        for (Beacon n : neighbours) if (n.isOn()) return true;
        return false;
    }

    /**
     * Phase 15: einen anderen Turm wählen (F36 A). light übernimmt Kennung, Sektoren und Optik; das Feuer bleibt in
     * der Laterne des Roten Sands. lat/lon setzen Sonne, Mond und Sterne; tides = Gezeiten rechnen (in der Ostsee aus).
     * others sind die Nachbarfeuer mit ihrer Lage relativ zum gewählten Turm (Meter, x Ost, z Süd) und der Feuerhöhe
     * in y; beim Roten Sand selbst (home) gelten die Nachbarn aus Phase 9 mit der Alten Weser am Horizont.
     */
    public synchronized void setSite(String name, double lat, double lon, boolean tides, boolean home, Beacon light, Beacon[] others) {
        siteName = name;
        homeSite = home;
        beacon.copyLight(light);
        clock.lat = lat;
        clock.lon = lon;
        sky.latDeg = lat;
        sky.lonDeg = lon;
        sky.domes.clear();
        if (home) for (Site.Town t : Site.TOWNS) sky.domes.add(new Sky.LightDome(t.name(), t.azimuthDeg(), t.distanceKm(), t.strength()));
        if (wiring != null) {
            if (!tides) { wiring.tide.on = false; wiring.tide.fixed = 0; }
            else wiring.tide.on = true;
        }
        if (home) {
            neighbours = new Beacon[]{Beacon.alteWeser(), Beacon.tegelerPlate()};
            placeNeighbours();
        } else {
            neighbours = others;
        }
        Wiring.attractors(lightning, home ? new double[]{neighbours[0].x, neighbours[0].y + 4.5, neighbours[0].z} : null);
    }

    /** Alte Weser (Turm am Horizont) und Tegeler Plate an ihre Orte, mit der Absenkung durch die Erdkrümmung. */
    private void placeNeighbours() {
        double[] aw = Turm.alteWeserPos();
        neighbours[0].x = aw[0];
        neighbours[0].z = aw[1];
        neighbours[0].y = 33 + Wiring.MHW - aw[2];
        double tx = Site.x(TEGELER_LON), tz = Site.z(TEGELER_LAT);
        neighbours[1].x = tx;
        neighbours[1].z = tz;
        neighbours[1].y = 20.9 + Wiring.MHW - Site.curvatureDrop(Math.hypot(tx, tz));
    }

    /** Tegeler Plate: 53°47′50″ N, 8°11′28″ E (Verzeichnis). */
    public static final double TEGELER_LAT = 53 + 47 / 60.0 + 50 / 3600.0, TEGELER_LON = 8 + 11 / 60.0 + 28 / 3600.0;

    /**
     * Phase 10: das Gewitter in Szenenzeit weiter (mit Zeitlupe), Sichtbarkeit neu, wenn das Auge weit gesprungen ist,
     * und wie weit die Belichtung im Blitz nachgibt.
     */
    private void stepLightning(double dt) {
        long t0 = System.nanoTime();
        lightning.quality = quality.lightning;
        if (wiring != null) wiring.feed(lightning, clouds.snapshot(), cloudsOk, camera.eye);
        double sceneDt = dt * Math.max(0, clock.lapse);
        int before = lightning.flashes();
        lightning.step(Math.min(sceneDt, 1.0) * lightningSlow);
        double[] e = camera.eye;
        if (lightningSeenFrom == null || Math.hypot(e[0] - lightningSeenFrom[0], e[2] - lightningSeenFrom[2]) > 300 || Math.abs(e[1] - lightningSeenFrom[1]) > 100) {
            lightning.updateVisibility();
            lightningGl.invalidate();
            lightningSeenFrom = e.clone();
        }
        if (lightning.flashes() != before && !lightning.bolts().isEmpty()) {
            Bolt b = lightning.bolts().get(lightning.bolts().size() - 1);
            lastBoltText = boltText(b);
        }
        double base = exposureBase();
        Lightning.Snapshot s = lightning.snapshot();
        double lf = showLightning ? s.flashLuminance(e[0], e[1], e[2], lightning.cloudBase) : 0;
        double give = 1 + base * lf / 1.5;
        flashGive = frozen ? give : Math.max(give, flashGive * Math.exp(-Math.max(0, dt) / 0.25));
        lightningCpuMs = lightningCpuMs * 0.95 + (System.nanoTime() - t0) / 1e6 * 0.05;
    }

    /** Belichtung ohne das Nachgeben im Blitz. */
    private double exposureBase() { return exposure() * flashGive; }

    /** „Erdblitz 3,4 km NW, 31 kA, 4 Schläge“ */
    private String boltText(Bolt b) {
        double[] e = camera.eye;
        double d = Math.hypot(b.hitX - e[0], b.hitZ - e[2]), az = (Math.toDegrees(Math.atan2(b.hitX - e[0], -(b.hitZ - e[2]))) + 360) % 360;
        String[] dirs = {"N", "NO", "O", "SO", "S", "SW", "W", "NW"};
        String kind = switch (b.kind) {
            case CLOUD_GROUND -> b.attractor >= 0 ? "Einschlag in " + lightning.attractors.get(b.attractor).name() : "Erdblitz";
            case INTRA_CLOUD -> "Wolkenblitz";
            case SPIDER -> "Spinnenblitz";
            case SHEET -> "Wetterleuchten";
        };
        double ka = 0;
        for (double k : b.strokeKA) ka = Math.max(ka, k);
        return String.format(Locale.GERMAN, "%s %.1f km %s, %.0f kA, %d %s", kind, d / 1000, dirs[(int) Math.round(az / 45) % 8], ka, b.strokeTime.length,
                b.kind == Bolt.Kind.CLOUD_GROUND ? (b.strokeTime.length == 1 ? "Schlag" : "Schläge") : "Pulse");
    }

    /** Auslöschung und Streuung der Luft für das Licht der Blitze (Dunst und Regen am Auge), 1/m. */
    private double[] lightningAir() {
        double haze = wiring != null ? wiring.hazePerM() : 7.5e-5;
        double[] e = camera.eye;
        double r = rainOk && showRain ? rain.scatter(e[0], e[1], e[2]) * 3e-3 : 0;
        return new double[]{haze + r, haze * 0.9 + r * 0.98};
    }

    public String lightningCheck() { return lightningCheck; }

    /** Blitz von Hand: Erdblitz in Blickrichtung (2 bis 5 km), in den Turm, Wolkenblitz oder Wetterleuchten. */
    public Bolt triggerLightning(Bolt.Kind kind, boolean tower) {
        if (wiring != null) wiring.feed(lightning, clouds.snapshot(), cloudsOk, camera.eye);
        double[] e = camera.eye, f = camera.fwd;
        double h = Math.hypot(f[0], f[2]);
        double fx = h > 1e-6 ? f[0] / h : 0, fz = h > 1e-6 ? f[2] / h : -1;
        if (tower) return lightning.strikeAttractor(0);
        double r = switch (kind) {
            case SHEET -> 45_000;
            case INTRA_CLOUD -> 5000;
            default -> 2500 + Math.random() * 2500;
        };
        Bolt b = kind == Bolt.Kind.CLOUD_GROUND ? lightning.strike(e[0] + fx * r, e[2] + fz * r) : lightning.discharge(kind, e[0] + fx * r, e[2] + fz * r);
        lastBoltText = boltText(b);
        return b;
    }

    /** „Blitz kräftig · 12 Blitze, 4 Erdblitze, 1 in den Turm · zuletzt … · Donner in 4,1 s · Prozessor … · GPU = CPU …“ */
    public String lightningLine() {
        if (!lightningOk) return "!Blitz: nicht gestartet";
        Lightning.Thunder next = null;
        for (Lightning.Thunder th : lightning.pendingThunder()) if (next == null || th.time() < next.time()) next = th;
        Lightning.Thunder last = lightning.lastThunder();
        String th = next != null ? String.format(Locale.GERMAN, " · Donner in %.1f s", next.time() - lightning.time())
                : last != null && lightning.time() - last.time() < last.duration() ? String.format(Locale.GERMAN, " · es donnert, %.0f dB%s", last.levelDb(), last.crack() ? ", Knall" : "") : "";
        String level = switch (lightning.level()) {
            case OFF -> "aus";
            case DISTANT -> "Wetterleuchten";
            case MODERATE -> "mäßig";
            case STRONG -> "kräftig";
            case SEVERE -> "schwer";
        };
        return String.format(Locale.GERMAN, "Blitz %s · %d Blitze, %d Erdblitze, %d in Türme%s%s · Prozessor %.2f ms · %s%s", level, lightning.flashes(), lightning.groundFlashes(),
                lightning.attractorHits(), lastBoltText.isEmpty() ? "" : " · zuletzt " + lastBoltText, th, lightningCpuMs,
                lightningCheck.isEmpty() ? "Abgleich beim ersten Blitz" : lightningCheck, lightningSlow < 0.99 ? String.format(Locale.GERMAN, " · Zeitlupe 1 : %.0f", 1 / lightningSlow) : "");
    }

    /**
     * Phase 9: das Feuer in Szenenzeit weiter (Dämmerungsschalter aus der Helligkeit am Turm), die Nachbarfeuer mit, und
     * das Streugitter um das Feuer aus Regen und Gischt in Portionen von höchstens 1 ms nachfüllen.
     */
    private void stepBeacon(Sky.State st, double dt) {
        long t0 = System.nanoTime();
        double sceneDt = dt * Math.max(0, clock.lapse);
        // Phase 11: in lx (bis Phase 10 standen hier Einheiten der Szene, 1 = 43 966 lx; der Dämmerungsschalter sah
        // dadurch auch mittags nur 2 bis 3 „lx“ und das Feuer brannte den ganzen Tag)
        double lux = daylightLux(st, cloudShade);
        beaconLux = lux;
        aussen.schalten(dt, lux, clock.hour());
        if (wiring != null) wiring.feed(beacon, lux);
        beacon.step(Math.max(dt, sceneDt));
        for (Beacon n : neighbours) {
            n.ambientLux = lux;
            n.hazePerM = beacon.hazePerM;
            n.step(Math.max(dt, sceneDt));
        }
        Beacon.Quality q = quality.beacon;
        if (beaconGrid == null || beaconGrid.nAz != q.gridAz || beaconGrid.nEl != q.gridEl || beaconGrid.nR != q.gridR)
            beaconGrid = new ScatterGrid(q.gridAz, q.gridEl, q.gridR);
        if (beacon.isOn() && wiring != null) {
            Beacon.Snapshot s = beacon.snapshot();
            beaconGrid.update(Wiring.scatter(rainOk && showRain ? rain : null, sprayOk && showSpray ? spray : null, fogNow() ? fog : null)::density, s.x(), s.y(), s.z(), simThreaded ? 1.0 : 0);
        }
        beaconCpuMs = beaconCpuMs * 0.95 + (System.nanoTime() - t0) / 1e6 * 0.05;
    }

    /** Streugitter sofort ganz füllen (Standbilder). */
    public void fillBeaconGrid() {
        Beacon.Snapshot s = beacon.snapshot();
        if (beaconGrid == null) beaconGrid = new ScatterGrid(quality.beacon.gridAz, quality.beacon.gridEl, quality.beacon.gridR);
        beaconGrid.fill(Wiring.scatter(rainOk && showRain ? rain : null, sprayOk && showSpray ? spray : null, fogNow() ? fog : null)::density, s.x(), s.y(), s.z());
    }

    public String beaconCheck() { return beaconCheck; }

    /** „Feuer Roter Sand 1985 · F WRG · an · 835 cd hier · Tragweite …“ */
    public String beaconLine() {
        if (!beaconOk) return "!Feuer: nicht gestartet";
        Beacon.Snapshot s = beacon.snapshot();
        double[] e = camera.eye;
        double dx = e[0] - s.x(), dy = e[1] - s.y(), dz = e[2] - s.z(), d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double cd = LightColour.luminance(s.intensity(dx / d, dy / d, dz / d));
        double rr = rainOk && showRain ? rain.rateAt(0, 0) : 0;
        // Phase 13: dazu der Seenebel am Auge
        double fv = fogNow() ? fog.visibilityAt(e[0], e[1], e[2]) / 1000 : 1e9;
        double visKm = 1 / (1 / Math.max(0.1, wiring != null ? wiring.visibilityKm : 40) + (rr > 0 ? 1 / Rain.visibilityKm(rr) : 0) + 1 / Math.max(0.01, fv));
        double visNm = visKm / 1.852;
        double eyeH = Math.max(0, e[1] - (wiring != null ? wiring.waterLevel.level() : 0));
        double nom = beacon.sectors().maxRangeNm();
        double peak = s.maxCd() * (s.optic() == Beacon.Optic.ROTATING ? s.gain() : 1);
        double eff = s.optic() == Beacon.Optic.ROTATING ? com.dan.beacon.Range.effective(peak, s.flashSeconds()) : peak;
        String chk = beaconCheck.isEmpty() ? "Abgleich folgt" : beaconCheck.replace("Grafikkarte = Prozessor: ", "GPU = CPU ").replaceAll(" \\(Median\\), größte [^;]*", "").replaceAll(" \\(Median\\).*", "");
        return String.format(Locale.GERMAN, "Feuer %s · %s · %s · hier %,.0f cd · Tragweite %.0f sm, bei Sicht %.1f sm: %.1f sm, Kimm %.1f sm · Prozessor %.2f ms · %s",
                s.name(), beacon.characteristic().text, beacon.isOn() ? "an" : String.format(Locale.GERMAN, "aus (%.0f lx)", beaconLux), cd, nom, visNm,
                com.dan.beacon.Range.luminousRangeAtVisibility(eff, visNm), com.dan.beacon.Range.geographicRange(s.y(), eyeH), beaconCpuMs, chk);
    }

    /** Steht das Auge im Laternenraum? */
    public boolean insideLantern() {
        double[] e = camera.eye;
        return Math.hypot(e[0], e[2]) < Turm.LANTERN_R - 0.05 && e[1] > Turm.GALLERY_TOP && e[1] < Turm.DOME_TOP;
    }

    /** Steht das Auge nah am Handlauf der Galerie (draußen auf der Galerie oder drinnen hinter dem Glas)? */
    public boolean nearRail() {
        double[] e = camera.eye;
        return !insideTower() && Math.abs(Math.hypot(e[0], e[2]) - Turm.HANDRAIL_RING) < 4 && Math.abs(e[1] - Turm.HANDRAIL_Y) < 5;
    }

    /**
     * Phase 8: Die Tropfen auf den Scheiben rechnen nur, solange das Auge im Laternenraum steht, die auf dem Handlauf,
     * solange es in seiner Nähe ist. Kommt es heran, holen sie eine halbe Minute nach, damit alles schon nass ist.
     * Phase 8b: Regen, Wind, Beschlag, Salz und Überspülung werden hier im Bildfaden eingespeist; gerechnet wird in
     * einem eigenen Faden ({@link #simThreaded}), das Bild nimmt den letzten fertigen Zustand.
     */
    private void stepGlass(double dt) {
        if (wiring == null || !showGlass) return;
        boolean in = insideLantern(), near = nearRail();
        if (!in && !near) { wasInside = wasNear = false; return; }
        pendG = in;
        pendR = near;
        long t0 = System.nanoTime();
        wiring.feed(glass, rain);
        wiring.feedRail(rail, railShape, rain);
        Sky.State st = skyOk ? skyState() : null;
        fogDiff = wiring.feedFog(glass, rain, lampOn, st != null ? st.sunAlt : 30);
        com.dan.spray.Spray sp = sprayOk && showSpray ? spray : null;
        wiring.feedSalt(glass, rail, sp);
        double fl = wiring.feedFlood(sp, glass, rail, ocean.state().totalHs() * 0.6);
        if (fl > 0) { lastFloodMm = fl; lastFloodT = (clock.epochMillis() / 1000.0); }
        double extra = (in && !wasInside) || (near && !wasNear) ? 30 : 0;
        wasInside = in;
        wasNear = near;
        glassCpuMs = glassCpuMs * 0.95 + (System.nanoTime() - t0) / 1e6 * 0.05;
        // Phase 13: der Auftrag geht mit dem der Brücke zusammen hinaus (stepLife)
        pendExtra = extra;
    }

    /** Einen Auftrag an den Faden der Tropfen geben (oder gleich rechnen). */
    private void post(double dt, double extra, boolean g, boolean r, boolean b) {
        if (!simThreaded) { runSims(dt + extra, g, r, b); return; }
        synchronized (simLock) {
            // hinkt der Faden hinterher, fällt Zeit weg (höchstens 2 s stehen aus)
            simDt = Math.min(2, simDt + dt);
            simExtra += extra;
            simGlass |= g;
            simRail |= r;
            simBridge |= b;
            simLock.notifyAll();
        }
        if (simThread == null) {
            simThread = new Thread(() -> {
                while (true) {
                    double d;
                    boolean gg, rr, bb;
                    synchronized (simLock) {
                        while (simDt + simExtra <= 0) {
                            try { simLock.wait(); } catch (InterruptedException e) { return; }
                        }
                        d = simDt + simExtra;
                        gg = simGlass;
                        rr = simRail;
                        bb = simBridge;
                        simDt = simExtra = 0;
                        simGlass = simRail = simBridge = false;
                    }
                    runSims(d, gg, rr, bb);
                }
            }, "Lighthouse-Tropfen");
            simThread.setDaemon(true);
            simThread.start();
        }
    }

    private volatile boolean simBridge;

    private void runSims(double dt, boolean g, boolean r, boolean b) {
        long t0 = System.nanoTime();
        if (g) for (Wetglass w : glass) w.step(dt);
        if (r) rail.step(dt);
        if (b) bridgeGlass.step(dt);
        double ms = (System.nanoTime() - t0) / 1e6;
        // je Sekunde Szenenzeit: was der Faden für eine Sekunde braucht, durch die Bilder je Sekunde ist sein Anteil am Bild
        simMs = simMs * 0.9 + ms * 0.1;
    }

    /** Das Auge frei an (ex, ey, ez) stellen und auf (tx, ty, tz) richten. */
    public void lookFrom(double ex, double ey, double ez, double tx, double ty, double tz) {
        double dx = tx - ex, dy = ty - ey, dz = tz - ez, l = Math.sqrt(dx * dx + dy * dy + dz * dz);
        camera.place(ex, ey, ez, Math.toDegrees(Math.atan2(dx, -dz)), Math.toDegrees(Math.asin(-dy / l)));
    }

    /**
     * Auf die Galerie, nah an den Handlauf auf der Wetterseite: von innerhalb des Geländers schräg auf das Rohr, so dass
     * Oberseite und Unterseite zu sehen sind (vierfach vergrößert).
     */
    public void toRail(double windFromDeg) {
        double a = Math.toRadians(windFromDeg + 5), t = Math.toRadians(windFromDeg);
        zoom = 4;
        lookFrom(Math.sin(a) * 3.8, Turm.HANDRAIL_Y + 0.05, -Math.cos(a) * 3.8,
                Math.sin(t) * Turm.HANDRAIL_RING, Turm.HANDRAIL_Y, -Math.cos(t) * Turm.HANDRAIL_RING);
    }

    /** Ein Brecher über die Galerie von Hand: Seewasser über Handlauf und Scheiben der Wetterseite. */
    public void floodByHand(double mm) {
        glass[0].flood(mm, true);
        glass[1].flood(mm * 0.5, true);
        rail.flood(mm, true);
        lastFloodMm = mm;
        lastFloodT = clock.epochMillis() / 1000.0;
    }

    /** Beschlag innen auf allen Scheiben wegwischen (Knopf im Bedienfeld). */
    public void wipeFog() { for (Wetglass g : glass) g.wipeAllInside(); }

    /** Handlauf trocken wischen. */
    public void wipeRail() { rail.wipeAll(); }

    /** Alle Scheiben wischen (Knopf im Bedienfeld). */
    public void wipeGlass() { for (Wetglass g : glass) g.wipeAll(); }

    public String glassCheck() { return glassCheck; }

    /** „Handlauf 1.234 Tropfen (12 laufen), 48 abgefallen · Salz … · eigener Faden …“ */
    public String railLine() {
        String r = String.format(Locale.GERMAN, "Handlauf %,d Tropfen (%d laufen), %,d abgefallen", rail.count(), rail.moving(), rail.totals()[4]);
        double salt = rail.saltMean() * Wetglass.SALT_FULL;
        if (salt > 0.05) r += String.format(Locale.GERMAN, " · Salz %.1f g/m²", salt);
        if ((clock.epochMillis() / 1000.0) - lastFloodT < 30) r += String.format(Locale.GERMAN, " · überspült %.1f mm", lastFloodMm);
        return r + String.format(Locale.GERMAN, " · eigener Faden %.1f ms je Auftrag", simMs);
    }

    /** „Scheiben Luv 2.812 Tropfen (14 laufen) · Seite … · Lee … · Prozessor 0,8 ms · GPU = CPU ± 0 µm“ */
    public String glassLine() {
        if (!glassOk) return "!Scheiben: nicht gestartet";
        if (!insideLantern()) return nearRail() ? railLine() : "Scheiben: rechnen nur im Laternenraum, der Handlauf nur auf der Galerie";
        StringBuilder b = new StringBuilder("Scheiben");
        for (int k = 0; k < glass.length; k++)
            b.append(k == 0 ? " " : " · ").append(GLASS_NAMES[k]).append(' ').append(String.format(Locale.GERMAN, "%,d (%d laufen, Fein %.0f %%)",
                    glass[k].count(), glass[k].moving(), glass[k].fineMean() * 100));
        b.append(String.format(Locale.GERMAN, " · Beschlag %.0f %% (Taupunkt %+.1f K)", glass[0].fogMean() * 100, fogDiff));
        double salt = glass[0].saltMean() * Wetglass.SALT_FULL;
        if (salt > 0.05) b.append(String.format(Locale.GERMAN, " · Salz %.1f g/m²", salt));
        if ((clock.epochMillis() / 1000.0) - lastFloodT < 30) b.append(String.format(Locale.GERMAN, " · überspült %.1f mm", lastFloodMm));
        String chk = glassCheck.isEmpty() ? "Abgleich folgt" : glassCheck.replace("Grafikkarte = Prozessor: Wasserhöhe ±", "GPU = CPU ±").replaceAll(" \\(Median\\).*", "");
        return b + String.format(Locale.GERMAN, " · eigener Faden %.1f ms je Auftrag, Bildfaden %.2f ms · %s", simMs, glassCpuMs, chk);
    }

    /**
     * Die zwölf Scheiben als je zwei flache Hälften (über und unter dem Kämpfer), etwas innerhalb des Glases, damit
     * sie vor den Sprossen liegen. Ecken: Lage, Ort auf der Scheibe in m (x von links nach rechts von innen gesehen).
     */
    private void buildPanes(GL4 gl) {
        float[] v = new float[24 * 6 * 5];
        double r = Turm.LANTERN_R - 0.012, mid = (Turm.GLASS_BOTTOM + Turm.GLASS_TOP) / 2;
        double chord = 2 * r * Math.sin(Math.toRadians(15));
        int o = 0;
        for (int i = 0; i < 12; i++) {
            double az = 30 * i;
            for (int half = 0; half < 2; half++) {
                double y0 = half == 0 ? Turm.GLASS_BOTTOM : mid + 0.04, y1 = half == 0 ? mid - 0.04 : Turm.GLASS_TOP;
                double al = Math.toRadians(az - 15), ar = Math.toRadians(az + 15);
                double[] pl = {Math.sin(al) * r, -Math.cos(al) * r}, pr = {Math.sin(ar) * r, -Math.cos(ar) * r};
                double[][] c = {{pl[0], y0, pl[1], 0, 0}, {pr[0], y0, pr[1], chord, 0}, {pr[0], y1, pr[1], chord, y1 - y0},
                        {pl[0], y0, pl[1], 0, 0}, {pr[0], y1, pr[1], chord, y1 - y0}, {pl[0], y1, pl[1], 0, y1 - y0}};
                for (double[] q : c) for (double x : q) v[o++] = (float) x;
                int k = i * 2 + half;
                panes[k] = new double[]{az, (0.137 * k + 0.05 * half) % 0.35, (0.211 * k + 0.17 * half) % 0.35};
            }
        }
        int[] t = new int[1];
        gl.glGenVertexArrays(1, t, 0);
        paneVao = t[0];
        gl.glGenBuffers(1, t, 0);
        paneVbo = t[0];
        gl.glBindVertexArray(paneVao);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, paneVbo);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, v.length * 4L, Buffers.newDirectFloatBuffer(v), GL.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 20, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 20, 12);
        gl.glBindVertexArray(0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }

    /** Welche Kachel eine Scheibe nimmt (0 Luv, 1 Seite, 2 Lee) nach dem Winkel zwischen ihr und dem Wind. */
    static int glassSide(double paneAz, double windFromDeg) {
        double d = Math.abs(((paneAz - windFromDeg) % 360 + 540) % 360 - 180);
        return d <= 60 ? 0 : d <= 120 ? 1 : 2;
    }

    /** Phase 8b: Tropfen, nasser Lack und Salz über den schon gezeichneten Handlauf (gleiche Ecken, gleiche Tiefe). */
    private void drawRail(GL4 gl, float[] vp, float[] inv, double[] sun, double level) {
        railGl.update(gl, rail.snapshot());
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(reversedZ ? GL.GL_GEQUAL : GL.GL_LEQUAL);
        gl.glDepthMask(false);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDisable(GL.GL_CULL_FACE);
        common(gl, railShader, vp, inv, sun, level);
        railGl.bind(gl, railShader.id(), 9);
        railShader.set(gl, "uRail", (float) Turm.HANDRAIL_RING, (float) Turm.HANDRAIL_Y, (float) Turm.HANDRAIL_T, (float) (wiring != null ? wiring.wind.fromDeg() : 240));
        railShader.set(gl, "uStrength", (float) Math.max(0, Math.min(1, glassStrength)));
        Turm.Part hp = turm.part("Handlauf");
        towerMesh.drawRange(gl, hp.first(), hp.count());
        gl.glBindVertexArray(emptyVao);
        gl.glDisable(GL.GL_BLEND);
        gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
        gl.glDepthMask(true);
    }

    /** Phase 8: die Scheiben von innen über das fertige Bild (eine Kopie wird gebrochen). */
    private void drawGlass(GL4 gl, Sky.State st, float[] vp, float[] inv, int w, int h, double[] sun, double level, double pre) {
        copyScene(gl, w, h);
        for (int k = 0; k < glass.length; k++) glassGl[k].update(gl, glass[k].snapshot());
        if (glassCheck.isEmpty() && glass[0].count() > 100) glassCheck = glassGl[0].selfCheck(gl, glass[0].snapshot());
        glassSetup(gl, st, vp, inv, w, h, sun, level, pre, 1.5e-3);
        double from = wiring != null ? wiring.wind.fromDeg() : 240;
        double[] wt = wiring != null ? wiring.wind.toVector() : new double[]{0.7, 0, -0.7};
        gl.glBindVertexArray(paneVao);
        for (int k = 0; k < 24; k++) {
            double az = panes[k][0], a = Math.toRadians(az);
            int side = glassSide(az, from);
            double[] right = {Math.cos(a), 0, Math.sin(a)};
            double flip = side == 1 ? Math.signum(wt[0] * right[0] + wt[2] * right[2]) : (k % 4 < 2 ? 1 : -1);
            if (flip == 0) flip = 1;
            glassGl[side].bind(gl, glassShader.id(), 9);
            glassShader.set(gl, "uOff", (float) panes[k][1], (float) panes[k][2]);
            glassShader.set(gl, "uFlip", (float) flip);
            glassShader.set(gl, "uPaneN", (float) Math.sin(a), 0f, (float) -Math.cos(a));
            glassShader.set(gl, "uPaneT", (float) (right[0] * flip), 0f, (float) (right[2] * flip));
            gl.glDrawArrays(GL.GL_TRIANGLES, k * 6, 6);
        }
        gl.glBindVertexArray(emptyVao);
        gl.glDepthMask(true);
    }

    /** Kopie des HDR-Bilds mit Mipmaps (die Scheiben brechen sie). */
    private void copyScene(GL4 gl, int w, int h) {
        if (glassCopyTex == 0 || glassCopyW != w || glassCopyH != h) {
            Gpu.deleteFbos(gl, glassCopyFbo);
            Gpu.deleteTextures(gl, glassCopyTex);
            glassCopyTex = Gpu.texture(gl, w, h, GL.GL_RGBA16F, true);
            glassCopyFbo = Gpu.fbo(gl, glassCopyTex, 0);
            glassCopyW = w;
            glassCopyH = h;
        }
        gl.glBindFramebuffer(GL4.GL_READ_FRAMEBUFFER, hdrFbo);
        gl.glBindFramebuffer(GL4.GL_DRAW_FRAMEBUFFER, glassCopyFbo);
        gl.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL.GL_COLOR_BUFFER_BIT, GL.GL_NEAREST);
        gl.glBindTexture(GL.GL_TEXTURE_2D, glassCopyTex);
        gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }

    /** Gemeinsame Einstellungen des Scheiben-Shaders (Laternenraum und Brücke); lampGlow: Licht der Raumlampe nachts. */
    private void glassSetup(GL4 gl, Sky.State st, float[] vp, float[] inv, int w, int h, double[] sun, double level, double pre, double lampGlow) {
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
        gl.glDepthMask(false);
        gl.glDisable(GL.GL_BLEND);
        gl.glDisable(GL.GL_CULL_FACE);
        common(gl, glassShader, vp, inv, sun, level);
        gl.glActiveTexture(GL.GL_TEXTURE8);
        gl.glBindTexture(GL.GL_TEXTURE_2D, glassCopyTex);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        glassShader.set(gl, "uScene", 8);
        glassShader.set(gl, "uViewport", (float) w, (float) h);
        glassShader.set(gl, "uLens", 0.3f);
        glassShader.set(gl, "uStrength", (float) Math.max(0, Math.min(1, glassStrength)));
        glassShader.set(gl, "uFogStrength", wiring == null || wiring.fogOn ? 1f : 0f);
        glassShader.set(gl, "uWiper", 0f, 0f, 0f, 0f);
        double sunAltG = skyOk ? st.sunAlt : clock.sun()[0];
        double glowG = lampOn ? lampGlow * (1 - smooth(-1, 2, sunAltG)) : 0;
        glassShader.set(gl, "uLampGlow", (float) (skyOk ? glowG * exposure() : glowG * 300));
        glassShader.set(gl, "uRoom", skyOk ? scale(st.skyIrradiance, pre / Math.PI * 0.05) : new double[]{0.02, 0.02, 0.02});
        bindLightning(gl, glassShader);
    }

    // ------------------------------------------------------------------ Phase 13: Leben und Nebel

    /** Maße der drei Frontscheiben eines Kümos: {linke Kante q, Breite aller drei, Unterkante y, Höhe}. */
    static double[] bridgePanes() {
        Ship k = Ship.coaster("Maß");
        double[] a = k.windows[0], c = k.windows[2];
        double q0 = a[1] - a[2] / 2, q1 = c[1] + c[2] / 2;
        return new double[]{q0, q1 - q0, a[3], a[4] - a[3]};
    }

    private void initLife(GL4 gl) {
        try {
            fogGl.init(gl);
            fogOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Nebel nicht verfügbar: " + e.getMessage());
            errorListener.accept("Der Seenebel (com.dan.fog) lässt sich nicht starten.\n" + e.getMessage());
        }
        try {
            buoyancyGl.init(gl);
            buoyancyOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Schwimmkörper nicht verfügbar: " + e.getMessage());
            errorListener.accept("Tonnen und Schiffe (com.dan.buoyancy) lassen sich nicht starten.\n" + e.getMessage());
        }
        try {
            flockGl.init(gl);
            flockOk = true;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Möwen nicht verfügbar: " + e.getMessage());
            errorListener.accept("Die Möwen (com.dan.flock) lassen sich nicht starten.\n" + e.getMessage());
        }
        try {
            bridgeGlassGl.init(gl);
            int[] t = new int[1];
            gl.glGenVertexArrays(1, t, 0);
            bridgeVao = t[0];
            gl.glGenBuffers(1, t, 0);
            bridgeVbo = t[0];
            gl.glBindVertexArray(bridgeVao);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bridgeVbo);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, 3 * 6 * 5 * 4L, null, GL.GL_DYNAMIC_DRAW);
            gl.glEnableVertexAttribArray(0);
            gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 20, 0);
            gl.glEnableVertexAttribArray(1);
            gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 20, 12);
            gl.glBindVertexArray(0);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            bridgeGlassOk = glassOk;
        } catch (RuntimeException e) {
            System.err.println("Lighthouse: Brückenscheiben nicht verfügbar: " + e.getMessage());
        }
        fog.quality = quality.fog;
        fog.seed = 1885;
        buoyancy.quality = quality.buoyancy;
        buoyancy.seed = 1885;
        flock.quality = quality.flock;
        flock.seed = 1885;
        if (wiring != null) {
            wiring.signals(fog);
            wiring.setup(buoyancy, ocean);
            wiring.setup(flock, buoyancy);
        }
        // eine Kachel wie im Laternenraum (fein genug für Tropfen von Millimetern), versetzt je Scheibe; der Wischer
        // wischt im Shader (der Sektor seines Blatts), die Tropfen der Kachel bleiben für die anderen Scheiben
        soundscape.quality = quality.sound;
        soundscape.seed = 1885;
        if (wiring != null) {
            wiring.soundscape = soundscape;
            wiring.sound = wiring.soundTo(soundscape);
            spray.onSound = (x, y, z, st) -> { SoundEvent e = wiring.sound; if (e != null) e.sound(SoundEvent.Kind.BRECHER, x, y, z, st); };
        }
        bridgeGlass.periodic = true;
        bridgeGlass.width = 0.45;
        bridgeGlass.height = 0.45;
        bridgeGlass.drainLength = BRIDGE_PANES[3];
        bridgeGlass.quality = quality.wetglass;
        bridgeGlass.seed = 1885 + 400;
        bridgeGlass.stepRate = 30;
        bridgeGlass.reset();
    }

    /** Nebel, Tonnen, Schiffe, Möwen, Brücke je Bild; dann ein Auftrag an den Faden der Tropfen. */
    private void stepLife(Sky.State st, double dt) {
        long t0 = System.nanoTime();
        double lux = daylightLux(st, cloudShade);
        lifeLux = lux;
        double hs = ocean.state().totalHs();
        if (wiring != null) {
            wiring.feed(fog);
            wiring.feed(buoyancy, lux, fog);
            wiring.feed(flock, lux, hs);
        }
        fog.step(dt);
        // die Schwimmkörper fragen das Meer auf dem Prozessor (je neuer Zeit ein Feld von gut 1 ms): eigener Faden
        if (showShips) stepBuoyancy(dt);
        if (showGulls) flock.step(dt);
        boolean b = stepBridge(dt);
        lifeCpuMs = lifeCpuMs * 0.95 + (System.nanoTime() - t0) / 1e6 * 0.05;
        if (pendG || pendR || b) post(dt, pendExtra, pendG, pendR, b);
        pendG = pendR = false;
        pendExtra = 0;
    }

    private java.util.concurrent.ExecutorService floatExec;
    private java.util.concurrent.Future<?> floatJob;
    private double floatDt;

    private void stepBuoyancy(double dt) {
        if (!simThreaded) { buoyancy.step(dt); return; }
        floatDt = Math.min(2, floatDt + dt);
        if (floatJob != null && !floatJob.isDone()) return;
        if (floatExec == null) floatExec = java.util.concurrent.Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "Lighthouse-Schwimmen"); t.setDaemon(true); return t; });
        double d = floatDt;
        floatDt = 0;
        floatJob = floatExec.submit(() -> buoyancy.step(d));
    }

    /** Phase 14: Ton über den Lautsprecher an (eigener Faden). */
    public synchronized void startSound() {
        if (audio == null) audio = new AudioOut(soundscape);
        audio.start();
        if (wiring != null) wiring.soundLive = audio.running() || audio.error().isEmpty();
    }

    /** Ton aus. */
    public synchronized void stopSound() {
        if (audio != null) audio.stop();
        if (wiring != null) wiring.soundLive = false;
    }

    public boolean soundRunning() { return audio != null && audio.running(); }

    /** Phase 14: Ohr, Raum, Wetter, Seegang, Tropfen auf Glas und Kuppel, Schiffsdiesel für den Klang (je Bild). */
    private void stepSound() {
        if (wiring == null) return;
        AudioOut a = audio;
        if (a != null && !a.error().isEmpty()) wiring.soundLive = false;
        long t0 = System.nanoTime();
        Room room = Room.OUTDOORS;
        double in = 0, glassDrops = 0, roofDrops = 0;
        Ship br = onBridge();
        boolean rainOn = rainOk && showRain;
        if (br != null) {
            room = Room.WHEELHOUSE;
            in = 1;
            glassDrops = bridgeGlass.hitRate * 7;
            soundWhere = "Steuerhaus";
        } else if (insideLantern()) {
            room = Room.GLASS_LANTERN;
            in = 1;
            for (Wetglass g : glass) glassDrops += g.hitRate * 3.5;
            // die Kupferkuppel über dem Laternenraum: Tropfen über 0,5 mm auf gut 11 m²
            roofDrops = rainOn ? rain.expectedHits(0, 0, Math.PI * Turm.LANTERN_R * Turm.LANTERN_R, 0, 1, 0, 0.5, 1) : 0;
            soundWhere = "Laternenraum";
        } else if (insideTower()) {
            room = Room.STONE_TOWER;
            in = 1;
            soundWhere = "Turm";
        } else soundWhere = "draußen";
        double[] e = camera.eye.clone();
        double rr = rainOn ? rain.rateAt(e[0], e[2]) * Math.min(1, rainStrength) : 0;
        com.dan.ocean.Ocean.State os = ocean.state();
        wiring.feed(soundscape, e, camera.heading(), os.totalHs(), os.peakPeriod, rr, room, in, glassDrops, roofDrops);
        wiring.feedHums(soundscape, buoyancy, br);
        soundCpuMs = soundCpuMs * 0.95 + (System.nanoTime() - t0) / 1e6 * 0.05;
    }

    /** „Klang an · draußen · Wind 72 dB … · 48 kHz, Puffer 93 ms, 2,1 % eines Kerns“ */
    public String soundLine() {
        AudioOut a = audio;
        if (a == null) return "Klang aus";
        if (!a.error().isEmpty()) return "!Klang: " + a.error();
        if (!a.running()) return "Klang aus";
        String last = soundscape.lastVoice();
        return String.format(Locale.GERMAN, "Klang %s · %s%s · %.1f kHz, Puffer %.0f ms, %.1f %% eines Kerns", soundWhere, soundscape.describe(),
                last.isEmpty() ? "" : " · zuletzt " + last, soundscape.sampleRate() / 1000.0, a.bufferMs(), soundscape.msPerSecond() / 10);
    }

    /** Das Schiff, auf dessen Brücke das Auge steht, oder null. */
    public Ship onBridge() {
        Ship s = bridgeShip;
        return s != null && camera.mode == Camera.Mode.FOLGEN && buoyancy.ships.contains(s) ? s : null;
    }

    /** Regen auf die Frontscheiben (mit dem Fahrtwind), der Wischer wischt; gibt an, ob die Scheiben rechnen sollen. */
    private boolean stepBridge(double dt) {
        Ship s = onBridge();
        if (s == null) { bridgeShip = null; return false; }
        double rate = rainOk && showRain ? rain.rateAt(s.x, s.z) * Math.min(1, rainStrength) : 0;
        double[] f = s.forward(), r = s.right();
        double sp = wiring != null ? wiring.wind.speed() : 5;
        double[] wt = wiring != null ? wiring.wind.toVector() : new double[]{0.7, 0, -0.7};
        // Luft gegen das Schiff: Wind minus Fahrt
        double ax = wt[0] * sp - f[0] * s.speed, az = wt[2] * sp - f[1] * s.speed, am = Math.hypot(ax, az);
        double facing = am > 1e-6 ? -(ax * f[0] + az * f[1]) / am : 0;
        // auch bei wenig Fahrtwind trifft Regen die Front (Böen, Wirbel ums Deckshaus, schräge Scheiben)
        bridgeGlass.rain(rate, Math.max(am, 4), Math.max(0.35, facing));
        bridgeGlass.windX = (ax * r[0] + az * r[1]) * 0.3;
        bridgeGlass.slopeX = 0;
        bridgeGlass.slopeY = -1;
        bridgeGlass.evaporation = rate > 0.05 ? 2e-8 : 2.5e-7;
        // überkommendes Wasser bei schwerer See: Gischt am Bug, ab Hs 3 m auf die Scheiben
        double hs = ocean.state().totalHs();
        if (hs > 3 && Math.abs(s.vPitch) > 0.05 && Math.random() < dt * 0.08 * (hs - 3)) bridgeGlass.flood(0.4 + 0.3 * Math.random(), true);
        boolean wipe = wiperMode == 1 || (wiperMode < 0 && (rate > 0.3 || bridgeGlass.count() > 600));
        s.wiper.on = wipe;
        double[] a = s.wiper.step(dt);
        // was das Blatt gewischt hat, wird mit dem Regen wieder nass (voll nach gut 20 s bei 2 mm/h)
        boolean moving = Math.abs(a[1] - a[0]) > 1e-6;
        wiperWet = moving ? Math.min(0.35, wiperWet + dt * (0.02 + 0.03 * rate)) : Math.min(1, wiperWet + dt * (0.01 + 0.02 * rate));
        if (moving && Math.abs(a[1] - s.wiper.parked()) < 1) wiperWet = 0.08;
        return true;
    }

    /** Auf die Brücke des nächsten Schiffs (gibt es keins, läuft eins ein, schon 2,5 km auf dem Fahrwasser). */
    public Ship toBridge() {
        Ship best = null;
        double bd = 1e18;
        for (Ship s : buoyancy.ships) {
            double d = Math.hypot(s.x - camera.eye[0], s.z - camera.eye[2]);
            if (d < bd && s.remaining() > 1500) { bd = d; best = s; }
        }
        if (best == null) best = buoyancy.spawn(true, 2500);
        Ship s = best;
        bridgeShip = s;
        zoom = 1;
        bridgeGlass.reset();
        camera.follow("Brücke " + s.name, () -> {
            if (!buoyancy.ships.contains(s)) return null;
            double[][] v = s.bridgeView();
            return new double[][]{v[0], v[1], v[2]};
        });
        return s;
    }

    /** Möwenflug: das Auge auf einer fliegenden Möwe nah am Turm (mit Querneigung); false, wenn keine fliegt. */
    public boolean rideGull() {
        Flock.Gull pick = null;
        double bd = 1e18;
        // am liebsten eine, die im Flug auf den Turm zuhält (dann sind Turm und Schwarm im Bild), 25 bis 150 m entfernt
        for (Flock.Gull g : flock.gulls()) {
            if (g.state != Flock.State.FLY && g.state != Flock.State.SOAR) continue;
            double d = Math.hypot(g.pos[0], g.pos[2]), lh = Math.hypot(g.vel[0], g.vel[2]);
            double toward = lh > 2 ? -(g.vel[0] * g.pos[0] + g.vel[2] * g.pos[2]) / (lh * Math.max(1, d)) : -0.5;
            double score = (1 - toward) * 100 + Math.abs(d - 60) + (g.state == Flock.State.SOAR ? 80 : 0);
            if (score < bd) { bd = score; pick = g; }
        }
        if (pick == null) return false;
        rideGull = pick.id;
        rideFwd = null;
        zoom = 1;
        camera.follow("Möwenflug", this::gullPose);
        return true;
    }

    private double[][] gullPose() {
        Flock.Gull g = null;
        for (Flock.Gull x : flock.gulls()) if (x.id == rideGull) g = x;
        if (g == null || g.state == Flock.State.AWAY || g.state == Flock.State.LEAVE) { rideGull = -1; return null; }
        // waagerecht in Flugrichtung (im Aufwind steht sie fast still: dann wohin sie schaut), leicht nach unten
        double vx = g.vel[0], vy = g.vel[1], vz = g.vel[2], lh = Math.hypot(vx, vz);
        double hx = lh > 2 ? vx / lh : Math.sin(Math.toRadians(g.facing)), hz = lh > 2 ? vz / lh : -Math.cos(Math.toRadians(g.facing));
        double[] f = {hx, Math.max(-0.3, Math.min(0.2, lh > 2 ? vy / lh * 0.5 : 0)) - 0.12, hz};
        if (rideFwd == null) rideFwd = f.clone();
        for (int k = 0; k < 3; k++) rideFwd[k] += (f[k] - rideFwd[k]) * 0.15;
        double[] fw = norm3(rideFwd);
        double[] r = {-fw[2], 0, fw[0]};
        double rl = Math.max(1e-9, Math.hypot(r[0], r[2]));
        r[0] /= rl;
        r[2] /= rl;
        double[] u = cross(r, fw);
        double c = Math.cos(g.bank * 0.7), sn = Math.sin(g.bank * 0.7);
        double[] e = {g.pos[0] - fw[0] * 0.15, g.pos[1] + 0.12, g.pos[2] - fw[2] * 0.15};
        return new double[][]{e, fw, {r[0] * sn + u[0] * c, r[1] * sn + u[1] * c, r[2] * sn + u[2] * c}};
    }

    /** Liegt Seenebel, der gezeichnet wird? */
    boolean fogNow() { return fogOk && showFog && fog.snapshot().any(); }

    /** Durchlässigkeit des Seenebels vom Auge zu einem Licht (1 ohne Nebel). */
    double fogT(double x, double y, double z) {
        if (!fogNow()) return 1;
        double[] e = camera.eye;
        return fog.transmittance(e[0], e[1], e[2], x, y, z);
    }

    /** Licht für Tonnen, Schiffe und Möwen: {Sonne, Mond, Himmel oben, Himmel am Horizont} vorbelichtet. */
    private double[][] lifeLight(Sky.State st, double[] sun, double pre) {
        if (skyOk) {
            double[] top = st.radiance(new double[]{0, 1, 0}), hor = new double[3];
            // Horizont rundum gemittelt (sonst färbt die Morgenröte im Osten den ganzen Nebel)
            for (int i = 0; i < 6; i++) {
                double a = Math.PI * i / 3;
                double[] h = st.radiance(norm3(new double[]{Math.sin(a), 0.08, -Math.cos(a)}));
                for (int k = 0; k < 3; k++) hor[k] += h[k] / 6;
            }
            return new double[][]{scale(st.sunColor, pre * cloudShade), scale(st.moonColor, pre * cloudShade), scale(top, pre), scale(hor, pre)};
        }
        double[] sl = sunLight(clock.sun()[0]);
        return new double[][]{sl, {0, 0, 0}, scale(sl, 0.15), scale(sl, 0.25)};
    }

    private void drawLife(GL4 gl, Sky.State st, float[] vp, double[] sun, double pre) {
        double[][] L = lifeLight(st, sun, pre);
        double fd = skyOk ? fogDistance(st) : 40_000;
        Ship br = onBridge();
        if (buoyancyOk && showShips) {
            BuoyancyGl.View v = new BuoyancyGl.View();
            v.viewProj = vp;
            v.eye = camera.eye.clone();
            v.reversedZ = reversedZ;
            v.bridge = br;
            BuoyancyGl.Light l = new BuoyancyGl.Light();
            l.sunDir = sun;
            l.sunColor = L[0];
            if (skyOk) l.moonDir = st.moonDir;
            l.moonColor = L[1];
            l.skyTop = L[2];
            l.skyHorizon = L[3];
            l.fogDistance = fd;
            boolean night = buoyancy.lightsOn();
            double u = 1 / Sky.CD_PER_UNIT * (skyOk ? pre : 300);
            l.lamp = night ? 2e4 * u : 0;
            l.windowGlow = night ? 4 * u : 0;
            l.screen = (night ? 8 : 60) * u;
            buoyancyGl.draw(gl, buoyancy, v, l);
            if (buoyancyCheck.isEmpty() && frame > 90 && !buoyancy.buoys.isEmpty()) buoyancyCheck = buoyancyGl.selfCheck(gl, buoyancy.buoys.get(0));
        }
        if (flockOk && showGulls) {
            FlockGl.View v = new FlockGl.View();
            v.viewProj = vp;
            v.eye = camera.eye.clone();
            v.reversedZ = reversedZ;
            v.hide = camera.mode == Camera.Mode.FOLGEN && rideGull >= 0 && br == null ? rideGull : -1;
            FlockGl.Light l = new FlockGl.Light();
            l.sunDir = sun;
            l.sunColor = L[0];
            if (skyOk) l.moonDir = st.moonDir;
            l.moonColor = L[1];
            l.skyTop = L[2];
            l.skyHorizon = L[3];
            l.fogDistance = fd;
            flockGl.draw(gl, flock, v, l);
            if (flockCheck.isEmpty() && frame > 95 && !flock.gulls().isEmpty()) flockCheck = flockGl.selfCheck(gl, flock);
        }
    }

    private void drawFog(GL4 gl, Sky.State st, float[] inv, int w, int h, double pre) {
        Fog.Snapshot s = fog.snapshot();
        fogGl.update(gl, s, camera.eye.clone());
        if (fogCheck.isEmpty() && frame > 100) fogCheck = fogGl.selfCheck(gl, fog, camera.eye.clone());
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        FogGl.View v = new FogGl.View();
        v.invViewProj = inv;
        v.eye = camera.eye.clone();
        v.reversedZ = reversedZ;
        v.width = w;
        v.height = h;
        v.depthTexture = sprayDepthTex;
        v.jitter = frozen ? 0.5 : -1;
        v.frame = frame;
        double[][] L = lifeLight(st, skyOk ? st.sunDir : Sun.direction(clock.sun()[0], clock.sun()[1]), pre);
        FogGl.Light l = new FogGl.Light();
        l.sunDir = skyOk ? st.sunDir : Sun.direction(clock.sun()[0], clock.sun()[1]);
        l.sunColor = L[0];
        if (skyOk) l.moonDir = st.moonDir;
        l.moonColor = L[1];
        l.ambientTop = L[2];
        l.ambientHorizon = L[3];
        fogGl.draw(gl, s, v, l);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        gl.glBindVertexArray(emptyVao);
    }

    /** Lichtpunkte der Tonnen und Schiffe am Auge (lx), durch Dunst und Nebel. */
    java.util.List<BeaconGl.Glare> lifeGlare() {
        java.util.List<BeaconGl.Glare> out = new java.util.ArrayList<>();
        double[] e = camera.eye;
        if (showRueckfluss && aussen.an()) {
            // Rückfluss aus Timmendorf (Phase 18): die Leuchten außen am Turm als Lichtpunkte (Lichtstärke zum Auge, Dunst)
            double sg = beacon.hazePerM + (rainOk && showRain ? rain.scatter(e[0], e[1], e[2]) * 3e-3 : 0);
            for (com.dan.lamp.Lamp lp : aussen.lampen.all()) {
                double dx = e[0] - lp.x, dy = e[1] - lp.y, dz = e[2] - lp.z, d2 = Math.max(1, dx * dx + dy * dy + dz * dz), d = Math.sqrt(d2);
                double cd = lp.candela(dx / d, dy / d, dz / d);
                if (cd <= 0) continue;
                double k = cd * Math.exp(-sg * d) * fogT(lp.x, lp.y, lp.z) / d2;
                double[] c = lp.rgb();
                out.add(new BeaconGl.Glare(lp.x, lp.y, lp.z, new double[]{c[0] * k, c[1] * k, c[2] * k}, 1, 1.0));
            }
        }
        if (!showShips) return out;
        double sigma = beacon.hazePerM + (rainOk && showRain ? rain.scatter(e[0], e[1], e[2]) * 3e-3 : 0);
        double eyeH = Math.max(0.5, e[1] - (wiring != null ? wiring.waterLevel.level() : 0));
        Ship br = onBridge();
        for (LightPoint p : buoyancy.lights(e[0], e[1], e[2])) {
            if (br != null && p.name().startsWith(br.name + " ")) continue;
            double dx = p.x() - e[0], dy = p.y() - e[1], dz = p.z() - e[2], d2 = Math.max(1, dx * dx + dy * dy + dz * dz), d = Math.sqrt(d2);
            if (d > com.dan.beacon.Range.geographicRange(Math.max(0, p.y() + Site.curvatureDrop(d)), eyeH) * Site.NM) continue;
            double k = Math.exp(-sigma * d) * fogT(p.x(), p.y(), p.z()) / d2;
            if (k * (p.r() + p.g() + p.b()) < 1e-9) continue;
            out.add(new BeaconGl.Glare(p.x(), p.y(), p.z(), new double[]{p.r() * k, p.g() * k, p.b() * k}, 1, 1.5));
        }
        return out;
    }

    private void drawLifeGlare(GL4 gl, float[] vp, float[] inv, int w, int h, double pre) {
        BeaconGl.View v = new BeaconGl.View();
        v.viewProj = vp;
        v.invViewProj = inv;
        v.eye = camera.eye.clone();
        v.reversedZ = reversedZ;
        v.width = w;
        v.height = h;
        v.fovyDeg = fovy();
        v.depthTexture = sprayDepthTex;
        v.targetFbo = hdrFbo;
        v.preExposure = skyOk ? pre * beaconUnit : 1;
        v.strength = 1;
        v.jitter = !frozen;
        beaconGl.drawGlare(gl, v, lifeGlare());
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        gl.glBindVertexArray(emptyVao);
    }

    /** Die drei Frontscheiben der Brücke von innen über das fertige Bild (mit Tropfen, Wischerspur, Beschlag aus). */
    private void drawBridgeGlass(GL4 gl, Sky.State st, float[] vp, float[] inv, int w, int h, double[] sun, double level, double pre) {
        Ship s = onBridge();
        if (s == null) return;
        copyScene(gl, w, h);
        bridgeGlassGl.update(gl, bridgeGlass.snapshot());
        float[] v = new float[3 * 6 * 5];
        int o = 0;
        for (int i = 0; i < 3; i++) {
            double[][] c = s.windowWorld(i);
            double[] win = s.windows[i];
            double u0 = win[1] - win[2] / 2 - BRIDGE_PANES[0], u1 = u0 + win[2], v0 = win[3] - BRIDGE_PANES[2], v1 = win[4] - BRIDGE_PANES[2];
            double[][] q = {{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
            for (int k : new int[]{0, 1, 2, 0, 2, 3}) {
                v[o++] = (float) c[k][0]; v[o++] = (float) c[k][1]; v[o++] = (float) c[k][2];
                v[o++] = (float) q[k][0]; v[o++] = (float) q[k][1];
            }
        }
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bridgeVbo);
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, v.length * 4L, Buffers.newDirectFloatBuffer(v));
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        glassSetup(gl, st, vp, inv, w, h, sun, level, pre, 0);
        glassShader.set(gl, "uFogStrength", 0f);
        double[][] c = s.windowWorld(1);
        bridgeGlassGl.bind(gl, glassShader.id(), 9);
        glassShader.set(gl, "uFlip", 1f);
        glassShader.set(gl, "uPaneN", (float) c[4][0], (float) c[4][1], (float) c[4][2]);
        glassShader.set(gl, "uPaneT", (float) c[5][0], (float) c[5][1], (float) c[5][2]);
        // Wischer an der mittleren Scheibe: Drehpunkt in Scheibenmetern (wie die Ecken oben), Blatt, Winkel
        double pu = s.windows[1][1] - s.windows[1][2] / 2 - BRIDGE_PANES[0] + s.wiper.pivotU, pv = s.windows[1][3] - BRIDGE_PANES[2] + s.wiper.pivotV;
        glassShader.set(gl, "uWiper", (float) pu, (float) pv, (float) (s.wiper.arm * s.wiper.bladeFrom), (float) s.wiper.arm);
        glassShader.set(gl, "uWiperArc", (float) Math.toRadians(s.wiper.sweepDeg / 2), (float) Math.toRadians(s.wiper.angle()));
        glassShader.set(gl, "uWiperWet", (float) wiperWet);
        gl.glBindVertexArray(bridgeVao);
        for (int i = 0; i < 3; i++) {
            glassShader.set(gl, "uOff", (float) (0.137 * i), (float) (0.211 * i));
            gl.glDrawArrays(GL.GL_TRIANGLES, i * 6, 6);
        }
        gl.glBindVertexArray(emptyVao);
        gl.glDepthMask(true);
    }

    /** „Leben: 10 Möwen (2 im Aufwind, 3 sitzen) · 8 Tonnen · Kümo 1 in 2,1 km … · Prozessor 0,3 ms · GPU = CPU …“ */
    public String lifeLine() {
        StringBuilder b = new StringBuilder("Leben");
        if (!flockOk || !buoyancyOk) b.insert(0, "!").append(!flockOk ? " (Möwen nicht gestartet)" : "").append(!buoyancyOk ? " (Schiffe nicht gestartet)" : "");
        int[] c = flock.census();
        b.append(String.format(Locale.GERMAN, ": %d Möwen", flock.gulls().size() - c[Flock.State.AWAY.ordinal()]));
        if (c[Flock.State.SOAR.ordinal()] > 0) b.append(String.format(Locale.GERMAN, " (%d im Aufwind)", c[Flock.State.SOAR.ordinal()]));
        if (c[Flock.State.PERCH.ordinal()] > 0) b.append(String.format(Locale.GERMAN, ", %d sitzen", c[Flock.State.PERCH.ordinal()]));
        if (!showGulls) b.append(" (aus)");
        b.append(" · ").append(showShips ? buoyancy.describe(camera.eye[0], camera.eye[2]) : "Schiffe aus");
        Ship br = onBridge();
        if (br != null) b.append(String.format(Locale.GERMAN, " · Brücke: %,d Tropfen, Wischer %s", bridgeGlass.count(), br.wiper.on ? "an" : "aus"));
        b.append(String.format(Locale.GERMAN, " · Prozessor %.2f ms", lifeCpuMs));
        String chk = (buoyancyCheck.isEmpty() ? "" : buoyancyCheck.replaceAll("Grafikkarte = Prozessor: ", "GPU = CPU ").replaceAll(" \\(Median\\).*", ""))
                + (flockCheck.isEmpty() ? "" : " · " + flockCheck.replaceAll("Grafikkarte = Prozessor: ", "GPU = CPU ").replaceAll(" \\(Median\\).*", ""));
        return b + (chk.isEmpty() ? " · Abgleich folgt" : " · " + chk);
    }

    /** „Nebel Bänke 40 % · Sicht in der Bank 180 m, hier 3,2 km · bis 36 m · Nebelhorn Mo(R) 30s · GPU = CPU ± 0,00 %“ */
    public String fogLine() {
        if (!fogOk) return "!Nebel: nicht gestartet";
        if (!fog.snapshot().any()) return "Nebel: keiner" + (showFog ? "" : " (aus)");
        double[] e = camera.eye;
        double vis = fog.visibilityAt(e[0], e[1], e[2]);
        String horn = wiring != null && fog.time() - wiring.lastHornT < 40 ? " · zuletzt: " + wiring.lastHorn : "";
        String chk = fogCheck.isEmpty() ? "Abgleich folgt" : fogCheck.replaceAll("Grafikkarte = Prozessor: ", "GPU = CPU ").replaceAll(" \\(Median\\).*", "");
        return String.format(Locale.GERMAN, "Nebel %s · am Auge %s%s%s · %s", fog.describe(), vis >= 1e5 ? "klar" : vis >= 1000 ? String.format(Locale.GERMAN, "Sicht %.1f km", vis / 1000) : String.format(Locale.GERMAN, "Sicht %.0f m", vis),
                horn, showFog ? "" : " (nicht gezeichnet)", chk);
    }

    /** „Regen mäßiger Regen · 4,2 mm/h hier … · Prozessor 0,1 ms · GPU = CPU ± 0,0 mm“ */
    public String rainLine() {
        if (!rainOk) return "!Regen: nicht gestartet";
        if (rain.rate <= 0) return "Regen aus";
        String chk = rainCheck.isEmpty() ? "Abgleich folgt" : rainCheck.replace("Grafikkarte = Prozessor: Regentropfen ±", "GPU = CPU ±").replaceAll(" \\(Median\\).*", "");
        return String.format(Locale.GERMAN, "Regen %s · Prozessor %.2f ms · %s", rain.describe(), rainCpuMs, chk);
    }

    /** Ein Schwall von Hand auf der Wetterseite des Sockels (Knopf „Brecher am Turm“). */
    public void breaker() {
        java.util.List<com.dan.spray.Body> b = spray.bodies();
        double lv = wiring != null ? wiring.waterLevel.level() : 0;
        for (com.dan.spray.Body body : b) {
            if (body.r0() > 4.5 && lv >= body.y0() && lv <= body.y1()) {
                double from = Math.toRadians(wiring != null ? wiring.wind.fromDeg() : 240);
                spray.splash(body, from, lv, 6 + 1.5 * ocean.state().totalHs());
                return;
            }
        }
    }

    public String sprayCheck() { return sprayCheck; }


    /** „Gischt 3 Schwälle, … · CPU 0,4 ms · GPU = CPU ± 0,0 mm“ */
    public String sprayLine() {
        if (!sprayOk) return "!Gischt: nicht gestartet";
        String chk = sprayCheck.isEmpty() ? "Abgleich folgt" : sprayCheck.replace("Grafikkarte = Prozessor: Tropfen ±", "GPU = CPU ±").replaceAll(" \\(Median\\).*", "");
        return String.format(Locale.GERMAN, "Gischt %d Schwälle · %d Nebel · %d Fahnen · %d Weißwasser · %,d Tropfen · letzter Schwall %.1f m · Prozessor %.2f ms · %s",
                spray.count(0), spray.count(1), spray.count(2), spray.count(3), sprayGl.drawn(), spray.lastApex(), sprayCpuMs, chk);
    }

    /** Wasserstand und nasse Zone für die Anzeige. */
    public String towerLine() {
        double lv = wiring != null ? wiring.waterLevel.level() : 0;
        String tide = "";
        if (wiring != null && wiring.tide.on) {
            double now = clock.epochMillis();
            boolean rising = Tide.at(now + 600_000) > lv;
            double[] next = nextTide;
            // das nächste Hoch- oder Niedrigwasser nur neu suchen, wenn es vorbei ist oder die Uhr gesprungen ist
            if (next == null || now > next[0] || now < next[2] || rising != (next[3] > 0)) {
                double[] n = Tide.next(now, rising);
                next = nextTide = new double[]{n[0], n[1], now, rising ? 1 : 0};
            }
            tide = String.format(Locale.GERMAN, " (%s, %s %s %+.1f m)", rising ? "steigt" : "fällt", rising ? "HW" : "NW",
                    String.format("%tR", java.time.Instant.ofEpochMilli((long) next[0]).atZone(SceneClock.ZONE)), next[1]);
        } else if (wiring != null) {
            tide = " (fest)";
        }
        Weathering.Snapshot w = weathering.snapshot();
        String chk = weatheringCheck.isEmpty() ? "Abgleich folgt" : weatheringCheck.replace("Grafikkarte = Prozessor: Verwitterung Median ±", "GPU = CPU ±").replaceAll(",.*", "");
        return String.format(Locale.GERMAN, "Turm · Wasser %+.2f m%s · Platte %s · nass bis %+.1f m · Schatten %d² · %s",
                lv, tide, lv < Turm.PLATE_TOP ? String.format(Locale.GERMAN, "%.1f m frei", Turm.PLATE_TOP - lv) : String.format(Locale.GERMAN, "%.1f m unter Wasser", lv - Turm.PLATE_TOP),
                w.wetLine(), shadowSize, chk);
    }

    /** Nasse Zone beim nächsten Schritt neu setzen (nach Sprüngen von Hand: Wasserstand, Uhrzeit; für Standbilder). */
    public void resettleWeathering() { weatheringSettled = false; }

    public String weatheringCheck() { return weatheringCheck; }
    public String weatheringInfo() { return weatheringGl.info(); }
    public Turm turm() { return turm; }

    /** Das Licht, das die Wolken formt: die Sonne, solange sie die hohen Wolken noch erreicht, sonst der Mond. */
    static boolean sunIsKey(Sky.State st) { return st.sunAlt > -4; }

    static double[] keyDir(Sky.State st) { return sunIsKey(st) ? st.sunDirTrue : st.moonDirTrue; }

    /** Licht für die Wolken aus dem Himmelszustand, vorbelichtet. */
    CloudGl.Light cloudLight(Sky.State st, double pre) {
        CloudGl.Light l = new CloudGl.Light();
        boolean sun = sunIsKey(st);
        l.dir = keyDir(st);
        for (int i = 0; i < 8; i++) l.colorAt[i] = scale(sun ? st.sunColorAt(CloudGl.LIGHT_HEIGHTS[i]) : st.moonColorAt(CloudGl.LIGHT_HEIGHTS[i]), pre);
        double[] e = st.skyIrradiance;
        l.ambTop = scale(e, pre / Math.PI);
        double s = Math.max(0, st.sunDir[1]), m = Math.max(0, st.moonDir[1]);
        l.ambBot = new double[3];
        for (int c = 0; c < 3; c++) l.ambBot[c] = (e[c] + st.sunColor[c] * s + st.moonColor[c] * m) * 0.06 / Math.PI * pre;
        l.haze = st.groundExtinction;
        l.flash = new double[]{st.flashDir[0] * 5000, st.flashDir[1] * 5000, st.flashDir[2] * 5000, st.flash * pre};
        // Phase 10: Lichtpunkte der Blitze in und unter der Wolke (beim Auflegen, ohne zeitliches Nachziehen)
        l.flashCount = 0;
        if (lightningOk && showLightning && lightningGl.active()) {
            int n = lightningGl.cloudLightCount();
            for (int i = 0; i < n; i++) {
                double[] c = lightningGl.cloudLight(i);
                for (int k = 0; k < 3; k++) {
                    l.flashPos[i * 4 + k] = (float) c[k];
                    l.flashRgb[i * 4 + k] = (float) (c[3] * Lightning.CHANNEL_RGB[k] * pre);
                }
            }
            l.flashCount = n;
        }
        return l;
    }

    @Override
    public void display(GLAutoDrawable d) {
        GL4 gl = d.getGL().getGL4();
        if (!ready) {
            gl.glClearColor(0.04f, 0.06f, 0.09f, 1);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            return;
        }
        if (reloadShaders) {
            reloadShaders = false;
            try {
                for (Shader s : all) s.build(gl);
                System.out.println("Shader neu geladen.");
            } catch (RuntimeException e) {
                System.err.println(e.getMessage());
                errorListener.accept(e.getMessage());
            }
        }
        defaultFbo = d.getContext().getDefaultDrawFramebuffer();
        readMarks(gl);
        long now = System.nanoTime();
        double rawMs = (now - lastNanos) / 1e6;
        if (frame > 2 && rawMs > worstMs) worstMs = rawMs;
        double dt = Math.min(0.1, (now - lastNanos) / 1e9);
        lastNanos = now;
        if (!frozen) step(dt);

        surfW = Math.max(1, d.getSurfaceWidth());
        surfH = Math.max(1, d.getSurfaceHeight());
        regulateScale(now);
        int w = Math.max(64, (int) Math.round(surfW * renderScale)), h = Math.max(64, (int) Math.round(surfH * renderScale));
        if (w != hdrW || h != hdrH) resizeTargets(gl, w, h);
        renderFrame(gl, w, h);
        kinoBild(gl, now);   // Kinomodus über das fertige Bild; F12 und Vorschau bekommen es mit
        if (photoRequest) { photoRequest = false; takePhoto(gl, surfW, surfH); }
        java.util.function.Consumer<BufferedImage> gr = grabRequest;
        if (gr != null) {
            grabRequest = null;
            try { gr.accept(readDefault(gl, surfW, surfH)); } catch (RuntimeException e) { System.err.println("Lighthouse: Vorschau fehlgeschlagen: " + e); }
        }
        cpuMs = cpuMs * 0.95 + (System.nanoTime() - now) / 1e6 * 0.05;
        frame++;
        stats(now, w, h);
    }

    /** Ein ganzes Bild in Rechengröße w × h, dann ins Fenster. */
    void renderFrame(GL4 gl, int w, int h) {
        mark(gl, 0);
        Sky.State st = skyState();
        if (adaptLum <= 0) adapt(st, 1);
        double pre = skyOk ? exposure() : 1;
        double[] sp = clock.sun();
        double[] sun = skyOk ? st.sunDir : Sun.direction(sp[0], sp[1]);
        // Phase 11: in der engen Luke und auf der Treppe rückt die vordere Schnittebene heran
        double near = insideTower() || insideLantern() ? 0.05 : 0.2;
        float[] proj = Mat4.perspective(fovy(), w / (double) h, near, 60_000, reversedZ);
        float[] vp = Mat4.mul(proj, camera.view());
        float[] inv = Mat4.invert(vp);
        double level = wiring != null ? wiring.waterLevel.level() : 0;
        double windSpeed = wiring != null ? wiring.wind.speed() : Beaufort.mid(4);
        double[] windTo = wiring != null ? wiring.wind.toVector() : new double[]{0.7, 0, -0.7};

        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        gl.glClearColor(0, 0, 0, 1);
        gl.glClearDepth(reversedZ ? 0 : 1);
        gl.glDepthMask(true);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glBindVertexArray(emptyVao);

        // 1 Himmel: Tabellen, dann Himmel, Sterne, Planeten
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_CULL_FACE);
        if (skyOk) {
            skyGl.update(gl, st);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            SkyGl.View sv = new SkyGl.View();
            sv.viewProj = vp;
            sv.invViewProj = inv;
            sv.reversedZ = reversedZ;
            sv.width = w;
            sv.height = h;
            sv.fovyDeg = fovy();
            sv.preExposure = pre;
            sv.time = clock.t();
            skyGl.draw(gl, st, sv);
            if (skyCheck.isEmpty() && frame > 30) skyCheck = skyGl.selfCheck(gl, st);
            gl.glBindVertexArray(emptyVao);
        } else {
            common(gl, skyShader, vp, inv, sun, level);
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
        }
        mark(gl, 1);

        // Phase 10: Blitze übernehmen (neue Kanäle hochladen, Lichtpunkte für Wolken, Meer, Turm, Scheiben)
        if (lightningOk) {
            lightningSnap = lightning.snapshot();
            lightningGl.update(gl, lightningSnap);
        }
        boolean flashNow = lightningOk && showLightning && lightningGl.active();
        double[] air = lightningAir();

        // 2 Wolken: Schattenkarte, dann über den Himmel gelegt (vor Meer und Turm, die sie verdecken)
        Clouds.Snapshot cs = clouds.snapshot();
        CloudGl.Light cl = cloudsOk ? cloudLight(st, pre) : null;
        if (cloudsOk) {
            cloudGl.update(gl, cs, cl, camera.eye.clone());
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            CloudGl.View cv = new CloudGl.View();
            cv.viewProj = vp;
            cv.invViewProj = inv;
            cv.eye = camera.eye.clone();
            cv.reversedZ = reversedZ;
            cv.width = w;
            cv.height = h;
            cloudGl.draw(gl, cs, cl, cv);
            if (cloudCheck.isEmpty() && frame > 40) cloudCheck = cloudGl.selfCheck(gl, cs, cl);
            gl.glBindVertexArray(emptyVao);
        }
        // Phase 10: die Kanäle über Himmel und Wolken (Meer und Turm verdecken sie danach); in der Wolke verborgen
        if (flashNow) {
            LightningGl.View lv = lightningView(vp, inv, w, h, pre, air);
            lightningGl.drawChannels(gl, lv);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            gl.glBindVertexArray(emptyVao);
        }
        mark(gl, 2);

        // 3 Meer rechnen: Himmelspanorama für die Spiegelung, dann die Compute-Kette der See
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, panoFbo);
        gl.glViewport(0, 0, PANO_W, PANO_H);
        if (skyOk) {
            skyGl.drawPanorama(gl, st, pre);
            if (cloudsOk) cloudGl.drawPanorama(gl, cs, cl, camera.eye.clone());
            // Phase 10: Kanäle und Leuchten ins Rundumbild, damit See und Scheiben sie spiegeln
            if (flashNow) lightningGl.drawPanorama(gl, PANO_W, PANO_H, camera.eye.clone(), pre, air[0], air[1], cloudsOk ? clouds.type.base : lightning.cloudBase);
            gl.glBindVertexArray(emptyVao);
        } else {
            common(gl, skyPanoShader, vp, inv, sun, level);
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
        }
        if (rainOk) {
            rainGl.update(gl, rain.snapshot());
            if (rainCheck.isEmpty() && rain.rate > 0 && frame > 60) rainCheck = rainGl.selfCheck(gl, rain.snapshot());
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, panoFbo);
        }
        if (beaconOk) {
            beaconGl.update(gl, beacon.snapshot(), beaconGrid);
            if (beaconCheck.isEmpty() && beacon.isOn() && frame > 70 && beaconGrid != null && beaconGrid.version() > 0)
                beaconCheck = beaconGl.selfCheck(gl, beacon.snapshot(), beaconGrid);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, panoFbo);
        }
        if (flashNow && lightningCheck.isEmpty()) {
            for (Bolt b : lightningSnap.bolts())
                if (b.kind == Bolt.Kind.CLOUD_GROUND && lightningSnap.time() > b.t0 + b.leaderTime) {
                    lightningCheck = lightningGl.selfCheck(gl, lightningSnap);
                    lightningGl.update(gl, lightningSnap);
                    break;
                }
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, panoFbo);
        }
        if (oceanOk) {
            oceanGl.update(gl, ocean);
            if (oceanCheck.isEmpty() && frame > 20) oceanCheck = oceanGl.selfCheck(gl, ocean);
        }
        mark(gl, 3);

        // 4 Schatten: Schattenkarte des Turms und sein Schatten auf dem Wasser (vor dem Meer, das ihn liest)
        double[] key = keyLight(st, sun);
        if (showTower) renderShadows(gl, key, level);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        gl.glBindVertexArray(emptyVao);
        mark(gl, 4);

        // 5 Meer zeichnen (oder die Platzhalter-See, die ihre Tiefe selbst schreibt)
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
        if (oceanOk) {
            OceanGl.View view = new OceanGl.View();
            view.viewProj = vp;
            view.eye = camera.eye.clone();
            view.reversedZ = reversedZ;
            OceanGl.Light light = new OceanGl.Light();
            light.sunDir = sun;
            light.skyTexture = panoTex;
            if (skyOk) {
                light.sunColor = scale(st.sunColor, pre);
                light.moonDir = st.moonDir;
                light.moonColor = scale(st.moonColor, pre);
                light.fogDistance = fogDistance(st);
                if (cloudsOk) {
                    light.shadowTexture = cloudGl.shadowTexture();
                    light.shadowRect = cloudGl.shadowRect();
                }
                if (showTower && key != null) {
                    light.bodyShadowTexture = castTex;
                    light.bodyShadowRect = castRect.clone();
                }
                if (rainOk && showRain && rain.rate > 0) {
                    light.detailTexture = rainGl.rippleTexture();
                    light.detailTile = rainGl.rippleTile();
                    light.detailStrength = rainGl.rippleStrength() * Math.min(1, rainStrength);
                    light.detailRange = 60;
                }
            } else {
                light.sunColor = sunLight(sp[0]);
                light.fogDistance = 40_000;
            }
            if (beaconOk && beacon.isOn()) {
                Beacon.Snapshot bs = beacon.snapshot();
                light.pointTexture = beaconGl.patternTexture();
                light.pointPos = new double[]{bs.x(), bs.y(), bs.z()};
                light.pointGain = skyOk ? pre * beaconUnit : 1;
                light.pointSigma = bs.hazePerM() + rain.scatter(0, 1, 0) * 3e-3;
            }
            if (flashNow) {
                int n = lightningGl.lightCount();
                light.flashCount = n;
                for (int i = 0; i < n; i++) {
                    double[] lp = lightningGl.lightPos(i), lr = lightningGl.lightRgb(i);
                    for (int k = 0; k < 4; k++) light.flashPos[i * 4 + k] = (float) lp[k];
                    for (int k = 0; k < 3; k++) light.flashRgb[i * 4 + k] = (float) (lr[k] * (skyOk ? pre : 1));
                }
                if (light.pointSigma <= 0) light.pointSigma = air[0];
            }
            // Rückfluss aus Timmendorf (Phase 18): die Leuchten als Punktlichter auf der See (in freie Plätze nach den Blitzen)
            if (showRueckfluss && aussen.an()) {
                double u = skyOk ? pre * beaconUnit : 300 * beaconUnit;
                for (com.dan.lamp.Lamp lp : aussen.lampen.all()) {
                    int i = light.flashCount;
                    if (i >= 8) break;
                    double cd = lp.candela(0, -1, 0);
                    if (cd <= 0) continue;
                    double[] c = lp.rgb();
                    light.flashPos[i * 4] = (float) lp.x;
                    light.flashPos[i * 4 + 1] = (float) lp.y;
                    light.flashPos[i * 4 + 2] = (float) lp.z;
                    light.flashPos[i * 4 + 3] = 0.3f;
                    for (int k = 0; k < 3; k++) light.flashRgb[i * 4 + k] = (float) (c[k] * cd * u);
                    light.flashCount = i + 1;
                }
                if (light.flashCount > 0 && light.pointSigma <= 0) light.pointSigma = air[0];
            }
            // Phase 17: kein Loch – die See bleibt, die Brandung legt ihr weißes Wasser darüber (Sockelbrandung.naht)
            double[][] loch = brandungAn() ? brandung.loch() : null;
            oceanGl.draw(gl, ocean, view, light);
            gl.glBindVertexArray(emptyVao);
            // Phase 17: die Brecher am Sockel im Loch der See (Tiefentest; der Turm verdeckt das ruhende Wasser im Sockel)
            com.dan.lighthouse.scene.Sockelbrandung.Stueck bs = loch != null ? brandung.stueck() : null;
            if (bs != null) {
                brandungGl.upload(gl, 0, bs.netz);
                com.dan.surf.gl.BreakerGl.View bv = new com.dan.surf.gl.BreakerGl.View();
                bv.viewProj = Mat4.mul(vp, bs.modell());
                double[] le = bs.lokal(camera.eye[0], camera.eye[2]);
                bv.eye = new double[]{le[0], camera.eye[1], le[1]};
                bv.reversedZ = reversedZ;
                brandungGl.draw(gl, 0, bv, brandungLicht(st, sun, pre, bs), ocean.time(), 99);
                gl.glBindVertexArray(emptyVao);
            }
        } else {
            common(gl, sea, vp, inv, sun, level);
            sea.set(gl, "uWindTo", (float) windTo[0], (float) windTo[2]);
            sea.set(gl, "uWindSpeed", (float) windSpeed);
            sea.set(gl, "uOctaves", 6);
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
        }
        mark(gl, 5);

        // 6 Turm: Rückseiten verwerfen (Wicklung gegen den Uhrzeigersinn außen)
        if (showTower) {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
            gl.glFrontFace(GL.GL_CCW);
            common(gl, tower, vp, inv, sun, level);
            drawTower(gl, st, key);
            gl.glDisable(GL.GL_CULL_FACE);
            drawFlagge(gl, st, vp, sun, pre);
            if (weatheringOk && weatheringCheck.isEmpty() && frame > 50) weatheringCheck = weatheringGl.selfCheck(gl, weathering.snapshot());
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
        }
        mark(gl, 6);

        // 7 Leben (Phase 13): Tonnen, Schiffe, Möwen; sie schreiben Tiefe, also vor Gischt, Regen und Nebel
        drawLife(gl, st, vp, sun, pre);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        gl.glBindVertexArray(emptyVao);
        // 7b Personen (Avatar Phase 15): der Wärter auf der Galerie; schreiben Tiefe wie das Leben (Zeit im Durchgang 7)
        if (personen.an || !personen.figuren.isEmpty()) {
            personen.zeichnen(gl, vp, camera.eye.clone(), fovy(), w, h, reversedZ, hdrFbo, personLight(st, sun, pre), quality.ordinal(), clock.t());
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            gl.glUseProgram(0);
            gl.glBindVertexArray(emptyVao);
        }
        mark(gl, 7);

        // 8 Gischt: Tiefe kopieren (für weiche Ränder), dann Tropfen und Nebel über die Szene
        boolean rainNow = rainOk && showRain && rain.rate > 0;
        if ((sprayOk && showSpray) || rainNow) copyDepth(gl, w, h);
        if (sprayOk && showSpray) drawSpray(gl, st, vp, w, h, pre);
        mark(gl, 8);

        // 9 Regen: Vorhänge über die Szene, Streifen, Krönchen (die Gischt schreibt keine Tiefe, die Kopie gilt noch)
        if (rainNow) drawRain(gl, st, vp, inv, w, h, pre);
        mark(gl, 9);

        // 10 Nebel (Phase 13): Seenebel über Meer, Turm und Schiffe, bis zur Szenentiefe
        boolean depthCopied = rainNow || (sprayOk && showSpray);
        if (fogNow()) {
            if (!depthCopied) copyDepth(gl, w, h);
            depthCopied = true;
            drawFog(gl, st, inv, w, h, pre);
        }
        mark(gl, 10);

        // 11 Feuer: der sichtbare Strahl (Tiefe der Szene als Kopie) und die Lichtpunkte
        if (beaconOk) {
            drawBeacon(gl, st, vp, inv, w, h, pre, depthCopied);
            Beacon.Snapshot bsn = beacon.snapshot();
            depthCopied |= bsn.lit() || (showNeighbours && neighbourOn());
        }
        // Phase 13: Lichter der Tonnen und Schiffe (auch am Tag ohne Feuer)
        if (beaconOk && !lifeGlare().isEmpty()) {
            if (!depthCopied) copyDepth(gl, w, h);
            depthCopied = true;
            drawLifeGlare(gl, vp, inv, w, h, pre);
        }
        mark(gl, 11);

        // 10 Blitz: das Leuchten der Luft um die Kanäle, bis zur Szene oder zur Wolkenuntergrenze
        if (flashNow) {
            if (!depthCopied) copyDepth(gl, w, h);
            LightningGl.View lv = lightningView(vp, inv, w, h, pre, air);
            lv.depthTexture = sprayDepthTex;
            lightningGl.drawGlow(gl, lv);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            gl.glBindVertexArray(emptyVao);
        }
        mark(gl, 12);

        // 10 Scheiben: im Laternenraum die nassen Scheiben über das fertige Bild der Welt draußen
        if (glassOk && showGlass && nearRail()) drawRail(gl, vp, inv, sun, level);
        if (glassOk && showGlass && insideLantern()) drawGlass(gl, st, vp, inv, w, h, sun, level, pre);
        // Phase 13: auf der Brücke die nassen Frontscheiben mit dem Wischer
        if (bridgeGlassOk && showGlass && onBridge() != null) drawBridgeGlass(gl, st, vp, inv, w, h, sun, level, pre);
        mark(gl, 13);

        // 10 Nachbearbeitung ins Fenster
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, defaultFbo);
        gl.glViewport(0, 0, surfW, surfH);
        post.use(gl);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, hdrTex);
        post.set(gl, "uHdr", 0);
        post.set(gl, "uExposure", skyOk ? 1f : (float) exposure());
        // Phase 11: im beleuchteten Turm sieht das Auge mit den Zapfen
        post.set(gl, "uScotopic", (float) (skyOk ? st.scotopic() * (1 - inBlend) * (1 - lanBlend) : 0));
        double[] wb = whiteBalance();
        post.set(gl, "uWhite", (float) wb[0], (float) wb[1], (float) wb[2]);
        gl.glBindVertexArray(emptyVao);
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
        mark(gl, 14);

        // 12 HUD (nicht im Kino)
        if (showHud && !kino.laeuft()) {
            hud.refresh(gl, 250, this::paintHud, frozen);
            hud.draw(gl, overlay, emptyVao, 12, surfH - hud.height - 12, surfW, surfH);
        }
        gl.glBindVertexArray(0);
        gl.glUseProgram(0);
        mark(gl, 15);
    }

    // ------------------------------------------------------------------ Avatar Phase 15: Personen auf der Galerie
    /** Der Wärter auf der Galerie (com.dan.avatar mit com.dan.hair; die App verbindet sie in {@code person/}). */
    public final com.dan.lighthouse.person.Personen personen = new com.dan.lighthouse.person.Personen();
    private volatile boolean personenFilm, personenEnde;

    /** Wind, Regen, Temperatur und Kamera an die Galerie, dann Regie und Bewegung einen Schritt weiter. */
    private void stepPersonen(double dt) {
        com.dan.lighthouse.person.Galerie g = personen.galerie;
        if (wiring != null) {
            g.windFromDeg = wiring.wind.fromDeg();
            g.windSpeed = wiring.wind.speed();
            com.dan.weather.Weather.Snapshot ws = wiring.weather.snapshot();
            if (ws != null) { g.tempC = ws.temperature(); g.humidity = Math.max(0.2, Math.min(1, ws.humidity() / 100.0)); }
        }
        g.rain = rainOk && showRain ? rain.rate : wiring != null ? wiring.rainRate : 0;
        personen.schritt(dt);
        if (personenFilm) {
            com.dan.avatar.Regie rg = personen.regie;
            if (rg != null && rg.finished() && !personenEnde) {
                personenEnde = true;
                java.util.List<String> cr = new java.util.ArrayList<>();
                cr.add(rg.title);
                for (String c : rg.credits) cr.add(c.replace(" | ", ": "));
                if (kino.laeuft()) kino.beenden(cr);
            }
            // zu Ende oder mit Esc verlassen: das Regiebuch läuft im Fenster weiter, ohne Kino
            if (!kino.laeuft()) personenFilm = false;
        }
    }

    /** Ein Regiebuch der Personen im Kino (Vorspann mit Titel, Untertitel aus den Zeilen, Abspann). */
    public void kinoPersonen(com.dan.avatar.Regie r) {
        frozen = false;
        kinoSchritt = -2;
        kinoBuch = null;
        personen.an = true;
        personenFilm = true;
        personenEnde = false;
        kino.beginnen("Lighthouse · Avatar", r.title, r.subtitle);
        startPersonen(r);
    }

    /** Ein Regiebuch der Personen ohne Kino (Bedienfeld, Werkzeuge). */
    public void startPersonen(com.dan.avatar.Regie r) {
        personen.an = true;
        personen.starten(r, new com.dan.lighthouse.person.GalerieStage(personen, galerieSzene()));
        java.util.function.Consumer<String> t = r.onText;
        r.onText = s -> { t.accept(s); if (kino.laeuft()) kino.untertitel(s); };
    }

    /** Was die Regie der Galerie von der Szene braucht (Kamera, Ansichten, Wetter, Uhr, Kino). */
    public com.dan.lighthouse.person.GalerieStage.Szene galerieSzene() {
        return new com.dan.lighthouse.person.GalerieStage.Szene() {
            @Override public void schnitt(Runnable r) { if (kino.schnitt()) kinoSchnitte.add(r); else r.run(); }
            @Override public void personKamera(com.dan.lighthouse.person.PersonKamera k) {
                k.kino = kino.laeuft();
                zoom = k.zoom();
                if (k.target != null) camera.follow(k.target.person.name, k);
            }
            @Override public boolean ansicht(String name) {
                if (!java.util.List.of("Aussen1", "Drohne", "Brecher", "Sturmnacht", "Aussen3", "Laternenraum", "Regenbogen", "Möwenflug", "Schiffsbrücke").contains(name)) return false;
                applyView(name);
                return true;
            }
            @Override public boolean wetter(String lage, Double windMs, Double regenMmH) {
                if (wiring == null) return false;
                if (lage != null) {
                    com.dan.weather.Weather.Phase p = null;
                    for (com.dan.weather.Weather.Phase x : com.dan.weather.Weather.Phase.values())
                        if (x.name().equalsIgnoreCase(lage) || x.label.equalsIgnoreCase(lage)) p = x;
                    if (p == null) return false;
                    wiring.resumeRegie();
                    wiring.regie = true;
                    wiring.weather.startFree(clock.epochMillis() / 1000.0, p);
                    wiring.weather.running = true;
                }
                if (windMs != null) wiring.hand(com.dan.weather.Weather.Var.WIND, windMs);
                if (regenMmH != null) wiring.hand(com.dan.weather.Weather.Var.RAIN, regenMmH);
                return true;
            }
            @Override public void uhr(double stunde) { clock.setHour(stunde); }
            @Override public int monat() { return clock.date().getMonthValue(); }
            @Override public boolean nachVorspann() { return !kino.laeuft() || kino.lage() != com.dan.kino.Kino.Lage.VORSPANN; }
        };
    }

    /**
     * Licht für die Personen, vorbelichtet wie Turm und Leben. Führungslicht: am Tag die Sonne, nachts das Feuer im
     * Nahfeld der Linse (com.dan.beacon {@code nearIrradiance} am Kopf: die Lampe und die Strahlen, die vorbeiziehen)
     * und der Schein der Raumlampe durch das Laternenglas. Kantenlicht: der Mond. Aufhellung: der Himmel oben und an der
     * Kimm; Blitze hellen auf. Das Avatar-Licht rechnet {@code Farbe / d²} ohne π, die Szene Strahldichte = Albedo·E/π.
     */
    private com.dan.avatar.gl.AvatarGl.Light personLight(Sky.State st, double[] sun, double pre) {
        com.dan.avatar.gl.AvatarGl.Light l = new com.dan.avatar.gl.AvatarGl.Light();
        double[][] L = lifeLight(st, sun, pre);
        double[] h = personen.kopf();
        if (h == null) h = new double[]{0, Turm.GALLERY_TOP + 1.6, -2.6};
        for (int k = 0; k < 3; k++) l.fillCol[k] = (float) (0.5 * L[2][k] + 0.5 * L[3][k]);
        boolean day = sun[1] > 0.02;
        if (day) {
            double d = 400;
            for (int k = 0; k < 3; k++) { l.keyPos[k] = (float) (h[k] + sun[k] * d); l.keyDir[k] = (float) -sun[k]; l.keyCol[k] = (float) (L[0][k] * d * d / Math.PI); }
            l.keyConeInner = 3; l.keyConeOuter = 6;
        } else {
            double[] lens = {0, Turm.LENS_Y, 0};
            double dx = lens[0] - h[0], dy = lens[1] - h[1], dz = lens[2] - h[2], d = Math.max(0.5, Math.sqrt(dx * dx + dy * dy + dz * dz));
            double[] E = new double[3];
            if (beaconOk && beacon.isOn()) {
                Beacon.Snapshot bs = beacon.snapshot();
                E = bs.nearIrradiance(h[0], h[1], h[2], dx / d, dy / d, dz / d);
            }
            double sunAlt = skyOk ? st.sunAlt : clock.sun()[0];
            double glow = lampOn ? 1.5e-3 * (1 - smooth(-1, 2, sunAlt)) * (skyOk ? 1 : 300) : 0;
            for (int k = 0; k < 3; k++) {
                // Glas der Laterne als Fläche: von der Galerie aus rund 0,8 sr [G]
                double e = E[k] * beaconUnit * (skyOk ? pre : 1) + glow * (skyOk ? pre : 1) * 0.8;
                l.keyPos[k] = (float) lens[k];
                l.keyDir[k] = (float) -((lens[k] - h[k]) / d);
                l.keyCol[k] = (float) (e * d * d / Math.PI);
                // Rückstrahlung von Galerieboden und Brüstung, die das Laternenglas beleuchtet [G, am Bild abgestimmt]: so bleibt
                // das Gesicht nicht ganz schwarz, wenn das Feuer hinter der Person steht
                l.fillCol[k] += (float) (0.16 * glow * (skyOk ? pre : 1));   // Phase 19 (Avatar): 0,08 ließ das Gesicht unter der Kapuze zu dunkel
            }
            l.keyConeInner = 70; l.keyConeOuter = 89;
        }
        // Kantenlicht: der Mond, sonst nichts
        if (skyOk && st.moonDir[1] > 0.02) {
            double d = 400;
            for (int k = 0; k < 3; k++) { l.rimPos[k] = (float) (h[k] + st.moonDir[k] * d); l.rimCol[k] = (float) (L[1][k] * d * d / Math.PI); }
        } else for (int k = 0; k < 3; k++) l.rimCol[k] = 0;
        // Blitze: als Aufhellung von überall
        if (lightningOk && showLightning && lightningGl.active()) {
            int n = lightningGl.lightCount();
            for (int i = 0; i < n; i++) {
                double[] lp = lightningGl.lightPos(i), lr = lightningGl.lightRgb(i);
                double q = (lp[0] - h[0]) * (lp[0] - h[0]) + (lp[1] - h[1]) * (lp[1] - h[1]) + (lp[2] - h[2]) * (lp[2] - h[2]);
                for (int k = 0; k < 3; k++) l.fillCol[k] += (float) (lr[k] * (skyOk ? pre : 1) / Math.max(1, q) / Math.PI);
            }
        }
        l.gain = 1;
        return l;
    }

    /** Kamera auf die erste Person (Halbnah, 20° von vorn links), ohne Regiebuch. */
    public void zumWaerter() {
        com.dan.lighthouse.person.Figur f = personen.erste();
        if (f == null) return;
        com.dan.lighthouse.person.PersonKamera k = personen.kamera;
        k.target = f;
        k.shot = com.dan.portrait.Shot.HALBNAH;
        k.angleDeg = 20;
        k.lift = 0;
        k.snap = true;
        galerieSzene().personKamera(k);
    }

    /** Zeile der Personen für HUD und Bedienfeld. */
    public String personLine() { return personen.zeile(); }

    /** Phase 10: Blick für die Blitze (Ziel ist das HDR-Bild; ohne Tiefentest, die Szene verdeckt später). */
    private LightningGl.View lightningView(float[] vp, float[] inv, int w, int h, double pre, double[] air) {
        LightningGl.View v = new LightningGl.View();
        v.viewProj = vp;
        v.invViewProj = inv;
        v.eye = camera.eye.clone();
        v.reversedZ = reversedZ;
        v.width = w;
        v.height = h;
        v.fovyDeg = fovy();
        v.targetFbo = hdrFbo;
        v.preExposure = skyOk ? pre : 1;
        v.extinction = air[0];
        v.scatter = air[1];
        v.ceiling = cloudsOk ? clouds.type.base : lightning.cloudBase;
        return v;
    }

    /** Phase 9: Strahl und Lichtpunkte über die Szene (die Tiefe ist kopiert, wenn Gischt oder Regen liefen). */
    private void drawBeacon(GL4 gl, Sky.State st, float[] vp, float[] inv, int w, int h, double pre, boolean depthCopied) {
        Beacon.Snapshot s = beacon.snapshot();
        boolean any = s.lit() || (showNeighbours && neighbourOn());
        if (!any) return;
        if (!depthCopied) copyDepth(gl, w, h);
        BeaconGl.View v = new BeaconGl.View();
        v.viewProj = vp;
        v.invViewProj = inv;
        v.eye = camera.eye.clone();
        v.reversedZ = reversedZ;
        v.width = w;
        v.height = h;
        v.fovyDeg = fovy();
        v.depthTexture = sprayDepthTex;
        v.targetFbo = hdrFbo;
        v.preExposure = skyOk ? pre * beaconUnit : 1;
        v.strength = beamStrength;
        v.jitter = !frozen;
        if (cloudsOk) {
            v.ceilingHeight = clouds.type.base;
            v.ceilingTexture = cloudGl.shadowTexture();
            v.ceilingRect = cloudGl.shadowRect();
        }
        if (s.lit() && showBeam) beaconGl.drawBeam(gl, v);
        java.util.List<BeaconGl.Glare> gl8 = new java.util.ArrayList<>();
        double[] e = camera.eye;
        double sigma = beacon.hazePerM + (rainOk && showRain ? rain.scatter(e[0], e[1], e[2]) * 3e-3 : 0);
        double eyeH = Math.max(0.5, e[1] - (wiring != null ? wiring.waterLevel.level() : 0));
        if (s.lit() && !insideLantern()) {
            double dl = Math.sqrt((e[0] - s.x()) * (e[0] - s.x()) + (e[1] - s.y()) * (e[1] - s.y()) + (e[2] - s.z()) * (e[2] - s.z()));
            double px = 2 * Math.tan(Math.toRadians(fovy()) / 2) / h * dl;
            gl8.add(new BeaconGl.Glare(s.x(), s.y(), s.z(), scale(s.illuminanceAt(e[0], e[1], e[2], sigma), fogT(s.x(), s.y(), s.z())), Math.max(0, Math.min(1, (2 * px - 0.9) / 0.9)), 2.5));
        }
        if (showNeighbours) {
            for (Beacon n : neighbours) {
                Beacon.Snapshot ns = n.snapshot();
                if (!ns.lit()) continue;
                double dist = Math.hypot(ns.x() - e[0], ns.z() - e[2]);
                // hinter der Kimm: geografische Tragweite aus Feuerhöhe (ohne Absenkung) und Augenhöhe
                if (dist > com.dan.beacon.Range.geographicRange(ns.y() + Site.curvatureDrop(dist), eyeH) * Site.NM) continue;
                gl8.add(new BeaconGl.Glare(ns.x(), ns.y(), ns.z(), scale(ns.illuminanceAt(e[0], e[1], e[2], sigma), fogT(ns.x(), ns.y(), ns.z())), 1, 3));
            }
        }
        beaconGl.drawGlare(gl, v, gl8);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        gl.glBindVertexArray(emptyVao);
    }

    /** Kopie der Szenentiefe für Gischt und Regen (weiche Ränder, Vorhänge, Krönchen). */
    private void copyDepth(GL4 gl, int w, int h) {
        if (sprayDepthTex == 0 || sprayDepthW != w || sprayDepthH != h) {
            Gpu.deleteFbos(gl, sprayDepthFbo);
            Gpu.deleteTextures(gl, sprayDepthTex);
            sprayDepthTex = Gpu.texture(gl, w, h, GL4.GL_DEPTH_COMPONENT32F, false);
            gl.glBindTexture(GL.GL_TEXTURE_2D, sprayDepthTex);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
            sprayDepthFbo = Gpu.depthFbo(gl, sprayDepthTex);
            sprayDepthW = w;
            sprayDepthH = h;
        }
        gl.glBindFramebuffer(GL4.GL_READ_FRAMEBUFFER, hdrFbo);
        gl.glBindFramebuffer(GL4.GL_DRAW_FRAMEBUFFER, sprayDepthFbo);
        gl.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL.GL_DEPTH_BUFFER_BIT, GL.GL_NEAREST);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
    }

    private void drawSpray(GL4 gl, Sky.State st, float[] vp, int w, int h, double pre) {
        sprayGl.update(gl, spray);
        if (sprayCheck.isEmpty()) { sprayCheck = sprayGl.selfCheckPoll(gl); if (sprayCheck.isEmpty() && spray.parcels().size() > 8) sprayGl.selfCheckStart(gl, spray.parcels(), spray.time()); }   // ohne Warten (Avatar-Phase 19)
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        SprayGl.View v = new SprayGl.View();
        v.viewProj = vp;
        v.eye = camera.eye.clone();
        v.forward = camera.fwd.clone();
        v.reversedZ = reversedZ;
        v.near = 0.2;
        v.far = 60_000;
        v.width = w;
        v.height = h;
        v.depthTexture = sprayDepthTex;
        // Phase 13 (Befund Status_Phase12: Gischt 9 ms bei Bft 10): große Bälle im Bild ausdünnen
        v.fovyDeg = fovy();
        v.lodPixels = sprayLodPixels;
        SprayGl.Light l = new SprayGl.Light();
        double[] sun = skyOk ? st.sunDir : Sun.direction(clock.sun()[0], clock.sun()[1]);
        l.sunDir = sun;
        if (skyOk) {
            l.sunColor = scale(st.sunColor, pre * cloudShade);
            l.moonDir = st.moonDir;
            l.moonColor = scale(st.moonColor, pre * cloudShade);
            double[][] fm = flashAsMoon(pre);
            if (fm != null && luminance(fm[1]) > luminance(l.moonColor)) {
                l.moonDir = fm[0];
                l.moonColor = fm[1];
            }
            l.skyTop = scale(st.skyIrradiance, pre / Math.PI);
            double a = Math.toRadians(camera.heading());
            l.skyHorizon = scale(st.radiance(new double[]{Math.sin(a), 0.03, -Math.cos(a)}), pre);
            l.fogDistance = fogDistance(st);
        } else {
            l.sunColor = sunLight(clock.sun()[0]);
        }
        gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
        sprayGl.draw(gl, v, l, spray.strength);
        gl.glDisable(GL.GL_BLEND);
        gl.glDepthMask(true);
    }

    /** Licht für Gischt und Regen aus dem Himmel, vorbelichtet: Sonne und Mond (hinter Wolken gedämpft), Himmel oben und am Horizont. */
    private double[][] particleLight(Sky.State st, double pre) {
        double[] sun = skyOk ? st.sunDir : Sun.direction(clock.sun()[0], clock.sun()[1]);
        if (!skyOk) return new double[][]{sun, sunLight(clock.sun()[0]), {0, -1, 0}, {0, 0, 0}, {0.2, 0.3, 0.5}, {0.5, 0.6, 0.7}, {40_000}};
        double a = Math.toRadians(camera.heading());
        double[][] r = {sun, scale(st.sunColor, pre * cloudShade), st.moonDir, scale(st.moonColor, pre * cloudShade),
                scale(st.skyIrradiance, pre / Math.PI), scale(st.radiance(new double[]{Math.sin(a), 0.03, -Math.cos(a)}), pre), {fogDistance(st)}};
        double[][] fm = flashAsMoon(pre);
        if (fm != null && luminance(fm[1]) > luminance(r[3])) {
            r[2] = fm[0];
            r[3] = fm[1];
        }
        return r;
    }

    /**
     * Phase 10: das stärkste Blitzlicht als Richtung und Bestrahlung am Auge (vorbelichtet), oder null. Gischt und
     * Regen kennen nur Sonne, Mond und Himmel; im Blitz nimmt es den Platz des Monds ein.
     */
    private double[][] flashAsMoon(double pre) {
        if (!lightningOk || !showLightning || !lightningGl.active() || lightningGl.lightCount() == 0) return null;
        double[] e = camera.eye, best = null, dir = null;
        double bestL = 0, sum = 0;
        double[] tot = new double[3];
        for (int i = 0; i < lightningGl.lightCount(); i++) {
            double[] p = lightningGl.lightPos(i), c = lightningGl.lightRgb(i);
            double dx = p[0] - e[0], dy = p[1] - e[1], dz = p[2] - e[2], d2 = dx * dx + dy * dy + dz * dz + p[3] * p[3], d = Math.sqrt(dx * dx + dy * dy + dz * dz + 1);
            double el = luminance(c) / d2;
            for (int k = 0; k < 3; k++) tot[k] += c[k] / d2 * pre;
            sum += el;
            if (el > bestL) { bestL = el; dir = new double[]{dx / d, dy / d, dz / d}; }
        }
        if (dir == null || sum <= 0) return null;
        return new double[][]{dir, tot};
    }

    private static double luminance(double[] c) { return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]; }

    /** Phase 10: Uniforms von lightning.glsl in einem Shader der App (ohne Blitz: keine Lichter). */
    private void bindLightning(GL4 gl, Shader sh) {
        if (lightningOk && showLightning && lightningGl.active()) {
            double[] air = lightningAir();
            lightningGl.bind(gl, sh.id(), skyOk ? exposure() : 1, air[0]);
        } else sh.set(gl, "ltgCount", 0);
    }

    private void drawRain(GL4 gl, Sky.State st, float[] vp, float[] inv, int w, int h, double pre) {
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        RainGl.View v = new RainGl.View();
        v.viewProj = vp;
        v.invViewProj = inv;
        v.eye = camera.eye.clone();
        v.forward = camera.fwd.clone();
        v.reversedZ = reversedZ;
        v.near = 0.2;
        v.far = 60_000;
        v.fovyDeg = fovy();
        v.width = w;
        v.height = h;
        v.depthTexture = sprayDepthTex;
        double[][] pl = particleLight(st, pre);
        RainGl.Light l = new RainGl.Light();
        l.sunDir = pl[0];
        l.sunColor = pl[1];
        l.moonDir = pl[2];
        l.moonColor = pl[3];
        // im Regen steht man unter der Wolke: Licht von oben nur, so weit sie es durchlässt (bedeckter Himmel dieser Art)
        double under = cloudsOk ? Math.min(1, clouds.type.overcastBrightness * 1.6) : 1;
        l.skyTop = scale(pl[4], under);
        l.skyHorizon = pl[5];
        l.fogDistance = pl[6][0];
        gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
        rainGl.draw(gl, v, l, rainStrength);
        gl.glDisable(GL.GL_BLEND);
        gl.glDepthMask(true);
    }

    // ------------------------------------------------------------------ Turm

    /** Das Gestirn, das Schatten wirft: {x, y, z, 1 = Sonne / 0 = Mond}, oder null (beide unter der Kimm). */
    double[] keyLight(Sky.State st, double[] sun) {
        if (sun[1] > 0.0) return new double[]{sun[0], sun[1], sun[2], 1};
        if (skyOk && st.moonDir[1] > 0.02 && st.moonFraction > 0.05) return new double[]{st.moonDir[0], st.moonDir[1], st.moonDir[2], 0};
        return null;
    }

    /**
     * Schattenkarte (Tiefe aus Richtung des Gestirns, rechtwinklig über Turm, Manschette und Steinschüttung) und der
     * Schatten auf dem Wasser: alle Ecken des Turms und des Peilpfahls entlang des Lichts auf die Wasserfläche gelegt,
     * von oben in ein Rechteck gezeichnet, das bis zur Spitze des Schattens reicht (höchstens 400 m).
     */
    private void renderShadows(GL4 gl, double[] key, double level) {
        shadowValid = false;
        if (key == null) return;
        int size = quality.towerShadow;
        if (size != shadowSize) {
            Gpu.deleteFbos(gl, shadowFbo);
            Gpu.deleteTextures(gl, shadowTex);
            shadowTex = Gpu.depthTexture(gl, size);
            shadowFbo = Gpu.depthFbo(gl, shadowTex);
            shadowSize = size;
        }
        double lx = key[0], ly = Math.max(0.03, key[1]), lz = key[2];
        double ll = Math.sqrt(lx * lx + ly * ly + lz * lz);
        lx /= ll; ly /= ll; lz /= ll;
        // Schattenkarte: Kugel um den Turm mit 24 m Halbmesser (Steinschüttung bis 11 m, Mastspitze 30,7 m)
        double cy = 12, rad = 24;
        float[] view = Mat4.lookAt(lx * 80, cy + ly * 80, lz * 80, 0, cy, 0);
        lightVP = Mat4.mul(ortho(rad, 80 - rad - 2, 80 + rad + 2, reversedZ), view);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, shadowFbo);
        gl.glViewport(0, 0, size, size);
        gl.glClearDepth(1);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);
        gl.glDepthMask(true);
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(1.5f, 2f);
        towerShadow.use(gl);
        towerShadow.mat(gl, "uLightVP", lightVP);
        Turm.Part far = turm.part("Alte Weser");
        towerMesh.drawRange(gl, 0, far.first());
        gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
        shadowValid = true;
        // Schatten auf dem Wasser: Rechteck vom Turm bis zur Spitze des Schattens
        double horiz = Math.hypot(lx, lz);
        double reach = Math.min(400, (Turm.MAST_TOP - level) * horiz / ly);
        double side = Math.max(40, reach + 24);
        double dx = horiz > 1e-6 ? -lx / horiz : 0, dz = horiz > 1e-6 ? -lz / horiz : 0;
        double cx = dx * reach / 2, cz = dz * reach / 2;
        castRect = new double[]{cx - side / 2, cz - side / 2, side};
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, castFbo);
        gl.glViewport(0, 0, CAST, CAST);
        gl.glClearColor(1, 1, 1, 1);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glDisable(GL.GL_DEPTH_TEST);
        towerCast.use(gl);
        towerCast.set(gl, "uLightDir", (float) lx, (float) ly, (float) lz);
        towerCast.set(gl, "uLevel", (float) level);
        towerCast.set(gl, "uRect", (float) castRect[0], (float) castRect[1], (float) castRect[2], 0f);
        towerCast.set(gl, "uZeroOne", reversedZ ? 1 : 0);
        Turm.Part t = turm.part("Turm"), pf = turm.part("Peilpfahl");
        towerMesh.drawRange(gl, t.first(), t.count());
        towerMesh.drawRange(gl, pf.first(), pf.count());
        gl.glClearColor(0, 0, 0, 1);
        gl.glClearDepth(reversedZ ? 0 : 1);
        gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
    }

    /** Rechtwinklige Projektion um die Blickachse, Tiefe 0..1 (glClipControl) oder −1..1. */
    static float[] ortho(double half, double near, double far, boolean zeroOne) {
        float[] m = new float[16];
        m[0] = (float) (1 / half);
        m[5] = (float) (1 / half);
        m[10] = (float) (zeroOne ? -1 / (far - near) : -2 / (far - near));
        m[14] = (float) (zeroOne ? -near / (far - near) : -(far + near) / (far - near));
        m[15] = 1;
        return m;
    }

    private void drawTower(GL4 gl, Sky.State st, double[] key) {
        Weathering.Snapshot ws = weathering.snapshot();
        if (weatheringOk) {
            weatheringGl.update(gl, ws);
            weatheringGl.bind(gl, tower.id(), ws, 6);
        }
        gl.glActiveTexture(GL.GL_TEXTURE5);
        gl.glBindTexture(GL.GL_TEXTURE_2D, shadowTex);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        tower.set(gl, "uShadowMap", 5);
        tower.set(gl, "uShadowOn", shadowValid && key != null && shadowTex != 0 ? 1 : 0);
        if (lightVP != null) tower.mat(gl, "uLightVP", lightVP);
        tower.set(gl, "uZeroOne", reversedZ ? 1 : 0);
        tower.set(gl, "uKeyIsSun", key != null && key[3] > 0.5 ? 1f : 0f);
        tower.set(gl, "uMapScale", (float) weathering.mapWidthM, (float) weathering.mapHeightM, (float) weathering.mapBottom);
        tower.set(gl, "uDetail", quality.towerDetail ? 1f : 0f);
        // Raumlampe im Laternenraum: an, sobald die Sonne unter 2° sinkt (das Feuer schaltet selbst, Phase 9)
        double sunAlt = skyOk ? st.sunAlt : clock.sun()[0];
        double glow = lampOn ? 1.5e-3 * (1 - smooth(-1, 2, sunAlt)) : 0;
        tower.set(gl, "uLampGlow", (float) (skyOk ? glow * exposure() : glow * 300));
        if (beaconOk && beacon.snapshot().lit()) {
            beaconGl.bind(gl, tower.id(), 10, skyOk ? exposure() * beaconUnit : 1);
            tower.set(gl, "uBeaconOn", 1f);
        } else {
            tower.set(gl, "uBeaconOn", 0f);
        }
        bindLightning(gl, tower);
        bindLamps(gl, tower);
        bindStair(gl, tower);
        tower.set(gl, "uInterior", 0);
        // Phase 15: die Alte Weser am Horizont nur beim Roten Sand selbst (sie ist der letzte Teil des Netzes)
        if (homeSite) towerMesh.draw(gl);
        else towerMesh.drawRange(gl, 0, turm.part("Alte Weser").first());
        // Phase 11: das Innere, nur wenn man es sehen kann
        if (inner == null && innerFuture != null && innerFuture.isDone()) {
            inner = innerFuture.join();
            innerMesh.upload(gl, inner.interleaved(), inner.idx, new int[]{3, 3, 3, 4, 4, 1, 4, 4});
        }
        if (interiorVisible() && inner != null) {
            bindInterior(gl, tower);
            tower.set(gl, "uInterior", 1);
            innerMesh.draw(gl);
            tower.set(gl, "uInterior", 0);
        }
    }

    /**
     * Rückfluss aus Timmendorf (Phase 18): die Leuchten außen an den Turm-Shader ({@code lamp.glsl}): Puffer, Zahl,
     * Vorbelichtung (lx → Einheiten der Szene); ohne Rückfluss oder bei Tag keine.
     */
    private void bindLamps(GL4 gl, Shader s) {
        lampK = 0;
        int n = 0;
        if (lampOk && showRueckfluss && aussen.an()) {
            lampGl.prepare(aussen.lampen, camera.eye.clone());
            lampGl.upload(gl);
            n = lampGl.count();
            lampK = skyOk ? exposure() * beaconUnit : 300 * beaconUnit;
        }
        if (n > 0) lampGl.bind(gl, s.id());
        else s.set(gl, "uLampCount", 0);
        s.set(gl, "uLampK", (float) lampK);
    }

    /** Rückfluss aus Timmendorf (Phase 18): die Flagge am Mast mit dem Licht von Sonne oder Mond und Himmel. */
    private void drawFlagge(GL4 gl, Sky.State st, float[] vp, double[] sun, double pre) {
        if (!flaggeOk || !showRueckfluss) return;
        double[][] L = lifeLight(st, sun, pre);
        boolean sunKey = !skyOk || st.sunAlt > -2;
        com.dan.cloth.gl.ClothGl.View v = new com.dan.cloth.gl.ClothGl.View();
        v.viewProj = vp;
        v.eye = camera.eye.clone();
        v.tonemap = false;
        v.exposure = 1;
        com.dan.cloth.gl.ClothGl.Light l = new com.dan.cloth.gl.ClothGl.Light();
        l.sunDir = sunKey || st == null ? sun : st.moonDir;
        l.sunColor = scale(sunKey ? L[0] : L[1], 1 / Math.PI);
        l.skyColor = L[2];
        l.groundColor = scale(L[3], 0.25);
        l.fogColor = L[3];
        l.fogDistance = skyOk ? fogDistance(st) : 40_000;
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
        gl.glDisable(GL.GL_BLEND);
        gl.glDisable(GL.GL_CULL_FACE);
        flaggeGl.upload(gl);
        flaggeGl.draw(gl, v, l);
        gl.glBindVertexArray(emptyVao);
    }

    /** Maße der Treppe für den Turm-Shader (Verdeckung, Innenraum). */
    private void bindStair(GL4 gl, Shader s) {
        s.set(gl, "uStairA", (float) Innenraum.FLOOR_Y, (float) Innenraum.RISE, (float) Innenraum.START_AZ, (float) Innenraum.STEP_DEG);
        s.set(gl, "uStairB", (float) Innenraum.WELL_R, (float) Innenraum.WALL_R, (float) Innenraum.THICK, (float) Innenraum.STEPS);
        s.set(gl, "uStairC", (float) Innenraum.ROOM_FLOOR, (float) Innenraum.SLAB, (float) Innenraum.ROOM_WALL_R, (float) Innenraum.CEIL_Y);
        s.set(gl, "uStairD", (float) Innenraum.SOLID_AZ0, (float) Innenraum.norm(Innenraum.SOLID_AZ1 - Innenraum.SOLID_AZ0));
    }

    /** Lichtquellen des Innenraums, vorbelichtet. */
    private void bindInterior(GL4 gl, Shader s) {
        double pre = skyOk ? exposure() : 1, u = lightUnit();
        int n = Innenraum.WINDOWS.size();
        float[] win = new float[4 * n], ax = new float[4 * n], sz = new float[4 * n], lw = new float[3 * n];
        for (int i = 0; i < n; i++) {
            Innenraum.Window w = Innenraum.WINDOWS.get(i);
            win[4 * i] = (float) w.x(); win[4 * i + 1] = (float) w.y(); win[4 * i + 2] = (float) w.z(); win[4 * i + 3] = w.round() ? 1 : 0;
            ax[4 * i] = (float) w.nx(); ax[4 * i + 2] = (float) w.nz(); ax[4 * i + 3] = (float) w.depth();
            sz[4 * i] = (float) w.a(); sz[4 * i + 1] = (float) w.b(); sz[4 * i + 2] = (float) w.area();
            for (int c = 0; c < 3; c++) lw[3 * i + c] = (float) (inWinL[i][c] * pre);
        }
        s.set(gl, "uInWinCount", n);
        s.vec4Array(gl, "uInWin", win);
        s.vec4Array(gl, "uInWinAx", ax);
        s.vec4Array(gl, "uInWinS", sz);
        s.vec3Array(gl, "uInWinL", lw);
        float[] lp = new float[4 * 7];
        for (int i = 0; i < 7 && i < Innenraum.LAMPS.size(); i++) {
            Innenraum.Lamp l = Innenraum.LAMPS.get(i);
            lp[4 * i] = (float) l.x(); lp[4 * i + 1] = (float) l.y(); lp[4 * i + 2] = (float) l.z(); lp[4 * i + 3] = l.room() ? 2 : l.name().startsWith("Wand") ? 1 : 0;
        }
        s.vec4Array(gl, "uInLamp", lp);
        double cd = stairLight ? Innenraum.lampCandela() * u * pre : 0, lb = stairLight ? Innenraum.lampCandela() / (Math.PI * Innenraum.BULB_R * Innenraum.BULB_R) * u * pre : 0;
        double[] k = Innenraum.LAMP_RGB;
        s.set(gl, "uInLampI", (float) (cd * k[0]), (float) (cd * k[1]), (float) (cd * k[2]));
        s.set(gl, "uInBulb", (float) (lb * k[0]), (float) (lb * k[1]), (float) (lb * k[2]));
        s.set(gl, "uInAmbS", (float) (inAmbS[0] / Math.PI * pre), (float) (inAmbS[1] / Math.PI * pre), (float) (inAmbS[2] / Math.PI * pre));
        s.set(gl, "uInAmbR", (float) (inAmbR[0] / Math.PI * pre), (float) (inAmbR[1] / Math.PI * pre), (float) (inAmbR[2] / Math.PI * pre));
        // Phase 12: das Barometer zeigt den Luftdruck der Wetterregie (ohne Regie wie in Phase 11 nach dem Wind)
        int bft = wiring != null ? com.dan.lighthouse.wiring.Beaufort.of(wiring.wind.speed()) : 4;
        double hPa = wiring != null && wiring.regie ? wiring.pressureHPa : Innenraum.pressure(bft);
        s.set(gl, "uBaro", (float) Innenraum.needle(hPa));
    }

    /**
     * Farbe des Sonnenlichts nach der Sonnenhöhe in Grad, wie sunLight() in common.glsl: weiß am Tag, golden und
     * schwächer tief am Himmel, aus unter der Kimm.
     */
    public static double[] sunLight(double elevDeg) {
        double e = Math.sin(Math.toRadians(elevDeg));
        double vis = smooth(-0.015, 0.01, e), t = smooth(0.02, 0.35, e), f = 2.6 * vis * smooth(-0.02, 0.12, e);
        return new double[]{f, (0.55 + 0.40 * t) * f, (0.28 + 0.60 * t) * f};
    }

    static double smooth(double a, double b, double x) {
        double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    /** Dunstlänge für exp(−d/L) aus der Sichtweite (Koschmieder: Kontrast 2 % bei der Sicht). */
    static double fogDistance(Sky.State st) { return st.visibilityKm * 1000 / 3.912; }

    static double[] scale(double[] v, double s) { return new double[]{v[0] * s, v[1] * s, v[2] * s}; }

    private void common(GL4 gl, Shader s, float[] vp, float[] inv, double[] sun, double level) {
        s.use(gl);
        boolean sm = skyOk && s != skyShader && s != skyPanoShader;
        s.set(gl, "uSkyMode", sm ? 1 : 0);
        if (sm) {
            Sky.State st = skyState();
            double pre = exposure();
            gl.glActiveTexture(GL.GL_TEXTURE7);
            gl.glBindTexture(GL.GL_TEXTURE_2D, panoTex);
            gl.glActiveTexture(GL.GL_TEXTURE0);
            s.set(gl, "uPano", 7);
            s.set(gl, "uSunColor", scale(st.sunColor, pre * cloudShade));
            s.set(gl, "uMoonColor", scale(st.moonColor, pre * cloudShade));
            s.set(gl, "uMoonDir", st.moonDir);
            s.set(gl, "uFogDist", (float) fogDistance(st));
            s.set(gl, "uPre", (float) pre);
        }
        s.mat(gl, "uVP", vp);
        s.mat(gl, "uInvVP", inv);
        s.set(gl, "uCam", camera.eye);
        s.set(gl, "uSun", sun);
        s.set(gl, "uTime", (float) (clock.t() % 10_000));
        s.set(gl, "uLevel", (float) level);
        s.set(gl, "uReversed", reversedZ ? 1 : 0);
    }

    private void resizeTargets(GL4 gl, int w, int h) {
        Gpu.deleteFbos(gl, hdrFbo);
        Gpu.deleteTextures(gl, hdrTex, depthTex);
        hdrTex = Gpu.texture(gl, w, h, GL.GL_RGBA16F, false);
        depthTex = Gpu.texture(gl, w, h, GL4.GL_DEPTH_COMPONENT32F, false);
        gl.glBindTexture(GL.GL_TEXTURE_2D, depthTex);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        hdrFbo = Gpu.fbo(gl, hdrTex, depthTex);
        hdrW = w;
        hdrH = h;
    }

    // ------------------------------------------------------------------ Zeitmessung

    private void mark(GL4 gl, int i) { gl.glQueryCounter(stamp[frame & 1][i], GL4.GL_TIMESTAMP); }

    /** Liest die Zeitstempel des vorigen Bilds, sobald sie da sind (kein Warten im Bildlauf). */
    private void readMarks(GL4 gl) {
        if (frame < 2) return;
        int o = (frame - 1) & 1;
        int[] av = new int[1];
        gl.glGetQueryObjectiv(stamp[o][MARKS - 1], GL4.GL_QUERY_RESULT_AVAILABLE, av, 0);
        if (av[0] == 0) return;
        long[] t = new long[MARKS];
        for (int i = 0; i < MARKS; i++) gl.glGetQueryObjecti64v(stamp[o][i], GL4.GL_QUERY_RESULT, t, i);
        for (int i = 0; i < MARKS - 1; i++) passMs[i] = passMs[i] * 0.9 + Math.max(0, t[i + 1] - t[i]) / 1e6 * 0.1;
        gpuMs = gpuMs * 0.9 + Math.max(0, t[MARKS - 1] - t[0]) / 1e6 * 0.1;
    }

    /** „Himmel 0,1 · See 0,6 · …“ in ms (gleitendes Mittel). */
    public String passTimes() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < passMs.length; i++)
            sb.append(i > 0 ? " · " : "").append(PASSES[i]).append(' ').append(String.format(Locale.GERMAN, "%.2f", passMs[i]));
        return sb.toString();
    }

    private void stats(long now, int w, int h) {
        statsFrames++;
        if (now - statsNanos < 500_000_000L) return;
        fps = statsFrames / ((now - statsNanos) / 1e9);
        statsFrames = 0;
        statsNanos = now;
        statusListener.accept(statusLine(w, h));
    }

    /** Phase 16: Auflösung nach der Zeit der Grafikkarte regeln (siehe {@link #autoScale}). */
    private void regulateScale(long now) {
        if (!autoScale || frame < 60 || now - scaleNanos < 1_500_000_000L) return;
        scaleNanos = now;
        double budget = 1000 / Math.max(10, targetFps), top = quality.renderScale, s = renderScale;
        // die Zeit der Grafikkarte wächst etwa mit der Fläche: gewünschter Faktor für die Kantenlänge
        double want = s * Math.sqrt(budget * 0.9 / Math.max(0.1, gpuMs));
        double next = s;
        if (gpuMs > budget * 1.05) next = Math.max(MIN_SCALE, Math.max(want, s - 0.15));
        else if (gpuMs < budget * 0.7 && s < top) next = Math.min(top, Math.min(want, s + 0.05));
        next = Math.round(next * 20) / 20.0;
        if (Math.abs(next - s) >= 0.045) { renderScale = next; scaleSteps++; }
    }

    /** Wie oft die Regelung die Auflösung geändert hat (für Prüfung und Dauerlauf). */
    public int scaleSteps() { return scaleSteps; }

    /** Für Prüfungen: die Regelung mit vorgegebener Grafikkartenzeit und Bildnummer. */
    public void regulateForTest(double gpuMsNow, long nowNanos) {
        gpuMs = gpuMsNow;
        frame = Math.max(frame, 60);
        regulateScale(nowNanos);
    }

    public String statusLine(int w, int h) {
        String s = String.format(Locale.GERMAN, "%s · %.0f Bilder/s · Grafikkarte %.2f ms · Prozessor %.2f ms · %d×%d (%s%s) · %s",
                clock.label(), fps, gpuMs, cpuMs, w, h, quality.label, autoScale ? String.format(Locale.GERMAN, ", %.0f %%", renderScale * 100) : "", glInfo);
        if (System.currentTimeMillis() < photoMsgUntil) s = photoMsg + " · " + s;
        return s;
    }

    // ------------------------------------------------------------------ HUD

    BufferedImage paintHud() {
        String[] lines = hudFull ? hudLines() : hudShort();
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = probe.createGraphics();
        java.awt.FontMetrics fm = pg.getFontMetrics(font);
        int tw = 0;
        for (String l : lines) tw = Math.max(tw, fm.stringWidth(l.startsWith("!") ? l.substring(1) : l));
        pg.dispose();
        int w = Math.max(200, tw + 22), lh = 17, h = 16 + lines.length * lh;
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(8, 14, 22, 175));
        g.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
        g.setColor(new Color(226, 67, 47, 200));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(8, 1, 60, 1);
        g.setFont(font);
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i];
            boolean warn = l.startsWith("!");
            if (warn) l = l.substring(1);
            g.setColor(i == 0 ? new Color(245, 240, 230) : warn ? new Color(240, 170, 80) : new Color(185, 200, 210));
            g.drawString(l, 10, 18 + i * lh);
        }
        g.dispose();
        return im;
    }

    /** Phase 16: das kurze HUD. Meldungen mit ! aus den vollen Zeilen (Fehler, laufender Test) kommen dazu. */
    String[] hudShort() {
        java.util.List<String> l = new java.util.ArrayList<>();
        String q = autoScale ? String.format(Locale.GERMAN, "%s %.0f %%", quality.label, renderScale * 100) : quality.label;
        l.add(String.format(Locale.GERMAN, "%.0f Bilder/s · Grafikkarte %.1f ms · Prozessor %.1f ms · %s %d×%d", fps, gpuMs, cpuMs, q, hdrW, hdrH));
        String wetter = weatherLine().replace("Wetter ", "").replaceAll(" · nächste Lage.*", "").replaceAll(" · \\d+ von Hand", "");
        Beacon.Snapshot bs = beacon.snapshot();
        l.add(String.format(Locale.GERMAN, "%s · %s · %s %s%s", clock.label(), wetter, beacon.name, beacon.characteristic().text, bs.lit() ? " an" : " aus"));
        double eyeH = camera.eye[1] - (wiring != null ? wiring.waterLevel.level() : 0);
        String ride = camera.mode == Camera.Mode.FAHRT ? String.format(Locale.GERMAN, "%s %.0f s", camera.rideName(), camera.rideTime())
                : camera.mode == Camera.Mode.FOLGEN ? camera.followName : camera.mode == Camera.Mode.FREI ? "Frei" : camera.autoOrbit ? "Kreisen (läuft)" : "Kreisen";
        l.add(String.format(Locale.GERMAN, "%s · Auge %.1f m · Blick %03.0f° · Turm %.0f m · H: volles HUD, F1: Tasten", ride, eyeH, camera.heading(), camera.towerDistance()));
        for (String s : hudLines()) if (s.startsWith("!")) l.add(s);
        if (System.currentTimeMillis() < photoMsgUntil) l.add(photoMsg);
        return l.toArray(new String[0]);
    }

    /** Alle Zeilen des vollen HUD (auch für den Block TECHNIK). */
    public String[] hudLines() {
        double[] sp = clock.sun();
        double eyeH = camera.eye[1] - (wiring != null ? wiring.waterLevel.level() : 0);
        ComputeProbe.Result c = compute;
        String wind = wiring == null ? "—" : String.format(Locale.GERMAN, "Wind %.1f m/s aus %03.0f° · Bft %d %s",
                wiring.wind.speed(), wiring.wind.fromDeg(), wiring.wind.beaufort(), Beaufort.NAME[wiring.wind.beaufort()]);
        return new String[]{
                String.format(Locale.GERMAN, "%.0f Bilder/s   Grafikkarte %.2f ms   Prozessor %.2f ms", fps, gpuMs, cpuMs),
                passTimes(),
                (c == null ? "!Compute-Test läuft …" : (c.ok() ? "" : "!") + c.line()),
                String.format(Locale.GERMAN, "%s · Sonne %.1f° / %03.0f°", clock.label(), skyOk ? skyState().sunAlt : sp[0], skyOk ? skyState().sunAz : sp[1]),
                weatherLine(),
                skyLine(),
                cloudLine(),
                wind,
                towerLine(),
                sprayLine(),
                rainLine(),
                beaconLine(),
                showRueckfluss ? aussen.zeile() + (lampOk ? "" : " · !ohne LampGl") + (flaggeOk ? "" : " · !ohne ClothGl") : "Rückfluss aus Timmendorf aus",
                lightningLine(),
                glassLine(),
                fogLine(),
                lifeLine(),
                soundLine(),
                oceanLine(),
                String.format(Locale.GERMAN, "%s · Auge %.1f m · Blick %03.0f° · Turm %.0f m · Kimm %.1f sm",
                        camera.mode == Camera.Mode.FAHRT ? String.format(Locale.GERMAN, "Aufstieg %.0f s", camera.rideTime()) : camera.mode == Camera.Mode.FOLGEN ? camera.followName : camera.mode == Camera.Mode.FREI ? "Frei" : camera.autoOrbit ? "Kreisen (läuft)" : "Kreisen",
                        eyeH, camera.heading(), camera.towerDistance(), Site.horizon(eyeH) / Site.NM),
                String.format(Locale.GERMAN, "%s · %d×%d · %s", quality.label, hdrW, hdrH, reversedZ ? "umgekehrte Tiefe" : "klassische Tiefe"),
                glInfo};
    }

    /** „Himmel Nautische Dämmerung · Mond 24 % … · Sicht 40 km · Grenzgröße 5,1 · Belichtung 2,3e3 · GPU = CPU ± 0,1 %“ */
    String skyLine() {
        if (!skyOk) return "!Himmel: Verlauf aus Phase 1 (com.dan.sky nicht gestartet)";
        Sky.State st = skyState();
        String chk = skyCheck.isEmpty() ? "Abgleich folgt" : skyCheck.replaceAll("Grafikkarte = Prozessor: Himmel ±", "GPU = CPU ±").replaceAll(",.*", "");
        return String.format(Locale.GERMAN, "Himmel %s · Mond %.0f %% %s %.0f° · Sicht %.0f km · Grenzgröße %.1f · Belichtung %.2g%s · %s",
                st.twilight(), st.moonFraction * 100, st.moonWaxing ? "zun." : "abn.", st.moonAlt, st.visibilityKm, st.limitingMagnitude, exposure(),
                sky.rebuilding() ? " · baut neu" : "", chk);
    }

    /** „Wolken Cumulus 3/8 · Basis 900 m · … · Sonne am Turm 100 % · GPU = CPU ± 0,002“ */
    String cloudLine() {
        if (!cloudsOk) return "!Wolken: nicht gestartet (Schleier aus Phase 3)";
        String chk = cloudCheck.isEmpty() ? "Abgleich folgt" : cloudCheck.replaceAll("Grafikkarte = Prozessor: Wolkenschatten ±", "GPU = CPU ±").replaceAll(" \\(.*\\)", "");
        return String.format(Locale.GERMAN, "Wolken %s · Sonne am Turm %.0f %% · %s", clouds.describe().replaceAll(" · Zug.*", ""), cloudShade * 100, chk);
    }

    /** „Meer Bft 4 · Hs 1,1 m · Tp 5,0 s · Schaum 2 % · FFT 4 × 256² · GPU = CPU ± 0,4 mm“ */
    String oceanLine() {
        if (!oceanOk) return "!Meer: Platzhalter-See (Compute-Shader fehlen)";
        Ocean.State st = ocean.state();
        String chk = oceanCheck.isEmpty() ? "Abgleich folgt" : oceanCheck.replace("Grafikkarte = Prozessor: größte Abweichung", "GPU = CPU ±").replaceAll(" \\(.*\\)", "");
        return String.format(Locale.GERMAN, "Meer %s · Hs %.1f m · Tp %.1f s · Schaum %.0f %%%s · %s · %s",
                com.dan.ocean.SeaState.describe(ocean.windSpeed).replaceAll(" · Douglas.*", ""), st.totalHs(), st.peakPeriod, st.foamCoverage * 100,
                ocean.rebuilding() ? " · baut neu" : "", oceanGl.info().replaceAll(", Ringgitter.*", ""), chk);
    }

    // ------------------------------------------------------------------ Foto

    private void takePhoto(GL4 gl, int w, int h) {
        try {
            BufferedImage img = readDefault(gl, w, h);
            File dir = photoDir != null ? photoDir : new File("bilder");
            if (!dir.isDirectory() && !dir.mkdirs()) throw new java.io.IOException("Ordner " + dir + " lässt sich nicht anlegen");
            File f = new File(dir, "Lighthouse_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png");
            javax.imageio.ImageIO.write(img, "png", f);
            photoMsg = "Foto gespeichert: " + f.getPath();
            System.out.println(photoMsg);
        } catch (Exception e) {
            photoMsg = "Foto fehlgeschlagen: " + e.getMessage();
            System.err.println(photoMsg);
        }
        photoMsgUntil = System.currentTimeMillis() + 5000;
    }

    /** Liest das fertige Fensterbild (nach Nachbearbeitung und HUD). */
    BufferedImage readDefault(GL4 gl, int w, int h) {
        ByteBuffer bb = Buffers.newDirectByteBuffer(w * h * 4);
        gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, defaultFbo);
        gl.glReadBuffer(defaultFbo == 0 ? GL.GL_BACK : GL.GL_COLOR_ATTACHMENT0);
        gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
        gl.glReadPixels(0, 0, w, h, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, bb);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[] row = new int[w];
        for (int y = 0; y < h; y++) {
            int o = (h - 1 - y) * w * 4;
            for (int x = 0; x < w; x++) {
                int i = o + x * 4;
                row[x] = ((bb.get(i) & 255) << 16) | ((bb.get(i + 1) & 255) << 8) | (bb.get(i + 2) & 255);
            }
            img.setRGB(0, y, w, 1, row, 0, w);
        }
        return img;
    }

    /** Für Werkzeuge: das letzte Bild als Bild (im GL-Faden aufrufen). */
    public BufferedImage grab(GLAutoDrawable d) {
        GL4 gl = d.getGL().getGL4();
        defaultFbo = d.getContext().getDefaultDrawFramebuffer();
        return readDefault(gl, d.getSurfaceWidth(), d.getSurfaceHeight());
    }

    // ------------------------------------------------------------------ Ende

    private void fail(String msg) {
        error = msg;
        System.err.println(msg);
        errorListener.accept(msg);
    }

    @Override
    public void dispose(GLAutoDrawable d) {
        GL4 gl = d.getGL().getGL4();
        for (Shader s : all) s.dispose(gl);
        towerMesh.dispose(gl);
        innerMesh.dispose(gl);
        if (lampOk) lampGl.dispose(gl);
        if (flaggeOk) flaggeGl.dispose(gl);
        if (sprayOk) sprayGl.dispose(gl);
        Gpu.deleteFbos(gl, sprayDepthFbo);
        Gpu.deleteTextures(gl, sprayDepthTex);
        sprayDepthTex = sprayDepthFbo = 0;
        if (weatheringOk) weatheringGl.dispose(gl);
        Gpu.deleteFbos(gl, shadowFbo, castFbo);
        Gpu.deleteTextures(gl, shadowTex, castTex);
        shadowSize = 0;
        hud.dispose(gl);
        if (kinoGl != null) kinoGl.dispose(gl);
        if (oceanOk) oceanGl.dispose(gl);
        if (brandungOk) brandungGl.dispose(gl);
        brandungOk = false;
        if (beaconOk) beaconGl.dispose(gl);
        if (lightningOk) lightningGl.dispose(gl);
        if (skyOk) skyGl.dispose(gl);
        if (cloudsOk) cloudGl.dispose(gl);
        Gpu.deleteFbos(gl, panoFbo);
        Gpu.deleteTextures(gl, panoTex);
        Gpu.deleteFbos(gl, hdrFbo);
        Gpu.deleteTextures(gl, hdrTex, depthTex);
        if (emptyVao != 0) gl.glDeleteVertexArrays(1, new int[]{emptyVao}, 0);
        for (int[] s : stamp) if (s[0] != 0) gl.glDeleteQueries(MARKS, s, 0);
        ready = false;
    }
}
