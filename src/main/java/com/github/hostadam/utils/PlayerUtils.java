package com.github.hostadam.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PlayerUtils {

    public static OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if(!player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }

        return player;
    }
}
