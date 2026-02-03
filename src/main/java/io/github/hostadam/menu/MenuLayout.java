package io.github.hostadam.menu;

import io.github.hostadam.menu.item.MenuItem;
import io.github.hostadam.menu.item.MenuItemType;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;

import java.util.*;

@Getter
public class MenuLayout {

    private final Component title;
    private final int size;
    private final String[] rows;

    @Setter
    private boolean allowPagination;
    private final List<MenuItem> items = new ArrayList<>();
    private final Map<Character, MenuItem> itemBindings = new HashMap<>();

    public MenuLayout(Component title, String[] rows) {
        this.title = title;
        this.rows = rows;
        this.size = rows.length * 9;
    }

    public void addItem(MenuItem menuItem) {
        this.items.add(menuItem);
    }

    public void setItem(char c, MenuItem menuItem) {
        this.itemBindings.put(c, menuItem);
    }

    public static MenuLayout of(Component title, int size) {
        int rowCount = Math.max(1, Math.floorDiv(size, 9));
        String[] rows = new String[rowCount];
        Arrays.fill(rows, " ".repeat(rowCount == 1 ? size : 9));
        return new MenuLayout(title, rows);
    }
}
