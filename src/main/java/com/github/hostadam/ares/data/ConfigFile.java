package com.github.hostadam.ares.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigFile extends YamlConfiguration {

    protected final File file;

    public ConfigFile(JavaPlugin plugin, String name) {
        this.file = new File(plugin.getDataFolder(), name + ".yml");

        if(!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(name + ".yml", false);
        }

        this.load();
    }

    public void load() {
        try {
            this.load(this.file);
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    public void save() {
        try {
            this.save(this.file);
        } catch(IOException exception) {
            exception.printStackTrace();
        }
    }
}
