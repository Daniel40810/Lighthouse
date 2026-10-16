package com.dan.lighthouse.scene;

/**
 * Der Ort der Szene: Leuchtturm Roter Sand in der Außenweser (F1, F2 = A). Echter Maßstab: 1 Einheit = 1 m,
 * Ursprung in der Turmachse auf mittlerem Meeresspiegel, x nach Osten, y nach oben, Nord = −z. Alles, was weiter
 * als ein paar hundert Meter reicht (Kimm, Tragweite, ferne Schiffe), rechnet die Erdkrümmung gleich ein.
 * <p>
 * Maße aus der Recherche im Werkbuch (Abschnitt 3); Durchmesser aus den Fotos geschätzt [G].
 */
public final class Site {
    private Site() { }

    /** 53°51′18″ N, 8°04′45″ E. */
    public static final double LAT = 53 + 51 / 60.0 + 18 / 3600.0, LON = 8 + 4 / 60.0 + 45 / 3600.0;
    public static final int SRID = 8307;
    public static final String ZONE = "Europe/Berlin";
    /** Mittlerer Erdradius in m. */
    public static final double EARTH_R = 6_371_000;
    /** Wirksamer Radius für die Sicht mit normaler Lichtbrechung (k = 0,13), wie in Seekarten üblich. */
    public static final double EARTH_R_OPTICAL = EARTH_R / (1 - 0.13);
    public static final double NM = 1852;

    /** Höhen am Turm in m über mittlerem Meeresspiegel. */
    public static final double TOP = 30.7, LIGHT = 24.0, GALLERY = 23.0, PLINTH_TOP = 8.0;
    /** Halber Tidenhub der Außenweser (rund 3 m Hub). */
    public static final double TIDE_HALF_RANGE = 1.5;

    /** Absenkung der Meeresoberfläche in d m Entfernung durch die Erdkrümmung: d²/2R (100 km → 785 m). */
    public static double curvatureDrop(double d) { return d * d / (2 * EARTH_R); }

    /** Entfernung der Kimm für ein Auge h m über dem Wasser, mit normaler Lichtbrechung (m). */
    public static double horizon(double h) { return Math.sqrt(2 * EARTH_R_OPTICAL * Math.max(0, h)); }

    /** Geografische Tragweite in Seemeilen zwischen Feuerhöhe hf und Augenhöhe ha (Formel der Seezeichenverzeichnisse 2,075·√h). */
    public static double geographicRangeNm(double hf, double ha) { return 2.075 * (Math.sqrt(Math.max(0, hf)) + Math.sqrt(Math.max(0, ha))); }

    /** Kimmtiefe in Grad: so weit liegt die Kimm unter der Waagerechten (ohne Brechung). */
    public static double dipDeg(double h) { return Math.toDegrees(Math.acos(EARTH_R / (EARTH_R + Math.max(0, h)))); }

    /** Breite und Länge eines Szenenpunkts (x Ost, z Süd), für Datenbank und Anzeige. */
    public static double lat(double z) { return LAT - Math.toDegrees(z / EARTH_R); }
    public static double lon(double x) { return LON + Math.toDegrees(x / (EARTH_R * Math.cos(Math.toRadians(LAT)))); }
    public static double x(double lon) { return Math.toRadians(lon - LON) * EARTH_R * Math.cos(Math.toRadians(LAT)); }
    public static double z(double lat) { return -Math.toRadians(lat - LAT) * EARTH_R; }

    /**
     * Orte an der Küste, deren Licht nachts als Glocke über der Kimm steht (Phase 3). Stärke grob nach Größe und
     * Hafenbeleuchtung [G]: Bremerhaven 1, Wilhelmshaven mit dem Containerhafen 0,6, Cuxhaven 0,35, Helgoland 0,1.
     */
    public record Town(String name, double lat, double lon, double strength) {
        /** Richtung vom Turm in Grad von Nord und Entfernung in km. */
        public double azimuthDeg() { double a = Math.toDegrees(Math.atan2(x(lon), -z(lat))); return a < 0 ? a + 360 : a; }
        public double distanceKm() { return Math.hypot(x(lon), z(lat)) / 1000; }
    }

    public static final Town[] TOWNS = {
            new Town("Bremerhaven", 53.540, 8.581, 1.0),
            new Town("Wilhelmshaven", 53.522, 8.122, 0.6),
            new Town("Cuxhaven", 53.861, 8.694, 0.35),
            new Town("Helgoland", 54.181, 7.885, 0.1)};

    /** „53°51′18″ N · 8°04′45″ E“ */
    public static String dms(double lat, double lon) {
        return dms(lat, lat >= 0 ? "N" : "S") + " · " + dms(lon, lon >= 0 ? "E" : "W");
    }

    private static String dms(double v, String h) {
        v = Math.abs(v);
        int d = (int) v;
        double mm = (v - d) * 60;
        int m = (int) mm;
        int s = (int) Math.round((mm - m) * 60);
        if (s == 60) { s = 0; m++; }
        if (m == 60) { m = 0; d++; }
        return String.format("%d°%02d′%02d″ %s", d, m, s, h);
    }
}
