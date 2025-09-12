/*    */ package deadpool23232.framelandcreative.GUI.Games.Pages.Own;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Functions.tpWorld;
/*    */ import deadpool23232.framelandcreative.Map.onMapCreate;
/*    */ import java.io.IOException;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.inventory.InventoryClickEvent;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.inventory.meta.ItemMeta;
/*    */ 
/*    */ public class Listener
/*    */   implements Listener
/*    */ {
/* 19 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   @EventHandler
/*    */   public void onMenuClick(InventoryClickEvent event) throws IOException {
/* 23 */     if (event.getView().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&eСписок игр - свои миры"))) {
/* 24 */       event.setCancelled(true);
/* 25 */       if (event.getCurrentItem() == null) {
/*    */         return;
/*    */       }
/* 28 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*    */         return;
/*    */       }
/* 31 */       Player player = (Player)event.getWhoClicked();
/*    */       
/* 33 */       ItemStack item = event.getCurrentItem();
/* 34 */       ItemMeta itemMeta = item.getItemMeta();
/* 35 */       String itemName = itemMeta.getDisplayName();
/*    */       
/* 37 */       if (itemName.equals(FrameLandCreative.Color(this.config.getString("newWorld.name")))) {
/* 38 */         player.closeInventory();
/* 39 */         new onMapCreate(player);
/* 40 */       } else if (item.getType() == Material.SKULL_ITEM) {
/*    */         try {
/* 42 */           if (((String)itemMeta.getLore().get(0)).substring(0, 8).equals(FrameLandCreative.Color("&8ID: &7"))) {
/* 43 */             player.closeInventory();
/* 44 */             String id = FrameLandCreative.Color(itemMeta.getLore().get(0)).substring(8);
/* 45 */             tpWorld.to(player, id);
/*    */           } 
/* 47 */         } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {}
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\Own\Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */