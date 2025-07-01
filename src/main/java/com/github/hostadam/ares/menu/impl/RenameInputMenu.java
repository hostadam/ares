package com.github.hostadam.ares.menu.impl;

import com.github.hostadam.ares.data.item.ItemBuilder;
import com.github.hostadam.ares.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class RenameInputMenu<T extends JavaPlugin> extends Menu<T> {

    private final String placeholder;
    private final int minLength, maxLength;
    private final Consumer<String> callback;

    public RenameInputMenu(T owningPlugin, Player player, String title, String placeholder, int maxLength, Consumer<String> callback) {
        super(owningPlugin, player, InventoryType.ANVIL, title);

        this.placeholder = placeholder;
        this.minLength = 1;
        this.maxLength = maxLength;
        this.callback = callback;
    }

    public RenameInputMenu(T owningPlugin, Player player, String placeholder, Consumer<String> callback) {
        super(owningPlugin, player, InventoryType.ANVIL, "§8Enter your input...");

        this.placeholder = placeholder;
        this.minLength = 1;
        this.maxLength = 50;
        this.callback = callback;
    }

    @Override
    public void setup() {
        this.inventory.setItem(0, new ItemBuilder(Material.PAPER).name(placeholder).build());
        this.inventory.setItem(2, new ItemBuilder(Material.NAME_TAG).name("§a§lSubmit").lore("§7Click here to submit your input.").build());
    }

    @Override
    public void click(InventoryClickEvent event) {
        super.click(event);
        event.setCancelled(true);

        if(event.getRawSlot() == -2) return;

        ItemStack input = inventory.getItem(0);
        if(input == null || !input.hasItemMeta() || !input.getItemMeta().hasDisplayName()) {
            return;
        }

        String result = input.getItemMeta().getDisplayName();
        if (result.isEmpty()) return;

        if (result.length() < minLength || result.length() > maxLength) {
            player.sendMessage("§cThe input must be between " + minLength + " and " + maxLength + " characters.");
            return;
        }

        this.callback.accept(result);
        this.close();
    }
}
