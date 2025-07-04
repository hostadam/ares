package com.github.hostadam.ares.command.context;

import com.github.hostadam.ares.command.tabcompletion.ParameterTabCompleter;
import com.github.hostadam.ares.utils.PlayerUtils;
import com.github.hostadam.ares.utils.TimeUtils;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CommandContextHelper {

    private final Map<Class<?>, ParameterTabCompleter> tabCompleters;
    private final Map<Class<?>, ParameterArgParser<?>> argParsers;

    public CommandContextHelper() {
        this.tabCompleters = new ConcurrentHashMap<>();
        this.argParsers = new ConcurrentHashMap<>();
        this.registerDefaultParsers();
        this.registerDefaultTabCompleters();
    }

    public ParameterTabCompleter getTabCompletion(Class<?> clazz) {
        if(!this.tabCompleters.containsKey(clazz)) return null;
        return this.tabCompleters.get(clazz);
    }

    private void registerDefaultParsers() {
        this.registerParser(World.class, arg -> Optional.ofNullable(Bukkit.getWorld(arg)));
        this.registerParser(Player.class, arg -> Optional.ofNullable(Bukkit.getPlayer(arg)));
        this.registerParser(OfflinePlayer.class, arg -> Optional.ofNullable(PlayerUtils.getOfflinePlayer(arg)));
        this.registerParser(Material.class, arg -> Optional.ofNullable(Material.getMaterial(arg.toUpperCase())));
        this.registerParser(Enchantment.class, arg -> Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(arg.toUpperCase()))));
        this.registerParser(Material.class, arg -> Optional.ofNullable(Material.getMaterial(arg.toUpperCase())));
        this.registerParser(TextColor.class, arg -> {
            TextColor textColor = NamedTextColor.NAMES.value(arg);
            if(textColor == null) {
                textColor = TextColor.fromHexString(arg);
            }

            return Optional.ofNullable(textColor);
        });

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

    private void registerDefaultTabCompleters() {
        this.registerTabCompleter(Enchantment.class, (sender, input) -> RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).keyStream().map(NamespacedKey::value).filter(string -> input.isEmpty() || string.startsWith(input)).toList());
        this.registerTabCompleter(World.class, (sender, input) -> Bukkit.getWorlds().stream().map(World::getName).filter(string -> input.isEmpty() || string.startsWith(input)).toList());
        this.registerTabCompleter(Player.class, (sender, input) -> Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(string -> input.isEmpty() || string.startsWith(input)).toList());
        this.registerTabCompleter(NamedTextColor.class, (sender, input) -> NamedTextColor.NAMES.keys().stream().filter(string -> input.isEmpty() || string.startsWith(input)).toList());
        this.registerTabCompleter(GameMode.class, (sender, input) -> Arrays.stream(GameMode.values()).map(GameMode::name).map(String::toLowerCase).filter(string -> input.isEmpty() || string.startsWith(input)).toList());
    }

    public <T> void registerParser(Class<T> clazz, ParameterArgParser<T> parser) {
        this.argParsers.put(clazz, parser);
    }

    //TODO: Create these
    public <T> void registerTabCompleter(Class<T> clazz, ParameterTabCompleter tabCompleter) {
        this.tabCompleters.put(clazz, tabCompleter);
    }

    public <T> Optional<T> parse(Class<?> type, String value) {
        if(!this.argParsers.containsKey(type)) return Optional.empty();
        ParameterArgParser<T> parameterArgParser = (ParameterArgParser<T>) this.argParsers.get(type);
        return parameterArgParser.apply(value);
    }
}
