/*    */ package deadpool23232.framelandcreative.GUI.WhiteList.Listeners;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.WhiteList.wList.RMB;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.inventory.InventoryClickEvent;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.inventory.meta.ItemMeta;
/*    */ 
/*    */ 
/*    */ public class RightListener
/*    */   implements Listener
/*    */ {
/* 22 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   @EventHandler
/*    */   public void onMenuClick(InventoryClickEvent event) {
/* 26 */     if (event.getView().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&eБелый список: &cУбрать"))) {
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
/* 43 */         String uuid = itemName.substring(2);
/* 44 */         World world = player.getWorld();
/* 45 */         String id = world.getName().replace("-world", "").replace("-code", "");
/* 46 */         List<String> list = new ArrayList<>(DataConfig.get().getStringList("registered-worlds." + id + ".whitelist"));
/* 47 */         list.remove(uuid);
/* 48 */         DataConfig.get().set("registered-worlds." + id + ".whitelist", list);
/* 49 */         RMB.open(player);
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WhiteList\Listeners\RightListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */