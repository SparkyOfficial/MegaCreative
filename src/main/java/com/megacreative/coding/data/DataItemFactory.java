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
    
    public static final NamespacedKey DATA_TYPE_KEY = new NamespacedKey(MegaCreative.getInstance(), "data_type");
    public static final NamespacedKey DATA_VALUE_KEY = new NamespacedKey(MegaCreative.getInstance(), "data_value");

    public static ItemStack createDataItem(DataType type, String initialValue) {
        ItemStack item = new ItemStack(Material.BOOK); // Используем книгу как универсальный контейнер
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§bДанные: " + type.getDisplayName());
        meta.setLore(Arrays.asList(
            "§7Тип: §f" + type.name(),
            "§7Значение: §f" + initialValue
        ));
        
        meta.getPersistentDataContainer().set(DATA_TYPE_KEY, PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(DATA_VALUE_KEY, PersistentDataType.STRING, initialValue);
        
        item.setItemMeta(meta);
        return item;
    }
    
    public static Optional<DataItem> fromItemStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Optional.empty();
        
        var container = item.getItemMeta().getPersistentDataContainer();
        if (!container.has(DATA_TYPE_KEY, PersistentDataType.STRING)) return Optional.empty();
        
        try {
            DataType type = DataType.valueOf(container.get(DATA_TYPE_KEY, PersistentDataType.STRING));
            String value = container.get(DATA_VALUE_KEY, PersistentDataType.STRING);
            return Optional.of(new DataItem(type, value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public static boolean isDataItem(ItemStack item) {
        return fromItemStack(item).isPresent();
    }
    
    // Вложенный класс для удобства
    public record DataItem(DataType type, String value) {}
} 