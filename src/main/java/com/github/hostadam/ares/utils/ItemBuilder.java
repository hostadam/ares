package com.github.hostadam.ares.utils;

import com.google.common.collect.ImmutableMultimap;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
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
        return this.unbreakable(true);
    }


    public ItemBuilder unbreakable(boolean shouldApply) {
        this.meta.setUnbreakable(shouldApply);
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
        if(this.meta instanceof PotionMeta) {
            ((PotionMeta) this.meta).setColor(color);
        }

        return this;
    }

    public ItemBuilder dyeColor(DyeColor color) {
        if(this.meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) this.meta).setColor(color.getColor());
        }

        return this;
    }

    public ItemBuilder skull(String playerName) {
        if(this.meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwner(playerName);
        }

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


    public ItemBuilder itemFlag(ItemFlag... flags) {
        this.meta.setAttributeModifiers(ImmutableMultimap.of());
        this.meta.addItemFlags(flags);
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

    public static ItemBuilder fromConfig(ConfigurationSection section) {
        if(section == null) return null;
        Material material = Material.getMaterial(section.getString("material").toUpperCase());
        int amount = section.getInt("amount", 1);
        ItemBuilder builder = new ItemBuilder(material)
                .amount(amount)
                .durability(section.getInt("durability", 0))
                .glow(section.contains("glow") && section.getBoolean("glow"))
                .unbreakable(section.contains("unbreakable") && section.getBoolean("unbreakable"));

        if(section.contains("customModelData")) {
            builder.customModelData(section.getInt("customModelData"));
        }

        if(section.contains("playerSkull")) {
            builder.skull(section.getString("playerSkull"));
        }

        if(section.contains("dyeColor")) {
            builder.dyeColor(DyeColor.valueOf(section.getString("dyeColor").toUpperCase()));
        }

        if(section.contains("itemFlags")) {
            section.getStringList("itemFlags").stream().map(string -> ItemFlag.valueOf(string.toUpperCase())).forEach(builder::itemFlag);
        }

        if(section.contains("potionColor")) {
            builder.potionColor(Color.fromRGB(section.getInt("potionColor")));
        }

        if(section.contains("displayName")) {
            builder.name(StringUtils.formatHex(section.getString("displayName")));
        }

        if(section.contains("enchantments")) {
            for(String key : section.getConfigurationSection("enchantments").getKeys(false)) {
                Enchantment enchantment = Enchantment.getByName(key.toUpperCase());
                if(enchantment == null) continue;
                int level = section.getInt("enchantments." + key, enchantment.getStartLevel());
                builder.enchant(enchantment, level);
            }
        }

        if(section.contains("lore")) {
            List<String> lore = section.getStringList("lore")
                    .stream()
                    .map(StringUtils::formatHex)
                    .toList();
            builder.lore(lore);
        }

        return builder;
    }

    public static ItemStack create(Material material, String displayName) {
        return new ItemBuilder(material).name(displayName).build();
    }
}
