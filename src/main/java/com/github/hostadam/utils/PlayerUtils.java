package com.github.hostadam.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Predicate;

public class PlayerUtils {

    /**
     * Get the damager in e.g. a PlayerDeathEvent, based on the entity.
     * If the damager is a Projectile, we assure the Player returned is the projectile shooter.
     *
     * @param entity the entity to return as a Player
     * @return the entity returned as a Player
     */
    public static Player getDamager(Entity entity) {
        if(entity.getType() == EntityType.PLAYER) {
            return (Player) entity;
        }

        if(entity instanceof Projectile projectile && projectile.getShooter() instanceof Player) {
            return (Player) projectile.getShooter();
        }

        return null;
    }

    /**
     * Get the offline player from a name. A new offline player instance is created when using the Bukkit method.
     * Therefore, we return the offline player as null if they have never been on the server before.
     *
     * @param name the name of the player
     * @return the offline player
     */
    public static OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if(!player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }

        return player;
    }

    /**
     * Get the offline player from a uuid. A new offline player instance is created when using the Bukkit method.
     * Therefore, we return the offline player as null if they have never been on the server before.
     *
     * @param uniqueId the uuid of the player
     * @return the offline player
     */
    public static OfflinePlayer getOfflinePlayer(UUID uniqueId) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueId);
        if(!player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }

        return player;
    }

    /**
     * Count the amount of a specific item in a player's inventory
     * Includes stacks of items to ensure proper counting.
     *
     * @param player the player to check
     * @param itemStack the itemstack to look for
     * @return the amount of items
     */
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

    /**
     * Check if a player's inventory is full, even for a certain item.
     * This makes sure that items of that type, who have not been fully stacked, don't count as full.
     *
     * @param inventory the inventory to check
     * @param itemStack the itemstack to look for
     * @return if the itemstack fits in the inventory or not.
     */
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

    /**
     * Removes an item from a player's inventory.
     *
     * @param player the player to remove from
     * @param item the itemstack to remove
     * @param amountToRemove the amount of the item to remove
     */
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


    /**
     * Gives a player an item. If the item does not fit in the inventory,
     * the item is instead dropped on the ground at the player's location.
     *
     * @param player the player to give the item to
     * @param item the itemstack to give
     */
    public static void giveItem(Player player, ItemStack item) {
        giveItem(player, item, player.getLocation());
    }

    /**
     * Gives a player an item. If the item does not fit in the inventory,
     * the item is instead dropped on the ground at a specific location.
     *
     * @param player the player to give the item to
     * @param item the itemstack to give
     * @param location the location to drop the item at if the inventory is full
     */
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

    /**
     * Gives a player a list of items. If the item does not fit in the inventory,
     * the item is instead dropped on the ground at the player's location.
     *
     * @param player the player to give the item to
     * @param items the itemstacks to give
     */
    public static void giveItems(Player player, ItemStack[] items) {
        Arrays.stream(items).forEach(itemStack -> giveItem(player, itemStack));
    }

    public static void broadcast(String permission, String message) {
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission(permission))
                .forEach(player -> player.sendMessage(message));
    }

    public static void connectPlayer(JavaPlugin plugin, Player player, String server) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        output.writeUTF("Connect");
        output.writeUTF(server);
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray());
    }
}
