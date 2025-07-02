package com.github.hostadam.ares.command.context;

import com.github.hostadam.ares.command.tabcompletion.ParameterTabCompleter;
import com.github.hostadam.ares.utils.PlayerUtils;
import com.github.hostadam.ares.utils.TimeUtils;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CommandContextHelper {

    private final Map<Class<?>, ParameterTabCompleter<?>> tabCompleters;
    private final Map<Class<?>, ParameterArgParser<?>> argParsers;

    public CommandContextHelper() {
        this.tabCompleters = new ConcurrentHashMap<>();
        this.argParsers = new ConcurrentHashMap<>();
        this.registerDefaultParsers();
    }

    public <T> ParameterTabCompleter<T> getTabCompletion(Class<T> clazz) {
        if(!this.tabCompleters.containsKey(clazz)) return null;
        return (ParameterTabCompleter<T>) this.tabCompleters.get(clazz);
    }

    private void registerDefaultParsers() {
        this.registerParser(World.class, arg -> Optional.ofNullable(Bukkit.getWorld(arg)));
        this.registerParser(Player.class, arg -> Optional.ofNullable(Bukkit.getPlayer(arg)));
        this.registerParser(OfflinePlayer.class, arg -> Optional.ofNullable(PlayerUtils.getOfflinePlayer(arg)));
        this.registerParser(Material.class, arg -> Optional.ofNullable(Material.getMaterial(arg.toUpperCase())));

        this.registerParser(String.class, Optional::of);
        this.registerParser(Boolean.class, arg -> {
            Boolean value = Boolean.parseBoolean(arg);
            return Optional.of(value);
        });

        this.registerParser(ChatColor.class, arg -> {
            try {
                return Optional.of(ChatColor.valueOf(arg.toUpperCase()));
            } catch (Exception exception) {
                return Optional.empty();
            }
        });

        this.registerParser(EntityType.class, arg -> {
            try {
                return Optional.of(EntityType.valueOf(arg.toUpperCase()));
            } catch (Exception exception) {
                return Optional.empty();
            }
        });

        this.registerParser(GameMode.class, arg -> {
            try {
                return Optional.of(GameMode.valueOf(arg.toUpperCase()));
            } catch (Exception exception) {
                return Optional.empty();
            }
        });

        this.registerParser(Integer.class, arg -> {
            Integer value = Ints.tryParse(arg);
            return Optional.ofNullable(value);
        });

        this.registerParser(Double.class, arg -> {
            Double value = Doubles.tryParse(arg);
            return Optional.ofNullable(value);
        });

        this.registerParser(Float.class, arg -> {
            Float value = Floats.tryParse(arg);
            return Optional.ofNullable(value);
        });

        this.registerParser(Long.class, arg -> {
            long value = TimeUtils.parseTime(arg);
            return value == -1 ? Optional.empty() : Optional.of(value);
        });
    }

    public <T> void registerParser(Class<T> clazz, ParameterArgParser<T> parser) {
        this.argParsers.put(clazz, parser);
    }

    public <T> void registerTabCompleter(Class<T> clazz, ParameterTabCompleter<T> tabCompleter) {
        this.tabCompleters.put(clazz, tabCompleter);
    }

    public <T> Optional<T> parse(Class<?> type, String value) {
        if(!this.argParsers.containsKey(type)) return Optional.empty();
        ParameterArgParser<T> parameterArgParser = (ParameterArgParser<T>) this.argParsers.get(type);
        return parameterArgParser.apply(value);
    }
}
