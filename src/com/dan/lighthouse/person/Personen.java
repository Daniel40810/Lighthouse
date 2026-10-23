package com.dan.lighthouse.person;

import com.dan.avatar.Body;
import com.dan.avatar.Climate;
import com.dan.avatar.Motion;
import com.dan.avatar.Outfit;
import com.dan.avatar.Person;
import com.dan.avatar.Regie;
import com.dan.avatar.Skeleton;
import com.dan.avatar.gl.AvatarGl;
import com.dan.avatar.gl.ShadowMap;
import com.dan.gltf.Gltf;
import com.dan.hair.gl.HairGl;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Personen in Lighthouse (Avatar Phase 15, F22 A): der Wärter auf der Galerie. Lädt den Grundkörper aus
 * {@code com.dan.avatar} (einmal, im Hintergrund, mit Abbild in {@code daten/cache}), führt das Regiebuch
 * ({@link Regie}) mit der Galerie als Szene ({@link GalerieStage}), rechnet je Bild Bewegung, Verhalten, Kleidung,
 * Haare und Nässe ({@link Figur}) und zeichnet sie in das HDR-Ziel des Renderers, nach dem Leben und vor Gischt, Regen
 * und Nebel (die Personen schreiben Tiefe).
 *
 * <p>Licht: Der Renderer gibt das Licht der Szene vorbelichtet herein ({@link AvatarGl.Light}): Führungslicht aus der
 * Linse (Lampe und Strahl im Nahfeld), Aufhellung aus dem Himmel, Blitze. Die Schattenkarte der Person rechnet mit
 * Tiefe −1..1 (die Szene sonst mit umgekehrter Tiefe 0..1); dafür wird die Abbildung kurz umgeschaltet.
 */
public final class Personen {

    public static final String GLB = "/com/dan/avatar/res/avatar_base.glb", RIG = "/com/dan/avatar/res/avatar_base.rig.json";
    /** Der Wärter: seed 19 aus {@code AV_API.WUERFELN} (Mann, 56, kurzes graues Haar, Bart) [G: ausgesucht]. */
    public static final long WAERTER_SEED = 19;

    public final List<Figur> figuren = new CopyOnWriteArrayList<>();
    public final Galerie galerie = new Galerie();
    public final PersonKamera kamera = new PersonKamera();
    public final ShadowMap shadow = new ShadowMap();
    /** Personen zeigen (Bedienfeld). */
    public volatile boolean an;
    /** Einzelne Haare (Fliegenhaare, Bart) ab dieser Größe der Person im Bild (Bildpunkte). */
    public volatile double strandsAbPx = 320;
    /** Laufendes Regiebuch der Personen (oder null) und seine Szene. */
    public volatile Regie regie;
    public volatile GalerieStage stage;
    /** Zuletzt: Zeile, Untertitel, GPU- und Prozessorzeit (ms), Größe der Person im Bild (px), Herkunft der Person. */
    public volatile String zeile = "", text = "", herkunft = "";
    public volatile double gpuMs, cpuMs, groessePx;
    /** Licht des letzten Bilds (Werkzeuge, Prüfung). */
    public volatile AvatarGl.Light licht;
    private volatile String error;

    private volatile CompletableFuture<Object[]> laden;
    private Gltf model;
    private Object rigJson;
    private double pendingDt;
    private final int[] query = new int[4];
    private int frame;

    public String error() { return error; }

    /** Grundkörper im Hintergrund laden (einmal); {@code true}, wenn er da ist. */
    public boolean bereit() {
        CompletableFuture<Object[]> f = laden;
        if (f == null) {
            synchronized (this) {
                if (laden == null) laden = CompletableFuture.supplyAsync(Personen::ladenJetzt);
                f = laden;
            }
        }
        if (!f.isDone()) return false;
        if (model == null) {
            try { Object[] r = f.join(); model = (Gltf) r[0]; rigJson = r[1]; }
            catch (RuntimeException e) { error = "Grundkörper: " + e.getMessage(); laden = CompletableFuture.failedFuture(e); return false; }
        }
        return true;
    }

    /** Wartet, bis der Grundkörper geladen ist (Werkzeuge, Prüfung). */
    public void warten() { bereit(); laden.join(); bereit(); }

    private static Object[] ladenJetzt() {
        try {
            byte[] glb = resource(GLB), rj = resource(RIG);
            if (glb == null) throw new IllegalStateException("fehlt: " + GLB);
            Gltf m = com.dan.gltf.GltfCache.load(glb, Path.of("daten", "cache"), "avatar_base");
            Object rig = rj == null ? null : com.dan.gltf.Json.parse(new String(rj, StandardCharsets.UTF_8));
            return new Object[]{m, rig};
        } catch (Exception e) {
            throw new java.util.concurrent.CompletionException(e);
        }
    }

    static byte[] resource(String name) throws java.io.IOException {
        try (InputStream in = Personen.class.getResourceAsStream(name)) { return in == null ? null : in.readAllBytes(); }
    }

    /** Eine Person auf die Galerie stellen (Füße am Ort {x, z}, Blick nach {@code yaw}, Bogenmaß, 0 = +z). */
    public Figur neu(Person p, Outfit o, double x, double z, double yaw) {
        if (model == null) throw new IllegalStateException("Grundkörper noch nicht geladen");
        Figur f = new Figur(new Body(model), new Skeleton(model, rigJson), p, (float) x, (float) z);
        f.outfit = o;
        f.motion = new Motion(Motion.line(x, z, yaw), 0);
        figuren.add(f);
        return f;
    }

    /** Alle Personen abräumen (beim nächsten Bild im GL-Faden freigegeben). */
    public void leeren() { abzuraeumen.addAll(figuren); figuren.clear(); regie = null; }
    private final List<Figur> abzuraeumen = new CopyOnWriteArrayList<>();

    /** Erste Person (oder null). */
    public Figur erste() { return figuren.isEmpty() ? null : figuren.get(0); }

    /** Kopf der ersten Person (Welt) oder null. */
    public double[] kopf() { Figur f = erste(); return f == null ? null : f.head(); }

    // ------------------------------------------------------------ Person aus der Datenbank AVATAR

    /**
     * Die Person mit diesem seed: aus der Datenbank über {@code AV_API.WUERFELN} (Schema LIGHTHOUSE mit der Rolle
     * AVATAR_USER), sonst mit demselben Würfel in Java. {@code herkunft} sagt, woher sie kam.
     */
    public Person person(long seed) {
        try {
            com.dan.lighthouse.db.Db.Konfig k = com.dan.lighthouse.db.Db.konfig();
            if (k.hatPasswort()) {
                try (java.sql.Connection c = com.dan.lighthouse.db.Db.oeffnen(k);
                     java.sql.Statement st = c.createStatement();
                     java.sql.ResultSet r = st.executeQuery("SELECT t.P.SEED, t.P.NAME, t.P.REGLER FROM (SELECT AV_API.WUERFELN(" + seed + ") P FROM DUAL) t")) {
                    if (r.next()) {
                        Person p = Person.fromJson(r.getString(3), r.getLong(1), r.getString(2));
                        herkunft = "AVATAR über AV_API.WUERFELN(" + seed + ")" + (p.sameAs(Person.wuerfeln(seed)) ? ", gleich wie in Java" : ", WEICHT AB");
                        return p;
                    }
                }
            }
            herkunft = "Java (ohne Datenbank), Person.wuerfeln(" + seed + ")";
        } catch (Exception e) {
            herkunft = "Java (Datenbank: " + String.valueOf(e.getMessage()).split("\\R")[0] + ")";
        }
        return Person.wuerfeln(seed);
    }

    // ------------------------------------------------------------ Regie

    /** Ein Regiebuch beginnen (die Szene ist die Galerie). */
    public void starten(Regie r, GalerieStage s) {
        leeren();
        r.onText = t -> text = t;
        r.onLine = l -> zeile = l.toString();
        stage = s;
        regie = r;
        r.start(s);
    }

    /** Zeit weiter (aus {@code Renderer.step}): Regie, Wind und Regen der Szene für die Galerie. */
    public void schritt(double dt) {
        Regie r = regie;
        // die Regie wartet, bis der Grundkörper geladen ist (das Laden beginnt hier, im Hintergrund)
        if (r != null && r.running() && bereit()) r.step(dt);
        pendingDt += dt;
    }

    // ------------------------------------------------------------ zeichnen

    /**
     * Personen rechnen und ins gebundene Ziel {@code fbo} (w × h) zeichnen. {@code vp} Ansicht·Projektion der Szene,
     * {@code eye} Auge, {@code fovyDeg} Bildwinkel, {@code reversed} umgekehrte Tiefe, {@code light} Licht der Szene
     * (vorbelichtet), {@code q} Stufe 0–3, {@code t} Zeit (s); Wind und Regen nimmt jede Person aus der {@link Galerie}.
     */
    public void zeichnen(GL4 gl, float[] vp, double[] eye, double fovyDeg, int w, int h, boolean reversed, int fbo,
                         AvatarGl.Light light, int q, double t) {
        for (Figur f : abzuraeumen) f.dispose(gl);
        abzuraeumen.clear();
        if (!an || figuren.isEmpty() || !bereit()) { pendingDt = 0; return; }
        long c0 = System.nanoTime();
        licht = light;
        if (query[0] == 0) gl.glGenQueries(4, query, 0);
        int qb = (frame & 1) * 2;
        if (frame > 2) {
            int ob = ((frame + 1) & 1) * 2;
            int[] av = new int[1];
            gl.glGetQueryObjectiv(query[ob + 1], com.jogamp.opengl.GL2ES3.GL_QUERY_RESULT_AVAILABLE, av, 0);
            if (av[0] != 0) {
                long[] a = new long[1], b = new long[1];
                gl.glGetQueryObjecti64v(query[ob], com.jogamp.opengl.GL2ES3.GL_QUERY_RESULT, a, 0);
                gl.glGetQueryObjecti64v(query[ob + 1], com.jogamp.opengl.GL2ES3.GL_QUERY_RESULT, b, 0);
                gpuMs = gpuMs * 0.9 + 0.1 * (b[0] - a[0]) / 1e6;
            }
        }
        gl.glQueryCounter(query[qb], GL4.GL_TIMESTAMP);
        double dt = Math.min(0.1, pendingDt);
        pendingDt = 0;
        galerie.eye = eye.clone();

        // rechnen: Körper, Bewegung, Verhalten, Kleidung, Haar, Nässe, Pupille
        for (Figur f : figuren) {
            double[] wl = f.dirToLocal(galerie.climateAt(f.x, f.groundY + 1.6, f.z).wind);
            f.windLocal = wl;
            f.prepare(gl, dt, eye, galerie, q);
            if (f.error() != null) error = f.error();
            Climate c = galerie.climateAt(f.x, f.groundY + 1.6, f.z);
            c.wind = f.dirToLocal(c.wind);
            f.environment(gl, dt, c);
            f.pupils(dt, light);
            f.hairStep(dt, galerie.climateAt(f.x, f.groundY + 1.6, f.z).wind, t);
        }
        // Größe der ersten Person im Bild (für die einzelnen Haare)
        Figur f0 = erste();
        double[] hd = f0 == null ? null : f0.head();
        groessePx = hd == null ? 0 : 1.8 / Math.max(0.1, dist(hd, eye)) / (2 * Math.tan(Math.toRadians(fovyDeg) / 2)) * h;

        // Schattenkarte des Führungslichts (Tiefe −1..1, dann zurück auf die Abbildung der Szene)
        double[] c = new double[3];
        double rr = bounds(c);
        boolean shadowOn = hd != null && groessePx > 40;
        if (shadowOn) {
            if (reversed) gl.glClipControl(GL4.GL_LOWER_LEFT, GL4.GL_NEGATIVE_ONE_TO_ONE);
            gl.glClearDepth(1);
            shadow.fit(light.keyPos, c, rr);
            shadow.soft = 2.5f;
            shadow.begin(gl, q <= 0 ? 1024 : 2048);
            for (Figur f : figuren) f.drawDepth(gl, shadow, (float) t);
            shadow.end(gl);
            if (reversed) gl.glClipControl(GL4.GL_LOWER_LEFT, GL4.GL_ZERO_TO_ONE);
            gl.glClearDepth(reversed ? 0 : 1);
        } else shadow.valid = false;

        // ins Bild der Szene
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, fbo);
        gl.glViewport(0, 0, w, h);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(reversed ? GL.GL_GREATER : GL.GL_LESS);
        gl.glDepthMask(true);
        gl.glDisable(GL.GL_BLEND);
        gl.glDisable(GL.GL_CULL_FACE);
        AvatarGl.Shading sh = new AvatarGl.Shading();
        sh.shadow = shadowOn ? shadow : null;
        sh.split = false;
        sh.low = true;
        for (Figur f : figuren) f.draw(gl, vp, eye, light, sh);
        float focal = (float) (0.5 * h / Math.tan(Math.toRadians(fovyDeg) / 2));
        HairGl.Lighting li = HairRig.lighting(light, shadowOn ? shadow : null, eye, focal, (float) t);
        for (Figur f : figuren) f.drawHairOpaque(gl, li, vp);
        boolean strands = groessePx >= strandsAbPx && q >= 1;
        for (Figur f : figuren) f.drawHairBlended(gl, li, vp, strands);
        for (Figur f : figuren) f.drawBreath(gl, vp, eye, light, focal);
        gl.glDisable(GL.GL_BLEND);
        gl.glDepthMask(true);
        gl.glDepthFunc(reversed ? GL.GL_GREATER : GL.GL_LESS);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, fbo);
        gl.glViewport(0, 0, w, h);
        gl.glQueryCounter(query[qb + 1], GL4.GL_TIMESTAMP);
        frame++;
        cpuMs = cpuMs * 0.9 + 0.1 * (System.nanoTime() - c0) / 1e6;
    }

    private double bounds(double[] c) {
        double[] lo = {1e9, 1e9, 1e9}, hi = {-1e9, -1e9, -1e9};
        for (Figur f : figuren) {
            double[][] jw = f.jointWorld;
            if (jw == null) continue;
            for (double[] m : jw) {
                double[] p = {m[12] + f.x, m[13] + f.baseY(), m[14] + f.z};
                for (int k = 0; k < 3; k++) { lo[k] = Math.min(lo[k], p[k]); hi[k] = Math.max(hi[k], p[k]); }
            }
        }
        if (lo[0] > hi[0]) { c[0] = 0; c[1] = 24; c[2] = 0; return 1.2; }
        double r = 0;
        for (int k = 0; k < 3; k++) { c[k] = (lo[k] + hi[k]) / 2; r += (hi[k] - lo[k]) * (hi[k] - lo[k]) / 4; }
        return Math.sqrt(r) + 0.3;
    }

    static double dist(double[] a, double[] b) { return Math.sqrt((a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]) + (a[2] - b[2]) * (a[2] - b[2])); }

    /** Zeile für HUD und Bedienfeld. */
    public String zeile() {
        Figur f = erste();
        if (!an) return "aus";
        if (laden != null && !laden.isDone()) return "Grundkörper wird geladen …";
        if (error != null) return "Fehler: " + error;
        if (f == null) return "keine Person";
        Regie r = regie;
        return String.format(Locale.GERMANY, "%s · %s · GPU %.2f ms, Prozessor %.1f ms · %.0f px%s", f.person.name, f.outfit.name, gpuMs, cpuMs, groessePx,
                r == null ? "" : String.format(Locale.GERMANY, " · Regie %.0f s%s", r.time(), r.finished() ? " (fertig)" : ""));
    }
}
