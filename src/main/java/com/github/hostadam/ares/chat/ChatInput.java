package com.github.hostadam.ares.chat;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ChatInput {

    private static final Map<UUID, ChatInput> INPUTS = new ConcurrentHashMap<>();

    private final UUID uniqueId;
    @Getter private Predicate<String> reader;
    @Getter private boolean cancellable = true;

    public ChatInput(Player player) {
        this.uniqueId = player.getUniqueId();
        INPUTS.put(this.uniqueId, this);
    }

    public ChatInput read(Predicate<String> reader) {
        this.reader = reader;
        return this;
    }

    public ChatInput nonCancellable() {
        this.cancellable = false;
        return this;
    }

    public void finish() {
        INPUTS.remove(this.uniqueId);
    }

    public static Optional<ChatInput> get(Player player) {
        return Optional.ofNullable(INPUTS.get(player.getUniqueId()));
    }

    public static ChatInput newInput(Player player) {
        return new ChatInput(player);
    }

}
