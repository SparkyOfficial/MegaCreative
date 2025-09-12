/*     */ package deadpool23232.framelandcreative.CODE.Blocks;
/*     */ 
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.World;
/*     */ import org.bukkit.block.Block;
/*     */ import org.bukkit.block.Chest;
/*     */ import org.bukkit.block.Sign;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.block.Action;
/*     */ import org.bukkit.event.player.PlayerInteractEvent;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ 
/*     */ 
/*     */ public class codeSwapper
/*     */   implements Listener
/*     */ {
/*     */   @EventHandler
/*     */   public void onUse(PlayerInteractEvent event) {
/*  23 */     if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
/*  24 */       Material blockType = event.getClickedBlock().getType();
/*  25 */       if (blockType == Material.COBBLESTONE || blockType == Material.WOOD || blockType == Material.NETHER_BRICK || blockType == Material.ENDER_STONE || blockType == Material.IRON_BLOCK || blockType == Material.OBSIDIAN || blockType == Material.LAPIS_ORE)
/*     */       {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*  33 */         if (event.getPlayer().getWorld().getName().contains("-code")) {
/*  34 */           Player player = event.getPlayer();
/*  35 */           if (player.getInventory().getItemInMainHand().getType() == Material.REDSTONE_COMPARATOR && 
/*  36 */             player.getInventory().getItemInMainHand().hasItemMeta() && 
/*  37 */             player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(FrameLandCreative.Color("&6Перемещатель кода"))) {
/*  38 */             Block block = event.getClickedBlock();
/*  39 */             Location blockLoc = block.getLocation();
/*  40 */             Location corner1 = new Location(player.getWorld(), blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  41 */             Location corner2 = new Location(player.getWorld(), -1.0D, (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/*  42 */             if (!player.isSneaking()) {
/*  43 */               Location check1 = new Location(block.getWorld(), 0.0D, block.getLocation().getBlockY(), block.getLocation().getBlockZ());
/*  44 */               if (check1.getBlock().isEmpty()) {
/*  45 */                 Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 2), blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  46 */                 cutPasteTO(blockLoc.getWorld(), corner1, corner2, toLoc);
/*     */               } 
/*     */             } else {
/*  49 */               Block check = player.getWorld().getBlockAt(blockLoc.getBlockX() + 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  50 */               if (check.getType() == Material.AIR) {
/*  51 */                 Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 2), blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  52 */                 cutPasteFROM(player.getWorld(), corner1, corner2, toLoc);
/*     */               } 
/*     */             } 
/*     */           } 
/*     */         } 
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void cutPasteTO(World world, Location corner1, Location corner2, Location pasteTo) {
/*  65 */     int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
/*  66 */     int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
/*  67 */     int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
/*  68 */     int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
/*  69 */     int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
/*  70 */     int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
/*     */     
/*  72 */     double rad = corner1.distance(pasteTo);
/*     */     int x;
/*  74 */     for (x = minX; x <= maxX; x++) {
/*  75 */       for (int y = minY; y <= maxY; y++) {
/*  76 */         for (int z = minZ; z <= maxZ; z++) {
/*  77 */           Block block = world.getBlockAt(x, y, z);
/*  78 */           if (block.getType() != Material.AIR) {
/*  79 */             Block block2 = pasteTo.getWorld().getBlockAt((int)(x - rad), y + 2, z).getLocation().getBlock().getState().getBlock();
/*  80 */             block2.setType(block.getType());
/*  81 */             block2.setData(block.getData());
/*  82 */             if (block.getState() instanceof Chest) {
/*  83 */               Chest chest = (Chest)block.getState();
/*  84 */               ItemStack[] toAdd = chest.getInventory().getContents();
/*     */               
/*  86 */               ((Chest)block2.getState()).getInventory().setContents(toAdd);
/*  87 */               ((Chest)block.getState()).getInventory().clear();
/*  88 */             } else if (block.getState() instanceof Sign) {
/*  89 */               Sign sign = (Sign)block.getState();
/*  90 */               Sign sign2 = (Sign)block2.getState();
/*  91 */               String[] lines = sign.getLines();
/*  92 */               for (int i = 0; i < lines.length; i++) {
/*  93 */                 sign2.setLine(i, lines[i]);
/*     */               }
/*  95 */               sign2.update(false, false);
/*     */             } 
/*  97 */             block.setType(Material.AIR);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/* 102 */     for (x = minX; x <= maxX; x++) {
/* 103 */       for (int y = minY; y <= maxY; y++) {
/* 104 */         for (int z = minZ; z <= maxZ; z++) {
/* 105 */           Block block = world.getBlockAt((int)(x - rad), y + 2, z);
/* 106 */           if (block.getType() != Material.AIR) {
/* 107 */             Block block2 = pasteTo.getWorld().getBlockAt((int)(x - rad), y, z).getLocation().getBlock().getState().getBlock();
/* 108 */             block2.setType(block.getType());
/* 109 */             block2.setData(block.getData());
/* 110 */             if (block.getState() instanceof Chest) {
/* 111 */               Chest chest = (Chest)block.getState();
/* 112 */               ItemStack[] toAdd = chest.getInventory().getContents();
/*     */               
/* 114 */               ((Chest)block2.getState()).getInventory().setContents(toAdd);
/* 115 */               ((Chest)block.getState()).getInventory().clear();
/* 116 */             } else if (block.getState() instanceof Sign) {
/* 117 */               Sign sign = (Sign)block.getState();
/* 118 */               Sign sign2 = (Sign)block2.getState();
/* 119 */               String[] lines = sign.getLines();
/* 120 */               for (int i = 0; i < lines.length; i++) {
/* 121 */                 sign2.setLine(i, lines[i]);
/*     */               }
/* 123 */               sign2.update(false, false);
/*     */             } 
/* 125 */             block.setType(Material.AIR);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void cutPasteFROM(World world, Location corner1, Location corner2, Location pasteTo) {
/* 134 */     int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
/* 135 */     int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
/* 136 */     int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
/* 137 */     int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
/* 138 */     int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
/* 139 */     int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
/*     */     
/* 141 */     double rad = corner1.distance(pasteTo);
/*     */     int x;
/* 143 */     for (x = minX; x <= maxX; x++) {
/* 144 */       for (int y = minY; y <= maxY; y++) {
/* 145 */         for (int z = minZ; z <= maxZ; z++) {
/* 146 */           Block block = world.getBlockAt(x, y, z);
/* 147 */           if (block.getType() != Material.AIR) {
/* 148 */             Block block2 = pasteTo.getWorld().getBlockAt((int)(x + rad), y + 2, z).getLocation().getBlock().getState().getBlock();
/* 149 */             block2.setType(block.getType());
/* 150 */             block2.setData(block.getData());
/* 151 */             if (block.getState() instanceof Chest) {
/* 152 */               Chest chest = (Chest)block.getState();
/* 153 */               ItemStack[] toAdd = chest.getInventory().getContents();
/*     */               
/* 155 */               ((Chest)block2.getState()).getInventory().setContents(toAdd);
/* 156 */               ((Chest)block.getState()).getInventory().clear();
/* 157 */             } else if (block.getState() instanceof Sign) {
/* 158 */               Sign sign = (Sign)block.getState();
/* 159 */               Sign sign2 = (Sign)block2.getState();
/* 160 */               String[] lines = sign.getLines();
/* 161 */               for (int i = 0; i < lines.length; i++) {
/* 162 */                 sign2.setLine(i, lines[i]);
/*     */               }
/* 164 */               sign2.update(false, false);
/*     */             } 
/* 166 */             block.setType(Material.AIR);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/* 171 */     for (x = minX; x <= maxX; x++) {
/* 172 */       for (int y = minY; y <= maxY; y++) {
/* 173 */         for (int z = minZ; z <= maxZ; z++) {
/* 174 */           Block block = world.getBlockAt((int)(x + rad), y + 2, z);
/* 175 */           if (block.getType() != Material.AIR) {
/* 176 */             Block block2 = pasteTo.getWorld().getBlockAt((int)(x + rad), y, z).getLocation().getBlock().getState().getBlock();
/* 177 */             block2.setType(block.getType());
/* 178 */             block2.setData(block.getData());
/*     */             
/* 180 */             if (block.getState() instanceof Chest) {
/* 181 */               Chest chest = (Chest)block.getState();
/* 182 */               ItemStack[] toAdd = chest.getInventory().getContents();
/*     */               
/* 184 */               ((Chest)block2.getState()).getInventory().setContents(toAdd);
/* 185 */               ((Chest)block.getState()).getInventory().clear();
/* 186 */             } else if (block.getState() instanceof Sign) {
/* 187 */               Sign sign = (Sign)block.getState();
/* 188 */               Sign sign2 = (Sign)block2.getState();
/* 189 */               String[] lines = sign.getLines();
/* 190 */               for (int i = 0; i < lines.length; i++) {
/* 191 */                 sign2.setLine(i, lines[i]);
/*     */               }
/* 193 */               sign2.update(false, false);
/*     */             } 
/* 195 */             block.setType(Material.AIR);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Blocks\codeSwapper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */