/*    */ package deadpool23232.framelandcreative.GUI.GameRules;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.RuleConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import java.util.List;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.AsyncPlayerChatEvent;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class randomTickSpeed
/*    */   implements Listener
/*    */ {
/* 19 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfigFile();
/* 20 */   static FileConfiguration ruleConfig = RuleConfig.get();
/*    */   static boolean a;
/*    */   
/*    */   public static void main(Player player, String newId) {
/* 24 */     List<String> msg = config.getStringList("gameRules.rts-message");
/* 25 */     if (!msg.isEmpty()) {
/* 26 */       for (String line : msg) {
/* 27 */         player.sendMessage(FrameLandCreative.Color(line));
/*    */       }
/*    */     }
/* 30 */     id = newId;
/* 31 */     a = true;
/*    */   }
/*    */   static String id;
/*    */   @EventHandler
/*    */   public void onMessage(AsyncPlayerChatEvent event) {
/* 36 */     Player player = event.getPlayer();
/* 37 */     if (a) {
/* 38 */       event.setCancelled(true);
/* 39 */       a = false;
/* 40 */       String message = event.getMessage().substring(2);
/* 41 */       if (message.length() == 6 && message.contains("cancel")) {
/* 42 */         player.sendMessage(FrameLandCreative.Color("&fОтменена установка значения.\nТекущее значение: &e" + ruleConfig.getInt(id + ".randomTickSpeed")));
/*    */       } else {
/* 44 */         int num = 3;
/*    */         try {
/* 46 */           num = Integer.parseInt(message);
/* 47 */         } catch (NumberFormatException e) {
/* 48 */           player.sendMessage(FrameLandCreative.Color("&fЗначение не является числом!"));
/*    */           return;
/*    */         } 
/* 51 */         if (Integer.toString(num).length() > 4) {
/* 52 */           num = 9999;
/*    */         }
/* 54 */         ruleConfig.set(id + ".randomTickSpeed", Integer.valueOf(num));
/* 55 */         World world = player.getWorld();
/* 56 */         world.setGameRuleValue("randomTickSpeed", Integer.toString(num));
/* 57 */         RuleConfig.save();
/* 58 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &erandomTickSpeed &b" + num));
/* 59 */         GameRules.main(player, id);
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\GameRules\randomTickSpeed.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */