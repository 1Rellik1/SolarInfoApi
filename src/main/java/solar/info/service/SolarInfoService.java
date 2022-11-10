package solar.info.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class SolarInfoService {

    public enum DayPeriod {
        DAY,
        CIVIL_TWILIGHT,
        NAUTICAL_TWILIGHT,
        ASTRONOMICAL_TWILIGHT,
        NIGHT
    }

    public static final double SUN_ALTITUDE_SUNRISE_SUNSET = -0.833;
    public static final double SUN_ALTITUDE_CIVIL_TWILIGHT = -6.0;
    public static final double SUN_ALTITUDE_NAUTICAL_TWILIGHT = -12.0;
    public static final double SUN_ALTITUDE_ASTRONOMICAL_TWILIGHT = -18.0;

    private static final int JULIAN_DATE_2000_01_01 = 2451545;
    private static final double CONST_0009 = 0.0009;
    private static final double CONST_360 = 360;
    private static final long MILLISECONDS_IN_DAY = 60 * 60 * 24 * 1000;

    private static class SolarEquationVariables {
        final double n;
        final double m;
        final double lambda;
        final double jtransit;
        final double delta;
        private SolarEquationVariables(double n, double m, double lambda, double jtransit, double delta) {
            this.n = n;
            this.m = m;
            this.lambda = lambda;
            this.jtransit = jtransit;
            this.delta = delta;
        }
    }

    private SolarInfoService() {
    }

    public static double getJulianDate(final Calendar gregorianDate) {
        TimeZone tzUTC = TimeZone.getTimeZone("UTC");
        Calendar gregorianDateUTC = Calendar.getInstance(tzUTC);
        gregorianDateUTC.setTimeInMillis(gregorianDate.getTimeInMillis());

        int year = gregorianDateUTC.get(Calendar.YEAR);
        int month = gregorianDateUTC.get(Calendar.MONTH) + 1;
        int day = gregorianDateUTC.get(Calendar.DAY_OF_MONTH);
        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + 12 * a - 3;

        int julianDay = day + (153 * m + 2) / 5 + 365 * y + (y / 4) - (y / 100)
                + (y / 400) - 32045;
        int hour = gregorianDateUTC.get(Calendar.HOUR_OF_DAY);
        int minute = gregorianDateUTC.get(Calendar.MINUTE);
        int second = gregorianDateUTC.get(Calendar.SECOND);

        return julianDay + ((double) hour - 12) / 24
                + ((double) minute) / 1440 + ((double) second) / 86400;
    }

    public static Calendar getGregorianDate(final double julianDate) {

        final int DAYS_PER_4000_YEARS = 146097;
        final int DAYS_PER_CENTURY = 36524;
        final int DAYS_PER_4_YEARS = 1461;
        final int DAYS_PER_5_MONTHS = 153;

        int J = (int) (julianDate + 0.5);

        int j = J + 32044;

        int g = j / DAYS_PER_4000_YEARS;
        int dg = j % DAYS_PER_4000_YEARS;

        int c = ((dg / DAYS_PER_CENTURY + 1) * 3) / 4;
        int dc = dg - c * DAYS_PER_CENTURY;

        int b = dc / DAYS_PER_4_YEARS;
        int db = dc % DAYS_PER_4_YEARS;

        int a = ((db / 365 + 1) * 3) / 4;
        int da = db - a * 365;

        int y = g * 400 + c * 100 + b * 4 + a;

        int m = (da * 5 + 308) / DAYS_PER_5_MONTHS - 2;

        int d = da - ((m + 4) * DAYS_PER_5_MONTHS) / 5 + 122;

        int year = y - 4800 + (m + 2) / 12;

        int month = (m + 2) % 12;

        int day = d + 1;

        final double dayFraction = (julianDate + 0.5) - J;

        final int hours = (int) (dayFraction * 24);
        final int minutes = (int) ((dayFraction * 24 - hours) * 60d);
        final int seconds = (int) ((dayFraction * 24 * 3600 - (hours * 3600 + minutes * 60)) + .5);

        final Calendar gregorianDateUTC = Calendar.getInstance(TimeZone
                .getTimeZone("UTC"));
        gregorianDateUTC.set(Calendar.YEAR, year);
        gregorianDateUTC.set(Calendar.MONTH, month);
        gregorianDateUTC.set(Calendar.DAY_OF_MONTH, day);
        gregorianDateUTC.set(Calendar.HOUR_OF_DAY, hours);
        gregorianDateUTC.set(Calendar.MINUTE, minutes);
        gregorianDateUTC.set(Calendar.SECOND, seconds);
        gregorianDateUTC.set(Calendar.MILLISECOND, 0);

        Calendar gregorianDate = Calendar.getInstance();
        gregorianDate.setTimeInMillis(gregorianDateUTC.getTimeInMillis());
        return gregorianDate;
    }

    public static Calendar[] getCivilTwilight(final Calendar day,
                                              final double latitude, double longitude) {
        return getSunriseSunset(day, latitude, longitude, SUN_ALTITUDE_CIVIL_TWILIGHT);
    }

    public static Calendar[] getNauticalTwilight(final Calendar day,
                                                 final double latitude, double longitude) {
        return getSunriseSunset(day, latitude, longitude, SUN_ALTITUDE_NAUTICAL_TWILIGHT);
    }

    public static Calendar[] getAstronomicalTwilight(final Calendar day,
                                                     final double latitude, double longitude) {
        return getSunriseSunset(day, latitude, longitude, SUN_ALTITUDE_ASTRONOMICAL_TWILIGHT);
    }


    public static Calendar[] getSunriseSunset(final Calendar day,
                                              final double latitude, double longitude) {
        return getSunriseSunset(day, latitude, longitude, SUN_ALTITUDE_SUNRISE_SUNSET);
    }

    private static SolarEquationVariables getSolarEquationVariables(final Calendar day, double longitude) {

        longitude = -longitude;

        final double julianDate = getJulianDate(day);

        final double nstar = julianDate - JULIAN_DATE_2000_01_01 - CONST_0009
                - longitude / CONST_360;
        final double n = Math.round(nstar);

        final double jstar = JULIAN_DATE_2000_01_01 + CONST_0009 + longitude
                / CONST_360 + n;

        final double m = Math
                .toRadians((357.5291 + 0.98560028 * (jstar - JULIAN_DATE_2000_01_01))
                        % CONST_360);

        final double c = 1.9148 * Math.sin(m) + 0.0200 * Math.sin(2 * m)
                + 0.0003 * Math.sin(3 * m);

        final double lambda = Math
                .toRadians((Math.toDegrees(m) + 102.9372 + c + 180) % CONST_360);

        final double jtransit = jstar + 0.0053 * Math.sin(m) - 0.0069
                * Math.sin(2 * lambda);

        final double delta = Math.asin(Math.sin(lambda)
                * Math.sin(Math.toRadians(23.439)));


        return new SolarEquationVariables(n, m, lambda, jtransit, delta);
    }

    public static Calendar[] getSunriseSunset(final Calendar day,
                                              final double latitude, double longitude, double sunAltitude) {

        final SolarEquationVariables solarEquationVariables = getSolarEquationVariables(day, longitude);

        longitude = -longitude;
        final double latitudeRad = Math.toRadians(latitude);

        final double omega = Math.acos((Math.sin(Math.toRadians(sunAltitude)) - Math
                .sin(latitudeRad) * Math.sin(solarEquationVariables.delta))
                / (Math.cos(latitudeRad) * Math.cos(solarEquationVariables.delta)));

        if (Double.isNaN(omega)) {
            return null;
        }

        final double jset = JULIAN_DATE_2000_01_01
                + CONST_0009
                + ((Math.toDegrees(omega) + longitude) / CONST_360 + solarEquationVariables.n + 0.0053
                * Math.sin(solarEquationVariables.m) - 0.0069 * Math.sin(2 * solarEquationVariables.lambda));

        final double jrise = solarEquationVariables.jtransit - (jset - solarEquationVariables.jtransit);
        final Calendar gregRiseUTC = getGregorianDate(jrise);
        final Calendar gregSetUTC = getGregorianDate(jset);

        final Calendar gregRise = Calendar.getInstance(day.getTimeZone());
        gregRise.setTimeInMillis(gregRiseUTC.getTimeInMillis());
        final Calendar gregSet = Calendar.getInstance(day.getTimeZone());
        gregSet.setTimeInMillis(gregSetUTC.getTimeInMillis());
        return new Calendar[]{gregRise, gregSet};
    }

    public static Calendar getSolarNoon(final Calendar day, final double latitude, double longitude) {
        SolarEquationVariables solarEquationVariables = getSolarEquationVariables(day, longitude);

        final double latitudeRad = Math.toRadians(latitude);

        final double omega = Math.acos((Math.sin(Math.toRadians(SUN_ALTITUDE_SUNRISE_SUNSET)) - Math
                .sin(latitudeRad) * Math.sin(solarEquationVariables.delta))
                / (Math.cos(latitudeRad) * Math.cos(solarEquationVariables.delta)));

        if (Double.isNaN(omega)) {
            return null;
        }

        final Calendar gregNoonUTC = getGregorianDate(solarEquationVariables.jtransit);
        final Calendar gregNoon = Calendar.getInstance(day.getTimeZone());
        gregNoon.setTimeInMillis(gregNoonUTC.getTimeInMillis());
        return gregNoon;
    }

    public static long getDayLength(Calendar calendar, double latitude, double longitude) {
        Calendar[] sunriseSunset = getSunriseSunset(calendar, latitude, longitude);
        if (sunriseSunset == null) {
            int month = calendar.get(Calendar.MONTH);
            if (latitude > 0) {
                if (month >= 3 && month <= 10) {
                    return MILLISECONDS_IN_DAY;
                } else {
                    return 0;
                }
            } else {
                if (month >= 3 && month <= 10) {
                    return 0;
                } else {
                    return MILLISECONDS_IN_DAY;
                }
            }
        }
        return sunriseSunset[1].getTimeInMillis() - sunriseSunset[0].getTimeInMillis();
    }
}
