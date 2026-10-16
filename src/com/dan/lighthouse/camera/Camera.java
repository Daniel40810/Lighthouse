package com.dan.lighthouse.camera;

import com.dan.lighthouse.gl.Mat4;

/**
 * Die Kamera der Szene in zwei Arten (Phase 1):
 * <ul>
 * <li><b>Kreisen (Orbit):</b> um einen Drehpunkt, links ziehen dreht, rechts ziehen verschiebt, das Rad zoomt in
 * Schritten von 12 %. R lässt sie von selbst um den Turm kreisen.</li>
 * <li><b>Frei:</b> fliegen mit W A S D, Q und E für ab und auf, Umschalt für schneller; ziehen schaut umher.</li>
 * <li><b>Folgen (Phase 13):</b> das Auge hängt an etwas Bewegtem, einer Möwe („Möwenflug“) oder der Brücke eines
 * Schiffs; mit eigenem Oben, also mit Querneigung und Rollen. Jede Taste oder jedes Ziehen beendet es (Frei).</li>
 * </ul>
 * Richtungen wie auf der Seekarte: {@code yaw} ist die Blickrichtung in Grad von Nord im Uhrzeigersinn, {@code pitch}
 * die Neigung nach unten. Alle Werte laufen weich ihrem Ziel nach. Das Auge bleibt mindestens {@link #MIN_EYE} m
 * über dem Wasser (ab Phase 2 über der Welle an dieser Stelle).
 */
public final class Camera {
    public enum Mode { KREISEN, FREI, FAHRT, FOLGEN }

    /** Phase 13: Lage zum Folgen je Bild: {Auge, vorn, oben} (je drei Werte, Welt), oder null, wenn es nichts mehr gibt. */
    public interface Pose { double[][] pose(); }

    public static final double MIN_DIST = 3, MAX_DIST = 40_000, MIN_EYE = 1.5, MAX_EYE = 12_000;
    public static final double FOVY = 55;

    public volatile Mode mode = Mode.KREISEN;
    /** Ziel: Drehpunkt, Blickrichtung (Grad), Neigung (Grad, positiv nach unten), Abstand. */
    public double tx, ty = 10, tz, yaw = 250, pitch = 2, dist = 120;
    /** Laufender Stand (weich nachgeführt). */
    private double cx, cy, cz, cyaw = yaw, cpitch = pitch, cdist = dist;
    /** Freie Kamera: Auge und Blick. */
    public double fx, fy, fz, fyaw, fpitch;
    private double fxs, fys, fzs, fyaws, fpitchs;
    public volatile boolean autoOrbit;
    /** Grad je Sekunde beim Kreisen. */
    public double orbitSpeed = 4;
    public volatile boolean kFwd, kBack, kLeft, kRight, kUp, kDown, kRun;
    /** Höhe des Wassers unter dem Auge (vom Renderer gesetzt; Phase 1: Meeresspiegel plus Gezeit). */
    public volatile double waterLevel;

    /** Phase 11: eine Kamerafahrt (Aufstieg) und ihre Zeit; jede Bewegung von Hand beendet sie. */
    private volatile Aufstieg ride;
    private double rideT;

    /** Auge und Blickrichtung des laufenden Bilds; Oben nur beim Folgen geneigt. */
    public final double[] eye = new double[3], fwd = new double[3], up = {0, 1, 0};
    private volatile Pose follow;
    /** Name dessen, dem die Kamera folgt (HUD). */
    public volatile String followName = "";

    /** Phase 13: der Kamera an etwas Bewegtem folgen (Möwe, Schiffsbrücke). */
    public void follow(String name, Pose p) {
        follow = p;
        followName = name;
        autoOrbit = false;
        mode = Mode.FOLGEN;
        update();
    }

    public Camera() { home(); snap(); }

