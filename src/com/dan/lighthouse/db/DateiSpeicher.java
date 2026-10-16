package com.dan.lighthouse.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Speichern ohne Datenbank (F40 A): Textdateien im Ordner {@code daten/} des Projekts, im Textformat von
 * {@link Felder#alsText}. Die mitgelieferten Einträge kommen aus {@link Startdaten} und stehen nicht in Dateien;
 * eigene Einträge liegen je in einer Datei. Der Ordner steht in {@code .gitignore}.
 * <pre>
 * daten/wetterlagen/*.txt   daten/regiebuecher/*.txt   daten/fahrten/*.txt
 * daten/ansichten/N.txt + N.png   daten/presets/paket/Klasse/*.txt
 * daten/sitzungen/N.txt   daten/protokoll.txt
 * </pre>
 */
public final class DateiSpeicher implements Speicher {
    private static final int SITZUNGEN_BEHALTEN = 10;
    private final File dir;

    public DateiSpeicher(File dir) { this.dir = dir; }

    /** Der Ordner daten neben db (oder -Dlighthouse.daten.dir). */
    public static File ordner() {
        String p = System.getProperty("lighthouse.daten.dir");
        if (p != null && !p.isEmpty()) return new File(p);
        File db = Db.ordner().getAbsoluteFile();
        File base = db.getParentFile();
        return new File(base != null ? base : new File("."), "daten");
    }

    public File dir() { return dir; }

    @Override public String wo() { return "Dateien in " + dir.getPath(); }
    @Override public boolean datenbank() { return false; }

    // ------------------------------------------------------------------ Hilfen

    /** Dateiname aus dem Namen: nur ASCII (ä → ae, ß → ss), alles Fremde wird _, damit jedes Dateisystem ihn nimmt. */
    static String dateiname(String name) {
        StringBuilder b = new StringBuilder();
        String n = name.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("Ä", "Ae").replace("Ö", "Oe").replace("Ü", "Ue").replace("ß", "ss");
        for (char c : n.toCharArray()) b.append(c < 128 && (Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '(' || c == ')' || c == '.') ? c : '_');
        String s = b.toString().strip();
        return (s.isEmpty() ? "_" : s) + ".txt";
    }

    private File sub(String... teile) {
        File f = dir;
        for (String t : teile) f = new File(f, t);
        return f;
    }

    private static void schreibe(File f, String text) throws Fehler {
        try {
            Files.createDirectories(f.getParentFile().toPath());
            File tmp = new File(f.getPath() + ".neu");
            Files.writeString(tmp.toPath(), text, StandardCharsets.UTF_8);
            Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Fehler("Datei " + f + " nicht geschrieben: " + e.getMessage(), e);
        }
    }

    private static LinkedHashMap<String, String> lies(File f) throws Fehler {
        try {
            return Felder.ausText(Files.readString(f.toPath(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new Fehler("Datei " + f + " nicht lesbar: " + e.getMessage(), e);
        }
    }

    private static List<File> dateien(File d) {
        File[] fs = d.listFiles((x, n) -> n.endsWith(".txt"));
        if (fs == null) return new ArrayList<>();
        List<File> l = new ArrayList<>(Arrays.asList(fs));
        l.sort(Comparator.comparing(File::getName));
        return l;
    }

    private static void loesche(File f, String was) throws Fehler {
        if (!f.isFile()) throw new Fehler(was + " gibt es nicht.");
        if (!f.delete()) throw new Fehler(was + " ließ sich nicht löschen.");
    }

    private static void eigenPruefen(boolean mitgeliefert, String was) throws Fehler {
        if (mitgeliefert) throw new Fehler(was + " ist mitgeliefert und bleibt, wie sie ist. Bitte unter einem eigenen Namen speichern.");
    }

    // ------------------------------------------------------------------ Stand

    @Override public Daten.Stand stand() throws Fehler {
        return new Daten.Stand(tuerme().size(), wetterlagen().size(), regiebuecher().size(), fahrten().size(), ansichten().size(),
                presets(null, null).size(), letzteSitzung(-1));
    }

    @Override public List<Daten.Turm> tuerme() { return Startdaten.tuerme(); }

    // ------------------------------------------------------------------ Wetterlagen

    @Override public List<Daten.Wetterlage> wetterlagen() throws Fehler {
        List<Daten.Wetterlage> l = new ArrayList<>(Startdaten.wetterlagen());
        for (File f : dateien(sub("wetterlagen"))) {
            Map<String, String> m = lies(f);
            double[] w = new double[Startdaten.VARS.length];
            for (int i = 0; i < w.length; i++) w[i] = Zahl.lies(m.get(Startdaten.VARS[i]));
            double bft = Zahl.lies(m.get("beaufort"));
            l.add(new Daten.Wetterlage(m.getOrDefault("name", f.getName()), Double.isNaN(bft) ? null : bft, m.get("phase"), w,
                    m.getOrDefault("beschreibung", ""), Daten.EIGEN));
        }
        return l;
    }

    @Override public void speichern(Daten.Wetterlage w) throws Fehler {
        eigenPruefen(Startdaten.wetterlagen().stream().anyMatch(x -> x.name().equals(w.name())), "Die Wetterlage „" + w.name() + "“");
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("name", w.name());
        m.put("beaufort", w.beaufort() == null ? "" : Zahl.text(w.beaufort()));
        m.put("phase", w.phase() == null ? "" : w.phase());
        m.put("beschreibung", w.beschreibung() == null ? "" : w.beschreibung());
        for (int i = 0; i < Startdaten.VARS.length; i++) m.put(Startdaten.VARS[i], Zahl.text(w.werte()[i]));
        schreibe(sub("wetterlagen", dateiname(w.name())), Felder.alsText(m, "Lighthouse · Wetterlage"));
    }

    @Override public void loeschenWetterlage(String name) throws Fehler {
        eigenPruefen(Startdaten.wetterlagen().stream().anyMatch(x -> x.name().equals(name)), "Die Wetterlage „" + name + "“");
        loesche(sub("wetterlagen", dateiname(name)), "Die Wetterlage „" + name + "“");
    }

    // ------------------------------------------------------------------ Regiebücher

    @Override public List<Daten.Regiebuch> regiebuecher() throws Fehler {
        List<Daten.Regiebuch> l = new ArrayList<>(Startdaten.regiebuecher());
        for (File f : dateien(sub("regiebuecher"))) {
            Map<String, String> m = lies(f);
            l.add(new Daten.Regiebuch(m.getOrDefault("name", f.getName()), m.getOrDefault("text", ""), m.getOrDefault("beschreibung", ""), Daten.EIGEN));
        }
        return l;
    }

    @Override public void speichern(Daten.Regiebuch r) throws Fehler {
        eigenPruefen(Startdaten.regiebuecher().stream().anyMatch(x -> x.name().equals(r.name())), "Das Regiebuch „" + r.name() + "“");
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("name", r.name());
        m.put("beschreibung", r.beschreibung() == null ? "" : r.beschreibung());
        m.put("text", r.text());
        schreibe(sub("regiebuecher", dateiname(r.name())), Felder.alsText(m, "Lighthouse · Regiebuch"));
    }

    @Override public void loeschenRegiebuch(String name) throws Fehler {
        eigenPruefen(Startdaten.regiebuecher().stream().anyMatch(x -> x.name().equals(name)), "Das Regiebuch „" + name + "“");
        loesche(sub("regiebuecher", dateiname(name)), "Das Regiebuch „" + name + "“");
    }

    // ------------------------------------------------------------------ Fahrten

    @Override public List<Daten.Fahrt> fahrten() throws Fehler {
        List<Daten.Fahrt> l = new ArrayList<>(Startdaten.fahrten());
        for (File f : dateien(sub("fahrten"))) {
            Map<String, String> m = lies(f);
            l.add(new Daten.Fahrt(m.getOrDefault("name", f.getName()), m.getOrDefault("art", "FREI"), "true".equals(m.get("schleife")),
                    m.getOrDefault("beschreibung", ""), Daten.EIGEN, punkteAus(m.getOrDefault("punkte", ""))));
        }
        return l;
    }

    static String punkteText(List<Daten.Stuetzpunkt> ps) {
        StringBuilder b = new StringBuilder();
        for (Daten.Stuetzpunkt p : ps) {
            b.append(Zahl.text(p.zeitS())).append(' ').append(Zahl.text(p.x())).append(' ').append(Zahl.text(p.y())).append(' ')
                    .append(Zahl.text(p.z())).append(' ').append(Zahl.text(p.gier())).append(' ').append(Zahl.text(p.nick())).append(' ')
                    .append(Zahl.text(p.fov())).append('\n');
        }
        return b.toString();
    }

    static List<Daten.Stuetzpunkt> punkteAus(String t) {
        List<Daten.Stuetzpunkt> l = new ArrayList<>();
        int nr = 0;
        for (String z : t.split("\\R")) {
            String[] f = z.strip().split("\\s+");
            if (f.length < 7) continue;
            l.add(new Daten.Stuetzpunkt(++nr, Zahl.lies(f[0]), Zahl.lies(f[1]), Zahl.lies(f[2]), Zahl.lies(f[3]), Zahl.lies(f[4]), Zahl.lies(f[5]), Zahl.lies(f[6])));
        }
        return l;
    }

    @Override public void speichern(Daten.Fahrt f) throws Fehler {
        eigenPruefen(Startdaten.fahrten().stream().anyMatch(x -> x.name().equals(f.name())), "Die Kamerafahrt „" + f.name() + "“");
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("name", f.name());
        m.put("art", f.art());
        m.put("schleife", Boolean.toString(f.schleife()));
        m.put("beschreibung", f.beschreibung() == null ? "" : f.beschreibung());
        m.put("punkte", punkteText(f.punkte()));
        schreibe(sub("fahrten", dateiname(f.name())), Felder.alsText(m, "Lighthouse · Kamerafahrt: Zeit x y z Gier Nick Bildwinkel je Zeile"));
    }

    @Override public void loeschenFahrt(String name) throws Fehler {
        eigenPruefen(Startdaten.fahrten().stream().anyMatch(x -> x.name().equals(name)), "Die Kamerafahrt „" + name + "“");
        loesche(sub("fahrten", dateiname(name)), "Die Kamerafahrt „" + name + "“");
    }

    // ------------------------------------------------------------------ Ansichten

    @Override public List<Daten.Ansicht> ansichten() throws Fehler {
        List<Daten.Ansicht> l = new ArrayList<>();
        for (File f : dateien(sub("ansichten"))) {
            Daten.Ansicht a = ansichtAus(lies(f), null);
            if (a != null) l.add(new Daten.Ansicht(a.id(), a.name(), a.szenenzeitMs(), a.kamera(), a.turm(), null, null));
        }
        l.sort(Comparator.comparingLong(Daten.Ansicht::id).reversed());
        return l;
    }

    private static Daten.Ansicht ansichtAus(Map<String, String> m, byte[] png) {
        try {
            long id = Long.parseLong(m.get("id"));
            String[] k = m.getOrDefault("kamera", "").split("\\s+");
            double[] cam = new double[6];
            for (int i = 0; i < 6 && i < k.length; i++) cam[i] = Zahl.lies(k[i]);
            return new Daten.Ansicht(id, m.getOrDefault("name", "Ansicht " + id), Long.parseLong(m.getOrDefault("szenenzeit", "0")), cam,
                    m.getOrDefault("turm", ""), m.get("zustand"), png);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override public Daten.Ansicht ansicht(long id) throws Fehler {
        File f = sub("ansichten", id + ".txt");
        if (!f.isFile()) throw new Fehler("Die Ansicht " + id + " gibt es nicht.");
        byte[] png = null;
        File p = sub("ansichten", id + ".png");
        try { if (p.isFile()) png = Files.readAllBytes(p.toPath()); } catch (IOException e) { /* ohne Vorschau */ }
        return ansichtAus(lies(f), png);
    }

    @Override public long speichern(Daten.Ansicht a, Long sitzung) throws Fehler {
        long id = 1;
        for (File f : dateien(sub("ansichten"))) {
            try { id = Math.max(id, Long.parseLong(f.getName().replace(".txt", "")) + 1); } catch (NumberFormatException e) { /* fremd */ }
        }
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("id", Long.toString(id));
        m.put("name", a.name());
        m.put("szenenzeit", Long.toString(a.szenenzeitMs()));
        StringBuilder k = new StringBuilder();
        for (double v : a.kamera()) k.append(k.length() == 0 ? "" : " ").append(Zahl.text(v));
        m.put("kamera", k.toString());
        m.put("turm", a.turm() == null ? "" : a.turm());
        m.put("zustand", a.zustand());
        schreibe(sub("ansichten", id + ".txt"), Felder.alsText(m, "Lighthouse · Ansicht"));
        if (a.vorschau() != null) {
            try { Files.write(sub("ansichten", id + ".png").toPath(), a.vorschau()); } catch (IOException e) { throw new Fehler("Vorschau nicht geschrieben: " + e.getMessage(), e); }
        }
        return id;
    }

    @Override public void loeschenAnsicht(long id) throws Fehler {
        loesche(sub("ansichten", id + ".txt"), "Die Ansicht " + id);
        File p = sub("ansichten", id + ".png");
        if (p.isFile() && !p.delete()) p.deleteOnExit();
    }

    // ------------------------------------------------------------------ Presets

    @Override public List<Daten.PaketPreset> presets(String paket, String klasse) throws Fehler {
        List<Daten.PaketPreset> l = new ArrayList<>();
        for (Daten.PaketPreset p : Startdaten.presets())
            if ((paket == null || p.paket().equals(paket)) && (klasse == null || p.klasse().equals(klasse))) l.add(p);
        File base = sub("presets");
        File[] ps = base.listFiles(File::isDirectory);
        if (ps != null) for (File pd : ps) {
            if (paket != null && !pd.getName().equals(paket)) continue;
            File[] ks = pd.listFiles(File::isDirectory);
            if (ks != null) for (File kd : ks) {
                if (klasse != null && !kd.getName().equals(klasse)) continue;
                for (File f : dateien(kd)) {
                    Map<String, String> m = lies(f);
                    l.add(new Daten.PaketPreset(pd.getName(), kd.getName(), m.getOrDefault("name", f.getName()), m.getOrDefault("beschreibung", ""),
                            Daten.EIGEN, Felder.teil(m, "feld")));
                }
            }
        }
        return l;
    }

    private static boolean mitgeliefert(String paket, String klasse, String name) {
        return Startdaten.presets().stream().anyMatch(p -> p.paket().equals(paket) && p.klasse().equals(klasse) && p.name().equals(name));
    }

    @Override public void speichern(Daten.PaketPreset p) throws Fehler {
        eigenPruefen(mitgeliefert(p.paket(), p.klasse(), p.name()), "Das Preset „" + p.name() + "“");
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("name", p.name());
        m.put("beschreibung", p.beschreibung() == null ? "" : p.beschreibung());
        for (Map.Entry<String, String> e : p.felder().entrySet()) m.put("feld." + e.getKey(), e.getValue());
        schreibe(sub("presets", p.paket(), p.klasse(), dateiname(p.name())), Felder.alsText(m, "Lighthouse · Preset " + p.paket() + "." + p.klasse()));
    }

    @Override public void loeschenPreset(String paket, String klasse, String name) throws Fehler {
        eigenPruefen(mitgeliefert(paket, klasse, name), "Das Preset „" + name + "“");
        loesche(sub("presets", paket, klasse, dateiname(name)), "Das Preset „" + name + "“");
    }

    // ------------------------------------------------------------------ Sitzungen

    @Override public long sitzungBeginnen(String version, String gpu, String qualitaet) throws Fehler {
        long id = 1;
        List<File> fs = dateien(sub("sitzungen"));
        for (File f : fs) {
            try { id = Math.max(id, Long.parseLong(f.getName().replace(".txt", "")) + 1); } catch (NumberFormatException e) { /* fremd */ }
        }
        // ältere Sitzungen aufräumen
        fs.sort(Comparator.comparingLong(f -> { try { return Long.parseLong(f.getName().replace(".txt", "")); } catch (NumberFormatException e) { return 0L; } }));
        for (int i = 0; i < fs.size() - SITZUNGEN_BEHALTEN + 1; i++) if (!fs.get(i).delete()) fs.get(i).deleteOnExit();
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("id", Long.toString(id));
        m.put("beginn", Long.toString(System.currentTimeMillis()));
        m.put("sauber", "false");
        m.put("version", version);
        m.put("gpu", gpu == null ? "" : gpu);
        m.put("qualitaet", qualitaet == null ? "" : qualitaet);
        m.put("rechner", rechner());
        schreibe(sub("sitzungen", id + ".txt"), Felder.alsText(m, "Lighthouse · Sitzung"));
        return id;
    }

    static String rechner() {
        String h = System.getenv("COMPUTERNAME");
        if (h == null) h = System.getenv("HOSTNAME");
        return h == null ? "" : h;
    }

    private LinkedHashMap<String, String> sitzung(long id) throws Fehler {
        File f = sub("sitzungen", id + ".txt");
        if (!f.isFile()) throw new Fehler("Sitzung " + id + " gibt es nicht.");
        return lies(f);
    }

    @Override public void sitzungSichern(long id, String zustand, long szenenzeitMs, String turm, String regiebuch, double regieStunde) throws Fehler {
        LinkedHashMap<String, String> m = sitzung(id);
        m.put("zustandZeit", Long.toString(System.currentTimeMillis()));
        m.put("szenenzeit", Long.toString(szenenzeitMs));
        m.put("turm", turm == null ? "" : turm);
        m.put("regiebuch", regiebuch == null ? "" : regiebuch);
        m.put("regieStunde", Zahl.text(regieStunde));
        m.put("zustand", zustand);
        schreibe(sub("sitzungen", id + ".txt"), Felder.alsText(m, "Lighthouse · Sitzung"));
    }

    @Override public void sitzungBeenden(long id) throws Fehler {
        LinkedHashMap<String, String> m = sitzung(id);
        m.put("ende", Long.toString(System.currentTimeMillis()));
        m.put("sauber", "true");
        schreibe(sub("sitzungen", id + ".txt"), Felder.alsText(m, "Lighthouse · Sitzung"));
    }

    @Override public Daten.Sitzung letzteSitzung(long ausser) throws Fehler {
        Daten.Sitzung best = null;
        for (File f : dateien(sub("sitzungen"))) {
            Map<String, String> m = lies(f);
            try {
                long id = Long.parseLong(m.get("id"));
                if (id == ausser || m.get("zustand") == null || m.get("zustand").isEmpty()) continue;
                if (best != null && best.id() > id) continue;
                String ende = m.get("ende"), zz = m.get("zustandZeit");
                best = new Daten.Sitzung(id, Long.parseLong(m.get("beginn")), ende == null ? null : Long.parseLong(ende), "true".equals(m.get("sauber")),
                        m.get("zustand"), zz == null ? null : Long.parseLong(zz), m.getOrDefault("turm", ""));
            } catch (RuntimeException e) {
                // kaputte Datei: übergehen
            }
        }
        return best;
    }

    // ------------------------------------------------------------------ Protokoll

    @Override public void protokoll(List<Daten.LogZeile> zeilen, Long sitzung) throws Fehler {
        StringBuilder b = new StringBuilder();
        for (Daten.LogZeile z : zeilen) b.append(zeile(z, sitzung)).append('\n');
        File f = sub("protokoll.txt");
        try {
            Files.createDirectories(dir.toPath());
            Files.writeString(f.toPath(), b, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new Fehler("Protokoll nicht geschrieben: " + e.getMessage(), e);
        }
    }

    /** Eine Zeile: Zeit TAB Stufe TAB Quelle TAB Szenenzeit TAB Ort TAB Text (Detail eingerückt darunter). */
    static String zeile(Daten.LogZeile z, Long sitzung) {
        String t = Instant.ofEpochMilli(z.zeitMs()) + "\t" + z.stufe() + "\t" + z.quelle() + "\t"
                + (z.szenenzeitMs() == null ? "" : Instant.ofEpochMilli(z.szenenzeitMs()).toString()) + "\t" + (z.ort() == null ? "" : z.ort()) + "\t"
                + (sitzung == null ? "" : "S" + sitzung) + "\t" + z.text().replace("\n", " ");
        if (z.detail() != null && !z.detail().isEmpty()) t += "\n    " + z.detail().strip().replace("\n", "\n    ");
        return t;
    }

    @Override public List<Daten.LogZeile> letzteZeilen(int n) throws Fehler {
        File f = sub("protokoll.txt");
        List<Daten.LogZeile> l = new ArrayList<>();
        if (!f.isFile()) return l;
        try {
            List<String> zs = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            for (int i = zs.size() - 1; i >= 0 && l.size() < n; i--) {
                String[] p = zs.get(i).split("\t", 7);
                if (p.length < 7 || zs.get(i).startsWith("    ")) continue;
                try {
                    l.add(0, new Daten.LogZeile(Instant.parse(p[0]).toEpochMilli(), p[1], p[2], p[6],
                            p[3].isEmpty() ? null : Instant.parse(p[3]).toEpochMilli(), p[4], null));
                } catch (RuntimeException e) { /* fremde Zeile */ }
            }
        } catch (IOException e) {
            throw new Fehler("Protokoll nicht lesbar: " + e.getMessage(), e);
        }
        return l;
    }

    @Override public void schliessen() { }
}
