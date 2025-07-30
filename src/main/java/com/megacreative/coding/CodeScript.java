package com.megacreative.coding;

import lombok.Data;

import java.util.UUID;

/**
 * Представляет собой полный скрипт, состоящий из блоков кода.
 * Хранит информацию о скрипте и его корневой блок.
 */
@Data
public class CodeScript {

    private UUID id;
    private String name;
    private boolean enabled;
    private final CodeBlock rootBlock; // Начальный блок-событие

    public CodeScript(String name, boolean enabled, CodeBlock rootBlock) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.enabled = enabled;
        this.rootBlock = rootBlock;
    }

    // Конструктор для обратной совместимости или тестов
    public CodeScript(CodeBlock rootBlock) {
        this("Безымянный скрипт", true, rootBlock);
    }

    /**
     * Проверяет, является ли корневой блок событием.
     * @return true, если корневой блок - это событие
     */
    public boolean isValid() {
        return rootBlock != null && rootBlock.getType().name().startsWith("EVENT_");
    }
}
