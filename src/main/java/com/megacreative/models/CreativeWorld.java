package com.megacreative.models;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import lombok.Data;
import org.bukkit.entity.Player;

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
    
    private WorldFlags flags;
    private Set<UUID> trustedBuilders;
    private Set<UUID> trustedCoders;
    
    private int likes;
    private int dislikes;
    private Set<UUID> likedBy;
    private Set<UUID> dislikedBy;
    private Set<UUID> favoriteBy;
    
    private List<WorldComment> comments;
    private List<CodeScript> scripts;
    
    // Ключ - это сериализованная локация (world,x,y,z), Значение - сам блок
    private Map<String, CodeBlock> devWorldBlocks;
    
    // transient, чтобы не сохранялось в JSON
    private transient Set<UUID> onlinePlayers;
    
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
        this.devWorldBlocks = new HashMap<>();
        this.onlinePlayers = new HashSet<>();
    }
    
    public String getWorldName() { return "megacreative_" + id; }
    public String getDevWorldName() { return "megacreative_" + id + "_dev"; }
    public boolean isOwner(Player player) { return player.getUniqueId().equals(ownerId); }
    public boolean isTrustedBuilder(Player player) { return trustedBuilders.contains(player.getUniqueId()) || isOwner(player); }
    public boolean isTrustedCoder(Player player) { return trustedCoders.contains(player.getUniqueId()) || isOwner(player); }
    public boolean canEdit(Player player) { return isOwner(player) || isTrustedBuilder(player); }
    public boolean canCode(Player player) { return isOwner(player) || isTrustedCoder(player); }

    public void addDevWorldBlock(String locationKey, CodeBlock block) {
        if (devWorldBlocks == null) devWorldBlocks = new HashMap<>();
        devWorldBlocks.put(locationKey, block);
    }

    public void removeDevWorldBlock(String locationKey) {
        if (devWorldBlocks != null) devWorldBlocks.remove(locationKey);
    }
    
    public CodeBlock getDevWorldBlock(String locationKey) {
        return (devWorldBlocks != null) ? devWorldBlocks.get(locationKey) : null;
    }
    
    public void addOnlinePlayer(UUID playerId) {
        if (onlinePlayers == null) onlinePlayers = new HashSet<>();
        onlinePlayers.add(playerId);
        updateActivity();
    }
    
    public void removeOnlinePlayer(UUID playerId) { if (onlinePlayers != null) onlinePlayers.remove(playerId); }
    public int getOnlineCount() { return onlinePlayers != null ? onlinePlayers.size() : 0; }
    public void updateActivity() { this.lastActivity = System.currentTimeMillis(); }
    public boolean addLike(UUID playerId) { if (likedBy.contains(playerId)) return false; if (dislikedBy.contains(playerId)) { dislikedBy.remove(playerId); dislikes--; } likedBy.add(playerId); likes++; return true; }
    public boolean addDislike(UUID playerId) { if (dislikedBy.contains(playerId)) return false; if (likedBy.contains(playerId)) { likedBy.remove(playerId); likes--; } dislikedBy.add(playerId); dislikes++; return true; }
    public void addToFavorites(UUID playerId) { favoriteBy.add(playerId); }
    public void removeFromFavorites(UUID playerId) { favoriteBy.remove(playerId); }
    public boolean isFavorite(UUID playerId) { return favoriteBy.contains(playerId); }
    public void addComment(WorldComment comment) { comments.add(comment); }
    public int getRating() { return likes - dislikes; }
}
