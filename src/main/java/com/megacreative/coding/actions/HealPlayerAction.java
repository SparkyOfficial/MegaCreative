package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for healing a player.
 * This action heals the player by a specified amount or to full health from container configuration.
 */
public class HealPlayerAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the heal amount from the container configuration
            Double amount = getHealAmountFromContainer(block, context);
            
            if (amount != null) {
                // Heal by specific amount
                double newHealth = Math.min(player.getHealth() + amount, player.getMaxHealth());
                player.setHealth(newHealth);
                return ExecutionResult.success("Player healed by " + amount + " points");
            } else {
                // Heal to full health
                player.setHealth(player.getMaxHealth());
                return ExecutionResult.success("Player healed to full health");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to heal player: " + e.getMessage());
        }
    }
    
    /**
     * Gets heal amount from the container configuration
     */
    private Double getHealAmountFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get amount from the amount slot
                Integer amountSlot = slotResolver.apply("amount_slot");
                if (amountSlot != null) {
                    ItemStack amountItem = block.getConfigItem(amountSlot);
                    if (amountItem != null && amountItem.hasItemMeta()) {
                        // Extract amount from item
                        return getAmountFromItem(amountItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting heal amount from container in HealPlayerAction: " + e.getMessage());
        }
        
        return null; // No amount specified, heal to full
    }
    
    /**
     * Extracts amount from an item
     */
    private Double getAmountFromItem(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse amount from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Double.parseDouble(cleanName);
                }
            }
            
            // Fallback to item amount
            return (double) item.getAmount();
        } catch (Exception e) {
            return null; // Invalid amount
        }
    }
}