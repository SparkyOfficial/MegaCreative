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
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Action to interact with a custom NPC (talk, animate, move, etc.)
 */
@BlockMeta(
    id = "npc_interaction",
    displayName = "NPC Interaction",
    type = BlockType.ACTION
)
public class NPCInteractionAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public NPCInteractionAction(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the block
            DataValue npcIdValue = block.getParameter("npcId");
            DataValue interactionTypeValue = block.getParameter("interactionType", DataValue.of("talk"));
            DataValue messageValue = block.getParameter("message");
            DataValue animationValue = block.getParameter("animation");
            DataValue targetXValue = block.getParameter("targetX");
            DataValue targetYValue = block.getParameter("targetY");
            DataValue targetZValue = block.getParameter("targetZ");
            
            if (npcIdValue == null || npcIdValue.isEmpty()) {
                return ExecutionResult.error("NPC ID parameter is missing.");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedNpcId = resolver.resolve(context, npcIdValue);
            DataValue resolvedInteractionType = resolver.resolve(context, interactionTypeValue);
            DataValue resolvedMessage = messageValue != null ? resolver.resolve(context, messageValue) : null;
            DataValue resolvedAnimation = animationValue != null ? resolver.resolve(context, animationValue) : null;
            DataValue resolvedTargetX = targetXValue != null ? resolver.resolve(context, targetXValue) : null;
            DataValue resolvedTargetY = targetYValue != null ? resolver.resolve(context, targetYValue) : null;
            DataValue resolvedTargetZ = targetZValue != null ? resolver.resolve(context, targetZValue) : null;
            
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
            
            // Perform the interaction
            String interactionType = resolvedInteractionType.asString().toLowerCase();
            
            switch (interactionType) {
                case "talk":
                    if (resolvedMessage != null) {
                        npc.talk(resolvedMessage.asString());
                        return ExecutionResult.success("NPC '" + npc.getName() + "' said: " + resolvedMessage.asString());
                    } else {
                        return ExecutionResult.error("Message parameter is required for talk interaction.");
                    }
                    
                case "animate":
                    if (resolvedAnimation != null) {
                        npc.playAnimation(resolvedAnimation.asString());
                        return ExecutionResult.success("NPC '" + npc.getName() + "' played animation: " + resolvedAnimation.asString());
                    } else {
                        return ExecutionResult.error("Animation parameter is required for animate interaction.");
                    }
                    
                case "move":
                    if (resolvedTargetX != null && resolvedTargetY != null && resolvedTargetZ != null) {
                        double x = resolvedTargetX.asNumber().doubleValue();
                        double y = resolvedTargetY.asNumber().doubleValue();
                        double z = resolvedTargetZ.asNumber().doubleValue();
                        
                        org.bukkit.Location target = new org.bukkit.Location(
                            player.getWorld(), x, y, z);
                        npc.walkTo(target);
                        
                        return ExecutionResult.success("NPC '" + npc.getName() + "' is moving to target location.");
                    } else {
                        return ExecutionResult.error("Target coordinates are required for move interaction.");
                    }
                    
                case "look":
                    npc.lookAt(player);
                    return ExecutionResult.success("NPC '" + npc.getName() + "' is looking at player.");
                    
                default:
                    return ExecutionResult.error("Unknown interaction type: " + interactionType);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ExecutionResult.error("Error interacting with NPC: " + e.getMessage());
        }
    }
}