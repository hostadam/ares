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

package com.github.hostadam.utilities.item;

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
