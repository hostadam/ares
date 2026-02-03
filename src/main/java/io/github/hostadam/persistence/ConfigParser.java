package io.github.hostadam.persistence;

import io.github.hostadam.AresPlugin;
import io.github.hostadam.persistence.serialization.ConfigTypeAdapter;
import io.github.hostadam.persistence.serialization.ConfigTypeDeserializer;
import io.github.hostadam.persistence.serialization.ConfigTypeSerializer;
import io.github.hostadam.utilities.AdventureUtils;
import io.github.hostadam.utilities.TimeUtils;
import io.github.hostadam.utilities.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ConfigParser {

    private final Map<Class<?>, ConfigTypeAdapter<?>> adapters;

    public ConfigParser(AresPlugin plugin) {
        this.adapters = new ConcurrentHashMap<>();

        this.register(String.class, ConfigTypeAdapter.string());
        this.register(Boolean.class, ConfigTypeAdapter.bool());
        this.register(Integer.class, ConfigTypeAdapter.numeric(Number::intValue));
        this.register(Long.class, ConfigTypeAdapter.numeric(Number::longValue));
        this.register(Double.class, ConfigTypeAdapter.numeric(Number::doubleValue));
        this.register(Float.class, ConfigTypeAdapter.numeric(Number::floatValue));
        this.register(UUID.class, new ConfigTypeAdapter<>(UUID::toString, object -> object instanceof String string ? Optional.of(UUID.fromString(string)) : Optional.empty()));
        this.register(ItemBuilder.class, new ConfigTypeAdapter<>(ConfigTypeSerializer.none(), value -> value instanceof ConfigurationSection section ? Optional.ofNullable(plugin.itemFactory().newBuilder(section)) : Optional.empty()));
        this.register(LocalDateTime.class, new ConfigTypeAdapter<>(TimeUtils::formatDate, object -> object instanceof String string ? TimeUtils.parseDate(string) : Optional.empty()));

        this.register(Duration.class, new ConfigTypeAdapter<>(TimeUtils::formatDuration, object -> object instanceof String string ? TimeUtils.parseDuration(string) : Optional.empty()));

        this.register(Component.class, new ConfigTypeAdapter<>(AdventureUtils::serializeToMiniMessage, object -> object instanceof String string ? Optional.ofNullable(AdventureUtils.parseMiniMessage(string)) : Optional.empty()));
        this.register(Location.class, new ConfigTypeAdapter<>(Location::serialize, object -> {
            if(!(object instanceof ConfigurationSection section)) return Optional.empty();
            World world = Bukkit.getWorld(Objects.requireNonNull(section.getString("world")));
            double x = section.getDouble("x", 0.0), y = section.getDouble("y", 0.0), z = section.getDouble("z", 0.0);
            float yaw = (float) section.getDouble("yaw", 0.0), pitch = (float) section.getDouble("pitch", 0.0);
            return Optional.of(new Location(world, x, y, z, yaw, pitch));
        }));

        this.register(Title.class, new ConfigTypeAdapter<>(title -> {
            Map<String, Object> map = new HashMap<>();
            map.put("title", AdventureUtils.serializeToMiniMessage(title.title()));
            map.put("sub-title", AdventureUtils.serializeToMiniMessage(title.subtitle()));

            Title.Times times = title.times();
            if(times != null) {
                map.put("fade-in", times.fadeIn().getSeconds() * Ticks.TICKS_PER_SECOND);
                map.put("duration", times.stay().getSeconds() * Ticks.TICKS_PER_SECOND);
                map.put("fade-out", times.fadeOut().getSeconds() * Ticks.TICKS_PER_SECOND);
            }

            return map;
        }, object -> {
            if(!(object instanceof ConfigurationSection section)) return Optional.empty();
            Component title = AdventureUtils.parseMiniMessage(section.getString("title"));
            Component subTitle = AdventureUtils.parseMiniMessage(section.getString("sub-title"));
            int fadeIn = section.getInt("fade-in", 0), stay = section.getInt("duration", 20), fadeOut = section.getInt("fade-out", 0);
            return Optional.of(Title.title(title, subTitle, fadeIn, stay, fadeOut));
        }));
    }

    public <T> void register(Class<T> clazz, ConfigTypeAdapter<T> adapter) {
        this.adapters.put(clazz, adapter);
    }

    @SuppressWarnings("unchecked")
    private <T> ConfigTypeSerializer<T> getSerializer(Class<T> type) {
        ConfigTypeAdapter<?> rawParser = this.adapters.get(type);
        if(rawParser == null) return null;
        return (ConfigTypeSerializer<T>) rawParser.serializer();
    }

    @SuppressWarnings("unchecked")
    private <T> ConfigTypeDeserializer<T> getDeserializer(Class<T> type) {
        ConfigTypeAdapter<?> rawParser = this.adapters.get(type);
        if(rawParser == null) return null;
        return (ConfigTypeDeserializer<T>) rawParser.deserializer();
    }

    public <T> void write(ConfigFile file, Class<T> clazz, String path, T value) {
        ConfigTypeSerializer<T> serializer = this.getSerializer(clazz);
        Object output = serializer != null ? serializer.serialize(value) : value;
        if(output == null) return;
        file.get().set(path, output);
    }

    public <T> T read(ConfigFile file, Class<T> clazz, String path, Supplier<T> defaultValueSupplier) {
        ConfigTypeDeserializer<T> deserializer = this.getDeserializer(clazz);
        T value = deserializer != null
                ? deserializer.deserialize(file.get().get(path)).orElse(null) : null;

        if(value == null) {
            value = defaultValueSupplier.get();
            write(file, clazz, path, value);
        }

        return value;
    }
}
