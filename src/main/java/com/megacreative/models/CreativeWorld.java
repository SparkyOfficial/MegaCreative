package com.megacreative.models;

import org.bukkit.entity.Player;
import com.megacreative.coding.CodeScript;
import java.util.*;
import java.util.Objects;

/**
 * Represents a creative world in the plugin with all its properties and settings.
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
    
    // Настройки мира
    private WorldFlags flags;
    
    // Доверенные игроки
    private Set<UUID> trustedBuilders;
    private Set<UUID> trustedCoders;
    private Set<UUID> trustedAdmins = new HashSet<>();
    
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
    
    /**
     * Получает список всех игроков в мире
     * @return Список игроков
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
    
    public void setMode(WorldMode mode) {
        this.mode = mode;
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
