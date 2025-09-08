package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SaveLocationAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawLocationName = block.getParameter("locationName");
        
        if (rawLocationName == null) {
            return ExecutionResult.error("Parameter 'locationName' is missing");
        }
        
        DataValue locationNameValue = resolver.resolve(context, rawLocationName);
        String locationName = locationNameValue.asString();

        if (locationName == null) {
            return ExecutionResult.error("Location name parameter is null");
        }

        try {
            // Получаем текущую локацию игрока
            Location location = player.getLocation();
            
            // Сохраняем локацию как глобальную переменную
            context.setGlobalVariable("location_" + locationName, location);
            
            player.sendMessage("§a✅ Локация '" + locationName + "' сохранена");
            return ExecutionResult.success("Location '" + locationName + "' saved");
        } catch (Exception e) {
            return ExecutionResult.error("Error saving location: " + e.getMessage());
        }
    }
}