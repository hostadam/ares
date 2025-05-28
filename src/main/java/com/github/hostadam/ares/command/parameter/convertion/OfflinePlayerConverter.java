package com.github.hostadam.ares.command.parameter.convertion;

import com.github.hostadam.ares.command.parameter.ParameterConverter;
import com.github.hostadam.ares.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class OfflinePlayerConverter implements ParameterConverter<OfflinePlayer> {
    @Override
    public OfflinePlayer convert(String arg) {
        return PlayerUtils.getOfflinePlayer(arg);
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid player.");
    }
}
