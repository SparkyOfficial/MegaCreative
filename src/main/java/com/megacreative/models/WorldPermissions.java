package com.megacreative.models;

import org.bukkit.entity.Player;

import java.util.*;

/**
 * 🎆 ENHANCED: Reference system-style world permissions with whitelist/blacklist functionality
 *
 * 🎆 ENHANCED: Reference system-style: Разрешения мира с функциональностью белого/черного списка
 *
 * 🎆 ENHANCED: Reference system-style: Weltenberechtigungen mit Whitelist-/Blacklist-Funktionalität
 */
public class WorldPermissions {
    
    /**
     * Permission modes
     *
     * Режимы разрешений
     *
     * Berechtigungsmodi
     */
    public enum AccessMode {
        /**
         * Private access mode
         *
         * Режим приватного доступа
         *
         * Privater Zugriffsmodus
         */
        PRIVATE("§c🔒 Private", "Only trusted players can access"),
        
        
        /**
         * Friends only access mode
         *
         * Режим доступа только для друзей
         *
         * Nur-Freunde-Zugriffsmodus
         */
        FRIENDS_ONLY("§e👥 Friends Only", "Only trusted builders and coders can access"),
        
        
        /**
         * Public access mode
         *
         * Режим публичного доступа
         *
         * Öffentlicher Zugriffsmodus
         */
        PUBLIC("§a🌍 Public", "Everyone can access"),
        
        
        /**
         * Whitelist access mode
         *
         * Режим доступа по белому списку
         *
         * Whitelist-Zugriffsmodus
         */
        WHITELIST("§f📝 Whitelist", "Only whitelisted players can access"),
        
        
        /**
         * Blacklist access mode
         *
         * Режим доступа по черному списку
         *
         * Blacklist-Zugriffsmodus
         */
        BLACKLIST("§7🚫 Blacklist", "Everyone except blacklisted players can access");
        
        
        
        private final String displayName;
        private final String description;
        
        /**
         * Creates an AccessMode
         * @param displayName Display name
         * @param description Description
         *
         * Создает AccessMode
         * @param displayName Отображаемое имя
         * @param description Описание
         *
         * Erstellt einen AccessMode
         * @param displayName Anzeigename
         * @param description Beschreibung
         */
        AccessMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        /**
         * Gets the display name
         * @return Display name
         *
         * Получает отображаемое имя
         * @return Отображаемое имя
         *
         * Ruft den Anzeigenamen ab
         * @return Anzeigename
         */
        public String getDisplayName() { return displayName; }
        
        /**
         * Gets the description
         * @return Description
         *
         * Получает описание
         * @return Описание
         *
         * Ruft die Beschreibung ab
         * @return Beschreibung
         */
        public String getDescription() { return description; }
    }
    
    /**
     * Permission levels for different world modes
     *
     * Уровни разрешений для разных режимов мира
     *
     * Berechtigungsebenen für verschiedene Weltenmodi
     */
    public enum PermissionLevel {
        /**
         * Visitor permission level
         *
         * Уровень разрешений посетителя
         *
         * Besucher-Berechtigungsebene
         */
        VISITOR("§7👁 Visitor", "Can view and explore"),
        
        
        /**
         * Player permission level
         *
         * Уровень разрешений игрока
         *
         * Spieler-Berechtigungsebene
         */
        PLAYER("§f🎮 Player", "Can interact and play"),
        
        
        /**
         * Builder permission level
         *
         * Уровень разрешений строителя
         *
         * Baumeister-Berechtigungsebene
         */
        BUILDER("§6🔨 Builder", "Can build and modify"),
        
        
        /**
         * Coder permission level
         *
         * Уровень разрешений программиста
         *
         * Coder-Berechtigungsebene
         */
        CODER("§e💻 Coder", "Can code and script"),
        
        
        /**
         * Admin permission level
         *
         * Уровень разрешений администратора
         *
         * Admin-Berechtigungsebene
         */
        ADMIN("§c⚡ Admin", "Full world control");
        
        
        
        private final String displayName;
        private final String description;
        
        /**
         * Creates a PermissionLevel
         * @param displayName Display name
         * @param description Description
         *
         * Создает PermissionLevel
         * @param displayName Отображаемое имя
         * @param description Описание
         *
         * Erstellt eine PermissionLevel
         * @param displayName Anzeigename
         * @param description Beschreibung
         */
        PermissionLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        /**
         * Gets the display name
         * @return Display name
         *
         * Получает отображаемое имя
         * @return Отображаемое имя
         *
         * Ruft den Anzeigenamen ab
         * @return Anzeigename
         */
        public String getDisplayName() { return displayName; }
        
        /**
         * Gets the description
         * @return Description
         *
         * Получает описание
         * @return Описание
         *
         * Ruft die Beschreibung ab
         * @return Beschreibung
         */
        public String getDescription() { return description; }
    }
    
    /**
     * Access modes for different world types
     *
     * Режимы доступа для разных типов миров
     *
     * Zugriffsmodi für verschiedene Welttypen
     */
    private AccessMode playWorldAccess = AccessMode.PUBLIC;
    private AccessMode devWorldAccess = AccessMode.FRIENDS_ONLY;
    
    /**
     * Permission lists
     *
     * Списки разрешений
     *
     * Berechtigungslisten
     */
    private final Map<UUID, PermissionLevel> playerPermissions = new HashMap<>();
    private final Set<UUID> whitelist = new HashSet<>();
    private final Set<UUID> blacklist = new HashSet<>();
    
