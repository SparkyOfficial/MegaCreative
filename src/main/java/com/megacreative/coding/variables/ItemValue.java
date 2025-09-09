package com.megacreative.coding.variables;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

/**
 * Specialized DataValue for storing and working with ItemStack objects.
 * This provides type safety and convenience methods for item operations.
 */
public class ItemValue extends DataValue {
    
    private final ItemStack itemStack;
    
    public ItemValue(ItemStack itemStack) {
        super(itemStack);
        this.itemStack = itemStack;
    }
    
    public ItemValue(Material material, int amount) {
        this(new ItemStack(material, amount));
    }
    
    public ItemValue(Material material) {
        this(new ItemStack(material, 1));
    }
    
    @Override
    public ItemStack asItemStack() {
        return itemStack;
    }
    
    @Override
    public boolean isItemStack() {
        return true;
    }
    
    @Override
    public String asString() {
        if (itemStack == null) {
            return "null";
        }
        return String.format("ItemStack{type=%s, amount=%d}", 
            itemStack.getType().name(), itemStack.getAmount());
    }
    
    @Override
    public double asNumber() {
        // For item, we might return the amount or some other numeric representation
        return itemStack != null ? itemStack.getAmount() : 0;
    }
    
    @Override
    public boolean asBoolean() {
        return itemStack != null && itemStack.getAmount() > 0;
    }
    
    // Convenience methods for item operations
    public Material getType() {
        return itemStack != null ? itemStack.getType() : Material.AIR;
    }
    
    public int getAmount() {
        return itemStack != null ? itemStack.getAmount() : 0;
    }
    
    public ItemValue setAmount(int amount) {
        if (itemStack == null) {
            return this;
        }
        ItemStack newItemStack = itemStack.clone();
        newItemStack.setAmount(amount);
        return new ItemValue(newItemStack);
    }
    
    public boolean isSameType(ItemValue other) {
        if (itemStack == null || other == null || other.itemStack == null) {
            return false;
        }
        return itemStack.getType() == other.itemStack.getType();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        
        ItemValue itemValue = (ItemValue) obj;
        return itemStack != null ? itemStack.isSimilar(itemValue.itemStack) : itemValue.itemStack == null;
    }
    
    @Override
    public int hashCode() {
        return itemStack != null ? itemStack.hashCode() : 0;
    }
}