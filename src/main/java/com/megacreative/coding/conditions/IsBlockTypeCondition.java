package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Условие для проверки типа блока.
 */
public class IsBlockTypeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawMaterial = block.getParameter("material");
        DataValue rawLocation = block.getParameter("location");

        if (rawMaterial == null) {
            context.getPlugin().getLogger().warning("Material not specified in IsBlockTypeCondition");
            return false;
        }

        DataValue materialValue = resolver.resolve(context, rawMaterial);
        String materialName = materialValue.asString();

        try {
            Material requiredMaterial = Material.valueOf(materialName.toUpperCase());
            
            // Get location to check
            Location checkLocation;
            if (rawLocation != null) {
                DataValue locationValue = resolver.resolve(context, rawLocation);
                String locationStr = locationValue.asString();
                
                // Parse location string "x y z"
                String[] coords = locationStr.split(" ");
                if (coords.length == 3) {
                    try {
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        int z = Integer.parseInt(coords[2]);
                        
                        if (context.getPlayer() != null) {
                            checkLocation = new Location(context.getPlayer().getWorld(), x, y, z);
                        } else {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        context.getPlugin().getLogger().warning("Invalid coordinates in IsBlockTypeCondition");
                        return false;
                    }
                } else {
                    // Use player location if no valid coordinates
                    if (context.getPlayer() != null) {
                        checkLocation = context.getPlayer().getLocation();
                    } else {
                        return false;
                    }
                }
            } else {
                // Use player location if no location specified
                if (context.getPlayer() != null) {
                    checkLocation = context.getPlayer().getLocation();
                } else {
                    return false;
                }
            }
            
            // Check if block at location is of required type
            return checkLocation.getBlock().getType() == requiredMaterial;
        } catch (IllegalArgumentException e) {
            context.getPlugin().getLogger().warning("Invalid material in IsBlockTypeCondition: " + materialName);
            return false;
        }
    }
}