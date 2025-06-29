package com.github.hostadam.ares.command.parameter.convertion;

import com.github.hostadam.ares.command.parameter.ParameterConverter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OnlinePlayerConverter implements ParameterConverter<Player> {
    @Override
    public Player convert(String arg) {
        return Bukkit.getPlayer(arg);
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid player.");
    }
}
