/*
 * MIT License
 * Copyright (c) 2026 Hostadam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.hostadam.menu;

import com.github.hostadam.menu.item.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Menu {

    private final static Map<UUID, Menu> OPEN_MENUS = new ConcurrentHashMap<>();
    private final JavaPlugin owningPlugin;

    private final Inventory inventory;
    private final MenuLayout layout;
    private final Map<Integer, MenuItem> itemCache = new HashMap<>();

    private Menu parent;
    private int page = 0, maxPages = 0;

    public Menu(JavaPlugin owningPlugin, Player player) {
        this.owningPlugin = owningPlugin;
        this.layout = this.createLayout(player);
        this.inventory = Bukkit.createInventory(null, this.layout.getSize(), this.layout.getTitle());
        this.open(player);
    }

    protected void open(Player player) {
        Menu menu = OPEN_MENUS.remove(player.getUniqueId());
        if(menu != null && !menu.getClass().isInstance(this)) {
            this.parent = menu;
        }

        this.page = 0;
        this.render();

        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(this.owningPlugin, () -> player.openInventory(this.inventory));
        } else {
            player.openInventory(this.inventory);
        }

        OPEN_MENUS.put(player.getUniqueId(), this);
    }

    private int countDynamicSlots() {
        int count = 0;

        for(String row : this.layout.getRows()) {
            for(int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);
                if(c == ' ') count++;
            }
        }

        return count;
    }

    private List<MenuItem> setupAndGetDynamicItems() {
        List<MenuItem> dynamicItems = this.layout.getItems();
        if(dynamicItems.isEmpty()) {
            this.maxPages = 0;
            return null;
        } else {
            final int dynamicSlots = countDynamicSlots();
            if(dynamicSlots <= 0) {
                this.maxPages = 0;
                return null;
            }

            final int dynamicSize = dynamicItems.size();
            this.maxPages = (dynamicSize + dynamicSlots - 1) / dynamicSlots;

            final int startIndex = this.page * dynamicSlots;
            final int endIndex = Math.min(startIndex + dynamicSlots, dynamicSize);

            return dynamicItems.subList(startIndex, endIndex);
        }
    }

    private void render() {
        List<MenuItem> pagedItems = this.setupAndGetDynamicItems();
        Map<Character, MenuItem> boundItems = this.layout.getItemBindings();
        String[] rows = this.layout.getRows();

        int dynamicCount = 0;

        for(int row = 0; row < rows.length; row++) {
            String rowString = rows[row];
            int startingSlot = row * 9;

            for(int col = 0; col < rowString.length(); col++) {
                int slot = startingSlot + col;
                char c = rowString.charAt(col);

                MenuItem menuItem = c == ' ' && pagedItems != null && dynamicCount < pagedItems.size() ? pagedItems.get(dynamicCount++) : boundItems.get(c);
                if(menuItem != null) {
                    this.itemCache.put(slot, menuItem);
                }
            }
        }

        this.inventory.clear();
        this.itemCache.forEach((integer, menuItem) -> this.inventory.setItem(integer, menuItem.getItem(this)));
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        MenuItem button = this.itemCache.get(slot);
        if(button == null) return;

        event.setCancelled(true);

        MenuAction action = new MenuAction(event);
        if(button.click(this, action)) {
            this.inventory.setItem(slot, button.getItem(this));
        }
    }

    public boolean hasNextPage() {
        return this.page < (this.maxPages - 1);
    }

    public boolean hasPreviousPage() {
        return this.page > 0;
    }

    public void nextPage() {
        if(!this.hasNextPage()) return;
        this.page++;
        this.render();
    }

    public void previousPage() {
        if(!this.hasPreviousPage()) return;
        this.page--;
        this.render();
    }

    public boolean hasParent() {
        return this.parent != null;
    }

    public void switchToParentMenu(Player player) {
        if(this.parent != null) {
            this.parent.open(player);
        }
    }

    public abstract MenuLayout createLayout(Player player);

    public static Optional<Menu> get(Player player) {
        return Optional.ofNullable(OPEN_MENUS.get(player.getUniqueId()));
    }

    public static void remove(Player player) {
        OPEN_MENUS.remove(player.getUniqueId());
    }
}
