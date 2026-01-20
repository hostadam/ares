package io.github.hostadam.config.lang;

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

    public void send(Audience audience) {
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
        if(this.placeholders.isEmpty() || !this.parsedMessage.needsResolution()) {
            return this.parsedMessage.getBaseComponent();
        }

        Component component = this.parsedMessage.getBaseComponent();
        component = component.replaceText(builder -> builder
                .match(ParsedMessage.getPlaceholderPattern())
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

        Pattern pattern = ParsedMessage.getPlaceholderPattern();
        String value = text.value();
        Matcher matcher = pattern.matcher(value);
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
