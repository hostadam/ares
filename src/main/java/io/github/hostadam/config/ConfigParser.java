package io.github.hostadam.config;

import io.github.hostadam.api.menu.MenuLayout;
import io.github.hostadam.utilities.PaperUtils;
import io.github.hostadam.utilities.StringUtils;
import io.github.hostadam.utilities.item.ItemBuilder;
import io.github.hostadam.utilities.item.ItemParser;
import io.github.hostadam.utilities.world.SafeLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigParser {

    private final Map<Class<?>, ConfigTypeSerializer<?>> parsers;

    public ConfigParser() {
        this.parsers = new ConcurrentHashMap<>();

        this.registerParser(String.class, new ConfigTypeSerializer<>(string -> string, MemorySection::getString));
        this.registerParser(int.class, (file, path) -> Optional.of(file.getInt(path)));

        this.registerParser(Integer.class, ConfigFile::getInt);
        this.registerParser(long.class, ConfigFile::getLong);
        this.registerParser(Long.class, ConfigFile::getLong);
        this.registerParser(boolean.class, ConfigFile::getBoolean);
        this.deserializerFor(Boolean.class, ConfigFile::getBoolean);
        this.deserializerFor(double.class, ConfigFile::getDouble);
        this.deserializerFor(Double.class, ConfigFile::getDouble);
        this.deserializerFor(float.class, (file, path) -> file.contains(path) ? (float) file.getDouble(path) : null);
        this.deserializerFor(Float.class, (file, path) -> file.contains(path) ? (float) file.getDouble(path) : null);
        this.deserializerFor(Component.class, (file, path) -> PaperUtils.stringToComponent(file.getString(path)));
        this.deserializerFor(Date.class, (file, path) -> StringUtils.fetchDate(file.getString(path)));
        this.deserializerFor(Location.class, (file, path) -> {
            ConfigurationSection section = file.getConfigurationSection(path);
            if(section == null) return null;

            World world = Bukkit.getWorld(Objects.requireNonNull(section.getString("world")));
            double x = section.getDouble("x", 0.0), y = section.getDouble("y", 0.0), z = section.getDouble("z", 0.0);
            float yaw = (float) section.getDouble("yaw", 0.0), pitch = (float) section.getDouble("pitch", 0.0);
            return new Location(world, x, y, z, yaw, pitch);
        });

        this.deserializerFor(Title.class, (file, path) -> {
            ConfigurationSection section = file.getConfigurationSection(path);
            if(section == null) return null;

            Component title = PaperUtils.stringToComponent(section.getString("title"));
            Component subTitle = PaperUtils.stringToComponent(section.getString("sub-title"));
            int fadeIn = section.getInt("fade-in", 0), stay = section.getInt("duration", 20), fadeOut = section.getInt("fade-out", 0);
            return Title.title(title, subTitle, fadeIn, stay, fadeOut);
        });

        this.deserializerFor(SafeLocation.class, (file, path) -> {
            String value = file.getString(path);
            return value != null ? new SafeLocation(value) : null;
        });

        this.deserializerFor(ItemStack.class, (file, path) -> {
            ConfigurationSection section = file.getConfigurationSection(path);
            if(section == null) return null;

            return ItemParser.parse(section).build();
        });

        this.deserializerFor(ItemBuilder.class, (file, path) -> {
            ConfigurationSection section = file.getConfigurationSection(path);
            if(section == null) return null;

            return ItemParser.parse(section);
        });

        this.deserializerFor(MenuLayout.class, (file, path) -> {
            ConfigurationSection section = file.getConfigurationSection(path);
            if(section == null || section.getKeys(false).isEmpty()) return null;
            return new MenuLayout(section);
        });

        this.deserializerFor(UUID.class, (file, path) -> {
            String value = file.getString(path);
            return value != null ? UUID.fromString(value) : null;
        });
    }

    public <T> void registerParser(Class<T> clazz, ConfigTypeSerializer<T> deserializer) {
        this.parsers.put(clazz, deserializer);
    }

    private <T> ConfigTypeSerializer<T> get(Class<T> parserClass) {
        ConfigTypeSerializer<?> rawParser = this.parsers.get(parserClass);
        return rawParser != null ? (ConfigTypeSerializer<T>) rawParser : null;
    }

    public <T> void addToConfig(ConfigFile file, String path, Class<T> clazz, T value) {
        ConfigTypeSerializer<T> parser = this.get(clazz);
        if(parser == null || parser.serializer() == null) return;
        file.set(path, parser.serializer().apply(value));
        file.save();
    }

    public <T> Optional<T> tryReadFromConfig(ConfigFile file, String path, Class<T> clazz) {
        ConfigTypeSerializer<T> parser = this.get(clazz);
        if(parser != null && parser.deserializer() != null) {
            return Optional.ofNullable(parser.deserializer().apply(file, path));
        }

        if(clazz.isEnum()) {
            String enumName = file.getString(path);
            if(enumName != null) {
                try {
                    return Optional.of(Enum.valueOf(clazz.asSubclass(Enum.class), enumName.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }
}
