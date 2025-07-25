package com.github.hostadam.ares.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PaperUtils {

    private static final MiniMessage MINI = MiniMessage.builder()
            .tags(TagResolver.builder().resolvers(
                    StandardTags.hoverEvent(),
                    StandardTags.reset(),
                    StandardTags.clickEvent(),
                    StandardTags.decorations(),
                    StandardTags.rainbow(),
                    StandardTags.color(),
                    StandardTags.shadowColor(),
                    StandardTags.gradient(),
                    TagResolver.resolver("space", (argumentQueue, context) -> Tag.selfClosingInserting(Component.space()))
            ).build()
    ).build();

    private static final GsonComponentSerializer JSON = GsonComponentSerializer.gson();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public static String asString(Component component) {
        return PLAIN.serialize(component);
    }

    public static Optional<Key> key(String namespace) {
        try {
            return namespace == null ? Optional.empty() : Optional.of(Key.key(namespace));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    public static Component strip(Component component) {
        return component.style(Style.empty())
                .children(component.children().stream()
                        .map(PaperUtils::strip)
                        .toList());
    }

    public static Component join(List<String> strings) {
        return Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)), strings.stream().map(string -> Component.text(string, NamedTextColor.WHITE)).toList());
    }

    public static Component fromConfig(ConfigurationSection section, String path) {
        String string = section.getString(path);
        return string == null ? null : MINI.deserialize(string);
    }

    public static String componentToString(Component message) {
        return MINI.serialize(message);
    }

    public static Component stringToComponent(String message) {
        return MINI.deserialize(message);
    }

    public static Component stringToComponent(String message, TagResolver resolver) {
        return MINI.deserialize(message, resolver);
    }

    public static Component jsonToComponent(String json) {
        return JSON.deserialize(json);
    }

    public static String componentToJson(Component component) {
        return JSON.serialize(component);
    }

    public static Component success(String message) {
        return Component.text(message, NamedTextColor.GREEN);
    }

    public static Component error(String message) {
        return Component.text(message, NamedTextColor.RED);
    }

    public static Component label(String name) {
        return Component.text(name, NamedTextColor.YELLOW).append(Component.text(": ", NamedTextColor.GRAY));
    }

    public static Component keyValue(String key, String value) {
        return label(key).append(Component.text(value, NamedTextColor.WHITE));
    }

    public static Component checkmark(boolean check) {
        return Component.text(check ? "✔" : "✘", check ? NamedTextColor.GREEN : NamedTextColor.RED).decorate(TextDecoration.BOLD);
    }

    public static Component decorated(String text, TextColor color, boolean bold, boolean italic) {
        Component component = Component.text(text, color);
        if(bold) component = component.decorate(TextDecoration.BOLD);
        if(italic) component = component.decorate(TextDecoration.ITALIC);
        return component;
    }

    public static Component progressBar(int value, int max, int bars, char barChar) {
        double ratio = (double) value / max;
        int filled = (int) Math.round(bars * ratio);
        int empty = bars - filled;

        String barAsString = String.valueOf(barChar);
        String bar = barAsString.repeat(filled);
        String pad = barAsString.repeat(empty);

        return Component.text()
                .append(Component.text(bar, NamedTextColor.GREEN))
                .append(Component.text(pad, NamedTextColor.GRAY))
                .append(Component.text(" " + (int)(ratio * 100) + "%", NamedTextColor.WHITE))
                .build();
    }

    public static String repeat(char c, int times) {
        return String.valueOf(c).repeat(Math.max(0, times));
    }

    public static Component coloredList(List<String> items, TextColor bulletColor, TextColor textColor) {
        List<TextComponent> lines = items.stream()
                .map(item -> Component.text("• ", bulletColor).append(Component.text(item, textColor)))
                .toList();
        return Component.join(JoinConfiguration.separator(Component.newline()), lines);
    }

    public static Component coloredList(String... items) {
        return coloredList(Arrays.asList(items), NamedTextColor.DARK_GRAY, NamedTextColor.WHITE);
    }
}
