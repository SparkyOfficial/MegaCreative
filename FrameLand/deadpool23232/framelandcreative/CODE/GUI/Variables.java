/*    */ package deadpool23232.framelandcreative.CODE.GUI;
/*    */ 
/*    */ import deadpool23232.framelandcreative.FrameLandCreative;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.inventory.Inventory;
/*    */ import org.bukkit.inventory.InventoryHolder;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.inventory.meta.ItemMeta;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Variables
/*    */ {
/* 19 */   public static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*    */   
/*    */   public static void open(Player player) {
/* 22 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 9, "Переменные");
/*    */     
/* 24 */     ItemStack name = new ItemStack(Material.BOOK);
/* 25 */     ItemStack var = new ItemStack(Material.SLIME_BALL);
/* 26 */     ItemStack coord = new ItemStack(Material.PAPER);
/* 27 */     ItemStack apple = new ItemStack(Material.GOLDEN_APPLE);
/*    */     
/* 29 */     ItemStack info = new ItemStack(Material.KNOWLEDGE_BOOK);
/*    */ 
/*    */     
/* 32 */     ItemMeta nameMeta = name.getItemMeta();
/* 33 */     nameMeta.setDisplayName(FrameLandCreative.Color(config.getString("CODE_GUI.name.name")));
/* 34 */     List<String> nameLore = new ArrayList<>();
/* 35 */     for (String line : config.getStringList("CODE_GUI.name.desc"))
/* 36 */       nameLore.add(FrameLandCreative.Color(line)); 
/* 37 */     nameMeta.setLore(nameLore);
/* 38 */     name.setItemMeta(nameMeta);
/*    */ 
/*    */     
/* 41 */     ItemMeta varMeta = var.getItemMeta();
/* 42 */     varMeta.setDisplayName(FrameLandCreative.Color(config.getString("CODE_GUI.var.name")));
/* 43 */     List<String> varLore = new ArrayList<>();
/* 44 */     for (String line : config.getStringList("CODE_GUI.var.desc"))
/* 45 */       varLore.add(FrameLandCreative.Color(line)); 
/* 46 */     varMeta.setLore(varLore);
/* 47 */     var.setItemMeta(varMeta);
/*    */ 
/*    */     
/* 50 */     ItemMeta coordMeta = coord.getItemMeta();
/* 51 */     coordMeta.setDisplayName(FrameLandCreative.Color(config.getString("CODE_GUI.coord.name")));
/* 52 */     List<String> coordLore = new ArrayList<>();
/* 53 */     for (String line : config.getStringList("CODE_GUI.coord.desc"))
/* 54 */       coordLore.add(FrameLandCreative.Color(line)); 
/* 55 */     coordMeta.setLore(coordLore);
/* 56 */     coord.setItemMeta(coordMeta);
/*    */ 
/*    */     
/* 59 */     ItemMeta appleMeta = apple.getItemMeta();
/* 60 */     appleMeta.setDisplayName(FrameLandCreative.Color(config.getString("CODE_GUI.apple.name")));
/* 61 */     List<String> appleLore = new ArrayList<>();
/* 62 */     for (String line : config.getStringList("CODE_GUI.apple.desc"))
/* 63 */       appleLore.add(FrameLandCreative.Color(line)); 
/* 64 */     appleMeta.setLore(appleLore);
/* 65 */     apple.setItemMeta(appleMeta);
/*    */ 
/*    */     
/* 68 */     ItemMeta infoMeta = info.getItemMeta();
/* 69 */     infoMeta.setDisplayName(FrameLandCreative.Color(config.getString("CODE_GUI.info.name")));
/* 70 */     List<String> infoLore = new ArrayList<>();
/* 71 */     for (String line : config.getStringList("CODE_GUI.info.desc"))
/* 72 */       infoLore.add(FrameLandCreative.Color(line)); 
/* 73 */     infoMeta.setLore(infoLore);
/* 74 */     info.setItemMeta(infoMeta);
/*    */ 
/*    */     
/* 77 */     gui.setItem(0, name);
/* 78 */     gui.setItem(1, var);
/* 79 */     gui.setItem(3, coord);
/* 80 */     gui.setItem(4, apple);
/* 81 */     gui.setItem(8, info);
/*    */     
/* 83 */     player.openInventory(gui);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\GUI\Variables.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */