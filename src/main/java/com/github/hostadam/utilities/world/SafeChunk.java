/*
 * MIT License
 * Copyright (c) 2026 Hostadam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.hostadam.utilities.world;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public record SafeChunk(String worldName, int x, int z) {

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
        if(!(other instanceof SafeChunk key)) return false;
        return this.x == key.x
                && this.z == key.z
                && (Objects.equals(worldName, key.worldName));
    }

    public static SafeChunk of(Chunk chunk) {
        return new SafeChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static SafeChunk of(Location location) {
        return new SafeChunk(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static SafeChunk of(SafeLocation location) {
        return new SafeChunk(location.getWorld().getName(), location.blockX() >> 4, location.blockZ() >> 4);
    }
}