package com.github.hostadam.command.parameter.convertion;

import com.github.hostadam.command.parameter.ParameterConverter;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;

public class GameModeConverter implements ParameterConverter<GameMode> {
    @Override
    public GameMode convert(String arg) {
        switch(arg.toLowerCase()) {
            case "c":
            case "creative":
                return GameMode.CREATIVE;
            case "s":
            case "survival":
                return GameMode.SURVIVAL;
            case "a":
            case "adventure":
                return GameMode.ADVENTURE;
            case "spec":
            case "spectator":
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid gamemode.");
    }
}
