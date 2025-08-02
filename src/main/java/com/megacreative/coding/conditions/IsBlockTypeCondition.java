package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Условие для проверки типа сломанного блока.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onBlockBreak -> if (block == [алмазная руда из GUI]) -> sendMessage("Найдена алмазная руда!")
 */
public class IsBlockTypeCondition implements BlockCondition {

    @Override
    public boolean evaluate(ExecutionContext context) {
        // 1. Убедимся, что у нас правильный тип события
        if (!(context.getEvent() instanceof BlockBreakEvent event)) {
            // Это условие работает только с событием ломания блока
            return false;
        }

        // 2. Получаем наш блок кода и его конфигурацию
        CodeBlock conditionBlock = context.getCurrentBlock();
        if (conditionBlock == null) return false;

        // 3. Получаем предмет-образец из слота 0 нашего виртуального GUI
        ItemStack sampleItem = conditionBlock.getConfigItem(0);
        if (sampleItem == null) {
            // Если в GUI ничего не положили, условие не выполняется
            return false; 
        }

        // 4. Получаем тип материала из нашего образца
        Material requiredType = sampleItem.getType();
        
        // 5. Получаем тип материала из реально сломанного блока (из события)
        Material brokenType = event.getBlock().getType();

        // 6. Сравниваем их и возвращаем результат
        return requiredType == brokenType;
    }
} 