package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SetBlockAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return;
        
        Object rawMaterial = block.getParameter("material");
        Object rawCoords = block.getParameter("coords");
        
        String materialStr = ParameterResolver.resolve(context, rawMaterial).toString();
        String coordsStr = ParameterResolver.resolve(context, rawCoords).toString();
        
        try {
            Material material = Material.valueOf(materialStr.toUpperCase());
            
            // Парсим координаты
            String[] parts = coordsStr.split(" ");
            if (parts.length == 3) {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);
                
                Location blockLocation = new Location(player.getWorld(), x, y, z);
                blockLocation.getBlock().setType(material);
                
                player.sendMessage("§a✓ Блок " + material.name() + " установлен на координатах " + coordsStr);
            } else {
                player.sendMessage("§cОшибка: координаты должны быть в формате 'x y z'");
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: координаты должны быть числами!");
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cОшибка: неизвестный материал: " + materialStr);
        }
    }
} 