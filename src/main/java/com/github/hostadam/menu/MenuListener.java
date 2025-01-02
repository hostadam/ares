package com.github.hostadam.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Menu.getPlayerMenu(player).ifPresent(Menu::close);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Menu.getPlayerMenu(player).ifPresent(Menu::close);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        //TODO: Make a custom event, so we can change the menu button and stuff.
        Menu.getPlayerMenu(player).ifPresent(menu -> {
            boolean cancel = menu.click(event);
            event.setCancelled(cancel);
        });
    }
}
