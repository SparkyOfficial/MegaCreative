/*     */ package deadpool23232.framelandcreative.GUI.WorldSettings;
/*     */ 
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import deadpool23232.framelandcreative.Configs.WorldData;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.enchantments.Enchantment;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.InventoryHolder;
/*     */ import org.bukkit.inventory.ItemFlag;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ 
/*     */ public class WorldSettings
/*     */ {
/*  22 */   static FileConfiguration worldConfig = WorldData.get();
/*  23 */   static FileConfiguration dataConfig = DataConfig.get();
/*  24 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfigFile();
/*     */   
/*     */   public static void main(Player player, String id) {
/*  27 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&2Настройки мира"));
/*     */     
/*  29 */     ItemStack nameItem = new ItemStack(Material.PAPER);
/*  30 */     ItemMeta nameMeta = nameItem.getItemMeta();
/*  31 */     ItemStack descriptionItem = new ItemStack(Material.BOOK);
/*  32 */     ItemMeta descMeta = descriptionItem.getItemMeta();
/*  33 */     ItemStack spawnItem = new ItemStack(Material.ENDER_PEARL);
/*  34 */     ItemMeta spawnMeta = spawnItem.getItemMeta();
/*     */     
/*  36 */     ItemStack idItem = new ItemStack(Material.LEASH);
/*  37 */     ItemMeta idMeta = idItem.getItemMeta();
/*  38 */     ItemStack closeItem = new ItemStack(Material.BARRIER);
/*  39 */     ItemMeta closeMeta = closeItem.getItemMeta();
/*  40 */     ItemStack ruleItem = new ItemStack(Material.SMOOTH_BRICK);
/*  41 */     ItemMeta ruleMeta = ruleItem.getItemMeta();
/*     */     
/*  43 */     ItemStack kickItem = new ItemStack(Material.TNT);
/*  44 */     ItemMeta kickMeta = kickItem.getItemMeta();
/*  45 */     ItemStack whiteItem = new ItemStack(Material.WOOL);
/*  46 */     ItemMeta whiteMeta = whiteItem.getItemMeta();
/*  47 */     ItemStack blackItem = new ItemStack(Material.WOOL, 1, (short)15);
/*  48 */     ItemMeta blackMeta = blackItem.getItemMeta();
/*     */     
/*  50 */     ItemStack playItem = new ItemStack(Material.DIAMOND_SWORD);
/*  51 */     ItemMeta playMeta = playItem.getItemMeta();
/*  52 */     ItemStack codeItem = new ItemStack(Material.EYE_OF_ENDER);
/*  53 */     ItemMeta codeMeta = codeItem.getItemMeta();
/*     */     
/*  55 */     ItemStack infoItem = new ItemStack(Material.MAP);
/*  56 */     ItemMeta infoMeta = infoItem.getItemMeta();
/*  57 */     ItemStack gamesItem = new ItemStack(Material.DIAMOND);
/*  58 */     ItemMeta gamesMeta = gamesItem.getItemMeta();
/*     */ 
/*     */ 
/*     */     
/*  62 */     nameMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/*  63 */     nameMeta.setDisplayName(FrameLandCreative.Color(config.getString("nameName")));
/*  64 */     ArrayList<String> nameLore = new ArrayList<>();
/*  65 */     List<String> nameLoreList = config.getStringList("nameDescription");
/*  66 */     if (!nameLoreList.isEmpty()) {
/*  67 */       for (String lore : nameLoreList) {
/*  68 */         nameLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/*  71 */     nameMeta.setLore(nameLore);
/*  72 */     nameItem.setItemMeta(nameMeta);
/*     */ 
/*     */     
/*  75 */     descMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/*  76 */     descMeta.setDisplayName(FrameLandCreative.Color(config.getString("descName")));
/*  77 */     ArrayList<String> descLore = new ArrayList<>();
/*  78 */     List<String> descLoreList = config.getStringList("descDescription");
/*  79 */     if (!descLoreList.isEmpty()) {
/*  80 */       for (String lore : descLoreList) {
/*  81 */         descLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/*  84 */     descMeta.setLore(descLore);
/*  85 */     descriptionItem.setItemMeta(descMeta);
/*     */ 
/*     */     
/*  88 */     spawnMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/*  89 */     spawnMeta.setDisplayName(FrameLandCreative.Color(config.getString("spawnName")));
/*  90 */     ArrayList<String> spawnLore = new ArrayList<>();
/*  91 */     List<String> spawnLoreList = config.getStringList("spawnDescription");
/*  92 */     if (!spawnLoreList.isEmpty()) {
/*  93 */       for (String lore : spawnLoreList) {
/*  94 */         spawnLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/*  97 */     spawnMeta.setLore(spawnLore);
/*  98 */     spawnItem.setItemMeta(spawnMeta);
/*     */ 
/*     */     
/* 101 */     idMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 102 */     idMeta.setDisplayName(FrameLandCreative.Color(config.getString("idName")));
/* 103 */     ArrayList<String> idLore = new ArrayList<>();
/* 104 */     List<String> idLoreList = config.getStringList("idDescription");
/* 105 */     if (!idLoreList.isEmpty()) {
/* 106 */       for (String lore : idLoreList) {
/* 107 */         idLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/* 110 */     idMeta.setLore(idLore);
/* 111 */     idItem.setItemMeta(idMeta);
/*     */ 
/*     */     
/* 114 */     if (dataConfig.getBoolean("registered-worlds." + id + ".opened")) {
/* 115 */       closeMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 116 */       closeMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
/* 117 */       closeMeta.setDisplayName(FrameLandCreative.Color(config.getString("closeName")));
/* 118 */       ArrayList<String> closeLore = new ArrayList<>();
/* 119 */       List<String> closeLoreList = config.getStringList("closeDescriptionOpened");
/* 120 */       if (!closeLoreList.isEmpty()) {
/* 121 */         for (String lore : closeLoreList) {
/* 122 */           closeLore.add(FrameLandCreative.Color(lore));
/*     */         }
/*     */       }
/* 125 */       closeMeta.addEnchant(Enchantment.DAMAGE_ALL, 4, true);
/* 126 */       closeMeta.setLore(closeLore);
/* 127 */       closeItem.setItemMeta(closeMeta);
/*     */     } else {
/* 129 */       closeMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 130 */       closeMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
/* 131 */       closeMeta.setDisplayName(FrameLandCreative.Color(config.getString("closeName")));
/* 132 */       ArrayList<String> closeLore = new ArrayList<>();
/* 133 */       List<String> closeLoreList = config.getStringList("closeDescriptionClosed");
/* 134 */       if (!closeLoreList.isEmpty()) {
/* 135 */         for (String lore : closeLoreList) {
/* 136 */           closeLore.add(FrameLandCreative.Color(lore));
/*     */         }
/*     */       }
/* 139 */       closeMeta.setLore(closeLore);
/* 140 */       closeItem.setItemMeta(closeMeta);
/*     */     } 
/*     */ 
/*     */     
/* 144 */     ruleMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 145 */     ruleMeta.setDisplayName(FrameLandCreative.Color(config.getString("ruleName")));
/* 146 */     ArrayList<String> ruleLore = new ArrayList<>();
/* 147 */     List<String> ruleLoreList = config.getStringList("ruleDescription");
/* 148 */     if (!ruleLoreList.isEmpty()) {
/* 149 */       for (String lore : ruleLoreList) {
/* 150 */         ruleLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/* 153 */     ruleMeta.setLore(ruleLore);
/* 154 */     ruleItem.setItemMeta(ruleMeta);
/*     */ 
/*     */     
/* 157 */     kickMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 158 */     kickMeta.setDisplayName(FrameLandCreative.Color(config.getString("kickName")));
/* 159 */     ArrayList<String> kickLore = new ArrayList<>();
/* 160 */     List<String> kickLoreList = config.getStringList("kickDescription");
/* 161 */     if (!kickLoreList.isEmpty()) {
/* 162 */       for (String lore : kickLoreList) {
/* 163 */         kickLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/* 166 */     kickMeta.setLore(kickLore);
/* 167 */     kickItem.setItemMeta(kickMeta);
/*     */ 
/*     */     
/* 170 */     whiteMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 171 */     whiteMeta.setDisplayName(FrameLandCreative.Color(config.getString("whiteName")));
/* 172 */     ArrayList<String> whiteLore = new ArrayList<>();
/* 173 */     List<String> whiteLoreList = config.getStringList("whiteDescription");
/* 174 */     if (!whiteLoreList.isEmpty()) {
/* 175 */       for (String lore : whiteLoreList) {
/* 176 */         whiteLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/* 179 */     whiteMeta.setLore(whiteLore);
/* 180 */     whiteItem.setItemMeta(whiteMeta);
/*     */ 
/*     */     
/* 183 */     blackMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 184 */     blackMeta.setDisplayName(FrameLandCreative.Color(config.getString("blackName")));
/* 185 */     ArrayList<String> blackLore = new ArrayList<>();
/* 186 */     List<String> blackLoreList = config.getStringList("blackDescription");
/* 187 */     if (!blackLoreList.isEmpty()) {
/* 188 */       for (String lore : blackLoreList) {
/* 189 */         blackLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/* 192 */     blackMeta.setLore(blackLore);
/* 193 */     blackItem.setItemMeta(blackMeta);
/*     */ 
/*     */     
/* 196 */     playMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 197 */     playMeta.setDisplayName(FrameLandCreative.Color(config.getString("playName")));
/* 198 */     ArrayList<String> playLore = new ArrayList<>();
/* 199 */     List<String> playLoreList = config.getStringList("playDescription");
/* 200 */     if (!playLoreList.isEmpty()) {
/* 201 */       for (String lore : playLoreList) {
/* 202 */         playLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/* 205 */     playMeta.setLore(playLore);
/* 206 */     playItem.setItemMeta(playMeta);
/*     */ 
/*     */     
/* 209 */     codeMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 210 */     codeMeta.setDisplayName(FrameLandCreative.Color(config.getString("codeName")));
/* 211 */     ArrayList<String> codeLore = new ArrayList<>();
/* 212 */     List<String> codeLoreList = config.getStringList("codeDescription");
/* 213 */     if (!codeLoreList.isEmpty()) {
/* 214 */       for (String lore : codeLoreList) {
/* 215 */         codeLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/* 218 */     codeMeta.setLore(codeLore);
/* 219 */     codeItem.setItemMeta(codeMeta);
/*     */ 
/*     */     
/* 222 */     infoMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 223 */     infoMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_DESTROYS });
/* 224 */     infoMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
/* 225 */     infoMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_UNBREAKABLE });
/* 226 */     infoMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_PLACED_ON });
/* 227 */     infoMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_POTION_EFFECTS });
/* 228 */     infoMeta.setDisplayName(FrameLandCreative.Color(config.getString("infoName")));
/* 229 */     ArrayList<String> infoLore = new ArrayList<>();
/* 230 */     List<String> infoLoreList = config.getStringList("infoDescription");
/* 231 */     if (!infoLoreList.isEmpty()) {
/* 232 */       for (String lore : infoLoreList) {
/* 233 */         infoLore.add(FrameLandCreative.Color(lore.replace("#uni#", Integer.toString(worldConfig.getInt("worlds." + id + ".uniquePlayers")))
/* 234 */               .replace("#liked#", Integer.toString(worldConfig.getInt("worlds." + id + ".liked")))
/* 235 */               .replace("#id#", dataConfig.getString("registered-worlds." + id + ".id"))));
/*     */       }
/*     */     }
/*     */     
/* 239 */     infoMeta.setLore(infoLore);
/* 240 */     infoItem.setItemMeta(infoMeta);
/*     */ 
/*     */     
/* 243 */     gamesMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
/* 244 */     gamesMeta.setDisplayName(FrameLandCreative.Color(config.getString("gamesName")));
/* 245 */     ArrayList<String> gamesLore = new ArrayList<>();
/* 246 */     List<String> gamesLoreList = config.getStringList("gamesDescription");
/* 247 */     if (!gamesLoreList.isEmpty()) {
/* 248 */       for (String lore : gamesLoreList) {
/* 249 */         gamesLore.add(FrameLandCreative.Color(lore));
/*     */       }
/*     */     }
/* 252 */     gamesMeta.setLore(gamesLore);
/* 253 */     gamesItem.setItemMeta(gamesMeta);
/*     */     
/* 255 */     gui.setItem(10, nameItem);
/* 256 */     gui.setItem(11, descriptionItem);
/* 257 */     gui.setItem(12, spawnItem);
/*     */     
/* 259 */     gui.setItem(15, idItem);
/* 260 */     gui.setItem(16, closeItem);
/* 261 */     gui.setItem(24, ruleItem);
/*     */     
/* 263 */     gui.setItem(29, kickItem);
/* 264 */     gui.setItem(37, whiteItem);
/* 265 */     gui.setItem(38, blackItem);
/*     */     
/* 267 */     gui.setItem(42, playItem);
/* 268 */     gui.setItem(43, codeItem);
/*     */     
/* 270 */     gui.setItem(49, infoItem);
/* 271 */     gui.setItem(31, gamesItem);
/*     */     
/* 273 */     player.openInventory(gui);
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WorldSettings\WorldSettings.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */