package com.dan.lighthouse.test;

import com.dan.cloth.Cloth;
import com.dan.lamp.Lamp;
import com.dan.lighthouse.gl.Shader;
import com.dan.lighthouse.scene.Aussenlicht;
import com.dan.lighthouse.scene.Turm;

import java.util.Locale;

/**
 * Rückfluss aus Timmendorf (Phase 18, F29 A): die Flagge am Mast ({@code com.dan.cloth}) und die Leuchten außen
 * ({@code com.dan.lamp}, über {@code lamp.glsl} im Turm-Shader). Ohne Grafikkarte.
 */
final class PruefungTimmendorf {
    private PruefungTimmendorf() { }

    static void check(boolean c, String w) { Pruefung.check(c, w); }

    static void alles() {
        System.out.println("Rückfluss aus Timmendorf (Phase 18): Flagge und Leuchten");
        String t = Shader.source("tower.frag", null).text;
        check(t.contains("struct LampData") && t.contains("lampIrradiance(vPos, N)") && t.contains("uniform float uLampK"),
                "tower.frag nimmt lamp.glsl (Licht und Glanz der Leuchten, uLampK)");
        Aussenlicht a = new Aussenlicht();
        a.schalten(0, 20_000, 12);
        boolean tag = !a.an() && !a.hindernis.on();
        a.schalten(0, 2, 22);
        boolean nacht = a.an() && a.hindernis.on();
        check(tag && nacht, "Dämmerungsschalter: mittags aus, nachts an (Tür und Hindernisfeuer)");
        double hoch = a.tuer.y - (Turm.SOCKEL_TOP + 0.05);
        double e = a.tuer.candela(0, -1, 0) / (hoch * hoch);
        check(e > 50 && e < 500, String.format(Locale.GERMAN, "Arbeitsleuchte: %.0f lx auf dem Podest vor der Tür (%.1f m darunter)", e, hoch));
        double cd = a.hindernis.candela(1, 0, 0);
        double[] c = a.hindernis.rgb();
        check(Math.abs(cd - 32) < 1 && c[0] > 10 * c[1], String.format(Locale.GERMAN, "Hindernisfeuer: %.0f cd rundum, rot (%.2f / %.2f / %.2f)", cd, c[0], c[1], c[2]));
        // Flagge: eine Minute Wind 10 m/s aus 240°
        double rMin = 1e9, yMin = 1e9;
        for (int i = 0; i < 60 * 60; i++) {
            a.schritt(1 / 60.0, 10, 240, 0.2, 0);
            if (i < 30 * 60) continue;
            Cloth f = a.flagge;
            for (int k = 0; k < f.nu * f.nv; k++) {
                double x = f.pos[k * 3], y = f.pos[k * 3 + 1], z = f.pos[k * 3 + 2];
                rMin = Math.min(rMin, Math.hypot(x, z));
                yMin = Math.min(yMin, y);
            }
        }
        double nach = a.flaggeNach(), d = Math.abs(((nach - 60) % 360 + 540) % 360 - 180);
        check(d < 30, String.format(Locale.GERMAN, "Flagge weht nach %03.0f° (Wind aus 240°, soll 060°)", nach));
        check(rMin > Turm.LANTERN_R + 0.3 && yMin > Turm.GALLERY_TOP + 2,
                String.format(Locale.GERMAN, "Flagge frei von Laterne und Galerie: mindestens %.2f m von der Achse, tiefster Punkt %.1f m", rMin, yMin));
        check(a.flagge.maxStretch() < 0.05, String.format(Locale.GERMAN, "Fahnentuch dehnt sich höchstens %.1f %%", a.flagge.maxStretch() * 100));
    }
}
