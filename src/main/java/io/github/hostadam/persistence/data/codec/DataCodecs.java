package io.github.hostadam.persistence.data.codec;

import io.github.hostadam.persistence.data.node.*;
import io.github.hostadam.utilities.AdventureUtils;
import io.github.hostadam.utilities.TimeUtils;
import io.github.hostadam.utilities.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class DataCodecs {

    public static final DataCodec<String> STRING_CODEC = primitive(String.class);
    public static final DataCodec<Integer> INT_CODEC = primitive(Integer.class);
    public static final DataCodec<Long> LONG_CODEC = primitive(Long.class);
    public static final DataCodec<Boolean> BOOLEAN_CODEC = primitive(Boolean.class);
    public static final DataCodec<Double> DOUBLE_CODEC = primitive(Double.class);
    public static final DataCodec<Float> FLOAT_CODEC = primitive(Float.class);
    public static final DataCodec<World> WORLD_CODEC = map(STRING_CODEC, World::getName, Bukkit::getWorld);
    public static final DataCodec<UUID> UUID_CODEC = map(STRING_CODEC, UUID::toString, UUID::fromString);
    public static final DataCodec<Component> COMPONENT_CODEC = map(STRING_CODEC, AdventureUtils::convertComponentToJsonString, AdventureUtils::convertJsonStringToComponent);
    public static final DataCodec<LocalDateTime> DATE_CODEC = map(STRING_CODEC, TimeUtils::formatDate, string -> TimeUtils.parseDate(string).orElse(null));
    public static final DataCodec<Duration> DURATION_CODEC = value(
            duration -> DataNode.value(duration.toMillis()),
            dataNode -> {
                if(!(dataNode instanceof DataNodeValue value)) return null;
                return value.isNumber() ? Duration.ofMillis(value.asLong()) : TimeUtils.parseDuration(value.asString()).orElse(null);
            }
    );

    public static final DataCodec<Location> LOCATION_CODEC =
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
            .add("world", WORLD_CODEC, Location::getWorld)
            .add("x", DOUBLE_CODEC, Location::getX)
            .add("y", DOUBLE_CODEC, Location::getY)
            .add("y", DOUBLE_CODEC, Location::getZ)
            .add("yaw", FLOAT_CODEC, Location::getYaw)
            .add("pitch", FLOAT_CODEC, Location::getPitch)
            .build();

    private static <T> DataCodec<T> primitive(Class<T> classType) {
        return new DataCodec<>() {
            @Override
            public DataNode encode(T value) {
                return value == null ? DataNode.none() : DataNode.value(value);
            }

            @Override
            public T decode(DataNode node) {
                return node.isNull() ? null : classType.cast(((DataNodeValue) node).raw());
            }
        };
    }

    public static <A> DataCodec<A> value(Function<A, DataNode> encoder, Function<DataNode, A> decoder) {
        return new DataCodec<A>() {
            @Override
            public DataNode encode(A value) {
                return value == null ? DataNode.none() : encoder.apply(value);
            }

            @Override
            public A decode(DataNode node) {
                return node.isNull() ? null : decoder.apply(node);
            }
        };
    }

    public static <A, B> DataCodec<A> map(DataCodec<B> base, Function<A, B> encoder, Function<B, A> decoder) {
        return new DataCodec<A>() {
            @Override
            public DataNode encode(A value) {
                return value == null ? DataNode.none() : base.encode(encoder.apply(value));
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
                if (value == null) return DataNode.none();
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
