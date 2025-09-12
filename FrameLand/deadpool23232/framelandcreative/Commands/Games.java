/*    */ package deadpool23232.framelandcreative.Commands;
/*    */ 
/*    */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Organizer;
/*    */ import org.bukkit.command.Command;
/*    */ import org.bukkit.command.CommandExecutor;
/*    */ import org.bukkit.command.CommandSender;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ 
/*    */ public class Games
/*    */   implements CommandExecutor
/*    */ {
/*    */   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
/* 14 */     Player player = (Player)sender;
/* 15 */     Organizer.use(player);
/* 16 */     return false;
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Commands\Games.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */