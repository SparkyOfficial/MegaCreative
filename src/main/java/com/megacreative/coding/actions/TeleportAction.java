package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем координаты
        Object rawCoords = block.getParameter("coords");
        String coordsStr = ParameterResolver.resolve(context, rawCoords);

        try {
            String[] parts = coordsStr.split(" ");
            if (parts.length == 3) {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                
                Location targetLocation = new Location(player.getWorld(), x, y, z);
                player.teleport(targetLocation);
                player.sendMessage("§a✓ Телепортация на координаты: " + coordsStr);
            } else {
                player.sendMessage("§cОшибка: координаты должны быть в формате 'x y z'");
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: координаты должны быть числами!");
        }
    }
} 