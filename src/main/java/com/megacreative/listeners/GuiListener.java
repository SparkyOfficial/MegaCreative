package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingActionGUI;
import com.megacreative.coding.CodingParameterGUI;
import com.megacreative.gui.*;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldComment;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiListener implements Listener {
    private final MegaCreative plugin;
    private static final Map<UUID, Object> openGuis = new HashMap<>();

    public GuiListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    public static void registerOpenGui(Player player, Object gui) {
        openGuis.put(player.getUniqueId(), gui);
    }

    public static void unregisterOpenGui(Player player) {
        openGuis.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            unregisterOpenGui(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!openGuis.containsKey(player.getUniqueId())) return;

        Object guiObject = openGuis.get(player.getUniqueId());

        // --- Диспетчер событий ---
        // Передаем событие клика конкретному объекту GUI, который открыт у игрока
        if (guiObject instanceof MyWorldsGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof WorldCreationGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof WorldBrowserGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof WorldSettingsGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof WorldActionsGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof WorldCommentsGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof ScriptsGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof TemplateBrowserGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof DataGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof CodingActionGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof CodingParameterGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof ParameterSelectorGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof AnvilInputGUI gui) {
            gui.onInventoryClick(event);
        } else if (guiObject instanceof TrustedPlayersGUI gui) {
             // Для этого GUI логика проще, можно оставить ее здесь или вынести
             event.setCancelled(true);
             handleTrustedPlayersGUI(event);
        }
        // ... добавьте другие GUI по мере их создания ...
    }

    private void handleTrustedPlayersGUI(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        int slot = event.getSlot();

        if (!player.hasPermission("megacreative.trusted")) {
            player.sendMessage("§c❌ У вас нет прав для управления доверенными игроками!");
            player.closeInventory();
            return;
        }

        switch (slot) {
            case 19: player.sendMessage("§a✅ Используйте: §f/trusted add <игрок> BUILDER " + player.getName()); player.closeInventory(); break;
            case 21: player.sendMessage("§a✅ Используйте: §f/trusted add <игрок> CODER " + player.getName()); player.closeInventory(); break;
            case 23: player.sendMessage("§a✅ Используйте: §f/trusted remove <игрок>"); player.closeInventory(); break;
            case 25: player.sendMessage("§a✅ Используйте: §f/trusted info <игрок>"); player.closeInventory(); break;
            default:
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                     String targetName = event.getCurrentItem().getItemMeta().getDisplayName().replaceAll("§f", "");
                     player.sendMessage("§a✅ Для удаления: §f/trusted remove " + targetName);
                     player.closeInventory();
                }
                break;
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDeleteConfirmations().containsKey(player.getUniqueId()) || plugin.getCommentInputs().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String message = event.getMessage();

            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                if (plugin.getDeleteConfirmations().containsKey(player.getUniqueId())) {
                    String worldId = plugin.getDeleteConfirmations().remove(player.getUniqueId());
                    if (message.equalsIgnoreCase("УДАЛИТЬ")) {
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
                } else if (plugin.getCommentInputs().containsKey(player.getUniqueId())) {
                    CreativeWorld world = plugin.getCommentInputs().remove(player.getUniqueId());
                    if (world == null) return;
                    if (message.equalsIgnoreCase("отмена")) {
                        player.sendMessage("§cДобавление комментария отменено.");
                        return;
                    }
                    if (message.length() > 100) {
                        player.sendMessage("§cКомментарий слишком длинный! Максимум 100 символов.");
                        plugin.getCommentInputs().put(player.getUniqueId(), world);
                        return;
                    }
                    WorldComment comment = new WorldComment(player.getUniqueId(), player.getName(), message);
                    world.addComment(comment);
                    plugin.getWorldManager().saveWorld(world);
                    new WorldCommentsGUI(plugin, player, world, 0).open();
                }
            });
        }
    }
}
