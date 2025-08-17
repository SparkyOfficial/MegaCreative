package com.megacreative.models;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class WorldFlags implements ConfigurationSerializable {
    public boolean isMobSpawning() {
        return mobSpawning;
    }

    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public boolean isExplosions() {
        return explosions;
    }

    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public void setFireSpread(boolean fireSpread) {
        this.fireSpread = fireSpread;
    }

    public boolean isMobGriefing() {
        return mobGriefing;
    }

    public void setMobGriefing(boolean mobGriefing) {
        this.mobGriefing = mobGriefing;
    }

    public boolean isWeatherCycle() {
        return weatherCycle;
    }

    public void setWeatherCycle(boolean weatherCycle) {
        this.weatherCycle = weatherCycle;
    }

    public boolean isDayNightCycle() {
        return dayNightCycle;
    }

    public void setDayNightCycle(boolean dayNightCycle) {
        this.dayNightCycle = dayNightCycle;
    }
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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("mobSpawning", mobSpawning);
        map.put("pvp", pvp);
        map.put("explosions", explosions);
        map.put("fireSpread", fireSpread);
        map.put("mobGriefing", mobGriefing);
        map.put("weatherCycle", weatherCycle);
        map.put("dayNightCycle", dayNightCycle);
        return map;
    }

    public static WorldFlags deserialize(Map<String, Object> map) {
        WorldFlags flags = new WorldFlags();
        flags.setMobSpawning((Boolean) map.getOrDefault("mobSpawning", true));
        flags.setPvp((Boolean) map.getOrDefault("pvp", false));
        flags.setExplosions((Boolean) map.getOrDefault("explosions", false));
        flags.setFireSpread((Boolean) map.getOrDefault("fireSpread", false));
        flags.setMobGriefing((Boolean) map.getOrDefault("mobGriefing", false));
        flags.setWeatherCycle((Boolean) map.getOrDefault("weatherCycle", true));
        flags.setDayNightCycle((Boolean) map.getOrDefault("dayNightCycle", true));
        return flags;
    }
}
