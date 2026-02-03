package io.github.hostadam.persistence.serialization;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public record ConfigTypeAdapter<T>(@NotNull ConfigTypeSerializer<T> serializer, @NotNull ConfigTypeDeserializer<T> deserializer) {

    public static ConfigTypeAdapter<String> string() {
        return new ConfigTypeAdapter<>(ConfigTypeSerializer.identical(), object -> Optional.of((String) object));
    }

    public static <T> ConfigTypeAdapter<T> numeric(Function<Number, T> function) {
        return new ConfigTypeAdapter<>(ConfigTypeSerializer.identical(), value -> value instanceof Number number ? Optional.of(function.apply(number)) : Optional.empty());
    }

    public static ConfigTypeAdapter<Boolean> bool() {
        return new ConfigTypeAdapter<>(ConfigTypeSerializer.identical(), value -> value instanceof Boolean bool ? Optional.of(bool) : Optional.empty());
    }
}
