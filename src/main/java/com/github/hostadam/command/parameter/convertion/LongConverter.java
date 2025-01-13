package com.github.hostadam.command.parameter.convertion;

import com.github.hostadam.command.parameter.ParameterConverter;
import com.github.hostadam.utils.TimeUtils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;

public class LongConverter implements ParameterConverter<Long> {

    @Override
    public Long defaultValue() {
        return -1L;
    }

    @Override
    public Long convert(String arg) {
        long parsed = TimeUtils.parseTime(arg);
        if(parsed == -1) {
            return null;
        }

        return parsed;
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid time.");
    }
}
