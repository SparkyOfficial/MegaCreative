/*     */ package deadpool23232.framelandcreative.CODE.Functions.NETHER_BRICK;
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
/*     */ public class GUI_Listener
/*     */   implements Listener
/*     */ {
/*     */   @EventHandler
/*     */   public void onMenuClick(InventoryClickEvent event) {
/*  28 */     if (event.getInventory().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&cИгровое действие"))) {
/*  29 */       event.setCancelled(true);
/*  30 */       if (event.getCurrentItem() == null) {
/*     */         return;
/*     */       }
/*  33 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*     */         return;
/*     */       }
/*  36 */       ItemStack item = event.getCurrentItem();
/*  37 */       ItemMeta itemMeta = item.getItemMeta();
/*  38 */       String itemName = itemMeta.getDisplayName();
/*  39 */       Player player = (Player)event.getWhoClicked();
/*  40 */       if (itemName == null) {
/*     */         return;
/*     */       }
/*     */       
/*  44 */       if (itemName.equals(FrameLandCreative.Color("&fВернутся"))) {
/*  45 */         GUI.firstGUI(player);
/*     */       }
/*     */       
/*  48 */       List<String> sign = new ArrayList<>();
/*  49 */       Block chest = player.getWorld().getBlockAt(((Location)OpenBySign.blockMap.get(player)).getBlockX(), ((Location)OpenBySign.blockMap.get(player)).getBlockY() + 1, ((Location)OpenBySign.blockMap.get(player)).getBlockZ() + 1);
/*     */ 
/*     */       
/*  52 */       if (itemName.equals(FrameLandCreative.Color("&eВзаимодействие с миром"))) {
/*  53 */         GUI.worldGUI(player);
/*     */       }
/*     */       
/*  56 */       if (itemName.equals(FrameLandCreative.Color("&aУстановить блок по координатам"))) {
/*  57 */         sign.add("Игровое действие");
/*  58 */         sign.add("Установить блок");
/*  59 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  60 */         if (chest.getState() instanceof Chest) {
/*  61 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/*  63 */         chest.setType(Material.CHEST);
/*  64 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/*  66 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/*  67 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/*  68 */         ItemMeta glassMeta = glass.getItemMeta();
/*  69 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/*  70 */         List<String> glassLore = new ArrayList<>();
/*  71 */         glassLore.add(FrameLandCreative.Color("&7"));
/*  72 */         glassMeta.setLore(glassLore);
/*  73 */         glass.setItemMeta(glassMeta);
/*  74 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/*  75 */         ItemMeta glass1Meta = glass1.getItemMeta();
/*  76 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&aКоординаты блока"));
/*  77 */         glass1.setItemMeta(glass1Meta);
/*  78 */         ItemStack glass2 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)2);
/*  79 */         ItemMeta glass2Meta = glass2.getItemMeta();
/*  80 */         glass2Meta.setDisplayName(FrameLandCreative.Color("&6Тип блока"));
/*  81 */         glass2.setItemMeta(glass2Meta);
/*  82 */         for (int i = 0; i < 27; i++) {
/*  83 */           inventory.setItem(i, glass);
/*     */         }
/*  85 */         inventory.setItem(2, glass1);
/*  86 */         inventory.setItem(20, glass1);
/*  87 */         inventory.setItem(6, glass2);
/*  88 */         inventory.setItem(24, glass2);
/*  89 */         inventory.setItem(11, new ItemStack(Material.AIR));
/*  90 */         inventory.setItem(15, new ItemStack(Material.AIR));
/*     */         
/*  92 */         chestBlock.getInventory().setContents(inventory.getContents());
/*  93 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  94 */         player.closeInventory();
/*  95 */       } else if (itemName.equals(FrameLandCreative.Color("&aЗаполнить область"))) {
/*  96 */         sign.add("Игровое действие");
/*  97 */         sign.add("Заполнить обл.");
/*  98 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  99 */         if (chest.getState() instanceof Chest) {
/* 100 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 102 */         chest.setType(Material.CHEST);
/* 103 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/* 105 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/* 106 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 107 */         ItemMeta glassMeta = glass.getItemMeta();
/* 108 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/* 109 */         List<String> glassLore = new ArrayList<>();
/* 110 */         glassLore.add(FrameLandCreative.Color("&7"));
/* 111 */         glassMeta.setLore(glassLore);
/* 112 */         glass.setItemMeta(glassMeta);
/* 113 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 114 */         ItemMeta glass1Meta = glass1.getItemMeta();
/* 115 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&aКоординаты заполнения от"));
/* 116 */         glass1.setItemMeta(glass1Meta);
/* 117 */         ItemStack glass3 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 118 */         ItemMeta glass3Meta = glass3.getItemMeta();
/* 119 */         glass3Meta.setDisplayName(FrameLandCreative.Color("&aКоординаты заполнения до"));
/* 120 */         glass3.setItemMeta(glass3Meta);
/* 121 */         ItemStack glass2 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)2);
/* 122 */         ItemMeta glass2Meta = glass2.getItemMeta();
/* 123 */         glass2Meta.setDisplayName(FrameLandCreative.Color("&6Тип блока"));
/* 124 */         glass2.setItemMeta(glass2Meta);
/* 125 */         for (int i = 0; i < 27; i++) {
/* 126 */           inventory.setItem(i, glass);
/*     */         }
/* 128 */         inventory.setItem(1, glass1);
/* 129 */         inventory.setItem(19, glass1);
/* 130 */         inventory.setItem(4, glass2);
/* 131 */         inventory.setItem(22, glass2);
/* 132 */         inventory.setItem(7, glass3);
/* 133 */         inventory.setItem(25, glass3);
/* 134 */         inventory.setItem(10, new ItemStack(Material.AIR));
/* 135 */         inventory.setItem(13, new ItemStack(Material.AIR));
/* 136 */         inventory.setItem(16, new ItemStack(Material.AIR));
/*     */         
/* 138 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 139 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 140 */         player.closeInventory();
/*     */       } 
/*     */       
/* 143 */       if (itemName.equals(FrameLandCreative.Color("&eВзамодействие с функциями"))) {
/* 144 */         GUI.funcGUI(player);
/*     */       }
/*     */       
/* 147 */       if (itemName.equals(FrameLandCreative.Color("&cУстановить задержку &7&o(в тиках)"))) {
/* 148 */         sign.add("Игровое действие");
/* 149 */         sign.add("Задержка в тик.");
/* 150 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 151 */         if (chest.getState() instanceof Chest) {
/* 152 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 154 */         chest.setType(Material.CHEST);
/* 155 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/* 157 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/* 158 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 159 */         ItemMeta glassMeta = glass.getItemMeta();
/* 160 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/* 161 */         List<String> glassLore = new ArrayList<>();
/* 162 */         glassLore.add(FrameLandCreative.Color("&7"));
/* 163 */         glassMeta.setLore(glassLore);
/* 164 */         glass.setItemMeta(glassMeta);
/* 165 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 166 */         ItemMeta glass1Meta = glass1.getItemMeta();
/* 167 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&3Число задержки &o(20 = 1сек)"));
/* 168 */         glass1.setItemMeta(glass1Meta);
/* 169 */         for (int i = 0; i < 27; i++) {
/* 170 */           inventory.setItem(i, glass);
/*     */         }
/* 172 */         inventory.setItem(4, glass1);
/* 173 */         inventory.setItem(22, glass1);
/* 174 */         inventory.setItem(13, new ItemStack(Material.AIR));
/* 175 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 176 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 177 */         player.closeInventory();
/* 178 */       } else if (itemName.equals(FrameLandCreative.Color("&cОтменить событие"))) {
/* 179 */         sign.add("Игровое действие");
/* 180 */         sign.add("Отмена события");
/* 181 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 182 */         if (chest.getState() instanceof Chest) {
/* 183 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 185 */         chest.setType(Material.AIR);
/* 186 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 187 */         player.closeInventory();
/* 188 */       } else if (itemName.equals(FrameLandCreative.Color("&cИспользовать функцию"))) {
/* 189 */         sign.add("Игровое действие");
/* 190 */         sign.add("Исп. функцию");
/* 191 */         sign.add(FrameLandCreative.Color("&a&lСинхронно"));
/* 192 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 193 */         if (chest.getState() instanceof Chest) {
/* 194 */           ((Chest)chest.getState()).getInventory().clear();
/*     */         }
/* 196 */         chest.setType(Material.CHEST);
/* 197 */         Chest chestBlock = (Chest)chest.getState();
/*     */         
/* 199 */         Inventory inventory = Bukkit.createInventory(null, 27, "ыыыы");
/* 200 */         ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 201 */         ItemMeta glassMeta = glass.getItemMeta();
/* 202 */         glassMeta.setDisplayName(FrameLandCreative.Color("&7"));
/* 203 */         List<String> glassLore = new ArrayList<>();
/* 204 */         glassLore.add(FrameLandCreative.Color("&7"));
/* 205 */         glassMeta.setLore(glassLore);
/* 206 */         glass.setItemMeta(glassMeta);
/* 207 */         ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
/* 208 */         ItemMeta glass1Meta = glass1.getItemMeta();
/* 209 */         glass1Meta.setDisplayName(FrameLandCreative.Color("&bНазвание функции"));
/* 210 */         glass1.setItemMeta(glass1Meta);
/* 211 */         for (int i = 0; i < 27; i++) {
/* 212 */           inventory.setItem(i, glass);
/*     */         }
/* 214 */         ItemStack modeFunc = new ItemStack(Material.STRUCTURE_VOID);
/* 215 */         ItemMeta modeMeta = modeFunc.getItemMeta();
/* 216 */         modeMeta.setDisplayName(FrameLandCreative.Color("&aСинхронно"));
/* 217 */         List<String> modeLore = new ArrayList<>();
/* 218 */         modeLore.add(FrameLandCreative.Color("&7Нажмите, чтобы переключить функцию:"));
/* 219 */         modeLore.add(FrameLandCreative.Color(""));
/* 220 */         modeLore.add(FrameLandCreative.Color("&aСинхронно"));
/* 221 */         modeLore.add(FrameLandCreative.Color("&8Асинхронно"));
/* 222 */         modeLore.add(FrameLandCreative.Color(""));
/* 223 */         modeMeta.setLore(modeLore);
/* 224 */         modeFunc.setItemMeta(modeMeta);
/* 225 */         inventory.setItem(4, glass1);
/* 226 */         inventory.setItem(22, modeFunc);
/* 227 */         inventory.setItem(13, new ItemStack(Material.AIR));
/* 228 */         chestBlock.getInventory().setContents(inventory.getContents());
/* 229 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 230 */         player.closeInventory();
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\NETHER_BRICK\GUI_Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */