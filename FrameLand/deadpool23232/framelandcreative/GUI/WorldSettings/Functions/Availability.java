/*    */ package deadpool23232.framelandcreative.GUI.WorldSettings.Functions;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.WorldSettings.WorldSettings;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class Availability
/*    */ {
/* 11 */   static FileConfiguration config = DataConfig.get();
/*    */   public static void main(Player player) {
/* 13 */     if (player.getWorld().getName().contains("-world") || player.getWorld().getName().contains("-code")) {
/* 14 */       String id = player.getWorld().getName().replace("-world", "").replace("-code", "");
/* 15 */       if (config.getString("registered-worlds." + id + ".author").equals(player.getUniqueId().toString())) {
/* 16 */         if (config.getBoolean("registered-worlds." + id + ".opened")) {
/* 17 */           config.set("registered-worlds." + id + ".opened", Boolean.valueOf(false));
/* 18 */           player.sendMessage(FrameLandCreative.Color("&fИгра закрыта!"));
/*    */         } else {
/* 20 */           config.set("registered-worlds." + id + ".opened", Boolean.valueOf(true));
/* 21 */           player.sendMessage(FrameLandCreative.Color("&fИгра открыта!"));
/*    */         } 
/* 23 */         WorldSettings.main(player, id);
/* 24 */         DataConfig.save();
/*    */       } else {
/* 26 */         player.sendMessage(FrameLandCreative.Color("&fТолько создатель мира может закрывать мир!"));
/*    */       } 
/*    */     } else {
/* 29 */       player.sendMessage(FrameLandCreative.Color("&fВы должны быть в своём мире, чтобы закрывать его!"));
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WorldSettings\Functions\Availability.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */