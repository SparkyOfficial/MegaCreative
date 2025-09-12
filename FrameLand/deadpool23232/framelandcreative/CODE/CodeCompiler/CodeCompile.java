/*    */ package deadpool23232.framelandcreative.CODE.CodeCompiler;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.WorldCode;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.block.Block;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class CodeCompile
/*    */ {
/*    */   public CodeCompile(World world) {
/* 15 */     List<String> code = new ArrayList<>();
/* 16 */     for (int y = 10; y <= 80; y += 10) {
/* 17 */       for (int z = 94; z > 1; z -= 4) {
/* 18 */         List<String> line = new ArrayList<>();
/* 19 */         for (int x = 93; x > -1 && 
/* 20 */           !world.getBlockAt(93, y, z).isEmpty(); x--) {
/*    */ 
/*    */           
/* 23 */           Block funcFind = world.getBlockAt(x, y, z);
/* 24 */           if (!funcFind.isEmpty()) {
/* 25 */             if (GetFunc_new.get(funcFind) != null) {
/* 26 */               line.add(GetFunc_new.get(funcFind));
/* 27 */             } else if (funcFind.getType() == Material.ENDER_STONE) {
/* 28 */               line.add("else");
/*    */             } 
/* 30 */             if (funcFind.getType() == Material.PISTON_BASE && funcFind
/* 31 */               .getData() == 4) {
/* 32 */               line.add("{");
/* 33 */             } else if (funcFind.getType() == Material.PISTON_BASE && funcFind
/* 34 */               .getData() == 5) {
/* 35 */               line.add("}");
/*    */             } 
/*    */           } 
/*    */         } 
/* 39 */         if (!line.isEmpty()) {
/* 40 */           String lineResult = String.join("&", (Iterable)line);
/* 41 */           code.add(lineResult);
/*    */         } 
/*    */       } 
/* 44 */       String id = world.getName().replace("-code", "");
/* 45 */       WorldCode.get().set("worlds." + id, code);
/* 46 */       WorldCode.save();
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\CodeCompiler\CodeCompile.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */