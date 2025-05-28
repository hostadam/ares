package com.github.hostadam.ares.command.parameter.convertion;

import com.github.hostadam.ares.command.parameter.ParameterConverter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class EntityTypeConverter implements ParameterConverter<EntityType> {
    @Override
    public EntityType convert(String arg) {
        EntityType type;
        try {
            type = EntityType.valueOf(arg.toUpperCase());
            return type;
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public void error(CommandSender sender, String arg) {
        sender.sendMessage("§c'" + arg + "' is not a valid entity type.");
    }
}
