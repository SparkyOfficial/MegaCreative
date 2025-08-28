package com.megacreative.coding;

import lombok.Data;

import java.util.UUID;

/**
 * Представляет собой полный скрипт, состоящий из блоков кода.
 * Хранит информацию о скрипте и его корневой блок.
 */
@Data
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

    public CodeScript(String name, boolean enabled, CodeBlock rootBlock) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.enabled = enabled;
        this.rootBlock = rootBlock;
    }

    public CodeScript(String name, boolean enabled, CodeBlock rootBlock, ScriptType type) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.enabled = enabled;
        this.rootBlock = rootBlock;
        this.type = type;
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
        return rootBlock != null && rootBlock.getMaterial() == org.bukkit.Material.DIAMOND_BLOCK;
    }
    
    /**
     * Проверяет, является ли скрипт шаблоном
     */
    public boolean isTemplate() {
        return isTemplate;
    }
    
    /**
     * Устанавливает, является ли скрипт шаблоном
     */
    public void setTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }
    
    // Additional getters for compatibility
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
    
    public CodeBlock getRootBlock() {
        return rootBlock;
    }
    
    public ScriptType getType() {
        return type;
    }
    
    public void setType(ScriptType type) {
        this.type = type;
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
}
