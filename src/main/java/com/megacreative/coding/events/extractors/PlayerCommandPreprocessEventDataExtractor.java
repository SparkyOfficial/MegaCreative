package com.megacreative.coding.events.extractors;

import com.megacreative.coding.events.AbstractEventDataExtractor;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Extracts data from PlayerCommandPreprocessEvent
 */
public class PlayerCommandPreprocessEventDataExtractor extends AbstractEventDataExtractor<PlayerCommandPreprocessEvent> {
    
    public PlayerCommandPreprocessEventDataExtractor() {
        super(PlayerCommandPreprocessEvent.class);
    }
    
    @Override
    protected void initializeVariables() {
        
        registerVariable("playerName", "Name of the player who executed the command");
        registerVariable("playerUUID", "UUID of the player who executed the command");
        registerVariable("playerDisplayName", "Display name of the player");
        registerVariable("playerHealth", "Health of the player");
        registerVariable("playerFoodLevel", "Food level of the player");
        registerVariable("playerGameMode", "Game mode of the player");
        registerVariable("playerLevel", "Level of the player");
        registerVariable("playerExp", "Experience of the player");
        
        
        registerVariable("commandX", "X coordinate where command was executed");
        registerVariable("commandY", "Y coordinate where command was executed");
        registerVariable("commandZ", "Z coordinate where command was executed");
        registerVariable("commandWorld", "World where command was executed");
        registerVariable("commandLocation", "Complete command location as string");
        registerVariable("commandYaw", "Yaw rotation at command location");
        registerVariable("commandPitch", "Pitch rotation at command location");
        
        
        registerVariable("fullCommand", "Complete command with slash (e.g., '/help test')");
        registerVariable("command", "Command without slash (e.g., 'help test')");
        registerVariable("commandName", "Command name only (e.g., 'help')");
        registerVariable("commandArgs", "Command arguments only (e.g., 'test')");
        registerVariable("isCancelled", "Whether the command execution is cancelled");
    }
    
    @Override
    public Map<String, DataValue> extractData(PlayerCommandPreprocessEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        
        Player player = event.getPlayer();
        
        
        extractPlayerData(data, player);
        
        
        Location commandLocation = player.getLocation();
        extractLocationData(data, commandLocation, "command");
        
        
        String fullCommand = event.getMessage();
        String command = fullCommand.startsWith("/") ? fullCommand.substring(1) : fullCommand;
        String[] parts = command.split(" ", 2);
        String commandName = parts[0];
        String commandArgs = parts.length > 1 ? parts[1] : "";
        
        data.put("fullCommand", DataValue.fromObject(fullCommand));
        data.put("command", DataValue.fromObject(command));
        data.put("commandName", DataValue.fromObject(commandName));
        data.put("commandArgs", DataValue.fromObject(commandArgs));
        data.put("isCancelled", DataValue.fromObject(event.isCancelled()));
        
        return data;
    }
}