package com.megacreative.coding.blocks;

import com.megacreative.coding.core.BlockContext;
import com.megacreative.coding.core.BlockType;
import org.bukkit.Material;

import java.util.*;

/**
 * Базовый класс для всех блоков визуального программирования.
 * Каждый блок может содержать параметры и дочерние блоки.
 */
public abstract class Block {
    protected final String id;
    protected final BlockType type;
    protected final String name;
    protected final String description;
    protected final Material icon;
    protected final Map<String, Object> parameters;
    protected final List<Block> children;
    protected Block next;
    
    protected Block(String id, BlockType type, String name, String description, Material icon) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.parameters = new HashMap<>();
        this.children = new ArrayList<>();
    }
    
    /**
     * Выполняет логику блока.
     * 
     * @param context Контекст выполнения
     * @return true, если выполнение прошло успешно, иначе false
     */
    public abstract boolean execute(BlockContext context);
    
    /**
     * Добавляет дочерний блок.
     */
    public void addChild(Block block) {
        if (block != null) {
            children.add(block);
        }
    }
    
    /**
     * Устанавливает следующий блок в цепочке.
     */
    public void setNext(Block next) {
        this.next = next;
    }
    
    /**
     * Устанавливает значение параметра.
     */
    public void setParameter(String name, Object value) {
        parameters.put(name, value);
    }
    
    /**
     * Получает значение параметра.
     */
    public Object getParameter(String name) {
        return parameters.get(name);
    }
    
    // Геттеры
    public String getId() { return id; }
    public BlockType getType() { return type; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Material getIcon() { return icon; }
    public List<Block> getChildren() { return new ArrayList<>(children); }
    public Block getNext() { return next; }
    
    /**
     * Создает копию блока.
     */
    @Override
    public Block clone() {
        try {
            Block clone = (Block) super.clone();
            // Дополнительная логика клонирования, если необходимо
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Ошибка при клонировании блока", e);
        }
    }
}
