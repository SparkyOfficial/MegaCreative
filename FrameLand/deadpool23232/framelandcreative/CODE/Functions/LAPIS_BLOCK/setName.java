/*    */ package deadpool23232.framelandcreative.CODE.Functions.LAPIS_BLOCK;
/*    */ 
/*    */ import deadpool23232.framelandcreative.CODE.Blocks.Sign;
/*    */ import java.util.ArrayList;
/*    */ import java.util.Arrays;
/*    */ import java.util.List;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.block.Sign;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.block.Action;
/*    */ import org.bukkit.event.player.PlayerInteractEvent;
/*    */ 
/*    */ public class setName
/*    */   implements Listener {
/*    */   @EventHandler
/*    */   public void onInteract(PlayerInteractEvent event) {
/* 20 */     if (event.getPlayer().getWorld().getName().contains("-code") && 
/* 21 */       event.getAction() == Action.RIGHT_CLICK_BLOCK && 
/* 22 */       event.getClickedBlock().getState() instanceof Sign) {
/* 23 */       Sign sign = (Sign)event.getClickedBlock().getState();
/* 24 */       String[] lines = sign.getLines();
/* 25 */       if (lines[0].equals("Функция")) {
/* 26 */         Player player = event.getPlayer();
/* 27 */         if (player.getInventory().getItemInMainHand() != null && player
/* 28 */           .getInventory().getItemInMainHand().getType() != Material.AIR && 
/* 29 */           player.getInventory().getItemInMainHand().hasItemMeta() && 
/* 30 */           player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) {
/* 31 */           String name = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().substring(2);
/* 32 */           List<String> newLines = new ArrayList<>(Arrays.asList(lines));
/* 33 */           newLines.set(1, name);
/* 34 */           Location blockLoc = sign.getLocation();
/* 35 */           Sign.configSign(blockLoc, player.getWorld(), newLines);
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\LAPIS_BLOCK\setName.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */