/*    */ package deadpool23232.framelandcreative.CODE.Items;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.block.Action;
/*    */ import org.bukkit.event.player.PlayerInteractEvent;
/*    */ 
/*    */ 
/*    */ public class Variables
/*    */   implements Listener
/*    */ {
/* 17 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   @EventHandler
/*    */   public void onRightClickItem(PlayerInteractEvent e) {
/* 21 */     Player player = e.getPlayer();
/* 22 */     World world = player.getWorld();
/*    */     
/* 24 */     if (world.getName().contains("-code") && (
/* 25 */       e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && 
/* 26 */       player.getInventory().getItemInMainHand().getType() == Material.IRON_INGOT && 
/* 27 */       player.getInventory().getItemInMainHand().getItemMeta().hasLore()) {
/* 28 */       String name = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
/* 29 */       if (name.equals(FrameLandCreative.Color(this.config.getString("CODE.items.values.name"))))
/* 30 */         deadpool23232.framelandcreative.CODE.GUI.Variables.open(player); 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Items\Variables.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */