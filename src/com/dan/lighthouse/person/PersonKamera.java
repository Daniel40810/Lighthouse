package com.dan.lighthouse.person;

import com.dan.lighthouse.scene.Turm;
import com.dan.portrait.Shot;

import java.util.Locale;

/**
 * Kamera für Menschen in Lighthouse (Avatar Phase 15): die Einstellungen Halbtotale, Amerikanisch, Halbnah, Nah und
 * Groß aus {@code com.dan.portrait.Shot} auf eine Person, mit einem Winkel zu ihrer Blickrichtung; sie folgt der Person
 * weich (Ort 0,3 s, Richtung 1,6 s), wie {@code CameraRig} im Avatar Studio. Sie ist eine {@code Camera.Pose} für den
 * Modus FOLGEN der Lighthouse-Kamera; den Bildwinkel stellt sie über die Vergrößerung des Renderers ein.
 * Das Auge bleibt außerhalb der Laterne (mindestens 2,3 m von der Achse).
 */
public final class PersonKamera implements com.dan.lighthouse.camera.Camera.Pose {

    public static final String[] NAMES = {"Halbtotale", "Amerikanisch", "Halbnah", "Nah", "Groß"};

    public volatile Figur target;
    public volatile Shot shot = Shot.HALBNAH;
    /** Winkel zur Blickrichtung der Person (Grad, 0 = von vorn). */
    public volatile double angleDeg = 20;
    /** Höhe der Kamera über den Augen der Person (m; negativ = von unten, Blick hinauf ins Licht). */
    public volatile double lift = 0;
    /** Im Kino: Balken verdecken ein Viertel, dieselbe Einstellung braucht mehr Abstand. */
    public volatile boolean kino;
    public volatile boolean snap = true;
    public double tauPos = 0.30, tauYaw = 1.6;

    private double tx, ty, tz, yaw;
    private long last;

    /** Bildwinkel je Einstellung (Grad), wie im Studio. */
    public static double fov(Shot s) {
        return switch (s) { case GROSS -> 18; case NAH -> 22; case HALBNAH -> 26; case AMERIKANISCH -> 30; case HALBTOTALE -> 34; };
    }

    /** Einstellung nach Namen; {@code null}, wenn unbekannt. */
    public static Shot shot(String name) {
        String n = name.toLowerCase(Locale.ROOT).replace("ß", "ss");
        return switch (n) {
            case "halbtotale" -> Shot.HALBTOTALE;
            case "amerikanisch" -> Shot.AMERIKANISCH;
            case "halbnah" -> Shot.HALBNAH;
            case "nah" -> Shot.NAH;
            case "gross" -> Shot.GROSS;
            default -> null;
        };
    }

    /** Vergrößerung für den Renderer (55° geteilt durch den Bildwinkel der Einstellung). */
    public double zoom() { return com.dan.lighthouse.camera.Camera.FOVY / fov(shot); }

    @Override public double[][] pose() {
        Figur f = target;
        if (f == null) return null;
        double[] head = f.head();
        // noch nicht gerechnet (erstes Bild nach dem Anlegen): Kopf ungefähr über den Füßen
        if (head == null) head = new double[]{f.x, f.groundY > 0 ? f.groundY + 1.62 : com.dan.lighthouse.scene.Turm.GALLERY_TOP + 1.62, f.z};
        long now = System.nanoTime();
        double dt = last == 0 ? 0 : (now - last) / 1e9;
        last = now;
        boolean sn = snap || dt <= 0 || dt > 0.5;
        snap = false;
        double kp = sn ? 1 : 1 - Math.exp(-dt / tauPos), ky = sn ? 1 : 1 - Math.exp(-dt / tauYaw);
        Shot s = shot;
        double k = kino ? com.dan.kino.Kino.BREITWAND * 9.0 / 16 : 1;
        com.dan.avatar.Motion mo = f.motion;
        double yawF = mo == null ? 0 : Math.toDegrees(mo.yaw);
        double want = yawF + angleDeg;
        double lead = mo == null ? 0 : mo.speed * tauPos;
        double ax = head[0] + (mo == null ? 0 : Math.sin(mo.yaw) * lead), az = head[2] + (mo == null ? 0 : Math.cos(mo.yaw) * lead);
        double wy = head[1] + 0.08 + s.offsetY * k;
        if (sn) { tx = ax; ty = wy; tz = az; yaw = want; }
        else {
            tx += (ax - tx) * kp; ty += (wy - ty) * kp; tz += (az - tz) * kp;
            yaw += (((want - yaw) % 360 + 540) % 360 - 180) * ky;
        }
        double d = s.distance(fov(s)) * k;
        double up = lift + Math.sin(Math.toRadians(s == Shot.HALBTOTALE ? 5 : 2)) * d;
        // Auge vor der Person in ihrer Blickrichtung (+z der Figur ist Richtung yaw: sin, cos). Verdeckt die Laterne den
        // Blick, nimmt sie den gespiegelten Winkel; geht auch das nicht, steht sie auf der Linie von der Turmachse durch
        // die Person nach außen (dann steht das Feuer hinter der Person).
        double[] e = eye(yaw, d, up);
        if (clearance(e) < Turm.LANTERN_R + 0.35) {
            double[] m = eye(2 * yawF - yaw, d, up);
            if (clearance(m) > clearance(e)) e = m;
        }
        if (clearance(e) < Turm.LANTERN_R + 0.35) {
            double r = Math.max(1e-6, Math.hypot(tx, tz));
            e = new double[]{tx + tx / r * d, ty + up, tz + tz / r * d};
        }
        double ex = e[0], ey = e[1], ez = e[2];
        double fx = tx - ex, fy = ty - ey, fz = tz - ez, l = Math.sqrt(fx * fx + fy * fy + fz * fz);
        return new double[][]{{ex, ey, ez}, {fx / l, fy / l, fz / l}, {0, 1, 0}};
    }

    private double[] eye(double yawDeg, double d, double up) {
        double a = Math.toRadians(yawDeg);
        return new double[]{tx + Math.sin(a) * d, ty + up, tz + Math.cos(a) * d};
    }

    /**
     * Wie frei die Sichtlinie Auge → Ziel ist, als Abstand von der Turmachse (m): kleinster Abstand zur Achse, oder,
     * wenn der hohe Erker näher kommt, dessen Rand umgerechnet auf die Laterne; über der Kuppel unendlich.
     */
    private double clearance(double[] e) {
        if (e[1] > Turm.DOME_TOP + 0.5 && ty > Turm.DOME_TOP + 0.5) return 1e9;
        double dx = tx - e[0], dz = tz - e[2], l2 = dx * dx + dz * dz;
        double t = l2 < 1e-9 ? 0 : Math.max(0, Math.min(1, -(e[0] * dx + e[2] * dz) / l2));
        double axis = Math.hypot(e[0] + t * dx, e[2] + t * dz);
        if (Math.min(e[1], ty) < Galerie.ERKER_TOP + 0.6) {
            double[] k = Galerie.at(Galerie.ERKER_AZ, Galerie.ERKER_D, 0);
            double te = l2 < 1e-9 ? 0 : Math.max(0, Math.min(1, ((k[0] - e[0]) * dx + (k[2] - e[2]) * dz) / l2));
            double er = Math.hypot(e[0] + te * dx - k[0], e[2] + te * dz - k[2]);
            axis = Math.min(axis, Turm.LANTERN_R + (er - Galerie.ERKER_R));
        }
        return axis;
    }

    @Override public String toString() {
        return shot.label + String.format(Locale.GERMANY, " · %.0f°", angleDeg);
    }
}
