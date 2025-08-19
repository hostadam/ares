package io.github.hostadam.utilities.item;

import io.github.hostadam.utilities.PaperUtils;
import com.google.common.collect.ImmutableMultimap;
import io.github.hostadam.utilities.StringUtils;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.examination.Examiner;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ItemBuilder {

    private ItemStack itemStack;
    private Pattern regexPattern;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material, 1);
    }

    public ItemBuilder switchWith(Material material) {
        this.itemStack = this.itemStack.withType(material);
        return this;
    }

    public ItemBuilder switchWith(ItemStack itemStack, Set<DataComponentType> excludedTypes) {
        this.itemStack = this.itemStack.withType(itemStack.getType());
        this.itemStack.setAmount(itemStack.getAmount());
        this.itemStack.copyDataFrom(itemStack, dataComponentType -> !excludedTypes.contains(dataComponentType));
        return this;
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
        Component formatted = component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        this.itemStack.setData(DataComponentTypes.CUSTOM_NAME, formatted);
        return this;
    }

    public ItemBuilder lore(Component... components) {
        List<Component> lore = this.itemStack.lore();
        if(lore == null && components.length > 0) {
            lore = new ArrayList<>();
        }

        for(Component component : components) {
            lore.add(component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        this.itemStack.lore(lore);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        this.itemStack.lore(lore.stream().map(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder glow() {
        return this.glow(true);
    }

    public ItemBuilder glow(Boolean value) {
        this.itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, value);
        return this;
    }

    public ItemBuilder maxStackSize(Integer amount) {
        this.itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, amount);
        return this;
    }

    public ItemBuilder unbreakable() {
        return this.unbreakable(true);
    }

    public ItemBuilder unbreakable(boolean shouldApply) {
        if(shouldApply) {
            this.itemStack.setData(DataComponentTypes.UNBREAKABLE);
        } else this.itemStack.unsetData(DataComponentTypes.UNBREAKABLE);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder dyeColor(Color color) {
        this.itemStack.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(color));
        return this;
    }

    public ItemBuilder skull(String playerName) {
        if(this.itemStack.hasItemMeta()) {
            ItemMeta itemMeta = this.itemStack.getItemMeta();
            if(itemMeta instanceof SkullMeta skullMeta) {
                skullMeta.setOwner(playerName);
                this.itemStack.setItemMeta(skullMeta);
            }
        }

        return this;
    }

    public ItemBuilder itemFlag(ItemFlag... flags) {
        this.itemStack.addItemFlags(flags);
        //this.itemMeta.setAttributeModifiers(ImmutableMultimap.of()); // Necessary on Paper
        return this;
    }

    public ItemBuilder durability(int durability) {
        this.itemStack.setData(DataComponentTypes.DAMAGE, durability);
        return this;
    }

    public ItemBuilder tooltip(boolean tooltip) {
        if(tooltip) {
            this.itemStack.unsetData(DataComponentTypes.TOOLTIP_DISPLAY);
        } else {
            this.itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        }

        return this;
    }

    public ItemBuilder rarity(ItemRarity rarity) {
        this.itemStack.setData(DataComponentTypes.RARITY, rarity);
        return this;
    }

    public ItemBuilder copy() {
        return new ItemBuilder(this.build());
    }

    public ItemStack build() {
        return this.itemStack.clone();
    }

    public ItemStack fetchCurrentItem() {
        return this.itemStack;
    }

    public ItemStack buildWithPlaceholders(Map<String, Component> placeholders) {
        if(placeholders.isEmpty()) {
            return this.build();
        }

        if(this.regexPattern == null) {
            this.regexPattern = StringUtils.combinePattern(placeholders.keySet());
        }

        ItemStack cloned = this.itemStack.clone();
        Component displayName = cloned.effectiveName();
        List<Component> lore = cloned.lore();
        UnaryOperator<Component> replacer = PaperUtils.configureReplacementOperator(this.regexPattern, placeholders);

        cloned.setData(DataComponentTypes.CUSTOM_NAME, replacer.apply(displayName).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        if(lore != null && !lore.isEmpty()) {
            cloned.lore(lore.stream().map(replacer).collect(Collectors.toList()));
        }

        return cloned;
    }

    public static ItemStack create(Material material, String displayName) {
        return new ItemBuilder(material).name(displayName).build();
    }
}
