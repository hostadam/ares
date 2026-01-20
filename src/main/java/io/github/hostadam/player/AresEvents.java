package io.github.hostadam.player;

import io.github.hostadam.api.menu.Menu;
import io.github.hostadam.implementation.AresImpl;
import io.github.hostadam.utilities.PaperUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class AresEvents implements Listener {

    private final AresImpl plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.plugin.scoreboard().setupPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.plugin.removeChatInput(player);
        this.plugin.scoreboard().destroyBoard(player);
        Menu.get(player).ifPresent(Menu::handleClose);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Menu.get(player).ifPresent(Menu::handleClose);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == -1) return;
        Menu.get(player).ifPresent(menu -> menu.click(event));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component message = event.message();
        ChatInput input = this.plugin.getChatInput(player);
        if(input != null) {
            event.setCancelled(true);

            String content = PaperUtils.asString(message).trim();
            if(content.equalsIgnoreCase("cancel")) {
                this.plugin.removeChatInput(player);
                player.sendMessage(Component.text("Your input has been cancelled.", NamedTextColor.RED));
                return;
            }

            if (!input.handleInput(content)) {
                player.sendMessage(Component.text("You have submitted an invalid input. Try again.", NamedTextColor.RED));
            } else {
                this.plugin.removeChatInput(player);
            }
        }
    }
}