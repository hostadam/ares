package io.github.hostadam.config;

import io.github.hostadam.api.menu.MenuLayout;
import io.github.hostadam.utilities.PaperUtils;
import io.github.hostadam.utilities.StringUtils;
import io.github.hostadam.utilities.item.ItemParser;
import io.github.hostadam.utilities.world.SafeLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigRegistry {

    private final Map<Class<?>, ConfigDeserializer<?>> deserializers;

    public ConfigRegistry() {
        this.deserializers = new ConcurrentHashMap<>();
        this.loadDefaultDeserializers();
    }

    public <T> void deserializerFor(Class<T> clazz, ConfigDeserializer<T> deserializer) {
        this.deserializers.put(clazz, deserializer);
    }

    public <T> T deserialize(Class<T> clazz, ConfigFile file, String path) {
        ConfigDeserializer<T> deserializer = (ConfigDeserializer<T>) this.deserializers.get(clazz);
        if(deserializer != null) {
            return deserializer.deserialize(file, path);
        }

        if(clazz.isEnum()) {
            String enumName = file.getString(path);
            if(enumName == null) return null;
            try {
                return (T) Enum.valueOf(clazz.asSubclass(Enum.class), enumName.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return null;
    }

    private void loadDefaultDeserializers() {
        this.deserializerFor(String.class, ConfigFile::getString);
        this.deserializerFor(int.class, ConfigFile::getInt);
        this.deserializerFor(Integer.class, ConfigFile::getInt);
        this.deserializerFor(long.class, ConfigFile::getLong);
        this.deserializerFor(Long.class, ConfigFile::getLong);
        this.deserializerFor(boolean.class, ConfigFile::getBoolean);
        this.deserializerFor(Boolean.class, ConfigFile::getBoolean);
        this.deserializerFor(Location.class, ConfigFile::getLocation);
        this.deserializerFor(double.class, ConfigFile::getDouble);
        this.deserializerFor(Double.class, ConfigFile::getDouble);
        this.deserializerFor(float.class, (file, path) -> file.contains(path) ? (float) file.getDouble(path) : null);
        this.deserializerFor(Float.class, (file, path) -> file.contains(path) ? (float) file.getDouble(path) : null);
        this.deserializerFor(Component.class, (file, path) -> PaperUtils.stringToComponent(file.getString(path)));
        this.deserializerFor(Date.class, (file, path) -> StringUtils.fetchDate(file.getString(path)));

        this.deserializerFor(SafeLocation.class, (file, path) -> {
            String value = file.getString(path);
            return value != null ? new SafeLocation(value) : null;
        });

        this.deserializerFor(ItemStack.class, (file, path) -> {
            ConfigurationSection section = file.getConfigurationSection(path);
            if(section == null) return null;

            return ItemParser.parse(section).build();
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
}
