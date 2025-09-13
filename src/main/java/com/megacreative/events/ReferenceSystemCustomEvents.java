package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.Location;

import java.util.Map;

/**
 * 🎆 Custom Reference System-style events for enhanced functionality
 */
public class ReferenceSystemCustomEvents {

    public static class PlayerEnterRegionEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String regionName;
        private final Location location;

        public PlayerEnterRegionEvent(Player player, String regionName, Location location) {
            this.player = player;
            this.regionName = regionName;
            this.location = location;
        }

        public Player getPlayer() { return player; }
        public String getRegionName() { return regionName; }
        public Location getLocation() { return location; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    public static class PlayerLeaveRegionEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String regionName;
        private final Location location;

        public PlayerLeaveRegionEvent(Player player, String regionName, Location location) {
            this.player = player;
            this.regionName = regionName;
            this.location = location;
        }

        public Player getPlayer() { return player; }
        public String getRegionName() { return regionName; }
        public Location getLocation() { return location; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    public static class PlayerVariableChangeEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String variableName;
        private final Object oldValue;
        private final Object newValue;

        public PlayerVariableChangeEvent(Player player, String variableName, Object oldValue, Object newValue) {
            this.player = player;
            this.variableName = variableName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public Player getPlayer() { return player; }
        public String getVariableName() { return variableName; }
        public Object getOldValue() { return oldValue; }
        public Object getNewValue() { return newValue; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    public static class TimerExpireEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String timerName;
        private final long duration;
        private final Object timerData;

        public TimerExpireEvent(Player player, String timerName, long duration, Object timerData) {
            this.player = player;
            this.timerName = timerName;
            this.duration = duration;
            this.timerData = timerData;
        }

        public Player getPlayer() { return player; }
        public String getTimerName() { return timerName; }
        public long getDuration() { return duration; }
        public Object getTimerData() { return timerData; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    public static class PlayerCustomActionEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String actionName;
        private final Map<String, Object> actionData;

        public PlayerCustomActionEvent(Player player, String actionName, Map<String, Object> actionData) {
            this.player = player;
            this.actionName = actionName;
            this.actionData = actionData;
        }

        public Player getPlayer() { return player; }
        public String getActionName() { return actionName; }
        public Map<String, Object> getActionData() { return actionData; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    public static class PlayerScoreChangeEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String scoreType;
        private final int oldScore;
        private final int newScore;
        private final String reason;

        public PlayerScoreChangeEvent(Player player, String scoreType, int oldScore, int newScore, String reason) {
            this.player = player;
            this.scoreType = scoreType;
            this.oldScore = oldScore;
            this.newScore = newScore;
            this.reason = reason;
        }

        public Player getPlayer() { return player; }
        public String getScoreType() { return scoreType; }
        public int getOldScore() { return oldScore; }
        public int getNewScore() { return newScore; }
        public String getReason() { return reason; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    public static class FunctionCallEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String functionName;
        private final Object[] parameters;

        public FunctionCallEvent(Player player, String functionName, Object[] parameters) {
            this.player = player;
            this.functionName = functionName;
            this.parameters = parameters;
        }

        public Player getPlayer() { return player; }
        public String getFunctionName() { return functionName; }
        public Object[] getParameters() { return parameters; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    public static class WorldModeChangeEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String worldId;
        private final String oldMode;
        private final String newMode;

        public WorldModeChangeEvent(Player player, String worldId, String oldMode, String newMode) {
            this.player = player;
            this.worldId = worldId;
            this.oldMode = oldMode;
            this.newMode = newMode;
        }

        public Player getPlayer() { return player; }
        public String getWorldId() { return worldId; }
        public String getOldMode() { return oldMode; }
        public String getNewMode() { return newMode; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }
}