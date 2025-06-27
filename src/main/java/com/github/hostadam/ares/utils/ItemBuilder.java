package com.github.hostadam.ares.utils;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = Bukkit.getItemFactory().asMetaFor(itemStack.getItemMeta(), itemStack);
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material, 1));
    }

    public ItemBuilder amount(int amount) {
        this.itemStack.setAmount(Math.min(amount, this.itemStack.getMaxStackSize()));
        return this;
    }

    public ItemBuilder name(String displayName) {
        this.itemMeta.setDisplayName("§r" + StringUtils.formatHex(displayName));
        return this;
    }

    private List<String> fetchLore() {
        List<String> loreList = new ArrayList<>();
        if(this.itemMeta.hasLore()) {
            loreList = this.itemMeta.getLore();
        }

        return loreList;
    }

    public ItemBuilder lore(String... strings) {
        List<String> lore = this.fetchLore();
        lore.addAll(List.of(strings));
        this.itemMeta.setLore(lore);
        return this;
    }

    public ItemBuilder lore(String lore) {
        List<String> loreList = this.fetchLore();
        loreList.add(lore);
        this.itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        this.itemMeta.setLore(lore);
        return this;
    }

    public ItemBuilder setLore(int position, String lore) {
        List<String> loreList = this.fetchLore();
        if(position >= loreList.size()) return this;
        loreList.set(position, lore);
        this.itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder addLore(int position, String lore) {
        List<String> loreList = this.fetchLore();
        if(position >= loreList.size()) return this;
        loreList.add(position, lore);
        this.itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder glow() {
        return this.glow(true);
    }

    public ItemBuilder glow(boolean shouldApply) {
        Boolean value = shouldApply ? true : null;
        this.itemMeta.setEnchantmentGlintOverride(value);
        return this;
    }

    public ItemBuilder maxStackSize(int amount) {
        this.itemMeta.setMaxStackSize(amount);
        return this;
    }

    public ItemBuilder unbreakable() {
        return this.unbreakable(true);
    }

    public ItemBuilder unbreakable(boolean shouldApply) {
        this.itemMeta.setUnbreakable(shouldApply);
        this.itemFlag(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder potionColor(Color color) {
        if(this.itemMeta instanceof PotionMeta potionMeta) {
            potionMeta.setColor(color);
        }

        return this;
    }

    public ItemBuilder dyeColor(DyeColor color) {
        if(this.itemMeta instanceof LeatherArmorMeta armorMeta) {
            armorMeta.setColor(color.getColor());
        }

        return this;
    }

    public ItemBuilder skull(String playerName) {
        if(this.itemMeta instanceof SkullMeta skullMeta) {
            skullMeta.setOwner(playerName);
        }

        return this;
    }

    public ItemBuilder itemFlag(ItemFlag... flags) {
        this.itemMeta.setAttributeModifiers(ImmutableMultimap.of()); // TODO: ?
        this.itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder durability(int durability) {
        if(this.itemMeta instanceof Damageable damageable) {
            damageable.setDamage(durability);
        }

        return this;
    }

    public ItemBuilder tooltip(boolean tooltip) {
        this.itemMeta.setHideTooltip(!tooltip);
        return this;
    }

    public ItemBuilder customModelData(int customModelData) {
        this.itemMeta.setCustomModelData(customModelData);
        return this;
    }

    public ItemBuilder rarity(ItemRarity rarity) {
        this.itemMeta.setRarity(rarity);
        return this;
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack.clone();
    }

    public static ItemBuilder fromConfig(ConfigurationSection section) {
        if(section == null) return null;
        Material material = Material.getMaterial(section.getString("material").toUpperCase());
        int amount = section.getInt("amount", 1);
        ItemBuilder builder = new ItemBuilder(material)
                .amount(amount)
                .glow(section.contains("glow") && section.getBoolean("glow"))
                .unbreakable(section.contains("unbreakable") && section.getBoolean("unbreakable"));

        if(section.contains("durability")) {
            builder.durability(section.getInt("durability"));
        }

        if(section.contains("max-stack-size")) {
            builder.maxStackSize(section.getInt("max-stack-size"));
        }

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

        if(section.contains("rarity")) {
            builder.rarity(ItemRarity.valueOf(section.getString("rarity").toUpperCase()));
        }

        if(section.contains("tooltip")) {
            builder.tooltip(section.getBoolean("tooltip"));
        }

        if(section.contains("potionColor")) {
            builder.potionColor(Color.fromRGB(section.getInt("potionColor")));
        }

        if(section.contains("displayName")) {
            builder.name(StringUtils.formatHex(section.getString("displayName")));
        }

        if(section.contains("attribute-modifiers")) {

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
