/*    */ package deadpool23232.framelandcreative.GUI.WorldSettings.Functions;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class SpawnPoint
/*    */ {
/* 12 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfigFile();
/* 13 */   static FileConfiguration dataConfig = DataConfig.get();
/*    */   
/*    */   public static void main(String ID, Player player) {
/* 16 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + ID + ".author"))) {
/* 17 */       World world = player.getWorld();
/* 18 */       if (world.getName().contains("-world")) {
/* 19 */         Location location = new Location(world, player.getLocation().getX(), player.getLocation().getY() + 0.5D, player.getLocation().getZ());
/* 20 */         player.sendMessage(location + "");
/* 21 */         world.setSpawnLocation(location);
/* 22 */         player.sendMessage(FrameLandCreative.Color("&fТочка появления установлена"));
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WorldSettings\Functions\SpawnPoint.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */