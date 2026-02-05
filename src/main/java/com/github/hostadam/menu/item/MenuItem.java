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

package com.github.hostadam.menu.item;

import com.github.hostadam.menu.Menu;
import com.github.hostadam.menu.MenuAction;
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
