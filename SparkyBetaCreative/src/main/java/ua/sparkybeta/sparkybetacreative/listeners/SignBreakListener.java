package ua.sparkybeta.sparkybetacreative.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.settings.WorldMode;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;

public class SignBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();

        if (!(brokenBlock.getState() instanceof Sign)) {
            return;
        }
        
        SparkyWorld sparkyWorld = SparkyBetaCreative.getInstance().getWorldManager().getWorld(event.getPlayer());
        if (sparkyWorld == null || sparkyWorld.getMode() != WorldMode.DEV) {
            return;
        }

        Sign sign = (Sign) brokenBlock.getState();
        if (sign.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
            org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) sign.getBlockData();
            Block attachedBlock = sign.getBlock().getRelative(wallSign.getFacing().getOppositeFace());
            
            // Check if the block it's attached to is a code block
            for (CodeBlock cb : CodeBlock.values()) {
                if (attachedBlock.getType() == cb.getMaterial()) {
                    event.setCancelled(true);
                    MessageUtils.sendError(event.getPlayer(), "You cannot break signs attached to code blocks. Break the code block itself.");
                    return;
                }
            }
        }
    }
} 