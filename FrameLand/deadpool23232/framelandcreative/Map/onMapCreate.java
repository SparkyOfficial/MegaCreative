/*     */ package deadpool23232.framelandcreative.Map;
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import deadpool23232.framelandcreative.Configs.WorldCode;
/*     */ import deadpool23232.framelandcreative.Configs.WorldData;
/*     */ import deadpool23232.framelandcreative.Configs.WorldList;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.World;
/*     */ import org.bukkit.WorldCreator;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ 
/*     */ public class onMapCreate {
/*  21 */   FileConfiguration config = DataConfig.get();
/*  22 */   FileConfiguration worldConfig = WorldData.get();
/*  23 */   FileConfiguration ruleConfig = RuleConfig.get();
/*  24 */   FileConfiguration gamesList = WorldList.get();
/*     */   
/*     */   public onMapCreate(Player player) throws IOException {
/*  27 */     player.sendTitle(FrameLandCreative.Color("&eМир создаётся!"), FrameLandCreative.Color("&7&oПодождите немного..."), 5, 40, 5);
/*  28 */     int ID = this.config.getInt("biggest-id") + 1;
/*  29 */     this.config.set("biggest-id", Integer.valueOf(ID));
/*  30 */     (new WorldCreator("worldTemp")).createWorld();
/*  31 */     (new WorldCreator("codeTemp")).createWorld();
/*  32 */     copyWorld(Bukkit.getWorld("worldTemp"), ID + "-world");
/*  33 */     copyWorld(Bukkit.getWorld("codeTemp"), ID + "-code");
/*  34 */     Location tp = new Location(Bukkit.getServer().getWorld(ID + "-world"), 0.0D, 128.0D, 0.0D);
/*     */     
/*  36 */     this.config.set("registered-worlds." + ID + ".author", player.getUniqueId().toString());
/*  37 */     this.config.set("registered-worlds." + ID + ".name", "&9Игра от игрока #" + ID);
/*  38 */     List<String> lore = new ArrayList<>();
/*  39 */     lore.add(FrameLandCreative.Color("&fИгра от игрока #" + ID));
/*  40 */     this.config.set("registered-worlds." + ID + ".description", lore);
/*  41 */     this.config.set("registered-worlds." + ID + ".world", ID + "-world");
/*  42 */     this.config.set("registered-worlds." + ID + ".code", ID + "-code");
/*  43 */     this.config.set("registered-worlds." + ID + ".id", Integer.toString(ID));
/*  44 */     this.config.set("registered-worlds." + ID + ".opened", Boolean.valueOf(true));
/*  45 */     List<String> blacklist = new ArrayList<>();
/*  46 */     this.config.set("registered-worlds." + ID + ".blacklist", blacklist);
/*  47 */     List<String> whitelist = new ArrayList<>();
/*  48 */     whitelist.add(player.getUniqueId().toString());
/*  49 */     this.config.set("registered-worlds." + ID + ".whitelist", whitelist);
/*     */     
/*  51 */     this.worldConfig.set("worlds." + ID + ".liked", Integer.valueOf(0));
/*  52 */     this.worldConfig.set("worlds." + ID + ".uniquePlayers", Integer.valueOf(0));
/*  53 */     List<String> uniquePlayers = new ArrayList<>();
/*  54 */     this.worldConfig.set("worlds." + ID + ".uPlayers", uniquePlayers);
/*  55 */     List<String> likedPlayers = new ArrayList<>();
/*  56 */     this.worldConfig.set("worlds." + ID + ".lPlayers", likedPlayers);
/*     */     
/*  58 */     this.config.set("worlds-id." + ID, Integer.valueOf(ID));
/*     */     
/*  60 */     this.ruleConfig.set(ID + ".doDaylightCycle", Boolean.valueOf(true));
/*  61 */     this.ruleConfig.set(ID + ".randomTickSpeed", Integer.valueOf(3));
/*  62 */     this.ruleConfig.set(ID + ".doEntityDrops", Boolean.valueOf(true));
/*  63 */     this.ruleConfig.set(ID + ".showDeathMessages", Boolean.valueOf(true));
/*  64 */     this.ruleConfig.set(ID + ".naturalRegeneration", Boolean.valueOf(true));
/*  65 */     this.ruleConfig.set(ID + ".mobGriefing", Boolean.valueOf(true));
/*  66 */     this.ruleConfig.set(ID + ".keepInventory", Boolean.valueOf(false));
/*  67 */     this.ruleConfig.set(ID + ".doWeatherCycle", Boolean.valueOf(true));
/*  68 */     this.ruleConfig.set(ID + ".doMobLoot", Boolean.valueOf(true));
/*  69 */     this.ruleConfig.set(ID + ".doTileDrops", Boolean.valueOf(true));
/*  70 */     this.ruleConfig.set(ID + ".doFireTick", Boolean.valueOf(true));
/*     */     
/*  72 */     this.gamesList.set("liked." + ID, Integer.valueOf(0));
/*  73 */     this.gamesList.set("unique." + ID, Integer.valueOf(0));
/*  74 */     List<String> newest = new ArrayList<>();
/*  75 */     newest.add(Integer.toString(ID));
/*  76 */     List<String> newestOld = this.gamesList.getStringList("newest");
/*  77 */     if (!newestOld.isEmpty()) {
/*  78 */       newest.addAll(newestOld);
/*     */     }
/*  80 */     this.gamesList.set("newest", newest);
/*     */ 
/*     */ 
/*     */     
/*  84 */     List<String> list = new ArrayList<>();
/*  85 */     WorldCode.get().set("worlds." + ID, list);
/*     */     
/*  87 */     CodeFloors.get().set(ID + ".floors", Integer.valueOf(0));
/*     */     
/*  89 */     CodeFloors.save();
/*  90 */     DataConfig.save();
/*  91 */     RuleConfig.save();
/*  92 */     WorldData.save();
/*  93 */     WorldCode.save();
/*  94 */     WorldList.save();
/*  95 */     PlayerData.save();
/*     */     
/*  97 */     player.teleport(tp);
/*     */   }
/*     */ 
/*     */   
/*     */   private static void copyFileStructure(File source, File target) {
/*     */     try {
/* 103 */       ArrayList<String> ignore = new ArrayList<>(Arrays.asList(new String[] { "uid.dat", "session.lock" }));
/* 104 */       if (!ignore.contains(source.getName())) {
/* 105 */         if (source.isDirectory()) {
/* 106 */           if (!target.exists() && 
/* 107 */             !target.mkdirs())
/* 108 */             throw new IOException("Couldn't create world directory!"); 
/* 109 */           String[] files = source.list();
/* 110 */           assert files != null;
/* 111 */           for (String file : files) {
/* 112 */             File srcFile = new File(source, file);
/* 113 */             File destFile = new File(target, file);
/* 114 */             copyFileStructure(srcFile, destFile);
/*     */           } 
/*     */         } else {
/* 117 */           InputStream in = Files.newInputStream(source.toPath(), new java.nio.file.OpenOption[0]);
/* 118 */           OutputStream out = Files.newOutputStream(target.toPath(), new java.nio.file.OpenOption[0]);
/* 119 */           byte[] buffer = new byte[1024];
/*     */           int length;
/* 121 */           while ((length = in.read(buffer)) > 0)
/* 122 */             out.write(buffer, 0, length); 
/* 123 */           in.close();
/* 124 */           out.close();
/*     */         } 
/*     */       }
/* 127 */     } catch (IOException e) {
/* 128 */       throw new RuntimeException(e);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static boolean unloadWorld(World world) {
/* 133 */     return (world != null && Bukkit.getServer().unloadWorld(world, false));
/*     */   }
/*     */   public static void copyWorld(World originalWorld, String newWorldName) {
/* 136 */     copyFileStructure(originalWorld.getWorldFolder(), new File(Bukkit.getWorldContainer(), newWorldName));
/* 137 */     (new WorldCreator(newWorldName)).createWorld();
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Map\onMapCreate.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */