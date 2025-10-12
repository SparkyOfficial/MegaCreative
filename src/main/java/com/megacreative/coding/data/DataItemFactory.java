package com.megacreative.coding.data;

import com.megacreative.MegaCreative;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;
import java.util.Optional;

public class DataItemFactory {
    
    private static NamespacedKey DATA_TYPE_KEY;
    private static NamespacedKey DATA_VALUE_KEY;
    
    
    public static void initialize(MegaCreative plugin) {
        DATA_TYPE_KEY = new NamespacedKey(plugin, "data_type");
        DATA_VALUE_KEY = new NamespacedKey(plugin, "data_value");
    }
    
    
    public static NamespacedKey getDataTypeKey() {
        if (DATA_TYPE_KEY == null) {
            throw new IllegalStateException("DataItemFactory not initialized. Call initialize() first.");
        }
        return DATA_TYPE_KEY;
    }
    
    public static NamespacedKey getDataValueKey() {
        if (DATA_VALUE_KEY == null) {
            throw new IllegalStateException("DataItemFactory not initialized. Call initialize() first.");
        }
        return DATA_VALUE_KEY;
    }

    public static ItemStack createDataItem(DataType type, String initialValue) {
        ItemStack item = new ItemStack(Material.BOOK); 
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§bДанные: " + type.getDisplayName());
        meta.setLore(Arrays.asList(
            "§7Тип: §f" + type.name(),
            "§7Значение: §f" + initialValue
        ));
        
        meta.getPersistentDataContainer().set(getDataTypeKey(), PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(getDataValueKey(), PersistentDataType.STRING, initialValue);
        
        item.setItemMeta(meta);
        return item;
    }
    
    public static Optional<DataItem> fromItemStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Optional.empty();
        
        var container = item.getItemMeta().getPersistentDataContainer();
        if (!container.has(getDataTypeKey(), PersistentDataType.STRING)) return Optional.empty();
        
        try {
            DataType type = DataType.valueOf(container.get(getDataTypeKey(), PersistentDataType.STRING));
            String value = container.get(getDataValueKey(), PersistentDataType.STRING);
            return Optional.of(new DataItem(type, value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public static boolean isDataItem(ItemStack item) {
        return fromItemStack(item).isPresent();
    }
    
    
    public record DataItem(DataType type, String value) {}
}