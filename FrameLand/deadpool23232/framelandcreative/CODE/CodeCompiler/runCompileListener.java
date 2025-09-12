/*    */ package deadpool23232.framelandcreative.CODE.CodeCompiler;
/*    */ 
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.EventPriority;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.PlayerChangedWorldEvent;
/*    */ import org.bukkit.event.player.PlayerQuitEvent;
/*    */ 
/*    */ public class runCompileListener
/*    */   implements Listener {
/*    */   @EventHandler(priority = EventPriority.LOWEST)
/*    */   public void onChangingWorld(PlayerChangedWorldEvent event) {
/* 13 */     if (event.getFrom().getName().contains("-code"))
/* 14 */       new CodeCompile(event.getFrom()); 
/*    */   }
/*    */   
/*    */   @EventHandler(priority = EventPriority.LOWEST)
/*    */   public void onLeave(PlayerQuitEvent event) {
/* 19 */     if (event.getPlayer().getWorld().getName().contains("-code"))
/* 20 */       new CodeCompile(event.getPlayer().getWorld()); 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\CodeCompiler\runCompileListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */