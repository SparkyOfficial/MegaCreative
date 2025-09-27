package com.megacreative.models;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeHandler;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.Objects;

/**
 * Represents a creative world in the plugin with all its properties and settings.
 *
 * Представляет творческий мир в плагине со всеми его свойствами и настройками.
 *
 * Stellt eine kreative Welt im Plugin mit all ihren Eigenschaften und Einstellungen dar.
 */
public class CreativeWorld {
    
    private String id;
    private String name;
    private String description;
    private UUID ownerId;
    private String ownerName;
    private CreativeWorldType worldType;
    private WorldMode mode;
    private boolean isPrivate;
    private long createdTime;
    private long lastActivity;
    
    /**
     * World settings
     *
     * Настройки мира
     *
     * Welteinstellungen
     */
    private WorldFlags flags;
    
    /**
     * Trusted players
     *
     * Доверенные игроки
     *
     * Vertrauenswürdige Spieler
     */
    private Set<UUID> trustedBuilders;
    private Set<UUID> trustedCoders;
    private Set<UUID> trustedAdmins = new HashSet<>();
    
    /**
     * Statistics
     *
     * Статистика
     *
     * Statistiken
     */
    private int likes;
    private int dislikes;
    private Set<UUID> likedBy;
    private Set<UUID> dislikedBy;
    private Set<UUID> favoriteBy;
    
    /**
     * Comments
     *
     * Комментарии
     *
     * Kommentare
     */
    private List<WorldComment> comments;

    /**
     * 🎆 ENHANCED: Reference system-style dual world support
     *
     * 🎆 ENHANCED: Reference system-style: Двойная поддержка миров
     *
     * 🎆 ENHANCED: Reference system-style: Duale Weltunterstützung
     */
    private String pairedWorldId; // ID of the paired world (dev/play)
    // ID парного мира (dev/play)
    // ID der gekoppelten Welt (dev/play)
    private WorldDualMode dualMode; // Whether this is dev or play world
    // Является ли это миром разработки или игры
    // Ob es sich um eine Entwicklungs- oder Spielwelt handelt
    
    /**
     * 🎆 ENHANCED: Advanced permission system
     *
     * 🎆 ENHANCED: Продвинутая система разрешений
     *
     * 🎆 ENHANCED: Erweitertes Berechtigungssystem
     */
    private WorldPermissions permissions;
    
    /**
     * Scripts
     *
     * Скрипты
     *
     * Skripte
     */
    private List<CodeScript> scripts;
    
    /**
     * Code handler for managing script execution
     *
     * Обработчик кода для управления выполнением скриптов
     *
     * Code-Handler zur Verwaltung der Skriptausführung
     */
    private CodeHandler codeHandler;
    
    /**
     * Online players
     *
     * Онлайн игроки
     *
     * Online-Spieler
     */
    private Set<UUID> onlinePlayers;
    
    /**
     * 🎆 ENHANCED: World dual mode enum
     *
     * 🎆 ENHANCED: Перечисление двойного режима мира
     *
     * 🎆 ENHANCED: Welt-Dualmodus-Enum
     */
    public enum WorldDualMode {
        DEV("code", "§e🔧 Development"),
        // Разработка
        // Entwicklung
        PLAY("world", "§a🎮 Play"),
        // Игра
        // Spielen
        STANDALONE("single", "§7📦 Standalone");
        // Автономный
        // Eigenständig
        
        private final String suffix;
        private final String displayName;
        
        /**
         * Creates a WorldDualMode
         * @param suffix Mode suffix
         * @param displayName Display name
         *
         * Создает WorldDualMode
         * @param suffix Суффикс режима
         * @param displayName Отображаемое имя
         *
         * Erstellt einen WorldDualMode
         * @param suffix Modus-Suffix
         * @param displayName Anzeigename
         */
        WorldDualMode(String suffix, String displayName) {
            this.suffix = suffix;
            this.displayName = displayName;
        }
        
        /**
         * Gets the suffix
         * @return Mode suffix
         *
         * Получает суффикс
         * @return Суффикс режима
         *
         * Ruft das Suffix ab
         * @return Modus-Suffix
         */
        public String getSuffix() { return suffix; }
        
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
    }
    
