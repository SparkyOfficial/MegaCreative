/*    */ package deadpool23232.framelandcreative.CODE.Items;
/*    */ 
/*    */ import deadpool23232.framelandcreative.CODE.Blocks.Sign;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import java.util.Arrays;
/*    */ import java.util.List;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.block.Block;
/*    */ import org.bukkit.block.Sign;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.block.Action;
/*    */ import org.bukkit.event.player.PlayerInteractEvent;
/*    */ 
/*    */ 
/*    */ public class arrowNO
/*    */   implements Listener
/*    */ {
/* 20 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   @EventHandler
/*    */   public void onUse(PlayerInteractEvent event) {
/* 24 */     if (event.getAction() == Action.RIGHT_CLICK_BLOCK && 
/* 25 */       event.getPlayer().getWorld().getName().contains("-code") && 
/* 26 */       event.getPlayer().getInventory().getItemInMainHand() != null && 
/* 27 */       event.getPlayer().getInventory().getItemInMainHand().hasItemMeta() && 
/* 28 */       event.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasDisplayName() && 
/* 29 */       event.getPlayer().getInventory().getItemInMainHand().getType() == Material.ARROW && 
/* 30 */       event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName()
/* 31 */       .equals(FrameLandCreative.Color(this.config.getString("CODE.items.arrowNO.name")))) {
/* 32 */       Block check = event.getPlayer().getWorld().getBlockAt(event.getClickedBlock().getLocation().getBlockX(), event.getClickedBlock().getLocation().getBlockY(), event.getClickedBlock().getLocation().getBlockZ() + 1);
/* 33 */       if (check.getType() == Material.WOOD && 
/* 34 */         event.getClickedBlock().getState() instanceof Sign) {
/* 35 */         Sign sign = (Sign)event.getClickedBlock().getState();
/* 36 */         List<String> newLines = Arrays.asList(sign.getLines());
/* 37 */         if (((String)newLines.get(2)).equals(FrameLandCreative.Color("&c&lНЕ"))) {
/* 38 */           newLines.set(2, "");
/* 39 */         } else if (((String)newLines.get(2)).equals("")) {
/* 40 */           newLines.set(2, FrameLandCreative.Color("&c&lНЕ"));
/*    */         } 
/* 42 */         Sign.configSign(sign.getLocation(), event.getPlayer().getWorld(), newLines);
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Items\arrowNO.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */