package io.github.hostadam;

import io.github.hostadam.api.ChatInput;
import io.github.hostadam.api.Selection;
import io.github.hostadam.api.compatibility.AsyncBridge;
import io.github.hostadam.api.handler.Handler;
import io.github.hostadam.api.menu.MenuItem;
import io.github.hostadam.board.BoardHandler;
import io.github.hostadam.canvas.CanvasRegistry;
import io.github.hostadam.canvas.events.BlockEvents;
import io.github.hostadam.command.CommandRegistry;
import io.github.hostadam.config.ConfigFile;
import io.github.hostadam.config.ConfigRegistry;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class AresImpl extends JavaPlugin implements Ares {

    private final Map<String, MenuItem> predefinedMenuItems = new HashMap<>();

    @Getter
    private ConfigFile configFile;

    private ConfigRegistry configRegistry;
    private AsyncBridge asyncBridge;
    private BoardHandler boardHandler;
    private CommandRegistry commandRegistry;
    private CanvasRegistry canvasRegistry;

    private BukkitTask task;

    private final Map<UUID, Selection> selections = new HashMap<>();
    private final Map<UUID, ChatInput> chatInputs = new ConcurrentHashMap<>();
    private final Map<Plugin, List<Handler<?>>> handlers = new HashMap<>();

    @Override
    public void onEnable() {
        if(!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        this.configFile = new ConfigFile(this, "config");
        this.configRegistry = new ConfigRegistry();
        this.boardHandler = new BoardHandler(this);
        this.commandRegistry = new CommandRegistry();
        this.asyncBridge = new AsyncBridge(this);
        this.canvasRegistry = new CanvasRegistry();
        this.loadPredefinedMenuItems();

        this.getServer().getPluginManager().registerEvents(new AresListeners(this), this);
        this.getServer().getPluginManager().registerEvents(new BlockEvents(this), this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.task = this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for(List<Handler<?>> handlerList : this.handlers.values()) {
                handlerList.forEach(Handler::cleanup);
            }
        }, 18000, 18000);

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

    public boolean isServerReady() {
        return Handler.allHandlersReady(this.handlers.size());
    }

    public List<Handler<?>> getAndRemoveHandlers(Plugin plugin) {
        List<Handler<?>> handlerList = this.handlers.remove(plugin);
        if(handlerList == null || handlerList.isEmpty()) return null;
        return new ArrayList<>(handlerList).reversed();
    }

    public ChatInput getChatInput(Player player) {
        return this.chatInputs.get(player.getUniqueId());
    }

    public void removeChatInput(Player player) {
        this.chatInputs.remove(player.getUniqueId());
    }

    public Selection getSelection(Player player) {
        return this.selections.get(player.getUniqueId());
    }

    public void removeSelection(Player player) {
        Selection selection = this.selections.remove(player.getUniqueId());
        if(selection != null) selection.cancel(player);
    }

    @Override
    public ConfigRegistry config() {
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
    public CanvasRegistry canvas() {
        return this.canvasRegistry;
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
    public void registerHandler(JavaPlugin owner, Handler<?> handler) {
        long now = System.currentTimeMillis();
        getLogger().log(Level.INFO, "Loading " + handler.getClass().getName());
        this.handlers.computeIfAbsent(owner, k -> new ArrayList<>()).add(handler);
        handler.enable();
        getLogger().log(Level.INFO, "Loaded " + handler.getClass().getName() + ", took " + (System.currentTimeMillis() - now) + " ms");
    }

    @Override
    public void startChatInput(Player player, ChatInput input) {
        this.chatInputs.put(player.getUniqueId(), input);
    }

    @Override
    public void startSelection(Player player, Selection selection) {
        this.selections.put(player.getUniqueId(), selection);
    }
}
