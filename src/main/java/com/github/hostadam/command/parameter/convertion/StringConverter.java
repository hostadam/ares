package com.github.hostadam.command.parameter.convertion;

import com.github.hostadam.command.parameter.ParameterConverter;
import org.bukkit.command.CommandSender;

public class StringConverter implements ParameterConverter<String> {
    @Override
    public String convert(String arg) {
        return arg;
    }

    @Override
    public void error(CommandSender sender, String arg) { }
}
