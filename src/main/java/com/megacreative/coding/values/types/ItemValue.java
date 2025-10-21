package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Item value with full NBT support
 */
public class ItemValue implements DataValue {
    private ItemStack value;
    
    public ItemValue(@NotNull ItemStack value) {
        this.value = value.clone();
    }
    
    @Override
    @NotNull
    public ValueType getType() { return ValueType.ITEM; }
    
    @Override
    @NotNull
    public Object getValue() { return value; }
    
    @Override
    public void setValue(@NotNull Object value) {
        if (value instanceof ItemStack) {
            this.value = (ItemStack) value;
        } else {
            throw new IllegalArgumentException("Cannot set item from: " + value);
        }
    }
    
    @Override
    @NotNull
    public String asString() {
        if (value == null) return "null";
        return value.getType().name() + " x" + value.getAmount();
    }
    
    @Override
    @NotNull
    public Number asNumber() { return value != null ? value.getAmount() : 0; }
    
    @Override
    public boolean asBoolean() { return value != null && !value.getType().isAir(); }
    
    @Override
    public boolean isEmpty() { return value == null || value.getType().isAir(); }
    
    @Override
    public boolean isValid() { return value != null; }
    
    @Override
    @NotNull
    public String getDescription() { return "Item: " + asString(); }
    
    @Override
    @NotNull
    public DataValue copy() { 
        return new ItemValue(value != null ? value.clone() : null); 
    }
    
    @Override
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}