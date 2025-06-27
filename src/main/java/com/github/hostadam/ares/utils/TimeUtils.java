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

    private static int getDurationCount(long duration, int length, int factor) {
        if(factor == 0) return (int) (duration / length);
        return (int) ((duration / length) % factor);
    }

    private static int[] getDurationCounts(long duration) {
        int seconds = getDurationCount(duration, 1000, 60);
        int minutes = getDurationCount(duration, 60000, 60);
        int hours = getDurationCount(duration, 3600000, 24);
        int days = getDurationCount(duration, 86400000, 0); // Don't want to wrap days since months / years does not exist
        return new int[] { seconds, minutes, hours, days };
    }

    private static String formatDurationWord(int type, int count, boolean compact) {
        String typeName = switch (type) {
            case 0 -> (compact ? "d" : " day" + (count != 1 ? "s" : ""));
            case 1 -> (compact ? "h" : " hour" + (count != 1 ? "s" : ""));
            case 2 -> (compact ? "m" : " minute" + (count != 1 ? "s" : ""));
            case 3 -> (compact ? "s" : " second" + (count != 1 ? "s" : ""));
            default -> "";
        };

        return count + typeName;
    }

    public static String format(long duration) {
        return format(duration, true);
    }

    public static String format(long duration, boolean compact) {
        if(duration == Long.MAX_VALUE) return "Permanent";

        final int[] counts = getDurationCounts(duration);
        final int seconds = counts[0], minutes = counts[1], hours = counts[2], days = counts[3];
        final StringBuilder builder = new StringBuilder();

        if(days > 0) builder.append(formatDurationWord(0, days, compact));
        if(hours > 0) builder.append(formatDurationWord(1, hours, compact));
        if(minutes > 0) builder.append(formatDurationWord(2, minutes, compact));
        if(seconds > 0) builder.append(formatDurationWord(3, seconds, compact));

        String string = builder.toString();
        return string.isEmpty() ? "0s" : string;
    }
}
