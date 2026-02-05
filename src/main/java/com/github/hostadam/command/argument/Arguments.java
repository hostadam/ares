/*
 * MIT License
 * Copyright (c) 2026 Hostadam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.hostadam.command.argument;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.github.hostadam.utilities.TimeUtils;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Arguments {

    public static final ArgumentType<String> STRING = Optional::of;
    public static final ArgumentType<Boolean> BOOLEAN = arg -> Optional.of(Boolean.parseBoolean(arg));
    public static final ArgumentType<Integer> INTEGER = arg -> Optional.ofNullable(Ints.tryParse(arg));
    public static final ArgumentType<Double> DOUBLE = arg -> Optional.ofNullable(Doubles.tryParse(arg));
    public static final ArgumentType<Float> FLOAT = arg -> Optional.ofNullable(Floats.tryParse(arg));
    public static final ArgumentType<Duration> DURATION = TimeUtils::parseDuration;
    public static final ArgumentType<LocalDateTime> DATE = TimeUtils::parseDate;
    public static final ArgumentType<World> WORLD = of(string -> Optional.ofNullable(Bukkit.getWorld(string)), (sender, string) -> Bukkit.getWorlds().stream().map(world -> world.getName().toLowerCase()).filter(worldName -> string.isEmpty() || string.startsWith(worldName)).toList());

    public static final ArgumentType<Player> PLAYER = of(string -> Optional.ofNullable(Bukkit.getPlayer(string)), (sender, string) -> Bukkit.getOnlinePlayers().stream().map(player -> player.getName().toLowerCase()).filter(playerName -> string.isEmpty() || string.startsWith(playerName)).toList());
    public static final ArgumentType<OfflinePlayer> OFFLINE_PLAYER = of(string -> Optional.ofNullable(Bukkit.getOfflinePlayerIfCached(string)), (sender, string) -> Bukkit.getOnlinePlayers().stream().map(player -> player.getName().toLowerCase()).filter(playerName -> string.isEmpty() || string.startsWith(playerName)).toList());

    public static final ArgumentType<GameMode> GAME_MODE = ofEnum(GameMode.class);
    public static final ArgumentType<EntityType> ENTITY_TYPE = ofEnum(EntityType.class);
    public static final ArgumentType<Material> MATERIAL = ofEnum(Material.class);
    public static final ArgumentType<Enchantment> ENCHANTMENT = ofKeyed(RegistryKey.ENCHANTMENT);
    public static final ArgumentType<TextColor> COLOR = of(string -> {
        TextColor textColor = NamedTextColor.NAMES.value(string);
        if(textColor == null) {
            textColor = TextColor.fromHexString(string);
        }

        return Optional.ofNullable(textColor);
    }, (sender, string) -> NamedTextColor.NAMES.keys().stream().filter(key -> string.isEmpty() || string.startsWith(key)).toList());
    
    public static final ArgumentType<ShadowColor> SHADOW_COLOR = of(string -> Optional.ofNullable(ShadowColor.fromHexString(string)), (sender, string) -> NamedTextColor.NAMES.keys().stream().filter(key -> string.isEmpty() || string.startsWith(key)).toList());

    public static <T> ArgumentType<T> of(Function<String, Optional<T>> parser, BiFunction<CommandSender, String, List<String>> suggester) {
        return new ArgumentType<T>() {
            @Override
            public Optional<T> parseArg(String arg) {
                return parser.apply(arg);
            }

            @Override
            public List<String> suggests(CommandSender sender, String textAlreadyInput) {
                return suggester.apply(sender, textAlreadyInput.toLowerCase());
            }
        };
    }

    public static <E extends Enum<E>> ArgumentType<E> ofEnum(Class<E> enumClass) {
        return new ArgumentType<>() {
            @Override
            public Optional<E> parseArg(String arg) {
                String upper = arg.toUpperCase();
                E parsed = Enum.valueOf(enumClass, upper);
                return Optional.of(parsed);
            }

            @Override
            public List<String> suggests(CommandSender sender, String textAlreadyInput) {
                String lowered = textAlreadyInput.toLowerCase();
                return Arrays.stream(enumClass.getEnumConstants()).map(e -> e.name().toLowerCase()).filter(string -> textAlreadyInput.isEmpty() || lowered.startsWith(string)).toList();
            }
        };
    }

    public static <T extends Keyed> ArgumentType<T> ofKeyed(RegistryKey<T> key) {
        return new ArgumentType<>() {

            private final Registry<@org.jetbrains.annotations.NotNull T> registry = RegistryAccess.registryAccess().getRegistry(key);

            @Override
            public Optional<T> parseArg(String arg) {
                NamespacedKey namespacedKey = NamespacedKey.fromString(arg.toLowerCase());
                if(namespacedKey == null) return Optional.empty();
                return Optional.ofNullable(registry.get(namespacedKey));
            }

            @Override
            public List<String> suggests(CommandSender sender, String textAlreadyInput) {
                String lowered = textAlreadyInput.toLowerCase();
                return registry.stream().map(t -> t.key().asString()).filter(string -> textAlreadyInput.isEmpty() || lowered.startsWith(string)).toList();
            }
        };
    }

    public void re() {;
        this.registerParser(Enchantment.class, arg -> Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(arg.toLowerCase()))));
        this.registerParser(TextColor.class, arg -> {
            TextColor textColor = NamedTextColor.NAMES.value(arg);
            if(textColor == null) {
                textColor = TextColor.fromHexString(arg);
            }

            return Optional.ofNullable(textColor);
        });

        this.registerParser(ShadowColor.class, arg -> {
            ShadowColor shadowColor = ShadowColor.fromHexString(arg);
            return Optional.ofNullable(shadowColor);
        });
    }
}
