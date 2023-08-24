package com.github.hostadam.command.parameter;

import com.github.hostadam.command.ParameterConverter;
import org.bukkit.command.CommandSender;

public class BooleanConverter implements ParameterConverter<Boolean> {
    @Override
    public Boolean convert(String arg) {
        return Boolean.parseBoolean(arg);
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid input.");
    }
}
