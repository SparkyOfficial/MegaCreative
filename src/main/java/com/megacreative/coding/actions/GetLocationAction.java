package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GetLocationAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawLocationName = block.getParameter("locationName");
        DataValue rawTargetVariable = block.getParameter("targetVariable");
        
        if (rawLocationName == null) {
            return ExecutionResult.error("Parameter 'locationName' is missing");
        }
        
        if (rawTargetVariable == null) {
            return ExecutionResult.error("Parameter 'targetVariable' is missing");
        }
        
        DataValue locationNameValue = resolver.resolve(context, rawLocationName);
        DataValue targetVariableValue = resolver.resolve(context, rawTargetVariable);
        
        String locationName = locationNameValue.asString();
        String targetVariable = targetVariableValue.asString();

        if (locationName == null) {
            return ExecutionResult.error("Location name parameter is null");
        }
        
        if (targetVariable == null) {
            return ExecutionResult.error("Target variable parameter is null");
        }

        try {
            // Получаем сохраненную локацию из глобальных переменных
            Object locationObj = context.getGlobalVariable("location_" + locationName);
            
            if (locationObj == null) {
                return ExecutionResult.error("Location '" + locationName + "' not found");
            }
            
            if (!(locationObj instanceof Location)) {
                return ExecutionResult.error("Variable '" + locationName + "' is not a location");
            }
            
            Location location = (Location) locationObj;
            
            // Сохраняем локацию в целевую переменную
            context.setVariable(targetVariable, location);
            
            player.sendMessage("§a✅ Локация '" + locationName + "' загружена в переменную '" + targetVariable + "'");
            return ExecutionResult.success("Location '" + locationName + "' loaded to variable '" + targetVariable + "'");
        } catch (Exception e) {
            return ExecutionResult.error("Error getting location: " + e.getMessage());
        }
    }
}