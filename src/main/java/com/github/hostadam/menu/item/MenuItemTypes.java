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

import java.util.function.BiFunction;

public class MenuItemTypes {

    private MenuItemTypes() { }

    public static final MenuItemType PREVIOUS_PAGE =
            MenuItemType.conditional(Menu::hasPreviousPage, (menu, click) -> {
                menu.previousPage();
                return true;
            });

    public static final MenuItemType NEXT_PAGE =
            MenuItemType.conditional(Menu::hasNextPage, (menu, click) -> {
                menu.nextPage();
                return true;
            });

    public static final MenuItemType PARENT_PAGE =
            MenuItemType.conditional(Menu::hasParent, (menu, click) -> {
                if(menu.hasParent()) menu.switchToParentMenu(click.player());
                return true;
            });

    public static MenuItemType custom(BiFunction<Menu, MenuAction, Boolean> consumer) {
        return MenuItemType.conditional(_ -> true, consumer);
    }
}