    /**
     * Default constructor
     *
     * Конструктор по умолчанию
     *
     * Standardkonstruktor
     */
    public CreativeWorld() {
        // Конструктор по умолчанию для десериализации
        // Default constructor for deserialization
        // Standardkonstruktor für Deserialisierung
        this.flags = new WorldFlags();
        this.trustedBuilders = new HashSet<>();
        this.trustedAdmins = new HashSet<>();
        this.trustedCoders = new HashSet<>();
        this.trustedAdmins = new HashSet<>();
        this.comments = new ArrayList<>();
        this.scripts = new ArrayList<>();
        this.onlinePlayers = new HashSet<>();
        this.likedBy = new HashSet<>();
        this.dislikedBy = new HashSet<>();
        this.favoriteBy = new HashSet<>();
        this.createdTime = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
        this.likes = 0;
        this.dislikes = 0;
    }
    
    /**
     * Constructor with parameters
     * @param id World ID
     * @param name World name
     * @param ownerId Owner ID
     * @param ownerName Owner name
     * @param worldType World type
     *
     * Конструктор с параметрами
     * @param id ID мира
     * @param name Название мира
     * @param ownerId ID владельца
     * @param ownerName Имя владельца
     * @param worldType Тип мира
     *
     * Konstruktor mit Parametern
     * @param id Welt-ID
     * @param name Weltname
     * @param ownerId Besitzer-ID
     * @param ownerName Besitzername
     * @param worldType Welttyp
     */
    public CreativeWorld(String id, String name, UUID ownerId, String ownerName, CreativeWorldType worldType) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.worldType = worldType;
        this.mode = WorldMode.BUILD;
        this.isPrivate = false;
        this.createdTime = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
        
        this.flags = new WorldFlags();
        this.trustedBuilders = new HashSet<>();
        this.trustedCoders = new HashSet<>();
        this.likes = 0;
        this.dislikes = 0;
        this.likedBy = new HashSet<>();
        this.dislikedBy = new HashSet<>();
        this.favoriteBy = new HashSet<>();
        this.comments = new ArrayList<>();
        this.scripts = new ArrayList<>();
        this.onlinePlayers = new HashSet<>();
        
        // 🎆 ENHANCED: Initialize dual world support
        // 🎆 ENHANCED: Инициализировать двойную поддержку миров
        // 🎆 ENHANCED: Duale Weltunterstützung initialisieren
        this.dualMode = WorldDualMode.STANDALONE;
        this.pairedWorldId = null;
        
