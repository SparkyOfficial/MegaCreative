package com.megacreative.models;

import lombok.Data;
import lombok.NoArgsConstructor;

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
        this(true, false, false);
    }
    

    
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
    
    // Setter methods
    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
    }
    
    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }
    
    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }
    
    public void setFireSpread(boolean fireSpread) {
        this.fireSpread = fireSpread;
    }
    
    public void setMobGriefing(boolean mobGriefing) {
        this.mobGriefing = mobGriefing;
    }
    
    public void setWeatherCycle(boolean weatherCycle) {
        this.weatherCycle = weatherCycle;
    }
    
    public void setDayNightCycle(boolean dayNightCycle) {
        this.dayNightCycle = dayNightCycle;
    }
}
