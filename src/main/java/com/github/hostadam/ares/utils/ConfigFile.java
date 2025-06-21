package com.github.hostadam.ares.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigFile extends YamlConfiguration {

    private File file;

    public ConfigFile(JavaPlugin owner, String name) {
        owner.getDataFolder().mkdirs();

        this.file = new File(owner.getDataFolder(), name + ".yml");
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            owner.saveResource(name + ".yml", false);
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

