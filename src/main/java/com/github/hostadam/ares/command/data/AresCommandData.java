package com.github.hostadam.ares.command.data;

import com.github.hostadam.ares.command.AresCommand;
import com.github.hostadam.ares.command.tabcompletion.TabCompletionMapper;
import com.github.hostadam.ares.command.context.CommandContext;
import com.github.hostadam.ares.command.context.CommandContextHelper;
import com.github.hostadam.ares.command.context.CommandExecutionException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
public class AresCommandData {

    private final String[] commandLabels;
    private final String description;
    private final String usageMessage;
    private final String permission;
    private final int requiredArgs;

    private final Map<String, Integer> expectedParameters;
    private final Map<String, Class<?>> tabCompleters;

    private final Method method;
    private final Object commandInstance;

    public AresCommandData(String[] labels, String description, String usageMessage, String permission, Method method, Object object) {
        this.commandLabels = labels;
        this.description = description;
        this.usageMessage = usageMessage;
        this.permission = permission;

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
        if(this.method.isAnnotationPresent(TabCompletionMapper.class)) {
            Arrays.stream(this.method.getAnnotationsByType(TabCompletionMapper.class))
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

    public void execute(CommandContextHelper contextHelper, CommandSender sender, String[] args) {
        CommandContext context = new CommandContext(
                contextHelper,
                this,
                sender,
                args
        );

        try {
            method.invoke(this.commandInstance, context);
        } catch (InvocationTargetException | CommandExecutionException ignored) {
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
