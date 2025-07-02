package com.github.hostadam.ares.command.data;

import com.github.hostadam.ares.command.AresCommand;
import com.github.hostadam.ares.command.TabCompletionMapper;
import com.github.hostadam.ares.command.context.CommandContext;
import com.github.hostadam.ares.command.context.CommandContextHelper;
import com.github.hostadam.ares.command.context.CommandExecutionException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.*;

@Data
@AllArgsConstructor
public class AresCommandData {

    private final String parentName;
    private final String[] commandLabels;
    private final String description;
    private final String usageMessage;
    private final String permission;
    private final int requiredArgs;
    private final List<String> expectedParameters;
    private final Map<String, Class<?>> tabCompleters;

    private final Method method;
    private final Object commandInstance;

    public AresCommandData(AresCommand command, Method method, Object object) {
        this.parentName = command.parent();
        this.commandLabels = command.labels();
        this.description = command.description();
        this.usageMessage = command.usage();
        this.permission = command.permission();

        this.method = method;
        this.commandInstance = object;
        this.expectedParameters = new LinkedList<>();

        int argCounter = 0;
        if(!this.usageMessage.isEmpty()) {
            for(String token : this.usageMessage.split(" ")) {
                String cleaned = token.replaceAll("[<>\\[\\]]", "");
                this.expectedParameters.add(cleaned);

                if(token.startsWith("<") && token.endsWith(">")) {
                    argCounter++;
                }
            }
        }

        this.requiredArgs = argCounter;
        this.tabCompleters = new HashMap<>();
        this.setupTabCompletions();
    }

    private void setupTabCompletions() {
        if(this.method.isAnnotationPresent(TabCompletionMapper.class)) {
            Arrays.stream(this.method.getAnnotationsByType(TabCompletionMapper.class))
                    .forEach(mapper -> this.tabCompleters.put(mapper.key(), mapper.mappedClass()));
        }
    }

    public Class<?> getTabCompleterClass(int args) {
        if(args >= this.expectedParameters.size()) return null;
        String key = this.expectedParameters.get(args);
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
        } catch (CommandExecutionException ignored) {
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
