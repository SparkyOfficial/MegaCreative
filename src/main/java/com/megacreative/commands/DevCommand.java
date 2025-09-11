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
        
        // Проверяем подкоманды
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "refresh", "tools" -> {
                    plugin.getServiceRegistry().getDevInventoryManager().refreshTools(player);
                    return true;
                }
                case "variables" -> {
                    openVariablesMenu(player);
                    return true;
                }
                case "help" -> {
                    sendHelp(player);
                    return true;
                }
            }
        }
        
        // Debug logging
        plugin.getLogger().info("[DEBUG] Player " + player.getName() + " executed /dev command");
       
        World currentWorld = player.getWorld();
        plugin.getLogger().info("[DEBUG] Player is in world: " + currentWorld.getName());
        
        CreativeWorld creativeWorld = findCreativeWorld(currentWorld);
       
        if (creativeWorld == null) {
            plugin.getLogger().info("[DEBUG] CreativeWorld not found for world: " + currentWorld.getName());
            player.sendMessage("§cВы не находитесь в мире MegaCreative!");
            return true;
        }
        
        plugin.getLogger().info("[DEBUG] Found CreativeWorld: " + creativeWorld.getName() + " (ID: " + creativeWorld.getId() + ")");
       
        if (!creativeWorld.canCode(player)) {
            plugin.getLogger().info("[DEBUG] Player " + player.getName() + " cannot code in world " + creativeWorld.getName());
            player.sendMessage("§cУ вас нет прав на кодирование в этом мире!");
            return true;
        }
        
        plugin.getLogger().info("[DEBUG] Player " + player.getName() + " has coding permissions");
       
        creativeWorld.setMode(WorldMode.DEV);
        plugin.getLogger().info("[DEBUG] Set world mode to DEV");

        // Запускаем всю логику в основном потоке сервера для потокобезопасности
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                // Проверяем существование мира в основном потоке
                World devWorld = Bukkit.getWorld(creativeWorld.getDevWorldName());
                if (devWorld != null) {
                    // Мир уже существует, просто телепортируем
                    teleportToDevWorld(player, devWorld);
                } else {
                    // Мир нужно создать
                    player.sendMessage("§eСоздаем мир для разработки...");
                    
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
                        player.sendMessage("§cПроизошла критическая ошибка при создании мира разработки.");
                    }
                }
            }
        }.runTask(plugin); // Выполнить в основном потоке
        return true;
    }
    
    /**
     * Отображает справку по команде /dev
     */
    private void sendHelp(Player player) {
        player.sendMessage("§8§m                    §r §6§l/dev Справка §8§m                    ");
        player.sendMessage("§7/dev §8- §fПерейти в режим разработки");
        player.sendMessage("§7/dev refresh §8- §fВосстановить недостающие инструменты");
        player.sendMessage("§7/dev tools §8- §fТо же, что и refresh");
        player.sendMessage("§7/dev variables §8- §fОткрыть меню переменных");
        player.sendMessage("§7/dev help §8- §fПоказать эту справку");
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Открывает меню переменных (базовая реализация)
     */
    private void openVariablesMenu(Player player) {
        // Проверяем, что игрок в мире разработки
        if (!player.getWorld().getName().endsWith("_dev")) {
            player.sendMessage("§cКоманда /dev variables доступна только в мире разработки!");
            return;
        }
        
        // Создаем простое GUI с предметами-переменными
        org.bukkit.inventory.Inventory variablesInventory = org.bukkit.Bukkit.createInventory(null, 27, "§8Меню переменных");
        
        // Текст (книга)
        org.bukkit.inventory.ItemStack textVar = new org.bukkit.inventory.ItemStack(org.bukkit.Material.WRITABLE_BOOK);
        org.bukkit.inventory.meta.ItemMeta textMeta = textVar.getItemMeta();
        textMeta.setDisplayName("§e§lТекстовая переменная");
        textMeta.setLore(java.util.Arrays.asList(
            "§7Для хранения текста",
            "§eПереименуйте с названием"
        ));
        textVar.setItemMeta(textMeta);
        
        // Число (слайм)
        org.bukkit.inventory.ItemStack numberVar = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SLIME_BALL);
        org.bukkit.inventory.meta.ItemMeta numberMeta = numberVar.getItemMeta();
        numberMeta.setDisplayName("§a§lЧисловая переменная");
        numberMeta.setLore(java.util.Arrays.asList(
            "§7Для хранения чисел",
            "§eПереименуйте с названием"
        ));
        numberVar.setItemMeta(numberMeta);
        
        // Локация (компас)
        org.bukkit.inventory.ItemStack locationVar = new org.bukkit.inventory.ItemStack(org.bukkit.Material.COMPASS);
        org.bukkit.inventory.meta.ItemMeta locationMeta = locationVar.getItemMeta();
        locationMeta.setDisplayName("§b§lПеременная локации");
        locationMeta.setLore(java.util.Arrays.asList(
            "§7Для хранения координат",
            "§eПереименуйте с названием"
        ));
        locationVar.setItemMeta(locationMeta);
        
        // Предмет (сундук)
        org.bukkit.inventory.ItemStack itemVar = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHEST);
        org.bukkit.inventory.meta.ItemMeta itemMeta = itemVar.getItemMeta();
        itemMeta.setDisplayName("§6§lПеременная предмета");
        itemMeta.setLore(java.util.Arrays.asList(
            "§7Для хранения предметов",
            "§eПереименуйте с названием"
        ));
        itemVar.setItemMeta(itemMeta);
        
        // Размещаем предметы в инвентаре
        variablesInventory.setItem(10, textVar);
        variablesInventory.setItem(12, numberVar);
        variablesInventory.setItem(14, locationVar);
        variablesInventory.setItem(16, itemVar);
        
        // Открываем инвентарь
        player.openInventory(variablesInventory);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
        player.sendMessage("§aМеню переменных открыто! Возьмите нужные предметы.");
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