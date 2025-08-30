package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

public class SetWeatherAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        if (context == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawWeather = block.getParameter("weather");
        if (rawWeather == null) return;

        String weatherStr = resolver.resolve(context, rawWeather).asString();

        if (weatherStr == null) return;

        try {
            WeatherType weatherType;
            switch (weatherStr.toLowerCase()) {
                case "clear":
                case "sunny":
                    weatherType = WeatherType.CLEAR;
                    break;
                case "rain":
                case "rainy":
                    weatherType = WeatherType.DOWNFALL;
                    break;
                default:
                    player.sendMessage("§cНеизвестный тип погоды: " + weatherStr);
                    return;
            }
            
            player.setPlayerWeather(weatherType);
            player.sendMessage("§a🌤 Погода изменена на: " + weatherStr);
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка установки погоды: " + e.getMessage());
        }
    }
} 