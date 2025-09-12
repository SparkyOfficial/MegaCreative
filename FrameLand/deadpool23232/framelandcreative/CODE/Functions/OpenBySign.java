/*    */ package deadpool23232.framelandcreative.CODE.Functions;
/*    */ import deadpool23232.framelandcreative.CODE.Functions.COBBLESTONE.GUI;
/*    */ import deadpool23232.framelandcreative.CODE.Functions.DIAMOND_BLOCK.GUI;
/*    */ import deadpool23232.framelandcreative.CODE.Functions.NETHER_BRICK.GUI;
/*    */ import deadpool23232.framelandcreative.CODE.Functions.WOOD.GUI;
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
/*    */ public class OpenBySign implements Listener {
/* 19 */   public static Map<Player, Location> blockMap = new HashMap<>();
/*    */   
/*    */   @EventHandler
/*    */   public void signEvent(PlayerInteractEvent event) {
/* 23 */     if (event.getPlayer().getWorld().getName().contains("-code") && 
/* 24 */       event.getAction() == Action.RIGHT_CLICK_BLOCK) {
/* 25 */       Block block = event.getClickedBlock();
/* 26 */       if (block.getType() == Material.WALL_SIGN) {
/* 27 */         Player player = event.getPlayer();
/* 28 */         if (!player.isSneaking()) {
/* 29 */           if (player.getInventory().getItemInMainHand().getType() == Material.ARROW && 
/* 30 */             player.getInventory().getItemInMainHand().hasItemMeta() && 
/* 31 */             player.getInventory().getItemInMainHand().getItemMeta().getDisplayName()
/* 32 */             .equals(FrameLandCreative.Color("&fСтрелка &cНЕ"))) {
/*    */             return;
/*    */           }
/*    */ 
/*    */           
/* 37 */           Location blockLoc = block.getLocation();
/* 38 */           Block func = player.getWorld().getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ() + 1);
/* 39 */           if (!func.isEmpty()) {
/* 40 */             blockMap.put(player, blockLoc);
/* 41 */             if (func.getType() == Material.DIAMOND_BLOCK) {
/* 42 */               GUI.firstGUI(player);
/* 43 */             } else if (func.getType() == Material.COBBLESTONE) {
/* 44 */               GUI.firstGUI(player);
/* 45 */             } else if (func.getType() == Material.NETHER_BRICK) {
/* 46 */               GUI.firstGUI(player);
/* 47 */             } else if (func.getType() == Material.WOOD) {
/* 48 */               GUI.firstGUI(player);
/* 49 */             } else if (func.getType() != Material.OBSIDIAN) {
/*    */               
/* 51 */               if (func.getType() == Material.IRON_BLOCK);
/*    */             } 
/*    */           } 
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\OpenBySign.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */