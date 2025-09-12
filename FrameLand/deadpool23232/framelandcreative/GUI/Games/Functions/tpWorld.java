/*    */ package deadpool23232.framelandcreative.GUI.Games.Functions;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.WorldCreator;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class tpWorld
/*    */ {
/* 14 */   static FileConfiguration config = DataConfig.get();
/*    */   public static void to(Player player, String world_id) {
/* 16 */     if (!config.contains("worlds-id." + world_id)) { player.sendMessage(FrameLandCreative.Color("&fАйди не найден! Пожалуйста, свяжитесь с администрацией")); return; }
/* 17 */      String id = config.getString("worlds-id." + world_id);
/* 18 */     if (config.getStringList("registered-worlds." + id + ".blacklist").contains(player.getUniqueId().toString())) {
/* 19 */       player.sendMessage(FrameLandCreative.Color("&fТы находишься в чёрном списке этого мира!"));
/* 20 */     } else if (player.getUniqueId().toString().equals(config.getString("registered-worlds." + id + ".author")) || config
/* 21 */       .getStringList("registered-worlds." + id + ".whitelist").contains(player.getUniqueId().toString())) {
/* 22 */       (new WorldCreator(id + "-world")).createWorld();
/* 23 */       World world = Bukkit.getServer().getWorld(id + "-world");
/* 24 */       Location worldTp = new Location(world, world.getSpawnLocation().getX(), world.getSpawnLocation().getY(), world.getSpawnLocation().getZ());
/* 25 */       player.teleport(worldTp);
/* 26 */     } else if (config.getBoolean("registered-worlds." + id + ".opened")) {
/* 27 */       (new WorldCreator(id + "-world")).createWorld();
/* 28 */       World world = Bukkit.getServer().getWorld(id + "-world");
/* 29 */       Location worldTp = new Location(world, world.getSpawnLocation().getX(), world.getSpawnLocation().getY(), world.getSpawnLocation().getZ());
/* 30 */       player.teleport(worldTp);
/*    */     } else {
/* 32 */       player.sendMessage(FrameLandCreative.Color("&fМир закрыт!"));
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Functions\tpWorld.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */