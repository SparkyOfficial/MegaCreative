/*    */ package deadpool23232.framelandcreative.Commands;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.PlayerData;
/*    */ import deadpool23232.framelandcreative.Configs.WorldData;
/*    */ import deadpool23232.framelandcreative.Configs.WorldList;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import org.bukkit.command.Command;
/*    */ import org.bukkit.command.CommandExecutor;
/*    */ import org.bukkit.command.CommandSender;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class Like
/*    */   implements CommandExecutor
/*    */ {
/* 18 */   FileConfiguration worldConfig = WorldData.get();
/* 19 */   FileConfiguration worldList = WorldList.get();
/* 20 */   FileConfiguration playerData = PlayerData.get();
/*    */ 
/*    */   
/*    */   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
/* 24 */     Player player = (Player)sender;
/* 25 */     String world = player.getWorld().getName();
/* 26 */     if (world.contains("-world") || world.contains("-code")) {
/* 27 */       String id = world.replace("-world", "").replace("-code", "");
/* 28 */       List<String> likedPlayers = this.worldConfig.getStringList("worlds." + id + ".lPlayers");
/* 29 */       if (!likedPlayers.contains(player.getUniqueId().toString())) {
/* 30 */         likedPlayers.add(player.getUniqueId().toString());
/* 31 */         int likes = this.worldConfig.getInt("worlds." + id + ".liked");
/* 32 */         this.worldConfig.set("worlds." + id + ".liked", Integer.valueOf(likes + 1));
/* 33 */         this.worldConfig.set("worlds." + id + ".lPlayers", likedPlayers);
/* 34 */         this.worldList.set("liked." + id, Integer.valueOf(likes + 1));
/* 35 */         player.sendMessage(FrameLandCreative.Color("&fВы поставили лайк этому миру!"));
/* 36 */         List<String> list = new ArrayList<>();
/* 37 */         list.add(id);
/* 38 */         if (!this.playerData.getStringList(player.getUniqueId().toString() + ".liked").isEmpty()) {
/* 39 */           for (String line : this.playerData.getStringList(player.getUniqueId().toString() + ".liked")) {
/* 40 */             list.add(FrameLandCreative.Color(line));
/*    */           }
/*    */         }
/* 43 */         this.playerData.set(player.getUniqueId().toString() + ".liked", list);
/* 44 */         List<String> latest = new ArrayList<>();
/* 45 */         List<String> latestOld = this.playerData.getStringList(player.getUniqueId().toString() + ".liked");
/* 46 */         latest.add(id);
/* 47 */         if (!latestOld.isEmpty()) {
/* 48 */           latestOld.remove(id);
/* 49 */           latest.addAll(latestOld);
/*    */         } 
/* 51 */         this.playerData.set(player.getUniqueId().toString() + ".liked", latest);
/*    */       } else {
/* 53 */         player.sendMessage(FrameLandCreative.Color("&fТы уже ставил лайк этому миру!"));
/*    */       } 
/* 55 */       WorldData.save();
/* 56 */       WorldList.save();
/* 57 */       PlayerData.save();
/*    */     } 
/* 59 */     return true;
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Commands\Like.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */