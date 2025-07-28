package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.worlds.WorldType;

import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;

public class WorldTypeSelectionMenu extends AbstractMenu {
    public WorldTypeSelectionMenu(Player player) {
        super(player, 27, "Select World Type");
        
        setButton(10, createTypeItem(Material.GRASS_BLOCK, "Default World", "A standard survival world."), (e) -> createWorld(WorldType.DEFAULT));
        setButton(12, createTypeItem(Material.FEATHER, "Flat World", "A flat world for creative building."), (e) -> createWorld(WorldType.FLAT));
        setButton(14, createTypeItem(Material.END_STONE, "Void World", "An empty world with just a platform."), (e) -> createWorld(WorldType.VOID));
        setButton(16, createTypeItem(Material.NETHERRACK, "Nether World", "A world with the Nether environment."), (e) -> createWorld(WorldType.NETHER));
        setButton(22, createTypeItem(Material.DRAGON_HEAD, "The End World", "A world with The End environment."), (e) -> createWorld(WorldType.THE_END));
    }

    private void createWorld(WorldType type) {
        player.closeInventory();
        player.sendMessage("§eCreating a " + type.name() + " world for you...");
        SparkyBetaCreative.getInstance().getWorldManager().createWorld(player, type)
            .thenAccept(success -> {
                if (success) {
                    player.sendMessage("§aWorld created successfully!");
                    // Here you might want to teleport the player or open the My Worlds menu again
                } else {
                    player.sendMessage("§cFailed to create world. You may have reached your world limit.");
                }
            });
    }

    private ItemStack createTypeItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§a" + name));
        meta.lore(List.of(Component.text("§7" + description)));
        item.setItemMeta(meta);
        return item;
    }
} 