package com.github.hostadam.ares.command.experimental;

import com.github.hostadam.ares.command.impl.AresCommandData;
import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CommandContext {

    private final CommandContextHelper helper;

    private String label;
    private CommandSender commandSender;
    private String[] arguments;
    private List<String> expectedParameters;

    public CommandContext(CommandContextHelper helper, AresCommandData command, String label, CommandSender sender, String[] arguments) {
        this.helper = helper;
        this.label = label;
        this.commandSender = sender;
        this.arguments = arguments;

        String usage = command.getCommand().usage();
        this.expectedParameters = usage.isEmpty() ? List.of() : Arrays.stream(usage.split(" ")).map(s -> s.replaceAll("[<>]", "")).toList();
    }

    private void response(String message) {
        this.commandSender.sendMessage(message);
    }

    public <T> T getArgument(int index, T defaultValue) {
        if(index == -1 || index >= arguments.length) return defaultValue;

        String value = arguments[index];
        Class<?> clazz = defaultValue.getClass();
        Optional<T> optional = this.helper.parse(clazz, value);
        return optional.orElse(defaultValue);
    }

    public <T> Optional<T> getArgument(int index, Class<T> type) {
        if(index == -1 || index >= arguments.length) return Optional.empty();
        String value = arguments[index];
        return this.helper.parse(type, value);
    }

    public <T> T getArgument(String parameterName, T defaultValue) {
        int index = this.expectedParameters.indexOf(parameterName);
        return this.getArgument(index, defaultValue);
    }

    public <T> Optional<T> getArgument(String parameterName, Class<T> type) {
        int index = this.expectedParameters.indexOf(parameterName);
        return this.getArgument(index, type);
    }
    
    public Optional<String> stringArg(String parameterName) {
        return this.getArgument(parameterName, String.class);
    }

    private <T> Optional<T> getArgument(int index, Class<T> type, String errorMessage) {
        Optional<T> optional = getArgument(index, type);
        if(optional.isEmpty()) this.response(errorMessage);
        return optional;
    }

    public <T> T requireArg(int index, Class<T> type, String errorMessage) {
        return this.getArgument(index, type, errorMessage).orElseThrow(CommandExecutionException::new); //TODO: try-catch in commandexecutor
    }

    public String executingCommand() {
        return this.label;
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
