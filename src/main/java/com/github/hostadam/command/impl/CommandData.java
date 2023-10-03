package com.github.hostadam.command.impl;

import com.github.hostadam.command.AresCommand;
import com.github.hostadam.command.ParameterConverter;
import com.github.hostadam.command.handler.CommandHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

@Data
@AllArgsConstructor
public class CommandData {

    private AresCommand command;
    private boolean playerOnly;

    private Method method;
    private Object object;

    public CommandData(AresCommand command, Method method, Object object) {
        this.command = command;
        this.method = method;
        this.object = object;
        this.playerOnly = method.getParameters()[0].getType() == Player.class;
    }

    public boolean match(String name) {
        for(String string : this.command.labels()) {
            if(!string.contains(" ")) continue;
            if(string.split(" ")[1].equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public void execute(CommandHandler handler, CommandSender sender, String[] args) {
        int parameters = this.method.getParameterCount();
        Object[] objects = new Object[parameters];

        if(this.playerOnly && !(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can run this command");
            return;
        }

        if((parameters - 2) > args.length) {
            sender.sendMessage("§cUsage: /" + this.command.usage());
            return;
        }

        objects[0] = sender;

        int argCount = 0;

        for(int i = 0; i < parameters; i++) {
            if(i + 1 > parameters - 1) break;
            Parameter parameter = this.method.getParameters()[i + 1];

            if(parameter.getType() == String[].class) {
                if(i + 1 > args.length) {
                    objects[i + 1] = new String[] {};
                    argCount++;
                    break;
                }

                String[] arrayCopy = Arrays.copyOfRange(args, i + 1, args.length);
                objects[i] = arrayCopy;
                argCount += arrayCopy.length;
                break;
            }

            argCount++;
            ParameterConverter<?> converter = handler.getConverter(parameter.getType());
            if(converter == null) continue;

            try {
                Object object = converter.convert(args[i].trim());
                if(object == null) {
                    converter.error(sender, args[i]);
                    return;
                }

                objects[i + 1] = object;
            } catch(Exception exception) {
                converter.error(sender, args[i]);
                return;
            }
        }

        if(argCount < this.command.requiredArgs()) {
            sender.sendMessage("§cUsage: /" + this.command.usage());
            return;
        }

        try {
            method.invoke(this.object, objects);
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }
}
