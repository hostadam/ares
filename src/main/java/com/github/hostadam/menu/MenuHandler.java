package com.github.hostadam.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuHandler implements Listener {

    private final Map<UUID, Menu> openMenus;

    public MenuHandler(JavaPlugin plugin) {
        this.openMenus = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player, Menu menu) {
        menu.open();
        this.openMenus.put(player.getUniqueId(), menu);
    }

    public void closeMenu(Player player) {
        Menu menu = this.openMenus.remove(player.getUniqueId());
        if(menu != null) {
            menu.close();
        }
    }

    public Menu get(Player player) {
        return this.openMenus.get(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.closeMenu(player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        this.closeMenu(player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Menu menu = this.get(player);
        if(menu != null) {
            menu.click(event);
        }
    }
}
