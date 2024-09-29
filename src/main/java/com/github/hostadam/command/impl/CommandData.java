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

    public void execute(CommandHandler handler, CommandSender sender, String[] args) {
        int parameters = this.method.getParameterCount(); //2
        Object[] objects = new Object[parameters]; //2

        /**
         * feed command (args 0 = heal self, args 1 = target)
         * onCommand(CommandSender sender, String[] args)
         *
         * faction create (args 0 = usage, args 1 = name)
         * onSubCommand(CommandSender sender, String name)
         */

        if(this.playerOnly && !(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can run this command");
            return;
        }

        if((parameters - 1) > args.length) { //1 > 0
            sender.sendMessage("§cUsage: /" + this.command.usage());
            return;
        }

        objects[0] = (this.playerOnly ? (Player) sender : sender);

        int argCount = 0;

        if(parameters > 1) {
            for(int i = 1; i < parameters; i++) {
                Parameter parameter = this.method.getParameters()[i];

                if(parameter.getType() == String[].class) {
                    if(i > args.length) {
                        argCount++;
                        objects[i] = new String[] {};
                    } else {
                        String[] arrayCopy = Arrays.copyOfRange(args, i, args.length);
                        objects[i] = arrayCopy;
                        argCount += arrayCopy.length;
                    }
                } else {
                    if(i - 1 >= args.length) {
                        sender.sendMessage("§cUsage: /" + this.command.usage());
                        return;
                    }

                    argCount++;
                }

                ParameterConverter<?> converter = handler.getConverter(parameter.getType());
                if(converter == null) continue;

                try {
                    Object object = converter.convert(args[i - 1].trim());
                    if(object == null) {
                        converter.error(sender, args[i - 1]);
                        return;
                    }

                    objects[i] = object;
                } catch(Exception exception) {
                    converter.error(sender, args[i - 1]);
                    return;
                }
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
