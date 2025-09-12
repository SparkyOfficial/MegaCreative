package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 🎆 Custom FrameLand-style events for enhanced functionality
 */
public class FrameLandCustomEvents {
    
    /**
     * Event fired when a player variable changes
     */
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
    
    /**
     * Event fired when a player enters a specific region/area
     */
    public static class PlayerEnterRegionEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        
        private final Player player;
        private final String regionName;
        private final org.bukkit.Location enterLocation;
        
        public PlayerEnterRegionEvent(Player player, String regionName, org.bukkit.Location enterLocation) {
            this.player = player;
            this.regionName = regionName;
            this.enterLocation = enterLocation;
        }
        
        public Player getPlayer() { return player; }
        public String getRegionName() { return regionName; }
        public org.bukkit.Location getEnterLocation() { return enterLocation; }
        
        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }
    
    /**
     * Event fired when a player leaves a specific region/area
     */
    public static class PlayerLeaveRegionEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        
        private final Player player;
        private final String regionName;
        private final org.bukkit.Location leaveLocation;
        
        public PlayerLeaveRegionEvent(Player player, String regionName, org.bukkit.Location leaveLocation) {
            this.player = player;
            this.regionName = regionName;
            this.leaveLocation = leaveLocation;
        }
        
        public Player getPlayer() { return player; }
        public String getRegionName() { return regionName; }
        public org.bukkit.Location getLeaveLocation() { return leaveLocation; }
        
        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }
    
    /**
     * Event fired when a function is called
     */
    public static class FunctionCallEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        
        private final Player player;
        private final String functionName;
        private final Object[] parameters;
        private Object returnValue;
        
        public FunctionCallEvent(Player player, String functionName, Object[] parameters) {
            this.player = player;
            this.functionName = functionName;
            this.parameters = parameters != null ? parameters.clone() : new Object[0];
        }
        
        public Player getPlayer() { return player; }
        public String getFunctionName() { return functionName; }
        public Object[] getParameters() { return parameters.clone(); }
        public Object getReturnValue() { return returnValue; }
        public void setReturnValue(Object returnValue) { this.returnValue = returnValue; }
        
        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }
    
    /**
     * Event fired when a player's score changes
     */
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
        public int getScoreChange() { return newScore - oldScore; }
        public String getReason() { return reason; }
        
        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }
    
    /**
     * Event fired when a player triggers a custom action
     */
    public static class PlayerCustomActionEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        
        private final Player player;
        private final String actionName;
        private final java.util.Map<String, Object> actionData;
        
        public PlayerCustomActionEvent(Player player, String actionName, java.util.Map<String, Object> actionData) {
            this.player = player;
            this.actionName = actionName;
            this.actionData = new java.util.HashMap<>(actionData != null ? actionData : new java.util.HashMap<>());
        }
        
        public Player getPlayer() { return player; }
        public String getActionName() { return actionName; }
        public java.util.Map<String, Object> getActionData() { return new java.util.HashMap<>(actionData); }
        
        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }
    
    /**
     * Event fired when a timer expires
     */
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
    
    /**
     * Event fired when a world mode changes (dev/play switch)
     */
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