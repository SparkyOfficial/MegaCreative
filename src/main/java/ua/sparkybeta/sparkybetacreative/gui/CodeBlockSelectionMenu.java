package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ua.sparkybeta.sparkybetacreative.coding.CodingKeys;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlockCategory;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CodeBlockSelectionMenu {

    public static final String MENU_TITLE = "Select Code Block"; // Changed title slightly
    private final Inventory inventory;
    private final Block targetBlock;

    public CodeBlockSelectionMenu(Block targetBlock, CodeBlockCategory category) {
        this.targetBlock = targetBlock;
        this.inventory = Bukkit.createInventory(null, 54, Component.text(MENU_TITLE + " (" + category.getDisplayName() + ")"));
        initializeItems(category);
    }

    private void initializeItems(CodeBlockCategory category) {
        List<CodeBlock> blocksInCategory = Arrays.stream(CodeBlock.values())
                .filter(cb -> cb.getCategory() == category)
                .collect(Collectors.toList());

        int slot = 0;
        for (CodeBlock codeBlock : blocksInCategory) {
            if(slot >= 54) break; // prevent overflow
            ItemStack item = new ItemBuilder(codeBlock.getMaterial())
                    .setName("§b" + codeBlock.getDisplayName())
                    .setLore(
                            "§7Category: " + category.name(),
                            "§aClick to select this action."
                    )
                    .build();

            item.editMeta(meta -> {
                meta.getPersistentDataContainer().set(CodingKeys.CODE_BLOCK, PersistentDataType.STRING, codeBlock.name());
            });
            inventory.setItem(slot++, item);
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public Block getTargetBlock() {
        return targetBlock;
    }
} 