package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.data.DataItemFactory;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlockPlacementHandler implements Listener {

    private final MegaCreative plugin;

    // Временные карты, не для хранения!
    private final Map<UUID, Location> playerSelections = new HashMap<>();
    private final Map<UUID, CodeBlock> clipboard = new HashMap<>();

    public BlockPlacementHandler(MegaCreative plugin) {
        this.plugin = plugin;
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ДАННЫМИ МИРА ---

    private CreativeWorld getCurrentCreativeWorld(Player player) {
        return plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
    }

    private String locationToString(Location location) {
        // Убираем дробную часть для консистентности
        return String.format("%s,%d,%d,%d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    private Location stringToLocation(String locString) {
        try {
            String[] parts = locString.split(",");
            if (parts.length != 4) return null;
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            return new Location(world, x, y, z);
        } catch (Exception e) {
            return null;
        }
    }
    
    public CodeBlock getBlockAt(Player player, Location location) {
        CreativeWorld world = getCurrentCreativeWorld(player);
        if (world == null || world.getDevWorldBlocks() == null) return null;
        return world.getDevWorldBlocks().get(locationToString(location));
    }
    
    public Map<Location, CodeBlock> getBlockCodeBlocks() {
        Map<Location, CodeBlock> allBlocks = new HashMap<>();
        // Собираем блоки из всех миров, которые загружены в плагине
        for (CreativeWorld world : plugin.getWorldManager().getAllPublicWorlds()) {
            if (world.getDevWorldBlocks() != null) {
                world.getDevWorldBlocks().forEach((locString, codeBlock) -> {
                    Location loc = stringToLocation(locString);
                    if (loc != null) {
                        allBlocks.put(loc, codeBlock);
                    }
                });
            }
        }
        return allBlocks;
    }

    // --- ОБНОВЛЕННЫЕ ОБРАБОТЧИКИ СОБЫТИЙ ---

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player) || !isCodingBlock(event.getItemInHand())) {
            return;
        }

        CreativeWorld creativeWorld = getCurrentCreativeWorld(player);
        if (creativeWorld == null) {
            event.setCancelled(true);
            player.sendMessage("§cОшибка: не удалось найти данные мира.");
            return;
        }

        if (!creativeWorld.canCode(player)) {
            event.setCancelled(true);
            player.sendMessage("§c❌ У вас нет прав для размещения блоков кода в этом мире!");
            return;
        }

        Block block = event.getBlockPlaced();
        Location location = block.getLocation();
        Material mat = block.getType();

        // Сразу создаем "заготовку" блока
        CodeBlock newCodeBlock = new CodeBlock(mat, "Настройка...");
        creativeWorld.addDevWorldBlock(locationToString(location), newCodeBlock);
        
        // Визуализируем и ставим табличку
        setSignOnBlock(location, "Настройка...");
        if (plugin.getBlockConnectionVisualizer() != null) {
            plugin.getBlockConnectionVisualizer().addBlock(creativeWorld, location, newCodeBlock);
        }

        // Открываем GUI через 1 тик, чтобы игрок увидел поставленный блок
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            List<String> actions = plugin.getBlockConfiguration().getActionsForMaterial(mat);
            if (actions == null || actions.isEmpty()) {
                player.sendMessage("§cДля этого блока нет доступных действий.");
                creativeWorld.removeDevWorldBlock(locationToString(location));
                block.setType(Material.AIR);
                removeSignFromBlock(location);
                return;
            }

            // Открываем GUI выбора действия
            new CodingActionGUI(player, mat, location, actions, action -> {
                // После выбора действия открываем GUI параметров
                new CodingParameterGUI(player, action, location, parameters -> {
                    // **КЛЮЧЕВОЙ МОМЕНТ:** Получаем блок из мира заново
                    CreativeWorld currentWorld = getCurrentCreativeWorld(player);
                    CodeBlock blockToUpdate = currentWorld.getDevWorldBlocks().get(locationToString(location));
                    
                    if (blockToUpdate != null) {
                        // Обновляем его
                        blockToUpdate.setAction(action);
                        blockToUpdate.setParameters(new HashMap<>(parameters));
                        // Сохраняем мир с обновленным блоком
                        plugin.getWorldManager().saveWorld(currentWorld);
                        
                        // Обновляем табличку
                        updateSignOnBlock(location, blockToUpdate);
                        player.sendMessage("§aДействие установлено: §e" + action);
                    } else {
                        player.sendMessage("§cПроизошла ошибка при настройке блока (блок не найден).");
                    }
                }).open();
            }).open();
        }, 1L);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;

        CreativeWorld creativeWorld = getCurrentCreativeWorld(player);
        if (creativeWorld == null) return;

        if (!creativeWorld.canCode(player)) {
            event.setCancelled(true);
            player.sendMessage("§c❌ У вас нет прав для удаления блоков кода в этом мире!");
            return;
        }

        Location loc = event.getBlock().getLocation();
        CodeBlock brokenBlock = getBlockAt(player, loc);
        
        if (brokenBlock != null) {
            creativeWorld.removeDevWorldBlock(locationToString(loc));
            plugin.getWorldManager().saveWorld(creativeWorld);
            player.sendMessage("§c❌ Блок кода удален: " + brokenBlock.getAction());
            removeSignFromBlock(loc);
            if (plugin.getBlockConnectionVisualizer() != null) {
                plugin.getBlockConnectionVisualizer().removeBlock(creativeWorld, loc);
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;

        Block clickedBlock = event.getClickedBlock();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || clickedBlock == null) return;

        Location location = clickedBlock.getLocation();
        CodeBlock codeBlock = getBlockAt(player, location);

        if (codeBlock != null) {
             if (!getCurrentCreativeWorld(player).canCode(player)) {
                event.setCancelled(true);
                player.sendMessage("§c❌ У вас нет прав для взаимодействия с блоками кода в этом мире!");
                return;
            }
            
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (isTool(itemInHand)) {
                // Тут можно добавить логику для инспектора, жезла и т.д.
                return;
            }
            
            event.setCancelled(true);
            plugin.getBlockConfigManager().openConfigGUI(player, location);
        }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---
    
    private boolean isCodingBlock(ItemStack item) { 
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false; 
        return CodingItems.isDisplayNameACodingItem(item.getItemMeta().getDisplayName()); 
    }
    
    private boolean isTool(ItemStack item) { 
        if (item == null) return false; 
        return item.getType() == Material.BLAZE_ROD || item.getType() == Material.DEBUG_STICK || 
               item.getType() == Material.GOLDEN_AXE || DataItemFactory.isDataItem(item); 
    }
    
    private boolean isInDevWorld(Player player) { 
        return player.getWorld().getName().endsWith("_dev"); 
    }
    
    private void setSignOnBlock(Location loc, String action) { 
        placeWallSign(loc, new String[]{"§e[КОД]", "§f" + action, "§7ПКМ", ""}); 
    }
    
    private void updateSignOnBlock(Location loc, CodeBlock codeBlock) { 
        String[] lines = new String[4]; 
        lines[0] = "§e[КОД]"; 
        lines[1] = "§f" + codeBlock.getAction(); 
        if (!codeBlock.getParameters().isEmpty()) { 
            Map.Entry<String, Object> firstParam = codeBlock.getParameters().entrySet().iterator().next(); 
            lines[2] = "§7" + firstParam.getKey() + ": "; 
            lines[3] = "§e" + String.valueOf(firstParam.getValue()); 
        } else { 
            lines[2] = "§7ПКМ"; 
            lines[3] = ""; 
        } 
        placeWallSign(loc, lines); 
    }
    
    private void placeWallSign(Location loc, String[] lines) { 
        Bukkit.getScheduler().runTask(plugin, () -> { 
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}; 
            for (BlockFace face : faces) { 
                Block signBlock = loc.getBlock().getRelative(face); 
                if (signBlock.getType().isAir() || signBlock.getState() instanceof Sign) { 
                    signBlock.setType(Material.OAK_WALL_SIGN); 
                    if (signBlock.getState() instanceof Sign sign) { 
                        org.bukkit.block.data.type.WallSign signData = (org.bukkit.block.data.type.WallSign) sign.getBlockData(); 
                        signData.setFacing(face); 
                        sign.setBlockData(signData); 
                        for (int i = 0; i < lines.length; i++) { 
                            sign.setLine(i, lines[i]); 
                        } 
                        sign.update(true); 
                        return; 
                    } 
                } 
            } 
        }); 
    }
    
    private void removeSignFromBlock(Location loc) { 
        Bukkit.getScheduler().runTask(plugin, () -> { 
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}; 
            for (BlockFace face : faces) { 
                Block signBlock = loc.getBlock().getRelative(face); 
                if (signBlock.getState() instanceof Sign) { 
                    signBlock.setType(Material.AIR); 
                } 
            } 
        }); 
    }
}
