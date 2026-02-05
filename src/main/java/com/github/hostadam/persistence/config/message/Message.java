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

package com.github.hostadam.persistence.config.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    private final ParsedMessage parsedMessage;
    private final Map<String, Component> placeholders = new HashMap<>();

    public Message(ParsedMessage parsedMessage) {
        this.parsedMessage = parsedMessage;
    }

    public Message withPlaceholder(String key, Component placeholder) {
        if(this.placeholders.containsKey(key)) {
            throw new IllegalStateException("Duplicate placeholder: " + key);
        }

        this.placeholders.put(key, placeholder);
        return this;
    }

    public void sendTo(Audience audience) {
        Component component = this.construct();
        audience.sendMessage(component);
    }

    public void broadcast() {
        Component component = this.construct();
        Bukkit.broadcast(component);
    }

    public void broadcast(String permission) {
        Component component = this.construct();
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission(permission))
                .forEach(player -> player.sendMessage(component));
        Bukkit.getConsoleSender().sendMessage(component);
    }

    public Component fetch() {
        return this.construct();
    }

    private Component construct() {
        Component component = this.parsedMessage.baseComponent();
        if(this.placeholders.isEmpty() || !this.parsedMessage.needsResolution()) {
            return component;
        }

        component = component.replaceText(builder -> builder
                .match(MessageConfig.PLACEHOLDER_PATTERN)
                .replacement((matchResult, _) -> {
                    String key = matchResult.group(1);
                    Component value = this.placeholders.get(key);
                    return value != null ? value : Component.text(matchResult.group());
                })
        );

        return this.parsedMessage.hasNestedPlaceholders() ? this.constructWithNestedPlaceholders(component) : component;
    }

    private Component constructWithNestedPlaceholders(Component component) {
        ClickEvent clickEvent = component.clickEvent();
        if(clickEvent == null || !(clickEvent.payload() instanceof ClickEvent.Payload.Text text)) {
            return component;
        }

        String value = text.value();
        Matcher matcher = MessageConfig.PLACEHOLDER_PATTERN.matcher(value);
        if(!matcher.find()) {
            return component;
        }

        String replacedValue = matcher.replaceAll(matchResult -> {
            Component replacement = this.placeholders.get(matchResult.group(1));
            return replacement instanceof TextComponent textComponent ? textComponent.content() : matchResult.group();
        });

        return component.clickEvent(ClickEvent.clickEvent(clickEvent.action(), replacedValue));
    }
}
