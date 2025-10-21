package com.megacreative.coding.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.CodeScript;
import com.megacreative.interfaces.ICodingManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Advanced region detection system for event handling
 * Provides sophisticated region-based event filtering and triggering
 */
public class RegionDetectionSystem {
    private static final Logger log = Logger.getLogger(RegionDetectionSystem.class.getName());
    
    private final MegaCreative plugin;
    private final CustomEventManager eventManager;
    
    
    private final Map<String, Region> regions = new ConcurrentHashMap<>();
    
    
    private final Map<UUID, Set<String>> playerRegions = new ConcurrentHashMap<>();
    
    
    private final Map<String, List<RegionEventHandler>> regionEventHandlers = new ConcurrentHashMap<>();
    
    
    private final Map<String, List<Region>> worldRegions = new ConcurrentHashMap<>();
    
    public RegionDetectionSystem(MegaCreative plugin, CustomEventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
    }
    
    /**
     * Defines a new region
     */
    public void defineRegion(String regionId, String worldName, Location minPoint, Location maxPoint, 
                           String description, Map<String, Object> metadata) {
        Region region = new Region(regionId, worldName, minPoint, maxPoint, description, metadata);
        regions.put(regionId, region);
        
        
        worldRegions.computeIfAbsent(worldName, k -> new ArrayList<>()).add(region);
        
        log.info("Defined region: " + regionId + " in world " + worldName);
    }
    
    /**
     * Defines a circular region
     */
    public void defineCircularRegion(String regionId, String worldName, Location center, double radius,
                                   String description, Map<String, Object> metadata) {
        Region region = new Region(regionId, worldName, center, radius, description, metadata);
        regions.put(regionId, region);
        
        
        worldRegions.computeIfAbsent(worldName, k -> new ArrayList<>()).add(region);
        
        log.info("Defined circular region: " + regionId + " in world " + worldName);
    }
    
    /**
     * Removes a region definition
     */
    public void removeRegion(String regionId) {
        Region region = regions.remove(regionId);
        if (region != null) {
            
            List<Region> worldRegionList = worldRegions.get(region.getWorldName());
            if (worldRegionList != null) {
                worldRegionList.remove(region);
                if (worldRegionList.isEmpty()) {
                    worldRegions.remove(region.getWorldName());
                }
            }
            
            
            for (Set<String> playerRegionSet : playerRegions.values()) {
                playerRegionSet.remove(regionId);
            }
            
            log.info("Removed region: " + regionId);
        }
    }
    
    /**
     * Gets all regions a player is currently in
     */
    public Set<String> getPlayerRegions(Player player) {
        return new HashSet<>(playerRegions.getOrDefault(player.getUniqueId(), new HashSet<>()));
    }
    
    /**
     * Checks if a player is in a specific region
     */
    public boolean isPlayerInRegion(Player player, String regionId) {
        Set<String> regions = playerRegions.get(player.getUniqueId());
        return regions != null && regions.contains(regionId);
    }
    
    /**
     * Gets all regions in a world at a specific location
     */
    public List<String> getRegionsAtLocation(String worldName, Location location) {
        List<String> result = new ArrayList<>();
        List<Region> worldRegionList = worldRegions.get(worldName);
        
        if (worldRegionList != null) {
            for (Region region : worldRegionList) {
                if (region.contains(location)) {
                    result.add(region.getId());
                }
            }
        }
        
        return result;
    }
    
    /**
     * Updates a player's region tracking
     */
    public void updatePlayerRegions(Player player) {
        Location location = player.getLocation();
        String worldName = player.getWorld().getName();
        Set<String> currentRegions = new HashSet<>();
        
        
        List<Region> worldRegionList = worldRegions.get(worldName);
        if (worldRegionList != null) {
            for (Region region : worldRegionList) {
                if (region.contains(location)) {
                    currentRegions.add(region.getId());
                }
            }
        }
        
        
        Set<String> previousRegions = playerRegions.getOrDefault(player.getUniqueId(), new HashSet<>());
        
        
        for (String regionId : currentRegions) {
            if (!previousRegions.contains(regionId)) {
                
                triggerRegionEnterEvent(player, regionId);
            }
        }
        
        for (String regionId : previousRegions) {
            if (!currentRegions.contains(regionId)) {
                
                triggerRegionExitEvent(player, regionId);
            }
        }
        
        
        playerRegions.put(player.getUniqueId(), currentRegions);
    }
    
    /**
     * Triggers a region enter event
     */
    private void triggerRegionEnterEvent(Player player, String regionId) {
        Region region = regions.get(regionId);
        if (region != null) {
            Map<String, DataValue> eventData = new HashMap<>();
            eventData.put("player", DataValue.fromObject(player));
            eventData.put("regionId", DataValue.fromObject(regionId));
            eventData.put("regionName", DataValue.fromObject(region.getDescription()));
            eventData.put("world", DataValue.fromObject(region.getWorldName()));
            
            
            if (region.getMetadata() != null) {
                for (Map.Entry<String, Object> entry : region.getMetadata().entrySet()) {
                    eventData.put("metadata_" + entry.getKey(), DataValue.fromObject(entry.getValue()));
                }
            }
            
            try {
                eventManager.triggerEvent("regionEnter", eventData, player, region.getWorldName());
                log.fine("Triggered regionEnter event for player " + player.getName() + " in region " + regionId);
            } catch (Exception e) {
                log.warning("Failed to trigger regionEnter event: " + e.getMessage());
            }
        }
    }
    
    /**
     * Triggers a region exit event
     */
    private void triggerRegionExitEvent(Player player, String regionId) {
        Region region = regions.get(regionId);
        if (region != null) {
            Map<String, DataValue> eventData = new HashMap<>();
            eventData.put("player", DataValue.fromObject(player));
            eventData.put("regionId", DataValue.fromObject(regionId));
            eventData.put("regionName", DataValue.fromObject(region.getDescription()));
            eventData.put("world", DataValue.fromObject(region.getWorldName()));
            
            
            if (region.getMetadata() != null) {
                for (Map.Entry<String, Object> entry : region.getMetadata().entrySet()) {
                    eventData.put("metadata_" + entry.getKey(), DataValue.fromObject(entry.getValue()));
                }
            }
            
            try {
                eventManager.triggerEvent("regionExit", eventData, player, region.getWorldName());
                log.fine("Triggered regionExit event for player " + player.getName() + " in region " + regionId);
            } catch (Exception e) {
                log.warning("Failed to trigger regionExit event: " + e.getMessage());
            }
        }
    }
    
    /**
     * Registers a region event handler
     */
    public void registerRegionEventHandler(String regionId, RegionEventHandler handler) {
        regionEventHandlers.computeIfAbsent(regionId, k -> new ArrayList<>()).add(handler);
        log.fine("Registered region event handler for region: " + regionId);
    }
    
    /**
     * Unregisters a region event handler
     */
    public void unregisterRegionEventHandler(String regionId, RegionEventHandler handler) {
        List<RegionEventHandler> handlers = regionEventHandlers.get(regionId);
        if (handlers != null) {
            handlers.remove(handler);
            if (handlers.isEmpty()) {
                regionEventHandlers.remove(regionId);
            }
        }
    }
    
    /**
     * Creates a new region event handler
     */
    public RegionEventHandler createRegionEventHandler(String eventName, java.util.function.Predicate<Map<String, DataValue>> condition, int priority) {
        return new RegionEventHandler(eventName, condition, priority, plugin);
    }
    
    /**
     * Gets all defined regions
     */
    public Collection<Region> getAllRegions() {
        return new ArrayList<>(regions.values());
    }
    
    /**
     * Gets a region by ID
     */
    public Region getRegion(String regionId) {
        return regions.get(regionId);
    }
    
    /**
     * Gets regions by metadata
     */
    public List<Region> getRegionsByMetadata(String key, Object value) {
        List<Region> result = new ArrayList<>();
        for (Region region : regions.values()) {
            Object metadataValue = region.getMetadata().get(key);
            if (metadataValue != null && metadataValue.equals(value)) {
                result.add(region);
            }
        }
        return result;
    }
    
    /**
     * Cleans up player tracking when they leave
     */
    public void cleanupPlayerTracking(UUID playerId) {
        playerRegions.remove(playerId);
    }
    
    /**
     * Associates a script with a specific region and event
     * @param regionId The region ID
     * @param eventName The event name (regionEnter, regionExit, etc.)
     * @param script The script to associate
     */
    public void setRegionScript(String regionId, String eventName, CodeScript script) {
        
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry == null) return;
        
        ICodingManager codingManager = serviceRegistry.getService(ICodingManager.class);
        if (codingManager == null) return;
        
        
        String scriptName = "region_" + regionId + "_" + eventName;
        script.setName(scriptName);
        
        
        codingManager.saveScript(script);
        
        log.info("Set region script: " + scriptName);
    }
    
    /**
     * Gets a script associated with a specific region and event
     * @param regionId The region ID
     * @param eventName The event name (regionEnter, regionExit, etc.)
     * @return The associated script, or null if none exists
     */
    public CodeScript getRegionScript(String regionId, String eventName) {
        
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry == null) return null;
        
        ICodingManager codingManager = serviceRegistry.getService(ICodingManager.class);
        if (codingManager == null) return null;
        
        
        String scriptName = "region_" + regionId + "_" + eventName;
        return codingManager.getScript(scriptName);
    }
    
    /**
     * Removes a script associated with a specific region and event
     * @param regionId The region ID
     * @param eventName The event name (regionEnter, regionExit, etc.)
     */
    public void removeRegionScript(String regionId, String eventName) {
        
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry == null) return;
        
        ICodingManager codingManager = serviceRegistry.getService(ICodingManager.class);
        if (codingManager == null) return;
        
        
        String scriptName = "region_" + regionId + "_" + eventName;
        codingManager.deleteScript(scriptName);
        
        log.info("Removed region script: " + scriptName);
    }
    
    /**
     * Sets a generic script for all regions of a specific event type
     * @param eventName The event name (regionEnter, regionExit, etc.)
     * @param script The script to associate
     */
    public void setGenericRegionScript(String eventName, CodeScript script) {
        
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry == null) return;
        
        ICodingManager codingManager = serviceRegistry.getService(ICodingManager.class);
        if (codingManager == null) return;
        
        
        String scriptName = "region_" + eventName;
        script.setName(scriptName);
        
        
        codingManager.saveScript(script);
        
        log.info("Set generic region script: " + scriptName);
    }
    
    /**
     * Gets a generic script for all regions of a specific event type
     * @param eventName The event name (regionEnter, regionExit, etc.)
     * @return The associated script, or null if none exists
     */
    public CodeScript getGenericRegionScript(String eventName) {
        
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry == null) return null;
        
        ICodingManager codingManager = serviceRegistry.getService(ICodingManager.class);
        if (codingManager == null) return null;
        
        
        String scriptName = "region_" + eventName;
        return codingManager.getScript(scriptName);
    }
    
    /**
     * Represents a defined region in the world
     */
    public static class Region {
        private final String id;
        private final String worldName;
        private final Location minPoint;
        private final Location maxPoint;
        private final Location center;
        private final double radius;
        private final String description;
        private final Map<String, Object> metadata;
        private final boolean isCircular;
        
        
        public Region(String id, String worldName, Location minPoint, Location maxPoint, 
                     String description, Map<String, Object> metadata) {
            this.id = id;
            this.worldName = worldName;
            this.minPoint = new Location(minPoint.getWorld(), 
                Math.min(minPoint.getX(), maxPoint.getX()),
                Math.min(minPoint.getY(), maxPoint.getY()),
                Math.min(minPoint.getZ(), maxPoint.getZ()));
            this.maxPoint = new Location(maxPoint.getWorld(),
                Math.max(minPoint.getX(), maxPoint.getX()),
                Math.max(minPoint.getY(), maxPoint.getY()),
                Math.max(minPoint.getZ(), maxPoint.getZ()));
            this.center = null;
            this.radius = 0;
            this.description = description != null ? description : id;
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            this.isCircular = false;
        }
        
        
        public Region(String id, String worldName, Location center, double radius,
                     String description, Map<String, Object> metadata) {
            this.id = id;
            this.worldName = worldName;
            this.minPoint = null;
            this.maxPoint = null;
            this.center = center.clone();
            this.radius = radius;
            this.description = description != null ? description : id;
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            this.isCircular = true;
        }
        
        /**
         * Checks if a location is within this region
         */
        public boolean contains(Location location) {
            
            if (!location.getWorld().getName().equals(worldName)) {
                return false;
            }
            
            if (isCircular) {
                
                return location.distance(center) <= radius;
            } else {
                
                return location.getX() >= minPoint.getX() && location.getX() <= maxPoint.getX() &&
                       location.getY() >= minPoint.getY() && location.getY() <= maxPoint.getY() &&
                       location.getZ() >= minPoint.getZ() && location.getZ() <= maxPoint.getZ();
            }
        }
        
        
        public String getId() { return id; }
        public String getWorldName() { return worldName; }
        public Location getMinPoint() { return isCircular ? null : minPoint.clone(); }
        public Location getMaxPoint() { return isCircular ? null : maxPoint.clone(); }
        public Location getCenter() { return isCircular ? center.clone() : null; }
        public double getRadius() { return isCircular ? radius : 0; }
        public String getDescription() { return description; }
        public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
        public boolean isCircular() { return isCircular; }
        
        @Override
        public String toString() {
            if (isCircular) {
                return "Region{id='" + id + "', world='" + worldName + "', center=" + center + ", radius=" + radius + "}";
            } else {
                return "Region{id='" + id + "', world='" + worldName + "', min=" + minPoint + ", max=" + maxPoint + "}";
            }
        }
    }
    
    /**
     * Handler for region-specific events
     */
    public static class RegionEventHandler {
        private final String eventName;
        private final java.util.function.Predicate<Map<String, DataValue>> condition;
        private final int priority;
        private final MegaCreative plugin; 
        
        public RegionEventHandler(String eventName, java.util.function.Predicate<Map<String, DataValue>> condition, int priority, MegaCreative plugin) {
            this.eventName = eventName;
            this.condition = condition;
            this.priority = priority;
            this.plugin = plugin; 
        }
        
        public boolean canHandle(Map<String, DataValue> eventData) {
            return condition == null || condition.test(eventData);
        }
        
        public void handle(Map<String, DataValue> eventData, Player player, String worldName) {
            
            MegaCreative plugin = this.plugin;
            if (plugin == null) return;
            
            
            ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
            if (serviceRegistry == null) return;
            
            ScriptEngine scriptEngine = serviceRegistry.getService(ScriptEngine.class);
            if (scriptEngine == null) return;
            
            
            ExecutionContext context = new ExecutionContext(
                plugin,
                player,
                null, 
                null, 
                null, 
                null  
            );
            
            
            if (eventData != null) {
                for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue().getValue());
                }
            }
            
            
            
            String regionId = null;
            if (eventData != null && eventData.get("regionId") != null) {
                regionId = eventData.get("regionId").asString();
            }
            if (regionId != null) {
                try {
                    
                    ICodingManager codingManager = plugin.getServiceRegistry().getService(ICodingManager.class);
                    if (codingManager != null) {
                        
                        String scriptName = "region_" + regionId + "_" + eventName;
                        CodeScript script = codingManager.getScript(scriptName);
                        
                        if (script != null && script.isEnabled()) {
                            plugin.getLogger().info("Executing region script: " + scriptName + " for player " + player.getName());
                            
                            
                            scriptEngine.executeScript(script, player, "region_event")
                                .thenAccept(result -> {
                                    if (!result.isSuccess()) {
                                        plugin.getLogger().warning("Region script execution failed: " + result.getMessage());
                                    }
                                })
                                .exceptionally(throwable -> {
                                    plugin.getLogger().warning("Error executing region script: " + throwable.getMessage());
                                    return null;
                                });
                        } else {
                            
                            String genericScriptName = "region_" + eventName;
                            CodeScript genericScript = codingManager.getScript(genericScriptName);
                            
                            if (genericScript != null && genericScript.isEnabled()) {
                                plugin.getLogger().info("Executing generic region script: " + genericScriptName + " for player " + player.getName() + " in region " + regionId);
                                
                                
                                scriptEngine.executeScript(genericScript, player, "region_event")
                                    .thenAccept(result -> {
                                        if (!result.isSuccess()) {
                                            plugin.getLogger().warning("Generic region script execution failed: " + result.getMessage());
                                        }
                                    })
                                    .exceptionally(throwable -> {
                                        plugin.getLogger().warning("Error executing generic region script: " + throwable.getMessage());
                                        return null;
                                    });
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(java.util.logging.Level.WARNING, "Failed to handle region event", e);
                }
            } else {
                plugin.getLogger().warning("Region event handler called without region ID");
            }
        }
        
        /**
         * Executes a region-specific script
         * This method is kept for backward compatibility but the main logic is now in the handle method
         */
        private void executeRegionScript(ScriptEngine scriptEngine, String scriptId, ExecutionContext context, Player player, String regionId) {
            
            MegaCreative plugin = this.plugin; 
            if (plugin == null) return;
            
            plugin.getLogger().info("Executing script " + scriptId + " for player " + player.getName() + " in region " + regionId);
            
            
            CodeScript script = null;
            
            // Remove the redundant check and directly get the script
            CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null) {
                script = creativeWorld.getScripts().stream()
                        .filter(s -> s.getName().equals(scriptId))
                        .findFirst()
                        .orElse(null);
            }
            
            if (script != null) {
                
                scriptEngine.executeScript(script, player, "region_event")
                        .thenAccept(result -> {
                            if (result.isSuccess()) {
                                plugin.getLogger().info("Successfully executed region script " + scriptId + " for player " + player.getName());
                            } else {
                                plugin.getLogger().warning("Failed to execute region script " + scriptId + ": " + result.getMessage());
                                player.sendMessage("§cError executing region script: " + result.getMessage());
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning("Error executing region script " + scriptId + ": " + throwable.getMessage());
                            player.sendMessage("§cError executing region script");
                            return null;
                        });
            } else {
                
                switch (eventName) {
                    case "regionEnter":
                        
                        player.sendMessage("§aWelcome to region: " + regionId);
                        break;
                    case "regionExit":
                        
                        player.sendMessage("§cLeaving region: " + regionId);
                        break;
                    default:
                        
                        player.sendMessage("§eRegion event triggered: " + eventName + " in " + regionId);
                        break;
                }
            }
        }
        
        public String getEventName() { return eventName; }
        public java.util.function.Predicate<Map<String, DataValue>> getCondition() { return condition; }
        public int getPriority() { return priority; }
    }
}