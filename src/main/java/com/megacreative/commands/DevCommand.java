package com.megacreative.commands;
import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class DevCommand implements CommandExecutor {
   
    private final MegaCreative plugin;
   
    public DevCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
   
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
       
        World currentWorld = player.getWorld();
        CreativeWorld creativeWorld = findCreativeWorld(currentWorld);
       
        if (creativeWorld == null) {
            player.sendMessage("§cВы не находитесь в мире MegaCreative!");
            return true;
        }
       
        if (!creativeWorld.canCode(player)) {
            player.sendMessage("§cУ вас нет прав на кодирование в этом мире!");
            return true;
        }
       
        creativeWorld.setMode(WorldMode.DEV);
       
        // Проверяем существование мира в основном потоке
        World devWorld = Bukkit.getWorld(creativeWorld.getDevWorldName());
        if (devWorld != null) {
            // Мир уже существует, просто телепортируем
            teleportToDevWorld(player, devWorld);
        } else {
            // Мир нужно создать
            player.sendMessage("§eСоздаем мир для разработки...");
            
            // Создаем мир в синхронном режиме
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    World newDevWorld = createDevWorld(creativeWorld);
                    if (newDevWorld != null) {
                        setupDevWorld(newDevWorld);
                        teleportToDevWorld(player, newDevWorld);
                        
                        // Сохраняем мир асинхронно
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            try {
                                plugin.getWorldManager().saveWorld(creativeWorld);
                            } catch (Exception e) {
                                plugin.getLogger().warning("Не удалось сохранить данные мира: " + e.getMessage());
                                Bukkit.getScheduler().runTask(plugin, () -> 
                                    player.sendMessage("§cНе удалось сохранить данные мира. Обратитесь к администратору."));
                            }
                        });
                    } else {
                        player.sendMessage("§cОшибка создания мира разработки!");
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Ошибка при создании мира разработки: " + e.getMessage());
                    e.printStackTrace();
                    player.sendMessage("§cПроизошла ошибка при создании мира разработки.");
                }
            });
        }
        return true;
    }
   
    /**
     * Телепортирует игрока в мир разработки и настраивает его
     */
    private void teleportToDevWorld(Player player, World devWorld) {
        player.teleport(devWorld.getSpawnLocation());
        player.setGameMode(GameMode.CREATIVE);
        
        // Очищаем инвентарь перед выдачей предметов
        player.getInventory().clear();
        
        // Выдаем блоки кодирования ДИНАМИЧЕСКИ
        CodingItems.giveCodingItems(player, plugin);
        
        player.sendMessage("§aВы телепортированы в мир разработки!");
        player.sendMessage("§7Здесь вы можете создавать код для своего мира");
    }
    
    /**
     * Создает мир для разработки
     */
    private World createDevWorld(CreativeWorld creativeWorld) {
        try {
            WorldCreator creator = new WorldCreator(creativeWorld.getDevWorldName());
            creator.type(WorldType.FLAT);
            creator.environment(World.Environment.NORMAL);
            
            // Используем наш кастомный генератор для мира разработки
            creator.generator(new com.megacreative.worlds.DevWorldGenerator());
            
            // Для плоского мира достаточно указать тип, generatorSettings не требуется
            // и вызывает ошибки на новых версиях
            creator.generateStructures(false);
            
            // Создаем мир с минимальными настройками
            return Bukkit.createWorld(creator);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка создания мира разработки: " + e.getMessage());
            e.printStackTrace();
            
            // Попытка создать мир с минимальными настройками
            try {
                WorldCreator fallbackCreator = new WorldCreator(creativeWorld.getDevWorldName());
                fallbackCreator.environment(World.Environment.NORMAL);
                fallbackCreator.type(WorldType.NORMAL);
                fallbackCreator.generateStructures(false);
                // Используем наш кастомный генератор для мира разработки
                fallbackCreator.generator(new com.megacreative.worlds.DevWorldGenerator());
                
                // Настройка (setupDevWorld) должна происходить в основном потоке
                return Bukkit.createWorld(fallbackCreator);
                
            } catch (Exception fallbackException) {
                plugin.getLogger().severe("Критическая ошибка создания мира: " + fallbackException.getMessage());
                fallbackException.printStackTrace();
                return null;
            }
        }
    }
   
    private void setupDevWorld(World devWorld) {
        try {
            devWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            devWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            devWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            devWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            devWorld.setGameRule(GameRule.KEEP_INVENTORY, true);
            devWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
            devWorld.setGameRule(GameRule.MOB_GRIEFING, false);
            
            devWorld.setTime(6000); // День
            devWorld.setStorm(false);
            devWorld.setThundering(false);
            
            // Устанавливаем спавн в безопасное место
            Location spawnLocation = new Location(devWorld, 0, 70, 0);
            
            // Проверяем по флагу, чтобы не делать это каждый раз
            if (!devWorld.getPersistentDataContainer().has(new NamespacedKey(plugin, "initialized"), PersistentDataType.BYTE)) {
                plugin.getLogger().info("Производится первичная настройка мира разработки...");
                
                // Спавн над платформой
                spawnLocation = new Location(devWorld, 0, 66, 0);
                // Ставим флаг, что мир настроен
                devWorld.getPersistentDataContainer().set(new NamespacedKey(plugin, "initialized"), PersistentDataType.BYTE, (byte)1);
            }
            
            devWorld.setSpawnLocation(spawnLocation);
           
            WorldBorder border = devWorld.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(400); // Увеличиваем размер для удобства разработки
            border.setWarningDistance(10);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка настройки мира разработки: " + e.getMessage());
        }
    }
   
    private CreativeWorld findCreativeWorld(World bukkitWorld) {
        try {
            String worldName = bukkitWorld.getName();
            if (worldName.startsWith("megacreative_")) {
                String id = worldName.replace("megacreative_", "").replace("_dev", "");
                return plugin.getWorldManager().getWorld(id);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка поиска мира: " + e.getMessage());
        }
        return null;
    }
}