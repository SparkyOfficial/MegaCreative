package com.megacreative.managers;

import com.megacreative.MegaCreative;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final MegaCreative plugin;
    private FileConfiguration config;

    public ConfigManager(MegaCreative plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public String getMessage(String path) {
        return config.getString(path, "");
    }

    public int getInt(String path) {
        return config.getInt(path, 0);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }
}
