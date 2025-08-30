package com.megacreative.models;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a comment made by a player in a world.
 */
public class WorldComment {
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
    
    // Getters and Setters
    
    public UUID getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldComment that = (WorldComment) o;
        return timestamp == that.timestamp &&
               Objects.equals(authorId, that.authorId) &&
               Objects.equals(authorName, that.authorName) &&
               Objects.equals(text, that.text);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(authorId, authorName, text, timestamp);
    }
    
    @Override
    public String toString() {
        return "WorldComment{" +
               "authorId=" + authorId +
               ", authorName='" + authorName + '\'' +
               ", text='" + text + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}
