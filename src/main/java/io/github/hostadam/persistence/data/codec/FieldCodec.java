package io.github.hostadam.persistence.data.codec;

import io.github.hostadam.persistence.data.node.DataNodeObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record FieldCodec<T, F>(
        String fieldName, DataCodec<F> fieldCodec,
        Function<T, F> getter,
        @Nullable BiConsumer<T, F> setter) {

    public void encode(T instance, DataNodeObject object) {
        object.put(this.fieldName, this.fieldCodec.encode(getter.apply(instance)));
    }

    public void decode(T instance, DataNodeObject object) {
        if(this.setter != null) {
            this.setter.accept(instance, this.fieldCodec.decode(object.get(this.fieldName)));
        }
    }
}