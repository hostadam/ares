package com.github.hostadam.ares.chat;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ChatInput {

    private static final Map<UUID, ChatInput> INPUT_MAP = new HashMap<>();

    private UUID uniqueId;
    @Getter
    private Predicate<String> validator;
    @Getter
    private Consumer<String> consumer;
    @Getter
    private boolean cancellable = true;

    public ChatInput(UUID uniqueId) {
        this.uniqueId = uniqueId;
        INPUT_MAP.put(uniqueId, this);
    }

    public ChatInput(Player player) {
        this(player.getUniqueId());
    }

    public ChatInput read(Consumer<String> eventConsumer) {
        this.consumer = eventConsumer;
        return this;
    }

    public ChatInput validator(Predicate<String> eventConsumer) {
        this.validator = eventConsumer;
        return this;
    }

    public ChatInput nonCancellable() {
        this.cancellable = false;
        return this;
    }

    public void finish() {
        INPUT_MAP.remove(this.uniqueId);
    }

    public static Optional<ChatInput> getPendingInput(Player player) {
        return Optional.ofNullable(INPUT_MAP.get(player.getUniqueId()));
    }

}
