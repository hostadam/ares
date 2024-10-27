package com.github.hostadam.command.parameter.convertion;

import com.github.hostadam.command.parameter.ParameterConverter;
import org.bukkit.command.CommandSender;

public class IntConverter implements ParameterConverter<Integer> {
    @Override
    public Integer convert(String arg) {
        return Integer.parseInt(arg);
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid number.");
    }
}
