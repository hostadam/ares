package io.github.hostadam.api;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public abstract class Selection {

    @NonNull
    private final ItemStack item;
    protected Location cornerOne;
    protected Location cornerTwo;

    public void click(Location location, Action action) {
        if(action == Action.LEFT_CLICK_BLOCK) {
            this.cornerOne = location;
            boolean allow = this.onLeftClick();
            if(!allow) this.cornerOne = null;
        } else {
            this.cornerTwo = location;
            boolean allow = this.onRightClick();
            if(!allow) this.cornerTwo = null;
        }
    }

    public void cancel(Player player) {
        if (player.getInventory().contains(this.item)) {
            player.getInventory().removeItem(this.item);
        }
    }

    public boolean areBothValid() {
        return this.cornerOne != null && this.cornerTwo != null && this.cornerOne.getWorld().getName().equals(this.cornerTwo.getWorld().getName());
    }

    public abstract void cancel();
    public abstract boolean onLeftClick();
    public abstract boolean onRightClick();
    public abstract void onConfirm();
}
