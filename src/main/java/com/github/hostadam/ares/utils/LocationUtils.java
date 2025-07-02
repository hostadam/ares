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
        for(int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = ThreadLocalRandom.current().nextInt(minX, maxX);
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ);

            Location highest = world.getHighestBlockAt(x, z).getLocation();
            if(condition.test(highest)) {
                return highest.clone().add(0.0, 1.0, 0.0);
            }
        }

        return null;
    }

    public static Location centralize(Location location) {
        Location centerLocation = location.clone();
        centerLocation.setX((double) centerLocation.getBlockX() + 0.5);
        centerLocation.setZ((double) centerLocation.getBlockZ() + 0.5);
        return centerLocation;
    }
}
