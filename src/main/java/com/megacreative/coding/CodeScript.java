package com.megacreative.coding;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Представляет собой полный скрипт, состоящий из блоков кода.
 * Хранит информацию о скрипте и его корневой блок.
 */
public class CodeScript implements ConfigurationSerializable {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public ScriptType getType() {
        return type;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(ScriptType type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }


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
    public boolean isEnabled() {
        return enabled;
    }

    public CodeBlock getRootBlock() {
        return rootBlock;
    }

    public void setTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id.toString());
        map.put("name", name);
        map.put("enabled", enabled);
        map.put("type", type.name());
        map.put("rootBlock", rootBlock.serialize());
        map.put("isTemplate", isTemplate);
        map.put("author", author);
        map.put("description", description);
        return map;
    }

    public static CodeScript deserialize(Map<String, Object> map) {
        CodeBlock rootBlock = CodeBlock.deserialize((Map<String, Object>) map.get("rootBlock"));
        CodeScript script = new CodeScript(
                (String) map.get("name"),
                (boolean) map.get("enabled"),
                rootBlock,
                ScriptType.valueOf((String) map.get("type"))
        );
        script.id = UUID.fromString((String) map.get("id"));
        script.isTemplate = (boolean) map.getOrDefault("isTemplate", false);
        script.author = (String) map.get("author");
        script.description = (String) map.get("description");
        return script;
    }
}
