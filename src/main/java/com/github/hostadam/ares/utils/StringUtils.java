package com.github.hostadam.ares.utils;

import com.google.common.primitives.Ints;
import net.md_5.bungee.api.ChatColor;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final String GRAY_LINE = "§7§m";
    public static final char BOX = '⬛';

    public static final Pattern UNICODE_PRIVATE_PATTERN = Pattern.compile("[\\uE000-\\uF8FF]");
    private static final Pattern LINE_PATTERN = Pattern.compile("%gray_line_(\\d+)%");
    private static final Pattern HEX_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("\\p{Alnum}+");

    /**
     * Convert a packed RGB value to a hex string
     *
     * @param rgb the rgb to convert
     * @return the color as hex
     */
    public static String hexFromRGB(int rgb) {
        rgb = rgb & 0xFFFFFF;

        final int r = (rgb >> 16) & 0xFF;
        final int g = (rgb >> 8) & 0xFF;
        final int b = rgb & 0xFF;
        return String.format("#%02X%02X%02X", r, g, b);
    }

    /**
     * Format a message with hex colors
     *
     * @param message the message to format
     * @return the message formatted with hex colors
     */
    public static String formatHex(String message) {
        Matcher match = HEX_PATTERN.matcher(message);
        while(match.find()) {
            String color = message.substring(match.start(), match.end());
            message = message.replace(color, ChatColor.of(color.substring(1)).toString());
            match = HEX_PATTERN.matcher(message);
        }

        Matcher lineMatcher = LINE_PATTERN.matcher(message);
        if(lineMatcher.find()) {
            String number = lineMatcher.group();
            if(Ints.tryParse(number) != null) {
                return makeLine(Integer.parseInt(number));
            }
        }

        return ChatColor.translateAlternateColorCodes('&', message.replace("%menu_line%", makeLine(40)));
    }

    public static String formatNumeric(double d) {
        return formatNumeric(d, false);
    }

    public static String formatNumeric(double d, boolean whole) {
        BigDecimal decimal = BigDecimal.valueOf(d);
        decimal = decimal.setScale(1, RoundingMode.HALF_UP);
        return String.valueOf(whole ? decimal.intValue() : decimal.doubleValue());
    }

    public static String makeLine(int length) {
        return GRAY_LINE + " ".repeat(length);
    }

    public static boolean isAlphanumeric(String string) {
        Matcher matcher = ALPHANUMERIC_PATTERN.matcher(string);
        return matcher.matches();
    }
}
