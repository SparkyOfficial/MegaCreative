/*    */ package deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.savedPages;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page1;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page10;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page2;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page3;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page4;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page5;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page6;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page7;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page8;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page9;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.inventory.Inventory;
/*    */ import org.bukkit.inventory.InventoryHolder;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ 
/*    */ public class Pages {
/*    */   public static void Page1(Player player) {
/* 21 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/* 22 */     gui.setContents((ItemStack[])Page1.page1Map.get(player));
/* 23 */     player.openInventory(gui);
/*    */   }
/*    */   public static void Page2(Player player) {
/* 26 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/* 27 */     gui.setContents((ItemStack[])Page2.page2Map.get(player));
/* 28 */     player.openInventory(gui);
/*    */   }
/*    */   public static void Page3(Player player) {
/* 31 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/* 32 */     gui.setContents((ItemStack[])Page3.page3Map.get(player));
/* 33 */     player.openInventory(gui);
/*    */   }
/*    */   public static void Page4(Player player) {
/* 36 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/* 37 */     gui.setContents((ItemStack[])Page4.page4Map.get(player));
/* 38 */     player.openInventory(gui);
/*    */   }
/*    */   public static void Page5(Player player) {
/* 41 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/* 42 */     gui.setContents((ItemStack[])Page5.page5Map.get(player));
/* 43 */     player.openInventory(gui);
/*    */   }
/*    */   public static void Page6(Player player) {
/* 46 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/* 47 */     gui.setContents((ItemStack[])Page6.page6Map.get(player));
/* 48 */     player.openInventory(gui);
/*    */   }
/*    */   public static void Page7(Player player) {
/* 51 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/* 52 */     gui.setContents((ItemStack[])Page7.page7Map.get(player));
/* 53 */     player.openInventory(gui);
/*    */   }
/*    */   public static void Page8(Player player) {
/* 56 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/* 57 */     gui.setContents((ItemStack[])Page8.page8Map.get(player));
/* 58 */     player.openInventory(gui);
/*    */   }
/*    */   public static void Page9(Player player) {
/* 61 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/* 62 */     gui.setContents((ItemStack[])Page9.page9Map.get(player));
/* 63 */     player.openInventory(gui);
/*    */   }
/*    */   public static void Page10(Player player) {
/* 66 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - по количеству лайков"));
/* 67 */     gui.setContents((ItemStack[])Page10.page10Map.get(player));
/* 68 */     player.openInventory(gui);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\byFilter\MaxLiked\Pages\savedPages\Pages.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */