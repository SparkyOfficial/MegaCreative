/*    */ package deadpool23232.framelandcreative.CODE;
/*    */ 
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.block.Block;
/*    */ import org.bukkit.scheduler.BukkitRunnable;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Structure
/*    */ {
/*    */   public static void remove(final World world, Location corner1, Location corner2) {
/* 18 */     int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
/* 19 */     int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
/* 20 */     int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
/* 21 */     int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
/* 22 */     int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
/* 23 */     int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
/* 24 */     for (int x = minX; x <= maxX; x++) {
/* 25 */       for (int y = minY; y <= maxY; y++) {
/* 26 */         for (int z = minZ; z <= maxZ; z++) {
/* 27 */           final int finalX = x;
/* 28 */           final int finalY = y;
/* 29 */           final int finalZ = z;
/* 30 */           (new BukkitRunnable()
/*    */             {
/*    */               public void run() {
/* 33 */                 Block block = world.getBlockAt(finalX, finalY, finalZ);
/* 34 */                 if (block.getType() != Material.AIR) {
/* 35 */                   block.setType(Material.AIR);
/*    */                 }
/*    */               }
/* 38 */             }).runTaskLater(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), 1L);
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Structure.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */