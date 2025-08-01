package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SetTimeAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        if (block == null) return;

        String timeStr = (String) block.getParameter("time");
        if (timeStr != null) {
            try {
                long time = Long.parseLong(timeStr);
                World world = player != null ? player.getWorld() : 
                    org.bukkit.Bukkit.getWorld(context.getCreativeWorld().getWorldName());
                
                if (world != null) {
                    world.setTime(time);
                    if (player != null) {
                        player.sendMessage("§aВремя установлено: " + time);
                    }
                }
            } catch (NumberFormatException e) {
                if (player != null) {
                    player.sendMessage("§cОшибка в значении времени: " + timeStr);
                }
            }
        }
    }
} 