package io.github.hostadam.api.menu;

import io.github.hostadam.Ares;
import io.github.hostadam.utilities.PaperUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;

import java.util.*;

@Getter
public class MenuLayout {

    private final Component title;
    private final Map<String, MenuItem> itemsByName = new HashMap<>();
    private final Map<Character, MenuItem> predefinedItems = new HashMap<>();

    private int size = 0;
    private String[] rows;

    public MenuLayout(Component title, String[] rows) {
        this.title = title;
        this.rows = rows;
        this.size = rows.length * 9;
    }

    public MenuLayout(Component title, int size) {
        this.title = title;
        this.size = size;

        int rows = Math.max(1, Math.floorDiv(size, 9));
        this.rows = new String[rows];
        Arrays.fill(this.rows, " ".repeat(rows == 1 ? size : 9));
    }

    public MenuLayout(Component title, InventoryType type) {
        this(title, type.getDefaultSize());
    }

    public MenuLayout(ConfigurationSection section) {
        if(section == null) throw new IllegalArgumentException("Configuration section is null for menu template");
        this.title = PaperUtils.stringToComponent(section.getString("title", "Inventory"));

        if(section.contains("type")) {
            InventoryType type = InventoryType.valueOf(section.getString("type").toUpperCase());
            this.size = type.getDefaultSize();
            this.createTemplate();
        } else if(section.contains("size")) {
            this.size = section.getInt("size");
            this.createTemplate();
        } else if(section.contains("rows")) {
            List<String> configuredTemplate = section.getStringList("rows");
            this.rows = new String[configuredTemplate.size()];

            for(int row = 0; row < configuredTemplate.size(); row++) {
                String templateRow = configuredTemplate.get(row);
                this.size += templateRow.length();
                this.rows[row] = templateRow;
            }
        }

        Ares ares = Bukkit.getServicesManager().load(Ares.class);
        this.populateDefaults(ares);

        if(section.contains("items")) {
            ConfigurationSection itemSection = section.getConfigurationSection("items");
            for(String key : itemSection.getKeys(false)) {
                String presetName = key.toLowerCase();

                MenuItem menuItem = new MenuItem(ares, itemSection.getConfigurationSection(key));
                this.itemsByName.put(presetName, menuItem);

                if(menuItem.hasAssignedChar()) {
                    this.predefinedItems.put(menuItem.getMenuChar(), menuItem.copy());
                }
            }
        }
    }

    private void populateDefaults(Ares ares) {
        if(ares != null) {
            for(MenuItem menuItem : ares.getAllPredefinedMenuItems()) {
                if(!menuItem.hasAssignedChar()) continue;
                this.predefinedItems.put(menuItem.getMenuChar(), menuItem.copy());
            }
        }
    }

    private void createTemplate() {
        int rows = Math.max(1, Math.floorDiv(size, 9));
        this.rows = new String[rows];
        Arrays.fill(this.rows, " ".repeat(rows == 1 ? size : 9));
    }

    public MenuItem preset(String name) {
        String lowered = name.toLowerCase();
        MenuItem menuItem = this.itemsByName.get(lowered);
        if(menuItem == null) return null;
        return menuItem.copy();
    }
}
