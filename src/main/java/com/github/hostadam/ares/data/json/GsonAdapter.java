package com.github.hostadam.ares.data.json;

import com.google.gson.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.util.function.Function;

@RequiredArgsConstructor
public class GsonAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    @Getter
    @NonNull
    private final Class<T> clazz;
    private Function<JsonElement, T> deserializer;
    private Function<T, JsonElement> serializer;

    public GsonAdapter<T> loader(Function<JsonElement, T> deserializer) {
        this.deserializer = deserializer;
        return this;
    }

    public GsonAdapter<T> saver(Function<T, JsonElement> serializer) {
        this.serializer = serializer;
        return this;
    }

    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if(deserializer == null) return null;
        return deserializer.apply(jsonElement);
    }

    @Override
    public JsonElement serialize(T t, Type type, JsonSerializationContext jsonSerializationContext) {
        if(serializer == null) return null;
        return serializer.apply(t);
    }
}
