package io.github.hostadam.persistence.serialization;

import java.util.Optional;

@FunctionalInterface
public interface ConfigTypeDeserializer<T> {
    Optional<T> deserialize(Object value);

    static <T> ConfigTypeDeserializer<T> none() {
        return _ -> Optional.empty();
    }
}
