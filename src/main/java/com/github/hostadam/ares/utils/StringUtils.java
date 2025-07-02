package com.github.hostadam.ares.utils;

import com.google.common.primitives.Ints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static final char BOX = '⬛';
    public static final Pattern UNICODE_PRIVATE_PATTERN = Pattern.compile("[\\uE000-\\uF8FF]");
    private static final Pattern HEX_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("\\p{Alnum}+");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    public static String formatBigNumeric(double value) {
        if(value >= 1000000000) {
            return DECIMAL_FORMAT.format(value / 1000000000) + "B";
        } else if(value >= 1000000.0) {
            return DECIMAL_FORMAT.format(value / 1000000.0) + "M";
        } else if(value >= 1000.0) {
            return DECIMAL_FORMAT.format(value / 1000.0) + "M";
        } else {
            return value % 1 == 0 ? Integer.toString((int) value) : DECIMAL_FORMAT.format(value);
        }
    }

    public static String formatNumeric(double d, boolean whole) {
        return whole ? Integer.toString((int) d) : DECIMAL_FORMAT.format(d);
    }

    public static String formatNumeric(double d) {
        return formatNumeric(d, false);
    }

    public static boolean isAlphanumeric(String string) {
        return ALPHANUMERIC_PATTERN.matcher(string).matches();
    }
}
