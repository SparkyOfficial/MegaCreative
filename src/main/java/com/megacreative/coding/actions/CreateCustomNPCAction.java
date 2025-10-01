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
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Action to create and configure a custom NPC
 */
@BlockMeta(
    id = "create_custom_npc",
    displayName = "Create Custom NPC",
    type = BlockType.ACTION
)
public class CreateCustomNPCAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public CreateCustomNPCAction(MegaCreative plugin) {
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
            DataValue nameValue = block.getParameter("name");
            DataValue skinValue = block.getParameter("skin");
            DataValue xValue = block.getParameter("x");
            DataValue yValue = block.getParameter("y");
            DataValue zValue = block.getParameter("z");
            DataValue worldValue = block.getParameter("world");
            DataValue variableValue = block.getParameter("variable");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("NPC name parameter is missing.");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedSkin = skinValue != null ? resolver.resolve(context, skinValue) : null;
            DataValue resolvedX = xValue != null ? resolver.resolve(context, xValue) : null;
            DataValue resolvedY = yValue != null ? resolver.resolve(context, yValue) : null;
            DataValue resolvedZ = zValue != null ? resolver.resolve(context, zValue) : null;
            DataValue resolvedWorld = worldValue != null ? resolver.resolve(context, worldValue) : null;
            DataValue resolvedVariable = variableValue != null ? resolver.resolve(context, variableValue) : null;
            
            String name = resolvedName.asString();
            String skin = resolvedSkin != null ? resolvedSkin.asString() : null;
            
            // Determine spawn location
            Location location;
            if (resolvedX != null && resolvedY != null && resolvedZ != null) {
                double x = resolvedX.asNumber().doubleValue();
                double y = resolvedY.asNumber().doubleValue();
                double z = resolvedZ.asNumber().doubleValue();
                
                org.bukkit.World world = null;
                if (resolvedWorld != null) {
                    world = plugin.getServer().getWorld(resolvedWorld.asString());
                }
                
                // Fallback to player's world
                if (world == null) {
                    world = player.getWorld();
                }
                
                location = new Location(world, x, y, z);
            } else {
                // Use player's location
                location = player.getLocation();
            }
            
            // Get NPC manager
            NPCManager npcManager = plugin.getServiceRegistry().getService(NPCManager.class);
            if (npcManager == null) {
                return ExecutionResult.error("NPC Manager not available.");
            }
            
            // Create the NPC
            CustomNPC npc = npcManager.createNPC(name, location);
            if (skin != null && !skin.isEmpty()) {
                npc.setSkinName(skin);
            }
            
            // Spawn the NPC
            if (!npc.spawn()) {
                return ExecutionResult.error("Failed to spawn NPC: " + name);
            }
            
            // Store NPC reference in variable if specified
            if (resolvedVariable != null && !resolvedVariable.isEmpty()) {
                String variableName = resolvedVariable.asString();
                if (variableName != null && !variableName.isEmpty()) {
                    // Store the NPC's UUID in the variable
                    context.getPlugin().getServiceRegistry().getVariableManager()
                        .setPlayerVariable(player.getUniqueId(), variableName, 
                                         DataValue.fromObject(npc.getUniqueId().toString()));
                }
            }
            
            return ExecutionResult.success("Created custom NPC '" + name + "' at location.");

        } catch (Exception e) {
            e.printStackTrace();
            return ExecutionResult.error("Error creating custom NPC: " + e.getMessage());
        }
    }
}