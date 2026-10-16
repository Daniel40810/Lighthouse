package com.dan.soundscape;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Erzeugter Ton ohne Audiodateien: Wind (Rauschen und Äolstöne an Stäben und Seilen), See und Brandung, Regen auf dem
 * Wasser, auf Glas und auf einem Dach, Brecher, Nebelhorn und Typhon, Möwen, Donner aus dem Kanal eines Blitzes und
 * der Diesel eines Schiffs. Alles gilt für ein Ohr mit Ort und Blickrichtung: Abstand (1/r), Laufzeit (Schall mit
 * c nach Temperatur), Luftdämpfung (die Ferne verliert die Höhen), Richtung (Stereo mit Laufzeitunterschied der Ohren).
 * Drinnen dämpft die Wand den Schall von draußen und der Raum hallt ({@link Room}).
 * <p>
 * <b>Pegel:</b> alles in dB SPL; ein Effektivwert von 1 im Signal sind {@link #REF_DB} dB. Was lauter ist, fängt ein
 * Begrenzer ab. <b>Zahlen:</b> Äolston f = 0,2 · U / d (Strouhal), Hörner nach KVR Anlage III (Typhon 130 bis
 * 350 Hz für 75 bis 200 m Länge, 138 dB in 1 m), Donner nach Few (N-Wellen vom Kanal).
 * <p>
 * <b>Benutzung:</b> Eingänge setzen (je Bild), Ereignisse melden ({@link #event}, {@link #horn}, {@link #thunder}),
 * {@link #render} liefert Stereo in {@link #sampleRate()} (ein Faden: {@link AudioOut}; ohne Lautsprecher: rechnen
 * und auswerten oder als WAV schreiben). Nur Java 21; die Ausgabe auf Lautsprecher nimmt {@code javax.sound.sampled}
 * aus dem JDK.
 */
public final class Soundscape {

    /** Stufen: Abtastrate, gleichzeitige Stimmen, Äolskörper, Regentropfen je s, Hall. */
    public enum Quality {
        LOW(22_050, 12, 2, 300, false), MEDIUM(32_000, 24, 4, 900, true), HIGH(44_100, 32, 6, 1800, true), ULTRA(48_000, 48, 10, 3500, true);
        public final int sampleRate, voices, bodies, drops;
        public final boolean reverb;
        Quality(int sr, int v, int b, int d, boolean rv) { sampleRate = sr; voices = v; bodies = b; drops = d; reverb = rv; }
    }

    /** Ereignisse, die klingen. */
    public enum Kind { BRECHER, NEBELHORN, TYFON, MOEWE, DONNER }

    /** Signal mit Effektivwert 1 entspricht so vielen dB SPL. */
    public static final double REF_DB = 104;
    /** Strouhal-Zahl für Zylinder quer zur Strömung (Re 10³ bis 10⁵). */
    public static final double STROUHAL = 0.2;
    /** Rauigkeit der See für das Windprofil (m). */
    public static final double Z0 = 2e-4;
    static final int BLOCK = 256;

    // ------------------------------------------------------------------ Eingänge
    public volatile long seed = 1;
    public volatile Quality quality = Quality.HIGH;
    /** Lautstärke 0..2 (1 = wie gerechnet). */
    public volatile double volume = 1;
    /**
     * Das Ohr passt sich an (wie das Auge an die Helligkeit): Über 4 s gemittelt zieht eine langsame Verstärkung den
     * Pegel zu {@link #AUTO_TARGET_DB}, höchstens 18 dB hinauf und 12 dB hinab. Kurze laute Ereignisse (Donner, Horn)
     * bleiben laut; eine stille Nacht wird hörbar, ein Orkan nicht unerträglich. false = Pegel wie gerechnet.
     */
    public volatile boolean autoGain = true;
    public static final double AUTO_TARGET_DB = 84;
    /** Wind in 10 m Höhe (m/s), woher (Grad von Nord), Böigkeit 0..1 (Standardabweichung als Anteil). */
    public volatile double windSpeed = 6, windFromDeg = 240, gustiness = 0.2;
    /** Regen (mm/h) am Ohr. */
    public volatile double rainRate;
    /** Seegang: signifikante Höhe (m) und Periode (s). */
    public volatile double waveHeight = 1, wavePeriod = 5;
    /** Ein Bauwerk im Wasser, an dem die Wellen brechen: Mitte und Radius (m); Radius 0 = keins. Stärke 0..1. */
    public volatile double surfX, surfZ, surfRadius, surfStrength = 1;
    /** Wasserspiegel (m). */
    public volatile double waterLevel;
    /** Lufttemperatur (°C) für die Schallgeschwindigkeit. */
    public volatile double temperatureC = 11;
    /** Das Ohr: Ort (m) und Blickrichtung (Grad von Nord). */
    public volatile double earX, earY = 2, earZ, earYawDeg;
    /** Wie weit drinnen 0..1 und in welchem Raum. */
    public volatile double inside;
    public volatile Room room = Room.OUTDOORS;
    /** Tropfen je s auf Glas nahe dem Ohr (drinnen hinter der Scheibe) und auf einem Dach darüber. */
    public volatile double glassDrops, roofDrops;
    /** Körper, an denen der Wind singt, und dauernde Quellen (Schiffsdiesel). */
    public final List<Aeolian> bodies = new CopyOnWriteArrayList<>();
    public final List<Hum> hums = new CopyOnWriteArrayList<>();

    // ------------------------------------------------------------------ Zustand (nur im Faden von render)
    private int sr;
    private long frame;
    private Dsp.Pink windL, windR, seaL, seaR, rainL, rainR;
    private Dsp.Brown earNoise, humNoise, seaLow;
    private Dsp.Rng rng;
    private final Dsp.Biquad[] windBp = new Dsp.Biquad[2], windHp = new Dsp.Biquad[2], seaLp = new Dsp.Biquad[2], rainHp = new Dsp.Biquad[2],
            rainPeak = new Dsp.Biquad[2], wallLp = new Dsp.Biquad[4];
    private final Dsp.Biquad earLp = new Dsp.Biquad(), humLp = new Dsp.Biquad(), surfLp = new Dsp.Biquad();
    private Dsp.Biquad[] bodyBp = new Dsp.Biquad[0];
    private double[] bodyPh = new double[0], bodyF = new double[0];
    private Dsp.Rng[] bodyRng = new Dsp.Rng[0];
    private Dsp.Reverb reverb;
    private Dsp.Limiter limiter;
    private double agcEnergy = -1, agcGain = 1;
    private double gust, gustAm, seaPhase, seaGroup = 1, surfNext, surfT = 99, surfAmp, surfLen = 2, humPh;
    private final List<Voice> voices = new ArrayList<>();
    private final ConcurrentLinkedQueue<Voice> pending = new ConcurrentLinkedQueue<>();
    private final Pings drops = new Pings(), ticks = new Pings(), roof = new Pings();
    /** Gezählte Tropfen auf Glas und Dach (seit Beginn). */
    public long glassTicks() { return ticks.spawned; }
    public long roofTicks() { return roof.spawned; }
    private final float[] oL = new float[BLOCK], oR = new float[BLOCK], iL = new float[BLOCK], iR = new float[BLOCK], tmp = new float[BLOCK];
    private Quality built;
    private long builtSeed;
    private long voiceSeed = 1;
    private final float[] humTable = Voice.Horn.harmonics(6, 1.2f);

    /** Pegel der Teile am Ohr (dB SPL, geglättet): Wind, Äolstöne, See, Brandung, Regen, Glas und Dach, Maschine, Ereignisse, alles. */
    public static final String[] PARTS = {"Wind", "Äolstöne", "See", "Brandung", "Regen", "Glas und Dach", "Maschine", "Ereignisse", "alles"};
    private final double[] energy = new double[PARTS.length];
    private volatile double[] levels = new double[PARTS.length];
    private volatile double reductionDb;
    private volatile String loudestVoice = "";
    private volatile double lastVoiceAt = -1e9;
    private volatile int activeVoices;
    private volatile String bodyText = "";
    private long renderNanos, renderFrames;
    /** Gleitendes Mittel der Rechenzeit über rund 5 s (seit Phase 16; vorher der Mittelwert seit dem Start samt Aufwärmen). */
    private double loadMs = -1;

    public Soundscape() { configure(); }

    // ------------------------------------------------------------------ Presets

    public static Soundscape calm() { return beaufort(1); }
    public static Soundscape breeze() { return beaufort(4); }
    public static Soundscape gale() { return beaufort(8); }
    public static Soundscape storm() { return beaufort(10); }

    static final double[] WIND = {0.2, 0.9, 2.4, 4.4, 6.7, 9.3, 12.3, 15.5, 18.9, 22.6, 26.4, 30.5, 34.0};
    static final double[] HS = {0.05, 0.1, 0.2, 0.6, 1.0, 2.0, 3.0, 4.0, 5.5, 7.0, 9.0, 11.5, 14.0};
    static final double[] TP = {3, 3, 3.5, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    static final double[] RAIN = {0, 0, 0, 0, 0, 0, 0.5, 1, 2, 4, 6, 10, 15};

    /** Wind, Seegang und Regen nach Beaufort; Stäbe zum Singen bringt erst der Aufrufer ({@link #bodies}). */
    public static Soundscape beaufort(int bft) {
        int b = Math.max(0, Math.min(12, bft));
        Soundscape s = new Soundscape();
        s.windSpeed = WIND[b];
        s.waveHeight = HS[b];
        s.wavePeriod = TP[b];
        s.rainRate = RAIN[b];
        s.gustiness = b >= 8 ? 0.25 : 0.18;
        return s;
    }

    // ------------------------------------------------------------------ Abfragen

    public int sampleRate() { return quality.sampleRate; }
    /** Gerechnete Zeit in s. */
    public double time() { return sr > 0 ? frame / (double) sr : 0; }
    /** Schallgeschwindigkeit (m/s) bei {@link #temperatureC}. */
    public double soundSpeed() { return 331.3 * Math.sqrt(1 + temperatureC / 273.15); }
    /** Pegel je Teil (dB SPL am Ohr), Reihenfolge wie {@link #PARTS}. */
    public double[] levels() { return levels.clone(); }
    /** Stärkste Absenkung des Begrenzers seit dem letzten Abruf (dB, 0 = keine). */
    public double reductionDb() { return reductionDb; }
    public int activeVoices() { return activeVoices; }
    /** Verstärkung der Anpassung des Ohrs (dB). */
    public double autoGainDb() { return 20 * Math.log10(agcGain); }
    /** Wind am Ohr samt Profil (m/s). */
    public double windAt(double height) { return windSpeed * Math.log(Math.max(1, height) / Z0) / Math.log(10 / Z0); }
    /** Rechenzeit je Sekunde Ton (ms). */
    public double msPerSecond() { return loadMs < 0 ? 0 : loadMs; }

    // ------------------------------------------------------------------ Ereignisse

    /** Ein Ereignis am Ort (x, y, z) mit Stärke 0..1; es erreicht das Ohr nach Abstand / c. */
    public void event(Kind k, double x, double y, double z, double strength) {
        switch (k) {
            case BRECHER -> enqueue(new Voice.Breaker(x, y, z, strength, nextSeed(), sampleRate()));
            case MOEWE -> enqueue(new Voice.Gull(x, y, z, strength, nextSeed(), sampleRate()));
            case NEBELHORN -> horn(k, x, y, z, strength, 1.0);
            case TYFON -> horn(k, x, y, z, strength, 6.0);
            case DONNER -> thunder(x, y, z, 70 + 40 * strength, null, null, null, true);
        }
    }

    /**
     * Ein Ton eines Nebelsignals von gegebener Dauer (s). Nebelhorn: Diaphon mit 175 Hz und 132 dB in 1 m [G];
     * Typhon: 220 Hz, 138 dB in 1 m (KVR Anlage III für 75 bis 200 m Länge).
     */
    public void horn(Kind k, double x, double y, double z, double strength, double seconds) {
        boolean ty = k == Kind.TYFON;
        double lv = (ty ? 138 : 132) + 6 * Math.log10(Math.max(0.05, strength));
        enqueue(new Voice.Horn(ty ? "Typhon" : "Nebelhorn", x, y, z, lv, ty ? 220 : 175, seconds, ty, nextSeed(), sampleRate()));
    }

    /**
     * Donner. distances und weights beschreiben den Kanal (Abstand jedes Stücks zum Ohr, Gewicht), strokes die Zeiten
     * der Schläge; fehlen sie, rollt ein Donner aus einem senkrechten Kanal über dem Ort. levelDb: Pegel am Ohr.
     * arrived = true, wenn der Aufrufer die Laufzeit schon abgewartet hat (der Donner beginnt jetzt).
     */
    public void thunder(double x, double y, double z, double levelDb, double[] distances, double[] weights, double[] strokes, boolean arrived) {
        double c = soundSpeed();
        double[] dist = distances, w = weights;
        if (dist == null || w == null) {
            // ein gewundener Kanal von der Wolke (1,5 km über dem Ort) zum Ort: Stücke von 6 m, die Richtung springt
            // um ±16° (Hill 1968), dazu drei Äste
            Dsp.Rng r = new Dsp.Rng(nextSeed());
            List<double[]> seg = new ArrayList<>();
            double top = Math.max(y + 300, 1500);
            for (int b = 0; b < 4; b++) {
                double px = x, py = b == 0 ? top : top - (top - y) * (0.2 + 0.5 * r.uniform()), pz = z, dx = 0, dz = 0;
                int steps = b == 0 ? (int) ((top - y) / 5.5) : 40 + (int) (r.uniform() * 60);
                for (int i = 0; i < steps && py > y - 1; i++) {
                    dx = Math.max(-0.6, Math.min(0.6, dx + 0.28 * r.gauss()));
                    dz = Math.max(-0.6, Math.min(0.6, dz + 0.28 * r.gauss()));
                    double l = Math.sqrt(1 + dx * dx + dz * dz), st = 6 / l;
                    double nx = px + dx * st, ny = py - st, nz = pz + dz * st;
                    seg.add(new double[]{(px + nx) / 2, (py + ny) / 2, (pz + nz) / 2, b == 0 ? 1 : 0.45});
                    px = nx; py = ny; pz = nz;
                }
            }
            dist = new double[seg.size()];
            w = new double[seg.size()];
            for (int i = 0; i < seg.size(); i++) {
                double[] q = seg.get(i);
                dist[i] = Math.sqrt(sq(q[0] - earX) + sq(q[1] - earY) + sq(q[2] - earZ));
                w[i] = q[3];
            }
        }
        float[] data = Thunder.build(sampleRate(), dist, w, strokes, c, nextSeed());
        Voice v = new Voice.Buffered("Donner", x, y, z, levelDb, data);
        double dmin = Double.MAX_VALUE;
        for (double d : dist) dmin = Math.min(dmin, d);
        v.distance = dmin;
        if (!arrived) v.delaySeconds = dmin / c;
        enqueue(v);
    }

    private synchronized long nextSeed() { return seed * 7919 + (voiceSeed++) * 104729; }

    private void enqueue(Voice v) {
        v.start = Long.MIN_VALUE;
        pending.add(v);
    }

    /** Zuletzt erklungenes Ereignis und vor wie viel s (für eine Anzeige). */
    public String lastVoice() { return loudestVoice.isEmpty() ? "" : String.format(Locale.GERMAN, "%s vor %.0f s", loudestVoice, time() - lastVoiceAt); }

    // ------------------------------------------------------------------ Rechnen

    private void configure() {
        Quality q = quality;
        sr = q.sampleRate;
        long s = seed;
        rng = new Dsp.Rng(s);
        windL = new Dsp.Pink(s + 1); windR = new Dsp.Pink(s + 2);
        seaL = new Dsp.Pink(s + 3); seaR = new Dsp.Pink(s + 4);
        rainL = new Dsp.Pink(s + 5); rainR = new Dsp.Pink(s + 6);
        earNoise = new Dsp.Brown(s + 7); humNoise = new Dsp.Brown(s + 8); seaLow = new Dsp.Brown(s + 9);
        for (int k = 0; k < 2; k++) {
            windBp[k] = new Dsp.Biquad(); windHp[k] = new Dsp.Biquad().highpass(40, 0.7, sr);
            seaLp[k] = new Dsp.Biquad(); rainHp[k] = new Dsp.Biquad().highpass(900, 0.7, sr);
            rainPeak[k] = new Dsp.Biquad().peaking(Math.min(13_000, sr * 0.4), 1.2, 5, sr);
        }
        for (int k = 0; k < 4; k++) wallLp[k] = new Dsp.Biquad().lowpass(sr * 0.45, 0.7, sr);
        earLp.lowpass(150, 0.7, sr);
        humLp.lowpass(180, 0.7, sr);
        surfLp.lowpass(1400, 0.6, sr);
        reverb = new Dsp.Reverb(sr, 1.6);
        limiter = new Dsp.Limiter(sr, -1);
        drops.clear(); ticks.clear(); roof.clear();
        voices.clear();
        built = q;
        builtSeed = s;
    }

    /**
     * Rechnet frames Stereowerte (verschränkt l, r) in out, ab out[0]. Eingänge und Ereignisse wirken ab dem nächsten
     * Block von 256 Werten.
     */
    public void render(float[] out, int frames) {
        long t0 = System.nanoTime();
        if (built != quality || builtSeed != seed) configure();
        int done = 0;
        while (done < frames) {
            int n = Math.min(BLOCK, frames - done);
            block(n);
            for (int i = 0; i < n; i++) {
                out[2 * (done + i)] = oL[i];
                out[2 * (done + i) + 1] = oR[i];
            }
            done += n;
        }
        renderNanos += System.nanoTime() - t0;
        renderFrames += frames;
        if (renderFrames >= sr / 2) {
            double ms = renderNanos / 1e6 / (renderFrames / (double) Math.max(1, sr));
            loadMs = loadMs < 0 ? ms : loadMs * 0.9 + ms * 0.1;
            renderNanos = 0;
            renderFrames = 0;
        }
    }

    /** Rechnet seconds Sekunden auf einmal (für Prüfungen und Dateien). */
    public float[] render(double seconds) {
        int n = (int) Math.round(seconds * sampleRate());
        if (built != quality || builtSeed != seed) configure();
        float[] out = new float[2 * n];
        render(out, n);
        return out;
    }

    private void block(int n) {
        Quality q = built;
        double dt = n / (double) sr;
        java.util.Arrays.fill(oL, 0, n, 0);
        java.util.Arrays.fill(oR, 0, n, 0);
        java.util.Arrays.fill(iL, 0, n, 0);
        java.util.Arrays.fill(iR, 0, n, 0);
        double[] e = new double[PARTS.length];
        double c = soundSpeed();
        double ex = earX, ey = earY, ez = earZ, yaw = Math.toRadians(earYawDeg);
        double ins = Math.max(0, Math.min(1, inside));
        boolean outdoorEar = ins < 0.5;

        // Böen: Ornstein-Uhlenbeck in Echtzeit (3 s), dazu langsames Schwellen des Rauschens
        double gs = Math.max(0, gustiness);
        gust += -gust * dt / 3 + gs * Math.sqrt(2 * dt / 3) * rng.gauss();
        gust = Math.max(-0.8, Math.min(1.5, gust));
        gustAm += -gustAm * dt / 1.2 + 0.25 * Math.sqrt(2 * dt / 1.2) * rng.gauss();

        // ---------------- Wind: Rauschen
        double ue = Math.max(0, windAt(ey) * (1 + gust));
        double lw = 22 + 50 * Math.log10(Math.max(0.3, ue));
        float aw = (float) (Dsp.amp(lw) * Math.pow(10, gustAm * 0.15) * 1.6);
        windBp[0].bandpass(120 + 22 * ue, 0.55, sr);
        windBp[1].bandpass(130 + 21 * ue, 0.55, sr);
        float ae = outdoorEar ? (float) (Dsp.amp(12 + 50 * Math.log10(Math.max(0.3, ue))) * 1.4) : 0;
        for (int i = 0; i < n; i++) {
            float l = windBp[0].process(windHp[0].process(windL.next())) * aw;
            float r = windBp[1].process(windHp[1].process(windR.next())) * aw;
            float er = earLp.process(earNoise.next()) * ae;
            oL[i] += l + er;
            oR[i] += r + er * 0.8f;
            e[0] += (l + er) * (l + er);
        }

        // ---------------- Äolstöne
        List<Aeolian> bl = bodies;
        int nb = Math.min(q.bodies, bl.size());
        if (bodyBp.length != nb) {
            bodyBp = new Dsp.Biquad[nb];
            bodyPh = new double[nb];
            bodyF = new double[nb];
            bodyRng = new Dsp.Rng[nb];
            for (int k = 0; k < nb; k++) { bodyBp[k] = new Dsp.Biquad(); bodyRng[k] = new Dsp.Rng(seed * 31 + k); }
        }
        StringBuilder bt = new StringBuilder();
        for (int k = 0; k < nb; k++) {
            Aeolian b = bl.get(k);
            double u = windAt(b.y() - waterLevel) * (1 + gust + 0.05 * bodyRng[k].gauss());
            double f = b.frequency(Math.max(0, u));
            bodyF[k] += (f - bodyF[k]) * Math.min(1, dt * 6);
            double fk = bodyF[k];
            if (u < 4 || fk < 60 || fk > sr * 0.42) continue;
            double d = Math.max(1, Math.sqrt(sq(b.x() - ex) + sq(b.y() - ey) + sq(b.z() - ez)));
            double lv = 60 + 50 * Math.log10(u / 15) + 5 * Math.log10(b.length() * b.diameter() / 0.025) - 20 * Math.log10(Math.max(1, d / 3));
            double fade = smooth(4, 8, u);
            float a = (float) (Dsp.amp(lv) * fade);
            if (a < 1e-5) continue;
            double qf = 22;
            bodyBp[k].bandpass(fk, qf, sr);
            float norm = (float) (1 / (0.577 * Math.sqrt(Math.PI * fk / (2 * qf) / (sr / 2.0))));
            float[] g = pan(b.x(), b.z(), ex, ez, yaw);
            Dsp.Rng br = bodyRng[k];
            for (int i = 0; i < n; i++) {
                bodyPh[k] += fk / sr;
                if (bodyPh[k] > 1) bodyPh[k] -= 1;
                float s = (bodyBp[k].process(br.white()) * norm * 0.8f + (float) Math.sin(Dsp.TAU * bodyPh[k]) * 0.6f) * a;
                oL[i] += s * g[0];
                oR[i] += s * g[1];
                e[1] += s * s;
            }
            if (bt.length() < 80) bt.append(bt.length() > 0 ? ", " : "").append(String.format(Locale.GERMAN, "%s %.0f Hz", b.name(), fk));
        }
        bodyText = bt.toString();

        // ---------------- See: Rauschen der Wellen und Schaumkronen, atmet mit der Periode
        double hs = Math.max(0, waveHeight), tp = Math.max(2, wavePeriod);
        double cap = smooth(4, 14, windSpeed);
        // höher über dem Wasser leiser (das Rauschen kommt von der Fläche unter dem Ohr)
        double ls = 20 + 12 * Math.log(1 + 4 * hs) / Math.log(2) + 6 * cap - 10 * Math.log10(1 + Math.max(0, ey - waterLevel) / 10);
        seaPhase += dt / tp;
        seaGroup += (-(seaGroup - 1) * dt / (4 * tp)) + 0.35 * Math.sqrt(2 * dt / (4 * tp)) * rng.gauss();
        double breathe = Math.max(0.2, (0.7 + 0.3 * Math.sin(Dsp.TAU * seaPhase)) * seaGroup);
        float asea = (float) (Dsp.amp(ls) * breathe * 1.3);
        seaLp[0].lowpass(700 + 260 * Math.min(6, hs) + 900 * cap, 0.5, sr);
        seaLp[1].lowpass(720 + 250 * Math.min(6, hs) + 900 * cap, 0.5, sr);
        for (int i = 0; i < n; i++) {
            float low = seaLow.next() * 0.5f;
            float l = (seaLp[0].process(seaL.next()) + low) * asea;
            float r = (seaLp[1].process(seaR.next()) + low) * asea;
            oL[i] += l;
            oR[i] += r;
            e[2] += l * l;
        }

        // ---------------- Brandung an einem Bauwerk: jede Welle klatscht und läuft ab
        if (surfRadius > 0 && surfStrength > 0) {
            double dx = surfX - ex, dz = surfZ - ez, hd = Math.max(0, Math.hypot(dx, dz) - surfRadius);
            double d = Math.max(1, Math.hypot(hd, ey - waterLevel));
            double lb = Math.min(96, 66 + 5 * hs) + 10 * Math.log10(Math.max(0.01, surfStrength)) - 20 * Math.log10(Math.max(1, d / 6));
            surfT += dt;
            if (surfT >= surfNext) {
                surfT = 0;
                surfNext = tp * (0.75 + 0.5 * rng.uniform());
                surfAmp = (0.45 + 0.8 * rng.uniform()) * Math.min(1.6, seaGroup);
                surfLen = tp * (0.3 + 0.25 * rng.uniform());
            }
            float[] g = pan(surfX, surfZ, ex, ez, yaw);
            double a0 = Dsp.amp(lb) * 2.2;
            double cut = Math.max(300, 1600 / (1 + d / 200));
            surfLp.lowpass(cut, 0.6, sr);
            for (int i = 0; i < n; i++) {
                double t = surfT + i / (double) sr;
                double env = surfAmp * (1 - Math.exp(-t / 0.08)) * Math.exp(-t / surfLen) + 0.18;
                float s = (float) (surfLp.process(seaL.next() * 0.7f + seaLow.next() * 0.6f) * a0 * env);
                oL[i] += s * g[0];
                oR[i] += s * g[1];
                e[3] += s * s;
            }
        }

        // ---------------- Regen auf dem Wasser: Zischen und einzelne Tropfen
        double rr = Math.max(0, rainRate);
        if (rr > 0.01) {
            double lr = 42 + 10 * Math.log10(rr);
            float ar = (float) (Dsp.amp(lr) * 1.8);
            for (int i = 0; i < n; i++) {
                float l = rainPeak[0].process(rainHp[0].process(rng.white())) * ar;
                float r = rainPeak[1].process(rainHp[1].process(rng.white())) * ar;
                oL[i] += l;
                oR[i] += r;
                e[4] += l * l;
            }
            drops.spawn(Math.min(q.drops, 45 * rr), dt, 1300, 5200, 0.002, 0.007, Dsp.amp(lr - 6), 0.9, rng, sr);
        }
        e[4] += drops.render(oL, oR, n, sr);

        // ---------------- Regen auf Glas und Dach (drinnen, ohne die Wand)
        ticks.spawn(Math.min(q.drops, Math.max(0, glassDrops)), dt, 2400, 5200, 0.0015, 0.004, Dsp.amp(62), 0.5, rng, sr);
        roof.spawn(Math.min(q.drops, Math.max(0, roofDrops)), dt, 480, 1500, 0.015, 0.04, Dsp.amp(55), 0.3, rng, sr);
        e[5] += ticks.render(iL, iR, n, sr) + roof.render(iL, iR, n, sr);

        // ---------------- Maschinen
        for (Hum h : hums) {
            float[] tab = humTable;
            double f = h.firingHz;
            float a;
            float[] g;
            boolean board = h.aboard;
            if (board) {
                a = (float) Dsp.amp(h.aboardDb);
                g = new float[]{0.7071f, 0.7071f};
            } else {
                double d = Math.max(1, Math.sqrt(sq(h.x - ex) + sq(h.y - ey) + sq(h.z - ez)));
                a = (float) (Dsp.amp(h.levelDb - 20 * Math.log10(d) - 0.002 * d));
                g = pan(h.x, h.z, ex, ez, yaw);
            }
            if (a < 1e-6) continue;
            float[] L = board ? iL : oL, R = board ? iR : oR;
            for (int i = 0; i < n; i++) {
                humPh += f / sr;
                humPh -= Math.floor(humPh);
                float s = (tab[(int) (humPh * tab.length) & 2047] * 0.8f + humLp.process(humNoise.next()) * 0.6f) * a;
                L[i] += s * g[0];
                R[i] += s * g[1];
                e[6] += s * s;
            }
        }

        // ---------------- Ereignisse
        for (Voice v; (v = pending.poll()) != null; ) {
            double d = Math.sqrt(sq(v.x - ex) + sq(v.y - ey) + sq(v.z - ez));
            if (!v.atEar) v.distance = d;
            // wer die Laufzeit schon abgewartet hat (Donner), beginnt jetzt; sonst nach Abstand / c
            long delay = (long) ((v.atEar ? v.delaySeconds : d / c) * sr);
            // was am Ohr unter 30 dB bliebe, kostet nur Rechenzeit (Brecher am Turm, wenn das Ohr 4 km weit auf See ist)
            if (!v.atEar && v.level1m - 20 * Math.log10(Math.max(1, d)) < 30) continue;
            v.start = frame + delay;
            if (voices.size() >= q.voices) voices.remove(0);
            voices.add(v);
        }
        for (int vi = 0; vi < voices.size(); vi++) {
            Voice v = voices.get(vi);
            if (v.start >= frame + n) continue;
            int off = (int) Math.max(0, v.start - frame);
            java.util.Arrays.fill(tmp, 0, n, 0);
            boolean alive = v.render(tmp, off, n, sr);
            if (off == 0 || v.age > 0) { loudestVoice = v.name; lastVoiceAt = time(); }
            double d = Math.max(1, v.atEar ? 1 : v.distance);
            double gain = Dsp.amp(v.level1m - (v.atEar ? 0 : 20 * Math.log10(d)));
            float[] g = pan(v.x, v.z, ex, ez, yaw);
            double air = v.atEar ? sr * 0.45 : Math.max(250, 18_000 / (1 + d / 350));
            if (!v.atEar && g[2] < 0) air *= 0.6;
            v.air.lowpass(air, 0.6, sr);
            int itd = (int) Math.round(Math.abs(g[3]) * 0.00066 * sr);
            boolean rightFar = g[3] < 0;
            float tl = (float) (gain * g[0]), tr = (float) (gain * g[1]);
            if (v.gl < 0) { v.gl = tl; v.gr = tr; }
            for (int i = 0; i < n; i++) hold[i] = v.air.process(tmp[i]);
            for (int i = 0; i < n; i++) {
                v.gl += (tl - v.gl) * 0.01f;
                v.gr += (tr - v.gr) * 0.01f;
                int j = i - itd;
                float near = hold[i], far = j >= 0 ? hold[j] : v.hist[64 + j];
                oL[i] += (rightFar ? near : far) * v.gl;
                oR[i] += (rightFar ? far : near) * v.gr;
                e[7] += near * near * 0.5 * (v.gl * v.gl + v.gr * v.gr);
            }
            // Verlauf für den Laufzeitunterschied der Ohren ins nächste Stück mitnehmen
            if (n >= 64) System.arraycopy(hold, n - 64, v.hist, 0, 64);
            else {
                System.arraycopy(v.hist, n, v.hist, 0, 64 - n);
                System.arraycopy(hold, 0, v.hist, 64 - n, n);
            }
            if (!alive) { voices.remove(vi); vi--; }
        }
        activeVoices = voices.size();

        // ---------------- Wand, Raum, Lautstärke, Begrenzer
        Room rm = room;
        if (ins > 0.001) {
            double fc = Math.exp(Math.log(sr * 0.45) + (Math.log(Math.max(60, rm.wallHz())) - Math.log(sr * 0.45)) * ins);
            float wg = (float) (1 + (rm.wallGain() - 1) * ins);
            wallLp[0].lowpass(fc, 0.6, sr); wallLp[1].lowpass(fc, 0.6, sr);
            wallLp[2].lowpass(fc * 1.3, 0.6, sr); wallLp[3].lowpass(fc * 1.3, 0.6, sr);
            for (int i = 0; i < n; i++) {
                oL[i] = wallLp[2].process(wallLp[0].process(oL[i])) * wg;
                oR[i] = wallLp[3].process(wallLp[1].process(oR[i])) * wg;
            }
        }
        double mix = (rm.mix() * ins + Room.OUTDOORS.mix() * (1 - ins));
        if (q.reverb && mix > 0.005) {
            reverb.set(rm.t60() * ins + Room.OUTDOORS.t60() * (1 - ins), 0.35);
            for (int i = 0; i < n; i++) {
                float x = 0.5f * (oL[i] + oR[i]) + 0.5f * (iL[i] + iR[i]);
                reverb.process(x, oL, oR, i, (float) mix);
            }
        }
        double et = 0;
        for (int i = 0; i < n; i++) {
            oL[i] += iL[i];
            oR[i] += iR[i];
            et += 0.5 * (oL[i] * oL[i] + oR[i] * oR[i]);
        }
        e[8] = et;
        // Anpassung des Ohrs: Energie über 4 s, Verstärkung folgt in 3 s
        double eb = et / n;
        agcEnergy = agcEnergy < 0 ? eb : agcEnergy + (eb - agcEnergy) * (1 - Math.exp(-dt / 4));
        double want = autoGain ? Math.max(Math.pow(10, -12 / 20.0), Math.min(Math.pow(10, 18 / 20.0), Dsp.amp(AUTO_TARGET_DB) / Math.sqrt(Math.max(1e-14, agcEnergy)))) : 1;
        double g0 = agcGain;
        agcGain += (want - agcGain) * (1 - Math.exp(-dt / 3));
        float vol = (float) Math.max(0, Math.min(2, volume));
        for (int i = 0; i < n; i++) {
            float gi = (float) ((g0 + (agcGain - g0) * i / n) * vol);
            oL[i] *= gi;
            oR[i] *= gi;
        }
        limiter.process(oL, oR, n);
        // Pegel über eine halbe Sekunde glätten
        double k = 1 - Math.exp(-dt / 0.5);
        double[] lv = new double[PARTS.length];
        for (int p = 0; p < PARTS.length; p++) {
            energy[p] += (e[p] / n - energy[p]) * k;
            lv[p] = Dsp.db(Math.sqrt(energy[p]));
        }
        levels = lv;
        reductionDb = Math.min(reductionDb, limiter.takeReductionDb());
        frame += n;
    }

    private final float[] hold = new float[BLOCK];

    /** Verstärkung links, rechts, vorn (+1) oder hinten (−1), Seite (−1 links … +1 rechts) für eine Quelle. */
    static float[] pan(double x, double z, double ex, double ez, double yaw) {
        double dx = x - ex, dz = z - ez, l = Math.hypot(dx, dz);
        if (l < 1e-6) return new float[]{0.7071f, 0.7071f, 1, 0};
        double fx = Math.sin(yaw), fz = -Math.cos(yaw), rx = Math.cos(yaw), rz = Math.sin(yaw);
        double side = (dx * rx + dz * rz) / l, front = (dx * fx + dz * fz) / l;
        // nicht ganz hart: auch eine Quelle genau seitlich hört das abgewandte Ohr (um den Kopf herum), gut 12 dB leiser
        double a = (0.7 * side + 1) * Math.PI / 4;
        return new float[]{(float) Math.cos(a), (float) Math.sin(a), (float) Math.signum(front), (float) side};
    }

    static double sq(double v) { return v * v; }

    static double smooth(double a, double b, double x) {
        double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    /** „Wind 72 dB, Äolstöne 64 dB (Handlauf 310 Hz) · See 58 dB · … · 3 Stimmen · Begrenzer −2,1 dB“ */
    public String describe() {
        double[] l = levels;
        StringBuilder b = new StringBuilder();
        for (int p = 0; p < PARTS.length - 1; p++) {
            if (l[p] < 20) continue;
            b.append(b.length() > 0 ? " · " : "").append(PARTS[p]).append(String.format(Locale.GERMAN, " %.0f dB", l[p]));
            if (p == 1 && !bodyText.isEmpty()) b.append(" (").append(bodyText).append(')');
        }
        b.append(b.length() > 0 ? " · " : "").append(String.format(Locale.GERMAN, "am Ohr %.0f dB", l[PARTS.length - 1]));
        if (autoGain) b.append(String.format(Locale.GERMAN, " · Ohr %+.0f dB", autoGainDb()));
        if (activeVoices > 0) b.append(String.format(Locale.GERMAN, " · %d %s", activeVoices, activeVoices == 1 ? "Stimme" : "Stimmen"));
        double rd = reductionDb;
        reductionDb = 0;
        if (rd < -0.5) b.append(String.format(Locale.GERMAN, " · Begrenzer %.1f dB", rd));
        return b.toString();
    }

    // ------------------------------------------------------------------ Tropfen, Ticks

    /** Viele kurze, abklingende Töne (Tropfen auf Wasser, Glas, Blech) als rekursive Schwinger. */
    static final class Pings {
        private static final int MAX = 96;
        private final double[] y1 = new double[MAX], y2 = new double[MAX], c1 = new double[MAX], c2 = new double[MAX];
        private final float[] gl = new float[MAX], gr = new float[MAX];
        private final int[] left = new int[MAX];
        private int count;
        private double wait;
        long spawned;

        void clear() { count = 0; wait = 0; }

        void spawn(double rate, double dt, double f0, double f1, double d0, double d1, double amp, double width, Dsp.Rng r, int sr) {
            if (rate <= 0) return;
            wait -= dt;
            while (wait <= 0 && count < MAX) {
                double f = f0 * Math.pow(f1 / f0, r.uniform()), decay = d0 + (d1 - d0) * r.uniform();
                double rad = Math.exp(-1 / (decay * sr)), w = Dsp.TAU * f / sr;
                c1[count] = 2 * rad * Math.cos(w);
                c2[count] = -rad * rad;
                double a = amp * (0.3 + 0.9 * r.uniform());
                y1[count] = a * Math.sin(w);
                y2[count] = 0;
                double p = (r.uniform() * 2 - 1) * width, ang = (p + 1) * Math.PI / 4;
                gl[count] = (float) Math.cos(ang);
                gr[count] = (float) Math.sin(ang);
                left[count] = (int) (decay * sr * 7);
                count++;
                spawned++;
                wait += r.exp(rate);
            }
            if (wait < -1) wait = 0;
        }

        /** Addiert in l, r; gibt die Energie zurück. */
        double render(float[] l, float[] rr, int n, int sr) {
            double e = 0;
            for (int k = 0; k < count; k++) {
                double a = y1[k], b = y2[k], ca = c1[k], cb = c2[k];
                float L = gl[k], R = gr[k];
                int m = Math.min(n, left[k]);
                for (int i = 0; i < m; i++) {
                    double y = ca * a + cb * b;
                    b = a;
                    a = y;
                    l[i] += (float) y * L;
                    rr[i] += (float) y * R;
                    e += y * y;
                }
                y1[k] = a;
                y2[k] = b;
                left[k] -= n;
            }
            int j = 0;
            for (int k = 0; k < count; k++) {
                if (left[k] <= 0) continue;
                y1[j] = y1[k]; y2[j] = y2[k]; c1[j] = c1[k]; c2[j] = c2[k]; gl[j] = gl[k]; gr[j] = gr[k]; left[j] = left[k];
                j++;
            }
            count = j;
            return e;
        }
    }
}
