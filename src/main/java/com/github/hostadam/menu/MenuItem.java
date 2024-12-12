package com.github.hostadam.menu;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Getter
public class MenuItem {

    @Setter
    private ItemStack itemStack;
    private ItemStack fallbackItem;
    private String permission = "";
    private Type type = Type.NORMAL;
    private Consumer<InventoryClickEvent> clickEvent;

    public MenuItem(ItemStack itemStack) {
        if(itemStack == null) itemStack = new ItemStack(Material.AIR);
        this.itemStack = itemStack;
    }

    public MenuItem permission(String permission) {
        this.permission = permission;
        return this;
    }

    public MenuItem onClick(Consumer<InventoryClickEvent> clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public MenuItem type(Type type) {
        this.type = type;
        return this;
    }

    public MenuItem fallback(ItemStack itemStack) {
        this.fallbackItem = itemStack;
        return this;
    }

    public boolean isPaginated() {
        return this.type != Type.NORMAL;
    }

    public boolean hasFallbackItem() {
        return this.fallbackItem != null;
    }

    public MenuItem fromConfigSection(ConfigurationSection section) {
        //TODO: This
        return null;
    }

    public enum Type {
        NORMAL,
        BACK_TO_MAIN_PAGE,
        NEXT_PAGE,
        PREVIOUS_PAGE;
    }
}
