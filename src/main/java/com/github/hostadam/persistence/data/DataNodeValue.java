/*
 * MIT License
 * Copyright (c) 2026 Hostadam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.hostadam.persistence.data;

import java.util.function.Function;

public final class DataNodeValue implements DataNode {

    private static final Function<Number, Long> LONG_FUNCTION = Number::longValue;
    private static final Function<Number, Integer> INT_FUNCTION = Number::intValue;
    private static final Function<Number, Double> DOUBLE_FUNCTION = Number::doubleValue;
    private static final Function<Number, Float> FLOAT_FUNCTION = Number::floatValue;

    private final Object value;

    DataNodeValue(Object value) {
        this.value = value;
    }

    public Object raw() {
        return this.value;
    }

    public boolean isNumber() {
        return this.value instanceof Number;
    }

    public boolean isString() {
        return this.value instanceof String;
    }

    @Override
    public boolean isValue() {
        return true;
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
    public double asDouble() {
        return value instanceof Number number ? DOUBLE_FUNCTION.apply(number) : DataNode.super.asDouble();
    }

    @Override
    public float asFloat() {
        return value instanceof Number number ? FLOAT_FUNCTION.apply(number) : DataNode.super.asFloat();
    }

    @Override
    public boolean asBoolean() {
        return value instanceof Boolean bool ? bool : DataNode.super.asBoolean();
    }
}
