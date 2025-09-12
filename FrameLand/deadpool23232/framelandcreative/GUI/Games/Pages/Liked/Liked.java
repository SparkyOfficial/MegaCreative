/*     */ package deadpool23232.framelandcreative.GUI.Games.Pages.Liked;
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
/*     */ public class Liked
/*     */ {
/*  25 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*     */   public static void open(Player player) {
/*  27 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - понравившееся"));
/*     */     
/*  29 */     String playeruuid = player.getUniqueId().toString();
/*  30 */     List<String> list = new ArrayList<>(PlayerData.get().getStringList(playeruuid + ".liked"));
/*  31 */     for (int i = 10; i < 44; i++) {
/*  32 */       if (i == 17) i = 19; 
/*  33 */       if (i == 26) i = 28; 
/*  34 */       if (i == 35) i = 37; 
/*  35 */       if (!list.isEmpty()) {
/*  36 */         String id = list.get(0);
/*  37 */         String name = DataConfig.get().getString("registered-worlds." + id + ".name");
/*     */         
/*  39 */         ItemStack item = RandomSkull.skull();
/*  40 */         String opened = "&cЗакрыт";
/*  41 */         if (DataConfig.get().getBoolean("registered-worlds." + id + ".opened"))
/*  42 */           opened = "&aОткрыт"; 
/*  43 */         String uuidString = DataConfig.get().getString("registered-worlds." + id + ".author");
/*  44 */         UUID uuid = UUID.fromString(uuidString);
/*  45 */         OfflinePlayer player1 = Bukkit.getOfflinePlayer(uuid);
/*  46 */         String prefix = PlayerData.get().getString(uuid + ".prefix");
/*  47 */         if (player1.isOnline()) {
/*  48 */           prefix = PlaceholderAPI.setPlaceholders(player1, "%vault_prefix%");
/*     */         }
/*  50 */         if (prefix == null) {
/*  51 */           prefix = "";
/*     */         }
/*  53 */         List<String> listt = new ArrayList<>();
/*  54 */         String likes = Integer.toString(WorldData.get().getInt("worlds." + id + ".liked"));
/*  55 */         String uniq = Integer.toString(WorldData.get().getInt("worlds." + id + ".uniquePlayers"));
/*  56 */         for (String line : config.getStringList("for-every-world.desc.part1")) {
/*  57 */           listt.add(FrameLandCreative.Color(line
/*  58 */                 .replace("#name#", name)
/*  59 */                 .replace("#author#", prefix + player1.getName())
/*  60 */                 .replace("#id#", DataConfig.get().getString("registered-worlds." + id + ".id"))
/*  61 */                 .replace("#opened#", opened)
/*  62 */                 .replace("#players#", uniq)
/*  63 */                 .replace("#likes#", likes)));
/*     */         }
/*     */         
/*  66 */         List<String> list1 = new ArrayList<>();
/*  67 */         for (String line : DataConfig.get().getStringList("registered-worlds." + id + ".description")) {
/*  68 */           list1.add(FrameLandCreative.Color("&f " + line));
/*     */         }
/*  70 */         List<String> list2 = new ArrayList<>();
/*  71 */         for (String line : config.getStringList("for-every-world.desc.part2")) {
/*  72 */           list2.add(FrameLandCreative.Color(line
/*  73 */                 .replace("#name#", name)
/*  74 */                 .replace("#author#", prefix + player1.getName())
/*  75 */                 .replace("#id#", DataConfig.get().getString("registered-worlds." + id + ".id"))
/*  76 */                 .replace("#opened#", opened)
/*  77 */                 .replace("#players#", uniq)
/*  78 */                 .replace("#likes#", likes)));
/*     */         }
/*     */         
/*  81 */         List<String> description = new ArrayList<>();
/*  82 */         description.addAll(listt);
/*  83 */         description.addAll(list1);
/*  84 */         description.addAll(list2);
/*     */         
/*  86 */         ItemMeta itemMeta = item.getItemMeta();
/*  87 */         itemMeta.setDisplayName(FrameLandCreative.Color(name));
/*  88 */         itemMeta.setLore(description);
/*  89 */         item.setItemMeta(itemMeta);
/*  90 */         gui.setItem(i, item);
/*  91 */         list.remove(0);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/*  96 */     ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/*  97 */     ItemMeta glass1 = glass.getItemMeta();
/*  98 */     glass1.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/*  99 */     glass1.setDisplayName(FrameLandCreative.Color("&7"));
/* 100 */     glass.setItemMeta(glass1);
/*     */ 
/*     */     
/* 103 */     gui.setItem(0, glass); gui.setItem(1, glass); gui.setItem(2, glass); gui.setItem(3, glass); gui.setItem(4, glass);
/* 104 */     gui.setItem(5, glass); gui.setItem(6, glass); gui.setItem(7, glass); gui.setItem(8, glass); gui.setItem(9, glass);
/* 105 */     gui.setItem(18, glass); gui.setItem(27, glass); gui.setItem(36, glass); gui.setItem(17, glass); gui.setItem(26, glass);
/* 106 */     gui.setItem(35, glass); gui.setItem(44, glass); gui.setItem(49, glass);
/* 107 */     gui.setItem(45, glass); gui.setItem(46, glass); gui.setItem(47, glass); gui.setItem(48, glass);
/* 108 */     gui.setItem(50, glass); gui.setItem(51, glass); gui.setItem(52, glass); gui.setItem(53, glass);
/*     */     
/* 110 */     player.openInventory(gui);
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\Liked\Liked.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */