package com.megacreative.models;

import lombok.Data;
import java.util.UUID;

@Data
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
}
