package com.megacreative.coding.script;

import com.megacreative.coding.events.EventAction;
import com.megacreative.coding.events.EventCondition;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.values.DataValue;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages script creation, storage, and execution.
 * This class handles saving and loading scripts for different worlds,
 * as well as script debugging and variable tracking.
 */
public class ScriptManager {
    private final Map<UUID, ScriptBuilder> activeBuilders;
    private final Map<String, Map<String, ScriptBuilder>> worldScripts;
    private final File scriptsFolder;
    
    public ScriptManager(File dataFolder) {
        this.activeBuilders = new HashMap<>();
        this.worldScripts = new HashMap<>();
        this.scriptsFolder = new File(dataFolder, "scripts");
        if (!scriptsFolder.exists() && !scriptsFolder.mkdirs()) {
            System.err.println("Failed to create scripts directory: " + scriptsFolder.getAbsolutePath());
        }
    }
    
    /**
     * Creates a new script builder for a player
     */
    public ScriptBuilder createBuilder(Player player) {
        ScriptBuilder builder = new ScriptBuilder(player);
        activeBuilders.put(player.getUniqueId(), builder);
        return builder;
    }
    
    /**
     * Gets the active script builder for a player
     */
    public ScriptBuilder getBuilder(Player player) {
        return activeBuilders.get(player.getUniqueId());
    }
    
    /**
     * Saves a script for a specific world
     */
    public void saveScript(World world, String name, ScriptBuilder builder) {
        File worldFolder = new File(scriptsFolder, world.getName());
        if (!worldFolder.exists() && !worldFolder.mkdirs()) {
            System.err.println("Failed to create world scripts directory: " + worldFolder.getAbsolutePath());
            return;
        }
        
        File scriptFile = new File(worldFolder, name + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        
        config.set("name", name);
        config.set("world", world.getName());
        config.set("created", System.currentTimeMillis());
        
        
        Map<String, Object> variables = builder.getVariables();
        config.createSection("variables", variables);
        
        
        List<ScriptBlock> blocks = builder.getBlocks();
        List<Map<String, Object>> serializedBlocks = new ArrayList<>();
        
        for (int i = 0; i < blocks.size(); i++) {
            ScriptBlock block = blocks.get(i);
            Map<String, Object> blockData = serializeScriptBlock(block, i);
            serializedBlocks.add(blockData);
        }
        
        config.set("blocks", serializedBlocks);
        
        try {
            config.save(scriptFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Map<String, ScriptBuilder> worldScriptMap = worldScripts.computeIfAbsent(
            world.getName(),
            k -> new HashMap<>()
        );
        worldScriptMap.put(name, builder);
    }
    
    /**
     * Loads a script for a specific world
     */
    public ScriptBuilder loadScript(World world, String name) {
        File scriptFile = new File(scriptsFolder, world.getName() + "/" + name + ".yml");
        if (!scriptFile.exists()) {
            return null;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(scriptFile);
        
        
        Player owner = null; 
        ScriptBuilder builder = new ScriptBuilder(owner);
        
        
        ConfigurationSection variablesSection = config.getConfigurationSection("variables");
        if (variablesSection != null) {
            for (String key : variablesSection.getKeys(false)) {
                builder.setVariable(key, variablesSection.get(key));
            }
        }
        
        
        List<Map<?, ?>> blocksList = config.getMapList("blocks");
        Map<Integer, ScriptBlock> blockMap = new HashMap<>();
        
        
        for (Map<?, ?> blockData : blocksList) {
            int id = (Integer) blockData.get("id");
            ScriptBlock block = deserializeScriptBlock(blockData);
            if (block != null) {
                blockMap.put(id, block);
                builder.addBlock(block);
            }
        }
        
        
        for (Map<?, ?> blockData : blocksList) {
            int id = (Integer) blockData.get("id");
            ScriptBlock block = blockMap.get(id);
            
            if (block != null) {
                
                if (blockData.containsKey("nextBlockId")) {
                    int nextId = (Integer) blockData.get("nextBlockId");
                    ScriptBlock nextBlock = blockMap.get(nextId);
                    if (nextBlock != null) {
                        block.setNextBlock(nextBlock);
                    }
                }
                
                
                if (blockData.containsKey("elseBlockId")) {
                    int elseId = (Integer) blockData.get("elseBlockId");
                    ScriptBlock elseBlock = blockMap.get(elseId);
                    if (elseBlock != null) {
                        block.setElseBlock(elseBlock);
                    }
                }
            }
        }
        
        
        Map<String, ScriptBuilder> worldScriptMap = worldScripts.computeIfAbsent(
            world.getName(),
            k -> new HashMap<>()
        );
        worldScriptMap.put(name, builder);
        
        return builder;
    }
    
    /**
     * Executes all scripts in a world for an event
     */
    public void executeWorldScripts(World world, GameEvent event) {
        Map<String, ScriptBuilder> worldScriptMap = worldScripts.get(world.getName());
        if (worldScriptMap != null) {
            for (ScriptBuilder builder : worldScriptMap.values()) {
                builder.execute(event);
            }
        }
    }
    
    /**
     * Gets the debug information for a script
     */
    public Map<String, Object> getDebugInfo(World world, String scriptName) {
        Map<String, ScriptBuilder> worldScriptMap = worldScripts.get(world.getName());
        if (worldScriptMap != null) {
            ScriptBuilder builder = worldScriptMap.get(scriptName);
            if (builder != null) {
                return builder.getVariables();
            }
        }
        return new HashMap<>();
    }
    
    /**
     * Cleans up resources when a player leaves
     */
    public void cleanup(Player player) {
        activeBuilders.remove(player.getUniqueId());
    }
    
    /**
     * Сериализует блок скрипта в Map для сохранения в YAML
     * @param block Блок скрипта для сериализации
     * @param id Идентификатор блока
     * @return Map с сериализованными данными блока
     */
    private Map<String, Object> serializeScriptBlock(ScriptBlock block, int id) {
        Map<String, Object> blockData = new HashMap<>();
        blockData.put("id", id);
        blockData.put("type", block.getType().name());
        
        
        if (block.getNextBlock() != null) {
            
            
            
            blockData.put("nextBlockId", id + 1);
        }
        
        if (block.getElseBlock() != null) {
            
            
            blockData.put("elseBlockId", id + 2);
        }
        
        
        Object content = block.getContent();
        if (content instanceof EventCondition) {
            EventCondition condition = (EventCondition) content;
            blockData.put("contentType", "condition");
            blockData.put("contentName", condition.getName());
            blockData.put("contentDescription", condition.getDescription());
            
            
            Map<String, Object> params = new HashMap<>();
            for (Map.Entry<String, DataValue> entry : condition.getRequiredVariables().entrySet()) {
                params.put(entry.getKey(), entry.getValue().getValue());
            }
            blockData.put("contentParams", params);
        } else if (content instanceof EventAction) {
            EventAction action = (EventAction) content;
            blockData.put("contentType", "action");
            blockData.put("contentName", action.getName());
            blockData.put("contentDescription", action.getDescription());
            
            
            Map<String, Object> params = new HashMap<>();
            for (Map.Entry<String, DataValue> entry : action.getParameters().entrySet()) {
                params.put(entry.getKey(), entry.getValue().getValue());
            }
            blockData.put("contentParams", params);
        }
        
        return blockData;
    }
    
    /**
     * Десериализует блок скрипта из Map
     * @param blockData Map с сериализованными данными блока
     * @return Десериализованный блок скрипта
     */
    private ScriptBlock deserializeScriptBlock(Map<?, ?> blockData) {
        String typeStr = (String) blockData.get("type");
        ScriptBlockType type = ScriptBlockType.valueOf(typeStr);
        
        String contentType = (String) blockData.get("contentType");
        String contentName = (String) blockData.get("contentName");
        String contentDescription = (String) blockData.get("contentDescription");
        
        
        if ("condition".equals(contentType)) {
            
            
            EventCondition condition = new EventCondition(
                contentName,
                contentDescription,
                event -> true 
            );
            
            
            Map<?, ?> params = (Map<?, ?>) blockData.get("contentParams");
            if (params != null) {
                for (Map.Entry<?, ?> entry : params.entrySet()) {
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();
                    
                    
                }
            }
            
            return new ScriptBlock(type, condition);
        } else if ("action".equals(contentType)) {
            
            
            EventAction action = new EventAction(
                contentName,
                contentDescription,
                event -> {} 
            );
            
            
            Map<?, ?> params = (Map<?, ?>) blockData.get("contentParams");
            if (params != null) {
                for (Map.Entry<?, ?> entry : params.entrySet()) {
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();
                    
                    
                }
            }
            
            return new ScriptBlock(type, action);
        }
        
        return null;
    }
}