package io.github.hostadam.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuAction {

    private final InventoryClickEvent event;

    public MenuAction(InventoryClickEvent event) {
        this.event = event;
    }

    public Player player() {
        return (Player) event.getWhoClicked();
    }

    public ClickType type() {
        return event.getClick();
    }

    public int slot() {
        return event.getRawSlot();
    }
}