    /** Grundansicht: vom Ostnordosten auf den Turm, Blick nach Westsüdwest in die Abenddämmerung. */
    public void home() {
        mode = Mode.KREISEN;
        autoOrbit = false;
        tx = 0; ty = 10; tz = 0;
        yaw = 250; pitch = 2; dist = 120;
        clamp();
    }

    /** Setzt alles sofort auf das Ziel (nach Sprüngen, beim Start, für Standbilder). */
    public void snap() {
        cx = tx; cy = ty; cz = tz; cyaw = yaw; cpitch = pitch; cdist = dist;
        fxs = fx; fys = fy; fzs = fz; fyaws = fyaw; fpitchs = fpitch;
        update();
    }

    public void lookAt(double x, double y, double z, double yawDeg, double pitchDeg, double distance) {
        tx = x; ty = y; tz = z; yaw = yawDeg; pitch = pitchDeg; dist = distance;
        clamp();
    }

    /** Phase 11: eine Kamerafahrt starten (Mode FAHRT); am Ende steht die Kamera frei am letzten Punkt. */
    public void startRide(Aufstieg a) {
        ride = a;
        rideT = 0;
        autoOrbit = false;
        mode = Mode.FAHRT;
        rideTo(0);
        fxs = fx; fys = fy; fzs = fz; fyaws = fyaw; fpitchs = fpitch;
        update();
    }

    /** Laufende Fahrt bei Zeit t (für Standbilder der Bildfolge). */
    public void rideTo(double t) {
        if (ride == null) return;
        rideT = t;
        double[] k = ride.sample(t);
        fx = fxs = k[0]; fy = fys = k[1]; fz = fzs = k[2]; fyaw = fyaws = k[3]; fpitch = fpitchs = k[4];
        update();
    }

    /** Zeit der laufenden Fahrt in s (−1 = keine). */
    public double rideTime() { return mode == Mode.FAHRT ? rideT : -1; }

    /** Phase 15: Name der laufenden Fahrt (HUD), sonst leer. */
    public String rideName() { Aufstieg a = ride; return mode == Mode.FAHRT && a != null ? a.name : ""; }

