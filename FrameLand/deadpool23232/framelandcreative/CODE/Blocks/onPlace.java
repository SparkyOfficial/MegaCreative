/*     */ package deadpool23232.framelandcreative.CODE.Blocks;
/*     */ 
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.World;
/*     */ import org.bukkit.block.Block;
/*     */ import org.bukkit.block.Chest;
/*     */ import org.bukkit.block.Sign;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.block.BlockPlaceEvent;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class onPlace
/*     */   implements Listener
/*     */ {
/*     */   @EventHandler
/*     */   public void onBlockPlace(BlockPlaceEvent event) {
/*  28 */     event.setCancelled(true);
/*     */     
/*  30 */     String id = event.getPlayer().getWorld().getName().replace("-code", "");
/*  31 */     Player player = event.getPlayer();
/*  32 */     if (event.getPlayer().getWorld().getName().contains("-code") && DataConfig.get().getStringList("registered-worlds." + id + ".whitelist").contains(player.getUniqueId().toString())) {
/*  33 */       Block block = event.getBlock();
/*  34 */       Location blockLoc = block.getLocation();
/*     */       
/*  36 */       World world = player.getWorld();
/*  37 */       if (block.getWorld().getName().equals(event.getPlayer().getWorld().getName())) {
/*  38 */         Block glassUnder = player.getWorld().getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY() - 1, blockLoc.getBlockZ());
/*  39 */         byte data = glassUnder.getData();
/*  40 */         List<String> lore = new ArrayList<>();
/*     */ 
/*     */ 
/*     */         
/*  44 */         if (data == 4 && glassUnder.getType() == Material.STAINED_GLASS) {
/*  45 */           if (block.getType() == Material.DIAMOND_BLOCK) {
/*     */             
/*  47 */             event.setCancelled(false);
/*  48 */             Block extBlock = world.getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  49 */             extBlock.setType(Material.DIAMOND_ORE);
/*  50 */             Block sign = world.getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ() - 1);
/*  51 */             sign.setType(Material.WALL_SIGN);
/*  52 */             lore.add("Событие игрока");
/*  53 */             Sign.configSign(sign.getLocation(), player.getWorld(), lore);
/*  54 */           } else if (block.getType() == Material.LAPIS_BLOCK) {
/*     */             
/*  56 */             event.setCancelled(false);
/*  57 */             Block extBlock = world.getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  58 */             extBlock.setType(Material.LAPIS_ORE);
/*  59 */             Block sign = world.getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ() - 1);
/*  60 */             sign.setType(Material.WALL_SIGN);
/*  61 */             lore.add("Функция");
/*  62 */             Sign.configSign(sign.getLocation(), player.getWorld(), lore);
/*     */ 
/*     */           
/*     */           }
/*     */ 
/*     */         
/*     */         }
/*  69 */         else if (data == 0 && glassUnder.getType() == Material.STAINED_GLASS) {
/*  70 */           Location end = new Location(player.getWorld(), 0.0D, blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  71 */           if (end.getBlock().getType() != Material.AIR) {
/*     */             return;
/*     */           }
/*     */           
/*  75 */           if (world.getBlockAt(blockLoc.getBlockX() + 2, blockLoc.getBlockY(), blockLoc.getBlockZ()).getType() == Material.AIR && 
/*  76 */             world.getBlockAt(blockLoc.getBlockX() + 1, blockLoc.getBlockY(), blockLoc.getBlockZ()).getType() != Material.PISTON_BASE) {
/*     */             return;
/*     */           }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/*  84 */           if (block.getType() == Material.WOOD) {
/*  85 */             Block check = player.getWorld().getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  86 */             Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 1), (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/*  87 */             Location corner1 = new Location(player.getWorld(), toLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  88 */             Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/*  89 */             if (check.getType() == Material.AIR) {
/*  90 */               cutPasteIF_ELSE(player.getWorld(), corner1, corner2, toLoc, "null");
/*     */             } else {
/*  92 */               cutPasteIF_ELSE(player.getWorld(), corner1, corner2, toLoc, "default");
/*     */             } 
/*  94 */             event.setCancelled(false);
/*  95 */             Block westPiston = world.getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  96 */             westPiston.setType(Material.PISTON_BASE);
/*  97 */             westPiston.setData((byte)4);
/*  98 */             Block eastPiston = world.getBlockAt(blockLoc.getBlockX() - 3, blockLoc.getBlockY(), blockLoc.getBlockZ());
/*  99 */             eastPiston.setType(Material.PISTON_BASE);
/* 100 */             eastPiston.setData((byte)5);
/* 101 */             Block sign = world.getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ() - 1);
/* 102 */             sign.setType(Material.WALL_SIGN);
/* 103 */             lore.add("Если игрок");
/* 104 */             Sign.configSign(sign.getLocation(), player.getWorld(), lore);
/*     */           }
/* 106 */           else if (block.getType() == Material.OBSIDIAN) {
/* 107 */             Block check = player.getWorld().getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 108 */             Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 1), (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/* 109 */             Location corner1 = new Location(player.getWorld(), toLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 110 */             Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/* 111 */             if (check.getType() == Material.AIR) {
/* 112 */               cutPasteIF_ELSE(player.getWorld(), corner1, corner2, toLoc, "null");
/*     */             } else {
/* 114 */               cutPasteIF_ELSE(player.getWorld(), corner1, corner2, toLoc, "default");
/*     */             } 
/* 116 */             event.setCancelled(false);
/* 117 */             Block westPiston = world.getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 118 */             westPiston.setType(Material.PISTON_BASE);
/* 119 */             westPiston.setData((byte)4);
/* 120 */             Block eastPiston = world.getBlockAt(blockLoc.getBlockX() - 3, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 121 */             eastPiston.setType(Material.PISTON_BASE);
/* 122 */             eastPiston.setData((byte)5);
/* 123 */             Block sign = world.getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ() - 1);
/* 124 */             sign.setType(Material.WALL_SIGN);
/* 125 */             lore.add("Если переменная");
/* 126 */             Sign.configSign(sign.getLocation(), player.getWorld(), lore);
/*     */ 
/*     */ 
/*     */ 
/*     */           
/*     */           }
/* 132 */           else if (block.getType() == Material.ENDER_STONE) {
/* 133 */             Block piston = world.getBlockAt(blockLoc.getBlockX() + 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 134 */             if (piston.getType() == Material.PISTON_BASE && piston.getData() == 5) {
/* 135 */               Block check = player.getWorld().getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 136 */               Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 1), (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/* 137 */               Location corner1 = new Location(player.getWorld(), toLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 138 */               Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/* 139 */               if (check.getType() == Material.AIR) {
/* 140 */                 cutPasteIF_ELSE(player.getWorld(), corner1, corner2, toLoc, "null");
/*     */               } else {
/* 142 */                 cutPasteIF_ELSE(player.getWorld(), corner1, corner2, toLoc, "default");
/*     */               } 
/* 144 */               event.setCancelled(false);
/* 145 */               Block westPiston = world.getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 146 */               westPiston.setType(Material.PISTON_BASE);
/* 147 */               westPiston.setData((byte)4);
/* 148 */               Block eastPiston = world.getBlockAt(blockLoc.getBlockX() - 3, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 149 */               eastPiston.setType(Material.PISTON_BASE);
/* 150 */               eastPiston.setData((byte)5);
/* 151 */               Block sign = world.getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ() - 1);
/* 152 */               sign.setType(Material.WALL_SIGN);
/* 153 */               lore.add("Иначе");
/* 154 */               Sign.configSign(sign.getLocation(), player.getWorld(), lore);
/*     */ 
/*     */             
/*     */             }
/*     */ 
/*     */           
/*     */           }
/* 161 */           else if (block.getType() == Material.COBBLESTONE) {
/* 162 */             Block check = player.getWorld().getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 163 */             Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 1), (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/* 164 */             Location corner1 = new Location(player.getWorld(), toLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 165 */             Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/* 166 */             if (check.getType() == Material.AIR) {
/* 167 */               cutPaste(player.getWorld(), corner1, corner2, toLoc, "blocks");
/*     */             } else {
/* 169 */               cutPaste(player.getWorld(), corner1, corner2, toLoc, "default");
/*     */             } 
/* 171 */             event.setCancelled(false);
/* 172 */             Block extBlock = world.getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 173 */             extBlock.setType(Material.STONE);
/* 174 */             Block sign = world.getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ() - 1);
/* 175 */             sign.setType(Material.WALL_SIGN);
/* 176 */             lore.add("Действие игрока");
/* 177 */             Sign.configSign(sign.getLocation(), player.getWorld(), lore);
/* 178 */           } else if (block.getType() == Material.NETHER_BRICK) {
/* 179 */             Block check = player.getWorld().getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 180 */             Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 1), (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/* 181 */             Location corner1 = new Location(player.getWorld(), toLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 182 */             Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/* 183 */             if (check.getType() == Material.AIR) {
/* 184 */               cutPaste(player.getWorld(), corner1, corner2, toLoc, "blocks");
/*     */             } else {
/* 186 */               cutPaste(player.getWorld(), corner1, corner2, toLoc, "default");
/*     */             } 
/* 188 */             event.setCancelled(false);
/* 189 */             Block extBlock = world.getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 190 */             extBlock.setType(Material.NETHERRACK);
/* 191 */             Block sign = world.getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ() - 1);
/* 192 */             sign.setType(Material.WALL_SIGN);
/* 193 */             lore.add("Игровое действие");
/* 194 */             Sign.configSign(sign.getLocation(), player.getWorld(), lore);
/* 195 */           } else if (block.getType() == Material.IRON_BLOCK) {
/* 196 */             Block check = player.getWorld().getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 197 */             Location toLoc = new Location(player.getWorld(), (blockLoc.getBlockX() - 1), (blockLoc.getBlockY() + 1), (blockLoc.getBlockZ() - 1));
/* 198 */             Location corner1 = new Location(player.getWorld(), toLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 199 */             Location corner2 = new Location(player.getWorld(), -1.0D, toLoc.getBlockY(), toLoc.getBlockZ());
/* 200 */             if (check.getType() == Material.AIR) {
/* 201 */               cutPaste(player.getWorld(), corner1, corner2, toLoc, "blocks");
/*     */             } else {
/* 203 */               cutPaste(player.getWorld(), corner1, corner2, toLoc, "default");
/*     */             } 
/* 205 */             event.setCancelled(false);
/* 206 */             Block extBlock = world.getBlockAt(blockLoc.getBlockX() - 1, blockLoc.getBlockY(), blockLoc.getBlockZ());
/* 207 */             extBlock.setType(Material.IRON_ORE);
/* 208 */             Block sign = world.getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ() - 1);
/* 209 */             sign.setType(Material.WALL_SIGN);
/* 210 */             lore.add("Действие с переменной");
/* 211 */             Sign.configSign(sign.getLocation(), player.getWorld(), lore);
/*     */           } 
/* 213 */         } else if (block.getType() == Material.PISTON_BASE || block
/* 214 */           .getType() == Material.ANVIL || 
/* 215 */           isShulker(block.getType()).booleanValue() || block
/* 216 */           .getType() == Material.WORKBENCH) {
/* 217 */           event.setCancelled(false);
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private Boolean isShulker(Material material) {
/* 225 */     return Boolean.valueOf((material == Material.BLACK_SHULKER_BOX || material == Material.BLUE_SHULKER_BOX || material == Material.CYAN_SHULKER_BOX || material == Material.GRAY_SHULKER_BOX || material == Material.GREEN_SHULKER_BOX || material == Material.SILVER_SHULKER_BOX || material == Material.BROWN_SHULKER_BOX || material == Material.LIME_SHULKER_BOX || material == Material.MAGENTA_SHULKER_BOX || material == Material.PINK_SHULKER_BOX || material == Material.ORANGE_SHULKER_BOX || material == Material.RED_SHULKER_BOX || material == Material.WHITE_SHULKER_BOX || material == Material.PURPLE_SHULKER_BOX || material == Material.LIGHT_BLUE_SHULKER_BOX || material == Material.YELLOW_SHULKER_BOX));
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
/*     */   public static void cutPasteIF_ELSE(World world, Location corner1, Location corner2, Location pasteTo, String k) {
/* 245 */     int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
/* 246 */     int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
/* 247 */     int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
/* 248 */     int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
/* 249 */     int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
/* 250 */     int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
/* 251 */     double rad = 2.0D;
/* 252 */     if (k.equals("default")) {
/* 253 */       rad = 4.0D;
/*     */     }
/*     */     int x;
/* 256 */     for (x = minX; x <= maxX; x++) {
/* 257 */       for (int y = minY; y <= maxY; y++) {
/* 258 */         for (int z = minZ; z <= maxZ; z++) {
/* 259 */           Block block = world.getBlockAt(x, y, z);
/* 260 */           if (block.getType() != Material.AIR) {
/* 261 */             Block block2 = pasteTo.getWorld().getBlockAt((int)(x - rad), y + 2, z).getLocation().getBlock().getState().getBlock();
/* 262 */             block2.setType(block.getType());
/* 263 */             block2.setData(block.getData());
/* 264 */             if (block.getState() instanceof Chest) {
/* 265 */               Chest chest = (Chest)block.getState();
/* 266 */               ItemStack[] toAdd = chest.getInventory().getContents();
/*     */               
/* 268 */               ((Chest)block2.getState()).getInventory().setContents(toAdd);
/* 269 */               ((Chest)block.getState()).getInventory().clear();
/* 270 */             } else if (block.getState() instanceof Sign) {
/* 271 */               Sign sign = (Sign)block.getState();
/* 272 */               Sign sign2 = (Sign)block2.getState();
/* 273 */               String[] lines = sign.getLines();
/* 274 */               for (int i = 0; i < lines.length; i++) {
/* 275 */                 sign2.setLine(i, lines[i]);
/*     */               }
/* 277 */               sign2.update(false, false);
/*     */             } 
/* 279 */             block.setType(Material.AIR);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/* 284 */     for (x = minX; x <= maxX; x++) {
/* 285 */       for (int y = minY; y <= maxY; y++) {
/* 286 */         for (int z = minZ; z <= maxZ; z++) {
/* 287 */           Block block = world.getBlockAt((int)(x - rad), y + 2, z);
/* 288 */           if (block.getType() != Material.AIR) {
/* 289 */             Block block2 = pasteTo.getWorld().getBlockAt((int)(x - rad), y, z).getLocation().getBlock().getState().getBlock();
/* 290 */             block2.setType(block.getType());
/* 291 */             block2.setData(block.getData());
/* 292 */             if (block.getState() instanceof Chest) {
/* 293 */               Chest chest = (Chest)block.getState();
/* 294 */               ItemStack[] toAdd = chest.getInventory().getContents();
/*     */               
/* 296 */               ((Chest)block2.getState()).getInventory().setContents(toAdd);
/* 297 */               ((Chest)block.getState()).getInventory().clear();
/* 298 */             } else if (block.getState() instanceof Sign) {
/* 299 */               Sign sign = (Sign)block.getState();
/* 300 */               Sign sign2 = (Sign)block2.getState();
/* 301 */               String[] lines = sign.getLines();
/* 302 */               for (int i = 0; i < lines.length; i++) {
/* 303 */                 sign2.setLine(i, lines[i]);
/*     */               }
/* 305 */               sign2.update(false, false);
/*     */             } 
/* 307 */             block.setType(Material.AIR);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void cutPaste(World world, Location corner1, Location corner2, Location pasteTo, String k) {
/* 316 */     int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
/* 317 */     int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
/* 318 */     int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
/* 319 */     int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
/* 320 */     int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
/* 321 */     int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
/*     */     
/* 323 */     double rad = corner1.distance(pasteTo);
/* 324 */     if (k.equals("blocks")) {
/* 325 */       rad -= 2.0D;
/*     */     }
/*     */     int x;
/* 328 */     for (x = minX; x <= maxX; x++) {
/* 329 */       for (int y = minY; y <= maxY; y++) {
/* 330 */         for (int z = minZ; z <= maxZ; z++) {
/* 331 */           Block block = world.getBlockAt(x, y, z);
/* 332 */           if (block.getType() != Material.AIR) {
/* 333 */             Block block2 = pasteTo.getWorld().getBlockAt((int)(x - rad), y + 2, z).getLocation().getBlock().getState().getBlock();
/* 334 */             block2.setType(block.getType());
/* 335 */             block2.setData(block.getData());
/* 336 */             if (block.getState() instanceof Chest) {
/* 337 */               Chest chest = (Chest)block.getState();
/* 338 */               ItemStack[] toAdd = chest.getInventory().getContents();
/*     */               
/* 340 */               ((Chest)block2.getState()).getInventory().setContents(toAdd);
/* 341 */               ((Chest)block.getState()).getInventory().clear();
/* 342 */             } else if (block.getState() instanceof Sign) {
/* 343 */               Sign sign = (Sign)block.getState();
/* 344 */               Sign sign2 = (Sign)block2.getState();
/* 345 */               String[] lines = sign.getLines();
/* 346 */               for (int i = 0; i < lines.length; i++) {
/* 347 */                 sign2.setLine(i, lines[i]);
/*     */               }
/* 349 */               sign2.update(false, false);
/*     */             } 
/* 351 */             block.setType(Material.AIR);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/* 356 */     for (x = minX; x <= maxX; x++) {
/* 357 */       for (int y = minY; y <= maxY; y++) {
/* 358 */         for (int z = minZ; z <= maxZ; z++) {
/* 359 */           Block block = world.getBlockAt((int)(x - rad), y + 2, z);
/* 360 */           if (block.getType() != Material.AIR) {
/* 361 */             Block block2 = pasteTo.getWorld().getBlockAt((int)(x - rad), y, z).getLocation().getBlock().getState().getBlock();
/* 362 */             block2.setType(block.getType());
/* 363 */             block2.setData(block.getData());
/* 364 */             if (block.getState() instanceof Chest) {
/* 365 */               Chest chest = (Chest)block.getState();
/* 366 */               ItemStack[] toAdd = chest.getInventory().getContents();
/*     */               
/* 368 */               ((Chest)block2.getState()).getInventory().setContents(toAdd);
/* 369 */               ((Chest)block.getState()).getInventory().clear();
/* 370 */             } else if (block.getState() instanceof Sign) {
/* 371 */               Sign sign = (Sign)block.getState();
/* 372 */               Sign sign2 = (Sign)block2.getState();
/* 373 */               String[] lines = sign.getLines();
/* 374 */               for (int i = 0; i < lines.length; i++) {
/* 375 */                 sign2.setLine(i, lines[i]);
/*     */               }
/* 377 */               sign2.update(false, false);
/*     */             } 
/* 379 */             block.setType(Material.AIR);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Blocks\onPlace.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */