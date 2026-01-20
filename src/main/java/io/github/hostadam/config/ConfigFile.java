package io.github.hostadam.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ConfigFile extends YamlConfiguration {

    private final File file;

    public ConfigFile(JavaPlugin javaPlugin, String fileName) {
        boolean doesFileExist = (this.file = new File(javaPlugin.getDataFolder(), fileName)).exists();
        if(!doesFileExist) {
            javaPlugin.saveResource(fileName, false);
        }

        this.load();
    }

    public void load() {
        try {
            this.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            this.save(this.file);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
