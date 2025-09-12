/*    */ package deadpool23232.framelandcreative.CODE.GUI;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
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
/*    */ public class Listeners
/*    */   implements Listener
/*    */ {
/* 17 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   @EventHandler
/*    */   public void onMenuClick(InventoryClickEvent event) {
/* 21 */     if (event.getView().getTitle().equalsIgnoreCase("Переменные")) {
/* 22 */       event.setCancelled(true);
/* 23 */       if (event.getCurrentItem() == null) {
/*    */         return;
/*    */       }
/* 26 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*    */         return;
/*    */       }
/* 29 */       ItemStack item = event.getCurrentItem();
/* 30 */       ItemMeta itemMeta = item.getItemMeta();
/* 31 */       String itemName = itemMeta.getDisplayName();
/* 32 */       Player player = (Player)event.getWhoClicked();
/* 33 */       if (itemName == null) {
/*    */         return;
/*    */       }
/* 36 */       if (itemName.equals(FrameLandCreative.Color(this.config.getString("CODE_GUI.name.name"))) || itemName
/* 37 */         .equals(FrameLandCreative.Color(this.config.getString("CODE_GUI.var.name"))) || itemName
/* 38 */         .equals(FrameLandCreative.Color(this.config.getString("CODE_GUI.coord.name"))) || itemName
/* 39 */         .equals(FrameLandCreative.Color(this.config.getString("CODE_GUI.apple.name"))))
/*    */       {
/* 41 */         player.getInventory().addItem(new ItemStack[] { item.clone() });
/*    */       }
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\GUI\Listeners.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */