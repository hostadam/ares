package io.github.hostadam.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class PlayerUtils {

    public static Player findDamagerFromEntity(Entity entity) {
        if(entity instanceof Player player) return player;
        if(entity instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

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
}
