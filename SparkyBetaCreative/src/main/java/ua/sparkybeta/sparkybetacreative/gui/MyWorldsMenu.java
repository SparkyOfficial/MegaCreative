package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.Component;

public class MyWorldsMenu extends AbstractMenu {

    public MyWorldsMenu(Player player) {
        super(player, 54, "My Worlds");
        redraw();
    }

    public void redraw() {
        inventory.clear();
        
        List<SparkyWorld> worlds = SparkyBetaCreative.getInstance().getWorldManager().getPlayerWorlds(player.getUniqueId());

        int slot = 0;
        for (SparkyWorld world : worlds) {
            if (slot >= 54) break; 
            
            ItemStack worldItem = createWorldItem(world);
            setButton(slot, worldItem, (event) -> {
                player.closeInventory();
                player.sendMessage("§eTeleporting to your world: " + world.getDisplayName());
                SparkyBetaCreative.getInstance().getWorldManager().teleportToWorld(player, world)
                    .thenAccept(success -> {
                        if (!success) {
                            player.sendMessage("§cCould not teleport to the world. Please try again later.");
                        }
                    });
            });
            slot++;
        }
        
        // Add create world button if player has less than 3 worlds
        if (worlds.size() < 3) {
            setButton(53, createAddItem(), (event) -> {
                new WorldTypeSelectionMenu(player).open();
            });
        }
    }

    private ItemStack createWorldItem(SparkyWorld world) {
        ItemStack item = new ItemStack(getMaterialForWorld(world));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§a" + world.getDisplayName()));
        meta.lore(List.of(
                Component.text("§7ID: " + world.getCustomId()),
                Component.text("§7Type: " + world.getType().name()),
                Component.text(""),
                Component.text("§eClick to teleport!")
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createAddItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§aCreate New World"));
        meta.lore(List.of(Component.text("§7You can create a new world.")));
        item.setItemMeta(meta);
        return item;
    }
    
    private Material getMaterialForWorld(SparkyWorld world) {
        return switch (world.getType()) {
            case FLAT, DEFAULT -> Material.GRASS_BLOCK;
            case VOID -> Material.END_STONE;
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE_BRICKS;
        };
    }
} 