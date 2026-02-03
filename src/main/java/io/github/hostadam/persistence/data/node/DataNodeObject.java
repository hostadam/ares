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

    public Set<String> keys() {
        return this.values.keySet();
    }

    @Override
    public DataNodeObject asObject() {
        return this;
    }
}