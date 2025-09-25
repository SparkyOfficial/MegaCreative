package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.function.Consumer;

public class PlaySoundEditor extends AbstractParameterEditor {
    
    public PlaySoundEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Play Sound Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Sound slot
        ItemStack soundItem = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta soundMeta = soundItem.getItemMeta();
        soundMeta.setDisplayName("§eSound Name");
        DataValue sound = codeBlock.getParameter("sound", DataValue.of("ENTITY_PLAYER_LEVELUP"));
        soundMeta.setLore(java.util.Arrays.asList(
            "§7Enter the sound name to play",
            "§aCurrent value: §f" + (sound != null ? sound.asString() : "ENTITY_PLAYER_LEVELUP")
        ));
        soundItem.setItemMeta(soundMeta);
        inventory.setItem(0, soundItem);
        
        // Volume slot
        ItemStack volumeItem = new ItemStack(Material.SLIME_BLOCK);
        ItemMeta volumeMeta = volumeItem.getItemMeta();
        volumeMeta.setDisplayName("§eVolume");
        DataValue volume = codeBlock.getParameter("volume", DataValue.of("1.0"));
        volumeMeta.setLore(java.util.Arrays.asList(
            "§7Enter the volume level (0.0-2.0)",
            "§aCurrent value: §f" + (volume != null ? volume.asString() : "1.0")
        ));
        volumeItem.setItemMeta(volumeMeta);
        inventory.setItem(1, volumeItem);
        
        // Pitch slot
        ItemStack pitchItem = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta pitchMeta = pitchItem.getItemMeta();
        pitchMeta.setDisplayName("§ePitch");
        DataValue pitch = codeBlock.getParameter("pitch", DataValue.of("1.0"));
        pitchMeta.setLore(java.util.Arrays.asList(
            "§7Enter the pitch level (0.5-2.0)",
            "§aCurrent value: §f" + (pitch != null ? pitch.asString() : "1.0")
        ));
        pitchItem.setItemMeta(pitchMeta);
        inventory.setItem(2, pitchItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Play Sound action",
            "",
            "§eHow to use:",
            "§71. Set the sound name to play",
            "§72. Adjust the volume level",
            "§73. Adjust the pitch level"
        ));
        helpItem.setItemMeta(helpMeta);
        inventory.setItem(8, helpItem);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();
        
        switch (slot) {
            case 0: // Sound slot
                // Open anvil GUI for sound input
                DataValue currentSound = codeBlock.getParameter("sound", DataValue.of("ENTITY_PLAYER_LEVELUP"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter sound name", 
                    newValue -> {
                        codeBlock.setParameter("sound", DataValue.of(newValue));
                        player.sendMessage("§aSound name set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Volume slot
                // Open anvil GUI for volume input
                DataValue currentVolume = codeBlock.getParameter("volume", DataValue.of("1.0"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter volume", 
                    newValue -> {
                        codeBlock.setParameter("volume", DataValue.of(newValue));
                        player.sendMessage("§aVolume set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 2: // Pitch slot
                // Open anvil GUI for pitch input
                DataValue currentPitch = codeBlock.getParameter("pitch", DataValue.of("1.0"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter pitch", 
                    newValue -> {
                        codeBlock.setParameter("pitch", DataValue.of(newValue));
                        player.sendMessage("§aPitch set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Play Sound action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}