package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Text value implementation with advanced string operations
 */
public class TextValue implements DataValue {
    private String value;
    
    public TextValue(String value) {
        this.value = value == null ? "" : value;
    }
    
    @Override
    public ValueType getType() { return ValueType.TEXT; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        this.value = value == null ? "" : value.toString();
    }
    
    @Override
    public String asString() { return value; }
    
    @Override
    public Number asNumber() throws NumberFormatException {
        try {
            if (value.contains(".")) return Double.parseDouble(value);
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Cannot convert '" + value + "' to number");
        }
    }
    
    @Override
    public boolean asBoolean() {
        return Boolean.parseBoolean(value) || value.equalsIgnoreCase("yes") || 
               value.equalsIgnoreCase("да") || value.equals("1");
    }
    
    @Override
    public boolean isEmpty() { return value.isEmpty(); }
    
    @Override
    public boolean isValid() { return value != null; }
    
    @Override
    public String getDescription() { return "Text: \"" + value + "\""; }
    
    @Override
    public DataValue clone() { return new TextValue(value); }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}

/**
 * Number value with support for integers and decimals
 */
public class NumberValue implements DataValue {
    private Number value;
    
    public NumberValue(Number value) {
        this.value = value == null ? 0 : value;
    }
    
    @Override
    public ValueType getType() { return ValueType.NUMBER; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            this.value = (Number) value;
        } else if (value instanceof String) {
            try {
                this.value = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number: " + value);
            }
        } else {
            throw new IllegalArgumentException("Cannot set number from: " + value);
        }
    }
    
    @Override
    public String asString() { return value.toString(); }
    
    @Override
    public Number asNumber() { return value; }
    
    @Override
    public boolean asBoolean() { return value.doubleValue() != 0; }
    
    @Override
    public boolean isEmpty() { return value.equals(0); }
    
    @Override
    public boolean isValid() { return value != null && !Double.isNaN(value.doubleValue()); }
    
    @Override
    public String getDescription() { return "Number: " + value; }
    
    @Override
    public DataValue clone() { return new NumberValue(value); }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}

/**
 * Boolean value implementation
 */
public class BooleanValue implements DataValue {
    private boolean value;
    
    public BooleanValue(boolean value) {
        this.value = value;
    }
    
    @Override
    public ValueType getType() { return ValueType.BOOLEAN; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof Boolean) {
            this.value = (Boolean) value;
        } else if (value instanceof String) {
            String str = (String) value;
            this.value = Boolean.parseBoolean(str) || str.equalsIgnoreCase("yes") || 
                        str.equalsIgnoreCase("да") || str.equals("1");
        } else if (value instanceof Number) {
            this.value = ((Number) value).doubleValue() != 0;
        } else {
            this.value = value != null;
        }
    }
    
    @Override
    public String asString() { return value ? "true" : "false"; }
    
    @Override
    public Number asNumber() { return value ? 1 : 0; }
    
    @Override
    public boolean asBoolean() { return value; }
    
    @Override
    public boolean isEmpty() { return !value; }
    
    @Override
    public boolean isValid() { return true; }
    
    @Override
    public String getDescription() { return "Boolean: " + value; }
    
    @Override
    public DataValue clone() { return new BooleanValue(value); }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}

/**
 * Location value with world coordinates
 */
public class LocationValue implements DataValue {
    private Location value;
    
    public LocationValue(Location value) {
        this.value = value;
    }
    
    @Override
    public ValueType getType() { return ValueType.LOCATION; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof Location) {
            this.value = (Location) value;
        } else {
            throw new IllegalArgumentException("Cannot set location from: " + value);
        }
    }
    
    @Override
    public String asString() {
        if (value == null) return "null";
        return String.format("%.1f, %.1f, %.1f (%s)", 
                value.getX(), value.getY(), value.getZ(), 
                value.getWorld() != null ? value.getWorld().getName() : "unknown");
    }
    
    @Override
    public Number asNumber() { throw new NumberFormatException("Cannot convert location to number"); }
    
    @Override
    public boolean asBoolean() { return value != null; }
    
    @Override
    public boolean isEmpty() { return value == null; }
    
    @Override
    public boolean isValid() { return value != null && value.getWorld() != null; }
    
    @Override
    public String getDescription() { return "Location: " + asString(); }
    
    @Override
    public DataValue clone() { 
        return new LocationValue(value != null ? value.clone() : null); 
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}

/**
 * Item value with full NBT support
 */
public class ItemValue implements DataValue {
    private ItemStack value;
    
    public ItemValue(ItemStack value) {
        this.value = value;
    }
    
    @Override
    public ValueType getType() { return ValueType.ITEM; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof ItemStack) {
            this.value = (ItemStack) value;
        } else {
            throw new IllegalArgumentException("Cannot set item from: " + value);
        }
    }
    
    @Override
    public String asString() {
        if (value == null) return "null";
        return value.getType().name() + " x" + value.getAmount();
    }
    
    @Override
    public Number asNumber() { return value != null ? value.getAmount() : 0; }
    
    @Override
    public boolean asBoolean() { return value != null && !value.getType().isAir(); }
    
    @Override
    public boolean isEmpty() { return value == null || value.getType().isAir(); }
    
    @Override
    public boolean isValid() { return value != null; }
    
    @Override
    public String getDescription() { return "Item: " + asString(); }
    
    @Override
    public DataValue clone() { 
        return new ItemValue(value != null ? value.clone() : null); 
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}

/**
 * Player value with online validation
 */
public class PlayerValue implements DataValue {
    private Player value;
    
    public PlayerValue(Player value) {
        this.value = value;
    }
    
    @Override
    public ValueType getType() { return ValueType.PLAYER; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof Player) {
            this.value = (Player) value;
        } else {
            throw new IllegalArgumentException("Cannot set player from: " + value);
        }
    }
    
    @Override
    public String asString() { return value != null ? value.getName() : "null"; }
    
    @Override
    public Number asNumber() { throw new NumberFormatException("Cannot convert player to number"); }
    
    @Override
    public boolean asBoolean() { return value != null && value.isOnline(); }
    
    @Override
    public boolean isEmpty() { return value == null; }
    
    @Override
    public boolean isValid() { return value != null && value.isOnline(); }
    
    @Override
    public String getDescription() { return "Player: " + asString(); }
    
    @Override
    public DataValue clone() { return new PlayerValue(value); }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value != null ? value.getName() : null);
        return map;
    }
}

/**
 * List value with type-safe operations
 */
public class ListValue implements DataValue {
    private List<DataValue> value;
    
    public ListValue(List<?> value) {
        this.value = new ArrayList<>();
        if (value != null) {
            for (Object item : value) {
                this.value.add(DataValue.fromObject(item));
            }
        }
    }
    
    @Override
    public ValueType getType() { return ValueType.LIST; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof List) {
            this.value = new ArrayList<>();
            for (Object item : (List<?>) value) {
                this.value.add(DataValue.fromObject(item));
            }
        } else {
            throw new IllegalArgumentException("Cannot set list from: " + value);
        }
    }
    
    @Override
    public String asString() { 
        return "[" + value.size() + " items]";
    }
    
    @Override
    public Number asNumber() { return value.size(); }
    
    @Override
    public boolean asBoolean() { return !value.isEmpty(); }
    
    @Override
    public boolean isEmpty() { return value.isEmpty(); }
    
    @Override
    public boolean isValid() { return value != null; }
    
    @Override
    public String getDescription() { return "List: " + asString(); }
    
    @Override
    public DataValue clone() { 
        List<Object> cloned = new ArrayList<>();
        for (DataValue item : value) {
            cloned.add(item.getValue());
        }
        return new ListValue(cloned);
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        
        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (DataValue item : value) {
            serializedItems.add(item.serialize());
        }
        map.put("value", serializedItems);
        return map;
    }
    
    // List-specific methods
    public void add(DataValue item) { value.add(item); }
    public void remove(int index) { value.remove(index); }
    public DataValue get(int index) { return value.get(index); }
    public int size() { return value.size(); }
}

/**
 * Any value - accepts any object type
 */
public class AnyValue implements DataValue {
    private Object value;
    
    public AnyValue(Object value) {
        this.value = value;
    }
    
    @Override
    public ValueType getType() { return ValueType.ANY; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) { this.value = value; }
    
    @Override
    public String asString() { return value != null ? value.toString() : "null"; }
    
    @Override
    public Number asNumber() {
        if (value instanceof Number) return (Number) value;
        try {
            return Double.parseDouble(asString());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Cannot convert to number: " + value);
        }
    }
    
    @Override
    public boolean asBoolean() {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        return value != null && !asString().isEmpty();
    }
    
    @Override
    public boolean isEmpty() { return value == null; }
    
    @Override
    public boolean isValid() { return true; }
    
    @Override
    public String getDescription() { 
        String type = value != null ? value.getClass().getSimpleName() : "null";
        return "Any (" + type + "): " + asString(); 
    }
    
    @Override
    public DataValue clone() { return new AnyValue(value); }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}