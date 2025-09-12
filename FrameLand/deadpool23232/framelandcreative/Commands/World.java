/*    */ package deadpool23232.framelandcreative.Commands;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.GameMode;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.WorldCreator;
/*    */ import org.bukkit.command.Command;
/*    */ import org.bukkit.command.CommandExecutor;
/*    */ import org.bukkit.command.CommandSender;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class World
/*    */   implements CommandExecutor
/*    */ {
/* 18 */   FileConfiguration config = DataConfig.get();
/*    */   
/*    */   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
/* 21 */     Player player = (Player)sender;
/* 22 */     if (player.getWorld().getName().contains("-code") || player.getWorld().getName().contains("-world")) {
/* 23 */       String id = player.getWorld().getName().replace("-code", "").replace("-world", "");
/* 24 */       if (this.config.contains("registered-worlds." + id + ".world")) {
/* 25 */         (new WorldCreator(id + "-world")).createWorld();
/* 26 */         org.bukkit.World worldCode = Bukkit.getWorld(this.config.getString("registered-worlds." + id + ".world"));
/* 27 */         Location codeTp = new Location(worldCode, worldCode.getSpawnLocation().getX(), worldCode.getSpawnLocation().getY(), worldCode.getSpawnLocation().getZ());
/* 28 */         player.setGameMode(GameMode.ADVENTURE);
/* 29 */         player.getInventory().clear();
/* 30 */         player.teleport(codeTp);
/*    */       } else {
/* 32 */         player.sendMessage(FrameLandCreative.Color("&fПроизошла ошибка. Мир с кодом не зарегестрирован.\n&e IDW:" + id + "\n&fОтправьте администраторам скрин с ошибкой"));
/*    */       } 
/*    */     } else {
/* 35 */       player.sendMessage(FrameLandCreative.Color("&fТы должен быть в игре, чтобы использовать эту команду"));
/*    */     } 
/* 37 */     return true;
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Commands\World.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */