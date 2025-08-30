package com.megacreative.coding;

import java.util.Objects;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/**
 * Представляет собой полный скрипт, состоящий из блоков кода.
 * Хранит информацию о скрипте и его корневой блок.
 */
public class CodeScript {

    /**
     * Типы скриптов
     */
    public enum ScriptType {
        EVENT,      // Скрипт-событие (запускается по триггеру)
        FUNCTION    // Функция (вызывается из других скриптов)
    }

    private UUID id;
    private String name;
    private boolean enabled;
    private ScriptType type = ScriptType.EVENT; // По умолчанию - событие
    private final CodeBlock rootBlock; // Начальный блок-событие
    
    // Поля для шаблонов
    private boolean isTemplate = false;
    private String author;
    private String description = "";
    
    /**
     * Основной конструктор
     */
    public CodeScript(String name, boolean enabled, CodeBlock rootBlock) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.enabled = enabled;
        this.rootBlock = rootBlock;
    }

    /**
     * Конструктор с указанием типа скрипта
     */
    public CodeScript(String name, boolean enabled, CodeBlock rootBlock, ScriptType type) {
        this(name, enabled, rootBlock);
        this.type = type;
    }

    /**
     * Конструктор для обратной совместимости или тестов
     */
    public CodeScript(CodeBlock rootBlock) {
        this("Безымянный скрипт", true, rootBlock);
    }

    // ===== Геттеры и сеттеры =====
    
    public UUID getId() { 
        return id; 
    }
    
    public void setId(UUID id) { 
        this.id = id; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public boolean isEnabled() { 
        return enabled; 
    }
    
    public void setEnabled(boolean enabled) { 
        this.enabled = enabled; 
    }
    
    public ScriptType getType() { 
        return type; 
    }
    
    public void setType(ScriptType type) { 
        this.type = type; 
    }
    
    public CodeBlock getRootBlock() { 
        return rootBlock; 
    }
    
    public boolean isTemplate() { 
        return isTemplate; 
    }
    
    public void setTemplate(boolean template) { 
        this.isTemplate = template; 
    }
    
    public String getAuthor() { 
        return author; 
    }
    
    public void setAuthor(String author) { 
        this.author = author; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    // ===== Основные методы =====
    
    /**
     * Проверяет, является ли корневой блок событием.
     * @return true, если корневой блок - это событие
     */
    public boolean isValid() {
        return rootBlock != null && rootBlock.getMaterial() == org.bukkit.Material.DIAMOND_BLOCK;
    }
    
    /**
     * Получает все блоки в этом скрипте
     * @return Список всех блоков в скрипте
     */
    public List<CodeBlock> getBlocks() {
        List<CodeBlock> blocks = new ArrayList<>();
        collectBlocks(rootBlock, blocks);
        return blocks;
    }
    
    /**
     * Рекурсивно собирает все блоки в скрипте
     */
    private void collectBlocks(CodeBlock block, List<CodeBlock> blocks) {
        if (block == null) return;
        
        blocks.add(block);
        
        // Добавляем дочерние блоки
        for (CodeBlock child : block.getChildren()) {
            collectBlocks(child, blocks);
        }
        
        // Добавляем следующий блок в цепочке
        collectBlocks(block.getNextBlock(), blocks);
    }
    
    // ===== equals и hashCode =====
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeScript that = (CodeScript) o;
        return enabled == that.enabled &&
               isTemplate == that.isTemplate &&
               Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               type == that.type &&
               Objects.equals(rootBlock, that.rootBlock) &&
               Objects.equals(author, that.author) &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, enabled, type, rootBlock, isTemplate, author, description);
    }
}
