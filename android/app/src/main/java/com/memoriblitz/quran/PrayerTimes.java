package com.memoriblitz.quran;

import java.util.Calendar;

/**
 * Compact solar prayer-time calculator for the five daily prayers.
 * Mirrors the desktop PrayerTimes. Defaults to Toronto.
 */
public final class PrayerTimes {
    private PrayerTimes() {}

    public static final double LAT = 43.6532;
    public static final double LNG = -79.3832;
    public static final double TZ  = -5.0;

    private static final double FAJR_ANGLE = -18.0;
    private static final double ISHA_ANGLE = -17.0;
    private static final double SUN_ANGLE  = -0.833;

    /** minutes-of-day for [fajr, sunrise, dhuhr, asr, maghrib, isha]. */
    public static int[] times(int y, int m, int d) {
        double jd = julian(y, m, d);
        double D = jd - 2451545.0;
        double g = deg2rad(357.529 + 0.98560028 * D);
        double q = deg2rad(280.459 + 0.98564736 * D);
        double L = deg2rad(rad2deg(q) + 1.915 * Math.sin(g) + 0.020 * Math.sin(2 * g));
        double e = deg2rad(23.439 - 0.00000036 * D);
        double RA = rad2deg(Math.atan2(Math.cos(e) * Math.sin(L), Math.cos(L))) / 15.0;
        double decl = Math.asin(Math.sin(e) * Math.sin(L));
        double eqT = rad2deg(q) / 15.0 - RA;

        double lat = deg2rad(LAT);
        double dhuhr = 12.0 - eqT + LNG / 15.0;

        int[] out = new int[6];
        out[0] = timeOf(lat, decl, dhuhr, FAJR_ANGLE, true);
        out[1] = timeOf(lat, decl, dhuhr, SUN_ANGLE, true);
        out[2] = (int) Math.round(dhuhr * 60);
        out[3] = asrTime(lat, decl, dhuhr, 1.0);
        out[4] = timeOf(lat, decl, dhuhr, SUN_ANGLE, false);
        out[5] = timeOf(lat, decl, dhuhr, ISHA_ANGLE, false);
        return out;
    }

    private static int timeOf(double lat, double decl, double dhuhr, double angleDeg, boolean before) {
        double a = deg2rad(angleDeg);
        double cosHA = (Math.sin(a) - Math.sin(lat) * Math.sin(decl)) / (Math.cos(lat) * Math.cos(decl));
        if (cosHA < -1) cosHA = -1;
        if (cosHA > 1) cosHA = 1;
        double HA = Math.acos(cosHA) / 15.0;
        double t = before ? dhuhr - HA : dhuhr + HA;
        return (int) Math.round(t * 60);
    }

    private static int asrTime(double lat, double decl, double dhuhr, double shadow) {
        double A = Math.atan2(1.0, shadow + Math.tan(Math.abs(lat - decl)));
        double cosHA = (Math.sin(A) - Math.sin(lat) * Math.sin(decl)) / (Math.cos(lat) * Math.cos(decl));
        if (cosHA < -1) cosHA = -1;
        if (cosHA > 1) cosHA = 1;
        double HA = Math.acos(cosHA) / 15.0;
        return (int) Math.round((dhuhr + HA) * 60);
    }

    private static double julian(int y, int m, int d) {
        if (m <= 2) { y -= 1; m += 12; }
        int A = y / 100;
        int B = 2 - A + A / 4;
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d + B - 1524.5;
    }

    private static double deg2rad(double d) { return d * Math.PI / 180.0; }
    private static double rad2deg(double r) { return r * 180.0 / Math.PI; }

    public static int currentPeriod() {
        Calendar c = Calendar.getInstance();
        int[] t = times(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
        int now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        if (now < t[0]) return 5;
        if (now < t[1]) return 0;
        if (now < t[2]) return 1;
        if (now < t[3]) return 2;
        if (now < t[4]) return 3;
        if (now < t[5]) return 4;
        return 5;
    }
}
