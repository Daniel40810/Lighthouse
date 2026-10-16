package com.dan.lighthouse.ui;

import com.dan.fbutton.FButton;
import com.dan.fcombobox.FComboBox;
import com.dan.foptionpane.FOptionPane;
import com.dan.lighthouse.camera.Aufstieg;
import com.dan.lighthouse.camera.Camera;
import com.dan.lighthouse.db.Daten;
import com.dan.lighthouse.db.DateiSpeicher;
import com.dan.lighthouse.db.DbSpeicher;
import com.dan.lighthouse.db.Dienst;
import com.dan.lighthouse.db.Einrichter;
import com.dan.lighthouse.db.Felder;
import com.dan.lighthouse.db.Protokoll;
import com.dan.lighthouse.db.Speicher;
import com.dan.lighthouse.db.Startdaten;
import com.dan.lighthouse.db.TurmWahl;
import com.dan.lighthouse.db.Uebernahme;
import com.dan.lighthouse.db.Zahl;
import com.dan.lighthouse.db.Zustand;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.gl.SceneView;
import com.dan.weather.Scenario;
import com.dan.weather.Weather;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Phase 15: alles, was die Oberfläche mit gespeicherten Daten tut: Turm wählen, Wetterlagen und Regiebücher laden und
 * speichern, Ansichten merken (V) und zeigen, Kamerafahrten fahren und aufnehmen, Presets der Packages, „Weiter wie
 * zuletzt“, Einrichter und Übernahme. Lesen und Schreiben laufen im Faden des {@link Dienst}; die Ergebnisse kommen
 * auf den Swing-Faden zurück. Die Bedienelemente baut diese Klasse; das Bedienfeld ordnet sie in seine Blöcke.
 */
public final class DatenSteuerung {
    private final Renderer r;
    private final SceneView view;
    private final Component parent;
    /** Was die Regie starten soll (das Bedienfeld setzt Zeitraffer und Kamera dazu). */
    private Consumer<Scenario> regieStarter = s -> { };

    private List<Daten.Turm> tuerme = new ArrayList<>(Startdaten.tuerme());
    private List<Daten.Regiebuch> regiebuecher = new ArrayList<>();
    private List<Daten.Wetterlage> wetterlagen = new ArrayList<>();
    private List<Daten.Fahrt> fahrten = new ArrayList<>();
    private List<Daten.Ansicht> ansichten = new ArrayList<>();
    private List<Daten.PaketPreset> presets = new ArrayList<>();
    private boolean fuellt;

    // Bedienelemente
    final FComboBox turm = new FComboBox(), wetterlage = new FComboBox(), regiebuch = new FComboBox(), fahrt = new FComboBox(),
            ansicht = new FComboBox(), paket = new FComboBox(), preset = new FComboBox();
    final JLabel turmInfo = ControlPanel.small(" "), wo = ControlPanel.small(" "), standLbl = ControlPanel.small(" "), sicherLbl = ControlPanel.small(" "),
            einrichtLbl = ControlPanel.small(" "), logLbl = ControlPanel.small(" "), vorschau = new JLabel(), ansichtInfo = ControlPanel.small(" "),
            fahrtInfo = ControlPanel.small(" "), presetInfo = ControlPanel.small(" "), meldung = ControlPanel.small(" "), wetterInfo = ControlPanel.small(" ");
    final FButton wetterAn = new FButton("Anwenden"), wetterSpeichern = new FButton("Speichern …"), regieStart = new FButton("Starten"),
            regieBearbeiten = new FButton("Bearbeiten …"), fahrtStart = new FButton("Fahren"), aufnahme = new FButton("Aufnahme"),
            merken = new FButton("Ansicht merken (V)"), zeigen = new FButton("Zeigen"), ansichtLoeschen = new FButton("Löschen"),
            presetLaden = new FButton("Laden"), presetSpeichern = new FButton("Speichern …"), presetLoeschen = new FButton("Löschen"),
            jetztSichern = new FButton("Jetzt sichern"), weiterBtn = new FButton("Letzten Stand laden"), einrichtenBtn = new FButton("Einrichten"),
            pruefenBtn = new FButton("Prüfen"), uebernehmenBtn = new FButton("Dateien in die Datenbank übernehmen");

    /** Aufnahme einer Kamerafahrt: Stützpunkte und Beginn (ns). */
    private List<Daten.Stuetzpunkt> aufnahmePunkte;
    private long aufnahmeBeginn;

    DatenSteuerung(Renderer r, SceneView view, Component parent) {
        this.r = r;
        this.view = view;
        this.parent = parent;
        vorschau.setHorizontalAlignment(JLabel.CENTER);
        String[] pk = new String[Startdaten.PAKETE.length];
        for (int i = 0; i < pk.length; i++) pk[i] = Startdaten.PAKETE[i].replaceAll("\\.([A-Z]\\w*)$", " · $1");
        paket.setModel(new DefaultComboBoxModel<Object>(pk));
        turm.setModel(new DefaultComboBoxModel<Object>(tuerme.toArray()));
        turm.addActionListener(e -> { if (!fuellt && turm.getSelectedItem() instanceof Daten.Turm t) turmWaehlen(t); });
        wetterAn.addActionListener(e -> { if (wetterlage.getSelectedItem() instanceof Daten.Wetterlage w) wetterlageAnwenden(w); });
        wetterlage.addActionListener(e -> { if (wetterlage.getSelectedItem() instanceof Daten.Wetterlage w) wetterInfo.setText(html(w.beschreibung())); });
        wetterSpeichern.addActionListener(e -> wetterlageSpeichern());
        regieStart.addActionListener(e -> { if (regiebuch.getSelectedItem() instanceof Daten.Regiebuch rb) regiebuchStarten(rb); });
        regieBearbeiten.addActionListener(e -> regiebuchBearbeiten());
        fahrtStart.addActionListener(e -> { if (fahrt.getSelectedItem() instanceof Daten.Fahrt f) fahren(f); });
        fahrt.addActionListener(e -> { if (fahrt.getSelectedItem() instanceof Daten.Fahrt f) fahrtInfo.setText(html(f.beschreibung() + " · " + f.punkte().size() + " Stützpunkte")); });
        aufnahme.addActionListener(e -> aufnahme());
        merken.addActionListener(e -> ansichtMerken());
        zeigen.addActionListener(e -> { if (ansicht.getSelectedItem() instanceof Daten.Ansicht a) ansichtZeigen(a.id()); });
        ansicht.addActionListener(e -> { if (!fuellt && ansicht.getSelectedItem() instanceof Daten.Ansicht a) vorschauLaden(a.id()); });
        ansichtLoeschen.addActionListener(e -> ansichtLoeschen());
        paket.addActionListener(e -> presetListe());
        presetLaden.addActionListener(e -> presetLaden());
        presetSpeichern.addActionListener(e -> presetSpeichern());
        presetLoeschen.addActionListener(e -> presetLoeschen());
        preset.addActionListener(e -> { if (preset.getSelectedItem() instanceof Daten.PaketPreset p) presetInfo.setText(html(p.beschreibung() + " · " + p.felder().size() + " Felder")); });
        jetztSichern.addActionListener(e -> Dienst.spaeter(() -> { Dienst.sichern(false); SwingUtilities.invokeLater(this::anzeigen); }));
        weiterBtn.addActionListener(e -> weiterWieZuletzt());
        einrichtenBtn.addActionListener(e -> einrichten(false));
        pruefenBtn.addActionListener(e -> einrichten(true));
        uebernehmenBtn.addActionListener(e -> uebernehmen());
        view.setKeyAction(java.awt.event.KeyEvent.VK_V, () -> SwingUtilities.invokeLater(this::ansichtMerken));
        view.setKeyAction(java.awt.event.KeyEvent.VK_P, () -> SwingUtilities.invokeLater(this::stuetzpunkt));
    }

    void setRegieStarter(Consumer<Scenario> s) { regieStarter = s; }

    static String html(String s) { return "<html><body style='width:215px'>" + Startbild.esc(s == null ? "" : s) + "</body></html>"; }

    private void meldung(String s) { meldung.setText(html(s)); }

    private void fehler(String was, Exception e) {
        meldung.setForeground(new Color(240, 170, 80));
        meldung(was + ": " + e.getMessage());
        Protokoll.fehler("Daten", was + ": " + e.getMessage(), e instanceof Speicher.Fehler ? null : e);
    }

    private void ok(String s) {
        meldung.setForeground(Theme.muted());
        meldung(s);
        Protokoll.info("Daten", s);
    }

    /** Eine Arbeit mit dem Speicher im Hintergrund; danach auf dem Swing-Faden weiter. */
    private interface Arbeit<T> { T tun(Speicher s) throws Exception; }

