package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
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

        // Получаем и разрешаем параметры
        Object rawMaterial = block.getParameter("material");
        Object rawCoords = block.getParameter("coords");

        String materialStr = ParameterResolver.resolve(context, rawMaterial);
        String coordsStr = ParameterResolver.resolve(context, rawCoords);

        if (materialStr == null) return;

        try {
            Material material = Material.valueOf(materialStr.toUpperCase());
            Location location;
            
            if (coordsStr != null && !coordsStr.isEmpty()) {
                // Парсим координаты "x y z"
                String[] coords = coordsStr.split(" ");
                if (coords.length == 3) {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);
                    location = new Location(player.getWorld(), x, y, z);
                } else {
                    location = player.getLocation();
                }
            } else {
                location = player.getLocation();
            }
            
            location.getBlock().setType(material);
            player.sendMessage("§a🔲 Блок " + material.name() + " установлен!");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в координатах: " + coordsStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cНеизвестный материал: " + materialStr);
        }
    }
} 