package io.github.hostadam.persistence.data.codec.object;

import io.github.hostadam.persistence.data.codec.DataCodec;
import io.github.hostadam.persistence.data.codec.FieldCodec;
import io.github.hostadam.persistence.data.codec.ObjectCodecBuilder;
import io.github.hostadam.persistence.data.node.DataNode;
import io.github.hostadam.persistence.data.node.DataNodeObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ImmutableCodecBuilder<T> extends ObjectCodecBuilder<T> {

    private final Function<DataNodeObject, T> constructor;

    public ImmutableCodecBuilder(Function<DataNodeObject, T> constructor) {
        this.constructor = constructor;
    }

    public <F> ImmutableCodecBuilder<T> add(String key, DataCodec<F> fieldCodec, Function<T, F> getter) {
        this.addField(new FieldCodec<>(key, fieldCodec, getter, null));
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
                for (FieldCodec<T, ?> field : fields()) {
                    field.encode(value, object);
                }
                return object;
            }

            @Override
            public T decode(DataNode node) {
                if(node.isNull() || !node.isObject()) {
                    return null;
                }

                DataNodeObject object = node.asObject();
                return constructor.apply(object);
            }
        };
    }
}
