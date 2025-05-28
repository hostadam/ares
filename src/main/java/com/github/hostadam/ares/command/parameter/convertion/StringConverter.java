package com.github.hostadam.ares.command.parameter.convertion;

import com.github.hostadam.ares.command.parameter.ParameterConverter;
import org.bukkit.command.CommandSender;

public class StringConverter implements ParameterConverter<String> {
    @Override
    public String convert(String arg) {
        return arg;
    }

    @Override
    public void error(CommandSender sender, String arg) { }
}
