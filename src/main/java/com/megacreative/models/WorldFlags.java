package com.megacreative.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorldFlags {
    private boolean mobSpawning = true;
    private boolean pvp = false;
    private boolean explosions = false;
    private boolean fireSpread = false;
    private boolean mobGriefing = false;
    private boolean weatherCycle = true;
    private boolean dayNightCycle = true;
    

    
    public WorldFlags(boolean mobSpawning, boolean pvp, boolean explosions) {
        this.mobSpawning = mobSpawning;
        this.pvp = pvp;
        this.explosions = explosions;
    }
    
    // Дополнительные геттеры для совместимости
    public boolean isMobSpawning() {
        return mobSpawning;
    }
    
    public boolean isPvp() {
        return pvp;
    }
    
    public boolean isExplosions() {
        return explosions;
    }
    
    public boolean isFireSpread() {
        return fireSpread;
    }
    
    public boolean isMobGriefing() {
        return mobGriefing;
    }
    
    public boolean isWeatherCycle() {
        return weatherCycle;
    }
    
    public boolean isDayNightCycle() {
        return dayNightCycle;
    }
}
