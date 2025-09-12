/*    */ package deadpool23232.framelandcreative.Map.Functions;
/*    */ 
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.PlayerChangedWorldEvent;
/*    */ 
/*    */ public class SpawnPoint implements Listener {
/*    */   @EventHandler
/*    */   public void onJoin(PlayerChangedWorldEvent event) {
/* 11 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/* 12 */       Location spawnLoc = event.getPlayer().getLocation();
/* 13 */       event.getPlayer().setBedSpawnLocation(spawnLoc, true);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Map\Functions\SpawnPoint.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */