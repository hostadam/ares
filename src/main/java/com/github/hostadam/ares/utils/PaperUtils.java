package com.github.hostadam.ares.utils;

import io.papermc.paper.datacomponent.item.BlockItemDataProperties;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.intellij.lang.annotations.Subst;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PaperUtils {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
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

    public static Component fromConfig(ConfigurationSection section, String path) {
        String string = section.getString(path);
        return string == null ? null : MINI.deserialize(string);
    }

    public static Component formatMiniMessage(String message) {
        return MINI.deserialize(message);
    }

    public static Component formatMiniMessage(String message, TagResolver resolver) {
        return MINI.deserialize(message, resolver);
    }
}
