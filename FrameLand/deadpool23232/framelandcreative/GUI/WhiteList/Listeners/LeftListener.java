/*    */ package deadpool23232.framelandcreative.GUI.WhiteList.Listeners;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.WhiteList.wList.LMB;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ public class LeftListener
/*    */   implements Listener
/*    */ {
/* 25 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   @EventHandler
/*    */   public void onMenuClick(InventoryClickEvent event) {
/* 29 */     if (event.getView().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&eБелый список: &aДобавить"))) {
/* 30 */       event.setCancelled(true);
/* 31 */       if (event.getCurrentItem() == null) {
/*    */         return;
/*    */       }
/* 34 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*    */         return;
/*    */       }
/*    */       
/* 38 */       Player player = (Player)event.getWhoClicked();
/*    */       
/* 40 */       ItemStack item = event.getCurrentItem();
/* 41 */       ItemMeta itemMeta = item.getItemMeta();
/* 42 */       String itemName = itemMeta.getDisplayName();
/*    */       
/* 44 */       if (item.getType() == Material.SKULL_ITEM) {
/* 45 */         player.closeInventory();
/* 46 */         String uuid = itemName.substring(2);
/* 47 */         World world = player.getWorld();
/* 48 */         String id = world.getName().replace("-world", "").replace("-code", "");
/* 49 */         List<String> list = new ArrayList<>(DataConfig.get().getStringList("registered-worlds." + id + ".whitelist"));
/* 50 */         list.add(uuid);
/* 51 */         DataConfig.get().set("registered-worlds." + id + ".whitelist", list);
/* 52 */         LMB.open(player);
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WhiteList\Listeners\LeftListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */