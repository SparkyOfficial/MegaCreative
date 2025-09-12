/*     */ package deadpool23232.framelandcreative.CODE.Functions.COBBLESTONE;
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
/*     */ public class GUI
/*     */ {
/*     */   public static void firstGUI(Player player) {
/*  31 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&bДействие игрока"));
/*     */     
/*  33 */     ItemStack inventoryGUI = new ItemStack(Material.WORKBENCH);
/*  34 */     ItemMeta inventoryMeta = inventoryGUI.getItemMeta();
/*  35 */     ItemStack commGUI = new ItemStack(Material.BEACON);
/*  36 */     ItemMeta commMeta = commGUI.getItemMeta();
/*  37 */     ItemStack playerGUI = new ItemStack(Material.GOLDEN_APPLE);
/*  38 */     ItemMeta playerMeta = playerGUI.getItemMeta();
/*     */ 
/*     */     
/*  41 */     inventoryMeta.setDisplayName(FrameLandCreative.Color("&aВзаимодействие с инвентарём"));
/*  42 */     inventoryGUI.setItemMeta(inventoryMeta);
/*  43 */     commMeta.setDisplayName(FrameLandCreative.Color("&aКоммуникация"));
/*  44 */     commGUI.setItemMeta(commMeta);
/*  45 */     playerMeta.setDisplayName(FrameLandCreative.Color("&aСостояние игрока"));
/*  46 */     playerGUI.setItemMeta(playerMeta);
/*     */ 
/*     */ 
/*     */     
/*  50 */     gui.setItem(11, inventoryGUI);
/*  51 */     gui.setItem(13, commGUI);
/*  52 */     gui.setItem(15, playerGUI);
/*     */     
/*  54 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public static void playerGUI(Player player) {
/*  58 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&bДействие игрока"));
/*     */     
/*  60 */     ItemStack back = new ItemStack(Material.ARROW);
/*  61 */     ItemMeta backM = back.getItemMeta();
/*  62 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/*  63 */     back.setItemMeta(backM);
/*  64 */     gui.setItem(26, back);
/*     */     
/*  66 */     ItemStack gamemodeFunc = new ItemStack(Material.WORKBENCH);
/*  67 */     ItemMeta gamemodeMeta = gamemodeFunc.getItemMeta();
/*  68 */     ItemStack healthFunc = new ItemStack(Material.APPLE);
/*  69 */     ItemMeta healthMeta = healthFunc.getItemMeta();
/*  70 */     ItemStack teleportFunc = new ItemStack(Material.ENDER_PEARL);
/*  71 */     ItemMeta teleportMeta = teleportFunc.getItemMeta();
/*  72 */     ItemStack addPotionFunc = new ItemStack(Material.POTION);
/*  73 */     ItemMeta addPotionMeta = addPotionFunc.getItemMeta();
/*  74 */     ItemStack removePotionFunc = new ItemStack(Material.GLASS_BOTTLE);
/*  75 */     ItemMeta removePotionMeta = removePotionFunc.getItemMeta();
/*  76 */     ItemStack clearPotionFunc = new ItemStack(Material.STRUCTURE_VOID);
/*  77 */     ItemMeta clearPotionMeta = clearPotionFunc.getItemMeta();
/*     */     
/*  79 */     addPotionMeta.setDisplayName(FrameLandCreative.Color("&bВыдать эффект зелья"));
/*  80 */     addPotionFunc.setItemMeta(addPotionMeta);
/*  81 */     removePotionMeta.setDisplayName(FrameLandCreative.Color("&bУбрать эффект зелья"));
/*  82 */     removePotionFunc.setItemMeta(removePotionMeta);
/*  83 */     clearPotionMeta.setDisplayName(FrameLandCreative.Color("&bОчистить эффекты"));
/*  84 */     clearPotionFunc.setItemMeta(clearPotionMeta);
/*  85 */     teleportMeta.setDisplayName(FrameLandCreative.Color("&bТелепортировать сущность"));
/*  86 */     teleportFunc.setItemMeta(teleportMeta);
/*  87 */     gamemodeMeta.setDisplayName(FrameLandCreative.Color("&bУстановить режим игры"));
/*  88 */     gamemodeFunc.setItemMeta(gamemodeMeta);
/*  89 */     healthMeta.setDisplayName(FrameLandCreative.Color("&bУстановить здоровье"));
/*  90 */     healthFunc.setItemMeta(healthMeta);
/*     */ 
/*     */ 
/*     */     
/*  94 */     gui.setItem(1, gamemodeFunc);
/*  95 */     gui.setItem(2, healthFunc);
/*  96 */     gui.setItem(3, teleportFunc);
/*  97 */     gui.setItem(4, addPotionFunc);
/*  98 */     gui.setItem(5, removePotionFunc);
/*  99 */     gui.setItem(6, clearPotionFunc);
/*     */     
/* 101 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public static void inventoryGUI(Player player) {
/* 105 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&bДействие игрока"));
/*     */     
/* 107 */     ItemStack back = new ItemStack(Material.ARROW);
/* 108 */     ItemMeta backM = back.getItemMeta();
/* 109 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/* 110 */     back.setItemMeta(backM);
/* 111 */     gui.setItem(26, back);
/*     */     
/* 113 */     ItemStack closeFunc = new ItemStack(Material.BARRIER);
/* 114 */     ItemMeta closeMeta = closeFunc.getItemMeta();
/* 115 */     ItemStack openFunc = new ItemStack(Material.CHEST);
/* 116 */     ItemMeta openMeta = openFunc.getItemMeta();
/* 117 */     ItemStack giveFunc = new ItemStack(Material.DIAMOND);
/* 118 */     ItemMeta giveMeta = giveFunc.getItemMeta();
/* 119 */     ItemStack randomFunc = new ItemStack(Material.GOLD_SWORD);
/* 120 */     ItemMeta randomMeta = randomFunc.getItemMeta();
/* 121 */     ItemStack clearFunc = new ItemStack(Material.STRUCTURE_VOID);
/* 122 */     ItemMeta clearMeta = clearFunc.getItemMeta();
/* 123 */     ItemStack deleteFunc = new ItemStack(Material.RED_ROSE);
/* 124 */     ItemMeta deleteMeta = deleteFunc.getItemMeta();
/*     */     
/* 126 */     giveMeta.setDisplayName(FrameLandCreative.Color("&aВыдать предметы"));
/* 127 */     giveFunc.setItemMeta(giveMeta);
/* 128 */     randomMeta.setDisplayName(FrameLandCreative.Color("&aВыдать случайный предмет"));
/* 129 */     randomFunc.setItemMeta(randomMeta);
/* 130 */     clearMeta.setDisplayName(FrameLandCreative.Color("&aОчистить инвентарь"));
/* 131 */     clearFunc.setItemMeta(clearMeta);
/* 132 */     deleteMeta.setDisplayName(FrameLandCreative.Color("&aУдалить предметы"));
/* 133 */     deleteFunc.setItemMeta(deleteMeta);
/* 134 */     closeMeta.setDisplayName(FrameLandCreative.Color("&aЗакрыть инвентарь"));
/* 135 */     closeFunc.setItemMeta(closeMeta);
/* 136 */     openMeta.setDisplayName(FrameLandCreative.Color("&aОткрыть инвентарь"));
/* 137 */     openFunc.setItemMeta(openMeta);
/*     */ 
/*     */     
/* 140 */     gui.setItem(1, closeFunc);
/* 141 */     gui.setItem(2, openFunc);
/* 142 */     gui.setItem(3, giveFunc);
/* 143 */     gui.setItem(4, randomFunc);
/* 144 */     gui.setItem(5, deleteFunc);
/* 145 */     gui.setItem(6, clearFunc);
/*     */     
/* 147 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public static void commGUI(Player player) {
/* 151 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&bДействие игрока"));
/*     */     
/* 153 */     ItemStack back = new ItemStack(Material.ARROW);
/* 154 */     ItemMeta backM = back.getItemMeta();
/* 155 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/* 156 */     back.setItemMeta(backM);
/* 157 */     gui.setItem(26, back);
/*     */     
/* 159 */     ItemStack messageFunc = new ItemStack(Material.PAPER);
/* 160 */     ItemMeta messageMeta = messageFunc.getItemMeta();
/* 161 */     ItemStack gameMsgFunc = new ItemStack(Material.EMPTY_MAP);
/* 162 */     ItemMeta gameMsgMeta = gameMsgFunc.getItemMeta();
/*     */     
/* 164 */     messageMeta.setDisplayName(FrameLandCreative.Color("&eСообщение игроку"));
/* 165 */     messageFunc.setItemMeta(messageMeta);
/* 166 */     gameMsgMeta.setDisplayName(FrameLandCreative.Color("&eСообщение на весь мир"));
/* 167 */     gameMsgFunc.setItemMeta(gameMsgMeta);
/*     */ 
/*     */     
/* 170 */     gui.setItem(1, messageFunc);
/* 171 */     gui.setItem(2, gameMsgFunc);
/*     */     
/* 173 */     player.openInventory(gui);
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\COBBLESTONE\GUI.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */