/*     */ package deadpool23232.framelandcreative.CODE.Functions;
/*     */ 
/*     */ import deadpool23232.framelandcreative.CODE.Blocks.Sign;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.block.Block;
/*     */ import org.bukkit.block.Sign;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.inventory.InventoryClickEvent;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.InventoryHolder;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ public class PlSel_GUILISTENER
/*     */   implements Listener
/*     */ {
/*     */   public static void open(Player player) {
/*  25 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 9, FrameLandCreative.Color("&bВыбрать игрока..."));
/*     */     
/*  27 */     ItemStack back = new ItemStack(Material.ARROW);
/*  28 */     ItemMeta backM = back.getItemMeta();
/*  29 */     backM.setDisplayName(FrameLandCreative.Color("&fВернутся"));
/*  30 */     back.setItemMeta(backM);
/*  31 */     gui.setItem(8, back);
/*     */     
/*  33 */     ItemStack playerFunc = new ItemStack(Material.NETHER_STAR);
/*  34 */     ItemMeta playerMeta = playerFunc.getItemMeta();
/*  35 */     ItemStack killerFunc = new ItemStack(Material.IRON_SWORD);
/*  36 */     ItemMeta killerMeta = killerFunc.getItemMeta();
/*  37 */     ItemStack victimFunc = new ItemStack(Material.INK_SACK, 1, (short)1);
/*  38 */     ItemMeta victimMeta = victimFunc.getItemMeta();
/*  39 */     ItemStack randomFunc = new ItemStack(Material.GRASS);
/*  40 */     ItemMeta randomMeta = randomFunc.getItemMeta();
/*     */     
/*  42 */     playerMeta.setDisplayName(FrameLandCreative.Color("&eИгрок"));
/*  43 */     playerFunc.setItemMeta(playerMeta);
/*  44 */     killerMeta.setDisplayName(FrameLandCreative.Color("&eАтакующий"));
/*  45 */     killerFunc.setItemMeta(killerMeta);
/*  46 */     victimMeta.setDisplayName(FrameLandCreative.Color("&eЖертва"));
/*  47 */     victimFunc.setItemMeta(victimMeta);
/*  48 */     randomMeta.setDisplayName(FrameLandCreative.Color("&eСлучайный игрок"));
/*  49 */     randomFunc.setItemMeta(randomMeta);
/*     */     
/*  51 */     gui.setItem(1, playerFunc);
/*  52 */     gui.setItem(2, killerFunc);
/*  53 */     gui.setItem(3, victimFunc);
/*  54 */     gui.setItem(4, randomFunc);
/*     */     
/*  56 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   @EventHandler
/*     */   public void onClick(InventoryClickEvent event) {
/*  61 */     if (event.getInventory().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&bВыбрать игрока..."))) {
/*  62 */       event.setCancelled(true);
/*  63 */       if (event.getCurrentItem() == null) {
/*     */         return;
/*     */       }
/*  66 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*     */         return;
/*     */       }
/*  69 */       ItemStack item = event.getCurrentItem();
/*  70 */       ItemMeta itemMeta = item.getItemMeta();
/*  71 */       String itemName = itemMeta.getDisplayName();
/*  72 */       Player player = (Player)event.getWhoClicked();
/*  73 */       if (itemName == null) {
/*     */         return;
/*     */       }
/*     */       
/*  77 */       if (itemName.equals(FrameLandCreative.Color("&fВернутся"))) {
/*  78 */         player.closeInventory();
/*     */       }
/*     */       
/*  81 */       Block block = ((Location)PlayerSelect.blockSelMap.get(player)).getBlock();
/*  82 */       Sign sign = (Sign)block.getState();
/*  83 */       String[] lines = sign.getLines();
/*  84 */       List<String> newLines = new ArrayList<>();
/*  85 */       if (lines[0] != null && lines[1] != null && lines[2] != null) {
/*  86 */         newLines.add(lines[0]);
/*  87 */         newLines.add(lines[1]);
/*  88 */         newLines.add(lines[2]);
/*  89 */         newLines.add(" ");
/*  90 */       } else if (lines[0] != null && lines[1] != null) {
/*  91 */         newLines.add(lines[0]);
/*  92 */         newLines.add(lines[1]);
/*  93 */         newLines.add(" ");
/*  94 */       } else if (lines[0] != null) {
/*  95 */         newLines.add(lines[0]);
/*  96 */         newLines.add(" ");
/*  97 */         newLines.add(" ");
/*     */       } 
/*     */       
/* 100 */       if (itemName.equals(FrameLandCreative.Color("&eИгрок"))) {
/* 101 */         newLines.add("Игрок");
/* 102 */         Location signLoc = PlayerSelect.blockSelMap.get(player);
/* 103 */         Sign.configSign(signLoc, player.getWorld(), newLines);
/* 104 */         player.closeInventory();
/* 105 */       } else if (itemName.equals(FrameLandCreative.Color("&eАтакующий"))) {
/* 106 */         newLines.add("Атакующий");
/* 107 */         Location signLoc = PlayerSelect.blockSelMap.get(player);
/* 108 */         Sign.configSign(signLoc, player.getWorld(), newLines);
/* 109 */         player.closeInventory();
/* 110 */       } else if (itemName.equals(FrameLandCreative.Color("&eЖертва"))) {
/* 111 */         newLines.add("Жертва");
/* 112 */         Location signLoc = PlayerSelect.blockSelMap.get(player);
/* 113 */         Sign.configSign(signLoc, player.getWorld(), newLines);
/* 114 */         player.closeInventory();
/* 115 */       } else if (itemName.equals(FrameLandCreative.Color("&eСлучайный игрок"))) {
/* 116 */         newLines.add("Случайный");
/* 117 */         Location signLoc = PlayerSelect.blockSelMap.get(player);
/* 118 */         Sign.configSign(signLoc, player.getWorld(), newLines);
/* 119 */         player.closeInventory();
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\PlSel_GUILISTENER.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */