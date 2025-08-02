package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldComment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.megacreative.gui.WorldCommentsGUI;

public class GuiListener implements Listener {
    private final MegaCreative plugin;
    
    // Централизованное отслеживание открытых GUI
    private static final Map<UUID, Object> openGuis = new HashMap<>();

    public GuiListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Регистрирует открытое GUI для игрока
     */
    public static void registerOpenGui(Player player, Object gui) {
        openGuis.put(player.getUniqueId(), gui);
    }

    /**
     * Удаляет регистрацию GUI для игрока
     */
    public static void unregisterOpenGui(Player player) {
        openGuis.remove(player.getUniqueId());
    }

    /**
     * Проверяет, есть ли у игрока открытое GUI
     */
    public static boolean hasOpenGui(Player player) {
        return openGuis.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String viewTitle = event.getView().getTitle();
        Inventory clickedInventory = event.getClickedInventory();
        
        if (clickedInventory == null || !viewTitle.startsWith("§")) {
            return;
        }
        
        // Проверяем, есть ли у игрока открытое GUI
        if (hasOpenGui(player)) {
            // GUI сам обработает клик, но мы отменяем стандартное поведение
            event.setCancelled(true);
            return;
        }
        
        // Обработка специальных GUI по заголовку
        if (viewTitle.startsWith("§6§lКомментарии:")) {
            // WorldCommentsGUI обрабатывает свои клики самостоятельно
            event.setCancelled(true);
            return;
        }
        
        // Обработка GUI доверенных игроков
        if (viewTitle.startsWith("§8Управление доверенными игроками")) {
            event.setCancelled(true);
            handleTrustedPlayersGUI(event);
            return;
        }
        
        // Добавить другие специальные GUI здесь при необходимости
        
        // По умолчанию отменяем события для всех наших GUI
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        
        // Удаляем регистрацию GUI при закрытии инвентаря
        if (hasOpenGui(player)) {
            unregisterOpenGui(player);
        }
    }
    
    /**
     * Обрабатывает клики в GUI доверенных игроков
     */
    private void handleTrustedPlayersGUI(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        
        // Проверяем права доступа
        if (!player.hasPermission("megacreative.trusted")) {
            player.sendMessage("§c❌ У вас нет прав для управления доверенными игроками!");
            return;
        }
        
        switch (slot) {
            case 19: // Добавить строителя
                player.sendMessage("§a✅ Используйте команду: §f/trusted add <игрок> BUILDER <ваше_имя>");
                player.closeInventory();
                break;
            case 21: // Добавить программиста
                player.sendMessage("§a✅ Используйте команду: §f/trusted add <игрок> CODER <ваше_имя>");
                player.closeInventory();
                break;
            case 23: // Удалить игрока
                player.sendMessage("§a✅ Используйте команду: §f/trusted remove <игрок>");
                player.closeInventory();
                break;
            case 25: // Информация
                player.sendMessage("§a✅ Используйте команду: §f/trusted info <игрок>");
                player.closeInventory();
                break;
            default:
                // Проверяем, кликнули ли по голове игрока (удаление)
                if (slot >= 28 && slot <= 53) {
                    player.sendMessage("§a✅ Используйте команду: §f/trusted remove <игрок>");
                    player.closeInventory();
                }
                break;
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Handle удаление мира
        if (plugin.getDeleteConfirmations().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            if (message.equalsIgnoreCase("УДАЛИТЬ")) {
                // Получаем worldId
                String worldId = plugin.getDeleteConfirmations().get(player.getUniqueId());
                var world = plugin.getWorldManager().getWorld(worldId);
                if (world != null) {
                    plugin.getWorldManager().deleteWorld(world.getId(), player);
                    player.sendMessage("§aМир успешно удалён!");
                } else {
                    player.sendMessage("§cМир не найден!");
                }
            } else {
                player.sendMessage("§cУдаление отменено.");
            }
            plugin.getDeleteConfirmations().remove(player.getUniqueId());
            return;
        }

        // Handle comment input
        if (plugin.getCommentInputs().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            
            CreativeWorld world = plugin.getCommentInputs().get(player.getUniqueId());
            
            // Check for cancel command
            if (message.equalsIgnoreCase("отмена")) {
                player.sendMessage("§cДобавление комментария отменено.");
                plugin.getCommentInputs().remove(player.getUniqueId());
                return;
            }
            
            // Validate message length
            if (message.length() > 100) {
                player.sendMessage("§cКомментарий слишком длинный! Максимум 100 символов.");
                return;
            }
            
            // Create and add the comment
            WorldComment comment = new WorldComment(
                player.getUniqueId(),
                player.getName(),
                message,
                System.currentTimeMillis()
            );
            
            world.addComment(comment);
            
            // Save the world to persist the comment
            plugin.getWorldManager().saveWorld(world);
            
            // Remove from inputs and show the updated comments GUI
            plugin.getCommentInputs().remove(player.getUniqueId());
            
            // Open the comments GUI on the first page
            new WorldCommentsGUI(plugin, player, world, 0).open();
            
            return;
        }
    }
}
