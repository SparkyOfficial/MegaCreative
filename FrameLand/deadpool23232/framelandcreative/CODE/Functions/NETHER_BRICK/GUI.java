/*    */ package deadpool23232.framelandcreative.CODE.Functions.NETHER_BRICK;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.inventory.Inventory;
/*    */ import org.bukkit.inventory.InventoryHolder;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.inventory.meta.ItemMeta;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class GUI
/*    */ {
/*    */   public static void firstGUI(Player player) {
/* 29 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&cИгровое действие"));
/*    */     
/* 31 */     ItemStack funcGUI = new ItemStack(Material.REDSTONE_ORE);
/* 32 */     ItemMeta funcMeta = funcGUI.getItemMeta();
/* 33 */     ItemStack worldGUI = new ItemStack(Material.GRASS);
/* 34 */     ItemMeta worldMeta = worldGUI.getItemMeta();
/*    */     
/* 36 */     worldMeta.setDisplayName(FrameLandCreative.Color("&eВзаимодействие с миром"));
/* 37 */     worldGUI.setItemMeta(worldMeta);
/* 38 */     funcMeta.setDisplayName(FrameLandCreative.Color("&eВзамодействие с функциями"));
/* 39 */     funcGUI.setItemMeta(funcMeta);
/*    */     
/* 41 */     gui.setItem(12, worldGUI);
/* 42 */     gui.setItem(14, funcGUI);
/*    */     
/* 44 */     player.openInventory(gui);
/*    */   }
/*    */   
/*    */   public static void worldGUI(Player player) {
/* 48 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&cИгровое действие"));
/*    */     
/* 50 */     ItemStack back = new ItemStack(Material.ARROW);
/* 51 */     ItemMeta backM = back.getItemMeta();
/* 52 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/* 53 */     back.setItemMeta(backM);
/* 54 */     gui.setItem(26, back);
/*    */     
/* 56 */     ItemStack setFunc = new ItemStack(Material.GRASS);
/* 57 */     ItemMeta setMeta = setFunc.getItemMeta();
/* 58 */     ItemStack fillFunc = new ItemStack(Material.STRUCTURE_BLOCK);
/* 59 */     ItemMeta fillMeta = fillFunc.getItemMeta();
/*    */     
/* 61 */     fillMeta.setDisplayName(FrameLandCreative.Color("&aЗаполнить область"));
/* 62 */     fillFunc.setItemMeta(fillMeta);
/* 63 */     setMeta.setDisplayName(FrameLandCreative.Color("&aУстановить блок по координатам"));
/* 64 */     setFunc.setItemMeta(setMeta);
/*    */     
/* 66 */     gui.setItem(1, setFunc);
/* 67 */     gui.setItem(2, fillFunc);
/*    */     
/* 69 */     player.openInventory(gui);
/*    */   }
/*    */   
/*    */   public static void funcGUI(Player player) {
/* 73 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color("&cИгровое действие"));
/*    */     
/* 75 */     ItemStack back = new ItemStack(Material.ARROW);
/* 76 */     ItemMeta backM = back.getItemMeta();
/* 77 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/* 78 */     back.setItemMeta(backM);
/* 79 */     gui.setItem(26, back);
/*    */     
/* 81 */     ItemStack timeFunc = new ItemStack(Material.WATCH);
/* 82 */     ItemMeta timeMeta = timeFunc.getItemMeta();
/* 83 */     ItemStack cancelFunc = new ItemStack(Material.BARRIER);
/* 84 */     ItemMeta cancelMeta = cancelFunc.getItemMeta();
/* 85 */     ItemStack funcFunc = new ItemStack(Material.LAPIS_ORE);
/* 86 */     ItemMeta funcMeta = funcFunc.getItemMeta();
/*    */     
/* 88 */     funcMeta.setDisplayName(FrameLandCreative.Color("&cИспользовать функцию"));
/* 89 */     funcFunc.setItemMeta(funcMeta);
/* 90 */     cancelMeta.setDisplayName(FrameLandCreative.Color("&cОтменить событие"));
/* 91 */     cancelFunc.setItemMeta(cancelMeta);
/* 92 */     timeMeta.setDisplayName(FrameLandCreative.Color("&cУстановить задержку &7&o(в тиках)"));
/* 93 */     timeFunc.setItemMeta(timeMeta);
/*    */     
/* 95 */     gui.setItem(1, timeFunc);
/* 96 */     gui.setItem(2, cancelFunc);
/* 97 */     gui.setItem(3, funcFunc);
/*    */     
/* 99 */     player.openInventory(gui);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\NETHER_BRICK\GUI.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */