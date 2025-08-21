package io.github.hostadam.api.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@Getter
public class PlayerModifyInventoryEvent extends InventoryInteractEvent {

    private final Player player;
    private final InventoryModification modification;
    private final int affectedSlot;

    public PlayerModifyInventoryEvent(@NotNull InventoryView view, @NotNull Player player, @NotNull InventoryModification modification, int affectedSlot) {
        super(view);
        this.player = player;
        this.modification = modification;
        this.affectedSlot = affectedSlot;
    }

    public @NotNull Inventory getInventory() {
        return super.getInventory();
    }

    public void cancelIfSlot(Predicate<Integer> predicate) {
        if(this.affectedSlot == -1 || predicate.test(this.affectedSlot)) {
            this.setCancelled(true);
        }
    }

    public boolean wasTakeOut() {
        return this.modification == InventoryModification.REMOVED || this.modification == InventoryModification.REPLACED;
    }

    public enum InventoryModification {
        ADDED, REMOVED, REPLACED;
    }

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
