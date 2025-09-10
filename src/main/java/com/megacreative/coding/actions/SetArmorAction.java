package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

/**
 * Action for setting a player's armor.
 * This action sets the player's armor pieces from the container configuration.
 */
public class SetArmorAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get armor items from the container configuration
            ItemStack[] armorItems = getArmorItemsFromContainer(block, context);
            
            // Set the armor pieces
            if (armorItems[0] != null) { // Helmet
                player.getInventory().setHelmet(armorItems[0]);
            }
            
            if (armorItems[1] != null) { // Chestplate
                player.getInventory().setChestplate(armorItems[1]);
            }
            
            if (armorItems[2] != null) { // Leggings
                player.getInventory().setLeggings(armorItems[2]);
            }
            
            if (armorItems[3] != null) { // Boots
                player.getInventory().setBoots(armorItems[3]);
            }
            
            return ExecutionResult.success("Set player's armor");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set armor: " + e.getMessage());
        }
    }
    
    /**
     * Gets armor items from the container configuration
     */
    private ItemStack[] getArmorItemsFromContainer(CodeBlock block, ExecutionContext context) {
        ItemStack[] armorItems = new ItemStack[4]; // helmet, chestplate, leggings, boots
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get items from named slots
                Integer helmetSlot = slotResolver.apply("helmet_slot");
                if (helmetSlot != null) {
                    armorItems[0] = block.getConfigItem(helmetSlot);
                }
                
                Integer chestplateSlot = slotResolver.apply("chestplate_slot");
                if (chestplateSlot != null) {
                    armorItems[1] = block.getConfigItem(chestplateSlot);
                }
                
                Integer leggingsSlot = slotResolver.apply("leggings_slot");
                if (leggingsSlot != null) {
                    armorItems[2] = block.getConfigItem(leggingsSlot);
                }
                
                Integer bootsSlot = slotResolver.apply("boots_slot");
                if (bootsSlot != null) {
                    armorItems[3] = block.getConfigItem(bootsSlot);
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting armor items from container in SetArmorAction: " + e.getMessage());
        }
        
        return armorItems;
    }
}