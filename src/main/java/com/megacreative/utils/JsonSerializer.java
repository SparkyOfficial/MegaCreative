package com.megacreative.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megacreative.coding.CodeBlockData;
import com.megacreative.models.CodeScriptData;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import com.megacreative.models.CreativeWorld;
import com.megacreative.MegaCreative;

/**
 * Утилитный класс для сериализации объектов в JSON с помощью Gson.
 * Заменяет ручную сериализацию на более надежную и автоматическую.
 */
public class JsonSerializer {
    
    // Standard Gson for objects without ItemStack serialization needs
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    
    // Enhanced Gson with ItemStack serialization support for CodeBlocks
    private static final Gson gsonWithItemStacks = ConfigItemsTypeAdapters.createGsonWithAdapters();
    
    /**
     * Сериализует объект в JSON строку
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
    
    /**
     * Десериализует JSON строку в объект указанного типа
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
    
    /**
     * Сериализует CreativeWorld в JSON используя безопасный DTO
     */
    public static String serializeWorld(CreativeWorld world) {
        // Используем DTO для избежания проблем с Java 9+ модулями
        com.megacreative.models.CreativeWorldData worldData = new com.megacreative.models.CreativeWorldData(world);
        return toJson(worldData);
    }
    
    /**
     * Десериализует CreativeWorld из JSON
     */
    public static CreativeWorld deserializeWorld(String json, MegaCreative plugin) {
        try {
            com.megacreative.models.CreativeWorldData worldData = fromJson(json, com.megacreative.models.CreativeWorldData.class);
            if (worldData == null) return null;
            
            // Создаем полноценный CreativeWorld из данных
            CreativeWorld world = new CreativeWorld(worldData.id, worldData.name, worldData.ownerId, worldData.ownerName, worldData.worldType);
            
            // Восстанавливаем все поля
            world.setDescription(worldData.description);
            world.setMode(worldData.mode);
            world.setPrivate(worldData.isPrivate);
            world.setCreatedTime(worldData.createdTime);
            world.setLastActivity(worldData.lastActivity);
            if (worldData.flags != null) world.setFlags(worldData.flags);
            if (worldData.trustedBuilders != null) world.setTrustedBuilders(worldData.trustedBuilders);
            if (worldData.trustedCoders != null) world.setTrustedCoders(worldData.trustedCoders);
            world.setLikes(worldData.likes);
            world.setDislikes(worldData.dislikes);
            if (worldData.likedBy != null) world.setLikedBy(worldData.likedBy);
            if (worldData.dislikedBy != null) world.setDislikedBy(worldData.dislikedBy);
            if (worldData.favoriteBy != null) world.setFavoriteBy(worldData.favoriteBy);
            if (worldData.comments != null) world.setComments(worldData.comments);
            // Восстанавливаем скрипты
            java.util.List<com.megacreative.coding.CodeScript> restoredScripts = new ArrayList<>();
            if (worldData.scripts != null) {
                for (CodeScriptData scriptData : worldData.scripts) {
                    com.megacreative.coding.CodeScript script = new com.megacreative.coding.CodeScript(
                        scriptData.name != null ? scriptData.name : "Unnamed Script",
                        scriptData.enabled,
                        restoreCodeBlock(scriptData.rootBlock)
                    );
                    script.setId(scriptData.id);
                    if (scriptData.type != null) {
                        script.setType(com.megacreative.coding.CodeScript.ScriptType.valueOf(scriptData.type));
                    }
                    restoredScripts.add(script);
                }
            }
            world.setScripts(restoredScripts);
            
            // 🎆 ENHANCED: Restore dual world data
            if (worldData.pairedWorldId != null) {
                world.setPairedWorldId(worldData.pairedWorldId);
            }
            if (worldData.dualMode != null) {
                world.setDualMode(worldData.dualMode);
            }
            
            return world;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to deserialize CreativeWorld: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Сериализует CodeScript в JSON
     */
    public static String serializeScript(com.megacreative.coding.CodeScript script) {
        return toJson(script);
    }
    
    /**
     * Десериализует CodeScript из JSON
     */
    public static com.megacreative.coding.CodeScript deserializeScript(String json) {
        return fromJson(json, com.megacreative.coding.CodeScript.class);
    }
    
    /**
     * Сериализует CodeBlock в JSON с поддержкой ItemStack
     */
    public static String serializeBlock(com.megacreative.coding.CodeBlock block) {
        return gsonWithItemStacks.toJson(block);
    }
    
    /**
     * Десериализует CodeBlock из JSON с поддержкой ItemStack
     */
    public static com.megacreative.coding.CodeBlock deserializeBlock(String json) {
        return gsonWithItemStacks.fromJson(json, com.megacreative.coding.CodeBlock.class);
    }
    
    /**
     * Рекурсивный метод для восстановления CodeBlock из CodeBlockData
     */
    private static com.megacreative.coding.CodeBlock restoreCodeBlock(CodeBlockData data) {
        if (data == null) return null;

        Material material = data.materialName != null ? Material.getMaterial(data.materialName) : Material.STONE;
        com.megacreative.coding.CodeBlock block = new com.megacreative.coding.CodeBlock(material, data.action);
        block.setId(data.id);

        // Восстанавливаем параметры
        if (data.parameters != null) {
            data.parameters.forEach((key, value) -> {
                block.setParameter(key, DataValue.fromObject(value));
            });
        }

        // Восстанавливаем configItems из сериализованной карты
        if (data.configItems != null) {
            data.configItems.forEach((slot, map) -> {
                try {
                    ItemStack itemStack = ItemStack.deserialize(map);
                    block.setConfigItem(slot, itemStack);
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем работу
                    System.err.println("Failed to deserialize ItemStack: " + e.getMessage());
                }
            });
        }
        
        // Восстанавливаем скобки
        if (data.bracketType != null) {
            try {
                block.setBracketType(com.megacreative.coding.CodeBlock.BracketType.valueOf(data.bracketType));
            } catch (IllegalArgumentException e) {
                // Игнорируем неверные значения
            }
        }
        
        // Рекурсивно восстанавливаем следующие и дочерние блоки
        if (data.nextBlock != null) {
            block.setNextBlock(restoreCodeBlock(data.nextBlock));
        }
        if (data.children != null) {
            for (CodeBlockData childData : data.children) {
                com.megacreative.coding.CodeBlock childBlock = restoreCodeBlock(childData);
                if (childBlock != null) {
                    block.addChild(childBlock);
                }
            }
        }

        return block;
    }
}