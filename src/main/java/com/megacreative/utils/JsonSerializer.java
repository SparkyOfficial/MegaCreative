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
 * Утилитный класс для сериализации объектов в JSON с помощью Gson
 * Заменяет ручную сериализацию на более надежную и автоматическую
 *
 * Utility class for serializing objects to JSON using Gson
 * Replaces manual serialization with more reliable and automatic approach
 *
 * Utility-Klasse für die Serialisierung von Objekten zu JSON mit Gson
 * Ersetzt die manuelle Serialisierung durch einen zuverlässigeren und automatischen Ansatz
 */
public class JsonSerializer {
    
    // Стандартный Gson для объектов без необходимости сериализации ItemStack
    // Standard Gson for objects without ItemStack serialization needs
    // Standard-Gson für Objekte ohne ItemStack-Serialisierungsbedarf
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    
    // Расширенный Gson с поддержкой сериализации ItemStack для CodeBlocks
    // Enhanced Gson with ItemStack serialization support for CodeBlocks
    // Erweiterter Gson mit ItemStack-Serialisierungsunterstützung für CodeBlocks
    private static final Gson gsonWithItemStacks = ConfigItemsTypeAdapters.createGsonWithAdapters();
    
    /**
     * Сериализует объект в JSON строку
     * @param obj Объект для сериализации
     * @return JSON строка
     *
     * Serializes object to JSON string
     * @param obj Object to serialize
     * @return JSON string
     *
     * Serialisiert das Objekt in eine JSON-Zeichenfolge
     * @param obj Zu serialisierendes Objekt
     * @return JSON-Zeichenfolge
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
    
    /**
     * Десериализует JSON строку в объект указанного типа
     * @param json JSON строка для десериализации
     * @param classOfT Класс целевого объекта
     * @return Десериализованный объект
     *
     * Deserializes JSON string to object of specified type
     * @param json JSON string to deserialize
     * @param classOfT Target object class
     * @return Deserialized object
     *
     * Deserialisiert eine JSON-Zeichenfolge in ein Objekt des angegebenen Typs
     * @param json Zu deserialisierende JSON-Zeichenfolge
     * @param classOfT Zielobjektklasse
     * @return Deserialisiertes Objekt
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
    
    /**
     * Сериализует CreativeWorld в JSON используя безопасный DTO
     * @param world CreativeWorld для сериализации
     * @return JSON строка
     *
     * Serializes CreativeWorld to JSON using safe DTO
     * @param world CreativeWorld to serialize
     * @return JSON string
     *
     * Serialisiert CreativeWorld zu JSON unter Verwendung eines sicheren DTO
     * @param world Zu serialisierende CreativeWorld
     * @return JSON-Zeichenfolge
     */
    public static String serializeWorld(CreativeWorld world) {
        // Используем DTO для избежания проблем с Java 9+ модулями
        // Use DTO to avoid problems with Java 9+ modules
        // Verwenden Sie DTO, um Probleme mit Java 9+-Modulen zu vermeiden
        com.megacreative.models.CreativeWorldData worldData = new com.megacreative.models.CreativeWorldData(world);
        return toJson(worldData);
    }
    
    /**
     * Десериализует CreativeWorld из JSON
     * @param json JSON строка для десериализации
     * @param plugin Экземпляр основного плагина
     * @return Десериализованный CreativeWorld или null при ошибке
     *
     * Deserializes CreativeWorld from JSON
     * @param json JSON string to deserialize
     * @param plugin Main plugin instance
     * @return Deserialized CreativeWorld or null on error
     *
     * Deserialisiert CreativeWorld aus JSON
     * @param json Zu deserialisierende JSON-Zeichenfolge
     * @param plugin Hauptplugin-Instanz
     * @return Deserialisierte CreativeWorld oder null bei Fehler
     */
    public static CreativeWorld deserializeWorld(String json, MegaCreative plugin) {
        try {
            com.megacreative.models.CreativeWorldData worldData = fromJson(json, com.megacreative.models.CreativeWorldData.class);
            if (worldData == null) return null;
            
            // Создаем полноценный CreativeWorld из данных
            // Create a full CreativeWorld from data
            // Erstellen Sie eine vollständige CreativeWorld aus Daten
            CreativeWorld world = new CreativeWorld(worldData.id, worldData.name, worldData.ownerId, worldData.ownerName, worldData.worldType);
            
            // Восстанавливаем все поля
            // Restore all fields
            // Alle Felder wiederherstellen
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
            // Restore scripts
            // Skripte wiederherstellen
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
            // 🎆 УЛУЧШЕНО: Восстановление данных двойного мира
            // 🎆 VERBESSERT: Dual-World-Daten wiederherstellen
            if (worldData.pairedWorldId != null) {
                world.setPairedWorldId(worldData.pairedWorldId);
            }
            if (worldData.dualMode != null) {
                world.setDualMode(worldData.dualMode);
            }
            
            return world;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to deserialize CreativeWorld: " + e.getMessage());
            // Не удалось десериализовать CreativeWorld:
            // Fehler beim Deserialisieren von CreativeWorld:
            return null;
        }
    }
    
    /**
     * Сериализует CodeScript в JSON
     * @param script CodeScript для сериализации
     * @return JSON строка
     *
     * Serializes CodeScript to JSON
     * @param script CodeScript to serialize
     * @return JSON string
     *
     * Serialisiert CodeScript zu JSON
     * @param script Zu serialisierendes CodeScript
     * @return JSON-Zeichenfolge
     */
    public static String serializeScript(com.megacreative.coding.CodeScript script) {
        return toJson(script);
    }
    
    /**
     * Десериализует CodeScript из JSON
     * @param json JSON строка для десериализации
     * @return Десериализованный CodeScript
     *
     * Deserializes CodeScript from JSON
     * @param json JSON string to deserialize
     * @return Deserialized CodeScript
     *
     * Deserialisiert CodeScript aus JSON
     * @param json Zu deserialisierende JSON-Zeichenfolge
     * @return Deserialisiertes CodeScript
     */
    public static com.megacreative.coding.CodeScript deserializeScript(String json) {
        return fromJson(json, com.megacreative.coding.CodeScript.class);
    }
    
    /**
     * Сериализует CodeBlock в JSON с поддержкой ItemStack
     * @param block CodeBlock для сериализации
     * @return JSON строка
     *
     * Serializes CodeBlock to JSON with ItemStack support
     * @param block CodeBlock to serialize
     * @return JSON string
     *
     * Serialisiert CodeBlock zu JSON mit ItemStack-Unterstützung
     * @param block Zu serialisierender CodeBlock
     * @return JSON-Zeichenfolge
     */
    public static String serializeBlock(com.megacreative.coding.CodeBlock block) {
        return gsonWithItemStacks.toJson(block);
    }
    
    /**
     * Десериализует CodeBlock из JSON с поддержкой ItemStack
     * @param json JSON строка для десериализации
     * @return Десериализованный CodeBlock
     *
     * Deserializes CodeBlock from JSON with ItemStack support
     * @param json JSON string to deserialize
     * @return Deserialized CodeBlock
     *
     * Deserialisiert CodeBlock aus JSON mit ItemStack-Unterstützung
     * @param json Zu deserialisierende JSON-Zeichenfolge
     * @return Deserialisierter CodeBlock
     */
    public static com.megacreative.coding.CodeBlock deserializeBlock(String json) {
        return gsonWithItemStacks.fromJson(json, com.megacreative.coding.CodeBlock.class);
    }
    
    /**
     * Рекурсивный метод для восстановления CodeBlock из CodeBlockData
     * @param data CodeBlockData для восстановления
     * @return Восстановленный CodeBlock
     *
     * Recursive method to restore CodeBlock from CodeBlockData
     * @param data CodeBlockData to restore
     * @return Restored CodeBlock
     *
     * Rekursive Methode zur Wiederherstellung von CodeBlock aus CodeBlockData
     * @param data Zu wiederherstellende CodeBlockData
     * @return Wiederhergestellter CodeBlock
     */
    private static com.megacreative.coding.CodeBlock restoreCodeBlock(CodeBlockData data) {
        if (data == null) return null;

        Material material = data.materialName != null ? Material.getMaterial(data.materialName) : Material.STONE;
        com.megacreative.coding.CodeBlock block = new com.megacreative.coding.CodeBlock(material.name(), data.action);
        block.setId(data.id);

        // Восстанавливаем параметры
        // Restore parameters
        // Parameter wiederherstellen
        if (data.parameters != null) {
            data.parameters.forEach((key, value) -> {
                block.setParameter(key, DataValue.fromObject(value));
            });
        }

        // Восстанавливаем configItems из сериализованной карты
        // Restore configItems from serialized map
        // configItems aus serialisierter Karte wiederherstellen
        if (data.configItems != null) {
            data.configItems.forEach((slot, map) -> {
                try {
                    ItemStack itemStack = ItemStack.deserialize(map);
                    block.setConfigItem(slot, itemStack);
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем работу
                    // Log error but continue working
                    // Fehler protokollieren, aber weiterarbeiten
                    System.err.println("Failed to deserialize ItemStack: " + e.getMessage());
                    // Не удалось десериализовать ItemStack:
                    // Fehler beim Deserialisieren von ItemStack:
                    System.err.println("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
                    // Трассировка стека:
                    // Stapelverfolgung:
                }
            });
        }
        
        // Восстанавливаем скобки
        // Restore brackets
        // Klammern wiederherstellen
        if (data.bracketType != null) {
            try {
                block.setBracketType(com.megacreative.coding.CodeBlock.BracketType.valueOf(data.bracketType));
            } catch (IllegalArgumentException e) {
                // Игнорируем неверные значения
                // Ignore invalid values
                // Ungültige Werte ignorieren
            }
        }
        
        // Рекурсивно восстанавливаем следующие и дочерние блоки
        // Recursively restore next and child blocks
        // Rekursiv nächste und untergeordnete Blöcke wiederherstellen
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