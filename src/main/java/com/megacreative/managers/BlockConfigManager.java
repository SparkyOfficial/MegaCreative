package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockConfiguration;
import com.megacreative.coding.CodeBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер для управления виртуальными инвентарями конфигурации блоков.
 * Позволяет игрокам настраивать блоки кода через GUI инвентаря.
 */
public class BlockConfigManager implements Listener {

    private final MegaCreative plugin;
    // Отслеживаем, какой игрок какой блок сейчас настраивает
    private final Map<UUID, Location> configuringBlocks = new HashMap<>();

    public BlockConfigManager(MegaCreative plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Открывает GUI для настройки блока
     * @param player Игрок, который будет настраивать блок
     * @param blockLocation Локация блока для настройки
     */
    public void openConfigGUI(Player player, Location blockLocation) {
        CodeBlock codeBlock = plugin.getBlockPlacementHandler().getBlockCodeBlocks().get(blockLocation);
        if (codeBlock == null) {
            player.sendMessage("§cОшибка: блок кода не найден.");
            return;
        }

        // Устанавливаем ссылку на плагин для доступа к конфигурации
        codeBlock.setPlugin(plugin);

        // Создаем инвентарь (сундук на 27 слотов - 3 ряда)
        Inventory configInventory = Bukkit.createInventory(null, 27, "§8Настройка: " + codeBlock.getAction());

        // Загружаем сохраненные предметы в GUI
        if (codeBlock.getConfigItems() != null) {
            for (Map.Entry<Integer, ItemStack> entry : codeBlock.getConfigItems().entrySet()) {
                if (entry.getKey() < configInventory.getSize()) {
                    configInventory.setItem(entry.getKey(), entry.getValue());
                }
            }
        }
        
        // --- НОВАЯ ЛОГИКА: ДОБАВЛЯЕМ PLACEHOLDER ПРЕДМЕТЫ ---
        addPlaceholderItems(configInventory, codeBlock);
        
        // Запоминаем, что игрок настраивает этот блок
        configuringBlocks.put(player.getUniqueId(), blockLocation);
        player.openInventory(configInventory);
        
        player.sendMessage("§e§l!§r §eНастройте блок, поместив предметы в инвентарь.");
        player.sendMessage("§7Предметы будут использоваться как параметры для действия '" + codeBlock.getAction() + "'");
        player.sendMessage("§7§oПодсказка: Используйте 27 слотов для сложных конфигураций");
    }
    
    /**
     * Добавляет placeholder предметы в инвентарь на основе конфигурации
     */
    private void addPlaceholderItems(Inventory inventory, CodeBlock codeBlock) {
        String actionName = codeBlock.getAction();
        
        // Добавляем placeholder предметы для именованных слотов
        var slotConfig = plugin.getBlockConfiguration().getActionSlotConfig(actionName);
        if (slotConfig != null) {
            for (Map.Entry<Integer, BlockConfiguration.SlotConfig> entry : slotConfig.getSlots().entrySet()) {
                int slotNumber = entry.getKey();
                BlockConfiguration.SlotConfig slotConfigData = entry.getValue();
                
                // Проверяем, есть ли уже предмет в этом слоте
                if (inventory.getItem(slotNumber) == null || inventory.getItem(slotNumber).getType().isAir()) {
                    ItemStack placeholder = plugin.getBlockConfiguration().createPlaceholderItem(actionName, slotNumber);
                    inventory.setItem(slotNumber, placeholder);
                }
            }
        }
        
        // Добавляем placeholder предметы для групп
        var groupConfig = plugin.getBlockConfiguration().getActionGroupConfig(actionName);
        if (groupConfig != null) {
            for (Map.Entry<String, BlockConfiguration.GroupConfig> entry : groupConfig.getGroups().entrySet()) {
                String groupName = entry.getKey();
                BlockConfiguration.GroupConfig groupConfigData = entry.getValue();
                
                // Добавляем placeholder в первый слот группы, если он пустой
                List<Integer> groupSlots = groupConfigData.getSlots();
                if (!groupSlots.isEmpty()) {
                    int firstSlot = groupSlots.get(0);
                    if (inventory.getItem(firstSlot) == null || inventory.getItem(firstSlot).getType().isAir()) {
                        ItemStack placeholder = plugin.getBlockConfiguration().createGroupPlaceholderItem(actionName, groupName);
                        inventory.setItem(firstSlot, placeholder);
                    }
                }
            }
        }
    }

    /**
     * Сохраняет содержимое GUI, когда игрок его закрывает
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        UUID playerId = player.getUniqueId();

        // Проверяем, настраивал ли этот игрок блок
        if (!configuringBlocks.containsKey(playerId)) {
            return;
        }
        
        // Проверяем, что это наше GUI
        if (!event.getView().getTitle().startsWith("§8Настройка:")) {
            // Если игрок открыл что-то другое, не закрыв наше GUI, то просто ждем закрытия нашего.
            return;
        }

        Location blockLocation = configuringBlocks.get(playerId);
        CodeBlock codeBlock = plugin.getBlockPlacementHandler().getBlockCodeBlocks().get(blockLocation);
        
        if (codeBlock != null) {
            // Очищаем старую конфигурацию
            codeBlock.clearConfigItems();
            
            // Считываем все предметы из инвентаря и сохраняем в CodeBlock
            int savedItems = 0;
            for (int i = 0; i < event.getInventory().getSize(); i++) {
                ItemStack item = event.getInventory().getItem(i);
                if (item != null && !item.getType().isAir()) {
                    codeBlock.setConfigItem(i, item);
                    savedItems++;
                }
            }
            
            if (savedItems > 0) {
                player.sendMessage("§a✓ Конфигурация блока сохранена! (" + savedItems + " предметов)");
            } else {
                player.sendMessage("§eℹ Конфигурация блока очищена.");
            }
            
            // Сохраняем весь мир, чтобы изменения не потерялись после перезагрузки
            var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null) {
                plugin.getWorldManager().saveWorld(creativeWorld);
            }
        }
        
        // Убираем игрока из списка настраивающих
        configuringBlocks.remove(playerId);
    }
    
    /**
     * Проверяет, настраивает ли игрок блок в данный момент
     * @param player Игрок для проверки
     * @return true, если игрок настраивает блок
     */
    public boolean isConfiguringBlock(Player player) {
        return configuringBlocks.containsKey(player.getUniqueId());
    }
    
    /**
     * Отменяет настройку блока для игрока
     * @param player Игрок, для которого нужно отменить настройку
     */
    public void cancelConfiguration(Player player) {
        configuringBlocks.remove(player.getUniqueId());
    }
} 