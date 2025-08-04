package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;

/**
 * Интерфейс для всех исполняемых блоков действий.
 */
public interface IActionBlock {

    /**
     * Выполняет действие блока.
     * @param context Контекст выполнения (игрок, мир и т.д.)
     * @param block Блок кода, содержащий параметры
     */
    void execute(ExecutionContext context, CodeBlock block);

    /**
     * Возвращает имя действия, на которое он отвечает.
     * @return Имя действия (например, "sendMessage")
     */
    String getActionName();
}
