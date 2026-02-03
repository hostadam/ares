package io.github.hostadam.persistence.serialization;

@FunctionalInterface
public interface ConfigTypeSerializer<T> {
    Object serialize(T value);

    static <T> ConfigTypeSerializer<T> none() {
        return _ -> null;
    }

    static <T> ConfigTypeSerializer<T> identical() {
        return value -> value;
    }
}
