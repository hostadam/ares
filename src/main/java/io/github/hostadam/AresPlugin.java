package io.github.hostadam;

import io.github.hostadam.board.BoardHandler;
import io.github.hostadam.persistence.Config;
import io.github.hostadam.persistence.ConfigFile;
import io.github.hostadam.persistence.messages.MessageConfig;
import io.github.hostadam.utilities.InputHandler;
import io.github.hostadam.utilities.item.ItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Function;

public class AresPlugin extends JavaPlugin implements Ares {

    private static Ares API;

    private ItemFactory itemFactory;
    private BoardHandler boardHandler;
    private InputHandler inputHandler;

    private Arguments argumentTypeRegistry;
    private AresCommandRegistry commandRegistry;

    @Override
    public void onEnable() {
        this.itemFactory = new ItemFactory();
        this.configParser = new ConfigParser(this);
        this.boardHandler = new BoardHandler(this);
        this.inputHandler = new InputHandler();

        this.argumentTypeRegistry = new Arguments();
        this.commandRegistry = new AresCommandRegistry(this.argumentTypeRegistry, this.getServer().getCommandMap());

        this.getServer().getPluginManager().registerEvents(new AresEvents(this.boardHandler, this.inputHandler), this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        API = this;
    }

    @Override
    public MessageConfig createMessageConfig(ConfigFile file) {
        return createConfig(file, MessageConfig.class);
    }

    @Override
    public <T extends Config> T createConfig(ConfigFile file, Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor(ConfigFile.class);
            T instance = constructor.newInstance(file);
            instance.load();
            return instance;
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create config.", e);
        }
    }

    @Override
    public <T> Optional<T> convertFromString(Class<T> clazz, String value) {
        return this.argumentTypeRegistry.parse(clazz, value);
    }

    @Override
    public <T> void registerConfigAdapter(Class<T> clazz, ConfigTypeAdapter<T> adapter) {
        this.configParser.register(clazz, adapter);
    }

    @Override
    public <T> void registerArgumentType(Class<T> clazz, ArgumentType<T> type) {
        this.argumentTypeRegistry.register(clazz, type);
    }

    @Override
    public void registerCommands(JavaPlugin owningPlugin, Object object) {
        this.commandRegistry.register(owningPlugin, object);
    }

    @Override
    public void startChatInput(Player player, Function<String, Boolean> function) {
        this.inputHandler.register(player, function);
    }

    @Override
    public BoardHandler scoreboard() {
        return this.boardHandler;
    }

    @Override
    public ItemFactory itemFactory() {
        return this.itemFactory;
    }

    public static Ares api() {
        return API;
    }
}
