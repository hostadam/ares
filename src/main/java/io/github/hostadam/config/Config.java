package io.github.hostadam.config;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Config {

    private final ConfigParser parser;
    protected final ConfigFile file;

    public Config(ConfigParser parser, ConfigFile file) {
        this.parser = parser;
        this.file = file;
    }

    private <T> void handleNoValue(String path, Class<T> clazz, Supplier<T> getter) {
        T defaultValue = getter.get();
        this.parser.addToConfig(this.file, path, clazz, defaultValue);
    }

    protected <T> void registerValue(String path, Class<T> clazz, Supplier<T> getter, Consumer<T> setter) {
        if(!this.file.contains(path)) {
            this.handleNoValue(path, clazz, getter);
            return;
        }

        Optional<T> value = this.parser.tryReadFromConfig(this.file, path, clazz);
        if(value.isEmpty()) {
            this.handleNoValue(path, clazz, getter);
            return;
        }

        setter.accept(value.get());
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
