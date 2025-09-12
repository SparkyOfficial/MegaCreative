/*    */ package deadpool23232.framelandcreative.GUI.WhiteList.wList;
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
/*    */ public class LMB
/*    */ {
/* 26 */   public static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   public static void open(Player player) {
/* 29 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eБелый список: &aДобавить"));
/*    */     
/* 31 */     World world = player.getWorld();
/* 32 */     String id = world.getName().replace("-world", "").replace("-code", "");
/* 33 */     List<String> list = new ArrayList<>();
/* 34 */     for (Player line : world.getPlayers()) {
/* 35 */       if (!DataConfig.get().getStringList("registered-worlds." + id + ".whitelist").contains(line.getUniqueId().toString())) {
/* 36 */         list.add(line.getUniqueId().toString());
/*    */       }
/*    */     } 
/* 39 */     for (int i = 0; i < 54; i++) {
/* 40 */       if (!list.isEmpty()) {
/* 41 */         String plUUID = list.get(0);
/* 42 */         OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(plUUID));
/* 43 */         String prefix = PlayerData.get().getString(plUUID + ".prefix");
/* 44 */         if (player1.isOnline()) {
/* 45 */           prefix = PlaceholderAPI.setPlaceholders(player1, "%vault_prefix%");
/*    */         }
/* 47 */         if (prefix == null) {
/* 48 */           prefix = "";
/*    */         }
/* 50 */         List<String> list1 = new ArrayList<>();
/* 51 */         for (String line : config.getStringList("whitePlayer.desc")) {
/* 52 */           list1.add(FrameLandCreative.Color(line
/* 53 */                 .replace("#uuid#", plUUID)
/* 54 */                 .replace("#name#", "&7" + prefix + player1.getName())));
/*    */         }
/*    */ 
/*    */ 
/*    */         
/* 59 */         ItemStack headItem = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
/* 60 */         SkullMeta headSkull = (SkullMeta)headItem.getItemMeta();
/* 61 */         headSkull.setOwningPlayer(player1);
/* 62 */         headItem.setItemMeta((ItemMeta)headSkull);
/* 63 */         ItemMeta headMeta = headItem.getItemMeta();
/* 64 */         headMeta.setDisplayName(FrameLandCreative.Color("&3" + plUUID));
/* 65 */         headMeta.setLore(list1);
/* 66 */         headItem.setItemMeta(headMeta);
/*    */         
/* 68 */         gui.setItem(i, headItem);
/* 69 */         list.remove(0);
/*    */       } 
/*    */     } 
/*    */     
/* 73 */     player.openInventory(gui);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WhiteList\wList\LMB.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */