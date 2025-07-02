package com.github.hostadam.ares.command.handler;

import com.github.hostadam.ares.command.AresCommand;
import com.github.hostadam.ares.command.AresSubCommand;
import com.github.hostadam.ares.command.context.CommandContext;
import com.github.hostadam.ares.command.context.CommandContextHelper;
import com.github.hostadam.ares.command.tabcompletion.ParameterTabCompleter;
import com.github.hostadam.ares.command.data.AresCommandData;
import com.github.hostadam.ares.command.data.AresCommandImpl;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class CommandHandler {

    private CommandMap commandMap;
    private final CommandContextHelper contextHelper;

    private final Map<String, AresCommandImpl> commands;
    private final Map<String, List<AresCommandData>> subCommandQueue;

    public CommandHandler() {
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

    public void register(Object object) {
        if(this.commandMap == null) return;
        for(Method method : object.getClass().getDeclaredMethods()) {
            // Ignore if no command ctx
            if(method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameters()[0].getType())) continue;

            if(method.isAnnotationPresent(AresCommand.class)) {
                AresCommand command = method.getAnnotation(AresCommand.class);
                AresCommandData data = new AresCommandData(command.labels(), command.description(), command.usage(), command.permission(), method, object);

                AresCommandImpl implementation = new AresCommandImpl(this, data);
                String name = implementation.getName();

                this.commands.put(name, implementation);
                this.commandMap.register(name, implementation);

                this.subCommandQueue.computeIfPresent(name, (key, queue) -> {
                    queue.forEach(implementation::addSubCommand);
                    return null;
                });
            } else if(method.isAnnotationPresent(AresSubCommand.class)) {
                AresSubCommand command = method.getAnnotation(AresSubCommand.class);
                AresCommandData data = new AresCommandData(command.labels(), command.description(), command.usage(), command.permission(), method, object);
                String parent = command.parent();
                if(!this.commands.containsKey(parent)) {
                    this.subCommandQueue.computeIfAbsent(parent, string -> new ArrayList<>()).add(data);
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
