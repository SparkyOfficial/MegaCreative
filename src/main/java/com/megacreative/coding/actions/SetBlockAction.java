package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SetBlockAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawMaterial = block.getParameter("material");
        DataValue rawCoords = block.getParameter("coords");

        if (rawMaterial == null) return;

        String materialStr = resolver.resolve(context, rawMaterial).asString();
        String coordsStr = rawCoords != null ? resolver.resolve(context, rawCoords).asString() : null;

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