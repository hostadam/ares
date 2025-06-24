package com.github.hostadam.ares.menu;

import com.github.hostadam.ares.utils.StringUtils;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;

import java.util.*;

@Getter
public class MenuLayout {

    private final String title;
    private final Map<String, MenuItem> itemsByName;

    private int size = 0;
    private String[] rows;

    public MenuLayout(String title, String[] rows) {
        this.title = title;
        this.rows = rows;
        this.itemsByName = new HashMap<>();
        this.size = rows.length * 9;
    }

    public MenuLayout(String title, int size) {
        this.title = title;
        this.size = size;
        this.itemsByName = new HashMap<>();

        int rows = Math.max(1, Math.floorDiv(size, 9));
        this.rows = new String[rows];
        Arrays.fill(this.rows, " ".repeat(rows == 1 ? size : 9));
    }

    public MenuLayout(String title, InventoryType type) {
        this(title, type.getDefaultSize());
    }

    private void createTemplate() {
        int rows = Math.max(1, Math.floorDiv(size, 9));
        this.rows = new String[rows];
        Arrays.fill(this.rows, " ".repeat(rows == 1 ? size : 9));
    }

    public MenuLayout(ConfigurationSection section) {
        if(section == null) throw new IllegalArgumentException("Configuration section is null for menu template");
        this.title = StringUtils.formatHex(section.getString("title", "Inventory"));
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
        return this.itemsByName.get(name.toLowerCase());
    }

    public Map<Character, MenuItem> getPredefinedItems() {
        Map<Character, MenuItem> mappings = new HashMap<>();
        for(MenuItem item : this.itemsByName.values()) {
            if(!item.hasAssignedChar()) continue;
            mappings.put(item.getMenuChar(), item);
        }

        return mappings;
    }
}
