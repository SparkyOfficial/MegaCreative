package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Action to give multiple items to a player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "giveItems", displayName = "§bGive Items", type = BlockType.ACTION)
public class GiveItemsAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get all parameters
            Map<String, DataValue> parameters = block.getParameters();
            
            if (parameters.isEmpty()) {
                return ExecutionResult.error("No items specified");
            }
            
            int totalItems = 0;
            
            // Process each parameter as an item
            for (Map.Entry<String, DataValue> entry : parameters.entrySet()) {
                String key = entry.getKey();
                DataValue value = entry.getValue();
                
                // Skip non-item parameters
                if (key.equals("action") || key.equals("type")) {
                    continue;
                }
                
                try {
                    String[] parts = value.asString().split(":");
                    String materialStr = parts[0];
                    int amount = 1;
                    
                    if (parts.length > 1) {
                        amount = Integer.parseInt(parts[1]);
                    }
                    
                    Material material = Material.matchMaterial(materialStr);
                    if (material != null) {
                        ItemStack itemStack = new ItemStack(material, amount);
                        player.getInventory().addItem(itemStack);
                        totalItems += amount;
                    }
                } catch (Exception e) {
                    // Skip invalid items
                    context.getPlugin().getLogger().warning("Invalid item specification: " + value.asString());
                }
            }
            
            return ExecutionResult.success("Gave " + totalItems + " items to player");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to give items: " + e.getMessage());
        }
    }
}