import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Prüfung von {@code lib/Renderer.jar} (Ant-Target {@code check-packages}, seit Phase 17 nach Sylt F22 A, wie in Sylt und
 * Brocken). Die Element-Packages aus der Natur liegen nicht mehr im Projekt Lighthouse: ihre Quelle ist das Projekt
 * Renderer (Paketvertrag Punkt 11), dort prüft {@code tools/PaketPruefung.java} jedes Package einzeln (Punkt 9). Hier
 * wird nur geprüft, ob das Jar alles hat, was Lighthouse braucht: jedes Package mit Klassen und README, Klassen für
 * Java 21 (Version 65) oder älter, dazu die Teile, die Phase 17 neu nimmt (die Brandung am Sockel aus {@code surf}, der
 * Körper ohne Brandungsring aus {@code ocean}, der Abgleich der Gischt über einen Pufferspeicher) und das Feuer
 * ({@code beacon}, seit Sylt F18 im Renderer). Die eigenen Packages (avatar, gltf, hair, portrait, voice) prüft weiter
 * {@code tools/PaketPruefung.java}. Aufruf ohne Ant: {@code java tools/JarPruefung.java lib/Renderer.jar}.
 */
public final class JarPruefung {
    /** Was Lighthouse aus Renderer.jar braucht: die 21 Packages, die bis Phase 16 als Kopie in src lagen, und surf. */
    static final String[] GEBRAUCHT = {"beacon", "buoyancy", "cloud", "flock", "fog", "forest", "ground", "kino", "lightning", "muster", "ocean",
            "rain", "river", "road", "sky", "soundscape", "spray", "tide", "weather", "weathering", "wetglass", "surf"};
    static final String[][] SHADER = {
            {"surf", "Surf.class", "WaveTrain.class", "WaveTrain$RowProfile.class", "BreakerMesh.class", "gl/BreakerGl.class", "gl/breaker.vert", "gl/breaker.frag", "demo/SurfCheck.class"},
            {"ocean", "Obstacle.class", "gl/OceanGl.class", "gl/surface.frag"},
            {"spray", "gl/SprayGl.class"},
            {"sky", "milkyway.png", "stars.bin"},
            {"beacon", "Beacon.class", "Characteristic.class", "SectorTable.class", "gl/BeaconGl.class", "gl/beacon.glsl", "gl/beam.frag", "demo/BeaconCheck.class"}};

    public static void main(String[] a) throws Exception {
        File jar = new File(a.length > 0 ? a[0] : "lib/Renderer.jar");
        if (!jar.isFile()) { System.err.println("check-packages: " + jar + " fehlt"); System.exit(1); }
        Map<String, int[]> p = new TreeMap<>();   // Klassen, README, höchste Version
        try (ZipFile z = new ZipFile(jar)) {
            for (ZipEntry e : java.util.Collections.list(z.entries())) {
                String n = e.getName();
                if (!n.startsWith("com/dan/") || e.isDirectory()) continue;
                String[] t = n.split("/");
                if (t.length < 4) continue;
                int[] v = p.computeIfAbsent(t[2], k -> new int[3]);
                if (n.endsWith(".class")) {
                    v[0]++;
                    try (InputStream in = z.getInputStream(e); DataInputStream d = new DataInputStream(in)) {
                        d.readInt();
                        d.readUnsignedShort();
                        v[2] = Math.max(v[2], d.readUnsignedShort());
                    }
                } else if (t.length == 4 && t[3].equals("README.md")) v[1] = 1;
            }
            int fehler = 0;
            System.out.println("check-packages: " + jar.getName() + " mit " + p.size() + " Packages (Quelle: Projekt Renderer)");
            System.out.println("  Package        Klassen  README  Java");
            for (String g : GEBRAUCHT) {
                int[] v = p.get(g);
                boolean ok = v != null && v[0] > 0 && v[1] == 1 && v[2] <= 65;
                if (!ok) fehler++;
                System.out.printf("  %-14s %-8s %-7s %-5s %s%n", g, v == null ? "–" : v[0], v == null || v[1] == 0 ? "fehlt" : "ok", v == null ? "–" : v[2] - 44, ok ? "" : "FEHLER");
            }
            for (String[] s : SHADER)
                for (int i = 1; i < s.length; i++)
                    if (z.getEntry("com/dan/" + s[0] + "/" + s[i]) == null) { fehler++; System.out.println("  FEHLER: com/dan/" + s[0] + "/" + s[i] + " fehlt"); }
            if (fehler > 0) { System.out.println("check-packages: " + fehler + " Fehler – Renderer neu bauen (Clean and Build) und dist/Renderer.jar nach lib/ kopieren"); System.exit(1); }
            System.out.println("check-packages: alle " + GEBRAUCHT.length + " gebrauchten Packages in " + jar.getName()
                    + ", dazu die Brecher aus surf (Grund je Reihe), ocean, spray, sky und beacon (seit Sylt F18 im Renderer).");
        }
    }
}
