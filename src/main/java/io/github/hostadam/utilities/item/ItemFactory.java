package io.github.hostadam.utilities.item;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemFactory {

    private final ItemDeserializer deserializer;
    private final Map<String, ItemStack> templates;

    public ItemFactory() {
        this.templates = new HashMap<>();
        this.deserializer = new ItemDeserializer(this);
    }

    public void registerTemplate(String key, ItemStack itemStack) {
        if(this.templates.containsKey(key.toLowerCase())) {
            throw new IllegalArgumentException("Duplicate template key: " + key);
        }

        this.templates.put(key.toLowerCase(), itemStack);
    }

    public ItemBuilder newBuilder(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder newBuilder(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public ItemBuilder newBuilder(String templateKey) {
        ItemStack itemStack = this.templates.get(templateKey.toLowerCase());
        if(itemStack == null) {
            throw new IllegalArgumentException("No registered template with key " + templateKey);
        }

        return this.newBuilder(itemStack);
    }

    public ItemBuilder newBuilder(ConfigurationSection section) {
        return this.newBuilder(section, null);
    }

    public ItemBuilder newBuilder(ConfigurationSection section, String templateKey) {
        if(section == null) {
            throw new RuntimeException("ConfigurationSection may not be null.");
        }

        ItemStack itemStack = this.deserializer.fromConfigurationSection(section);
        if(itemStack == null) {
            throw new RuntimeException("ItemBuilder could not be deserialized from ConfigurationSection " + section.getName() + ".");
        }

        if(templateKey != null && !templateKey.isEmpty()) {
            this.templates.put(templateKey.toLowerCase(), itemStack);
        }

        return this.newBuilder(itemStack);
    }
}
