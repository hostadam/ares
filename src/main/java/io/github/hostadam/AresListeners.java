package io.github.hostadam;

import io.github.hostadam.api.ChatInput;
import io.github.hostadam.api.Selection;
import io.github.hostadam.api.events.PlayerModifyInventoryEvent;
import io.github.hostadam.api.handler.Handler;
import io.github.hostadam.api.menu.Menu;
import io.github.hostadam.utilities.PaperUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Level;

@AllArgsConstructor
public class AresListeners implements Listener {

    private final Component textNotReady = Component.text("The server is not ready yet.", NamedTextColor.RED);
    private final AresImpl plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDisable(PluginDisableEvent event) {
        List<Handler<?>> handlers = this.plugin.getAndRemoveHandlers(event.getPlugin());
        if(handlers == null) return;
        for(Handler<?> handler : handlers) {
            handler.disable();
            this.plugin.getLogger().log(Level.INFO, "Plugin shutdown: disabling " + handler.getClass().getName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
        if (!this.plugin.isServerReady()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.kickMessage(textNotReady);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.plugin.scoreboard().setupPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.plugin.removeChatInput(player);
        this.plugin.removeSelection(player);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.isCancelled()) return;
        if(event.getAction() == InventoryAction.NOTHING) return;

        InventoryView view = event.getView();
        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean doesCursorExist = !cursor.isEmpty();
        boolean doesClickedExist = clicked != null && !clicked.isEmpty();
        boolean wasTopGuiAffected = event.getClickedInventory() == view.getTopInventory();

        PlayerModifyInventoryEvent.InventoryModification modification;
        int affectedSlot = event.getRawSlot();

        switch (event.getAction()) {
            case PICKUP_ALL, PICKUP_SOME, PICKUP_HALF, PICKUP_ONE, PICKUP_FROM_BUNDLE, PICKUP_ALL_INTO_BUNDLE, PICKUP_SOME_INTO_BUNDLE, DROP_ONE_SLOT, DROP_ALL_SLOT -> {
                if(!wasTopGuiAffected || !doesClickedExist) return;
                modification = PlayerModifyInventoryEvent.InventoryModification.REMOVED;
            }
            case PLACE_ALL, PLACE_SOME, PLACE_ONE, PLACE_FROM_BUNDLE -> {
                if(!wasTopGuiAffected || !doesCursorExist) return;
                modification = PlayerModifyInventoryEvent.InventoryModification.ADDED;
            }
            case SWAP_WITH_CURSOR -> {
                if(!wasTopGuiAffected || !doesClickedExist || !doesCursorExist) return;
                modification = PlayerModifyInventoryEvent.InventoryModification.REPLACED;
            }
            case MOVE_TO_OTHER_INVENTORY -> {
                if(!doesClickedExist) return;
                modification = wasTopGuiAffected ? PlayerModifyInventoryEvent.InventoryModification.REMOVED : PlayerModifyInventoryEvent.InventoryModification.ADDED;
                affectedSlot = wasTopGuiAffected ? event.getRawSlot() : -1;
            }
            case COLLECT_TO_CURSOR -> {
                if(!doesCursorExist) return;
                modification = (wasTopGuiAffected ? PlayerModifyInventoryEvent.InventoryModification.ADDED : PlayerModifyInventoryEvent.InventoryModification.REMOVED);
                affectedSlot = -1;
            }
            case HOTBAR_SWAP -> {
                if(!wasTopGuiAffected) return;
                modification = doesClickedExist ? PlayerModifyInventoryEvent.InventoryModification.REMOVED : PlayerModifyInventoryEvent.InventoryModification.ADDED;
            }
            default -> {
                return;
            }
        }

        if(!new PlayerModifyInventoryEvent(view, (Player) event.getWhoClicked(), modification, affectedSlot).callEvent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        InventoryView view = event.getView();
        Player player = (Player) event.getWhoClicked();
        int size = view.getTopInventory().getSize();

        for(int slot : event.getRawSlots()) {
            if(slot >= size) continue;
            new PlayerModifyInventoryEvent(view, player, PlayerModifyInventoryEvent.InventoryModification.ADDED, slot).callEvent();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component message = event.message();
        ChatInput input = this.plugin.getChatInput(player);
        if(input != null) {
            event.setCancelled(true);

            String content = PaperUtils.asString(message).trim();
            if(input.cancellable() && content.equalsIgnoreCase("cancel")) {
                this.plugin.removeChatInput(player);
                player.sendMessage(Component.text("Your input has been cancelled.", NamedTextColor.RED));
                return;
            }

            if (!input.reader().test(content)) {
                player.sendMessage(Component.text("You have submitted an invalid input. Try again.", NamedTextColor.RED));
            } else {
                this.plugin.removeChatInput(player);
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Selection selection = this.plugin.getSelection(player);
        if(selection == null) return;

        ItemStack itemStack = event.getItem();
        if(itemStack == null || !itemStack.isSimilar(selection.getItem())) {
            return;
        }

        event.setCancelled(true);

        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            selection.cancel();
            this.plugin.removeSelection(player);
        } else if (player.isSneaking() && event.getAction() == Action.LEFT_CLICK_AIR) {
            if (selection.getCornerOne() == null || selection.getCornerTwo() == null) {
                player.sendMessage("§cYou must set both corners.");
                return;
            }

            selection.onConfirm();
            this.plugin.removeSelection(player);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.click(event.getClickedBlock().getLocation(), event.getAction());
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Selection selection = this.plugin.getSelection(player);
        if(selection != null && event.getItemDrop().getItemStack().isSimilar(selection.getItem())) {
            event.setCancelled(true);
        }
    }
}