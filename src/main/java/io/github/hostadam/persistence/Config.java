package io.github.hostadam.persistence;

import io.github.hostadam.persistence.data.adapter.YamlDataNodeAdapter;
import io.github.hostadam.persistence.data.codec.DataCodec;
import io.github.hostadam.persistence.data.node.DataNode;

import java.util.function.Supplier;

public abstract class Config {

    protected final ConfigFile file;

    public Config(ConfigFile file) {
        this.file = file;
    }

    protected <T> void write(ConfigFile file, DataCodec<T> codec, String path, T value) {
        DataNode node = codec.encode(value);
        if(node.isNull()) return;
        file.get().set(path, YamlDataNodeAdapter.INSTANCE.write(node));
    }

    protected <T> T read(ConfigFile file, DataCodec<T> codec, String path, Supplier<T> defaultSupplier) {
        Object value = file.get().get(path);
        if(value == null) {
            T defaultValue = defaultSupplier.get();
            write(file, codec, path, defaultValue);
            return defaultValue;
        }

        DataNode node = YamlDataNodeAdapter.INSTANCE.read(value);
        return codec.decode(node);
    }

    public void save() {
        this.file.save();
    }

    public void reload() {
        this.file.load();
        this.load();
    }

    public abstract void load();
}
