package com.megacreative.models;

import org.bukkit.GameMode;
import org.bukkit.Material;

public enum WorldMode {
    PLAY("Игра", Material.DIAMOND_SWORD, GameMode.ADVENTURE, true),
    BUILD("Строительство", Material.GOLDEN_PICKAXE, GameMode.CREATIVE, false),
    DEV("Разработка", Material.COMMAND_BLOCK, GameMode.CREATIVE, false);
    
    private final String displayName;
    private final Material icon;
    private final GameMode defaultGameMode;
    private final boolean codeEnabled;
    
    WorldMode(String displayName, Material icon, GameMode defaultGameMode, boolean codeEnabled) {
        this.displayName = displayName;
        this.icon = icon;
        this.defaultGameMode = defaultGameMode;
        this.codeEnabled = codeEnabled;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public GameMode getDefaultGameMode() {
        return defaultGameMode;
    }
    
    public boolean isCodeEnabled() {
        return codeEnabled;
    }
}
