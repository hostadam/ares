package io.github.hostadam.api.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.builder.InventoryViewBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

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

    public Menu(T owningPlugin, Player player, InventoryType type, Component title) {
        this.owningPlugin = owningPlugin;
        this.player = player;
        this.layout = new MenuLayout(title, type);
        this.inventory = Bukkit.createInventory(null, type, title);
    }

    public Menu(T owningPlugin, Player player, Component title, String[] template) {
        this(owningPlugin, player, new MenuLayout(title, template));
    }

    public Menu(T owningPlugin, Player player, Component title, int size) {
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

    public void render() {
        this.buttons.clear();
        this.dynamicButtons.clear();
        this.buttonBindings = new HashMap<>(this.layout.getPredefinedItems());

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
                    MenuItem binding = this.buttonBindings.get(c);
                    if(binding != null) {
                        this.buttons.put(inventorySlot, binding);
                    }
                }
            }
        }

        this.inventory.clear();
        this.buttons.forEach((slot, item) -> this.inventory.setItem(slot, this.buildItem(item)));
    }

    public void click(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        MenuItem button = this.buttons.get(slot);

        if(button != null) {
            event.setCancelled(true);

            if(!this.checkIfFallback(button)) {
                if(button.getType().isPageControl()) {
                    this.handlePagedClick(button);
                } else if(button.getClickHandler() != null) {
                    button.getClickHandler().handle(event, button);
                    this.buttons.put(slot, button);

                    ItemStack newItem = this.buildItem(button);
                    this.inventory.setItem(slot, newItem);
                }
            }
        }
    }

    public void close() {
        if(player == null) return;
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        OPEN_MENUS.remove(player.getUniqueId());
    }

    public void handleClose() {
        if(player == null) return;
        OPEN_MENUS.remove(player.getUniqueId());
    }

    /**
     * Pagination
     */

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
            this.parent.open();
        }
    }

    /**
     * Item modifying
     */

    public void add(MenuItem menuItem) {
        this.dynamicButtons.add(menuItem);
    }

    public void set(char c, MenuItem item) {
        this.buttonBindings.put(c, item);
    }

    public void replace(String presetName, UnaryOperator<MenuItem> currentItem) {
        MenuItem item = this.layout.preset(presetName);
        if(item == null || !item.hasAssignedChar()) return;
        this.buttonBindings.put(item.getMenuChar(), currentItem.apply(item));
    }

    public void replace(String presetName, MenuItem newItem) {
        MenuItem item = this.layout.preset(presetName);
        if(item == null || !item.hasAssignedChar()) return;
        this.buttonBindings.put(item.getMenuChar(), newItem);
    }

    /**
     * Item helpers
     */
    private int countDynamicSlots() {
        int count = 0;
        for (String row : this.layout.getRows()) {
            for (char c : row.toCharArray()) {
                if (c == ' ') count++;
            }
        }

        return count;
    }

    private ItemStack buildItem(MenuItem menuItem) {
        return this.tryFallback(menuItem).orElseGet(menuItem::build);
    }

    private void handlePagedClick(MenuItem menuItem) {
        switch (menuItem.getType()) {
            case PREVIOUS_PAGE:
                this.previousPage();
                break;
            case NEXT_PAGE:
                this.nextPage();
                break;
            case BACK_TO_MAIN_PAGE:
                this.switchToParentMenu();
                break;
        }
    }

    public Optional<ItemStack> tryFallback(MenuItem menu) {
        return checkIfFallback(menu) ? Optional.of(menu.getFallbackItem().build()) : Optional.empty();
    }

    public boolean checkIfFallback(MenuItem menuItem) {
        if(!menuItem.hasFallbackItem()) return false;
        return switch (menuItem.getType()) {
            case PREVIOUS_PAGE -> !this.hasPreviousPage();
            case NEXT_PAGE -> !this.hasNextPage();
            case BACK_TO_MAIN_PAGE -> !this.hasParent();
            default -> menuItem.getPermission() != null && !menuItem.getPermission().isEmpty() && !this.player.hasPermission(menuItem.getPermission());
        };
    }

    public abstract void setup();

    public static Optional<Menu<?>> get(Player player) {
        return Optional.ofNullable(OPEN_MENUS.get(player.getUniqueId()));
    }
}
