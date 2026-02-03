package io.github.hostadam.menu.item;

import io.github.hostadam.menu.Menu;
import io.github.hostadam.menu.MenuAction;

import java.util.function.BiConsumer;
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

