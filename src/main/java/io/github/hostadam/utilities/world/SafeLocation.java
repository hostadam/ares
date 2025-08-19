package io.github.hostadam.utilities.world;

import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;

import java.util.Map;
import java.util.Objects;

public class SafeLocation implements ConfigurationSerializable {

    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    @Getter
    private final ChunkKey chunkKey;

    public SafeLocation(String worldName, double x, double y, double z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.chunkKey = ChunkKey.of(this);
    }

    public SafeLocation(Map<String, Object> map) {
        this((String) map.get("world"), (double) map.get("x"), (double) map.get("y"), (double) map.get("z"));
    }

    public SafeLocation(ConfigurationSection section) {
        this(section.getString("world"), section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
    }

    public SafeLocation(String string) {
        String[] split = string.split(",");
        this.worldName = split[0];
        this.x = Double.parseDouble(split[1]);
        this.y = Double.parseDouble(split[2]);
        this.z = Double.parseDouble(split[3]);
        this.chunkKey = ChunkKey.of(this);
    }

    public int distanceTo(SafeLocation other) {
        if(other == null) return Integer.MAX_VALUE;

        int dx = other.blockX() - this.blockX();
        int dy = other.blockY() - this.blockY();
        int dz = other.blockZ() - this.blockZ();

        double distance = Math.hypot(Math.hypot(dx, dy), dz);
        return (int) Math.ceil(distance);
    }

    public Block blockRelative(BlockFace face) {
        World world = this.getWorld();
        return world.getBlockAt(this.blockX() + face.getModX(), this.blockY() + face.getModY(), this.blockZ() + face.getModZ());
    }

    public Component format() {
        return Component.text(this.blockX() + ", " + this.blockY() + ", " + this.blockZ());
    }

    public Component formatFull() {
        return Component.text(this.worldName + ", " + this.blockX() + ", " + this.blockY() + ", " + this.blockZ());
    }

    public boolean compare(Location location) {
        if(location == null) return false;
        return this.worldName.equals(location.getWorld().getName())
                && this.blockX() == location.getBlockX()
                && this.blockY() == location.getBlockY()
                && this.blockZ() == location.getBlockZ();
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
        return this.chunkKey.isLoaded();
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

    public String saveAsString() {
        return String.join(",", this.worldName, String.valueOf(this.x), String.valueOf(this.y), String.valueOf(this.z));
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
        return Objects.hash(this.worldName, this.blockX(), this.blockY(), this.blockZ());
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) return true;
        if(!(other instanceof SafeLocation safeLocation)) return false;
        return this.blockX() == safeLocation.blockX()
                && this.blockY() == safeLocation.blockY()
                && this.blockZ() == safeLocation.blockZ()
                && (Objects.equals(worldName, safeLocation.worldName));
    }
}
