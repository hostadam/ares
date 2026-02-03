package io.github.hostadam.persistence.data;

import io.github.hostadam.persistence.data.node.DataNode;

public interface DataNodeAdapter<F> {
    F write(DataNode node);
    DataNode read(F format);
}
