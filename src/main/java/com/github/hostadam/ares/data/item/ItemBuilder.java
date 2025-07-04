package com.github.hostadam.ares.data.item;

import com.github.hostadam.ares.utils.PaperUtils;
import com.google.common.collect.ImmutableMultimap;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

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
        this.name(PaperUtils.stringToComponent(displayName));
        return this;
    }

    public ItemBuilder name(Component component) {
        this.itemMeta.displayName(component);
        return this;
    }

    public ItemBuilder lore(Component... components) {
        List<Component> lore = this.itemMeta.hasLore() ? this.itemMeta.lore() : List.of();
        lore.addAll(Arrays.asList(components));
        this.itemMeta.lore(lore);
        return this;
    }

    public ItemBuilder lore(Component component) {
        List<Component> lore = this.itemMeta.hasLore() ? this.itemMeta.lore() : List.of();
        lore.add(component);
        this.itemMeta.lore(lore);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        this.itemMeta.lore(lore);
        return this;
    }

    public ItemBuilder setLore(int position, Component component) {
        List<Component> lore = this.itemMeta.hasLore() ? this.itemMeta.lore() : List.of();
        lore.set(position, component);
        this.itemMeta.lore(lore);
        return this;
    }

    public ItemBuilder addLore(int position, Component component) {
        List<Component> lore = this.itemMeta.hasLore() ? this.itemMeta.lore() : List.of();
        lore.add(position, component);
        this.itemMeta.lore(lore);
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

    public ItemStack fetchCurrentItem() {
        return this.itemStack;
    }

    public ItemStack buildWithPlaceholders(Map<String, Component> placeholders) {
        if(!placeholders.isEmpty()) {
            if(this.itemMeta.hasDisplayName()) {
                Component displayName = this.itemMeta.displayName();
                Component replacedDisplayName = displayName.replaceText(builder -> {
                    for (String key : placeholders.keySet()) {
                        String placeholderTag = "<" + key + ">";
                        builder.matchLiteral(placeholderTag)
                                .replacement(placeholders.get(key));
                    }
                });

                this.itemMeta.displayName(replacedDisplayName);
            }

            if(this.itemMeta.hasLore()) {
                List<Component> lore = this.itemMeta.lore();
                List<Component> replacedLore = lore.stream().map(loreLine -> loreLine.replaceText(builder -> {
                    for(String key : placeholders.keySet()) {
                        String placeholderTag = "<" + key + ">";
                        builder.matchLiteral(placeholderTag)
                                .replacement(placeholders.get(key));
                    }
                })).toList();

                this.itemMeta.lore(replacedLore);
            }
        }

        return this.build();
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack.clone();
    }

    public static ItemStack create(Material material, String displayName) {
        return new ItemBuilder(material).name(displayName).build();
    }
}
