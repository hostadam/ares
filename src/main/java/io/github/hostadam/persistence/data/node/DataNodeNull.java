package io.github.hostadam.persistence.data.node;

public final class DataNodeNull implements DataNode {

    static final DataNodeNull INSTANCE = new DataNodeNull();

    private DataNodeNull() {}

    @Override
    public boolean isNull() {
        return true;
    }
}
