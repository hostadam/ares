package com.github.hostadam.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Menu {

    private final Player player;
    private String title;
    private int size;

    private boolean paginated = true;
    private int page = 0, maxPages = 0;
    private Inventory inventory;
    private final Map<Integer, MenuButton> buttons = new HashMap<>();

    public Menu(Player player, String title, int size) {
        this.player = player;
        this.title = title;
        this.size = size;
    }

    public void init() {
        this.build();

        if(this.paginated) {
            Optional<Integer> highest = this.buttons.keySet().stream()
                    .max(Integer::compare);
            if(highest.isPresent()) {
                int highestSlot = highest.get();
                this.maxPages = (int) Math.ceil((double) highestSlot / (this.size - 1));
            }
        }
    }

    public void set(int slot, MenuButton button) {
        this.buttons.put(slot, button);
        button.setSlot(slot);
    }

    public void open() {
        this.open(0);
    }

    public void open(int page) {
        this.page = page;
        this.inventory = Bukkit.createInventory(null, this.size, this.title);

        List<MenuButton> menuButtons = this.findButtons(this.getStartOfRange(), this.getEndOfRange());
        for(int index = 0; index < menuButtons.size(); index++) {
            MenuButton button = this.buttons.get(index);

            ItemStack itemStack = this.inventory.getItem(button.getSlot());
            if(itemStack != null && itemStack.getType() == Material.AIR) {
                continue;
            }

            this.inventory.setItem(button.getSlot(), button.constructItem());
        }

        this.player.openInventory(this.inventory);
    }

    public int getStartOfRange() {
        return this.page * this.size;
    }

    public int getEndOfRange() {
        //TODO: Subtract the last row if the pages item are there to avoid conflict.
        return this.getStartOfRange() + (this.size - 1);
    }

    public void nextPage() {
        this.open(this.page + 1);
    }

    public void previousPage() {
        this.open(this.page - 1);
    }

    public boolean hasPreviousPage() {
        return this.page > 0;
    }

    public boolean hasNextPage() {
        return this.page < (this.maxPages - 1);
    }

    public boolean hasPages() {
        return this.maxPages > 0;
    }

    public List<MenuButton> findButtons(int min, int max) {
        return this.buttons.entrySet().stream()
                .filter(entry -> entry.getKey() >= min && entry.getKey() < max)
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
    }

    public boolean click(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if(slot == -1) return event.isCancelled();
        MenuButton button = this.buttons.get(slot);
        if(button != null && button.getClickEvent() != null) {
            button.getClickEvent().accept(event);
        }

        return event.isCancelled();
    }

    public void close() {}

    public abstract void build();
}