    /**
     * World-specific restrictions
     *
     * Ограничения, специфичные для мира
     *
     * Weltspezifische Einschränkungen
     */
    private boolean allowPvP = false;
    private boolean allowMobSpawning = false;
    private boolean allowExplosions = false;
    private boolean allowFlightInPlay = false;
    private boolean allowItemDrops = true;
    private boolean protectFromGriefing = true;
    
    /**
     * Default constructor
     *
     * Конструктор по умолчанию
     *
     * Standardkonstruktor
     */
    public WorldPermissions() {
        
        
        
    }
    
    /**
     * Checks if a player can access the world in the specified mode
     *
     * Проверяет, может ли игрок получить доступ к миру в указанном режиме
     *
     * Prüft, ob ein Spieler auf die Welt im angegebenen Modus zugreifen kann
     */
    public boolean canAccess(Player player, CreativeWorld.WorldDualMode worldMode) {
        UUID playerId = player.getUniqueId();
        AccessMode accessMode = worldMode == CreativeWorld.WorldDualMode.DEV ? devWorldAccess : playWorldAccess;
        
        
        
        
        if (player.hasPermission("megacreative.world.bypass")) {
            return true;
        }
        
        
        
        
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
     *
     * Проверяет, может ли игрок выполнить определенное действие
     *
     * Prüft, ob ein Spieler eine bestimmte Aktion durchführen kann
     */
    public boolean canPerform(Player player, String action, CreativeWorld.WorldDualMode worldMode) {
        UUID playerId = player.getUniqueId();
        PermissionLevel level = playerPermissions.getOrDefault(playerId, PermissionLevel.VISITOR);
        
        // Check for bypass permission first
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
                // Visitor level or higher can view/explore
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Sets permission level for a player
     *
     * Устанавливает уровень разрешений для игрока
     *
     * Setzt die Berechtigungsebene für einen Spieler
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
     *
     * Получает уровень разрешений для игрока
     *
     * Ruft die Berechtigungsebene für einen Spieler ab
     */
    public PermissionLevel getPlayerPermission(UUID playerId) {
        return playerPermissions.getOrDefault(playerId, PermissionLevel.VISITOR);
    }
    
    /**
     * Adds player to whitelist
     *
     * Добавляет игрока в белый список
     *
     * Fügt Spieler zur Whitelist hinzu
     */
    public void addToWhitelist(UUID playerId) {
        whitelist.add(playerId);
        blacklist.remove(playerId); 
        
        
    }
    
    /**
     * Removes player from whitelist
     *
     * Удаляет игрока из белого списка
     *
     * Entfernt Spieler von der Whitelist
     */
    public void removeFromWhitelist(UUID playerId) {
        whitelist.remove(playerId);
    }
    
    /**
     * Adds player to blacklist
     *
     * Добавляет игрока в черный список
     *
     * Fügt Spieler zur Blacklist hinzu
     */
    public void addToBlacklist(UUID playerId) {
        blacklist.add(playerId);
        whitelist.remove(playerId); 
        
        
        playerPermissions.remove(playerId); 
        
        
    }
    
    /**
     * Removes player from blacklist
     *
     * Удаляет игрока из черного списка
     *
     * Entfernt Spieler von der Blacklist
     */
    public void removeFromBlacklist(UUID playerId) {
        blacklist.remove(playerId);
    }
    
    
    
    
    public AccessMode getPlayWorldAccess() { return playWorldAccess; }
    public void setPlayWorldAccess(AccessMode playWorldAccess) { this.playWorldAccess = playWorldAccess; }
    
    public AccessMode getDevWorldAccess() { return devWorldAccess; }
    public void setDevWorldAccess(AccessMode devWorldAccess) { this.devWorldAccess = devWorldAccess; }
    
    public Map<UUID, PermissionLevel> getPlayerPermissions() { return new HashMap<>(playerPermissions); }
    public Set<UUID> getWhitelist() { return new HashSet<>(whitelist); }
    public Set<UUID> getBlacklist() { return new HashSet<>(blacklist); }
    
    public boolean isAllowPvP() { return allowPvP; }
    public void setAllowPvP(final boolean allowPvP) { this.allowPvP = allowPvP; }
    
    public boolean isAllowMobSpawning() { return allowMobSpawning; }
    public void setAllowMobSpawning(final boolean allowMobSpawning) { this.allowMobSpawning = allowMobSpawning; }
    
    public boolean isAllowExplosions() { return allowExplosions; }
    public void setAllowExplosions(final boolean allowExplosions) { this.allowExplosions = allowExplosions; }
    
    public boolean isAllowFlightInPlay() { return allowFlightInPlay; }
    public void setAllowFlightInPlay(final boolean allowFlightInPlay) { this.allowFlightInPlay = allowFlightInPlay; }
    
    public boolean isAllowItemDrops() { return allowItemDrops; }
    public void setAllowItemDrops(final boolean allowItemDrops) { this.allowItemDrops = allowItemDrops; }
    
    public boolean isProtectFromGriefing() { return protectFromGriefing; }
    public void setProtectFromGriefing(final boolean protectFromGriefing) { this.protectFromGriefing = protectFromGriefing; }
    
    /**
     * Gets summary of permissions for display
     *
     * Получает сводку разрешений для отображения
     *
     * Ruft Zusammenfassung der Berechtigungen für die Anzeige ab
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