package com.github.hostadam.command.parameter;

import com.github.hostadam.command.ParameterConverter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class OfflinePlayerConverter implements ParameterConverter<OfflinePlayer> {
    @Override
    public OfflinePlayer convert(String arg) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(arg);
        if(!player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }

        return player;
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid player.");
    }
}
