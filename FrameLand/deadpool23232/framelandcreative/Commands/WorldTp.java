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
/*    */ public class WorldTp
/*    */   implements CommandExecutor
/*    */ {
/* 19 */   static FileConfiguration config = DataConfig.get();
/*    */   
/*    */   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
/* 22 */     tpTo(sender, args);
/* 23 */     return true;
/*    */   }
/*    */   
/*    */   public static void tpTo(CommandSender sender, String[] args) {
/* 27 */     Player player = (Player)sender;
/* 28 */     if (args.length != 1) { player.sendMessage(FrameLandCreative.Color("&fУкажите айди мира!")); return; }
/* 29 */      if (!config.contains("worlds-id." + args[0])) { player.sendMessage(FrameLandCreative.Color("&fАйди не найден!")); return; }
/* 30 */      String id = config.getString("worlds-id." + args[0]);
/* 31 */     if (config.getStringList("registered-worlds." + id + ".blacklist").contains(player.getUniqueId().toString())) {
/* 32 */       player.sendMessage(FrameLandCreative.Color("&fТы находишься в чёрном списке этого мира!"));
/* 33 */     } else if (player.getUniqueId().toString().equals(config.getString("registered-worlds." + id + ".author")) || config
/* 34 */       .getStringList("registered-worlds." + id + ".whitelist").contains(player.getUniqueId().toString())) {
/* 35 */       (new WorldCreator(id + "-world")).createWorld();
/* 36 */       World world = Bukkit.getServer().getWorld(id + "-world");
/* 37 */       Location worldTp = new Location(world, world.getSpawnLocation().getX(), world.getSpawnLocation().getY(), world.getSpawnLocation().getZ());
/* 38 */       player.setGameMode(GameMode.ADVENTURE);
/* 39 */       player.teleport(worldTp);
/* 40 */     } else if (config.getBoolean("registered-worlds." + id + ".opened")) {
/* 41 */       (new WorldCreator(id + "-world")).createWorld();
/* 42 */       World world = Bukkit.getServer().getWorld(id + "-world");
/* 43 */       Location worldTp = new Location(world, world.getSpawnLocation().getX(), world.getSpawnLocation().getY(), world.getSpawnLocation().getZ());
/* 44 */       player.teleport(worldTp);
/*    */     } else {
/* 46 */       player.sendMessage(FrameLandCreative.Color("&fМир закрыт!"));
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Commands\WorldTp.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */