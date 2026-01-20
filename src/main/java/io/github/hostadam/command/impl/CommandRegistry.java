package io.github.hostadam.command.impl;

import io.github.hostadam.command.AresCommand;
import io.github.hostadam.command.context.CommandContext;
import io.github.hostadam.command.context.CommandContextRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class CommandRegistry {

    private CommandMap commandMap;
    private final CommandContextRegistry contextHelper;

    private final Map<String, CommandImpl> commands;
    private final Map<String, Set<CommandData>> subCommandQueue;

    public CommandRegistry() {
        this.contextHelper = new CommandContextRegistry();
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

    public CommandContextRegistry context() {
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
                CommandData data = new CommandData(this, command.labels(), command.description(), command.usage(), command.permission(), sendUsagePrioritized, method, object);

                CommandImpl implementation = new CommandImpl(this, data);
                String name = implementation.getName();

                this.commands.put(name, implementation);
                this.commandMap.register(name, pluginName, implementation);

                this.subCommandQueue.computeIfPresent(name, (key, queue) -> {
                    queue.forEach(implementation::addSubCommand);
                    return null;
                });
            } else if(method.isAnnotationPresent(AresCommand.class)) {
                if(method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameters()[0].getType())) continue;

                AresCommand command = method.getAnnotation(AresCommand.class);
                CommandData data = new CommandData(this, command.labels(), command.description(), command.usage(), command.permission(), true, method, object);
                String parent = command.parent();
                if(!this.commands.containsKey(parent)) {
                    this.subCommandQueue.computeIfAbsent(parent, string -> new HashSet<>()).add(data);
                } else {
                    CommandImpl parentCommand = this.getCommandByLabel(parent);
                    if(parentCommand != null) {
                        parentCommand.addSubCommand(data);
                    }
                }
            }
        }
    }

    public CommandImpl getCommandByLabel(String name) {
        return this.commands.get(name);
    }
}
