package com.github.hostadam.menu;

import com.github.hostadam.utils.ItemBuilder;
import com.github.hostadam.utils.StringUtils;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MenuTemplate {

    private String inventoryTitle;
    private int inventoryRows;
    private String[] template;
    private Map<String, ItemStack> itemsByName;

    public MenuTemplate(ConfigurationSection section) {
        this.inventoryTitle = StringUtils.formatHex(section.getString("title", "Inventory"));
        this.inventoryRows = section.getInt("rows", 1);
        this.template = new String[this.inventoryRows];
        this.itemsByName = new HashMap<>();

        if(section.contains("template")) {
            List<String> configuredTemplate = section.getStringList("template");
            if(configuredTemplate.size() != template.length) {
                throw new IllegalArgumentException("Inventory size does not match template.");
            }

            for(int row = 0; row < configuredTemplate.size(); row++) {
                String templateRow = configuredTemplate.get(row);
                if(templateRow.length() != 9) {
                    throw new IllegalArgumentException("Template row must be 9 characters long.");
                }

                this.template[row] = templateRow;
            }
        }

        if(section.contains("items")) {
            for(String key : section.getConfigurationSection("items").getKeys(false)) {
                ItemStack itemStack = ItemBuilder.fromConfig(section.getConfigurationSection("items." + key)).build();
                this.itemsByName.put(key.toLowerCase(), itemStack);
            }
        }
    }

    public MenuItem preset(String name) {
        ItemStack itemStack = this.itemsByName.get(name.toLowerCase());
        if(itemStack == null) {
            return null;
        }

        return new MenuItem(itemStack.clone());
    }
}
