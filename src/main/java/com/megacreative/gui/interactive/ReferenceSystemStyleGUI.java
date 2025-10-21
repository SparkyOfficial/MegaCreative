package com.megacreative.gui.interactive;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 🎆 Reference System-Style Interactive GUI Examples
 * 
 * Demonstrates various reference system-style interactive elements:
 * - Material selection with live preview
 * - Mode toggles with visual feedback
 * - Dynamic value adjustments
 * - Real-time parameter editing
 */
public class ReferenceSystemStyleGUI {
    
    private final MegaCreative plugin;
    private final InteractiveGUIManager guiManager;
    
    public ReferenceSystemStyleGUI(MegaCreative plugin) {
        this.plugin = plugin;
        this.guiManager = new InteractiveGUIManager(plugin);
    }
    
    /**
     * Creates a reference system-style block parameter editor
     */
    public InteractiveGUI createBlockParameterEditor(Player player, CodeBlock block) {
        InteractiveGUI gui = guiManager.createInteractiveGUI(player, 
            "🎆 Reference System Block Editor - " + block.getAction(), 54);
        
        
        gui.getInventory().setItem(4, createTitleItem(block));
        
        
        if (block.hasParameter("material")) {
            Map<String, Object> materialProps = new HashMap<>();
            materialProps.put("materials", Arrays.asList(
                Material.STONE, Material.DIRT, Material.GRASS_BLOCK, Material.OAK_PLANKS,
                Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK,
                Material.EMERALD_BLOCK, Material.OBSIDIAN, Material.BEDROCK
            ));
            
            InteractiveGUIManager.MaterialSelectorElement materialSelector = 
                new InteractiveGUIManager.MaterialSelectorElement("material", materialProps);
            
            
            materialSelector.addChangeListener(value -> {
                block.setParameter("material", value.getValue());
                saveBlockToWorld(player, block);
            });
            
            gui.setElement(10, materialSelector);
        }
        
        
        if (block.hasParameter("amount") || block.hasParameter("count")) {
            Map<String, Object> amountProps = new HashMap<>();
            amountProps.put("min", 1.0);
            amountProps.put("max", 64.0);
            amountProps.put("step", 1.0);
            amountProps.put("value", 1.0);
            
            InteractiveGUIManager.NumberSliderElement amountSlider = 
                new InteractiveGUIManager.NumberSliderElement("amount", amountProps);
            
            amountSlider.addChangeListener(value -> {
                String paramName = block.hasParameter("amount") ? "amount" : "count";
                block.setParameter(paramName, value.getValue());
                saveBlockToWorld(player, block);
            });
            
            gui.setElement(12, amountSlider);
        }
        
        
        Map<String, Object> enabledProps = new HashMap<>();
        enabledProps.put("modes", Arrays.asList("ENABLED", "DISABLED"));
        
        InteractiveGUIManager.ModeToggleElement enabledToggle = 
            new InteractiveGUIManager.ModeToggleElement("enabled", enabledProps);
        
        enabledToggle.addChangeListener(value -> {
            boolean enabled = "ENABLED".equals(value.getValue());
            block.setParameter("enabled", enabled);
            saveBlockToWorld(player, block);
        });
        
        gui.setElement(14, enabledToggle);
        
        
        if (block.getAction().contains("color") || block.hasParameter("color")) {
            Map<String, Object> colorProps = new HashMap<>();
            
            InteractiveGUIManager.ColorPickerElement colorPicker = 
                new InteractiveGUIManager.ColorPickerElement("color", colorProps);
            
            colorPicker.addChangeListener(value -> {
                block.setParameter("color", value.getValue());
                saveBlockToWorld(player, block);
            });
            
            gui.setElement(16, colorPicker);
        }
        
        
        gui.getInventory().setItem(45, createSaveButton());
        gui.getInventory().setItem(53, createCancelButton());
        
        return gui;
    }
    
