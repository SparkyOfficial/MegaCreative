package com.megacreative.coding;

import org.bukkit.Bukkit;

/**
 * "Движок" для выполнения скриптов.
 * Проходит по блокам и выполняет их логику.
 */
public class ScriptExecutor {

    public void execute(CodeScript script, ExecutionContext context) {
        if (!script.isValid() || !script.isEnabled()) {
            return;
        }

        processBlock(script.getRootBlock(), context);
    }

    private void processBlock(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            return;
        }

        // Основная логика выполнения блока
        switch (block.getType()) {
            // --- Действия ---
            case ACTION_SEND_MESSAGE:
                handleSendMessage(block, context);
                break;

            // --- Условия (пример) ---
            case CONDITION_HAS_ITEM:
                boolean result = checkHasItem(block, context);
                if (result) {
                    // Если условие истинно, выполняем дочерние блоки
                    for (CodeBlock child : block.getChildren()) {
                        processBlock(child, context);
                    }
                }
                break;

            // Другие типы блоков будут добавлены здесь
            default:
                Bukkit.getLogger().warning("Неизвестный или необработанный тип блока: " + block.getType());
                break;
        }

        // Переходим к следующему блоку в основной цепочке
        if (block.getNextBlock() != null) {
            processBlock(block.getNextBlock(), context);
        }
    }

    private void handleSendMessage(CodeBlock block, ExecutionContext context) {
        if (context.getPlayer() != null) {
            String message = (String) block.getParameters().getOrDefault("message", "");
            // Простое форматирование, в будущем можно добавить переменные
            message = message.replace("%player%", context.getPlayer().getName());
            context.getPlayer().sendMessage(message);
        }
    }

    private boolean checkHasItem(CodeBlock block, ExecutionContext context) {
        // TODO: Реализовать логику проверки наличия предмета у игрока
        // Для примера, пока всегда возвращает true
        return true;
    }

    // Здесь будут другие методы-обработчики для каждого типа блока

}
