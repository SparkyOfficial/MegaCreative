/*    */ package deadpool23232.framelandcreative.Functions;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.PlayerData;
/*    */ import me.clip.placeholderapi.PlaceholderAPI;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.PlayerJoinEvent;
/*    */ 
/*    */ public class onJoin
/*    */   implements Listener
/*    */ {
/* 14 */   public FileConfiguration config = PlayerData.get();
/*    */   
/*    */   @EventHandler
/*    */   public void playerOnJoin(PlayerJoinEvent event) {
/* 18 */     Player player = event.getPlayer();
/* 19 */     this.config.set(player.getUniqueId().toString() + ".prefix", PlaceholderAPI.setPlaceholders(player, "%vault_prefix%"));
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Functions\onJoin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */