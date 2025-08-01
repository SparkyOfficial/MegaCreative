package com.megacreative.coding;

import com.megacreative.MegaCreative;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;

/**
 * Обработчик размещения и взаимодействия с блоками кодирования в мире разработки.
 */
public class BlockPlacementHandler implements Listener {

    private final MegaCreative plugin;
    
    // Хранилище: Location -> CodeBlock (содержит действие и параметры)
    private final Map<Location, CodeBlock> blockCodeBlocks = new HashMap<>();
    
    // Новое поле для хранения первого выбранного блока
    private final Map<UUID, Location> playerSelections = new HashMap<>();

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
        if (!ACTIONS.containsKey(mat)) return;
        if (!isInDevWorld(player)) return;
        
        handleBlockConfiguration(player, mat, block.getLocation(), false);
    }

    /**
     * Обрабатывает клики по размещенным блокам кодирования
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !isInDevWorld(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        // Проверяем, что игрок не использует связующий жезл
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.BLAZE_ROD && itemInHand.hasItemMeta() && 
            itemInHand.getItemMeta().getDisplayName().contains("Связующий жезл")) {
            return; // Пропускаем, если используется связующий жезл
        }
        
        Material mat = clickedBlock.getType();
        if (!ACTIONS.containsKey(mat)) return;
        event.setCancelled(true);
        
        handleBlockConfiguration(player, mat, clickedBlock.getLocation(), true);
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
}
