/*    */ package deadpool23232.framelandcreative.GUI.WorldSettings.Display;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.Configs.WorldData;
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
/*    */ public class Description
/*    */   implements Listener
/*    */ {
/* 20 */   static FileConfiguration pluginConfig = FrameLandCreative.getInstance().getConfigFile();
/* 21 */   static FileConfiguration worldConfig = WorldData.get();
/* 22 */   static FileConfiguration config = DataConfig.get();
/* 23 */   static Map<Player, Boolean> desc = new HashMap<>();
/* 24 */   static Map<Player, Integer> lines = new HashMap<>();
/*    */   
/*    */   public static void main(Player player) {
/* 27 */     List<String> msg = pluginConfig.getStringList("message.setDescription");
/* 28 */     if (!msg.isEmpty()) {
/* 29 */       for (String line : msg) {
/* 30 */         player.sendMessage(FrameLandCreative.Color(line));
/*    */       }
/*    */     }
/* 33 */     if (player.getWorld().getName().contains("-world") || player.getWorld().getName().contains("-code")) {
/* 34 */       String id = player.getWorld().getName().replace("-world", "").replace("-code", "");
/* 35 */       List<String> lore = config.getStringList("registered-worlds." + id + ".description");
/* 36 */       config.set("registered-worlds." + id + ".description-old", lore);
/* 37 */       config.set("registered-worlds." + id + ".description", null);
/* 38 */       desc.put(player, Boolean.valueOf(true));
/*    */     } else {
/* 40 */       player.sendMessage(FrameLandCreative.Color("&fПроизошла ошибка! WID не найден. Пожалуйста, отправьте скрин с ошибкой администраторам! \n&eМир: " + player.getWorld().getName()));
/*    */     } 
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void onMessage(AsyncPlayerChatEvent event) {
/* 46 */     Player player = event.getPlayer();
/* 47 */     if (desc.get(player) != null && (
/* 48 */       (Boolean)desc.get(player)).booleanValue()) {
/* 49 */       event.setCancelled(true);
/* 50 */       String message = event.getMessage().substring(2, event.getMessage().length() - 1);
/* 51 */       if (player.getWorld().getName().contains("-world") || player.getWorld().getName().contains("-code")) {
/* 52 */         String id = player.getWorld().getName().replace("-world", "").replace("-code", "");
/* 53 */         List<String> lore = config.getStringList("registered-worlds." + id + ".description");
/* 54 */         if (message.equals("cancel")) {
/* 55 */           List<String> old = config.getStringList("registered-worlds." + id + ".description-old");
/* 56 */           config.set("registered-worlds." + id + ".description", old);
/* 57 */           player.sendMessage(FrameLandCreative.Color("&fОтменена установка описание.\nТекущее описание: "));
/* 58 */           for (String line : old) {
/* 59 */             player.sendMessage(FrameLandCreative.Color(line));
/*    */           }
/* 61 */         } else if (message.length() == 4 && message.contains("stop")) {
/* 62 */           desc.put(player, Boolean.valueOf(false));
/* 63 */           player.sendMessage(FrameLandCreative.Color("&fОписание установлено:\n"));
/* 64 */           for (String line : lore) {
/* 65 */             player.sendMessage(FrameLandCreative.Color(line));
/*    */           }
/* 67 */           WorldSettings.main(player, id);
/* 68 */         } else if (message.length() <= 26) {
/* 69 */           lore.add(message);
/* 70 */           config.set("registered-worlds." + id + ".description", lore);
/* 71 */           player.sendMessage(FrameLandCreative.Color("&fДобавлена строка:\n" + message));
/* 72 */           lines.merge(player, Integer.valueOf(1), Integer::sum);
/* 73 */           if (((Integer)lines.get(player)).intValue() == 6) {
/* 74 */             desc.put(player, Boolean.valueOf(false));
/* 75 */             player.sendMessage(FrameLandCreative.Color("&fОписание установлено:\n"));
/* 76 */             for (String line : lore) {
/* 77 */               player.sendMessage(FrameLandCreative.Color(line));
/*    */             }
/* 79 */             WorldSettings.main(player, id);
/* 80 */             lines.put(player, Integer.valueOf(0));
/*    */           } 
/*    */         } 
/* 83 */         DataConfig.save();
/*    */       } else {
/* 85 */         player.sendMessage(FrameLandCreative.Color("&fПроизошла ошибка! WID не найден. Пожалуйста, отправьте скрин с ошибкой администраторам! \n&eМир: " + player.getWorld().getName()));
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WorldSettings\Display\Description.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */