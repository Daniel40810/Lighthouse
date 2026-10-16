package com.dan.lighthouse.scene;

import com.dan.lighthouse.wiring.Clock;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Die Uhr der Szene (F26 = A): eigenes Datum und eigene Uhrzeit in Ortszeit (MEZ/MESZ), Zeitraffer bis 600-fach.
 * Beim ersten Lauf startet sie am heutigen Tag um 20:00 Uhr: Dämmerung, das Feuer ist an. Sonne, Mond und
 * Gezeiten rechnen mit der echten Position des Roten Sands. Die Uhr ist zugleich die Schnittstelle {@link Clock},
 * über die alle Packages Zeit bekommen.
 */
public final class SceneClock implements Clock {
    public static final double MAX_LAPSE = 600;
    public static final ZoneId ZONE = ZoneId.of(Site.ZONE);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d.M.yyyy", Locale.GERMANY),
            TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY), ZONE_NAME = DateTimeFormatter.ofPattern("zzz", Locale.GERMANY);

    /** Szenenzeit in ms seit 1970 (UTC), als double, damit kleine Schritte im Zeitraffer nicht verloren gehen. */
    private volatile double epochMs;
    /** Zeitraffer: Szenensekunden je echter Sekunde (0 = Uhr steht). */
    public volatile double lapse = 1;
    /** Phase 15: Ort für Sonne, Mond und Sterne (folgt der Leuchtturm-Auswahl); anfangs der Rote Sand. */
    public volatile double lat = Site.LAT, lon = Site.LON;
    /** Laufzeit der Szene in s (für Wellen, Animation; läuft auch bei stehender Uhr) und letzter Schritt. */
    private double t, dt;

    public SceneClock() { this(LocalDate.now(ZONE), 20.0); }

    public SceneClock(LocalDate date, double hour) { set(date, hour); }

    /** Datum und Uhrzeit (Stunden, Ortszeit) setzen. */
    public synchronized void set(LocalDate date, double hour) {
        hour = Math.max(0, Math.min(24 - 1e-9, hour));
        int sec = (int) Math.floor(hour * 3600);
        ZonedDateTime z = ZonedDateTime.of(date, LocalTime.ofSecondOfDay(sec), ZONE);
        epochMs = z.toInstant().toEpochMilli() + (hour * 3600 - sec) * 1000;
    }

    public synchronized void setHour(double hour) { set(date(), hour); }

    /** Läuft um dt echte Sekunden weiter. */
    public synchronized void step(double realDt) {
        dt = realDt;
        t += realDt;
        epochMs += realDt * Math.max(0, Math.min(MAX_LAPSE, lapse)) * 1000;
    }

    public synchronized void shiftDays(int days) {
        epochMs += days * 86_400_000.0;
    }

    public ZonedDateTime local() { return Instant.ofEpochMilli((long) Math.floor(epochMs)).atZone(ZONE); }
    public LocalDate date() { return local().toLocalDate(); }
    public double hour() {
        ZonedDateTime z = local();
        return z.getHour() + z.getMinute() / 60.0 + (z.getSecond() + (epochMs - Math.floor(epochMs / 1000) * 1000) / 1000) / 3600.0;
    }

    /** Sonnenhöhe und -azimut in Grad am Turm. */
    public double[] sun() { return Sun.position(epochMs, lat, lon); }

    /** Phase 15: Szenenzeit direkt setzen (ms seit 1970, UTC), etwa für „Weiter wie zuletzt“. */
    public synchronized void setEpochMillis(double ms) { epochMs = ms; }

    /** „15.10.2026 20:00 MESZ“ */
    public String label() {
        ZonedDateTime z = local();
        return DATE.format(z) + " " + TIME.format(z) + " " + zoneName(z);
    }

    static String zoneName(ZonedDateTime z) {
        boolean summer = ZONE.getRules().isDaylightSavings(z.toInstant());
        return summer ? "MESZ" : "MEZ";
    }

    public String dateLabel() { return DATE.format(local()); }
    public String timeLabel() { return TIME.format(local()) + " " + zoneName(local()); }

    // ------------------------------------------------------------------ Clock
    @Override public double t() { return t; }
    @Override public double dt() { return dt; }
    @Override public double epochMillis() { return epochMs; }
    @Override public double latDeg() { return lat; }
    @Override public double lonDeg() { return lon; }
}
