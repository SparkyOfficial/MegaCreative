package ua.sparkycreative.worldsystem.logic;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LogicBlockConfigGUI implements Listener {
    private static final String GUI_TITLE = "§bНастройка блока";
    private final LogicBlock block;
    private static final java.util.Map<Player, LogicBlock> linking = new java.util.HashMap<>();

    public LogicBlockConfigGUI(LogicBlock block) {
        this.block = block;
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, GUI_TITLE);
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName("§eТип: " + block.getType());
        info.setItemMeta(meta);
        inv.setItem(4, info);
        // Параметры для IF
        if (block.getType() == LogicBlockType.IF) {
            ItemStack cond = new ItemStack(Material.COMPASS);
            ItemMeta condMeta = cond.getItemMeta();
            condMeta.setDisplayName("§bУсловие: " + block.getParams().getOrDefault("condition", "нет"));
            cond.setItemMeta(condMeta);
            inv.setItem(2, cond);
        }
        // Параметры для ACTION
        if (block.getType() == LogicBlockType.ACTION) {
            ItemStack act = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta actMeta = act.getItemMeta();
            actMeta.setDisplayName("§bДействие: " + block.getParams().getOrDefault("action", "нет"));
            act.setItemMeta(actMeta);
            inv.setItem(6, act);
        }
        // Параметры для EVENT
        if (block.getType() == LogicBlockType.EVENT) {
            ItemStack ev = new ItemStack(Material.BELL);
            ItemMeta evMeta = ev.getItemMeta();
            evMeta.setDisplayName("§bСобытие: " + block.getParams().getOrDefault("event", "по клику"));
            ev.setItemMeta(evMeta);
            inv.setItem(2, ev);
        }
        // Кнопка связей
        ItemStack link = new ItemStack(Material.CHAIN);
        ItemMeta linkMeta = link.getItemMeta();
        linkMeta.setDisplayName("§dСвязи: " + block.getOutputs().size());
        link.setItemMeta(linkMeta);
        inv.setItem(8, link);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!e.getView().getTitle().equals(GUI_TITLE)) return;
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        String name = e.getCurrentItem().getItemMeta().getDisplayName();
        if (block.getType() == LogicBlockType.IF && name.startsWith("§bУсловие")) {
            // Пример выбора условия (циклично)
            String[] conds = {"игрок на земле", "игрок в воздухе", "игрок в воде"};
            int idx = 0;
            String current = (String) block.getParams().getOrDefault("condition", conds[0]);
            for (int i = 0; i < conds.length; i++) if (conds[i].equals(current)) idx = i;
            idx = (idx + 1) % conds.length;
            block.getParams().put("condition", conds[idx]);
            player.sendMessage("§aУсловие установлено: " + conds[idx]);
            open(player);
        }
        if (block.getType() == LogicBlockType.ACTION && name.startsWith("§bДействие")) {
            // Пример выбора действия (циклично)
            String[] acts = {"выдать алмаз", "поджечь", "подпрыгнуть"};
            int idx = 0;
            String current = (String) block.getParams().getOrDefault("action", acts[0]);
            for (int i = 0; i < acts.length; i++) if (acts[i].equals(current)) idx = i;
            idx = (idx + 1) % acts.length;
            block.getParams().put("action", acts[idx]);
            player.sendMessage("§aДействие установлено: " + acts[idx]);
            open(player);
        }
        if (block.getType() == LogicBlockType.EVENT && name.startsWith("§bСобытие")) {
            // Пример выбора события (циклично)
            String[] evs = {"по клику", "при входе", "по таймеру"};
            int idx = 0;
            String current = (String) block.getParams().getOrDefault("event", evs[0]);
            for (int i = 0; i < evs.length; i++) if (evs[i].equals(current)) idx = i;
            idx = (idx + 1) % evs.length;
            block.getParams().put("event", evs[idx]);
            player.sendMessage("§aСобытие установлено: " + evs[idx]);
            open(player);
        }
        if (name.startsWith("§dСвязи")) {
            // Включить режим выбора блока для связи
            linking.put(player, block);
            player.closeInventory();
            player.sendMessage("§bКликните ПКМ по другому блоку для создания/удаления связи.");
        }
    }

    public static LogicBlock getLinking(Player player) {
        return linking.get(player);
    }
    public static void clearLinking(Player player) {
        linking.remove(player);
    }
} 