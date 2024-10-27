package com.github.hostadam.command.parameter.convertion;

import com.github.hostadam.command.parameter.ParameterConverter;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class MaterialConverter implements ParameterConverter<Material> {
    @Override
    public Material convert(String arg) {
        try {
            return Material.getMaterial(arg.toUpperCase());
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid material.");
    }
}
