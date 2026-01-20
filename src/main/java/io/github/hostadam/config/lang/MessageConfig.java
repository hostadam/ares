package io.github.hostadam.config.lang;

import io.github.hostadam.config.ConfigFile;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MessageConfig {

    private final ConfigFile file;
    private final Map<String, ParsedMessage> messages = new ConcurrentHashMap<>();

    public MessageConfig(ConfigFile file) {
        this.file = file;
    }

    public void load() {
        this.file.load();

        for(String path : this.file.getKeys(true)) {
            List<String> entries = this.file.isList(path) ? this.file.getStringList(path) : this.file.isString(path) ? List.of(Objects.requireNonNull(this.file.getString(path))) : null;
            if(entries == null || entries.isEmpty()) continue;
            ParsedMessage message = new ParsedMessage(entries);
            this.messages.put(path.toLowerCase(), message);
        }
    }

    public boolean doesMessageExist(String path) {
        return this.messages.containsKey(path.toLowerCase());
    }

    public Message message(String path) {
        ParsedMessage message = this.messages.getOrDefault(path,
                new ParsedMessage(Component.text("An unexpected error occurred when sending a message. Please contact an administrator with the following information: path " + path + " does not exist."))
        );
        return new Message(message);
    }
}
