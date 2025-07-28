package ua.sparkybeta.sparkybetacreative.coding;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlockCategory;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;

public class CodingKit {

    public static void give(Player player) {
        player.getInventory().clear();

        for (CodeBlockCategory category : CodeBlockCategory.values()) {
            ItemStack item = new ItemBuilder(category.getMaterial())
                    .setName("§b§l" + category.getDisplayName())
                    .setLore("§7A block representing the", "§7'" + category.getDisplayName() + "' category.")
                    .build();

            item.editMeta(meta -> {
                meta.getPersistentDataContainer().set(CodingKeys.CATEGORY_KEY, PersistentDataType.STRING, category.name());
            });

            player.getInventory().addItem(item);
        }
    }
}