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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class DataNodeObject implements DataNode {

    private final Map<String, DataNode> values = new HashMap<>();

    public void put(String key, DataNode dataNode) {
        this.values.put(key, dataNode);
    }

    public DataNode get(String key) {
        return this.values.get(key);
    }

    public String getString(String key) {
        DataNode node = this.get(key);
        return node.asString();
    }

    public double getDouble(String key) {
        DataNode node = this.get(key);
        return node.asDouble();
    }

    public float getFloat(String key) {
        DataNode node = this.get(key);
        return node.asFloat();
    }

    public float getFloat(String key, float defaultValue) {
        DataNode node = this.get(key);
        return node instanceof DataNodeValue value && value.raw() instanceof Number number ? number.floatValue() : defaultValue;
    }

    public Set<String> keys() {
        return this.values.keySet();
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public DataNodeObject asObject() {
        return this;
    }
}