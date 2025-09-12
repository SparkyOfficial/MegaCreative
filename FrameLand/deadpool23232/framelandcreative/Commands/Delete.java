/*    */ package deadpool23232.framelandcreative.Commands;
/*    */ import deadpool23232.framelandcreative.Configs.CodeFloors;
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.Configs.PlayerData;
/*    */ import deadpool23232.framelandcreative.Configs.RuleConfig;
/*    */ import deadpool23232.framelandcreative.Configs.WorldCode;
/*    */ import deadpool23232.framelandcreative.Configs.WorldData;
/*    */ import deadpool23232.framelandcreative.Configs.WorldList;
/*    */ import org.bukkit.command.CommandExecutor;
/*    */ import org.bukkit.command.CommandSender;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class Delete implements CommandExecutor {
/*    */   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
/* 15 */     Player player = (Player)sender;
/*    */     
/* 17 */     if (player.hasPermission("framelandcreative.delete") && 
/* 18 */       args.length == 1 && 
/* 19 */       isNumeric(args[0])) {
/* 20 */       String id = args[0];
/*    */       
/* 22 */       CodeFloors.get().set(id, null);
/* 23 */       CodeFloors.save();
/*    */       
/* 25 */       DataConfig.get().set("registered-worlds." + id, null);
/* 26 */       if (DataConfig.get().getInt("biggest-id") == Integer.parseInt(id)) {
/* 27 */         DataConfig.get().set("biggest-id", Integer.valueOf(Integer.parseInt(id) - 1));
/*    */       }
/* 29 */       DataConfig.get().set("worlds-id." + id, null);
/* 30 */       if (DataConfig.get().getString("worlds-id.old." + id) != null) {
/* 31 */         String stID = DataConfig.get().getString("worlds-id.old." + id);
/* 32 */         DataConfig.get().set("worlds-id.old." + id, null);
/* 33 */         DataConfig.get().set("worlds-id." + stID, null);
/*    */       } 
/* 35 */       DataConfig.save();
/*    */       
/* 37 */       RuleConfig.get().set(id, null);
/* 38 */       RuleConfig.save();
/*    */       
/* 40 */       PlayerData.get().getKeys(false).forEach(key -> {
/*    */             PlayerData.get().getStringList(key + ".unique").remove(key);
/*    */             PlayerData.get().getStringList(key + ".latest").remove(key);
/*    */             PlayerData.get().getStringList(key + ".liked").remove(key);
/*    */           });
/* 45 */       PlayerData.save();
/*    */       
/* 47 */       WorldCode.get().set("worlds." + id, null);
/* 48 */       WorldCode.save();
/*    */       
/* 50 */       WorldData.get().set("worlds." + id, null);
/* 51 */       WorldData.save();
/*    */       
/* 53 */       WorldList.get().set("liked." + id, null);
/* 54 */       WorldList.get().set("unique." + id, null);
/* 55 */       WorldList.get().set("newest." + id, null);
/* 56 */       WorldList.save();
/*    */     } 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 62 */     return false;
/*    */   }
/*    */   public static boolean isNumeric(String text) {
/*    */     try {
/* 66 */       Integer.parseInt(text);
/* 67 */       return true;
/* 68 */     } catch (NumberFormatException e) {
/* 69 */       return false;
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Commands\Delete.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */