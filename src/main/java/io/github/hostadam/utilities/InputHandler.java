package io.github.hostadam.utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InputHandler {

    private final Map<UUID, Function<String, Boolean>> chatInputs = new ConcurrentHashMap<>();

    public void register(Player player, Function<String, Boolean> function) {
        this.chatInputs.put(player.getUniqueId(), function);
    }

    public void removeChatInput(Player player) {
        this.chatInputs.remove(player.getUniqueId());
    }

    public boolean hasChatInput(Player player) {
        return this.chatInputs.containsKey(player.getUniqueId());
    }

    // Return false if player had no chat input.
    public boolean handleChatInput(Player player, String arg) {
        Function<String, Boolean> function = this.chatInputs.get(player.getUniqueId());
        if(function == null) return false;

        if(arg.equalsIgnoreCase("cancel")) {
            this.chatInputs.remove(player.getUniqueId());
            player.sendMessage(Component.text("Your input has been cancelled.", NamedTextColor.RED));
            return true;
        }

        boolean wasSuccessful = function.apply(arg);
        if(wasSuccessful) {
            this.chatInputs.remove(player.getUniqueId());
        } else {
            player.sendMessage(Component.text("You have submitted an invalid input. Try again.", NamedTextColor.RED));
        }

        return true;
    }
}
