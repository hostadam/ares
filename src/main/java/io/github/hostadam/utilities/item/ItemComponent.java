package io.github.hostadam.utilities.item;

import org.bukkit.configuration.ConfigurationSection;

@FunctionalInterface
public interface ItemComponent {
    void apply(ItemBuilder builder, String key, ConfigurationSection section);
}
