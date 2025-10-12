package com.megacreative.npc;

import com.megacreative.MegaCreative;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Manages custom NPCs in the world
 */
public class NPCManager {
    private final MegaCreative plugin;
    private final Map<UUID, CustomNPC> npcs;
    private final Map<String, CustomNPC> namedNpcs;
    private BukkitTask aiTask;
    
    public NPCManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.npcs = new ConcurrentHashMap<>();
        this.namedNpcs = new ConcurrentHashMap<>();
    }
    
    /**
     * Creates a new custom NPC
     * @param name The name of the NPC
     * @param location The location to spawn the NPC
     * @return The created NPC
     */
    public CustomNPC createNPC(String name, Location location) {
        CustomNPC npc = new CustomNPC(name, location);
        npcs.put(npc.getUniqueId(), npc);
        namedNpcs.put(name.toLowerCase(), npc);
        return npc;
    }
    
    /**
     * Gets an NPC by its unique ID
     * @param uuid The UUID of the NPC
     * @return The NPC, or null if not found
     */
    public CustomNPC getNPC(UUID uuid) {
        return npcs.get(uuid);
    }
    
    /**
     * Gets an NPC by its name
     * @param name The name of the NPC
     * @return The NPC, or null if not found
     */
    public CustomNPC getNPC(String name) {
        return namedNpcs.get(name.toLowerCase());
    }
    
    /**
     * Removes an NPC
     * @param npc The NPC to remove
     */
    public void removeNPC(CustomNPC npc) {
        if (npc != null) {
            npc.despawn();
            npcs.remove(npc.getUniqueId());
            namedNpcs.remove(npc.getName().toLowerCase());
        }
    }
    
    /**
     * Removes an NPC by its unique ID
     * @param uuid The UUID of the NPC to remove
     */
    public void removeNPC(UUID uuid) {
        CustomNPC npc = getNPC(uuid);
        removeNPC(npc);
    }
    
    /**
     * Removes an NPC by its name
     * @param name The name of the NPC to remove
     */
    public void removeNPC(String name) {
        CustomNPC npc = getNPC(name);
        removeNPC(npc);
    }
    
    /**
     * Starts the NPC AI system
     */
    public void startAI() {
        if (aiTask != null && !aiTask.isCancelled()) {
            aiTask.cancel();
        }
        
        aiTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateNPCs();
            }
        }.runTaskTimer(plugin, 0L, 5L); 
    }
    
    /**
     * Stops the NPC AI system
     */
    public void stopAI() {
        if (aiTask != null && !aiTask.isCancelled()) {
            aiTask.cancel();
            aiTask = null;
        }
    }
    
    /**
     * Updates all NPCs
     */
    private void updateNPCs() {
        for (CustomNPC npc : npcs.values()) {
            
            updateNPC(npc);
        }
    }
    
    /**
     * Updates a single NPC
     * @param npc The NPC to update
     */
    private void updateNPC(CustomNPC npc) {
        
        
        
        
        
        
    }
    
    /**
     * Makes all NPCs look at a player
     * @param player The player to look at
     */
    public void lookAtPlayer(Player player) {
        if (player == null) return;
        
        for (CustomNPC npc : npcs.values()) {
            npc.lookAt(player);
        }
    }
    
    /**
     * Cleans up all NPCs when the plugin is disabled
     */
    public void cleanup() {
        stopAI();
        
        for (CustomNPC npc : npcs.values()) {
            npc.despawn();
        }
        
        npcs.clear();
        namedNpcs.clear();
    }
    
    /**
     * Gets all NPCs
     * @return A map of all NPCs
     */
    public Map<UUID, CustomNPC> getAllNPCs() {
        return new ConcurrentHashMap<>(npcs);
    }
}