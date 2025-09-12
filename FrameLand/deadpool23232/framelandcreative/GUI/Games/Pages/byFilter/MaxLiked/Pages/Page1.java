/*     */ package deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages;
/*     */ 
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import deadpool23232.framelandcreative.Configs.PlayerData;
/*     */ import deadpool23232.framelandcreative.Configs.WorldData;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Functions.Skull.RandomSkull;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Organizer;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Page1
/*     */ {
/*     */   public static HashMap<Player, ItemStack[]> page1Map;
/*     */   public static boolean page1FirstTime = true;
/*  35 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*     */   public static void open(Player player) {
/*  37 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/*     */     
/*  39 */     if (Organizer.pages.intValue() >= 1) {
/*  40 */       Integer integer = Organizer.pages; Organizer.pages = Integer.valueOf(Organizer.pages.intValue() - 1);
/*  41 */       int index = 0;
/*  42 */       for (int i = 28; i < 44; i++) {
/*  43 */         if (i == 35) {
/*  44 */           i = 37;
/*     */         }
/*  46 */         if (!Organizer.stringList.isEmpty()) {
/*  47 */           String string = DataConfig.get().getString("registered-worlds." + (String)Organizer.stringList.get(index) + ".name");
/*  48 */           ItemStack item = RandomSkull.skull();
/*  49 */           String id = Organizer.stringList.get(index);
/*  50 */           String opened = "&cЗакрыт";
/*  51 */           if (DataConfig.get().getBoolean("registered-worlds." + id + ".opened"))
/*  52 */             opened = "&aОткрыт"; 
/*  53 */           String uuidString = DataConfig.get().getString("registered-worlds." + id + ".author");
/*  54 */           UUID uuid = UUID.fromString(uuidString);
/*  55 */           OfflinePlayer player1 = Bukkit.getOfflinePlayer(uuid);
/*  56 */           String prefix = PlayerData.get().getString(uuid + ".prefix");
/*  57 */           if (player1.isOnline()) {
/*  58 */             prefix = PlaceholderAPI.setPlaceholders(player1, "%vault_prefix%");
/*     */           }
/*  60 */           if (prefix == null) {
/*  61 */             prefix = "";
/*     */           }
/*  63 */           List<String> list = new ArrayList<>();
/*  64 */           String likes = Integer.toString(WorldData.get().getInt("worlds." + id + ".liked"));
/*  65 */           String uniq = Integer.toString(WorldData.get().getInt("worlds." + id + ".uniquePlayers"));
/*  66 */           for (String line : config.getStringList("for-every-world.desc.part1")) {
/*  67 */             list.add(FrameLandCreative.Color(line
/*  68 */                   .replace("#name#", string)
/*  69 */                   .replace("#author#", prefix + player1.getName())
/*  70 */                   .replace("#id#", DataConfig.get().getString("registered-worlds." + id + ".id"))
/*  71 */                   .replace("#opened#", opened)
/*  72 */                   .replace("#players#", uniq)
/*  73 */                   .replace("#likes#", likes)));
/*     */           }
/*     */           
/*  76 */           List<String> list1 = new ArrayList<>();
/*  77 */           for (String line : DataConfig.get().getStringList("registered-worlds." + id + ".description")) {
/*  78 */             list1.add(FrameLandCreative.Color("&f " + line));
/*     */           }
/*  80 */           List<String> list2 = new ArrayList<>();
/*  81 */           for (String line : config.getStringList("for-every-world.desc.part2")) {
/*  82 */             list2.add(FrameLandCreative.Color(line
/*  83 */                   .replace("#name#", string)
/*  84 */                   .replace("#author#", prefix + player1.getName())
/*  85 */                   .replace("#id#", DataConfig.get().getString("registered-worlds." + id + ".id"))
/*  86 */                   .replace("#opened#", opened)
/*  87 */                   .replace("#players#", uniq)
/*  88 */                   .replace("#likes#", likes)));
/*     */           }
/*     */           
/*  91 */           List<String> description = new ArrayList<>();
/*  92 */           description.addAll(list);
/*  93 */           description.addAll(list1);
/*  94 */           description.addAll(list2);
/*     */           
/*  96 */           ItemMeta itemMeta = item.getItemMeta();
/*  97 */           itemMeta.setDisplayName(FrameLandCreative.Color(string));
/*  98 */           itemMeta.setLore(description);
/*  99 */           item.setItemMeta(itemMeta);
/* 100 */           gui.setItem(i, item);
/* 101 */           Organizer.stringList.remove(index);
/*     */         } 
/*     */       } 
/*     */     } 
/*     */     
/* 106 */     ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 107 */     ItemMeta glass1 = glass.getItemMeta();
/* 108 */     glass1.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/* 109 */     glass1.setDisplayName(FrameLandCreative.Color("&7"));
/* 110 */     glass.setItemMeta(glass1);
/*     */     
/* 112 */     ItemStack glass2 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 113 */     ItemMeta glass3 = glass2.getItemMeta();
/* 114 */     glass3.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/* 115 */     glass3.setDisplayName(FrameLandCreative.Color("&7"));
/* 116 */     glass2.setItemMeta(glass3);
/*     */ 
/*     */     
/* 119 */     ItemStack maxLiked = RandomSkull.getSkull("http://textures.minecraft.net/texture/e03ccf028c61c2dff4437578ce71e1550478185b54fc94c5927f0f06f966273a");
/* 120 */     ItemMeta maxLikedMeta = maxLiked.getItemMeta();
/* 121 */     maxLikedMeta.setDisplayName(FrameLandCreative.Color(config.getString("maxLiked.name")));
/* 122 */     List<String> maxLikedLore = new ArrayList<>();
/* 123 */     for (String line : config.getStringList("maxLiked.description")) {
/* 124 */       maxLikedLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 126 */     maxLikedMeta.setLore(maxLikedLore);
/*     */     
/* 128 */     ItemStack maxUniq = RandomSkull.getSkull("http://textures.minecraft.net/texture/676dcb274e2e23149f472781605b7c6f839931a4f1d2edbd1ff5463ab7c41246");
/* 129 */     ItemMeta maxUniqMeta = maxUniq.getItemMeta();
/* 130 */     maxUniqMeta.setDisplayName(FrameLandCreative.Color(config.getString("maxUniq.name")));
/* 131 */     List<String> maxUniqLore = new ArrayList<>();
/* 132 */     for (String line : config.getStringList("maxUniq.description")) {
/* 133 */       maxUniqLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 135 */     maxUniqMeta.setLore(maxUniqLore);
/*     */     
/* 137 */     ItemStack Newest = RandomSkull.getSkull("http://textures.minecraft.net/texture/e5348ab6eedc3c76bf71d4395f0607829835ac8b4dc37ab31d22931ad255e454");
/* 138 */     ItemMeta NewestMeta = Newest.getItemMeta();
/* 139 */     NewestMeta.setDisplayName(FrameLandCreative.Color(config.getString("newest.name")));
/* 140 */     List<String> NewestLore = new ArrayList<>();
/* 141 */     for (String line : config.getStringList("newest.description")) {
/* 142 */       NewestLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 144 */     NewestMeta.setLore(NewestLore);
/*     */ 
/*     */     
/* 147 */     ItemStack likedItem = RandomSkull.getSkull("http://textures.minecraft.net/texture/fbefc867d847ab95e775e04aa5383b1670d38420a827b162d898b8f7ec148ec");
/* 148 */     ItemMeta likedMeta = likedItem.getItemMeta();
/* 149 */     likedMeta.setDisplayName(FrameLandCreative.Color(config.getString("liked.name")));
/* 150 */     List<String> likedLore = new ArrayList<>();
/* 151 */     for (String line : config.getStringList("liked.description")) {
/* 152 */       likedLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 154 */     likedMeta.setLore(likedLore);
/*     */     
/* 156 */     ItemStack ownItem = RandomSkull.getSkull("http://textures.minecraft.net/texture/15ad93d56546f12d5356effcbc6ec4c87ba245d81e1662c4b830f7d298e9");
/* 157 */     ItemMeta ownMeta = ownItem.getItemMeta();
/* 158 */     ownMeta.setDisplayName(FrameLandCreative.Color(config.getString("own.name")));
/* 159 */     List<String> ownLore = new ArrayList<>();
/* 160 */     for (String line : config.getStringList("own.description")) {
/* 161 */       ownLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 163 */     ownMeta.setLore(ownLore);
/*     */     
/* 165 */     ItemStack latestItem = RandomSkull.getSkull("http://textures.minecraft.net/texture/74133f6ac3be2e2499a784efadcfffeb9ace025c3646ada67f3414e5ef3394");
/* 166 */     ItemMeta latestMeta = latestItem.getItemMeta();
/* 167 */     latestMeta.setDisplayName(FrameLandCreative.Color(config.getString("latest.name")));
/* 168 */     List<String> latestLore = new ArrayList<>();
/* 169 */     for (String line : config.getStringList("latest.description")) {
/* 170 */       latestLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 172 */     latestMeta.setLore(latestLore);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 180 */     ItemStack front = new ItemStack(Material.ARROW, 2);
/* 181 */     ItemMeta frontMeta = front.getItemMeta();
/* 182 */     frontMeta.setDisplayName(FrameLandCreative.Color(config.getString("gameList.front")));
/* 183 */     frontMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/*     */     
/* 185 */     Newest.setItemMeta(NewestMeta);
/* 186 */     maxLiked.setItemMeta(maxLikedMeta);
/* 187 */     maxUniq.setItemMeta(maxUniqMeta);
/* 188 */     likedItem.setItemMeta(likedMeta);
/* 189 */     ownItem.setItemMeta(ownMeta);
/*     */     
/* 191 */     front.setItemMeta(frontMeta);
/* 192 */     latestItem.setItemMeta(latestMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 197 */     gui.setItem(3, maxUniq);
/* 198 */     gui.setItem(4, maxLiked);
/* 199 */     gui.setItem(5, Newest);
/*     */     
/* 201 */     gui.setItem(0, likedItem);
/* 202 */     gui.setItem(1, latestItem);
/*     */     
/* 204 */     gui.setItem(8, ownItem);
/*     */ 
/*     */     
/* 207 */     gui.setItem(44, front);
/*     */     
/* 209 */     gui.setItem(9, glass); gui.setItem(10, glass); gui.setItem(11, glass); gui.setItem(12, glass); gui.setItem(13, glass2);
/* 210 */     gui.setItem(14, glass); gui.setItem(15, glass); gui.setItem(16, glass); gui.setItem(17, glass);
/*     */     
/* 212 */     player.openInventory(gui);
/* 213 */     page1Map = (HashMap)new HashMap<>();
/* 214 */     page1Map.put(player, gui.getContents());
/* 215 */     page1FirstTime = false;
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\byFilter\MaxLiked\Pages\Page1.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */