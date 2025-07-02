package com.github.hostadam.ares.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.intellij.lang.annotations.Subst;

import java.util.Optional;

public class PaperUtils {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public static String asString(Component component) {
        return PLAIN.serialize(component);
    }

    public static Optional<Key> key(String namespace) {
        if(namespace == null) return Optional.empty();
        try {
            Key key = Key.key(namespace);
            return Optional.of(key);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public static Component fromConfig(ConfigurationSection section, String path) {
        String string = section.getString(path);
        if(string == null) return null;
        return MINI.deserialize(string);
    }

    public static Component formatMiniMessage(String message) {
        return MINI.deserialize(message);
    }
}
