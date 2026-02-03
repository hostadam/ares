package io.github.hostadam.persistence.data.adapter;

import io.github.hostadam.persistence.data.DataNodeAdapter;
import io.github.hostadam.persistence.data.node.DataNode;
import io.github.hostadam.persistence.data.node.DataNodeArray;
import io.github.hostadam.persistence.data.node.DataNodeObject;
import io.github.hostadam.persistence.data.node.DataNodeValue;
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
                return DataNode.none();
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
