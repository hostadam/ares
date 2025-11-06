package io.github.hostadam.command;

import io.github.hostadam.AresImpl;
import io.github.hostadam.api.command.AresCommand;
import io.github.hostadam.api.command.AresSubCommand;
import io.github.hostadam.command.context.CommandContext;
import io.github.hostadam.command.context.CommandContextHelper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


public class CommandRegistry {

    private @Getter @Setter boolean debugMode = false;

    private CommandMap commandMap;
    private final CommandContextHelper contextHelper;

    private final Map<String, AresCommandImpl> commands;
    private final Map<String, Set<AresCommandData>> subCommandQueue;

    public CommandRegistry() {
        this.contextHelper = new CommandContextHelper();
        this.commands = new ConcurrentHashMap<>();
        this.subCommandQueue = new HashMap<>();

        try {
            final Server server = Bukkit.getServer();
            Field field = server.getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            this.commandMap = (CommandMap) field.get(server);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            this.commandMap = null;
            exception.printStackTrace();
        }
    }

    public CommandContextHelper context() {
        return this.contextHelper;
    }

    public void register(JavaPlugin plugin, Object object) {
        if(this.commandMap == null) return;

        String pluginName = plugin.namespace();

        for(Method method : object.getClass().getDeclaredMethods()) {
            // Ignore if no command ctx
            if(method.isAnnotationPresent(AresCommand.class)) {
                boolean sendUsagePrioritized = method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameters()[0].getType());

                AresCommand command = method.getAnnotation(AresCommand.class);
                AresCommandData data = new AresCommandData(this, command.labels(), command.description(), command.usage(), command.permission(), sendUsagePrioritized, method, object);

                AresCommandImpl implementation = new AresCommandImpl(this, data);
                String name = implementation.getName();

                this.commands.put(name, implementation);
                this.commandMap.register(name, pluginName, implementation);

                this.subCommandQueue.computeIfPresent(name, (key, queue) -> {
                    queue.forEach(implementation::addSubCommand);
                    return null;
                });
            } else if(method.isAnnotationPresent(AresSubCommand.class)) {
                if(method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameters()[0].getType())) continue;

                AresSubCommand command = method.getAnnotation(AresSubCommand.class);
                AresCommandData data = new AresCommandData(this, command.labels(), command.description(), command.usage(), command.permission(), true, method, object);
                String parent = command.parent();
                if(!this.commands.containsKey(parent)) {
                    this.subCommandQueue.computeIfAbsent(parent, string -> new HashSet<>()).add(data);
                } else {
                    AresCommandImpl parentCommand = this.getCommandByLabel(parent);
                    if(parentCommand != null) {
                        parentCommand.addSubCommand(data);
                    }
                }
            }
        }
    }

    public AresCommandImpl getCommandByLabel(String name) {
        return this.commands.get(name);
    }
}
