package com.github.hostadam.ares.menu;

import com.github.hostadam.ares.utils.PaperUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;

import java.util.*;

@Getter
public class MenuLayout {

    private final Component title;
    private final Map<String, MenuItem> itemsByName;

    private int size = 0;
    private String[] rows;

    public MenuLayout(Component title, String[] rows) {
        this.title = title;
        this.rows = rows;
        this.itemsByName = new HashMap<>();
        this.size = rows.length * 9;
    }

    public MenuLayout(Component title, int size) {
        this.title = title;
        this.size = size;
        this.itemsByName = new HashMap<>();

        int rows = Math.max(1, Math.floorDiv(size, 9));
        this.rows = new String[rows];
        Arrays.fill(this.rows, " ".repeat(rows == 1 ? size : 9));
    }

    public MenuLayout(Component title, InventoryType type) {
        this(title, type.getDefaultSize());
    }

    private void createTemplate() {
        int rows = Math.max(1, Math.floorDiv(size, 9));
        this.rows = new String[rows];
        Arrays.fill(this.rows, " ".repeat(rows == 1 ? size : 9));
    }

    public MenuLayout(ConfigurationSection section) {
        if(section == null) throw new IllegalArgumentException("Configuration section is null for menu template");
        this.title = PaperUtils.stringToComponent(section.getString("title", "Inventory"));
        this.itemsByName = new HashMap<>();

        if(section.contains("type")) {
            InventoryType type = InventoryType.valueOf(section.getString("type").toUpperCase());
            this.size = type.getDefaultSize();
            this.createTemplate();
        } else if(section.contains("size")) {
            this.size = section.getInt("size");
            this.createTemplate();
        } else if(section.contains("rows")) {
            List<String> configuredTemplate = section.getStringList("rows");
            for(int row = 0; row < configuredTemplate.size(); row++) {
                String templateRow = configuredTemplate.get(row);
                this.size += templateRow.length();
                this.rows[row] = templateRow;
            }
        }

        if(section.contains("items")) {
            ConfigurationSection itemSection = section.getConfigurationSection("items");
            for(String key : itemSection.getKeys(false)) {
                MenuItem item = new MenuItem(itemSection.getConfigurationSection(key));
                this.itemsByName.put(key.toLowerCase(), item);
            }
        }
    }

    public MenuItem preset(String name) {
        if(!this.itemsByName.containsKey(name)) return null;
        return this.itemsByName.get(name.toLowerCase()).copy();
    }

    public Map<Character, MenuItem> getPredefinedItems() {
        Map<Character, MenuItem> mappings = new HashMap<>();
        for(MenuItem item : this.itemsByName.values()) {
            if(!item.hasAssignedChar()) continue;
            mappings.put(item.getMenuChar(), item.copy());
        }

        return mappings;
    }
}
