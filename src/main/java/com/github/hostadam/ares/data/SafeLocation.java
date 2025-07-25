package com.github.hostadam.ares.data;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;

import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public class SafeLocation implements ConfigurationSerializable {

    private final String worldName;
    private final double x;
    private final double y;
    private final double z;

    public SafeLocation(ConfigurationSection section) {
        this.worldName = section.getString("world");
        this.x = section.getDouble("x");
        this.y = section.getDouble("y");
        this.z = section.getDouble("z");
    }

    public int blockX() {
        return NumberConversions.floor(this.x);
    }

    public int blockY() {
        return NumberConversions.floor(this.y);
    }

    public int blockZ() {
        return NumberConversions.floor(this.z);
    }

    public boolean isSafe() {
        World world = this.getWorld();
        return world.isChunkLoaded((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4);
    }

    public SafeLocation centralize() {
        double x = this.blockX() + 0.5;
        double z = this.blockZ() + 0.5;
        return new SafeLocation(this.worldName, x, this.y, z);
    }

    public Location toBukkitLocation() {
        return new Location(this.getWorld(), this.x, this.y, this.z);
    }

    public World getWorld() {
        return Bukkit.getWorld(this.worldName);
    }

    public static SafeLocation of(Location location) {
        return new SafeLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    public static SafeLocation of(Block block) {
        return of(block.getLocation());
    }

    @Override
    public @NonNull Map<String, Object> serialize() {
        return Map.of(
                "world", this.worldName,
                "x", this.x,
                "y", this.y,
                "z", this.z
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.worldName, x, y, z);
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) return true;
        if(!(other instanceof SafeLocation safeLocation)) return false;
        return Double.compare(this.x, safeLocation.x) == 0
                && Double.compare(this.y, safeLocation.y) == 0
                && Double.compare(this.z, safeLocation.z) == 0
                && (Objects.equals(worldName, safeLocation.worldName));
    }

}
