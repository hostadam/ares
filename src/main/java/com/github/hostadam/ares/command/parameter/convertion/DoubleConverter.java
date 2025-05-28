package com.github.hostadam.ares.command.parameter.convertion;

import com.github.hostadam.ares.command.parameter.ParameterConverter;
import com.google.common.primitives.Doubles;
import org.bukkit.command.CommandSender;

public class DoubleConverter implements ParameterConverter<Double> {

    @Override
    public Double defaultValue() {
        return 0.0d;
    }

    @Override
    public Double convert(String arg) {
        if(Doubles.tryParse(arg) == null) {
            return null;
        }

        return Doubles.tryParse(arg);
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid number.");
    }
}
