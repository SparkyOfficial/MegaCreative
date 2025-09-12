/*    */ package deadpool23232.framelandcreative.Commands;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.GameMode;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.WorldCreator;
/*    */ import org.bukkit.command.Command;
/*    */ import org.bukkit.command.CommandExecutor;
/*    */ import org.bukkit.command.CommandSender;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class Code
/*    */   implements CommandExecutor {
/* 18 */   FileConfiguration config = DataConfig.get();
/*    */ 
/*    */   
/*    */   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
/* 22 */     Player player = (Player)sender;
/* 23 */     if (player.getWorld().getName().contains("-world")) {
/* 24 */       String id = player.getWorld().getName().replace("-world", "");
/* 25 */       if (player.getUniqueId().toString().equals(this.config.getString("registered-worlds." + id + ".author")) || this.config
/* 26 */         .getStringList("registered-worlds." + id + ".whitelist").contains(player.getUniqueId().toString())) {
/*    */         
/* 28 */         if (this.config.contains("registered-worlds." + id + ".code")) {
/* 29 */           (new WorldCreator(id + "-code")).createWorld();
/* 30 */           World worldCode = Bukkit.getWorld(id + "-code");
/* 31 */           Location codeTp = new Location(worldCode, worldCode.getSpawnLocation().getX(), worldCode.getSpawnLocation().getY(), worldCode.getSpawnLocation().getZ());
/* 32 */           player.setGameMode(GameMode.CREATIVE);
/* 33 */           player.teleport(codeTp);
/*    */         } else {
/* 35 */           player.sendMessage(FrameLandCreative.Color("&fПроизошла ошибка. Мир с кодом не зарегестрирован.\n&e IDW:" + id + "\n&fОтправьте администраторам скрин с ошибкой"));
/*    */         } 
/*    */       } else {
/* 38 */         player.sendMessage(FrameLandCreative.Color("&fТы должен быть в своём мире, чтобы использовать эту команду"));
/*    */       } 
/* 40 */     } else if (player.getWorld().getName().contains("-code")) {
/* 41 */       player.sendMessage(FrameLandCreative.Color("&fТы уже в коде!"));
/*    */     } else {
/* 43 */       player.sendMessage(FrameLandCreative.Color("&fТы должен быть в своём мире, чтобы использовать эту команду"));
/*    */     } 
/* 45 */     return true;
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Commands\Code.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */