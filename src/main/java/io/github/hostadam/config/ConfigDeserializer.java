package io.github.hostadam.config;

@FunctionalInterface
public interface ConfigDeserializer<T> {

    T deserialize(ConfigFile file, String path);
}
