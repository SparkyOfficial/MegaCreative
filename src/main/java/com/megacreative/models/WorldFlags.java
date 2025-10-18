package com.megacreative.models;

import java.util.Objects;

/**
 * Represents various world flags that can be toggled on/off.
 * These flags control different world behaviors and features.
 *
 * Представляет различные флаги мира, которые можно включать/выключать.
 * Эти флаги управляют различными поведениями и функциями мира.
 *
 * Stellt verschiedene Welt-Flags dar, die ein- und ausgeschaltet werden können.
 * Diese Flags steuern verschiedene Weltverhalten und -funktionen.
 */
public class WorldFlags {
    private boolean mobSpawning;
    private boolean pvp;
    private boolean explosions;
    private boolean fireSpread;
    private boolean mobGriefing;
    private boolean weatherCycle;
    private boolean dayNightCycle;
    
    /**
     * Creates a new WorldFlags instance with default values.
     * Defaults: mobSpawning=true, pvp=false, explosions=false
     *
     * Создает новый экземпляр WorldFlags со значениями по умолчанию.
     * По умолчанию: mobSpawning=true, pvp=false, explosions=false
     *
     * Erstellt eine neue WorldFlags-Instanz mit Standardwerten.
     * Standard: mobSpawning=true, pvp=false, explosions=false
     */
    public WorldFlags() {
        this.mobSpawning = true;
        this.pvp = false;
        this.explosions = false;
        this.fireSpread = false;
        this.mobGriefing = false;
        this.weatherCycle = true;
        this.dayNightCycle = true;
    }
    
    /**
     * Creates a new WorldFlags instance with the specified values.
     * 
     * @param mobSpawning whether mob spawning is enabled
     * @param pvp whether PvP is enabled
     * @param explosions whether explosions are enabled
     *
     * Создает новый экземпляр WorldFlags с указанными значениями.
     * 
     * @param mobSpawning включено ли возрождение мобов
     * @param pvp включен ли PvP
     * @param explosions включены ли взрывы
     *
     * Erstellt eine neue WorldFlags-Instanz mit den angegebenen Werten.
     * 
     * @param mobSpawning ob die Mob-Spawning aktiviert ist
     * @param pvp ob PvP aktiviert ist
     * @param explosions ob Explosionen aktiviert sind
     */
    public WorldFlags(boolean mobSpawning, boolean pvp, boolean explosions) {
        this.mobSpawning = mobSpawning;
        this.pvp = pvp;
        this.explosions = explosions;
        this.fireSpread = false;
        this.mobGriefing = false;
        this.weatherCycle = true;
        this.dayNightCycle = true;
    }
    
    
    
    
    
    /**
     * @return true if mob spawning is enabled, false otherwise
     *
     * @return true, если возрождение мобов включено, false в противном случае
     *
     * @return true, wenn die Mob-Spawning aktiviert ist, sonst false
     */
    public boolean isMobSpawning() {
        return mobSpawning;
    }
    
    /**
     * @param mobSpawning whether mob spawning should be enabled
     *
     * @param mobSpawning следует ли включить возрождение мобов
     *
     * @param mobSpawning ob die Mob-Spawning aktiviert werden soll
     */
    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
    }
    
    /**
     * @return true if PvP is enabled, false otherwise
     *
     * @return true, если PvP включен, false в противном случае
     *
     * @return true, wenn PvP aktiviert ist, sonst false
     */
    public boolean isPvp() {
        return pvp;
    }
    
    /**
     * @param pvp whether PvP should be enabled
     *
     * @param pvp следует ли включить PvP
     *
     * @param pvp ob PvP aktiviert werden soll
     */
    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }
    
    /**
     * @return true if explosions are enabled, false otherwise
     *
     * @return true, если взрывы включены, false в противном случае
     *
     * @return true, wenn Explosionen aktiviert sind, sonst false
     */
    public boolean isExplosions() {
        return explosions;
    }
    
    /**
     * @param explosions whether explosions should be enabled
     *
     * @param explosions следует ли включить взрывы
     *
     * @param explosions ob Explosionen aktiviert werden sollen
     */
    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }
    
    /**
     * @return true if fire spread is enabled, false otherwise
     *
     * @return true, если распространение огня включено, false в противном случае
     *
     * @return true, wenn Feuerausbreitung aktiviert ist, sonst false
     */
    public boolean isFireSpread() {
        return fireSpread;
    }
    
    /**
     * @param fireSpread whether fire spread should be enabled
     *
     * @param fireSpread следует ли включить распространение огня
     *
     * @param fireSpread ob Feuerausbreitung aktiviert werden soll
     */
    public void setFireSpread(boolean fireSpread) {
        this.fireSpread = fireSpread;
    }
    
    /**
     * @return true if mob griefing is enabled, false otherwise
     *
     * @return true, если грифинг мобов включен, false в противном случае
     *
     * @return true, wenn Mob-Griefing aktiviert ist, sonst false
     */
    public boolean isMobGriefing() {
        return mobGriefing;
    }
    
    /**
     * @param mobGriefing whether mob griefing should be enabled
     *
     * @param mobGriefing следует ли включить грифинг мобов
     *
     * @param mobGriefing ob Mob-Griefing aktiviert werden soll
     */
    public void setMobGriefing(boolean mobGriefing) {
        this.mobGriefing = mobGriefing;
    }
    
    /**
     * @return true if weather cycle is enabled, false otherwise
     *
     * @return true, если цикл погоды включен, false в противном случае
     *
     * @return true, wenn Wetterzyklus aktiviert ist, sonst false
     */
    public boolean isWeatherCycle() {
        return weatherCycle;
    }
    
    /**
     * @param weatherCycle whether weather cycle should be enabled
     *
     * @param weatherCycle следует ли включить цикл погоды
     *
     * @param weatherCycle ob Wetterzyklus aktiviert werden soll
     */
    public void setWeatherCycle(boolean weatherCycle) {
        this.weatherCycle = weatherCycle;
    }
    
    /**
     * @return true if day/night cycle is enabled, false otherwise
     *
     * @return true, если цикл день/ночь включен, false в противном случае
     *
     * @return true, wenn Tag/Nacht-Zyklus aktiviert ist, sonst false
     */
    public boolean isDayNightCycle() {
        return dayNightCycle;
    }
    
    /**
     * @param dayNightCycle whether day/night cycle should be enabled
     *
     * @param dayNightCycle следует ли включить цикл день/ночь
     *
     * @param dayNightCycle ob Tag/Nacht-Zyklus aktiviert werden soll
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