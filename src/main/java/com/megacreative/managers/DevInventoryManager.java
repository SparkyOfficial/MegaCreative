package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Менеджер инвентаря разработчика
 * Отслеживает и восстанавливает инструменты разработчика при входе в dev-мир
 */
public class DevInventoryManager implements Listener {
    
    private final MegaCreative plugin;
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Set<UUID> playersInDevWorld = new HashSet<>();
    
    public DevInventoryManager(MegaCreative plugin) {
        this.plugin = plugin;
        
        
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        if (isDevWorld(player.getWorld().getName())) {
            handleDevWorldEntry(player);
        } else if (isDevWorld(event.getFrom().getName())) {
            handleDevWorldExit(player);
        }
    }
    
    private void handleDevWorldEntry(Player player) {
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null || !creativeWorld.canCode(player)) {
            player.sendMessage("§cУ вас нет прав на кодирование в этом мире!");
            return;
        }
        
        
        savedInventories.put(player.getUniqueId(), player.getInventory().getContents().clone());
        playersInDevWorld.add(player.getUniqueId());
        giveDevTools(player);
        player.sendMessage("§aВы вошли в режим разработки! Инструменты восстанавливаются автоматически.");
    }
    
    private void handleDevWorldExit(Player player) {
        UUID playerId = player.getUniqueId();
        playersInDevWorld.remove(playerId);
        
        ItemStack[] savedInventory = savedInventories.remove(playerId);
        if (savedInventory != null) {
            player.getInventory().setContents(savedInventory);
            player.sendMessage("§aВаш инвентарь восстановлен!");
        }
    }
    
    private void giveDevTools(Player player) {
        player.getInventory().clear();
        int currentSlot = 0;

        BlockConfigService configService = plugin.getServiceRegistry().getBlockConfigService();
        if (configService == null) {
            player.sendMessage("§cОшибка: сервис конфигурации блоков не загружен!");
            return;
        }

        // Extracted method for creating block items
        currentSlot = createAndAddBlockItems(player, configService, currentSlot);

        // Continue with the rest of the method
        if (currentSlot < 36) {
            player.getInventory().setItem(currentSlot, CodingItems.getGameValue());
            currentSlot++;
        }
        if (currentSlot < 36) {
            player.getInventory().setItem(currentSlot, CodingItems.getArrowNot());
            currentSlot++;
        }
        if (currentSlot < 36) {
            player.getInventory().setItem(currentSlot, CodingItems.getDataCreator());
            currentSlot++;
        }
        if (currentSlot < 36) {
            player.getInventory().setItem(currentSlot, CodingItems.getCodeMover());
        }

        player.updateInventory();
    }
    
    /**
     * Creates and adds block items to player inventory
     * @param player the player
     * @param configService the block config service
     * @param currentSlot the current inventory slot
     * @return the updated slot index
     */
    private int createAndAddBlockItems(Player player, BlockConfigService configService, int currentSlot) {
        for (BlockConfigService.BlockConfig config : configService.getAllBlockConfigs()) {
            if (currentSlot >= 36) break;

            ItemStack item = createBlockItem(config);
            player.getInventory().setItem(currentSlot, item);
            currentSlot++;
        }
        return currentSlot;
    }
    
    /**
     * Creates a block item from config
     * @param config the block config
     * @return the created ItemStack
     */
    private ItemStack createBlockItem(BlockConfigService.BlockConfig config) {
        ItemStack item = new ItemStack(config.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§r" + config.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + config.getDescription());
            lore.add("§8Тип: " + config.getType());
            lore.add("§8ID: " + config.getId());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private void startInventoryChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID playerId : new HashSet<>(playersInDevWorld)) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        checkAndRestoreTools(player);
                    } else {
                        playersInDevWorld.remove(playerId);
                        savedInventories.remove(playerId);
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 600L); 
    }
    
    private void checkAndRestoreTools(Player player) {
        if (!playersInDevWorld.contains(player.getUniqueId())) return;
        
        // Проверяет, каких именно предметов не хватает
        List<String> missingItems = getMissingCodingItems(player);
        if (!missingItems.isEmpty()) {
            giveMissingItems(player, missingItems);
            player.sendMessage("§aНекоторые инструменты разработчика были восстановлены!");
        }
    }
    
    /**
     * Проверяет, каких именно предметов не хватает
     */
    private List<String> getMissingCodingItems(Player player) {
        List<String> missingItems = new ArrayList<>();
    
        // Remove redundant initialization
        boolean hasCopier = false;
        boolean hasArrowNot = false;
        boolean hasDataCreator = false;
        boolean hasCodeMover = false;
        boolean hasGameValue = false; 
    
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String name = item.getItemMeta().getDisplayName();
                if (name.contains("Стрела НЕ") || name.contains(CodingItems.ARROW_NOT_NAME)) {
                    hasArrowNot = true;
                } else if (name.contains("Создатель данных") || name.contains(CodingItems.DATA_CREATOR_NAME)) {
                    hasDataCreator = true;
                } else if (name.contains("Перемещатель кода") || name.contains(CodingItems.CODE_MOVER_NAME)) {
                    hasCodeMover = true;
                } else if (name.contains("Игровое значение") || name.contains(CodingItems.GAME_VALUE_NAME)) { 
                    hasGameValue = true;
                }
            }
        }
    
        // Add missing items to list
        if (!hasArrowNot) missingItems.add("arrow_not");
        if (!hasDataCreator) missingItems.add("data_creator");
        if (!hasCodeMover) missingItems.add("code_mover");
        if (!hasGameValue) missingItems.add("game_value"); 
    
        return missingItems;
    }

    /**
     * Выдает только недостающие предметы
     */
    private void giveMissingItems(Player player, List<String> missingItems) {
        for (String item : missingItems) {
            switch (item) {
                case "arrow_not" -> player.getInventory().addItem(CodingItems.getArrowNot());
                case "data_creator" -> player.getInventory().addItem(CodingItems.getDataCreator());
                case "code_mover" -> player.getInventory().addItem(CodingItems.getCodeMover());
                case "game_value" -> player.getInventory().addItem(CodingItems.getGameValue()); 
                default -> {
                    // Handle unknown items
                }
            }
        }
    }
    
    private static ItemStack createDevItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private boolean isDevWorld(String worldName) {
        // Проверяет, является ли мир dev-миром
        return worldName.endsWith("-code") || worldName.endsWith("_dev");
    }
    
    /**
     * Публичный метод для сохранения инвентаря игрока перед входом в мир разработки
     */
    public void savePlayerInventory(Player player) {
        if (playersInDevWorld.contains(player.getUniqueId())) {
            savedInventories.put(player.getUniqueId(), player.getInventory().getContents());
            // Сохраняет инвентарь игрока
        }
    }
    
    /**
     * Публичный метод для восстановления инвентаря игрока при выходе из мира разработки
     */
    public void restorePlayerInventory(Player player) {
        UUID playerId = player.getUniqueId();
        playersInDevWorld.remove(playerId);
        
        ItemStack[] savedInventory = savedInventories.remove(playerId);
        if (savedInventory != null) {
            player.getInventory().setContents(savedInventory);
            player.sendMessage("§aВаш инвентарь восстановлен!");
        }
    }
    
    public void forceRestoreTools(Player player) {
        // Принудительно восстанавливает инструменты
        giveDevTools(player);
        player.sendMessage("§aИнструменты принудительно восстановлены!");
    }
    
    /**
     * Публичный метод для команд - восстанавливает недостающие инструменты
     */
    public void refreshTools(Player player) {
        if (!playersInDevWorld.contains(player.getUniqueId())) {
            player.sendMessage("§cВы не находитесь в мире разработки!");
            return;
        }
        
        List<String> missingItems = getMissingCodingItems(player);
        if (!missingItems.isEmpty()) {
            giveMissingItems(player, missingItems);
            player.sendMessage("§aНедостающие инструменты разработчика восстановлены!");
        } else {
            player.sendMessage("§eВсе инструменты уже на месте!");
        }
    }
    
    /**
     * Добавь такой метод в DevInventoryManager
     * Проверяет, находится ли игрок в dev мире
     */
    public boolean isPlayerInDevWorld(Player player) {
        return playersInDevWorld.contains(player.getUniqueId());
    }
}