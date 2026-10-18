package com.dan.lighthouse.app;

import com.dan.fframe.FFrame;
import com.dan.foptionpane.FOptionPane;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.gl.SceneView;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.ui.AppIcon;
import com.dan.lighthouse.ui.ControlPanel;
import com.dan.lighthouse.ui.Startbild;
import com.dan.lighthouse.ui.Theme;
import com.dan.lighthouse.wiring.Wiring;
import com.jogamp.opengl.GLProfile;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Rectangle;

/**
 * Lighthouse — der Rote Sand bei jedem Wetter, als 3D-Szene auf der Grafikkarte (JOGL, OpenGL 4.3).
 * <p>
 * Einstieg: Startbild, dann FFrame beim Start maximiert (Taskleiste bleibt frei), Größe frei veränderbar; Szene in
 * der Mitte, Bedienfeld rechts, Statuszeile unten. Die Naturelemente kommen als eigenständige Packages unter
 * {@code com.dan} dazu; diese App verdrahtet sie nur ({@link Wiring}).
 * <p>
 * Schalter: {@code -Dlighthouse.splash=false} ohne Startbild, {@code -Dlighthouse.gljpanel=true} leichter
 * GL-Rückfall, {@code -Dlighthouse.shaders=<ordner>} Shader von der Platte (F5 lädt neu).
 */
public final class LighthouseApp {
    /** Für Werkzeuge: Fenster und Szene, sobald sie stehen. */
    public static volatile FFrame frame;
    public static volatile SceneView scene;
    public static volatile ControlPanel controls;

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        // Der GL-Canvas ist ein schweres AWT-Element: Menüs und Tooltips müssen darüber liegen können
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            com.dan.lighthouse.db.Protokoll.fehler("Faden " + t.getName(), String.valueOf(e), e);
        });
        GLProfile.initSingleton();
        SwingUtilities.invokeLater(LighthouseApp::open);
    }

    private static void open() {
        if ("false".equals(System.getProperty("lighthouse.splash"))) Startbild.pruefenImHintergrund(null);
        else Startbild.zeigen();
        SceneClock clock = new SceneClock();
        Wiring wiring = new Wiring(clock);
        Renderer rd = new Renderer(clock, wiring);
        FFrame f = new FFrame("Lighthouse · Roter Sand bei jedem Wetter · " + Renderer.VERSION);
        AppIcon.install(f);
        if (!Boolean.getBoolean("lighthouse.gljpanel")) makeOpaque(f);
        SceneView view;
        try {
            view = new SceneView(rd);
        } catch (RuntimeException e) {
            FOptionPane.showMessageDialog(null, "OpenGL lässt sich nicht starten:\n" + e.getMessage()
                    + "\n\nBitte den Grafiktreiber aktualisieren. Als Rückfall: Start mit -Dlighthouse.gljpanel=true.", "Lighthouse", FOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        ControlPanel controls = new ControlPanel(view, wiring);
        JLabel status = new JLabel("Die See wird angelegt …");
        status.setForeground(new Color(170, 182, 194));
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        status.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        JLabel dbStatus = new JLabel(Startbild.statuszeile());
        dbStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dbStatus.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        Runnable dbFarbe = () -> {
            dbStatus.setText(Startbild.statuszeile());
            dbStatus.setForeground(Startbild.lage() == Startbild.Lage.BEREIT ? new Color(150, 210, 160) : new Color(230, 190, 120));
        };
        dbFarbe.run();
        new javax.swing.Timer(2000, e -> dbFarbe.run()).start();
        rd.setStatusListener(s -> SwingUtilities.invokeLater(() -> status.setText(s)));
        rd.setErrorListener(msg -> SwingUtilities.invokeLater(() -> FOptionPane.showMessageDialog(f, msg, "Lighthouse · Grafik", FOptionPane.ERROR_MESSAGE)));

        JPanel root = f.getComponentPane();
        root.setLayout(new BorderLayout());
        root.setBackground(Theme.bg());
        root.add(view, BorderLayout.CENTER);
        JScrollPane side = new JScrollPane(controls, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        side.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(36, 50, 66)));
        side.getViewport().setBackground(Theme.bg());
        side.setBackground(Theme.bg());
        try {
            com.dan.fscrollbar.FScrollBar bar = new com.dan.fscrollbar.FScrollBar(javax.swing.JScrollBar.VERTICAL);
            bar.setUnitIncrement(16);
            bar.setBlockIncrement(120);
            side.setVerticalScrollBar(bar);
        } catch (RuntimeException | LinkageError e) {
            side.getVerticalScrollBar().setUnitIncrement(16);
        }
        side.setPreferredSize(new Dimension(320, 600));
        root.add(side, BorderLayout.EAST);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(Theme.status());
        south.add(status, BorderLayout.CENTER);
        south.add(dbStatus, BorderLayout.EAST);
        root.add(south, BorderLayout.SOUTH);

        f.setPreferredFrameSize(new Dimension(1480, 900));
        f.setSize(1480, 900);
        f.setMinimumSize(new Dimension(800, 520));
        f.setLocationRelativeTo(null);
        f.setResizable(true);
        f.setVisible(true);
        startMaximized(f);
        view.start();
        // Phase 14: der Klang (ausschalten mit -Dlighthouse.sound=false oder im Bedienfeld)
        if (!"false".equals(System.getProperty("lighthouse.sound"))) rd.startSound();
        // Phase 15: Daten, Sitzung, „Weiter wie zuletzt“; beim Ende den letzten Stand sichern
        controls.datenStarten(Startbild.weiterWieZuletzt);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { com.dan.lighthouse.db.Dienst.beenden(); } catch (RuntimeException e) { e.printStackTrace(); }
            rd.stopSound();
            view.stop();
        }, "Lighthouse-Ende"));
        // Kinomodus (Rückfluss aus Glacier Phase 16): Bedienfeld, Statuszeile und Titelleiste weg, ganzer Bildschirm
        rd.kinoFenster = an -> SwingUtilities.invokeLater(() -> kinoFenster(f, view, side, south, root, an));
        if (Startbild.kino != null) {
            // nach den Daten und dem ersten Bild (der Vorspann deckt den Rest)
            com.dan.weather.Scenario sc = Startbild.kino;
            long bis = System.currentTimeMillis() + 60_000;
            javax.swing.Timer kt = new javax.swing.Timer(250, null);
            kt.addActionListener(e -> {
                if (rd.frames() < 30 && System.currentTimeMillis() < bis) return;
                kt.stop();
                controls.kinoStarten(sc);
            });
            kt.start();
        }
        // F11: nur die Szene (Bedienfeld und Statuszeile weg und wieder da)
        view.setKeyAction(java.awt.event.KeyEvent.VK_F11, () -> SwingUtilities.invokeLater(() -> {
            boolean an = !side.isVisible();
            side.setVisible(an);
            south.setVisible(an);
            root.revalidate();
        }));
        view.requestFocusInWindow();
        frame = f;
        scene = view;
        LighthouseApp.controls = controls;
    }

    /**
     * Startet maximiert (MAXIMIZED_BOTH), ohne die Taskleiste zu verdecken. Die normale Größe von 1480 × 900 bleibt
     * als Rückfall; „Wiederherstellen“ des FFrame stellt sie wieder her (wie in Kīlauea, Kneiphof und Geyser).
     */
    private static void startMaximized(FFrame f) {
        Rectangle normal = f.getBounds();
        f.setMaximizedBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        f.setExtendedState(f.getExtendedState() | Frame.MAXIMIZED_BOTH);
        try {
            // Die Titelleiste des FFrame führt einen eigenen Maximiert-Zustand; ihn angleichen
            for (java.lang.reflect.Field fd : FFrame.class.getDeclaredFields()) {
                if (!fd.getType().getSimpleName().equals("FTaskbar")) continue;
                fd.setAccessible(true);
                Object bar = fd.get(f);
                Class<?> bc = bar.getClass();
                java.lang.reflect.Field mx = bc.getDeclaredField("maximized"), rb = bc.getDeclaredField("restoreBounds"),
                        btn = bc.getDeclaredField("btnMaximize");
                mx.setAccessible(true);
                rb.setAccessible(true);
                btn.setAccessible(true);
                mx.setBoolean(bar, true);
                rb.set(bar, normal);
                Object icon = btn.get(bar);
                icon.getClass().getMethod("setType", com.dan.ficons.FIconType.class).invoke(icon, com.dan.ficons.FIconType.RESTORE);
                ((javax.swing.JComponent) icon).setToolTipText("Wiederherstellen");
                ((java.awt.Component) icon).addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                        SwingUtilities.invokeLater(() -> {
                            if ((f.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0) {
                                f.setExtendedState(f.getExtendedState() & ~Frame.MAXIMIZED_BOTH);
                                f.setBounds(normal);
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("FFrame-Titelleiste nicht angeglichen: " + e);
        }
    }

    /**
     * Das FFrame ist ein durchscheinendes Fenster (runde Ecken). Darin zeigt Windows schwere Elemente wie den
     * GL-Canvas nicht; darum wird es deckend gemacht und die Rundung als Fensterform gesetzt.
     */
    private static void makeOpaque(FFrame f) {
        f.setBackground(Theme.bg());
        java.awt.event.ComponentAdapter shape = new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { reshape(f); }
            @Override public void componentShown(java.awt.event.ComponentEvent e) { reshape(f); }
        };
        f.addComponentListener(shape);
        f.addWindowStateListener(e -> reshape(f));
    }

    private static boolean kinoVoll, kinoSeite = true, kinoSueden = true;
    private static int kinoZustand;
    private static java.awt.Rectangle kinoRahmen;

    /** Kinomodus: Vollbild und zurück. */
    private static void kinoFenster(FFrame f, SceneView view, java.awt.Component side, java.awt.Component south, JPanel root, boolean an) {
        if (an == kinoVoll) return;
        java.awt.Component bar = taskbar(f);
        if (an) {
            kinoSeite = side.isVisible();
            kinoSueden = south.isVisible();
            kinoZustand = f.getExtendedState();
            kinoRahmen = f.getBounds();
            kinoVoll = true;
            side.setVisible(false);
            south.setVisible(false);
            if (bar != null) bar.setVisible(false);
            java.awt.Rectangle schirm = f.getGraphicsConfiguration().getBounds();
            f.setExtendedState(Frame.NORMAL);
            f.setBounds(schirm);
            reshape(f);
            view.zeigerVerbergen(true);
            f.toFront();
        } else {
            kinoVoll = false;
            side.setVisible(kinoSeite);
            south.setVisible(kinoSueden);
            if (bar != null) bar.setVisible(true);
            if ((kinoZustand & Frame.MAXIMIZED_BOTH) != 0) f.setExtendedState(kinoZustand);
            else if (kinoRahmen != null) f.setBounds(kinoRahmen);
            reshape(f);
            view.zeigerVerbergen(false);
        }
        root.revalidate();
        root.repaint();
        view.requestFocusInWindow();
    }

    /** Die Titelleiste des FFrame (FTaskbar) oder null. */
    private static java.awt.Component taskbar(FFrame f) {
        try {
            for (java.lang.reflect.Field fd : FFrame.class.getDeclaredFields()) {
                if (!fd.getType().getSimpleName().equals("FTaskbar")) continue;
                fd.setAccessible(true);
                return (java.awt.Component) fd.get(f);
            }
        } catch (Exception e) {
            System.err.println("FFrame-Titelleiste nicht gefunden: " + e);
        }
        return null;
    }

    private static void reshape(FFrame f) {
        try {
            boolean max = kinoVoll || (f.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
            f.setShape(max ? null : new java.awt.geom.RoundRectangle2D.Double(0, 0, f.getWidth(), f.getHeight(), 20, 20));
        } catch (UnsupportedOperationException | IllegalComponentStateException e) {
            // Form nicht möglich: dann eben eckig
        }
    }

    private LighthouseApp() { }
}
