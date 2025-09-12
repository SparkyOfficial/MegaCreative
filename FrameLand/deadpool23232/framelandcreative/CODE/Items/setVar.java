/*    */ package deadpool23232.framelandcreative.CODE.Items;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.apache.commons.lang.StringUtils;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.Sound;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.EventPriority;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.AsyncPlayerChatEvent;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.inventory.meta.ItemMeta;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class setVar
/*    */   implements Listener
/*    */ {
/*    */   @EventHandler(priority = EventPriority.LOWEST)
/*    */   public void onChatEvent(AsyncPlayerChatEvent event) {
/* 32 */     Player player = event.getPlayer();
/* 33 */     if (player.getWorld().getName().contains("-code") && 
/* 34 */       player.getInventory().getItemInMainHand() != null) {
/* 35 */       ItemStack item = player.getInventory().getItemInMainHand();
/* 36 */       ItemStack air = new ItemStack(Material.AIR);
/* 37 */       ItemStack newItem = item.clone();
/* 38 */       ItemMeta newMeta = newItem.getItemMeta();
/*    */       
/* 40 */       if (item.getType() == Material.BOOK) {
/* 41 */         event.setCancelled(true);
/* 42 */         player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
/* 43 */         player.sendTitle(FrameLandCreative.Color("&eУстановлено новое значение!"), FrameLandCreative.Color(event.getMessage()), 5, 25, 5);
/* 44 */         player.getInventory().setItemInMainHand(air);
/* 45 */         newMeta.setDisplayName(FrameLandCreative.Color("&f" + event.getMessage()));
/* 46 */         newItem.setItemMeta(newMeta);
/* 47 */         player.getInventory().setItemInMainHand(newItem);
/* 48 */       } else if (item.getType() == Material.SLIME_BALL) {
/* 49 */         event.setCancelled(true);
/* 50 */         if (StringUtils.isNumeric(event.getMessage())) {
/* 51 */           player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
/* 52 */           player.sendTitle(FrameLandCreative.Color("&eУстановлено новое значение!"), FrameLandCreative.Color("&3" + event.getMessage()), 5, 25, 5);
/* 53 */           player.getInventory().setItemInMainHand(air);
/* 54 */           newMeta.setDisplayName(FrameLandCreative.Color("&3" + event.getMessage()));
/* 55 */           newItem.setItemMeta(newMeta);
/* 56 */           player.getInventory().setItemInMainHand(newItem);
/*    */         } else {
/* 58 */           player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
/* 59 */           player.sendTitle(FrameLandCreative.Color("&cНевозможно установить значение!"), FrameLandCreative.Color("&fЗначение &3" + event.getMessage() + "&f не является числом."), 5, 25, 5);
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Items\setVar.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */