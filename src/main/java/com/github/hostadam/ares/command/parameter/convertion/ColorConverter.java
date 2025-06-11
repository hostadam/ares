package com.github.hostadam.ares.command.parameter.convertion;

import com.github.hostadam.ares.command.parameter.ParameterConverter;
import com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;

public class ColorConverter implements ParameterConverter<Color> {
    @Override
    public Color convert(String arg) {
        Color color = null;
        switch(arg) {
            case "red":
                color = Color.RED;
                break;
            case "maroon":
                color = Color.MAROON;
                break;
            case "darkgreen":
                color = Color.OLIVE;
                break;
            case "green":
                color = Color.GREEN;
                break;
            case "purple":
                color = Color.PURPLE;
                break;
            case "navy":
                color = Color.NAVY;
                break;
            case "pink":
                color = Color.FUCHSIA;
                break;
            case "blue":
                color = Color.BLUE;
                break;
            case "orange":
                color = Color.ORANGE;
                break;
            default:
                if(arg.startsWith("#") || arg.startsWith("&#")) {
                    net.md_5.bungee.api.ChatColor chatColor;

                    try {
                        chatColor = net.md_5.bungee.api.ChatColor.of(arg.replace("&", ""));
                    } catch(Exception exception) {
                        return null;
                    }

                    java.awt.Color javaColor = chatColor.getColor();
                    color = Color.fromRGB(javaColor.getRed(), javaColor.getGreen(), javaColor.getBlue());
                }
        }

        return color;
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid color.");
    }
}
