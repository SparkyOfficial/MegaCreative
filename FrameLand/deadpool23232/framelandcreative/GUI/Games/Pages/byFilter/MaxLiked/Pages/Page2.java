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
/*     */ public class Page2
/*     */ {
/*     */   public static HashMap<Player, ItemStack[]> page2Map;
/*     */   public static boolean page2FirstTime = true;
/*  31 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*     */   public static void open(Player player) {
/*  33 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/*     */     
/*  35 */     if (Organizer.pages.intValue() >= 1) {
/*  36 */       Integer integer = Organizer.pages; Organizer.pages = Integer.valueOf(Organizer.pages.intValue() - 1);
/*  37 */       int index = 0;
/*  38 */       for (int i = 28; i < 44; i++) {
/*  39 */         if (i == 35) {
/*  40 */           i = 37;
/*     */         }
/*  42 */         if (!Organizer.stringList.isEmpty()) {
/*  43 */           String string = DataConfig.get().getString("registered-worlds." + (String)Organizer.stringList.get(index) + ".name");
/*  44 */           ItemStack item = RandomSkull.skull();
/*  45 */           String id = Organizer.stringList.get(index);
/*  46 */           String opened = "&cЗакрыт";
/*  47 */           if (DataConfig.get().getBoolean("registered-worlds." + id + ".opened"))
/*  48 */             opened = "&aОткрыт"; 
/*  49 */           String uuidString = DataConfig.get().getString("registered-worlds." + id + ".author");
/*  50 */           UUID uuid = UUID.fromString(uuidString);
/*  51 */           OfflinePlayer player1 = Bukkit.getOfflinePlayer(uuid);
/*  52 */           String prefix = PlayerData.get().getString(uuid + ".prefix");
/*  53 */           if (player1.isOnline()) {
/*  54 */             prefix = PlaceholderAPI.setPlaceholders(player1, "%vault_prefix%");
/*     */           }
/*  56 */           if (prefix == null) {
/*  57 */             prefix = "";
/*     */           }
/*  59 */           List<String> list = new ArrayList<>();
/*  60 */           String likes = Integer.toString(WorldData.get().getInt("worlds." + id + ".liked"));
/*  61 */           String uniq = Integer.toString(WorldData.get().getInt("worlds." + id + ".uniquePlayers"));
/*  62 */           for (String line : config.getStringList("for-every-world.desc.part1")) {
/*  63 */             list.add(FrameLandCreative.Color(line
/*  64 */                   .replace("#name#", string)
/*  65 */                   .replace("#author#", prefix + player1.getName())
/*  66 */                   .replace("#id#", DataConfig.get().getString("registered-worlds." + id + ".id"))
/*  67 */                   .replace("#opened#", opened)
/*  68 */                   .replace("#players#", uniq)
/*  69 */                   .replace("#likes#", likes)));
/*     */           }
/*     */           
/*  72 */           List<String> list1 = new ArrayList<>();
/*  73 */           for (String line : DataConfig.get().getStringList("registered-worlds." + id + ".description")) {
/*  74 */             list1.add(FrameLandCreative.Color("&f " + line));
/*     */           }
/*  76 */           List<String> list2 = new ArrayList<>();
/*  77 */           for (String line : config.getStringList("for-every-world.desc.part2")) {
/*  78 */             list2.add(FrameLandCreative.Color(line
/*  79 */                   .replace("#name#", string)
/*  80 */                   .replace("#author#", prefix + player1.getName())
/*  81 */                   .replace("#id#", DataConfig.get().getString("registered-worlds." + id + ".id"))
/*  82 */                   .replace("#opened#", opened)
/*  83 */                   .replace("#players#", uniq)
/*  84 */                   .replace("#likes#", likes)));
/*     */           }
/*     */           
/*  87 */           List<String> description = new ArrayList<>();
/*  88 */           description.addAll(list);
/*  89 */           description.addAll(list1);
/*  90 */           description.addAll(list2);
/*     */           
/*  92 */           ItemMeta itemMeta = item.getItemMeta();
/*  93 */           itemMeta.setDisplayName(FrameLandCreative.Color(string));
/*  94 */           itemMeta.setLore(description);
/*  95 */           item.setItemMeta(itemMeta);
/*  96 */           gui.setItem(i, item);
/*  97 */           Organizer.stringList.remove(index);
/*     */         } 
/*     */       } 
/*     */     } 
/*     */     
/* 102 */     ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 103 */     ItemMeta glass1 = glass.getItemMeta();
/* 104 */     glass1.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/* 105 */     glass1.setDisplayName(FrameLandCreative.Color("&7"));
/* 106 */     glass.setItemMeta(glass1);
/*     */     
/* 108 */     ItemStack glass2 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 109 */     ItemMeta glass3 = glass2.getItemMeta();
/* 110 */     glass3.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/* 111 */     glass3.setDisplayName(FrameLandCreative.Color("&7"));
/* 112 */     glass2.setItemMeta(glass3);
/*     */ 
/*     */     
/* 115 */     ItemStack maxLiked = RandomSkull.getSkull("http://textures.minecraft.net/texture/e03ccf028c61c2dff4437578ce71e1550478185b54fc94c5927f0f06f966273a");
/* 116 */     ItemMeta maxLikedMeta = maxLiked.getItemMeta();
/* 117 */     maxLikedMeta.setDisplayName(FrameLandCreative.Color(config.getString("maxLiked.name")));
/* 118 */     List<String> maxLikedLore = new ArrayList<>();
/* 119 */     for (String line : config.getStringList("maxLiked.description")) {
/* 120 */       maxLikedLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 122 */     maxLikedMeta.setLore(maxLikedLore);
/*     */     
/* 124 */     ItemStack maxUniq = RandomSkull.getSkull("http://textures.minecraft.net/texture/676dcb274e2e23149f472781605b7c6f839931a4f1d2edbd1ff5463ab7c41246");
/* 125 */     ItemMeta maxUniqMeta = maxUniq.getItemMeta();
/* 126 */     maxUniqMeta.setDisplayName(FrameLandCreative.Color(config.getString("maxUniq.name")));
/* 127 */     List<String> maxUniqLore = new ArrayList<>();
/* 128 */     for (String line : config.getStringList("maxUniq.description")) {
/* 129 */       maxUniqLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 131 */     maxUniqMeta.setLore(maxUniqLore);
/*     */     
/* 133 */     ItemStack Newest = RandomSkull.getSkull("http://textures.minecraft.net/texture/e5348ab6eedc3c76bf71d4395f0607829835ac8b4dc37ab31d22931ad255e454");
/* 134 */     ItemMeta NewestMeta = Newest.getItemMeta();
/* 135 */     NewestMeta.setDisplayName(FrameLandCreative.Color(config.getString("newest.name")));
/* 136 */     List<String> NewestLore = new ArrayList<>();
/* 137 */     for (String line : config.getStringList("newest.description")) {
/* 138 */       NewestLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 140 */     NewestMeta.setLore(NewestLore);
/*     */ 
/*     */     
/* 143 */     ItemStack likedItem = RandomSkull.getSkull("http://textures.minecraft.net/texture/fbefc867d847ab95e775e04aa5383b1670d38420a827b162d898b8f7ec148ec");
/* 144 */     ItemMeta likedMeta = likedItem.getItemMeta();
/* 145 */     likedMeta.setDisplayName(FrameLandCreative.Color(config.getString("liked.name")));
/* 146 */     List<String> likedLore = new ArrayList<>();
/* 147 */     for (String line : config.getStringList("liked.description")) {
/* 148 */       likedLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 150 */     likedMeta.setLore(likedLore);
/*     */     
/* 152 */     ItemStack ownItem = RandomSkull.getSkull("http://textures.minecraft.net/texture/15ad93d56546f12d5356effcbc6ec4c87ba245d81e1662c4b830f7d298e9");
/* 153 */     ItemMeta ownMeta = ownItem.getItemMeta();
/* 154 */     ownMeta.setDisplayName(FrameLandCreative.Color(config.getString("own.name")));
/* 155 */     List<String> ownLore = new ArrayList<>();
/* 156 */     for (String line : config.getStringList("own.description")) {
/* 157 */       ownLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 159 */     ownMeta.setLore(ownLore);
/*     */     
/* 161 */     ItemStack latestItem = RandomSkull.getSkull("http://textures.minecraft.net/texture/74133f6ac3be2e2499a784efadcfffeb9ace025c3646ada67f3414e5ef3394");
/* 162 */     ItemMeta latestMeta = latestItem.getItemMeta();
/* 163 */     latestMeta.setDisplayName(FrameLandCreative.Color(config.getString("latest.name")));
/* 164 */     List<String> latestLore = new ArrayList<>();
/* 165 */     for (String line : config.getStringList("latest.description")) {
/* 166 */       latestLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 168 */     latestMeta.setLore(latestLore);
/*     */ 
/*     */     
/* 171 */     ItemStack back = new ItemStack(Material.ARROW, 1);
/* 172 */     ItemMeta backMeta = back.getItemMeta();
/* 173 */     backMeta.setDisplayName(FrameLandCreative.Color(config.getString("gameList.back")));
/* 174 */     backMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/*     */     
/* 176 */     ItemStack front = new ItemStack(Material.ARROW, 3);
/* 177 */     ItemMeta frontMeta = front.getItemMeta();
/* 178 */     frontMeta.setDisplayName(FrameLandCreative.Color(config.getString("gameList.front")));
/* 179 */     frontMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/*     */     
/* 181 */     Newest.setItemMeta(NewestMeta);
/* 182 */     maxLiked.setItemMeta(maxLikedMeta);
/* 183 */     maxUniq.setItemMeta(maxUniqMeta);
/* 184 */     likedItem.setItemMeta(likedMeta);
/* 185 */     ownItem.setItemMeta(ownMeta);
/* 186 */     back.setItemMeta(backMeta);
/* 187 */     front.setItemMeta(frontMeta);
/* 188 */     latestItem.setItemMeta(latestMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 193 */     gui.setItem(3, maxUniq);
/* 194 */     gui.setItem(4, maxLiked);
/* 195 */     gui.setItem(5, Newest);
/*     */     
/* 197 */     gui.setItem(0, likedItem);
/* 198 */     gui.setItem(1, latestItem);
/*     */     
/* 200 */     gui.setItem(8, ownItem);
/*     */     
/* 202 */     gui.setItem(36, back);
/* 203 */     gui.setItem(44, front);
/*     */     
/* 205 */     gui.setItem(9, glass); gui.setItem(10, glass); gui.setItem(11, glass); gui.setItem(12, glass); gui.setItem(13, glass2);
/* 206 */     gui.setItem(14, glass); gui.setItem(15, glass); gui.setItem(16, glass); gui.setItem(17, glass);
/*     */     
/* 208 */     player.openInventory(gui);
/* 209 */     page2Map = (HashMap)new HashMap<>();
/* 210 */     page2Map.put(player, gui.getContents());
/* 211 */     page2FirstTime = false;
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\byFilter\MaxLiked\Pages\Page2.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */