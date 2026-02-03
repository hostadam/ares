package io.github.hostadam.persistence.data.codec;

import io.github.hostadam.persistence.data.node.DataNode;
import io.github.hostadam.persistence.data.node.DataNodeArray;
import io.github.hostadam.persistence.data.node.DataNodeNull;
import io.github.hostadam.persistence.data.node.DataNodeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class DataCodecs {

    public static final DataCodec<String> STRING_CODEC = primitive(String.class);
    public static final DataCodec<Integer> INT_CODEC = primitive(Integer.class);
    public static final DataCodec<Long> LONG_CODEC = primitive(Long.class);

    public static final DataCodec<UUID> UUID_CODEC = map(STRING_CODEC, UUID::fromString, UUID::toString);

    private static <T> DataCodec<T> primitive(Class<T> classType) {
        return new DataCodec<>() {
            @Override
            public DataNode encode(T value) {
                return value == null ? DataNode.none() : DataNode.value(value);
            }

            @Override
            public T decode(DataNode node) {
                return node.isNull() ? null : classType.cast(((DataNodeValue) node).raw());
            }
        };
    }

    public static <A, B> DataCodec<A> map(DataCodec<B> base, Function<B, A> decoder, Function<A, B> encoder) {
        return new DataCodec<A>() {
            @Override
            public DataNode encode(A value) {
                return value == null ? DataNode.none() : base.encode(encoder.apply(value));
            }

            @Override
            public A decode(DataNode node) {
                return node.isNull() ? null : decoder.apply(base.decode(node));
            }
        };
    }

    public static <T> DataCodec<List<T>> arrayOf(DataCodec<T> elementCodec) {
        return new DataCodec<>() {
            @Override
            public DataNode encode(List<T> value) {
                if (value == null) return DataNode.none();
                DataNodeArray array = new DataNodeArray();
                for (T element : value) {
                    array.add(elementCodec.encode(element));
                }
                return array;
            }

            @Override
            public List<T> decode(DataNode node) {
                if(node.isNull()) {
                    return List.of();
                }

                List<T> result = new ArrayList<>();
                for(DataNode child : node.asArray().values()) {
                    result.add(elementCodec.decode(child));
                }
                return result;
            }
        };
    }
}