    /**
     * Creates a reference system-style world settings GUI
     */
    public InteractiveGUI createWorldSettingsGUI(Player player, CreativeWorld world) {
        InteractiveGUI gui = guiManager.createInteractiveGUI(player, 
            "🎆 Reference System World Settings - " + world.getName(), 54);
        
        
        InteractiveGUIManager.ModeToggleElement modeToggle = createModeToggleElement(world);
        
        modeToggle.addChangeListener(value -> {
            try {
                world.setMode(com.megacreative.models.WorldMode.valueOf((String) value.getValue()));
                plugin.getServiceRegistry().getWorldManager().saveWorld(world);
            } catch (Exception e) {
                player.sendMessage("§cFailed to change world mode: " + e.getMessage());
            }
        });
        
        gui.setElement(10, modeToggle);
        
        
        gui.setElement(12, createTimeSlider(world));
        
        
        Map<String, Object> weatherProps = new HashMap<>();
        weatherProps.put("modes", Arrays.asList("CLEAR", "RAIN", "THUNDER"));
        
        InteractiveGUIManager.ModeToggleElement weatherToggle = 
            new InteractiveGUIManager.ModeToggleElement("weather", weatherProps);
        
        weatherToggle.addChangeListener(value -> {
            String weather = (String) value.getValue();
            switch (weather) {
                case "CLEAR":
                    world.getBukkitWorld().setStorm(false);
                    world.getBukkitWorld().setThundering(false);
                    break;
                case "RAIN":
                    world.getBukkitWorld().setStorm(true);
                    world.getBukkitWorld().setThundering(false);
                    break;
                case "THUNDER":
                    world.getBukkitWorld().setStorm(true);
                    world.getBukkitWorld().setThundering(true);
                    break;
            }
        });
        
        gui.setElement(14, weatherToggle);
        
        
        Map<String, Object> difficultyProps = new HashMap<>();
        difficultyProps.put("modes", Arrays.asList("PEACEFUL", "EASY", "NORMAL", "HARD"));
        
        InteractiveGUIManager.ModeToggleElement difficultyToggle = 
            new InteractiveGUIManager.ModeToggleElement("difficulty", difficultyProps);
        
        difficultyToggle.addChangeListener(value -> {
            String difficulty = (String) value.getValue();
            try {
                org.bukkit.Difficulty bukkitDifficulty = org.bukkit.Difficulty.valueOf(difficulty);
                world.getBukkitWorld().setDifficulty(bukkitDifficulty);
            } catch (Exception e) {
                player.sendMessage("§cInvalid difficulty: " + difficulty);
            }
        });
        
        gui.setElement(16, difficultyToggle);
        
        return gui;
    }
    
    /**
     * Creates a reference system-style item editor GUI
     */
    public InteractiveGUI createItemEditorGUI(Player player, String parameterName, DataValue currentValue) {
        InteractiveGUI gui = guiManager.createInteractiveGUI(player, 
            "🎆 Reference System Item Editor - " + parameterName, 27);
        
        
        Map<String, Object> itemProps = new HashMap<>();
        
        InteractiveGUIManager.ItemStackEditorElement itemEditor = 
            new InteractiveGUIManager.ItemStackEditorElement("item", itemProps);
        
        gui.setElement(13, itemEditor);
        
        
        gui.setElement(15, createAmountSliderElement());
        
        
        Map<String, Object> materialProps = new HashMap<>();
        materialProps.put("materials", Arrays.asList(
            Material.STONE, Material.DIRT, Material.OAK_PLANKS, Material.IRON_INGOT,
            Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD
        ));
        
        InteractiveGUIManager.MaterialSelectorElement materialSelector = 
            new InteractiveGUIManager.MaterialSelectorElement("material", materialProps);
        
        gui.setElement(11, materialSelector);
        
        return gui;
    }
    
    /**
     * Helper methods
     */
    
    private ItemStack createTitleItem(CodeBlock block) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6🎆 Editing Block: §e" + block.getAction());
            meta.setLore(Arrays.asList(
                "§7Block Type: §f" + block.getAction(),
                "§7Material: §f" + block.getMaterial().name(),
                "§7Parameters: §f" + block.getParameters().size(),
                "",
                "§eUse the controls below to modify parameters"
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createSaveButton() {
        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a🎆 Save Changes");
            meta.setLore(Arrays.asList(
                "§7Click to save all changes",
                "§7and close the editor"
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§c🎆 Cancel");
            meta.setLore(Arrays.asList(
                "§7Click to discard changes",
                "§7and close the editor"
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates a time slider element for the world editor GUI
     */
    private InteractiveGUIManager.NumberSliderElement createTimeSlider(CreativeWorld world) {
        Map<String, Object> timeProps = new HashMap<>();
        timeProps.put("min", 0.0);
        timeProps.put("max", 24000.0);
        timeProps.put("step", 1000.0);
        timeProps.put("value", (double) world.getBukkitWorld().getTime());
        
        InteractiveGUIManager.NumberSliderElement timeSlider = 
            new InteractiveGUIManager.NumberSliderElement("time", timeProps);
        
        timeSlider.addChangeListener(value -> {
            long time = ((Number) value.getValue()).longValue();
            world.getBukkitWorld().setTime(time);
        });
        
        return timeSlider;
    }
    
    /**
     * Creates a mode toggle element for world settings
     */
    private InteractiveGUIManager.ModeToggleElement createModeToggleElement(CreativeWorld world) {
        Map<String, Object> modeProps = new HashMap<>();
        modeProps.put("modes", Arrays.asList("BUILD", "PLAY", "DEV"));
        
        return new InteractiveGUIManager.ModeToggleElement("world_mode", modeProps);
    }
    
    /**
     * Creates an amount slider element for the item editor GUI
     */
    private InteractiveGUIManager.NumberSliderElement createAmountSliderElement() {
        Map<String, Object> amountProps = new HashMap<>();
        amountProps.put("min", 1.0);
        amountProps.put("max", 64.0);
        amountProps.put("step", 1.0);
        amountProps.put("value", 1.0);
        
        return new InteractiveGUIManager.NumberSliderElement("amount", amountProps);
    }
    
    private void saveBlockToWorld(Player player, CodeBlock block) {
        
        CreativeWorld world = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world != null) {
            plugin.getServiceRegistry().getWorldManager().saveWorld(world);
        }
    }
    
    /**
     * Gets the GUI manager
     */
    public InteractiveGUIManager getGUIManager() {
        return guiManager;
    }
}