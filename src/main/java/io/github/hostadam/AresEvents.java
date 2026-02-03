package io.github.hostadam;

import io.github.hostadam.board.BoardHandler;
import io.github.hostadam.menu.Menu;
import io.github.hostadam.utilities.AdventureUtils;
import io.github.hostadam.utilities.InputHandler;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.AllArgsConstructor;
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

    private final BoardHandler boardHandler;
    private final InputHandler inputHandler;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.boardHandler.handlePlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.boardHandler.destroyBoard(player);
        this.inputHandler.removeChatInput(player);
        Menu.get(player).ifPresent(_ -> {
            Menu.remove(player);
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Menu.get(player).ifPresent(_ -> Menu.remove(player));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if(slot == -1) return;
        Menu.get(player).ifPresent(menu -> menu.handleClick(event));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if(!this.inputHandler.hasChatInput(player)) return;
        boolean hadInput = this.inputHandler.handleChatInput(player, AdventureUtils.toPlainString(event.message()).trim());
        if(hadInput) {
            event.setCancelled(true);
        }
    }
}