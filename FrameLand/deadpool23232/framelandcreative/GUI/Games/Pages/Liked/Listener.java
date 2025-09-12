/*    */ package deadpool23232.framelandcreative.GUI.Games.Pages.Liked;
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
/*    */ public class Listener
/*    */   implements Listener {
/* 16 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   @EventHandler
/*    */   public void onMenuClick(InventoryClickEvent event) {
/* 20 */     if (event.getView().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&eСписок игр - понравившееся"))) {
/* 21 */       event.setCancelled(true);
/* 22 */       if (event.getCurrentItem() == null) {
/*    */         return;
/*    */       }
/* 25 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*    */         return;
/*    */       }
/* 28 */       Player player = (Player)event.getWhoClicked();
/*    */       
/* 30 */       ItemStack item = event.getCurrentItem();
/* 31 */       ItemMeta itemMeta = item.getItemMeta();
/*    */       
/* 33 */       if (item.getType() == Material.SKULL_ITEM)
/*    */         try {
/* 35 */           if (((String)itemMeta.getLore().get(0)).substring(0, 8).equals(FrameLandCreative.Color("&8ID: &7"))) {
/* 36 */             player.closeInventory();
/* 37 */             String id = FrameLandCreative.Color(itemMeta.getLore().get(0)).substring(8);
/* 38 */             tpWorld.to(player, id);
/*    */           } 
/* 40 */         } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {} 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\Liked\Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */