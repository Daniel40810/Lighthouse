package com.dan.lighthouse.db;

import com.dan.lighthouse.camera.Camera;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.weather.Scenario;
import com.dan.weather.Weather;
import com.dan.wetglass.Wetglass;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Der ganze Zustand der Szene als Text (F39 A): alle öffentlichen Felder aller Packages, dazu Uhr, Kamera, Regie
 * mit Regiebuch und Stelle, gewählter Turm, Qualität und Ton. Dasselbe Format dient für „Weiter wie zuletzt“, für
 * gemerkte Ansichten und für die Dateien ohne Datenbank.
 * <p>
 * Die Schlüssel haben die Form {@code teil.feld}: {@code ocean.windSpeed}, {@code kamera.x}, {@code regie.hand.WIND}.
 */
public final class Zustand {
    private Zustand() { }

    /** Felder, die nicht zum Zustand gehören: Anfragen, Tastenzustände, abgeleitete Größen. */
    static final Set<String> OHNE_RENDERER = Set.of("photoRequest", "zoom");
    /** Was die Verdrahtung je Bild aus der Regie ableitet, gehört nicht dazu (es kommt aus Regie und Handreglern wieder). */
    static final Set<String> OHNE_WIRING = Set.of("lastHorn", "lastHornT", "cloudsDrawn", "showerEnd", "gustNow", "pressureHPa", "pressureTendency",
            "soundLive", "regie", "swellHeight", "swellPeriod", "swellFromDeg", "visibilityKm", "rainRate", "oktas", "highCover", "cloudType",
            "thunderstorm", "airTempC", "fogAmount");
    static final Set<String> OHNE_SKY = Set.of("epochMillis", "latDeg", "lonDeg");
    static final Set<String> OHNE = Set.of("quality");

    /** Die Teile mit öffentlichen Feldern: Name im Zustand → Objekt. */
    static Map<String, Object> teile(Renderer r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("renderer", r);
        if (r.wiring != null) {
            m.put("wiring", r.wiring);
            m.put("tide", r.wiring.tide);
            m.put("weather", r.wiring.weather);
        }
        m.put("ocean", r.ocean);
        m.put("sky", r.sky);
        m.put("clouds", r.clouds);
        m.put("weathering", r.weathering);
        m.put("spray", r.spray);
        m.put("rain", r.rain);
        m.put("glass", r.glass[0]);
        m.put("beacon", r.beacon);
        m.put("lightning", r.lightning);
        m.put("fog", r.fog);
        m.put("buoyancy", r.buoyancy);
        m.put("flock", r.flock);
        m.put("soundscape", r.soundscape);
        return m;
    }

    private static Set<String> ohne(String teil) {
        return switch (teil) {
            case "renderer" -> OHNE_RENDERER;
            case "wiring" -> OHNE_WIRING;
            case "sky" -> OHNE_SKY;
            default -> OHNE;
        };
    }

    /** Erfasst den Zustand (aus dem Swing-Faden; die Felder sind volatile). */
    public static LinkedHashMap<String, String> erfassen(Renderer r, boolean tonAn) {
        LinkedHashMap<String, String> z = new LinkedHashMap<>();
        z.put("app.version", Renderer.VERSION);
        z.put("app.turm", r.siteName);
        z.put("app.qualitaet", r.quality().name());
        z.put("app.ton", Boolean.toString(tonAn));
        z.put("uhr.zeit", Long.toString((long) r.clock.epochMillis()));
        z.put("uhr.lapse", Zahl.text(r.clock.lapse));
        Camera c = r.camera;
        z.put("kamera.modus", c.mode == Camera.Mode.KREISEN ? "KREISEN" : "FREI");
        z.put("kamera.kreisen", Boolean.toString(c.autoOrbit));
        double[] k = kamera(r);
        String[] n = {"x", "y", "z", "gier", "nick", "fov"};
        for (int i = 0; i < 6; i++) z.put("kamera." + n[i], Zahl.text(Math.round(k[i] * 1000) / 1000.0));
        z.put("kamera.ziel", Zahl.text(c.tx) + " " + Zahl.text(c.ty) + " " + Zahl.text(c.tz) + " " + Zahl.text(c.yaw) + " " + Zahl.text(c.pitch) + " " + Zahl.text(c.dist));
        z.put("kamera.zoom", Zahl.text(r.zoom));
        if (r.wiring != null) {
            Weather w = r.wiring.weather;
            Weather.Snapshot ws = w.snapshot();
            z.put("regie.an", Boolean.toString(r.wiring.regie));
            z.put("regie.lage", ws != null ? ws.phase().name() : "KLAR");
            Scenario sc = w.scenario();
            if (sc != null && ws != null && ws.step() >= 0) {
                z.put("regie.text", sc.toText());
                z.put("regie.beginn", Zahl.text(w.scenarioStart()));
            }
            double[] h = w.handValues();
            for (int i = 0; i < h.length; i++) if (!Double.isNaN(h[i])) z.put("regie.hand." + Weather.Var.values()[i].name(), Zahl.text(h[i]));
        }
        z.put("beacon.kennung", r.beacon.characteristic().text);
        for (Map.Entry<String, Object> e : teile(r).entrySet())
            for (Map.Entry<String, String> f : Felder.lesen(e.getValue(), ohne(e.getKey())).entrySet()) z.put(e.getKey() + "." + f.getKey(), f.getValue());
        return z;
    }

    /** Kamera für Ansichten: {x, y, z, Gier, Nick, Bildwinkel} aus dem laufenden Bild. */
    public static double[] kamera(Renderer r) {
        Camera c = r.camera;
        double[] e = c.eye, f = c.fwd;
        double nick = Math.toDegrees(-Math.asin(Math.max(-1, Math.min(1, f[1]))));
        return new double[]{e[0], e[1], e[2], c.heading(), nick, Camera.FOVY / Math.max(1e-3, r.zoom)};
    }

    /** Das Regiebuch, das im Zustand läuft (oder null), und die Stunde darin. */
    public static String regiebuch(Map<String, String> z) {
        String t = z.get("regie.text");
        if (t == null) return null;
        try { return Scenario.parse(t).name(); } catch (RuntimeException e) { return null; }
    }

    public static double regieStunde(Map<String, String> z) {
        double b = Zahl.lies(z.get("regie.beginn")), t = Zahl.lies(z.get("uhr.zeit"));
        return Double.isNaN(b) || Double.isNaN(t) ? Double.NaN : (t / 1000 - b) / 3600;
    }

    /**
     * Stellt den Zustand her (aus dem Swing-Faden). Turm, Qualität und Ton stellt der Aufrufer ein (sie brauchen die
     * Oberfläche); hier alles andere. Gibt zurück, was nicht ging.
     */
    public static List<String> anwenden(Renderer r, Map<String, String> z) {
        List<String> fehler = new ArrayList<>();
        Map<String, Object> teile = teile(r);
        for (Map.Entry<String, Object> e : teile.entrySet()) {
            Map<String, String> t = Felder.teil(z, e.getKey());
            t.keySet().removeIf(x -> x.contains("."));
            if (e.getKey().equals("beacon")) t.remove("kennung");
            for (String f : Felder.schreiben(e.getValue(), t, ohne(e.getKey()))) fehler.add(e.getKey() + "." + f);
            if (e.getKey().equals("glass")) {
                Map<String, String> g = Felder.lesen(r.glass[0], OHNE);
                for (Wetglass x : r.glass) Felder.schreiben(x, g, OHNE);
                Felder.schreiben(r.rail, g, OHNE);
            }
        }
        String ken = z.get("beacon.kennung");
        if (ken != null) {
            String err = r.beacon.setCharacteristic(ken);
            if (err != null) fehler.add("beacon.kennung: " + err);
        }
        double zeit = Zahl.lies(z.get("uhr.zeit"));
        if (!Double.isNaN(zeit)) r.clock.setEpochMillis(zeit);
        double lapse = Zahl.lies(z.get("uhr.lapse"));
        if (!Double.isNaN(lapse)) r.clock.lapse = lapse;
        // Kamera
        Camera c = r.camera;
        double zoom = Zahl.lies(z.get("kamera.zoom"));
        r.zoom = Double.isNaN(zoom) ? 1 : zoom;
        String[] ziel = z.getOrDefault("kamera.ziel", "").split(" ");
        if ("KREISEN".equals(z.get("kamera.modus")) && ziel.length == 6) {
            c.mode = Camera.Mode.KREISEN;
            c.lookAt(Zahl.lies(ziel[0]), Zahl.lies(ziel[1]), Zahl.lies(ziel[2]), Zahl.lies(ziel[3]), Zahl.lies(ziel[4]), Zahl.lies(ziel[5]));
            c.autoOrbit = "true".equals(z.get("kamera.kreisen"));
            c.snap();
        } else {
            double x = Zahl.lies(z.get("kamera.x")), y = Zahl.lies(z.get("kamera.y")), zz = Zahl.lies(z.get("kamera.z"));
            if (!Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(zz)) {
                c.place(x, y, zz, Zahl.lies(z.get("kamera.gier")), Zahl.lies(z.get("kamera.nick")));
                c.snap();
            }
        }
        // Regie
        if (r.wiring != null) {
            Weather w = r.wiring.weather;
            double now = r.clock.epochMillis() / 1000.0;
            String text = z.get("regie.text");
            boolean gestartet = false;
            if (text != null) {
                try {
                    Scenario sc = Scenario.parse(text);
                    double b = Zahl.lies(z.get("regie.beginn"));
                    w.start(sc, Double.isNaN(b) ? now : b);
                    gestartet = true;
                } catch (RuntimeException ex) {
                    fehler.add("regie.text: " + ex.getMessage());
                }
            }
            if (!gestartet) {
                Weather.Phase p;
                try { p = Weather.Phase.valueOf(z.getOrDefault("regie.lage", "KLAR")); } catch (IllegalArgumentException ex) { p = Weather.Phase.KLAR; }
                w.startFree(now, p);
            }
            r.wiring.resumeRegie();
            for (Weather.Var v : Weather.Var.values()) {
                double h = Zahl.lies(z.get("regie.hand." + v.name()));
                if (!Double.isNaN(h)) r.wiring.hand(v, h);
            }
            // Handwerte der Dünung und des Regenbogens kamen schon mit den Feldern von wiring
            Map<String, String> wf = Felder.teil(z, "wiring");
            for (String k : new String[]{"swellPeriodHand", "swellDirHand", "rainbowHand"})
                if (wf.containsKey(k)) Felder.schreiben(r.wiring, Map.of(k, wf.get(k)));
            r.wiring.regie = !"false".equals(z.get("regie.an"));
        }
        return fehler;
    }

    /** Die Qualität aus dem Zustand oder null. */
    public static Quality qualitaet(Map<String, String> z) {
        try { return Quality.valueOf(z.get("app.qualitaet")); } catch (RuntimeException e) { return null; }
    }
}
