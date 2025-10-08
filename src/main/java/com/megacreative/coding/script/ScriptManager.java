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
        if (!scriptsFolder.exists()) {
            scriptsFolder.mkdirs();
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
        if (!worldFolder.exists()) {
            worldFolder.mkdirs();
        }
        
        File scriptFile = new File(worldFolder, name + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        // Сохраняем основную информацию о скрипте
        config.set("name", name);
        config.set("world", world.getName());
        config.set("created", System.currentTimeMillis());
        
        // Сохраняем переменные скрипта
        Map<String, Object> variables = builder.getVariables();
        config.createSection("variables", variables);
        
        // Сохраняем блоки скрипта
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
        
        // Создаем нового строителя скриптов
        Player owner = null; // Владелец может быть null при загрузке из файла
        ScriptBuilder builder = new ScriptBuilder(owner);
        
        // Загружаем переменные
        ConfigurationSection variablesSection = config.getConfigurationSection("variables");
        if (variablesSection != null) {
            for (String key : variablesSection.getKeys(false)) {
                builder.setVariable(key, variablesSection.get(key));
            }
        }
        
        // Загружаем блоки скрипта
        List<Map<?, ?>> blocksList = config.getMapList("blocks");
        Map<Integer, ScriptBlock> blockMap = new HashMap<>();
        
        // Сначала создаем все блоки
        for (Map<?, ?> blockData : blocksList) {
            int id = (Integer) blockData.get("id");
            ScriptBlock block = deserializeScriptBlock(blockData);
            if (block != null) {
                blockMap.put(id, block);
                builder.addBlock(block);
            }
        }
        
        // Затем устанавливаем связи между блоками
        for (Map<?, ?> blockData : blocksList) {
            int id = (Integer) blockData.get("id");
            ScriptBlock block = blockMap.get(id);
            
            if (block != null) {
                // Устанавливаем следующий блок
                if (blockData.containsKey("nextBlockId")) {
                    int nextId = (Integer) blockData.get("nextBlockId");
                    ScriptBlock nextBlock = blockMap.get(nextId);
                    if (nextBlock != null) {
                        block.setNextBlock(nextBlock);
                    }
                }
                
                // Устанавливаем блок else для условий
                if (blockData.containsKey("elseBlockId")) {
                    int elseId = (Integer) blockData.get("elseBlockId");
                    ScriptBlock elseBlock = blockMap.get(elseId);
                    if (elseBlock != null) {
                        block.setElseBlock(elseBlock);
                    }
                }
            }
        }
        
        // Сохраняем скрипт в кэше
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
        
        // Сохраняем связи с другими блоками
        if (block.getNextBlock() != null) {
            // Здесь мы должны знать ID следующего блока
            // В реальной реализации нужно будет отслеживать ID всех блоков
            // Для простоты предположим, что следующий блок имеет ID + 1
            blockData.put("nextBlockId", id + 1);
        }
        
        if (block.getElseBlock() != null) {
            // Аналогично, для простоты используем условную логику
            // В реальной реализации нужно будет отслеживать ID всех блоков
            blockData.put("elseBlockId", id + 2);
        }
        
        // Сериализуем содержимое блока в зависимости от его типа
        Object content = block.getContent();
        if (content instanceof EventCondition) {
            EventCondition condition = (EventCondition) content;
            blockData.put("contentType", "condition");
            blockData.put("contentName", condition.getName());
            blockData.put("contentDescription", condition.getDescription());
            
            // Сохраняем параметры условия
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
            
            // Сохраняем параметры действия
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
        
        // Создаем содержимое блока в зависимости от его типа
        if ("condition".equals(contentType)) {
            // Для простоты создаем базовое условие
            // В реальной реализации нужно будет восстанавливать точный тип условия
            EventCondition condition = new EventCondition(
                contentName,
                contentDescription,
                event -> true // Заглушка для предиката
            );
            
            // Восстанавливаем параметры условия
            Map<?, ?> params = (Map<?, ?>) blockData.get("contentParams");
            if (params != null) {
                for (Map.Entry<?, ?> entry : params.entrySet()) {
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();
                    // Здесь нужно создать DataValue из value
                    // Для простоты опустим эту часть
                }
            }
            
            return new ScriptBlock(type, condition);
        } else if ("action".equals(contentType)) {
            // Для простоты создаем базовое действие
            // В реальной реализации нужно будет восстанавливать точный тип действия
            EventAction action = new EventAction(
                contentName,
                contentDescription,
                event -> {} // Заглушка для действия
            );
            
            // Восстанавливаем параметры действия
            Map<?, ?> params = (Map<?, ?>) blockData.get("contentParams");
            if (params != null) {
                for (Map.Entry<?, ?> entry : params.entrySet()) {
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();
                    // Здесь нужно создать DataValue из value
                    // Для простоты опустим эту часть
                }
            }
            
            return new ScriptBlock(type, action);
        }
        
        return null;
    }
}