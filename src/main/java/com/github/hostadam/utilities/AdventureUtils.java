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

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AdventureUtils {

    public static final MiniMessage MINI_MESSAGE_PARSER = MiniMessage.builder()
            .tags(TagResolver.builder().resolvers(StandardTags.defaults(),
                    TagResolver.resolver("empty", (argumentQueue, context) -> Tag.selfClosingInserting(Component.empty())),
                    TagResolver.resolver("space", (argumentQueue, context) -> Tag.selfClosingInserting(Component.space()))
            ).build()
    ).build();

    private static final GsonComponentSerializer GSON_COMPONENT_SERIALIZER = GsonComponentSerializer.gson();
    private static final PlainTextComponentSerializer PLAIN_TEXT_COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();

    public static Optional<Key> key(String namespace) {
        try {
            return namespace == null ? Optional.empty() : Optional.of(Key.key(namespace));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    public static String toPlainString(Component component) {
        return PLAIN_TEXT_COMPONENT_SERIALIZER.serialize(component);
    }

    public static Component stripStyle(Component component) {
        return component.style(Style.empty())
                .children(component.children().stream()
                        .map(AdventureUtils::stripStyle)
                        .collect(Collectors.toList()));
    }

    public static Component joinStrings(Collection<String> strings) {
        return Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)), strings.stream().map(string -> Component.text(string, NamedTextColor.WHITE)).collect(Collectors.toList()));
    }

    public static Component formatTextWithPlaceholder(String miniMessageString, @TagPattern String placeholderKey, Component placeholderValue) {
        return MINI_MESSAGE_PARSER.deserialize(miniMessageString, Placeholder.component(placeholderKey, placeholderValue));
    }

    public static String serializeToMiniMessage(Component message) {
        return MINI_MESSAGE_PARSER.serialize(message);
    }

    @Nullable
    public static Component parseMiniMessage(@Nullable String message) {
        return message != null && !message.isEmpty() ? MINI_MESSAGE_PARSER.deserialize(message) : null;
    }

    public static Component convertJsonStringToComponent(String json) {
        if(json == null) return null;
        return GSON_COMPONENT_SERIALIZER.deserialize(json);
    }

    public static String convertComponentToJsonString(Component component) {
        return GSON_COMPONENT_SERIALIZER.serialize(component);
    }

    public static Component createSuccess(String message) {
        return Component.text(message, NamedTextColor.GREEN);
    }

    public static Component createError(String message) {
        return Component.text(message, NamedTextColor.RED);
    }

    public static Component createLabel(String name) {
        return Component.text(name, NamedTextColor.YELLOW).append(Component.text(": ", NamedTextColor.GRAY));
    }

    public static Component createKeyValuePair(String key, String value) {
        return createLabel(key).append(Component.text(value, NamedTextColor.WHITE));
    }

    public static Component createCheckmark(boolean check) {
        return Component.text(check ? "✔" : "✘", check ? NamedTextColor.GREEN : NamedTextColor.RED);
    }

    public static Component decorateComponent(String text, TextColor color, boolean bold, boolean italic) {
        Component component = Component.text(text, color);
        if(bold) component = component.decorate(TextDecoration.BOLD);
        if(italic) component = component.decorate(TextDecoration.ITALIC);
        return component;
    }

    public static Component createProgressBar(int value, int max, int bars, char barChar) {
        double ratio = (double) value / max;
        int filled = (int) Math.round(bars * ratio);
        int empty = bars - filled;

        String barAsString = String.valueOf(barChar);
        String bar = barAsString.repeat(filled);
        String pad = barAsString.repeat(empty);

        return Component.text()
                .append(Component.text(bar, NamedTextColor.GREEN))
                .append(Component.text(pad, NamedTextColor.GRAY))
                .append(Component.text(" " + (int) (ratio * 100) + "%", NamedTextColor.WHITE))
                .build();
    }
}
