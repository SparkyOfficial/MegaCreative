package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;

public class WorldActionMenu extends AbstractMenu {

    private final SparkyWorld world;

    public WorldActionMenu(Player player, SparkyWorld world) {
        super(player, 27, "World Actions");
        this.world = world;
        redraw();
    }

    public void redraw() {
        inventory.clear();

        // World Info
        setButton(4, new ItemBuilder(getMaterialForWorld(world))
                .setName("§a" + world.getDisplayName())
                .setLore(
                        "§7ID: " + world.getCustomId(),
                        "§7by " + player.getName(),
                        "§7" + world.getDescription()
                ).build(), (e) -> {});

        // Teleport Button
        setButton(11, new ItemBuilder(Material.ENDER_PEARL).setName("§aTeleport to this World").build(), (e) -> {
            player.closeInventory();
            getApp().getWorldManager().teleportToWorld(player, world);
        });
        
        // Like/Dislike Buttons
        ItemBuilder likeButton = new ItemBuilder(Material.GREEN_WOOL)
                .setName("§aLike")
                .setLore("§7Likes: " + world.getLikes().size());
        
        if (world.getLikes().contains(player.getUniqueId())) {
            likeButton.addEnchant(Enchantment.DURABILITY, 1).addFlags(ItemFlag.HIDE_ENCHANTS);
        }
        
        setButton(13, likeButton.build(), (e) -> {
            world.addLike(player.getUniqueId());
            redraw();
        });

        ItemBuilder dislikeButton = new ItemBuilder(Material.RED_WOOL)
                .setName("§cDislike")
                .setLore("§7Dislikes: " + world.getDislikes().size());

        if (world.getDislikes().contains(player.getUniqueId())) {
            dislikeButton.addEnchant(Enchantment.DURABILITY, 1).addFlags(ItemFlag.HIDE_ENCHANTS);
        }
        
        setButton(15, dislikeButton.build(), (e) -> {
            world.addDislike(player.getUniqueId());
            redraw();
        });

        // Comments Button
        setButton(22, new ItemBuilder(Material.BOOK).setName("§eComments").setLore("§7View or add comments").build(), (e) -> {
            new CommentsMenu(player, world).open();
        });

        if (world.getOwner().equals(player.getUniqueId())) {
            setButton(26, new ItemBuilder(Material.COMMAND_BLOCK).setName("§6World Settings").build(), (e) -> {
                new WorldSettingsMenu(player, world).open();
            });
        }

        // Back Button
        setButton(18, new ItemBuilder(Material.ARROW).setName("§cBack to Browser").build(), (e) -> {
            new WorldsBrowserMenu(player).open();
        });
    }

    private SparkyBetaCreative getApp() {
        return SparkyBetaCreative.getInstance();
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