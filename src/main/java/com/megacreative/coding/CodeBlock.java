package com.megacreative.coding;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;

/**
 * Представляет один блок в скрипте.
 * Содержит тип, параметры и ссылки на другие блоки.
 */
@Data
@NoArgsConstructor
public class CodeBlock implements Cloneable {

    private UUID id;
    private Material material; // Тип блока (DIAMOND_BLOCK и т.д.)
    private String action;     // Выбранное действие (например, onJoin, sendMessage)
    private Map<String, Object> parameters;
    private List<CodeBlock> children; // Для вложенных блоков (например, внутри условия IF)
    private CodeBlock nextBlock; // Следующий блок в последовательности

    public CodeBlock(Material material, String action) {
        this.id = UUID.randomUUID();
        this.material = material;
        this.action = action;
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

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public Map<String, Object> getParameters() {
        return parameters;
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

    @Override
    public CodeBlock clone() throws CloneNotSupportedException {
        CodeBlock cloned = (CodeBlock) super.clone();
        cloned.id = UUID.randomUUID(); // У нового блока новый ID
        cloned.parameters = new HashMap<>(this.parameters);
        // Важно: не копируем nextBlock и children, чтобы не создавать непредсказуемых связей
        cloned.nextBlock = null;
        cloned.children = new ArrayList<>();
        return cloned;
    }
}
