package com.github.hostadam.ares.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Menu<T extends JavaPlugin> {

    private final static Map<UUID, Menu<?>> OPEN_MENUS = new ConcurrentHashMap<>();

    protected final T owningPlugin;
    protected final Player player;
    protected final Inventory inventory;
    protected final MenuLayout layout;

    private Menu<?> parent;
    private Map<Character, MenuItem> buttonBindings;
    private final Map<Integer, MenuItem> buttons = new HashMap<>();
    private final List<MenuItem> dynamicButtons = new ArrayList<>();

    private int page = 0, maxPages = 0;

    public Menu(T owningPlugin, Player player, MenuLayout layout) {
        this.owningPlugin = owningPlugin;
        this.player = player;
        this.layout = layout;
        this.inventory = Bukkit.createInventory(null, this.layout.getSize(), this.layout.getTitle());
    }

    public Menu(T owningPlugin, Player player, InventoryType type, String title) {
        this.owningPlugin = owningPlugin;
        this.player = player;
        this.layout = new MenuLayout(title, type);
        this.inventory = Bukkit.createInventory(null, type, title);
    }

    public Menu(T owningPlugin, Player player, String title, String[] template) {
        this(owningPlugin, player, new MenuLayout(title, template));
    }

    public Menu(T owningPlugin, Player player, String title, int size) {
        this(owningPlugin, player, new MenuLayout(title, size));
    }

    public void open() {
        if(this.layout == null) return;

        Menu<?> menu = OPEN_MENUS.get(player.getUniqueId());
        if(menu != null && !menu.getClass().isInstance(this)) {
            this.parent = menu;
            menu.close();
        }

        this.page = 0;
        this.render();
        OPEN_MENUS.put(player.getUniqueId(), this);

        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(this.owningPlugin, () -> this.player.openInventory(this.inventory));
        } else {
            this.player.openInventory(this.inventory);
        }
    }

    private int countDynamicSlots() {
        int count = 0;
        for (String row : this.layout.getRows()) {
            for (char c : row.toCharArray()) {
                if (c == ' ') count++;
            }
        }

        return count;
    }

    public void render() {
        this.buttons.clear();
        this.dynamicButtons.clear();
        this.buttonBindings = this.layout.getPredefinedItems();

        this.setup();

        int dynamicCount = 0, dynamicSlots = this.countDynamicSlots();
        int startIndex = this.page * dynamicSlots, endIndex = Math.min(startIndex + dynamicSlots, this.dynamicButtons.size());
        List<MenuItem> pagedItems = this.dynamicButtons.isEmpty() ? null : this.dynamicButtons.subList(startIndex, endIndex);
        this.maxPages = dynamicSlots == 0 ? 0 : (int) Math.ceil((double) this.dynamicButtons.size() / dynamicSlots);

        String[] rows = this.layout.getRows();
        for(int row = 0; row < rows.length; row++) {
            String string = rows[row];
            for(int col = 0; col < string.length(); col++) {
                int inventorySlot = (row * 9) + col;
                char c = string.charAt(col);

                if(c == ' ') {
                    if (pagedItems == null || dynamicCount >= pagedItems.size()) continue;
                    MenuItem item = pagedItems.get(dynamicCount++);
                    this.buttons.put(inventorySlot, item);
                } else {
                    MenuItem binding = this.getItemByChar(c);
                    if(binding != null) this.buttons.put(inventorySlot, binding);
                }
            }
        }

        this.inventory.clear();
        this.buttons.forEach((slot, item) -> this.inventory.setItem(slot, item.buildItem(this)));
    }

    public void add(MenuItem menuItem) {
        this.dynamicButtons.add(menuItem);
    }

    public void set(char c, MenuItem item) {
        this.buttonBindings.put(c, item);
    }

    private MenuItem getItemByChar(char c) {
        return this.buttonBindings.get(c);
    }

    public void click(InventoryClickEvent event) {
        MenuItem button = this.buttons.get(event.getRawSlot());
        if(button != null && !button.checkIfFallback(this)) {
            event.setCancelled(true);

            if(button.getType().isPageControl()) {
                button.handlePagedClick(this);
            } else if(button.getClickHandler() != null) {
                button.getClickHandler().handle(event, button);
                event.setCurrentItem(button.getItemStack());
            }
        }
    }

    public void close() {
        if(player == null) return;
        player.closeInventory();
        OPEN_MENUS.remove(player.getUniqueId());
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

    public void switchToParentMenu() {
        if(this.parent != null) {
            this.close();
            this.parent.open();
        }
    }

    public abstract void setup();

    public static Optional<Menu<?>> get(Player player) {
        return Optional.ofNullable(OPEN_MENUS.get(player.getUniqueId()));
    }
}