    /** Wechsel zwischen Kreisen und Frei; die Ansicht bleibt dabei stehen. */
    public void setMode(Mode m) {
        if (m == mode) return;
        if (mode == Mode.FAHRT || mode == Mode.FOLGEN) { leaveFollow(); if (m == Mode.FREI) return; }
        if (m == Mode.FREI) {
            fx = fxs = eye[0]; fy = fys = eye[1]; fz = fzs = eye[2];
            fyaw = fyaws = cyaw; fpitch = fpitchs = cpitch;
            autoOrbit = false;
        } else {
            // Drehpunkt dort, wo der Blick 60 m voraus hinzeigt (nicht unter Wasser)
            double d = 60;
            tx = eye[0] + fwd[0] * d; tz = eye[2] + fwd[2] * d;
            ty = Math.max(waterLevel + 1, eye[1] + fwd[1] * d);
            yaw = fyaw; dist = Math.hypot(Math.hypot(tx - eye[0], tz - eye[2]), ty - eye[1]);
            pitch = Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, (eye[1] - ty) / dist))));
            cx = tx; cy = ty; cz = tz; cyaw = yaw; cpitch = pitch; cdist = dist;
        }
        mode = m;
    }

    /** Folgen oder Fahrt beenden: frei dort stehen bleiben, wohin gerade geschaut wird. */
    private void leaveFollow() {
        if (mode == Mode.FOLGEN) {
            fx = fxs = eye[0]; fy = fys = eye[1]; fz = fzs = eye[2];
            fyaw = fyaws = Math.toDegrees(Math.atan2(fwd[0], -fwd[2]));
            fpitch = fpitchs = Math.toDegrees(-Math.asin(Math.max(-1, Math.min(1, fwd[1]))));
            follow = null;
            up[0] = 0; up[1] = 1; up[2] = 0;
        }
        mode = Mode.FREI;
    }

    /** Drehen um den Drehpunkt bzw. Umschauen (Grad). */
    public void orbit(double dYawDeg, double dPitchDeg) {
        if (mode == Mode.FAHRT || mode == Mode.FOLGEN) leaveFollow();
        if (mode == Mode.FREI) {
            fyaw += dYawDeg;
            fpitch = Math.max(-85, Math.min(85, fpitch + dPitchDeg));
        } else {
            yaw += dYawDeg;
            pitch += dPitchDeg;
            clamp();
        }
    }

    /** Verschieben in Bildschirmanteilen (rechts ziehen). */
    public void pan(double dxFrac, double dyFrac) {
        if (mode != Mode.KREISEN) return;
        double s = dist * 1.1;
        double a = Math.toRadians(yaw);
        double rx = Math.cos(a), rz = Math.sin(a);   // rechts vom Blick
        double fxh = Math.sin(a), fzh = -Math.cos(a);
        tx += -dxFrac * s * rx + dyFrac * s * fxh;
        tz += -dxFrac * s * rz + dyFrac * s * fzh;
        clamp();
    }

    /** Rad: Schritte von 12 % (positiv = weg). */
    public void zoom(double steps) {
        if (mode == Mode.FAHRT || mode == Mode.FOLGEN) leaveFollow();
        if (mode == Mode.FREI) {
            double d = steps * -4 * (1 + eye[1] / 20);
            fx += fwd[0] * d; fy += fwd[1] * d; fz += fwd[2] * d;
            return;
        }
        dist *= Math.pow(1.12, steps);
        clamp();
    }

    private void clamp() {
        dist = Math.max(MIN_DIST, Math.min(MAX_DIST, dist));
        pitch = Math.max(-60, Math.min(89, pitch));
        // Auge über dem Wasser: ty + dist·sin(pitch) ≥ Wasser + MIN_EYE
        double need = (waterLevel + MIN_EYE - ty) / dist;
        if (need > -1 && need < 1) pitch = Math.max(pitch, Math.toDegrees(Math.asin(need)));
        ty = Math.max(waterLevel - 5, Math.min(MAX_EYE, ty));
    }

    /** Frei an einen Ort stellen und in eine Richtung schauen (Laternenraum, Nahaufnahmen); springt sofort. */
    public void place(double x, double y, double z, double yawDeg, double pitchDeg) {
        follow = null;
        up[0] = 0; up[1] = 1; up[2] = 0;
        mode = Mode.FREI;
        autoOrbit = false;
        fx = x; fy = y; fz = z; fyaw = yawDeg; fpitch = pitchDeg;
        snap();
    }

    /** Läuft um dt Sekunden weiter: Kreisen, Tasten, weiches Nachführen. */
    public void step(double dt) {
        if (mode == Mode.FOLGEN) {
            if (kFwd || kBack || kLeft || kRight || kUp || kDown || follow == null) leaveFollow();
            else { update(); if (mode == Mode.FOLGEN) return; }
        }
        if (mode == Mode.FAHRT) {
            if (kFwd || kBack || kLeft || kRight || kUp || kDown || ride == null) mode = Mode.FREI;
            else {
                double nt = rideT + dt;
                if (ride.loop && ride.duration() > 0) nt %= ride.duration();
                rideTo(nt);
                if (!ride.loop && rideT >= ride.duration()) mode = Mode.FREI;
                return;
            }
        }
        if (mode == Mode.KREISEN) {
            if (autoOrbit) yaw += orbitSpeed * dt;
            double f = (kFwd ? 1 : 0) - (kBack ? 1 : 0), s = (kRight ? 1 : 0) - (kLeft ? 1 : 0);
            if (f != 0 || s != 0) {
                double v = (kRun ? 4 : 1) * Math.max(8, dist * 0.6) * dt, a = Math.toRadians(yaw);
                tx += (Math.sin(a) * f + Math.cos(a) * s) * v;
                tz += (-Math.cos(a) * f + Math.sin(a) * s) * v;
            }
            clamp();
            double k = 1 - Math.exp(-dt * 7);
            cx += (tx - cx) * k; cy += (ty - cy) * k; cz += (tz - cz) * k;
            cyaw += (yaw - cyaw) * k; cpitch += (pitch - cpitch) * k;
            cdist = Math.exp(Math.log(cdist) + (Math.log(dist) - Math.log(cdist)) * k);
        } else {
            double f = (kFwd ? 1 : 0) - (kBack ? 1 : 0), s = (kRight ? 1 : 0) - (kLeft ? 1 : 0), u = (kUp ? 1 : 0) - (kDown ? 1 : 0);
            double v = (kRun ? 4 : 1) * 6 * (1 + Math.max(0, fy - waterLevel) / 20) * dt;
            double a = Math.toRadians(fyaw), p = Math.toRadians(fpitch);
            double dx = Math.sin(a) * Math.cos(p), dy = -Math.sin(p), dz = -Math.cos(a) * Math.cos(p);
            fx += (dx * f + Math.cos(a) * s) * v;
            fy += (dy * f + u) * v;
            fz += (dz * f + Math.sin(a) * s) * v;
            fy = Math.max(waterLevel + MIN_EYE, Math.min(MAX_EYE, fy));
            double k = 1 - Math.exp(-dt * 10);
            fxs += (fx - fxs) * k; fys += (fy - fys) * k; fzs += (fz - fzs) * k;
            fyaws += (fyaw - fyaws) * k; fpitchs += (fpitch - fpitchs) * k;
        }
        update();
    }

    private void update() {
        if (mode == Mode.FOLGEN) {
            Pose p = follow;
            double[][] q = p != null ? p.pose() : null;
            if (q == null) { leaveFollow(); update(); return; }
            for (int i = 0; i < 3; i++) { eye[i] = q[0][i]; fwd[i] = q[1][i]; up[i] = q[2][i]; }
            return;
        }
        up[0] = 0; up[1] = 1; up[2] = 0;
        if (mode == Mode.KREISEN) {
            setFwd(cyaw, cpitch);
            eye[0] = cx - fwd[0] * cdist;
            eye[1] = cy - fwd[1] * cdist;
            eye[2] = cz - fwd[2] * cdist;
            if (eye[1] < waterLevel + MIN_EYE) eye[1] = waterLevel + MIN_EYE;
        } else {
            setFwd(fyaws, fpitchs);
            eye[0] = fxs; eye[1] = Math.max(waterLevel + MIN_EYE, fys); eye[2] = fzs;
        }
    }

    private void setFwd(double yawDeg, double pitchDeg) {
        double a = Math.toRadians(yawDeg), p = Math.toRadians(pitchDeg);
        fwd[0] = Math.sin(a) * Math.cos(p);
        fwd[1] = -Math.sin(p);
        fwd[2] = -Math.cos(a) * Math.cos(p);
    }

    public float[] view() {
        if (mode == Mode.FOLGEN) return Mat4.lookAt(eye[0], eye[1], eye[2], eye[0] + fwd[0], eye[1] + fwd[1], eye[2] + fwd[2], up[0], up[1], up[2]);
        return Mat4.lookAt(eye[0], eye[1], eye[2], eye[0] + fwd[0], eye[1] + fwd[1], eye[2] + fwd[2]);
    }

    /** Blickrichtung des laufenden Bilds als Kompasswert 0..360. */
    public double heading() {
        double h = Math.toDegrees(Math.atan2(fwd[0], -fwd[2]));
        return h < 0 ? h + 360 : h;
    }

    /** Abstand vom Auge zur Turmachse (m). */
    public double towerDistance() { return Math.hypot(eye[0], eye[2]); }
}
