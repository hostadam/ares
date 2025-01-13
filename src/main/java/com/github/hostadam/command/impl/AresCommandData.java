package com.github.hostadam.command.impl;

import com.github.hostadam.command.AresCommand;
import com.github.hostadam.command.parameter.Param;
import com.github.hostadam.command.parameter.ParameterConverter;
import com.github.hostadam.command.handler.CommandHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

@Data
@AllArgsConstructor
public class AresCommandData {

    private AresCommand command;
    private boolean playerOnly;

    private Method method;
    private Object commandInstance;

    public AresCommandData(AresCommand command, Method method, Object object) {
        this.command = command;
        this.method = method;
        this.commandInstance = object;
        this.playerOnly = method.getParameters()[0].getType() == Player.class;
    }

    public void execute(CommandHandler handler, CommandSender sender, String[] args) {
        //First we check if only players should be able to run this.
        if(this.playerOnly && !(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can run this command.");
            return;
        }

        final int parameters = this.method.getParameterCount();
        //First object is assigned instantly, for the command sender.
        Object[] objects = new Object[parameters];
        objects[0] = (this.playerOnly ? (Player) sender : sender);

        int requiredArgCount = 0;
        if(parameters > 1) {
            for(int i = 1; i < parameters; i++) {
                Parameter parameter = this.method.getParameters()[i];

                if(parameter.getType() == String[].class) {
                    if(i > args.length) {
                        objects[i] = new String[] {};
                    } else {
                        String[] arrayCopy = Arrays.copyOfRange(args, i - 1, args.length);
                        objects[i] = arrayCopy;
                        requiredArgCount += arrayCopy.length;
                    }
                    break;
                }

                ParameterConverter<?> converter = handler.getConverter(parameter.getType());
                if(converter == null) continue;

                Param param = parameter.getAnnotation(Param.class);
                boolean required = param == null || !param.optional();
                if(required) requiredArgCount++;

                if(i - 1 >= args.length) {
                    if(required) {
                        break;
                    } else {
                        objects[i] = converter.defaultValue();
                        continue;
                    }
                }

                String arg = args[i - 1].trim();
                try {
                    Object object = converter.convert(arg);
                    if(object == null) {
                        if(required) {
                            converter.error(sender, arg);
                            return;
                        }

                        object = converter.defaultValue();
                    }

                    objects[i] = object;
                } catch(Exception exception) {
                    if(required) {
                        converter.error(sender, arg);
                        return;
                    } else {
                        objects[i] = converter.defaultValue();
                    }
                }
            }
        }

        final int requiredArgs = Math.max(this.command.requiredArgs(), requiredArgCount);
        if(args.length < requiredArgs) {
            sender.sendMessage("§cUsage: /" + (this.command.parent().isEmpty() ? "" : this.command.parent() + " ") + this.command.labels()[0] + " " + this.command.usage());
            return;
        }

        try {
            method.invoke(this.commandInstance, objects);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
