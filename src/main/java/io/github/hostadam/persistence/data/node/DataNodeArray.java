package io.github.hostadam.persistence.data.node;

import java.util.ArrayList;
import java.util.List;

public final class DataNodeArray implements DataNode {

    private final List<DataNode> values = new ArrayList<>();

    public void add(DataNode dataNode) {
        this.values.add(dataNode);
    }

    public List<DataNode> values() {
        return this.values;
    }

    @Override
    public DataNodeArray asArray() {
        return this;
    }
}
