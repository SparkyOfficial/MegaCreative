package com.megacreative.commands;
import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.coding.SimpleScriptCompiler;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        // Check for test script command
        if (args.length > 0 && "testscript".equals(args[0])) {
            createTestScript(player);
            return true;
        }
        
        // Check for compile command
        if (args.length > 0 && "compile".equals(args[0])) {
            compileScripts(player);
            return true;
        }
        
        // Check for listblocks command
        if (args.length > 0 && "listblocks".equals(args[0])) {
            listCodeBlocks(player);
            return true;
        }
        
        // Check for run command
        if (args.length > 0 && "run".equals(args[0])) {
            if (args.length > 1) {
                runScript(player, args[1]);
            } else {
                player.sendMessage("§cИспользование: /dev run <имя скрипта>");
            }
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
     * Compiles all scripts in the current world
     */
    private void compileScripts(Player player) {
        try {
            World currentWorld = player.getWorld();
            CreativeWorld creativeWorld = findCreativeWorld(currentWorld);
            
            if (creativeWorld == null) {
                player.sendMessage("§cВы не находитесь в мире MegaCreative!");
                return;
            }
            
            // Create the script compiler
            SimpleScriptCompiler compiler = new SimpleScriptCompiler(
                plugin,
                plugin.getServiceRegistry().getBlockConfigService(),
                plugin.getServiceRegistry().getBlockPlacementHandler()
            );
            
            // Compile all scripts in the world
            player.sendMessage("§eКомпиляция скриптов...");
            java.util.List<com.megacreative.coding.CodeScript> scripts = compiler.compileWorldScripts(currentWorld);
            
            // Save the compiled scripts
            compiler.saveScriptsToWorld(currentWorld, scripts);
            
            player.sendMessage("§aСкрипты скомпилированы успешно!");
            player.sendMessage("§7Найдено скриптов: " + scripts.size());
            
            // Show detailed structure of each script
            compiler.printScriptStructure(scripts);
            
            // Show summary of each script
            for (int i = 0; i < scripts.size(); i++) {
                com.megacreative.coding.CodeScript script = scripts.get(i);
                int blockCount = script.getBlocks().size();
                player.sendMessage("§7  " + (i + 1) + ". " + script.getRootBlock().getAction() + " (" + blockCount + " блоков)");
            }
        } catch (Exception e) {
            player.sendMessage("§cОшибка компиляции скриптов: " + e.getMessage());
            plugin.getLogger().severe("Ошибка компиляции скриптов: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Lists all code blocks in the current world
     */
    private void listCodeBlocks(Player player) {
        try {
            World currentWorld = player.getWorld();
            
            // Get the block placement handler
            com.megacreative.coding.BlockPlacementHandler placementHandler = 
                plugin.getServiceRegistry().getBlockPlacementHandler();
            
            // Get all code blocks
            java.util.Map<org.bukkit.Location, com.megacreative.coding.CodeBlock> codeBlocks = 
                placementHandler.getBlockCodeBlocks();
            
            player.sendMessage("§eСписок всех блоков кода в мире:");
            player.sendMessage("§7Всего блоков: " + codeBlocks.size());
            
            // Filter blocks by world and display them
            int count = 0;
            for (java.util.Map.Entry<org.bukkit.Location, com.megacreative.coding.CodeBlock> entry : codeBlocks.entrySet()) {
                org.bukkit.Location loc = entry.getKey();
                com.megacreative.coding.CodeBlock block = entry.getValue();
                
                // Only show blocks in the current world
                if (loc.getWorld().equals(currentWorld)) {
                    count++;
                    player.sendMessage("§7" + count + ". " + block.getMaterialName() + " (" + block.getAction() + ") at " + 
                        loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
                }
            }
            
            if (count == 0) {
                player.sendMessage("§7В текущем мире нет блоков кода.");
            }
        } catch (Exception e) {
            player.sendMessage("§cОшибка при получении списка блоков: " + e.getMessage());
            plugin.getLogger().severe("Ошибка при получении списка блоков: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Runs a specific script by name
     */
    private void runScript(Player player, String scriptName) {
        try {
            World currentWorld = player.getWorld();
            CreativeWorld creativeWorld = findCreativeWorld(currentWorld);
            
            if (creativeWorld == null) {
                player.sendMessage("§cВы не находитесь в мире MegaCreative!");
                return;
            }
            
            // Get compiled scripts
            java.util.List<com.megacreative.coding.CodeScript> scripts = creativeWorld.getScripts();
            if (scripts == null || scripts.isEmpty()) {
                player.sendMessage("§cВ мире нет скомпилированных скриптов. Сначала выполните /dev compile");
                return;
            }
            
            // Find script by name or event type
            com.megacreative.coding.CodeScript targetScript = null;
            for (com.megacreative.coding.CodeScript script : scripts) {
                if (script != null && script.getRootBlock() != null) {
                    // Check if it matches by name or action
                    if (scriptName.equals(script.getName()) || 
                        scriptName.equals(script.getRootBlock().getAction())) {
                        targetScript = script;
                        break;
                    }
                }
            }
            
            if (targetScript == null) {
                player.sendMessage("§cСкрипт '" + scriptName + "' не найден.");
                return;
            }
            
            // Execute the script
            player.sendMessage("§eЗапуск скрипта: " + targetScript.getName());
            
            // Get the script engine
            com.megacreative.coding.ScriptEngine scriptEngine = 
                plugin.getServiceRegistry().getScriptEngine();
            
            // Execute the script
            java.util.concurrent.CompletableFuture<com.megacreative.coding.executors.ExecutionResult> future = 
                scriptEngine.executeScript(targetScript, player, "manual");
            
            // Handle the result
            future.thenAccept(result -> {
                if (result.isSuccess()) {
                    player.sendMessage("§aСкрипт выполнен успешно!");
                } else {
                    player.sendMessage("§cОшибка выполнения скрипта: " + result.getMessage());
                }
            });
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при запуске скрипта: " + e.getMessage());
            plugin.getLogger().severe("Ошибка при запуске скрипта: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a test script by placing actual blocks in the world
     */
    private void createTestScript(Player player) {
        try {
            World currentWorld = player.getWorld();
            CreativeWorld creativeWorld = findCreativeWorld(currentWorld);
            
            if (creativeWorld == null) {
                player.sendMessage("§cВы не находитесь в мире MegaCreative!");
                return;
            }
            
            // Get the player's current location as the starting point
            Location startLoc = player.getLocation().clone();
            
            // Make sure we're placing blocks on a valid surface (glass platform)
            startLoc.setY(64); // Platform height
            
            // Round coordinates to integers
            startLoc.setX(Math.round(startLoc.getX()));
            startLoc.setZ(Math.round(startLoc.getZ()));
            
            player.sendMessage("§eСоздание тестового скрипта на координатах: " + 
                startLoc.getBlockX() + ", " + startLoc.getBlockY() + ", " + startLoc.getBlockZ());
            
            // Get the block placement handler
            com.megacreative.coding.BlockPlacementHandler placementHandler = 
                plugin.getServiceRegistry().getBlockPlacementHandler();
            
            // Place event block (diamond block for player join)
            Location eventBlockLoc = startLoc.clone();
            eventBlockLoc.getBlock().setType(Material.DIAMOND_BLOCK);
            
            // Simulate block placement event
            BlockPlaceEvent eventBlockEvent = new BlockPlaceEvent(
                eventBlockLoc.getBlock(),
                eventBlockLoc.getBlock().getState(),
                eventBlockLoc.clone().add(0, -1, 0).getBlock(), // Placed against
                new org.bukkit.inventory.ItemStack(Material.DIAMOND_BLOCK),
                player,
                true,
                org.bukkit.inventory.EquipmentSlot.HAND
            );
            placementHandler.onBlockPlace(eventBlockEvent);
            
            // Place action block (cobblestone for send message) to the right
            Location actionBlockLoc = startLoc.clone().add(1, 0, 0);
            actionBlockLoc.getBlock().setType(Material.COBBLESTONE);
            
            // Simulate block placement event
            BlockPlaceEvent actionBlockEvent = new BlockPlaceEvent(
                actionBlockLoc.getBlock(),
                actionBlockLoc.getBlock().getState(),
                actionBlockLoc.clone().add(0, -1, 0).getBlock(), // Placed against
                new org.bukkit.inventory.ItemStack(Material.COBBLESTONE),
                player,
                true,
                org.bukkit.inventory.EquipmentSlot.HAND
            );
            placementHandler.onBlockPlace(actionBlockEvent);
            
            player.sendMessage("§aТестовый скрипт создан успешно!");
            player.sendMessage("§7Попробуйте скомпилировать скрипты с помощью /dev compile");
            player.sendMessage("§7Затем перезайдите на сервер для тестирования.");
        } catch (Exception e) {
            player.sendMessage("§cОшибка создания тестового скрипта: " + e.getMessage());
            plugin.getLogger().severe("Ошибка создания тестового скрипта: " + e.getMessage());
            e.printStackTrace();
        }
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
        player.sendMessage("§7/dev compile §8- §fСкомпилировать скрипты в текущем мире");
        player.sendMessage("§7/dev testscript §8- §fСоздать тестовый скрипт");
        player.sendMessage("§7/dev listblocks §8- §fПоказать все блоки кода в мире");
        player.sendMessage("§7/dev run <script> §8- §fЗапустить скрипт вручную");
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
            
        createVariableItem(variablesInventory, 12, org.bukkit.Material.SLIME_BALL, "§a§лЧисловая переменная",
            "§7Для хранения чисел", "§eПример: счет, уровень", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 14, org.bukkit.Material.COMPASS, "§b§лПеременная локации",
            "§7Для хранения координат", "§eПример: точка спавна", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 16, org.bukkit.Material.CHEST, "§6§лПеременная предмета",
            "§7Для хранения предметов", "§eПример: награда", "§8Нажмите для создания");
            
        
        createVariableItem(variablesInventory, 28, org.bukkit.Material.REPEATER, "§d§лПеременная списка",
            "§7Для хранения списков", "§eПример: топ игроков", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 30, org.bukkit.Material.DISPENSER, "§5§лПеременная команды",
            "§7Для хранения команд", "§eПример: действие", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 32, org.bukkit.Material.PLAYER_HEAD, "§3§лПеременная игрока",
            "§7Для хранения данных игрока", "§eПример: последний убийца", "§8Нажмите для создания");
            
        createVariableItem(variablesInventory, 34, org.bukkit.Material.HOPPER, "§c§лГлобальная переменная",
            "§7Для хранения общих данных", "§eДоступна всем игрокам", "§8Нажмите для создания");
        
        
        for (int i = 0; i < 9; i++) {
            variablesInventory.setItem(i, createGlassPane());
            variablesInventory.setItem(45 + i, createGlassPane());
        }
        
        
        createVariableItem(variablesInventory, 48, org.bukkit.Material.BOOK, "§e§лПомощь",
            "§7Нажмите для просмотра", "§7руководства по переменным");
            
        createVariableItem(variablesInventory, 50, org.bukkit.Material.BARRIER, "§c§лЗакрыть",
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
     * Teleportiert den Spieler in die Entwicklungs-Welt und konфигuriert sie
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
            creator.environment(World.Environment.NORMAL);
            
            
            creator.generator(new com.megacreative.worlds.DevWorldGenerator());
            
            
            creator.generateStructures(false);
            
            
            return Bukkit.createWorld(creator);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка создания мира разработки: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            
            
            try {
                WorldCreator fallbackCreator = new WorldCreator(creativeWorld.getDevWorldName());
                fallbackCreator.environment(World.Environment.NORMAL);
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
                plugin.getLogger().fine("Производится первичная настройка мира разработки...");
                
                
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