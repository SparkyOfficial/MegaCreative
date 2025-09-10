package com.megacreative.coding.values;

import org.bukkit.entity.Player;
import java.util.Map;
import java.util.HashMap;

public class PlayerValue implements DataValue {
    private final Player player;

    public PlayerValue(Player player) {
        this.player = player;
    }

    public static PlayerValue of(Player player) {
        return new PlayerValue(player);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public ValueType getType() {
        return ValueType.PLAYER;
    }

    @Override
    public Object getValue() {
        return player;
    }

    @Override
    public void setValue(Object value) throws IllegalArgumentException {
        // PlayerValue is immutable, so we don't allow setting values
        throw new IllegalArgumentException("PlayerValue is immutable");
    }

    @Override
    public String asString() {
        if (player == null) {
            return "";
        }
        return player.getName();
    }

    @Override
    public Number asNumber() {
        return 0; // Player doesn't convert to a number
    }

    @Override
    public boolean asBoolean() {
        return player != null;
    }

    @Override
    public boolean isEmpty() {
        return player == null;
    }

    @Override
    public boolean isValid() {
        return player != null && player.isOnline();
    }

    @Override
    public String getDescription() {
        if (player == null) {
            return "No player";
        }
        return "Player: " + player.getName();
    }

    @Override
    public DataValue clone() {
        // Player objects can't be cloned, so we return the same instance
        return new PlayerValue(player);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "PLAYER");
        // We don't serialize the player object directly as it's not serializable
        map.put("value", player != null ? player.getUniqueId().toString() : null);
        return map;
    }
}