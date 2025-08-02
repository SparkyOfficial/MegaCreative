package com.megacreative.coding.arguments;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.TextValue;
import com.megacreative.coding.values.Value;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

/**
 * Аргумент для извлечения названия предмета из слота инвентаря.
 */
public class ItemNameArgument implements Argument<TextValue> {
    private final String slotName;

    public ItemNameArgument(String slotName) {
        this.slotName = slotName;
    }

    @Override
    public Optional<TextValue> parse(CodeBlock block) {
        ItemStack item = block.getItemFromSlot(slotName);
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            // Возвращаем TextValue, который сам разберется с плейсхолдерами
            return Optional.of(new TextValue(item.getItemMeta().getDisplayName()));
        }
        return Optional.empty();
    }

    /**
     * Возвращает имя слота, из которого извлекается значение.
     * @return Имя слота
     */
    public String getSlotName() {
        return slotName;
    }
} 