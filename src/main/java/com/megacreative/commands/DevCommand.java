package com.megacreative.commands;
import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

/**
 * Команда для перехода в режим разработки
 *
 * Command to switch to development mode
 *
 * Befehl zum Wechseln in den Entwicklungsmodus
 */
public class DevCommand implements CommandExecutor {
   
    private final MegaCreative plugin;
   
    /**
     * Конструктор команды DevCommand
     * @param plugin основной плагин
     *
     * Constructor for DevCommand
     * @param plugin main plugin
     *
     * Konstruktor für DevCommand
     * @param plugin Haupt-Plugin
     */
    public DevCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
   
    /**
     * Обрабатывает выполнение команды /dev
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /dev command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /dev-Befehls
     * @param sender Befehlsabsender
     * @param command Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
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
        
        
        plugin.getServiceRegistry().getWorldManager().switchToDevWorld(player, creativeWorld.getId());
        
        return true;
    }
    
    /**
     * Отображает справку по команде /dev
     * Displays help for the /dev command
     * Zeigt Hilfe für den /dev-Befehl an
     */
    private void sendHelp(Player player) {
        player.sendMessage("§8§m                    §r §6§l/dev Справка §8§m                    ");
        player.sendMessage("§7/dev §8- §fПерейти в режим разработки");
        player.sendMessage("§7/dev refresh §8- §fВосстановить недостающие инструменты");
        player.sendMessage("§7/dev tools §8- §fТо же, что и refresh");
        player.sendMessage("§7/dev variables §8- §fОткрыть меню переменных");
        player.sendMessage("§7/dev switch §8- §fПереключиться в режим разработки (дуальные миры)");
        player.sendMessage("§7/dev help §8- §fПоказать эту справку");
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Открывает меню переменных (базовая реализация)
     * Opens the variables menu (basic implementation)
     * Öffnet das Variablen-Menü (Grundimplementierung)
     */
    private void openVariablesMenu(Player player) {
        
        if (!player.getWorld().getName().endsWith("_dev")) {
            player.sendMessage("§cКоманда /dev variables доступна только в мире разработки!");
            return;
        }
        
        
        org.bukkit.inventory.Inventory variablesInventory = org.bukkit.Bukkit.createInventory(null, 54, "§8§lМеню переменных");
        
        
        createVariableItem(variablesInventory, 10, org.bukkit.Material.WRITABLE_BOOK, "§e§lТекстовая переменная",
            "§7Для хранения текста", "§eПример: имя, сообщение", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 12, org.bukkit.Material.SLIME_BALL, "§a§lЧисловая переменная",
            "§7Для хранения чисел", "§eПример: счет, уровень", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 14, org.bukkit.Material.COMPASS, "§b§lПеременная локации",
            "§7Для хранения координат", "§eПример: точка спавна", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 16, org.bukkit.Material.CHEST, "§6§lПеременная предмета",
            "§7Для хранения предметов", "§eПример: награда", "§8Нажмите для создания");
            
        
        createVariableItem(variablesInventory, 28, org.bukkit.Material.REPEATER, "§d§lПеременная списка",
            "§7Для хранения списков", "§eПример: топ игроков", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 30, org.bukkit.Material.DISPENSER, "§5§lПеременная команды",
            "§7Для хранения команд", "§eПример: действие", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 32, org.bukkit.Material.PLAYER_HEAD, "§3§lПеременная игрока",
            "§7Для хранения данных игрока", "§eПример: последний убийца", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 34, org.bukkit.Material.HOPPER, "§c§lГлобальная переменная",
            "§7Для хранения общих данных", "§eДоступна всем игрокам", "§8Нажмите для создания");
        
        
        for (int i = 0; i < 9; i++) {
            variablesInventory.setItem(i, createGlassPane());
            variablesInventory.setItem(45 + i, createGlassPane());
        }
        
        
        createVariableItem(variablesInventory, 48, org.bukkit.Material.BOOK, "§e§lПомощь",
            "§7Нажмите для просмотра", "§7руководства по переменным");
            
        createVariableItem(variablesInventory, 50, org.bukkit.Material.BARRIER, "§c§lЗакрыть",
            "§7Нажмите для закрытия", "§7меню переменных");
        
        
        player.openInventory(variablesInventory);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
        player.sendMessage("§a§lМеню переменных открыто! §7Выберите тип переменной для создания.");
    }
    
    private void createVariableItem(org.bukkit.inventory.Inventory inventory, int slot, org.bukkit.Material material,
                                  String name, String... lore) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(java.util.Arrays.asList(lore));
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }
    
    private org.bukkit.inventory.ItemStack createGlassPane() {
        org.bukkit.inventory.ItemStack glass = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLACK_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }
   
    /**
     * Телепортирует игрока в мир разработки и настраивает его
     * Teleports player to development world and configures it
     * Teleportiert den Spieler in die Entwicklungs-Welt und konfiguriert sie
     */
    private void teleportToDevWorld(Player player, World devWorld) {
        player.teleport(devWorld.getSpawnLocation());
        player.setGameMode(GameMode.CREATIVE);
        
        
        player.getInventory().clear();
        
        
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
            
            
            creator.generator(new com.megacreative.worlds.DevWorldGenerator());
            
            
            creator.generatorSettings("{\"layers\":[{\"block\":\"bedrock\",\"height\":1},{\"block\":\"stone\",\"height\":2},{\"block\":\"grass_block\",\"height\":1}],\"biome\":\"plains\"}");
            creator.generateStructures(false);
            
            
            return Bukkit.createWorld(creator);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка создания мира разработки: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            
            
            try {
                WorldCreator fallbackCreator = new WorldCreator(creativeWorld.getDevWorldName());
                fallbackCreator.environment(World.Environment.NORMAL);
                fallbackCreator.type(WorldType.NORMAL);
                fallbackCreator.generateStructures(false);
                
                fallbackCreator.generator(new com.megacreative.worlds.DevWorldGenerator());
                
                
                return Bukkit.createWorld(fallbackCreator);
                
            } catch (Exception fallbackException) {
                plugin.getLogger().severe("Критическая ошибка создания мира: " + fallbackException.getMessage());
                plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(fallbackException.getStackTrace()));
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
            
            devWorld.setTime(6000); 
            devWorld.setStorm(false);
            devWorld.setThundering(false);
            
            
            Location spawnLocation = new Location(devWorld, 0, 70, 0);
            
            
            if (!devWorld.getPersistentDataContainer().has(new NamespacedKey(plugin, "initialized"), PersistentDataType.BYTE)) {
                plugin.getLogger().info("Производится первичная настройка мира разработки...");
                
                
                spawnLocation = new Location(devWorld, 0, 66, 0);
                
                devWorld.getPersistentDataContainer().set(new NamespacedKey(plugin, "initialized"), PersistentDataType.BYTE, (byte)1);
            }
            
            devWorld.setSpawnLocation(spawnLocation);
           
            WorldBorder border = devWorld.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(400); 
            border.setWarningDistance(10);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка настройки мира разработки: " + e.getMessage());
        }
    }
   
    private CreativeWorld findCreativeWorld(World bukkitWorld) {
        try {
            String worldName = bukkitWorld.getName();
            
            
            if (worldName.startsWith("megacreative_")) {
                String id = worldName.replace("megacreative_", "")
                                      .replace("-code", "")    
                                      .replace("-world", "")   
                                      .replace("_dev", "");    
                return plugin.getServiceRegistry().getWorldManager().getWorld(id);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка поиска мира: " + e.getMessage());
        }
        return null;
    }
}