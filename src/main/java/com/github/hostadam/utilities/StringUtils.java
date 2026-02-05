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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern PUA_PATTERN = Pattern.compile("[\uE000-\uF8FF]|[\uDB80-\uDBBF][\uDC00-\uDFFF]|[\uDBC0-\uDBFF][\uDC00-\uDFFF]");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("\\p{Alnum}+");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private static final Map<Character, Character> DIGIT_MAPPER = Map.of('0', 'o', '1', 'i', '2', 'z', '3', 'e', '4', 'a', '5', 's', '6', 'g', '7', 't', '8', 'b', '9', 'p');

    public static Component replaceIllegalCharacters(Component component) {
        return component.replaceText(config -> config.match(PUA_PATTERN).replacement(""));
    }

    public static String convertNumbersToCharacter(String input) {
        StringBuilder builder = new StringBuilder();
        for(char c : input.toCharArray()) {
            builder.append(Character.isDigit(c) ? DIGIT_MAPPER.get(c) : c);
        }

        return builder.toString();
    }

    public static Component formatPercentage(double d) {
        int percentage = (int) Math.round(d * 100);
        return Component.text(percentage + "%", (percentage > 90 ? NamedTextColor.DARK_GREEN : percentage > 0.5 ? NamedTextColor.GREEN : percentage > 0.10 ? NamedTextColor.YELLOW : percentage > 0.5 ? NamedTextColor.RED : NamedTextColor.DARK_RED));
    }

    public static String formatBigNumeric(double value) {
        if(value >= 1000000000) {
            return DECIMAL_FORMAT.format(value / 1000000000) + "B";
        } else if(value >= 1000000.0) {
            return DECIMAL_FORMAT.format(value / 1000000.0) + "M";
        } else if(value >= 1000.0) {
            return DECIMAL_FORMAT.format(value / 1000.0) + "k";
        } else {
            return value % 1 == 0 ? Integer.toString((int) value) : DECIMAL_FORMAT.format(value);
        }
    }

    public static String formatNumeric(double d, boolean whole) {
        return whole ? Integer.toString((int) d) : DECIMAL_FORMAT.format(d);
    }

    public static boolean isAlphanumeric(String string) {
        return ALPHANUMERIC_PATTERN.matcher(string).matches();
    }
}
