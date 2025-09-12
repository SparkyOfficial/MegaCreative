/*    */ package deadpool23232.framelandcreative.CODE;
/*    */ 
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.block.Block;
/*    */ import org.bukkit.block.Chest;
/*    */ 
/*    */ 
/*    */ public class Fill
/*    */ {
/*    */   public static void fillArea(World world, Location loc1, Location loc2, Material material) {
/* 13 */     int x1 = Math.min(loc1.getBlockX(), loc2.getBlockX());
/* 14 */     int y1 = Math.min(loc1.getBlockY(), loc2.getBlockY());
/* 15 */     int z1 = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
/* 16 */     int x2 = Math.max(loc1.getBlockX(), loc2.getBlockX());
/* 17 */     int y2 = Math.max(loc1.getBlockY(), loc2.getBlockY());
/* 18 */     int z2 = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
/*    */     
/* 20 */     for (int x = x1; x <= x2; x++) {
/* 21 */       for (int y = y1; y <= y2; y++) {
/* 22 */         for (int z = z1; z <= z2; z++) {
/* 23 */           Block block = world.getBlockAt(x, y, z);
/* 24 */           if (block.getState() instanceof Chest) {
/* 25 */             ((Chest)block.getState()).getInventory().clear();
/*    */           }
/* 27 */           block.setType(material);
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Fill.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */