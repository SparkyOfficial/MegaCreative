package com.megacreative.coding.execution;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.CodeScript;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.gui.ConnectionVisualizationManager;
// використовуємо власний CodeScript, а не з пакету script
import com.megacreative.coding.CodeScript;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Виконує скрипти блоків програмування
 */
public class HybridScriptExecutor {

    private final Map<Location, CodeBlock> codeBlocks = new ConcurrentHashMap<>();

    /**
     * Знаходить розташування блоку коду в світі
     * @param codeBlock Блок коду для пошуку
     * @return Розташування блоку або null, якщо не знайдено
     */
    public Location findBlockLocation(CodeBlock codeBlock) {
        if (codeBlock == null) return null;

        // Перебираємо всі блоки коду у менеджері та шукаємо співпадіння
        for (Map.Entry<Location, CodeBlock> entry : codeBlocks.entrySet()) {
            if (entry.getValue().equals(codeBlock)) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Обробляє виконання блоку коду
     * @param codeBlock Блок коду для обробки
     * @param context Контекст виконання
     * @return true, якщо обробка пройшла успішно
     */
    public boolean processBlock(CodeBlock codeBlock, ExecutionContext context) {
        if (codeBlock == null || context == null) return false;

        // Встановлюємо поточний блок у контексті
        ExecutionContext updated = context.withCurrentBlock(codeBlock, context.getBlockLocation());

        // Отримуємо дію блоку та виконуємо її
        // Тут codeBlock.getAction() возвращает строковый ключ в нашей архитектуре.
        // Для пакета execution используем отражение, аналогично новому исполнительному классу.
        BlockAction action = getActionForBlock(codeBlock);
        if (action != null) {
            try {
                action.execute(updated);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    /**
     * Виконує скрипт з вказаним тригером
     * @param script Скрипт для виконання
     * @param context Контекст виконання
     * @param trigger Назва тригера
     * @return true, якщо виконання пройшло успішно
     */
    public boolean execute(CodeScript script, ExecutionContext context, String trigger) {
        if (script == null || context == null || trigger == null) return false;

        // Шукаємо початковий блок з вказаним тригером
        // В этой версии CodeScript не содержит getBlocksByTrigger, используем корень
        CodeBlock startBlock = script.getRootBlock();
        if (startBlock == null) return false;

        // Запускаємо обробку блоку
        return processBlock(startBlock, context);
    }

    private final MegaCreative plugin;
    private final Map<String, BlockAction> actionCache = new HashMap<>();

    // Лічильник рекурсії для запобігання нескінченним циклам
    private final Map<String, Integer> recursionCounter = new HashMap<>();
    private static final int MAX_RECURSION_DEPTH = 50;

    /**
     * Конструктор
     * @param plugin Посилання на основний плагін
     */
    public HybridScriptExecutor(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Виконує скрипт
     * @param script Скрипт для виконання
     * @param context Контекст виконання
     */
    public void executeScript(CodeScript script, ExecutionContext context) {
        if (script == null || script.getRootBlock() == null) {
            return;
        }

        // Починаємо виконання з кореня (или из соответствующего тригера при необходимости)
        executeBlock(script.getRootBlock(), context, null);
    }

    /**
     * Виконує блок коду
     * @param block Блок для виконання
     * @param context Контекст виконання
     * @param blockLocation Розташування блоку у світі
     */
    public void executeBlock(CodeBlock block, ExecutionContext context, Location blockLocation) {
        if (block == null) {
            return;
        }

        // Перевіряємо глибину рекурсії
        String executionId = context.getPlayer() != null ? context.getPlayer().getUniqueId().toString() : "server";
        int recursionDepth = recursionCounter.getOrDefault(executionId, 0);

        if (recursionDepth > MAX_RECURSION_DEPTH) {
            if (context.getPlayer() != null) {
                context.getPlayer().sendMessage("§cПомилка: Досягнуто максимальну глибину рекурсії ("+MAX_RECURSION_DEPTH+").");
            }
            return;
        }

        recursionCounter.put(executionId, recursionDepth + 1);

        try {
            // Оновлюємо контекст поточним блоком
            ExecutionContext updatedContext = context.withCurrentBlock(block, blockLocation);

            // Отримуємо дію для блоку
            BlockAction action = getActionForBlock(block);

            if (action == null) {
                // Якщо дія не знайдена, показуємо помилку
                if (context.getPlayer() != null && blockLocation != null) {
                    context.getPlayer().sendMessage("§cПомилка: Невідома дія '" + block.getAction() + "'");
                    // Визуализация доступна только если менеджер подключен в основной системе
                    // здесь опускаем вызов
                }
            } else {
                // Візуалізуємо виконання дії
                // опускаем визуализацию в execution-пакете

                // Виконуємо дію
                action.execute(updatedContext);
            }

            // Знаходимо наступний блок
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock != null) {
                // Візуалізацію в execution-пакеті пропускаємо

                // Виконуємо наступний блок
                executeBlock(nextBlock, updatedContext, updatedContext.getBlockLocation());
            }

        } catch (Exception e) {
            // Обробляємо помилки виконання
            plugin.getLogger().warning("Помилка виконання блоку: " + e.getMessage());
            e.printStackTrace();

            if (context.getPlayer() != null) {
                context.getPlayer().sendMessage("§cПомилка виконання блоку: " + e.getMessage());

                // Візуалізацію помилки в execution-пакеті пропускаємо
            }
        } finally {
            // Зменшуємо лічильник рекурсії
            recursionCounter.put(executionId, recursionDepth);
        }
    }

    /**
     * Отримує дію для блоку з кешу або створює нову
     * @param block Блок коду
     * @return Дія для блоку або null, якщо не знайдено
     */
    private BlockAction getActionForBlock(CodeBlock block) {
        String actionKey = block.getAction().toLowerCase();

        // Перевіряємо кеш
        if (actionCache.containsKey(actionKey)) {
            return actionCache.get(actionKey);
        }

        // Намагаємося знайти клас дії
        try {
            // Перевіряємо різні пакети, де можуть бути дії
            String[] packagePrefixes = {
                "com.megacreative.coding.actions.",
                "com.megacreative.coding.blocks.actions.",
                "com.megacreative.coding.blocks.conditions."
            };

            // Перетворюємо назву дії у формат ClassName
            String className = actionKey.substring(0, 1).toUpperCase() + actionKey.substring(1) + "Action";

            // Спробуємо знайти клас у різних пакетах
            for (String prefix : packagePrefixes) {
                try {
                    Class<?> actionClass = Class.forName(prefix + className);
                    BlockAction action = (BlockAction) actionClass.getDeclaredConstructor().newInstance();
                    actionCache.put(actionKey, action);
                    return action;
                } catch (ClassNotFoundException e) {
                    // Ігноруємо, спробуємо наступний пакет
                }
            }

            // Для умов перевіряємо ще одну назву формату
            if (!actionKey.endsWith("condition")) {
                String conditionClassName = actionKey.substring(0, 1).toUpperCase() + actionKey.substring(1) + "Condition";

                for (String prefix : packagePrefixes) {
                    try {
                        Class<?> actionClass = Class.forName(prefix + conditionClassName);
                        BlockAction action = (BlockAction) actionClass.getDeclaredConstructor().newInstance();
                        actionCache.put(actionKey, action);
                        return action;
                    } catch (ClassNotFoundException e) {
                        // Ігноруємо, спробуємо наступний пакет
                    }
                }
            }

            // Якщо не знайдено, кешуємо null
            actionCache.put(actionKey, null);
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("Помилка створення дії для '" + actionKey + "': " + e.getMessage());
            e.printStackTrace();
            actionCache.put(actionKey, null);
            return null;
        }
    }

    /**
     * Отримує менеджер візуалізації
     * @return Менеджер візуалізації
     */
    // В execution-пакете визуализацию не используем напрямую

    /**
     * Очищає кеш дій
     */
    public void clearActionCache() {
        actionCache.clear();
    }
}
