package com.megacreative.coding.events.extractors;

import com.megacreative.coding.events.AbstractEventDataExtractor;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Extracts data from PlayerDeathEvent
 */
public class PlayerDeathEventDataExtractor extends AbstractEventDataExtractor<PlayerDeathEvent> {
    
    public PlayerDeathEventDataExtractor() {
        super(PlayerDeathEvent.class);
    }
    
    @Override
    protected void initializeVariables() {
        // Player variables
        registerVariable("playerName", "Name of the player who died");
        registerVariable("playerUUID", "UUID of the player who died");
        registerVariable("playerDisplayName", "Display name of the player who died");
        registerVariable("playerHealth", "Health of the player before death");
        registerVariable("playerFoodLevel", "Food level of the player before death");
        registerVariable("playerGameMode", "Game mode of the player");
        registerVariable("playerLevel", "Level of the player");
        registerVariable("playerExp", "Experience of the player");
        
        // Death location variables
        registerVariable("deathX", "X coordinate of death location");
        registerVariable("deathY", "Y coordinate of death location");
        registerVariable("deathZ", "Z coordinate of death location");
        registerVariable("deathWorld", "World name where death occurred");
        registerVariable("deathLocation", "Complete death location as string");
        registerVariable("deathYaw", "Yaw rotation at death location");
        registerVariable("deathPitch", "Pitch rotation at death location");
        
        // Death specific variables
        registerVariable("deathMessage", "Death message displayed to players");
        registerVariable("deathCause", "Cause of death");
        registerVariable("keepInventory", "Whether player keeps inventory on death");
        registerVariable("keepLevel", "Whether player keeps level on death");
        registerVariable("newExp", "New experience value after death");
        registerVariable("newLevel", "New level value after death");
        registerVariable("newTotalExp", "New total experience after death");
    }
    
    @Override
    public Map<String, DataValue> extractData(PlayerDeathEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        
        Player player = event.getEntity();
        
        // Extract player data
        extractPlayerData(data, player);
        
        // Extract death location data
        Location deathLocation = player.getLocation();
        extractLocationData(data, deathLocation, "death");
        
        // Death specific data
        data.put("deathMessage", DataValue.fromObject(event.getDeathMessage()));
        data.put("deathCause", DataValue.fromObject(
            player.getLastDamageCause() != null ? 
            player.getLastDamageCause().getCause().name() : 
            "UNKNOWN"));
        data.put("keepInventory", DataValue.fromObject(event.getKeepInventory()));
        data.put("keepLevel", DataValue.fromObject(event.getKeepLevel()));
        data.put("newExp", DataValue.fromObject(event.getNewExp()));
        data.put("newLevel", DataValue.fromObject(event.getNewLevel()));
        data.put("newTotalExp", DataValue.fromObject(event.getNewTotalExp()));
        
        return data;
    }
}