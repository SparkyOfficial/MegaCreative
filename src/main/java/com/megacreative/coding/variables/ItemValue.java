package com.megacreative.coding.variables;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.util.*;

/**
 * Specialized DataValue for storing and working with ItemStack objects.
 * This provides type safety and convenience methods for item operations.
 */
public class ItemValue implements DataValue, Cloneable {
    
    private ItemStack itemStack;
    
    public ItemValue(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    public ItemValue(Material material, int amount) {
        this(new ItemStack(material, amount));
    }
    
    public ItemValue(Material material) {
        this(new ItemStack(material, 1));
    }
    
    @Override
    public ValueType getType() {
        return ValueType.ITEM;
    }
    
    @Override
    public Object getValue() {
        return itemStack;
    }
    
    @Override
    public void setValue(Object value) throws IllegalArgumentException {
        if (value instanceof ItemStack) {
            this.itemStack = (ItemStack) value;
        } else {
            throw new IllegalArgumentException("Value must be an ItemStack instance");
        }
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
    public Number asNumber() throws NumberFormatException {
        // For item, we might return the amount or some other numeric representation
        return itemStack != null ? itemStack.getAmount() : 0;
    }
    
    @Override
    public boolean asBoolean() {
        return itemStack != null && itemStack.getAmount() > 0;
    }
    
    @Override
    public boolean isEmpty() {
        return itemStack == null || itemStack.getAmount() <= 0;
    }
    
    @Override
    public boolean isValid() {
        return itemStack != null && itemStack.getType() != Material.AIR;
    }
    
    @Override
    public String getDescription() {
        return "Item: " + asString();
    }
    
    // Convenience methods for item operations
    public Material getItemType() {
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
    public DataValue clone() {
        try {
            // Call super.clone() first to create the new instance
            ItemValue cloned = (ItemValue) super.clone();
            // Clone the ItemStack if it exists
            if (itemStack != null) {
                cloned.itemStack = itemStack.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new AssertionError("Clone not supported", e);
        }
    }
    
    @Override
    public DataValue copy() {
        return clone();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        if (itemStack != null) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("type", itemStack.getType().name());
            itemData.put("amount", itemStack.getAmount());
            // Note: We're not serializing item meta for simplicity
            map.put("value", itemData);
        } else {
            map.put("value", null);
        }
        return map;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ItemValue itemValue = (ItemValue) obj;
        return itemStack != null ? itemStack.isSimilar(itemValue.itemStack) : itemValue.itemStack == null;
    }
    
    @Override
    public int hashCode() {
        return itemStack != null ? itemStack.hashCode() : 0;
    }
}