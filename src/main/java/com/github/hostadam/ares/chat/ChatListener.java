package com.github.hostadam.ares.chat;

import com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.function.Predicate;

public class ChatListener implements Listener {

    @EventHandler
    public void onQuit(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        ChatInput.getPendingInput(player).ifPresent(chatInput -> {
            event.setCancelled(true);

            if(chatInput.isCancellable() && message.equalsIgnoreCase("cancel")) {
                chatInput.finish();
                player.sendMessage("§cYour input has been cancelled.");
                return;
            }

            Predicate<String> predicate = chatInput.getReader();
            if(!predicate.test(message)) {
                player.sendMessage("§cYou have submitted an invalid input. Try again.");
            } else {
                chatInput.finish();
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ChatInput.getPendingInput(player).ifPresent(ChatInput::finish);
    }
}
