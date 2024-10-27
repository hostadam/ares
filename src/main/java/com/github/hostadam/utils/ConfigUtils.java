package com.github.hostadam.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ConfigUtils {

    /**
     * Parse a location from a String
     *
     * @param string the string to parse
     * @return the Location parsed
     */
    public static Location loadLocation(String string) {
        //Fail-safe if the provided string is null or empty.
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

    /**
     * Convert a Location to a String
     *
     * @param location the location to save
     * @return the savable String
     */
    public static String saveLocation(Location location) {
        //Fail-safe if the provided location is null.
        if(location == null) return "";
        return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ() + ":" + location.getYaw() + ":" + location.getPitch();
    }

    /**
     * Parse a Potion Effect from a ConfigurationSection
     *
     * @param section The ConfigurationSection to parse from
     * @return the parsed Potion Effect
     */
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
