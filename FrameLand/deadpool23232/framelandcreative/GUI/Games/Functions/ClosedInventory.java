/*    */ package deadpool23232.framelandcreative.GUI.Games.Functions;
/*    */ 
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
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.inventory.InventoryCloseEvent;
/*    */ import org.bukkit.event.inventory.InventoryOpenEvent;
/*    */ import org.bukkit.inventory.Inventory;
/*    */ import org.bukkit.scheduler.BukkitRunnable;
/*    */ 
/*    */ public class ClosedInventory
/*    */   implements Listener
/*    */ {
/* 27 */   private final Map<Player, Inventory> inventory = new HashMap<>();
/* 28 */   private final Map<Player, Boolean> opened = new HashMap<>();
/*    */   
/*    */   @EventHandler
/*    */   public void onClosedInventory(InventoryCloseEvent event) {
/* 32 */     final Player player = (Player)event.getPlayer();
/* 33 */     this.inventory.put(player, event.getInventory());
/* 34 */     this.opened.put(player, Boolean.valueOf(false));
/*    */     
/* 36 */     (new BukkitRunnable()
/*    */       {
/*    */         public void run() {
/* 39 */           if (!((Boolean)ClosedInventory.this.opened.get(player)).booleanValue()) {
/* 40 */             Page1.page1FirstTime = true;
/* 41 */             Page2.page2FirstTime = true;
/* 42 */             Page3.page3FirstTime = true;
/* 43 */             Page4.page4FirstTime = true;
/* 44 */             Page5.page5FirstTime = true;
/* 45 */             Page6.page6FirstTime = true;
/* 46 */             Page7.page7FirstTime = true;
/* 47 */             Page8.page8FirstTime = true;
/* 48 */             Page9.page9FirstTime = true;
/* 49 */             Page10.page10FirstTime = true;
/*    */           } 
/*    */         }
/* 52 */       }).runTaskLater(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), 10L);
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void onOpenInventory(InventoryOpenEvent event) {
/* 57 */     Player player = (Player)event.getPlayer();
/* 58 */     Inventory eventInventory = event.getInventory();
/* 59 */     if (this.inventory.get(player) == null)
/* 60 */       return;  if (((Inventory)this.inventory.get(player)).getName().equals(eventInventory.getName()))
/* 61 */       this.opened.put(player, Boolean.valueOf(true)); 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Functions\ClosedInventory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */