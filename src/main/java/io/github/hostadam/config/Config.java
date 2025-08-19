package io.github.hostadam.config;

import java.lang.reflect.Field;

public abstract class Config {

    private final ConfigRegistry registry;
    protected final ConfigFile file;

    public Config(ConfigRegistry registry, ConfigFile file) {
        this.registry = registry;
        this.file = file;
    }

    public void save() {
        file.save();
    }

    public void load() {
        for(Field field : this.getClass().getDeclaredFields()) {
            if(!field.isAnnotationPresent(ConfigPath.class)) continue;
            ConfigPath path = field.getAnnotation(ConfigPath.class);
            if(!this.file.contains(path.value())) {
                continue;
            }

            Object deserialized = this.registry.deserialize(field.getType(), this.file, path.value());
            if(deserialized == null) continue;
            try {
                field.setAccessible(true);
                field.set(this, deserialized);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        this.postLoad();
    }

    public void reload() {
        this.file.load();
        this.load();
    }

    public abstract void postLoad();
}
