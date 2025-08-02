package com.megacreative.coding;

import com.megacreative.listeners.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.List;
import java.util.function.Consumer;
import java.util.Arrays;

public class CodingActionGUI implements Listener {
    private final Player player;
    private final Material material;
    private final Location blockLocation;
    private final Consumer<String> onSelect;
    private final List<String> actions;
    private final Inventory inventory;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 7; // 7 слотов для действий, 2 для навигации

    public CodingActionGUI(Player player, Material material, Location blockLocation, List<String> actions, Consumer<String> onSelect) {
        this.player = player;
        this.material = material;
        this.blockLocation = blockLocation;
        this.actions = actions;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 9, "§bВыберите действие");
        // Регистрируем GUI в централизованной системе
        GuiListener.registerOpenGui(player, this);
        setupInventory();
    }

    private void setupInventory() {
        inventory.clear();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, actions.size());
        
        // Добавляем действия для текущей страницы
        for (int i = 0; i < endIndex - startIndex; i++) {
            String action = actions.get(startIndex + i);
            ItemStack item = new ItemStack(getActionIcon(action));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§a" + action);
                meta.setLore(Arrays.asList(
                    "§7" + getActionDescription(action),
                    "§e▶ Нажмите для выбора"
                ));
                item.setItemMeta(meta);
            }
            inventory.setItem(i, item);
        }
        
        // Добавляем кнопки навигации
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§e§lПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(7, prevButton);
        }
        
        if (endIndex < actions.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("§e§lСледующая страница");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(8, nextButton);
        }
    }

    private Material getActionIcon(String action) {
        switch (action) {
            case "sendMessage": return Material.PAPER;
            case "teleport": return Material.COMPASS;
            case "giveItem": return Material.CHEST;
            case "playSound": return Material.NOTE_BLOCK;
            case "effect": return Material.POTION;
            case "command": return Material.COMMAND_BLOCK;
            case "broadcast": return Material.BELL;
            case "setVar": return Material.IRON_INGOT;
            case "addVar": return Material.GOLD_INGOT;
            case "subVar": return Material.REDSTONE;
            case "mulVar": return Material.REDSTONE_TORCH;
            case "divVar": return Material.REDSTONE_LAMP;
            case "setTime": return Material.CLOCK;
            case "setWeather": return Material.WATER_BUCKET;
            case "spawnMob": return Material.ZOMBIE_HEAD;
            case "ifVar": return Material.OBSIDIAN;
            case "ifNotVar": return Material.BARRIER;
            case "ifGameMode": return Material.DIAMOND_SWORD;
            case "ifWorldType": return Material.GRASS_BLOCK;
            case "ifMobType": return Material.SPAWNER;
            case "ifMobNear": return Material.ENDER_EYE;
            case "getVar": return Material.BOOK;
            case "getPlayerName": return Material.PLAYER_HEAD;
            case "isOp": return Material.COMMAND_BLOCK;
            case "isInWorld": return Material.GLOBE_BANNER_PATTERN;
            case "hasItem": return Material.STICK;
            case "hasPermission": return Material.SHIELD;
            case "isNearBlock": return Material.GLASS;
            case "timeOfDay": return Material.SUNFLOWER;
            case "onJoin": return Material.EMERALD;
            case "onChat": return Material.OAK_SIGN;
            case "onLeave": return Material.REDSTONE_TORCH;
            case "onInteract": return Material.LEVER;
            case "else": return Material.END_STONE;
            case "repeat": return Material.HOPPER;
            case "callFunction": return Material.BOOKSHELF;
            case "saveFunction": return Material.WRITABLE_BOOK;
            case "repeatTrigger": return Material.REPEATER;
            default: return material; // Используем материал блока как иконку по умолчанию
        }
    }

    private String getActionDescription(String action) {
        switch (action) {
            case "sendMessage": return "Отправить сообщение игроку";
            case "teleport": return "Телепортировать игрока";
            case "giveItem": return "Выдать предмет игроку";
            case "playSound": return "Проиграть звук игроку";
            case "effect": return "Наложить эффект на игрока";
            case "command": return "Выполнить команду";
            case "broadcast": return "Отправить сообщение всем";
            case "setVar": return "Установить значение переменной";
            case "addVar": return "Добавить к переменной";
            case "subVar": return "Вычесть из переменной";
            case "mulVar": return "Умножить переменную";
            case "divVar": return "Разделить переменную";
            case "setTime": return "Установить время суток";
            case "setWeather": return "Изменить погоду";
            case "spawnMob": return "Создать моба";
            case "ifVar": return "Если переменная равна";
            case "ifNotVar": return "Если переменная не равна";
            case "ifGameMode": return "Если режим игры";
            case "ifWorldType": return "Если тип мира";
            case "ifMobType": return "Если рядом моб";
            case "ifMobNear": return "Если моб в радиусе";
            case "getVar": return "Получить значение переменной";
            case "getPlayerName": return "Получить имя игрока";
            case "isOp": return "Если игрок оператор";
            case "isInWorld": return "Если игрок в мире";
            case "hasItem": return "Если у игрока есть предмет";
            case "hasPermission": return "Если у игрока есть права";
            case "isNearBlock": return "Если рядом блок";
            case "timeOfDay": return "Если время суток";
            case "onJoin": return "При входе игрока";
            case "onChat": return "При сообщении в чат";
            case "onLeave": return "При выходе игрока";
            case "onInteract": return "При взаимодействии";
            case "else": return "Иначе (если условие не выполнено)";
            case "repeat": return "Повторить блок N раз";
            case "callFunction": return "Вызвать функцию";
            case "saveFunction": return "Сохранить как функцию";
            case "repeatTrigger": return "Повторяющийся триггер";
            default: return "Действие: " + action;
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Обработка навигации
        if (displayName.contains("Предыдущая страница")) {
            currentPage--;
            setupInventory();
            return;
        }
        if (displayName.contains("Следующая страница")) {
            currentPage++;
            setupInventory();
            return;
        }
        
        // Обработка выбора действия
        String action = displayName.replace("§a", "");
        if (actions.contains(action)) {
            onSelect.accept(action);
            player.closeInventory();
            // Удаляем регистрацию GUI
            GuiListener.unregisterOpenGui(player);
        }
    }
}