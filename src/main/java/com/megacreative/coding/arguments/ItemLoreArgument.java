package com.megacreative.coding.arguments;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.TextValue;
import com.megacreative.coding.values.Value;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;

/**
 * Аргумент для извлечения lore предмета из слота инвентаря.
 */
public class ItemLoreArgument implements Argument<TextValue> {
    private final String slotName;
    private final int lineIndex; // Индекс строки lore (0-based)

    public ItemLoreArgument(String slotName) {
        this(slotName, 0);
    }

    public ItemLoreArgument(String slotName, int lineIndex) {
        this.slotName = slotName;
        this.lineIndex = lineIndex;
    }

    @Override
    public Optional<TextValue> parse(CodeBlock block) {
        ItemStack item = block.getItemFromSlot(slotName);
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (lore != null && lineIndex < lore.size()) {
                    return Optional.of(new TextValue(lore.get(lineIndex)));
                }
            }
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

    /**
     * Возвращает индекс строки lore.
     * @return Индекс строки
     */
    public int getLineIndex() {
        return lineIndex;
    }
} 