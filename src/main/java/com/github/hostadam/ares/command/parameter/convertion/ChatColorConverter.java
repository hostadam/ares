package com.github.hostadam.ares.command.parameter.convertion;

import com.github.hostadam.ares.command.parameter.ParameterConverter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ChatColorConverter implements ParameterConverter<ChatColor> {
    @Override
    public ChatColor convert(String arg) {
        ChatColor color;
        try {
            color = ChatColor.valueOf(arg.toUpperCase());
            return color;
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid color.");
    }
}
