/*    */ package deadpool23232.framelandcreative.Commands;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.WorldSettings.WorldSettings;
/*    */ import org.bukkit.command.Command;
/*    */ import org.bukkit.command.CommandExecutor;
/*    */ import org.bukkit.command.CommandSender;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class Settings
/*    */   implements CommandExecutor {
/* 14 */   FileConfiguration config = DataConfig.get();
/*    */ 
/*    */   
/*    */   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
/* 18 */     Player player = (Player)sender;
/* 19 */     if (player.getWorld().getName().contains("-world") || player.getWorld().getName().contains("-code")) {
/* 20 */       String id = player.getWorld().getName().replace("-world", "").replace("-code", "");
/* 21 */       if (player.getUniqueId().toString().equals(this.config.getString("registered-worlds." + id + ".author"))) {
/* 22 */         WorldSettings.main(player, id);
/*    */       } else {
/* 24 */         player.sendMessage(FrameLandCreative.Color("&fТы должен быть в своём мире, чтобы использовать эту команду"));
/*    */       } 
/*    */     } else {
/* 27 */       player.sendMessage(FrameLandCreative.Color("&fТы должен быть в своём мире, чтобы использовать эту команду"));
/*    */     } 
/*    */     
/* 30 */     return true;
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Commands\Settings.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */