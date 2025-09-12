/*    */ package deadpool23232.framelandcreative.CODE.Listeners;
/*    */ 
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.block.Action;
/*    */ import org.bukkit.event.player.PlayerInteractEvent;
/*    */ 
/*    */ public class InteractListener
/*    */   implements Listener {
/*    */   @EventHandler
/*    */   public void onPlayerInteract(PlayerInteractEvent event) {
/* 14 */     Player player = event.getPlayer();
/* 15 */     if (player.getWorld().getName().contains("-code")) {
/* 16 */       if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
/*    */         return;
/*    */       }
/* 19 */       Material main = player.getInventory().getItemInMainHand().getType();
/* 20 */       Material off = player.getInventory().getItemInOffHand().getType();
/*    */       
/* 22 */       if (main == Material.WATER_BUCKET || off == Material.WATER_BUCKET || main == Material.LAVA_BUCKET || off == Material.LAVA_BUCKET || main == Material.ITEM_FRAME || off == Material.ITEM_FRAME || main == Material.ARMOR_STAND || off == Material.ARMOR_STAND)
/*    */       {
/*    */ 
/*    */         
/* 26 */         event.setCancelled(true);
/*    */       }
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Listeners\InteractListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */