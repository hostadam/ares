package com.github.hostadam.menu;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public abstract class Menu {

    private static final Map<UUID, Menu> PLAYER_MENUS = new HashMap<>();

    private final JavaPlugin plugin;
    @Getter
    protected Player player;
    @Getter
    protected Inventory inventory;
    @Setter
    private Menu parent;
    private int size;
    private String[] template;

    private Map<Integer, MenuItem> buttons = new HashMap<>();
    private Map<Character, MenuItem> permanentButtons = new HashMap<>();
    private List<MenuItem> dynamicalButtons = new ArrayList<>();
    private List<Integer> airSlots = new ArrayList<>();

    private int page = 1, maxPages = 1;

    public Menu(JavaPlugin plugin, Player player, String title, int size) {
        this.player = player;
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.size = size;
    }

    public Menu(JavaPlugin plugin, Player player, InventoryType type, String title) {
        this.player = player;
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, type, title);
        this.size = type.getDefaultSize();
    }

    public void setTemplate(String... template) {
        if(template.length != this.size / 9) {
            throw new UnsupportedOperationException("Template rows must be the same as the size of the inventory.");
        }

        for(String row : template) {
            if(row.length() != 9) {
                throw new UnsupportedOperationException("Each row must be 9 characters long.");
            }
        }

        this.template = template;
    }

    public void open() {
        if(this.template == null) {
            throw new UnsupportedOperationException("No template was found.");
        }

        Menu menu = PLAYER_MENUS.get(player.getUniqueId());
        if(menu != null && !menu.getClass().isInstance(this)) {
            this.setParent(menu);
            menu.close();
        }

        this.refresh();
        PLAYER_MENUS.put(player.getUniqueId(), this);

        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> this.player.openInventory(this.inventory));
        } else {
            this.player.openInventory(this.inventory);
        }
    }

    public void refresh() {
        this.airSlots.clear();
        this.buttons.clear();
        this.dynamicalButtons.clear();
        this.permanentButtons.clear();
        this.inventory.clear();

        this.setup();

        for(int rowIndex = 0; rowIndex < this.template.length; rowIndex++) {
            String row = this.template[rowIndex];
            for(int charIndex = 0; charIndex < row.length(); charIndex++) {
                int inventorySlot = (9 * rowIndex) + charIndex;
                char c = row.charAt(charIndex);
                if(c == ' ') {
                    this.airSlots.add(inventorySlot);
                    continue;
                }

                if(this.permanentButtons.containsKey(c)) {
                    MenuItem button = this.permanentButtons.get(c);
                    this.buttons.put(inventorySlot, button);
                }
            }
        }

        for(int i = 0; i < airSlots.size(); i++) {
            int slot = airSlots.get(i);
            int buttonIndex = ((this.page - 1) * this.airSlots.size()) + i;
            if(buttonIndex >= this.dynamicalButtons.size()) {
                break;
            }

            MenuItem button = this.dynamicalButtons.get(buttonIndex);
            this.buttons.put(slot, button);
        }

        if(!this.dynamicalButtons.isEmpty() && !this.airSlots.isEmpty()) {
            this.maxPages = (int) Math.ceil((double) this.dynamicalButtons.size() / (double) airSlots.size());
        }

        for(Map.Entry<Integer, MenuItem> buttons : this.buttons.entrySet()) {
            MenuItem button = buttons.getValue();
            ItemStack itemStack = button.getItemStack();

            if(button.hasFallbackItem()) {
                //Only show with proper permission.
                if((!button.getPermission().isEmpty() && !player.hasPermission(button.getPermission())) || (button.isPaginated() && button.hasFallbackItem() && ((button.getType() == MenuItem.Type.NEXT_PAGE && !this.hasNextPage()) || (button.getType() == MenuItem.Type.PREVIOUS_PAGE && !this.hasPreviousPage()) || (button.getType() == MenuItem.Type.BACK_TO_MAIN_PAGE && this.parent == null)))) {
                    itemStack = button.getFallbackItem();
                }
            }

            this.inventory.setItem(buttons.getKey(), itemStack);
        }
    }

    public void add(MenuItem item) {
        this.dynamicalButtons.add(item);
    }

    public void set(char c, MenuItem item) {
        this.permanentButtons.put(c, item);
    }

    public boolean click(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if(slot == -1) return event.isCancelled();

        MenuItem button = this.buttons.get(event.getRawSlot());
        if(button != null) {
            event.setCancelled(true);
            if(button.getClickEvent() != null) button.getClickEvent().accept(event);
            if(button.isPaginated()) {
                switch(button.getType()) {
                    case NEXT_PAGE:
                        this.nextPage();
                        break;
                    case PREVIOUS_PAGE:
                        this.previousPage();
                        break;
                    case BACK_TO_MAIN_PAGE:
                        this.mainPage();
                        break;
                }
            }
        }

        return event.isCancelled();
    }

    public void close() {
        PLAYER_MENUS.remove(this.player.getUniqueId());
        this.player.closeInventory();
    }

    public void mainPage() {
        this.close();
        this.parent.open();
    }

    public boolean hasNextPage() {
        return this.page < this.maxPages;
    }

    public boolean hasPreviousPage() {
        return this.page > 1;
    }

    public void nextPage() {
        if(!this.hasNextPage()) return;
        this.page++;
        this.refresh();
    }

    public void previousPage() {
        if(!this.hasPreviousPage()) return;
        this.page--;
        this.refresh();
    }

    public abstract void setup();

    public static Optional<Menu> getPlayerMenu(Player player) {
        return Optional.ofNullable(PLAYER_MENUS.get(player.getUniqueId()));
    }

    public static void clearAll() {
        Iterator<Map.Entry<UUID, Menu>> iterator = PLAYER_MENUS.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<UUID, Menu> entry = iterator.next();
            entry.getValue().close();
            iterator.remove();
        }
    }
}
