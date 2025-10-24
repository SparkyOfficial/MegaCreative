package com.megacreative.configs;

import com.megacreative.MegaCreative;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * 🎆 Reference System-Style World Code Configuration
 * 
 * Stores compiled code strings for each world, similar to FrameLand's WorldCode system.
 * This is the bridge between the visual programming interface and the script execution engine.
 */
public class WorldCode {
    
    private static File file;
    private static FileConfiguration customFile;
    private static Logger logger;
    
    public static void setup(MegaCreative plugin) {
        logger = plugin.getLogger();
        file = new File(plugin.getDataFolder(), "world-codes.yml");
        
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (created) {
                    logger.fine("Created world-codes.yml configuration file");
                } else {
                    logger.warning("world-codes.yml file already exists or could not be created");
                }
            } catch (IOException e) {
                logger.severe("Failed to create world-codes.yml file: " + e.getMessage());
            }
        }
        
        customFile = YamlConfiguration.loadConfiguration(file);
        logger.fine("WorldCode configuration system initialized");
    }
    
    public static FileConfiguration get() {
        return customFile;
    }
    
    public static void save() {
        try {
            customFile.save(file);
        } catch (IOException e) {
            System.out.println("Couldn't save WorldCode file: " + e.getMessage());
        }
    }
    
    public static void reload() {
        customFile = YamlConfiguration.loadConfiguration(file);
    }
    
    /**
     * Sets compiled code for a world
     * @param worldId The world ID
     * @param codeLines List of compiled code lines
     */
    public static void setCode(String worldId, List<String> codeLines) {
        customFile.set("worlds." + worldId, codeLines);
        save();
    }
    
    /**
     * Gets compiled code for a world
     * @param worldId The world ID
     * @return List of compiled code lines
     */
    public static List<String> getCode(String worldId) {
        return customFile.getStringList("worlds." + worldId);
    }
    
    /**
     * Checks if a world has compiled code
     * @param worldId The world ID
     * @return true if world has compiled code
     */
    public static boolean hasCode(String worldId) {
        return customFile.contains("worlds." + worldId);
    }
}