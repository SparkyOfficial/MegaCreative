/*    */ package deadpool23232.framelandcreative.GUI.WorldSettings.Functions;
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.GameMode;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.WorldCreator;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class ToPlay {
/* 11 */   static FileConfiguration config = DataConfig.get();
/*    */   
/*    */   public static void main(Player player) {
/* 14 */     if (player.getWorld().getName().contains("-code") || player.getWorld().getName().contains("-world")) {
/* 15 */       String id = player.getWorld().getName().replace("-code", "").replace("-world", "");
/* 16 */       if (config.contains("registered-worlds." + id + ".world")) {
/* 17 */         (new WorldCreator(id + "-world")).createWorld();
/* 18 */         World worldCode = Bukkit.getWorld(config.getString("registered-worlds." + id + ".world"));
/* 19 */         Location codeTp = new Location(worldCode, worldCode.getSpawnLocation().getX(), worldCode.getSpawnLocation().getY(), worldCode.getSpawnLocation().getZ());
/* 20 */         player.setGameMode(GameMode.ADVENTURE);
/* 21 */         player.teleport(codeTp);
/*    */       } else {
/* 23 */         player.sendMessage(FrameLandCreative.Color("&fПроизошла ошибка. Мир с кодом не зарегестрирован.\n&e IDW:" + id + "\n&fОтправьте администраторам скрин с ошибкой"));
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WorldSettings\Functions\ToPlay.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */