package com.megacreative.models;

import org.bukkit.entity.Player;

import java.util.*;

/**
 * 🎆 ENHANCED: FrameLand-style world permissions with whitelist/blacklist functionality
 */
public class WorldPermissions {
    
    // Permission modes
    public enum AccessMode {
        PRIVATE("§c🔒 Private", "Only trusted players can access"),
        FRIENDS_ONLY("§e👥 Friends Only", "Only trusted builders and coders can access"),
        PUBLIC("§a🌍 Public", "Everyone can access"),
        WHITELIST("§f📝 Whitelist", "Only whitelisted players can access"),
        BLACKLIST("§7🚫 Blacklist", "Everyone except blacklisted players can access");
        
        private final String displayName;
        private final String description;
        
        AccessMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    // Permission levels for different world modes
    public enum PermissionLevel {
        VISITOR("§7👁 Visitor", "Can view and explore"),
        PLAYER("§f🎮 Player", "Can interact and play"),
        BUILDER("§6🔨 Builder", "Can build and modify"),
        CODER("§e💻 Coder", "Can code and script"),
        ADMIN("§c⚡ Admin", "Full world control");
        
        private final String displayName;
        private final String description;
        
        PermissionLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    // Access modes for different world types
    private AccessMode playWorldAccess = AccessMode.PUBLIC;
    private AccessMode devWorldAccess = AccessMode.FRIENDS_ONLY;
    
    // Permission lists
    private final Map<UUID, PermissionLevel> playerPermissions = new HashMap<>();
    private final Set<UUID> whitelist = new HashSet<>();
    private final Set<UUID> blacklist = new HashSet<>();
    
    // World-specific restrictions
    private boolean allowPvP = false;
    private boolean allowMobSpawning = false;
    private boolean allowExplosions = false;
    private boolean allowFlightInPlay = false;
    private boolean allowItemDrops = true;
    private boolean protectFromGriefing = true;
    
    // Default constructor
    public WorldPermissions() {
        // Default settings for new worlds
    }
    
    /**
     * Checks if a player can access the world in the specified mode
     */
    public boolean canAccess(Player player, CreativeWorld.WorldDualMode worldMode) {
        UUID playerId = player.getUniqueId();
        AccessMode accessMode = worldMode == CreativeWorld.WorldDualMode.DEV ? devWorldAccess : playWorldAccess;
        
        // Always allow if player has permission override
        if (player.hasPermission("megacreative.world.bypass")) {
            return true;
        }
        
        // Check blacklist first
        if (blacklist.contains(playerId)) {
            return false;
        }
        
        switch (accessMode) {
            case PRIVATE:
                return playerPermissions.containsKey(playerId);
                
            case FRIENDS_ONLY:
                PermissionLevel level = playerPermissions.get(playerId);
                return level != null && (level == PermissionLevel.BUILDER || 
                                       level == PermissionLevel.CODER || 
                                       level == PermissionLevel.ADMIN);
                
            case PUBLIC:
                return true;
                
            case WHITELIST:
                return whitelist.contains(playerId);
                
            case BLACKLIST:
                return !blacklist.contains(playerId);
                
            default:
                return false;
        }
    }
    
    /**
     * Checks if a player can perform a specific action
     */
    public boolean canPerform(Player player, String action, CreativeWorld.WorldDualMode worldMode) {
        UUID playerId = player.getUniqueId();
        PermissionLevel level = playerPermissions.getOrDefault(playerId, PermissionLevel.VISITOR);
        
        // Always allow if player has permission override
        if (player.hasPermission("megacreative.world.bypass")) {
            return true;
        }
        
        switch (action.toLowerCase()) {
            case "build":
            case "break":
            case "place":
                return level.ordinal() >= PermissionLevel.BUILDER.ordinal();
                
            case "code":
            case "script":
            case "dev":
                return level.ordinal() >= PermissionLevel.CODER.ordinal() && 
                       worldMode == CreativeWorld.WorldDualMode.DEV;
                
            case "admin":
            case "settings":
            case "permissions":
                return level == PermissionLevel.ADMIN;
                
            case "interact":
            case "use":
                return level.ordinal() >= PermissionLevel.PLAYER.ordinal();
                
            case "view":
            case "explore":
                return level.ordinal() >= PermissionLevel.VISITOR.ordinal();
                
            default:
                return false;
        }
    }
    
    /**
     * Sets permission level for a player
     */
    public void setPlayerPermission(UUID playerId, PermissionLevel level) {
        if (level == null) {
            playerPermissions.remove(playerId);
        } else {
            playerPermissions.put(playerId, level);
        }
    }
    
    /**
     * Gets permission level for a player
     */
    public PermissionLevel getPlayerPermission(UUID playerId) {
        return playerPermissions.getOrDefault(playerId, PermissionLevel.VISITOR);
    }
    
    /**
     * Adds player to whitelist
     */
    public void addToWhitelist(UUID playerId) {
        whitelist.add(playerId);
        blacklist.remove(playerId); // Remove from blacklist if present
    }
    
    /**
     * Removes player from whitelist
     */
    public void removeFromWhitelist(UUID playerId) {
        whitelist.remove(playerId);
    }
    
    /**
     * Adds player to blacklist
     */
    public void addToBlacklist(UUID playerId) {
        blacklist.add(playerId);
        whitelist.remove(playerId); // Remove from whitelist if present
        playerPermissions.remove(playerId); // Remove permissions
    }
    
    /**
     * Removes player from blacklist
     */
    public void removeFromBlacklist(UUID playerId) {
        blacklist.remove(playerId);
    }
    
    // Getters and setters
    public AccessMode getPlayWorldAccess() { return playWorldAccess; }
    public void setPlayWorldAccess(AccessMode playWorldAccess) { this.playWorldAccess = playWorldAccess; }
    
    public AccessMode getDevWorldAccess() { return devWorldAccess; }
    public void setDevWorldAccess(AccessMode devWorldAccess) { this.devWorldAccess = devWorldAccess; }
    
    public Map<UUID, PermissionLevel> getPlayerPermissions() { return new HashMap<>(playerPermissions); }
    public Set<UUID> getWhitelist() { return new HashSet<>(whitelist); }
    public Set<UUID> getBlacklist() { return new HashSet<>(blacklist); }
    
    public boolean isAllowPvP() { return allowPvP; }
    public void setAllowPvP(boolean allowPvP) { this.allowPvP = allowPvP; }
    
    public boolean isAllowMobSpawning() { return allowMobSpawning; }
    public void setAllowMobSpawning(boolean allowMobSpawning) { this.allowMobSpawning = allowMobSpawning; }
    
    public boolean isAllowExplosions() { return allowExplosions; }
    public void setAllowExplosions(boolean allowExplosions) { this.allowExplosions = allowExplosions; }
    
    public boolean isAllowFlightInPlay() { return allowFlightInPlay; }
    public void setAllowFlightInPlay(boolean allowFlightInPlay) { this.allowFlightInPlay = allowFlightInPlay; }
    
    public boolean isAllowItemDrops() { return allowItemDrops; }
    public void setAllowItemDrops(boolean allowItemDrops) { this.allowItemDrops = allowItemDrops; }
    
    public boolean isProtectFromGriefing() { return protectFromGriefing; }
    public void setProtectFromGriefing(boolean protectFromGriefing) { this.protectFromGriefing = protectFromGriefing; }
    
    /**
     * Gets summary of permissions for display
     */
    public Map<String, Object> getPermissionsSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("playAccess", playWorldAccess.getDisplayName());
        summary.put("devAccess", devWorldAccess.getDisplayName());
        summary.put("trustedPlayers", playerPermissions.size());
        summary.put("whitelistedPlayers", whitelist.size());
        summary.put("blacklistedPlayers", blacklist.size());
        summary.put("pvpEnabled", allowPvP);
        summary.put("griefProtection", protectFromGriefing);
        return summary;
    }
}
