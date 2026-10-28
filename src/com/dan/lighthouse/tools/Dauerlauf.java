package com.dan.lighthouse.tools;

import com.dan.lighthouse.db.Daten;
import com.dan.lighthouse.db.DateiSpeicher;
import com.dan.lighthouse.db.Felder;
import com.dan.lighthouse.db.Startdaten;
import com.dan.lighthouse.db.TurmWahl;
import com.dan.lighthouse.db.Zustand;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.wiring.Wiring;
import com.dan.weather.Scenario;
import com.dan.weather.Weather;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLRunnable;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Phase 16: der Dauerlauf. Die ganze Szene läuft ohne Fenster so lange wie angegeben (Vorgabe 30 Minuten) im
 * Zeitraffer 600 : 1 durch Tage und Nächte, mit der freien Regie und den Regiebüchern, allen acht Ansichten, Blitzen,
 * Brechern (seit Phase 17 mit der Brandung am Sockel aus {@code surf}, abwechselnd mit dem Ring aus {@code ocean}), Nebelhorn, allen neun Türmen, allen vier Qualitätsstufen, der Regelung der Auflösung, dem Klang (offline
 * gerechnet) und jede Minute einem Zustand hin und zurück. Gemessen werden Bilder je Sekunde, das schlechteste Bild,
 * der Speicher nach dem Aufräumen und jede Ausnahme in irgendeinem Faden.
 * <p>
 * Bestanden heißt: keine Ausnahme, kein Grafikfehler, der Speicher nach der fünften Minute nicht mehr als 25 %
 * gewachsen, kein Bild über 2 s (oder das 20-fache des Mittels, auf Mesa; die zwei Sekunden nach einem Stufenwechsel zählen
 * getrennt und dürfen bis 5 s brauchen), und am Ende sind Zustand und Felder noch lesbar. Das Protokoll steht in
 * {@code docs/status/Dauerlauf_claude.txt} (oder dem angegebenen Pfad); die letzte Zeile ist „Dauerlauf bestanden“
 * oder „Dauerlauf NICHT bestanden“ mit dem Grund.
 * <pre>
 * Dauerlauf [Minuten] [Breite] [Datei]
 * </pre>
 */
public final class Dauerlauf {
    private Dauerlauf() { }

    static final List<String> FEHLER = new ArrayList<>();
    /** Beginn des laufenden Bildes (für den Wächter, der bei langen Bildern den Stapel ins Protokoll schreibt). */
    static volatile long frameStart;
    static final String[] EREIGNIS = {"Ansicht", "Regiebuch", "Blitz", "Ansicht", "Brecher, Brandung am Sockel an/aus", "Turmwechsel", "Ansicht", "Stufenwechsel", "freies Wetter", "Ansicht", "Wind von Hand", "Kreisen"};

