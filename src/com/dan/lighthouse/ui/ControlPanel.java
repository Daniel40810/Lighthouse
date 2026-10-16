package com.dan.lighthouse.ui;

import com.dan.fbutton.FButton;
import com.dan.fcheckbox.FCheckBox;
import com.dan.fcollapsiblepanel.FCollapsiblePanel;
import com.dan.fcombobox.FComboBox;
import com.dan.fslider.FSlider;
import com.dan.lighthouse.camera.Camera;
import com.dan.lighthouse.gl.ComputeProbe;
import com.dan.lighthouse.gl.Quality;
import com.dan.lighthouse.gl.Renderer;
import com.dan.lighthouse.gl.SceneView;
import com.dan.lighthouse.scene.SceneClock;
import com.dan.lighthouse.scene.Site;
import com.dan.lighthouse.scene.Sun;
import com.dan.lighthouse.wiring.Beaufort;
import com.dan.lighthouse.wiring.Wiring;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import com.dan.lighthouse.scene.Turm;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Das Bedienfeld rechts, fertig geordnet nach Werkbuch Abschnitt 7 (Phase 15, F42 A): acht Klappblöcke WETTER, ZEIT,
 * KAMERA, LEUCHTFEUER, BILD, LEBEN, DATEN und TECHNIK. Offen bleibt, was zuletzt offen war (je Rechner gemerkt).
 * Jeder Regler schreibt direkt in die öffentlichen Felder von Uhr, Kamera, Verdrahtung und Renderer; es gibt keinen
 * zweiten Zustand. Was mit gespeicherten Daten zu tun hat, macht {@link DatenSteuerung}.
 * <p>
 * Tasten: V Ansicht merken, P Stützpunkt bei der Aufnahme einer Fahrt, F1 Tastenübersicht, F11 nur die Szene,
 * Leertaste Regie an/aus, dazu die Tasten der Szene (F frei, R kreisen, H HUD, F12 Foto, Pos1 Grundansicht).
 */
public final class ControlPanel extends JPanel implements javax.swing.Scrollable {
    private final SceneView view;
    private final Renderer r;
    private final SceneClock clock;
    private final Wiring wiring;
    final DatenSteuerung daten;
    private final JLabel dateLbl = label(""), timeLbl = label(""), sunLbl = small(""), windLbl = label(""), dirLbl = label(""),
            camLbl = small(""), seaLbl = small(""), seaCheckLbl = small(""), skyLbl = small(""), skyCheckLbl = small(""), visLbl = label(""), coverLbl = label(""),
            cloudLbl = small(""), cloudCheckLbl = small(""), towerLbl = small(""), innerLbl = small(""), towerCheckLbl = small(""), sprayLbl = small(""), sprayCheckLbl = small(""), sprayStrLbl = label(""), rainLbl = small(""), rainCheckLbl = small(""), rainRateLbl = label(""), rainStrLbl = label(""), glassLbl = small(""), lifeLbl = small(""), soundLbl = small(""), soundVolLbl = label(""), fogLbl = small(""), fogStrLbl = label(""), glassCheckLbl = small(""), glassStrLbl = label(""), beaconLbl = small(""), beaconFeedback = small(""), beaconCheckLbl = small(""), stormLbl = small(""), stormCheckLbl = small(""), beamStrLbl = label(""), levelLbl = label(""), rustLbl = label(""),
            algaeLbl = label(""), saltLbl = label(""), highLbl = label(""), swellLbl = label(""), perfLbl = small(""), computeLbl = small(""), glLbl = small(""), linksLbl = small(""), dbLbl = small("");
    private final FSlider time = new FSlider(0, 24 * 60 - 1, 20 * 60), wind = new FSlider(0, 12, 4), windDir = new FSlider(0, 359, 240),
            swellH = new FSlider(0, 40, 6), swellT = new FSlider(60, 160, 95), swellDir = new FSlider(0, 359, 290),
            vis = new FSlider(2, 100, 40), cover = new FSlider(0, 80, 30), high = new FSlider(0, 100, 0), bow = new FSlider(0, 100, 0),
            sprayStr = new FSlider(0, 200, 100), rainRate = new FSlider(0, 100, 0), rainStr = new FSlider(0, 200, 100), glassStr = new FSlider(0, 100, 100), beamStr = new FSlider(0, 300, 100), level = new FSlider(-20, 20, 0), rust = new FSlider(0, 100, 50), algae = new FSlider(0, 100, 80), salt = new FSlider(0, 100, 20), fogSl = new FSlider(0, 100, 0), soundVol = new FSlider(0, 200, 100);
    private final FCheckBox free = new FCheckBox("Frei fliegen (F)"), orbit = new FCheckBox("Um den Turm kreisen (R)"),
            towerBox = new FCheckBox("Turm"), tideBox = new FCheckBox("Gezeiten rechnen"),
            lampBox = new FCheckBox("Raumlampe im Laternenraum"), turnBox = new FCheckBox("Linse dreht"), beamBox = new FCheckBox("Strahl zeigen"),
            neighbourBox = new FCheckBox("Nachbarfeuer"), sprayBox = new FCheckBox("Gischt"), rainBox = new FCheckBox("Regen"), glassBox = new FCheckBox("Nasse Scheiben"), fogBox = new FCheckBox("Beschlag innen");
    private boolean fromScene;
    private FComboBox cloudKind, stormBox, lapseBox, qualBox, hudBox;
    private final JLabel hudLbl = small("");
    private final JLabel weatherLbl = small(""), baroLbl = label("");
    private final FCheckBox regieBox = new FCheckBox("Regie läuft (Leertaste)"), followBox = new FCheckBox("Kamera folgt dem Regiebuch"),
            autoBox = new FCheckBox("Auflösung selbst regeln");
    private final FSlider scaleSl = new FSlider(50, 100, 100);
    private final JLabel scaleLbl = label("");
    private final com.dan.ftextfield.FTextfield kennung = new com.dan.ftextfield.FTextfield();
    private int lastStep = -2, ticks;
    private JPanel target = this;
    private static final Preferences PREFS = prefs();
    /** Die Klappblöcke mit ihrem Inhalt; der Inhalt kommt erst hinein, wenn er fertig ist (sonst misst der Block ihn leer). */
    private final java.util.List<Object[]> bloecke = new java.util.ArrayList<>();
    private final java.util.List<FCollapsiblePanel> zu = new java.util.ArrayList<>();

    /** Die Ansichten auf den Tasten 1 bis 9 (Werkbuch Abschnitt 7). */
    public static final String[] ANSICHTEN = {"Aussen1", "Drohne", "Brecher", "Sturmnacht", "Aussen3", "Laternenraum", "Regenbogen", "Möwenflug", "Schiffsbrücke"};

    private static Preferences prefs() {
        try { return Preferences.userRoot().node("com/dan/lighthouse/bedienfeld"); } catch (RuntimeException | Error e) { return null; }
    }

    public ControlPanel(SceneView view, Wiring wiring) {
        this.view = view;
        this.r = view.renderer();
        this.clock = r.clock;
        this.wiring = wiring;
        this.daten = new DatenSteuerung(r, view, this);
        setBackground(Theme.bg());
        setOpaque(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 8, 16, 10));
        daten.setRegieStarter(this::startScenario);
        daten.onHergestellt = () -> { lastStep = -2; syncFromScene(); qualBox.setSelectedItem(r.quality()); kennung.setText(r.beacon.characteristic().text); };

