package com.github.hostadam.ares.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ConfigUtils {

    public static Location loadLocation(String string) {
        if(string == null || string.isEmpty()) return null;
        String[] split = string.split(":");
        return new Location(
                Bukkit.getWorld(split[0]),
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2]),
                Double.parseDouble(split[3]),
                Float.parseFloat(split[4]),
                Float.parseFloat(split[5])
        );
    }

    public static String saveLocation(Location location) {
        if(location == null) return "";
        return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ() + ":" + location.getYaw() + ":" + location.getPitch();
    }

    public static PotionEffect loadPotionEffect(ConfigurationSection section) {
        if(section == null) return null;
        PotionEffectType type = PotionEffectType.getByName(section.getName());
        if(type == null) return null;

        int duration = section.getInt("duration");
        if(duration == -1) duration = Integer.MAX_VALUE;

        int amplifier = section.getInt("amplifier");
        return new PotionEffect(type, duration, amplifier);
    }
}
