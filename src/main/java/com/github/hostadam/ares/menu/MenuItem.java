package com.github.hostadam.ares.menu;

import com.github.hostadam.ares.data.item.ItemBuilder;
import com.github.hostadam.ares.data.item.ItemParser;
import com.github.hostadam.ares.utils.internals.ScheduledForChange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@ScheduledForChange(
        since = "4.1.1",
        description = """
                Goal is to make menus fully configurable.
                The lore and name should use TagResolvers (which can feature conditional logic)
                """
)
@Getter
@AllArgsConstructor
public class MenuItem {

    @Setter
    private ItemBuilder itemStack;
    private ItemStack fallbackItem;

    private char menuChar = 0;
    private String permission = "";
    private Type type = Type.NORMAL;
    private MenuItemClickHandler clickHandler;

    private final Map<String, Component> placeholders = new HashMap<>();

    public MenuItem(ItemBuilder builder) {
        this.itemStack = (builder == null ? new ItemBuilder(Material.AIR) : builder);
    }

    public MenuItem(ConfigurationSection section) {
        this.itemStack = ItemParser.parse(section.getConfigurationSection("item"));
        this.permission = section.getString("permission", "");
        this.type = Type.valueOf(section.getString("type", "NORMAL"));

        if(section.contains("menu-char")) {
            this.menuChar = section.getString("menu-char").charAt(0);
        }

        if(section.contains("fallbackItem")) {
            this.fallbackItem = ItemParser.parse(section.getConfigurationSection("fallback")).build();
        }
    }

    public MenuItem withPlaceholder(String key, Component value) {
        this.placeholders.put(key, value);
        return this;
    }

    public boolean hasAssignedChar() {
        return this.menuChar != 0;
    }

    public ItemStack buildItem(Menu<?> menu) {
        if(this.checkIfFallback(menu)) return this.fallbackItem.clone();
        return itemStack.buildWithPlaceholders(this.placeholders);
    }

    public void rebuildItem(MenuItem copyFrom) {
        this.itemStack = copyFrom.itemStack;
        this.fallbackItem = copyFrom.fallbackItem;
        this.placeholders.clear();
        this.placeholders.putAll(copyFrom.placeholders);
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

    public MenuItem fallback(ItemStack fallbackItem) {
        this.fallbackItem = fallbackItem;
        return this;
    }

    public void handlePagedClick(Menu<?> menu) {
        switch (this.type) {
            case PREVIOUS_PAGE:
                menu.previousPage();
                break;
            case NEXT_PAGE:
                menu.nextPage();
                break;
            case BACK_TO_MAIN_PAGE:
                menu.switchToParentMenu();
                break;
        }
    }

    public boolean checkIfFallback(Menu<?> menu) {
        if(this.fallbackItem == null) return false;
        return switch (this.type) {
            case PREVIOUS_PAGE -> !menu.hasPreviousPage();
            case NEXT_PAGE -> !menu.hasNextPage();
            case BACK_TO_MAIN_PAGE -> !menu.hasParent();
            default -> !this.permission.isEmpty() && !menu.player.hasPermission(this.permission);
        };
    }

    public boolean hasFallbackItem() {
        return this.fallbackItem != null;
    }

    public MenuItem copy() {
        MenuItem menuItem = new MenuItem(this.itemStack).fallback(this.fallbackItem).permission(this.permission).type(this.type).click(this.clickHandler);
        menuItem.menuChar = this.menuChar;
        menuItem.placeholders.clear();
        menuItem.placeholders.putAll(this.placeholders);
        return menuItem;
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
