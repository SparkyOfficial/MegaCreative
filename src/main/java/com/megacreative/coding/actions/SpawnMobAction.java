package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpawnMobAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;
        
        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
        DataValue rawMobType = block.getParameter("mob");
        if (rawMobType == null) return;
        
        String mobType = resolver.resolve(context, rawMobType).asString();
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