package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

public class SetWeatherAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawWeather = block.getParameter("weather");

        String weatherStr = ParameterResolver.resolve(context, rawWeather);

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