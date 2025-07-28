package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.StoredCodeBlock;
import net.kyori.adventure.text.Component;

public class BlockConfigMenu {

    public static final String MENU_TITLE = "Configure Code Block";
    private final Inventory inventory;
    private final Player player;
    private final SparkyWorld sparkyWorld;
    private final StoredCodeBlock codeBlock;

    public BlockConfigMenu(Player player, SparkyWorld sparkyWorld, StoredCodeBlock codeBlock) {
        this.player = player;
        this.sparkyWorld = sparkyWorld;
        this.codeBlock = codeBlock;
        this.inventory = Bukkit.createInventory(null, 27, Component.text(MENU_TITLE));
        initializeItems();
    }

    private void initializeItems() {
        // Info about the block
        inventory.setItem(4, new ItemBuilder(codeBlock.getType().getMaterial())
                .setName("§b" + codeBlock.getType().getDisplayName())
                .setLore(
                        "§7ID: " + codeBlock.getId().toString().substring(0, 8),
                        "§7Arguments: " + codeBlock.getArguments().size(),
                        "§7Links: " + codeBlock.getNextBlocks().size()
                ).build());

        // Set Arguments Button
        inventory.setItem(11, new ItemBuilder(Material.BARREL)
                .setName("§aSet Arguments")
                .setLore("§7Click to set arguments for this block.")
                .build());

        // Link Next Block Button
        inventory.setItem(13, new ItemBuilder(Material.REPEATER)
                .setName("§eLink Next Block")
                .setLore("§7Click to get the Linker tool.")
                .build());

        // Delete Block Button
        inventory.setItem(15, new ItemBuilder(Material.REDSTONE_BLOCK)
                .setName("§cDelete Block")
                .setLore("§7Click to permanently delete this block.")
                .build());

        // Glass panes for decoration
        ItemStack pane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, pane);
            }
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public StoredCodeBlock getCodeBlock() {
        return codeBlock;
    }
    
    public SparkyWorld getSparkyWorld() {
        return sparkyWorld;
    }
} 