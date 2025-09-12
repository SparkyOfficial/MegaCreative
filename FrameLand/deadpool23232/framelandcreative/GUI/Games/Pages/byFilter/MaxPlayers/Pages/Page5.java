/*     */ package deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxPlayers.Pages;
/*     */ 
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import deadpool23232.framelandcreative.Configs.PlayerData;
/*     */ import deadpool23232.framelandcreative.Configs.WorldData;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Functions.Skull.RandomSkull;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxPlayers.Organizer;
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
/*     */ public class Page5
/*     */ {
/*     */   public static HashMap<Player, ItemStack[]> page5Map;
/*     */   public static boolean page5FirstTime = true;
/*  30 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*     */   public static void open(Player player) {
/*  32 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по уникальным посетителям"));
/*     */     
/*  34 */     if (Organizer.pages.intValue() >= 1) {
/*  35 */       Integer integer = Organizer.pages; Organizer.pages = Integer.valueOf(Organizer.pages.intValue() - 1);
/*  36 */       int index = 0;
/*  37 */       for (int i = 28; i < 44; i++) {
/*  38 */         if (i == 35) {
/*  39 */           i = 37;
/*     */         }
/*  41 */         if (!Organizer.stringList.isEmpty()) {
/*  42 */           String string = DataConfig.get().getString("registered-worlds." + (String)Organizer.stringList.get(index) + ".name");
/*  43 */           ItemStack item = RandomSkull.skull();
/*  44 */           String id = Organizer.stringList.get(index);
/*  45 */           String opened = "&cЗакрыт";
/*  46 */           if (DataConfig.get().getBoolean("registered-worlds." + id + ".opened"))
/*  47 */             opened = "&aОткрыт"; 
/*  48 */           String uuidString = DataConfig.get().getString("registered-worlds." + id + ".author");
/*  49 */           UUID uuid = UUID.fromString(uuidString);
/*  50 */           OfflinePlayer player1 = Bukkit.getOfflinePlayer(uuid);
/*  51 */           String prefix = PlayerData.get().getString(uuid + ".prefix");
/*  52 */           if (player1.isOnline()) {
/*  53 */             prefix = PlaceholderAPI.setPlaceholders(player1, "%vault_prefix%");
/*     */           }
/*  55 */           if (prefix == null) {
/*  56 */             prefix = "";
/*     */           }
/*  58 */           List<String> list = new ArrayList<>();
/*  59 */           String likes = Integer.toString(WorldData.get().getInt("worlds." + id + ".liked"));
/*  60 */           String uniq = Integer.toString(WorldData.get().getInt("worlds." + id + ".uniquePlayers"));
/*  61 */           for (String line : config.getStringList("for-every-world.desc.part1")) {
/*  62 */             list.add(FrameLandCreative.Color(line
/*  63 */                   .replace("#name#", string)
/*  64 */                   .replace("#author#", prefix + player1.getName())
/*  65 */                   .replace("#id#", DataConfig.get().getString("registered-worlds." + id + ".id"))
/*  66 */                   .replace("#opened#", opened)
/*  67 */                   .replace("#players#", uniq)
/*  68 */                   .replace("#likes#", likes)));
/*     */           }
/*     */           
/*  71 */           List<String> list1 = new ArrayList<>();
/*  72 */           for (String line : DataConfig.get().getStringList("registered-worlds." + id + ".description")) {
/*  73 */             list1.add(FrameLandCreative.Color("&f " + line));
/*     */           }
/*  75 */           List<String> list2 = new ArrayList<>();
/*  76 */           for (String line : config.getStringList("for-every-world.desc.part2")) {
/*  77 */             list2.add(FrameLandCreative.Color(line
/*  78 */                   .replace("#name#", string)
/*  79 */                   .replace("#author#", prefix + player1.getName())
/*  80 */                   .replace("#id#", DataConfig.get().getString("registered-worlds." + id + ".id"))
/*  81 */                   .replace("#opened#", opened)
/*  82 */                   .replace("#players#", uniq)
/*  83 */                   .replace("#likes#", likes)));
/*     */           }
/*     */           
/*  86 */           List<String> description = new ArrayList<>();
/*  87 */           description.addAll(list);
/*  88 */           description.addAll(list1);
/*  89 */           description.addAll(list2);
/*     */           
/*  91 */           ItemMeta itemMeta = item.getItemMeta();
/*  92 */           itemMeta.setDisplayName(FrameLandCreative.Color(string));
/*  93 */           itemMeta.setLore(description);
/*  94 */           item.setItemMeta(itemMeta);
/*  95 */           gui.setItem(i, item);
/*  96 */           Organizer.stringList.remove(index);
/*     */         } 
/*     */       } 
/*     */     } 
/*     */     
/* 101 */     ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 102 */     ItemMeta glass1 = glass.getItemMeta();
/* 103 */     glass1.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/* 104 */     glass1.setDisplayName(FrameLandCreative.Color("&7"));
/* 105 */     glass.setItemMeta(glass1);
/*     */     
/* 107 */     ItemStack glass2 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 108 */     ItemMeta glass3 = glass2.getItemMeta();
/* 109 */     glass3.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/* 110 */     glass3.setDisplayName(FrameLandCreative.Color("&7"));
/* 111 */     glass2.setItemMeta(glass3);
/*     */ 
/*     */     
/* 114 */     ItemStack maxLiked = RandomSkull.getSkull("http://textures.minecraft.net/texture/e03ccf028c61c2dff4437578ce71e1550478185b54fc94c5927f0f06f966273a");
/* 115 */     ItemMeta maxLikedMeta = maxLiked.getItemMeta();
/* 116 */     maxLikedMeta.setDisplayName(FrameLandCreative.Color(config.getString("maxLiked.name")));
/* 117 */     List<String> maxLikedLore = new ArrayList<>();
/* 118 */     for (String line : config.getStringList("maxLiked.description")) {
/* 119 */       maxLikedLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 121 */     maxLikedMeta.setLore(maxLikedLore);
/*     */     
/* 123 */     ItemStack maxUniq = RandomSkull.getSkull("http://textures.minecraft.net/texture/676dcb274e2e23149f472781605b7c6f839931a4f1d2edbd1ff5463ab7c41246");
/* 124 */     ItemMeta maxUniqMeta = maxUniq.getItemMeta();
/* 125 */     maxUniqMeta.setDisplayName(FrameLandCreative.Color(config.getString("maxUniq.name")));
/* 126 */     List<String> maxUniqLore = new ArrayList<>();
/* 127 */     for (String line : config.getStringList("maxUniq.description")) {
/* 128 */       maxUniqLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 130 */     maxUniqMeta.setLore(maxUniqLore);
/*     */     
/* 132 */     ItemStack Newest = RandomSkull.getSkull("http://textures.minecraft.net/texture/e5348ab6eedc3c76bf71d4395f0607829835ac8b4dc37ab31d22931ad255e454");
/* 133 */     ItemMeta NewestMeta = Newest.getItemMeta();
/* 134 */     NewestMeta.setDisplayName(FrameLandCreative.Color(config.getString("newest.name")));
/* 135 */     List<String> NewestLore = new ArrayList<>();
/* 136 */     for (String line : config.getStringList("newest.description")) {
/* 137 */       NewestLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 139 */     NewestMeta.setLore(NewestLore);
/*     */ 
/*     */     
/* 142 */     ItemStack likedItem = RandomSkull.getSkull("http://textures.minecraft.net/texture/fbefc867d847ab95e775e04aa5383b1670d38420a827b162d898b8f7ec148ec");
/* 143 */     ItemMeta likedMeta = likedItem.getItemMeta();
/* 144 */     likedMeta.setDisplayName(FrameLandCreative.Color(config.getString("liked.name")));
/* 145 */     List<String> likedLore = new ArrayList<>();
/* 146 */     for (String line : config.getStringList("liked.description")) {
/* 147 */       likedLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 149 */     likedMeta.setLore(likedLore);
/*     */     
/* 151 */     ItemStack ownItem = RandomSkull.getSkull("http://textures.minecraft.net/texture/15ad93d56546f12d5356effcbc6ec4c87ba245d81e1662c4b830f7d298e9");
/* 152 */     ItemMeta ownMeta = ownItem.getItemMeta();
/* 153 */     ownMeta.setDisplayName(FrameLandCreative.Color(config.getString("own.name")));
/* 154 */     List<String> ownLore = new ArrayList<>();
/* 155 */     for (String line : config.getStringList("own.description")) {
/* 156 */       ownLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 158 */     ownMeta.setLore(ownLore);
/*     */     
/* 160 */     ItemStack latestItem = RandomSkull.getSkull("http://textures.minecraft.net/texture/74133f6ac3be2e2499a784efadcfffeb9ace025c3646ada67f3414e5ef3394");
/* 161 */     ItemMeta latestMeta = latestItem.getItemMeta();
/* 162 */     latestMeta.setDisplayName(FrameLandCreative.Color(config.getString("latest.name")));
/* 163 */     List<String> latestLore = new ArrayList<>();
/* 164 */     for (String line : config.getStringList("latest.description")) {
/* 165 */       latestLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 167 */     latestMeta.setLore(latestLore);
/*     */ 
/*     */     
/* 170 */     ItemStack back = new ItemStack(Material.ARROW, 4);
/* 171 */     ItemMeta backMeta = back.getItemMeta();
/* 172 */     backMeta.setDisplayName(FrameLandCreative.Color(config.getString("gameList.back")));
/* 173 */     backMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/*     */     
/* 175 */     ItemStack front = new ItemStack(Material.ARROW, 6);
/* 176 */     ItemMeta frontMeta = front.getItemMeta();
/* 177 */     frontMeta.setDisplayName(FrameLandCreative.Color(config.getString("gameList.front")));
/* 178 */     frontMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/*     */     
/* 180 */     Newest.setItemMeta(NewestMeta);
/* 181 */     maxLiked.setItemMeta(maxLikedMeta);
/* 182 */     maxUniq.setItemMeta(maxUniqMeta);
/* 183 */     likedItem.setItemMeta(likedMeta);
/* 184 */     ownItem.setItemMeta(ownMeta);
/* 185 */     back.setItemMeta(backMeta);
/* 186 */     front.setItemMeta(frontMeta);
/* 187 */     latestItem.setItemMeta(latestMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 192 */     gui.setItem(3, maxUniq);
/* 193 */     gui.setItem(4, maxLiked);
/* 194 */     gui.setItem(5, Newest);
/*     */     
/* 196 */     gui.setItem(0, likedItem);
/* 197 */     gui.setItem(1, latestItem);
/*     */     
/* 199 */     gui.setItem(8, ownItem);
/*     */     
/* 201 */     gui.setItem(36, back);
/* 202 */     gui.setItem(44, front);
/*     */     
/* 204 */     gui.setItem(9, glass); gui.setItem(10, glass); gui.setItem(11, glass); gui.setItem(12, glass2); gui.setItem(13, glass);
/* 205 */     gui.setItem(14, glass); gui.setItem(15, glass); gui.setItem(16, glass); gui.setItem(17, glass);
/*     */     
/* 207 */     player.openInventory(gui);
/* 208 */     page5Map = (HashMap)new HashMap<>();
/* 209 */     page5Map.put(player, gui.getContents());
/* 210 */     page5FirstTime = false;
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\byFilter\MaxPlayers\Pages\Page5.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */