/*    */ package deadpool23232.framelandcreative.GUI.WorldSettings.Functions;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.GameMode;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.WorldCreator;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class ToCode {
/* 14 */   static FileConfiguration config = DataConfig.get();
/*    */   
/*    */   public static void main(Player player) {
/* 17 */     if (player.getWorld().getName().contains("-world")) {
/* 18 */       String id = player.getWorld().getName().replace("-world", "");
/* 19 */       if (player.getUniqueId().toString().equals(config.getString("registered-worlds." + id + ".author")) || config
/* 20 */         .getStringList("registered-worlds." + id + ".whitelist").contains(player.getUniqueId().toString())) {
/*    */         
/* 22 */         if (config.contains("registered-worlds." + id + ".code")) {
/* 23 */           (new WorldCreator(id + "-code")).createWorld();
/* 24 */           World worldCode = Bukkit.getWorld(id + "-code");
/* 25 */           Location codeTp = new Location(worldCode, worldCode.getSpawnLocation().getX(), worldCode.getSpawnLocation().getY(), worldCode.getSpawnLocation().getZ());
/* 26 */           player.setGameMode(GameMode.CREATIVE);
/* 27 */           player.teleport(codeTp);
/*    */         } else {
/* 29 */           player.sendMessage(FrameLandCreative.Color("&fПроизошла ошибка. Мир с кодом не зарегестрирован.\n&e IDW:" + id + "\n&fОтправьте администраторам скрин с ошибкой"));
/*    */         } 
/*    */       } else {
/* 32 */         player.sendMessage(FrameLandCreative.Color("&fТы должен быть в своём мире, чтобы использовать эту команду"));
/*    */       } 
/* 34 */     } else if (player.getWorld().getName().contains("-code")) {
/* 35 */       player.sendMessage(FrameLandCreative.Color("&fТы уже в коде!"));
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WorldSettings\Functions\ToCode.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */