package io.github.hostadam.api.handler;

import io.github.hostadam.api.persistance.Serializable;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

@Getter
public abstract class ReversableSerializableHandler<P extends JavaPlugin, K, S, V extends Serializable<K, S>> extends MappedSerializableHandler<P, K, S, V> {

    private final ConcurrentNavigableMap<String, K> reversedLookup = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Function<V, String> function;

    public ReversableSerializableHandler(P plugin, Function<V, String> function) {
        super(plugin);
        this.function = function;
    }

    public void replaceReversed(String oldName, String newName) {
        K value = this.reversedLookup.remove(oldName);
        if(value != null) this.reversedLookup.put(newName, value);
    }

    public Optional<V> get(String string) {
        if(string == null) return Optional.empty();
        K key = this.reversedLookup.get(string);
        return key == null ? Optional.empty() : this.getIfCached(key);
    }

    public Optional<V> fetch(String string) {
        if(string == null) return Optional.empty();
        K key = this.reversedLookup.get(string);
        if(key != null) {
            Optional<V> optional = this.getIfCached(key);
            return Optional.ofNullable(optional.orElseGet(() -> fetchRecord(key)));
        }

        return Optional.ofNullable(this.fetchRecord(string));
    }

    @Override
    public void register(V value) {
        super.register(value);
        K key = value.getKey();
        String reversed = this.function.apply(value);
        if(reversed != null) this.reversedLookup.put(reversed, key);
    }

    @Override
    public void unregister(V value) {
        super.unregister(value);

        String reversed = this.function.apply(value);
        if(reversed != null) this.reversedLookup.remove(reversed);
    }

    @Override
    public V createAndCache(K key) {
        V object = super.createAndCache(key);
        String reversed = this.function.apply(object);
        if(reversed != null) this.reversedLookup.put(reversed, key);
        return object;
    }

    public abstract V fetchRecord(String string);
}
