package com.megacreative.coding;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Представляет один блок в скрипте.
 * Содержит тип, параметры и ссылки на другие блоки.
 */
@Data
@NoArgsConstructor
public class CodeBlock {

    private UUID id;
    private BlockType type;
    private Map<String, Object> parameters;
    private List<CodeBlock> children; // Для вложенных блоков (например, внутри условия IF)
    private CodeBlock nextBlock; // Следующий блок в последовательности

    public CodeBlock(BlockType type) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.parameters = new HashMap<>();
        this.children = new ArrayList<>();
    }

    /**
     * Устанавливает параметр для блока.
     * @param key Ключ параметра (например, "material" или "message")
     * @param value Значение параметра
     */
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    /**
     * Добавляет дочерний блок (для условий).
     * @param child Блок, который будет выполнен внутри этого блока
     */
    public void addChild(CodeBlock child) {
        children.add(child);
    }

    /**
     * Устанавливает следующий блок в цепочке.
     * @param next Следующий блок
     */
    public void setNext(CodeBlock next) {
        this.nextBlock = next;
    }
}
