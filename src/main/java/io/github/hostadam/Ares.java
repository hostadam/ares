package io.github.hostadam;

import io.github.hostadam.api.menu.MenuItem;
import io.github.hostadam.config.ConfigTypeSerializer;
import io.github.hostadam.implementation.board.BoardHandler;
import io.github.hostadam.implementation.board.BoardSettings;
import io.github.hostadam.command.impl.CommandRegistry;
import io.github.hostadam.config.ConfigParser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.function.UnaryOperator;

public interface Ares {

    /*
    Register a config type serializer to be able to deserialize data from configs on startup.
     */
    <T> void registerConfigParser(Class<T> clazz, ConfigTypeSerializer<T> serializer);

    BoardHandler scoreboard();
    CommandRegistry commands();
    void startChatInput(Player player, ChatInput input);

    MenuItem getPredefinedMenuItem(String name);
    Collection<MenuItem> getAllPredefinedMenuItems();

    default void refreshPlayerOnTab(Player player) {
        this.scoreboard().refreshPlayerOnTab(player);
    }

    default void computeBoardSettings(UnaryOperator<BoardSettings> operator) {
        this.scoreboard().compute(operator);
    }

    default void registerCommand(JavaPlugin plugin, Object object) {
        this.commands().register(plugin, object);
    }
}
