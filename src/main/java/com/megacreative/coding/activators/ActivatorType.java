package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Enum representing different types of activators
 * Based on YottaCreative's ActivatorType but adapted for MegaCreative
 */
public enum ActivatorType {
    // Player events
    PLAYER_JOIN("Player Join", Material.OAK_DOOR, "Triggers when a player joins the world"),
    PLAYER_QUIT("Player Quit", Material.IRON_DOOR, "Triggers when a player leaves the world"),
    PLAYER_RIGHT_CLICK("Right Click", Material.WOODEN_SWORD, "Triggers when a player right-clicks"),
    PLAYER_LEFT_CLICK("Left Click", Material.STONE_SWORD, "Triggers when a player left-clicks"),
    PLAYER_KILL_PLAYER("Player Kill Player", Material.DIAMOND_SWORD, "Triggers when a player kills another player"),
    PLAYER_SNEAK("Player Sneak", Material.LEATHER_BOOTS, "Triggers when a player starts sneaking"),
    PLAYER_UNSNEAK("Player Unsneak", Material.CHAINMAIL_BOOTS, "Triggers when a player stops sneaking"),
    PLAYER_DAMAGE_PLAYER("Player Damage Player", Material.IRON_SWORD, "Triggers when a player damages another player"),
    PLAYER_TAKE_DAMAGE("Player Take Damage", Material.RED_DYE, "Triggers when a player takes damage"),
    PLAYER_KILL_MOB("Player Kill Mob", Material.GOLDEN_SWORD, "Triggers when a player kills a mob"),
    PLAYER_DAMAGE_MOB("Player Damage Mob", Material.STONE_SWORD, "Triggers when a player damages a mob"),
    PLAYER_PICKUP_ITEM("Player Pickup Item", Material.HOPPER, "Triggers when a player picks up an item"),
    PLAYER_DROP_ITEM("Player Drop Item", Material.DROPPER, "Triggers when a player drops an item"),
    PLAYER_CONSUME_ITEM("Player Consume Item", Material.APPLE, "Triggers when a player consumes an item"),
    PLAYER_CHAT("Player Chat", Material.BOOK, "Triggers when a player sends a chat message"),
    PLAYER_MOVE("Player Move", Material.FEATHER, "Triggers when a player moves"),
    PLAYER_DEATH("Player Death", Material.BONE, "Triggers when a player dies"),
    PLAYER_RESPAWN("Player Respawn", Material.TOTEM_OF_UNDYING, "Triggers when a player respawns"),
    PLAYER_TELEPORT("Player Teleport", Material.ENDER_PEARL, "Triggers when a player teleports"),
    ENTITY_PICKUP_ITEM("Entity Pickup Item", Material.HOPPER, "Triggers when an entity picks up an item"),
    CHAT("Chat", Material.BOOK, "Triggers when a player sends a chat message"),
    BLOCK_PLACE("Block Place", Material.STONE, "Triggers when a block is placed"),
    BLOCK_BREAK("Block Break", Material.STONE_PICKAXE, "Triggers when a block is broken"),
    INVENTORY_CLICK("Inventory Click", Material.CHEST, "Triggers when a player clicks in an inventory"),
    ENTITY_DAMAGE("Entity Damage", Material.DIAMOND_SWORD, "Triggers when an entity takes damage"),
    
    // Game events
    GAME_LOOP("Game Loop", Material.EMERALD_BLOCK, "Triggers repeatedly at specified intervals"),
    FUNCTION("Function", Material.LAPIS_BLOCK, "Represents a callable function"),
    TICK("Tick", Material.CLOCK, "Triggers on game ticks");
    
    private final String displayName;
    private final Material iconMaterial;
    private final String description;
    
    ActivatorType(String displayName, Material iconMaterial, String description) {
        this.displayName = displayName;
        this.iconMaterial = iconMaterial;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ItemStack getIcon() {
        return new ItemStack(iconMaterial);
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Creates a new activator instance of this type
     */
    public Activator createActivator(MegaCreative plugin, CreativeWorld world) {
        switch (this) {
            case GAME_LOOP:
                return new GameLoopActivator(plugin, world);
            case FUNCTION:
                return new FunctionActivator(plugin, world);
            case PLAYER_JOIN:
                return new PlayerJoinActivator(plugin, world);
            case PLAYER_QUIT:
                return new PlayerQuitActivator(plugin, world);
            case PLAYER_CHAT:
                return new PlayerChatActivator(plugin, world);
            case PLAYER_MOVE:
                return new PlayerMoveActivator(plugin, world);
            case BLOCK_PLACE:
                return new BlockPlaceActivator(plugin, world);
            case BLOCK_BREAK:
                return new BlockBreakActivator(plugin, world);
            case PLAYER_DEATH:
                return new PlayerDeathActivator(plugin, world);
            case PLAYER_RESPAWN:
                return new PlayerRespawnActivator(plugin, world);
            case ENTITY_PICKUP_ITEM:
                return new EntityPickupItemActivator(plugin, world);
            case CHAT:
                return new ChatActivator(plugin, world);
            default:
                // Default to a generic activator for other types
                return new GenericActivator(plugin, world, this);
        }
    }
}