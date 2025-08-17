package com.megacreative.models;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CreativeWorld implements ConfigurationSerializable {
    
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
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public WorldMode getMode() {
        return mode;
    }

    public List<CodeScript> getScripts() {
        return scripts;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setMode(WorldMode mode) {
        this.mode = mode;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
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

    public void setTrustedBuilders(Set<UUID> trustedBuilders) {
        this.trustedBuilders = trustedBuilders;
    }

    public void setTrustedCoders(Set<UUID> trustedCoders) {
        this.trustedCoders = trustedCoders;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public void setLikedBy(Set<UUID> likedBy) {
        this.likedBy = likedBy;
    }

    public void setDislikedBy(Set<UUID> dislikedBy) {
        this.dislikedBy = dislikedBy;
    }

    public void setFavoriteBy(Set<UUID> favoriteBy) {
        this.favoriteBy = favoriteBy;
    }

    public void setComments(List<WorldComment> comments) {
        this.comments = comments;
    }

    public void setScripts(List<CodeScript> scripts) {
        this.scripts = scripts;
    }

    public void setDevWorldBlocks(Map<String, CodeBlock> devWorldBlocks) {
        this.devWorldBlocks = devWorldBlocks;
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

    public Set<UUID> getOnlinePlayers() {
        return onlinePlayers;
    }

    public Map<String, CodeBlock> getDevWorldBlocks() {
        return devWorldBlocks;
    }

    public CreativeWorldType getWorldType() {
        return worldType;
    }

    public int getRating() { return likes - dislikes; }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("name", this.name);
        map.put("description", this.description);
        map.put("ownerId", this.ownerId.toString());
        map.put("ownerName", this.ownerName);
        map.put("worldType", this.worldType.name());
        map.put("mode", this.mode.name());
        map.put("isPrivate", this.isPrivate);
        map.put("createdTime", this.createdTime);
        map.put("lastActivity", this.lastActivity);
        map.put("flags", this.flags.serialize());
        map.put("trustedBuilders", this.trustedBuilders.stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("trustedCoders", this.trustedCoders.stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("likes", this.likes);
        map.put("dislikes", this.dislikes);
        map.put("likedBy", this.likedBy.stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("dislikedBy", this.dislikedBy.stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("favoriteBy", this.favoriteBy.stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("comments", this.comments.stream().map(WorldComment::serialize).collect(Collectors.toList()));
        map.put("scripts", this.scripts.stream().map(CodeScript::serialize).collect(Collectors.toList()));
        map.put("devWorldBlocks", this.devWorldBlocks.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
        return map;
    }

    @SuppressWarnings("unchecked")
    public static CreativeWorld deserialize(Map<String, Object> map) {
        CreativeWorld world = new CreativeWorld(
                (String) map.get("id"),
                (String) map.get("name"),
                UUID.fromString((String) map.get("ownerId")),
                (String) map.get("ownerName"),
                CreativeWorldType.valueOf((String) map.get("worldType"))
        );

        world.setDescription((String) map.get("description"));
        world.setMode(WorldMode.valueOf((String) map.get("mode")));
        world.setPrivate((Boolean) map.get("isPrivate"));
        world.setCreatedTime(((Number) map.get("createdTime")).longValue());
        world.setLastActivity(((Number) map.get("lastActivity")).longValue());
        world.setFlags(WorldFlags.deserialize((Map<String, Object>) map.get("flags")));
        world.setTrustedBuilders(((List<String>) map.get("trustedBuilders")).stream().map(UUID::fromString).collect(Collectors.toSet()));
        world.setTrustedCoders(((List<String>) map.get("trustedCoders")).stream().map(UUID::fromString).collect(Collectors.toSet()));
        world.setLikes((Integer) map.get("likes"));
        world.setDislikes((Integer) map.get("dislikes"));
        world.setLikedBy(((List<String>) map.get("likedBy")).stream().map(UUID::fromString).collect(Collectors.toSet()));
        world.setDislikedBy(((List<String>) map.get("dislikedBy")).stream().map(UUID::fromString).collect(Collectors.toSet()));
        world.setFavoriteBy(((List<String>) map.get("favoriteBy")).stream().map(UUID::fromString).collect(Collectors.toSet()));
        world.setComments(((List<Map<String, Object>>) map.get("comments")).stream().map(WorldComment::deserialize).collect(Collectors.toList()));
        world.setScripts(((List<Map<String, Object>>) map.get("scripts")).stream().map(CodeScript::deserialize).collect(Collectors.toList()));

        Map<String, Map<String, Object>> devBlocksMap = (Map<String, Map<String, Object>>) map.get("devWorldBlocks");
        world.setDevWorldBlocks(devBlocksMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> CodeBlock.deserialize(e.getValue()))));

        return world;
    }
}
