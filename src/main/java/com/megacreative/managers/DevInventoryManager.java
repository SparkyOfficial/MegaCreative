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
        // Не запускаем автоматическую проверку - будем использовать команды
        // startInventoryChecker(); // Отключено по запросу для уменьшения спама
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
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null || !creativeWorld.canCode(player)) {
            player.sendMessage("§cУ вас нет прав на кодирование в этом мире!");
            return;
        }
        
        // ВАЖНО: Сначала сохраняем, ПОТОМ очищаем!
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

        // Проходим по ВСЕМ блокам, определенным в coding_blocks.yml
        for (BlockConfigService.BlockConfig config : configService.getAllBlockConfigs()) {
            if (currentSlot >= 36) break;

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
            player.getInventory().setItem(currentSlot++, item);
        }

        player.updateInventory();
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
        }.runTaskTimer(plugin, 200L, 600L); // Проверяем каждые 30 секунд (вместо 5), меньше спама
    }
    
    private void checkAndRestoreTools(Player player) {
        if (!playersInDevWorld.contains(player.getUniqueId())) return;
        
        // Вместо полного сброса, будем просто добавлять недостающие
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
        
        // Проверяем ключевые инструменты (не все блоки кода!)
        boolean hasCopier = false;
        boolean hasArrowNot = false;
        boolean hasDataCreator = false;
        boolean hasCodeMover = false;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String name = item.getItemMeta().getDisplayName();
                if (name.contains("Копировщик блоков") || name.contains(CodingItems.COPIER_TOOL_NAME)) {
                    hasCopier = true;
                } else if (name.contains("Стрела НЕ") || name.contains(CodingItems.ARROW_NOT_NAME)) {
                    hasArrowNot = true;
                } else if (name.contains("Создатель данных") || name.contains(CodingItems.DATA_CREATOR_NAME)) {
                    hasDataCreator = true;
                } else if (name.contains("Перемещатель кода") || name.contains(CodingItems.CODE_MOVER_NAME)) {
                    hasCodeMover = true;
                }
            }
        }
        
        // Добавляем недостающие инструменты
        if (!hasCopier) missingItems.add("copier");
        if (!hasArrowNot) missingItems.add("arrow_not");
        if (!hasDataCreator) missingItems.add("data_creator");
        if (!hasCodeMover) missingItems.add("code_mover");
        
        return missingItems;
    }
    
    /**
     * Выдает только недостающие предметы
     */
    private void giveMissingItems(Player player, List<String> missingItems) {
        for (String item : missingItems) {
            switch (item) {
                case "copier" -> {
                    ItemStack copier = new ItemStack(Material.GOLDEN_AXE);
                    ItemMeta copierMeta = copier.getItemMeta();
                    copierMeta.setDisplayName("§6📋 Копировщик блоков");
                    copierMeta.setLore(Arrays.asList(
                        "§7ЛКМ по блоку - скопировать",
                        "§7ПКМ по блоку - вставить"
                    ));
                    copier.setItemMeta(copierMeta);
                    player.getInventory().addItem(copier);
                }
                case "arrow_not" -> player.getInventory().addItem(CodingItems.getArrowNot());
                case "data_creator" -> player.getInventory().addItem(CodingItems.getDataCreator());
                case "code_mover" -> player.getInventory().addItem(CodingItems.getCodeMover());
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
        return worldName.endsWith("_dev");
    }
    
    /**
     * Публичный метод для сохранения инвентаря игрока перед входом в мир разработки
     */
    public void savePlayerInventory(Player player) {
        if (playersInDevWorld.contains(player.getUniqueId())) {
            savedInventories.put(player.getUniqueId(), player.getInventory().getContents());
            plugin.getLogger().info("Saved dev inventory for " + player.getName());
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
        if (playersInDevWorld.contains(player.getUniqueId())) {
            giveDevTools(player);
            player.sendMessage("§aИнструменты принудительно восстановлены!");
        }
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
}