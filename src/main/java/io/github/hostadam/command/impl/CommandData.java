package io.github.hostadam.command.impl;

import io.github.hostadam.command.param.ParamMapper;
import io.github.hostadam.command.context.CommandContext;
import io.github.hostadam.command.context.CommandContextRegistry;
import io.github.hostadam.command.AresCommandException;
import io.github.hostadam.command.param.Parameters;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
public class CommandData {

    private final CommandRegistry handler;
    private final String[] commandLabels;
    private final String description;
    private final String usageMessage;
    private final String permission;
    private final int requiredArgs;
    private final boolean sendUsagePrioritized;

    private final Map<String, Integer> expectedParameters;
    private final Map<String, Class<?>> tabCompleters;

    private final Method method;
    private final Object commandInstance;

    public CommandData(CommandRegistry handler, String[] labels, String description, String usageMessage, String permission, boolean sendUsagePrioritized, Method method, Object object) {
        this.handler = handler;
        this.commandLabels = labels;
        this.description = description;
        this.usageMessage = usageMessage;
        this.permission = permission;
        this.sendUsagePrioritized = sendUsagePrioritized;

        this.method = method;
        this.commandInstance = object;
        this.expectedParameters = new ConcurrentHashMap<>();

        int argCounter = 0, requiredArgCount = 0;
        if(!this.usageMessage.isEmpty()) {
            for(String token : this.usageMessage.split(" ")) {
                String cleaned = token.replaceAll("[<>\\[\\]]", "");
                this.expectedParameters.put(cleaned, argCounter++);

                if(token.startsWith("<") && token.endsWith(">")) {
                    requiredArgCount++;
                }
            }
        }

        this.requiredArgs = requiredArgCount;
        this.tabCompleters = new HashMap<>();
        this.setupTabCompletions();
    }

    private void setupTabCompletions() {
        if(this.method.isAnnotationPresent(ParamMapper.class) || this.method.isAnnotationPresent(Parameters.class)) {
            Arrays.stream(this.method.getDeclaredAnnotationsByType(ParamMapper.class))
                    .forEach(mapper -> this.tabCompleters.put(mapper.key(), mapper.value()));
        }
    }

    public int getIndexOf(String key) {
        return this.expectedParameters.getOrDefault(key, -1);
    }

    public Class<?> getTabCompleterClass(int args) {
        if(args >= this.expectedParameters.size()) return null;

        Optional<Map.Entry<String, Integer>> optional = this.expectedParameters.entrySet().stream().filter(stringIntegerEntry -> stringIntegerEntry.getValue() == args).findAny();
        if(optional.isEmpty()) return null;
        String key = optional.get().getKey();
        if(!this.tabCompleters.containsKey(key)) return null;
        return this.tabCompleters.get(key);
    }

    public String getMainLabel() {
        return this.commandLabels[0];
    }

    public List<String> getAliases() {
        String[] labels = this.commandLabels;
        if(labels.length == 1) return List.of();
        return List.of(Arrays.copyOfRange(labels, 1, labels.length));
    }

    private void executeMethod(CommandContext context) {
        try {
            method.invoke(this.commandInstance, context);
        } catch (Exception exception) {
            if(exception instanceof AresCommandException) return;
            context.response(Component.text("An unknown error occurred while executing this command. Please contact an administrator with relevant information:", NamedTextColor.RED).append(Component.newline()).append(Component.text(exception.getCause().getMessage(), NamedTextColor.GRAY)));
        }
    }

    public void execute(CommandContextRegistry contextHelper, CommandSender sender, String[] args) {
        this.executeMethod(new CommandContext(
                contextHelper,
                this,
                sender,
                args
        ));
    }
}
