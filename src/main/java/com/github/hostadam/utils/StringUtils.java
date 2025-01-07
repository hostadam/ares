package com.github.hostadam.utils;

import com.google.common.primitives.Ints;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final String GRAY_LINE = "§7§m";
    private static final Pattern LINE_PATTERN = Pattern.compile("%gray_line_(\\d+)%");
    private static final Pattern HEX_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("\\p{Alnum}+");

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

    public static String makeLine(int length) {
        return GRAY_LINE + " ".repeat(length);
    }

    public static boolean isAlphanumeric(String string) {
        Matcher matcher = ALPHANUMERIC_PATTERN.matcher(string);
        return matcher.matches();
    }
}
