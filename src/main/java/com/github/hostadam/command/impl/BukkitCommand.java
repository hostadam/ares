package com.github.hostadam.command.impl;

import com.github.hostadam.command.Command;
import com.github.hostadam.command.ParameterConverter;
import com.github.hostadam.command.handler.CommandHandler;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BukkitCommand extends org.bukkit.command.Command {

    protected final CommandHandler commandHandler;
    private final CommandImpl impl;
    private List<CommandImpl> subcommands;

    public BukkitCommand(CommandHandler commandHandler, CommandImpl impl, String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.commandHandler = commandHandler;
        this.impl = impl;
        this.subcommands = new ArrayList<>();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        final Command command = this.impl.getCommand();
        if(!command.target().testFor(sender)) {
            return true;
        }

        if(!command.permission().isEmpty() && !sender.hasPermission(command.permission())) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        //gamemode <gamemode>
        //gamemode set <player> <target>

        CommandImpl subcommand = null;
        if(args.length > 0 && (subcommand = this.getSubCommand(args[0])) != null) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        int requiredArgs = (subcommand != null ? subcommand.getCommand().requiredArgs() : this.impl.getCommand().requiredArgs());
        Object[] objects = new Object[this.impl.getMethod().getParameterCount()];
        if(objects.length < 1) {
            return true;
        }

        if(objects.length < 2 && requiredArgs > 0) {
            sender.sendMessage(subcommand != null ? subcommand.getCommand().usage() : this.impl.getCommand().usage());
            return true;
        }

        Parameter[] parameters = impl.getMethod().getParameters();
        int argCount = 0;
        for(int i = 0; i < args.length; i++) {
            if(i >= objects.length) {
                break;
            }

            Parameter parameter = parameters[i];
            Class<?> clazz = parameter.getType();
            if(clazz == CommandSender.class) {
                objects[i] = sender;
                argCount++;
                continue;
            }

            if(clazz == String[].class) {
                String[] copy = Arrays.copyOfRange(args, i, args.length);
                if(argCount + copy.length < requiredArgs) {
                    sender.sendMessage(subcommand != null ? subcommand.getCommand().usage() : this.impl.getCommand().usage());
                    return true;
                }

                objects[i] = copy;
                break;
            }

            argCount++;
            ParameterConverter<?> converter = this.commandHandler.getConverter(clazz);
            if(converter == null) {
                continue;
            }

            try {
                Object converted = converter.convert(args[i]);
                if(converted == null) {
                    converter.error(sender, args[i]);
                    return true;
                }

                objects[i] = converted;
            } catch(Exception exception) {
                converter.error(sender, args[i]);
                return true;
            }
        }

        try {
            this.call(subcommand, objects);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return true;
    }

    public void addSubCommand(CommandImpl impl) {
        this.subcommands.add(impl);
    }

    private void call(CommandImpl subcommand, Object[] objects) throws InvocationTargetException, IllegalAccessException {
        if(subcommand == null) {
            this.impl.getMethod().invoke(this.impl.getObject(), objects);
        } else {
            subcommand.getMethod().invoke(subcommand.getObject(), objects);
        }
    }

    public CommandImpl getSubCommand(String name) {
        for(CommandImpl command : this.subcommands) {
            for(String label : command.getCommand().labels()) {
                if(label.equalsIgnoreCase(name)) {
                    return command;
                }
            }
        }

        return null;
    }
}
