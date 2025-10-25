package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.logging.Logger;

/**
 * Action that heals the player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "healPlayer", displayName = "Heal Player", type = BlockType.ACTION)
public class HealPlayerAction implements BlockAction {
    
    private static final Logger LOGGER = Logger.getLogger(HealPlayerAction.class.getName());
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("No player in execution context");
            }
            
            // Heal the player to full health
            player.setHealth(player.getMaxHealth());
            
            // Remove negative potion effects
            player.removePotionEffect(PotionEffectType.POISON);
            player.removePotionEffect(PotionEffectType.WITHER);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.CONFUSION);
            player.removePotionEffect(PotionEffectType.HUNGER);
            player.removePotionEffect(PotionEffectType.SLOW);
            player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
            player.removePotionEffect(PotionEffectType.WEAKNESS);
            player.removePotionEffect(PotionEffectType.UNLUCK);
            
            // Send a message to the player
            player.sendMessage("§aYou have been healed!");
            
            LOGGER.fine("Healed player " + player.getName());
            return ExecutionResult.success("Player healed successfully");
        } catch (Exception e) {
            LOGGER.severe("Error executing HealPlayerAction: " + e.getMessage());
            return ExecutionResult.error("Failed to heal player: " + e.getMessage());
        }
    }
}