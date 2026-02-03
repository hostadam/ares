package io.github.hostadam.persistence.data.codec;

import io.github.hostadam.persistence.data.codec.object.MutableObjectCodecBuilder;
import io.github.hostadam.persistence.data.codec.object.ImmutableCodecBuilder;
import io.github.hostadam.persistence.data.node.DataNode;
import io.github.hostadam.persistence.data.node.DataNodeObject;

import java.util.function.Function;
import java.util.function.Supplier;

public interface DataCodec<T> {
    DataNode encode(T value);
    T decode(DataNode node);

    static <T> ImmutableCodecBuilder<T> newBuilder(Function<DataNodeObject, T> constructor) {
        return new ImmutableCodecBuilder<>(constructor);
    }

    static <T> MutableObjectCodecBuilder<T> newBuilder(Supplier<T> supplier) {
        return new MutableObjectCodecBuilder<>(supplier);
    }
}