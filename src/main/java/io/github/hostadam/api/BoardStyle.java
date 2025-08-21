package io.github.hostadam.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public interface BoardStyle {

    default boolean shouldUpdateLines(Player player) {
        return true;
    }

    Component title(Player player);
    Component header(Player player);
    Component footer(Player player);
    List<Component> lines(Player player);
}
