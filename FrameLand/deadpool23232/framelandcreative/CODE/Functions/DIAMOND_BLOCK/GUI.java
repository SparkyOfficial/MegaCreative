/*     */ package deadpool23232.framelandcreative.CODE.Functions.DIAMOND_BLOCK;
/*     */ 
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.InventoryHolder;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class GUI
/*     */ {
/*     */   public static void firstGUI(Player player) {
/*  36 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&bСобытие игрока"));
/*     */     
/*  38 */     ItemStack worldGUI = new ItemStack(Material.POTATO_ITEM);
/*  39 */     ItemMeta worldMeta = worldGUI.getItemMeta();
/*  40 */     ItemStack blockGUI = new ItemStack(Material.COBBLESTONE);
/*  41 */     ItemMeta blockMeta = blockGUI.getItemMeta();
/*  42 */     ItemStack playerGUI = new ItemStack(Material.ARMOR_STAND);
/*  43 */     ItemMeta playerMeta = playerGUI.getItemMeta();
/*  44 */     ItemStack damageGUI = new ItemStack(Material.IRON_SWORD);
/*  45 */     ItemMeta damageMeta = damageGUI.getItemMeta();
/*     */ 
/*     */     
/*  48 */     worldMeta.setDisplayName(FrameLandCreative.Color("&aВзаимодействие с миром"));
/*  49 */     worldGUI.setItemMeta(worldMeta);
/*  50 */     blockMeta.setDisplayName(FrameLandCreative.Color("&aВзаимодействие с блоком"));
/*  51 */     blockGUI.setItemMeta(blockMeta);
/*  52 */     playerMeta.setDisplayName(FrameLandCreative.Color("&aСостояние игрока"));
/*  53 */     playerGUI.setItemMeta(playerMeta);
/*  54 */     damageMeta.setDisplayName(FrameLandCreative.Color("&aУрон сущности"));
/*  55 */     damageGUI.setItemMeta(damageMeta);
/*     */ 
/*     */ 
/*     */     
/*  59 */     gui.setItem(10, worldGUI);
/*  60 */     gui.setItem(12, blockGUI);
/*  61 */     gui.setItem(14, playerGUI);
/*  62 */     gui.setItem(16, damageGUI);
/*     */     
/*  64 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public static void damageGUI(Player player) {
/*  68 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&bСобытие игрока"));
/*     */     
/*  70 */     ItemStack back = new ItemStack(Material.ARROW);
/*  71 */     ItemMeta backM = back.getItemMeta();
/*  72 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/*  73 */     back.setItemMeta(backM);
/*  74 */     gui.setItem(26, back);
/*     */     
/*  76 */     ItemStack pkpFunc = new ItemStack(Material.FERMENTED_SPIDER_EYE);
/*  77 */     ItemMeta pkpMeta = pkpFunc.getItemMeta();
/*  78 */     ItemStack pkmFunc = new ItemStack(Material.IRON_SWORD);
/*  79 */     ItemMeta pkmMeta = pkmFunc.getItemMeta();
/*  80 */     ItemStack pkFunc = new ItemStack(Material.SKULL_ITEM);
/*  81 */     ItemMeta pkMeta = pkFunc.getItemMeta();
/*  82 */     ItemStack mkFunc = new ItemStack(Material.ROTTEN_FLESH);
/*  83 */     ItemMeta mkMeta = mkFunc.getItemMeta();
/*  84 */     ItemStack pdpFunc = new ItemStack(Material.NETHER_BRICK_ITEM);
/*  85 */     ItemMeta pdpMeta = pdpFunc.getItemMeta();
/*  86 */     ItemStack pdmFunc = new ItemStack(Material.WOOD_SWORD);
/*  87 */     ItemMeta pdmMeta = pdmFunc.getItemMeta();
/*  88 */     ItemStack mdpFunc = new ItemStack(Material.RED_ROSE);
/*  89 */     ItemMeta mdpMeta = mdpFunc.getItemMeta();
/*  90 */     ItemStack mdmFunc = new ItemStack(Material.INK_SACK, 1, (short)1);
/*  91 */     ItemMeta mdmMeta = mdmFunc.getItemMeta();
/*     */     
/*  93 */     mdmMeta.setDisplayName(FrameLandCreative.Color("&cСущность ударила сущность"));
/*  94 */     mdmFunc.setItemMeta(mdmMeta);
/*  95 */     pkpMeta.setDisplayName(FrameLandCreative.Color("&cИгрок убил игрока"));
/*  96 */     pkpFunc.setItemMeta(pkpMeta);
/*  97 */     pkmMeta.setDisplayName(FrameLandCreative.Color("&cИгрок убил моба"));
/*  98 */     pkmFunc.setItemMeta(pkmMeta);
/*  99 */     pkMeta.setDisplayName(FrameLandCreative.Color("&cИгрок умер"));
/* 100 */     pkFunc.setItemMeta(pkMeta);
/* 101 */     mkMeta.setDisplayName(FrameLandCreative.Color("&cСущность умерла"));
/* 102 */     mkFunc.setItemMeta(mkMeta);
/* 103 */     pdpMeta.setDisplayName(FrameLandCreative.Color("&cИгрок ударил игрока"));
/* 104 */     pdpFunc.setItemMeta(pdpMeta);
/* 105 */     pdmMeta.setDisplayName(FrameLandCreative.Color("&cИгрок ударил моба"));
/* 106 */     pdmFunc.setItemMeta(pdmMeta);
/* 107 */     mdpMeta.setDisplayName(FrameLandCreative.Color("&cМоб ударил игрока"));
/* 108 */     mdpFunc.setItemMeta(mdpMeta);
/*     */     
/* 110 */     gui.setItem(1, pkpFunc);
/* 111 */     gui.setItem(2, pkmFunc);
/* 112 */     gui.setItem(3, pkFunc);
/* 113 */     gui.setItem(4, mkFunc);
/* 114 */     gui.setItem(5, pdpFunc);
/* 115 */     gui.setItem(6, pdmFunc);
/* 116 */     gui.setItem(10, mdpFunc);
/* 117 */     gui.setItem(11, mdmFunc);
/*     */     
/* 119 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public static void playerGUI(Player player) {
/* 123 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&bСобытие игрока"));
/*     */     
/* 125 */     ItemStack back = new ItemStack(Material.ARROW);
/* 126 */     ItemMeta backM = back.getItemMeta();
/* 127 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/* 128 */     back.setItemMeta(backM);
/* 129 */     gui.setItem(26, back);
/*     */     
/* 131 */     ItemStack movingEvent = new ItemStack(Material.LEATHER_BOOTS);
/* 132 */     ItemMeta movingMeta = movingEvent.getItemMeta();
/* 133 */     ItemStack LMBEvent = new ItemStack(Material.STRUCTURE_VOID);
/* 134 */     ItemMeta LMBMeta = LMBEvent.getItemMeta();
/* 135 */     ItemStack RMBEvent = new ItemStack(Material.BARRIER);
/* 136 */     ItemMeta RMBMeta = RMBEvent.getItemMeta();
/* 137 */     ItemStack invOpenEvent = new ItemStack(Material.CHEST);
/* 138 */     ItemMeta invOpenMeta = invOpenEvent.getItemMeta();
/* 139 */     ItemStack invCloseEvent = new ItemStack(Material.ENDER_CHEST);
/* 140 */     ItemMeta invCloseMeta = invCloseEvent.getItemMeta();
/* 141 */     ItemStack invClickEvent = new ItemStack(Material.BLAZE_POWDER);
/* 142 */     ItemMeta invClickMeta = invClickEvent.getItemMeta();
/* 143 */     ItemStack slotEvent = new ItemStack(Material.ITEM_FRAME);
/* 144 */     ItemMeta slotMeta = slotEvent.getItemMeta();
/*     */     
/* 146 */     slotMeta.setDisplayName(FrameLandCreative.Color("&eИгрок меняет слот"));
/* 147 */     slotEvent.setItemMeta(slotMeta);
/* 148 */     invOpenMeta.setDisplayName(FrameLandCreative.Color("&eИгрок открыл инвентарь"));
/* 149 */     invOpenEvent.setItemMeta(invOpenMeta);
/* 150 */     invCloseMeta.setDisplayName(FrameLandCreative.Color("&eИгрок закрыл инвентарь"));
/* 151 */     invCloseEvent.setItemMeta(invCloseMeta);
/* 152 */     invClickMeta.setDisplayName(FrameLandCreative.Color("&eИгрок кликнул по инвентарю"));
/* 153 */     invClickEvent.setItemMeta(invClickMeta);
/* 154 */     movingMeta.setDisplayName(FrameLandCreative.Color("&eИгрок передвигается"));
/* 155 */     movingEvent.setItemMeta(movingMeta);
/* 156 */     LMBMeta.setDisplayName(FrameLandCreative.Color("&eЛевый клик мыши"));
/* 157 */     LMBEvent.setItemMeta(LMBMeta);
/* 158 */     RMBMeta.setDisplayName(FrameLandCreative.Color("&eПравый клик мыши"));
/* 159 */     RMBEvent.setItemMeta(RMBMeta);
/*     */ 
/*     */     
/* 162 */     gui.setItem(1, movingEvent);
/* 163 */     gui.setItem(2, LMBEvent);
/* 164 */     gui.setItem(3, RMBEvent);
/* 165 */     gui.setItem(4, invClickEvent);
/* 166 */     gui.setItem(5, invCloseEvent);
/* 167 */     gui.setItem(6, invOpenEvent);
/* 168 */     gui.setItem(9, slotEvent);
/*     */     
/* 170 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public static void blockGUI(Player player) {
/* 174 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&bСобытие игрока"));
/*     */     
/* 176 */     ItemStack back = new ItemStack(Material.ARROW);
/* 177 */     ItemMeta backM = back.getItemMeta();
/* 178 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/* 179 */     back.setItemMeta(backM);
/* 180 */     gui.setItem(26, back);
/*     */     
/* 182 */     ItemStack breakEvent = new ItemStack(Material.GOLD_PICKAXE);
/* 183 */     ItemMeta breakMeta = breakEvent.getItemMeta();
/* 184 */     ItemStack placeEvent = new ItemStack(Material.SMOOTH_BRICK);
/* 185 */     ItemMeta placeMeta = placeEvent.getItemMeta();
/*     */ 
/*     */     
/* 188 */     placeMeta.setDisplayName(FrameLandCreative.Color("&aИгрок поставил блок"));
/* 189 */     placeEvent.setItemMeta(placeMeta);
/* 190 */     breakMeta.setDisplayName(FrameLandCreative.Color("&aИгрок разрушил блок"));
/* 191 */     breakEvent.setItemMeta(breakMeta);
/*     */ 
/*     */     
/* 194 */     gui.setItem(1, placeEvent);
/* 195 */     gui.setItem(2, breakEvent);
/*     */     
/* 197 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public static void worldGUI(Player player) {
/* 201 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&bСобытие игрока"));
/*     */     
/* 203 */     ItemStack back = new ItemStack(Material.ARROW);
/* 204 */     ItemMeta backM = back.getItemMeta();
/* 205 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/* 206 */     back.setItemMeta(backM);
/* 207 */     gui.setItem(26, back);
/*     */     
/* 209 */     ItemStack joinEvent = new ItemStack(Material.GRASS);
/* 210 */     ItemMeta joinMeta = joinEvent.getItemMeta();
/* 211 */     ItemStack quitEvent = new ItemStack(Material.BEDROCK);
/* 212 */     ItemMeta quitMeta = joinEvent.getItemMeta();
/* 213 */     ItemStack messageEvent = new ItemStack(Material.FEATHER);
/* 214 */     ItemMeta messageMeta = messageEvent.getItemMeta();
/* 215 */     ItemStack teleportEvent = new ItemStack(Material.ENDER_PEARL);
/* 216 */     ItemMeta teleportMeta = teleportEvent.getItemMeta();
/* 217 */     ItemStack itemPickupEvent = new ItemStack(Material.IRON_INGOT);
/* 218 */     ItemMeta itemPickupMeta = itemPickupEvent.getItemMeta();
/* 219 */     ItemStack itemDropEvent = new ItemStack(Material.BARRIER);
/* 220 */     ItemMeta itemDropMeta = itemDropEvent.getItemMeta();
/*     */     
/* 222 */     teleportMeta.setDisplayName(FrameLandCreative.Color("&bИгрок телепортировался"));
/* 223 */     teleportEvent.setItemMeta(teleportMeta);
/* 224 */     itemPickupMeta.setDisplayName(FrameLandCreative.Color("&bИгрок взял предмет"));
/* 225 */     itemPickupEvent.setItemMeta(itemPickupMeta);
/* 226 */     itemDropMeta.setDisplayName(FrameLandCreative.Color("&bИгрок выбросил предмет"));
/* 227 */     itemDropEvent.setItemMeta(itemDropMeta);
/* 228 */     messageMeta.setDisplayName(FrameLandCreative.Color("&bИгрок написал в чат"));
/* 229 */     messageEvent.setItemMeta(messageMeta);
/* 230 */     joinMeta.setDisplayName(FrameLandCreative.Color("&bВход игрока"));
/* 231 */     joinEvent.setItemMeta(joinMeta);
/* 232 */     quitMeta.setDisplayName(FrameLandCreative.Color("&bВыход игрока"));
/* 233 */     quitEvent.setItemMeta(quitMeta);
/*     */     
/* 235 */     gui.setItem(1, joinEvent);
/* 236 */     gui.setItem(2, quitEvent);
/* 237 */     gui.setItem(3, messageEvent);
/* 238 */     gui.setItem(4, teleportEvent);
/* 239 */     gui.setItem(5, itemDropEvent);
/* 240 */     gui.setItem(6, itemPickupEvent);
/*     */     
/* 242 */     player.openInventory(gui);
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\DIAMOND_BLOCK\GUI.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */