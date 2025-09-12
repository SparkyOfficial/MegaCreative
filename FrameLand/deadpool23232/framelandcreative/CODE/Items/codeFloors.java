/*    */ package deadpool23232.framelandcreative.CODE.Items;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.CodeFloors;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.block.Block;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.inventory.InventoryClickEvent;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class codeFloors
/*    */   implements Listener
/*    */ {
/* 20 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   @EventHandler
/*    */   public void onUse(InventoryClickEvent event) {
/* 23 */     if (event.getWhoClicked().getWorld().getName().contains("-code")) {
/* 24 */       if (event.getCurrentItem() == null)
/* 25 */         return;  if (event.getCurrentItem().getType() == Material.AIR)
/*    */         return; 
/* 27 */       if (event.getCurrentItem().getType() == Material.LADDER) {
/* 28 */         event.setCancelled(true);
/* 29 */         event.getWhoClicked().closeInventory();
/* 30 */         if (event.getCurrentItem().hasItemMeta() && 
/* 31 */           event.getCurrentItem().getItemMeta().hasDisplayName() && 
/* 32 */           event.getCurrentItem().getItemMeta().getDisplayName().equals(FrameLandCreative.Color(this.config.getString("CODE.items.codeFloors.name")))) {
/* 33 */           String id = event.getWhoClicked().getWorld().getName().replace("-code", "");
/* 34 */           int plus = CodeFloors.get().getInt(id + ".floors");
/* 35 */           if (plus != 7) {
/* 36 */             Location corner1 = new Location(event.getWhoClicked().getWorld(), 93.0D, 9.0D, 94.0D);
/* 37 */             Location corner2 = new Location(event.getWhoClicked().getWorld(), 0.0D, 9.0D, 2.0D);
/* 38 */             int plus_EDITED = 10 * (plus + 1);
/* 39 */             cutPaste(event.getWhoClicked().getWorld(), corner1, corner2, Integer.valueOf(plus_EDITED));
/* 40 */             CodeFloors.get().set(id + ".floors", Integer.valueOf(plus + 1));
/* 41 */             CodeFloors.save();
/*    */           } 
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static void cutPaste(World world, Location corner1, Location corner2, Integer plus) {
/* 52 */     int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
/* 53 */     int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
/* 54 */     int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
/* 55 */     int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
/* 56 */     int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
/* 57 */     int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
/*    */     
/* 59 */     for (int x = minX; x <= maxX; x++) {
/* 60 */       for (int y = minY; y <= maxY; y++) {
/* 61 */         for (int z = minZ; z <= maxZ; z++) {
/* 62 */           Block block = world.getBlockAt(x, y, z);
/* 63 */           Block block2 = world.getBlockAt(x, y + plus.intValue(), z).getLocation().getBlock().getState().getBlock();
/* 64 */           block2.setType(block.getType());
/* 65 */           block2.setData(block.getData());
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Items\codeFloors.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */