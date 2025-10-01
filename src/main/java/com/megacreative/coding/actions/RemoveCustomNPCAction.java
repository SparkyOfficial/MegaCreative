package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.npc.CustomNPC;
import com.megacreative.npc.NPCManager;

import java.util.UUID;

/**
 * Action to remove a custom NPC
 */
@BlockMeta(
    id = "remove_custom_npc",
    displayName = "Remove Custom NPC",
    type = BlockType.ACTION
)
public class RemoveCustomNPCAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public RemoveCustomNPCAction(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get parameters from the block
            DataValue npcIdValue = block.getParameter("npcId");
            
            if (npcIdValue == null || npcIdValue.isEmpty()) {
                return ExecutionResult.error("NPC ID parameter is missing.");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedNpcId = resolver.resolve(context, npcIdValue);
            
            // Get NPC manager
            NPCManager npcManager = plugin.getServiceRegistry().getService(NPCManager.class);
            if (npcManager == null) {
                return ExecutionResult.error("NPC Manager not available.");
            }
            
            // Get the NPC by ID
            CustomNPC npc;
            try {
                UUID npcId = UUID.fromString(resolvedNpcId.asString());
                npc = npcManager.getNPC(npcId);
            } catch (IllegalArgumentException e) {
                // Try by name
                npc = npcManager.getNPC(resolvedNpcId.asString());
            }
            
            if (npc == null) {
                return ExecutionResult.error("NPC not found: " + resolvedNpcId.asString());
            }
            
            // Remove the NPC
            npcManager.removeNPC(npc);
            
            return ExecutionResult.success("Removed custom NPC '" + npc.getName() + "'.");

        } catch (Exception e) {
            e.printStackTrace();
            return ExecutionResult.error("Error removing custom NPC: " + e.getMessage());
        }
    }
}