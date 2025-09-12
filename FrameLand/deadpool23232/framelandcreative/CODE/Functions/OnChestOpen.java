/*    */ package deadpool23232.framelandcreative.CODE.Functions;
/*    */ 
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.block.Block;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.EventPriority;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.block.Action;
/*    */ import org.bukkit.event.player.PlayerInteractEvent;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class OnChestOpen
/*    */   implements Listener
/*    */ {
/* 20 */   public static Map<Player, Location> signLocation = new HashMap<>();
/*    */   
/*    */   @EventHandler(priority = EventPriority.LOWEST)
/*    */   public void a(PlayerInteractEvent event) {
/* 24 */     if (event.getPlayer().getWorld().getName().contains("-code") && 
/* 25 */       event.getAction() == Action.RIGHT_CLICK_BLOCK) {
/* 26 */       Block block = event.getClickedBlock();
/* 27 */       if (block != null && block.getType() == Material.CHEST) {
/* 28 */         Player player = event.getPlayer();
/* 29 */         Location blockLoc = block.getLocation();
/* 30 */         Location signLoc = new Location(player.getWorld(), blockLoc.getBlockX(), (blockLoc.getBlockY() - 1), (blockLoc.getBlockZ() - 1));
/* 31 */         if (!player.getWorld().getBlockAt(signLoc).isEmpty())
/* 32 */           signLocation.put(player, signLoc); 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\OnChestOpen.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */