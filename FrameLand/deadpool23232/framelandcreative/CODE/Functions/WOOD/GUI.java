/*     */ package deadpool23232.framelandcreative.CODE.Functions.WOOD;
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
/*     */ public class GUI
/*     */ {
/*     */   public static void firstGUI(Player player) {
/*  30 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&6Если игрок"));
/*     */     
/*  32 */     ItemStack anyGUI = new ItemStack(Material.WOOD);
/*  33 */     ItemMeta anyMeta = anyGUI.getItemMeta();
/*  34 */     ItemStack inventoryGUI = new ItemStack(Material.CHEST);
/*  35 */     ItemMeta inventoryMeta = inventoryGUI.getItemMeta();
/*  36 */     ItemStack playerGUI = new ItemStack(Material.ARMOR_STAND);
/*  37 */     ItemMeta playerMeta = playerGUI.getItemMeta();
/*     */     
/*  39 */     playerMeta.setDisplayName(FrameLandCreative.Color("&eСостояние"));
/*  40 */     playerGUI.setItemMeta(playerMeta);
/*  41 */     inventoryMeta.setDisplayName(FrameLandCreative.Color("&eИнвентарь"));
/*  42 */     inventoryGUI.setItemMeta(inventoryMeta);
/*  43 */     anyMeta.setDisplayName(FrameLandCreative.Color("&eРазное"));
/*  44 */     anyGUI.setItemMeta(anyMeta);
/*     */     
/*  46 */     gui.setItem(11, anyGUI);
/*  47 */     gui.setItem(13, inventoryGUI);
/*  48 */     gui.setItem(15, playerGUI);
/*     */     
/*  50 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public static void anyGUI(Player player) {
/*  54 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&6Если игрок"));
/*     */     
/*  56 */     ItemStack back = new ItemStack(Material.ARROW);
/*  57 */     ItemMeta backM = back.getItemMeta();
/*  58 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/*  59 */     back.setItemMeta(backM);
/*  60 */     gui.setItem(26, back);
/*     */     
/*  62 */     ItemStack messageFunc = new ItemStack(Material.FEATHER);
/*  63 */     ItemMeta messageMeta = messageFunc.getItemMeta();
/*  64 */     ItemStack nameFunc = new ItemStack(Material.BOOK_AND_QUILL);
/*  65 */     ItemMeta nameMeta = nameFunc.getItemMeta();
/*  66 */     ItemStack radiusFunc = new ItemStack(Material.DIAMOND_ORE);
/*  67 */     ItemMeta radiusMeta = radiusFunc.getItemMeta();
/*  68 */     ItemStack lookingFunc = new ItemStack(Material.STONE_BUTTON);
/*  69 */     ItemMeta lookingMeta = lookingFunc.getItemMeta();
/*  70 */     ItemStack standsFunc = new ItemStack(Material.GRASS);
/*  71 */     ItemMeta standsMeta = standsFunc.getItemMeta();
/*     */     
/*  73 */     lookingMeta.setDisplayName(FrameLandCreative.Color("&fЕсли игрок смотрит на"));
/*  74 */     lookingFunc.setItemMeta(lookingMeta);
/*  75 */     standsMeta.setDisplayName(FrameLandCreative.Color("&fЕсли игрок стоит на"));
/*  76 */     standsFunc.setItemMeta(standsMeta);
/*  77 */     radiusMeta.setDisplayName(FrameLandCreative.Color("&fЕсли игрок рядом с координатами"));
/*  78 */     radiusFunc.setItemMeta(radiusMeta);
/*  79 */     nameMeta.setDisplayName(FrameLandCreative.Color("&fИмя игрока равно"));
/*  80 */     nameFunc.setItemMeta(nameMeta);
/*  81 */     messageMeta.setDisplayName(FrameLandCreative.Color("&fСообщение равно"));
/*  82 */     messageFunc.setItemMeta(messageMeta);
/*     */     
/*  84 */     gui.setItem(1, messageFunc);
/*  85 */     gui.setItem(2, nameFunc);
/*  86 */     gui.setItem(3, standsFunc);
/*  87 */     gui.setItem(4, lookingFunc);
/*  88 */     gui.setItem(5, radiusFunc);
/*     */     
/*  90 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public static void inventoryGUI(Player player) {
/*  94 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&6Если игрок"));
/*     */     
/*  96 */     ItemStack back = new ItemStack(Material.ARROW);
/*  97 */     ItemMeta backM = back.getItemMeta();
/*  98 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/*  99 */     back.setItemMeta(backM);
/* 100 */     gui.setItem(26, back);
/*     */     
/* 102 */     ItemStack hasItemFunc = new ItemStack(Material.IRON_SWORD);
/* 103 */     ItemMeta hasItemMeta = hasItemFunc.getItemMeta();
/* 104 */     ItemStack invHasItemFunc = new ItemStack(Material.CHEST);
/* 105 */     ItemMeta invHasItemMeta = invHasItemFunc.getItemMeta();
/* 106 */     ItemStack itemIsFunc = new ItemStack(Material.CHORUS_FRUIT_POPPED);
/* 107 */     ItemMeta itemIsMeta = itemIsFunc.getItemMeta();
/* 108 */     ItemStack invNameFunc = new ItemStack(Material.SIGN);
/* 109 */     ItemMeta invNameMeta = invNameFunc.getItemMeta();
/* 110 */     ItemStack holdItemMainFunc = new ItemStack(Material.YELLOW_FLOWER);
/* 111 */     ItemMeta holdItemMainMeta = holdItemMainFunc.getItemMeta();
/* 112 */     ItemStack holdItemOffFunc = new ItemStack(Material.RED_ROSE);
/* 113 */     ItemMeta holdItemOffMeta = holdItemOffFunc.getItemMeta();
/*     */     
/* 115 */     holdItemMainMeta.setDisplayName(FrameLandCreative.Color("&aИгрок держит предмет в основной руке"));
/* 116 */     holdItemMainFunc.setItemMeta(holdItemMainMeta);
/* 117 */     holdItemOffMeta.setDisplayName(FrameLandCreative.Color("&aИгрок держит предмет в дополнительной руке"));
/* 118 */     holdItemOffFunc.setItemMeta(holdItemOffMeta);
/* 119 */     hasItemMeta.setDisplayName(FrameLandCreative.Color("&aИгрок имеет предметы"));
/* 120 */     hasItemFunc.setItemMeta(hasItemMeta);
/* 121 */     invHasItemMeta.setDisplayName(FrameLandCreative.Color("&aОткрытый инвентарь имеет предметы"));
/* 122 */     invHasItemFunc.setItemMeta(invHasItemMeta);
/* 123 */     itemIsMeta.setDisplayName(FrameLandCreative.Color("&aПредмет равен"));
/* 124 */     itemIsFunc.setItemMeta(itemIsMeta);
/* 125 */     invNameMeta.setDisplayName(FrameLandCreative.Color("&aНазвание открытого инвентаря равно"));
/* 126 */     invNameFunc.setItemMeta(invNameMeta);
/*     */     
/* 128 */     gui.setItem(1, hasItemFunc);
/* 129 */     gui.setItem(2, invHasItemFunc);
/* 130 */     gui.setItem(3, invNameFunc);
/* 131 */     gui.setItem(4, itemIsFunc);
/* 132 */     gui.setItem(5, holdItemMainFunc);
/* 133 */     gui.setItem(6, holdItemOffFunc);
/*     */     
/* 135 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public static void playerGUI(Player player) {
/* 139 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&6Если игрок"));
/*     */     
/* 141 */     ItemStack back = new ItemStack(Material.ARROW);
/* 142 */     ItemMeta backM = back.getItemMeta();
/* 143 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/* 144 */     back.setItemMeta(backM);
/* 145 */     gui.setItem(26, back);
/*     */     
/* 147 */     ItemStack flyFunc = new ItemStack(Material.ELYTRA);
/* 148 */     ItemMeta flyMeta = flyFunc.getItemMeta();
/* 149 */     ItemStack sneakingFunc = new ItemStack(Material.LEATHER_BOOTS);
/* 150 */     ItemMeta sneakingMeta = sneakingFunc.getItemMeta();
/* 151 */     ItemStack sprintFunc = new ItemStack(Material.GOLD_HOE);
/* 152 */     ItemMeta sprintMeta = sprintFunc.getItemMeta();
/*     */     
/* 154 */     sprintMeta.setDisplayName(FrameLandCreative.Color("&aИгрок бежит"));
/* 155 */     sprintFunc.setItemMeta(sprintMeta);
/* 156 */     sneakingMeta.setDisplayName(FrameLandCreative.Color("&aИгрок крадётся"));
/* 157 */     sneakingFunc.setItemMeta(sneakingMeta);
/* 158 */     flyMeta.setDisplayName(FrameLandCreative.Color("&aИгрок летает"));
/* 159 */     flyFunc.setItemMeta(flyMeta);
/*     */     
/* 161 */     gui.setItem(1, flyFunc);
/* 162 */     gui.setItem(2, sneakingFunc);
/* 163 */     gui.setItem(3, sprintFunc);
/*     */     
/* 165 */     player.openInventory(gui);
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\WOOD\GUI.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */