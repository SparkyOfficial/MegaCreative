package com.megacreative.models;

import java.util.UUID;

/**
 * Модель доверенного игрока для системы управления правами
 */
public class TrustedPlayer {
    private final UUID playerId;
    private final String playerName;
    private final TrustedPlayerType type;
    private final long timestamp;
    private final UUID addedBy;

    public enum TrustedPlayerType {
        TRUSTED_BUILDER("Доверенный строитель"),
        TRUSTED_CODER("Доверенный программист");

        private final String displayName;

        TrustedPlayerType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public TrustedPlayer(UUID playerId, String playerName, TrustedPlayerType type, UUID addedBy) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.addedBy = addedBy;
    }

    public TrustedPlayer(UUID playerId, String playerName, TrustedPlayerType type, long timestamp, UUID addedBy) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.type = type;
        this.timestamp = timestamp;
        this.addedBy = addedBy;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public TrustedPlayerType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * @return The timestamp when this player was added as a trusted player
     */
    public long getAddedAt() {
        return timestamp;
    }

    public UUID getAddedBy() {
        return addedBy;
    }
    
    public String getAddedByName() {
        if (addedBy == null) {
            return "System";
        }
        try {
            return org.bukkit.Bukkit.getOfflinePlayer(addedBy).getName();
        } catch (Exception e) {
            return addedBy.toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrustedPlayer that = (TrustedPlayer) obj;
        return playerId.equals(that.playerId);
    }

    @Override
    public int hashCode() {
        return playerId.hashCode();
    }

    @Override
    public String toString() {
        return "TrustedPlayer{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", type=" + type +
                ", timestamp=" + timestamp +
                ", addedBy=" + (addedBy != null ? addedBy.toString() : "System") +
                '}';
    }
}