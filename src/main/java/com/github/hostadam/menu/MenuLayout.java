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

package com.github.hostadam.menu;

import com.github.hostadam.menu.item.MenuItem;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.*;

@Getter
public class MenuLayout {

    private final Component title;
    private final int size;
    private final String[] rows;

    @Setter
    private boolean allowPagination;
    private final List<MenuItem> items = new ArrayList<>();
    private final Map<Character, MenuItem> itemBindings = new HashMap<>();

    public MenuLayout(Component title, String[] rows) {
        this.title = title;
        this.rows = rows;
        this.size = rows.length * 9;
    }

    public void addItem(MenuItem menuItem) {
        this.items.add(menuItem);
    }

    public void setItem(char c, MenuItem menuItem) {
        this.itemBindings.put(c, menuItem);
    }

    public static MenuLayout of(Component title, int size) {
        int rowCount = Math.max(1, Math.floorDiv(size, 9));
        String[] rows = new String[rowCount];
        Arrays.fill(rows, " ".repeat(rowCount == 1 ? size : 9));
        return new MenuLayout(title, rows);
    }
}
