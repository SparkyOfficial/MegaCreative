/*     */ package deadpool23232.framelandcreative.CODE.Functions.WOOD;
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
/*     */ public class GUI_Listener
/*     */   implements Listener
/*     */ {
/*     */   @EventHandler
/*     */   public void onMenuClick(InventoryClickEvent event) {
/*  26 */     if (event.getInventory().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&6Если игрок"))) {
/*  27 */       event.setCancelled(true);
/*  28 */       if (event.getCurrentItem() == null) {
/*     */         return;
/*     */       }
/*  31 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*     */         return;
/*     */       }
/*  34 */       ItemStack item = event.getCurrentItem();
/*  35 */       ItemMeta itemMeta = item.getItemMeta();
/*  36 */       String itemName = itemMeta.getDisplayName();
/*  37 */       Player player = (Player)event.getWhoClicked();
/*  38 */       if (itemName == null) {
/*     */         return;
/*     */       }
/*     */       
/*  42 */       if (itemName.equals(FrameLandCreative.Color("&fВернутся"))) {
/*  43 */         GUI.firstGUI(player);
/*     */       }
/*     */       
/*  46 */       List<String> sign = new ArrayList<>();
/*  47 */       Block chest = player.getWorld().getBlockAt(((Location)OpenBySign.blockMap.get(player)).getBlockX(), ((Location)OpenBySign.blockMap.get(player)).getBlockY() + 1, ((Location)OpenBySign.blockMap.get(player)).getBlockZ() + 1);
/*     */ 
/*     */       
/*  50 */       if (itemName.equals(FrameLandCreative.Color("&eСостояние"))) {
/*  51 */         GUI.playerGUI(player);
/*     */       }
/*     */       
/*  54 */       if (itemName.equals(FrameLandCreative.Color("&fИмя игрока равно"))) {
/*  55 */         sign.add("Если игрок");
/*  56 */         sign.add("Имя игрока");
/*  57 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  58 */         if (chest.getState() instanceof Chest) {
/*  59 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/*  61 */         chest.setType(Material.CHEST);
/*  62 */         Chest inv = (Chest)chest.getState();
/*  63 */         inv.getInventory().clear();
/*  64 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  65 */         player.closeInventory();
/*  66 */       } else if (itemName.equals(FrameLandCreative.Color("&fСообщение равно"))) {
/*  67 */         sign.add("Если игрок");
/*  68 */         sign.add("Сообщение");
/*  69 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  70 */         if (chest.getState() instanceof Chest) {
/*  71 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/*  73 */         chest.setType(Material.CHEST);
/*  74 */         Chest inv = (Chest)chest.getState();
/*  75 */         inv.getInventory().clear();
/*  76 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  77 */         player.closeInventory();
/*  78 */       } else if (itemName.equals(FrameLandCreative.Color("&fЕсли игрок смотрит на"))) {
/*  79 */         sign.add("Если игрок");
/*  80 */         sign.add("Смотрит на");
/*  81 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  82 */         if (chest.getState() instanceof Chest) {
/*  83 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/*  85 */         chest.setType(Material.CHEST);
/*  86 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/*  88 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/*  89 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/*  90 */         ItemMeta glassMeta = glass.getItemMeta();
/*  91 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/*  92 */         List<String> glassLore = new ArrayList<>();
/*  93 */         glassLore.add(FrameLandCreative.Color("&7"));
/*  94 */         glassMeta.setLore(glassLore);
/*  95 */         glass.setItemMeta(glassMeta);
/*  96 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/*  97 */         ItemMeta glass1Meta = glass1.getItemMeta();
/*  98 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&aМаксимальное расстояние"));
/*  99 */         glass1.setItemMeta(glass1Meta);
/*     */         
/* 101 */         inventory.setItem(7, glass); inventory.setItem(8, glass);
/* 102 */         inventory.setItem(16, glass1);
/* 103 */         inventory.setItem(25, glass); inventory.setItem(26, glass);
/*     */         
/* 105 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 106 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 107 */         player.closeInventory();
/* 108 */       } else if (itemName.equals(FrameLandCreative.Color("&fЕсли игрок рядом с координатами"))) {
/* 109 */         sign.add("Если игрок");
/* 110 */         sign.add("Рядом с");
/* 111 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 112 */         if (chest.getState() instanceof Chest) {
/* 113 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 115 */         chest.setType(Material.CHEST);
/* 116 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/* 118 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/* 119 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 120 */         ItemMeta glassMeta = glass.getItemMeta();
/* 121 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/* 122 */         List<String> glassLore = new ArrayList<>();
/* 123 */         glassLore.add(FrameLandCreative.Color("&7"));
/* 124 */         glassMeta.setLore(glassLore);
/* 125 */         glass.setItemMeta(glassMeta);
/* 126 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 127 */         ItemMeta glass1Meta = glass1.getItemMeta();
/* 128 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&aРасстояние"));
/* 129 */         glass1.setItemMeta(glass1Meta);
/*     */         
/* 131 */         inventory.setItem(7, glass); inventory.setItem(8, glass);
/* 132 */         inventory.setItem(16, glass1);
/* 133 */         inventory.setItem(25, glass); inventory.setItem(26, glass);
/*     */         
/* 135 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 136 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 137 */         player.closeInventory();
/* 138 */       } else if (itemName.equals(FrameLandCreative.Color("&fЕсли игрок стоит на"))) {
/* 139 */         sign.add("Если игрок");
/* 140 */         sign.add("Стоит на");
/* 141 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 142 */         if (chest.getState() instanceof Chest) {
/* 143 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 145 */         chest.setType(Material.CHEST);
/* 146 */         Chest inv = (Chest)chest.getState();
/* 147 */         inv.getInventory().clear();
/* 148 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 149 */         player.closeInventory();
/*     */       } 
/*     */       
/* 152 */       if (itemName.equals(FrameLandCreative.Color("&eИнвентарь"))) {
/* 153 */         GUI.inventoryGUI(player);
/*     */       }
/*     */       
/* 156 */       if (itemName.equals(FrameLandCreative.Color("&aИгрок держит предмет в основной руке"))) {
/* 157 */         sign.add("Если игрок");
/* 158 */         sign.add("Держит в осн.");
/* 159 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 160 */         if (chest.getState() instanceof Chest) {
/* 161 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 163 */         chest.setType(Material.CHEST);
/* 164 */         Chest inv = (Chest)chest.getState();
/* 165 */         inv.getInventory().clear();
/* 166 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 167 */         player.closeInventory();
/* 168 */       } else if (itemName.equals(FrameLandCreative.Color("&aИгрок держит предмет в дополнительной руке"))) {
/* 169 */         sign.add("Если игрок");
/* 170 */         sign.add("Держит в доп.");
/* 171 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 172 */         if (chest.getState() instanceof Chest) {
/* 173 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 175 */         chest.setType(Material.CHEST);
/* 176 */         Chest inv = (Chest)chest.getState();
/* 177 */         inv.getInventory().clear();
/* 178 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 179 */         player.closeInventory();
/* 180 */       } else if (itemName.equals(FrameLandCreative.Color("&aПредмет равен"))) {
/* 181 */         sign.add("Если игрок");
/* 182 */         sign.add("Предмет равен");
/* 183 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 184 */         if (chest.getState() instanceof Chest) {
/* 185 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 187 */         chest.setType(Material.CHEST);
/* 188 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/* 190 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/* 191 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 192 */         ItemMeta glassMeta = glass.getItemMeta();
/* 193 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/* 194 */         List<String> glassLore = new ArrayList<>();
/* 195 */         glassLore.add(FrameLandCreative.Color("&7"));
/* 196 */         glassMeta.setLore(glassLore);
/* 197 */         glass.setItemMeta(glassMeta);
/* 198 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 199 */         ItemMeta glass1Meta = glass1.getItemMeta();
/* 200 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&3Предмет"));
/* 201 */         glass1.setItemMeta(glass1Meta);
/* 202 */         for (int i = 0; i < 27; i++) {
/* 203 */           inventory.setItem(i, glass);
/*     */         }
/* 205 */         inventory.setItem(4, glass1);
/* 206 */         inventory.setItem(22, glass1);
/* 207 */         inventory.setItem(13, new ItemStack(Material.AIR));
/* 208 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 209 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 210 */         player.closeInventory();
/* 211 */       } else if (itemName.equals(FrameLandCreative.Color("&aОткрытый инвентарь имеет предметы"))) {
/* 212 */         sign.add("Если игрок");
/* 213 */         sign.add("Инвентарь имеет");
/* 214 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 215 */         if (chest.getState() instanceof Chest) {
/* 216 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 218 */         chest.setType(Material.CHEST);
/* 219 */         Chest inv = (Chest)chest.getState();
/* 220 */         inv.getInventory().clear();
/* 221 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 222 */         player.closeInventory();
/* 223 */       } else if (itemName.equals(FrameLandCreative.Color("&aИгрок имеет предметы"))) {
/* 224 */         sign.add("Если игрок");
/* 225 */         sign.add("Игрок имеет");
/* 226 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 227 */         if (chest.getState() instanceof Chest) {
/* 228 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 230 */         chest.setType(Material.CHEST);
/* 231 */         Chest inv = (Chest)chest.getState();
/* 232 */         inv.getInventory().clear();
/* 233 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 234 */         player.closeInventory();
/* 235 */       } else if (itemName.equals(FrameLandCreative.Color("&aНазвание открытого инвентаря равно"))) {
/* 236 */         sign.add("Если игрок");
/* 237 */         sign.add("Название инв.");
/* 238 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 239 */         if (chest.getState() instanceof Chest) {
/* 240 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 242 */         chest.setType(Material.CHEST);
/* 243 */         Chest inv = (Chest)chest.getState();
/* 244 */         inv.getInventory().clear();
/* 245 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 246 */         player.closeInventory();
/*     */       } 
/*     */       
/* 249 */       if (itemName.equals(FrameLandCreative.Color("&eРазное"))) {
/* 250 */         GUI.anyGUI(player);
/*     */       }
/*     */       
/* 253 */       if (itemName.equals(FrameLandCreative.Color("&aИгрок летает"))) {
/* 254 */         sign.add("Если игрок");
/* 255 */         sign.add("Летает");
/* 256 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 257 */         if (chest.getState() instanceof Chest) {
/* 258 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 260 */         chest.setType(Material.AIR);
/* 261 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 262 */         player.closeInventory();
/* 263 */       } else if (itemName.equals(FrameLandCreative.Color("&aИгрок крадётся"))) {
/* 264 */         sign.add("Если игрок");
/* 265 */         sign.add("Крадётся");
/* 266 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 267 */         if (chest.getState() instanceof Chest) {
/* 268 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 270 */         chest.setType(Material.AIR);
/* 271 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 272 */         player.closeInventory();
/* 273 */       } else if (itemName.equals(FrameLandCreative.Color("&aИгрок бежит"))) {
/* 274 */         sign.add("Если игрок");
/* 275 */         sign.add("Бежит");
/* 276 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 277 */         if (chest.getState() instanceof Chest) {
/* 278 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 280 */         chest.setType(Material.AIR);
/* 281 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 282 */         player.closeInventory();
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\WOOD\GUI_Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */