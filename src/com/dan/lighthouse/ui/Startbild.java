package com.dan.lighthouse.ui;

import com.dan.fbutton.FButton;
import com.dan.lighthouse.db.Daten;
import com.dan.lighthouse.db.DateiSpeicher;
import com.dan.lighthouse.db.Db;
import com.dan.lighthouse.db.DbSpeicher;
import com.dan.lighthouse.db.Dienst;
import com.dan.lighthouse.db.Einrichter;
import com.dan.lighthouse.db.Protokoll;
import com.dan.lighthouse.db.Speicher;
import com.dan.lighthouse.gl.Renderer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * Das Startbild vor der Szene (Phase 1): Symbol, Titel, Stand der Datenbank. Die Verbindung wird im Hintergrund
 * geprüft; fehlt das Passwort, fragt das Startbild danach und trägt es nach dem ersten erfolgreichen Verbinden in
 * {@code db/db.properties} ein. Ist alles klar (verbunden, keine Datenbank eingerichtet oder nicht erreichbar), geht
 * es nach 3 s von selbst weiter; jede Eingabe hält an.
 * <p>
 * Phase 15: Das Startbild zeigt den Stand der Daten (Türme, Wetterlagen, Regiebücher, Ansichten, letzte Sitzung) und
 * bietet „Weiter wie zuletzt“ an; ist die letzte Sitzung abgebrochen, sagt es das dazu. Fehlen die Tabellen, richtet
 * „Einrichten“ sie ein (F34 A). Ohne Datenbank gilt dasselbe für die Dateien in {@code daten/} (F40 A).
 */
public final class Startbild extends JDialog {
    public enum Lage { PRUEFT, BEREIT, KEIN_PASSWORT, KEINE_KONFIG, OFFLINE, AUS }

    private static final int SEKUNDEN = 3;
    private static final Color OK = new Color(150, 210, 160), WARN = new Color(230, 190, 120);
    private static volatile Lage lage = Lage.PRUEFT;
    private static volatile String meldung = "", kurz = "";
    /** Phase 15: Stufe des Schemas (−2 = unbekannt, 0 = leer, −1 = unvollständig, sonst LH_SCHEMA.VERSION). */
    private static volatile int stufe = -2;
    private static volatile DbSpeicher db;
    private static volatile Daten.Stand stand;
    /** Ob der Stand der Daten schon gelesen wurde (auch wenn es scheiterte); vorher geht es nicht von selbst weiter. */
    private static volatile boolean standFertig;
    /** Ergebnis des letzten Einrichter-Laufs (für den Block DATEN) oder null. */
    public static volatile Einrichter.Ergebnis letzteEinrichtung;
    /** Wahl im Startbild: weiter wie zuletzt. */
    public static volatile boolean weiterWieZuletzt;
    private volatile boolean richtetEin;

    private final JLabel dbLine = new JLabel(" "), noteLine = new JLabel(" "), countLine = new JLabel(" ");
    private final JPanel pwRow = new JPanel(new BorderLayout(8, 0));
    private final JPasswordField pw = new JPasswordField();
    private final FButton los = new FButton("Los"), ohne = new FButton("Ohne Datenbank"), weiter = new FButton("Weiter wie zuletzt"),
            einrichten = new FButton("Einrichten");
    private Timer timer;
    private int rest = SEKUNDEN, viertel;
    private boolean angehalten, fertig;
    private AWTEventListener aufmerken;

    public static Lage lage() { return lage; }
    /** „Datenbank: LIGHTHOUSE auf PDBORCL · Oracle 21“ oder der Grund, warum nicht. */
    public static String statuszeile() {
        return switch (lage) {
            case BEREIT -> "Datenbank: " + meldung + (stufe >= Einrichter.VERSION ? "" : stufe == -2 ? "" : " · nicht eingerichtet");
            case PRUEFT -> "Datenbank: wird geprüft …";
            case KEIN_PASSWORT -> "Datenbank: Passwort fehlt";
            case KEINE_KONFIG -> "Datenbank: keine db.properties";
            case OFFLINE -> "Datenbank: nicht erreichbar";
            case AUS -> "Datenbank: aus";
        };
    }

    private Startbild() {
        super((java.awt.Frame) null, "Lighthouse", true);
        setUndecorated(true);
        JPanel root = new JPanel(new BorderLayout(18, 14)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(18, 30, 52), 0, getHeight(), new Color(6, 9, 15)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.ROT);
                g2.fillRect(0, 0, getWidth(), 3);
                // Bänder wie am Schaft
                int y = getHeight() - 6, w = getWidth();
                Color[] c = {new Color(21, 22, 26), new Color(233, 230, 224), Theme.ROT, new Color(233, 230, 224), Theme.ROT, new Color(233, 230, 224)};
                for (int i = 0; i < c.length; i++) { g2.setColor(c[i]); g2.fillRect(w * i / c.length, y, w / c.length + 1, 6); }
            }
        };
        root.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 74, 92)),
                BorderFactory.createEmptyBorder(22, 26, 24, 28)));
        BufferedImage icon = AppIcon.load(128);
        JLabel il = new JLabel(new javax.swing.ImageIcon(icon));
        il.setVerticalAlignment(JLabel.TOP);
        root.add(il, BorderLayout.WEST);
        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("LIGHTHOUSE");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(Theme.GISCHT);
        JLabel sub = new JLabel("Roter Sand bei jedem Wetter · Außenweser, 53°51′ N 8°05′ E");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(Theme.MESSING);
        JLabel info = new JLabel("<html><body style='width:430px'>" + Renderer.VERSION + ": der Leuchtturm Roter Sand auf offener See. "
                + "Meer, Himmel, Wolken, Gischt, Regen, Scheiben, Feuer, Blitz, Wetter, Leben und Klang sind eigenständige Packages. "
                + "Wetterlagen, Regiebücher, Ansichten und Presets kommen aus der Datenbank oder, ohne sie, aus dem Ordner daten.</body></html>");
        info.setFont(new Font("SansSerif", Font.PLAIN, 12));
        info.setForeground(new Color(160, 174, 188));
        info.setBorder(BorderFactory.createEmptyBorder(10, 0, 8, 0));
        dbLine.setFont(new Font("SansSerif", Font.PLAIN, 13));
        noteLine.setFont(new Font("SansSerif", Font.PLAIN, 11));
        noteLine.setForeground(new Color(160, 174, 188));
        countLine.setFont(new Font("SansSerif", Font.PLAIN, 12));
        countLine.setForeground(Theme.MESSING);
        countLine.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        pwRow.setOpaque(false);
        pwRow.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
        pw.setBackground(new Color(6, 10, 16));
        pw.setForeground(Theme.GISCHT);
        pw.setCaretColor(Theme.GISCHT);
        pw.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(70, 86, 104)), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        FButton conn = new FButton("Verbinden");
        conn.addActionListener(e -> verbinden());
        pw.addActionListener(e -> verbinden());
        pwRow.add(pw, BorderLayout.CENTER);
        pwRow.add(conn, BorderLayout.EAST);
        pwRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        for (Component c : new Component[]{title, sub, info, dbLine, noteLine, pwRow, countLine}) {
            ((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
            mid.add(c);
        }
        root.add(mid, BorderLayout.CENTER);
        JPanel south = new JPanel(new java.awt.GridLayout(1, 0, 10, 0));
        south.setOpaque(false);
        los.addActionListener(e -> { weiterWieZuletzt = false; schliessen(); });
        weiter.addActionListener(e -> { weiterWieZuletzt = true; schliessen(); });
        einrichten.addActionListener(e -> einrichtenStarten());
        ohne.addActionListener(e -> {
            lage = Lage.AUS;
            meldung = "";
            Dienst.setzen(new DateiSpeicher(DateiSpeicher.ordner()));
            standLesen();
            aktualisieren();
            schliessen();
        });
        south.add(los);
        south.add(weiter);
        south.add(einrichten);
        south.add(ohne);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        setPreferredSize(new Dimension(820, 380));
        pack();
        setLocationRelativeTo(null);
        setIconImages(AppIcon.images());
        aufmerken = ev -> {
            if (!isShowing()) return;
            if (ev instanceof java.awt.event.MouseEvent && ev.getID() != java.awt.event.MouseEvent.MOUSE_PRESSED) return;
            if (ev instanceof KeyEvent ke) {
                if (ke.getID() != KeyEvent.KEY_PRESSED) return;
                if (ke.getKeyCode() == KeyEvent.VK_ENTER && !pwRow.isVisible()) { schliessen(); return; }
            }
            angehalten = true;
            countLine.setText(" ");
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(aufmerken, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
        aktualisieren();
    }

    /** Prüft die Datenbank im Hintergrund (auch ohne Startbild, etwa mit -Dlighthouse.splash=false). */
    public static void pruefenImHintergrund(Runnable danach) {
        Thread t = new Thread(() -> {
            pruefen(null);
            if (lage != Lage.BEREIT) dateienWaehlen();
            if (danach != null) SwingUtilities.invokeLater(danach);
        }, "Lighthouse-Datenbank");
        t.setDaemon(true);
        t.start();
    }

    /** Verbindung prüfen; mit pw wird das Passwort probiert und bei Erfolg gespeichert. */
    static void pruefen(char[] neuesPw) {
        lage = Lage.PRUEFT;
        Db.Konfig k;
        try {
            k = Db.konfig();
        } catch (Exception e) {
            lage = Lage.KEINE_KONFIG;
            meldung = e.getMessage();
            return;
        }
        kurz = k.kurz();
        if (neuesPw != null) k = k.mitPasswort(neuesPw);
        if (!k.hatPasswort()) {
            lage = Lage.KEIN_PASSWORT;
            meldung = "Passwort für " + k.kurz() + " eingeben; es wird nach dem Verbinden in db/db.properties gespeichert.";
            return;
        }
        try {
            String wer = Db.pruefen(k);
            if (neuesPw != null) {
                try {
                    Db.passwortSpeichern(k, neuesPw);
                } catch (Exception e) {
                    System.err.println("Passwort nicht gespeichert: " + e.getMessage());
                }
            }
            meldung = wer;
            schemaPruefen(k);
            lage = Lage.BEREIT;
        } catch (Exception e) {
            String m = String.valueOf(e.getMessage());
            boolean falsch = m.contains("ORA-01017");
            lage = falsch ? Lage.KEIN_PASSWORT : Lage.OFFLINE;
            meldung = falsch ? "Benutzername oder Passwort falsch (" + k.kurz() + ")." : "Keine Verbindung zu " + k.kurz() + ": " + m.split("\n")[0];
        }
    }

    /** Phase 15: Verbindung für die Daten öffnen, Stufe des Schemas lesen, Speicher wählen, Stand lesen. */
    private static void schemaPruefen(Db.Konfig k) {
        try {
            if (db != null) db.schliessen();
            db = new DbSpeicher(k);
            stufe = db.stufe();
        } catch (Speicher.Fehler e) {
            stufe = -2;
            db = null;
            Protokoll.fehler("Startbild", "Datenbank verbunden, Schema nicht lesbar: " + e.getMessage(), e);
        }
        if (db != null && stufe >= Einrichter.VERSION) Dienst.setzen(db);
        else Dienst.setzen(new DateiSpeicher(DateiSpeicher.ordner()));
        standLesen();
    }

    /** Stand der Daten aus dem gewählten Speicher (Dateien oder Datenbank). */
    static void standLesen() {
        try {
            stand = Dienst.speicher().stand();
        } catch (Speicher.Fehler e) {
            stand = null;
            Protokoll.fehler("Startbild", "Stand der Daten nicht lesbar: " + e.getMessage(), e);
        }
        standFertig = true;
    }

    /** Ohne Datenbank (keine Konfig, nicht erreichbar): die Dateien. */
    private static void dateienWaehlen() {
        Dienst.setzen(new DateiSpeicher(DateiSpeicher.ordner()));
        standLesen();
    }

    /** Die Verbindung für den Einrichter aus dem Block DATEN (oder null ohne Datenbank). */
    public static DbSpeicher datenbank() { return db; }
    public static int stufe() { return stufe; }
    /** Der gelesene Stand der Daten (null, solange er gelesen wird). */
    public static Daten.Stand stand() { return stand; }
    public static void stufe(int s) { stufe = s; }

    /** Zeigt das Startbild und wartet, bis es weitergeht. */
    public static void zeigen() {
        Startbild s = new Startbild();
        pruefenImHintergrund(s::aktualisieren);
        s.timer = new Timer(250, e -> s.tick());
        s.timer.start();
        s.setVisible(true);
    }

    private void verbinden() {
        char[] p = pw.getPassword();
        pw.setText("");
        if (p.length == 0) return;
        angehalten = false;
        rest = SEKUNDEN;
        Thread t = new Thread(() -> {
            pruefen(p);
            if (lage != Lage.BEREIT) dateienWaehlen();
            SwingUtilities.invokeLater(this::aktualisieren);
        }, "Lighthouse-Verbinden");
        t.setDaemon(true);
        t.start();
        aktualisieren();
    }

    private void aktualisieren() {
        Lage l = lage;
        dbLine.setText(statuszeile());
        dbLine.setForeground(l == Lage.BEREIT ? OK : l == Lage.PRUEFT ? new Color(160, 174, 188) : WARN);
        String m = l == Lage.PRUEFT || l == Lage.KEIN_PASSWORT ? meldung : standText();
        if (l != Lage.BEREIT && l != Lage.PRUEFT && l != Lage.KEIN_PASSWORT && meldung != null && !meldung.isEmpty()) m = meldung + "\n" + m;
        noteLine.setText("<html><body style='width:500px'>" + (m == null || m.isEmpty() ? " " : esc(m).replace("\n", "<br>")) + "</body></html>");
        pwRow.setVisible(l == Lage.KEIN_PASSWORT);
        ohne.setVisible(l == Lage.PRUEFT || l == Lage.KEIN_PASSWORT);
        Daten.Stand st = stand;
        weiter.setVisible(l != Lage.PRUEFT && l != Lage.KEIN_PASSWORT && st != null && st.letzte() != null && !richtetEin);
        einrichten.setVisible(l == Lage.BEREIT && db != null && stufe < Einrichter.VERSION);
        einrichten.setEnabled(!richtetEin);
        los.setEnabled(!richtetEin);
        revalidate();
        repaint();
        if (pwRow.isVisible()) pw.requestFocusInWindow();
    }

    private boolean laeuft() {
        Lage l = lage;
        // nicht von selbst weiter, solange der Einrichter läuft oder er gebraucht wird
        if (richtetEin || !standFertig || (l == Lage.BEREIT && db != null && stufe < Einrichter.VERSION)) return false;
        return !angehalten && (l == Lage.BEREIT || l == Lage.OFFLINE || l == Lage.KEINE_KONFIG || l == Lage.AUS);
    }

    /** „9 Türme · 10 Wetterlagen · … · Letzte Sitzung 16.10. 20:05, abgebrochen“ */
    static String standText() {
        Lage l = lage;
        StringBuilder b = new StringBuilder();
        if (l == Lage.BEREIT && db != null && stufe < Einrichter.VERSION) {
            b.append(stufe == 0 ? "Das Schema ist leer: Die Tabellen fehlen noch. " : "Das Schema ist nicht vollständig eingerichtet. ")
                    .append("„Einrichten“ führt die Skripte aus db/sql aus (Tabellen, Spatial, Sichten, Startdaten) und prüft danach alles. Es löscht nichts.\n")
                    .append("Bis dahin speichert die App in die Dateien im Ordner daten.");
            return b.toString();
        }
        Daten.Stand st = stand;
        if (st == null) return l == Lage.BEREIT ? "Daten werden gelesen …" : "";
        b.append(l == Lage.BEREIT ? "Eingerichtet: " : "Ohne Datenbank, gespeichert wird in daten: ");
        b.append(st.tuerme()).append(" Türme · ").append(st.wetterlagen()).append(" Wetterlagen · ").append(st.regiebuecher()).append(" Regiebücher · ")
                .append(st.fahrten()).append(" Kamerafahrten · ").append(st.ansichten()).append(st.ansichten() == 1 ? " Ansicht · " : " Ansichten · ")
                .append(st.presets()).append(" Presets");
        Daten.Sitzung s = st.letzte();
        if (s != null) {
            java.time.ZonedDateTime z = java.time.Instant.ofEpochMilli(s.zustandMs() != null ? s.zustandMs() : s.beginnMs()).atZone(java.time.ZoneId.systemDefault());
            b.append(String.format("\nLetzte Sitzung %d.%d. %02d:%02d", z.getDayOfMonth(), z.getMonthValue(), z.getHour(), z.getMinute()))
                    .append(s.turm() != null && !s.turm().isEmpty() ? ", " + s.turm() : "")
                    .append(s.sauber() ? ", geordnet beendet." : ", abgebrochen. „Weiter wie zuletzt“ nimmt den letzten gesicherten Stand.");
        }
        Einrichter.Ergebnis e = letzteEinrichtung;
        if (e != null) b.append("\nEinrichter: ").append(e.kurz());
        return b.toString();
    }

    /** Den Einrichter im Hintergrund laufen lassen; der Fortschritt steht in der untersten Zeile. */
    private void einrichtenStarten() {
        DbSpeicher d = db;
        if (d == null || richtetEin) return;
        richtetEin = true;
        angehalten = true;
        aktualisieren();
        Thread t = new Thread(() -> {
            Einrichter.Ergebnis e;
            try {
                e = d.einrichten((text, anteil) -> SwingUtilities.invokeLater(() ->
                        countLine.setText(String.format("Einrichten %.0f %% · %s", anteil * 100, text))));
            } catch (Speicher.Fehler ex) {
                e = new Einrichter.Ergebnis(false, java.util.List.of("FEHLER " + ex.getMessage()), 0, 0, 1);
            }
            letzteEinrichtung = e;
            for (String z : e.zeilen()) if (!z.startsWith("ok")) Protokoll.warnung("Einrichter", z);
            Protokoll.info("Einrichter", "Einrichter: " + e.kurz());
            try { stufe = d.stufe(); } catch (Speicher.Fehler ex) { stufe = -2; }
            if (stufe >= Einrichter.VERSION) Dienst.setzen(d);
            standLesen();
            Einrichter.Ergebnis fe = e;
            SwingUtilities.invokeLater(() -> {
                richtetEin = false;
                countLine.setForeground(fe.ok() ? OK : WARN);
                countLine.setText(fe.ok() ? "Einrichter: alles in Ordnung" : "Einrichter: " + fe.kurz());
                aktualisieren();
            });
        }, "Lighthouse-Einrichter");
        t.setDaemon(true);
        t.start();
    }

    private void tick() {
        if (++viertel % 4 != 0) return;
        if (!laeuft()) {
            rest = SEKUNDEN;
            if (!angehalten) countLine.setText(lage == Lage.PRUEFT ? "Datenbank wird geprüft …" : " ");
            return;
        }
        rest--;
        countLine.setText(rest > 0 ? "Weiter in " + rest + " s (jede Eingabe hält an)" : " ");
        if (rest <= 0) schliessen();
    }

    private void schliessen() {
        if (fertig) return;
        fertig = true;
        if (timer != null) timer.stop();
        Toolkit.getDefaultToolkit().removeAWTEventListener(aufmerken);
        dispose();
    }

    static String esc(String s) { return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    @Override public void paint(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paint(g);
    }
}
