package com.megacreative.models;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a comment made by a player in a world.
 *
 * Представляет комментарий, сделанный игроком в мире.
 *
 * Stellt einen Kommentar dar, der von einem Spieler in einer Welt abgegeben wurde.
 */
public class WorldComment {
    private UUID authorId;
    private String authorName;
    private String text;
    private long timestamp;
    
    /**
     * Creates a WorldComment
     * @param authorId Author ID
     * @param authorName Author name
     * @param text Comment text
     *
     * Создает WorldComment
     * @param authorId ID автора
     * @param authorName Имя автора
     * @param text Текст комментария
     *
     * Erstellt einen WorldComment
     * @param authorId Autor-ID
     * @param authorName Autorname
     * @param text Kommentartext
     */
    public WorldComment(UUID authorId, String authorName, String text) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Creates a WorldComment with timestamp
     * @param authorId Author ID
     * @param authorName Author name
     * @param text Comment text
     * @param timestamp Timestamp
     *
     * Создает WorldComment с временной меткой
     * @param authorId ID автора
     * @param authorName Имя автора
     * @param text Текст комментария
     * @param timestamp Временная метка
     *
     * Erstellt einen WorldComment mit Zeitstempel
     * @param authorId Autor-ID
     * @param authorName Autorname
     * @param text Kommentartext
     * @param timestamp Zeitstempel
     */
    public WorldComment(UUID authorId, String authorName, String text, long timestamp) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.timestamp = timestamp;
    }
    
    
    
    
    
    /**
     * Gets the author ID
     * @return Author ID
     *
     * Получает ID автора
     * @return ID автора
     *
     * Ruft die Autor-ID ab
     * @return Autor-ID
     */
    public UUID getAuthorId() {
        return authorId;
    }
    
    /**
     * Sets the author ID
     * @param authorId Author ID
     *
     * Устанавливает ID автора
     * @param authorId ID автора
     *
     * Setzt die Autor-ID
     * @param authorId Autor-ID
     */
    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }
    
    /**
     * Gets the author name
     * @return Author name
     *
     * Получает имя автора
     * @return Имя автора
     *
     * Ruft den Autornamen ab
     * @return Autorname
     */
    public String getAuthorName() {
        return authorName;
    }
    
    /**
     * Sets the author name
     * @param authorName Author name
     *
     * Устанавливает имя автора
     * @param authorName Имя автора
     *
     * Setzt den Autornamen
     * @param authorName Autorname
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    /**
     * Gets the comment text
     * @return Comment text
     *
     * Получает текст комментария
     * @return Текст комментария
     *
     * Ruft den Kommentartext ab
     * @return Kommentartext
     */
    public String getText() {
        return text;
    }
    
    /**
     * Sets the comment text
     * @param text Comment text
     *
     * Устанавливает текст комментария
     * @param text Текст комментария
     *
     * Setzt den Kommentartext
     * @param text Kommentartext
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Gets the timestamp
     * @return Timestamp
     *
     * Получает временную метку
     * @return Временная метка
     *
     * Ruft den Zeitstempel ab
     * @return Zeitstempel
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Sets the timestamp
     * @param timestamp Timestamp
     *
     * Устанавливает временную метку
     * @param timestamp Временная метка
     *
     * Setzt den Zeitstempel
     * @param timestamp Zeitstempel
     */
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