    public static void main(String[] a) throws Exception {
        double minuten = a.length > 0 ? Double.parseDouble(a[0].replace(',', '.')) : 30;
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1280;
        File out = new File(a.length > 2 ? a[2] : "docs/status/Dauerlauf_claude.txt");
        int height = width * 9 / 16;
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> { synchronized (FEHLER) { FEHLER.add("Faden " + t.getName() + ": " + e); } e.printStackTrace(); });
        StringBuilder log = new StringBuilder();
        PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out.toPath(), StandardCharsets.UTF_8));
        java.util.function.Consumer<String> say = s -> { System.out.println(s); pw.println(s); pw.flush(); log.append(s).append('\n'); };
        say.accept(String.format(Locale.GERMAN, "Lighthouse · Dauerlauf · %s · %.0f Minuten · %d×%d · %s", Renderer.VERSION, minuten, width, height,
                java.time.LocalDateTime.now().withNano(0)));
        say.accept("Rechner: " + System.getProperty("os.name") + " · Java " + System.getProperty("java.version") + " · " + Runtime.getRuntime().availableProcessors() + " Kerne · Xmx "
                + Runtime.getRuntime().maxMemory() / 1048576 + " MB");

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, width, height);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock(LocalDate.of(2026, 10, 16), 6.0);
        clock.lapse = 600;
        Wiring wiring = new Wiring(clock);
        Renderer r = new Renderer(clock, wiring);
        r.showHud = true;
        r.autoScale = true;
        r.targetFps = 60;
        r.setQuality(Quality.MITTEL);
        wiring.regie = true;
        wiring.weather.startFree(clock.epochMillis() / 1000.0, Weather.Phase.KLAR);
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { say.accept("FEHLER Grafik: " + r.error()); say.accept("Dauerlauf NICHT bestanden"); pw.close(); System.exit(2); }
        say.accept("Grafik: " + r.glInfo());
        r.awaitInterior();
        // der Klang wird offline gerechnet, 1/30 s je Bild (wie AudioOut, nur ohne Lautsprecher)
        float[] ton = new float[2 * 2048];
        List<Daten.Turm> tuerme = Startdaten.tuerme();
        String[] views = {"Aussen1", "Drohne", "Brecher", "Sturmnacht", "Aussen3", "Laternenraum", "Regenbogen", "Möwenflug", "Schiffsbrücke"};
        Quality[] qs = Quality.values();
        Random rnd = new Random(1885);
        File daten = Files.createTempDirectory("lighthouse-dauerlauf").toFile();
        DateiSpeicher ds = new DateiSpeicher(daten);
        long sid = ds.sitzungBeginnen(Renderer.VERSION, r.glInfo(), r.quality().name());

        final Thread gl = Thread.currentThread();
        Thread waechter = new Thread(() -> {
            long gemeldet = 0;
            while (true) {
                try { Thread.sleep(200); } catch (InterruptedException e) { return; }
                long fs = frameStart;
                if (fs != 0 && fs != gemeldet && System.nanoTime() - fs > 800_000_000L) {
                    gemeldet = fs;
                    StringBuilder b = new StringBuilder("  Bild hängt seit 0,8 s in:");
                    StackTraceElement[] tr = gl.getStackTrace();
                    for (int i = 0; i < Math.min(8, tr.length); i++) b.append("\n    ").append(tr[i]);
                    say.accept(b.toString());
                }
            }
        }, "Wächter");
        waechter.setDaemon(true);
        waechter.start();
        // Avatar-Phase 19: höchstens zwei Bilder in Arbeit, wie im Fenster der Tausch der Puffer. Ohne diesen Takt lief der
        // Prozessor der Grafikkarte weit voraus, und der erste Abgleich (glReadPixels) wartete Sekunden auf sie.
        final long[] fences = new long[2];
        final int[] fenceI = {0};
        GLRunnable takt = drawable -> {
            GL4 g = drawable.getGL().getGL4();
            int k = fenceI[0]++ % fences.length;
            if (fences[k] != 0) { g.glClientWaitSync(fences[k], GL4.GL_SYNC_FLUSH_COMMANDS_BIT, 5_000_000_000L); g.glDeleteSync(fences[k]); }
            fences[k] = g.glFenceSync(GL4.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
            return true;
        };
        long start = System.nanoTime(), ende = start + (long) (minuten * 60e9), lastMinute = start, lastShot = start;
        long frames = 0;
        String lastEvent = "Start";
        long lastEventAt = start;
        double worst = 0, worstAll = 0, worstSwitch = 0;
        long switchUntil = 0;
        long heap5 = -1, heapEnd = -1;
        int minute = 0, scenes = 0, bolts = 0, zustaende = 0, turmWechsel = 0, stufen = 0, brandungWechsel = 0;
        double sumFps = 0;
        int fpsN = 0;
        long lastNanos = System.nanoTime();
        while (System.nanoTime() < ende) {
            long t0 = System.nanoTime();
            frameStart = t0;
            d.display();
            d.invoke(true, takt);
            frameStart = 0;
            long t1 = System.nanoTime();
            double ms = (t1 - t0) / 1e6;
            frames++;
            // die zwei Sekunden nach einem Stufenwechsel zählen getrennt (Texturen und FFT werden neu angelegt); maßgeblich ist der
            // Beginn des Bildes (bis Avatar-Phase 19 das Ende: ein Wechselbild über 2 s zählte dann als gewöhnliches Bild)
            if (frames > 30 && t0 < switchUntil) worstSwitch = Math.max(worstSwitch, ms);
            else if (frames > 30) { worst = Math.max(worst, ms); worstAll = Math.max(worstAll, ms); }
            if (frames > 30 && ms > 1000) say.accept(String.format(Locale.GERMAN, "  langes Bild %.0f ms, %.1f s nach „%s“", ms, (t0 - lastEventAt) / 1e9, lastEvent));
            // Klang wie die Ausgabe
            double dt = (t1 - lastNanos) / 1e9;
            lastNanos = t1;
            int n = Math.min(2048, (int) Math.round(dt * r.soundscape.sampleRate()));
            if (n > 0) r.soundscape.render(ton, n);
            if (r.error() != null) { synchronized (FEHLER) { FEHLER.add("Grafik: " + r.error()); } break; }
            // alle 12 s etwas tun
            if (t1 - lastShot > 12_000_000_000L) {
                lastShot = t1;
                int k = scenes++ % 12;
                lastEvent = EREIGNIS[k];
                lastEventAt = t1;
                switch (k) {
                    case 0, 3, 6, 9 -> r.applyView(views[rnd.nextInt(views.length)]);
                    case 1 -> { Scenario sc = rnd.nextBoolean() ? Scenario.stormDay() : Scenario.fogDay(); wiring.weather.start(sc, clock.epochMillis() / 1000.0); }
                    case 2 -> { r.triggerLightning(com.dan.lightning.Bolt.Kind.CLOUD_GROUND, rnd.nextInt(3) == 0); bolts++; }
                    case 4 -> {
                        r.breaker();
                        r.soundscape.horn(com.dan.soundscape.Soundscape.Kind.NEBELHORN, 0, 26, 0, 1, 3);
                        // Phase 17: die Brandung am Sockel jedes Mal umschalten (Brecher aus surf und Ring aus ocean wechseln sich ab)
                        r.brandung.an = !r.brandung.an;
                        brandungWechsel++;
                    }
                    case 5 -> { Daten.Turm t = tuerme.get(rnd.nextInt(tuerme.size())); TurmWahl.waehlen(r, t, tuerme); turmWechsel++; }
                    case 7 -> { r.setQuality(qs[rnd.nextInt(qs.length)]); stufen++; switchUntil = System.nanoTime() + 2_000_000_000L; }
                    case 8 -> { wiring.resumeRegie(); wiring.weather.startFree(clock.epochMillis() / 1000.0, Weather.Phase.values()[rnd.nextInt(8)]); }
                    case 10 -> { wiring.hand(Weather.Var.WIND, 2 + rnd.nextInt(30)); wiring.hand(Weather.Var.RAIN, rnd.nextInt(3) == 0 ? 20 : 0); }
                    case 11 -> { r.camera.autoOrbit = true; r.camera.orbitSpeed = 12; }
                    default -> { }
                }
            }
            // jede Minute: Zustand hin und zurück, Speicher, Zeile
            if (t1 - lastMinute > 60_000_000_000L) {
                lastMinute = t1;
                minute++;
                java.util.LinkedHashMap<String, String> z = Zustand.erfassen(r, false);
                String text = Felder.alsText(z, "Dauerlauf");
                List<String> f = Zustand.anwenden(r, Felder.ausText(text));
                if (!f.isEmpty()) synchronized (FEHLER) { FEHLER.add("Minute " + minute + ": Zustand nicht ganz hergestellt: " + f); }
                ds.sitzungSichern(sid, text, (long) clock.epochMillis(), r.siteName, Zustand.regiebuch(z), Zustand.regieStunde(z));
                zustaende++;
                System.gc();
                Thread.sleep(50);
                long heap = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
                if (minute == 5) heap5 = heap;
                heapEnd = heap;
                double fps = r.fps();
                sumFps += fps;
                fpsN++;
                say.accept(String.format(Locale.GERMAN, "Minute %2d · %s · %.0f Bilder/s · Grafikkarte %.1f ms · Prozessor %.1f ms · schlechtestes Bild %.0f ms · Auflösung %.0f %% (%s) · Speicher %d MB · %s · %s · Klang %.1f %% · %d Werte",
                        minute, clock.label(), fps, r.gpuMs(), r.cpuMs(), worst, r.renderScale * 100, r.quality().label, heap, r.siteName,
                        r.weatherLine().replace("Wetter ", "").replaceAll(" · nächste.*", ""), r.soundscape.msPerSecond() / 10, z.size()));
                worst = 0;
            }
        }
        ds.sitzungBeenden(sid);
        // Abschluss: Zustand und Felder noch lesbar
        boolean lesbar = true;
        try {
            java.util.LinkedHashMap<String, String> z = Zustand.erfassen(r, false);
            lesbar = z.size() > 100 && Zustand.anwenden(r, z).isEmpty();
        } catch (RuntimeException e) {
            lesbar = false;
            FEHLER.add("Zustand am Ende: " + e);
        }
        double sek = (System.nanoTime() - start) / 1e9;
        say.accept(String.format(Locale.GERMAN, "Ende nach %.1f Minuten: %d Bilder, im Mittel %.1f Bilder/s, schlechtestes Bild %.0f ms, %d Ansichten, %d Regiebücher, %d Blitze, %d Turmwechsel, %d Stufenwechsel, "
                + "%d× Brandung am Sockel umgeschaltet, %d× Zustand hin und zurück, Auflösung %d× geregelt, Speicher Minute 5: %d MB, Ende: %d MB, größter Ruck beim Stufenwechsel %.0f ms",
                sek / 60, frames, fpsN > 0 ? sumFps / fpsN : frames / sek, worstAll, scenes, scenes / 12 + 1, bolts, turmWechsel, stufen, brandungWechsel, zustaende, r.scaleSteps(), heap5, heapEnd, worstSwitch));
        List<String> gruende = new ArrayList<>();
        synchronized (FEHLER) { gruende.addAll(FEHLER); }
        if (heap5 > 0 && heapEnd > heap5 * 1.25 + 64) gruende.add(String.format(Locale.GERMAN, "Speicher wächst: %d MB nach Minute 5, %d MB am Ende", heap5, heapEnd));
        double mean = sek / Math.max(1, frames) * 1000;
        if (worstAll > Math.max(2000, 20 * mean)) gruende.add(String.format(Locale.GERMAN, "ein Bild brauchte %.0f ms (Mittel %.0f ms)", worstAll, mean));
        if (worstSwitch > 5000) gruende.add(String.format(Locale.GERMAN, "ein Stufenwechsel brauchte %.0f ms", worstSwitch));
        if (!lesbar) gruende.add("Zustand am Ende nicht mehr herstellbar");
        if (minute < 1) gruende.add("keine volle Minute gelaufen");
        for (String g : gruende) say.accept("FEHLER " + g);
        say.accept(gruende.isEmpty() ? "Dauerlauf bestanden" : "Dauerlauf NICHT bestanden (" + gruende.size() + " Gründe)");
        pw.close();
        System.exit(gruende.isEmpty() ? 0 : 1);
    }
}
