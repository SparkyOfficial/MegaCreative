package com.megacreative.coding;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Перечисление всех типов блоков, доступных в системе кодирования.
 * Разделены на категории для удобства.
 */
public enum BlockType {
    // События игрока (зеленые блоки)
    EVENT_PLAYER_JOIN(Material.LIME_CONCRETE, "onJoin"),      // Игрок входит в мир
    EVENT_PLAYER_QUIT(Material.LIME_CONCRETE, "onQuit"),      // Игрок выходит из мира
    EVENT_PLAYER_INTERACT(Material.LIME_CONCRETE, "onInteract"),  // Игрок кликает ПКМ
    EVENT_PLAYER_MOVE(Material.LIME_CONCRETE, "onMove"),      // Игрок перемещается
    EVENT_PLAYER_CHAT(Material.LIME_CONCRETE, "onChat"),      // Игрок отправляет сообщение
    EVENT_PLAYER_DEATH(Material.LIME_CONCRETE, "onDeath"),     // Игрок умирает
    EVENT_PLAYER_RESPAWN(Material.LIME_CONCRETE, "onRespawn"),   // Игрок респаунится

    // Условия игрока (синие)
    CONDITION_HAS_ITEM(Material.BLUE_CONCRETE, "hasItem"),
    CONDITION_PLAYER_HEALTH(Material.BLUE_CONCRETE, "checkHealth"),
    CONDITION_PLAYER_GAMEMODE(Material.BLUE_CONCRETE, "checkGamemode"),
    CONDITION_PLAYER_LOCATION(Material.BLUE_CONCRETE, "checkLocation"),
    CONDITION_VARIABLE_EQUALS(Material.BLUE_CONCRETE, "varEquals"),
    CONDITION_IS_IN_REGION(Material.BLUE_CONCRETE, "inRegion"),
    CONDITION_HAS_PERMISSION(Material.BLUE_CONCRETE, "hasPermission"),

    // Действия игрока (красные)
    ACTION_SEND_MESSAGE(Material.RED_CONCRETE, "sendMessage"),
    ACTION_TELEPORT_PLAYER(Material.RED_CONCRETE, "teleport"),
    ACTION_GIVE_ITEM(Material.RED_CONCRETE, "giveItem"),
    ACTION_SET_HEALTH(Material.RED_CONCRETE, "setHealth"),
    ACTION_SET_GAMEMODE(Material.RED_CONCRETE, "setGamemode"),
    ACTION_PLAY_SOUND(Material.RED_CONCRETE, "playSound"),

    // Переменные (желтые)
    VARIABLE_SET(Material.YELLOW_CONCRETE, "setVariable"),
    VARIABLE_GET(Material.YELLOW_CONCRETE, "getVariable"),
    VARIABLE_ADD(Material.YELLOW_CONCRETE, "addToVariable"),
    VARIABLE_SUBTRACT(Material.YELLOW_CONCRETE, "subtractFromVariable"),

    // Условные блоки (фиолетовые)
    IF_CONDITION(Material.PURPLE_CONCRETE, "if"),
    ELSE_CONDITION(Material.PURPLE_CONCRETE, "else"),

    // Игровые действия (оранжевые)
    GAME_ACTION_SPAWN_MOB(Material.ORANGE_CONCRETE, "spawnMob"),
    GAME_ACTION_EXPLOSION(Material.ORANGE_CONCRETE, "createExplosion"),
    GAME_ACTION_WEATHER(Material.ORANGE_CONCRETE, "setWeather"),
    GAME_ACTION_TIME(Material.ORANGE_CONCRETE, "setTime"),
    GAME_ACTION_BROADCAST(Material.ORANGE_CONCRETE);

    private final Material material;
    private final String actionName;
    private static final Map<String, BlockType> BY_ACTION_NAME = new HashMap<>();
    private static final Map<Material, Map<String, BlockType>> BY_MATERIAL_AND_ACTION = new HashMap<>();

    static {
        // Initialize lookup maps
        for (BlockType type : values()) {
            BY_ACTION_NAME.put(type.getActionName().toLowerCase(), type);
            
            BY_MATERIAL_AND_ACTION
                .computeIfAbsent(type.material, k -> new HashMap<>())
                .put(type.actionName.toLowerCase(), type);
        }
    }

    BlockType(Material material, String actionName) {
        this.material = material;
        this.actionName = actionName;
    }

    /**
     * Gets the material associated with this block type
     * @return The Bukkit Material
     */
    public Material getMaterial() {
        return material;
    }
    
    /**
     * Gets the action name used in configuration files
     * @return The action name as a string
     */
    public String getActionName() {
        return actionName;
    }
    
    /**
     * Finds a BlockType by its material and action name
     * @param material The material to search for
     * @param actionName The action name to search for (case-insensitive)
     * @return The matching BlockType, or null if not found
     */
    @Nullable
    public static BlockType getByMaterialAndAction(Material material, String actionName) {
        if (material == null || actionName == null) {
            return null;
        }
        
        Map<String, BlockType> actions = BY_MATERIAL_AND_ACTION.get(material);
        return actions != null ? actions.get(actionName.toLowerCase()) : null;
    }
    
    /**
     * Finds a BlockType by its action name
     * @param actionName The action name to search for (case-insensitive)
     * @return The matching BlockType, or null if not found
     */
    @Nullable
    public static BlockType getByActionName(String actionName) {
        return actionName != null ? BY_ACTION_NAME.get(actionName.toLowerCase()) : null;
    }
}