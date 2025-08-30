package com.megacreative.models;

import java.util.Objects;

/**
 * Represents various world flags that can be toggled on/off.
 * These flags control different world behaviors and features.
 */
public class WorldFlags {
    private boolean mobSpawning = true;
    private boolean pvp = false;
    private boolean explosions = false;
    private boolean fireSpread = false;
    private boolean mobGriefing = false;
    private boolean weatherCycle = true;
    private boolean dayNightCycle = true;
    
    /**
     * Creates a new WorldFlags instance with default values.
     * Defaults: mobSpawning=true, pvp=false, explosions=false
     */
    public WorldFlags() {
        this(true, false, false);
    }
    
    /**
     * Creates a new WorldFlags instance with the specified values.
     * 
     * @param mobSpawning whether mob spawning is enabled
     * @param pvp whether PvP is enabled
     * @param explosions whether explosions are enabled
     */
    public WorldFlags(boolean mobSpawning, boolean pvp, boolean explosions) {
        this.mobSpawning = mobSpawning;
        this.pvp = pvp;
        this.explosions = explosions;
    }
    
    // Getters and setters with documentation
    
    /**
     * @return true if mob spawning is enabled, false otherwise
     */
    public boolean isMobSpawning() {
        return mobSpawning;
    }
    
    /**
     * @param mobSpawning whether mob spawning should be enabled
     */
    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
    }
    
    /**
     * @return true if PvP is enabled, false otherwise
     */
    public boolean isPvp() {
        return pvp;
    }
    
    /**
     * @param pvp whether PvP should be enabled
     */
    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }
    
    /**
     * @return true if explosions are enabled, false otherwise
     */
    public boolean isExplosions() {
        return explosions;
    }
    
    /**
     * @param explosions whether explosions should be enabled
     */
    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }
    
    /**
     * @return true if fire spread is enabled, false otherwise
     */
    public boolean isFireSpread() {
        return fireSpread;
    }
    
    /**
     * @param fireSpread whether fire spread should be enabled
     */
    public void setFireSpread(boolean fireSpread) {
        this.fireSpread = fireSpread;
    }
    
    /**
     * @return true if mob griefing is enabled, false otherwise
     */
    public boolean isMobGriefing() {
        return mobGriefing;
    }
    
    /**
     * @param mobGriefing whether mob griefing should be enabled
     */
    public void setMobGriefing(boolean mobGriefing) {
        this.mobGriefing = mobGriefing;
    }
    
    /**
     * @return true if weather cycle is enabled, false otherwise
     */
    public boolean isWeatherCycle() {
        return weatherCycle;
    }
    
    /**
     * @param weatherCycle whether weather cycle should be enabled
     */
    public void setWeatherCycle(boolean weatherCycle) {
        this.weatherCycle = weatherCycle;
    }
    
    /**
     * @return true if day/night cycle is enabled, false otherwise
     */
    public boolean isDayNightCycle() {
        return dayNightCycle;
    }
    
    /**
     * @param dayNightCycle whether day/night cycle should be enabled
     */
    public void setDayNightCycle(boolean dayNightCycle) {
        this.dayNightCycle = dayNightCycle;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldFlags that = (WorldFlags) o;
        return mobSpawning == that.mobSpawning &&
               pvp == that.pvp &&
               explosions == that.explosions &&
               fireSpread == that.fireSpread &&
               mobGriefing == that.mobGriefing &&
               weatherCycle == that.weatherCycle &&
               dayNightCycle == that.dayNightCycle;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(mobSpawning, pvp, explosions, fireSpread, mobGriefing, weatherCycle, dayNightCycle);
    }
    
    @Override
    public String toString() {
        return "WorldFlags{" +
               "mobSpawning=" + mobSpawning +
               ", pvp=" + pvp +
               ", explosions=" + explosions +
               ", fireSpread=" + fireSpread +
               ", mobGriefing=" + mobGriefing +
               ", weatherCycle=" + weatherCycle +
               ", dayNightCycle=" + dayNightCycle +
               '}';
    }
}
