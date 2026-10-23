package com.dan.lighthouse.person;

import com.dan.avatar.Actor;
import com.dan.avatar.Climate;
import com.dan.avatar.Outfit;
import com.dan.avatar.Person;
import com.dan.avatar.Regie;
import com.dan.avatar.World;

import java.util.List;
import java.util.Locale;

/**
 * Die Galerie als Szene der Regie (Avatar Phase 15). Befehle mit Namen vor dem Punkt führt {@link Actor} aus, alles
 * andere landet hier:
 * <pre>
 * person  hauke Regenjacke seed=19 ort=Lee dreh=Luv nass=20     (Outfit, Würfel aus AVATAR, Ort, Blick, Minuten Regen vorab)
 * kamera  Halbnah hauke winkel=25 hoehe=-0.3                    (Halbtotale, Amerikanisch, Halbnah, Nah, Groß auf eine Person)
 * ansicht Aussen1                                               (Ansichten von Lighthouse: Aussen1, Drohne, Sturmnacht, Laternenraum …)
 * rund    hauke Luv                                             (um die Laterne gehen, auf dem kürzeren Bogen; warte rund)
 * wetter  Sturm wind=20 regen=8                                 (Lage der Wetterregie, Größen von Hand)
 * uhr     22:40                                                 (Uhrzeit der Szene)
 * warte   vorspann
 * </pre>
 * Die Szene selbst (Kamera, Wetter, Uhr, Kino) erreicht sie über {@link Szene}; so bleibt sie ohne Grafikkarte prüfbar.
 */
public final class GalerieStage implements Regie.Stage {

    /** Was die Galerie von Lighthouse braucht. */
    public interface Szene {
        /** Schnitt: im Kino nach dem Merken des Bilds, sonst gleich. */
        void schnitt(Runnable r);
        /** Kamera auf eine Person (Einstellung) oder frei. */
        void personKamera(PersonKamera k);
        /** Eine Ansicht von Lighthouse („Aussen1“ …); {@code false}, wenn unbekannt. */
        boolean ansicht(String name);
        /** Lage der Wetterregie und Größen von Hand (null = lassen). */
        boolean wetter(String lage, Double windMs, Double regenMmH);
        /** Uhrzeit der Szene (Stunden). */
        void uhr(double stunde);
        /** Monat der Szene (für das Outfit nach Monat). */
        int monat();
        /** Ist der Vorspann des Kinos vorbei? */
        boolean nachVorspann();
    }

    final Personen personen;
    final Szene szene;

    public GalerieStage(Personen personen, Szene szene) { this.personen = personen; this.szene = szene; }

    @Override public World world() { return personen.galerie; }

    @Override public boolean done(String verb) {
        return switch (verb) {
            case "vorspann" -> szene.nachVorspann();
            case "rund" -> { boolean idle = true; for (Figur f : personen.figuren) idle &= f.motion == null || f.motion.idle(); yield idle; }
            default -> true;
        };
    }

    @Override public boolean command(String verb, String args, Regie regie) {
        List<String> w = Regie.words(args);
        try {
            switch (verb) {
                case "person" -> { return person(w, regie); }
                case "kamera" -> {
                    if (w.isEmpty()) return false;
                    com.dan.portrait.Shot s = PersonKamera.shot(w.get(0));
                    if (s == null) return szene.ansicht(w.get(0));
                    Figur who = w.size() > 1 && !w.get(1).contains("=") ? figur(regie, w.get(1)) : personen.erste();
                    String a = Regie.option(w, "winkel", null), hh = Regie.option(w, "hoehe", null);
                    szene.schnitt(() -> {
                        PersonKamera k = personen.kamera;
                        k.shot = s;
                        if (who != null) k.target = who;
                        if (a != null) k.angleDeg = Double.parseDouble(a.replace(',', '.'));
                        k.lift = hh == null ? 0 : Double.parseDouble(hh.replace(',', '.'));
                        k.snap = true;
                        szene.personKamera(k);
                    });
                    return true;
                }
                case "ansicht" -> { return !w.isEmpty() && szene.ansicht(w.get(0)); }
                case "rund" -> {
                    if (w.size() < 2) return false;
                    Figur f = figur(regie, w.get(0));
                    double[] to = personen.galerie.place(w.get(1));
                    if (f == null || to == null) return false;
                    rund(f, to);
                    return true;
                }
                case "wetter" -> {
                    if (w.isEmpty()) return false;
                    String wi = Regie.option(w, "wind", null), re = Regie.option(w, "regen", null);
                    return szene.wetter(w.get(0).contains("=") ? null : w.get(0), wi == null ? null : num(wi), re == null ? null : num(re));
                }
                case "uhr" -> {
                    if (w.isEmpty()) return false;
                    String[] hm = w.get(0).split(":");
                    szene.uhr(Integer.parseInt(hm[0]) + (hm.length > 1 ? Integer.parseInt(hm[1]) / 60.0 : 0));
                    return true;
                }
                default -> { return false; }
            }
        } catch (Exception e) {
            regie.errors.add(verb + ": " + e.getMessage());
            return true;
        }
    }

