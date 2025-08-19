package io.github.hostadam.utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {

    public static final char BOX = '⬛';
    private static final Pattern PUA_PATTERN = Pattern.compile("[\uE000-\uF8FF]|[\uDB80-\uDBBF][\uDC00-\uDFFF]|[\uDBC0-\uDBFF][\uDC00-\uDFFF]");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("\\p{Alnum}+");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Map<Character, Character> DIGIT_MAPPER = Map.of('0', 'o', '1', 'i', '2', 'z', '3', 'e', '4', 'a', '5', 's', '6', 'g', '7', 't', '8', 'b', '9', 'p');

    public static String convertNumbersToCharacter(String input) {
        StringBuilder builder = new StringBuilder();
        for(char c : input.toCharArray()) {
            builder.append(Character.isDigit(c) ? DIGIT_MAPPER.get(c) : c);
        }

        return builder.toString();
    }

    public static Pattern combinePattern(Collection<String> strings) {
        return Pattern.compile(strings.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|")
        ));
    }

    public static Component replaceIllegalCharacters(Component component) {
        return component.replaceText(config -> config.match(PUA_PATTERN).replacement(""));
    }

    public static Component formatPercentage(double d) {
        int percentage = (int) Math.round(d * 100);
        return Component.text(percentage + "%", (percentage > 90 ? NamedTextColor.DARK_GREEN : percentage > 0.5 ? NamedTextColor.GREEN : percentage > 0.10 ? NamedTextColor.YELLOW : percentage > 0.5 ? NamedTextColor.RED : NamedTextColor.DARK_RED));
    }

    public static Date fetchDate(String string) {
        if(string == null) return null;
        try {
            return DATE_FORMAT.parse(string);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String formatFullDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static String formatFullDate(long time) {
        return DATE_FORMAT.format(new Date(time));
    }

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
