package io.github.hostadam.persistence.data.codec.object;

import io.github.hostadam.persistence.data.codec.DataCodec;
import io.github.hostadam.persistence.data.codec.FieldCodec;
import io.github.hostadam.persistence.data.codec.ObjectCodecBuilder;
import io.github.hostadam.persistence.data.node.DataNode;
import io.github.hostadam.persistence.data.node.DataNodeObject;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MutableObjectCodecBuilder<T> extends ObjectCodecBuilder<T> {

    private final Supplier<T> supplier;

    public MutableObjectCodecBuilder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public <F> MutableObjectCodecBuilder<T> add(String key, DataCodec<F> fieldCodec, Function<T, F> getter, BiConsumer<T, F> setter) {
        this.addField(new FieldCodec<>(key, fieldCodec, getter, setter));
        return this;
    }

    @Override
    public DataCodec<T> build() {
        Objects.requireNonNull(this.supplier, "Supplier must be defined");
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
                T value = supplier.get();
                for (FieldCodec<T, ?> field : fields()) {
                    field.decode(value, object);
                }
                return value;
            }
        };
    }
}
