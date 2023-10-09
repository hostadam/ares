package com.github.hostadam.menu;

import com.github.hostadam.utils.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Getter
public class MenuButton extends ItemBuilder {

    @Setter
    private int slot;
    private Consumer<InventoryClickEvent> clickEvent;

    public MenuButton(Material material) {
        super(material);
    }

    public MenuButton(ItemStack itemStack) {
        super(itemStack);
    }

    public MenuButton onClick(Consumer<InventoryClickEvent> clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }
}
