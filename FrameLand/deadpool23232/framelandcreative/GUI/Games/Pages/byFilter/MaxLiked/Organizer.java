/*    */ package deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked;
/*    */ 
/*    */ import deadpool23232.framelandcreative.GUI.Games.Functions.Filter.MaxLiked;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Pages.Page1;
/*    */ import java.util.List;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class Organizer
/*    */ {
/*    */   public static List<String> stringList;
/*    */   public static Integer pages;
/*    */   public static Integer totalPages;
/*    */   
/*    */   public static void use(Player player) {
/* 15 */     stringList = MaxLiked.getMax();
/*    */     
/* 17 */     if (stringList.size() > 14)
/* 18 */     { pages = Integer.valueOf(stringList.size() % 14 + 1); }
/* 19 */     else { pages = Integer.valueOf(1); }
/* 20 */      totalPages = pages;
/* 21 */     Page1.open(player);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\byFilter\MaxLiked\Organizer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */