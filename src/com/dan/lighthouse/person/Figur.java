package com.dan.lighthouse.person;

import com.dan.avatar.Behavior;
import com.dan.avatar.Body;
import com.dan.avatar.Climate;
import com.dan.avatar.Motion;
import com.dan.avatar.Outfit;
import com.dan.avatar.Person;
import com.dan.avatar.Pose;
import com.dan.avatar.Skeleton;
import com.dan.avatar.Wardrobe;
import com.dan.avatar.Wetness;
import com.dan.avatar.World;
import com.dan.avatar.gl.AvatarGl;
import com.dan.avatar.gl.ShadowMap;
import com.dan.hair.gl.HairGl;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Eine Person in der Szene (Avatar Phase 15): Körper, Skelett, Bewegung, Verhalten, Kleidung, Haare, Nässe und Atem,
 * je Bild gerechnet und gezeichnet. Übernommen aus dem Avatar Studio ({@code StudioScene.Figure} mit
 * {@code prepareFigures}, {@code dress}, {@code environment}, {@code pupils}, {@code hairs}); hier ohne Menge, ohne
 * Bank und ohne Bildraum-Streuung (Lighthouse zeichnet in ein Ziel, die Haut nimmt die vorab integrierte Streuung).
 * Nur die App kennt zugleich {@code com.dan.avatar} und {@code com.dan.hair}, wie in der Verdrahtung.
 */
public final class Figur implements com.dan.avatar.Actor.Host {

    public final Body body;
    public final Skeleton skeleton;
    public volatile Person person;
    /** Ort der Füße (Welt, m); folgt der Bewegung. */
    public volatile float x, z;
    /** Höhe des Bodens am Ort (aus der {@link World}). */
    public volatile float groundY;
    public volatile Outfit outfit = Outfit.preset("Alltag");
    public volatile Motion motion;
    public volatile Behavior behavior;
    public volatile com.dan.avatar.Actor actor;
    /** Zuletzt gerechnete Weltmatrizen der Gelenke (relativ zur Figur). */
    public volatile double[][] jointWorld;
    public final Wetness wetness = new Wetness();
    public final com.dan.avatar.ClothSway clothSway = new com.dan.avatar.ClothSway();
    /** Pupille (mm). */
    public volatile double pupilMm = 4;
    /** Netze, die nicht gezeichnet werden. */
    public final Set<String> hidden = new HashSet<>();

    private AvatarGl gl;
    HairRig hair;
    private int drawn = -1, version;
    private float footY;
    private Map<String, Double> baseW;
    private double[][] rest;
    private boolean motionReady;
    private final Skeleton.Additive add = new Skeleton.Additive();
    private Map<String, Double> lastForms = Map.of();
    private Outfit dressed;
    private final Set<String> clothHidden = new HashSet<>();
    private double swayClock, dropIdle;
    private com.dan.avatar.SkinDrops drops;
    private com.dan.avatar.BreathFog breath;
    private com.dan.avatar.gl.DropMapGl dropGl;
    private com.dan.avatar.gl.BreathGl breathGl;
    private String error;

    /** Kleiderschrank des Grundkörpers (einmal je Programm) und was sich je Netz nicht ändert. */
    static final Wardrobe WARDROBE = Wardrobe.load();
    private static final Map<com.dan.gltf.Gltf.Primitive, float[]> FLEX = new java.util.IdentityHashMap<>();
    private static final Set<Long> COVER_DONE = new HashSet<>();
    /** Kugel, in die die Kapuze das Haar drückt (wie im Studio). */
    static final double HOOD_LIFT = 0.06, HOOD_RADIUS = 0.088;

    public Figur(Body body, Skeleton skeleton, Person person, float x, float z) {
        this.body = body; this.skeleton = skeleton; this.person = person; this.x = x; this.z = z;
    }

    public void set(Person p) { person = p; version++; }

    public String error() { return error; }

    public boolean ready() { return gl != null && jointWorld != null; }

    /** Unterkante der Ruhelage in der Welt. */
    public float baseY() { return groundY - footY; }

    // ---- Sprechen (Avatar Phase 16): sage im Regiebuch bewegt den Mund (com.dan.voice, stumm)
    /** Die Spur, die gerade gesprochen wird (oder null), und die Zeit darin (s). */
    public volatile com.dan.voice.Speech speech;
    public volatile double speechT;
    private com.dan.voice.Speech stepped;
    private int breathI, endI;

    @Override public double say(String text) {
        com.dan.voice.Speech s = com.dan.voice.Speech.fromText(text, 1.0);
        speechT = 0;
        speech = s;
        return s.duration;
    }

    /** Je Bild: Viseme ins Verhalten, vor dem Satz einatmen, am Phrasenende bald blinzeln. */
    private void speakStep(Behavior b, double dt) {
        com.dan.voice.Speech s = speech;
        if (s != stepped) { stepped = s; breathI = 0; endI = 0; }
        if (s == null) { if (!b.speech.isEmpty()) b.speech = Map.of(); b.talking = false; return; }
        speechT += dt;
        double t = speechT;
        while (breathI < s.breaths.size() && s.breaths.get(breathI) <= t) { b.inhale(); breathI++; }
        while (endI < s.phraseEnds.size() && s.phraseEnds.get(endI) <= t) { b.blinkSoon(0.15); endI++; }
        b.speech = s.forms(t);
        b.talking = s.talking(t);
        if (t > s.duration + 0.4) speech = null;
    }

    // ---- Actor.Host
    @Override public Behavior behavior() { return behavior; }
    @Override public Motion motion() {
        Motion m = motion;
        if (m == null) { m = new Motion(Motion.line(x, z, 0), 0); motionReady = false; motion = m; }
        return m;
    }
    @Override public Outfit outfit() { return outfit; }
    @Override public void outfit(Outfit o) { outfit = o; }
    @Override public double[] head() {
        double[][] w = jointWorld;
        if (w == null) return null;
        double[] hp = Skeleton.position(w, skeleton.joint("head"));
        return new double[]{hp[0] + x, hp[1] + baseY(), hp[2] + z};
    }

    /** Momentaufnahme (Ausgang der Schnittstelle). */
    public com.dan.avatar.Snapshot snapshot() {
        String n = actor != null ? actor.name : person.name;
        return com.dan.avatar.Snapshot.of(n, groundY, motion, behavior, wetness, outfit, x, z, head(), actor == null ? null : actor.gazeTarget());
    }

    /** Alles abtrocknen. */
    public void dry() { wetness.dry(); if (drops != null) drops.clear(); }

    // ------------------------------------------------------------ je Bild rechnen

    /**
     * Körper, Kleidung, Bewegung, Verhalten, Skinning, Augen und Frisur (wie {@code prepareFigures} im Studio).
     * {@code eye}: Auge der Kamera (Welt); {@code q}: Stufe 0–3.
     */
    public void prepare(GL4 g, double dt, double[] eye, World world, int q) {
        boolean newBody = gl == null || drawn != version;
        if (gl == null) {
            gl = new AvatarGl();
            gl.init(g, body);
            if (gl.error() != null) error = gl.error();
            hair = new HairRig();
            hair.init(g, gl, body);
            if (hair.error() != null) error = hair.error();
        }
        String activeHair = HairRig.STYLE_MESH.get(person.hair);
        for (int i = 0; i < body.parts.size(); i++) {
            String nm = body.parts.get(i).mesh.name;
            boolean idle = false;
            for (String hm : HairRig.STYLE_MESH.values()) if (hm != null && nm.contains(hm) && !hm.equals(activeHair)) idle = true;
            for (String hd : hidden) if (nm.contains(hd)) idle = true;
            if (clothHidden.contains(nm)) idle = true;
            if (idle) gl.idle.add(i); else gl.idle.remove(i);
        }
        dress(g, dt);
        if (newBody) {
            Map<String, Double> w = person.weights();
            baseW = w;
            gl.setWeights(g, w);
            gl.setPerson(person);
            footY = body.footY(w);
            rest = skeleton.restFor(w);
            drawn = version;
            if (behavior == null) behavior = new Behavior(person.seed);
            Map<String, Double> plain = new LinkedHashMap<>(w);
            plain.keySet().removeIf(k -> k.startsWith("arkit.") || k.startsWith("visem."));
            behavior.calibrate(gl.eyeRig(), body, plain);
        }
        Motion mo = motion();
        if (!motionReady) { mo.calibrate(skeleton, rest, person); motionReady = true; }
        mo.step(dt);
        mo.trackFeet();
        x = (float) mo.x; z = (float) mo.z;
        int he = mo.takeHoodEvent();
        if (he != 0) outfit = outfit.withHood(he > 0);
        Pose st = skeleton.poses.get("stehen");
        groundY = (float) world.ground(x, z);
        int hj = skeleton.joint("head");
        Behavior b = behavior;
        b.camera = toLocal(eye[0], eye[1] - baseY(), eye[2]);
        b.standing = mo.gait < 0.05 && mo.sit < 0.05;
        com.dan.avatar.Actor ac = actor;
        double[] gz = ac == null ? null : ac.gazeTarget();
        boolean directed = ac != null && ac.gazeDirected();
        if (gz != null) b.lookAt(toLocal(gz[0], gz[1] - baseY(), gz[2]));
        else if (mo.gait > 0.05 && !directed) {
            double[] ahead = mo.path.at(mo.s + 3);
            b.lookAt(toLocal(ahead[0], 1.2, ahead[1]));
        } else if (!directed && b.look() == Behavior.Look.POINT) b.look(Behavior.Look.CAMERA);
        speakStep(b, dt);
        b.step(dt);
        b.additive(add);
        mo.additive(add);
        double[][] wj = skeleton.pose(st, rest, true, add);
        mo.apply(skeleton, wj, rest);
        mo.measure(skeleton, wj, dt);
        Map<String, Double> forms = b.forms(wj, rest, skeleton);
        Map<String, Double> all = new LinkedHashMap<>(baseW);
        Map<String, Double> face = new LinkedHashMap<>(person.forms);
        for (Map.Entry<String, Double> en : forms.entrySet()) {
            all.merge(en.getKey(), en.getValue(), (a, c) -> Math.min(1, a + c));
            face.merge(en.getKey(), en.getValue(), (a, c) -> Math.min(1, a + c));
        }
        gl.setWeights(g, all);
        gl.setMouthOpen(AvatarGl.mouthOpening(face));
        double[][] sk = Skeleton.skin(wj, rest);
        float[] dq = Skeleton.toDq(sk);
        gl.setSkin(g, dq, Skeleton.toMat(sk), true);
        if (gl.eyeRig() != null && gl.eyeRig().valid()) gl.setEyes(gl.eyeRig().frames(body, all, dq));
        jointWorld = wj;
        lastForms = forms;
        if (hair != null && hj >= 0) hair.headRotation(toF(Skeleton.skinRotation(wj, rest, hj)));
        if (hair != null) hair.update(g, gl, body, person, q, hidden);
    }

    /** Kleidung anziehen, wenn sich das Outfit geändert hat; Feder am Hals mit dem Wind; Kapuze drückt das Haar an. */
    private void dress(GL4 g, double dt) {
        Outfit o = outfit;
        if (o != dressed) {
            clothHidden.clear();
            int mask = 0;
            for (int i = 0; i < body.parts.size(); i++) {
                Body.Part bp = body.parts.get(i);
                String nm = bp.mesh.name;
                Wardrobe.Item it = WARDROBE.item(nm);
                if (it == null) continue;
                Outfit.Piece pc = null;
                for (Outfit.Piece p : o.pieces) if (WARDROBE.find(p.mesh) == it) pc = p;
                if (pc == null) {
                    clothHidden.add(nm);
                    gl.setFabric(i, null, null);
                    gl.setFlex(g, i, null);
                } else {
                    mask |= 1 << it.bit;
                    gl.setFabric(i, pc.fabric, pc.color);
                    if (nm.startsWith("Monks_Hood")) {
                        boolean robe = false;
                        for (Outfit.Piece q : o.pieces) robe |= q.mesh.equals(Outfit.ROBE);
                        gl.setPartIndices(g, i, robe ? bp.prim.indices : above(bp, 1.15f));
                    }
                    Body.Part bodyP = body.body();
                    synchronized (COVER_DONE) {
                        if (!it.groups && bodyP != null && !nm.startsWith("Monks_Hood") && COVER_DONE.add(System.identityHashCode(body) * 64L + it.bit)) {
                            boolean[] cv = Wardrobe.cover(bodyP, bp);
                            if (bodyP.extraCover == null) bodyP.extraCover = new int[bodyP.prim.vertexCount];
                            for (int v = 0; v < cv.length; v++) if (cv[v]) bodyP.extraCover[v] |= 1 << it.bit;
                        }
                    }
                    float[] fx;
                    synchronized (FLEX) { fx = FLEX.computeIfAbsent(bp.prim, k -> flexOf(body, bp)); }
                    gl.setFlex(g, i, maxOf(fx) > 0.05f ? fx : null);
                }
            }
            gl.setWorn(g, mask);
            dressed = o;
            gl.idle.clear();
        }
        if (jointWorld != null) {
            int nj = skeleton.joint("neck_01");
            double[] a = Skeleton.position(jointWorld, nj >= 0 ? nj : 0);
            // die Kapuze wird im Sturm zugebunden: mehr als 9 m/s Wind nimmt die Feder nicht an [G, am Bild: sonst schlägt
            // der Rand der Kapuze übers Gesicht]
            double[] wl = windLocal;
            double ws = Math.hypot(wl[0], wl[2]), cap = ws > 9 ? 9 / ws : 1;
            clothSway.step(dt, a, new double[]{wl[0] * cap, wl[1], wl[2] * cap});
            swayClock += Math.max(0, Math.min(0.1, dt));
            for (int k = 0; k < 3; k++) { gl.sway[k] = (float) clothSway.sway[k]; gl.flutter[k] = (float) clothSway.flutter[k]; }
            gl.swayTime = (float) swayClock;
            if (hair != null) {
                float[] hd = hair.hgl.hood;
                if (o.hoodUp()) {
                    double[] hp = Skeleton.position(jointWorld, skeleton.joint("head"));
                    hd[0] = (float) (hp[0] + x); hd[1] = (float) (hp[1] + baseY() + HOOD_LIFT); hd[2] = (float) (hp[2] + z); hd[3] = (float) HOOD_RADIUS;
                } else hd[3] = 0;
            }
        }
    }

    /** Wind am Ort in Figur-Koordinaten (für Kleidung und Nässe; die Szene setzt ihn je Bild). */
    public volatile double[] windLocal = {0, 0, 0};

    /**
     * Umwelt (wie {@code environment} im Studio): Nässe je Zone, Tropfen im Gesicht, Kälte, Atemhauch. {@code c} ist
     * das Klima am Ort (Wind in Figur-Koordinaten).
     */
    public void environment(GL4 g, double dt, Climate c) {
        if (gl == null || jointWorld == null || rest == null) return;
        if (drops == null) {
            drops = new com.dan.avatar.SkinDrops(new com.dan.avatar.DropField(body), person.seed);
            breath = new com.dan.avatar.BreathFog(person.seed);
            dropGl = new com.dan.avatar.gl.DropMapGl();
            dropGl.init(g);
            if (dropGl.error() != null) error = dropGl.error();
            breathGl = new com.dan.avatar.gl.BreathGl();
            breathGl.init(g);
            if (breathGl.error() != null) error = breathGl.error();
            double[] r = drops.field.rect;
            gl.dropRect = new float[]{(float) r[0], (float) r[1], (float) r[2], (float) r[3]};
            gl.drops = dropGl;
        }
        double edt = Math.min(0.1, dt);
        Wetness w = wetness;
        String hs = person.hair;
        w.hair = !"glatze".equals(hs);
        w.capacity[Wetness.HAIR] = switch (hs) { case "kurz" -> 0.4; case "lang", "locken" -> 1.6; case "bob" -> 1.0; default -> 1.2; };
        int sub = Math.max(1, (int) Math.ceil(edt / 0.25));
        Climate cf = c;
        if (outfit.hoodUp() && c.hood < 1) { cf = c.copy(); cf.hood = 1; }
        for (int i = 0; i < sub; i++) w.step(edt / sub, cf);
        double faceWet = w.wet(Wetness.FACE);
        float cold = (float) c.coldness(faceWet);
        Behavior b = behavior;
        if (b != null) {
            b.cold = cold;
            b.rainOnFace = Math.min(1, w.inflow(Wetness.FACE) / 5.0);
        }
        int hj = skeleton.joint("head");
        double[] S = Skeleton.skinMatrix(jointWorld, rest, hj);
        double[] from = w.rainFrom(c);
        double[] fromRest = {S[0] * from[0] + S[1] * from[1] + S[2] * from[2], S[4] * from[0] + S[5] * from[1] + S[6] * from[2], S[8] * from[0] + S[9] * from[1] + S[10] * from[2]};
        double evap = 0.35 / 3600 * (1 + 0.5 * c.windSpeed()) * Math.max(0.02, 1 - c.humidity) * Math.pow(2, (c.tempC - 20) / 12) * 2.0;
        drops.film = faceWet;
        int dsub = Math.min(20, Math.max(1, (int) Math.ceil(edt / 0.05)));
        for (int i = 0; i < dsub; i++) drops.step(edt / dsub, cf.rain * (1 - 0.78 * Math.max(0, Math.min(1, cf.hood))), fromRest, evap, w.hair);
        double tau = 20 + 200 * Math.pow(c.humidity, 4);
        float[] bd = drops.beadData(), td = drops.trailData();
        if (bd.length > 0 || td.length > 0 || c.rain > 0) dropIdle = 0; else dropIdle += edt;
        if (dropIdle < 5 * tau) dropGl.update(g, bd, td, (float) Math.exp(-edt / tau));
        else if (dropIdle < 5 * tau + 1) { dropGl.clear(g); dropIdle = 1e9; }
        gl.wet[0] = (float) faceWet;
        gl.wet[1] = (float) w.wet(Wetness.HAIR);
        gl.wet[2] = (float) ((w.wet(Wetness.SHOULDERS) + w.wet(Wetness.CHEST) + w.wet(Wetness.BACK)) / 3);
        gl.wet[3] = (float) w.wet(Wetness.LEGS);
        gl.rainFrom = new float[]{(float) from[0], (float) from[1], (float) from[2]};
        gl.cold = cold;
        int nj = skeleton.joint("neck_01"), pj = skeleton.joint("pelvis");
        if (nj >= 0) gl.neckY = (float) jointWorld[nj][13];
        if (pj >= 0) gl.hipY = (float) jointWorld[pj][13];
        if (hair != null) { hair.wetness = gl.wet[1]; hair.browWet = faceWet; }
        com.dan.avatar.SkinMasks sm = com.dan.avatar.SkinMasks.of(body);
        double mo = AvatarGl.mouthOpening(lastForms);
        double[] at, dir;
        if (mo > 0.12) { at = pt(S, sm.mouth[0], sm.mouth[1], sm.mouth[2] + 0.004); dir = vec(S, 0, -0.2, 0.98); }
        else { at = pt(S, sm.noseTip[0], sm.noseTip[1] - 0.014, sm.noseTip[2] - 0.006); dir = vec(S, 0, -0.62, 0.78); }
        double bdt = b == null ? dt : Math.min(0.1, dt) * b.timeScale;
        breath.step(bdt, b == null ? 0 : b.breathFlow(), at, dir, c.breathFog(), c.wind);
    }

    /** Pupille nach dem Licht am Auge (vereinfacht: Führungs-, Kanten- und Aufhelllicht am Kopf). */
    public void pupils(double dt, AvatarGl.Light l) {
        double[] h = head();
        if (h == null || gl == null) return;
        double ill = lum(l.fillCol) * 3;
        double[] lk = {l.keyPos[0] - h[0], l.keyPos[1] - h[1], l.keyPos[2] - h[2]};
        double dk2 = Math.max(1e-6, lk[0] * lk[0] + lk[1] * lk[1] + lk[2] * lk[2]);
        ill += lum(l.keyCol) / dk2 * 0.5;
        double[] lr = {l.rimPos[0] - h[0], l.rimPos[1] - h[1], l.rimPos[2] - h[2]};
        ill += lum(l.rimCol) / Math.max(1e-6, lr[0] * lr[0] + lr[1] * lr[1] + lr[2] * lr[2]) * 0.3;
        double target = com.dan.avatar.EyeRig.pupilMm(Math.max(1e-4, ill * 25 * l.gain));
        double tau = target < pupilMm ? 0.25 : 1.5;
        pupilMm = dt <= 0 || dt > 1 ? target : pupilMm + (target - pupilMm) * (1 - Math.exp(-dt / tau));
        gl.setPupil(pupilMm / 11.8);
    }

    /** Haare: Kopf aus den Gelenken, Wind (Welt, m/s). */
    public void hairStep(double dt, double[] windWorld, double t) {
        double[][] jw = jointWorld;
        if (hair == null || jw == null) return;
        double[] hp = Skeleton.position(jw, skeleton.joint("head"));
        hair.step(dt, new double[]{hp[0] + x, hp[1] + baseY(), hp[2] + z}, windWorld, t);
    }

    // ------------------------------------------------------------ zeichnen

    public void drawDepth(GL4 g, ShadowMap sm, float t) {
        if (gl == null) return;
        gl.drawDepth(g, sm.viewProj, x, baseY(), z);
        if (hair != null) hair.depth(g, sm.viewProj, x, baseY(), z, t, hidden);
    }

    public void draw(GL4 g, float[] vp, double[] eye, AvatarGl.Light light, AvatarGl.Shading sh) {
        if (gl == null) return;
        gl.hidden.clear();
        gl.hidden.addAll(hidden);
        gl.hidden.addAll(clothHidden);
        gl.draw(g, vp, eye, light, sh, x, baseY(), z);
    }

    public void drawHairOpaque(GL4 g, HairGl.Lighting li, float[] vp) { if (hair != null) hair.drawOpaque(g, li, vp, x, baseY(), z, hidden); }

    public void drawHairBlended(GL4 g, HairGl.Lighting li, float[] vp, boolean strands) {
        if (hair == null) return;
        hair.strandsOn = strands;
        hair.drawBlended(g, li, vp, x, baseY(), z, hidden);
    }

    public void drawBreath(GL4 g, float[] vp, double[] eye, AvatarGl.Light light, float focal) {
        if (breathGl != null && breath != null && !breath.puffs().isEmpty()) breathGl.draw(g, breath.data(), vp, eye, light, focal, x, baseY(), z);
    }

    public void dispose(GL4 g) {
        if (hair != null) { hair.dispose(g); hair = null; }
        if (gl != null) { gl.dispose(g); gl = null; }
        drawn = -1;
    }

    // ------------------------------------------------------------ Hilfen

    /** Szenenpunkt → Figur-Koordinaten (Blick +z). */
    double[] toLocal(double wx, double wy, double wz) {
        Motion m = motion;
        double yaw = m == null ? 0 : m.yaw;
        double dx = wx - x, dz = wz - z, c = Math.cos(yaw), s = Math.sin(yaw);
        return new double[]{c * dx - s * dz, wy, s * dx + c * dz};
    }

    /** Welt → Figur-Richtung (für den Wind). */
    public double[] dirToLocal(double[] v) {
        Motion m = motion;
        double yaw = m == null ? 0 : m.yaw, c = Math.cos(yaw), s = Math.sin(yaw);
        return new double[]{c * v[0] - s * v[2], v[1], s * v[0] + c * v[2]};
    }

    static int[] above(Body.Part p, float y) {
        int[] ix = p.prim.indices, out = new int[ix.length];
        float[] pos = p.prim.position;
        int n = 0;
        for (int k = 0; k + 2 < ix.length; k += 3)
            if (pos[ix[k] * 3 + 1] > y || pos[ix[k + 1] * 3 + 1] > y || pos[ix[k + 2] * 3 + 1] > y) { out[n++] = ix[k]; out[n++] = ix[k + 1]; out[n++] = ix[k + 2]; }
        return java.util.Arrays.copyOf(out, n);
    }

    private static float maxOf(float[] a) { float m = 0; for (float v : a) m = Math.max(m, v); return m; }

    static float[] flexOf(Body b, Body.Part cloth) {
        Body.Part body = b.body();
        float[] bp = body.prim.position, cp = cloth.prim.position;
        double cs = 0.03;
        java.util.HashMap<Long, java.util.List<Integer>> grid = new java.util.HashMap<>();
        for (int i = 0; i < bp.length / 3; i++) {
            long key = key((int) Math.floor(bp[i * 3] / cs), (int) Math.floor(bp[i * 3 + 1] / cs), (int) Math.floor(bp[i * 3 + 2] / cs));
            grid.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(i);
        }
        int n = cp.length / 3;
        float[] fx = new float[n];
        for (int v = 0; v < n; v++) {
            int gx = (int) Math.floor(cp[v * 3] / cs), gy = (int) Math.floor(cp[v * 3 + 1] / cs), gz = (int) Math.floor(cp[v * 3 + 2] / cs);
            double best = 0.12 * 0.12;
            for (int dx = -3; dx <= 3; dx++) for (int dy = -3; dy <= 3; dy++) for (int dz = -3; dz <= 3; dz++) {
                java.util.List<Integer> l = grid.get(key(gx + dx, gy + dy, gz + dz));
                if (l == null) continue;
                for (int i : l) {
                    double ddx = bp[i * 3] - cp[v * 3], ddy = bp[i * 3 + 1] - cp[v * 3 + 1], ddz = bp[i * 3 + 2] - cp[v * 3 + 2];
                    double d2 = ddx * ddx + ddy * ddy + ddz * ddz;
                    if (d2 < best) best = d2;
                }
            }
            fx[v] = (float) com.dan.avatar.ClothSway.flex(Math.sqrt(best));
        }
        return fx;
    }

    private static long key(int x, int y, int z) { return ((long) (x & 0x1FFFFF) << 42) | ((long) (y & 0x1FFFFF) << 21) | (z & 0x1FFFFF); }

    private static float[] toF(double[] d) { float[] f = new float[d.length]; for (int i = 0; i < d.length; i++) f[i] = (float) d[i]; return f; }

    private static double[] pt(double[] S, double x, double y, double z) {
        return new double[]{S[0] * x + S[4] * y + S[8] * z + S[12], S[1] * x + S[5] * y + S[9] * z + S[13], S[2] * x + S[6] * y + S[10] * z + S[14]};
    }

    private static double[] vec(double[] S, double x, double y, double z) {
        double[] v = {S[0] * x + S[4] * y + S[8] * z, S[1] * x + S[5] * y + S[9] * z, S[2] * x + S[6] * y + S[10] * z};
        double l = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return new double[]{v[0] / l, v[1] / l, v[2] / l};
    }

    static double lum(float[] c) { return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]; }

    /** GL-Zustand, den {@link AvatarGl} braucht, ist gesetzt? (für Werkzeuge) */
    static void noCull(GL4 g) { g.glDisable(GL.GL_CULL_FACE); }
}
