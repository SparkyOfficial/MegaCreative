package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.listeners.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.potion.PotionEffectType;
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

/**
 * GUI для выбора параметров из предопределенных списков.
 * Заменяет наковальню для параметров с ограниченным набором вариантов.
 */
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
        this.inventory = Bukkit.createInventory(null, 27, "§8Выбор: " + parameterName);
        
        fillOptions();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        GuiListener.registerOpenGui(player, this);
    }
    
    private void fillOptions() {
        inventory.clear();
        
        // Заполняем границы стеклом
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, glass);
            inventory.setItem(18 + i, glass);
        }
        inventory.setItem(9, glass);
        inventory.setItem(17, glass);
        
        // Размещаем опции
        int slot = 10;
        for (String option : options) {
            if (slot >= 17) break;
            
            ItemStack optionItem = new ItemStack(Material.PAPER);
            ItemMeta optionMeta = optionItem.getItemMeta();
            optionMeta.setDisplayName("§f" + option);
            optionMeta.setLore(Arrays.asList(
                "§7Параметр: §f" + parameterName,
                "",
                "§a▶ Нажмите для выбора"
            ));
            optionItem.setItemMeta(optionMeta);
            inventory.setItem(slot, optionItem);
            slot++;
        }
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        if (displayName.startsWith("§f")) {
            String selectedOption = displayName.substring(2); // Убираем "§f"
            
            player.closeInventory();
            GuiListener.unregisterOpenGui(player);
            
            // Вызываем callback
            onSelect.accept(selectedOption);
        }
    }
    
    // Статические методы для создания GUI для разных типов параметров
    
    public static void openGameModeSelection(MegaCreative plugin, Player player, Consumer<String> onSelect) {
        String[] gameModes = {
            GameMode.SURVIVAL.name(),
            GameMode.CREATIVE.name(),
            GameMode.ADVENTURE.name(),
            GameMode.SPECTATOR.name()
        };
        new ParameterSelectionGUI(plugin, player, "GameMode", gameModes, onSelect).open();
    }
    
    public static void openSoundSelection(MegaCreative plugin, Player player, Consumer<String> onSelect) {
        String[] sounds = {
            Sound.BLOCK_NOTE_BLOCK_PLING.name(),
            Sound.ENTITY_PLAYER_LEVELUP.name(),
            Sound.BLOCK_ANVIL_LAND.name(),
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP.name(),
            Sound.BLOCK_CHEST_OPEN.name(),
            Sound.ENTITY_VILLAGER_YES.name(),
            Sound.BLOCK_GLASS_BREAK.name(),
            Sound.ENTITY_GENERIC_EXPLODE.name()
        };
        new ParameterSelectionGUI(plugin, player, "Sound", sounds, onSelect).open();
    }
    
    public static void openWeatherSelection(MegaCreative plugin, Player player, Consumer<String> onSelect) {
        String[] weathers = {
            "CLEAR",
            "RAIN",
            "THUNDER"
        };
        new ParameterSelectionGUI(plugin, player, "Weather", weathers, onSelect).open();
    }
    
    public static void openEffectSelection(MegaCreative plugin, Player player, Consumer<String> onSelect) {
        String[] effects = {
            PotionEffectType.SPEED.name(),
            PotionEffectType.SLOW.name(),
            PotionEffectType.FAST_DIGGING.name(),
            PotionEffectType.SLOW_DIGGING.name(),
            PotionEffectType.INCREASE_DAMAGE.name(),
            PotionEffectType.HEAL.name(),
            PotionEffectType.HARM.name(),
            PotionEffectType.JUMP.name(),
            PotionEffectType.CONFUSION.name(),
            PotionEffectType.REGENERATION.name(),
            PotionEffectType.DAMAGE_RESISTANCE.name(),
            PotionEffectType.FIRE_RESISTANCE.name(),
            PotionEffectType.WATER_BREATHING.name(),
            PotionEffectType.INVISIBILITY.name(),
            PotionEffectType.BLINDNESS.name(),
            PotionEffectType.NIGHT_VISION.name(),
            PotionEffectType.HUNGER.name(),
            PotionEffectType.WEAKNESS.name(),
            PotionEffectType.POISON.name(),
            PotionEffectType.WITHER.name(),
            PotionEffectType.HEALTH_BOOST.name(),
            PotionEffectType.ABSORPTION.name(),
            PotionEffectType.SATURATION.name(),
            PotionEffectType.GLOWING.name(),
            PotionEffectType.LEVITATION.name(),
            PotionEffectType.LUCK.name(),
            PotionEffectType.UNLUCK.name(),
            PotionEffectType.SLOW_FALLING.name(),
            PotionEffectType.CONDUIT_POWER.name(),
            PotionEffectType.DOLPHINS_GRACE.name(),
            PotionEffectType.BAD_OMEN.name(),
            PotionEffectType.HERO_OF_THE_VILLAGE.name()
        };
        new ParameterSelectionGUI(plugin, player, "Effect", effects, onSelect).open();
    }
} 