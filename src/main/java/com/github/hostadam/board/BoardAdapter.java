package com.github.hostadam.board;

import org.bukkit.entity.Player;

import java.util.List;

public interface BoardAdapter {

    String title(Player player);
    String[] tab(Player player);
    List<String> lines(Player player);
}
