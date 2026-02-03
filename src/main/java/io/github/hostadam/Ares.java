package io.github.hostadam;

import io.github.hostadam.board.BoardHandler;
import io.github.hostadam.persistence.Config;
import io.github.hostadam.persistence.ConfigFile;
import io.github.hostadam.persistence.messages.MessageConfig;
import io.github.hostadam.utilities.item.ItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.function.Function;

public interface Ares {

    MessageConfig createMessageConfig(ConfigFile file);

    <T extends Config> T createConfig(ConfigFile file, Class<T> clazz);
    /*
    Parse a given object based on a string. Internally it uses the command argument type parsing system.
     */
    <T> Optional<T> convertFromString(Class<T> clazz, String value);

    /*
    Register a config type serializer to be able to deserialize data from configs on startup.
     */
    <T> void registerConfigAdapter(Class<T> clazz, ConfigTypeAdapter<T> serializer);

    /*
    Register a command argument type.
     */
    <T> void registerArgumentType(Class<T> clazz, ArgumentType<T> type);

    /*
    Register all commands in a given class.
     */
    void registerCommands(JavaPlugin owningPlugin, Object object);

    /*
    Start a chat input for a given player.
     */
    void startChatInput(Player player, Function<String, Boolean> function);

    /*
    Get the current scoreboard handler.
     */
    BoardHandler scoreboard();

    /*
    Get the current universal item factory.
     */
    ItemFactory itemFactory();

}
