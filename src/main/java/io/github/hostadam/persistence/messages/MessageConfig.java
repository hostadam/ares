package io.github.hostadam.persistence.messages;

import io.github.hostadam.persistence.Config;
import io.github.hostadam.persistence.ConfigFile;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MessageConfig extends Config {

    private final Map<String, ParsedMessage> messages = new ConcurrentHashMap<>();

    public MessageConfig(ConfigFile file) {
        super(file);
    }

    @Override
    public void load() {
        this.messages.clear();

        YamlConfiguration config = this.file.get();
        for(String path : this.file.allPaths()) {
            List<String> entries = config.isList(path) ? config.getStringList(path) : config.isString(path) ? List.of(Objects.requireNonNull(config.getString(path))) : null;
            if(entries == null) continue;
            this.messages.put(path, new ParsedMessage(entries));
        }
    }

    public Message message(String path) {
        ParsedMessage message = this.messages.getOrDefault(path.toLowerCase(),
                new ParsedMessage(Component.text("An unexpected error occurred when sending a message. Please contact an administrator with the following information: path " + path + " does not exist."))
        );
        return new Message(message);
    }
}
