package com.github.hostadam.ares.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class PlayerUtils {

    public static Player getDamager(Entity entity) {
        if(entity instanceof Player player) return player;
        if(entity instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        return Bukkit.getOfflinePlayerIfCached(name);
    }

    /**
     * Get the offline player from an uuid. A new offline player instance is created when using the Bukkit method.
     * Therefore, we return the offline player as null if they have never been on the server before.
     *
     * @param uniqueId the uuid of the player
     * @return the offline player
     */
    public static OfflinePlayer getOfflinePlayer(UUID uniqueId) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueId);
        return (player.hasPlayedBefore() || player.isOnline()) ? player : null;
    }

    public static int countItems(Player player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();
        return Arrays.stream(inventory.getContents())
                .filter(item -> item != null && item.isSimilar(itemStack))
                .mapToInt(ItemStack::getAmount).sum();
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
        if(amountToRemove <= 0) return;
        final PlayerInventory inventory = player.getInventory();
        final ItemStack[] contents = inventory.getContents();

        for(int slot = 0; slot < contents.length && amountToRemove > 0; slot++) {
            ItemStack itemStack = contents[slot];
            if(itemStack == null || !item.isSimilar(itemStack)) continue;
            int amount = itemStack.getAmount();
            if(amount >= amountToRemove) {
                itemStack.setAmount(amount - amountToRemove);
                amountToRemove = 0;
            } else {
                inventory.clear(slot);
                amountToRemove -= amount;
            }
        }
    }

    public static void giveItem(Player player, ItemStack itemStack, Location location) {
        if(itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0) return;

        PlayerInventory inventory = player.getInventory();
        if(isFull(inventory, itemStack)) {
            player.getWorld().dropItem(location, itemStack);
        } else {
            inventory.addItem(itemStack);
        }
    }

    public static void giveItem(Player player, ItemStack item) {
        giveItem(player, item, player.getLocation());
    }

    public static void giveItems(Player player, ItemStack[] items) {
        Arrays.stream(items).filter(Objects::nonNull).forEach(itemStack -> giveItem(player, itemStack));
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

    public static void kickPlayer(JavaPlugin plugin, OfflinePlayer target, String reason) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("KickPlayer");
        output.writeUTF(target.getName());
        output.writeUTF(reason);
        Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", output.toByteArray());
    }
}
