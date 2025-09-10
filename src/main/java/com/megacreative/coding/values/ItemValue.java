package com.megacreative.coding.values;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.Map;
import java.util.HashMap;

public class ItemValue implements DataValue {
    private final ItemStack itemStack;

    public ItemValue(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static ItemValue of(ItemStack itemStack) {
        return new ItemValue(itemStack);
    }

    public static ItemValue of(Material material, int amount) {
        return new ItemValue(new ItemStack(material, amount));
    }

    public static ItemValue of(String materialName, int amount) {
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            return null;
        }
        return new ItemValue(new ItemStack(material, amount));
    }

    public ItemStack getItemStack() {
        return itemStack;
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
        // ItemValue is immutable, so we don't allow setting values
        throw new IllegalArgumentException("ItemValue is immutable");
    }

    @Override
    public String asString() {
        if (itemStack == null) {
            return "";
        }
        return itemStack.getType().name() + "," + itemStack.getAmount();
    }

    @Override
    public Number asNumber() {
        return itemStack != null ? itemStack.getAmount() : 0;
    }

    @Override
    public boolean asBoolean() {
        return itemStack != null;
    }

    @Override
    public boolean isEmpty() {
        return itemStack == null;
    }

    @Override
    public boolean isValid() {
        return itemStack != null && itemStack.getType() != Material.AIR;
    }

    @Override
    public String getDescription() {
        if (itemStack == null) {
            return "Empty item";
        }
        return "Item: " + itemStack.getType().name() + " x" + itemStack.getAmount();
    }

    @Override
    public DataValue clone() {
        if (itemStack == null) {
            return new ItemValue(null);
        }
        return new ItemValue(itemStack.clone());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "ITEM");
        map.put("value", itemStack);
        return map;
    }
}