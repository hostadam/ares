package com.github.hostadam.ares.board;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public interface BoardAdapter {

    Component title(Player player);
    Component header(Player player);
    Component footer(Player player);
    List<Component> lines(Player player);
}
