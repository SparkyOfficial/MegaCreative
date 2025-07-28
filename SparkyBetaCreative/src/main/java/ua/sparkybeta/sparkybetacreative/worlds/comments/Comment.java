package ua.sparkybeta.sparkybetacreative.worlds.comments;

import java.util.UUID;
import lombok.Data;

@Data
public class Comment {
    private final UUID author;
    private final long timestamp;
    private String text;

    public Comment(UUID author, long timestamp, String text) {
        this.author = author;
        this.timestamp = timestamp;
        this.text = text;
    }
} 