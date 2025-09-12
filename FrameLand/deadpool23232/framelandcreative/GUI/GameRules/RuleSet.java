/*     */ package deadpool23232.framelandcreative.GUI.GameRules;
/*     */ 
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import deadpool23232.framelandcreative.Configs.RuleConfig;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import org.bukkit.World;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class RuleSet
/*     */ {
/*  16 */   static FileConfiguration config = RuleConfig.get();
/*  17 */   static FileConfiguration dataConfig = DataConfig.get();
/*     */   public static void doDaylightCycle(Player player, String id) {
/*  19 */     World world = player.getWorld();
/*  20 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + id + ".author"))) {
/*  21 */       if (config.getBoolean(id + ".doDaylightCycle")) {
/*  22 */         world.setGameRuleValue("doDaylightCycle", "false");
/*  23 */         config.set(id + ".doDaylightCycle", Boolean.valueOf(false));
/*  24 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoDaylightCycle &cfalse"));
/*     */       } else {
/*  26 */         world.setGameRuleValue("doDaylightCycle", "true");
/*  27 */         config.set(id + ".doDaylightCycle", Boolean.valueOf(true));
/*  28 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoDaylightCycle &atrue"));
/*     */       } 
/*  30 */       RuleConfig.save();
/*  31 */       GameRules.main(player, id);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void doEntityDrops(Player player, String id) {
/*  36 */     World world = player.getWorld();
/*  37 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + id + ".author"))) {
/*  38 */       if (config.getBoolean(id + ".doEntityDrops")) {
/*  39 */         world.setGameRuleValue("doEntityDrops", "false");
/*  40 */         config.set(id + ".doEntityDrops", Boolean.valueOf(false));
/*  41 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoEntityDrops &cfalse"));
/*     */       } else {
/*  43 */         world.setGameRuleValue("doEntityDrops", "true");
/*  44 */         config.set(id + ".doEntityDrops", Boolean.valueOf(true));
/*  45 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoEntityDrops &atrue"));
/*     */       } 
/*  47 */       RuleConfig.save();
/*  48 */       GameRules.main(player, id);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void showDeathMessages(Player player, String id) {
/*  53 */     World world = player.getWorld();
/*  54 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + id + ".author"))) {
/*  55 */       if (config.getBoolean(id + ".showDeathMessages")) {
/*  56 */         world.setGameRuleValue("showDeathMessages", "false");
/*  57 */         config.set(id + ".showDeathMessages", Boolean.valueOf(false));
/*  58 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &eshowDeathMessages &cfalse"));
/*     */       } else {
/*  60 */         world.setGameRuleValue("showDeathMessages", "true");
/*  61 */         config.set(id + ".showDeathMessages", Boolean.valueOf(true));
/*  62 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &eshowDeathMessages &atrue"));
/*     */       } 
/*  64 */       RuleConfig.save();
/*  65 */       GameRules.main(player, id);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void naturalRegeneration(Player player, String id) {
/*  70 */     World world = player.getWorld();
/*  71 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + id + ".author"))) {
/*  72 */       if (config.getBoolean(id + ".naturalRegeneration")) {
/*  73 */         world.setGameRuleValue("naturalRegeneration", "false");
/*  74 */         config.set(id + ".naturalRegeneration", Boolean.valueOf(false));
/*  75 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &enaturalRegeneration &cfalse"));
/*     */       } else {
/*  77 */         world.setGameRuleValue("naturalRegeneration", "true");
/*  78 */         config.set(id + ".naturalRegeneration", Boolean.valueOf(true));
/*  79 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &enaturalRegeneration &atrue"));
/*     */       } 
/*  81 */       RuleConfig.save();
/*  82 */       GameRules.main(player, id);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void mobGriefing(Player player, String id) {
/*  87 */     World world = player.getWorld();
/*  88 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + id + ".author"))) {
/*  89 */       if (config.getBoolean(id + ".mobGriefing")) {
/*  90 */         world.setGameRuleValue("mobGriefing", "false");
/*  91 */         config.set(id + ".mobGriefing", Boolean.valueOf(false));
/*  92 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &emobGriefing &cfalse"));
/*     */       } else {
/*  94 */         world.setGameRuleValue("mobGriefing", "true");
/*  95 */         config.set(id + ".mobGriefing", Boolean.valueOf(true));
/*  96 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &emobGriefing &atrue"));
/*     */       } 
/*  98 */       RuleConfig.save();
/*  99 */       GameRules.main(player, id);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void keepInventory(Player player, String id) {
/* 104 */     World world = player.getWorld();
/* 105 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + id + ".author"))) {
/* 106 */       if (config.getBoolean(id + ".keepInventory")) {
/* 107 */         world.setGameRuleValue("keepInventory", "false");
/* 108 */         config.set(id + ".keepInventory", Boolean.valueOf(false));
/* 109 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &ekeepInventory &cfalse"));
/*     */       } else {
/* 111 */         world.setGameRuleValue("keepInventory", "true");
/* 112 */         config.set(id + ".keepInventory", Boolean.valueOf(true));
/* 113 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &ekeepInventory &atrue"));
/*     */       } 
/* 115 */       RuleConfig.save();
/* 116 */       GameRules.main(player, id);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void doWeatherCycle(Player player, String id) {
/* 121 */     World world = player.getWorld();
/* 122 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + id + ".author"))) {
/* 123 */       if (config.getBoolean(id + ".doWeatherCycle")) {
/* 124 */         world.setGameRuleValue("doWeatherCycle", "false");
/* 125 */         config.set(id + ".doWeatherCycle", Boolean.valueOf(false));
/* 126 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoWeatherCycle &cfalse"));
/*     */       } else {
/* 128 */         world.setGameRuleValue("doWeatherCycle", "true");
/* 129 */         config.set(id + ".doWeatherCycle", Boolean.valueOf(true));
/* 130 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoWeatherCycle &atrue"));
/*     */       } 
/* 132 */       RuleConfig.save();
/* 133 */       GameRules.main(player, id);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void doMobLoot(Player player, String id) {
/* 138 */     World world = player.getWorld();
/* 139 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + id + ".author"))) {
/* 140 */       if (config.getBoolean(id + ".doMobLoot")) {
/* 141 */         world.setGameRuleValue("doMobLoot", "false");
/* 142 */         config.set(id + ".doMobLoot", Boolean.valueOf(false));
/* 143 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoMobLoot &cfalse"));
/*     */       } else {
/* 145 */         world.setGameRuleValue("doMobLoot", "true");
/* 146 */         config.set(id + ".doMobLoot", Boolean.valueOf(true));
/* 147 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoMobLoot &atrue"));
/*     */       } 
/* 149 */       RuleConfig.save();
/* 150 */       GameRules.main(player, id);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void doTileDrops(Player player, String id) {
/* 155 */     World world = player.getWorld();
/* 156 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + id + ".author"))) {
/* 157 */       if (config.getBoolean(id + ".doTileDrops")) {
/* 158 */         world.setGameRuleValue("doTileDrops", "false");
/* 159 */         config.set(id + ".doTileDrops", Boolean.valueOf(false));
/* 160 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoTileDrops &cfalse"));
/*     */       } else {
/* 162 */         world.setGameRuleValue("doTileDrops", "true");
/* 163 */         config.set(id + ".doTileDrops", Boolean.valueOf(true));
/* 164 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoTileDrops &atrue"));
/*     */       } 
/* 166 */       RuleConfig.save();
/* 167 */       GameRules.main(player, id);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void doFireTick(Player player, String id) {
/* 172 */     World world = player.getWorld();
/* 173 */     if (player.getUniqueId().toString().equals(dataConfig.getString("registered-worlds." + id + ".author"))) {
/* 174 */       if (config.getBoolean(id + ".doFireTick")) {
/* 175 */         world.setGameRuleValue("doFireTick", "false");
/* 176 */         config.set(id + ".doFireTick", Boolean.valueOf(false));
/* 177 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoFireTick &cfalse"));
/*     */       } else {
/* 179 */         world.setGameRuleValue("doFireTick", "true");
/* 180 */         config.set(id + ".doFireTick", Boolean.valueOf(true));
/* 181 */         player.sendMessage(FrameLandCreative.Color("&f Установлено значение &edoFireTick &atrue"));
/*     */       } 
/* 183 */       RuleConfig.save();
/* 184 */       GameRules.main(player, id);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\GameRules\RuleSet.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */