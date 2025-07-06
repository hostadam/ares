package com.github.hostadam.ares.command.data;

import com.github.hostadam.ares.command.AresCommand;
import com.github.hostadam.ares.command.handler.CommandHandler;
import com.github.hostadam.ares.command.tabcompletion.TabCompletionMapper;
import com.github.hostadam.ares.command.context.CommandContext;
import com.github.hostadam.ares.command.context.CommandContextHelper;
import com.github.hostadam.ares.command.context.CommandExecutionException;
import com.github.hostadam.ares.command.tabcompletion.TabCompletions;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@AllArgsConstructor
public class AresCommandData {

    private final CommandHandler handler;
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

    public AresCommandData(CommandHandler handler, String[] labels, String description, String usageMessage, String permission, boolean sendUsagePrioritized, Method method, Object object) {
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
        if(this.method.isAnnotationPresent(TabCompletionMapper.class) || this.method.isAnnotationPresent(TabCompletions.class)) {
            Arrays.stream(this.method.getDeclaredAnnotationsByType(TabCompletionMapper.class))
                    .forEach(mapper -> this.tabCompleters.put(mapper.key(), mapper.mappedClass()));
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
        } catch (InvocationTargetException exception) {
            if(exception.getCause() instanceof CommandExecutionException || this.handler.isDebugMode()) exception.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void execute(CommandContextHelper contextHelper, CommandSender sender, String[] args) {
        this.executeMethod(new CommandContext(
                contextHelper,
                this,
                sender,
                args
        ));
    }
}
