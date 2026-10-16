package com.dan.lighthouse.test;

import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.tools.Symbol;
import com.dan.lighthouse.ui.AppIcon;
import com.dan.ocean.Ocean;
import com.dan.soundscape.Soundscape;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dan.lighthouse.test.Pruefung.check;

/** Phase 16: Schliff. Symbol, Verknüpfung, READMEs und Bilder, Quellen, Qualitätsstufen, Regelung der Auflösung, Dauerlauf. */
final class PruefungSchliff {
    private PruefungSchliff() { }

    static void alles() throws Exception {
        System.out.println("Schliff: Programmsymbol und Verknüpfung");
        File ico = new File("icon/lighthouse.ico");
        check(ico.isFile(), "icon/lighthouse.ico vorhanden");
        int[] s = Symbol.icoGroessen(ico);
        check(Arrays.equals(s, AppIcon.SIZES), "ICO mit PNG-Einträgen in den Größen " + Arrays.toString(s));
        boolean pngs = true;
        for (int n : AppIcon.SIZES) pngs &= new File("src/com/dan/lighthouse/ui/icon/lighthouse_" + n + ".png").isFile() && AppIcon.load(n).getWidth() == n;
        check(pngs, "PNGs des Symbols im Jar, AppIcon.load liefert jede Größe");
        java.awt.image.BufferedImage ic = AppIcon.load(64);
        int edge = ic.getRGB(32, 1);
        check(((edge >> 16) & 255) > 150 && ((edge >> 8) & 255) > 100 && (edge & 255) < 150, "Goldkante am Rand des Symbols");
        String ps1 = Files.readString(new File("Verknuepfung.ps1").toPath(), StandardCharsets.UTF_8);
        check(ps1.contains("dist\\Lighthouse.jar") && ps1.contains("icon\\lighthouse.ico") && ps1.contains("CreateShortcut") && ps1.contains("WorkingDirectory"),
                "Verknuepfung.ps1 legt die Verknüpfung auf das Jar mit Symbol und Arbeitsordner");
        String bat = Files.readString(new File("Verknuepfung.bat").toPath(), StandardCharsets.UTF_8);
        check(bat.contains("Verknuepfung.ps1") && bat.contains("ExecutionPolicy Bypass") && bat.contains("\r\n"), "Verknuepfung.bat ruft das Skript (CRLF)");
        check(new File("Lighthouse.bat").isFile() && Files.readString(new File("Lighthouse.bat").toPath()).contains("dist\\Lighthouse.jar"), "Lighthouse.bat startet das Jar");

        System.out.println("Schliff: READMEs, Bilder, Quellen");
        List<String> ohne = new ArrayList<>();
        File[] pk = new File("src/com/dan").listFiles(File::isDirectory);
        int n = 0;
        if (pk != null) for (File p : pk) {
            File[] java = p.listFiles((d, f) -> f.endsWith(".java"));
            if (java == null || java.length == 0) continue;
            n++;
            if (!new File(p, "README.md").isFile()) ohne.add(p.getName());
        }
        check(n >= 19 && ohne.isEmpty(), "README.md in allen " + n + " Packages" + (ohne.isEmpty() ? "" : "; fehlt: " + ohne));
        check(new File("src/com/dan/lighthouse/scene/README.md").isFile() && new File("src/com/dan/lighthouse/wiring/README.md").isFile() && new File("db/README.md").isFile(),
                "READMEs für scene, wiring und db");
        String readme = Files.readString(new File("README.md").toPath(), StandardCharsets.UTF_8);
        List<String> kaputt = new ArrayList<>();
        int links = 0;
        Matcher m = Pattern.compile("\\]\\(([^)#]+)\\)").matcher(readme);
        while (m.find()) {
            String t = m.group(1);
            if (t.startsWith("http")) continue;
            links++;
            if (!new File(t).isFile()) kaputt.add(t);
        }
        check(links >= 25 && kaputt.isEmpty(), "GitHub-README: " + links + " Verweise auf Dateien im Projekt, alle vorhanden" + (kaputt.isEmpty() ? "" : "; fehlt: " + kaputt));
        int bilder = 0;
        Matcher b = Pattern.compile("!\\[[^\\]]*\\]\\(docs/bilder/([^)]+)\\)").matcher(readme);
        while (b.find()) bilder++;
        File[] bf = new File("docs/bilder").listFiles((d, f) -> f.endsWith(".jpg") || f.endsWith(".png"));
        long bytes = 0;
        if (bf != null) for (File f : bf) bytes += f.length();
        check(bilder >= 10 && bf != null && bf.length >= bilder && bytes < 6_000_000, String.format(Locale.GERMAN, "%d Bilder in der README aus docs/bilder (%d Dateien, %.1f MB)", bilder, bf == null ? 0 : bf.length, bytes / 1e6));
        for (String p : new String[]{"com.dan.ocean", "com.dan.sky", "com.dan.cloud", "com.dan.weathering", "com.dan.spray", "com.dan.rain", "com.dan.wetglass", "com.dan.beacon",
                "com.dan.lightning", "com.dan.weather", "com.dan.fog", "com.dan.buoyancy", "com.dan.flock", "com.dan.soundscape"})
            if (!readme.contains("`" + p + "`")) kaputt.add(p);
        check(kaputt.isEmpty(), "Alle 14 Element-Packages stehen in der README");
        String q = Files.readString(new File("docs/QUELLEN.md").toPath(), StandardCharsets.UTF_8);
        long eintraege = q.lines().filter(z -> z.startsWith("- ")).count();
        check(eintraege >= 20 && q.contains("[G]") && q.contains("deutsche-leuchtfeuer.de") && q.contains("Hillaire"), "docs/QUELLEN.md mit " + eintraege + " Einträgen");

        System.out.println("Schliff: Qualitätsstufen durchgängig");
        boolean steigt = true;
        Quality[] qs = Quality.values();
        for (int i = 1; i < qs.length; i++) {
            Quality a = qs[i - 1], c = qs[i];
            steigt &= c.ocean.ordinal() > a.ocean.ordinal() && c.weathering.ordinal() > a.weathering.ordinal() && c.spray.ordinal() > a.spray.ordinal()
                    && c.rain.ordinal() > a.rain.ordinal() && c.wetglass.ordinal() > a.wetglass.ordinal() && c.beacon.ordinal() > a.beacon.ordinal()
                    && c.lightning.ordinal() > a.lightning.ordinal() && c.fog.ordinal() > a.fog.ordinal() && c.buoyancy.ordinal() > a.buoyancy.ordinal()
                    && c.flock.ordinal() > a.flock.ordinal() && c.sound.ordinal() > a.sound.ordinal() && c.clouds.ordinal() >= a.clouds.ordinal();
        }
        check(steigt, "Jede Stufe hebt die Stufe aller zwölf Packages (Meer, Wolken, Verwitterung, Gischt, Regen, Scheiben, Feuer, Blitz, Nebel, Schwimmkörper, Möwen, Klang)");
        Renderer r = PruefungDaten.renderer();
        r.setQuality(Quality.NIEDRIG);
        check(r.ocean.quality == Ocean.Quality.LOW && r.soundscape.quality == Soundscape.Quality.LOW && r.fog.quality == com.dan.fog.Fog.Quality.LOW
                && r.flock.quality == com.dan.flock.Flock.Quality.LOW && r.buoyancy.quality == com.dan.buoyancy.Buoyancy.Quality.LOW && r.renderScale == Quality.NIEDRIG.renderScale,
                "setQuality reicht die Stufe bis in Meer, Nebel, Möwen, Schwimmkörper und Klang");
        check(Quality.HOCH.summary().contains("Klang 44,1 kHz") && Quality.ULTRA.summary().contains("Meer 4"), "Quality.summary nennt alle Teile: " + Quality.HOCH.summary().substring(0, 60) + " …");

        System.out.println("Schliff: Auflösung regelt sich selbst");
        r.setQuality(Quality.HOCH);
        r.autoScale = true;
        r.targetFps = 60;
        long t = 10_000_000_000L;
        r.regulateForTest(30, t);
        double s1 = r.renderScale;
        check(s1 < 1.0 && s1 >= 0.85, String.format(Locale.GERMAN, "Grafikkarte 30 ms bei Ziel 60: Auflösung sinkt von 100 auf %.0f %% (höchstens 15 Punkte je Schritt)", s1 * 100));
        r.regulateForTest(30, t + 1_000_000_000L);
        check(r.renderScale == s1, "Innerhalb von 1,5 s wird nicht noch einmal geregelt");
        for (int i = 0; i < 20; i++) r.regulateForTest(40, t + (i + 2) * 2_000_000_000L);
        check(r.renderScale == Renderer.MIN_SCALE, String.format(Locale.GERMAN, "Bei dauerhaft 40 ms bleibt sie bei %.0f %% stehen, nie darunter", Renderer.MIN_SCALE * 100));
        int steps = r.scaleSteps();
        for (int i = 0; i < 30; i++) r.regulateForTest(6, t + (i + 30) * 2_000_000_000L);
        check(r.renderScale == Quality.HOCH.renderScale, "Mit 6 ms steigt sie in Schritten von 5 % wieder auf die Stufe (100 %)");
        for (int i = 0; i < 5; i++) r.regulateForTest(14, t + (i + 70) * 2_000_000_000L);
        check(r.renderScale == Quality.HOCH.renderScale && r.scaleSteps() > steps, "Bei 14 ms (im Budget) bleibt sie stehen; " + r.scaleSteps() + " Änderungen gezählt");
        r.autoScale = false;
        r.renderScale = 0.7;
        r.regulateForTest(40, t + 200_000_000_000L);
        check(r.renderScale == 0.7, "Ausgeschaltet regelt nichts, der Regler gilt");

        System.out.println("Schliff: Befunde aus den Statusbildern");
        double leise = Ocean.surfRing(0.2, 0.3, 1, 1, 0), mittel = Ocean.surfRing(0.2, 1.0, 1, 1, 0), stark = Ocean.surfRing(0.2, 3.0, 1, 1, 0);
        check(leise < 0.06 && mittel > 0.2 && stark > 0.5 && leise < mittel && mittel < stark,
                String.format(Locale.GERMAN, "Brandungsring am Sockel: Hs 0,3 m → %.0f %%, 1 m → %.0f %%, 3 m → %.0f %% (Bft 1 nur ein Saum)", leise * 100, mittel * 100, stark * 100));
        Soundscape sc = Soundscape.beaufort(5);
        float[] buf = new float[2048];
        for (int i = 0; i < 200; i++) sc.render(buf, 1024);
        double last = sc.msPerSecond();
        check(last > 0 && last < 400, String.format(Locale.GERMAN, "Rechenzeit des Klangs als gleitendes Mittel über die letzten Sekunden: %.1f %% eines Kerns (vorher der Mittelwert seit dem Start samt Aufwärmen)", last / 10));

        File dl = new File("docs/status/Dauerlauf_claude.txt");
        if (dl.isFile()) {
            List<String> z = Files.readAllLines(dl.toPath(), StandardCharsets.UTF_8);
            String letzte = z.isEmpty() ? "" : z.get(z.size() - 1);
            check(letzte.equals("Dauerlauf bestanden"), "docs/status/Dauerlauf_claude.txt endet mit „Dauerlauf bestanden“ (" + z.size() + " Zeilen)");
        } else {
            check(true, "Kein Dauerlauf-Protokoll (tools.Dauerlauf läuft nicht in der Prüfung)");
        }
    }
}
