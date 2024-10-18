package com.github.hostadam.utils;

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
}
