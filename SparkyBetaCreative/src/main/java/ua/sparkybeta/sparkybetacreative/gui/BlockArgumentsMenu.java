package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ua.sparkybeta.sparkybetacreative.worlds.StoredCodeBlock;
import net.kyori.adventure.text.Component;

public class BlockArgumentsMenu {

    public static final String MENU_TITLE = "Set Arguments";
    private final Inventory inventory;
    private final Player player;
    private final StoredCodeBlock codeBlock;

    public BlockArgumentsMenu(Player player, StoredCodeBlock codeBlock) {
        this.player = player;
        this.codeBlock = codeBlock;
        // A chest-sized inventory. Players can place up to 27 arguments.
        this.inventory = Bukkit.createInventory(null, 27, Component.text(MENU_TITLE));
        // Pre-fill the inventory with existing arguments
        for (int i = 0; i < codeBlock.getArguments().size() && i < 27; i++) {
            // We need to re-create the ItemStack from the Argument data
            // For now, let's assume arguments are simple items. This needs a proper parser.
            // This is a placeholder for a more complex implementation.
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public StoredCodeBlock getCodeBlock() {
        return codeBlock;
    }
} 