package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.data.DataItemFactory;
import com.megacreative.coding.data.DataItemFactory.DataItem;
import com.megacreative.coding.ParameterSelectorGUI;
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

    // Список действий для каждого типа блока (MVP, можно расширять)
    private static final Map<Material, List<String>> ACTIONS = Map.of(
        Material.DIAMOND_BLOCK, List.of("onJoin", "onLeave", "onChat", "onInteract"),
        Material.OAK_PLANKS, List.of("isOp", "isInWorld", "hasItem", "hasPermission", "isNearBlock", "timeOfDay"),
        Material.COBBLESTONE, List.of("sendMessage", "teleport", "giveItem", "playSound", "effect", "command", "broadcast"),
        Material.IRON_BLOCK, List.of("setVar", "addVar", "subVar", "mulVar", "divVar"),
        Material.END_STONE, List.of("else"),
        Material.NETHERITE_BLOCK, List.of("setTime", "setWeather", "spawnMob"),
        Material.OBSIDIAN, List.of("ifVar", "ifNotVar"),
        Material.REDSTONE_BLOCK, List.of("ifGameMode", "ifWorldType"),
        Material.BRICKS, List.of("ifMobType", "ifMobNear"),
        Material.POLISHED_GRANITE, List.of("getVar", "getPlayerName")
    );

    public BlockPlacementHandler(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Обрабатывает размещение блоков кодирования
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        Material mat = block.getType();
        
        // Проверяем, является ли предмет "блоком кода" по названию, а не просто по материалу.
        if (!isCodingBlock(event.getItemInHand())) return;
        if (!isInDevWorld(player)) return;
        
        // НЕ отменяем событие, позволяем блоку установиться
        
        // Запускаем настройку через 1 тик, чтобы сервер успел обработать установку
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            handleBlockConfiguration(player, mat, block.getLocation(), false);
        }, 1L);
    }

    /**
     * Обрабатывает ломание блоков кодирования
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isInDevWorld(event.getPlayer())) return;
        
        Location loc = event.getBlock().getLocation();
        if (blockCodeBlocks.containsKey(loc)) {
            blockCodeBlocks.remove(loc);
            var world = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (world != null) {
                plugin.getBlockConnectionVisualizer().removeBlock(world, loc);
            }
            event.getPlayer().sendMessage("§cБлок кода удален.");
        }
    }

    /**
     * Обрабатывает клики по размещенным блокам кодирования
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Проверяем, что игрок в мире разработки
        if (!player.getWorld().getName().equals("dev")) {
            return;
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
        
        // Предметы-данные (DataItem)
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
            itemInHand.getItemMeta().getDisplayName().contains("Связующий жезл")) {
            return; // Пропускаем, если используется связующий жезл
        }
        
        // Проверяем железный слиток для создания данных
        if (itemInHand.getType() == Material.IRON_INGOT && itemInHand.hasItemMeta() && 
            itemInHand.getItemMeta().getDisplayName().contains("Создать данные")) {
            event.setCancelled(true);
            new com.megacreative.gui.DataGUI(player).open();
            return;
        }
        
        // Остальная логика для блоков кода
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Location location = clickedBlock.getLocation();
        
        // Проверяем, есть ли уже блок кода на этой локации
        if (blockCodeBlocks.containsKey(location)) {
            CodeBlock existingBlock = blockCodeBlocks.get(location);
            openBlockConfigurationMenu(player, existingBlock, location);
            return;
        }
        
        // Проверяем, держит ли игрок блок кода
        if (isCodingBlock(itemInHand)) {
            event.setCancelled(true);
            placeCodeBlock(player, itemInHand, location);
        }
    }

    /**
     * Обрабатывает использование "Связующего жезла"
     */
    @EventHandler
    public void onLinkerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Проверяем, что игрок в мире разработки и держит наш инструмент
        if (!isInDevWorld(player) || itemInHand.getType() != Material.BLAZE_ROD || !itemInHand.hasItemMeta() || !itemInHand.getItemMeta().getDisplayName().contains("Связующий жезл")) {
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
                player.sendMessage("§a✓ Начальный блок выбран. Нажмите ПКМ по конечному блоку.");
            }
        } 
        // --- ПРАВЫЙ КЛИК: ВЫБОР ВТОРОГО БЛОКА И СОЕДИНЕНИЕ ---
        else if (action == Action.RIGHT_CLICK_BLOCK) {
            Location firstBlockLoc = playerSelections.get(player.getUniqueId());

            if (firstBlockLoc == null) {
                player.sendMessage("§c✗ Сначала выберите начальный блок (ЛКМ).");
                return;
            }

            if (clickedBlock != null && blockCodeBlocks.containsKey(clickedBlock.getLocation())) {
                Location secondBlockLoc = clickedBlock.getLocation();

                if (firstBlockLoc.equals(secondBlockLoc)) {
                    player.sendMessage("§c✗ Нельзя соединить блок с самим собой.");
                    return;
                }

                CodeBlock firstBlock = blockCodeBlocks.get(firstBlockLoc);
                CodeBlock secondBlock = blockCodeBlocks.get(secondBlockLoc);

                if (firstBlock != null && secondBlock != null) {
                    firstBlock.setNext(secondBlock);
                    player.sendMessage("§a✓ Связь установлена!");
                    playerSelections.remove(player.getUniqueId());
                    
                    // Обновляем визуализацию
                    var creativeWorld = plugin.getWorldManager().getWorld(player.getWorld().getName());
                    if(creativeWorld != null) {
                         plugin.getBlockConnectionVisualizer().addBlock(creativeWorld, firstBlockLoc, firstBlock);
                    }
                }
            }
        }
    }

    /**
     * Общий метод для обработки конфигурации блока
     */
    private void handleBlockConfiguration(Player player, Material material, Location location, boolean isUpdate) {
        List<String> actions = ACTIONS.get(material);
        
        // Открываем GUI для выбора действия
        new CodingActionGUI(player, material, location, actions, action -> {
            // После выбора действия открываем GUI для параметров
            new CodingParameterGUI(player, action, location, parameters -> {
                // Создаем CodeBlock с параметрами
                CodeBlock codeBlock = createCodeBlockWithParameters(material, action, parameters);
                blockCodeBlocks.put(location, codeBlock);
                
                // Добавляем/обновляем блок в визуализации
                var world = plugin.getWorldManager().getWorld(player.getWorld().getName());
                if (world != null) {
                    plugin.getBlockConnectionVisualizer().addBlock(world, location, codeBlock);
                }
                
                setSignOnBlock(location, action);
                
                String message = isUpdate ? "§aДействие обновлено: §e" : "§aДействие установлено: §e";
                player.sendMessage(message + action);
                player.sendMessage("§7Параметры: §f" + parameters.toString());
            }).open();
        }).open();
    }

    /**
     * Проверяет, находится ли игрок в мире разработки
     */
    private boolean isInDevWorld(Player player) {
        String worldName = player.getWorld().getName();
        return worldName.startsWith("megacreative_") && worldName.endsWith("_dev");
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

    // Установить табличку с действием на блок
    private void setSignOnBlock(Location loc, String action) {
        Block above = loc.clone().add(0, 1, 0).getBlock();
        above.setType(Material.OAK_SIGN);
        if (above.getState() instanceof Sign sign) {
            sign.setLine(0, "§e[КОД]");
            sign.setLine(1, action);
            sign.update();
        }
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
            
            player.sendMessage("§a✓ Параметры блока обновлены!");
        }).open();
    }

    /**
     * Проверяет, является ли предмет блоком кода
     */
    private boolean isCodingBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        
        String displayName = meta.getDisplayName();
        return displayName.contains("Событие игрока") || 
               displayName.contains("Условие игрока") || 
               displayName.contains("Действие игрока") || 
               displayName.contains("Присвоить переменную") || 
               displayName.contains("Иначе") || 
               displayName.contains("Игровое действие") || 
               displayName.contains("Если переменная") || 
               displayName.contains("Если игра") || 
               displayName.contains("Если существо") || 
               displayName.contains("Получить данные");
    }
    
    /**
     * Размещает блок кода в мире
     */
    private void placeCodeBlock(Player player, ItemStack item, Location location) {
        // Определяем материал блока по предмету
        Material material = item.getType();
        if (!ACTIONS.containsKey(material)) {
            player.sendMessage("§cНеизвестный тип блока кода!");
            return;
        }
        
        // Открываем GUI для выбора действия
        handleBlockConfiguration(player, material, location, false);
    }

    /**
     * Отображает информацию о блоке кода
     */
    private void displayBlockInfo(Player player, CodeBlock block) {
        player.sendMessage("§e===== [Инспектор Блоков] =====");
        player.sendMessage("§7Действие: §f" + block.getAction());
        player.sendMessage("§7Параметры:");
        block.getParameters().forEach((key, value) -> {
            player.sendMessage("  §7- §f" + key + ": §e" + value.toString());
        });
        if (block.getNextBlock() != null) {
            player.sendMessage("§7Следующий блок: §a" + block.getNextBlock().getAction());
        } else {
            player.sendMessage("§7Следующий блок: §cНет");
        }
        if (!block.getChildren().isEmpty()) {
            player.sendMessage("§7Дочерние блоки: §b" + block.getChildren().size() + " шт.");
        }
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
}
