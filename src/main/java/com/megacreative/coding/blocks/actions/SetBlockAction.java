package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Действие для установки блока.
 * Поддерживает получение материала и координат из параметров.
 */
public class SetBlockAction implements BlockAction {
    
    // Аргументы для получения данных
    private final Argument<TextValue> materialArgument = new ParameterArgument("material");
    private final Argument<TextValue> coordsArgument = new ParameterArgument("coords");
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // 1. Получаем материал
        TextValue materialValue = materialArgument.parse(context.getCurrentBlock()).orElse(null);
        if (materialValue == null) {
            player.sendMessage("§cОшибка: не указан материал!");
            return;
        }
        
        try {
            // 2. Вычисляем материал
            String materialStr = materialValue.get(context);
            Material material = Material.valueOf(materialStr.toUpperCase());
            
            // 3. Получаем координаты
            Location location;
            TextValue coordsValue = coordsArgument.parse(context.getCurrentBlock()).orElse(null);
            
            if (coordsValue != null) {
                // 4. Парсим координаты "x y z"
                String coordsStr = coordsValue.get(context);
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
                // 5. Если координаты не указаны, используем локацию игрока
                location = player.getLocation();
            }
            
            // 6. Устанавливаем блок
            location.getBlock().setType(material);
            player.sendMessage("§a🔲 Блок " + material.name() + " установлен!");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: координаты должны быть числами!");
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cОшибка: неизвестный материал '" + materialValue.getRawText() + "'");
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Установить блок': " + e.getMessage());
        }
    }
} 