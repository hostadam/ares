package com.github.hostadam.persistence.config;

import com.github.hostadam.persistence.data.DataNode;
import com.github.hostadam.persistence.data.codec.DataCodec;
import com.github.hostadam.persistence.data.adapter.YamlDataNodeAdapter;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.util.function.Supplier;

public abstract class Config {

    protected final ConfigFile file;

    public Config(ConfigFile file) {
        this.file = file;
    }

    public void saveFile() {
        this.file.save();
    }

    /* Reload the backing config file and the Config object */
    public void reload() {
        try {
            this.file.load();
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Failed to reload config: " + getClass().getName(), e);
        }

        this.load();
    }

    /**
     * Serialize and write a value to the config
     * @param codec - the codec for the value
     * @param path - the config path
     * @param value - the value
     */
    protected <T> void write(DataCodec<T> codec, String path, T value) {
        Preconditions.checkNotNull(value, "Value may not be null");

        DataNode dataNode = codec.encode(value);
        if(!dataNode.isNull()) {
            this.file.get().set(path, YamlDataNodeAdapter.INSTANCE.write(dataNode));
        }
    }

    /**
     * Deserialize and read a value from the config
     * @param codec - the codec for the value
     * @param path - the config path
     * @param defaultSupplier - a fallback supplier if the value is absent
     */
    protected <T> T read(ConfigFile file, DataCodec<T> codec, String path, Supplier<T> defaultSupplier) {
        Object value = file.get().get(path);
        if(value != null) {
            return codec.decode(YamlDataNodeAdapter.INSTANCE.read(value));
        }

        T defaultValue = defaultSupplier.get();
        write(codec, path, defaultValue);
        return defaultValue;
    }

    /**
     * Called externally by the initiator of the Config.
     * This method should include the reading of desired configurations.
     */
    public abstract void load();
}
