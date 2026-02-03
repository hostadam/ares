package io.github.hostadam.persistence;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ConfigFile {

    private final File file;
    private final YamlConfiguration configuration = new YamlConfiguration();

    public ConfigFile(JavaPlugin javaPlugin, String fileName) {
        this.file = new File(javaPlugin.getDataFolder(), fileName);
        if(!this.file.exists()) {
            javaPlugin.saveResource(fileName, false);
        }
    }

    public void load() {
        try {
            this.configuration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Failed to load config " + file.getPath(), e);
        }
    }

    public void save() {
        try {
            this.configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config " + file.getPath(), e);
        }
    }

    public YamlConfiguration get() {
        return this.configuration;
    }

    public Set<String> allPaths() {
        return this.configuration.getKeys(true);
    }
}
