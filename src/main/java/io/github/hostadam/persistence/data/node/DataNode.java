package io.github.hostadam.persistence.data.node;

import java.lang.reflect.Array;
import java.util.*;

public sealed interface DataNode permits DataNodeArray, DataNodeNull, DataNodeObject, DataNodeValue {

    default boolean isNull() {
        return false;
    }

    default boolean isValue() {
        return false;
    }

    default boolean isObject() {
        return false;
    }

    default boolean isArray() {
        return false;
    }

    default String asString() {
        throw new IllegalStateException("Data node cannot be cast to string");
    }

    default long asLong() {
        throw new IllegalStateException("Data node cannot be cast to a long");
    }

    default int asInt() {
        throw new IllegalStateException("Data node cannot be cast to an int");
    }

    default double asDouble() {
        throw new IllegalStateException("Data node cannot be cast to a double");
    }

    default float asFloat() {
        throw new IllegalStateException("Data node cannot be cast to a float");
    }

    default boolean asBoolean() {
        throw new IllegalStateException("Data node cannot be cast to a boolean");
    }

    default DataNodeObject asObject() {
        throw new IllegalStateException("Data node cannot be cast to an object");
    }

    default DataNodeArray asArray() {
        throw new IllegalStateException("Data node cannot be cast to an array");
    }

    static DataNodeValue value(Object object) {
        return new DataNodeValue(object);
    }

    static DataNodeNull none() {
        return DataNodeNull.INSTANCE;
    }

    static DataNodeArray newArray() {
        return new DataNodeArray();
    }

    static DataNodeObject newEmptyObject() {
        return new DataNodeObject();
    }
}
