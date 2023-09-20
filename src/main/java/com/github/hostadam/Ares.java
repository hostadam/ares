package com.github.hostadam;

import com.github.hostadam.menu.MenuHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class Ares extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new MenuHandler(), this);
    }
}
