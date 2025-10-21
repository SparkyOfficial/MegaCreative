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
        EVENT,      
        FUNCTION    
    }

    private UUID id;
    private String name;
    private boolean enabled;
    private ScriptType type = ScriptType.EVENT; 
    private final CodeBlock rootBlock; 
    
    
    private String worldName;
    private String author;
    private String description;
    
    
    public CodeScript(String name, boolean enabled, CodeBlock rootBlock) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.enabled = enabled;
        this.rootBlock = rootBlock;
    }

    public CodeScript(String name, boolean enabled, CodeBlock rootBlock, ScriptType type) {
        this(name, enabled, rootBlock);
        this.type = type;
    }

    public CodeScript(CodeBlock rootBlock) {
        this("Безымянный скрипт", true, rootBlock);
    }

    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public ScriptType getType() { return type; }
    public void setType(ScriptType type) { this.type = type; }
    
    public CodeBlock getRootBlock() { return rootBlock; }
    
    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    
    
    /**
     * Проверяет, является ли корневой блок событием.
     * @return true, если корневой блок - это событие
     */
    public boolean isValid() {
        return rootBlock != null && org.bukkit.Material.getMaterial(rootBlock.getMaterialName()) == org.bukkit.Material.DIAMOND_BLOCK;
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
        
        
        for (CodeBlock child : block.getChildren()) {
            collectBlocks(child, blocks);
        }
        
        
        collectBlocks(block.getNextBlock(), blocks);
    }
    
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeScript that = (CodeScript) o;
        return enabled == that.enabled &&
               Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               type == that.type &&
               Objects.equals(rootBlock, that.rootBlock) &&
               Objects.equals(worldName, that.worldName) &&
               Objects.equals(author, that.author) &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, enabled, type, rootBlock, worldName, 
                author, description);
    }
}