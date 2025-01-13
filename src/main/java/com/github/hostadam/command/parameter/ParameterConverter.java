package com.github.hostadam.command.parameter;

import org.bukkit.command.CommandSender;

public interface ParameterConverter<T> {

    default T defaultValue() {
        return null;
    }

    T convert(String arg);
    void error(CommandSender sender, String arg);
}
