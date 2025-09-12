/*    */ package deadpool23232.framelandcreative.GUI.Kick.Functions;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.WorldCreator;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Kick
/*    */ {
/* 15 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   public Kick(Player player) {
/* 17 */     String world = this.config.getString("kick-to");
/* 18 */     (new WorldCreator(world)).createWorld();
/* 19 */     World worldTo = Bukkit.getWorld(world);
/* 20 */     Location location = new Location(worldTo, worldTo.getSpawnLocation().getX(), worldTo.getSpawnLocation().getY(), worldTo.getSpawnLocation().getZ());
/* 21 */     player.teleport(location);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Kick\Functions\Kick.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */