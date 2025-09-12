/*     */ package deadpool23232.framelandcreative.CODE.Blocks;
/*     */ 
/*     */ import deadpool23232.framelandcreative.CODE.Fill;
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.World;
/*     */ import org.bukkit.block.Block;
/*     */ import org.bukkit.block.Chest;
/*     */ import org.bukkit.block.Sign;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.block.BlockBreakEvent;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class onBreak
/*     */   implements Listener
/*     */ {
/*     */   @EventHandler
/*     */   public void onBlockBreak(BlockBreakEvent event) {
/*  31 */     event.setCancelled(true);
/*     */     
/*  33 */     String id = event.getPlayer().getWorld().getName().replace("-code", "");
/*  34 */     Player player = event.getPlayer();
/*  35 */     if (event.getPlayer().getWorld().getName().contains("-code") && DataConfig.get().getStringList("registered-worlds." + id + ".whitelist").contains(player.getUniqueId().toString())) {
/*  36 */       Block block = event.getBlock();
/*  37 */       Location blockLoc = block.getLocation();
/*  38 */       if (block.getWorld().getName().equals(event.getPlayer().getWorld().getName())) {
/*  39 */         Block glassUnder = player.getWorld().getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY() - 1, blockLoc.getBlockZ());
/*  40 */         byte data = glassUnder.getData();
/*     */ 
/*     */ 
/*     */         
/*  44 */         if (data == 4 && glassUnder.getType() == Material.STAINED_GLASS) {
/*  45 */           if (block.getType() == Material.DIAMOND_BLOCK) {
/*  46 */             Location toLoc = new Location(player.getWorld(), 0.0D, (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/*  47 */             Fill.fillArea(player.getWorld(), blockLoc, toLoc, Material.AIR);
/*     */           }
/*  49 */           else if (block.getType() == Material.LAPIS_BLOCK) {
/*  50 */             Location toLoc = new Location(player.getWorld(), 0.0D, (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/*  51 */             Fill.fillArea(player.getWorld(), blockLoc, toLoc, Material.AIR);
/*     */ 
/*     */           
/*     */           }
/*     */ 
/*     */ 
/*     */         
/*     */         }
/*  59 */         else if (data == 0 && glassUnder.getType() == Material.STAINED_GLASS) {
/*     */ 
/*     */ 
/*     */ 
/*     */           
/*  64 */           if (block.getType() == Material.WOOD) {
/*  65 */             Location fillTo; if (player.getWorld().getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ()).getType() != Material.PISTON_BASE)
/*     */               return; 
/*  67 */             Location firstBracket = new Location(player.getWorld(), (blockLoc.getBlockX() - 1), blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  68 */             Location lastBracket = findClosingBracket(firstBracket);
/*     */ 
/*     */             
/*  71 */             Location corner1 = new Location(player.getWorld(), (lastBracket.getBlockX() - 1), blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  72 */             Location corner2 = new Location(player.getWorld(), -1.0D, (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/*     */             
/*  74 */             if (corner1.getBlock().getType() == Material.ENDER_STONE) {
/*  75 */               Location elseFirstBracket = new Location(player.getWorld(), (corner1.getBlockX() - 1), corner1.getBlockY(), corner1.getBlockZ());
/*  76 */               Location elseLastBracket = findClosingBracket(elseFirstBracket);
/*  77 */               corner1.setX((elseLastBracket.getBlockX() - 1));
/*     */               
/*  79 */               fillTo = new Location(player.getWorld(), elseLastBracket.getBlockX(), (elseLastBracket.getBlockY() + 1), (elseLastBracket.getBlockZ() - 1));
/*     */             } else {
/*  81 */               fillTo = new Location(player.getWorld(), lastBracket.getBlockX(), (lastBracket.getBlockY() + 1), (lastBracket.getBlockZ() - 1));
/*     */             } 
/*  83 */             Fill.fillArea(player.getWorld(), blockLoc, fillTo, Material.AIR);
/*     */             
/*  85 */             cutPaste(player.getWorld(), corner1, corner2, blockLoc);
/*  86 */           } else if (block.getType() == Material.OBSIDIAN) {
/*  87 */             if (player.getWorld().getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ()).getType() != Material.PISTON_BASE)
/*     */               return; 
/*  89 */             Location toLoc = null;
/*  90 */             for (int i = blockLoc.getBlockX() - 2; i > 0; i--) {
/*  91 */               Block block1 = player.getWorld().getBlockAt(i, blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  92 */               if (block1.getType() == Material.PISTON_BASE) {
/*  93 */                 toLoc = new Location(player.getWorld(), i, (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/*     */                 break;
/*     */               } 
/*     */             } 
/*  97 */             if (toLoc != null) {
/*  98 */               Fill.fillArea(player.getWorld(), blockLoc, toLoc, Material.AIR);
/*  99 */               Location corner1 = new Location(player.getWorld(), (toLoc.getBlockX() - 1), blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 100 */               Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/* 101 */               cutPaste(player.getWorld(), corner1, corner2, blockLoc);
/*     */ 
/*     */             
/*     */             }
/*     */ 
/*     */           
/*     */           }
/* 108 */           else if (block.getType() == Material.ENDER_STONE) {
/* 109 */             if (player.getWorld().getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ()).getType() != Material.PISTON_BASE)
/*     */               return; 
/* 111 */             Location toLoc = null;
/* 112 */             for (int i = blockLoc.getBlockX() - 2; i > 0; i--) {
/* 113 */               Block block1 = player.getWorld().getBlockAt(i, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 114 */               if (block1.getType() == Material.PISTON_BASE) {
/* 115 */                 toLoc = new Location(player.getWorld(), i, (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/*     */                 break;
/*     */               } 
/*     */             } 
/* 119 */             if (toLoc != null) {
/* 120 */               Fill.fillArea(player.getWorld(), blockLoc, toLoc, Material.AIR);
/* 121 */               Location corner1 = new Location(player.getWorld(), (toLoc.getBlockX() - 1), blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 122 */               Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/* 123 */               cutPaste(player.getWorld(), corner1, corner2, blockLoc);
/*     */ 
/*     */             
/*     */             }
/*     */ 
/*     */           
/*     */           }
/* 130 */           else if (block.getType() == Material.COBBLESTONE) {
/* 131 */             Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 2), (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/* 132 */             removeBlocks(player, blockLoc);
/* 133 */             Location corner1 = new Location(player.getWorld(), toLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 134 */             Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/* 135 */             cutPaste(player.getWorld(), corner1, corner2, blockLoc);
/* 136 */           } else if (block.getType() == Material.NETHER_BRICK) {
/* 137 */             Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 2), (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/* 138 */             removeBlocks(player, blockLoc);
/* 139 */             Location corner1 = new Location(player.getWorld(), toLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 140 */             Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/* 141 */             cutPaste(player.getWorld(), corner1, corner2, blockLoc);
/* 142 */           } else if (block.getType() == Material.IRON_BLOCK) {
/* 143 */             Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 2), (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/* 144 */             removeBlocks(player, blockLoc);
/* 145 */             Location corner1 = new Location(player.getWorld(), toLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 146 */             Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/* 147 */             cutPaste(player.getWorld(), corner1, corner2, blockLoc);
/*     */           } 
/* 149 */         } else if (block.getType() == Material.PISTON_BASE || block
/* 150 */           .getType() == Material.ANVIL || 
/* 151 */           isShulker(block.getType()).booleanValue() || block
/* 152 */           .getType() == Material.WORKBENCH) {
/* 153 */           Block block1 = player.getWorld().getBlockAt(blockLoc.getBlockX() + 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 154 */           if (block1.getType() != Material.WOOD && block1
/* 155 */             .getType() != Material.OBSIDIAN && block1
/* 156 */             .getType() != Material.ENDER_STONE)
/*     */           {
/* 158 */             event.setCancelled(false);
/*     */           }
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private Boolean isShulker(Material material) {
/* 167 */     return Boolean.valueOf((material == Material.BLACK_SHULKER_BOX || material == Material.BLUE_SHULKER_BOX || material == Material.CYAN_SHULKER_BOX || material == Material.GRAY_SHULKER_BOX || material == Material.GREEN_SHULKER_BOX || material == Material.SILVER_SHULKER_BOX || material == Material.BROWN_SHULKER_BOX || material == Material.LIME_SHULKER_BOX || material == Material.MAGENTA_SHULKER_BOX || material == Material.PINK_SHULKER_BOX || material == Material.ORANGE_SHULKER_BOX || material == Material.RED_SHULKER_BOX || material == Material.WHITE_SHULKER_BOX || material == Material.PURPLE_SHULKER_BOX || material == Material.LIGHT_BLUE_SHULKER_BOX || material == Material.YELLOW_SHULKER_BOX));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void cutPaste(World world, Location corner1, Location corner2, Location pasteTo) {
/* 187 */     int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
/* 188 */     int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
/* 189 */     int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
/* 190 */     int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
/* 191 */     int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
/* 192 */     int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
/*     */     
/* 194 */     double rad = corner1.distance(pasteTo);
/*     */     int x;
/* 196 */     for (x = minX; x <= maxX; x++) {
/* 197 */       for (int y = minY; y <= maxY; y++) {
/* 198 */         for (int z = minZ; z <= maxZ; z++) {
/* 199 */           Block block = world.getBlockAt(x, y, z);
/* 200 */           if (block.getType() != Material.AIR) {
/* 201 */             Block block2 = pasteTo.getWorld().getBlockAt((int)(x + rad), y + 2, z).getLocation().getBlock().getState().getBlock();
/* 202 */             block2.setType(block.getType());
/* 203 */             block2.setData(block.getData());
/* 204 */             if (block.getState() instanceof Chest) {
/* 205 */               Chest chest = (Chest)block.getState();
/* 206 */               ItemStack[] toAdd = chest.getInventory().getContents();
/*     */               
/* 208 */               ((Chest)block2.getState()).getInventory().setContents(toAdd);
/* 209 */               ((Chest)block.getState()).getInventory().clear();
/* 210 */             } else if (block.getState() instanceof Sign) {
/* 211 */               Sign sign = (Sign)block.getState();
/* 212 */               Sign sign2 = (Sign)block2.getState();
/* 213 */               String[] lines = sign.getLines();
/* 214 */               for (int i = 0; i < lines.length; i++) {
/* 215 */                 sign2.setLine(i, lines[i]);
/*     */               }
/* 217 */               sign2.update(false, false);
/*     */             } 
/* 219 */             block.setType(Material.AIR);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/* 224 */     for (x = minX; x <= maxX; x++) {
/* 225 */       for (int y = minY; y <= maxY; y++) {
/* 226 */         for (int z = minZ; z <= maxZ; z++) {
/* 227 */           Block block = world.getBlockAt((int)(x + rad), y + 2, z);
/* 228 */           if (block.getType() != Material.AIR) {
/* 229 */             Block block2 = pasteTo.getWorld().getBlockAt((int)(x + rad), y, z).getLocation().getBlock().getState().getBlock();
/* 230 */             block2.setType(block.getType());
/* 231 */             block2.setData(block.getData());
/*     */             
/* 233 */             if (block.getState() instanceof Chest) {
/* 234 */               Chest chest = (Chest)block.getState();
/* 235 */               ItemStack[] toAdd = chest.getInventory().getContents();
/*     */               
/* 237 */               ((Chest)block2.getState()).getInventory().setContents(toAdd);
/* 238 */               ((Chest)block.getState()).getInventory().clear();
/* 239 */             } else if (block.getState() instanceof Sign) {
/* 240 */               Sign sign = (Sign)block.getState();
/* 241 */               Sign sign2 = (Sign)block2.getState();
/* 242 */               String[] lines = sign.getLines();
/* 243 */               for (int i = 0; i < lines.length; i++) {
/* 244 */                 sign2.setLine(i, lines[i]);
/*     */               }
/* 246 */               sign2.update(false, false);
/*     */             } 
/* 248 */             block.setType(Material.AIR);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void removeBlocks(Player player, Location blockLoc) {
/* 256 */     Block block = player.getWorld().getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY() + 1, blockLoc.getBlockZ());
/* 257 */     if (block.getState() instanceof Chest) {
/* 258 */       ((Chest)block.getState()).getInventory().clear();
/*     */     }
/* 260 */     Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 1), (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/* 261 */     Fill.fillArea(player.getWorld(), blockLoc, toLoc, Material.AIR);
/*     */   }
/*     */ 
/*     */   
/*     */   public Location findClosingBracket(Location firstBracket) {
/* 266 */     Location block = firstBracket.clone();
/*     */     
/*     */     try {
/* 269 */       int k = 0;
/* 270 */       for (int i = firstBracket.getBlockX(); i > -1; i--) {
/* 271 */         block.subtract(1.0D, 0.0D, 0.0D);
/* 272 */         if (block.getBlock().getType() == Material.PISTON_BASE) {
/* 273 */           if (block.getBlock().getData() == 4) {
/* 274 */             k++;
/* 275 */           } else if (block.getBlock().getData() == 5) {
/* 276 */             k--;
/*     */           } 
/*     */         }
/* 279 */         if (k < 0) {
/*     */           break;
/*     */         }
/*     */       } 
/* 283 */     } catch (Exception exception) {}
/*     */ 
/*     */ 
/*     */     
/* 287 */     return block;
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Blocks\onBreak.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */