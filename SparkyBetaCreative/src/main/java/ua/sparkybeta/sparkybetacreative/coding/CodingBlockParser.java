package ua.sparkybeta.sparkybetacreative.coding;

import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlockCategory;
import ua.sparkybeta.sparkybetacreative.coding.models.ActionBlockData;
import ua.sparkybeta.sparkybetacreative.coding.models.CodeLine;
import ua.sparkybeta.sparkybetacreative.coding.models.CodeScript;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.StoredCodeBlock;

import java.util.*;

public class CodingBlockParser {

    public CodeScript parse(SparkyWorld sparkyWorld) {
        CodeScript script = sparkyWorld.getCodeScript();
        script.clear();
        
        Map<String, StoredCodeBlock> blockMap = sparkyWorld.getCodeBlocks();
        if (blockMap.isEmpty()) return script;

        // Find all event blocks to start parsing from
        for (StoredCodeBlock storedBlock : blockMap.values()) {
            if (storedBlock.getType().getCategory() == CodeBlockCategory.EVENT) {
                CodeLine codeLine = new CodeLine(storedBlock.getType());
                
                // Recursively build the action chain for this event
                buildActionChain(storedBlock, codeLine, blockMap);
                
                script.addLine(codeLine);
            }
        }
        return script;
    }
    
    private void buildActionChain(StoredCodeBlock currentBlock, CodeLine codeLine, Map<String, StoredCodeBlock> blockMap) {
        for (UUID nextBlockId : currentBlock.getNextBlocks()) {
            StoredCodeBlock nextBlock = findBlockById(nextBlockId, blockMap);
            if (nextBlock != null) {
                ActionBlockData actionData = new ActionBlockData(nextBlock.getType());
                actionData.getArguments().addAll(nextBlock.getArguments());
                codeLine.addAction(actionData);
                
                // Continue the chain
                buildActionChain(nextBlock, codeLine, blockMap);
            }
        }
    }
    
    private StoredCodeBlock findBlockById(UUID id, Map<String, StoredCodeBlock> blockMap) {
        return blockMap.values().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
} 