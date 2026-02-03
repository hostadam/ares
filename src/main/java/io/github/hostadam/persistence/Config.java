package io.github.hostadam.persistence;

import java.util.function.Supplier;

public abstract class Config {

    protected final ConfigFile file;
    protected final ConfigParser parser;

    public Config(ConfigFile file, ConfigParser parser) {
        this.file = file;
        this.parser = parser;
    }

    protected <T> T read(Class<T> clazz, String path, Supplier<T> defaultSupplier) {
        return this.parser.read(this.file, clazz, path, defaultSupplier);
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
