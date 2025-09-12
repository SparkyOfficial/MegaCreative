/*    */ package deadpool23232.framelandcreative.GUI.BlackList.bList;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.Configs.PlayerData;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import java.util.UUID;
/*    */ import me.clip.placeholderapi.PlaceholderAPI;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.OfflinePlayer;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.inventory.Inventory;
/*    */ import org.bukkit.inventory.InventoryHolder;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.inventory.meta.ItemMeta;
/*    */ import org.bukkit.inventory.meta.SkullMeta;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class RMB
/*    */ {
/* 26 */   public static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   public static void open(Player player) {
/* 29 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eЧёрный список: &cУбрать"));
/*    */     
/* 31 */     World world = player.getWorld();
/* 32 */     String id = world.getName().replace("-world", "").replace("-code", "");
/* 33 */     List<String> list = new ArrayList<>(DataConfig.get().getStringList("registered-worlds." + id + ".blacklist"));
/* 34 */     for (int i = 0; i < 54; i++) {
/* 35 */       if (!list.isEmpty()) {
/* 36 */         String plUUID = list.get(0);
/* 37 */         OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(plUUID));
/* 38 */         String prefix = PlayerData.get().getString(plUUID + ".prefix");
/* 39 */         if (player1.isOnline()) {
/* 40 */           prefix = PlaceholderAPI.setPlaceholders(player1, "%vault_prefix%");
/*    */         }
/* 42 */         if (prefix == null) {
/* 43 */           prefix = "";
/*    */         }
/* 45 */         List<String> list1 = new ArrayList<>();
/* 46 */         for (String line : config.getStringList("blackPlayer.desc")) {
/* 47 */           list1.add(FrameLandCreative.Color(line
/* 48 */                 .replace("#uuid#", plUUID)
/* 49 */                 .replace("#name#", "&7" + prefix + player1.getName())));
/*    */         }
/*    */ 
/*    */ 
/*    */         
/* 54 */         ItemStack headItem = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
/* 55 */         SkullMeta headSkull = (SkullMeta)headItem.getItemMeta();
/* 56 */         headSkull.setOwningPlayer(player1);
/* 57 */         headItem.setItemMeta((ItemMeta)headSkull);
/* 58 */         ItemMeta headMeta = headItem.getItemMeta();
/* 59 */         headMeta.setDisplayName(FrameLandCreative.Color("&3" + plUUID));
/* 60 */         headMeta.setLore(list1);
/* 61 */         headItem.setItemMeta(headMeta);
/*    */         
/* 63 */         gui.setItem(i, headItem);
/* 64 */         list.remove(0);
/*    */       } 
/*    */     } 
/*    */     
/* 68 */     player.openInventory(gui);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\BlackList\bList\RMB.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */