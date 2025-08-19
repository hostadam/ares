package io.github.hostadam.api.persistance;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.hostadam.utilities.PaperUtils;
import net.kyori.adventure.text.Component;

public interface JsonSerializable {

    JsonObject toJson();
    void fromJson(JsonObject object);

    static Component deserializeComponent(JsonObject object) {
        return PaperUtils.jsonToComponent(object.toString());
    }

    static JsonObject serializeComponent(Component component) {
        String json = PaperUtils.componentToJson(component);
        return JsonParser.parseString(json).getAsJsonObject();
    }
}
