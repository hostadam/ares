package com.github.hostadam.ares.chat;

import com.github.hostadam.ares.utils.PaperUtils;
import com.google.common.primitives.Ints;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.function.Predicate;

public class ChatListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onQuit(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component message = event.message();

        ChatInput.get(player).ifPresent(chatInput -> {
            event.setCancelled(true);

            String content = PaperUtils.asString(message).trim();
            if(chatInput.isCancellable() && content.equalsIgnoreCase("cancel")) {
                chatInput.finish();
                player.sendMessage(Component.text("Your input has been cancelled.", NamedTextColor.RED));
                return;
            }

            Predicate<String> predicate = chatInput.getReader();
            if(!predicate.test(content)) {
                player.sendMessage(Component.text("You have submitted an invalid input. Try again.", NamedTextColor.RED));
            } else {
                chatInput.finish();
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ChatInput.get(player).ifPresent(ChatInput::finish);
    }
}
