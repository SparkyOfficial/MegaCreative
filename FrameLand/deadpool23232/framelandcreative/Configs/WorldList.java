/*    */ package deadpool23232.framelandcreative.Configs;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.IOException;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.configuration.file.YamlConfiguration;
/*    */ 
/*    */ public class WorldList {
/*    */   private static File file;
/*    */   private static FileConfiguration customFile;
/*    */   
/*    */   public static void setup() {
/* 14 */     file = new File(Bukkit.getServer().getPluginManager().getPlugin("FrameLandCreative").getDataFolder(), "world-lists.yml");
/*    */     
/* 16 */     if (!file.exists()) {
/*    */       try {
/* 18 */         file.createNewFile();
/* 19 */       } catch (IOException iOException) {}
/*    */     }
/*    */ 
/*    */     
/* 23 */     customFile = (FileConfiguration)YamlConfiguration.loadConfiguration(file);
/*    */   }
/*    */   
/*    */   public static FileConfiguration get() {
/* 27 */     return customFile;
/*    */   }
/*    */   public static void save() {
/*    */     try {
/* 31 */       customFile.save(file);
/* 32 */     } catch (IOException e) {
/* 33 */       System.out.println("Couldn't save file");
/*    */     } 
/*    */   }
/*    */   public static void reload() {
/* 37 */     customFile = (FileConfiguration)YamlConfiguration.loadConfiguration(file);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Configs\WorldList.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */