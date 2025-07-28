package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.util.InputType;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import ua.sparkybeta.sparkybetacreative.util.TrustedType;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TrustedPlayersMenu extends AbstractMenu {
    private final SparkyWorld world;
    private final TrustedType type;

    public TrustedPlayersMenu(Player player, SparkyWorld world, TrustedType type) {
        super(player, 54, "Manage " + (type == TrustedType.BUILDER ? "Builders" : "Coders"));
        this.world = world;
        this.type = type;
        redraw();
    }

    public void redraw() {
        inventory.clear();
        Set<UUID> trustedUuids = getTrustedSet();

        // Display heads
        int slot = 0;
        for (UUID uuid : trustedUuids) {
            if (slot >= 45) break;
            OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(uuid);
            ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§c" + (trustedPlayer.getName() != null ? trustedPlayer.getName() : "Unknown"))
                    .setLore("§eClick to remove")
                    .build();
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(trustedPlayer);
            head.setItemMeta(meta);
            setButton(slot, head, (e) -> {
                getTrustedSet().remove(uuid);
                redraw();
            });
            slot++;
        }

        // Back button
        setButton(49, new ItemBuilder(Material.BARRIER).setName("§cBack").build(),
                (e) -> new WorldSettingsMenu(player, world).open());

        // Add player button
        setButton(53, new ItemBuilder(Material.NETHER_STAR).setName("§aAdd Player").build(), (e) -> {
            player.closeInventory();
            InputType inputType = (type == TrustedType.BUILDER) ? InputType.ADD_TRUSTED_BUILDER : InputType.ADD_TRUSTED_CODER;
            SparkyBetaCreative.getInstance().getChatInputManager().startInput(player.getUniqueId(), world, inputType);
            player.sendMessage("§aEnter the name of the player to add. Type 'cancel' to abort.");
        });
    }

    private Set<UUID> getTrustedSet() {
        return (type == TrustedType.BUILDER) ? world.getSettings().getBuildTrusted() : world.getSettings().getCodeTrusted();
    }
} 