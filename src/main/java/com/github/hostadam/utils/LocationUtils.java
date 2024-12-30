package com.github.hostadam.utils;

import org.bukkit.Location;

public class LocationUtils {

    public static Location centralize(Location location) {
        Location centerLocation = location.clone();
        centerLocation.setX((double) centerLocation.getBlockX() + 0.5);
        centerLocation.setZ((double) centerLocation.getBlockZ() + 0.5);
        return centerLocation;
    }
}
