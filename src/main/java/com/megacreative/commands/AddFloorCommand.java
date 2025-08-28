package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для добавления дополнительных этажей в dev-мире
 * Использование: /addfloor [номер_этажа]
 */
public class AddFloorCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public AddFloorCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        World world = player.getWorld();
        
        // Проверяем, находится ли игрок в dev-мире
        if (!isDevWorld(world)) {
            player.sendMessage("§cЭту команду можно использовать только в мире разработки!");
            return true;
        }
        
        // Получаем CreativeWorld для проверки прав
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld == null) {
            player.sendMessage("§cОшибка: мир не найден в системе!");
            return true;
        }
        
        // Проверяем права на кодирование
        if (!creativeWorld.canCode(player)) {
            player.sendMessage("§cУ вас нет прав на изменение кода в этом мире!");
            return true;
        }
        
        // Определяем номер этажа
        int floorNumber = 1; // По умолчанию первый дополнительный этаж
        
        if (args.length > 0) {
            try {
                floorNumber = Integer.parseInt(args[0]);
                if (floorNumber < 1 || floorNumber > DevWorldGenerator.getMaxFloors()) {
                    player.sendMessage("§cНомер этажа должен быть от 1 до " + DevWorldGenerator.getMaxFloors() + "!");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cНеверный номер этажа! Используйте число от 1 до " + DevWorldGenerator.getMaxFloors());
                return true;
            }
        }
        
        // Проверяем, не существует ли уже этот этаж
        if (floorExists(world, floorNumber)) {
            player.sendMessage("§cЭтаж " + floorNumber + " уже существует!");
            return true;
        }
        
        // Добавляем этаж
        player.sendMessage("§aДобавление этажа " + floorNumber + "...");
        
        try {
            DevWorldGenerator.addFloorToWorld(world, floorNumber);
            
            // Сохраняем информацию об этаже в мире
            saveFloorInfo(creativeWorld, floorNumber);
            
            // Телепортируем игрока на новый этаж
            int floorY = DevWorldGenerator.getPlatformY() + (floorNumber * DevWorldGenerator.getFloorHeight());
            player.teleport(player.getLocation().clone().add(0, floorY - player.getLocation().getY() + 2, 0));
            
            player.sendMessage("§aЭтаж " + floorNumber + " успешно добавлен!");
            player.sendMessage("§7Вы можете использовать /tp чтобы перемещаться между этажами");
            
            // Уведомляем других игроков в мире
            for (Player p : world.getPlayers()) {
                if (!p.equals(player)) {
                    p.sendMessage("§a" + player.getName() + " добавил новый этаж кодирования: " + floorNumber);
                }
            }
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при создании этажа: " + e.getMessage());
            plugin.getLogger().severe("Ошибка при создании этажа " + floorNumber + " в мире " + world.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    /**
     * Проверяет, является ли мир dev-миром
     */
    private boolean isDevWorld(World world) {
        return world.getName().endsWith("_dev");
    }
    
    /**
     * Проверяет, существует ли уже указанный этаж
     */
    private boolean floorExists(World world, int floorNumber) {
        int floorY = DevWorldGenerator.getPlatformY() + (floorNumber * DevWorldGenerator.getFloorHeight());
        
        // Проверяем несколько блоков на предполагаемой высоте этажа
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                if (world.getBlockAt(x, floorY, z).getType().name().contains("GLASS")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Сохраняет информацию об этаже в мире
     */
    private void saveFloorInfo(CreativeWorld creativeWorld, int floorNumber) {
        // Можно расширить для сохранения метаданных об этажах
        // Например, кто создал, когда, для какой цели и т.д.
        plugin.getLogger().info("Этаж " + floorNumber + " добавлен в мир " + creativeWorld.getName());
    }
}