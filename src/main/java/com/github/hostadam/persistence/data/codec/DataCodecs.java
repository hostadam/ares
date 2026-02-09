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

package com.github.hostadam.persistence.data.codec;

import com.github.hostadam.persistence.data.DataNode;
import com.github.hostadam.persistence.data.DataNodeArray;
import com.github.hostadam.persistence.data.DataNodeValue;
import com.github.hostadam.utilities.AdventureUtils;
import com.github.hostadam.utilities.TimeUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class DataCodecs {

    public static final DataCodec<String> STRING = primitive(String.class);
    public static final DataCodec<List<String>> STRING_LIST = arrayOf(STRING);

    public static final DataCodec<Integer> INT = primitive(Integer.class);
    public static final DataCodec<Long> LONG = primitive(Long.class);
    public static final DataCodec<Boolean> BOOLEAN = primitive(Boolean.class);
    public static final DataCodec<Double> DOUBLE = primitive(Double.class);
    public static final DataCodec<Float> FLOAT = primitive(Float.class);

    public static final DataCodec<World> MINECRAFT_WORLD = map(STRING, World::getName, Bukkit::getWorld);
    public static final DataCodec<UUID> UUID_V4 = map(STRING, UUID::toString, UUID::fromString);
    public static final DataCodec<Component> COMPONENT = map(STRING, AdventureUtils::convertComponentToJsonString, AdventureUtils::convertJsonStringToComponent);
    public static final DataCodec<LocalDateTime> DATE = map(STRING, TimeUtils::formatDate, string -> TimeUtils.parseDate(string).orElse(null));
    public static final DataCodec<Duration> DURATION = value(
            duration -> DataNode.value(duration.toMillis()),
            dataNode -> {
                if(!(dataNode instanceof DataNodeValue value)) return null;
                return value.isNumber() ? Duration.ofMillis(value.asLong()) : TimeUtils.parseDuration(value.asString()).orElse(null);
            }
    );
    public static final DataCodec<Location> LOCATION =
            DataCodec.newBuilder(object -> {
                String worldName = object.getString("world");
                World world = Bukkit.getWorld(worldName);
                if(world == null) {
                    throw new IllegalStateException("Unknown world: " + worldName);
                }

                double x = object.getDouble("x");
                double y = object.getDouble("y");
                double z = object.getDouble("z");
                float yaw = object.getFloat("yaw", 0.0f);
                float pitch = object.getFloat("pitch", 0.0f);
                return new Location(world, x, y, z, yaw, pitch);
            })
            .add("world", MINECRAFT_WORLD, Location::getWorld)
            .add("x", DOUBLE, Location::getX)
            .add("y", DOUBLE, Location::getY)
            .add("y", DOUBLE, Location::getZ)
            .add("yaw", FLOAT, Location::getYaw)
            .add("pitch", FLOAT, Location::getPitch)
            .build();

    private static <T> DataCodec<T> primitive(Class<T> classType) {
        return new DataCodec<>() {
            @Override
            public DataNode encode(T value) {
                return value == null ? DataNode.nullType() : DataNode.value(value);
            }

            @Override
            public T decode(DataNode node) {
                return node.isNull() ? null : classType.cast(((DataNodeValue) node).raw());
            }
        };
    }

    public static <T> DataCodec<T> value(Function<T, DataNode> encoder, Function<DataNode, T> decoder) {
        return new DataCodec<>() {
            @Override
            public DataNode encode(T value) {
                return value == null ? DataNode.nullType() : encoder.apply(value);
            }

            @Override
            public T decode(DataNode node) {
                return node.isNull() ? null : decoder.apply(node);
            }
        };
    }

    public static <A, B> DataCodec<A> map(DataCodec<B> base, Function<A, B> encoder, Function<B, A> decoder) {
        return new DataCodec<>() {
            @Override
            public DataNode encode(A value) {
                return value == null ? DataNode.nullType() : base.encode(encoder.apply(value));
            }

            @Override
            public A decode(DataNode node) {
                return node.isNull() ? null : decoder.apply(base.decode(node));
            }
        };
    }

    public static <T> DataCodec<List<T>> arrayOf(DataCodec<T> elementCodec) {
        return new DataCodec<>() {
            @Override
            public DataNode encode(List<T> value) {
                if (value == null) return DataNode.nullType();
                DataNodeArray array = new DataNodeArray();
                for (T element : value) {
                    array.add(elementCodec.encode(element));
                }
                return array;
            }

            @Override
            public List<T> decode(DataNode node) {
                if(node.isNull()) {
                    return List.of();
                }

                List<T> result = new ArrayList<>();
                for(DataNode child : node.asArray().values()) {
                    result.add(elementCodec.decode(child));
                }
                return result;
            }
        };
    }
}
