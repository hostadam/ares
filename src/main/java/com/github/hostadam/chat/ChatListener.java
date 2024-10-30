package com.github.hostadam.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onQuit(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        ChatInput.getPendingInput(player).ifPresent(chatInput -> {
            if(chatInput.isCancellable() && message.equalsIgnoreCase("cancel")) {
                chatInput.finish();
                player.sendMessage("§cYour input has been cancelled.");
                return;
            }

            if(chatInput.getValidator() != null && !chatInput.getValidator().test(message)) {
                player.sendMessage("§cYou have submitted an invalid input. Try again.");
                return;
            }

            chatInput.getConsumer().accept(message);
            chatInput.finish();
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ChatInput.getPendingInput(player).ifPresent(ChatInput::finish);
    }
}
