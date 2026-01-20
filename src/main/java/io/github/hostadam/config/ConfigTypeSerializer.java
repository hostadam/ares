package io.github.hostadam.config;

import java.util.function.BiFunction;
import java.util.function.Function;

public record ConfigTypeSerializer<T>(
        Function<T, Object> serializer,
        BiFunction<ConfigFile, String, T> deserializer
) { }
