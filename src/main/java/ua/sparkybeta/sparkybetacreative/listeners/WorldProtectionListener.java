package ua.sparkybeta.sparkybetacreative.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.settings.WorldMode;

public class WorldProtectionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        SparkyWorld world = SparkyBetaCreative.getInstance().getWorldManager().getWorld(player);

        if (world == null) return; // Not a SparkyWorld, do nothing

        if (world.getMode() == WorldMode.PLAY) {
            if (!canBypass(player, world)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        SparkyWorld world = SparkyBetaCreative.getInstance().getWorldManager().getWorld(player);

        if (world == null) return;

        if (world.getMode() == WorldMode.PLAY) {
            if (!canBypass(player, world)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean canBypass(Player player, SparkyWorld world) {
        if (world.getOwner().equals(player.getUniqueId()) && player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }
        return world.getSettings().getBuildTrusted().contains(player.getUniqueId());
    }
} 