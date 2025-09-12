/*    */ package deadpool23232.framelandcreative.GUI.GameRules;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.WorldSettings.WorldSettings;
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
/* 18 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfigFile();
/* 19 */   FileConfiguration dataConfig = DataConfig.get();
/*    */   
/*    */   @EventHandler
/*    */   public void onMenuClick(InventoryClickEvent event) {
/* 23 */     if (event.getView().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&2Правила игры"))) {
/* 24 */       event.setCancelled(true);
/* 25 */       if (event.getCurrentItem() == null)
/* 26 */         return;  if (event.getCurrentItem().getType() == Material.AIR)
/* 27 */         return;  ItemStack item = event.getCurrentItem();
/* 28 */       ItemMeta itemMeta = item.getItemMeta();
/* 29 */       String itemName = itemMeta.getDisplayName();
/* 30 */       Player player = (Player)event.getWhoClicked();
/*    */       
/* 32 */       String id = player.getWorld().getName().replace("-world", "").replace("-code", "");
/* 33 */       if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.randomTickSpeed")))) {
/* 34 */         player.closeInventory();
/* 35 */         randomTickSpeed.main(player, id);
/* 36 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.doDaylightCycle")))) {
/* 37 */         player.closeInventory();
/* 38 */         RuleSet.doDaylightCycle(player, id);
/* 39 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.doEntityDrops")))) {
/* 40 */         player.closeInventory();
/* 41 */         RuleSet.doEntityDrops(player, id);
/* 42 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.showDeathMessages")))) {
/* 43 */         player.closeInventory();
/* 44 */         RuleSet.showDeathMessages(player, id);
/* 45 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.naturalRegeneration")))) {
/* 46 */         player.closeInventory();
/* 47 */         RuleSet.naturalRegeneration(player, id);
/* 48 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.mobGriefing")))) {
/* 49 */         player.closeInventory();
/* 50 */         RuleSet.mobGriefing(player, id);
/* 51 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.keepInventory")))) {
/* 52 */         player.closeInventory();
/* 53 */         RuleSet.keepInventory(player, id);
/* 54 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.doWeatherCycle")))) {
/* 55 */         player.closeInventory();
/* 56 */         RuleSet.doWeatherCycle(player, id);
/* 57 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.doMobLoot")))) {
/* 58 */         player.closeInventory();
/* 59 */         RuleSet.doMobLoot(player, id);
/* 60 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.doTileDrops")))) {
/* 61 */         player.closeInventory();
/* 62 */         RuleSet.doTileDrops(player, id);
/* 63 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("gameRules.doFireTick")))) {
/* 64 */         player.closeInventory();
/* 65 */         RuleSet.doFireTick(player, id);
/* 66 */       } else if (itemName.equals(FrameLandCreative.Color(config.getString("back.name")))) {
/* 67 */         player.closeInventory();
/* 68 */         WorldSettings.main(player, id);
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\GameRules\Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */