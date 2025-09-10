package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.MapValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class CreateMapAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the block
            DataValue mapNameValue = block.getParameter("map_name");
            
            if (mapNameValue == null || mapNameValue.isEmpty()) {
                return ExecutionResult.error("Map name parameter is missing.");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMapName = resolver.resolve(context, mapNameValue);
            
            String mapName = resolvedMapName.asString();
            
            // Create the map
            Map<String, DataValue> initialMap = new HashMap<>();
            MapValue mapValue = new MapValue(initialMap);
            
            // Store the map in the variable manager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                variableManager.setVariable(mapName, mapValue, VariableScope.LOCAL, context.getScriptId());
            }
            
            return ExecutionResult.success("Map '" + mapName + "' created.");

        } catch (Exception e) {
            return ExecutionResult.error("Error creating map: " + e.getMessage());
        }
    }
}