package com.megacreative.models;

import org.bukkit.Material;
import org.bukkit.World;

public enum CreativeWorldType {
    FLAT("Плоский", Material.GRASS_BLOCK, World.Environment.NORMAL, "FLAT"),
    SURVIVAL("Выживание", Material.STONE, World.Environment.NORMAL, "DEFAULT"),
    OCEAN("Океан", Material.WATER_BUCKET, World.Environment.NORMAL, "DEFAULT"),
    NETHER("Незер", Material.NETHERRACK, World.Environment.NETHER, "DEFAULT"),
    END("Энд", Material.END_STONE, World.Environment.THE_END, "DEFAULT"),
    VOID("Пустота", Material.BARRIER, World.Environment.NORMAL, "VOID");
    
    private final String displayName;
    private final Material icon;
    private final World.Environment environment;
    private final String generatorType;
    
    CreativeWorldType(String displayName, Material icon, World.Environment environment, String generatorType) {
        this.displayName = displayName;
        this.icon = icon;
        this.environment = environment;
        this.generatorType = generatorType;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public World.Environment getEnvironment() {
        return environment;
    }
    
    public String getGeneratorType() {
        return generatorType;
    }
}
