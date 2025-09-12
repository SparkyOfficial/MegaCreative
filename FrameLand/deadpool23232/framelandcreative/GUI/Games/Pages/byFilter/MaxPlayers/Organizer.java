/*    */ package deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxPlayers;
/*    */ 
/*    */ import deadpool23232.framelandcreative.GUI.Games.Functions.Filter.MaxPlayers;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxPlayers.Pages.Page1;
/*    */ import java.util.List;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ 
/*    */ public class Organizer
/*    */ {
/*    */   public static List<String> stringList;
/*    */   public static Integer pages;
/*    */   public static Integer totalPages;
/*    */   
/*    */   public static void use(Player player) {
/* 16 */     stringList = MaxPlayers.getMax();
/*    */     
/* 18 */     if (stringList.size() > 14)
/* 19 */     { pages = Integer.valueOf(stringList.size() % 14 + 1); }
/* 20 */     else { pages = Integer.valueOf(1); }
/* 21 */      totalPages = pages;
/* 22 */     Page1.open(player);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\byFilter\MaxPlayers\Organizer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */