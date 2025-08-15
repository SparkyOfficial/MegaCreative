package com.megacreative.models;

import lombok.Data;

@Data
public class WorldFlags {
    private boolean mobSpawning = true;
    private boolean pvp = false;
    private boolean explosions = false;
    private boolean fireSpread = false;
    private boolean mobGriefing = false;
    private boolean weatherCycle = true;
    private boolean dayNightCycle = true;
    
    public WorldFlags() {
        // Значения по умолчанию
    }
    
    public WorldFlags(boolean mobSpawning, boolean pvp, boolean explosions) {
        this.mobSpawning = mobSpawning;
        this.pvp = pvp;
        this.explosions = explosions;
    }
}
