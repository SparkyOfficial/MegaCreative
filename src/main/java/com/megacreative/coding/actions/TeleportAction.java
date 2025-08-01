package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;
        
        String coords = (String) block.getParameter("coords");
        if (coords != null) {
            try {
                String[] parts = coords.split(" ");
                if (parts.length == 3) {
                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    double z = Double.parseDouble(parts[2]);
                    player.teleport(new Location(player.getWorld(), x, y, z));
                }
            } catch (Exception e) {
                player.sendMessage("§cОшибка в координатах для телепортации: " + coords);
            }
        }
    }
} 