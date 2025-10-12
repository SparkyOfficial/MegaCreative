package com.megacreative.coding.events.extractors;

import com.megacreative.coding.events.AbstractEventDataExtractor;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Extracts data from PlayerJoinEvent
 */
public class PlayerJoinEventDataExtractor extends AbstractEventDataExtractor<PlayerJoinEvent> {
    
    public PlayerJoinEventDataExtractor() {
        super(PlayerJoinEvent.class);
    }
    
    @Override
    protected void initializeVariables() {
        
        registerVariable("playerName", "Name of the joining player");
        registerVariable("playerUUID", "UUID of the joining player");
        registerVariable("playerDisplayName", "Display name of the joining player");
        registerVariable("playerHealth", "Health of the joining player");
        registerVariable("playerFoodLevel", "Food level of the joining player");
        registerVariable("playerGameMode", "Game mode of the joining player");
        registerVariable("playerLevel", "Level of the joining player");
        registerVariable("playerExp", "Experience of the joining player");
        
        
        registerVariable("joinX", "X coordinate of join location");
        registerVariable("joinY", "Y coordinate of join location");
        registerVariable("joinZ", "Z coordinate of join location");
        registerVariable("joinWorld", "World name where player joined");
        registerVariable("joinLocation", "Complete join location as string");
        registerVariable("joinYaw", "Yaw rotation at join location");
        registerVariable("joinPitch", "Pitch rotation at join location");
        
        
        registerVariable("joinMessage", "Join message displayed to other players");
        registerVariable("firstTime", "Whether this is the player's first time joining");
        registerVariable("lastPlayed", "Timestamp of when player last played");
        registerVariable("playTime", "Total time player has played (in ticks)");
    }
    
    @Override
    public Map<String, DataValue> extractData(PlayerJoinEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        
        Player player = event.getPlayer();
        
        
        extractPlayerData(data, player);
        
        
        Location joinLocation = player.getLocation();
        extractLocationData(data, joinLocation, "join");
        
        
        data.put("joinMessage", DataValue.fromObject(event.getJoinMessage()));
        data.put("firstTime", DataValue.fromObject(!player.hasPlayedBefore()));
        data.put("lastPlayed", DataValue.fromObject(player.getLastPlayed()));
        data.put("playTime", DataValue.fromObject(player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE)));
        
        return data;
    }
}