package com.github.hostadam.ares.data.item;

import com.github.hostadam.ares.utils.StringUtils;
import com.google.common.collect.ImmutableMultimap;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.ArrayList;
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

    public ItemMeta fetchCurrentMeta() {
        return this.itemMeta;
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack.clone();
    }

    public static ItemStack create(Material material, String displayName) {
        return new ItemBuilder(material).name(displayName).build();
    }

    @Deprecated(forRemoval = true, since = "3.2.0")
    public static ItemBuilder fromConfig(ConfigurationSection section) {
        return ItemParser.parse(section);
    }
}
