package io.github.hostadam.command.context;

import io.github.hostadam.command.AresCommandData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CommandContext {

    private static final Component CONSOLE = Component.text("CONSOLE", NamedTextColor.RED);

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

    public String[] getRawArguments() {
        return Arrays.copyOf(this.arguments, this.arguments.length);
    }

    public void response(Component message) {
        this.commandSender.sendMessage(message);
    }

    /** Helper methods **/
    private <T> T getArgument(int index, Class<T> type, T defaultValue) {
        if(index == -1 || index >= arguments.length) return defaultValue;
        String value = arguments[index];
        Optional<T> optional = this.helper.parse(type, value);
        return optional.orElse(defaultValue);
    }

    private <T> Optional<T> getArgument(int index, Class<T> type, Predicate<T> predicate) {
        if(index == -1 || index >= arguments.length) return Optional.empty();
        String value = arguments[index];
        Optional<T> optional = this.helper.parse(type, value);
        if(predicate != null) optional = optional.filter(predicate);
        return optional;
    }

    /**
     * This method will join all arguments together, starting from the index of the specified parameter name.
     * @return the concatenated string
     */
    public String joinArguments(String parameterName) {
        int index = this.command.getIndexOf(parameterName);
        if(index == -1 || index >= arguments.length) return "";
        return String.join(" ", Arrays.copyOfRange(arguments, index, arguments.length));
    }

    /**
     * This method will fetch an argument based on a type and predicate. If the argument is not present, it will still proceed.
     */
    public <T> Optional<T> getArgument(String parameterName, Class<T> type, Predicate<T> predicate) {
        int index = this.command.getIndexOf(parameterName);
        return this.getArgument(index, type, predicate);
    }

    /**
     * This method will fetch an argument based on a type. If the argument is not present, it will still proceed.
     */
    public <T> Optional<T> getArgument(String parameterName, Class<T> type) {
        return this.getArgument(parameterName, type, (Predicate<T>) null);
    }

    /**
     * This method will fetch an argument based on a type and predicate.
     * If the argument is not specified (not enough args), it will continue.
     * If the argument parser returns an empty value (invalid input), it will send an error.
     */
    public <T> Optional<T> getArgument(String parameterName, Class<T> type, Predicate<T> predicate, Component errorMessage) {
        int index = this.command.getIndexOf(parameterName);
        if(index == -1 || index >= this.arguments.length) {
            return Optional.empty();
        }

        Optional<T> optional = getArgument(index, type, predicate);
        if(optional.isEmpty()) {
            this.response(errorMessage);
            return Optional.empty();
        }

        return optional;
    }

    /**
     * This method will fetch an argument based on a type.
     * If the argument is not specified (not enough args), it will continue.
     * If the argument parser returns an empty value (invalid input), it will send an error.
     */
    public <T> Optional<T> getArgument(String parameterName, Class<T> type, Component errorMessage) {
        return this.getArgument(parameterName, type, null, errorMessage);
    }

    /**
     * This method will fetch an argument based on a type.
     * If the argument is not specified (not enough args) or if the argument is invalid, it will use the default value.
     */
    public <T> T getArgument(String parameterName, Class<T> type, T defaultValue) {
        int index = this.command.getIndexOf(parameterName);
        return this.getArgument(index, type, defaultValue);
    }

    /**
     * This method will fetch an argument based on a type and predicate.
     * If the argument is not specified (not enough args) or if the argument is invalid, it will use the default value.
     */
    public <T> T getArgument(String parameterName, Class<T> type, Predicate<T> predicate, T defaultValue) {
        int index = this.command.getIndexOf(parameterName);
        T value = this.getArgument(index, type, defaultValue);
        return (predicate == null || predicate.test(value)) ? value : defaultValue;
    }

    /**
     * This method will fetch an argument based on a type and predicate.
     * If the argument is not specified (not enough args) or if the argument is invalid, it will send an error and halt execution.
     */
    public <T> T requireArg(String parameterName, Class<T> type, Predicate<T> predicate, Component errorMessage) {
        int index = this.command.getIndexOf(parameterName);
        if(index == -1 || index >= this.arguments.length) {
            this.response(errorMessage);
            throw new CommandExecutionException();
        }

        Optional<T> optional = getArgument(index, type, predicate);
        if(optional.isEmpty()) {
            this.response(errorMessage);
            throw new CommandExecutionException();
        }

        return optional.get();
    }

    /**
     * This method will fetch an argument based on a type .
     * If the argument is not specified (not enough args) or if the argument is invalid, it will send an error and halt execution.
     */
    public <T> T requireArg(String parameterName, Class<T> type, Component errorMessage) {
        return this.requireArg(parameterName, type, null, errorMessage);
    }

    /**
     * This method will fetch an argument based on a type .
     * If the argument is not specified (not enough args) or if the argument is invalid, it will halt execution.
     */
    public <T> T requireArg(String parameterName, Class<T> type) {
        return this.requireArg(parameterName, type, null, Component.text("An unknown error occurred.", NamedTextColor.RED));
    }

    // Used to return the name of the sender with a function for player names specifically
    public Component senderFormattedName(Function<Player, Component> function) {
        return this.senderFormattedName(function, () -> CONSOLE);
    }

    public Component senderFormattedName(Function<Player, Component> function, Supplier<Component> orFunction) {
        return commandSender instanceof Player player ? function.apply(player) : orFunction.get();
    }

    public CommandSender sender() {
        return this.commandSender;
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
