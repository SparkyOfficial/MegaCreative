package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import com.megacreative.coding.values.LocationValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Действие для телепортации игрока.
 * Поддерживает получение координат из параметра "coords" в формате "x y z".
 */
public class TeleportAction implements BlockAction {
    
    // Аргумент для получения координат
    private final Argument<TextValue> coordsArgument = new ParameterArgument("coords");
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // 1. Получаем координаты из параметра
        TextValue coordsValue = coordsArgument.parse(context.getCurrentBlock()).orElse(null);
        if (coordsValue == null) {
            player.sendMessage("§cОшибка: не указаны координаты!");
            return;
        }
        
        try {
            // 2. Вычисляем координаты (обрабатываем плейсхолдеры)
            String coordsStr = coordsValue.get(context);
            
            // 3. Парсим координаты
            String[] parts = coordsStr.split(" ");
            if (parts.length == 3) {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                
                // 4. Создаем локацию и телепортируем
                Location targetLocation = new Location(player.getWorld(), x, y, z);
                player.teleport(targetLocation);
                
                // 5. Уведомляем игрока
                player.sendMessage("§a✓ Телепортация на координаты: " + coordsStr);
                
            } else {
                player.sendMessage("§cОшибка: координаты должны быть в формате 'x y z'");
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: координаты должны быть числами!");
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Телепортация': " + e.getMessage());
        }
    }
} 