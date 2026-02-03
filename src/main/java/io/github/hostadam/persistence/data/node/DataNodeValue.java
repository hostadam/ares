package io.github.hostadam.persistence.data.node;

import java.util.function.Function;

public final class DataNodeValue implements DataNode {

    private static final Function<Number, Long> LONG_FUNCTION = Number::longValue;
    private static final Function<Number, Integer> INT_FUNCTION = Number::intValue;

    private final Object value;

    DataNodeValue(Object value) {
        this.value = value;
    }

    public Object raw() {
        return this.value;
    }

    @Override
    public String asString() {
        return value instanceof String string ? string : DataNode.super.asString();
    }

    @Override
    public long asLong() {
        return value instanceof Number number ? LONG_FUNCTION.apply(number) : DataNode.super.asLong();
    }

    @Override
    public int asInt() {
        return value instanceof Number number ? INT_FUNCTION.apply(number) : DataNode.super.asInt();
    }

    @Override
    public boolean asBoolean() {
        return value instanceof Boolean bool ? bool : DataNode.super.asBoolean();
    }
}
