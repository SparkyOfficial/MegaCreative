/*    */ package deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.WorldList;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page1;
/*    */ import java.util.List;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class Organizer
/*    */ {
/*    */   public static List<String> stringList;
/*    */   public static Integer pages;
/*    */   public static Integer totalPages;
/* 14 */   public static FileConfiguration config = WorldList.get();
/*    */   
/*    */   public static void use(Player player) {
/* 17 */     stringList = config.getStringList("newest");
/*    */     
/* 19 */     if (stringList.size() > 14)
/* 20 */     { pages = Integer.valueOf(stringList.size() % 14 + 1); }
/* 21 */     else { pages = Integer.valueOf(1); }
/* 22 */      totalPages = pages;
/* 23 */     Page1.open(player);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\byFilter\Newest\Organizer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */