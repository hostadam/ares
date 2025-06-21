package com.github.hostadam.ares.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class LocationUtils {

    public static Location generateRandomLocation(World world, int minX, int minZ, int maxX, int maxZ, int maxAttempts, Predicate<Location> condition) {
        Location location = null;

        for(int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = randomizeCoordinate(minX, maxX);
            int z = randomizeCoordinate(minZ, maxZ);

            Location highest = world.getHighestBlockAt(x, z).getLocation();
            if(condition.test(highest)) {
                location = highest.clone().add(0.0, 1.0, 0.0);
                break;
            }
        }

        return location;
    }

    private static int randomizeCoordinate(int min, int max) {
        return ThreadLocalRandom.current().nextInt(max - min) + min;
    }

    public static Location centralize(Location location) {
        Location centerLocation = location.clone();
        centerLocation.setX((double) centerLocation.getBlockX() + 0.5);
        centerLocation.setZ((double) centerLocation.getBlockZ() + 0.5);
        return centerLocation;
    }
}
