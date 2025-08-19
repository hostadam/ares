package io.github.hostadam.api.persistance;

import io.github.hostadam.utilities.PaperUtils;
import net.kyori.adventure.text.Component;

public interface Serializable<K, S> extends Identifiable<K> {

    S serialize();
    void deserialize(S data);

    default Component deserializeComponent(String string) {
        return PaperUtils.jsonToComponent(string);
    }

    default String serializeComponent(Component component) {
        return PaperUtils.componentToJson(component);
    }
}
