package com.dan.lighthouse.test;

import com.dan.avatar.Climate;
import com.dan.avatar.Person;
import com.dan.avatar.Regie;
import com.dan.lighthouse.person.Galerie;
import com.dan.lighthouse.person.GalerieStage;
import com.dan.lighthouse.person.PersonKamera;
import com.dan.lighthouse.person.Personen;
import com.dan.lighthouse.scene.Turm;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Set;

import static com.dan.lighthouse.test.Pruefung.check;

/**
 * Avatar Phase 15: der Wärter auf der Galerie, ohne Grafikkarte und ohne Datenbank. Galerie (Boden, Orte, Wege, Windschatten),
 * Kamera für Menschen, das Regiebuch „Sturmnacht auf der Galerie“ (gelesen und bis zur ersten Bewegung ausgeführt), der Gang
 * um die Laterne, die Packages des Avatars und der Durchgang im Renderer.
 */
final class PruefungPersonen {
    private PruefungPersonen() { }

    static void alles() throws Exception {
        System.out.println("Personen auf der Galerie (Avatar Phase 15)");
        Galerie g = new Galerie();
        g.windFromDeg = 250;
        g.windSpeed = 19;
        g.rain = 6;
        double[] luv = g.place("Luv"), lee = g.place("Lee");
        check(Math.abs(g.ground(luv[0], luv[2]) - 23.0) < 1e-9 && Math.abs(Math.hypot(luv[0], luv[2]) - Galerie.WEG_R) < 1e-9,
                String.format(Locale.GERMAN, "Galerie: Boden auf %.1f m, Weg %.1f m um die Achse, Luv bei %.0f°", Turm.GALLERY_TOP, Galerie.WEG_R, Galerie.azimuth(luv[0], luv[2])));
        boolean around = true;
        for (int a = 90; a < 360; a += 30) around &= Galerie.wegFrei(g.place("G" + a), g.place("G" + ((a + 30) % 360)));
        double[] erker = Galerie.at(Galerie.ERKER_AZ, Galerie.ERKER_D, 0);
        check(around && !Galerie.wegFrei(g.place("G90"), g.place("G270")) && !Galerie.frei(erker[0], erker[2]) && Galerie.frei(g.place("G30")[0], g.place("G30")[2]),
                String.format(Locale.GERMAN, "Wege: zwischen Nachbarorten (30°) frei, nicht durch die Laterne und nicht durch den hohen Erker (%.0f°; G30 rückt auf %.0f°)",
                        Galerie.ERKER_AZ, Galerie.azimuth(g.place("G30")[0], g.place("G30")[2])));
        check(g.place("Reling_180") == null && g.place("Reling_75") == null && g.place("Reling_270") != null && g.place("Reling_Luv") != null,
                "Reling: nicht über den Solarpaneelen (150°–210°) und nicht am Mast (75°)");
        Climate cl = g.climateAt(luv[0], 24.6, luv[2]), ce = g.climateAt(lee[0], 24.6, lee[2]);
        check(ce.windSpeed() < 0.5 * cl.windSpeed() && ce.rain < cl.rain && cl.windSpeed() > 19,
                String.format(Locale.GERMAN, "Windschatten der Laterne: Luv %.1f m/s und %.1f mm/h, Lee %.1f m/s und %.1f mm/h", cl.windSpeed(), cl.rain, ce.windSpeed(), ce.rain));
        double[] see = g.place("See");
        check(Math.abs(Galerie.azimuth(see[0], see[2]) - 250) < 1e-6 && Math.abs(g.place("Feuer")[1] - Turm.LENS_Y) < 1e-9, "Orte nach dem Wind: See in 250°, Feuer in Höhe der Linse");

        // Kamera für Menschen: Bildwinkel je Einstellung, Auge außerhalb der Laterne
        PersonKamera k = new PersonKamera();
        check(Math.abs(k.zoom() - 55 / 26.0) < 1e-9 && PersonKamera.shot("Groß") != null && PersonKamera.shot("Totale") == null,
                "Kamera: Einstellungen Halbtotale bis Groß, Halbnah mit 26° (Vergrößerung 2,1)");

        // Der Wärter: dieselbe Person wie AV_API.WUERFELN(19)
        Person p = Person.wuerfeln(Personen.WAERTER_SEED);
        check(p.gender > 0.9 && p.ageYears() > 50 && "kurz".equals(p.hair) && p.beard > 0.5,
                String.format(Locale.GERMAN, "Wärter: seed %d ist „%s“ (kurzes Haar, Bart %.2f, grau %.2f)", Personen.WAERTER_SEED, p.name, p.beard, p.grey));

        // Regiebuch: lesen, Befehle bekannt, bis zur ersten Bewegung ausführen (ohne Körper)
        Regie rg = Regie.resource(Personen.class, "galerie_sturmnacht.txt");
        Set<String> scene = Set.of("person", "kamera", "ansicht", "rund", "wetter", "uhr", "warte");
        Set<String> actor = Set.of("gehe", "dreh", "blick", "gefuehl", "geste", "sage", "kapuze", "outfit", "pose", "halt");
        boolean known = true;
        for (Regie.Line l : rg.lines) known &= l.actor() == null ? scene.contains(l.verb()) : actor.contains(l.verb());
        check(known && rg.title.equals("Die Galerie") && rg.credits.size() >= 5 && rg.lines.size() > 35,
                "Regiebuch „Sturmnacht auf der Galerie“: " + rg.lines.size() + " Zeilen, " + rg.credits.size() + " Zeilen Abspann, alle Befehle bekannt");
        // Kreisweg: „rund“ von Lee nach Luv geht auf dem Kreis und endet im Luv
        com.dan.avatar.Motion mo = new com.dan.avatar.Motion(com.dan.avatar.Motion.line(lee[0], lee[2], 0), 0);
        double[] end = GalerieStage.rundPruefen(mo, luv);
        boolean umErker = true;
        for (int i = 0; i <= 50; i++) { double[] q = mo.path.at(mo.s + end[2] * i / 50.0); umErker &= Galerie.frei(q[0], q[1]); }
        check(umErker && Math.abs(Math.hypot(end[0], end[1]) - Galerie.WEG_R) < 1e-6 && Math.hypot(end[0] - luv[0], end[1] - luv[2]) < 0.05,
                String.format(Locale.GERMAN, "rund: Kreis um die Laterne von Lee nach Luv, am hohen Erker vorbei (%.1f m Bogen), endet %.0f mm vom Ziel", end[2], Math.hypot(end[0] - luv[0], end[1] - luv[2]) * 1000));

        // Packages des Avatars und Durchgang im Renderer
        boolean pk = true;
        for (String n : new String[]{"gltf", "avatar", "hair", "portrait"}) pk &= new File("src/com/dan/" + n + "/README.md").isFile();
        check(pk && new File("src/com/dan/avatar/res/avatar_base.glb").length() > 100_000_000L, "Packages gltf, avatar, hair, portrait als Kopie aus dem Projekt Avatar (mit Grundkörper)");
        String r = Files.readString(new File("src/com/dan/lighthouse/gl/Renderer.java").toPath(), StandardCharsets.UTF_8);
        int life = r.indexOf("drawLife(gl, st, vp, sun, pre);"), pers = r.indexOf("personen.zeichnen("), spray = r.indexOf("// 8 Gischt");
        check(life > 0 && pers > life && spray > pers && r.contains("stepPersonen(dt)"), "Renderer: Personen nach dem Leben, vor Gischt, Regen und Nebel (sie schreiben Tiefe)");
        check(r.contains("GL4.GL_NEGATIVE_ONE_TO_ONE") || Files.readString(new File("src/com/dan/lighthouse/person/Personen.java").toPath()).contains("GL_NEGATIVE_ONE_TO_ONE"),
                "Schattenkarte der Person mit Tiefe −1..1, die Szene bleibt bei umgekehrter Tiefe");
    }
}
