package com.megacreative.models;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldComment implements ConfigurationSerializable {
    public UUID getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
    private UUID authorId;
    private String authorName;
    private String text;
    private long timestamp;
    
    public WorldComment(UUID authorId, String authorName, String text) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }
    
    public WorldComment(UUID authorId, String authorName, String text, long timestamp) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.timestamp = timestamp;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("authorId", authorId.toString());
        map.put("authorName", authorName);
        map.put("text", text);
        map.put("timestamp", timestamp);
        return map;
    }

    public static WorldComment deserialize(Map<String, Object> map) {
        return new WorldComment(
            UUID.fromString((String) map.get("authorId")),
            (String) map.get("authorName"),
            (String) map.get("text"),
            ((Number) map.get("timestamp")).longValue()
        );
    }
}
