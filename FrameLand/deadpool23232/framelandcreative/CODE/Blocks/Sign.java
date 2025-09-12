/*    */ package deadpool23232.framelandcreative.CODE.Blocks;
/*    */ 
/*    */ import java.util.List;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.block.Block;
/*    */ 
/*    */ public class Sign
/*    */ {
/*    */   public static void configSign(Location blockLocation, World world, List<String> lines) {
/* 11 */     Block block = world.getBlockAt(blockLocation);
/* 12 */     if (block.getState() instanceof org.bukkit.block.Sign) {
/* 13 */       org.bukkit.block.Sign sign = (org.bukkit.block.Sign)block.getState();
/* 14 */       for (int i = 0; i < 4; i++) {
/* 15 */         if (lines.size() > i && lines.get(i) != null) {
/* 16 */           sign.setLine(i, lines.get(i));
/*    */         }
/*    */       } 
/* 19 */       sign.update();
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Blocks\Sign.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */