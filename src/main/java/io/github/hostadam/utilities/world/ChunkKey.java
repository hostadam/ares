package io.github.hostadam.utilities.world;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public record ChunkKey(String worldName, int x, int z) {

    public boolean isLoaded() {
        return this.getWorld().isChunkLoaded(this.x, this.z);
    }

    private World getWorld() {
        return Bukkit.getWorld(this.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.worldName, this.x, this.z);
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) return true;
        if(!(other instanceof ChunkKey key)) return false;
        return this.x == key.x
                && this.z == key.z
                && (Objects.equals(worldName, key.worldName));
    }

    public static ChunkKey of(Chunk chunk) {
        return new ChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static ChunkKey of(Location location) {
        return new ChunkKey(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static ChunkKey of(SafeLocation location) {
        return new ChunkKey(location.getWorld().getName(), location.blockX() >> 4, location.blockZ() >> 4);
    }
}