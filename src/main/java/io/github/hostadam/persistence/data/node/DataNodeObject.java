package io.github.hostadam.persistence.data.node;

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