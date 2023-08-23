package com.github.hostadam;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class AresPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        AresAPI.log("Ares utilities is loading...");

        getServer().getPluginManager().registerEvents(this, this);
    }
}
