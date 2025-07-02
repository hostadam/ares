package com.github.hostadam.ares.command.handler;

import com.github.hostadam.ares.command.AresCommand;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            if(!method.isAnnotationPresent(AresCommand.class)
                    || method.getParameterCount() != 1
                    || !CommandContext.class.isAssignableFrom(method.getParameters()[0].getType())) {
                continue;
            }

            AresCommand command = method.getAnnotation(AresCommand.class);
            AresCommandData data = new AresCommandData(command, method, object);
            String name = command.labels()[0], parent = command.parent();

            if(parent.isEmpty()) {
                AresCommandImpl impl = new AresCommandImpl(this, data);
                this.commands.put(name, impl);

                if(this.subCommandQueue.containsKey(name)) {
                    this.subCommandQueue.get(name).forEach(impl::addSubCommand);
                }

                this.commandMap.register(name, impl);
            } else if(!this.commands.containsKey(parent)) {
                List<AresCommandData> subCommands = this.subCommandQueue.computeIfAbsent(parent, string -> new ArrayList<>());
                subCommands.add(data);
                this.subCommandQueue.put(parent, subCommands);
            } else {
                AresCommandImpl parentCommand = this.getCommandByLabel(parent);
                if(parentCommand != null) {
                    parentCommand.addSubCommand(data);
                }
            }
        }
    }

    public AresCommandImpl getCommandByLabel(String name) {
        return this.commands.get(name);
    }
}
