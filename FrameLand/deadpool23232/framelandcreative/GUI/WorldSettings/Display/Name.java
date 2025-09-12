/*    */ package deadpool23232.framelandcreative.GUI.WorldSettings.Display;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.WorldSettings.WorldSettings;
/*    */ import java.util.HashMap;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.AsyncPlayerChatEvent;
/*    */ 
/*    */ 
/*    */ public class Name
/*    */   implements Listener
/*    */ {
/* 19 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfigFile();
/* 20 */   static FileConfiguration dataConfig = DataConfig.get();
/* 21 */   static Map<Player, Boolean> name = new HashMap<>();
/*    */   
/*    */   public static void main(Player player) {
/* 24 */     List<String> msg = config.getStringList("message.setName");
/* 25 */     if (!msg.isEmpty()) {
/* 26 */       for (String line : msg) {
/* 27 */         player.sendMessage(FrameLandCreative.Color(line));
/*    */       }
/*    */     }
/* 30 */     name.put(player, Boolean.valueOf(true));
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void onMessage(AsyncPlayerChatEvent event) {
/* 35 */     Player player = event.getPlayer();
/* 36 */     if (name.get(player) != null && (
/* 37 */       (Boolean)name.get(player)).booleanValue()) {
/* 38 */       event.setCancelled(true);
/* 39 */       name.put(player, Boolean.valueOf(false));
/* 40 */       String message = event.getMessage().substring(2, event.getMessage().length() - 1);
/* 41 */       if (player.getWorld().getName().contains("-world") || player.getWorld().getName().contains("-code")) {
/* 42 */         String id = player.getWorld().getName().replace("-world", "").replace("-code", "");
/* 43 */         String args = event.getMessage().substring(2, event.getMessage().length() - 1);
/* 44 */         if (message.equals("cancel")) {
/* 45 */           player.sendMessage(FrameLandCreative.Color("&fОтменена установка названия.\nТекущее название: " + dataConfig.getString("registered-worlds." + id + ".name")));
/*    */         }
/* 47 */         else if (args.length() <= 26) {
/* 48 */           dataConfig.set("registered-worlds." + id + ".name", message);
/* 49 */           DataConfig.save();
/* 50 */           player.sendMessage(FrameLandCreative.Color("&fНазвание установлено: " + dataConfig.getString("registered-worlds." + id + ".name")));
/* 51 */           WorldSettings.main(player, id);
/*    */         } else {
/* 53 */           player.sendMessage(FrameLandCreative.Color("&fДлинна названия не должна превышать 26 символов!"));
/*    */         } 
/*    */       } else {
/*    */         
/* 57 */         player.sendMessage(FrameLandCreative.Color("&fПроизошла ошибка! WID не найден. Пожалуйста, отправьте скрин с ошибкой администраторам! \n&eМир: " + player.getWorld().getName()));
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WorldSettings\Display\Name.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */