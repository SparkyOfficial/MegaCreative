/*     */ package deadpool23232.framelandcreative.GUI.Games.Pages.Latest;
/*     */ 
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import deadpool23232.framelandcreative.Configs.PlayerData;
/*     */ import deadpool23232.framelandcreative.Configs.WorldData;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Functions.Skull.RandomSkull;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.UUID;
/*     */ import me.clip.placeholderapi.PlaceholderAPI;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.OfflinePlayer;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.InventoryHolder;
/*     */ import org.bukkit.inventory.ItemFlag;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ 
/*     */ public class Last
/*     */ {
/*  26 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*     */   public static void open(Player player) {
/*  28 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - последние"));
/*     */     
/*  30 */     String playeruuid = player.getUniqueId().toString();
/*  31 */     List<String> list = new ArrayList<>(PlayerData.get().getStringList(playeruuid + ".latest"));
/*  32 */     for (int i = 10; i < 44; i++) {
/*  33 */       if (i == 17) i = 19; 
/*  34 */       if (i == 26) i = 28; 
/*  35 */       if (i == 35) i = 37; 
/*  36 */       if (!list.isEmpty()) {
/*  37 */         String id = list.get(0);
/*  38 */         String name = DataConfig.get().getString("registered-worlds." + id + ".name");
/*     */         
/*  40 */         ItemStack item = RandomSkull.skull();
/*  41 */         String opened = "&cЗакрыт";
/*  42 */         if (DataConfig.get().getBoolean("registered-worlds." + id + ".opened"))
/*  43 */           opened = "&aОткрыт"; 
/*  44 */         String uuidString = DataConfig.get().getString("registered-worlds." + id + ".author");
/*  45 */         UUID uuid = UUID.fromString(uuidString);
/*  46 */         OfflinePlayer player1 = Bukkit.getOfflinePlayer(uuid);
/*  47 */         String prefix = PlayerData.get().getString(uuid + ".prefix");
/*  48 */         if (player1.isOnline()) {
/*  49 */           prefix = PlaceholderAPI.setPlaceholders(player1, "%vault_prefix%");
/*     */         }
/*  51 */         if (prefix == null) {
/*  52 */           prefix = "";
/*     */         }
/*  54 */         List<String> listt = new ArrayList<>();
/*  55 */         String likes = Integer.toString(WorldData.get().getInt("worlds." + id + ".liked"));
/*  56 */         String uniq = Integer.toString(WorldData.get().getInt("worlds." + id + ".uniquePlayers"));
/*  57 */         for (String line : config.getStringList("for-every-world.desc.part1")) {
/*  58 */           listt.add(FrameLandCreative.Color(line
/*  59 */                 .replace("#name#", name)
/*  60 */                 .replace("#author#", prefix + player1.getName())
/*  61 */                 .replace("#id#", DataConfig.get().getString("registered-worlds." + id + ".id"))
/*  62 */                 .replace("#opened#", opened)
/*  63 */                 .replace("#players#", uniq)
/*  64 */                 .replace("#likes#", likes)));
/*     */         }
/*     */         
/*  67 */         List<String> list1 = new ArrayList<>();
/*  68 */         for (String line : DataConfig.get().getStringList("registered-worlds." + id + ".description")) {
/*  69 */           list1.add(FrameLandCreative.Color("&f " + line));
/*     */         }
/*  71 */         List<String> list2 = new ArrayList<>();
/*  72 */         for (String line : config.getStringList("for-every-world.desc.part2")) {
/*  73 */           list2.add(FrameLandCreative.Color(line
/*  74 */                 .replace("#name#", name)
/*  75 */                 .replace("#author#", prefix + player1.getName())
/*  76 */                 .replace("#id#", DataConfig.get().getString("registered-worlds." + id + ".id"))
/*  77 */                 .replace("#opened#", opened)
/*  78 */                 .replace("#players#", uniq)
/*  79 */                 .replace("#likes#", likes)));
/*     */         }
/*     */         
/*  82 */         List<String> description = new ArrayList<>();
/*  83 */         description.addAll(listt);
/*  84 */         description.addAll(list1);
/*  85 */         description.addAll(list2);
/*     */         
/*  87 */         ItemMeta itemMeta = item.getItemMeta();
/*  88 */         itemMeta.setDisplayName(FrameLandCreative.Color(name));
/*  89 */         itemMeta.setLore(description);
/*  90 */         item.setItemMeta(itemMeta);
/*  91 */         gui.setItem(i, item);
/*  92 */         list.remove(0);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/*  97 */     ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/*  98 */     ItemMeta glass1 = glass.getItemMeta();
/*  99 */     glass1.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/* 100 */     glass1.setDisplayName(FrameLandCreative.Color("&7"));
/* 101 */     glass.setItemMeta(glass1);
/*     */ 
/*     */     
/* 104 */     gui.setItem(0, glass); gui.setItem(1, glass); gui.setItem(2, glass); gui.setItem(3, glass); gui.setItem(4, glass);
/* 105 */     gui.setItem(5, glass); gui.setItem(6, glass); gui.setItem(7, glass); gui.setItem(8, glass); gui.setItem(9, glass);
/* 106 */     gui.setItem(18, glass); gui.setItem(27, glass); gui.setItem(36, glass); gui.setItem(17, glass); gui.setItem(26, glass);
/* 107 */     gui.setItem(35, glass); gui.setItem(44, glass); gui.setItem(49, glass);
/* 108 */     gui.setItem(45, glass); gui.setItem(46, glass); gui.setItem(47, glass); gui.setItem(48, glass);
/* 109 */     gui.setItem(50, glass); gui.setItem(51, glass); gui.setItem(52, glass); gui.setItem(53, glass);
/*     */     
/* 111 */     player.openInventory(gui);
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\Latest\Last.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */