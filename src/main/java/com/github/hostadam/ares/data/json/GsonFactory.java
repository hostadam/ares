package com.github.hostadam.ares.data.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GsonFactory {

    private Gson gson;
    private final Map<Class<?>, GsonAdapter<?>> adapters = new ConcurrentHashMap<>();

    private Gson buildGson() {
        GsonBuilder builder = new GsonBuilder();
        this.adapters.forEach(builder::registerTypeAdapter);
        return builder.create();
    }

    public <T> List<T> fromJsonList(String json, Class<T> clazz) {
        Type type = TypeToken.getParameterized(List.class, clazz).getType();
        return gson.fromJson(json, type);
    }

    public <T> void registerAdapter(GsonAdapter<T> adapter) {
        this.adapters.put(adapter.getClazz(), adapter);
        this.gson = this.buildGson();
    }

    public <T> GsonAdapter<T> getAdapter(Class<T> clazz) {
        return (GsonAdapter<T>) this.adapters.get(clazz);
    }

    public Gson gson() {
        return this.gson;
    }
}
