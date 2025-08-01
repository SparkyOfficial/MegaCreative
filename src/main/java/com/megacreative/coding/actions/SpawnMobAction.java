package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpawnMobAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;
        
        String mobType = (String) block.getParameter("mob");
        if (mobType != null) {
            try {
                EntityType entityType = EntityType.valueOf(mobType.toUpperCase());
                Location spawnLocation = player.getLocation();
                player.getWorld().spawnEntity(spawnLocation, entityType);
                player.sendMessage("§aЗаспаунен моб: " + mobType);
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cНеизвестный тип моба: " + mobType);
            }
        }
    }
} 