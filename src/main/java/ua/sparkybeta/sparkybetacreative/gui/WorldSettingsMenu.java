package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.util.InputType;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;

public class WorldSettingsMenu extends AbstractMenu {

    private final SparkyWorld world;

    public WorldSettingsMenu(Player player, SparkyWorld world) {
        super(player, 45, "World Settings: " + world.getDisplayName());
        this.world = world;
        redraw();
    }

    public void redraw() {
        inventory.clear();

        // Info and Text Settings
        setButton(10, new ItemBuilder(Material.NAME_TAG).setName("§eChange Display Name").setLore("§7Current: " + world.getDisplayName()).build(), this::changeDisplayName);
        setButton(11, new ItemBuilder(Material.PAPER).setName("§eChange Description").setLore("§7Current: " + world.getDescription()).build(), this::changeDescription);
        setButton(12, new ItemBuilder(Material.EMERALD).setName("§eChange Custom ID").setLore("§7Current: " + world.getCustomId()).build(), this::changeCustomId);
        
        // Boolean Flags
        setButton(19, createToggleItem("§bPublic Access", world.getSettings().isPublic()), this::togglePublic);
        setButton(20, createToggleItem("§cPVP", world.getSettings().isPvpAllowed()), this::togglePvp);
        setButton(21, createToggleItem("§cExplosions", world.getSettings().isExplosionsAllowed()), this::toggleExplosions);
        setButton(22, createToggleItem("§cMob Spawning", world.getSettings().isMobSpawningAllowed()), this::toggleMobSpawning);
        
        // Trusted Players
        setButton(28, new ItemBuilder(Material.PLAYER_HEAD).setName("§dManage Builders").build(), (e) -> {
            new TrustedPlayersMenu(player, world, ua.sparkybeta.sparkybetacreative.util.TrustedType.BUILDER).open();
        });
        setButton(29, new ItemBuilder(Material.COMMAND_BLOCK).setName("§dManage Coders").build(), (e) -> {
            new TrustedPlayersMenu(player, world, ua.sparkybeta.sparkybetacreative.util.TrustedType.CODER).open();
        });

        // Back Button
        setButton(36, new ItemBuilder(Material.BARRIER).setName("§cBack").build(), (e) -> new WorldActionMenu(player, world).open());
    }
    
    private void changeDisplayName(org.bukkit.event.inventory.InventoryClickEvent e) {
        startChatInput(InputType.WORLD_NAME, "Enter the new display name in chat.");
    }
    
    private void changeDescription(org.bukkit.event.inventory.InventoryClickEvent e) {
        startChatInput(InputType.WORLD_DESCRIPTION, "Enter the new description in chat.");
    }
    
    private void changeCustomId(org.bukkit.event.inventory.InventoryClickEvent e) {
        startChatInput(InputType.WORLD_ID, "Enter the new custom ID in chat.");
    }

    private void togglePublic(org.bukkit.event.inventory.InventoryClickEvent e) {
        world.getSettings().setPublic(!world.getSettings().isPublic());
        redraw();
    }
    
    private void togglePvp(org.bukkit.event.inventory.InventoryClickEvent e) {
        world.getSettings().setPvpAllowed(!world.getSettings().isPvpAllowed());
        redraw();
    }
    
    private void toggleExplosions(org.bukkit.event.inventory.InventoryClickEvent e) {
        world.getSettings().setExplosionsAllowed(!world.getSettings().isExplosionsAllowed());
        redraw();
    }

    private void toggleMobSpawning(org.bukkit.event.inventory.InventoryClickEvent e) {
        world.getSettings().setMobSpawningAllowed(!world.getSettings().isMobSpawningAllowed());
        redraw();
    }

    private void startChatInput(InputType type, String prompt) {
        player.closeInventory();
        SparkyBetaCreative.getInstance().getChatInputManager().startInput(player.getUniqueId(), world, type);
        player.sendMessage("§a" + prompt + " Type 'cancel' to abort.");
    }

    private ItemStack createToggleItem(String name, boolean value) {
        return new ItemBuilder(value ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName(name)
                .setLore(value ? "§aEnabled" : "§cDisabled")
                .build();
    }
} 