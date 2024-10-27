package com.github.hostadam.command.handler;

import com.github.hostadam.command.AresCommand;
import com.github.hostadam.command.parameter.ParameterConverter;
import com.github.hostadam.command.impl.AresCommandData;
import com.github.hostadam.command.impl.AresCommandImpl;
import com.github.hostadam.command.parameter.convertion.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class CommandHandler {

    private CommandMap map;

    private final Map<Class<?>, ParameterConverter<?>> parameters;
    private final Map<String, AresCommandImpl> commands;
    private final Map<String, List<AresCommandData>> subCommandQueue;

    public CommandHandler() {
        this.parameters = new HashMap<>();
        this.commands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.subCommandQueue = new HashMap<>();

        try {
            final Server server = Bukkit.getServer();
            Field field = server.getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            this.map = (CommandMap) field.get(server);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        this.addConverter(boolean.class, new BooleanConverter());
        this.addConverter(int.class, new IntConverter());
        this.addConverter(Material.class, new MaterialConverter());
        this.addConverter(OfflinePlayer.class, new OfflinePlayerConverter());
        this.addConverter(Player.class, new OnlinePlayerConverter());
    }

    public void register(Object object) {
        if(this.map == null) return;
        for(Method method : object.getClass().getMethods()) {
            if(!method.isAnnotationPresent(AresCommand.class)
                    || method.getParameterCount() < 1
                    || !method.getParameters()[0].getType().isAssignableFrom(CommandSender.class)) {
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

                this.map.register(name, impl);
            } else if(!this.commands.containsKey(parent)) {
                List<AresCommandData> subCommands = this.subCommandQueue.getOrDefault(parent, new ArrayList<>());
                subCommands.add(data);
            } else {
                AresCommandImpl parentCommand = this.getCommandByLabel(parent);
                if(parentCommand != null) parentCommand.addSubCommand(data);
            }
        }
    }

    public AresCommandImpl getCommandByLabel(String name) {
        return this.commands.get(name);
    }

    public void addConverter(Class<?> clazz, ParameterConverter<?> converter) {
        this.parameters.put(clazz, converter);
    }

    public <T> ParameterConverter<T> getConverter(Class<T> clazz) {
        return (ParameterConverter<T>) this.parameters.get(clazz);
    }
}
