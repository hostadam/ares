package io.github.hostadam.implementation;

import io.github.hostadam.Ares;
import io.github.hostadam.player.AresEvents;
import io.github.hostadam.config.ConfigFile;
import io.github.hostadam.api.menu.MenuItem;
import io.github.hostadam.implementation.board.BoardHandler;
import io.github.hostadam.command.impl.CommandRegistry;
import io.github.hostadam.config.ConfigParser;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class AresImpl extends JavaPlugin implements Ares {

    private final Map<String, MenuItem> predefinedMenuItems = new HashMap<>();
    private static final Map<UUID, Function<String, Boolean>> PENDING_INPUTS = new ConcurrentHashMap<>();


    @Getter
    private ConfigFile configFile;
    private AsyncBridge asyncBridge;
    private ConfigParser configRegistry;
    private BoardHandler boardHandler;
    private CommandRegistry commandRegistry;

    private BukkitTask task;
    private final Map<UUID, ChatInput> chatInputs = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        if(!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        this.configFile = new ConfigFile(this, "config");
        this.configRegistry = new ConfigParser();
        this.boardHandler = new BoardHandler(this);
        this.commandRegistry = new CommandRegistry();
        this.asyncBridge = new PaperAsyncBridge(this);
        this.loadPredefinedMenuItems();

        this.getServer().getPluginManager().registerEvents(new AresEvents(this), this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getServicesManager().register(Ares.class, this, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        if(this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    private void loadPredefinedMenuItems() {
        ConfigurationSection section = this.configFile.getConfigurationSection("menu-items");
        if(section != null) {
            for(String key : section.getKeys(false)) {
                MenuItem menuItem = new MenuItem(this, section.getConfigurationSection(key));
                this.predefinedMenuItems.put(key.toLowerCase(), menuItem);
            }
        }
    }

    public ChatInput getChatInput(Player player) {
        return this.chatInputs.get(player.getUniqueId());
    }

    public void removeChatInput(Player player) {
        this.chatInputs.remove(player.getUniqueId());
    }

    @Override
    public ConfigParser config() {
        return this.configRegistry;
    }

    @Override
    public AsyncBridge async() {
        return this.asyncBridge;
    }

    @Override
    public BoardHandler scoreboard() {
        return this.boardHandler;
    }

    @Override
    public CommandRegistry commands() {
        return this.commandRegistry;
    }

    @Override
    public MenuItem getPredefinedMenuItem(String name) {
        if(name == null) return null;
        return this.predefinedMenuItems.get(name.toLowerCase());
    }

    @Override
    public Collection<MenuItem> getAllPredefinedMenuItems() {
        return this.predefinedMenuItems.values();
    }

    @Override
    public void startChatInput(Player player, ChatInput input) {
        this.chatInputs.put(player.getUniqueId(), input);
    }
}
