/*    */ package deadpool23232.framelandcreative.Map;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.PlayerData;
/*    */ import deadpool23232.framelandcreative.Configs.WorldData;
/*    */ import deadpool23232.framelandcreative.Configs.WorldList;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.PlayerChangedWorldEvent;
/*    */ 
/*    */ public class UniquePlayer
/*    */   implements Listener
/*    */ {
/* 18 */   FileConfiguration worldConfig = WorldData.get();
/* 19 */   FileConfiguration worldList = WorldList.get();
/* 20 */   FileConfiguration playerData = PlayerData.get();
/*    */   @EventHandler
/*    */   public void onJoin(PlayerChangedWorldEvent event) {
/* 23 */     Player player = event.getPlayer();
/* 24 */     String world = event.getPlayer().getWorld().getName();
/* 25 */     if (world.contains("-world") || world.contains("-code")) {
/* 26 */       String id = world.replace("-world", "").replace("-code", "");
/* 27 */       List<String> uniquePlayers = this.worldConfig.getStringList("worlds." + id + ".uPlayers");
/* 28 */       if (!uniquePlayers.contains(player.getUniqueId().toString())) {
/* 29 */         uniquePlayers.add(player.getUniqueId().toString());
/* 30 */         int pl = this.worldConfig.getInt("worlds." + id + ".uniquePlayers");
/* 31 */         this.worldConfig.set("worlds." + id + ".uniquePlayers", Integer.valueOf(pl + 1));
/* 32 */         this.worldConfig.set("worlds." + id + ".uPlayers", uniquePlayers);
/* 33 */         this.worldList.set("unique." + id, Integer.valueOf(pl + 1));
/* 34 */         List<String> list = new ArrayList<>();
/* 35 */         list.add(id);
/* 36 */         if (!this.playerData.getStringList(player.getUniqueId().toString() + ".unique").isEmpty()) {
/* 37 */           for (String line : this.playerData.getStringList(player.getUniqueId().toString() + ".unique")) {
/* 38 */             list.add(FrameLandCreative.Color(line));
/*    */           }
/*    */         }
/* 41 */         this.playerData.set(player.getUniqueId().toString() + ".unique", list);
/*    */       } 
/* 43 */       List<String> latest = new ArrayList<>();
/* 44 */       List<String> latestOld = this.playerData.getStringList(player.getUniqueId().toString() + ".latest");
/* 45 */       latest.add(id);
/* 46 */       if (!latestOld.isEmpty()) {
/* 47 */         latestOld.remove(id);
/* 48 */         latest.addAll(latestOld);
/*    */       } 
/* 50 */       this.playerData.set(player.getUniqueId().toString() + ".latest", latest);
/* 51 */       WorldData.save();
/* 52 */       WorldList.save();
/* 53 */       PlayerData.save();
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Map\UniquePlayer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */