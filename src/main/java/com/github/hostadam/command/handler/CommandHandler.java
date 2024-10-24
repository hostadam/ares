package com.github.hostadam.command.handler;

import com.github.hostadam.command.AresCommand;
import com.github.hostadam.command.ParameterConverter;
import com.github.hostadam.command.impl.CommandData;
import com.github.hostadam.command.impl.CommandImpl;
import com.github.hostadam.command.parameter.BooleanConverter;
import com.github.hostadam.command.parameter.IntConverter;
import com.github.hostadam.command.parameter.OfflinePlayerConverter;
import com.github.hostadam.command.parameter.OnlinePlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class CommandHandler {

    private CommandMap map;

    private final Map<Class<?>, ParameterConverter<?>> parameters;
    private final Map<String, CommandImpl> commands;

    public CommandHandler() {
        this.parameters = new HashMap<>();
        this.commands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        try {
            final Server server = Bukkit.getServer();
            Field field = server.getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            this.map = (CommandMap) field.get(server);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        this.addConverter(Boolean.class, new BooleanConverter());
        this.addConverter(Integer.class, new IntConverter());
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
            CommandData data = new CommandData(command, method, object);
            String name = command.labels()[0];

            if(name.contains(" ")) {
                String[] split = name.split(" ");
                CommandImpl parentCommand = this.getCommandByLabel(split[0]);
                if(parentCommand != null) parentCommand.addSubCommand(data);
            } else {
                CommandImpl impl = new CommandImpl(this, data);
                this.map.register(name, impl);
                this.commands.put(name, impl);
            }
        }
    }

    public CommandImpl getCommandByLabel(String name) {
        return this.commands.get(name);
    }

    public void addConverter(Class<?> clazz, ParameterConverter<?> converter) {
        this.parameters.put(clazz, converter);
    }

    public <T> ParameterConverter<T> getConverter(Class<T> clazz) {
        return (ParameterConverter<T>) this.parameters.get(clazz);
    }
}
