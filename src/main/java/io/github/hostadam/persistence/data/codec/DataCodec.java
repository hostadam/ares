package io.github.hostadam.persistence.data.codec;

import io.github.hostadam.persistence.data.node.DataNode;
import io.github.hostadam.persistence.data.node.DataNodeObject;

import java.util.function.Function;

public interface DataCodec<T> {
    DataNode encode(T value);
    T decode(DataNode node);

    static <T> ObjectCodecBuilder<T> newObjectBuilder(Function<DataNodeObject, T> constructor) {
        return new ObjectCodecBuilder<>(constructor);
    }
}