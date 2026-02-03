package io.github.hostadam.menu.item;

import io.github.hostadam.menu.Menu;
import io.github.hostadam.menu.MenuAction;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface MenuItemType {
    boolean isVisible(Menu menu);
    boolean click(Menu menu, MenuAction click);

    static MenuItemType conditional(Predicate<Menu> applicable, BiFunction<Menu, MenuAction, Boolean> clickAction) {
        return new MenuItemType() {
            @Override
            public boolean isVisible(Menu menu) {
                return applicable.test(menu);
            }

            @Override
            public boolean click(Menu menu, MenuAction click) {
                return clickAction.apply(menu, click);
            }
        };
    }
}

