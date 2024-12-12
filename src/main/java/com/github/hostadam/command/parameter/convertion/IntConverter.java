package com.github.hostadam.command.parameter.convertion;

import com.github.hostadam.command.parameter.ParameterConverter;
import com.google.common.primitives.Ints;
import org.bukkit.command.CommandSender;

public class IntConverter implements ParameterConverter<Integer> {
    @Override
    public Integer convert(String arg) {
        if(Ints.tryParse(arg) == null) {
            return null;
        }

        int i = Integer.parseInt(arg);
        if(i < 0) {
            return null;
        }

        return i;
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid number.");
    }
}