        // 🎆 ENHANCED: Initialize advanced permissions
        // 🎆 ENHANCED: Инициализировать продвинутые разрешения
        // 🎆 ENHANCED: Erweiterte Berechtigungen initialisieren
        this.permissions = new WorldPermissions();
    }
    
    /**
     * 🎆 ENHANCED: Reference system-style world naming with dual mode support
     *
     * 🎆 ENHANCED: Reference system-style: Именование мира с поддержкой двойного режима
     *
     * 🎆 ENHANCED: Reference system-style: Weltbenennung mit Dualmodus-Unterstützung
     */
    public String getWorldName() {
        if (dualMode == WorldDualMode.STANDALONE) {
            return "megacreative_" + id;
        }
        return "megacreative_" + id + "-" + dualMode.getSuffix();
    }
    
    /**
     * Gets the Bukkit World instance for this CreativeWorld
     * @return The Bukkit World or null if not loaded
     *
     * Получает экземпляр Bukkit World для этого CreativeWorld
     * @return Bukkit World или null, если не загружен
     *
     * Ruft die Bukkit-Weltinstanz für diese CreativeWorld ab
     * @return Die Bukkit-Welt oder null, wenn nicht geladen
     */
    public org.bukkit.World getBukkitWorld() {
        return org.bukkit.Bukkit.getWorld(getWorldName());
    }
    
    /**
     * Gets the base name of the world (without suffixes)
     * @return The base world name
     *
     * Получает базовое имя мира (без суффиксов)
     * @return Базовое имя мира
     *
     * Ruft den Basisnamen der Welt ab (ohne Suffixe)
     * @return Der Basisweltname
     */
    public String getBaseName() {
        return name != null ? name : "World_" + id;
    }
    
    /**
     * Gets the development world name
     * @return Development world name
     *
     * Получает имя мира разработки
     * @return Имя мира разработки
     *
     * Ruft den Entwicklungs-Weltnamen ab
     * @return Entwicklungs-Weltname
     */
    public String getDevWorldName() {
        // Legacy support - now returns the dev mode world name
        // Поддержка устаревших версий - теперь возвращает имя мира разработки
        // Legacy-Unterstützung - gibt jetzt den Entwicklungsmodus-Weltnamen zurück
        if (dualMode == WorldDualMode.DEV) {
            return getWorldName();
        }
        return "megacreative_" + id + "-code";
    }
    
    /**
     * Gets the play world name
     * @return Play world name
     *
     * Получает имя мира игры
     * @return Имя мира игры
     *
     * Ruft den Spiel-Weltnamen ab
     * @return Spiel-Weltname
     */
    public String getPlayWorldName() {
        if (dualMode == WorldDualMode.PLAY) {
            return getWorldName();
        }
        return "megacreative_" + id + "-world";
    }
    
    /**
     * Gets the paired world name
     * @return Paired world name or null
     *
     * Получает имя парного мира
     * @return Имя парного мира или null
     *
     * Ruft den gekoppelten Weltnamen ab
     * @return Gekoppelter Weltname oder null
     */
    public String getPairedWorldName() {
        if (pairedWorldId != null) {
            switch (dualMode) {
                case DEV:
                    return "megacreative_" + pairedWorldId + "-world";
                case PLAY:
                    return "megacreative_" + pairedWorldId + "-code";
                default:
                    return null;
            }
        }
        return null;
    }
    
    /**
     * Checks if player is the owner
     * @param player Player to check
     * @return true if player is owner
     *
     * Проверяет, является ли игрок владельцем
     * @param player Игрок для проверки
     * @return true, если игрок является владельцем
     *
     * Prüft, ob der Spieler der Besitzer ist
     * @param player Zu prüfender Spieler
     * @return true, wenn der Spieler der Besitzer ist
     */
    public boolean isOwner(Player player) {
        return player.getUniqueId().equals(ownerId);
    }
    
    /**
     * Checks if player is a trusted builder
     * @param player Player to check
     * @return true if player is trusted builder
     *
     * Проверяет, является ли игрок доверенным строителем
     * @param player Игрок для проверки
     * @return true, если игрок является доверенным строителем
     *
     * Prüft, ob der Spieler ein vertrauenswürdiger Baumeister ist
     * @param player Zu prüfender Spieler
     * @return true, wenn der Spieler ein vertrauenswürdiger Baumeister ist
     */
    public boolean isTrustedBuilder(Player player) {
        if (trustedBuilders == null) {
            trustedBuilders = new HashSet<>();
        }
        return trustedBuilders.contains(player.getUniqueId()) || isOwner(player);
    }
    
    /**
     * Checks if player is a trusted coder
     * @param player Player to check
     * @return true if player is trusted coder
     *
     * Проверяет, является ли игрок доверенным кодером
     * @param player Игрок для проверки
     * @return true, если игрок является доверенным кодером
     *
     * Prüft, ob der Spieler ein vertrauenswürdiger Coder ist
     * @param player Zu prüfender Spieler
     * @return true, wenn der Spieler ein vertrauenswürdiger Coder ist
     */
    public boolean isTrustedCoder(Player player) {
        if (trustedCoders == null) {
            trustedCoders = new HashSet<>();
        }
        return trustedCoders.contains(player.getUniqueId()) || isOwner(player);
    }
    
    /**
     * Checks if player can edit the world
     * @param player Player to check
     * @return true if player can edit
     *
     * Проверяет, может ли игрок редактировать мир
     * @param player Игрок для проверки
     * @return true, если игрок может редактировать
     *
     * Prüft, ob der Spieler die Welt bearbeiten kann
     * @param player Zu prüfender Spieler
     * @return true, wenn der Spieler bearbeiten kann
     */
    public boolean canEdit(Player player) {
        // 🎆 ENHANCED: Use advanced permission system with fallback to legacy
        // 🎆 ENHANCED: Использовать продвинутую систему разрешений с откатом к устаревшей
        // 🎆 ENHANCED: Erweitertes Berechtigungssystem mit Fallback zur Legacy-Version verwenden
        if (permissions != null) {
            return isOwner(player) || 
                   permissions.canAccess(player, dualMode) && 
                   permissions.canPerform(player, "build", dualMode);
        }
        // Legacy fallback
        // Откат к устаревшей версии
        // Legacy-Fallback
        return isOwner(player) || isTrustedBuilder(player);
    }
    
    /**
     * Checks if player can code in the world
     * @param player Player to check
     * @return true if player can code
     *
     * Проверяет, может ли игрок программировать в мире
     * @param player Игрок для проверки
     * @return true, если игрок может программировать
     *
     * Prüft, ob der Spieler in der Welt codieren kann
     * @param player Zu prüfender Spieler
     * @return true, wenn der Spieler codieren kann
     */
    public boolean canCode(Player player) {
        // 🎆 ENHANCED: Use advanced permission system with fallback to legacy
        // 🎆 ENHANCED: Использовать продвинутую систему разрешений с откатом к устаревшей
        // 🎆 ENHANCED: Erweitertes Berechtigungssystem mit Fallback zur Legacy-Version verwenden
        if (permissions != null) {
            return isOwner(player) || 
                   permissions.canAccess(player, dualMode) && 
                   permissions.canPerform(player, "code", dualMode);
        }
        // Legacy fallback
        // Откат к устаревшей версии
        // Legacy-Fallback
        return isOwner(player) || isTrustedCoder(player);
    }
    
    /**
     * 🎆 ENHANCED: Checks if player can access the world in specific mode
     *
     * 🎆 ENHANCED: Проверяет, может ли игрок получить доступ к миру в определенном режиме
     *
     * 🎆 ENHANCED: Prüft, ob der Spieler auf die Welt im bestimmten Modus zugreifen kann
     */
    public boolean canAccess(Player player, WorldDualMode mode) {
        if (permissions != null) {
            return isOwner(player) || permissions.canAccess(player, mode);
        }
        // Legacy fallback - public access
        // Откат к устаревшей версии - публичный доступ
        // Legacy-Fallback - öffentlicher Zugriff
        return !isPrivate || isOwner(player) || isTrustedBuilder(player);
    }
    
    /**
     * 🎆 ENHANCED: Checks if player can perform a specific action
     *
     * 🎆 ENHANCED: Проверяет, может ли игрок выполнить определенное действие
     *
     * 🎆 ENHANCED: Prüft, ob der Spieler eine bestimmte Aktion durchführen kann
     */
    public boolean canPerform(Player player, String action) {
        if (permissions != null) {
            return isOwner(player) || permissions.canPerform(player, action, dualMode);
        }
        // Legacy fallback
        // Откат к устаревшей версии
        // Legacy-Fallback
        switch (action.toLowerCase()) {
            case "build":
            case "edit":
                return canEdit(player);
            case "code":
                return canCode(player);
            default:
                return !isPrivate || isOwner(player);
        }
    }
    
    /**
     * Adds a player to online players
     * @param playerId Player ID to add
     *
     * Добавляет игрока в онлайн игроков
     * @param playerId ID игрока для добавления
     *
     * Fügt einen Spieler zu den Online-Spielern hinzu
     * @param playerId Spieler-ID zum Hinzufügen
     */
    public void addOnlinePlayer(UUID playerId) {
        if (onlinePlayers == null) {
            onlinePlayers = new HashSet<>();
        }
        onlinePlayers.add(playerId);
        updateActivity();
    }
    
    /**
     * Removes a player from online players
     * @param playerId Player ID to remove
     *
     * Удаляет игрока из онлайн игроков
     * @param playerId ID игрока для удаления
     *
     * Entfernt einen Spieler von den Online-Spielern
     * @param playerId Spieler-ID zum Entfernen
     */
    public void removeOnlinePlayer(UUID playerId) {
        if (onlinePlayers == null) {
            onlinePlayers = new HashSet<>();
        }
        onlinePlayers.remove(playerId);
    }
    
    /**
     * Gets the online player count
     * @return Number of online players
     *
     * Получает количество онлайн игроков
     * @return Количество онлайн игроков
     *
     * Ruft die Anzahl der Online-Spieler ab
     * @return Anzahl der Online-Spieler
     */
    public int getOnlineCount() {
        if (onlinePlayers == null) {
            onlinePlayers = new HashSet<>();
        }
        return onlinePlayers.size();
    }
    
    /**
     * Updates the last activity time
     *
     * Обновляет время последней активности
     *
     * Aktualisiert die letzte Aktivitätszeit
     */
    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
    
    /**
     * Gets the code handler for this world
     * @return The code handler
     *
     * Получает обработчик кода для этого мира
     * @return Обработчик кода
     *
     * Ruft den Code-Handler für diese Welt ab
     * @return Der Code-Handler
     */
    public CodeHandler getCodeHandler() {
        return codeHandler;
    }
    
    /**
     * Sets the code handler for this world
     * @param codeHandler The code handler to set
     *
     * Устанавливает обработчик кода для этого мира
     * @param codeHandler Обработчик кода для установки
     *
     * Setzt den Code-Handler für diese Welt
     * @param codeHandler Der zu setzende Code-Handler
     */
    public void setCodeHandler(CodeHandler codeHandler) {
        this.codeHandler = codeHandler;
    }
    
    /**
     * Gets the script engine for this world
     * @return The script engine
     *
     * Получает движок скриптов для этого мира
     * @return Движок скриптов
     *
     * Ruft die Skript-Engine für diese Welt ab
     * @return Die Skript-Engine
     */
    public ScriptEngine getScriptEngine() {
        // Get the script engine from the plugin's service registry
        return MegaCreative.getInstance().getServiceRegistry().getScriptEngine();
    }
    
    /**
     * Adds a like from a player
     * @param playerId Player ID who liked
     * @return true if like was added, false if already liked
     *
     * Добавляет лайк от игрока
     * @param playerId ID игрока, который поставил лайк
     * @return true, если лайк был добавлен, false, если уже поставлен
     *
     * Fügt ein Like von einem Spieler hinzu
     * @param playerId Spieler-ID, der geliked hat
     * @return true, wenn Like hinzugefügt wurde, false, wenn bereits geliked
     */
    public boolean addLike(UUID playerId) {
        if (likedBy.contains(playerId)) {
            return false;
        }
        if (dislikedBy.contains(playerId)) {
            dislikedBy.remove(playerId);
            dislikes--;
        }
        likedBy.add(playerId);
        likes++;
        return true;
    }
    
    /**
     * Adds a dislike from a player
     * @param playerId Player ID who disliked
     * @return true if dislike was added, false if already disliked
     *
     * Добавляет дизлайк от игрока
     * @param playerId ID игрока, который поставил дизлайк
     * @return true, если дизлайк был добавлен, false, если уже поставлен
     *
     * Fügt ein Dislike von einem Spieler hinzu
     * @param playerId Spieler-ID, der dislikt hat
     * @return true, wenn Dislike hinzugefügt wurde, false, wenn bereits dislikt
     */
    public boolean addDislike(UUID playerId) {
        if (dislikedBy.contains(playerId)) {
            return false;
        }
        if (likedBy.contains(playerId)) {
            likedBy.remove(playerId);
            likes--;
        }
        dislikedBy.add(playerId);
        dislikes++;
        return true;
    }
    
    /**
     * Adds a player to favorites
     * @param playerId Player ID to add to favorites
     *
     * Добавляет игрока в избранное
     * @param playerId ID игрока для добавления в избранное
     *
     * Fügt einen Spieler zu den Favoriten hinzu
     * @param playerId Spieler-ID zum Hinzufügen zu den Favoriten
     */
    public void addToFavorites(UUID playerId) {
        favoriteBy.add(playerId);
    }
    
    /**
     * Removes a player from favorites
     * @param playerId Player ID to remove from favorites
     *
     * Удаляет игрока из избранного
     * @param playerId ID игрока для удаления из избранного
     *
     * Entfernt einen Spieler von den Favoriten
     * @param playerId Spieler-ID zum Entfernen von den Favoriten
     */
    public void removeFromFavorites(UUID playerId) {
        favoriteBy.remove(playerId);
    }
    
    /**
     * Checks if player is in favorites
     * @param playerId Player ID to check
     * @return true if player is in favorites
     *
     * Проверяет, находится ли игрок в избранном
     * @param playerId ID игрока для проверки
     * @return true, если игрок находится в избранном
     *
     * Prüft, ob der Spieler in den Favoriten ist
     * @param playerId Spieler-ID zum Prüfen
     * @return true, wenn der Spieler in den Favoriten ist
     */
    public boolean isFavorite(UUID playerId) {
        return favoriteBy.contains(playerId);
    }
    
    /**
     * Adds a comment to the world
     * @param comment Comment to add
     *
     * Добавляет комментарий к миру
     * @param comment Комментарий для добавления
     *
     * Fügt einen Kommentar zur Welt hinzu
     * @param comment Hinzuzufügender Kommentar
     */
    public void addComment(WorldComment comment) {
        comments.add(comment);
    }
    
    /**
     * Gets the world rating (likes - dislikes)
     * @return World rating
     *
     * Получает рейтинг мира (лайки - дизлайки)
     * @return Рейтинг мира
     *
     * Ruft die Weltrating ab (Likes - Dislikes)
     * @return Weltrating
     */
    public int getRating() {
        return likes - dislikes;
    }
    
    /**
     * Получает список всех игроков в мире
     * @return Список игроков
     *
     * Gets list of all players in the world
     * @return List of players
     *
     * Ruft Liste aller Spieler in der Welt ab
     * @return Liste der Spieler
     */
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        if (onlinePlayers != null) {
            for (UUID playerId : onlinePlayers) {
                Player player = org.bukkit.Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    players.add(player);
                }
            }
        }
        return players;
    }
    
    // Дополнительные геттеры для совместимости
    // Additional getters for compatibility
    // Zusätzliche Getter für Kompatibilität
    public String getId() {
        return id;
    }
    
    /**
     * @return The world ID as a UUID
     *
     * @return ID мира как UUID
     *
     * @return Die Welt-ID als UUID
     */
    public UUID getWorldId() {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // Generate a consistent UUID from the string ID if it's not a valid UUID
            // Генерировать согласованный UUID из строкового ID, если это недопустимый UUID
            // Einen konsistenten UUID aus der Zeichenfolgen-ID generieren, wenn es kein gültiger UUID ist
            return UUID.nameUUIDFromBytes(id.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public CreativeWorldType getWorldType() {
        return worldType;
    }
    
    public WorldMode getMode() {
        return mode;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public long getLastActivity() {
        return lastActivity;
    }
    
    public WorldFlags getFlags() {
        return flags;
    }
    
    public Set<UUID> getTrustedBuilders() {
        return trustedBuilders;
    }

    public void setTrustedBuilders(Set<UUID> trustedBuilders) {
        this.trustedBuilders = trustedBuilders != null ? new HashSet<>(trustedBuilders) : new HashSet<>();
    }

    public Set<UUID> getTrustedCoders() {
        return trustedCoders != null ? trustedCoders : (trustedCoders = new HashSet<>());
    }

    public void setTrustedCoders(Set<UUID> trustedCoders) {
        this.trustedCoders = trustedCoders != null ? new HashSet<>(trustedCoders) : new HashSet<>();
    }

    public Set<UUID> getTrustedAdmins() {
        return trustedAdmins != null ? trustedAdmins : (trustedAdmins = new HashSet<>());
    }

    public void setTrustedAdmins(Set<UUID> trustedAdmins) {
        this.trustedAdmins = trustedAdmins;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public Set<UUID> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(Set<UUID> likedBy) {
        this.likedBy = likedBy;
    }

    public Set<UUID> getDislikedBy() {
        return dislikedBy;
    }

    public void setDislikedBy(Set<UUID> dislikedBy) {
        this.dislikedBy = dislikedBy;
    }

    public Set<UUID> getFavoriteBy() {
        return favoriteBy;
    }

    public void setFavoriteBy(Set<UUID> favoriteBy) {
        this.favoriteBy = favoriteBy;
    }

    public List<WorldComment> getComments() {
        return comments;
    }
    
    public List<CodeScript> getScripts() {
        return scripts;
    }
    
    public Set<UUID> getOnlinePlayers() {
        return onlinePlayers;
    }
    
    /**
     * 🎆 ENHANCED: Dual world getters/setters
     *
     * 🎆 ENHANCED: Геттеры/сеттеры двойного мира
     *
     * 🎆 ENHANCED: Duale Welt-Getter/Setter
     */
    public WorldDualMode getDualMode() {
        return dualMode != null ? dualMode : WorldDualMode.STANDALONE;
    }
    
    public void setDualMode(WorldDualMode dualMode) {
        this.dualMode = dualMode;
    }
    
    public String getPairedWorldId() {
        return pairedWorldId;
    }
    
    public void setPairedWorldId(String pairedWorldId) {
        this.pairedWorldId = pairedWorldId;
    }
    
    public boolean isPaired() {
        return pairedWorldId != null;
    }
    
    public boolean isDevWorld() {
        return dualMode == WorldDualMode.DEV;
    }
    
    public boolean isPlayWorld() {
        return dualMode == WorldDualMode.PLAY;
    }
    
    public void setMode(WorldMode mode) {
        this.mode = mode;
    }
    
    /**
     * 🎆 ENHANCED: Permission system getters/setters
     *
     * 🎆 ENHANCED: Геттеры/сеттеры системы разрешений
     *
     * 🎆 ENHANCED: Berechtigungssystem-Getter/Setter
     */
    public WorldPermissions getPermissions() {
        if (permissions == null) {
            permissions = new WorldPermissions();
        }
        return permissions;
    }
    
    public void setPermissions(WorldPermissions permissions) {
        this.permissions = permissions;
    }
    
    /**
     * Gets permission summary for display
     *
     * Получает сводку разрешений для отображения
     *
     * Ruft Berechtigungszusammenfassung für die Anzeige ab
     */
    public Map<String, Object> getPermissionsSummary() {
        return getPermissions().getPermissionsSummary();
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public void setWorldType(CreativeWorldType worldType) {
        this.worldType = worldType;
    }
    
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
    
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
    
    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public void setFlags(WorldFlags flags) {
        this.flags = flags;
    }
    
    public void setComments(List<WorldComment> comments) {
        this.comments = comments;
    }
    
    public void setScripts(List<CodeScript> scripts) {
        this.scripts = scripts;
    }
    
    public void setOnlinePlayers(Set<UUID> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreativeWorld that = (CreativeWorld) o;
        return isPrivate == that.isPrivate &&
               createdTime == that.createdTime &&
               lastActivity == that.lastActivity &&
               likes == that.likes &&
               dislikes == that.dislikes &&
               Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(ownerId, that.ownerId) &&
               Objects.equals(ownerName, that.ownerName) &&
               worldType == that.worldType &&
               mode == that.mode &&
               Objects.equals(flags, that.flags) &&
               Objects.equals(trustedBuilders, that.trustedBuilders) &&
               Objects.equals(trustedCoders, that.trustedCoders) &&
               Objects.equals(trustedAdmins, that.trustedAdmins) &&
               Objects.equals(likedBy, that.likedBy) &&
               Objects.equals(dislikedBy, that.dislikedBy) &&
               Objects.equals(favoriteBy, that.favoriteBy) &&
               Objects.equals(comments, that.comments) &&
               Objects.equals(scripts, that.scripts) &&
               Objects.equals(onlinePlayers, that.onlinePlayers);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, ownerId, ownerName, worldType, mode, 
                          isPrivate, createdTime, lastActivity, flags, trustedBuilders, 
                          trustedCoders, trustedAdmins, likes, dislikes, likedBy, 
                          dislikedBy, favoriteBy, comments, scripts, onlinePlayers);
    }
    
    @Override
    public String toString() {
        return "CreativeWorld{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", ownerId=" + ownerId +
               ", ownerName='" + ownerName + '\'' +
               ", worldType=" + worldType +
               ", mode=" + mode +
               ", isPrivate=" + isPrivate +
               ", createdTime=" + createdTime +
               ", lastActivity=" + lastActivity +
               ", flags=" + flags +
               ", trustedBuilders=" + trustedBuilders.size() +
               ", trustedCoders=" + trustedCoders.size() +
               ", trustedAdmins=" + (trustedAdmins != null ? trustedAdmins.size() : 0) +
               ", likes=" + likes +
               ", dislikes=" + dislikes +
               ", likedBy=" + (likedBy != null ? likedBy.size() : 0) +
               ", dislikedBy=" + (dislikedBy != null ? dislikedBy.size() : 0) +
               ", favoriteBy=" + (favoriteBy != null ? favoriteBy.size() : 0) +
               ", comments=" + (comments != null ? comments.size() : 0) +
               ", scripts=" + (scripts != null ? scripts.size() : 0) +
               ", onlinePlayers=" + (onlinePlayers != null ? onlinePlayers.size() : 0) +
               '}';
    }
}