package io.github.hostadam.persistence.data.codec;

import io.github.hostadam.persistence.data.node.DataNode;
import io.github.hostadam.persistence.data.node.DataNodeObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ObjectCodecBuilder<T> {

    // TODO: Constructor values are currently not encoded.
    private final Function<DataNodeObject, T> constructor;
    private final List<FieldCodec<T, ?>> fields = new ArrayList<>();

    public ObjectCodecBuilder(Function<DataNodeObject, T> constructor) {
        this.constructor = constructor;
    }

    public <F> ObjectCodecBuilder<T> field(String key, DataCodec<F> fieldCodec, Function<T, F> getter, BiConsumer<T, F> setter) {
        this.fields.add(new FieldCodec<>(key, fieldCodec, getter, setter));
        return this;
    }

    public DataCodec<T> build() {
        Objects.requireNonNull(this.constructor, "Constructor function must be defined");
        return new DataCodec<>() {
            @Override
            public DataNode encode(T value) {
                if(value == null) {
                    return DataNode.none();
                }

                DataNodeObject object = new DataNodeObject();
                for (FieldCodec<T, ?> field : fields) {
                    field.encode(value, object);
                }
                return object;
            }

            @Override
            public T decode(DataNode node) {
                if(node.isNull()) {
                    return null;
                }

                DataNodeObject object = node.asObject();
                T value = constructor.apply(object);
                for (FieldCodec<T, ?> field : fields) {
                    field.decode(value, object);
                }
                return value;
            }
        };
    }
}
