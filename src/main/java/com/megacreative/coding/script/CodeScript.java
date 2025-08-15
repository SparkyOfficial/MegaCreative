package com.megacreative.coding.script;

import com.megacreative.coding.CodeBlock;
import lombok.Data;

import java.util.List;

/**
 * Представляє скрипт, який складається з блоків коду
 */
@Data
public class CodeScript {
    private String id;
    private String name;
    private String description;
    private CodeBlock entryPoint;
    private List<CodeBlock> allBlocks;

    /**
     * Перевіряє, чи має скрипт точку входу
     * @return true, якщо скрипт має точку входу
     */
    public boolean hasEntryPoint() {
        return entryPoint != null;
    }
}
