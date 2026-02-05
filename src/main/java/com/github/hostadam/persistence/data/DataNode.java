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

    static DataNodeNull nullType() {
        return DataNodeNull.INSTANCE;
    }

    static DataNodeArray newArray() {
        return new DataNodeArray();
    }

    static DataNodeObject newEmptyObject() {
        return new DataNodeObject();
    }
}
