package com.megacreative.models;

import java.util.UUID;

/**
 * Модель доверенного игрока для системы управления правами
 *
 * Trusted player model for the permission management system
 *
 * Vertrauenswürdiges Spielermodell für das Berechtigungsverwaltungssystem
 */
public class TrustedPlayer {
    private final UUID playerId;
    private final String playerName;
    private final TrustedPlayerType type;
    private final long timestamp;
    private final UUID addedBy;

    /**
     * Types of trusted players
     *
     * Типы доверенных игроков
     *
     * Arten vertrauenswürdiger Spieler
     */
    public enum TrustedPlayerType {
        /**
         * Trusted builder
         *
         * Доверенный строитель
         *
         * Vertrauenswürdiger Baumeister
         */
        TRUSTED_BUILDER("Доверенный строитель"),
        /**
         * Trusted coder
         *
         * Доверенный программист
         *
         * Vertrauenswürdiger Coder
         */
        TRUSTED_CODER("Доверенный программист");

        private final String displayName;

        /**
         * Creates a TrustedPlayerType
         * @param displayName Display name
         *
         * Создает TrustedPlayerType
         * @param displayName Отображаемое имя
         *
         * Erstellt einen TrustedPlayerType
         * @param displayName Anzeigename
         */
        TrustedPlayerType(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Gets the display name
         * @return Display name
         *
         * Получает отображаемое имя
         * @return Отображаемое имя
         *
         * Ruft den Anzeigenamen ab
         * @return Anzeigename
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Creates a TrustedPlayer
     * @param playerId Player ID
     * @param playerName Player name
     * @param type Player type
     * @param addedBy Added by player ID
     *
     * Создает TrustedPlayer
     * @param playerId ID игрока
     * @param playerName Имя игрока
     * @param type Тип игрока
     * @param addedBy ID игрока, который добавил
     *
     * Erstellt einen TrustedPlayer
     * @param playerId Spieler-ID
     * @param playerName Spielername
     * @param type Spielertyp
     * @param addedBy Hinzugefügt von Spieler-ID
     */
    public TrustedPlayer(UUID playerId, String playerName, TrustedPlayerType type, UUID addedBy) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.addedBy = addedBy;
    }

    /**
     * Creates a TrustedPlayer with timestamp
     * @param playerId Player ID
     * @param playerName Player name
     * @param type Player type
     * @param timestamp Timestamp
     * @param addedBy Added by player ID
     *
     * Создает TrustedPlayer с временной меткой
     * @param playerId ID игрока
     * @param playerName Имя игрока
     * @param type Тип игрока
     * @param timestamp Временная метка
     * @param addedBy ID игрока, который добавил
     *
     * Erstellt einen TrustedPlayer mit Zeitstempel
     * @param playerId Spieler-ID
     * @param playerName Spielername
     * @param type Spielertyp
     * @param timestamp Zeitstempel
     * @param addedBy Hinzugefügt von Spieler-ID
     */
    public TrustedPlayer(UUID playerId, String playerName, TrustedPlayerType type, long timestamp, UUID addedBy) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.type = type;
        this.timestamp = timestamp;
        this.addedBy = addedBy;
    }

    /**
     * Gets the player ID
     * @return Player ID
     *
     * Получает ID игрока
     * @return ID игрока
     *
     * Ruft die Spieler-ID ab
     * @return Spieler-ID
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Gets the player name
     * @return Player name
     *
     * Получает имя игрока
     * @return Имя игрока
     *
     * Ruft den Spielernamen ab
     * @return Spielername
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the player type
     * @return Player type
     *
     * Получает тип игрока
     * @return Тип игрока
     *
     * Ruft den Spielertyp ab
     * @return Spielertyp
     */
    public TrustedPlayerType getType() {
        return type;
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
     * @return The timestamp when this player was added as a trusted player
     *
     * @return Временная метка, когда этот игрок был добавлен как доверенный игрок
     *
     * @return Der Zeitstempel, wann dieser Spieler als vertrauenswürdiger Spieler hinzugefügt wurde
     */
    public long getAddedAt() {
        return timestamp;
    }

    /**
     * Gets the ID of the player who added this trusted player
     * @return Added by player ID
     *
     * Получает ID игрока, который добавил этого доверенного игрока
     * @return ID игрока, который добавил
     *
     * Ruft die ID des Spielers ab, der diesen vertrauenswürdigen Spieler hinzugefügt hat
     * @return Hinzugefügt von Spieler-ID
     */
    public UUID getAddedBy() {
        return addedBy;
    }
    
    /**
     * Gets the name of the player who added this trusted player
     * @return Added by player name
     *
     * Получает имя игрока, который добавил этого доверенного игрока
     * @return Имя игрока, который добавил
     *
     * Ruft den Namen des Spielers ab, der diesen vertrauenswürdigen Spieler hinzugefügt hat
     * @return Hinzugefügt von Spielername
     */
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