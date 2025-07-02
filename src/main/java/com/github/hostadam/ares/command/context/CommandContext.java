package com.github.hostadam.ares.command.context;

import com.github.hostadam.ares.command.data.AresCommandData;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class CommandContext {

    private final CommandContextHelper helper;
    private final AresCommandData command;
    private final CommandSender commandSender;
    private final String[] arguments;

    public CommandContext(CommandContextHelper helper, AresCommandData command, CommandSender sender, String[] arguments) {
        this.helper = helper;
        this.command = command;
        this.commandSender = sender;
        this.arguments = arguments;
    }

    public void response(Component message) {
        this.commandSender.sendMessage(message);
    }

    /** Helper methods **/
    private <T> T getArgument(int index, T defaultValue) {
        if(index == -1 || index >= arguments.length) return defaultValue;
        Class<?> clazz = defaultValue.getClass();
        if(clazz == String.class && index == this.command.getExpectedParameters().size() - 1) {
            String joined = String.join(" ", Arrays.copyOfRange(arguments, index, arguments.length));
            return (T) Optional.of(clazz.cast(joined));
        }

        String value = arguments[index];
        Optional<T> optional = this.helper.parse(clazz, value);
        return optional.orElse(defaultValue);
    }

    private <T> Optional<T> getArgument(int index, Class<T> type) {
        if(index == -1 || index >= arguments.length) return Optional.empty();

        if (type == String.class && index == this.command.getExpectedParameters().size() - 1) {
            String joined = String.join(" ", Arrays.copyOfRange(arguments, index, arguments.length));
            return Optional.of(type.cast(joined));
        }

        String value = arguments[index];
        return this.helper.parse(type, value);
    }

    private <T> Optional<T> getArgument(int index, Class<T> type, Component errorMessage) {
        Optional<T> optional = getArgument(index, type);
        if(optional.isEmpty()) this.response(errorMessage);
        return optional;
    }

    /** Public methods **/

    // Used to fetch an argument that may not be present, and if it isn't, it will still proceed.
    public <T> Optional<T> getArgument(String parameterName, Class<T> type) {
        int index = this.command.getIndexOf(parameterName);
        return this.getArgument(index, type);
    }

    // Used to fetch an argument that may not be present, and if not, it will send an error message.
    public <T> Optional<T> getArgument(String parameterName, Class<T> type, Component errorMessage) {
        int index = this.command.getIndexOf(parameterName);
        if(index == -1 || index >= this.arguments.length) return Optional.empty();

        Optional<T> optional = getArgument(index, type);
        if(optional.isEmpty()) {
            this.response(errorMessage);
            throw new CommandExecutionException();
        }

        return optional;
    }

    // Used to fetch an argument directly with a provided default value as fallback
    public <T> T getArgument(String parameterName, T defaultValue) {
        int index = this.command.getIndexOf(parameterName);
        return this.getArgument(index, defaultValue);
    }

    // Used to fetch a required argument with an error message if not present.
    public <T> T requireArg(String parameterName, Class<T> type, Component errorMessage) {
        return this.getArgument(parameterName, type, errorMessage).orElseThrow(CommandExecutionException::new);
    }

    // Used to return the name of the sender with a function for player names specifically
    public String senderFormattedName(Function<Player, String> function) {
        return commandSender instanceof Player player ? function.apply(player) : "CONSOLE";
    }

    // Used to get the sender
    public <T extends CommandSender> T sender(Class<T> type, Component error) {
        if(type.isInstance(this.commandSender)) {
            return type.cast(this.commandSender);
        }

        response(error);
        throw new CommandExecutionException();
    }
}
