/*    */ package deadpool23232.framelandcreative.GUI.Kick;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.Kick.Functions.Kick;
/*    */ import java.util.UUID;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.inventory.InventoryClickEvent;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.inventory.meta.ItemMeta;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Listener
/*    */   implements Listener
/*    */ {
/* 22 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   @EventHandler
/*    */   public void onMenuClick(InventoryClickEvent event) {
/* 26 */     if (event.getView().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&9Выгнать игрока"))) {
/* 27 */       event.setCancelled(true);
/* 28 */       if (event.getCurrentItem() == null) {
/*    */         return;
/*    */       }
/* 31 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*    */         return;
/*    */       }
/*    */       
/* 35 */       Player player = (Player)event.getWhoClicked();
/*    */       
/* 37 */       ItemStack item = event.getCurrentItem();
/* 38 */       ItemMeta itemMeta = item.getItemMeta();
/* 39 */       String itemName = itemMeta.getDisplayName();
/*    */       
/* 41 */       if (item.getType() == Material.SKULL_ITEM) {
/* 42 */         player.closeInventory();
/* 43 */         String uid = itemName.substring(2);
/* 44 */         Player offplayer = Bukkit.getPlayer(UUID.fromString(uid));
/* 45 */         new Kick(offplayer);
/* 46 */         KickPlayers.open(player);
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Kick\Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */