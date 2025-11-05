package io.github.hostadam.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public interface BoardStyle {

    default boolean shouldUpdateLines(Player player) {
        return true;
    }

    List<Component> lines(Player player);
}
