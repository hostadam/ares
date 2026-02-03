package io.github.hostadam.persistence.data.codec;

import io.github.hostadam.persistence.data.node.DataNodeObject;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record FieldCodec<T, F>(
        String fieldName, DataCodec<F> fieldCodec,
        Function<T, F> getter,
        BiConsumer<T, F> setter) {

    public void encode(T instance, DataNodeObject object) {
        object.put(this.fieldName, this.fieldCodec.encode(getter.apply(instance)));
    }

    public void decode(T instance, DataNodeObject object) {
        this.setter.accept(instance, this.fieldCodec.decode(object.get(this.fieldName)));
    }
}