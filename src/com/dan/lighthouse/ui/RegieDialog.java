package com.dan.lighthouse.ui;

import com.dan.fbutton.FButton;
import com.dan.ftextfield.FTextfield;
import com.dan.lighthouse.db.Daten;
import com.dan.lighthouse.db.Zahl;
import com.dan.weather.Scenario;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.function.BiConsumer;

/**
 * Phase 15: ein Regiebuch als Text bearbeiten (Format von {@code Scenario.toText}), prüfen, unter eigenem Namen
 * speichern und auf Wunsch gleich starten. Mitgelieferte Regiebücher bleiben, wie sie sind; ihre Abwandlung bekommt
 * einen eigenen Namen.
 */
final class RegieDialog {
    private RegieDialog() { }

    static void zeigen(Component parent, Daten.Regiebuch rb, BiConsumer<Daten.Regiebuch, Boolean> speichern) {
        Window w = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(w, "Regiebuch bearbeiten", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(Theme.bg());
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        boolean mit = rb != null && Daten.MITGELIEFERT.equals(rb.herkunft());
        FTextfield name = new FTextfield();
        name.setText(rb == null ? "Mein Regiebuch" : mit ? rb.name() + " (eigen)" : rb.name());
        FTextfield beschreibung = new FTextfield();
        beschreibung.setText(rb == null || rb.beschreibung() == null ? "" : rb.beschreibung());
        JTextArea text = new JTextArea(rb == null ? Scenario.stormDay().toText() : rb.text(), 18, 90);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        text.setBackground(new Color(6, 10, 16));
        text.setForeground(Theme.GISCHT);
        text.setCaretColor(Theme.GISCHT);
        text.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JLabel info = ControlPanel.small(" ");
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        for (Component c : new Component[]{ControlPanel.small("Name"), name, ControlPanel.small("Beschreibung"), beschreibung,
                ControlPanel.small("Je Zeile ein Schritt: Stunden | Lage | Text | GRÖSSE=Wert … | Kamera. Lagen: KLAR, AUFZIEHEND, FRONT, STURM, GEWITTER, "
                        + "ABKLINGEND, AUFKLAREND, NEBEL. Größen: WIND (m/s), DIRECTION, GUST, COVER (Achtel), HIGH, CLOUD (0–4), RAIN (mm/h), VISIBILITY (km), "
                        + "FOG (0–1), PRESSURE, TEMPERATURE, HUMIDITY, SWELL (m), THUNDER (0–4).")}) {
            ((javax.swing.JComponent) c).setAlignmentX(0);
            top.add(c);
        }
        root.add(top, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(text);
        sp.setBorder(BorderFactory.createLineBorder(new Color(60, 74, 92)));
        root.add(sp, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout(8, 6));
        south.setOpaque(false);
        south.add(info, BorderLayout.NORTH);
        JPanel bt = new JPanel(new GridLayout(1, 0, 8, 0));
        bt.setOpaque(false);
        FButton pruefen = new FButton("Prüfen"), sp1 = new FButton("Speichern"), sp2 = new FButton("Speichern und starten"), ab = new FButton("Abbrechen");
        Runnable check = () -> {
            try {
                Scenario s = Scenario.parse(text.getText());
                info.setForeground(new Color(150, 210, 160));
                info.setText("In Ordnung: „" + s.name() + "“, " + s.steps().size() + " Schritte, " + Zahl.text(Math.round(s.hours() * 10) / 10.0)
                        + " Stunden, im Zeitraffer " + Zahl.text(Math.round(s.hours() * 3600 / Math.max(1, s.lapse()) / 6) / 10.0) + " Minuten.");
            } catch (RuntimeException e) {
                info.setForeground(new Color(240, 170, 80));
                info.setText("Fehler: " + e.getMessage());
            }
        };
        pruefen.addActionListener(e -> check.run());
        java.util.function.Consumer<Boolean> save = start -> {
            String n = name.getText().strip();
            if (n.isEmpty()) { info.setText("Bitte einen Namen eingeben."); return; }
            if (mit && n.equals(rb.name())) { info.setForeground(new Color(240, 170, 80)); info.setText("„" + n + "“ ist mitgeliefert. Bitte einen eigenen Namen wählen."); return; }
            String t;
            try {
                Scenario s = Scenario.parse(text.getText());
                // der Name im Text folgt dem Feld Name
                t = new Scenario(n.length() > 60 ? n.substring(0, 60) : n, s.startHour(), s.lapse(), s.steps()).toText();
            } catch (RuntimeException e) {
                check.run();
                return;
            }
            d.dispose();
            speichern.accept(new Daten.Regiebuch(n, t, beschreibung.getText().strip(), Daten.EIGEN), start);
        };
        sp1.addActionListener(e -> save.accept(false));
        sp2.addActionListener(e -> save.accept(true));
        ab.addActionListener(e -> d.dispose());
        bt.add(pruefen);
        bt.add(sp1);
        bt.add(sp2);
        bt.add(ab);
        south.add(bt, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);
        d.setContentPane(root);
        d.setPreferredSize(new Dimension(980, 620));
        d.pack();
        d.setLocationRelativeTo(w);
        check.run();
        d.setVisible(true);
    }
}
