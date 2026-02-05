/*
 * MIT License
 * Copyright (c) 2026 Hostadam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.hostadam;

import com.github.hostadam.board.BoardHandler;
import com.github.hostadam.menu.Menu;
import com.github.hostadam.utilities.AdventureUtils;
import com.github.hostadam.utilities.InputHandler;
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