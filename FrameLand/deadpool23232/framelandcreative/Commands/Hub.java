/*    */ package deadpool23232.framelandcreative.Commands;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.GameMode;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.WorldCreator;
/*    */ import org.bukkit.command.Command;
/*    */ import org.bukkit.command.CommandExecutor;
/*    */ import org.bukkit.command.CommandSender;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.PlayerJoinEvent;
/*    */ import org.bukkit.event.player.PlayerQuitEvent;
/*    */ 
/*    */ public class Hub
/*    */   implements CommandExecutor, Listener
/*    */ {
/*    */   @EventHandler
/*    */   public void onJoin(PlayerJoinEvent event) {
/* 23 */     if (event.getPlayer().getWorld().getName().contains("-world") && 
/* 24 */       !event.getPlayer().getWorld().getName().equals("hub")) {
/* 25 */       (new WorldCreator("hub")).createWorld();
/* 26 */       World world = Bukkit.getServer().getWorld("hub");
/* 27 */       Location worldTp = new Location(world, 222.5D, 7.0D, 338.5D, -90.0F, 0.0F);
/* 28 */       Player player = event.getPlayer();
/* 29 */       player.getInventory().clear();
/* 30 */       player.setGameMode(GameMode.ADVENTURE);
/* 31 */       player.teleport(worldTp);
/*    */     } 
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void onQuit(PlayerQuitEvent event) {
/* 37 */     Player player = event.getPlayer();
/* 38 */     player.getInventory().clear();
/* 39 */     player.setGameMode(GameMode.ADVENTURE);
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
/* 44 */     (new WorldCreator("hub")).createWorld();
/* 45 */     World world = Bukkit.getServer().getWorld("hub");
/* 46 */     Location worldTp = new Location(world, 222.5D, 7.0D, 338.5D, -90.0F, 0.0F);
/* 47 */     Player player = (Player)sender;
/* 48 */     player.getInventory().clear();
/* 49 */     player.setGameMode(GameMode.ADVENTURE);
/* 50 */     player.teleport(worldTp);
/* 51 */     player.sendMessage(FrameLandCreative.Color("&9Frameland &7» &fВы успешно телепортированы!"));
/* 52 */     return true;
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Commands\Hub.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */