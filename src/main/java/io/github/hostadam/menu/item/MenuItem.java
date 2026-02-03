package io.github.hostadam.menu.item;

import io.github.hostadam.menu.Menu;
import io.github.hostadam.menu.MenuAction;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MenuItem {

    @NotNull
    private final Supplier<ItemStack> itemStackSupplier;
    private ItemStack fallbackItem;
    private MenuItemType itemType;

    public MenuItem(@NotNull Supplier<ItemStack> supplier) {
        this.itemStackSupplier = supplier;
    }

    public boolean click(Menu menu, MenuAction action) {
        if(this.itemType == null) return false;
        return this.itemType.click(menu, action);
    }

    public ItemStack getItem(Menu menu) {
        boolean shouldSlow = this.testVisibility(menu);
        if(shouldSlow || this.fallbackItem == null) {
            return this.itemStackSupplier.get();
        } else {
            return this.fallbackItem.clone();
        }
    }

    public boolean testVisibility(Menu menu) {
        if(this.itemType == null) return true;
        return this.itemType.isVisible(menu);
    }

    public MenuItem type(MenuItemType itemType) {
        this.itemType = itemType;
        return this;
    }

    public MenuItem fallback(ItemStack fallbackItem) {
        this.fallbackItem = fallbackItem;
        return this;
    }

    public MenuItem copy() {
        return new MenuItem(this.itemStackSupplier)
                .fallback(this.fallbackItem)
                .type(this.itemType);
    }
}
