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

package com.github.hostadam.persistence.data.adapter;

import com.github.hostadam.persistence.data.*;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YamlDataNodeAdapter implements DataNodeAdapter<Object> {

    public static final YamlDataNodeAdapter INSTANCE = new YamlDataNodeAdapter();

    private YamlDataNodeAdapter() {}

    @Override
    public Object write(DataNode node) {
        switch (node) {
            case DataNodeObject object -> {
                Map<String, Object> map = new HashMap<>();
                for (String key : object.keys()) {
                    map.put(key, this.write(object.get(key)));
                }
                return map;
            }
            case DataNodeArray array -> {
                return array.values().stream().map(this::write).collect(Collectors.toList());
            }
            case DataNodeValue value -> {
                return value.raw();
            }
            case null, default -> {
                return null;
            }
        }
    }

    @Override
    public DataNode read(Object value) {
        switch (value) {
            case null -> {
                return DataNode.nullType();
            }
            case ConfigurationSection section -> {
                DataNodeObject object = DataNode.newEmptyObject();
                for (String key : section.getKeys(false)) {
                    Object o = section.get(key);
                    object.put(key, this.read(o));
                }
                return object;
            }
            case List<?> list -> {
                DataNodeArray array = DataNode.newArray();
                list.forEach(element -> array.add(this.read(element)));
                return array;
            }
            default -> {
                return DataNode.value(value);
            }
        }
    }
}
