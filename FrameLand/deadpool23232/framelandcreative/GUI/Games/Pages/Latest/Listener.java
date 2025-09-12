/*    */ package deadpool23232.framelandcreative.GUI.Games.Pages.Latest;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Functions.tpWorld;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.inventory.InventoryClickEvent;
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
/*    */ public class Listener
/*    */   implements Listener
/*    */ {
/* 26 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   @EventHandler
/*    */   public void onMenuClick(InventoryClickEvent event) {
/* 30 */     if (event.getView().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&eСписок игр - последние"))) {
/* 31 */       event.setCancelled(true);
/* 32 */       if (event.getCurrentItem() == null) {
/*    */         return;
/*    */       }
/* 35 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*    */         return;
/*    */       }
/* 38 */       Player player = (Player)event.getWhoClicked();
/*    */       
/* 40 */       ItemStack item = event.getCurrentItem();
/* 41 */       ItemMeta itemMeta = item.getItemMeta();
/*    */       
/* 43 */       if (item.getType() == Material.SKULL_ITEM)
/*    */         try {
/* 45 */           if (((String)itemMeta.getLore().get(0)).substring(0, 8).equals(FrameLandCreative.Color("&8ID: &7"))) {
/* 46 */             player.closeInventory();
/* 47 */             String id = FrameLandCreative.Color(itemMeta.getLore().get(0)).substring(8);
/* 48 */             tpWorld.to(player, id);
/*    */           } 
/* 50 */         } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {} 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\Latest\Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */