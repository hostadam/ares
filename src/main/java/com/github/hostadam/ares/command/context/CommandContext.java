package com.github.hostadam.ares.command.context;

import com.github.hostadam.ares.command.data.AresCommandData;
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

    public void response(String message) {
        this.commandSender.sendMessage(message);
    }

    /** Helper methods **/
    private <T> T getArgument(int index, T defaultValue) {
        if(index == -1 || index >= arguments.length) return defaultValue;
        String value = arguments[index];
        Class<?> clazz = defaultValue.getClass();
        Optional<T> optional = this.helper.parse(clazz, value);
        return optional.orElse(defaultValue);
    }

    private <T> Optional<T> getArgument(int index, Class<T> type) {
        if(index == -1 || index >= arguments.length) return Optional.empty();
        String value = arguments[index];
        return this.helper.parse(type, value);
    }

    private <T> Optional<T> getArgument(int index, Class<T> type, String errorMessage) {
        Optional<T> optional = getArgument(index, type);
        if(optional.isEmpty()) this.response(errorMessage);
        return optional;
    }

    /** Public methods **/
    public <T> T getArgument(String parameterName, T defaultValue) {
        int index = this.command.getExpectedParameters().indexOf(parameterName);
        return this.getArgument(index, defaultValue);
    }

    public <T> T getArgument(String parameterName, Class<T> type, String errorMessage) {
        int index = this.command.getExpectedParameters().indexOf(parameterName);
        return this.getArgument(index, type, errorMessage).orElseThrow(CommandExecutionException::new);
    }

    public Optional<String> getStringArgument(String parameterName) {
        int index = this.command.getExpectedParameters().indexOf(parameterName);
        if(index == -1 || index >= arguments.length) return Optional.empty();
        if(index == this.command.getExpectedParameters().size() - 1) {
            String joined = String.join(" ", Arrays.copyOfRange(arguments, index, arguments.length));
            return Optional.of(joined);
        }

        return this.getArgument(index, String.class);
    }

    public String senderFormattedName(Function<Player, String> function) {
        return commandSender instanceof Player player ? function.apply(player) : "CONSOLE";
    }

    public <T extends CommandSender> T sender(Class<T> type, String error) {
        if(type.isInstance(this.commandSender)) {
            return type.cast(this.commandSender);
        }

        response(error);
        throw new CommandExecutionException();
    }
}
