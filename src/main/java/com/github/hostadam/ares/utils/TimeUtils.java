package com.github.hostadam.ares.utils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    private static final Map<Character, Long> CHARACTERS = Map.of(
            'y', TimeUnit.DAYS.toMillis(365L),
            'M', TimeUnit.DAYS.toMillis(30L),
            'd', TimeUnit.DAYS.toMillis(1L),
            'h', TimeUnit.HOURS.toMillis(1L),
            'm', TimeUnit.MINUTES.toMillis(1L),
            's', TimeUnit.SECONDS.toMillis(1L)
    );


    /**
     * Parse a long from a string using the y-M-d-h-m-s format.
     *
     * @param string the string to parse from, with support for "permanent" or "lifetime".
     * @return the time in millis (-1 if no time was parsed, MAX_VALUE if the string was permanent)
     */
    public static long parseTime(String string) {
        if(string.contains("perm") || string.equalsIgnoreCase("lifetime")) {
            return Long.MAX_VALUE;
        }

        long result = -1;
        int startIndex = 0;
        for(int index = 0; index < string.length(); index++) {
            char charAt = string.charAt(index);
            if(!CHARACTERS.containsKey(charAt)) continue;

            try {
                String substring = string.substring(startIndex, index);
                int parsedInt = Integer.parseInt(substring);
                result += parsedInt * CHARACTERS.get(charAt);
                startIndex = (index + 1);
            } catch (Exception ignored) {}
        }

        return result > 0 ? result + 1L : result == 0 ? result : -1;
    }

    /**
     * Formats the time in a 1:00:00 format.
     *
     * @param time the time in seconds.
     * @return the formatted string
     */
    public static String format(int time) {
        int sec = time % 60;
        int min = time / 60 % 60;
        int h = time / 3600 % 24;

        return (h > 0 ? h + ":" : "") + (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
    }

    public static String format(long duration) {
        if(duration == Long.MAX_VALUE) {
            return "Permanent";
        }

        int sec = (int) (duration / 1000) % 60 ;
        int min = (int) ((duration / (1000*60)) % 60);
        int hour = (int) ((duration / (1000*60*60)) % 24);
        int days = (int) ((duration / (1000*60*60*24)) % 365);

        StringBuilder builder = new StringBuilder();

        if(days > 0) {
            builder.append(days + "d");
        }
        if(hour > 0) {
            builder.append(hour + "h");
        }
        if(min > 0) {
            builder.append(min + "m");
        }
        if(sec > 0) {
            builder.append(sec + "s");
        }

        String string = builder.toString();
        if(string.isEmpty()) {
            string = "0s";
        }

        return string;
    }
}