    private <T> void hinten(String was, Arbeit<T> a, Consumer<T> danach) {
        Dienst.spaeter(() -> {
            try {
                T t = a.tun(Dienst.speicher());
                SwingUtilities.invokeLater(() -> danach.accept(t));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> fehler(was, e));
            }
        });
    }

    // ------------------------------------------------------------------ Start

    /** Beim Start: Listen laden, wenn gewünscht den letzten Stand herstellen, Sitzung beginnen. */
    void starten(boolean weiter) {
        Protokoll.kontext(() -> (long) r.clock.epochMillis(), this::ort);
        hinten("Daten lesen", s -> new Object[]{s.tuerme(), s.wetterlagen(), s.regiebuecher(), s.fahrten(), s.ansichten(), weiter ? s.letzteSitzung(-1) : null}, o -> {
            fuellen(o);
            Daten.Sitzung ls = (Daten.Sitzung) o[5];
            if (weiter && ls != null) herstellen(Felder.ausText(ls.zustand()), "Weiter wie zuletzt (Sitzung " + ls.id() + (ls.sauber() ? "" : ", abgebrochen") + ")");
            String gpu = r.glInfo(), q = r.quality().name();
            Dienst.spaeter(() -> Dienst.starten(Renderer.VERSION, gpu, q, this::erfassen));
            anzeigen();
        });
    }

    /** Für den Dienst: Zustand, Szenenzeit, Turm, Regiebuch, Stunde. */
    private String[] erfassen() {
        LinkedHashMap<String, String> z = Zustand.erfassen(r, r.soundRunning());
        return new String[]{Felder.alsText(z, "Lighthouse · Zustand"), Long.toString((long) r.clock.epochMillis()), r.siteName,
                Zustand.regiebuch(z), Zahl.text(Zustand.regieStunde(z))};
    }

    /** „Laternenraum“, „Galerie“, „x 120 · z −40“ – wo das Auge ist (für das Protokoll). */
    private String ort() {
        double[] e = r.camera.eye;
        if (r.insideLantern()) return "Laternenraum";
        if (r.camera.towerDistance() < 4 && e[1] < 22) return "im Turm";
        return String.format(Locale.ROOT, "x %.0f · y %.0f · z %.0f", e[0], e[1], e[2]);
    }

    @SuppressWarnings("unchecked")
    private void fuellen(Object[] o) {
        fuellt = true;
        try {
            tuerme = (List<Daten.Turm>) o[0];
            wetterlagen = (List<Daten.Wetterlage>) o[1];
            regiebuecher = (List<Daten.Regiebuch>) o[2];
            fahrten = (List<Daten.Fahrt>) o[3];
            ansichten = (List<Daten.Ansicht>) o[4];
            setze(turm, tuerme, r.siteName);
            setze(wetterlage, wetterlagen, null);
            setze(regiebuch, regiebuecher, null);
            setze(fahrt, fahrten, null);
            setze(ansicht, ansichten, null);
            turmInfoSetzen();
        } finally {
            fuellt = false;
        }
        presetListe();
        if (!ansichten.isEmpty()) vorschauLaden(ansichten.get(0).id());
        else { vorschau.setIcon(null); ansichtInfo.setText(html("Noch keine Ansicht gemerkt. Taste V merkt die Ansicht mit Vorschau.")); }
    }

    private static <T> void setze(FComboBox c, List<T> l, String waehle) {
        Object alt = c.getSelectedItem();
        c.setModel(new DefaultComboBoxModel<Object>(l.toArray()));
        String w = waehle != null ? waehle : alt != null ? alt.toString() : null;
        for (T t : l) if (t.toString().equals(w)) { c.setSelectedItem(t); break; }
    }

    /** Listen neu lesen (nach Speichern, Einrichten, Übernehmen). */
    void neuLesen() {
        hinten("Daten lesen", s -> new Object[]{s.tuerme(), s.wetterlagen(), s.regiebuecher(), s.fahrten(), s.ansichten(), null}, o -> { fuellen(o); anzeigen(); });
    }

    /** Texte im Block DATEN (alle halbe Sekunde vom Bedienfeld). */
    void anzeigen() {
        Speicher s = Dienst.speicher();
        wo.setText(html("Gespeichert wird in: " + s.wo() + (Dienst.sitzung() >= 0 ? " · Sitzung " + Dienst.sitzung() : "")));
        long g = Dienst.gesichert();
        sicherLbl.setText(html((g == 0 ? "Noch nicht gesichert" : "Zuletzt gesichert " + hm(g)) + "; der Zustand wird alle zwei Minuten und beim Beenden gesichert."));
        standLbl.setText(html(tuerme.size() + " Türme · " + wetterlagen.size() + " Wetterlagen · " + regiebuecher.size() + " Regiebücher · " + fahrten.size()
                + " Kamerafahrten · " + ansichten.size() + (ansichten.size() == 1 ? " Ansicht" : " Ansichten")));
        DbSpeicher db = Startbild.datenbank();
        einrichtenBtn.setEnabled(db != null);
        pruefenBtn.setEnabled(db != null);
        uebernehmenBtn.setEnabled(s.datenbank());
        Einrichter.Ergebnis e = Startbild.letzteEinrichtung;
        StringBuilder b = new StringBuilder();
        if (db == null) b.append("Ohne Datenbank. Einrichten geht, sobald die Verbindung steht (Startbild).");
        else if (e == null) b.append(Startbild.stufe() >= Einrichter.VERSION ? "Schema eingerichtet (Stufe " + Startbild.stufe() + ")." : "Schema noch nicht eingerichtet.");
        else {
            b.append("Einrichter: ").append(e.kurz());
            int n = 0;
            for (String z : e.zeilen()) if (!z.startsWith("ok") && n++ < 8) b.append("\n· ").append(z);
            if (e.ok()) for (String z : e.zeilen()) if (z.startsWith("ok") && n++ < 12) b.append("\n· ").append(z.substring(3));
        }
        einrichtLbl.setText("<html><body style='width:215px'>" + Startbild.esc(b.toString()).replace("\n", "<br>") + "</body></html>");
        StringBuilder l = new StringBuilder();
        for (Daten.LogZeile z : Protokoll.zuletzt(8)) l.append(l.length() == 0 ? "" : "<br>").append(Startbild.esc(Protokoll.zeile(z)));
        logLbl.setText("<html><body style='width:215px'>" + (l.length() == 0 ? "Noch nichts protokolliert." : l) + "</body></html>");
        if (aufnahmePunkte != null) aufnahme.setText("Fertig (" + aufnahmePunkte.size() + ")");
    }

    static String hm(long ms) {
        ZonedDateTime z = Instant.ofEpochMilli(ms).atZone(java.time.ZoneId.systemDefault());
        return String.format("%02d:%02d", z.getHour(), z.getMinute());
    }

    // ------------------------------------------------------------------ Turm

    /** Für Werkzeuge: einen Turm nach Namen wählen (auch in der Liste). */
    public void turmWaehlen(String name) {
        Daten.Turm t = turm(name);
        if (t == null) return;
        turmWaehlen(t);
        fuellt = true;
        try { setze(turm, tuerme, name); } finally { fuellt = false; }
    }

    /** Für Werkzeuge: die Liste der Ansichten neu lesen. */
    public void listenNeu() { neuLesen(); }

    void turmWaehlen(Daten.Turm t) {
        try {
            String z = TurmWahl.waehlen(r, t, tuerme);
            turmInfoSetzen();
            if (onHergestellt != null) onHergestellt.run();
            ok("Turm: " + z);
            view.requestFocusInWindow();
        } catch (RuntimeException e) {
            fehler("Turm „" + t.name() + "“", e);
        }
    }

    private Daten.Turm turm(String name) {
        for (Daten.Turm t : tuerme) if (t.name().equals(name)) return t;
        return null;
    }

    private void turmInfoSetzen() {
        Daten.Turm t = turm(r.siteName);
        if (t == null) { turmInfo.setText(" "); return; }
        StringBuilder b = new StringBuilder();
        b.append(t.kennung()).append(", Feuerhöhe ").append(Zahl.text(t.feuerhoeheM())).append(" m");
        if (t.tragweiteSm() != null) b.append(", ").append(Zahl.text(t.tragweiteSm())).append(" sm");
        b.append(", ").append(t.sektoren().size()).append(t.sektoren().size() == 1 ? " Sektor" : " Sektoren");
        if (t.baujahr() != null) b.append(" · gebaut ").append(t.baujahr());
        if (t.ausserBetrieb() != null) b.append(", außer Betrieb seit ").append(t.ausserBetrieb());
        b.append(String.format(Locale.GERMAN, " · %.4f° N, %.4f° E", t.breite(), t.laenge()));
        List<Daten.Turm> nb = TurmWahl.nachbarn(t, tuerme);
        if (!nb.isEmpty()) {
            b.append(" · Nachbarn: ");
            for (int i = 0; i < nb.size(); i++) b.append(i == 0 ? "" : ", ").append(nb.get(i).name()).append(String.format(Locale.GERMAN, " %.1f km", t.abstandKm(nb.get(i))));
        }
        if (!TurmWahl.heim(t)) b.append(". Der Turm bleibt der Rote Sand; das Feuer brennt in seiner Laterne.");
        if (t.beschreibung() != null && !t.beschreibung().isEmpty()) b.append("\n").append(t.beschreibung());
        turmInfo.setText("<html><body style='width:215px'>" + Startbild.esc(b.toString()).replace("\n", "<br>") + "</body></html>");
    }

    // ------------------------------------------------------------------ Wetterlagen und Regiebücher

    void wetterlageAnwenden(Daten.Wetterlage w) {
        if (r.wiring == null) return;
        r.wiring.resumeRegie();
        int n = 0;
        for (Weather.Var v : Weather.Var.values()) {
            double x = w.werte()[v.ordinal()];
            if (!Double.isNaN(x)) { r.wiring.hand(v, x); n++; }
        }
        ok("Wetterlage „" + w.name() + "“: " + n + " Größen von Hand gesetzt; „Regie weiter“ gibt sie zurück.");
        view.requestFocusInWindow();
    }

    private String fragen(String frage, String vorgabe) {
        String s = FOptionPane.showInputDialog(parent, frage, "Lighthouse", FOptionPane.QUESTION_MESSAGE);
        if (s == null) return null;
        s = s.strip();
        if (s.isEmpty()) return vorgabe;
        return s.length() > 60 ? s.substring(0, 60) : s;
    }

    private void wetterlageSpeichern() {
        if (r.wiring == null) return;
        String name = fragen("Name der Wetterlage (so, wie sie gerade ist):", null);
        if (name == null || name.isEmpty()) return;
        Weather.Snapshot ws = r.wiring.weather.snapshot();
        double[] v = r.wiring.weather.values();
        Daten.Wetterlage w = new Daten.Wetterlage(name, (double) r.wiring.wind.beaufort(), ws != null ? ws.phase().name() : null, v,
                "Gespeichert am " + java.time.LocalDate.now() + " um " + hm(System.currentTimeMillis()), Daten.EIGEN);
        hinten("Wetterlage speichern", s -> { s.speichern(w); return null; }, x -> { ok("Wetterlage „" + name + "“ gespeichert."); neuLesen(); });
    }

    void regiebuchStarten(Daten.Regiebuch rb) {
        try {
            Scenario s = Scenario.parse(rb.text());
            regieStarter.accept(s);
            ok("Regiebuch „" + rb.name() + "“ gestartet: " + s.steps().size() + " Schritte, " + Zahl.text(Math.round(s.hours() * 10) / 10.0) + " Stunden.");
        } catch (RuntimeException e) {
            fehler("Regiebuch „" + rb.name() + "“", e);
        }
    }

    private void regiebuchBearbeiten() {
        Daten.Regiebuch rb = regiebuch.getSelectedItem() instanceof Daten.Regiebuch x ? x : null;
        RegieDialog.zeigen(parent, rb, (neu, starten) -> {
            hinten("Regiebuch speichern", s -> { s.speichern(neu); return null; }, x -> {
                ok("Regiebuch „" + neu.name() + "“ gespeichert.");
                neuLesen();
                if (starten) regiebuchStarten(neu);
            });
        });
    }

    // ------------------------------------------------------------------ Kamerafahrten

    void fahren(Daten.Fahrt f) {
        try {
            Aufstieg a = Startdaten.fahrt(f);
            r.zoom = 1;
            r.camera.startRide(a);
            ok("Kamerafahrt „" + f.name() + "“" + (f.schleife() ? " (läuft im Kreis; jede Bewegung von Hand beendet sie)" : ""));
            view.requestFocusInWindow();
        } catch (RuntimeException e) {
            fehler("Kamerafahrt „" + f.name() + "“", e);
        }
    }

    private void aufnahme() {
        if (aufnahmePunkte == null) {
            aufnahmePunkte = new ArrayList<>();
            aufnahmeBeginn = System.nanoTime();
            stuetzpunkt();
            meldung.setForeground(Theme.muted());
            meldung("Aufnahme läuft: Kamera bewegen und bei jeder Einstellung P drücken. „Fertig“ beendet und speichert.");
            view.requestFocusInWindow();
            return;
        }
        List<Daten.Stuetzpunkt> l = aufnahmePunkte;
        aufnahmePunkte = null;
        aufnahme.setText("Aufnahme");
        if (l.size() < 2) { meldung("Aufnahme verworfen: Eine Fahrt braucht mindestens zwei Stützpunkte (Taste P)."); return; }
        String name = fragen("Name der Kamerafahrt (" + l.size() + " Stützpunkte):", null);
        if (name == null || name.isEmpty()) return;
        Daten.Fahrt f = new Daten.Fahrt(name, "FREI", false, "Aufgenommen am " + java.time.LocalDate.now(), Daten.EIGEN, l);
        hinten("Kamerafahrt speichern", s -> { s.speichern(f); return null; }, x -> { ok("Kamerafahrt „" + name + "“ gespeichert."); neuLesen(); });
    }

    /** Taste P während der Aufnahme: Stützpunkt an der Stelle des Auges, Zeit seit Beginn. */
    private void stuetzpunkt() {
        if (aufnahmePunkte == null) return;
        double[] k = Zustand.kamera(r);
        double t = (System.nanoTime() - aufnahmeBeginn) / 1e9;
        if (!aufnahmePunkte.isEmpty() && t - aufnahmePunkte.get(aufnahmePunkte.size() - 1).zeitS() < 0.05) return;
        aufnahmePunkte.add(new Daten.Stuetzpunkt(aufnahmePunkte.size() + 1, Math.round(t * 1000) / 1000.0, k[0], k[1], k[2], k[3], k[4], k[5]));
        aufnahme.setText("Fertig (" + aufnahmePunkte.size() + ")");
    }

    // ------------------------------------------------------------------ Ansichten

    public void ansichtMerken() {
        LinkedHashMap<String, String> z = Zustand.erfassen(r, r.soundRunning());
        double[] k = Zustand.kamera(r);
        long zeit = (long) r.clock.epochMillis();
        String name = r.clock.label() + " · " + ort();
        String turmName = r.siteName;
        r.grabRequest = img -> {
            byte[] png = png(img);
            Daten.Ansicht a = new Daten.Ansicht(0, name.length() > 60 ? name.substring(0, 60) : name, zeit, k, turmName, Felder.alsText(z, "Lighthouse · Ansicht"), png);
            Long sz = Dienst.sitzung() >= 0 ? Dienst.sitzung() : null;
            hinten("Ansicht merken", s -> s.speichern(a, sz), id -> { ok("Ansicht gemerkt: " + a.name()); neuLesen(); });
        };
    }

    /** Vorschau 320 × 180 als PNG. */
    static byte[] png(BufferedImage img) {
        int w = 320, h = 180;
        BufferedImage s = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = s.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        double a = img.getWidth() / (double) img.getHeight(), b = w / (double) h;
        int sw = a > b ? (int) (img.getHeight() * b) : img.getWidth(), sh = a > b ? img.getHeight() : (int) (img.getWidth() / b);
        int sx = (img.getWidth() - sw) / 2, sy = (img.getHeight() - sh) / 2;
        g.drawImage(img, 0, 0, w, h, sx, sy, sx + sw, sy + sh, null);
        g.dispose();
        try {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(s, "png", o);
            return o.toByteArray();
        } catch (java.io.IOException e) {
            return null;
        }
    }

    private void vorschauLaden(long id) {
        hinten("Ansicht lesen", s -> s.ansicht(id), a -> {
            ImageIcon ic = null;
            if (a.vorschau() != null) {
                try { ic = new ImageIcon(javax.imageio.ImageIO.read(new ByteArrayInputStream(a.vorschau())).getScaledInstance(240, 135, java.awt.Image.SCALE_SMOOTH)); }
                catch (Exception e) { /* ohne Bild */ }
            }
            vorschau.setIcon(ic);
            ansichtInfo.setText(html(a.name() + (a.turm() != null && !a.turm().isEmpty() ? " · " + a.turm() : "")));
        });
    }

    void ansichtZeigen(long id) {
        hinten("Ansicht lesen", s -> s.ansicht(id), a -> herstellen(Felder.ausText(a.zustand()), "Ansicht „" + a.name() + "“"));
    }

    private void ansichtLoeschen() {
        if (!(ansicht.getSelectedItem() instanceof Daten.Ansicht a)) return;
        if (FOptionPane.showConfirmDialog(parent, "Ansicht „" + a.name() + "“ löschen?", "Lighthouse", FOptionPane.YES_NO_OPTION) != FOptionPane.YES_OPTION) return;
        hinten("Ansicht löschen", s -> { s.loeschenAnsicht(a.id()); return null; }, x -> { ok("Ansicht gelöscht."); neuLesen(); });
    }

    /** Einen gespeicherten Zustand herstellen: Turm, Qualität, Ton, dann alles andere. */
    void herstellen(Map<String, String> z, String was) {
        String tn = z.get("app.turm");
        Daten.Turm t = tn == null ? null : turm(tn);
        if (t != null && !tn.equals(r.siteName)) {
            try { TurmWahl.waehlen(r, t, tuerme); } catch (RuntimeException e) { Protokoll.warnung("Daten", "Turm " + tn + ": " + e.getMessage()); }
        }
        Quality q = Zustand.qualitaet(z);
        if (q != null && q != r.quality()) r.setQuality(q);
        List<String> f = Zustand.anwenden(r, z);
        boolean ton = !"false".equals(z.get("app.ton"));
        if (ton != r.soundRunning() && !"false".equals(System.getProperty("lighthouse.sound"))) { if (ton) r.startSound(); else r.stopSound(); }
        fuellt = true;
        try { setze(turm, tuerme, r.siteName); } finally { fuellt = false; }
        turmInfoSetzen();
        if (f.isEmpty()) ok(was + " hergestellt.");
        else {
            ok(was + " hergestellt; " + f.size() + (f.size() == 1 ? " Wert passte" : " Werte passten") + " nicht: " + String.join(", ", f.subList(0, Math.min(3, f.size()))));
            for (String x : f) Protokoll.warnung("Zustand", x);
        }
        if (onHergestellt != null) onHergestellt.run();
        view.requestFocusInWindow();
    }

    /** Nach dem Herstellen eines Zustands (das Bedienfeld stellt seine Regler nach). */
    Runnable onHergestellt;

    private void weiterWieZuletzt() {
        long jetzt = Dienst.sitzung();
        hinten("Letzte Sitzung lesen", s -> s.letzteSitzung(jetzt), ls -> {
            if (ls == null) { meldung("Es gibt noch keinen gesicherten Stand einer früheren Sitzung."); return; }
            herstellen(Felder.ausText(ls.zustand()), "Stand der Sitzung " + ls.id());
        });
    }

    // ------------------------------------------------------------------ Presets der Packages

    private String[] paketKlasse() {
        String k = Startdaten.PAKETE[Math.max(0, paket.getSelectedIndex())];
        int i = k.lastIndexOf('.');
        return new String[]{k.substring(0, i), k.substring(i + 1)};
    }

    /** Die Objekte in der Szene, die zu einer Package-Klasse gehören. */
    List<Object> ziele(String klasse) {
        List<Object> l = new ArrayList<>();
        switch (klasse) {
            case "Ocean" -> l.add(r.ocean);
            case "Spray" -> l.add(r.spray);
            case "Rain" -> l.add(r.rain);
            case "Wetglass" -> { l.addAll(List.of(r.glass)); l.add(r.rail); l.add(r.bridgeGlass); }
            case "Weathering" -> l.add(r.weathering);
            case "Lightning" -> l.add(r.lightning);
            case "Fog" -> l.add(r.fog);
            case "Buoyancy" -> l.add(r.buoyancy);
            case "Flock" -> l.add(r.flock);
            case "Soundscape" -> l.add(r.soundscape);
            default -> { }
        }
        return l;
    }

    private void presetListe() {
        String[] pk = paketKlasse();
        hinten("Presets lesen", s -> s.presets(pk[0], pk[1]), l -> {
            presets = l;
            setze(preset, l, null);
            if (!l.isEmpty() && preset.getSelectedItem() instanceof Daten.PaketPreset p) presetInfo.setText(html(p.beschreibung() + " · " + p.felder().size() + " Felder"));
        });
    }

    private static final Set<String> NICHT_LADEN = Set.of("quality");

    private void presetLaden() {
        if (!(preset.getSelectedItem() instanceof Daten.PaketPreset p)) return;
        List<String> f = new ArrayList<>();
        for (Object o : ziele(p.klasse())) f = Felder.schreiben(o, p.felder(), NICHT_LADEN);
        ok("Preset „" + p.name() + "“ in " + p.paket() + " geladen" + (f.isEmpty() ? "" : "; " + f.size() + " Felder passten nicht")
                + ". Was die Verdrahtung je Bild setzt (Wind, Seegang), kommt gleich wieder aus Wetter und Regie.");
    }

    private void presetSpeichern() {
        String[] pk = paketKlasse();
        List<Object> z = ziele(pk[1]);
        if (z.isEmpty()) return;
        String name = fragen("Name des Presets für " + pk[0] + "." + pk[1] + " (so, wie es gerade eingestellt ist):", null);
        if (name == null || name.isEmpty()) return;
        Daten.PaketPreset p = new Daten.PaketPreset(pk[0], pk[1], name, "Gespeichert am " + java.time.LocalDate.now(), Daten.EIGEN, Felder.lesen(z.get(0)));
        hinten("Preset speichern", s -> { s.speichern(p); return null; }, x -> { ok("Preset „" + name + "“ gespeichert."); presetListe(); });
    }

    private void presetLoeschen() {
        if (!(preset.getSelectedItem() instanceof Daten.PaketPreset p)) return;
        if (FOptionPane.showConfirmDialog(parent, "Preset „" + p.name() + "“ löschen?", "Lighthouse", FOptionPane.YES_NO_OPTION) != FOptionPane.YES_OPTION) return;
        hinten("Preset löschen", s -> { s.loeschenPreset(p.paket(), p.klasse(), p.name()); return null; }, x -> { ok("Preset gelöscht."); presetListe(); });
    }

    // ------------------------------------------------------------------ Einrichter und Übernahme

    private void einrichten(boolean nurPruefen) {
        DbSpeicher db = Startbild.datenbank();
        if (db == null) return;
        einrichtenBtn.setEnabled(false);
        pruefenBtn.setEnabled(false);
        meldung.setForeground(Theme.muted());
        meldung(nurPruefen ? "Prüfe das Schema …" : "Einrichter läuft …");
        Dienst.spaeter(() -> {
            Einrichter.Ergebnis e;
            try {
                e = nurPruefen ? db.pruefen() : db.einrichten((t, a) -> SwingUtilities.invokeLater(() -> meldung(String.format(Locale.GERMAN, "Einrichten %.0f %% · %s", a * 100, t))));
            } catch (Speicher.Fehler ex) {
                e = new Einrichter.Ergebnis(false, List.of("FEHLER " + ex.getMessage()), 0, 0, 1);
            }
            Startbild.letzteEinrichtung = e;
            for (String z : e.zeilen()) if (!z.startsWith("ok")) Protokoll.warnung("Einrichter", z);
            Protokoll.info("Einrichter", (nurPruefen ? "Prüfung: " : "Einrichter: ") + e.kurz());
            try { Startbild.stufe(db.stufe()); } catch (Speicher.Fehler ex) { /* bleibt */ }
            boolean wechsel = Startbild.stufe() >= Einrichter.VERSION && !Dienst.speicher().datenbank();
            if (wechsel) { Dienst.setzen(db); Dienst.neuBeginnen(); }
            Einrichter.Ergebnis fe = e;
            SwingUtilities.invokeLater(() -> {
                meldung.setForeground(fe.ok() ? new Color(150, 210, 160) : new Color(240, 170, 80));
                meldung((nurPruefen ? "Prüfung: " : "Einrichter: ") + fe.kurz() + (wechsel ? ". Ab jetzt wird in die Datenbank gespeichert." : ""));
                anzeigen();
                neuLesen();
            });
        });
    }

    private void uebernehmen() {
        Speicher s = Dienst.speicher();
        if (!s.datenbank()) return;
        hinten("Dateien übernehmen", x -> Uebernahme.dateienInDb(new DateiSpeicher(DateiSpeicher.ordner()), x), z -> { ok(z); neuLesen(); });
    }

    // ------------------------------------------------------------------ für das Bedienfeld

    JComponent[] turmTeile() { return new JComponent[]{turm, turmInfo}; }
}
