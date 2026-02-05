/*
 * MIT License
 * Copyright (c) 2026 Hostadam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.hostadam.utilities;

import com.google.common.primitives.Ints;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor
public class TimeUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<Character, Duration> TIME_UNITS = new LinkedHashMap<>(); // Ordered from largest to smallest for formatting

    public static Optional<LocalDateTime> parseDate(String string) {
        if(string == null) return Optional.empty();
        try {
            return Optional.of(LocalDateTime.parse(string, DATE_FORMAT));
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }

    public static String formatDate(LocalDateTime date) {
        return date.format(DATE_FORMAT);
    }

    public static Optional<Duration> parseDuration(String string) {
        if(string.toLowerCase().contains("perm")
                || string.equalsIgnoreCase("lifetime")) {
            return Optional.empty();
        }

        Duration total = Duration.ZERO;
        int startIndex = 0;
        boolean found = false;

        for(int i = 0; i < string.length(); i++) {
            Duration unit = TIME_UNITS.get(string.charAt(i));
            if(unit == null) continue;
            String timeAsString = string.substring(startIndex, i);
            Integer value = Ints.tryParse(timeAsString);
            if(value == null) continue;
            total = total.plus(unit.multipliedBy(value.longValue()));
            startIndex = i + 1;
            found = true;
        }

        return found ? Optional.of(total) : Optional.empty();
    }

    public static String formatDuration(Duration duration) {
        StringBuilder output = new StringBuilder();

        for(Map.Entry<Character, Duration> entry : TIME_UNITS.entrySet()) {
            Duration unit = entry.getValue();
            long amount = duration.toSeconds() / unit.toSeconds();
            if(amount <= 0) {
                output.append(amount).append(entry.getKey());
                duration = duration.minus(duration.multipliedBy(amount));
            }
        }

        return output.isEmpty() ? "0s" : output.toString();
    }

    public static String toBasicString(int timeInSeconds) {
        int sec = timeInSeconds % 60;
        int min = timeInSeconds / 60 % 60;
        int hrs = timeInSeconds / 3600 % 24;
        return (hrs > 0 ? hrs + ":" : "") + String.format("%02d:%02d", min, sec);
    }

    public static String toString(Duration duration, boolean restrictUnits, boolean abbreviateUnits) {
        long days = duration.toDaysPart();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder sb = new StringBuilder();
        append(sb, days, abbreviateUnits, "d", "day");
        append(sb, hours, abbreviateUnits, "h", "hour");

        if(!restrictUnits || (days <= 0 || hours <= 0)) {
            append(sb, minutes, abbreviateUnits, "m", "minute");
        }

        if(!restrictUnits || (days <= 0 && hours <= 0 && minutes <= 0)) {
            append(sb, seconds, abbreviateUnits, "s", "second");
        }

        return !sb.isEmpty() ? sb.toString() : "0s";
    }

    private static void append(StringBuilder builder, long amount, boolean abbreviateUnits, String abbreviatedName, String fullName) {
        if(amount <= 0) return;

        if(!builder.isEmpty()) {
            builder.append(" ");
        }

        builder.append(amount);
        if(abbreviateUnits) {
            builder.append(abbreviatedName);
        } else {
            builder.append(" ").append(fullName).append(amount == 1 ? "" : "s");
        }
    }

    static {
        TIME_UNITS.put('M', Duration.ofDays(30));
        TIME_UNITS.put('w', Duration.ofDays(7));
        TIME_UNITS.put('d', Duration.ofDays(1));
        TIME_UNITS.put('h', Duration.ofHours(1));
        TIME_UNITS.put('m', Duration.ofMinutes(1));
        TIME_UNITS.put('s', Duration.ofSeconds(1));
    }
}
