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

import com.github.hostadam.utilities.AdventureUtils;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ItemBuilder {

    private final ItemStack bukkitItem;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this.bukkitItem = ItemStack.of(material, amount);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.bukkitItem = itemStack.clone();
    }

    public ItemBuilder withAmount(int amount) {
        this.bukkitItem.setAmount(Math.min(amount, this.bukkitItem.getMaxStackSize()));
        return this;
    }

    public ItemBuilder withName(String displayName) {
        return this.withName(AdventureUtils.parseMiniMessage(displayName));
    }

    public ItemBuilder withName(Component component) {
        this.bukkitItem.setData(DataComponentTypes.CUSTOM_NAME, component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        return this;
    }

    public ItemBuilder withLore(List<Component> lore) {
        this.bukkitItem.lore(lore);
        return this;
    }

    public ItemBuilder withLore(Component... components) {
        List<Component> lore = Optional.ofNullable(this.bukkitItem.lore()).orElseGet(ArrayList::new);
        for(Component component : components) {
            lore.add(component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        this.bukkitItem.lore(lore);
        return this;
    }

    public ItemBuilder withItemFlags(ItemFlag... flags) {
        if(flags != null && flags.length > 0) {
            this.bukkitItem.addItemFlags(flags);
        }

        return this;
    }

    @Nullable
    public <T> T retrieveData(DataComponentType.Valued<T> component) {
        T currentData = this.bukkitItem.getData(component);
        return currentData != null ? currentData : this.bukkitItem.getType().getDefaultData(component);
    }

    public ItemBuilder removeData(DataComponentType.Valued<?> component, boolean resetToDefault) {
        if(resetToDefault) {
            this.bukkitItem.resetData(component);
        } else {
            this.bukkitItem.unsetData(component);
        }

        return this;
    }

    public <T> ItemBuilder withData(DataComponentType.Valued<T> component, @NotNull T value) {
        this.bukkitItem.setData(component, value);
        return this;
    }

    public <T> ItemBuilder withDataOrIgnore(DataComponentType.Valued<T> component, @Nullable T value) {
        if(value != null) {
            this.bukkitItem.setData(component, value);
        }

        return this;
    }

    public ItemBuilder withData(DataComponentType.NonValued component, boolean value) {
        if(value) {
            this.bukkitItem.setData(component);
        } else {
            this.bukkitItem.unsetData(component);
        }

        return this;
    }

    public ItemBuilder withDataOrIgnore(DataComponentType.NonValued component, @Nullable Boolean value) {
        if(value != null) {
            return this.withData(component, value);
        }

        return this;
    }

    public ItemBuilder withDataFrom(ItemStack itemStack, Set<DataComponentType> excluded) {
        this.bukkitItem.copyDataFrom(itemStack, type -> !excluded.contains(type));
        return this;
    }

    public ItemBuilder withEnchant(Enchantment enchantment, int level) {
        this.bukkitItem.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder withPlayerSkullSkin(String playerName) {
        if(this.bukkitItem.hasItemMeta() && this.bukkitItem.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwner(playerName);
            this.bukkitItem.setItemMeta(skullMeta);
        }

        return this;
    }

    public ItemStack build() {
        return this.bukkitItem.clone();
    }
}
