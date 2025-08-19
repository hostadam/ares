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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public interface Ares {

    ConfigRegistry config();
    AsyncBridge async();
    BoardHandler scoreboard();
    CommandRegistry commands();
    CanvasRegistry canvas();

    void registerHandler(JavaPlugin owner, Handler<?> handler);
    void startChatInput(Player player, ChatInput input);
    void startSelection(Player player, Selection selection);
    MenuItem getPredefinedMenuItem(String name);
    Collection<MenuItem> getAllPredefinedMenuItems();

    default void refreshPlayerOnTab(Player player) {
        this.scoreboard().refreshPlayerOnTab(player);
    }

    default void updateBoardSettings(BoardSettings settings) {
        this.scoreboard().updateSettings(settings);
    }

    default void registerCommand(JavaPlugin plugin, Object object) {
        this.commands().register(plugin, object);
    }
}
