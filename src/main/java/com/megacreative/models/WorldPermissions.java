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
                return level == PermissionLevel.ADMIN;\n                \n            case \"interact\":\n            case \"use\":\n                return level.ordinal() >= PermissionLevel.PLAYER.ordinal();\n                \n            case \"view\":\n            case \"explore\":\n                return level.ordinal() >= PermissionLevel.VISITOR.ordinal();\n                \n            default:\n                return false;\n        }\n    }\n    \n    /**\n     * Sets permission level for a player\n     */\n    public void setPlayerPermission(UUID playerId, PermissionLevel level) {\n        if (level == null) {\n            playerPermissions.remove(playerId);\n        } else {\n            playerPermissions.put(playerId, level);\n        }\n    }\n    \n    /**\n     * Gets permission level for a player\n     */\n    public PermissionLevel getPlayerPermission(UUID playerId) {\n        return playerPermissions.getOrDefault(playerId, PermissionLevel.VISITOR);\n    }\n    \n    /**\n     * Adds player to whitelist\n     */\n    public void addToWhitelist(UUID playerId) {\n        whitelist.add(playerId);\n        blacklist.remove(playerId); // Remove from blacklist if present\n    }\n    \n    /**\n     * Removes player from whitelist\n     */\n    public void removeFromWhitelist(UUID playerId) {\n        whitelist.remove(playerId);\n    }\n    \n    /**\n     * Adds player to blacklist\n     */\n    public void addToBlacklist(UUID playerId) {\n        blacklist.add(playerId);\n        whitelist.remove(playerId); // Remove from whitelist if present\n        playerPermissions.remove(playerId); // Remove permissions\n    }\n    \n    /**\n     * Removes player from blacklist\n     */\n    public void removeFromBlacklist(UUID playerId) {\n        blacklist.remove(playerId);\n    }\n    \n    // Getters and setters\n    public AccessMode getPlayWorldAccess() { return playWorldAccess; }\n    public void setPlayWorldAccess(AccessMode playWorldAccess) { this.playWorldAccess = playWorldAccess; }\n    \n    public AccessMode getDevWorldAccess() { return devWorldAccess; }\n    public void setDevWorldAccess(AccessMode devWorldAccess) { this.devWorldAccess = devWorldAccess; }\n    \n    public Map<UUID, PermissionLevel> getPlayerPermissions() { return new HashMap<>(playerPermissions); }\n    public Set<UUID> getWhitelist() { return new HashSet<>(whitelist); }\n    public Set<UUID> getBlacklist() { return new HashSet<>(blacklist); }\n    \n    public boolean isAllowPvP() { return allowPvP; }\n    public void setAllowPvP(boolean allowPvP) { this.allowPvP = allowPvP; }\n    \n    public boolean isAllowMobSpawning() { return allowMobSpawning; }\n    public void setAllowMobSpawning(boolean allowMobSpawning) { this.allowMobSpawning = allowMobSpawning; }\n    \n    public boolean isAllowExplosions() { return allowExplosions; }\n    public void setAllowExplosions(boolean allowExplosions) { this.allowExplosions = allowExplosions; }\n    \n    public boolean isAllowFlightInPlay() { return allowFlightInPlay; }\n    public void setAllowFlightInPlay(boolean allowFlightInPlay) { this.allowFlightInPlay = allowFlightInPlay; }\n    \n    public boolean isAllowItemDrops() { return allowItemDrops; }\n    public void setAllowItemDrops(boolean allowItemDrops) { this.allowItemDrops = allowItemDrops; }\n    \n    public boolean isProtectFromGriefing() { return protectFromGriefing; }\n    public void setProtectFromGriefing(boolean protectFromGriefing) { this.protectFromGriefing = protectFromGriefing; }\n    \n    /**\n     * Gets summary of permissions for display\n     */\n    public Map<String, Object> getPermissionsSummary() {\n        Map<String, Object> summary = new HashMap<>();\n        summary.put(\"playAccess\", playWorldAccess.getDisplayName());\n        summary.put(\"devAccess\", devWorldAccess.getDisplayName());\n        summary.put(\"trustedPlayers\", playerPermissions.size());\n        summary.put(\"whitelistedPlayers\", whitelist.size());\n        summary.put(\"blacklistedPlayers\", blacklist.size());\n        summary.put(\"pvpEnabled\", allowPvP);\n        summary.put(\"griefProtection\", protectFromGriefing);\n        return summary;\n    }\n}\n