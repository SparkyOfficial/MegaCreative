package com.megacreative.models;

import lombok.Data;

import org.bukkit.entity.Player;

import com.megacreative.coding.CodeScript;

import java.util.*;

@Data
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
    
    // Настройки мира
    private WorldFlags flags;
    
    // Доверенные игроки
    private Set<UUID> trustedBuilders;
    private Set<UUID> trustedCoders;
    
    // Статистика
    private int likes;
    private int dislikes;
    private Set<UUID> likedBy;
    private Set<UUID> dislikedBy;
    private Set<UUID> favoriteBy;
    
    // Комментарии
    private List<WorldComment> comments;

    // Скрипты
    private List<CodeScript> scripts;
    
    // Онлайн игроки
    private Set<UUID> onlinePlayers;
    
    public CreativeWorld() {
        // Конструктор по умолчанию для десериализации
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
    }
    
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
    }
    
    public String getWorldName() {
        return "megacreative_" + id;
    }
    
    public String getDevWorldName() {
        return "megacreative_" + id + "_dev";
    }
    
    public boolean isOwner(Player player) {
        return player.getUniqueId().equals(ownerId);
    }
    
    public boolean isTrustedBuilder(Player player) {
        if (trustedBuilders == null) {
            trustedBuilders = new HashSet<>();
        }
        return trustedBuilders.contains(player.getUniqueId()) || isOwner(player);
    }
    
    public boolean isTrustedCoder(Player player) {
        if (trustedCoders == null) {
            trustedCoders = new HashSet<>();
        }
        return trustedCoders.contains(player.getUniqueId()) || isOwner(player);
    }
    
    public boolean canEdit(Player player) {
        return isOwner(player) || isTrustedBuilder(player);
    }
    
    public boolean canCode(Player player) {
        return isOwner(player) || isTrustedCoder(player);
    }
    
    public void addOnlinePlayer(UUID playerId) {
        if (onlinePlayers == null) {
            onlinePlayers = new HashSet<>();
        }
        onlinePlayers.add(playerId);
        updateActivity();
    }
    
    public void removeOnlinePlayer(UUID playerId) {
        if (onlinePlayers == null) {
            onlinePlayers = new HashSet<>();
        }
        onlinePlayers.remove(playerId);
    }
    
    public int getOnlineCount() {
        if (onlinePlayers == null) {
            onlinePlayers = new HashSet<>();
        }
        return onlinePlayers.size();
    }
    
    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
    
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
    
    public void addToFavorites(UUID playerId) {
        favoriteBy.add(playerId);
    }
    
    public void removeFromFavorites(UUID playerId) {
        favoriteBy.remove(playerId);
    }
    
    public boolean isFavorite(UUID playerId) {
        return favoriteBy.contains(playerId);
    }
    
    public void addComment(WorldComment comment) {
        comments.add(comment);
    }
    
    public int getRating() {
        return likes - dislikes;
    }
    
    // Дополнительные геттеры для совместимости
    public String getId() {
        return id;
    }
    
    /**
     * @return The world ID as a UUID
     */
    public UUID getWorldId() {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // Generate a consistent UUID from the string ID if it's not a valid UUID
            return UUID.nameUUIDFromBytes(id.getBytes());
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
    
    public Set<UUID> getTrustedCoders() {
        return trustedCoders;
    }
    
    public int getLikes() {
        return likes;
    }
    
    public int getDislikes() {
        return dislikes;
    }
    
    public Set<UUID> getLikedBy() {
        return likedBy;
    }
    
    public Set<UUID> getDislikedBy() {
        return dislikedBy;
    }
    
    public Set<UUID> getFavoriteBy() {
        return favoriteBy;
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
    
    public void setMode(WorldMode mode) {
        this.mode = mode;
    }
}