        // ============================================================== WETTER
        block("WETTER", true);
        wiring.regie = true;
        if (wiring.weather.snapshot() == null) wiring.weather.startFree(clock.epochMillis() / 1000.0, com.dan.weather.Weather.Phase.KLAR);
        put(weatherLbl);
        put(baroLbl);
        sub("Regiebuch");
        put(daten.regiebuch);
        JPanel rg = grid(2);
        rg.add(daten.regieStart);
        rg.add(daten.regieBearbeiten);
        put(rg);
        JPanel regieButtons = grid(2);
        FButton freeRegie = new FButton("Freie Regie"), resume = new FButton("Regie weiter");
        freeRegie.addActionListener(e -> {
            com.dan.weather.Weather.Snapshot ws = wiring.weather.snapshot();
            wiring.weather.startFree(clock.epochMillis() / 1000.0, ws != null ? ws.phase() : com.dan.weather.Weather.Phase.KLAR);
            if (lapseBox.getSelectedIndex() == 4) lapseBox.setSelectedIndex(1);
            view.requestFocusInWindow();
        });
        resume.addActionListener(e -> { wiring.resumeRegie(); syncFromScene(); view.requestFocusInWindow(); });
        regieButtons.add(freeRegie);
        regieButtons.add(resume);
        put(regieButtons);
        regieBox.setSelected(true);
        regieBox.addActionListener(e -> { wiring.weather.running = regieBox.isSelected(); view.requestFocusInWindow(); });
        view.setKeyAction(java.awt.event.KeyEvent.VK_SPACE, () -> javax.swing.SwingUtilities.invokeLater(() -> {
            wiring.weather.running = !wiring.weather.running;
            regieBox.setSelected(wiring.weather.running);
        }));
        put(regieBox);
        put(followBox);
        sub("Wetterlage");
        put(daten.wetterlage);
        put(daten.wetterInfo);
        JPanel wl = grid(2);
        wl.add(daten.wetterAn);
        wl.add(daten.wetterSpeichern);
        put(wl);
        put(small("<html><body style='width:215px'>Die Regie wechselt zwischen acht Lagen und setzt Wind, Böen, Wolken, Regen, Sicht, Gewitter, "
                + "Dünung, Temperatur und Luftdruck; das Barometer fällt Stunden vor dem Sturm. Eine Wetterlage und jeder Regler unten "
                + "überschreiben ihre Größen, „Regie weiter“ gibt sie zurück.</body></html>"));

        sub("Wind");
        put(windLbl);
        put(wind);
        put(dirLbl);
        put(windDir);
        wind.addChangeListener(e -> { if (!fromScene) wiring.hand(com.dan.weather.Weather.Var.WIND, Beaufort.mid(wind.getValue())); labels(); });
        windDir.addChangeListener(e -> { if (!fromScene) wiring.hand(com.dan.weather.Weather.Var.DIRECTION, windDir.getValue()); labels(); });

        sub("See");
        put(seaLbl);
        put(swellLbl);
        put(swellH);
        put(swellT);
        put(swellDir);
        swellH.addChangeListener(e -> { if (!fromScene) wiring.hand(com.dan.weather.Weather.Var.SWELL, swellH.getValue() / 10.0); labels(); });
        swellT.addChangeListener(e -> { if (!fromScene) wiring.swellPeriodHand = swellT.getValue() / 10.0; labels(); });
        swellDir.addChangeListener(e -> { if (!fromScene) wiring.swellDirHand = swellDir.getValue(); labels(); });
        JPanel sea = grid(2);
        FButton breaker = new FButton("Brecher"), stormPic = new FButton("Sturmbild");
        breaker.addActionListener(e -> {
            // Ringwellen rund um den Sockel und ein Schwall Gischt auf der Wetterseite
            double from = Math.toRadians(wiring.wind.fromDeg());
            for (int i = 0; i < 8; i++) {
                double a = from + (i - 3.5) * 0.25 + Math.random() * 0.1, rr = Turm.SOCKEL_R0 + 0.6;
                r.ocean.addImpulse(Math.sin(a) * rr, -Math.cos(a) * rr, 0.7 + Math.random() * 0.3);
            }
            r.breaker();
            view.requestFocusInWindow();
        });
        stormPic.setToolTipText("Bft 10, Gewitterwolken, Sicht 15 km");
        stormPic.addActionListener(e -> {
            wind.setValue(10);
            cloudPreset(com.dan.cloud.CloudType.CUMULONIMBUS, 7, 0.4, cloudKind);
            vis.setValue(15);
            view.requestFocusInWindow();
        });
        sea.add(breaker);
        sea.add(stormPic);
        put(sea);

        sub("Himmel und Wolken");
        put(skyLbl);
        put(visLbl);
        put(vis);
        vis.addChangeListener(e -> { if (!fromScene) { wiring.hand(com.dan.weather.Weather.Var.FOG, 0); wiring.hand(com.dan.weather.Weather.Var.VISIBILITY, vis.getValue()); } labels(); });
        put(cloudLbl);
        put(coverLbl);
        put(cover);
        cover.addChangeListener(e -> { if (!fromScene) wiring.hand(com.dan.weather.Weather.Var.COVER, cover.getValue() / 10.0); labels(); });
        FComboBox kind = new FComboBox(com.dan.cloud.CloudType.values());
        cloudKind = kind;
        kind.setSelectedItem(wiring.cloudType);
        kind.addActionListener(e -> {
            if (!fromScene) wiring.hand(com.dan.weather.Weather.Var.CLOUD, com.dan.weather.Weather.CloudKind.valueOf(((com.dan.cloud.CloudType) kind.getSelectedItem()).name()).ordinal());
            view.requestFocusInWindow();
        });
        put(kind);
        put(highLbl);
        put(high);
        high.addChangeListener(e -> { if (!fromScene) wiring.hand(com.dan.weather.Weather.Var.HIGH, high.getValue() / 100.0); labels(); });
        JPanel presets = grid(3);
        FButton fair = new FButton("Schön"), grey = new FButton("Grau"), storm = new FButton("Gewitter");
        fair.addActionListener(e -> cloudPreset(com.dan.cloud.CloudType.CUMULUS, 3, 0, kind));
        grey.addActionListener(e -> cloudPreset(com.dan.cloud.CloudType.STRATOCUMULUS, 8, 0.3, kind));
        storm.addActionListener(e -> cloudPreset(com.dan.cloud.CloudType.CUMULONIMBUS, 6, 0.5, kind));
        presets.add(fair); presets.add(grey); presets.add(storm);
        put(presets);
        JPanel shows = grid(2);
        FCheckBox stars = new FCheckBox("Sterne"), planets = new FCheckBox("Planeten"), milky = new FCheckBox("Milchstraße"), figures = new FCheckBox("Sternbilder");
        stars.setSelected(true);
        planets.setSelected(true);
        milky.setSelected(true);
        stars.addActionListener(e -> { r.sky.stars = stars.isSelected(); view.requestFocusInWindow(); });
        planets.addActionListener(e -> { r.sky.planets = planets.isSelected(); view.requestFocusInWindow(); });
        milky.addActionListener(e -> { r.sky.milkyWay = milky.isSelected(); view.requestFocusInWindow(); });
        figures.addActionListener(e -> { r.sky.figures = figures.isSelected(); view.requestFocusInWindow(); });
        shows.add(stars); shows.add(planets);
        put(shows);
        JPanel shows2 = grid(2);
        shows2.add(milky); shows2.add(figures);
        put(shows2);
        put(label("Regenbogen (sonst nach einem Schauer)"));
        put(bow);
        bow.addChangeListener(e -> { if (!fromScene) wiring.rainbowHand = bow.getValue() / 100.0; });

        sub("Regen");
        put(rainLbl);
        put(rainRateLbl);
        put(rainRate);
        // logarithmisch: 1 → 0,1 mm/h, 100 → 100 mm/h, 0 = trocken
        rainRate.addChangeListener(e -> { if (!fromScene) wiring.hand(com.dan.weather.Weather.Var.RAIN, rainOf(rainRate.getValue())); labels(); });
        JPanel rp = grid(2);
        FButton shower = new FButton("Schauer"), steady = new FButton("Landregen");
        shower.addActionListener(e -> {
            cloudPreset(com.dan.cloud.CloudType.CUMULONIMBUS, 7, 0.3, cloudKind);
            rainRate.setValue(sliderOf(15));
            view.requestFocusInWindow();
        });
        steady.addActionListener(e -> {
            cloudPreset(com.dan.cloud.CloudType.NIMBOSTRATUS, 8, 0, cloudKind);
            rainRate.setValue(sliderOf(3));
            vis.setValue(12);
            view.requestFocusInWindow();
        });
        rp.add(shower);
        rp.add(steady);
        put(rp);

        sub("Nebel");
        put(fogLbl);
        put(fogStrLbl);
        put(fogSl);
        fogSl.addChangeListener(e -> { if (!fromScene) wiring.hand(com.dan.weather.Weather.Var.FOG, fogSl.getValue() / 100.0); labels(); });

        sub("Gewitter");
        put(stormLbl);
        com.dan.fcombobox.FComboBox stormLevel = new com.dan.fcombobox.FComboBox(new Object[]{"kein Gewitter", "Wetterleuchten am Horizont", "mäßig", "kräftig", "schwer"});
        stormBox = stormLevel;
        stormLevel.addActionListener(e -> {
            if (!fromScene) wiring.hand(com.dan.weather.Weather.Var.THUNDER, stormLevel.getSelectedIndex());
            view.requestFocusInWindow();
        });
        put(stormLevel);
        JPanel bp = grid(2);
        FButton bolt = new FButton("Blitz"), tower = new FButton("In den Turm"), ic = new FButton("Wolkenblitz"), sheet = new FButton("Wetterleuchten");
        bolt.addActionListener(e -> { r.triggerLightning(com.dan.lightning.Bolt.Kind.CLOUD_GROUND, false); view.requestFocusInWindow(); });
        tower.addActionListener(e -> { r.triggerLightning(com.dan.lightning.Bolt.Kind.CLOUD_GROUND, true); view.requestFocusInWindow(); });
        ic.addActionListener(e -> {
            r.triggerLightning(Math.random() < 0.4 ? com.dan.lightning.Bolt.Kind.SPIDER : com.dan.lightning.Bolt.Kind.INTRA_CLOUD, false);
            view.requestFocusInWindow();
        });
        sheet.addActionListener(e -> { r.triggerLightning(com.dan.lightning.Bolt.Kind.SHEET, false); view.requestFocusInWindow(); });
        bp.add(bolt); bp.add(tower); bp.add(ic); bp.add(sheet);
        put(bp);
        com.dan.fcheckbox.FCheckBox slow = new com.dan.fcheckbox.FCheckBox("Zeitlupe 1 : 50");
        slow.addActionListener(e -> { r.lightningSlow = slow.isSelected() ? 0.02 : 1; view.requestFocusInWindow(); });
        com.dan.fcheckbox.FCheckBox showBolts = new com.dan.fcheckbox.FCheckBox("Blitze zeigen");
        showBolts.setSelected(true);
        showBolts.addActionListener(e -> { r.showLightning = showBolts.isSelected(); view.requestFocusInWindow(); });
        JPanel bx = grid(2);
        bx.add(slow);
        bx.add(showBolts);
        put(bx);

        // ============================================================== ZEIT
        block("ZEIT", true);
        put(dateLbl);
        JPanel days = grid(3);
        FButton prev = new FButton("− 1 Tag"), today = new FButton("Heute"), next = new FButton("+ 1 Tag");
        prev.addActionListener(e -> { clock.shiftDays(-1); syncFromScene(); });
        next.addActionListener(e -> { clock.shiftDays(1); syncFromScene(); });
        today.addActionListener(e -> { clock.set(LocalDate.now(SceneClock.ZONE), clock.hour()); syncFromScene(); });
        days.add(prev); days.add(today); days.add(next);
        put(days);
        put(timeLbl);
        put(time);
        time.addChangeListener(e -> { if (!fromScene) { clock.setHour(time.getValue() / 60.0); labels(); } });
        FComboBox lapse = new FComboBox(new Object[]{"Uhr steht", "Echtzeit", "1 Minute je Sekunde", "10 Minuten je Sekunde (600-fach)", "wie das Regiebuch"});
        lapseBox = lapse;
        lapse.setSelectedIndex(1);
        lapse.addActionListener(e -> {
            com.dan.weather.Scenario sc = wiring.weather.scenario();
            clock.lapse = switch (lapse.getSelectedIndex()) { case 0 -> 0; case 2 -> 60; case 3 -> 600; case 4 -> sc != null ? sc.lapse() : 1; default -> 1; };
            view.requestFocusInWindow();
        });
        put(lapse);
        put(sunLbl);
        sub("Gezeiten");
        tideBox.setSelected(true);
        tideBox.addActionListener(e -> {
            wiring.tide.fixed = wiring.waterLevel.level();
            wiring.tide.on = tideBox.isSelected();
            level.setValue((int) Math.round(wiring.tide.fixed * 10));
            view.requestFocusInWindow();
        });
        put(tideBox);
        put(levelLbl);
        put(level);
        level.addChangeListener(e -> {
            if (fromScene) return;
            if (wiring.tide.on) { wiring.tide.on = false; tideBox.setSelected(false); }
            wiring.tide.fixed = level.getValue() / 10.0;
            labels();
        });
        put(small("<html><body style='width:215px'>Die Gezeit folgt Mond und Sonne (Hochwasser rund eine Stunde vor Bremerhaven); in der Ostsee "
                + "ist sie aus. Der Regler setzt den Wasserstand von Hand.</body></html>"));

        // ============================================================== KAMERA
        block("KAMERA", true);
        put(camLbl);
        FButton home = new FButton("Grundansicht (Pos1)");
        home.addActionListener(e -> { view.goHome(); view.requestFocusInWindow(); });
        put(home);
        free.addActionListener(e -> { view.setMode(free.isSelected() ? Camera.Mode.FREI : Camera.Mode.KREISEN); view.requestFocusInWindow(); });
        orbit.addActionListener(e -> { view.setAutoOrbit(orbit.isSelected()); view.requestFocusInWindow(); });
        view.setModeListener(m -> free.setSelected(m == Camera.Mode.FREI));
        view.setOrbitListener(orbit::setSelected);
        put(free);
        put(orbit);
        JPanel views = grid(2);
        FButton v1 = new FButton("Wie Aussen1"), v2 = new FButton("Drohne"), innen2 = new FButton("Wie Innen2"), inside = new FButton("Laternenraum"),
                near = new FButton("An die Scheibe"), toRail = new FButton("Handlauf"), gullRide = new FButton("Möwenflug"), bridge = new FButton("Brücke");
        v1.addActionListener(e -> { r.camera.mode = Camera.Mode.KREISEN; r.camera.autoOrbit = false; r.camera.lookAt(0, 13.5, 0, 62, 1.5, 70); view.requestFocusInWindow(); });
        v2.addActionListener(e -> { r.camera.mode = Camera.Mode.KREISEN; r.camera.autoOrbit = false; r.camera.lookAt(0, 9, 0, 325, 55, 48); view.requestFocusInWindow(); });
        innen2.addActionListener(e -> {
            r.zoom = com.dan.lighthouse.scene.Innenraum.INNEN2[5];
            r.camera.place(com.dan.lighthouse.scene.Innenraum.INNEN2[0], com.dan.lighthouse.scene.Innenraum.INNEN2[1], com.dan.lighthouse.scene.Innenraum.INNEN2[2], com.dan.lighthouse.scene.Innenraum.INNEN2[3], com.dan.lighthouse.scene.Innenraum.INNEN2[4]);
            view.requestFocusInWindow();
        });
        inside.addActionListener(e -> {
            // hinter der Linse, Blick zur Wetterseite
            double from = wiring.wind.fromDeg, a = Math.toRadians(from + 180 + 12);
            r.zoom = 1;
            r.camera.place(Math.sin(a) * 1.4, Turm.GALLERY_TOP + 1.7, -Math.cos(a) * 1.4, from + 5, 9);
            view.requestFocusInWindow();
        });
        near.addActionListener(e -> {
            // eine Handbreit vor der Scheibe auf der Wetterseite, leicht vergrößert
            double from = wiring.wind.fromDeg, a = Math.toRadians(from);
            r.zoom = 3;
            r.camera.place(Math.sin(a) * 1.53, Turm.GALLERY_TOP + 1.3, -Math.cos(a) * 1.53, from + 6, 10);
            view.requestFocusInWindow();
        });
        toRail.addActionListener(e -> { r.toRail(wiring.wind.fromDeg); view.requestFocusInWindow(); });
        gullRide.addActionListener(e -> { if (!r.rideGull()) gullRide.setToolTipText("Gerade fliegt keine Möwe (nachts oder im Sturm)."); view.requestFocusInWindow(); });
        bridge.addActionListener(e -> { r.toBridge(); view.requestFocusInWindow(); });
        for (FButton b : new FButton[]{v1, v2, innen2, inside, near, toRail, gullRide, bridge}) views.add(b);
        views.setLayout(new java.awt.GridLayout(0, 2, 6, 6));
        put(views);
        sub("Kamerafahrten");
        put(daten.fahrt);
        JPanel fp = grid(2);
        fp.add(daten.fahrtStart);
        fp.add(daten.aufnahme);
        put(fp);
        put(daten.fahrtInfo);
        sub("Ansichten");
        put(daten.merken);
        put(daten.ansicht);
        put(daten.vorschau);
        put(daten.ansichtInfo);
        JPanel ap = grid(2);
        ap.add(daten.zeigen);
        ap.add(daten.ansichtLoeschen);
        put(ap);
        put(small("<html><body style='width:215px'>Links ziehen dreht, rechts ziehen verschiebt, Rad zoomt. W A S D gleiten, frei mit Q/E ab und auf, "
                + "Umschalt schneller. V merkt die Ansicht mit Vorschau, P setzt bei der Aufnahme einen Stützpunkt. Jede Bewegung von Hand beendet eine Fahrt.</body></html>"));

        // ============================================================== LEUCHTFEUER
        block("LEUCHTFEUER", false);
        sub("Turm");
        put(daten.turm);
        put(daten.turmInfo);
        sub("Feuer");
        put(beaconLbl);
        put(small("Voreinstellung des Feuers"));
        com.dan.fcombobox.FComboBox preset = new com.dan.fcombobox.FComboBox(com.dan.beacon.Beacon.PRESETS);
        kennung.setText(r.beacon.characteristic().text);
        preset.addActionListener(e -> {
            r.beacon.copyLight(com.dan.beacon.Beacon.preset(preset.getSelectedIndex()));
            kennung.setText(r.beacon.characteristic().text);
            beaconFeedback.setForeground(new Color(150, 165, 178));
            beaconFeedback.setText("<html><body style='width:215px'>" + Startbild.esc(r.beacon.characteristic().describe()) + "</body></html>");
            view.requestFocusInWindow();
        });
        put(preset);
        put(small("Kennung (Enter übernimmt), z. B. Fl(3) W 15s, Oc(3) WRG 12s"));
        kennung.addActionListener(e -> {
            String err = r.beacon.setCharacteristic(kennung.getText());
            beaconFeedback.setForeground(err == null ? new Color(150, 165, 178) : new Color(240, 170, 80));
            beaconFeedback.setText("<html><body style='width:215px'>" + Startbild.esc(err == null
                    ? r.beacon.characteristic().describe() + " · Lichtzeiten " + r.beacon.characteristic().timesText() : err) + "</body></html>");
            view.requestFocusInWindow();
        });
        put(kennung);
        put(beaconFeedback);
        com.dan.fcombobox.FComboBox schalter = new com.dan.fcombobox.FComboBox(new Object[]{"Dämmerungsschalter", "immer an", "aus"});
        schalter.addActionListener(e -> {
            int i = schalter.getSelectedIndex();
            r.beacon.automatic = i == 0;
            r.beacon.manualOn = i == 1;
            view.requestFocusInWindow();
        });
        put(schalter);
        turnBox.setSelected(true);
        turnBox.addActionListener(e -> { r.beacon.turning = turnBox.isSelected(); view.requestFocusInWindow(); });
        beamBox.setSelected(true);
        beamBox.addActionListener(e -> { r.showBeam = beamBox.isSelected(); view.requestFocusInWindow(); });
        neighbourBox.setSelected(true);
        neighbourBox.addActionListener(e -> { r.showNeighbours = neighbourBox.isSelected(); view.requestFocusInWindow(); });
        JPanel bb = grid(2);
        bb.add(turnBox);
        bb.add(beamBox);
        put(bb);
        put(neighbourBox);
        put(beamStrLbl);
        put(beamStr);
        beamStr.addChangeListener(e -> { r.beamStrength = beamStr.getValue() / 100.0; labels(); });
        sub("Licht im Turm");
        lampBox.setSelected(true);
        lampBox.addActionListener(e -> { r.lampOn = lampBox.isSelected(); view.requestFocusInWindow(); });
        put(lampBox);
        FCheckBox stairBox = new FCheckBox("Licht im Treppenhaus");
        stairBox.setSelected(true);
        stairBox.addActionListener(e -> { r.stairLight = stairBox.isSelected(); view.requestFocusInWindow(); });
        put(stairBox);
        put(innerLbl);

        // ============================================================== BILD
        block("BILD", false);
        FComboBox qual = new FComboBox(Quality.values());
        qualBox = qual;
        qual.setSelectedItem(r.quality());
        qual.addActionListener(e -> { r.setQuality((Quality) qual.getSelectedItem()); view.requestFocusInWindow(); });
        put(qual);
        autoBox.setSelected(r.autoScale);
        autoBox.addActionListener(e -> { r.autoScale = autoBox.isSelected(); if (!r.autoScale) r.renderScale = scaleSl.getValue() / 100.0; view.requestFocusInWindow(); });
        put(autoBox);
        put(scaleLbl);
        put(scaleSl);
        scaleSl.addChangeListener(e -> { if (!fromScene) { r.autoScale = false; autoBox.setSelected(false); r.renderScale = scaleSl.getValue() / 100.0; } labels(); });
        FComboBox fpsBox = new FComboBox(new Object[]{"Ziel 30 Bilder/s", "Ziel 60 Bilder/s", "Ziel 120 Bilder/s"});
        fpsBox.setSelectedIndex(1);
        fpsBox.addActionListener(e -> { r.targetFps = switch (fpsBox.getSelectedIndex()) { case 0 -> 30; case 2 -> 120; default -> 60; }; view.requestFocusInWindow(); });
        put(fpsBox);
        put(small("<html><body style='width:215px'>Die Regelung senkt die Auflösung bis auf 50 %, wenn die Grafikkarte das Ziel nicht hält, "
                + "und hebt sie wieder, sobald sie Luft hat. Der Regler setzt sie fest.</body></html>"));
        hudBox = new FComboBox(new Object[]{"HUD kurz (H)", "HUD voll", "HUD aus"});
        hudBox.addActionListener(e -> {
            if (fromScene) return;
            int i = hudBox.getSelectedIndex();
            r.showHud = i != 2;
            r.hudFull = i == 1;
            view.requestFocusInWindow();
        });
        put(hudBox);
        towerBox.setSelected(true);
        towerBox.addActionListener(e -> { r.showTower = towerBox.isSelected(); view.requestFocusInWindow(); });
        put(towerBox);
        JPanel ev = grid(3);
        FButton dark = new FButton("− 1 EV"), zero = new FButton("± 0"), bright = new FButton("+ 1 EV");
        dark.addActionListener(e -> r.exposureBias -= 1);
        zero.addActionListener(e -> r.exposureBias = 0);
        bright.addActionListener(e -> r.exposureBias += 1);
        ev.add(dark); ev.add(zero); ev.add(bright);
        put(ev);
        FButton photo = new FButton("Foto (F12)");
        photo.addActionListener(e -> { r.photoRequest = true; view.requestFocusInWindow(); });
        put(photo);
        sub("Gischt und Regen");
        put(sprayLbl);
        sprayBox.setSelected(true);
        sprayBox.addActionListener(e -> { r.showSpray = sprayBox.isSelected(); view.requestFocusInWindow(); });
        rainBox.setSelected(true);
        rainBox.addActionListener(e -> { r.showRain = rainBox.isSelected(); view.requestFocusInWindow(); });
        JPanel sr = grid(2);
        sr.add(sprayBox);
        sr.add(rainBox);
        put(sr);
        put(sprayStrLbl);
        put(sprayStr);
        sprayStr.addChangeListener(e -> { r.spray.strength = sprayStr.getValue() / 100.0; labels(); });
        put(rainStrLbl);
        put(rainStr);
        rainStr.addChangeListener(e -> { r.rainStrength = rainStr.getValue() / 100.0; labels(); });
        sub("Scheiben");
        put(glassLbl);
        glassBox.setSelected(true);
        glassBox.addActionListener(e -> { r.showGlass = glassBox.isSelected(); view.requestFocusInWindow(); });
        fogBox.setSelected(true);
        fogBox.addActionListener(e -> { wiring.fogOn = fogBox.isSelected(); view.requestFocusInWindow(); });
        JPanel gb = grid(2);
        gb.add(glassBox);
        gb.add(fogBox);
        put(gb);
        put(glassStrLbl);
        put(glassStr);
        glassStr.addChangeListener(e -> { r.glassStrength = glassStr.getValue() / 100.0; labels(); });
        JPanel wp = grid(2);
        FButton wipe = new FButton("Scheiben wischen"), wipeIn = new FButton("Beschlag wischen"), flood = new FButton("Brecher über die Galerie");
        wipe.addActionListener(e -> { r.wipeGlass(); r.wipeRail(); view.requestFocusInWindow(); });
        wipeIn.addActionListener(e -> { r.wipeFog(); view.requestFocusInWindow(); });
        flood.addActionListener(e -> { r.floodByHand(1.2); view.requestFocusInWindow(); });
        wp.add(wipe);
        wp.add(wipeIn);
        put(wp);
        put(flood);
        sub("Turm");
        put(towerLbl);
        put(rustLbl);
        put(rust);
        rust.addChangeListener(e -> { r.weathering.rust = rust.getValue() / 100.0; labels(); });
        put(algaeLbl);
        put(algae);
        algae.addChangeListener(e -> { r.weathering.algae = algae.getValue() / 100.0; labels(); });
        put(saltLbl);
        put(salt);
        salt.addChangeListener(e -> { r.weathering.salt = salt.getValue() / 100.0; labels(); });

        // ============================================================== LEBEN
        block("LEBEN", false);
        put(lifeLbl);
        JPanel lb = grid(3);
        FCheckBox gullBox = new FCheckBox("Möwen"), shipBox = new FCheckBox("Schiffe"), seaFogBox = new FCheckBox("Nebel");
        gullBox.setSelected(true);
        shipBox.setSelected(true);
        seaFogBox.setSelected(true);
        gullBox.addActionListener(e -> { r.showGulls = gullBox.isSelected(); view.requestFocusInWindow(); });
        shipBox.addActionListener(e -> { r.showShips = shipBox.isSelected(); view.requestFocusInWindow(); });
        seaFogBox.addActionListener(e -> { r.showFog = seaFogBox.isSelected(); wiring.seaFogOn = seaFogBox.isSelected(); view.requestFocusInWindow(); });
        lb.add(gullBox); lb.add(shipBox); lb.add(seaFogBox);
        put(lb);
        JPanel lp2 = grid(2);
        FButton shipCome = new FButton("Schiff kommt"), wiper = new FButton("Wischer an/aus");
        shipCome.addActionListener(e -> {
            // ein Kümo läuft ein, schon nah am Turm (5 km auf dem Fahrwasser)
            com.dan.buoyancy.Ship s = r.buoyancy.spawn(true, 5200);
            r.camera.mode = Camera.Mode.KREISEN;
            r.camera.autoOrbit = false;
            r.camera.lookAt(s.x, 6, s.z, Math.toDegrees(Math.atan2(s.x, -s.z)), 4, 260);
            view.requestFocusInWindow();
        });
        wiper.addActionListener(e -> {
            com.dan.buoyancy.Ship s = r.onBridge();
            boolean on = s != null ? s.wiper.on : r.wiperMode == 1;
            r.wiperMode = on ? 0 : 1;
            view.requestFocusInWindow();
        });
        lp2.add(shipCome); lp2.add(wiper);
        put(lp2);
        sub("Klang");
        put(soundLbl);
        FCheckBox soundBox = new FCheckBox("Ton an"), earBox = new FCheckBox("Ohr passt sich an");
        soundBox.setSelected(!"false".equals(System.getProperty("lighthouse.sound")));
        earBox.setSelected(true);
        soundBox.addActionListener(e -> { if (soundBox.isSelected()) r.startSound(); else r.stopSound(); view.requestFocusInWindow(); });
        earBox.addActionListener(e -> { r.soundscape.autoGain = earBox.isSelected(); view.requestFocusInWindow(); });
        JPanel sb = grid(2);
        sb.add(soundBox);
        sb.add(earBox);
        put(sb);
        put(soundVolLbl);
        put(soundVol);
        soundVol.addChangeListener(e -> { r.soundscape.volume = soundVol.getValue() / 100.0; labels(); });
        JPanel hp = grid(2);
        FButton hornNow = new FButton("Nebelhorn"), gullNow = new FButton("Möwe am Turm");
        hornNow.addActionListener(e -> {
            r.soundscape.horn(com.dan.soundscape.Soundscape.Kind.NEBELHORN, 0, Turm.LENS_Y + 2.5, 0, 1, 3);
            view.requestFocusInWindow();
        });
        gullNow.addActionListener(e -> {
            double a = Math.random() * 2 * Math.PI;
            r.soundscape.event(com.dan.soundscape.Soundscape.Kind.MOEWE, Math.sin(a) * 15, 26, -Math.cos(a) * 15, 1);
            view.requestFocusInWindow();
        });
        hp.add(hornNow);
        hp.add(gullNow);
        put(hp);
        put(small("<html><body style='width:215px'>Möwen stehen im Aufwind vor der Luvseite, landen auf Handlauf, Knauf, Peilpfahl und Tonnen; "
                + "Schiffe und Tonnen schwimmen auf dem Meer. Unter 2 sm Sicht ruft das Nebelhorn. Der Klang ist ganz erzeugt: Wind, singende Stäbe, "
                + "Brandung, Regen, Brecher, Horn, Möwen, Donner und Diesel.</body></html>"));

        // ============================================================== DATEN
        block("DATEN", false);
        put(daten.wo);
        put(daten.standLbl);
        put(daten.meldung);
        sub("Weiter wie zuletzt");
        put(daten.sicherLbl);
        put(daten.jetztSichern);
        put(daten.weiterBtn);
        sub("Presets der Packages");
        put(daten.paket);
        put(daten.preset);
        put(daten.presetInfo);
        JPanel pp = grid(2);
        pp.add(daten.presetLaden);
        pp.add(daten.presetSpeichern);
        put(pp);
        put(daten.presetLoeschen);
        sub("Datenbank");
        put(dbLbl);
        JPanel dp = grid(2);
        dp.add(daten.einrichtenBtn);
        dp.add(daten.pruefenBtn);
        put(dp);
        put(daten.uebernehmenBtn);
        put(daten.einrichtLbl);
        sub("Protokoll");
        put(daten.logLbl);

        // ============================================================== TECHNIK
        block("TECHNIK", false);
        put(perfLbl);
        put(computeLbl);
        put(glLbl);
        sub("Abgleich Grafikkarte = Prozessor");
        for (JLabel l : new JLabel[]{seaCheckLbl, skyCheckLbl, cloudCheckLbl, towerCheckLbl, sprayCheckLbl, rainCheckLbl, glassCheckLbl, beaconCheckLbl, stormCheckLbl})
            put(l);
        sub("Alle Zeilen des HUD");
        put(hudLbl);
        sub("Verdrahtung");
        put(linksLbl);

        for (Object[] b : bloecke) ((FCollapsiblePanel) b[0]).setBodyComponent((JComponent) b[1]);
        for (FCollapsiblePanel p : zu) p.setExpanded(false, false);
        add(Box.createVerticalGlue());
        view.setKeyAction(java.awt.event.KeyEvent.VK_F1, () -> javax.swing.SwingUtilities.invokeLater(this::tasten));
        // Phase 16: die Ansichten aus Abschnitt 7 auf den Tasten 1 bis 9
        for (int i = 0; i < ANSICHTEN.length; i++) {
            String name = ANSICHTEN[i];
            view.setKeyAction(java.awt.event.KeyEvent.VK_1 + i, () -> r.applyView(name));
            view.setKeyAction(java.awt.event.KeyEvent.VK_NUMPAD1 + i, () -> r.applyView(name));
        }
        syncFromScene();
        new Timer(500, e -> syncFromScene()).start();
    }

    /** Für Werkzeuge (Statusbild): genau diese Blöcke öffnen, alle anderen schließen. */
    public void oeffnen(String... titel) {
        java.util.Set<String> t = java.util.Set.of(titel);
        for (Object[] b : bloecke) {
            FCollapsiblePanel p = (FCollapsiblePanel) b[0];
            p.setExpanded(t.contains(p.getTitle()), false);
        }
        revalidate();
    }

    /** Für Werkzeuge: die Datensteuerung. */
    public DatenSteuerung daten() { return daten; }

    /** Phase 15: Daten laden, auf Wunsch den letzten Stand herstellen, Sitzung beginnen (nach dem Start der Szene). */
    public void datenStarten(boolean weiterWieZuletzt) { daten.starten(weiterWieZuletzt); }

    /** Phase 15: ein Regiebuch starten (aus der Liste oder dem Bearbeiten-Fenster). */
    private void startScenario(com.dan.weather.Scenario sc) {
        clock.set(clock.date(), sc.startHour());
        wiring.resumeRegie();
        wiring.weather.start(sc, clock.epochMillis() / 1000.0);
        wiring.weather.running = true;
        lapseBox.setSelectedIndex(4);
        followBox.setSelected(true);
        lastStep = -2;
        syncFromScene();
        view.requestFocusInWindow();
    }

    /** F1: die Tasten. */
    private void tasten() {
        com.dan.foptionpane.FOptionPane.showMessageDialog(this, "<html><body style='width:420px'>"
                + "<b>Szene</b><br>Links ziehen: drehen · rechts ziehen: verschieben · Rad, Bild↑/↓, +/−: zoomen<br>"
                + "Pfeile: drehen · W A S D: gleiten · Q/E: ab und auf (frei) · Umschalt: schneller<br>"
                + "1–9: Ansichten Aussen1, Drohne, Brecher, Sturmnacht, Aussen3, Laternenraum, Regenbogen, Möwenflug, Schiffsbrücke<br>"
                + "F: frei fliegen · R: um den Turm kreisen · Pos1: Grundansicht · H: HUD kurz, voll, aus<br>"
                + "Leertaste: Regie an/aus · F12: Foto nach bilder/ · F5: Shader neu laden<br><br>"
                + "<b>Daten</b><br>V: Ansicht merken (mit Vorschau) · P: Stützpunkt bei der Aufnahme einer Kamerafahrt<br><br>"
                + "<b>Fenster</b><br>F1: diese Übersicht · F11: nur die Szene (Bedienfeld und Statuszeile weg und wieder da)</body></html>",
                "Lighthouse · Tasten", com.dan.foptionpane.FOptionPane.INFORMATION_MESSAGE);
    }

    /** Regler und Texte nach der Szene stellen (die Uhr läuft weiter, die Kamera wird bewegt). */
    private void syncFromScene() {
        fromScene = true;
        int m = (int) Math.round(clock.hour() * 60) % (24 * 60);
        if (!time.getValueIsAdjusting() && Math.abs(time.getValue() - m) > 0) time.setValue(m);
        if (wiring.tide.on && !level.getValueIsAdjusting()) level.setValue((int) Math.round(wiring.waterLevel.level() * 10));
        syncWeather();
        fromScene = false;
        free.setSelected(r.camera.mode == Camera.Mode.FREI);
        orbit.setSelected(r.camera.autoOrbit);
        labels();
        Camera c = r.camera;
        camLbl.setText(String.format(Locale.GERMAN, "<html><body style='width:215px'>Auge %.1f m über dem Wasser, %.0f m vom Turm, Blick %03.0f°. "
                        + "Kimm in %.1f sm, Feuer auf %.0f m sichtbar bis %.1f sm.</body></html>",
                c.eye[1], c.towerDistance(), c.heading(), Site.horizon(c.eye[1]) / Site.NM, Site.LIGHT, Site.geographicRangeNm(Site.LIGHT, c.eye[1]))
                .replace("</body>", c.rideTime() >= 0 ? String.format(Locale.GERMAN, " Fahrt „%s“ %.0f s.</body>", c.rideName(), c.rideTime()) : "</body>"));
        perfLbl.setText(String.format(Locale.GERMAN, "<html><body style='width:215px'>%.0f Bilder/s · Grafikkarte %.2f ms · Prozessor %.2f ms<br>%s</body></html>",
                r.fps(), r.gpuMs(), r.cpuMs(), r.passTimes()));
        seaLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.ocean.describe()) + (r.ocean.rebuilding() ? " · baut neu …" : "") + "</body></html>");
        cloudLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.clouds.describe()) + String.format(Locale.GERMAN,
                ". Sonne am Turm %.0f %%.", r.cloudShade() * 100) + "</body></html>");
        innerLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.interiorLine()) + (r.camera.rideTime() >= 0
                ? String.format(java.util.Locale.GERMAN, "<br>Aufstieg: %.0f s", r.camera.rideTime()) : "") + "</body></html>");
        towerLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.towerLine().replace("Turm · ", "")) + "<br>"
                + Startbild.esc(r.weathering.describe()) + "</body></html>");
        sprayLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.spray.describe()) + "</body></html>");
        rainLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.rain.rate > 0 ? r.rain.describe() : "trocken") + "</body></html>");
        rainCheckLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.rainCheck().isEmpty() ? "Abgleich Grafikkarte/Prozessor folgt, sobald es regnet …" : r.rainCheck()) + "</body></html>");
        if (r.zoom > 1 && !r.insideLantern() && !r.nearRail()) r.zoom = 1;
        soundLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.soundLine()) + "</body></html>");
        soundVolLbl.setText(String.format(Locale.GERMAN, "Lautstärke %d %%", soundVol.getValue()));
        lifeLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.lifeLine()) + "</body></html>");
        fogLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.fogLine()) + "</body></html>");
        fromScene = true;
        if (!fogSl.getValueIsAdjusting()) fogSl.setValue((int) Math.round(wiring.fogAmount * 100));
        fromScene = false;
        fogStrLbl.setText(String.format(Locale.GERMAN, "Nebel %.0f %% (Sicht in der Bank %s)", wiring.fogAmount * 100,
                wiring.fogAmount < 0.05 ? "–" : String.format(Locale.GERMAN, "%.0f m", Wiring.fogVisibilityM(wiring.fogAmount))));
        glassLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.insideLantern() || r.nearRail() ? r.glassLine() : "Auge außerhalb des Laternenraums, die Scheiben ruhen") + "</body></html>");
        beaconLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.beaconLine().replaceAll(" · GPU = CPU.*", "")) + "</body></html>");
        beaconCheckLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.beaconCheck().isEmpty() ? "Abgleich Grafikkarte/Prozessor folgt, sobald das Feuer brennt …" : r.beaconCheck()) + "</body></html>");
        String ll = r.lightningLine();
        stormLbl.setText("<html><body style='width:215px'>" + Startbild.esc(ll.replaceAll(" · (GPU = CPU|Abgleich).*", "").replaceAll(" · Prozessor [0-9,]+ ms", "")) + "</body></html>");
        stormCheckLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.lightningCheck().isEmpty() ? "Abgleich Grafikkarte/Prozessor folgt beim ersten Erdblitz …" : r.lightningCheck()) + "</body></html>");
        glassCheckLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.glassCheck().isEmpty() ? "Abgleich Grafikkarte/Prozessor folgt im Laternenraum …" : r.glassCheck()) + "</body></html>");
        sprayCheckLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.sprayCheck().isEmpty() ? "Abgleich Grafikkarte/Prozessor folgt …" : r.sprayCheck()) + "</body></html>");
        towerCheckLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.weatheringInfo()) + "<br>"
                + Startbild.esc(r.weatheringCheck().isEmpty() ? "Abgleich Grafikkarte/Prozessor folgt …" : r.weatheringCheck()) + "</body></html>");
        cloudCheckLbl.setText("<html><body style='width:215px'>" + (r.cloudsRunning() ? Startbild.esc(r.cloudInfo()) + "<br>"
                + Startbild.esc(r.cloudCheck().isEmpty() ? "Abgleich Grafikkarte/Prozessor folgt …" : r.cloudCheck()) : "Wolken nicht gestartet") + "</body></html>");
        if (r.skyRunning()) {
            com.dan.sky.Sky.State st = r.skyState();
            skyLbl.setText("<html><body style='width:215px'>" + Startbild.esc(st.twilight()) + String.format(Locale.GERMAN,
                    ". Mond %s, %.0f %% beleuchtet, %.1f Tage alt, %.1f° hoch. Grenzgröße der Sterne %.1f mag.", st.moonPhaseName,
                    st.moonFraction * 100, st.moonAgeDays, st.moonAlt, st.limitingMagnitude) + planetsVisible(st) + "</body></html>");
            skyCheckLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.skyInfo()) + "<br>"
                    + Startbild.esc(r.skyCheck().isEmpty() ? "Abgleich Grafikkarte/Prozessor folgt …" : r.skyCheck()) + "</body></html>");
        } else {
            skyLbl.setText("<html><body style='width:215px'>Verlauf aus Phase 1 (com.dan.sky nicht gestartet)</body></html>");
        }
        seaCheckLbl.setText("<html><body style='width:215px'>" + (r.oceanRunning() ? Startbild.esc(r.oceanInfo()) + "<br>"
                + Startbild.esc(r.oceanCheck().isEmpty() ? "Abgleich Grafikkarte/Prozessor folgt …" : r.oceanCheck()) : "Platzhalter-See (ohne Compute-Shader)") + "</body></html>");
        ComputeProbe.Result cr = r.compute();
        computeLbl.setText("<html><body style='width:215px'>" + (cr == null ? "Compute-Test läuft …" : Startbild.esc(cr.line())) + "</body></html>");
        computeLbl.setForeground(cr == null || cr.ok() ? Theme.muted() : new Color(230, 170, 90));
        glLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.glInfo()) + (r.reversedDepth() ? " · umgekehrte Tiefe" : "") + "</body></html>");
        dbLbl.setText("<html><body style='width:215px'>" + Startbild.esc(Startbild.statuszeile()) + "</body></html>");
        fromScene = true;
        int hm = !r.showHud ? 2 : r.hudFull ? 1 : 0;
        if (hudBox != null && hudBox.getSelectedIndex() != hm) hudBox.setSelectedIndex(hm);
        fromScene = false;
        if (ticks % 2 == 0) {
            StringBuilder hb = new StringBuilder("<html><body style='width:215px'>");
            for (String z : r.hudLines()) hb.append(Startbild.esc(z.startsWith("!") ? z.substring(1) : z)).append("<br>");
            hudLbl.setText(hb.append("</body></html>").toString());
        }
        if (ticks++ % 4 == 0) daten.anzeigen();
        StringBuilder sb = new StringBuilder("<html><body style='width:215px'>Verdrahtung:");
        for (Wiring.Link l : wiring.links()) sb.append("<br>· ").append(Startbild.esc(l.toString()));
        linksLbl.setText(sb.append("</body></html>").toString());
    }

    /** Phase 12: Regler zeigen, was die Regie setzt; Regiebuch, Kamera und Ende des Szenarios. */
    private void syncWeather() {
        com.dan.weather.Weather.Snapshot ws = wiring.weather.snapshot();
        if (ws == null) return;
        if (!wind.getValueIsAdjusting()) wind.setValue(wiring.wind.beaufort());
        if (!windDir.getValueIsAdjusting()) windDir.setValue((int) Math.round(wiring.wind.fromDeg()) % 360);
        if (!vis.getValueIsAdjusting()) vis.setValue((int) Math.round(Math.max(2, Math.min(100, wiring.visibilityKm))));
        if (!cover.getValueIsAdjusting()) cover.setValue((int) Math.round(wiring.oktas * 10));
        if (!high.getValueIsAdjusting()) high.setValue((int) Math.round(wiring.highCover * 100));
        if (!rainRate.getValueIsAdjusting()) rainRate.setValue(sliderOf(wiring.rainRate));
        if (!swellH.getValueIsAdjusting()) swellH.setValue((int) Math.round(wiring.swellHeight * 10));
        if (!swellT.getValueIsAdjusting()) swellT.setValue((int) Math.round(wiring.swellPeriod * 10));
        if (!swellDir.getValueIsAdjusting()) swellDir.setValue((int) Math.round(wiring.swellFromDeg) % 360);
        if (cloudKind != null && cloudKind.getSelectedItem() != wiring.cloudType) cloudKind.setSelectedItem(wiring.cloudType);
        if (stormBox != null && stormBox.getSelectedIndex() != wiring.thunderstorm.ordinal()) stormBox.setSelectedIndex(wiring.thunderstorm.ordinal());
        weatherLbl.setText("<html><body style='width:215px'>" + Startbild.esc(r.weatherLine().replace("Wetter ", "")) + "</body></html>");
        baroLbl.setText(String.format(Locale.GERMAN, "Barometer %.1f hPa · %s · „%s“", ws.pressure(), ws.tendencyText(), ws.barometerWord()));
        com.dan.weather.Scenario sc = wiring.weather.scenario();
        if (sc != null && ws.step() >= 0 && ws.step() != lastStep) {
            lastStep = ws.step();
            if (followBox.isSelected()) applyView(sc.steps().get(ws.step()).camera());
        }
        if (sc != null && ws.step() < 0 && lastStep >= 0) {
            // Regiebuch zu Ende: zurück in Echtzeit, die Regie geht frei weiter
            lastStep = -1;
            if (lapseBox.getSelectedIndex() == 4) lapseBox.setSelectedIndex(1);
        }
        regieBox.setSelected(wiring.weather.running);
    }

    /** Phase 12: Kamera nach dem Wunsch des Regiebuchs (siehe {@link Renderer#applyView}). */
    void applyView(String name) { r.applyView(name); }

    private void cloudPreset(com.dan.cloud.CloudType t, double oktas, double highCover, FComboBox kind) {
        wiring.hand(com.dan.weather.Weather.Var.CLOUD, com.dan.weather.Weather.CloudKind.valueOf(t.name()).ordinal());
        wiring.hand(com.dan.weather.Weather.Var.COVER, oktas);
        wiring.hand(com.dan.weather.Weather.Var.HIGH, highCover);
        fromScene = true;
        kind.setSelectedItem(t);
        cover.setValue((int) Math.round(oktas * 10));
        high.setValue((int) Math.round(highCover * 100));
        fromScene = false;
        view.requestFocusInWindow();
    }

    /** „ Sichtbar: Venus, Jupiter.“ – Planeten über der Kimm und heller als die Grenzgröße. */
    private static String planetsVisible(com.dan.sky.Sky.State st) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < st.planets.size(); i++) {
            double alt = Math.toDegrees(Math.asin(st.planetDirs[i][1]));
            if (alt > 1 && st.planets.get(i).magnitude() < st.limitingMagnitude) sb.append(sb.length() == 0 ? "" : ", ").append(st.planets.get(i).name());
        }
        return sb.length() == 0 ? "" : " Zu sehen: " + sb + ".";
    }

    private void labels() {
        dateLbl.setText("Datum: " + clock.dateLabel());
        timeLbl.setText("Uhrzeit: " + clock.timeLabel());
        visLbl.setText(String.format(Locale.GERMAN, "Sicht %.0f km", wiring.visibilityKm));
        coverLbl.setText(String.format(Locale.GERMAN, "Bedeckung %.1f/8", wiring.oktas));
        highLbl.setText(String.format(Locale.GERMAN, "Hoher Schleier %.0f %%", wiring.highCover * 100));
        levelLbl.setText(String.format(Locale.GERMAN, "Wasserstand %+.2f m%s", wiring.waterLevel.level(), wiring.tide.on ? " (Gezeit)" : " (von Hand)"));
        rustLbl.setText(String.format(Locale.GERMAN, "Rost %.0f %%", r.weathering.rust * 100));
        algaeLbl.setText(String.format(Locale.GERMAN, "Algen %.0f %%", r.weathering.algae * 100));
        saltLbl.setText(String.format(Locale.GERMAN, "Salz %.0f %%", r.weathering.salt * 100));
        sprayStrLbl.setText(String.format(Locale.GERMAN, "Stärke %.0f %%", r.spray.strength * 100));
        rainRateLbl.setText(wiring.rainRate <= 0 ? "Regen: trocken" : String.format(Locale.GERMAN, "Regen bis %s mm/h", wiring.rainRate < 1 ? String.format(Locale.GERMAN, "%.1f", wiring.rainRate) : String.format(Locale.GERMAN, "%.0f", wiring.rainRate)));
        rainStrLbl.setText(String.format(Locale.GERMAN, "Deckkraft %.0f %%", r.rainStrength * 100));
        glassStrLbl.setText(String.format(Locale.GERMAN, "Wasser auf den Scheiben %.0f %%", r.glassStrength * 100));
        beamStrLbl.setText(String.format(Locale.GERMAN, "Strahl ×%.1f (1 = physikalisch)", r.beamStrength));
        fromScene = true;
        if (!scaleSl.getValueIsAdjusting() && r.autoScale) scaleSl.setValue((int) Math.round(r.renderScale * 100));
        fromScene = false;
        scaleLbl.setText(String.format(Locale.GERMAN, "Auflösung %.0f %% (%s)", r.renderScale * 100, r.autoScale ? "geregelt, " + r.scaleSteps() + "× geändert" : "fest"));
        double[] s = r.skyRunning() ? new double[]{r.skyState().sunAlt, r.skyState().sunAz} : clock.sun();
        LocalDate d = clock.date();
        double day0 = d.atStartOfDay(SceneClock.ZONE).toInstant().toEpochMilli();
        double up = Sun.crossing(day0, clock.lat, clock.lon, true), down = Sun.crossing(day0, clock.lat, clock.lon, false);
        sunLbl.setText(String.format(Locale.GERMAN, "<html><body style='width:215px'>Sonne %.1f° hoch, Azimut %03.0f°. Aufgang %s, Untergang %s.</body></html>",
                s[0], s[1], hm(up), hm(down)));
        windLbl.setText(String.format(Locale.GERMAN, "Bft %d · %s · %.1f m/s", wiring.wind.beaufort(), Beaufort.NAME[wiring.wind.beaufort()], wiring.wind.speed()));
        dirLbl.setText(String.format(Locale.GERMAN, "aus %03.0f° (%s)", wiring.wind.fromDeg(), compass(wiring.wind.fromDeg())));
        swellLbl.setText(String.format(Locale.GERMAN, "Dünung %.1f m · %.1f s · aus %03.0f°", r.ocean.swellHeight, r.ocean.swellPeriod, r.ocean.swellFromDeg));
    }

    /** Regler 0..100 → mm/h: 0 trocken, sonst logarithmisch von 0,1 bis 100 mm/h. */
    static double rainOf(int v) { return v <= 0 ? 0 : 0.1 * Math.pow(1000, (v - 1) / 99.0); }

    static int sliderOf(double mmh) { return mmh <= 0 ? 0 : (int) Math.round(1 + 99 * Math.log(mmh / 0.1) / Math.log(1000)); }

    static String hm(double epochMs) {
        if (Double.isNaN(epochMs)) return "—";
        return String.format("%tR", Instant.ofEpochMilli((long) epochMs).atZone(SceneClock.ZONE));
    }

    static String compass(double deg) {
        String[] n = {"Nord", "Nordnordost", "Nordost", "Ostnordost", "Ost", "Ostsüdost", "Südost", "Südsüdost",
                "Süd", "Südsüdwest", "Südwest", "Westsüdwest", "West", "Westnordwest", "Nordwest", "Nordnordwest"};
        return n[(int) Math.round(((deg % 360) + 360) % 360 / 22.5) % 16];
    }

    // ------------------------------------------------------------ Aufbau

    /** Ein Klappblock (FCollapsiblePanel); offen bleibt, was zuletzt offen war. */
    private void block(String title, boolean offen) {
        FCollapsiblePanel p = new FCollapsiblePanel() {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
        };
        p.setTitle(title);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel inhalt = new JPanel();
        inhalt.setOpaque(false);
        inhalt.setLayout(new BoxLayout(inhalt, BoxLayout.Y_AXIS));
        inhalt.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 8));
        bloecke.add(new Object[]{p, inhalt});
        String key = "offen." + title;
        boolean auf = PREFS == null ? offen : PREFS.getBoolean(key, offen);
        if (!auf) zu.add(p);
        p.addFCollapsiblePanelListener((panel, expanded) -> {
            if (PREFS != null) try { PREFS.putBoolean(key, expanded); } catch (RuntimeException ex) { /* nicht gemerkt */ }
        });
        if (getComponentCount() > 0) add(Box.createVerticalStrut(6));
        add(p);
        target = inhalt;
    }

    /** Eine Zwischenüberschrift im Block. */
    private void sub(String text) {
        JLabel h = new JLabel(text);
        h.setForeground(Theme.accent());
        h.setFont(new Font("SansSerif", Font.BOLD, 11));
        h.setBorder(BorderFactory.createEmptyBorder(10, 0, 3, 0));
        put(h);
    }

    private void put(Component c) { target.add(row(c)); }

    private static JPanel grid(int n) {
        JPanel p = new JPanel(new java.awt.GridLayout(1, n, 6, 0));
        p.setOpaque(false);
        return p;
    }

    /** Eine Zeile, die mit ihrem Inhalt wächst (die Höchstgröße folgt der bevorzugten Größe, auch wenn der Text länger wird). */
    private static JComponent row(Component c) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        p.add(c, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    static JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Theme.ink());
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    static JLabel small(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Theme.muted());
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }

    // ------------------------------------------------------------ Scrollable: nur senkrecht
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) { return 120; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
