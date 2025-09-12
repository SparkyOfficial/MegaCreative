/*    */ package deadpool23232.framelandcreative.GUI.Kick;
/*    */ 
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
/*    */ public class KickPlayers
/*    */ {
/* 25 */   public static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   public static void open(Player player) {
/* 28 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&9Выгнать игрока"));
/*    */     
/* 30 */     World world = player.getWorld();
/* 31 */     List<String> list = new ArrayList<>();
/* 32 */     for (Player line : world.getPlayers()) {
/* 33 */       list.add(line.getUniqueId().toString());
/*    */     }
/* 35 */     list.remove(player.getUniqueId().toString());
/* 36 */     for (int i = 0; i < 54; i++) {
/* 37 */       if (!list.isEmpty()) {
/* 38 */         String plUUID = list.get(0);
/* 39 */         OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(plUUID));
/* 40 */         String prefix = PlayerData.get().getString(plUUID + ".prefix");
/* 41 */         if (player1.isOnline()) {
/* 42 */           prefix = PlaceholderAPI.setPlaceholders(player1, "%vault_prefix%");
/*    */         }
/* 44 */         if (prefix == null) {
/* 45 */           prefix = "";
/*    */         }
/* 47 */         List<String> list1 = new ArrayList<>();
/* 48 */         for (String line : config.getStringList("kickPlayer.desc")) {
/* 49 */           list1.add(FrameLandCreative.Color(line
/* 50 */                 .replace("#uuid#", plUUID)
/* 51 */                 .replace("#name#", "&7" + prefix + player1.getName())));
/*    */         }
/*    */ 
/*    */ 
/*    */         
/* 56 */         ItemStack headItem = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
/* 57 */         SkullMeta headSkull = (SkullMeta)headItem.getItemMeta();
/* 58 */         headSkull.setOwningPlayer(player1);
/* 59 */         headItem.setItemMeta((ItemMeta)headSkull);
/* 60 */         ItemMeta headMeta = headItem.getItemMeta();
/* 61 */         headMeta.setDisplayName(FrameLandCreative.Color("&3" + plUUID));
/* 62 */         headMeta.setLore(list1);
/* 63 */         headItem.setItemMeta(headMeta);
/*    */         
/* 65 */         gui.setItem(i, headItem);
/* 66 */         list.remove(0);
/*    */       } 
/*    */     } 
/*    */     
/* 70 */     player.openInventory(gui);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Kick\KickPlayers.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */