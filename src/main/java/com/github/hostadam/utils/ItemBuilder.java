package com.github.hostadam.utils;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material, 1);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material material, int data) {
        this.item = new ItemStack(material, 1, (short) data);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(Math.min(amount, 64));
        return this;
    }

    public <T, Z> ItemBuilder withPersistance(NamespacedKey key, PersistentDataType<T, Z> dataType, Z value) {
        this.meta.getPersistentDataContainer().set(key, dataType, value);
        return this;
    }

    public ItemBuilder name(String displayName) {
        this.meta.setDisplayName("§r" + StringUtils.formatHex(displayName));
        return this;
    }

    public ItemBuilder glow() {
        return this.glow(true);
    }

    public ItemBuilder glow(boolean shouldApply) {
        if(shouldApply) {
            this.enchant(Enchantment.INFINITY, 0);
            this.itemFlag(ItemFlag.HIDE_ENCHANTS);
        }

        return this;
    }

    public ItemBuilder unbreakable() {
        this.meta.setUnbreakable(!this.meta.isUnbreakable());
        this.itemFlag(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    public ItemBuilder lore(String... strings) {
        for(String string : strings) {
            addLore(string);
        }

        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder addLore(String lore) {
        List<String> loreList = new ArrayList<>();
        if(meta.hasLore()) {
            loreList = meta.getLore();
        }

        loreList.add(lore);
        this.meta.setLore(loreList);
        return this;
    }

    public ItemBuilder potionColor(Color color) {
        ((PotionMeta) this.meta).setColor(color);
        return this;
    }

    public ItemBuilder dyeColor(DyeColor color) {
        ((LeatherArmorMeta) this.meta).setColor(color.getColor());
        return this;
    }

    public ItemBuilder setLore(int pos, String lore) {
        List<String> loreList = new ArrayList<>();
        if(meta.hasLore()) {
            loreList = meta.getLore();
        }

        loreList.set(pos, lore);
        this.meta.setLore(loreList);
        return this;
    }

    public ItemBuilder addLore(int pos, String lore) {
        List<String> loreList = new ArrayList<>();
        if(meta.hasLore()) {
            loreList = meta.getLore();
        }

        loreList.add(pos, lore);
        this.meta.setLore(loreList);
        return this;
    }

    public ItemBuilder itemFlag(ItemFlag... flag) {
        this.meta.addItemFlags(flag);
        return this;
    }

    public ItemBuilder lore(List<String> list) {
        this.meta.setLore(list);
        return this;
    }

    public ItemBuilder durability(int durability) {
        this.item.setDurability((short) durability);
        return this;
    }

    public ItemBuilder customModelData(int customModelData) {
        this.meta.setCustomModelData(customModelData);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
