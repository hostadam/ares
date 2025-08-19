package io.github.hostadam.utilities.item;

import io.github.hostadam.utilities.PlayerUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public class ProbableItem {

    private double chance;
    private String description;
    private ItemStack item;
    private String command;

    public ProbableItem(double chance, String description, ItemStack item) {
        this(chance, description, item, null);
    }

    public ProbableItem(double chance, ItemStack item) {
        this(chance, null, item, null);
    }

    public ProbableItem(double chance, String description, String command) {
        this(chance, description, null, command);
    }

    public ProbableItem(double chance, String command) {
        this(chance, null, null, command);
    }

    public ProbableItem(ConfigurationSection section) {
        this.chance = section.getDouble("chance");
        if(section.contains("description")) this.description = section.getString("description");
        if(section.contains("item")) this.item = ItemParser.parse(section.getConfigurationSection("item")).build();
        if(section.contains("command")) this.command = section.getString("command");
    }

    public void give(Player player) {
        if(this.item != null) PlayerUtils.giveItem(player, item);
        if(this.command != null) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
    }
}
