package com.dan.lighthouse.test;

import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.scene.Sockelbrandung;
import com.dan.lighthouse.scene.Turm;
import com.dan.lighthouse.wiring.Wiring;
import com.dan.ocean.Obstacle;
import com.dan.ocean.Ocean;
import com.dan.surf.Surf;
import com.dan.surf.WaveTrain;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Phase 17 (Rückfluss aus Sylt, F22 A): Lighthouse nimmt {@code lib/Renderer.jar} statt der Kopien von 21 Packages, und
 * die Brandung am Sockel kommt aus {@code com.dan.surf} ({@link Sockelbrandung}). Ohne Grafikkarte.
 */
final class PruefungRueckfluss {
    private PruefungRueckfluss() { }

    static final Set<String> EIGENE = Set.of("avatar", "gltf", "hair", "lighthouse", "portrait", "voice");

    static void check(boolean c, String w) { Pruefung.check(c, w); }

    static void alles() throws Exception {
        System.out.println("Phase 17: Renderer.jar statt der Kopien (Sylt F22 A)");
        Set<String> da = new TreeSet<>();
        File[] pk = new File("src/com/dan").listFiles(File::isDirectory);
        if (pk != null) for (File f : pk) da.add(f.getName());
        check(da.equals(new TreeSet<>(EIGENE)), "src/com/dan hat nur noch die eigenen Packages " + da);
        check(new File("lib/Renderer.jar").isFile() || System.getProperty("java.class.path").contains("Renderer.jar"), "lib/Renderer.jar liegt bei");
        String cp = Files.readString(new File("nbproject/project.properties").toPath(), StandardCharsets.UTF_8);
        int r = cp.indexOf("${file.reference.Renderer.jar}"), f = cp.indexOf("${file.reference.FStyle.jar}", cp.indexOf("javac.classpath"));
        check(cp.contains("file.reference.Renderer.jar=lib\\\\Renderer.jar") && r > 0 && r < f, "Renderer.jar steht in javac.classpath vor FStyle (Klassenpfad von Bau, Lauf und Prüfung)");
        String quelle = Ocean.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        check(quelle.endsWith("Renderer.jar"), "com.dan.ocean kommt aus dem Jar (" + new File(quelle).getName() + ")");
        String bx = Files.readString(new File("build.xml").toPath(), StandardCharsets.UTF_8);
        check(bx.contains("tools/JarPruefung.java") && bx.contains("tools/PaketPruefung.java") && new File("tools/JarPruefung.java").isFile(),
                "check-packages prüft lib/Renderer.jar (JarPruefung) und die eigenen Packages einzeln (PaketPruefung)");
        File ps = new File("docs/status/Pruefsummen_Phase17.txt");
        String pst = ps.isFile() ? Files.readString(ps.toPath(), StandardCharsets.UTF_8) : "";
        check(pst.contains("21 Packages") && pst.contains("spray/gl/SprayGl.java") && pst.contains("0 nur Lighthouse"),
                "Prüfsummen vorher: docs/status/Pruefsummen_Phase17.txt (21 Packages, keine Datei nur in Lighthouse)");
        // die Änderung aus Avatar-Phase 19 ist im Renderer
        boolean poll = Arrays.stream(com.dan.spray.gl.SprayGl.class.getMethods()).anyMatch(m -> m.getName().equals("selfCheckPoll"));
        check(poll, "Rückgeführt: SprayGl.selfCheckStart/selfCheckPoll (Abgleich über Pufferspeicher mit Zaun, Avatar-Phase 19)");
        check(com.dan.beacon.Beacon.class.getResource("/com/dan/sky/milkyway.png") != null, "Milchstraße (sky/milkyway.png) im Jar");

        System.out.println("Phase 17: der Brandungsring bleibt für Pfahl, Tonnen und Schiffe");
        Obstacle o = new Obstacle(1, 2, 3);
        check(o.ring() && !new Obstacle(1, 2, 3, false).ring(), "Obstacle(x, z, r) mit Ring wie bisher, Obstacle(…, false) ohne");
        check(Pruefung.ressource("/com/dan/ocean/gl/surface.frag").contains("uObst[i].w * smoothr"), "surface.frag: der Ring je Körper mit seinem Schalter (uObst.w)");
        Wiring w = new Wiring(new SceneClock(LocalDate.of(2026, 10, 15), 13.0));
        w.tide.on = false;
        w.tide.fixed = -1.0;
        Ocean oc = new Ocean();
        w.feedObstacles(oc, false, false);
        List<Obstacle> obs = oc.obstacles();
        long ohne = obs.stream().filter(b -> !b.ring()).count(), mit = obs.stream().filter(Obstacle::ring).count();
        check(obs.size() == 5 && ohne == 4 && mit == 1 && obs.get(4).radius() == 0.35,
                "Sockel und Manschette ohne Ring (" + ohne + "), der Peilpfahl mit (" + mit + ")");
        w.feedObstacles(oc, true, true);
        check(oc.obstacles().stream().allMatch(Obstacle::ring), "Brandung am Sockel aus: alle Körper wieder mit Ring (wie bis Phase 16)");

        System.out.println("Phase 17: Brandung am Sockel (com.dan.surf, Surf.sockel())");
        Surf so = Surf.sockel();
        check(so.hs == 2.0 && so.tp == 9 && so.seaFromDeg == 300, "Surf.sockel(): Nordsee draußen 2 m, 9 s, aus 300° (Vorgabe, die App setzt die See)");
        double g0 = Sockelbrandung.grund(0, 0), g1 = Sockelbrandung.grund(6.0, 0), gr = Sockelbrandung.grund(0, Turm.PLATE_B * 1.3),
                gs = Sockelbrandung.grund(0, 40);
        check(g0 == Turm.PLATE_TOP && g1 == Turm.PLATE_TOP && gr < -1.2 && gr > -2.6 && gs == Sockelbrandung.SEEGRUND,
                String.format(Locale.GERMAN, "Grund: im Sockel und auf der Manschette %.1f m, Steinschüttung %.2f m, Seegrund %.1f m", g0, gr, gs));
        boolean mono = true;
        for (double z = Turm.PLATE_B * 1.05; z < 40; z += 0.5) if (Sockelbrandung.grund(0, z + 0.5) > Sockelbrandung.grund(0, z) + 1e-9) mono = false;
        check(mono, "Grund fällt von der Manschette über Schüttung und Böschung stetig zum Seegrund");
        check(Math.abs(Sockelbrandung.hsRand(9, 0.3) - 0.55 * 7.3) < 1e-9 && Sockelbrandung.hsRand(1, 0) == 1,
                "H_s am Rand des Stücks tiefenbegrenzt (Bft 10: 9 m draußen → 4,0 m in 7,3 m Wasser), kleine See unverändert");

        // WaveTrain: ohne Grund je Reihe genau wie bisher
        WaveTrain a = new WaveTrain(8, 0, 80, 20), b = new WaveTrain(8, 0, 80, 20);
        a.profile = x -> -5 + x / 16;
        b.profile = a.profile;
        b.rowProfile = (x, rr) -> -5 + x / 16;
        for (WaveTrain t : List.of(a, b)) { t.hs = 1.2; t.tp = 8; }
        double diff = 0;
        for (int i = 0; i < 30 * 40; i++) {
            a.step(1 / 30.0);
            b.step(1 / 30.0);
            for (int k = 0; k < Math.min(a.waves.size(), b.waves.size()); k++)
                for (int rr = 0; rr < 8; rr++) diff = Math.max(diff, Math.abs(a.heightRow(a.waves.get(k), rr) - b.heightRow(b.waves.get(k), rr)));
        }
        check(diff < 1e-3 && a.spawned == b.spawned && Arrays.equals(a.brokenByType, b.brokenByType),
                String.format(Locale.GERMAN, "WaveTrain: Grund je Reihe gleich dem gemeinsamen gibt dieselben Wellen (Abweichung %.1e m, die Walze merkt sich die flachste Stelle)", diff));
        // eine Reihe mit Bauwerk: trocken ruht, hinter der Schüttung bleibt die Walze niedrig
        WaveTrain c = new WaveTrain(2, 0, 60, 10);
        c.profile = x -> -7;
        c.rowProfile = (x, rr) -> rr == 0 ? -7 : (x > 25 && x < 30 ? 2 : x > 18 && x < 34 ? -1.0 : -7);
        c.hs = 2.5;
        c.tp = 8;
        c.slopeBase = 3;
        double lee0 = 0, lee1 = 0;
        for (int i = 0; i < 30 * 90; i++) {
            c.step(1 / 30.0);
            for (WaveTrain.Wave wv : c.waves) if (wv.x > 40 && wv.x < 50) { lee0 = Math.max(lee0, c.heightRow(wv, 0)); lee1 = Math.max(lee1, c.heightRow(wv, 1)); }
        }
        int gebrochen = 0;
        for (int k : c.brokenByType) gebrochen += k;
        check(gebrochen > 0 && lee1 < 0.6 && lee0 > 1.0,
                String.format(Locale.GERMAN, "Reihe über einer Schüttung bricht (%d Brecher), dahinter bleibt sie niedrig (%.2f m gegen %.2f m in der freien Reihe)", gebrochen, lee1, lee0));

        // ein Stück am Sockel bauen (Bft 7, Mittelwasser, See aus 240°)
        Sockelbrandung sb = new Sockelbrandung();
        sb.reihen = 24;
        sb.see = (x, z) -> 0.0;
        long t0 = System.nanoTime();
        sb.jetzt(0.0, 4.0, 8.3, 240);
        double ms = (System.nanoTime() - t0) / 1e6;
        Sockelbrandung.Stueck st = sb.stueck();
        check(st != null && st.netz.vertexCount > 0 && st.zug.rows == 24, String.format(Locale.GERMAN, "Stück gebaut und %.0f s eingeschwungen in %.0f ms: %d Ecken", Sockelbrandung.EINSCHWINGEN, ms, st == null ? 0 : st.netz.vertexCount));
        for (int i = 0; i < 30 * 30; i++) sb.schritt(1 / 30.0, 0.0, 4.0, 8.3, 240);
        int[] bt = st.zug.brokenByType;
        int steil = bt[Surf.Breaker.COLLAPSING.ordinal()] + bt[Surf.Breaker.SURGING.ordinal()] + bt[Surf.Breaker.PLUNGING.ordinal()];
        check(st.brecher > 2 && steil >= bt[Surf.Breaker.SPILLING.ordinal()],
                "Auf der steilen Schüttung brechen die Wellen stürzend oder als Reflexion, kaum schäumend: " + sb.meldung.replaceAll(" · Rechnen.*", ""));
        double[] p = st.welt(Sockelbrandung.VOR, Sockelbrandung.HALB);
        check(Math.hypot(p[0], p[1]) < 1e-9, "Die Mitte des Stücks liegt im Turm");
        double[] lu = st.lokal(10, -7);
        double[] back = st.welt(lu[0], lu[1]);
        check(Math.abs(back[0] - 10) < 1e-9 && Math.abs(back[1] + 7) < 1e-9 && st.ux > 0.8 && st.uz < -0.4,
                "Lage im Stück hin und zurück; die See aus 240° läuft nach 60° (x Ost, z Süd)");
        double[][] loch = sb.loch();
        check(loch != null && loch[2][0] > 20 && loch[2][2] > 5, "Loch für die See beschrieben (die App zeichnet ohne Loch, das Netz zeigt sein weißes Wasser über der See)");
        sb.an = false;
        check(sb.stueck() == null, "Brandung am Sockel aus: kein Stück, der Ring aus ocean übernimmt");
        String rs = Files.readString(new File("src/com/dan/lighthouse/gl/Renderer.java").toPath(), StandardCharsets.UTF_8);
        check(rs.contains("feedObstacles(ocean, plateDry, !brandungAn())") && rs.contains("spray.impact(") && rs.contains("SoundEvent.Kind.BRECHER") && rs.contains("BRANDUNG_REIHEN"),
                "Verdrahtung: Ring aus, Lippen werfen Gischt, Brecher klingen, Reihen je Stufe (24, 32, 40, 56)");
    }
}