    /**
     * Um die Laterne gehen (gerade Wege würden sie schneiden): der Weg wird ein Kreis um die Turmachse durch den
     * jetzigen Ort, auf dem kürzeren Bogen zum Azimut des Ziels; erst in die Laufrichtung drehen, dann gehen.
     */
    static void rund(Figur f, double[] to) { rund(f.motion(), to); }

    /** Für die Prüfung: „rund“ auf eine Bewegung anwenden; Endpunkt des Wegs {x, z} und Länge des Bogens (m). */
    public static double[] rundPruefen(com.dan.avatar.Motion mo, double[] to) {
        double len = rund(mo, to);
        double[] e = mo.path.at(mo.s + len);
        return new double[]{e[0], e[1], len};
    }

    static double rund(com.dan.avatar.Motion mo, double[] to) {
        double a0 = Math.atan2(mo.z, mo.x), a1 = Math.atan2(to[2], to[0]);
        double d = Math.atan2(Math.sin(a1 - a0), Math.cos(a1 - a0));
        // nicht durch den hohen Erker: liegt er auf dem kürzeren Bogen, den längeren nehmen
        double rr = Math.hypot(mo.x, mo.z);
        for (int i = 1; i < 40; i++) {
            double a = a0 + d * i / 40.0;
            if (!Galerie.frei(Math.cos(a) * rr, Math.sin(a) * rr)) { d = d > 0 ? d - 2 * Math.PI : d + 2 * Math.PI; break; }
        }
        boolean ccw = d > 0;
        double r = Math.max(Galerie.WAND_R + 0.35, Math.min(3.0, Math.hypot(mo.x, mo.z)));
        double sg = ccw ? 1 : -1;
        mo.path = com.dan.avatar.Motion.circle(0, 0, r, ccw);
        mo.s = sg * r * a0;
        double tx = -Math.sin(a0) * sg, tz = Math.cos(a0) * sg;
        mo.then(com.dan.avatar.Motion.turnTo(mo.x + tx, mo.z + tz));
        mo.then(com.dan.avatar.Motion.walk(Math.abs(d) * r));
        return Math.abs(d) * r;
    }

    static double num(String s) { return Double.parseDouble(s.replace(',', '.')); }

    /** {@code person <name> [Outfit] [seed=N] [rolle=Wärter] [ort=Ort] [dreh=Ort] [nass=Minuten]}. */
    private boolean person(List<String> w, Regie regie) {
        if (w.isEmpty()) return false;
        String name = w.get(0);
        long seed = Long.parseLong(Regie.option(w, "seed", String.valueOf(Personen.WAERTER_SEED)));
        Person p = personen.person(seed).copy();
        p.name = Character.toUpperCase(name.charAt(0)) + name.substring(1) + ", " + Math.round(p.ageYears());
        Outfit o = null;
        for (int i = 1; i < w.size(); i++) {
            String s = w.get(i);
            if (s.contains("=")) continue;
            for (String pr : Outfit.PRESETS) if (pr.equalsIgnoreCase(s)) o = Outfit.preset(pr);
        }
        if (o == null) {
            // ohne Angabe: was die Rolle in diesem Monat trägt (wie AV_API.OUTFIT)
            String rolle = Regie.option(w, "rolle", "Wärter");
            o = Outfit.forMonth(szene.monat(), rolle.toLowerCase(Locale.ROOT).startsWith("w") ? Outfit.Role.DRAUSSEN : Outfit.Role.ALLTAG, Outfit.Epoch.HEUTE, seed);
        }
        String kap = Regie.option(w, "kapuze", null);
        if (kap != null) o = o.withHood(kap.equalsIgnoreCase("auf"));
        World wo = personen.galerie;
        double[] at = place(wo, Regie.option(w, "ort", null), Galerie.at(250, Galerie.WEG_R, 0));
        double[] to = place(wo, Regie.option(w, "dreh", null), null);
        double yaw = to == null ? Math.atan2(at[0], at[2]) : Math.atan2(to[0] - at[0], to[2] - at[2]);
        if (!personen.bereit()) personen.warten();
        Figur f = personen.neu(p, o, at[0], at[2], yaw);
        Actor a = new Actor(name, f);
        f.actor = a;
        regie.cast.put(name, a);
        double min = Double.parseDouble(Regie.option(w, "nass", "0").replace(',', '.'));
        if (min > 0) {
            Climate c = personen.galerie.climateAt(at[0], at[1] + 1.6, at[2]);
            if (o.hoodUp()) c.hood = 1;
            for (int i = 0; i < min * 60; i++) f.wetness.step(1, c);
        }
        return true;
    }

    static double[] place(World wo, String n, double[] def) {
        if (n == null) return def;
        double[] p = wo.place(n);
        return p == null ? def : p;
    }

    Figur figur(Regie regie, String name) {
        Actor a = regie.cast.get(name);
        if (a == null) for (var e : regie.cast.entrySet()) if (e.getKey().equalsIgnoreCase(name)) a = e.getValue();
        return a != null && a.host instanceof Figur f ? f : null;
    }
}
