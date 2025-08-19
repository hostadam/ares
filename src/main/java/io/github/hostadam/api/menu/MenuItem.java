package io.github.hostadam.api.menu;

import io.github.hostadam.Ares;
import io.github.hostadam.utilities.item.ItemBuilder;
import io.github.hostadam.utilities.item.ItemParser;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class MenuItem {

    @Setter @Nullable
    private ItemBuilder itemStack;
    private ItemBuilder fallbackItem;

    private Type type = Type.NORMAL;
    private String permission;
    private MenuItemClickHandler clickHandler;
    private char menuChar = Character.UNASSIGNED;
    private final Map<String, Component> placeholders = new HashMap<>();

    public MenuItem(ItemBuilder builder) {
        this.itemStack = builder == null ? null : builder.copy();
    }

    public MenuItem(Ares ares, ConfigurationSection section) {
        this.type = Type.valueOf(section.getString("type", "NORMAL"));

        if(section.contains("template") && ares != null) {
            MenuItem menuItem = ares.getPredefinedMenuItem(section.getString("template"));
            if(menuItem != null) {
                this.rebuildItem(menuItem);
            }
        }

        if(section.contains("item")) {
            this.itemStack = ItemParser.parse(section.getConfigurationSection("item"));
        }

        if(section.contains("permission")) {
            this.permission = section.getString("permission");
        }

        if(section.contains("menu-char")) {
            this.menuChar = section.getString("menu-char").charAt(0);
        }

        if(section.contains("fallbackItem")) {
            this.fallbackItem = ItemParser.parse(section.getConfigurationSection("fallbackItem"));
        }
    }

    public MenuItem switchWithPreset(ItemBuilder builder) {
        this.itemStack = builder;
        return this;
    }

    public MenuItem switchWith(Material material) {
        this.itemStack.switchWith(material);
        return this;
    }

    public MenuItem switchWith(ItemStack itemStack) {
        this.itemStack.switchWith(itemStack, Set.of(DataComponentTypes.LORE, DataComponentTypes.CUSTOM_NAME));
        return this;
    }

    public MenuItem switchWith(ItemStack itemStack, Set<DataComponentType> excludedTypes) {
        this.itemStack.switchWith(itemStack, excludedTypes);
        return this;
    }

    public MenuItem asSkull(String owner) {
        if(owner != null) this.itemStack.skull(owner);
        return this;
    }

    public MenuItem withPlaceholder(String key, Component value) {
        this.placeholders.put("{" + key + "}", value);
        return this;
    }

    public MenuItem permission(String permission) {
        this.permission = permission;
        return this;
    }

    public MenuItem click(MenuItemClickHandler clickEvent) {
        this.clickHandler = clickEvent;
        return this;
    }

    public MenuItem type(Type type) {
        this.type = type;
        return this;
    }

    public MenuItem fallback(ItemBuilder fallbackItem) {
        this.fallbackItem = fallbackItem != null ? fallbackItem.copy() : null;
        return this;
    }

    public boolean hasAssignedChar() {
        return this.menuChar != Character.UNASSIGNED;
    }

    public boolean hasFallbackItem() {
        return this.fallbackItem != null;
    }

    public MenuItem copy() {
        MenuItem menuItem = new MenuItem(this.itemStack != null ? this.itemStack.copy() : null)
                .fallback(this.fallbackItem)
                .permission(this.permission)
                .type(this.type)
                .click(this.clickHandler);
        menuItem.menuChar = this.menuChar;
        menuItem.placeholders.clear();
        menuItem.placeholders.putAll(this.placeholders);
        return menuItem;
    }

    public void rebuildItem(MenuItem other) {
        this.itemStack = other.itemStack != null ? other.itemStack.copy() : null;
        this.fallbackItem = other.fallbackItem != null ? other.fallbackItem.copy() : null;
        this.permission = other.permission;
        this.type = other.type;
        this.menuChar = other.menuChar;

        if(this.placeholders.isEmpty() || !other.placeholders.isEmpty()) {
            this.placeholders.clear();
            this.placeholders.putAll(other.placeholders);
        }

        if(this.clickHandler == null || other.clickHandler != null) {
            this.clickHandler = other.clickHandler;
        }
    }

    public ItemStack build() {
        return this.itemStack != null ? this.itemStack.buildWithPlaceholders(this.placeholders) : new ItemStack(Material.AIR);
    }

    public enum Type {
        NORMAL,
        BACK_TO_MAIN_PAGE,
        NEXT_PAGE,
        PREVIOUS_PAGE;

        public boolean isPageControl() {
            return this == NEXT_PAGE || this == PREVIOUS_PAGE || this == BACK_TO_MAIN_PAGE;
        }
    }

    @FunctionalInterface
    public interface MenuItemClickHandler {
        void handle(InventoryClickEvent event, MenuItem item);
    }
}
