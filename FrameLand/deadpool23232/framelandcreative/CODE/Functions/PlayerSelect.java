/*    */ package deadpool23232.framelandcreative.CODE.Functions;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.block.Block;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.block.Action;
/*    */ import org.bukkit.event.player.PlayerInteractEvent;
/*    */ 
/*    */ 
/*    */ public class PlayerSelect
/*    */   implements Listener
/*    */ {
/* 19 */   public static Map<Player, Location> blockSelMap = new HashMap<>();
/*    */   
/*    */   @EventHandler
/*    */   public void signEvent(PlayerInteractEvent event) {
/* 23 */     if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
/* 24 */       Block block = event.getClickedBlock();
/* 25 */       if (block.getType() == Material.WALL_SIGN) {
/* 26 */         Player player = event.getPlayer();
/* 27 */         if (player.isSneaking()) {
/* 28 */           if (player.getInventory().getItemInMainHand().getType() == Material.ARROW && 
/* 29 */             player.getInventory().getItemInMainHand().hasItemMeta() && 
/* 30 */             player.getInventory().getItemInMainHand().getItemMeta().getDisplayName()
/* 31 */             .equals(FrameLandCreative.Color("&fСтрелка &cНЕ"))) {
/*    */             return;
/*    */           }
/*    */ 
/*    */           
/* 36 */           if (player.getWorld().getName().contains("-code")) {
/* 37 */             Location blockLoc = block.getLocation();
/* 38 */             Block func = player.getWorld().getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ() + 1);
/* 39 */             if (!func.isEmpty() && (
/* 40 */               func.getType() == Material.COBBLESTONE || func
/* 41 */               .getType() == Material.WOOD || func
/* 42 */               .getType() == Material.IRON_BLOCK || func
/* 43 */               .getType() == Material.NETHER_BRICK)) {
/*    */               
/* 45 */               blockSelMap.put(player, blockLoc);
/* 46 */               PlSel_GUILISTENER.open(player);
/*    */             } 
/*    */           } 
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\PlayerSelect.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */