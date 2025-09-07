package com.megacreative.coding;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of all block types available in the coding system.
 * Organized into categories for convenience.
 * Updated to work with the new configuration-driven approach while maintaining backward compatibility.
 */
public enum BlockType {
    // Player Events (green blocks)
    EVENT_PLAYER_JOIN(Material.PLAYER_HEAD, "onPlayerJoin"),      // Player joins the server
    EVENT_PLAYER_QUIT(Material.LIME_CONCRETE, "onPlayerQuit"),    // Player leaves the server
    EVENT_PLAYER_INTERACT(Material.STICK, "onPlayerInteract"),    // Player right-clicks
    EVENT_PLAYER_MOVE(Material.DIAMOND_BLOCK, "onPlayerMove"),    // Player moves
    EVENT_PLAYER_CHAT(Material.PAPER, "onPlayerChat"),            // Player sends message
    EVENT_PLAYER_DEATH(Material.BONE, "onPlayerDeath"),           // Player dies
    EVENT_PLAYER_RESPAWN(Material.END_STONE, "onPlayerRespawn"),  // Player respawns

    // Player Conditions (blue blocks)
    CONDITION_HAS_PERMISSION(Material.KNOWLEDGE_BOOK, "hasPermission"),
    CONDITION_HAS_ITEM(Material.CHEST, "hasItem"),
    CONDITION_IS_IN_WORLD(Material.GRASS_BLOCK, "isInWorld"),
    CONDITION_COMPARE_VARIABLE(Material.COMPARATOR, "compareVariable"),

    // Player Actions (red blocks)
    ACTION_SEND_MESSAGE(Material.PAPER, "sendMessage"),
    ACTION_TELEPORT_PLAYER(Material.ENDER_PEARL, "teleportPlayer"),
    ACTION_GIVE_ITEM(Material.CHEST, "giveItem"),
    ACTION_PLAY_SOUND(Material.NOTE_BLOCK, "playSound"),

    // Variable Actions (yellow blocks)
    ACTION_SET_VARIABLE(Material.REDSTONE_TORCH, "setVariable"),
    ACTION_GET_VARIABLE(Material.REPEATER, "getVariable"),

    // Control Flow (purple blocks)
    CONTROL_IF_CONDITION(Material.STONE_BUTTON, "ifCondition"),
    CONTROL_ELSE_CONDITION(Material.STONE_PRESSURE_PLATE, "elseCondition"),

    // Game Actions (orange blocks)
    ACTION_SPAWN_MOB(Material.ZOMBIE_SPAWN_EGG, "spawnMob"),
    ACTION_CREATE_EXPLOSION(Material.TNT, "createExplosion"),
    ACTION_SET_WEATHER(Material.CLOCK, "setWeather"),
    ACTION_SET_TIME(Material.CLOCK, "setTime");

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

    // Default constructor for enum constants without action name
    BlockType(Material material) {
        this(material, name().toLowerCase().replace('_', ' '));
    }
    
    // Constructor with material and action name
    BlockType(Material material, String actionName) {
        if (material == null) {
            throw new IllegalArgumentException("Material cannot be null");
        }
        this.material = material;
        this.actionName = actionName != null ? actionName : name().toLowerCase().replace('_', ' ');
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
    
    /**
     * Checks if this block type is an event block
     * @return true if this is an event block
     */
    public boolean isEvent() {
        return this.name().startsWith("EVENT_");
    }
    
    /**
     * Checks if this block type is a condition block
     * @return true if this is a condition block
     */
    public boolean isCondition() {
        return this.name().startsWith("CONDITION_");
    }
    
    /**
     * Checks if this block type is an action block
     * @return true if this is an action block
     */
    public boolean isAction() {
        return this.name().startsWith("ACTION_");
    }
    
    /**
     * Checks if this block type is a control block
     * @return true if this is a control block
     */
    public boolean isControl() {
        return this.name().startsWith("CONTROL_");
    }
    
    /**
     * Gets the block type category
     * @return The category as a string
     */
    public String getCategory() {
        if (isEvent()) return "EVENT";
        if (isCondition()) return "CONDITION";
        if (isControl()) return "CONTROL";
        return "ACTION"; // Default to action
    }
}