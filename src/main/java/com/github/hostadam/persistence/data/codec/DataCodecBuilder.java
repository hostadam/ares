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

package com.github.hostadam.persistence.data.codec;

import com.github.hostadam.persistence.data.DataNode;
import com.github.hostadam.persistence.data.DataNodeObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DataCodecBuilder<T> {

    private final Function<DataNodeObject, T> constructor;
    private final List<FieldCodec<T, ?>> fields;

    public DataCodecBuilder(Function<DataNodeObject, T> constructor) {
        this.constructor = constructor;
        this.fields = new ArrayList<>();
    }

    public <F> DataCodecBuilder<T> add(String key, DataCodec<F> fieldCodec, Function<T, F> getter) {
        this.add(key, fieldCodec, getter, null);
        return this;
    }

    public <F> DataCodecBuilder<T> add(String key, DataCodec<F> fieldCodec, Function<T, F> getter, BiConsumer<T, F> setter) {
        this.fields.add(new FieldCodec<>(key, fieldCodec, getter, setter));
        return this;
    }

    public DataCodec<T> build() {
        Objects.requireNonNull(this.constructor, "Constructor must be defined");
        return new DataCodec<>() {
            @Override
            public DataNode encode(T value) {
                if(value == null) {
                    return DataNode.nullType();
                }

                DataNodeObject object = new DataNodeObject();
                for(FieldCodec<T, ?> field : fields) {
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
                T value = constructor.apply(object);
                for (FieldCodec<T, ?> field : fields) {
                    field.decode(value, object);
                }
                return value;
            }
        };
    }

    private record FieldCodec<T, F>(
            String fieldName, DataCodec<F> fieldCodec,
            Function<T, F> getter,
            @Nullable BiConsumer<T, F> setter) {

        public void encode(T instance, DataNodeObject object) {
            object.put(this.fieldName, this.fieldCodec.encode(getter.apply(instance)));
        }

        public void decode(T instance, DataNodeObject object) {
            if(this.setter != null) {
                this.setter.accept(instance, this.fieldCodec.decode(object.get(this.fieldName)));
            }
        }
    }
}
