package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.data.DataItemFactory;
import com.megacreative.coding.data.DataItemFactory.DataItem;
import com.megacreative.coding.ParameterSelectorGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Обработчик размещения и взаимодействия с блоками кодирования в мире разработки.
 */
public class BlockPlacementHandler implements Listener {

    private final MegaCreative plugin;
    
    // Хранилище: Location -> CodeBlock (содержит действие и параметры)
    private final Map<Location, CodeBlock> blockCodeBlocks = new HashMap<>();
    private final Map<UUID, Boolean> playerVisualizationStates = new HashMap<>();
    private final Map<UUID, Boolean> playerDebugStates = new HashMap<>();
    private final Map<UUID, Location> playerSelections = new HashMap<>();
    private final Map<UUID, CodeBlock> clipboard = new HashMap<>(); // Буфер обмена для копирования

<<<<<<< HEAD
    // Удаляем жестко закодированную карту ACTIONS - теперь используем BlockConfiguration
    // private static final Map<Material, List<String>> ACTIONS = Map.ofEntries(...);

=======
>>>>>>> ba7215a (Я вернулся)
    public BlockPlacementHandler(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Очищает данные игрока при отключении
     * @param playerId UUID игрока
     */
    public void cleanUpPlayerData(UUID playerId) {
        playerVisualizationStates.remove(playerId);
        playerDebugStates.remove(playerId);
        playerSelections.remove(playerId);
        clipboard.remove(playerId);
<<<<<<< HEAD
        plugin.getLogger().info("Очищены данные для игрока " + playerId);
=======
>>>>>>> ba7215a (Я вернулся)
    }

    /**
     * Обрабатывает размещение блоков кодирования
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        Material mat = block.getType();
        
<<<<<<< HEAD
        Bukkit.getLogger().info("[DEBUG] BlockPlaceEvent triggered for player " + player.getName());
        
        // Проверяем, является ли предмет "блоком кода" по названию, а не просто по материалу.
        if (!isCodingBlock(event.getItemInHand())) {
            Bukkit.getLogger().info("[DEBUG] Canceled: Not a coding block.");
            return;
        }
        if (!isInDevWorld(player)) {
            Bukkit.getLogger().info("[DEBUG] Canceled: Not in a dev world.");
=======
        // Проверяем, является ли предмет "блоком кода" по названию, а не просто по материалу.
        if (!isCodingBlock(event.getItemInHand())) {
            return;
        }
        if (!isInDevWorld(player)) {
>>>>>>> ba7215a (Я вернулся)
            return;
        }
        
        // Проверяем права доверенного игрока
        if (!plugin.getTrustedPlayerManager().canCodeInDevWorld(player)) {
            event.setCancelled(true);
<<<<<<< HEAD
            player.sendMessage("§c❌ У вас нет прав для размещения блоков кода в этом мире!");
            return;
        }

        Bukkit.getLogger().info("[DEBUG] Checks passed! Placing block...");
        
        // --- НАЧАЛО ИСПРАВЛЕНИЯ ---
        
        // 1. НЕ отменяем событие. Позволяем блоку установиться.
        // event.setCancelled(true); // <--- УБЕДИТЕСЬ, ЧТО ЭТОЙ СТРОКИ НЕТ!
        
        // 2. Создаем "заготовку" блока кода сразу.
        CodeBlock newCodeBlock = new CodeBlock(mat, "Настройка..."); // Временное действие
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        
        var world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world != null) {
            plugin.getBlockConnectionVisualizer().addBlock(world, block.getLocation(), newCodeBlock);
        }
        setSignOnBlock(block.getLocation(), "Настройка...");
        
        // 3. Открываем GUI для выбора действия, передавая уже созданный блок.
        // Запускаем через 1 тик, чтобы игрок увидел, что блок поставился.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            
            List<String> actions = plugin.getBlockConfiguration().getActionsForMaterial(mat);
            if (actions == null || actions.isEmpty()) {
                player.sendMessage("§cДля этого блока нет доступных действий.");
                blockCodeBlocks.remove(block.getLocation()); // Убираем, если что-то пошло не так
                block.setType(Material.AIR); // Удаляем физический блок
                return;
            }
            
            new CodingActionGUI(player, mat, block.getLocation(), actions, action -> {
                // Теперь открываем GUI для параметров
                new CodingParameterGUI(player, action, block.getLocation(), parameters -> {
                    // Обновляем наш уже существующий CodeBlock
                    newCodeBlock.setAction(action);
                    newCodeBlock.getParameters().clear();
                    newCodeBlock.getParameters().putAll(parameters);
                    
                    // Обновляем табличку
                    updateSignOnBlock(block.getLocation(), newCodeBlock);
                    
                    player.sendMessage("§aДействие установлено: §e" + action);
                    player.sendMessage("§7Параметры: §f" + parameters);
                }).open();
            }).open();
            
        }, 1L);
        
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---
    }

    /**
     * Обрабатывает ломание блоков кодирования
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isInDevWorld(event.getPlayer())) return;
        
        // Проверяем права доверенного игрока
        if (!plugin.getTrustedPlayerManager().canCodeInDevWorld(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c❌ У вас нет прав для удаления блоков кода в этом мире!");
            return;
        }
        
        Location loc = event.getBlock().getLocation();
        if (blockCodeBlocks.containsKey(loc)) {
            CodeBlock removedBlock = blockCodeBlocks.remove(loc);
            event.getPlayer().sendMessage("§c❌ Блок кода удален: " + removedBlock.getAction());
            
            // Удаляем табличку над блоком
            removeSignFromBlock(loc);
            
            var world = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (world != null) {
                plugin.getBlockConnectionVisualizer().removeBlock(world, loc);
            }
        }
    }
    
    // Удалить табличку с блока
    private void removeSignFromBlock(Location loc) {
        try {
            Block above = loc.clone().add(0, 1, 0).getBlock();
            if (above.getType() == Material.OAK_SIGN) {
                above.setType(Material.AIR);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось удалить табличку: " + e.getMessage());
=======
            return;
        }
        
        // Создаем "заготовку" блока кода сразу.
        CodeBlock newCodeBlock = new CodeBlock(mat, "Настройка..."); // Временное действие
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        
        // Устанавливаем табличку на блок
        setSignOnBlock(block.getLocation(), "Настройка...");
        
        // Открываем GUI для настройки блока
        handleBlockConfiguration(player, mat, block.getLocation(), false);
    }

    /**
     * Обрабатывает разрушение блоков кодирования
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        
        // Удаляем блок из нашей карты
        if (blockCodeBlocks.containsKey(loc)) {
            blockCodeBlocks.remove(loc);
            
            // Удаляем табличку, если она есть
            removeSignFromBlock(loc);
>>>>>>> ba7215a (Я вернулся)
        }
    }

    /**
<<<<<<< HEAD
     * Обрабатывает клики по размещенным блокам кодирования
=======
     * Обрабатывает взаимодействие с блоками
>>>>>>> ba7215a (Я вернулся)
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
<<<<<<< HEAD
        
        // Проверяем, что игрок в мире разработки
        if (!isInDevWorld(player)) {
            return;
        }
        
        // Проверяем права доверенного игрока для взаимодействия с блоками кода
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Location clickedLocation = event.getClickedBlock().getLocation();
            if (blockCodeBlocks.containsKey(clickedLocation) && 
                !plugin.getTrustedPlayerManager().canCodeInDevWorld(player)) {
                event.setCancelled(true);
                player.sendMessage("§c❌ У вас нет прав для взаимодействия с блоками кода в этом мире!");
                return;
            }
        }
        
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        // Инспектор блоков (DEBUG_STICK)
        if (itemInHand.getType() == Material.DEBUG_STICK && itemInHand.hasItemMeta() && event.getAction().isRightClick()) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && blockCodeBlocks.containsKey(clickedBlock.getLocation())) {
                event.setCancelled(true);
                displayBlockInfo(player, blockCodeBlocks.get(clickedBlock.getLocation()));
                return;
            }
        }
        
        // Копировщик блоков (GOLDEN_AXE)
        if (itemInHand.getType() == Material.GOLDEN_AXE && itemInHand.hasItemMeta()) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null) {
                event.setCancelled(true);
                handleBlockCopying(player, event.getAction(), clickedBlock, event.getBlockFace());
                return;
            }
        }
        
        // Предметы-данные (DataItem) - Взаимодействие с уже поставленным блоком
        if (DataItemFactory.isDataItem(itemInHand) && event.getAction().isRightClick()) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && blockCodeBlocks.containsKey(clickedBlock.getLocation())) {
                event.setCancelled(true);
                handleDataItemInsertion(player, clickedBlock, itemInHand);
                return;
            }
        }
        
        // Проверяем, что игрок не использует связующий жезл
        if (itemInHand.getType() == Material.BLAZE_ROD && itemInHand.hasItemMeta() &&
            itemInHand.getItemMeta().getDisplayName().contains(CodingItems.LINKER_TOOL_NAME)) {
            return; // Пропускаем, если используется связующий жезл
        }
        
=======
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
>>>>>>> ba7215a (Я вернулся)
        // Проверяем железный слиток для создания данных
        if (itemInHand.getType() == Material.IRON_INGOT && itemInHand.hasItemMeta() &&
            itemInHand.getItemMeta().getDisplayName().contains(CodingItems.DATA_CREATOR_NAME)) {
            event.setCancelled(true);
            new com.megacreative.gui.DataGUI(plugin, player).open();
<<<<<<< HEAD
            player.sendMessage("§a✅ Открыто меню создания данных!");
=======
>>>>>>> ba7215a (Я вернулся)
            return;
        }
        
        // Остальная логика только для кликов по уже существующим блокам
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Location location = clickedBlock.getLocation();
        
        // Проверяем, есть ли уже блок кода на этой локации
        if (blockCodeBlocks.containsKey(location)) {
<<<<<<< HEAD
            // Предотвращаем открытие GUI, если в руке связующий жезл или другой инструмент
=======
            // Предотвращаем открытие GUI, если в руке инструмент
>>>>>>> ba7215a (Я вернулся)
            if (isTool(itemInHand)) {
                return;
            }
            
            event.setCancelled(true); // Важно, чтобы не открылся, например, верстак
            // ВЫЗЫВАЕМ НАШ НОВЫЙ МЕНЕДЖЕР ВМЕСТО СТАРОГО GUI
            plugin.getBlockConfigManager().openConfigGUI(player, location);
        }
<<<<<<< HEAD

        // --- ПРОБЛЕМНЫЙ КОД БЫЛ УДАЛЕН ОТСЮДА ---
        // Больше нет проверки "if (isCodingBlock(itemInHand))" для установки блока
    }

    /**
     * Обрабатывает использование "Связующего жезла"
     */
    @EventHandler
    public void onLinkerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Проверяем, что игрок в мире разработки и держит наш инструмент
        if (!isInDevWorld(player) || itemInHand.getType() != Material.BLAZE_ROD || !itemInHand.hasItemMeta() || !itemInHand.getItemMeta().getDisplayName().contains(CodingItems.LINKER_TOOL_NAME)) {
            return;
        }
        
        // Предотвращаем другие действия с жезлом (например, открытие печки)
        event.setCancelled(true);
        
        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();

        // --- ЛЕВЫЙ КЛИК: ВЫБОР ПЕРВОГО БЛОКА ---
        if (action == Action.LEFT_CLICK_BLOCK) {
            if (clickedBlock != null && blockCodeBlocks.containsKey(clickedBlock.getLocation())) {
                playerSelections.put(player.getUniqueId(), clickedBlock.getLocation());
                CodeBlock selectedBlock = blockCodeBlocks.get(clickedBlock.getLocation());
                player.sendMessage("§a✅ Начальный блок выбран: §f" + selectedBlock.getAction());
                player.sendMessage("§7Теперь нажмите §eПКМ§7 по конечному блоку для соединения.");
            } else {
                player.sendMessage("§c❌ Это не блок кода! Выберите блок кода.");
            }
        } 
        // --- ПРАВЫЙ КЛИК: ВЫБОР ВТОРОГО БЛОКА И СОЕДИНЕНИЕ ---
        else if (action == Action.RIGHT_CLICK_BLOCK) {
            Location firstBlockLoc = playerSelections.get(player.getUniqueId());

            if (firstBlockLoc == null) {
                player.sendMessage("§c❌ Сначала выберите начальный блок (ЛКМ).");
                return;
            }

            if (clickedBlock != null && blockCodeBlocks.containsKey(clickedBlock.getLocation())) {
                Location secondBlockLoc = clickedBlock.getLocation();

                if (firstBlockLoc.equals(secondBlockLoc)) {
                    player.sendMessage("§c❌ Нельзя соединить блок с самим собой.");
                    return;
                }

                CodeBlock firstBlock = blockCodeBlocks.get(firstBlockLoc);
                CodeBlock secondBlock = blockCodeBlocks.get(secondBlockLoc);

                if (firstBlock != null && secondBlock != null) {
                    firstBlock.setNext(secondBlock);
                    player.sendMessage("§a✅ Связь установлена!");
                    player.sendMessage("§7§f" + firstBlock.getAction() + " §7→ §f" + secondBlock.getAction());
                    playerSelections.remove(player.getUniqueId());
                    
                            // Обновляем визуализацию
        var creativeWorld = plugin.getWorldManager().getWorldByName(player.getWorld().getName());
        if(creativeWorld != null) {
             plugin.getBlockConnectionVisualizer().addBlock(creativeWorld, firstBlockLoc, firstBlock);
        }
                }
            } else {
                player.sendMessage("§c❌ Это не блок кода! Выберите блок кода для соединения.");
            }
        }
=======
>>>>>>> ba7215a (Я вернулся)
    }

    /**
     * Общий метод для обработки конфигурации блока
     */
    private void handleBlockConfiguration(Player player, Material material, Location location, boolean isUpdate) {
        List<String> actions = plugin.getBlockConfiguration().getActionsForMaterial(material);
        
        // Открываем GUI для выбора действия
        new CodingActionGUI(player, material, location, actions, action -> {
            // После выбора действия открываем GUI для параметров
            new CodingParameterGUI(player, action, location, parameters -> {
                // Создаем CodeBlock с параметрами
                CodeBlock codeBlock = createCodeBlockWithParameters(material, action, parameters);
                blockCodeBlocks.put(location, codeBlock);
                
<<<<<<< HEAD
                // Добавляем/обновляем блок в визуализации
                var world = plugin.getWorldManager().getWorldByName(player.getWorld().getName());
                if (world != null) {
                    plugin.getBlockConnectionVisualizer().addBlock(world, location, codeBlock);
                }
                
                setSignOnBlock(location, action);
                
                String message = isUpdate ? "§aДействие обновлено: §e" : "§aДействие установлено: §e";
                player.sendMessage(message + action);
                player.sendMessage("§7Параметры: §f" + parameters.toString());
=======
                setSignOnBlock(location, action);
>>>>>>> ba7215a (Я вернулся)
            }).open();
        }).open();
    }

    /**
     * Проверяет, находится ли игрок в мире разработки
     */
    private boolean isInDevWorld(Player player) {
        String worldName = player.getWorld().getName();
        // Проверяем разные варианты названий миров разработки
<<<<<<< HEAD
        return worldName.startsWith("megacreative_") && worldName.endsWith("_dev") ||
               worldName.endsWith("_dev") ||
               worldName.contains("_dev");
    }

    /**
     * Преобразует Location в строку
     */
    private String locationToString(Location location) {
        return String.format("(%d, %d, %d)", 
            location.getBlockX(), 
            location.getBlockY(), 
            location.getBlockZ());
    }

    /**
     * Возвращает карту соединений блоков (для отладки)
     */
    public Map<Location, CodeBlock> getBlockCodeBlocks() {
        return new HashMap<>(blockCodeBlocks);
    }

    // Установить табличку с действием на блок (НОВАЯ ВЕРСИЯ)
    private void setSignOnBlock(Location loc, String action) {
        placeWallSign(loc, new String[]{"§e[КОД]", "§f" + action, "§7ПКМ для настройки", ""});
    }
    
    // Обновить табличку с параметрами (НОВАЯ ВЕРСИЯ)
    private void updateSignOnBlock(Location loc, CodeBlock codeBlock) {
        String[] lines = new String[4];
        lines[0] = "§e[КОД]";
        lines[1] = "§f" + codeBlock.getAction();
        
        if (!codeBlock.getParameters().isEmpty()) {
            Map.Entry<String, Object> firstParam = codeBlock.getParameters().entrySet().iterator().next();
            lines[2] = "§7" + firstParam.getKey() + ": ";
            lines[3] = "§e" + firstParam.getValue().toString();
        } else {
            lines[2] = "§7ПКМ для настройки";
            lines[3] = "";
        }
        
        placeWallSign(loc, lines);
    }

    // Метод для создания CodeBlock с параметрами
    private CodeBlock createCodeBlockWithParameters(Material material, String action, Map<String, Object> parameters) {
        CodeBlock block = new CodeBlock(material, action);
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                block.setParameter(entry.getKey(), entry.getValue());
            }
        }
        return block;
    }

    private void openBlockConfigurationMenu(Player player, CodeBlock codeBlock, Location location) {
        // Проверяем, держит ли игрок предмет-данные
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (DataItemFactory.isDataItem(itemInHand)) {
            // Игрок хочет вставить данные в блок
            var dataItem = DataItemFactory.fromItemStack(itemInHand);
            if (dataItem.isPresent()) {
                new ParameterSelectorGUI(player, codeBlock, dataItem.get(), parameterAndData -> {
                    // Парсим результат: "parameterName:data:TYPE:value"
                    String[] parts = parameterAndData.split(":", 2);
                    if (parts.length != 2) return;
                    
                    String parameterName = parts[0];
                    String dataPointer = parts[1];
                    
                    // Устанавливаем параметр в блок
                    codeBlock.setParameter(parameterName, dataPointer);
                    
                    // Обновляем табличку на блоке
                    setSignOnBlock(location, codeBlock.getAction());
                    
                    player.sendMessage("§a✓ Данные вставлены в блок '" + codeBlock.getAction() + "'");
                }).open();
                return;
            }
        }
        
        // Обычная настройка блока
        new CodingParameterGUI(player, codeBlock.getAction(), location, parameters -> {
            // Обновляем параметры блока
            codeBlock.getParameters().clear();
            codeBlock.getParameters().putAll(parameters);
            
            // Обновляем табличку на блоке
            updateSignOnBlock(location, codeBlock);
            
            player.sendMessage("§a✅ Параметры блока обновлены!");
            player.sendMessage("§7Действие: §f" + codeBlock.getAction());
            if (!parameters.isEmpty()) {
                player.sendMessage("§7Параметры:");
                parameters.forEach((key, value) -> 
                    player.sendMessage("§7  - §f" + key + ": §e" + value));
            }
        }).open();
    }

    /**
     * Проверяет, является ли предмет блоком кода
     */
    private boolean isCodingBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        String displayName = item.getItemMeta().getDisplayName();
        
        // Новая, чистая проверка
        return CodingItems.isDisplayNameACodingItem(displayName);
    }
    
    /**
     * Размещает блок кода в мире
     */
    private void placeCodeBlock(Player player, ItemStack item, Location location) {
        // Определяем материал блока по предмету
        Material material = item.getType();
        List<String> actions = plugin.getBlockConfiguration().getActionsForMaterial(material);
        if (actions != null && !actions.isEmpty()) {
            // Открываем GUI для выбора действия
            handleBlockConfiguration(player, material, location, false);
        } else {
            player.sendMessage("§cНеизвестный тип блока кода!");
        }
    }

    /**
     * Отображает информацию о блоке кода
     */
    private void displayBlockInfo(Player player, CodeBlock block) {
        player.sendMessage("§e===== [Инспектор Блоков] =====");
        player.sendMessage("§7Действие: §f" + block.getAction());
        
        if (!block.getParameters().isEmpty()) {
            player.sendMessage("§7Параметры:");
            block.getParameters().forEach((key, value) -> {
                player.sendMessage("  §7- §f" + key + ": §e" + value.toString());
            });
        } else {
            player.sendMessage("§7Параметры: §cНет");
        }
        
        if (block.getNextBlock() != null) {
            player.sendMessage("§7Следующий блок: §a" + block.getNextBlock().getAction());
        } else {
            player.sendMessage("§7Следующий блок: §cНет");
        }
        
        if (!block.getChildren().isEmpty()) {
            player.sendMessage("§7Дочерние блоки: §b" + block.getChildren().size() + " шт.");
        }
        
        player.sendMessage("§7ПКМ по блоку для настройки");
        player.sendMessage("§e=========================");
    }
    
    /**
     * Обрабатывает копирование и вставку блоков
     */
    private void handleBlockCopying(Player player, Action action, Block clickedBlock, BlockFace blockFace) {
        // ЛКМ по блоку кода - Копировать
        if (action == Action.LEFT_CLICK_BLOCK && blockCodeBlocks.containsKey(clickedBlock.getLocation())) {
            try {
                CodeBlock original = blockCodeBlocks.get(clickedBlock.getLocation());
                clipboard.put(player.getUniqueId(), (CodeBlock) original.clone());
                player.sendMessage("§a✓ Блок скопирован в буфер обмена.");
            } catch (CloneNotSupportedException e) {
                player.sendMessage("§cЭтот блок не может быть скопирован.");
            }
        } 
        // ПКМ по блоку - Вставить
        else if (action == Action.RIGHT_CLICK_BLOCK) {
            CodeBlock copied = clipboard.get(player.getUniqueId());
            if (copied == null) {
                player.sendMessage("§cВаш буфер обмена пуст.");
                return;
            }
            
            Location placeLocation = clickedBlock.getRelative(blockFace).getLocation();
            placeLocation.getBlock().setType(copied.getMaterial());
            try {
                blockCodeBlocks.put(placeLocation, (CodeBlock) copied.clone());
            } catch (CloneNotSupportedException e) {
                player.sendMessage("§cОшибка при вставке блока.");
                return;
            }
            setSignOnBlock(placeLocation, copied.getAction());
            player.sendMessage("§a✓ Блок вставлен из буфера обмена.");
        }
    }

    /**
     * Обрабатывает вставку данных из DataItem в блок кода
     */
    private void handleDataItemInsertion(Player player, Block clickedBlock, ItemStack dataItemStack) {
        CodeBlock targetBlock = blockCodeBlocks.get(clickedBlock.getLocation());
        if (targetBlock == null) return;
        
        // Получаем DataItem из предмета
        var dataItemOpt = DataItemFactory.fromItemStack(dataItemStack);
        if (dataItemOpt.isEmpty()) {
            player.sendMessage("§cОшибка: не удалось прочитать данные из предмета");
            return;
        }
        
        DataItem dataItem = dataItemOpt.get();
        
        // Открываем GUI для выбора параметра
        new ParameterSelectorGUI(player, targetBlock, dataItem, parameterAndData -> {
            // Парсим результат: "parameterName:data:TYPE:value"
            String[] parts = parameterAndData.split(":", 2);
            if (parts.length != 2) return;
            
            String parameterName = parts[0];
            String dataPointer = parts[1];
            
            // Устанавливаем параметр в блок
            targetBlock.setParameter(parameterName, dataPointer);
            
            // Обновляем табличку на блоке
            setSignOnBlock(clickedBlock.getLocation(), targetBlock.getAction());
            
            player.sendMessage("§a✓ Данные вставлены в блок '" + targetBlock.getAction() + "'");
        }).open();
    }
    
    // Новый вспомогательный метод для установки настенной таблички
    private void placeWallSign(Location blockLocation, String[] lines) {
        // Список сторон, которые мы будем проверять в поисках свободного места
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        for (BlockFace face : faces) {
            Block signBlock = blockLocation.getBlock().getRelative(face);

            // Если на этом месте уже есть наша табличка или там пусто, ставим/обновляем
            if (signBlock.getType() == Material.AIR || signBlock.getType() == Material.OAK_WALL_SIGN) {
                signBlock.setType(Material.OAK_WALL_SIGN);
                if (signBlock.getState() instanceof Sign sign) {
                    // Устанавливаем, к какой стене она "прикреплена"
                    org.bukkit.block.data.type.WallSign signData = (org.bukkit.block.data.type.WallSign) sign.getBlockData();
                    signData.setFacing(face);
                    sign.setBlockData(signData);
                    
                    // Устанавливаем текст
                    for (int i = 0; i < lines.length; i++) {
                        sign.setLine(i, lines[i]);
                    }
                    
                    sign.update();
                    return; // Нашли место, поставили табличку, выходим из метода
                }
            }
        }

        // Если не нашли свободного места сбоку, ставим сверху, как и раньше
        plugin.getLogger().warning("Не удалось найти место для настенной таблички, ставлю сверху.");
        try {
            Block above = blockLocation.clone().add(0, 1, 0).getBlock();
            above.setType(Material.OAK_SIGN);
            
            if (above.getState() instanceof Sign sign) {
                for (int i = 0; i < lines.length; i++) {
                    sign.setLine(i, lines[i]);
                }
                sign.update();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Не удалось поставить даже верхнюю табличку: " + e.getMessage());
        }
    }

    // Метод для проверки, является ли предмет инструментом
    private boolean isTool(ItemStack item) {
        return item.getType() == Material.BLAZE_ROD || item.getType() == Material.GOLDEN_AXE || DataItemFactory.isDataItem(item);
=======
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative");
    }

    /**
     * Проверяет, является ли предмет блоком кодирования
     */
    private boolean isCodingBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        
        String displayName = meta.getDisplayName();
        return CodingItems.isDisplayNameACodingItem(displayName);
    }

    /**
     * Проверяет, является ли предмет инструментом
     */
    private boolean isTool(ItemStack item) {
        if (item == null) return false;
        
        Material type = item.getType();
        return type == Material.WOODEN_AXE || type == Material.STONE_AXE || 
               type == Material.IRON_AXE || type == Material.DIAMOND_AXE || 
               type == Material.NETHERITE_AXE || type == Material.WOODEN_PICKAXE || 
               type == Material.STONE_PICKAXE || type == Material.IRON_PICKAXE || 
               type == Material.DIAMOND_PICKAXE || type == Material.NETHERITE_PICKAXE ||
               type == Material.WOODEN_SHOVEL || type == Material.STONE_SHOVEL || 
               type == Material.IRON_SHOVEL || type == Material.DIAMOND_SHOVEL || 
               type == Material.NETHERITE_SHOVEL || type == Material.WOODEN_HOE || 
               type == Material.STONE_HOE || type == Material.IRON_HOE || 
               type == Material.DIAMOND_HOE || type == Material.NETHERITE_HOE;
    }

    /**
     * Создает CodeBlock с параметрами
     */
    private CodeBlock createCodeBlockWithParameters(Material material, String action, Map<String, Object> parameters) {
        CodeBlock codeBlock = new CodeBlock(material, action);
        codeBlock.setParameters(parameters);
        return codeBlock;
    }

    /**
     * Устанавливает табличку на блок
     */
    private void setSignOnBlock(Location location, String text) {
        // Удаляем существующую табличку
        removeSignFromBlock(location);
        
        // Создаем новую табличку
        Block block = location.getBlock();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Location signLocation = location.clone().add(face.getDirection());
            Block signBlock = signLocation.getBlock();
            
            if (signBlock.getType().isAir()) {
                signBlock.setType(Material.OAK_WALL_SIGN);
                
                if (signBlock.getState() instanceof WallSign) {
                    org.bukkit.block.data.type.WallSign wallSignData = (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();
                    wallSignData.setFacing(face.getOppositeFace());
                    signBlock.setBlockData(wallSignData);
                    
                    Sign sign = (Sign) signBlock.getState();
                    sign.setLine(0, text);
                    sign.update();
                    break;
                }
            }
        }
    }

    /**
     * Удаляет табличку с блока
     */
    private void removeSignFromBlock(Location location) {
        Block block = location.getBlock();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Location signLocation = location.clone().add(face.getDirection());
            Block signBlock = signLocation.getBlock();
            
            if (signBlock.getType().name().contains("SIGN")) {
                signBlock.setType(Material.AIR);
            }
        }
    }

    /**
     * Получает CodeBlock по локации
     */
    public CodeBlock getCodeBlock(Location location) {
        return blockCodeBlocks.get(location);
    }

    /**
     * Проверяет, есть ли CodeBlock по локации
     */
    public boolean hasCodeBlock(Location location) {
        return blockCodeBlocks.containsKey(location);
    }

    /**
     * Получает все CodeBlock'и
     */
    public Map<Location, CodeBlock> getAllCodeBlocks() {
        return new HashMap<>(blockCodeBlocks);
>>>>>>> ba7215a (Я вернулся)
    }
}
