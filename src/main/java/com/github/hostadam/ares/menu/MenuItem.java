package com.github.hostadam.ares.menu;

import com.github.hostadam.ares.data.item.ItemParser;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@Getter
public class MenuItem {

    @Setter
    private ItemStack itemStack;
    private ItemStack fallbackItem;

    private char menuChar = 0;
    private String permission = "";
    private Type type = Type.NORMAL;
    private MenuItemClickHandler clickHandler;

    public MenuItem(ItemStack itemStack) {
        this.itemStack = (itemStack == null ? new ItemStack(Material.AIR) : itemStack);
    }

    public MenuItem(ConfigurationSection section) {
        this.itemStack = ItemParser.parse(section.getConfigurationSection("item")).build();
        this.permission = section.getString("permission", "");
        this.type = Type.valueOf(section.getString("type", "NORMAL"));

        if(section.contains("menu-char")) {
            this.menuChar = section.getString("menu-char").charAt(0);
        }

        if(section.contains("fallbackItem")) {
            this.fallbackItem = ItemParser.parse(section.getConfigurationSection("fallback")).build();
        }
    }

    public boolean hasAssignedChar() {
        return this.menuChar != 0;
    }

    public ItemStack buildItem(Menu<?> menu) {
        if(this.checkIfFallback(menu)) return this.fallbackItem.clone();
        return itemStack.clone();
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
