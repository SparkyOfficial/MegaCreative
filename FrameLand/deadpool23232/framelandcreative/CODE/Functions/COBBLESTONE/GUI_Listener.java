/*     */ package deadpool23232.framelandcreative.CODE.Functions.COBBLESTONE;
/*     */ 
/*     */ import deadpool23232.framelandcreative.CODE.Blocks.Sign;
/*     */ import deadpool23232.framelandcreative.CODE.Functions.OpenBySign;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.block.Block;
/*     */ import org.bukkit.block.Chest;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.inventory.InventoryClickEvent;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class GUI_Listener
/*     */   implements Listener
/*     */ {
/*     */   @EventHandler
/*     */   public void onMenuClick(InventoryClickEvent event) {
/*  30 */     if (event.getInventory().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&bДействие игрока"))) {
/*  31 */       event.setCancelled(true);
/*  32 */       if (event.getCurrentItem() == null) {
/*     */         return;
/*     */       }
/*  35 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*     */         return;
/*     */       }
/*  38 */       ItemStack item = event.getCurrentItem();
/*  39 */       ItemMeta itemMeta = item.getItemMeta();
/*  40 */       String itemName = itemMeta.getDisplayName();
/*  41 */       Player player = (Player)event.getWhoClicked();
/*  42 */       if (itemName == null) {
/*     */         return;
/*     */       }
/*     */       
/*  46 */       if (itemName.equals(FrameLandCreative.Color("&fВернутся"))) {
/*  47 */         GUI.firstGUI(player);
/*     */       }
/*     */       
/*  50 */       List<String> sign = new ArrayList<>();
/*  51 */       Block chest = player.getWorld().getBlockAt(((Location)OpenBySign.blockMap.get(player)).getBlockX(), ((Location)OpenBySign.blockMap.get(player)).getBlockY() + 1, ((Location)OpenBySign.blockMap.get(player)).getBlockZ() + 1);
/*     */ 
/*     */       
/*  54 */       if (itemName.equals(FrameLandCreative.Color("&aВзаимодействие с инвентарём"))) {
/*  55 */         GUI.inventoryGUI(player);
/*     */       }
/*     */       
/*  58 */       if (itemName.equals(FrameLandCreative.Color("&aЗакрыть инвентарь"))) {
/*  59 */         sign.add("Действие игрока");
/*  60 */         sign.add("Закр. инвентарь");
/*  61 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  62 */         if (chest.getState() instanceof Chest) {
/*  63 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/*  65 */         chest.setType(Material.AIR);
/*  66 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  67 */         player.closeInventory();
/*  68 */       } else if (itemName.equals(FrameLandCreative.Color("&aОткрыть инвентарь"))) {
/*  69 */         sign.add("Действие игрока");
/*  70 */         sign.add("Откр. инвентарь");
/*  71 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  72 */         if (chest.getState() instanceof Chest) {
/*  73 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/*  75 */         chest.setType(Material.CHEST);
/*  76 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/*  78 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/*  79 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/*  80 */         ItemMeta glassMeta = glass.getItemMeta();
/*  81 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/*  82 */         List<String> glassLore = new ArrayList<>();
/*  83 */         glassLore.add(FrameLandCreative.Color("&7"));
/*  84 */         glassMeta.setLore(glassLore);
/*  85 */         glass.setItemMeta(glassMeta);
/*  86 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/*  87 */         ItemMeta glass1Meta = glass1.getItemMeta();
/*  88 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&aКоординаты сундука"));
/*  89 */         glass1.setItemMeta(glass1Meta);
/*  90 */         for (int i = 0; i < 27; i++) {
/*  91 */           inventory.setItem(i, glass);
/*     */         }
/*  93 */         inventory.setItem(4, glass1);
/*  94 */         ItemStack mode = new ItemStack(Material.ENDER_CHEST);
/*  95 */         ItemMeta modeMeta = mode.getItemMeta();
/*  96 */         modeMeta.setDisplayName(FrameLandCreative.Color("&eКопия"));
/*  97 */         List<String> lore = new ArrayList<>();
/*  98 */         lore.add(FrameLandCreative.Color("&7Установить тип сундука:"));
/*  99 */         lore.add(FrameLandCreative.Color(""));
/* 100 */         lore.add(FrameLandCreative.Color("&aКопия"));
/* 101 */         lore.add(FrameLandCreative.Color("&8Оригинал"));
/* 102 */         lore.add(FrameLandCreative.Color(""));
/* 103 */         modeMeta.setLore(lore);
/* 104 */         mode.setItemMeta(modeMeta);
/* 105 */         inventory.setItem(22, mode);
/* 106 */         inventory.setItem(13, new ItemStack(Material.AIR));
/*     */         
/* 108 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 109 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 110 */         player.closeInventory();
/* 111 */       } else if (itemName.equals(FrameLandCreative.Color("&aВыдать предметы"))) {
/* 112 */         sign.add("Действие игрока");
/* 113 */         sign.add("Выдать предметы");
/* 114 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 115 */         if (chest.getState() instanceof Chest) {
/* 116 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 118 */         chest.setType(Material.CHEST);
/* 119 */         Chest inv = (Chest)chest.getState();
/* 120 */         inv.getInventory().clear();
/* 121 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 122 */         player.closeInventory();
/* 123 */       } else if (itemName.equals(FrameLandCreative.Color("&aВыдать случайный предмет"))) {
/* 124 */         sign.add("Действие игрока");
/* 125 */         sign.add("Рандом предмет");
/* 126 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 127 */         if (chest.getState() instanceof Chest) {
/* 128 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 130 */         chest.setType(Material.CHEST);
/* 131 */         Chest inv = (Chest)chest.getState();
/* 132 */         inv.getInventory().clear();
/* 133 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 134 */         player.closeInventory();
/* 135 */       } else if (itemName.equals(FrameLandCreative.Color("&aОчистить инвентарь"))) {
/* 136 */         sign.add("Действие игрока");
/* 137 */         sign.add("Очистить инв.");
/* 138 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 139 */         if (chest.getState() instanceof Chest) {
/* 140 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 142 */         chest.setType(Material.AIR);
/* 143 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 144 */         player.closeInventory();
/* 145 */       } else if (itemName.equals(FrameLandCreative.Color("&aУдалить предметы"))) {
/* 146 */         sign.add("Действие игрока");
/* 147 */         sign.add("Удалить предм.");
/* 148 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 149 */         if (chest.getState() instanceof Chest) {
/* 150 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 152 */         chest.setType(Material.CHEST);
/* 153 */         Chest inv = (Chest)chest.getState();
/* 154 */         inv.getInventory().clear();
/* 155 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 156 */         player.closeInventory();
/*     */       } 
/*     */ 
/*     */       
/* 160 */       if (itemName.equals(FrameLandCreative.Color("&aКоммуникация"))) {
/* 161 */         GUI.commGUI(player);
/*     */       }
/*     */       
/* 164 */       if (itemName.equals(FrameLandCreative.Color("&eСообщение игроку"))) {
/* 165 */         sign.add("Действие игрока");
/* 166 */         sign.add("Сообщение игр.");
/* 167 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 168 */         if (chest.getState() instanceof Chest) {
/* 169 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 171 */         chest.setType(Material.CHEST);
/* 172 */         Chest inv = (Chest)chest.getState();
/* 173 */         inv.getInventory().clear();
/* 174 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 175 */         player.closeInventory();
/* 176 */       } else if (itemName.equals(FrameLandCreative.Color("&eСообщение на весь мир"))) {
/* 177 */         sign.add("Действие игрока");
/* 178 */         sign.add("Мир. сообщение");
/* 179 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 180 */         if (chest.getState() instanceof Chest) {
/* 181 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 183 */         chest.setType(Material.CHEST);
/* 184 */         Chest inv = (Chest)chest.getState();
/* 185 */         inv.getInventory().clear();
/* 186 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 187 */         player.closeInventory();
/*     */       } 
/*     */ 
/*     */       
/* 191 */       if (itemName.equals(FrameLandCreative.Color("&aСостояние игрока"))) {
/* 192 */         GUI.playerGUI(player);
/*     */       }
/*     */       
/* 195 */       if (itemName.equals(FrameLandCreative.Color("&bУстановить режим игры"))) {
/* 196 */         sign.add("Действие игрока");
/* 197 */         sign.add("Режим игры");
/* 198 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 199 */         if (chest.getState() instanceof Chest) {
/* 200 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 202 */         chest.setType(Material.CHEST);
/* 203 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/* 205 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/* 206 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 207 */         ItemMeta glassMeta = glass.getItemMeta();
/* 208 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/* 209 */         List<String> glassLore = new ArrayList<>();
/* 210 */         glassLore.add(FrameLandCreative.Color("&7"));
/* 211 */         glassMeta.setLore(glassLore);
/* 212 */         glass.setItemMeta(glassMeta);
/* 213 */         for (int i = 0; i < 27; i++) {
/* 214 */           inventory.setItem(i, glass);
/*     */         }
/* 216 */         ItemStack mode = new ItemStack(Material.WORKBENCH);
/* 217 */         ItemMeta modeMeta = mode.getItemMeta();
/* 218 */         modeMeta.setDisplayName(FrameLandCreative.Color("&eКреатив"));
/* 219 */         List<String> lore = new ArrayList<>();
/* 220 */         lore.add(FrameLandCreative.Color("&7Установить режим игры:"));
/* 221 */         lore.add(FrameLandCreative.Color(""));
/* 222 */         lore.add(FrameLandCreative.Color("&aКреатив"));
/* 223 */         lore.add(FrameLandCreative.Color("&8Выживание"));
/* 224 */         lore.add(FrameLandCreative.Color("&8Спектральный"));
/* 225 */         lore.add(FrameLandCreative.Color("&8Приключение"));
/* 226 */         lore.add(FrameLandCreative.Color(""));
/* 227 */         modeMeta.setLore(lore);
/* 228 */         mode.setItemMeta(modeMeta);
/* 229 */         inventory.setItem(13, mode);
/*     */         
/* 231 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 232 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 233 */         player.closeInventory();
/* 234 */       } else if (itemName.equals(FrameLandCreative.Color("&bУстановить здоровье"))) {
/* 235 */         sign.add("Действие игрока");
/* 236 */         sign.add("Уст. здоровье");
/* 237 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 238 */         if (chest.getState() instanceof Chest) {
/* 239 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 241 */         chest.setType(Material.CHEST);
/* 242 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/* 244 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/* 245 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 246 */         ItemMeta glassMeta = glass.getItemMeta();
/* 247 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/* 248 */         List<String> glassLore = new ArrayList<>();
/* 249 */         glassLore.add(FrameLandCreative.Color("&7"));
/* 250 */         glassMeta.setLore(glassLore);
/* 251 */         glass.setItemMeta(glassMeta);
/* 252 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 253 */         ItemMeta glass1Meta = glass1.getItemMeta();
/* 254 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&3Число устанавливаемого здоровья"));
/* 255 */         glass1.setItemMeta(glass1Meta);
/* 256 */         for (int i = 0; i < 27; i++) {
/* 257 */           inventory.setItem(i, glass);
/*     */         }
/* 259 */         inventory.setItem(4, glass1);
/* 260 */         inventory.setItem(22, glass1);
/* 261 */         inventory.setItem(13, new ItemStack(Material.AIR));
/* 262 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 263 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 264 */         player.closeInventory();
/* 265 */       } else if (itemName.equals(FrameLandCreative.Color("&bТелепортировать сущность"))) {
/* 266 */         sign.add("Действие игрока");
/* 267 */         sign.add("Телепортация");
/* 268 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 269 */         if (chest.getState() instanceof Chest) {
/* 270 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 272 */         chest.setType(Material.CHEST);
/* 273 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/* 275 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/* 276 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 277 */         ItemMeta glassMeta = glass.getItemMeta();
/* 278 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/* 279 */         List<String> glassLore = new ArrayList<>();
/* 280 */         glassLore.add(FrameLandCreative.Color("&7"));
/* 281 */         glassMeta.setLore(glassLore);
/* 282 */         glass.setItemMeta(glassMeta);
/* 283 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 284 */         ItemMeta glass1Meta = glass1.getItemMeta();
/* 285 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&aКоординаты телепортации"));
/* 286 */         glass1.setItemMeta(glass1Meta);
/* 287 */         for (int i = 0; i < 27; i++) {
/* 288 */           inventory.setItem(i, glass);
/*     */         }
/* 290 */         inventory.setItem(4, glass1);
/* 291 */         inventory.setItem(22, glass1);
/* 292 */         inventory.setItem(13, new ItemStack(Material.AIR));
/* 293 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 294 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 295 */         player.closeInventory();
/* 296 */       } else if (itemName.equals(FrameLandCreative.Color("&bОчистить эффекты"))) {
/* 297 */         sign.add("Действие игрока");
/* 298 */         sign.add("Очистить эфф.");
/* 299 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 300 */         if (chest.getState() instanceof Chest) {
/* 301 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 303 */         chest.setType(Material.AIR);
/* 304 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 305 */         player.closeInventory();
/* 306 */       } else if (itemName.equals(FrameLandCreative.Color("&bВыдать эффект зелья"))) {
/* 307 */         sign.add("Действие игрока");
/* 308 */         sign.add("Выдать эффект");
/* 309 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 310 */         if (chest.getState() instanceof Chest) {
/* 311 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 313 */         chest.setType(Material.CHEST);
/* 314 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/* 316 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/* 317 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 318 */         ItemMeta glassMeta = glass.getItemMeta();
/* 319 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/* 320 */         List<String> glassLore = new ArrayList<>();
/* 321 */         glassLore.add(FrameLandCreative.Color("&7"));
/* 322 */         glassMeta.setLore(glassLore);
/* 323 */         glass.setItemMeta(glassMeta);
/*     */         
/* 325 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 326 */         ItemMeta glass1Meta = glass1.getItemMeta();
/* 327 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&3Время действия эффектов в тиках (20 тиков = 1 секунда)"));
/* 328 */         glass1.setItemMeta(glass1Meta);
/*     */         
/* 330 */         ItemStack glass2 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 331 */         ItemMeta glass2Meta = glass2.getItemMeta();
/* 332 */         glass2Meta.setDisplayName(FrameLandCreative.Color("&3Усиление эффектов"));
/* 333 */         glass2.setItemMeta(glass2Meta);
/*     */         
/* 335 */         inventory.setItem(7, glass1);
/* 336 */         inventory.setItem(16, glass); inventory.setItem(17, glass);
/* 337 */         inventory.setItem(25, glass2);
/* 338 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 339 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 340 */         player.closeInventory();
/* 341 */       } else if (itemName.equals(FrameLandCreative.Color("&bУбрать эффект зелья"))) {
/* 342 */         sign.add("Действие игрока");
/* 343 */         sign.add("Убрать эффект");
/* 344 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 345 */         if (chest.getState() instanceof Chest) {
/* 346 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 348 */         chest.setType(Material.CHEST);
/* 349 */         Chest inv = (Chest)chest.getState();
/* 350 */         inv.getInventory().clear();
/* 351 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 352 */         player.closeInventory();
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\COBBLESTONE\GUI_Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */