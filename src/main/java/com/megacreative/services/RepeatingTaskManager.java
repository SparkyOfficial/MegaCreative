package com.megacreative.services;

import com.megacreative.MegaCreative;
import com.megacreative.core.DependencyContainer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Service for managing repeating tasks in the plugin
 * Replaces static fields in actions with proper service-based management
 */
public class RepeatingTaskManager implements DependencyContainer.Disposable {
    
    private static final Logger LOGGER = Logger.getLogger(RepeatingTaskManager.class.getName());
    
    private final MegaCreative plugin;
    private final Map<UUID, Integer> activeTasks = new ConcurrentHashMap<>();
    
    public RepeatingTaskManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Starts a repeating task for a player
     * @param playerId The player UUID
     * @param task The Bukkit task to start
     * @return The task ID
     */
    public int startRepeatingTask(UUID playerId, BukkitTask task) {
        
        stopRepeatingTask(playerId);
        
        
        activeTasks.put(playerId, task.getTaskId());
        LOGGER.fine("Started repeating task " + task.getTaskId() + " for player " + playerId);
        
        return task.getTaskId();
    }
    
    /**
     * Stops a repeating task for a player
     * @param playerId The player UUID
     */
    public void stopRepeatingTask(UUID playerId) {
        Integer taskId = activeTasks.get(playerId);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            activeTasks.remove(playerId);
            LOGGER.fine("Stopped repeating task " + taskId + " for player " + playerId);
        }
    }
    
    /**
     * Stops all repeating tasks
     * @return Number of tasks stopped
     */
    public int stopAllRepeatingTasks() {
        int count = activeTasks.size();
        for (Integer taskId : activeTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        activeTasks.clear();
        LOGGER.fine("Stopped " + count + " repeating tasks");
        return count;
    }
    
    /**
     * Checks if a player has an active repeating task
     * @param playerId The player UUID
     * @return true if the player has an active task
     */
    public boolean hasActiveTask(UUID playerId) {
        return activeTasks.containsKey(playerId);
    }
    
    /**
     * Gets the task ID for a player
     * @param playerId The player UUID
     * @return The task ID or null if no active task
     */
    public Integer getTaskId(UUID playerId) {
        return activeTasks.get(playerId);
    }
    
    /**
     * Shutdown the manager and stop all tasks
     */
    public void shutdown() {
        stopAllRepeatingTasks();
        LOGGER.fine("RepeatingTaskManager shutdown completed");
    }
    
    @Override
    public void dispose() {
        shutdown();
    }
}