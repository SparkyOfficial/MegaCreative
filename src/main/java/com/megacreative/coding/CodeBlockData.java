package com.megacreative.coding;

import org.bukkit.Material;
import java.util.*;

/**
 * Safe serializable data transfer object for CodeBlock.
 * Contains only serializable data, avoiding complex Bukkit objects that cause serialization issues.
 */
public class CodeBlockData {
    public UUID id;
    public String materialName; // Store material name instead of Material object
    public String action;
    public Map<String, Object> parameters; // Parameters as simple objects
    public List<CodeBlockData> children; // Children also in data format
    public CodeBlockData nextBlock; // Next block
    public Map<Integer, Map<String, Object>> configItems; // Serialized ItemStacks
    public String bracketType; // Store as string

    public CodeBlockData() {}

    /**
     * Constructor that converts a "live" CodeBlock to "serializable" data
     */
    public CodeBlockData(CodeBlock block) {
        this.id = block.getId();
        this.materialName = block.getMaterial() != null ? block.getMaterial().name() : null;
        this.action = block.getAction();
        
        // Convert DataValue to simple objects
        this.parameters = new HashMap<>();
        if (block.getParameters() != null) {
            block.getParameters().forEach((key, dataValue) -> {
                this.parameters.put(key, dataValue.getValue());
            });
        }

        // Recursively convert child blocks
        this.children = new ArrayList<>();
        if (block.getChildren() != null) {
            for (CodeBlock child : block.getChildren()) {
                this.children.add(new CodeBlockData(child));
            }
        }
        
        // Recursively convert next block
        if (block.getNextBlock() != null) {
            this.nextBlock = new CodeBlockData(block.getNextBlock());
        }

        // Serialize ItemStacks properly using Bukkit's safe serialization!
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