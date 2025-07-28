package ua.sparkybeta.sparkybetacreative.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.coding.CodingKeys;
import ua.sparkybeta.sparkybetacreative.coding.ValueType;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlockCategory;
import ua.sparkybeta.sparkybetacreative.coding.models.Argument;
import ua.sparkybeta.sparkybetacreative.gui.BlockArgumentsMenu;
import ua.sparkybeta.sparkybetacreative.gui.BlockConfigMenu;
import ua.sparkybeta.sparkybetacreative.gui.CodeBlockSelectionMenu;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.StoredCodeBlock;
import ua.sparkybeta.sparkybetacreative.worlds.settings.WorldMode;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Particle;

public class CodeBlockInteractListener implements Listener {

    private final Map<UUID, BlockConfigMenu> openConfigMenus = new HashMap<>();
    private final Map<UUID, BlockArgumentsMenu> openArgumentsMenus = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SparkyWorld sparkyWorld = SparkyBetaCreative.getInstance().getWorldManager().getWorld(player);
        if (sparkyWorld == null || sparkyWorld.getMode() != WorldMode.DEV) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            // Handle placing a new category block
            if (itemInHand.hasItemMeta()) {
                String categoryName = itemInHand.getItemMeta().getPersistentDataContainer().get(CodingKeys.CATEGORY_KEY, PersistentDataType.STRING);
                if (categoryName != null) {
                    Block targetBlock = event.getClickedBlock() != null ? event.getClickedBlock().getRelative(event.getBlockFace()) : player.getTargetBlock(null, 5).getRelative(event.getBlockFace());
                    placeNewBlock(player, targetBlock, CodeBlockCategory.valueOf(categoryName));
                    event.setCancelled(true);
                    return;
                }

                // Handle linking blocks
                String linkerStartId = itemInHand.getItemMeta().getPersistentDataContainer().get(CodingKeys.LINKER_KEY, PersistentDataType.STRING);
                if (linkerStartId != null && event.getClickedBlock() != null) {
                    StoredCodeBlock startBlock = findBlockById(UUID.fromString(linkerStartId), sparkyWorld);
                    StoredCodeBlock endBlock = sparkyWorld.getCodeBlock(event.getClickedBlock().getLocation());
                    if (startBlock != null && endBlock != null && !startBlock.getId().equals(endBlock.getId())) {
                        startBlock.getNextBlocks().add(endBlock.getId());
                        player.getInventory().setItemInMainHand(null); // Consume linker
                        MessageUtils.sendSuccess(player, "Blocks linked successfully!");
                        // Visual effect
                        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, startBlock.getLocation().toCenterLocation(), 30);
                        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, endBlock.getLocation().toCenterLocation(), 30);
                    } else {
                        MessageUtils.sendError(player, "Invalid target block.");
                    }
                    event.setCancelled(true);
                    return;
                }
            }

            // Handle interacting with an existing code block
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                StoredCodeBlock storedBlock = sparkyWorld.getCodeBlock(event.getClickedBlock().getLocation());
                if (storedBlock != null) {
                    openConfigMenu(player, sparkyWorld, storedBlock);
                    event.setCancelled(true);
                }
            }
        }
    }
    
    private StoredCodeBlock findBlockById(UUID id, SparkyWorld world) {
        return world.getCodeBlocks().values().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    private void placeNewBlock(Player player, Block block, CodeBlockCategory category) {
        // Find a default block type for the category
        CodeBlock defaultType = CodeBlock.getDefaultForCategory(category);
        if(defaultType == null) return; // Should not happen

        block.setType(defaultType.getMaterial());
        StoredCodeBlock newStoredBlock = new StoredCodeBlock(block.getLocation(), defaultType);
        SparkyWorld sparkyWorld = SparkyBetaCreative.getInstance().getWorldManager().getWorld(player);
        if(sparkyWorld != null) {
            sparkyWorld.addCodeBlock(newStoredBlock);
            updateSign(newStoredBlock);
            openConfigMenu(player, sparkyWorld, newStoredBlock);
        }
    }
    
    private void openConfigMenu(Player player, SparkyWorld world, StoredCodeBlock block) {
        BlockConfigMenu menu = new BlockConfigMenu(player, world, block);
        menu.open();
        openConfigMenus.put(player.getUniqueId(), menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (Component.text(BlockConfigMenu.MENU_TITLE).equals(event.getView().title())) {
            handleConfigMenuClick(event);
        }
    }

    private void handleSelectionMenuClick(InventoryClickEvent event) {
        // This part is for changing the block type from the config menu in the future.
        // For now, we are creating blocks directly.
        event.setCancelled(true);
    }
    
    private void handleConfigMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        BlockConfigMenu menu = openConfigMenus.get(player.getUniqueId());
        if (menu == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        switch (clickedItem.getType()) {
            case BARREL:
                player.closeInventory();
                BlockArgumentsMenu argsMenu = new BlockArgumentsMenu(player, menu.getCodeBlock());
                argsMenu.open();
                openArgumentsMenus.put(player.getUniqueId(), argsMenu);
                break;
            case REPEATER:
                player.closeInventory();
                ItemStack linker = new ItemBuilder(Material.BLAZE_ROD)
                        .setName("§6§lLinker Tool")
                        .setLore("§7Right-click a code block to start linking.", "§7Right-click another to finish.")
                        .build();
                linker.editMeta(meta -> {
                    meta.getPersistentDataContainer().set(CodingKeys.LINKER_KEY, PersistentDataType.STRING, menu.getCodeBlock().getId().toString());
                });
                player.getInventory().addItem(linker);
                MessageUtils.sendSuccess(player, "You have been given the Linker Tool.");
                break;

            case REDSTONE_BLOCK:
                // Delete the block
                player.closeInventory();
                StoredCodeBlock blockToDelete = menu.getCodeBlock();
                blockToDelete.getLocation().getBlock().setType(Material.AIR);
                menu.getSparkyWorld().removeCodeBlock(blockToDelete.getLocation());
                // Also remove sign
                updateSign(blockToDelete, true);
                MessageUtils.sendSuccess(player, "Block deleted.");
                break;
            default:
                break;
        }
    }
    
    @EventHandler
    public void onArgumentsMenuClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (openArgumentsMenus.containsKey(player.getUniqueId())) {
            BlockArgumentsMenu menu = openArgumentsMenus.remove(player.getUniqueId());
            StoredCodeBlock codeBlock = menu.getCodeBlock();
            codeBlock.getArguments().clear();

            for (ItemStack item : event.getInventory().getContents()) {
                if (item != null && item.hasItemMeta()) {
                    String valueTypeName = item.getItemMeta().getPersistentDataContainer().get(CodingKeys.VALUE_TYPE, PersistentDataType.STRING);
                    if (valueTypeName != null) {
                        ValueType type = ValueType.valueOf(valueTypeName);
                        // This is a simplified parsing. A more robust solution is needed.
                        Object value = item.getItemMeta().displayName(); 
                        codeBlock.getArguments().add(new Argument(type, value));
                    }
                }
            }
            MessageUtils.sendSuccess(player, "Arguments saved!");
        }
    }
    
    private void updateSign(StoredCodeBlock storedBlock) {
        updateSign(storedBlock, false);
    }

    private void updateSign(StoredCodeBlock storedBlock, boolean remove) {
        Block block = storedBlock.getLocation().getBlock();
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block potentialSignBlock = block.getRelative(face);
            if (potentialSignBlock.getState() instanceof Sign || remove) {
                potentialSignBlock.setType(Material.AIR);
                if(remove) continue;

                potentialSignBlock.setType(Material.OAK_WALL_SIGN, false);
                if (potentialSignBlock.getState() instanceof Sign sign) {
                    WallSign signData = (WallSign) sign.getBlockData();
                    signData.setFacing(face);
                    sign.setBlockData(signData);

                    CodeBlock type = storedBlock.getType();
                    sign.getSide(org.bukkit.block.sign.Side.FRONT).line(0, net.kyori.adventure.text.Component.text("§b["+type.getCategory().getDisplayName()+"]"));
                    sign.getSide(org.bukkit.block.sign.Side.FRONT).line(1, net.kyori.adventure.text.Component.text("§f"+type.getDisplayName()));
                    sign.update(true);
                    return;
                }
            }
        }
        
        // If no sign was found and we are not removing, place a new one
        if(!remove) {
             for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                Block potentialSignBlock = block.getRelative(face);
                if (potentialSignBlock.getType().isAir()) {
                    potentialSignBlock.setType(Material.OAK_WALL_SIGN, false);
                     if (potentialSignBlock.getState() instanceof Sign sign) {
                        WallSign signData = (WallSign) sign.getBlockData();
                        signData.setFacing(face);
                        sign.setBlockData(signData);
                        CodeBlock type = storedBlock.getType();
                        sign.getSide(org.bukkit.block.sign.Side.FRONT).line(0, net.kyori.adventure.text.Component.text("§b["+type.getCategory().getDisplayName()+"]"));
                        sign.getSide(org.bukkit.block.sign.Side.FRONT).line(1, net.kyori.adventure.text.Component.text("§f"+type.getDisplayName()));
                        sign.update(true);
                        return;
                    }
                }
             }
        }
    }
} 