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
/*    */ public class ID
/*    */   implements Listener
/*    */ {
/* 19 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfigFile();
/* 20 */   static FileConfiguration dataConfig = DataConfig.get();
/* 21 */   static Map<Player, Boolean> id = new HashMap<>();
/*    */   
/*    */   public static void main(Player player) {
/* 24 */     List<String> msg = config.getStringList("message.setID");
/* 25 */     if (!msg.isEmpty()) {
/* 26 */       for (String line : msg) {
/* 27 */         player.sendMessage(FrameLandCreative.Color(line));
/*    */       }
/*    */     }
/* 30 */     id.put(player, Boolean.valueOf(true));
/*    */   }
/*    */   @EventHandler
/*    */   public void onMessage(AsyncPlayerChatEvent event) {
/* 34 */     Player player = event.getPlayer();
/* 35 */     if (ID.id.get(player) != null && (
/* 36 */       (Boolean)ID.id.get(player)).booleanValue()) {
/* 37 */       event.setCancelled(true);
/* 38 */       ID.id.put(player, Boolean.valueOf(false));
/* 39 */       String message = event.getMessage().substring(2, event.getMessage().length() - 1);
/* 40 */       if (player.getWorld().getName().contains("-world") || player.getWorld().getName().contains("-code")) {
/* 41 */         String id = player.getWorld().getName().replace("-world", "").replace("-code", "");
/* 42 */         String args = event.getMessage().substring(2, event.getMessage().length() - 1);
/* 43 */         if (message.equals("cancel")) {
/* 44 */           player.sendMessage(FrameLandCreative.Color("&fОтменена установка айди.\nТекущий айди: " + dataConfig.getString("registered-worlds." + id + ".id")));
/*    */         }
/* 46 */         else if (args.length() <= 17) {
/* 47 */           if (args.matches("[а-яА-Яa-zA-Z0-9_]+")) {
/* 48 */             if (dataConfig.contains("worlds-id." + args)) {
/* 49 */               player.sendMessage("Айди мира " + args + " уже существует.");
/*    */               return;
/*    */             } 
/* 52 */             dataConfig.set("registered-worlds." + id + ".id", message);
/* 53 */             if (dataConfig.contains("worlds-id.old." + id)) {
/* 54 */               dataConfig.set("worlds-id." + dataConfig.getString("worlds-id.old." + id), null);
/*    */             }
/* 56 */             dataConfig.set("worlds-id.old." + id, message);
/* 57 */             dataConfig.set("worlds-id." + message, Integer.valueOf(Integer.parseInt(id)));
/* 58 */             DataConfig.save();
/* 59 */             player.sendMessage(FrameLandCreative.Color("&fАйди установлено: " + dataConfig.getString("registered-worlds." + id + ".id")));
/* 60 */             WorldSettings.main(player, id);
/*    */           } else {
/* 62 */             player.sendMessage(FrameLandCreative.Color("&fАйди содержит запрещённые символы!"));
/*    */           } 
/*    */         } else {
/* 65 */           player.sendMessage(FrameLandCreative.Color("&fДлинна айди не должна превышать 17 символов!"));
/*    */         } 
/*    */       } else {
/*    */         
/* 69 */         player.sendMessage(FrameLandCreative.Color("&fПроизошла ошибка! WID не найден. Пожалуйста, отправьте скрин с ошибкой администраторам! \n&eМир: " + player.getWorld().getName()));
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WorldSettings\Display\ID.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */