package io.github.hostadam;

import io.github.hostadam.api.ChatInput;
import io.github.hostadam.api.Selection;
import io.github.hostadam.api.compatibility.AsyncBridge;
import io.github.hostadam.api.handler.Handler;
import io.github.hostadam.api.menu.MenuItem;
import io.github.hostadam.board.BoardHandler;
import io.github.hostadam.board.BoardSettings;
import io.github.hostadam.canvas.CanvasRegistry;
import io.github.hostadam.command.CommandRegistry;
import io.github.hostadam.config.ConfigRegistry;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public interface Ares {

    ConfigRegistry config();
    AsyncBridge async();
    BoardHandler scoreboard();
    CommandRegistry commands();
    CanvasRegistry canvas();

    List<Handler<?>> unregisterHandlers(Plugin plugin);
    void registerHandler(JavaPlugin owner, Handler<?> handler);
    void startChatInput(Player player, ChatInput input);
    void startSelection(Player player, Selection selection);
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
