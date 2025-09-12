/*    */ package deadpool23232.framelandcreative.Map.Functions;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import java.util.List;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.PlayerChangedWorldEvent;
/*    */ import org.bukkit.event.player.PlayerJoinEvent;
/*    */ import org.bukkit.event.player.PlayerQuitEvent;
/*    */ import org.bukkit.scoreboard.DisplaySlot;
/*    */ import org.bukkit.scoreboard.Objective;
/*    */ import org.bukkit.scoreboard.Score;
/*    */ import org.bukkit.scoreboard.Scoreboard;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class ScoreBoard
/*    */   implements Listener
/*    */ {
/* 34 */   public FileConfiguration cfg = FrameLandCreative.getInstance().getConfigFile();
/*    */   
/*    */   public Scoreboard getScoreBoard(String wid, String type) {
/* 37 */     Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
/*    */     
/* 39 */     Objective objective = scoreboard.registerNewObjective("sb", "dummy");
/* 40 */     objective.setDisplayName(FrameLandCreative.Color(this.cfg.getString("scoreboard." + type + ".name")));
/* 41 */     objective.setDisplaySlot(DisplaySlot.SIDEBAR);
/*    */     
/* 43 */     String id = DataConfig.get().getString("registered-worlds." + wid + ".id");
/* 44 */     List<String> lore = this.cfg.getStringList("scoreboard." + type + ".lore");
/* 45 */     int ii = 0;
/* 46 */     for (int i = lore.size(); i > 0; i--) {
/* 47 */       Score score = objective.getScore(FrameLandCreative.Color(lore.get(ii)).replace("#id#", id));
/* 48 */       score.setScore(i);
/* 49 */       ii++;
/*    */     } 
/*    */     
/* 52 */     return scoreboard;
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void joinEvent(PlayerJoinEvent e) {
/* 57 */     Player player = e.getPlayer();
/* 58 */     World world = player.getWorld();
/*    */     
/* 60 */     String wID = world.getName().replace("-world", "").replace("-code", "");
/*    */     
/* 62 */     if (world.getName().contains("-world")) {
/* 63 */       player.setScoreboard(getScoreBoard(wID, "world"));
/* 64 */     } else if (world.getName().contains("-code")) {
/* 65 */       player.setScoreboard(getScoreBoard(wID, "code"));
/*    */     } 
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void worldEvent(PlayerChangedWorldEvent e) {
/* 71 */     Player player = e.getPlayer();
/* 72 */     World world = player.getWorld();
/* 73 */     player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
/*    */     
/* 75 */     String wID = world.getName().replace("-world", "").replace("-code", "");
/*    */     
/* 77 */     if (world.getName().contains("-world")) {
/* 78 */       player.setScoreboard(getScoreBoard(wID, "world"));
/* 79 */     } else if (world.getName().contains("-code")) {
/* 80 */       player.setScoreboard(getScoreBoard(wID, "code"));
/*    */     } 
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void onPlayerQuit(PlayerQuitEvent event) {
/* 86 */     Player player = event.getPlayer();
/* 87 */     player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Map\Functions\ScoreBoard.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */