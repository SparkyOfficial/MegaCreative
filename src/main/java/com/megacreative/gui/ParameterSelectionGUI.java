package com.megacreative.gui;

import com.megacreative.MegaCreative;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ParameterSelectionGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final String parameterName;
    private final String[] options;
    private final Consumer<String> onSelect;
    private final Inventory inventory;
    
    public ParameterSelectionGUI(MegaCreative plugin, Player player, String parameterName, String[] options, Consumer<String> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.parameterName = parameterName;
        this.options = options;
        this.onSelect = onSelect;
        
        // Создаем инвентарь с заголовком
        this.inventory = Bukkit.createInventory(null, 27, "§8Выбор: " + parameterName);
        
        // Заполняем опциями
        fillOptions();
        
        // Регистрируем слушатель
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void fillOptions() {
        // Очищаем инвентарь
        inventory.clear();
        
        // Добавляем опции
        for (int i = 0; i < options.length && i < 18; i++) {
            ItemStack item = createOptionItem(options[i], i);
            inventory.setItem(i + 9, item); // Размещаем в центре
        }
        
        // Добавляем заголовок
        ItemStack header = new ItemStack(Material.BOOK);
        ItemMeta headerMeta = header.getItemMeta();
        headerMeta.setDisplayName("§e" + parameterName);
        headerMeta.setLore(Arrays.asList(
            "§7Выберите значение для параметра",
            "§7Кликните на нужный вариант"
        ));
        header.setItemMeta(headerMeta);
        inventory.setItem(4, header);
    }
    
    private ItemStack createOptionItem(String option, int index) {
        Material material = getMaterialForOption(option);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f" + option);
        meta.setLore(Arrays.asList(
            "§7Кликните для выбора",
            "§7Значение: §e" + option
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    private Material getMaterialForOption(String option) {
        // Определяем материал в зависимости от типа параметра
        if (parameterName.toLowerCase().contains("gamemode")) {
            switch (option.toLowerCase()) {
                case "survival": return Material.IRON_SWORD;
                case "creative": return Material.DIAMOND_PICKAXE;
                case "adventure": return Material.LEATHER_BOOTS;
                case "spectator": return Material.GLASS;
                default: return Material.STONE;
            }
        } else if (parameterName.toLowerCase().contains("sound")) {
            return Material.NOTE_BLOCK;
        } else if (parameterName.toLowerCase().contains("effect")) {
            return Material.POTION;
        } else if (parameterName.toLowerCase().contains("weather")) {
            switch (option.toLowerCase()) {
                case "clear": return Material.SUNFLOWER;
                case "rain": return Material.WATER_BUCKET;
                case "thunder": return Material.LIGHTNING_ROD;
                default: return Material.STONE;
            }
        } else if (parameterName.toLowerCase().contains("time")) {
            return Material.CLOCK;
        } else {
            return Material.PAPER;
        }
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != inventory) return;
        if (event.getWhoClicked() != player) return;
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        if (slot < 9 || slot >= 27) return; // Только центральная область
        
        int optionIndex = slot - 9;
        if (optionIndex >= 0 && optionIndex < options.length) {
            String selectedOption = options[optionIndex];
            
            // Вызываем callback
            onSelect.accept(selectedOption);
            
            // Закрываем инвентарь
            player.closeInventory();
            
            // Отправляем сообщение
            player.sendMessage("§a✓ Выбрано: §f" + selectedOption);
        }
    }
    
    // Статические методы для создания GUI для разных типов параметров
    
    public static void showGameModeSelection(Player player, Consumer<String> onSelect) {
        String[] gameModes = {"SURVIVAL", "CREATIVE", "ADVENTURE", "SPECTATOR"};
        ParameterSelectionGUI gui = new ParameterSelectionGUI(
            MegaCreative.getInstance(), 
            player, 
            "GameMode", 
            gameModes, 
            onSelect
        );
        gui.open();
    }
    
    public static void showSoundSelection(Player player, Consumer<String> onSelect) {
        String[] sounds = {
            "BLOCK_NOTE_BLOCK_PLING",
            "ENTITY_PLAYER_LEVELUP", 
            "ENTITY_EXPERIENCE_ORB_PICKUP",
            "BLOCK_ANVIL_LAND",
            "ENTITY_VILLAGER_YES",
            "ENTITY_VILLAGER_NO"
        };
        ParameterSelectionGUI gui = new ParameterSelectionGUI(
            MegaCreative.getInstance(), 
            player, 
            "Sound", 
            sounds, 
            onSelect
        );
        gui.open();
    }
    
    public static void showWeatherSelection(Player player, Consumer<String> onSelect) {
        String[] weathers = {"CLEAR", "RAIN", "THUNDER"};
        ParameterSelectionGUI gui = new ParameterSelectionGUI(
            MegaCreative.getInstance(), 
            player, 
            "Weather", 
            weathers, 
            onSelect
        );
        gui.open();
    }
    
    public static void showEffectSelection(Player player, Consumer<String> onSelect) {
        String[] effects = {
            "SPEED", "SLOW", "FAST_DIGGING", "SLOW_DIGGING",
            "INCREASE_DAMAGE", "HEAL", "HARM", "JUMP",
            "CONFUSION", "REGENERATION", "DAMAGE_RESISTANCE",
            "FIRE_RESISTANCE", "WATER_BREATHING", "INVISIBILITY",
            "BLINDNESS", "NIGHT_VISION", "HUNGER", "WEAKNESS",
            "POISON", "WITHER", "HEALTH_BOOST", "ABSORPTION",
            "SATURATION", "GLOWING", "LEVITATION", "LUCK",
            "UNLUCK", "SLOW_FALLING", "CONDUIT_POWER", "DOLPHINS_GRACE"
        };
        ParameterSelectionGUI gui = new ParameterSelectionGUI(
            MegaCreative.getInstance(), 
            player, 
            "Effect", 
            effects, 
            onSelect
        );
        gui.open();
    }
} 