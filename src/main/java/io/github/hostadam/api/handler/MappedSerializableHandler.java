package io.github.hostadam.api.handler;

import io.github.hostadam.api.persistance.Serializable;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Getter
public abstract class MappedSerializableHandler<P extends JavaPlugin, K, S, V extends Serializable<K, S>> extends Handler<P> {

    private final Map<K, V> cache = new ConcurrentHashMap<>();

    public MappedSerializableHandler(P plugin) {
        super(plugin);
    }

    public V createAndCache(K key) {
        V object = this.createNewObject(key);
        this.cache.put(key, object);
        return object;
    }

    public void register(V value) {
        this.cache.put(value.getKey(), value);
    }

    public void unregister(V value) {
        this.cache.remove(value.getKey());
    }

    public void deleteBlocking(V value) {
        this.delete(value, false);
    }

    public void delete(V value, boolean async) {
        this.unregister(value);
        this.deleteRecord(value, async);
    }

    public void saveBlocking(V value) {
        this.saveRecord(value, false);
    }

    public void saveRecord(V value, boolean async) {
        this.saveRecord(value, async, null);
    }

    public void clearEntryAndSave(V value, boolean async) {
        this.saveRecord(value, async, () -> this.unregister(value));
    }

    public V get(K key) {
        Optional<V> optional = this.getIfCached(key);
        return optional.orElseGet(() -> fetchRecord(key));
    }

    public V get(K key, S value) {
        Optional<V> optional = this.getIfCached(key);
        return optional.orElseGet(() -> load(key, value));
    }

    public V load(K key, S value) {
        V object = this.createNewObject(key);
        if(value != null) object.deserialize(value);
        this.register(object);
        return object;
    }

    public Optional<V> getIfCached(K key) {
        return Optional.ofNullable(this.cache.get(key));
    }

    public Collection<V> getCachedEntries() {
        return Collections.unmodifiableCollection(this.cache.values());
    }

    public void fetchAllRecords() {
        this.fetchAllRecords(null);
    }

    public void queryRemoval(Predicate<V> predicate) {
        this.cache.entrySet().removeIf(entry -> predicate.test(entry.getValue()));
    }

    public abstract V fetchRecord(K key);
    public abstract void fetchAllRecords(Runnable onCompletion);
    public abstract void saveRecord(V value, boolean async, Runnable onCompletion);
    public abstract void deleteRecord(V value, boolean async);
    public abstract V createNewObject(K key);
}
