package com.github.hostadam.ares.menu;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Getter
public class MenuItem {

    //TODO: load from config.

    @Setter
    private ItemStack itemStack;
    private ItemStack fallbackItem;
    private String permission = "";
    private Type type = Type.NORMAL;
    private BiConsumer<InventoryClickEvent, MenuItem> clickEvent;

    private Map<String, Supplier<Object>> placeholders;

    public MenuItem(ItemStack itemStack) {
        if(itemStack == null) itemStack = new ItemStack(Material.AIR);
        this.itemStack = itemStack;
    }

    private void replaceDisplayName(ItemMeta meta) {
        if(!meta.getDisplayName().contains("%")) return;

        for(Map.Entry<String, Supplier<Object>> entry : this.placeholders.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue().get();
            if(object instanceof List<?>) continue;
            meta.setDisplayName(meta.getDisplayName().replace(key, object.toString()));
        }
    }

    public ItemStack buildItem() {
        if(this.placeholders == null || !this.itemStack.hasItemMeta()) {
            return this.itemStack;
        }

        ItemStack cloned = this.itemStack.clone();
        ItemMeta meta = cloned.getItemMeta();

        if(!meta.hasDisplayName() && !meta.hasLore()) {
            return this.itemStack;
        }

        this.replaceDisplayName(meta);
        List<String> newLore = new ArrayList<>();

        for(int index = 0; index < meta.getLore().size(); index++) {
            String lore = meta.getLore().get(index);
            boolean expanded = false;

            for(Map.Entry<String, Supplier<Object>> entry : this.placeholders.entrySet()) {
                String key = entry.getKey();
                Supplier<Object> value = entry.getValue();
                Object replacement = value.get();

                if(!lore.contains(key)) continue;

                if(replacement instanceof List<?> list) {
                    if(!expanded) {
                        list.forEach(object -> newLore.add(lore.replace(key, object.toString())));
                        expanded = true;
                        continue;
                    }

                    for(int listIndex = 0; listIndex < list.size(); listIndex++) {
                        Object object = list.get(listIndex);
                        final int newIndex = index + listIndex;
                        if(newIndex >= newLore.size()) continue;
                        String loreLine = newLore.get(newIndex);
                        newLore.set(newIndex, loreLine.replace(key, object.toString()));
                    }
                } else {
                    newLore.add(lore.replace(key, replacement.toString()));
                }
            }
        }

        meta.setLore(newLore);
        cloned.setItemMeta(meta);
        return cloned;
    }

    public MenuItem withPlaceholder(String key, Supplier<Object> value) {
        if(this.placeholders == null) {
            this.placeholders = new HashMap<>();
        }

        this.placeholders.put(key, value);
        return this;
    }

    public MenuItem permission(String permission) {
        this.permission = permission;
        return this;
    }

    public MenuItem onClick(BiConsumer<InventoryClickEvent, MenuItem> clickEvent) {
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

    public enum Type {
        NORMAL,
        BACK_TO_MAIN_PAGE,
        NEXT_PAGE,
        PREVIOUS_PAGE;
    }
}
