package com.github.hostadam.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Predicate;

public class PlayerUtils {

    public static Player getDamager(Entity entity) {
        if(entity.getType() == EntityType.PLAYER) {
            return (Player) entity;
        }

        if(entity instanceof Projectile projectile && projectile.getShooter() instanceof Player) {
            return (Player) projectile.getShooter();
        }

        return null;
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if(!player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }

        return player;
    }

    public static OfflinePlayer getOfflinePlayer(UUID uniqueId) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueId);
        if(!player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }

        return player;
    }

    public static int countItems(Player player, ItemStack itemStack) {
        final Inventory inventory = player.getInventory();
        int count = 0;

        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack item = inventory.getItem(i);
            if (item == null || !item.isSimilar(itemStack)) {
                continue;
            }

            count += item.getAmount();
        }

        return count;
    }

    public static boolean isFull(Inventory inventory, ItemStack itemStack) {
        int firstEmpty = inventory.firstEmpty();
        if(firstEmpty != -1) return false;

        int first = inventory.first(itemStack);
        if(first != -1) {
            ItemStack inventoryItem = inventory.getItem(first);
            return !(inventoryItem != null && inventoryItem.getAmount() + itemStack.getAmount() <= itemStack.getMaxStackSize());
        }

        return false;
    }

    public static void removeItem(Player player, ItemStack item, int amountToRemove) {
        if(amountToRemove <= 0) {
            return;
        }

        final PlayerInventory inventory = player.getInventory();
        final int size = inventory.getSize();

        Predicate<ItemStack> check = itemStack -> itemStack == null || item.getType() != itemStack.getType();
        for(int slot = 0; slot < size; slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if(check.test(itemStack)) {
                continue;
            }

            int newAmount = itemStack.getAmount() - amountToRemove;
            if (newAmount > 0) {
                itemStack.setAmount(newAmount);
                break;
            } else {
                inventory.clear(slot);
                amountToRemove = -newAmount;
                if (amountToRemove == 0) break;
            }
        }
    }

    public static void giveItem(Player player, ItemStack item) {
        giveItem(player, item, player.getLocation());
    }

    public static void giveItem(Player player, ItemStack item, Location location) {
        PlayerInventory inventory = player.getInventory();
        if(item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
            return;
        }

        final int firstEmpty = inventory.firstEmpty();
        final int firstItem = inventory.first(item.getType());
        if(firstEmpty == -1 && (firstItem == -1 || inventory.getItem(firstItem).getAmount() >= item.getMaxStackSize())) {
            player.getWorld().dropItem(location, item);
        } else {
            inventory.addItem(item);
        }
    }

    public static void giveItems(Player player, ItemStack[] items) {
        Arrays.stream(items).forEach(itemStack -> giveItem(player, itemStack));
    }
}
