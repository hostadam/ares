package com.github.hostadam.menu;

import com.github.hostadam.utils.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Getter
public class MenuButton {

    private ItemBuilder builder;
    @Setter
    private int slot;
    private Consumer<InventoryClickEvent> clickEvent;

    public MenuButton(Material material) {
        this.builder = new ItemBuilder(material);
    }

    public MenuButton(ItemStack itemStack) {
        this.builder = new ItemBuilder(itemStack);
    }

    public MenuButton onClick(Consumer<InventoryClickEvent> clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public ItemStack constructItem() {
        return this.builder.build();
    }
}
