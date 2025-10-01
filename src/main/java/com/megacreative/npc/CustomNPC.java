package com.megacreative.npc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import java.util.UUID;

/**
 * Represents a custom NPC (Non-Player Character) with enhanced functionality
 */
public class CustomNPC {
    private final UUID uniqueId;
    private final String name;
    private Location location;
    private Entity entity;
    private String skinName;
    private ItemStack[] equipment;
    private boolean gravity;
    private boolean visible;
    private boolean collidable;
    
    public CustomNPC(String name, Location location) {
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.location = location.clone();
        this.equipment = new ItemStack[6]; // Helmet, Chestplate, Leggings, Boots, Main Hand, Off Hand
        this.gravity = true;
        this.visible = true;
        this.collidable = true;
    }
    
    /**
     * Spawns the NPC in the world
     * @return true if successful, false otherwise
     */
    public boolean spawn() {
        // This would be implemented with a library like Citizens or custom entity creation
        // For now, we'll just mark it as spawned
        return true;
    }
    
    /**
     * Despawns the NPC from the world
     */
    public void despawn() {
        if (entity != null && !entity.isDead()) {
            entity.remove();
            entity = null;
        }
    }
    
    /**
     * Makes the NPC look at a player
     * @param player The player to look at
     */
    public void lookAt(Player player) {
        if (entity != null && player != null) {
            // Implementation would depend on the NPC library used
        }
    }
    
    /**
     * Makes the NPC walk to a location
     * @param target The target location
     */
    public void walkTo(Location target) {
        if (entity != null && target != null) {
            // Implementation would depend on the NPC library used
        }
    }
    
    /**
     * Makes the NPC play an animation
     * @param animation The animation to play
     */
    public void playAnimation(String animation) {
        if (entity != null) {
            // Implementation would depend on the NPC library used
        }
    }
    
    /**
     * Makes the NPC talk
     * @param message The message to say
     */
    public void talk(String message) {
        if (entity != null) {
            // Implementation would depend on the NPC library used
        }
    }
    
    /**
     * Sets the NPC's equipment
     * @param slot The equipment slot (0-5)
     * @param item The item to equip
     */
    public void setEquipment(int slot, ItemStack item) {
        if (slot >= 0 && slot < equipment.length) {
            equipment[slot] = item;
            if (entity != null) {
                // Update the entity's equipment
            }
        }
    }
    
    /**
     * Gets the NPC's equipment
     * @param slot The equipment slot (0-5)
     * @return The item in the slot
     */
    public ItemStack getEquipment(int slot) {
        if (slot >= 0 && slot < equipment.length) {
            return equipment[slot];
        }
        return null;
    }
    
    // Getters and setters
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location.clone();
        if (entity != null) {
            entity.teleport(location);
        }
    }
    
    public Entity getEntity() {
        return entity;
    }
    
    public void setEntity(Entity entity) {
        this.entity = entity;
    }
    
    public String getSkinName() {
        return skinName;
    }
    
    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }
    
    public boolean hasGravity() {
        return gravity;
    }
    
    public void setGravity(boolean gravity) {
        this.gravity = gravity;
        if (entity != null) {
            // Set gravity on the entity
        }
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
        if (entity != null) {
            // Set visibility on the entity
        }
    }
    
    public boolean isCollidable() {
        return collidable;
    }
    
    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
        if (entity != null) {
            // Set collision on the entity
        }
    }
}