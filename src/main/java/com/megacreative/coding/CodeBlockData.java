package com.megacreative.coding;

import org.bukkit.Material;
import java.util.*;

/**
 * Safe serializable data transfer object for CodeBlock.
 * Contains only serializable data, avoiding complex Bukkit objects that cause serialization issues.
 */
public class CodeBlockData {
    public UUID id;
    public String materialName; 
    public String action;
    public Map<String, Object> parameters; 
    public List<CodeBlockData> children; 
    public CodeBlockData nextBlock; 
    public Map<Integer, Map<String, Object>> configItems; 
    public String bracketType; 

    public CodeBlockData() {}

    /**
     * Constructor that converts a "live" CodeBlock to "serializable" data
     */
    public CodeBlockData(CodeBlock block) {
        this.id = block.getId();
        this.materialName = block.getMaterial() != null ? block.getMaterial().name() : null;
        this.action = block.getAction();
        
        
        this.parameters = new HashMap<>();
        if (block.getParameters() != null) {
            block.getParameters().forEach((key, dataValue) -> {
                this.parameters.put(key, dataValue.getValue());
            });
        }

        
        this.children = new ArrayList<>();
        if (block.getChildren() != null) {
            for (CodeBlock child : block.getChildren()) {
                this.children.add(new CodeBlockData(child));
            }
        }
        
        
        if (block.getNextBlock() != null) {
            this.nextBlock = new CodeBlockData(block.getNextBlock());
        }

        
        this.configItems = new HashMap<>();
        if (block.getConfigItems() != null) {
            block.getConfigItems().forEach((slot, itemStack) -> {
                this.configItems.put(slot, itemStack.serialize());
            });
        }
        
        if (block.getBracketType() != null) {
            this.bracketType = block.getBracketType().name();
        }
    }
}