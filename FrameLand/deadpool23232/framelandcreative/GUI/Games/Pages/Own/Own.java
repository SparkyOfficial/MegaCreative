/*    */ package deadpool23232.framelandcreative.GUI.Games.Pages.Own;
/*    */ 
/*    */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*    */ import deadpool23232.framelandcreative.Configs.WorldData;
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import deadpool23232.framelandcreative.GUI.Games.Functions.Skull.RandomSkull;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.inventory.Inventory;
/*    */ import org.bukkit.inventory.InventoryHolder;
/*    */ import org.bukkit.inventory.ItemFlag;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.inventory.meta.ItemMeta;
/*    */ 
/*    */ public class Own
/*    */ {
/* 21 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   public static void open(Player player) {
/* 23 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 54, FrameLandCreative.Color("&eСписок игр - свои миры"));
/*    */     
/* 25 */     List<String> list = new ArrayList<>();
/* 26 */     String uuid = player.getUniqueId().toString();
/* 27 */     DataConfig.get().getConfigurationSection("registered-worlds").getKeys(false).forEach(key -> {
/*    */           if (DataConfig.get().getString("registered-worlds." + key + ".author").equals(uuid))
/*    */             list.add(key); 
/*    */         });
/* 31 */     for (int i = 10; i < 44; i++) {
/* 32 */       if (i == 17) i = 19; 
/* 33 */       if (i == 26) i = 28; 
/* 34 */       if (i == 35) i = 37; 
/* 35 */       if (!list.isEmpty()) {
/* 36 */         String id = list.get(0);
/* 37 */         String name = DataConfig.get().getString("registered-worlds." + id + ".name");
/*    */         
/* 39 */         String likes = Integer.toString(WorldData.get().getInt("worlds." + id + ".liked"));
/*    */         
/* 41 */         String uniq = Integer.toString(WorldData.get().getInt("worlds." + id + ".uniquePlayers"));
/*    */         
/* 43 */         ItemStack item = RandomSkull.skull();
/* 44 */         List<String> list1 = new ArrayList<>();
/* 45 */         for (String line : config.getStringList("for-own-world.desc")) {
/* 46 */           list1.add(FrameLandCreative.Color(line
/* 47 */                 .replace("#id#", id)
/* 48 */                 .replace("#name#", name)
/* 49 */                 .replace("#likes#", likes)
/* 50 */                 .replace("#players#", uniq)));
/*    */         }
/*    */         
/* 53 */         ItemMeta itemMeta = item.getItemMeta();
/* 54 */         itemMeta.setDisplayName(FrameLandCreative.Color(name));
/* 55 */         itemMeta.setLore(list1);
/* 56 */         item.setItemMeta(itemMeta);
/* 57 */         gui.setItem(i, item);
/* 58 */         list.remove(0);
/*    */       } 
/*    */     } 
/*    */ 
/*    */     
/* 63 */     ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/* 64 */     ItemMeta glass1 = glass.getItemMeta();
/* 65 */     glass1.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/* 66 */     glass1.setDisplayName(FrameLandCreative.Color("&7"));
/* 67 */     glass.setItemMeta(glass1);
/*    */ 
/*    */     
/* 70 */     ItemStack newItem = RandomSkull.getSkull("http://textures.minecraft.net/texture/10c97e4b68aaaae8472e341b1d872b93b36d4eb6ea89ecec26a66e6c4e178");
/* 71 */     ItemMeta newMeta = newItem.getItemMeta();
/* 72 */     newMeta.setDisplayName(FrameLandCreative.Color(config.getString("newWorld.name")));
/* 73 */     List<String> newLore = new ArrayList<>();
/* 74 */     for (String line : config.getStringList("newWorld.desc")) {
/* 75 */       newLore.add(FrameLandCreative.Color(line));
/*    */     }
/* 77 */     newMeta.setLore(newLore);
/* 78 */     newItem.setItemMeta(newMeta);
/*    */     
/* 80 */     gui.setItem(0, glass); gui.setItem(1, glass); gui.setItem(2, glass); gui.setItem(3, glass); gui.setItem(4, glass);
/* 81 */     gui.setItem(5, glass); gui.setItem(6, glass); gui.setItem(7, glass); gui.setItem(8, glass); gui.setItem(9, glass);
/* 82 */     gui.setItem(18, glass); gui.setItem(27, glass); gui.setItem(36, glass); gui.setItem(17, glass); gui.setItem(26, glass);
/* 83 */     gui.setItem(35, glass); gui.setItem(44, glass); gui.setItem(49, newItem);
/* 84 */     gui.setItem(45, glass); gui.setItem(46, glass); gui.setItem(47, glass); gui.setItem(48, glass);
/* 85 */     gui.setItem(50, glass); gui.setItem(51, glass); gui.setItem(52, glass); gui.setItem(53, glass);
/*    */     
/* 87 */     player.openInventory(gui);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\Own\Own.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */