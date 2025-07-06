package com.github.hostadam.ares.utils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    private static final Map<Character, Long> TIME_UNITS = Map.of(
            'y', TimeUnit.DAYS.toMillis(365L),
            'M', TimeUnit.DAYS.toMillis(30L),
            'd', TimeUnit.DAYS.toMillis(1L),
            'h', TimeUnit.HOURS.toMillis(1L),
            'm', TimeUnit.MINUTES.toMillis(1L),
            's', TimeUnit.SECONDS.toMillis(1L)
    );

    public static long parseTime(String string) {
        String input = string.toLowerCase();
        if(input.contains("perm") || input.equalsIgnoreCase("lifetime") )return Long.MAX_VALUE;

        long totalMillis = 0L;
        int start = 0;
        boolean found = false;

        for(int index = 0; index < input.length(); index++) {
            char charAt = input.charAt(index);
            if(!TIME_UNITS.containsKey(charAt)) continue;

            try {
                int value = Integer.parseInt(input.substring(start, index));
                totalMillis += value * TIME_UNITS.get(charAt);
                start = index + 1;
                found = true;
            } catch (Exception ignored) {}
        }

        return found ? totalMillis : -1;
    }

    public static String format(int time) {
        int sec = time % 60;
        int min = time / 60 % 60;
        int hrs = time / 3600 % 24;
        return (hrs > 0 ? hrs + ":" : "") + String.format("%02d:%02d", min, sec);
    }

    private static void append(StringBuilder builder, long amount, boolean compact, String compactString, String nonCompactString) {
        if(amount > 0) {
            if(!builder.isEmpty()) {
                builder.append(" ");
            }

            builder.append(amount).append(compact ? compactString : amount == 1 ? " " + nonCompactString : " " + nonCompactString + "s");
        }
    }

    public static String format(long duration, boolean compact) {
        if (duration == Long.MAX_VALUE) return "Permanent";
        long seconds = duration / 1000;
        long mins = seconds / 60; seconds %= 60;
        long hrs = mins / 60; mins %= 60;
        long days = hrs / 24; hrs %= 24;

        StringBuilder sb = new StringBuilder();
        append(sb, days, compact, "d", "day");
        append(sb, hrs, compact, "h", "hour");
        append(sb, mins, compact, "m", "minute");
        append(sb, seconds, compact, "s", "second");

        return !sb.isEmpty() ? sb.toString() : "0s";
    }

    public static String format(long duration) {
        return format(duration, true);
    }
}
