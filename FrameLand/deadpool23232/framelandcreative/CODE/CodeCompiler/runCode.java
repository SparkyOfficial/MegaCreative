/*      */ package deadpool23232.framelandcreative.CODE.CodeCompiler;
/*      */ import deadpool23232.framelandcreative.Configs.WorldCode;
/*      */ import deadpool23232.framelandcreative.Configs.WorldData;
/*      */ import java.util.Arrays;
/*      */ import java.util.HashSet;
/*      */ import java.util.List;
/*      */ import java.util.Set;
/*      */ import java.util.stream.Collectors;
/*      */ import org.bukkit.Location;
/*      */ import org.bukkit.Material;
/*      */ import org.bukkit.World;
/*      */ import org.bukkit.block.Block;
/*      */ import org.bukkit.block.Chest;
/*      */ import org.bukkit.entity.Entity;
/*      */ import org.bukkit.entity.LivingEntity;
/*      */ import org.bukkit.entity.Player;
/*      */ import org.bukkit.event.Event;
/*      */ import org.bukkit.event.EventHandler;
/*      */ import org.bukkit.event.block.BlockEvent;
/*      */ import org.bukkit.event.entity.EntityDamageByEntityEvent;
/*      */ import org.bukkit.event.entity.EntityDeathEvent;
/*      */ import org.bukkit.event.inventory.InventoryClickEvent;
/*      */ import org.bukkit.event.player.AsyncPlayerChatEvent;
/*      */ import org.bukkit.event.player.PlayerDropItemEvent;
/*      */ import org.bukkit.event.player.PlayerInteractEvent;
/*      */ import org.bukkit.event.player.PlayerItemBreakEvent;
/*      */ import org.bukkit.event.player.PlayerItemHeldEvent;
/*      */ import org.bukkit.event.player.PlayerPickupItemEvent;
/*      */ import org.bukkit.inventory.Inventory;
/*      */ import org.bukkit.inventory.ItemStack;
/*      */ import org.bukkit.inventory.meta.ItemMeta;
/*      */ 
/*      */ public class runCode implements Listener {
/*      */   @EventHandler
/*      */   public void joinEvent(PlayerChangedWorldEvent event) {
/*   36 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*   37 */       Player player = event.getPlayer();
/*   38 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*   39 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*   42 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*   43 */       for (List<String> line : code) {
/*   44 */         if (((String)line.get(0)).equals("joinEvent")) {
/*   45 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void quitEvent(PlayerChangedWorldEvent event) {
/*   53 */     if (event.getFrom().getName().contains("-world")) {
/*   54 */       World world = event.getFrom();
/*   55 */       Player player = event.getPlayer();
/*   56 */       String id = event.getFrom().getName().replace("-world", "");
/*   57 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*   60 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*   61 */       for (List<String> line : code) {
/*   62 */         if (((String)line.get(0)).equals("quitEvent")) {
/*   63 */           handleLine(line, (Entity)player, world, 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void breakEvent(BlockBreakEvent event) {
/*   71 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*   72 */       Player player = event.getPlayer();
/*   73 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*   74 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*   77 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*   78 */       for (List<String> line : code) {
/*   79 */         if (((String)line.get(0)).equals("breakEvent")) {
/*   80 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void placeEvent(BlockPlaceEvent event) {
/*   88 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*   89 */       Player player = event.getPlayer();
/*   90 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*   91 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*   94 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*   95 */       for (List<String> line : code) {
/*   96 */         if (((String)line.get(0)).equals("placeEvent")) {
/*   97 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void moveEvent(PlayerMoveEvent event) {
/*  105 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*  106 */       Location from = event.getFrom();
/*  107 */       Location to = event.getTo();
/*  108 */       int fromX = from.getBlockX();
/*  109 */       int fromZ = from.getBlockZ();
/*  110 */       int toX = to.getBlockX();
/*  111 */       int toZ = to.getBlockZ();
/*  112 */       if (fromX != toX || fromZ != toZ) {
/*  113 */         Player player = event.getPlayer();
/*  114 */         String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  115 */         List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */         
/*  118 */         List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  119 */         for (List<String> line : code) {
/*  120 */           if (((String)line.get(0)).equals("moveEvent")) {
/*  121 */             handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */           }
/*      */         } 
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void LMBEvent(PlayerInteractEvent event) {
/*  130 */     if (event.getPlayer().getWorld().getName().contains("-world") && (
/*  131 */       event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
/*  132 */       Player player = event.getPlayer();
/*  133 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  134 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  137 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  138 */       for (List<String> line : code) {
/*  139 */         if (((String)line.get(0)).equals("LMBEvent")) {
/*  140 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   @EventHandler
/*      */   public void RMBEvent(PlayerInteractEvent event) {
/*  149 */     if (event.getPlayer().getWorld().getName().contains("-world") && (
/*  150 */       event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && 
/*  151 */       event.getHand() == EquipmentSlot.HAND) {
/*  152 */       Player player = event.getPlayer();
/*  153 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  154 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  157 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  158 */       for (List<String> line : code) {
/*  159 */         if (((String)line.get(0)).equals("RMBEvent")) {
/*  160 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   @EventHandler(priority = EventPriority.LOWEST)
/*      */   public void messageEvent(AsyncPlayerChatEvent event) {
/*  170 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*  171 */       Player player = event.getPlayer();
/*  172 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  173 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  176 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  177 */       for (List<String> line : code) {
/*  178 */         if (((String)line.get(0)).equals("messageEvent")) {
/*  179 */           handleLine(line, (Entity)Bukkit.getPlayer(player.getName()), player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void mobDeath(EntityDeathEvent event) {
/*  187 */     if (event.getEntity().getWorld().getName().contains("-world")) {
/*  188 */       String id = event.getEntity().getWorld().getName().replace("-world", "");
/*  189 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  192 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  193 */       for (List<String> line : code) {
/*  194 */         if (((String)line.get(0)).equals("mobDeathEvent")) {
/*  195 */           handleLine(line, (Entity)event.getEntity(), event.getEntity().getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void playerDeath(PlayerDeathEvent event) {
/*  203 */     if (event.getEntity().getWorld().getName().contains("-world")) {
/*  204 */       String id = event.getEntity().getWorld().getName().replace("-world", "");
/*  205 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  208 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  209 */       for (List<String> line : code) {
/*  210 */         if (((String)line.get(0)).equals("playerDeathEvent")) {
/*  211 */           handleLine(line, (Entity)event.getEntity(), event.getEntity().getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void playerKillPlayer(EntityDeathEvent event) {
/*  219 */     if (event.getEntity() instanceof Player && event.getEntity().getKiller() != null && 
/*  220 */       event.getEntity().getKiller().getWorld().getName().contains("-world")) {
/*  221 */       String id = event.getEntity().getKiller().getWorld().getName().replace("-world", "");
/*  222 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  225 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  226 */       for (List<String> line : code) {
/*  227 */         if (((String)line.get(0)).equals("plKillPlEvent")) {
/*  228 */           handleLine(line, (Entity)event.getEntity().getKiller(), event.getEntity().getKiller().getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   @EventHandler
/*      */   public void playerKillMob(EntityDeathEvent event) {
/*  237 */     if (event.getEntity() instanceof org.bukkit.entity.Monster && event.getEntity().getKiller() != null && 
/*  238 */       event.getEntity().getKiller().getWorld().getName().contains("-world")) {
/*  239 */       String id = event.getEntity().getKiller().getWorld().getName().replace("-world", "");
/*  240 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  243 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  244 */       for (List<String> line : code) {
/*  245 */         if (((String)line.get(0)).equals("plKillMobEvent")) {
/*  246 */           handleLine(line, (Entity)event.getEntity().getKiller(), event.getEntity().getKiller().getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   @EventHandler
/*      */   public void playerPlayerDamage(EntityDamageByEntityEvent event) {
/*  255 */     if (event.getDamager() instanceof Player && event.getEntity() instanceof Player && 
/*  256 */       event.getDamager().getWorld().getName().contains("-world") && 
/*  257 */       event.getEntity() instanceof Player) {
/*  258 */       String id = event.getDamager().getWorld().getName().replace("-world", "");
/*  259 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  262 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  263 */       for (List<String> line : code) {
/*  264 */         if (((String)line.get(0)).equals("plDmgPlEvent")) {
/*  265 */           handleLine(line, event.getDamager(), event.getDamager().getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   @EventHandler
/*      */   public void playerMobDamage(EntityDamageByEntityEvent event) {
/*  275 */     if (event.getEntity() instanceof Player && event.getDamager() instanceof org.bukkit.entity.Monster && 
/*  276 */       event.getEntity().getWorld().getName().contains("-world")) {
/*  277 */       String id = event.getEntity().getWorld().getName().replace("-world", "");
/*  278 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  281 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  282 */       for (List<String> line : code) {
/*  283 */         if (((String)line.get(0)).equals("mobDmgPlEvent")) {
/*  284 */           handleLine(line, event.getDamager(), event.getDamager().getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   @EventHandler
/*      */   public void mobPlayerDamage(EntityDamageByEntityEvent event) {
/*  293 */     if (event.getDamager() instanceof Player && event.getEntity() instanceof org.bukkit.entity.Monster && 
/*  294 */       event.getDamager().getWorld().getName().contains("-world")) {
/*  295 */       String id = event.getDamager().getWorld().getName().replace("-world", "");
/*  296 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  299 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  300 */       for (List<String> line : code) {
/*  301 */         if (((String)line.get(0)).equals("plDmgMobEvent")) {
/*  302 */           handleLine(line, event.getDamager(), event.getDamager().getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   @EventHandler
/*      */   public void mobMobDamage(EntityDamageByEntityEvent event) {
/*  311 */     if (event.getDamager().getWorld().getName().contains("-world")) {
/*  312 */       String id = event.getDamager().getWorld().getName().replace("-world", "");
/*  313 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  316 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  317 */       for (List<String> line : code) {
/*  318 */         if (((String)line.get(0)).equals("mobDmgMobEvent")) {
/*  319 */           handleLine(line, event.getDamager(), event.getDamager().getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void inventoryOpenEvent(InventoryOpenEvent event) {
/*  327 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*  328 */       Player player = (Player)event.getPlayer();
/*  329 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  330 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  333 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  334 */       for (List<String> line : code) {
/*  335 */         if (((String)line.get(0)).equals("invOpenEvent")) {
/*  336 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void inventoryCloseEvent(InventoryCloseEvent event) {
/*  344 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*  345 */       Player player = (Player)event.getPlayer();
/*  346 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  347 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  350 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  351 */       for (List<String> line : code) {
/*  352 */         if (((String)line.get(0)).equals("invCloseEvent")) {
/*  353 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void inventoryClickEvent(InventoryClickEvent event) {
/*  361 */     if (event.getWhoClicked().getWorld().getName().contains("-world") && 
/*  362 */       event.getCurrentItem() != null) {
/*  363 */       Player player = (Player)event.getWhoClicked();
/*  364 */       String id = event.getWhoClicked().getWorld().getName().replace("-world", "");
/*  365 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  368 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  369 */       for (List<String> line : code) {
/*  370 */         if (((String)line.get(0)).equals("invClickEvent")) {
/*  371 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   @EventHandler
/*      */   public void itemPickup(PlayerPickupItemEvent event) {
/*  381 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*  382 */       Player player = event.getPlayer();
/*  383 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  384 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  387 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  388 */       for (List<String> line : code) {
/*  389 */         if (((String)line.get(0)).equals("itemPickupEvent")) {
/*  390 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void itemDrop(PlayerDropItemEvent event) {
/*  398 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*  399 */       Player player = event.getPlayer();
/*  400 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  401 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  404 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  405 */       for (List<String> line : code) {
/*  406 */         if (((String)line.get(0)).equals("itemDropEvent")) {
/*  407 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void teleportEvent(PlayerTeleportEvent event) {
/*  415 */     if (event.getFrom().getWorld().getName().equals(event.getTo().getWorld().getName())) {
/*  416 */       Player player = event.getPlayer();
/*  417 */       if (player.getWorld().getName().contains("-world")) {
/*  418 */         String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  419 */         List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */         
/*  422 */         List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  423 */         for (List<String> line : code) {
/*  424 */           if (((String)line.get(0)).equals("teleportEvent")) {
/*  425 */             handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */           }
/*      */         } 
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void itemDamage(PlayerItemDamageEvent event) {
/*  434 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*  435 */       Player player = event.getPlayer();
/*  436 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  437 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  440 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  441 */       for (List<String> line : code) {
/*  442 */         if (((String)line.get(0)).equals("itemDamageEvent")) {
/*  443 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void itemBreak(PlayerItemBreakEvent event) {
/*  451 */     if (event.getPlayer().getWorld().getName().contains("-world")) {
/*  452 */       Player player = event.getPlayer();
/*  453 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  454 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  457 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  458 */       for (List<String> line : code) {
/*  459 */         if (((String)line.get(0)).equals("itemBreakEvent")) {
/*  460 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void slotChange(PlayerItemHeldEvent event) {
/*  468 */     if (event.getPreviousSlot() != event.getNewSlot() && 
/*  469 */       event.getPlayer().getWorld().getName().contains("-world")) {
/*  470 */       Player player = event.getPlayer();
/*  471 */       String id = event.getPlayer().getWorld().getName().replace("-world", "");
/*  472 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  475 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  476 */       for (List<String> line : code) {
/*  477 */         if (((String)line.get(0)).equals("slotChangeEvent")) {
/*  478 */           handleLine(line, (Entity)player, player.getWorld(), 1, (Event)event);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void function(String func, Entity player) {
/*  488 */     if (player.getWorld().getName().contains("-world")) {
/*  489 */       String id = player.getWorld().getName().replace("-world", "");
/*  490 */       List<String> code_source = WorldCode.get().getStringList("worlds." + id);
/*      */ 
/*      */       
/*  493 */       List<List<String>> code = (List<List<String>>)code_source.stream().map(s -> Arrays.asList(s.split("&"))).collect(Collectors.toList());
/*  494 */       for (List<String> line : code) {
/*  495 */         if (((String)line.get(0)).equals("function(" + func + ")")) {
/*  496 */           handleLine(line, player, player.getWorld(), 1, null);
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void openInventory(String eq, String type, Entity entity1asd, World world, Event e) {
/*  505 */     List<String> why = Arrays.asList(eq.split("\\|"));
/*  506 */     if (type.equals("copy")) {
/*      */       try {
/*  508 */         if (eq.startsWith("apple")) {
/*  509 */           Set<Material> transparentBlock; int appleFrom = eq.indexOf("[") + 1;
/*  510 */           int appleTo = eq.indexOf("]~");
/*  511 */           String apple = eq.substring(appleFrom, appleTo);
/*  512 */           Player player = (Player)entity1asd;
/*      */           
/*  514 */           Location coords = null;
/*  515 */           switch (apple) {
/*      */             case "location":
/*  517 */               coords = player.getLocation();
/*      */               break;
/*      */             case "look_block_loc":
/*  520 */               transparentBlock = new HashSet<>();
/*  521 */               transparentBlock.add(Material.AIR);
/*  522 */               coords = player.getTargetBlock(transparentBlock, 12).getLocation();
/*      */               break;
/*      */             case "block_loc":
/*  525 */               if (e instanceof BlockEvent) {
/*  526 */                 coords = ((BlockEvent)e).getBlock().getLocation();
/*      */               }
/*      */               break;
/*      */           } 
/*  530 */           if (coords != null && 
/*  531 */             coords.getBlock().getState() instanceof Chest) {
/*  532 */             Chest chest = (Chest)coords.getBlock().getState();
/*  533 */             String name = chest.getInventory().getName();
/*  534 */             if (name.equals("container.chest")) {
/*  535 */               name = "Chest";
/*      */             }
/*  537 */             Inventory chestinventory = Bukkit.createInventory(null, chest.getInventory().getSize(), name);
/*  538 */             chestinventory.setContents(chest.getInventory().getContents());
/*  539 */             ((Player)entity1asd).openInventory(chestinventory);
/*      */           } 
/*      */         } else {
/*      */           
/*  543 */           Location coords = new Location(world, Integer.parseInt(why.get(0)), Integer.parseInt(why.get(1)), Integer.parseInt(why.get(2)));
/*  544 */           if (coords.getBlock().getState() instanceof Chest) {
/*  545 */             Chest chest = (Chest)coords.getBlock().getState();
/*  546 */             String name = chest.getInventory().getName();
/*  547 */             if (name.equals("container.chest")) {
/*  548 */               name = "Chest";
/*      */             }
/*  550 */             Inventory chestinventory = Bukkit.createInventory(null, chest.getInventory().getSize(), name);
/*  551 */             chestinventory.setContents(chest.getInventory().getContents());
/*  552 */             ((Player)entity1asd).openInventory(chestinventory);
/*      */           } 
/*      */         } 
/*  555 */       } catch (Exception exception) {}
/*      */     
/*      */     }
/*  558 */     else if (type.equals("original")) {
/*      */       try {
/*  560 */         if (eq.startsWith("apple")) {
/*  561 */           Set<Material> transparentBlock; int appleFrom = eq.indexOf("[") + 1;
/*  562 */           int appleTo = eq.indexOf("]~");
/*  563 */           String apple = eq.substring(appleFrom, appleTo);
/*  564 */           Player player = (Player)entity1asd;
/*      */           
/*  566 */           Location coords = null;
/*  567 */           switch (apple) {
/*      */             case "location":
/*  569 */               coords = player.getLocation();
/*      */               break;
/*      */             case "look_block_loc":
/*  572 */               transparentBlock = new HashSet<>();
/*  573 */               transparentBlock.add(Material.AIR);
/*  574 */               coords = player.getTargetBlock(transparentBlock, 12).getLocation();
/*      */               break;
/*      */             case "block_loc":
/*  577 */               if (e instanceof BlockEvent) {
/*  578 */                 coords = ((BlockEvent)e).getBlock().getLocation();
/*      */               }
/*      */               break;
/*      */           } 
/*  582 */           if (coords != null && 
/*  583 */             coords.getBlock().getState() instanceof Chest) {
/*  584 */             Chest chest = (Chest)coords.getBlock().getState();
/*  585 */             Inventory chestInventory = chest.getInventory();
/*  586 */             ((Player)entity1asd).openInventory(chestInventory);
/*      */           } 
/*      */         } else {
/*      */           
/*  590 */           Location coords = new Location(world, Integer.parseInt(why.get(0)), Integer.parseInt(why.get(1)), Integer.parseInt(why.get(2)));
/*  591 */           if (coords.getBlock().getState() instanceof Chest) {
/*  592 */             Chest chest = (Chest)coords.getBlock().getState();
/*  593 */             Inventory chestInventory = chest.getInventory();
/*  594 */             ((Player)entity1asd).openInventory(chestInventory);
/*      */           }
/*      */         
/*      */         } 
/*  598 */       } catch (Exception exception) {}
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public void worldMessage(String message, Entity entity1asd, World world, Event e, Integer random) {
/*  604 */     for (Player players : world.getPlayers()) {
/*      */       
/*  606 */       String result = message.replace("%player%", entity1asd.getName()).replace("%random%", ((Player)world.getPlayers().get(random.intValue())).getName());
/*  607 */       if (e instanceof EntityDeathEvent) {
/*  608 */         result = result.replace("%victim%", ((EntityDeathEvent)e).getEntity().getName());
/*  609 */         result = result.replace("%damager%", ((EntityDeathEvent)e).getEntity().getKiller().getName());
/*      */       } 
/*  611 */       if (e instanceof EntityDamageByEntityEvent) {
/*  612 */         result = result.replace("%victim%", ((EntityDamageByEntityEvent)e).getEntity().getName());
/*  613 */         result = result.replace("%damager%", ((EntityDamageByEntityEvent)e).getDamager().getName());
/*      */       } 
/*  615 */       if (e instanceof AsyncPlayerChatEvent) {
/*  616 */         result = result.replace("%message%", ((AsyncPlayerChatEvent)e).getMessage());
/*      */       }
/*      */       
/*  619 */       Player player = (Player)entity1asd;
/*  620 */       String world_id = player.getWorld().getName().replace("-code", "").replace("-world", "");
/*      */       
/*  622 */       double health_now = player.getHealth();
/*  623 */       double health_max = player.getHealthScale();
/*  624 */       int hunger = player.getFoodLevel();
/*  625 */       float satiety = player.getSaturation();
/*  626 */       int xp = player.getTotalExperience();
/*  627 */       double armor = player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
/*  628 */       int air = Math.round(player.getRemainingAir() / 10.0F);
/*  629 */       int slot_now = player.getInventory().getHeldItemSlot();
/*  630 */       int ping = 0;
/*      */       try {
/*  632 */         Object entityPlayer = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
/*  633 */         ping = ((Integer)entityPlayer.getClass().getField("ping").get(entityPlayer)).intValue();
/*  634 */       } catch (Exception exception) {}
/*      */ 
/*      */       
/*  637 */       Location locationLocation = player.getLocation();
/*  638 */       String location = String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getPitch()) }).replace(",", ".");
/*  639 */       String inventory_name = player.getOpenInventory().getTitle();
/*  640 */       Set<Material> transparentBlock = new HashSet<>();
/*  641 */       transparentBlock.add(Material.AIR);
/*  642 */       Location look_block_locLocation = player.getTargetBlock(transparentBlock, 12).getLocation();
/*  643 */       String look_block_loc = String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getPitch()) }).replace(",", ".");
/*  644 */       double damage = 0.0D;
/*  645 */       if (e instanceof EntityDamageEvent) {
/*  646 */         damage = ((EntityDamageEvent)e).getFinalDamage();
/*      */       }
/*  648 */       int click_slot = 0;
/*  649 */       if (e instanceof InventoryClickEvent) {
/*  650 */         click_slot = ((InventoryClickEvent)e).getSlot();
/*      */       }
/*  652 */       int new_slot = 0;
/*  653 */       if (e instanceof PlayerItemHeldEvent) {
/*  654 */         new_slot = ((PlayerItemHeldEvent)e).getNewSlot();
/*      */       }
/*  656 */       int old_slot = 0;
/*  657 */       if (e instanceof PlayerItemHeldEvent) {
/*  658 */         old_slot = ((PlayerItemHeldEvent)e).getPreviousSlot();
/*      */       }
/*  660 */       String msg = "";
/*  661 */       if (e instanceof AsyncPlayerChatEvent) {
/*  662 */         msg = ((AsyncPlayerChatEvent)e).getMessage();
/*      */       }
/*  664 */       String block_loc = "";
/*  665 */       if (e instanceof BlockEvent) {
/*  666 */         Location block_locLocation = ((BlockEvent)e).getBlock().getLocation();
/*  667 */         block_loc = String.format("%.2f", new Object[] { Double.valueOf(block_locLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(block_locLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(block_locLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(block_locLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(block_locLocation.getPitch()) }).replace(",", ".");
/*      */       } 
/*  669 */       int player_count = player.getWorld().getPlayers().size();
/*  670 */       int like_count = WorldData.get().getInt("worlds." + world_id + ".liked");
/*  671 */       int unique_count = WorldData.get().getInt("worlds." + world_id + ".uniquePlayers");
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  694 */       result = result.replace("apple[health_now]~", String.valueOf(health_now)).replace("apple[health_max]~", String.valueOf(health_max)).replace("apple[hunger]~", String.valueOf(hunger)).replace("apple[satiety]~", String.valueOf(satiety)).replace("apple[xp]~", String.valueOf(xp)).replace("apple[armor]~", String.valueOf(armor)).replace("apple[air]~", String.valueOf(air)).replace("apple[slot_now]~", String.valueOf(slot_now)).replace("apple[ping]~", String.valueOf(ping)).replace("apple[location]~", location).replace("apple[inventory_name]~", inventory_name).replace("apple[look_block_loc]~", look_block_loc).replace("apple[damage]~", String.valueOf(damage)).replace("apple[click_slot]~", String.valueOf(click_slot)).replace("apple[new_slot]~", String.valueOf(new_slot)).replace("apple[old_slot]~", String.valueOf(old_slot)).replace("apple[message]~", msg).replace("apple[block_loc]~", block_loc).replace("apple[player_count]~", String.valueOf(player_count)).replace("apple[like_count]~", String.valueOf(like_count)).replace("apple[unique_count]~", String.valueOf(unique_count)).replace("apple[world_id]~", world_id);
/*      */       
/*  696 */       players.sendMessage(FrameLandCreative.Color(result.replaceAll("\\n", "\n")));
/*      */     } 
/*      */   }
/*      */   
/*      */   public void playerMessage(String message, Entity entity1asd, World world, Event e, Integer random) {
/*  701 */     String result = message.replace("%player%", entity1asd.getName()).replace("%random%", ((Player)world.getPlayers().get(random.intValue())).getName());
/*  702 */     if (e instanceof EntityDeathEvent) {
/*  703 */       result = result.replace("%victim%", ((EntityDeathEvent)e).getEntity().getName());
/*  704 */       result = result.replace("%damager%", ((EntityDeathEvent)e).getEntity().getKiller().getName());
/*      */     } 
/*  706 */     if (e instanceof EntityDamageByEntityEvent) {
/*  707 */       result = result.replace("%victim%", ((EntityDamageByEntityEvent)e).getEntity().getName());
/*  708 */       result = result.replace("%damager%", ((EntityDamageByEntityEvent)e).getDamager().getName());
/*      */     } 
/*  710 */     if (e instanceof AsyncPlayerChatEvent) {
/*  711 */       result = result.replace("%message%", ((AsyncPlayerChatEvent)e).getMessage());
/*      */     }
/*      */     
/*  714 */     Player player = (Player)entity1asd;
/*  715 */     String world_id = player.getWorld().getName().replace("-code", "").replace("-world", "");
/*      */     
/*  717 */     double health_now = player.getHealth();
/*  718 */     double health_max = player.getHealthScale();
/*  719 */     int hunger = player.getFoodLevel();
/*  720 */     float satiety = player.getSaturation();
/*  721 */     int xp = player.getTotalExperience();
/*  722 */     double armor = player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
/*  723 */     int air = Math.round(player.getRemainingAir() / 10.0F);
/*  724 */     int slot_now = player.getInventory().getHeldItemSlot();
/*  725 */     int ping = 0;
/*      */     try {
/*  727 */       Object entityPlayer = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
/*  728 */       ping = ((Integer)entityPlayer.getClass().getField("ping").get(entityPlayer)).intValue();
/*  729 */     } catch (Exception exception) {}
/*      */ 
/*      */     
/*  732 */     Location locationLocation = player.getLocation();
/*  733 */     String location = String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getPitch()) }).replace(",", ".");
/*  734 */     String inventory_name = player.getOpenInventory().getTitle();
/*  735 */     Set<Material> transparentBlock = new HashSet<>();
/*  736 */     transparentBlock.add(Material.AIR);
/*  737 */     Location look_block_locLocation = player.getTargetBlock(transparentBlock, 12).getLocation();
/*  738 */     String look_block_loc = String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getPitch()) }).replace(",", ".");
/*  739 */     double damage = 0.0D;
/*  740 */     if (e instanceof EntityDamageEvent) {
/*  741 */       damage = ((EntityDamageEvent)e).getFinalDamage();
/*      */     }
/*  743 */     int click_slot = 0;
/*  744 */     if (e instanceof InventoryClickEvent) {
/*  745 */       click_slot = ((InventoryClickEvent)e).getSlot();
/*      */     }
/*  747 */     int new_slot = 0;
/*  748 */     if (e instanceof PlayerItemHeldEvent) {
/*  749 */       new_slot = ((PlayerItemHeldEvent)e).getNewSlot();
/*      */     }
/*  751 */     int old_slot = 0;
/*  752 */     if (e instanceof PlayerItemHeldEvent) {
/*  753 */       old_slot = ((PlayerItemHeldEvent)e).getPreviousSlot();
/*      */     }
/*  755 */     String msg = "";
/*  756 */     if (e instanceof AsyncPlayerChatEvent) {
/*  757 */       msg = ((AsyncPlayerChatEvent)e).getMessage();
/*      */     }
/*  759 */     String block_loc = "";
/*  760 */     if (e instanceof BlockEvent) {
/*  761 */       Location block_locLocation = ((BlockEvent)e).getBlock().getLocation();
/*  762 */       block_loc = String.format("%.2f", new Object[] { Double.valueOf(block_locLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(block_locLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(block_locLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(block_locLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(block_locLocation.getPitch()) }).replace(",", ".");
/*      */     } 
/*  764 */     int player_count = player.getWorld().getPlayers().size();
/*  765 */     int like_count = WorldData.get().getInt("worlds." + world_id + ".liked");
/*  766 */     int unique_count = WorldData.get().getInt("worlds." + world_id + ".uniquePlayers");
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  789 */     result = result.replace("apple[health_now]~", String.valueOf(health_now)).replace("apple[health_max]~", String.valueOf(health_max)).replace("apple[hunger]~", String.valueOf(hunger)).replace("apple[satiety]~", String.valueOf(satiety)).replace("apple[xp]~", String.valueOf(xp)).replace("apple[armor]~", String.valueOf(armor)).replace("apple[air]~", String.valueOf(air)).replace("apple[slot_now]~", String.valueOf(slot_now)).replace("apple[ping]~", String.valueOf(ping)).replace("apple[location]~", location).replace("apple[inventory_name]~", inventory_name).replace("apple[look_block_loc]~", look_block_loc).replace("apple[damage]~", String.valueOf(damage)).replace("apple[click_slot]~", String.valueOf(click_slot)).replace("apple[new_slot]~", String.valueOf(new_slot)).replace("apple[old_slot]~", String.valueOf(old_slot)).replace("apple[message]~", msg).replace("apple[block_loc]~", block_loc).replace("apple[player_count]~", String.valueOf(player_count)).replace("apple[like_count]~", String.valueOf(like_count)).replace("apple[unique_count]~", String.valueOf(unique_count)).replace("apple[world_id]~", world_id);
/*      */     
/*  791 */     entity1asd.sendMessage(FrameLandCreative.Color(result.replaceAll("\\n", "\n")));
/*      */   }
/*      */   public void gameMode(String eq, Entity entity1asd) {
/*  794 */     Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), () -> {
/*      */           switch (eq) {
/*      */             case "creative":
/*      */               ((Player)entity1asd).getPlayer().setGameMode(GameMode.CREATIVE);
/*      */               break;
/*      */             case "survival":
/*      */               ((Player)entity1asd).getPlayer().setGameMode(GameMode.SURVIVAL);
/*      */               break;
/*      */             case "spectator":
/*      */               ((Player)entity1asd).getPlayer().setGameMode(GameMode.SPECTATOR);
/*      */               break;
/*      */             case "adventure":
/*      */               ((Player)entity1asd).getPlayer().setGameMode(GameMode.ADVENTURE);
/*      */               break;
/*      */           } 
/*      */         });
/*      */   }
/*      */   public void setHealth(String eq, Entity entity1asd, Event e) {
/*  812 */     if (eq.startsWith("apple")) {
/*  813 */       int appleFrom = eq.indexOf("[") + 1;
/*  814 */       int appleTo = eq.indexOf("]~");
/*  815 */       String apple = eq.substring(appleFrom, appleTo);
/*  816 */       LivingEntity player = (LivingEntity)entity1asd;
/*  817 */       Player pl = null;
/*  818 */       if (player instanceof Player) {
/*  819 */         pl = (Player)player;
/*      */       }
/*      */       
/*  822 */       String id = player.getWorld().getName().replace("-code", "").replace("-world", "");
/*  823 */       switch (apple) {
/*      */         case "health_now":
/*  825 */           player.setHealth(player.getHealth());
/*      */           break;
/*      */         case "health_max":
/*  828 */           assert pl != null;
/*  829 */           player.setHealth(pl.getHealthScale());
/*      */           break;
/*      */         case "hunger":
/*  832 */           assert pl != null;
/*  833 */           player.setHealth(pl.getFoodLevel());
/*      */           break;
/*      */         case "satiety":
/*  836 */           assert pl != null;
/*  837 */           player.setHealth(pl.getSaturation());
/*      */           break;
/*      */         case "xp":
/*  840 */           assert pl != null;
/*  841 */           player.setHealth(pl.getTotalExperience());
/*      */           break;
/*      */         case "armor":
/*  844 */           player.setHealth(player.getAttribute(Attribute.GENERIC_ARMOR).getValue());
/*      */           break;
/*      */         case "air":
/*  847 */           player.setHealth(Math.round(player.getRemainingAir() / 10.0F));
/*      */           break;
/*      */         case "slot_now":
/*  850 */           assert pl != null;
/*  851 */           player.setHealth(pl.getInventory().getHeldItemSlot());
/*      */           break;
/*      */         case "ping":
/*      */           try {
/*  855 */             assert pl != null;
/*  856 */             Object entityPlayer = pl.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
/*  857 */             player.setHealth(((Integer)entityPlayer.getClass().getField("ping").get(entityPlayer)).intValue());
/*  858 */           } catch (Exception exception) {}
/*      */           break;
/*      */ 
/*      */         
/*      */         case "damage":
/*  863 */           if (e instanceof EntityDamageEvent) {
/*  864 */             player.setHealth(((EntityDamageEvent)e).getFinalDamage());
/*      */           }
/*      */           break;
/*      */         case "click_slot":
/*  868 */           if (e instanceof InventoryClickEvent) {
/*  869 */             player.setHealth(((InventoryClickEvent)e).getSlot());
/*      */           }
/*      */           break;
/*      */         case "new_slot":
/*  873 */           if (e instanceof PlayerItemHeldEvent) {
/*  874 */             player.setHealth(((PlayerItemHeldEvent)e).getNewSlot());
/*      */           }
/*      */           break;
/*      */         case "old_slot":
/*  878 */           if (e instanceof PlayerItemHeldEvent) {
/*  879 */             player.setHealth(((PlayerItemHeldEvent)e).getPreviousSlot());
/*      */           }
/*      */           break;
/*      */         case "player_count":
/*  883 */           player.setHealth(player.getWorld().getPlayers().size());
/*      */           break;
/*      */         case "like_count":
/*  886 */           player.setHealth(WorldData.get().getInt("worlds." + id + ".liked"));
/*      */           break;
/*      */         case "unique_count":
/*  889 */           player.setHealth(WorldData.get().getInt("worlds." + id + ".uniquePlayers"));
/*      */           break;
/*      */         case "world_id":
/*  892 */           player.setHealth(Integer.parseInt(id));
/*      */           break;
/*      */       } 
/*      */     } else {
/*      */       try {
/*  897 */         int amount = Integer.parseInt(eq);
/*  898 */         ((LivingEntity)entity1asd).setHealth(amount);
/*  899 */       } catch (Exception exception) {}
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public void timeWait(String eq, Integer i, final List<String> line, final Entity eventEntity, final World world, final Event e, Entity entity1asd) {
/*  905 */     long ticksToWait = 0L;
/*  906 */     if (eq.startsWith("apple")) {
/*  907 */       if (entity1asd instanceof LivingEntity) {
/*  908 */         int appleFrom = eq.indexOf("[") + 1;
/*  909 */         int appleTo = eq.indexOf("]~");
/*  910 */         String apple = eq.substring(appleFrom, appleTo);
/*  911 */         LivingEntity player = (LivingEntity)entity1asd;
/*  912 */         Player pl = null;
/*  913 */         if (player instanceof Player) {
/*  914 */           pl = (Player)player;
/*      */         }
/*      */         
/*  917 */         String id = player.getWorld().getName().replace("-code", "").replace("-world", "");
/*  918 */         switch (apple) {
/*      */           case "health_now":
/*  920 */             ticksToWait = (long)player.getHealth() * 20L;
/*      */             break;
/*      */           case "health_max":
/*  923 */             assert pl != null;
/*  924 */             ticksToWait = (long)pl.getHealthScale() * 20L;
/*      */             break;
/*      */           case "hunger":
/*  927 */             assert pl != null;
/*  928 */             ticksToWait = pl.getFoodLevel() * 20L;
/*      */             break;
/*      */           case "satiety":
/*  931 */             assert pl != null;
/*  932 */             ticksToWait = (long)pl.getSaturation() * 20L;
/*      */             break;
/*      */           case "xp":
/*  935 */             assert pl != null;
/*  936 */             ticksToWait = pl.getTotalExperience() * 20L;
/*      */             break;
/*      */           case "armor":
/*  939 */             ticksToWait = (long)player.getAttribute(Attribute.GENERIC_ARMOR).getValue() * 20L;
/*      */             break;
/*      */           case "air":
/*  942 */             ticksToWait = Math.round(player.getRemainingAir() / 10.0F) * 20L;
/*      */             break;
/*      */           case "slot_now":
/*  945 */             assert pl != null;
/*  946 */             ticksToWait = pl.getInventory().getHeldItemSlot() * 20L;
/*      */             break;
/*      */           case "ping":
/*      */             try {
/*  950 */               assert pl != null;
/*  951 */               Object entityPlayer = pl.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
/*  952 */               ticksToWait = ((Long)entityPlayer.getClass().getField("ping").get(entityPlayer)).longValue() * 20L;
/*  953 */             } catch (Exception exception) {}
/*      */             break;
/*      */ 
/*      */           
/*      */           case "damage":
/*  958 */             if (e instanceof EntityDamageEvent) {
/*  959 */               ticksToWait = (long)((EntityDamageEvent)e).getFinalDamage() * 20L;
/*      */             }
/*      */             break;
/*      */           case "click_slot":
/*  963 */             if (e instanceof InventoryClickEvent) {
/*  964 */               ticksToWait = ((InventoryClickEvent)e).getSlot() * 20L;
/*      */             }
/*      */             break;
/*      */           case "new_slot":
/*  968 */             if (e instanceof PlayerItemHeldEvent) {
/*  969 */               ticksToWait = ((PlayerItemHeldEvent)e).getNewSlot() * 20L;
/*      */             }
/*      */             break;
/*      */           case "old_slot":
/*  973 */             if (e instanceof PlayerItemHeldEvent) {
/*  974 */               ticksToWait = ((PlayerItemHeldEvent)e).getPreviousSlot() * 20L;
/*      */             }
/*      */             break;
/*      */           case "player_count":
/*  978 */             ticksToWait = player.getWorld().getPlayers().size() * 20L;
/*      */             break;
/*      */           case "like_count":
/*  981 */             ticksToWait = WorldData.get().getInt("worlds." + id + ".liked") * 20L;
/*      */             break;
/*      */           case "unique_count":
/*  984 */             ticksToWait = WorldData.get().getInt("worlds." + id + ".uniquePlayers") * 20L;
/*      */             break;
/*      */           case "world_id":
/*  987 */             ticksToWait = Integer.parseInt(id) * 20L;
/*      */             break;
/*      */         } 
/*      */       } else {
/*  991 */         ticksToWait = Long.parseLong(eq);
/*      */       } 
/*      */     } else {
/*  994 */       ticksToWait = Long.parseLong(eq);
/*      */     } 
/*  996 */     final int finalI = i.intValue();
/*  997 */     (new BukkitRunnable()
/*      */       {
/*      */         public void run() {
/* 1000 */           runCode.this.handleLine(line, eventEntity, world, finalI + 1, e);
/*      */         }
/* 1002 */       }).runTaskLater(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), ticksToWait);
/*      */   }
/*      */   
/*      */   public void setBlock(String eq, World world, String type, Entity entity1asd, Event e) {
/* 1006 */     List<String> why = Arrays.asList(eq.split("\\|"));
/*      */     try {
/* 1008 */       Location coords = new Location(world, Integer.parseInt(why.get(0)), Integer.parseInt(why.get(1)), Integer.parseInt(why.get(2)));
/* 1009 */       if (eq.startsWith("apple")) {
/* 1010 */         int appleFrom = eq.indexOf("[") + 1;
/* 1011 */         int appleTo = eq.indexOf("]~");
/* 1012 */         String apple = eq.substring(appleFrom, appleTo);
/*      */         
/* 1014 */         switch (apple) {
/*      */           case "location":
/* 1016 */             coords = entity1asd.getLocation();
/*      */             break;
/*      */           case "look_block_loc":
/* 1019 */             if (entity1asd instanceof LivingEntity) {
/* 1020 */               Set<Material> transparentBlock = new HashSet<>();
/* 1021 */               transparentBlock.add(Material.AIR);
/* 1022 */               coords = ((LivingEntity)entity1asd).getTargetBlock(transparentBlock, 12).getLocation();
/*      */             } 
/*      */             break;
/*      */           case "block_loc":
/* 1026 */             if (e instanceof BlockEvent) {
/* 1027 */               coords = ((BlockEvent)e).getBlock().getLocation();
/*      */             }
/*      */             break;
/*      */         } 
/*      */       } 
/* 1032 */       if (type.startsWith("apple")) {
/* 1033 */         int appleFrom = type.indexOf("[") + 1;
/* 1034 */         int appleTo = type.indexOf("]~");
/* 1035 */         String apple = type.substring(appleFrom, appleTo);
/*      */         
/* 1037 */         switch (apple) {
/*      */           case "look_block":
/* 1039 */             if (entity1asd instanceof LivingEntity) {
/* 1040 */               Set<Material> transparentBlocks = new HashSet<>();
/* 1041 */               transparentBlocks.add(Material.AIR);
/* 1042 */               Block block = ((LivingEntity)entity1asd).getTargetBlock(transparentBlocks, 12);
/* 1043 */               type = String.valueOf(block.getState().getData().toItemStack(1).getType().getId());
/*      */             } 
/*      */             break;
/*      */           case "main_hand_item":
/* 1047 */             if (entity1asd instanceof Player) {
/* 1048 */               type = String.valueOf(((Player)entity1asd).getInventory().getItemInMainHand().getType().getId());
/*      */             }
/*      */             break;
/*      */           case "off_hand_item":
/* 1052 */             if (entity1asd instanceof Player) {
/* 1053 */               type = String.valueOf(((Player)entity1asd).getInventory().getItemInOffHand().getType().getId());
/*      */             }
/*      */             break;
/*      */           case "click_item":
/* 1057 */             if (e instanceof InventoryClickEvent) {
/* 1058 */               type = String.valueOf(((InventoryClickEvent)e).getCurrentItem().getType().getId());
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 1062 */             if (e instanceof BlockEvent) {
/* 1063 */               Block block1 = ((BlockEvent)e).getBlock();
/* 1064 */               type = String.valueOf(block1.getState().getData().toItemStack(1).getType().getId());
/*      */             } 
/*      */             break;
/*      */         } 
/*      */       } 
/* 1069 */       world.getBlockAt(coords).setType(Material.getMaterial(Integer.parseInt(type)));
/* 1070 */     } catch (Exception exception) {}
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void teleport(String eq, Entity entity1asd, Event e) {
/* 1076 */     List<String> why = Arrays.asList(eq.split("\\|"));
/*      */     try {
/* 1078 */       if (eq.startsWith("apple")) {
/* 1079 */         int appleFrom = eq.indexOf("[") + 1;
/* 1080 */         int appleTo = eq.indexOf("]~");
/* 1081 */         String apple = eq.substring(appleFrom, appleTo);
/*      */         
/* 1083 */         Location coords = null;
/* 1084 */         switch (apple) {
/*      */           case "location":
/* 1086 */             coords = entity1asd.getLocation();
/*      */             break;
/*      */           case "look_block_loc":
/* 1089 */             if (entity1asd instanceof LivingEntity) {
/* 1090 */               Set<Material> transparentBlock = new HashSet<>();
/* 1091 */               transparentBlock.add(Material.AIR);
/* 1092 */               coords = ((LivingEntity)entity1asd).getTargetBlock(transparentBlock, 12).getLocation();
/*      */             } 
/*      */             break;
/*      */           case "block_loc":
/* 1096 */             if (e instanceof BlockEvent) {
/* 1097 */               coords = ((BlockEvent)e).getBlock().getLocation();
/*      */             }
/*      */             break;
/*      */         } 
/* 1101 */         if (coords != null) {
/* 1102 */           entity1asd.teleport(coords);
/*      */         }
/*      */       } else {
/* 1105 */         Location coords = new Location(entity1asd.getWorld(), Float.parseFloat(why.get(0)), Float.parseFloat(why.get(1)), Float.parseFloat(why.get(2)), Float.parseFloat(why.get(3)), Float.parseFloat(why.get(4)));
/* 1106 */         entity1asd.teleport(coords);
/*      */       } 
/* 1108 */     } catch (Exception exception) {}
/*      */   }
/*      */ 
/*      */   
/*      */   public void useFunc(String type, String eq, Entity entity1asd) {
/* 1113 */     if (type.equals("sync")) {
/* 1114 */       function(eq, entity1asd);
/* 1115 */     } else if (type.equals("async")) {
/* 1116 */       Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), () -> function(eq, entity1asd));
/*      */     } 
/*      */   }
/*      */   
/*      */   public void giveItems(String func, Entity entity1asd, Event e) {
/* 1121 */     String items = func.substring(func.indexOf("(") + 1);
/* 1122 */     String[] itemList = items.split("\\|");
/* 1123 */     List<ItemStack> itemStackList = new ArrayList<>();
/* 1124 */     for (String l : itemList) {
/* 1125 */       if (l.startsWith("item")) {
/* 1126 */         int start = l.indexOf("[-") + 2;
/* 1127 */         int end = l.indexOf("-]");
/* 1128 */         int lrStart = l.indexOf("[+") + 2;
/* 1129 */         int lrEnd = l.indexOf("+]");
/* 1130 */         int amStart = l.indexOf("[=") + 2;
/* 1131 */         int amEnd = l.indexOf("=]");
/* 1132 */         int centerTo = l.indexOf('*');
/* 1133 */         int centerFrom = l.indexOf('*') + 1;
/*      */         
/* 1135 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 1136 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 1137 */         String name = l.substring(centerFrom, end);
/* 1138 */         String lore = l.substring(lrStart, lrEnd);
/*      */         
/* 1140 */         ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 1141 */         if (!name.equals("default")) {
/* 1142 */           ItemMeta itemMeta = item.getItemMeta();
/* 1143 */           itemMeta.setDisplayName(name);
/* 1144 */           item.setItemMeta(itemMeta);
/*      */         } 
/* 1146 */         if (!lore.equals("none")) {
/* 1147 */           ItemMeta itemMeta = item.getItemMeta();
/* 1148 */           List<String> loreList = Arrays.asList(lore.split("\n"));
/* 1149 */           itemMeta.setLore(loreList);
/* 1150 */           item.setItemMeta(itemMeta);
/*      */         } 
/* 1152 */         itemStackList.add(item);
/* 1153 */       } else if (l.startsWith("apple")) {
/* 1154 */         Set<Material> transparentBlocks; Block block; int appleFrom = l.indexOf("[") + 1;
/* 1155 */         int appleTo = l.indexOf("]~");
/* 1156 */         String apple = l.substring(appleFrom, appleTo);
/* 1157 */         Player player = (Player)entity1asd;
/*      */         
/* 1159 */         switch (apple) {
/*      */           case "look_block":
/* 1161 */             transparentBlocks = new HashSet<>();
/* 1162 */             transparentBlocks.add(Material.AIR);
/* 1163 */             block = player.getTargetBlock(transparentBlocks, 12);
/* 1164 */             itemStackList.add(block.getState().getData().toItemStack(1));
/*      */             break;
/*      */           case "main_hand_item":
/* 1167 */             itemStackList.add(player.getInventory().getItemInMainHand());
/*      */             break;
/*      */           case "off_hand_item":
/* 1170 */             itemStackList.add(player.getInventory().getItemInOffHand());
/*      */             break;
/*      */           case "click_item":
/* 1173 */             if (e instanceof InventoryClickEvent) {
/* 1174 */               itemStackList.add(((InventoryClickEvent)e).getCurrentItem());
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 1178 */             if (e instanceof BlockEvent) {
/* 1179 */               Block block1 = ((BlockEvent)e).getBlock();
/* 1180 */               itemStackList.add(block1.getState().getData().toItemStack(1));
/*      */             } 
/*      */             break;
/*      */         } 
/*      */       } 
/*      */     } 
/* 1186 */     if (!itemStackList.isEmpty()) {
/* 1187 */       for (ItemStack item : itemStackList) {
/*      */         try {
/* 1189 */           ((Player)entity1asd).getInventory().addItem(new ItemStack[] { item });
/* 1190 */         } catch (Exception exception) {}
/*      */       } 
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void giveRandItem(String func, Entity entity1asd, Event e) {
/* 1198 */     String items = func.substring(func.indexOf("(") + 1);
/* 1199 */     String[] itemList = items.split("\\|");
/* 1200 */     List<ItemStack> itemStackList = new ArrayList<>();
/* 1201 */     for (String l : itemList) {
/* 1202 */       if (l.startsWith("item")) {
/* 1203 */         int start = l.indexOf("[-") + 2;
/* 1204 */         int end = l.indexOf("-]");
/* 1205 */         int lrStart = l.indexOf("[+") + 2;
/* 1206 */         int lrEnd = l.indexOf("+]");
/* 1207 */         int amStart = l.indexOf("[=") + 2;
/* 1208 */         int amEnd = l.indexOf("=]");
/* 1209 */         int centerTo = l.indexOf('*');
/* 1210 */         int centerFrom = l.indexOf('*') + 1;
/*      */         
/* 1212 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 1213 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 1214 */         String name = l.substring(centerFrom, end);
/* 1215 */         String lore = l.substring(lrStart, lrEnd);
/*      */         
/* 1217 */         ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 1218 */         if (!name.equals("default")) {
/* 1219 */           ItemMeta itemMeta = item.getItemMeta();
/* 1220 */           itemMeta.setDisplayName(name);
/* 1221 */           item.setItemMeta(itemMeta);
/*      */         } 
/* 1223 */         if (!lore.equals("none")) {
/* 1224 */           ItemMeta itemMeta = item.getItemMeta();
/* 1225 */           List<String> loreList = Arrays.asList(lore.split("\n"));
/* 1226 */           itemMeta.setLore(loreList);
/* 1227 */           item.setItemMeta(itemMeta);
/*      */         } 
/* 1229 */         itemStackList.add(item);
/* 1230 */       } else if (l.startsWith("apple")) {
/* 1231 */         Set<Material> transparentBlocks; Block block; int appleFrom = l.indexOf("[") + 1;
/* 1232 */         int appleTo = l.indexOf("]~");
/* 1233 */         String apple = l.substring(appleFrom, appleTo);
/* 1234 */         Player player = (Player)entity1asd;
/*      */         
/* 1236 */         switch (apple) {
/*      */           case "look_block":
/* 1238 */             transparentBlocks = new HashSet<>();
/* 1239 */             transparentBlocks.add(Material.AIR);
/* 1240 */             block = player.getTargetBlock(transparentBlocks, 12);
/* 1241 */             itemStackList.add(block.getState().getData().toItemStack(1));
/*      */             break;
/*      */           case "main_hand_item":
/* 1244 */             itemStackList.add(player.getInventory().getItemInMainHand());
/*      */             break;
/*      */           case "off_hand_item":
/* 1247 */             itemStackList.add(player.getInventory().getItemInOffHand());
/*      */             break;
/*      */           case "click_item":
/* 1250 */             if (e instanceof InventoryClickEvent) {
/* 1251 */               itemStackList.add(((InventoryClickEvent)e).getCurrentItem());
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 1255 */             if (e instanceof BlockEvent) {
/* 1256 */               Block block1 = ((BlockEvent)e).getBlock();
/* 1257 */               itemStackList.add(block1.getState().getData().toItemStack(1));
/*      */             } 
/*      */             break;
/*      */         } 
/*      */       } 
/*      */     } 
/* 1263 */     if (!itemStackList.isEmpty()) {
/* 1264 */       Random random1 = new Random();
/* 1265 */       int randomInt = random1.nextInt(itemStackList.size());
/* 1266 */       ((Player)entity1asd).getInventory().addItem(new ItemStack[] { itemStackList.get(randomInt) });
/*      */     } 
/*      */   }
/*      */   
/*      */   public void deleteItems(String func, Entity entity1asd, Event e) {
/* 1271 */     String items = func.substring(func.indexOf("(") + 1);
/* 1272 */     String[] itemList = items.split("\\|");
/* 1273 */     List<ItemStack> itemStackList = new ArrayList<>();
/* 1274 */     for (String l : itemList) {
/* 1275 */       if (l.startsWith("item")) {
/* 1276 */         int start = l.indexOf("[-") + 2;
/* 1277 */         int end = l.indexOf("-]");
/* 1278 */         int lrStart = l.indexOf("[+") + 2;
/* 1279 */         int lrEnd = l.indexOf("+]");
/* 1280 */         int amStart = l.indexOf("[=") + 2;
/* 1281 */         int amEnd = l.indexOf("=]");
/* 1282 */         int centerTo = l.indexOf('*');
/* 1283 */         int centerFrom = l.indexOf('*') + 1;
/*      */         
/* 1285 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 1286 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 1287 */         String name = l.substring(centerFrom, end);
/* 1288 */         String lore = l.substring(lrStart, lrEnd);
/*      */         
/* 1290 */         ItemStack item = new ItemStack(Material.getMaterial(itemID));
/* 1291 */         if (!name.equals("default")) {
/* 1292 */           ItemMeta itemMeta = item.getItemMeta();
/* 1293 */           itemMeta.setDisplayName(name);
/* 1294 */           item.setItemMeta(itemMeta);
/*      */         } 
/* 1296 */         if (!lore.equals("none")) {
/* 1297 */           ItemMeta itemMeta = item.getItemMeta();
/* 1298 */           List<String> loreList = Arrays.asList(lore.split("\n"));
/* 1299 */           itemMeta.setLore(loreList);
/* 1300 */           item.setItemMeta(itemMeta);
/*      */         } 
/* 1302 */         for (int ii = 0; ii < amount; ii++) {
/* 1303 */           itemStackList.add(item);
/*      */         }
/* 1305 */       } else if (l.startsWith("apple")) {
/* 1306 */         Set<Material> transparentBlocks; Block block; int appleFrom = l.indexOf("[") + 1;
/* 1307 */         int appleTo = l.indexOf("]~");
/* 1308 */         String apple = l.substring(appleFrom, appleTo);
/* 1309 */         Player player = (Player)entity1asd;
/*      */         
/* 1311 */         switch (apple) {
/*      */           case "look_block":
/* 1313 */             transparentBlocks = new HashSet<>();
/* 1314 */             transparentBlocks.add(Material.AIR);
/* 1315 */             block = player.getTargetBlock(transparentBlocks, 12);
/* 1316 */             itemStackList.add(block.getState().getData().toItemStack(1));
/*      */             break;
/*      */           case "main_hand_item":
/* 1319 */             itemStackList.add(player.getInventory().getItemInMainHand());
/*      */             break;
/*      */           case "off_hand_item":
/* 1322 */             itemStackList.add(player.getInventory().getItemInOffHand());
/*      */             break;
/*      */           case "click_item":
/* 1325 */             if (e instanceof InventoryClickEvent) {
/* 1326 */               itemStackList.add(((InventoryClickEvent)e).getCurrentItem());
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 1330 */             if (e instanceof BlockEvent) {
/* 1331 */               Block block1 = ((BlockEvent)e).getBlock();
/* 1332 */               itemStackList.add(block1.getState().getData().toItemStack(1));
/*      */             } 
/*      */             break;
/*      */         } 
/*      */       } 
/*      */     } 
/* 1338 */     if (!itemStackList.isEmpty()) {
/* 1339 */       Player player = (Player)entity1asd;
/* 1340 */       for (ItemStack item : itemStackList) {
/* 1341 */         if (item.hasItemMeta()) {
/*      */           try {
/* 1343 */             player.getInventory().removeItem(new ItemStack[] { item });
/* 1344 */           } catch (Exception exception) {}
/*      */           
/*      */           continue;
/*      */         } 
/* 1348 */         Material material = item.getType();
/* 1349 */         for (ItemStack playerItem : player.getInventory().getContents()) {
/* 1350 */           if (playerItem != null && 
/* 1351 */             playerItem.getType() == material) {
/*      */             try {
/* 1353 */               player.getInventory().removeItem(new ItemStack[] { new ItemStack(material) });
/* 1354 */             } catch (Exception exception) {}
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String getSomeString(String need, String func) {
/*      */     String eq;
/*      */     String type;
/*      */     String message;
/* 1369 */     switch (need) {
/*      */       
/*      */       case "eq":
/* 1372 */         if (func.contains("(") && func.contains(")")) {
/* 1373 */           int startIndexFunc = func.indexOf('(') + 1;
/* 1374 */           int endIndexFunc = func.indexOf(')');
/* 1375 */           eq = func.substring(startIndexFunc, endIndexFunc);
/*      */         } else {
/* 1377 */           eq = null;
/*      */         } 
/* 1379 */         return eq;
/*      */       
/*      */       case "type":
/* 1382 */         if (func.contains("[") && func.contains("]")) {
/* 1383 */           int firstbracket = func.indexOf('[') + 1;
/* 1384 */           int lastbracket = func.indexOf(']');
/* 1385 */           type = func.substring(firstbracket, lastbracket);
/*      */         } else {
/* 1387 */           type = null;
/*      */         } 
/* 1389 */         return type;
/*      */       
/*      */       case "message":
/* 1392 */         if (func.contains("~(") && func.contains(")~")) {
/* 1393 */           int startIndexFuncMSG = func.indexOf("~(") + 2;
/* 1394 */           int endIndexFuncMSG = func.indexOf(")~");
/* 1395 */           message = func.substring(startIndexFuncMSG, endIndexFuncMSG);
/*      */         } else {
/* 1397 */           message = null;
/*      */         } 
/* 1399 */         return message;
/*      */     } 
/* 1401 */     return null;
/*      */   }
/*      */   
/*      */   public void handleLine(List<String> line, Entity eventEntity, World world, int startIndex, Event e) {
/* 1405 */     boolean shouldExecuteElse = false;
/* 1406 */     for (int i = startIndex; i < line.size(); i++) {
/* 1407 */       String func = line.get(i);
/*      */       
/* 1409 */       String eq = getSomeString("eq", func);
/* 1410 */       String type = getSomeString("type", func);
/* 1411 */       String message = getSomeString("message", func);
/*      */       
/* 1413 */       Random randomFunc = new Random();
/* 1414 */       int random = randomFunc.nextInt(world.getPlayers().size());
/* 1415 */       Entity entity1asd = getEntity(func, eventEntity, e, Integer.valueOf(random));
/*      */       
/* 1417 */       if (func.startsWith("closeInventory")) {
/* 1418 */         if (entity1asd instanceof Player) {
/* 1419 */           ((Player)entity1asd).closeInventory();
/*      */         }
/* 1421 */       } else if (func.startsWith("openInventory")) {
/* 1422 */         if (entity1asd instanceof Player) {
/* 1423 */           assert eq != null;
/* 1424 */           assert type != null;
/* 1425 */           openInventory(eq, type, entity1asd, world, e);
/*      */         } 
/* 1427 */       } else if (func.startsWith("worldMessage")) {
/* 1428 */         assert message != null;
/* 1429 */         worldMessage(message, entity1asd, world, e, Integer.valueOf(random));
/* 1430 */       } else if (func.startsWith("playerMessage")) {
/* 1431 */         if (entity1asd instanceof Player) {
/* 1432 */           assert message != null;
/* 1433 */           playerMessage(message, entity1asd, world, e, Integer.valueOf(random));
/*      */         } 
/* 1435 */       } else if (func.startsWith("gameMode")) {
/* 1436 */         if (entity1asd instanceof Player) {
/* 1437 */           assert eq != null;
/* 1438 */           gameMode(eq, entity1asd);
/*      */         } 
/* 1440 */       } else if (func.startsWith("setHealth")) {
/* 1441 */         assert eq != null;
/* 1442 */         if (entity1asd instanceof LivingEntity)
/* 1443 */           setHealth(eq, entity1asd, e); 
/*      */       } else {
/* 1445 */         if (func.startsWith("timeWait")) {
/* 1446 */           assert eq != null;
/* 1447 */           timeWait(eq, Integer.valueOf(i), line, eventEntity, world, e, entity1asd); return;
/*      */         } 
/* 1449 */         if (func.startsWith("setBlock")) {
/* 1450 */           assert eq != null;
/* 1451 */           assert type != null;
/* 1452 */           setBlock(eq, world, type, entity1asd, e);
/* 1453 */         } else if (func.equals("cancelEvent") && e instanceof Cancellable) {
/* 1454 */           ((Cancellable)e).setCancelled(true);
/* 1455 */         } else if (func.startsWith("teleport")) {
/* 1456 */           assert eq != null;
/* 1457 */           teleport(eq, entity1asd, e);
/* 1458 */         } else if (func.startsWith("useFunc")) {
/* 1459 */           assert type != null;
/* 1460 */           assert eq != null;
/* 1461 */           useFunc(type, eq, entity1asd);
/* 1462 */         } else if (func.startsWith("giveItems")) {
/* 1463 */           if (entity1asd instanceof Player) {
/* 1464 */             giveItems(func, entity1asd, e);
/*      */           }
/* 1466 */         } else if (func.startsWith("clearInventory")) {
/* 1467 */           if (entity1asd instanceof Player) {
/* 1468 */             ((Player)entity1asd).getInventory().clear();
/*      */           }
/* 1470 */         } else if (func.startsWith("giveRandItem")) {
/* 1471 */           if (entity1asd instanceof Player) {
/* 1472 */             giveRandItem(func, entity1asd, e);
/*      */           }
/* 1474 */         } else if (func.startsWith("deleteItems")) {
/* 1475 */           if (entity1asd instanceof Player) {
/* 1476 */             deleteItems(func, entity1asd, e);
/*      */ 
/*      */ 
/*      */           
/*      */           }
/*      */ 
/*      */         
/*      */         }
/* 1484 */         else if (func.startsWith("if")) {
/* 1485 */           if (evaluateCondition(eq, entity1asd, e)) {
/* 1486 */             String innerCode = getInnerCode(line.subList(i + 1, line.size()));
/* 1487 */             if (innerCode != null) {
/* 1488 */               List<String> innerCodeList = Arrays.asList(innerCode.split("&"));
/* 1489 */               handleLine(innerCodeList, entity1asd, world, 0, e);
/* 1490 */               i = findEndOfInnerCode(line, i);
/*      */             } 
/*      */           } else {
/* 1493 */             i = findEndOfInnerCode(line, i);
/*      */           } 
/* 1495 */         } else if (func.equals("else")) {
/* 1496 */           if (shouldExecuteElse) {
/* 1497 */             String innerCode = getInnerCode(line.subList(i + 1, line.size()));
/* 1498 */             if (innerCode != null) {
/* 1499 */               List<String> innerCodeList = Arrays.asList(innerCode.split("&"));
/* 1500 */               handleLine(innerCodeList, entity1asd, world, 0, e);
/* 1501 */               i = findEndOfInnerCode(line, i);
/*      */             } 
/*      */           } else {
/* 1504 */             i = findEndOfInnerCode(line, i);
/*      */           } 
/*      */         } 
/* 1507 */       }  if (func.startsWith("if")) {
/* 1508 */         shouldExecuteElse = !evaluateCondition(eq, entity1asd, e);
/*      */       }
/*      */     } 
/*      */   }
/*      */   
/*      */   private Entity getEntity(String func, Entity entity, Event e, Integer random) {
/* 1514 */     if (func.contains("%_") && func.contains("_%")) {
/* 1515 */       String sendTo = func.substring(func.indexOf("%_") + 2, func.indexOf("_%"));
/* 1516 */       switch (sendTo) {
/*      */         case "player":
/* 1518 */           if (entity instanceof Player) {
/* 1519 */             return (Entity)((Player)entity).getPlayer();
/*      */           }
/*      */           break;
/*      */         case "damager":
/* 1523 */           if (e instanceof EntityDeathEvent) {
/* 1524 */             if (((EntityDeathEvent)e).getEntity().getKiller() != null)
/* 1525 */               return (Entity)((EntityDeathEvent)e).getEntity().getKiller();  break;
/*      */           } 
/* 1527 */           if (e instanceof EntityDamageByEntityEvent && (
/* 1528 */             (EntityDamageByEntityEvent)e).getDamager() instanceof LivingEntity) {
/* 1529 */             return ((EntityDamageByEntityEvent)e).getDamager();
/*      */           }
/*      */           break;
/*      */         
/*      */         case "victim":
/* 1534 */           if (e instanceof EntityDeathEvent) {
/* 1535 */             if (((EntityDeathEvent)e).getEntity() != null)
/* 1536 */               return (Entity)((EntityDeathEvent)e).getEntity();  break;
/*      */           } 
/* 1538 */           if (e instanceof EntityDamageByEntityEvent && (
/* 1539 */             (EntityDamageByEntityEvent)e).getEntity() instanceof LivingEntity) {
/* 1540 */             return ((EntityDamageByEntityEvent)e).getEntity();
/*      */           }
/*      */           break;
/*      */         
/*      */         case "random":
/* 1545 */           return entity.getWorld().getPlayers().get(random.intValue());
/*      */       } 
/*      */     } 
/* 1548 */     return entity;
/*      */   }
/*      */   
/*      */   private int findEndOfInnerCode(List<String> line, int startIndex) {
/* 1552 */     int count = 0;
/* 1553 */     for (int i = startIndex + 1; i < line.size(); i++) {
/* 1554 */       String s = line.get(i);
/* 1555 */       if (s.contains("{")) {
/* 1556 */         count++;
/*      */       }
/*      */       
/* 1559 */       count--;
/* 1560 */       if (s.contains("}") && count == 0) {
/* 1561 */         return i;
/*      */       }
/*      */     } 
/*      */     
/* 1565 */     return line.size() - 1;
/*      */   }
/*      */   private String getInnerCode(List<String> line) {
/* 1568 */     int count = 0;
/* 1569 */     int startIndex = -1;
/* 1570 */     for (int i = 0; i < line.size(); i++) {
/* 1571 */       String s = line.get(i);
/* 1572 */       if (s.contains("{")) {
/* 1573 */         if (startIndex == -1) {
/* 1574 */           startIndex = i;
/*      */         }
/* 1576 */         count++;
/*      */       } 
/*      */       
/* 1579 */       count--;
/* 1580 */       if (s.contains("}") && count == 0) {
/* 1581 */         return String.join("&", line.subList(startIndex + 1, i));
/*      */       }
/*      */     } 
/*      */     
/* 1585 */     return null;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean message(String condition, AsyncPlayerChatEvent event, Player player) {
/* 1591 */     String world_id = player.getWorld().getName().replace("-code", "").replace("-world", "");
/*      */     
/* 1593 */     double health_now = player.getHealth();
/* 1594 */     double health_max = player.getHealthScale();
/* 1595 */     int hunger = player.getFoodLevel();
/* 1596 */     float satiety = player.getSaturation();
/* 1597 */     int xp = player.getTotalExperience();
/* 1598 */     double armor = player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
/* 1599 */     int air = Math.round(player.getRemainingAir() / 10.0F);
/* 1600 */     int slot_now = player.getInventory().getHeldItemSlot();
/* 1601 */     int ping = 0;
/*      */     try {
/* 1603 */       Object entityPlayer = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
/* 1604 */       ping = ((Integer)entityPlayer.getClass().getField("ping").get(entityPlayer)).intValue();
/* 1605 */     } catch (Exception exception) {}
/*      */ 
/*      */     
/* 1608 */     Location locationLocation = player.getLocation();
/* 1609 */     String location = String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getPitch()) }).replace(",", ".");
/* 1610 */     String inventory_name = player.getOpenInventory().getTitle();
/* 1611 */     Set<Material> transparentBlock = new HashSet<>();
/* 1612 */     transparentBlock.add(Material.AIR);
/* 1613 */     Location look_block_locLocation = player.getTargetBlock(transparentBlock, 12).getLocation();
/* 1614 */     String look_block_loc = String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getPitch()) }).replace(",", ".");
/* 1615 */     String msg = event.getMessage();
/* 1616 */     int player_count = player.getWorld().getPlayers().size();
/* 1617 */     int like_count = WorldData.get().getInt("worlds." + world_id + ".liked");
/* 1618 */     int unique_count = WorldData.get().getInt("worlds." + world_id + ".uniquePlayers");
/*      */     
/* 1620 */     int from = condition.indexOf("=") + 1;
/* 1621 */     String eq = condition.substring(from);
/* 1622 */     List<String> eqList = new ArrayList<>();
/* 1623 */     for (String s : eq.split("\\|")) {
/* 1624 */       eqList.add(s.replace("apple[health_now]~", String.valueOf(health_now))
/* 1625 */           .replace("apple[health_max]~", String.valueOf(health_max))
/* 1626 */           .replace("apple[hunger]~", String.valueOf(hunger))
/* 1627 */           .replace("apple[satiety]~", String.valueOf(satiety))
/* 1628 */           .replace("apple[xp]~", String.valueOf(xp))
/* 1629 */           .replace("apple[armor]~", String.valueOf(armor))
/* 1630 */           .replace("apple[air]~", String.valueOf(air))
/* 1631 */           .replace("apple[slot_now]~", String.valueOf(slot_now))
/* 1632 */           .replace("apple[ping]~", String.valueOf(ping))
/* 1633 */           .replace("apple[location]~", location)
/* 1634 */           .replace("apple[inventory_name]~", inventory_name)
/* 1635 */           .replace("apple[look_block_loc]~", look_block_loc)
/* 1636 */           .replace("apple[message]~", msg)
/* 1637 */           .replace("apple[player_count]~", String.valueOf(player_count))
/* 1638 */           .replace("apple[like_count]~", String.valueOf(like_count))
/* 1639 */           .replace("apple[unique_count]~", String.valueOf(unique_count))
/* 1640 */           .replace("apple[world_id]~", world_id));
/*      */     }
/* 1642 */     return eqList.stream().anyMatch(str -> str.equalsIgnoreCase(event.getMessage().replaceAll("§[0-9a-fbolmk]", "")));
/*      */   }
/*      */   public boolean message_no(String condition, AsyncPlayerChatEvent event, Player player) {
/* 1645 */     String world_id = player.getWorld().getName().replace("-code", "").replace("-world", "");
/*      */     
/* 1647 */     double health_now = player.getHealth();
/* 1648 */     double health_max = player.getHealthScale();
/* 1649 */     int hunger = player.getFoodLevel();
/* 1650 */     float satiety = player.getSaturation();
/* 1651 */     int xp = player.getTotalExperience();
/* 1652 */     double armor = player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
/* 1653 */     int air = Math.round(player.getRemainingAir() / 10.0F);
/* 1654 */     int slot_now = player.getInventory().getHeldItemSlot();
/* 1655 */     int ping = 0;
/*      */     try {
/* 1657 */       Object entityPlayer = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
/* 1658 */       ping = ((Integer)entityPlayer.getClass().getField("ping").get(entityPlayer)).intValue();
/* 1659 */     } catch (Exception exception) {}
/*      */ 
/*      */     
/* 1662 */     Location locationLocation = player.getLocation();
/* 1663 */     String location = String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getPitch()) }).replace(",", ".");
/* 1664 */     String inventory_name = player.getOpenInventory().getTitle();
/* 1665 */     Set<Material> transparentBlock = new HashSet<>();
/* 1666 */     transparentBlock.add(Material.AIR);
/* 1667 */     Location look_block_locLocation = player.getTargetBlock(transparentBlock, 12).getLocation();
/* 1668 */     String look_block_loc = String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getPitch()) }).replace(",", ".");
/* 1669 */     String msg = event.getMessage();
/* 1670 */     int player_count = player.getWorld().getPlayers().size();
/* 1671 */     int like_count = WorldData.get().getInt("worlds." + world_id + ".liked");
/* 1672 */     int unique_count = WorldData.get().getInt("worlds." + world_id + ".uniquePlayers");
/*      */     
/* 1674 */     int from = condition.indexOf("=") + 1;
/* 1675 */     String eq = condition.substring(from);
/* 1676 */     List<String> eqList = new ArrayList<>();
/* 1677 */     for (String s : eq.split("\\|")) {
/* 1678 */       eqList.add(s.replace("apple[health_now]~", String.valueOf(health_now))
/* 1679 */           .replace("apple[health_max]~", String.valueOf(health_max))
/* 1680 */           .replace("apple[hunger]~", String.valueOf(hunger))
/* 1681 */           .replace("apple[satiety]~", String.valueOf(satiety))
/* 1682 */           .replace("apple[xp]~", String.valueOf(xp))
/* 1683 */           .replace("apple[armor]~", String.valueOf(armor))
/* 1684 */           .replace("apple[air]~", String.valueOf(air))
/* 1685 */           .replace("apple[slot_now]~", String.valueOf(slot_now))
/* 1686 */           .replace("apple[ping]~", String.valueOf(ping))
/* 1687 */           .replace("apple[location]~", location)
/* 1688 */           .replace("apple[inventory_name]~", inventory_name)
/* 1689 */           .replace("apple[look_block_loc]~", look_block_loc)
/* 1690 */           .replace("apple[message]~", msg)
/* 1691 */           .replace("apple[player_count]~", String.valueOf(player_count))
/* 1692 */           .replace("apple[like_count]~", String.valueOf(like_count))
/* 1693 */           .replace("apple[unique_count]~", String.valueOf(unique_count))
/* 1694 */           .replace("apple[world_id]~", world_id));
/*      */     }
/* 1696 */     return eqList.stream().noneMatch(str -> str.equalsIgnoreCase(event.getMessage().replaceAll("§[0-9a-fbolmk]", "")));
/*      */   }
/*      */   
/*      */   public boolean playerName(String condition, Player player) {
/* 1700 */     int from = condition.indexOf("=") + 1;
/* 1701 */     String eq = condition.substring(from);
/* 1702 */     List<String> eqList = Arrays.asList(eq.split("\\|"));
/* 1703 */     return eqList.stream().anyMatch(str -> str.equals(player.getName()));
/*      */   }
/*      */   public boolean playerName_no(String condition, Player player) {
/* 1706 */     int from = condition.indexOf("=") + 1;
/* 1707 */     String eq = condition.substring(from);
/* 1708 */     List<String> eqList = Arrays.asList(eq.split("\\|"));
/* 1709 */     return eqList.stream().noneMatch(str -> str.equals(player.getName()));
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean playerHaveItems(String condition, Player player, Event event) {
/* 1714 */     Set<Material> transparentBlock = new HashSet<>();
/* 1715 */     transparentBlock.add(Material.AIR);
/* 1716 */     int from = condition.indexOf("=") + 1;
/* 1717 */     String eq = condition.substring(from);
/* 1718 */     String[] eqList = eq.split("\\|");
/*      */     
/* 1720 */     List<ItemStack> itemStackList = new ArrayList<>();
/* 1721 */     for (String l : eqList) {
/* 1722 */       if (l.startsWith("item")) {
/* 1723 */         int start = l.indexOf("[-") + 2;
/* 1724 */         int end = l.indexOf("-]");
/* 1725 */         int centerTo = l.indexOf('*');
/* 1726 */         int centerFrom = l.indexOf('*') + 1;
/* 1727 */         int amStart = l.indexOf("[+") + 2;
/* 1728 */         int amEnd = l.indexOf("+]");
/*      */         
/* 1730 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 1731 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 1732 */         String name = l.substring(centerFrom, end);
/* 1733 */         ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 1734 */         if (!name.equals("default")) {
/* 1735 */           ItemMeta itemMeta = item.getItemMeta();
/* 1736 */           itemMeta.setDisplayName(name);
/* 1737 */           item.setItemMeta(itemMeta);
/*      */         } 
/* 1739 */         itemStackList.add(item);
/* 1740 */       } else if (l.startsWith("apple")) {
/* 1741 */         Block block; int appleFrom = l.indexOf("[") + 1;
/* 1742 */         int appleTo = l.indexOf("]~");
/* 1743 */         String apple = l.substring(appleFrom, appleTo);
/*      */         
/* 1745 */         switch (apple) {
/*      */           case "look_block":
/* 1747 */             block = player.getTargetBlock(transparentBlock, 12);
/* 1748 */             itemStackList.add(block.getState().getData().toItemStack(1));
/*      */             break;
/*      */           case "main_hand_item":
/* 1751 */             itemStackList.add(player.getInventory().getItemInMainHand());
/*      */             break;
/*      */           case "off_hand_item":
/* 1754 */             itemStackList.add(player.getInventory().getItemInOffHand());
/*      */             break;
/*      */           case "click_item":
/* 1757 */             if (event instanceof InventoryClickEvent) {
/* 1758 */               itemStackList.add(((InventoryClickEvent)event).getCurrentItem());
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 1762 */             if (event instanceof BlockEvent) {
/* 1763 */               Block block1 = ((BlockEvent)event).getBlock();
/* 1764 */               itemStackList.add(block1.getState().getData().toItemStack(1));
/*      */             } 
/*      */             break;
/*      */         } 
/*      */       } 
/*      */     } 
/* 1770 */     for (ItemStack item : itemStackList) {
/* 1771 */       int amount = item.getAmount();
/* 1772 */       if (item.hasItemMeta()) {
/* 1773 */         if (amount == 1) {
/* 1774 */           if (!player.getInventory().containsAtLeast(item, 1))
/* 1775 */             return false;  continue;
/*      */         } 
/* 1777 */         if (!player.getInventory().contains(item))
/* 1778 */           return false; 
/*      */         continue;
/*      */       } 
/* 1781 */       boolean found = false;
/* 1782 */       Material material = item.getType();
/*      */       
/* 1784 */       if (amount == 1) {
/* 1785 */         for (ItemStack playerItem : player.getInventory().getContents()) {
/* 1786 */           if (playerItem != null && playerItem.getType() == material) {
/* 1787 */             found = true;
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } else {
/* 1792 */         for (ItemStack playerItem : player.getInventory().getContents()) {
/* 1793 */           if (playerItem != null && playerItem.getType() == material && playerItem.getAmount() >= amount) {
/* 1794 */             found = true;
/*      */             
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } 
/* 1800 */       if (!found) {
/* 1801 */         return false;
/*      */       }
/*      */     } 
/*      */     
/* 1805 */     return true;
/*      */   }
/*      */   
/*      */   public boolean playerHaveItems_no(String condition, Player player, Event event) {
/* 1809 */     Set<Material> transparentBlock = new HashSet<>();
/* 1810 */     transparentBlock.add(Material.AIR);
/* 1811 */     int from = condition.indexOf("=") + 1;
/* 1812 */     String eq = condition.substring(from);
/* 1813 */     String[] eqList = eq.split("\\|");
/*      */     
/* 1815 */     List<ItemStack> itemStackList = new ArrayList<>();
/* 1816 */     for (String l : eqList) {
/* 1817 */       if (l.startsWith("item")) {
/* 1818 */         int start = l.indexOf("[-") + 2;
/* 1819 */         int end = l.indexOf("-]");
/* 1820 */         int centerTo = l.indexOf('*');
/* 1821 */         int centerFrom = l.indexOf('*') + 1;
/* 1822 */         int amStart = l.indexOf("[+") + 2;
/* 1823 */         int amEnd = l.indexOf("+]");
/*      */         
/* 1825 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 1826 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 1827 */         String name = l.substring(centerFrom, end);
/* 1828 */         ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 1829 */         if (!name.equals("default")) {
/* 1830 */           ItemMeta itemMeta = item.getItemMeta();
/* 1831 */           itemMeta.setDisplayName(name);
/* 1832 */           item.setItemMeta(itemMeta);
/*      */         } 
/* 1834 */         itemStackList.add(item);
/* 1835 */       } else if (l.startsWith("apple")) {
/* 1836 */         Block block; int appleFrom = l.indexOf("[") + 1;
/* 1837 */         int appleTo = l.indexOf("]~");
/* 1838 */         String apple = l.substring(appleFrom, appleTo);
/*      */         
/* 1840 */         switch (apple) {
/*      */           case "look_block":
/* 1842 */             block = player.getTargetBlock(transparentBlock, 12);
/* 1843 */             itemStackList.add(block.getState().getData().toItemStack(1));
/*      */             break;
/*      */           case "main_hand_item":
/* 1846 */             itemStackList.add(player.getInventory().getItemInMainHand());
/*      */             break;
/*      */           case "off_hand_item":
/* 1849 */             itemStackList.add(player.getInventory().getItemInOffHand());
/*      */             break;
/*      */           case "click_item":
/* 1852 */             if (event instanceof InventoryClickEvent) {
/* 1853 */               itemStackList.add(((InventoryClickEvent)event).getCurrentItem());
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 1857 */             if (event instanceof BlockEvent) {
/* 1858 */               Block block1 = ((BlockEvent)event).getBlock();
/* 1859 */               itemStackList.add(block1.getState().getData().toItemStack(1));
/*      */             } 
/*      */             break;
/*      */         } 
/*      */       } 
/*      */     } 
/* 1865 */     for (ItemStack item : itemStackList) {
/* 1866 */       int amount = item.getAmount();
/* 1867 */       if (item.hasItemMeta()) {
/* 1868 */         if (amount == 1) {
/* 1869 */           if (player.getInventory().containsAtLeast(item, 1))
/* 1870 */             return false;  continue;
/*      */         } 
/* 1872 */         if (player.getInventory().contains(item))
/* 1873 */           return false; 
/*      */         continue;
/*      */       } 
/* 1876 */       boolean found = false;
/* 1877 */       Material material = item.getType();
/*      */       
/* 1879 */       if (amount == 1) {
/* 1880 */         for (ItemStack playerItem : player.getInventory().getContents()) {
/* 1881 */           if (playerItem != null && playerItem.getType() == material) {
/* 1882 */             found = true;
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } else {
/* 1887 */         for (ItemStack playerItem : player.getInventory().getContents()) {
/* 1888 */           if (playerItem != null && playerItem.getType() == material && playerItem.getAmount() >= amount) {
/* 1889 */             found = true;
/*      */             
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } 
/* 1895 */       if (found) {
/* 1896 */         return false;
/*      */       }
/*      */     } 
/*      */     
/* 1900 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean inventoryHaveItems(String condition, Player player, Event event) {
/* 1905 */     Set<Material> transparentBlock = new HashSet<>();
/* 1906 */     transparentBlock.add(Material.AIR);
/* 1907 */     int from = condition.indexOf("=") + 1;
/* 1908 */     String eq = condition.substring(from);
/* 1909 */     String[] eqList = eq.split("\\|");
/*      */     
/* 1911 */     List<ItemStack> itemStackList = new ArrayList<>();
/* 1912 */     for (String l : eqList) {
/* 1913 */       if (l.startsWith("item")) {
/* 1914 */         int start = l.indexOf("[-") + 2;
/* 1915 */         int end = l.indexOf("-]");
/* 1916 */         int centerTo = l.indexOf('*');
/* 1917 */         int centerFrom = l.indexOf('*') + 1;
/* 1918 */         int amStart = l.indexOf("[+") + 2;
/* 1919 */         int amEnd = l.indexOf("+]");
/*      */         
/* 1921 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 1922 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 1923 */         String name = l.substring(centerFrom, end);
/* 1924 */         ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 1925 */         ItemMeta itemMeta = item.getItemMeta();
/* 1926 */         if (!name.equals("default")) {
/* 1927 */           itemMeta.setDisplayName(name);
/* 1928 */           item.setItemMeta(itemMeta);
/*      */         } 
/* 1930 */         itemStackList.add(item);
/* 1931 */       } else if (l.startsWith("apple")) {
/* 1932 */         Block block; int appleFrom = l.indexOf("[") + 1;
/* 1933 */         int appleTo = l.indexOf("]~");
/* 1934 */         String apple = l.substring(appleFrom, appleTo);
/*      */         
/* 1936 */         switch (apple) {
/*      */           case "look_block":
/* 1938 */             block = player.getTargetBlock(transparentBlock, 12);
/* 1939 */             itemStackList.add(block.getState().getData().toItemStack(1));
/*      */             break;
/*      */           case "main_hand_item":
/* 1942 */             itemStackList.add(player.getInventory().getItemInMainHand());
/*      */             break;
/*      */           case "off_hand_item":
/* 1945 */             itemStackList.add(player.getInventory().getItemInOffHand());
/*      */             break;
/*      */           case "click_item":
/* 1948 */             if (event instanceof InventoryClickEvent) {
/* 1949 */               itemStackList.add(((InventoryClickEvent)event).getCurrentItem());
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 1953 */             if (event instanceof BlockEvent) {
/* 1954 */               Block block1 = ((BlockEvent)event).getBlock();
/* 1955 */               itemStackList.add(block1.getState().getData().toItemStack(1));
/*      */             } 
/*      */             break;
/*      */         } 
/*      */       } 
/*      */     } 
/* 1961 */     for (ItemStack item : itemStackList) {
/* 1962 */       int amount = item.getAmount();
/* 1963 */       if (item.hasItemMeta()) {
/* 1964 */         if (amount == 1) {
/* 1965 */           if (!player.getOpenInventory().getTopInventory().containsAtLeast(item, 1))
/* 1966 */             return false;  continue;
/*      */         } 
/* 1968 */         if (!player.getOpenInventory().getTopInventory().contains(item))
/* 1969 */           return false; 
/*      */         continue;
/*      */       } 
/* 1972 */       boolean found = false;
/* 1973 */       Material material = item.getType();
/*      */       
/* 1975 */       if (amount == 1) {
/* 1976 */         for (ItemStack playerItem : player.getOpenInventory().getTopInventory().getContents()) {
/* 1977 */           if (playerItem != null && playerItem.getType() == material) {
/* 1978 */             found = true;
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } else {
/* 1983 */         for (ItemStack playerItem : player.getOpenInventory().getTopInventory().getContents()) {
/* 1984 */           if (playerItem != null && playerItem.getType() == material && playerItem.getAmount() >= amount) {
/* 1985 */             found = true;
/*      */             
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } 
/* 1991 */       if (!found) {
/* 1992 */         return false;
/*      */       }
/*      */     } 
/*      */     
/* 1996 */     return true;
/*      */   }
/*      */   
/*      */   public boolean inventoryHaveItems_no(String condition, Player player, Event event) {
/* 2000 */     Set<Material> transparentBlock = new HashSet<>();
/* 2001 */     transparentBlock.add(Material.AIR);
/* 2002 */     int from = condition.indexOf("=") + 1;
/* 2003 */     String eq = condition.substring(from);
/* 2004 */     String[] eqList = eq.split("\\|");
/*      */     
/* 2006 */     List<ItemStack> itemStackList = new ArrayList<>();
/* 2007 */     for (String l : eqList) {
/* 2008 */       if (l.startsWith("item")) {
/* 2009 */         int start = l.indexOf("[-") + 2;
/* 2010 */         int end = l.indexOf("-]");
/* 2011 */         int centerTo = l.indexOf('*');
/* 2012 */         int centerFrom = l.indexOf('*') + 1;
/* 2013 */         int amStart = l.indexOf("[+") + 2;
/* 2014 */         int amEnd = l.indexOf("+]");
/*      */         
/* 2016 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 2017 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 2018 */         String name = l.substring(centerFrom, end);
/* 2019 */         ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 2020 */         ItemMeta itemMeta = item.getItemMeta();
/* 2021 */         if (!name.equals("default")) {
/* 2022 */           itemMeta.setDisplayName(name);
/* 2023 */           item.setItemMeta(itemMeta);
/*      */         } 
/* 2025 */         itemStackList.add(item);
/* 2026 */       } else if (l.startsWith("apple")) {
/* 2027 */         Block block; int appleFrom = l.indexOf("[") + 1;
/* 2028 */         int appleTo = l.indexOf("]~");
/* 2029 */         String apple = l.substring(appleFrom, appleTo);
/*      */         
/* 2031 */         switch (apple) {
/*      */           case "look_block":
/* 2033 */             block = player.getTargetBlock(transparentBlock, 12);
/* 2034 */             itemStackList.add(block.getState().getData().toItemStack(1));
/*      */             break;
/*      */           case "main_hand_item":
/* 2037 */             itemStackList.add(player.getInventory().getItemInMainHand());
/*      */             break;
/*      */           case "off_hand_item":
/* 2040 */             itemStackList.add(player.getInventory().getItemInOffHand());
/*      */             break;
/*      */           case "click_item":
/* 2043 */             if (event instanceof InventoryClickEvent) {
/* 2044 */               itemStackList.add(((InventoryClickEvent)event).getCurrentItem());
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 2048 */             if (event instanceof BlockEvent) {
/* 2049 */               Block block1 = ((BlockEvent)event).getBlock();
/* 2050 */               itemStackList.add(block1.getState().getData().toItemStack(1));
/*      */             } 
/*      */             break;
/*      */         } 
/*      */       } 
/*      */     } 
/* 2056 */     for (ItemStack item : itemStackList) {
/* 2057 */       int amount = item.getAmount();
/* 2058 */       if (item.hasItemMeta()) {
/* 2059 */         if (amount == 1) {
/* 2060 */           if (player.getOpenInventory().getTopInventory().containsAtLeast(item, 1))
/* 2061 */             return false;  continue;
/*      */         } 
/* 2063 */         if (player.getOpenInventory().getTopInventory().contains(item))
/* 2064 */           return false; 
/*      */         continue;
/*      */       } 
/* 2067 */       boolean found = false;
/* 2068 */       Material material = item.getType();
/*      */       
/* 2070 */       if (amount == 1) {
/* 2071 */         for (ItemStack playerItem : player.getOpenInventory().getTopInventory().getContents()) {
/* 2072 */           if (playerItem != null && playerItem.getType() == material) {
/* 2073 */             found = true;
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } else {
/* 2078 */         for (ItemStack playerItem : player.getOpenInventory().getTopInventory().getContents()) {
/* 2079 */           if (playerItem != null && playerItem.getType() == material && playerItem.getAmount() >= amount) {
/* 2080 */             found = true;
/*      */             
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } 
/* 2086 */       if (found) {
/* 2087 */         return false;
/*      */       }
/*      */     } 
/*      */     
/* 2091 */     return true;
/*      */   }
/*      */   
/*      */   public boolean inventoryName(String condition, InventoryEvent event, Player player) {
/* 2095 */     String world_id = player.getWorld().getName().replace("-code", "").replace("-world", "");
/*      */     
/* 2097 */     double health_now = player.getHealth();
/* 2098 */     double health_max = player.getHealthScale();
/* 2099 */     int hunger = player.getFoodLevel();
/* 2100 */     float satiety = player.getSaturation();
/* 2101 */     int xp = player.getTotalExperience();
/* 2102 */     double armor = player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
/* 2103 */     int air = Math.round(player.getRemainingAir() / 10.0F);
/* 2104 */     int slot_now = player.getInventory().getHeldItemSlot();
/* 2105 */     int ping = 0;
/*      */     try {
/* 2107 */       Object entityPlayer = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
/* 2108 */       ping = ((Integer)entityPlayer.getClass().getField("ping").get(entityPlayer)).intValue();
/* 2109 */     } catch (Exception exception) {}
/*      */ 
/*      */     
/* 2112 */     int click_slot = 0;
/* 2113 */     if (event instanceof InventoryClickEvent) {
/* 2114 */       click_slot = ((InventoryClickEvent)event).getSlot();
/*      */     }
/* 2116 */     Location locationLocation = player.getLocation();
/* 2117 */     String location = String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getPitch()) }).replace(",", ".");
/* 2118 */     String inventory_name = player.getOpenInventory().getTitle();
/* 2119 */     Set<Material> transparentBlock = new HashSet<>();
/* 2120 */     transparentBlock.add(Material.AIR);
/* 2121 */     Location look_block_locLocation = player.getTargetBlock(transparentBlock, 12).getLocation();
/* 2122 */     String look_block_loc = String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getPitch()) }).replace(",", ".");
/* 2123 */     int player_count = player.getWorld().getPlayers().size();
/* 2124 */     int like_count = WorldData.get().getInt("worlds." + world_id + ".liked");
/* 2125 */     int unique_count = WorldData.get().getInt("worlds." + world_id + ".uniquePlayers");
/*      */     
/* 2127 */     int from = condition.indexOf("=") + 1;
/* 2128 */     String eq = condition.substring(from);
/* 2129 */     List<String> eqList = new ArrayList<>();
/* 2130 */     for (String s : eq.split("\\|")) {
/* 2131 */       eqList.add(s.replace("apple[health_now]~", String.valueOf(health_now))
/* 2132 */           .replace("apple[health_max]~", String.valueOf(health_max))
/* 2133 */           .replace("apple[hunger]~", String.valueOf(hunger))
/* 2134 */           .replace("apple[satiety]~", String.valueOf(satiety))
/* 2135 */           .replace("apple[xp]~", String.valueOf(xp))
/* 2136 */           .replace("apple[armor]~", String.valueOf(armor))
/* 2137 */           .replace("apple[air]~", String.valueOf(air))
/* 2138 */           .replace("apple[slot_now]~", String.valueOf(slot_now))
/* 2139 */           .replace("apple[ping]~", String.valueOf(ping))
/* 2140 */           .replace("apple[location]~", location)
/* 2141 */           .replace("apple[inventory_name]~", inventory_name)
/* 2142 */           .replace("apple[look_block_loc]~", look_block_loc)
/* 2143 */           .replace("apple[player_count]~", String.valueOf(player_count))
/* 2144 */           .replace("apple[like_count]~", String.valueOf(like_count))
/* 2145 */           .replace("apple[unique_count]~", String.valueOf(unique_count))
/* 2146 */           .replace("apple[world_id]~", world_id)
/* 2147 */           .replace("apple[click_slot]~", String.valueOf(click_slot)));
/*      */     }
/* 2149 */     Inventory inventory = event.getInventory();
/* 2150 */     return eqList.contains(inventory.getName());
/*      */   }
/*      */   public boolean inventoryName_no(String condition, InventoryEvent event, Player player) {
/* 2153 */     String world_id = player.getWorld().getName().replace("-code", "").replace("-world", "");
/*      */     
/* 2155 */     double health_now = player.getHealth();
/* 2156 */     double health_max = player.getHealthScale();
/* 2157 */     int hunger = player.getFoodLevel();
/* 2158 */     float satiety = player.getSaturation();
/* 2159 */     int xp = player.getTotalExperience();
/* 2160 */     double armor = player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
/* 2161 */     int air = Math.round(player.getRemainingAir() / 10.0F);
/* 2162 */     int slot_now = player.getInventory().getHeldItemSlot();
/* 2163 */     int ping = 0;
/*      */     try {
/* 2165 */       Object entityPlayer = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
/* 2166 */       ping = ((Integer)entityPlayer.getClass().getField("ping").get(entityPlayer)).intValue();
/* 2167 */     } catch (Exception exception) {}
/*      */ 
/*      */     
/* 2170 */     int click_slot = 0;
/* 2171 */     if (event instanceof InventoryClickEvent) {
/* 2172 */       click_slot = ((InventoryClickEvent)event).getSlot();
/*      */     }
/* 2174 */     Location locationLocation = player.getLocation();
/* 2175 */     String location = String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(locationLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(locationLocation.getPitch()) }).replace(",", ".");
/* 2176 */     String inventory_name = player.getOpenInventory().getTitle();
/* 2177 */     Set<Material> transparentBlock = new HashSet<>();
/* 2178 */     transparentBlock.add(Material.AIR);
/* 2179 */     Location look_block_locLocation = player.getTargetBlock(transparentBlock, 12).getLocation();
/* 2180 */     String look_block_loc = String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getX()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(look_block_locLocation.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(look_block_locLocation.getPitch()) }).replace(",", ".");
/* 2181 */     int player_count = player.getWorld().getPlayers().size();
/* 2182 */     int like_count = WorldData.get().getInt("worlds." + world_id + ".liked");
/* 2183 */     int unique_count = WorldData.get().getInt("worlds." + world_id + ".uniquePlayers");
/*      */     
/* 2185 */     int from = condition.indexOf("=") + 1;
/* 2186 */     String eq = condition.substring(from);
/* 2187 */     List<String> eqList = new ArrayList<>();
/* 2188 */     for (String s : eq.split("\\|")) {
/* 2189 */       eqList.add(s.replace("apple[health_now]~", String.valueOf(health_now))
/* 2190 */           .replace("apple[health_max]~", String.valueOf(health_max))
/* 2191 */           .replace("apple[hunger]~", String.valueOf(hunger))
/* 2192 */           .replace("apple[satiety]~", String.valueOf(satiety))
/* 2193 */           .replace("apple[xp]~", String.valueOf(xp))
/* 2194 */           .replace("apple[armor]~", String.valueOf(armor))
/* 2195 */           .replace("apple[air]~", String.valueOf(air))
/* 2196 */           .replace("apple[slot_now]~", String.valueOf(slot_now))
/* 2197 */           .replace("apple[ping]~", String.valueOf(ping))
/* 2198 */           .replace("apple[location]~", location)
/* 2199 */           .replace("apple[inventory_name]~", inventory_name)
/* 2200 */           .replace("apple[look_block_loc]~", look_block_loc)
/* 2201 */           .replace("apple[player_count]~", String.valueOf(player_count))
/* 2202 */           .replace("apple[like_count]~", String.valueOf(like_count))
/* 2203 */           .replace("apple[unique_count]~", String.valueOf(unique_count))
/* 2204 */           .replace("apple[world_id]~", world_id)
/* 2205 */           .replace("apple[click_slot]~", String.valueOf(click_slot)));
/*      */     }
/* 2207 */     Inventory inventory = event.getInventory();
/* 2208 */     return !eqList.contains(inventory.getName());
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean playerHoldingItemMain(String condition, Player player, Event event) {
/* 2213 */     Set<Material> transparentBlock = new HashSet<>();
/* 2214 */     transparentBlock.add(Material.AIR);
/* 2215 */     int from = condition.indexOf("=") + 1;
/* 2216 */     String eq = condition.substring(from);
/* 2217 */     String[] eqList = eq.split("\\|");
/* 2218 */     for (String l : eqList) {
/* 2219 */       if (l.startsWith("item")) {
/* 2220 */         int start = l.indexOf("[-") + 2;
/* 2221 */         int end = l.indexOf("-]");
/* 2222 */         int centerTo = l.indexOf('*');
/* 2223 */         int centerFrom = l.indexOf('*') + 1;
/* 2224 */         int amStart = l.indexOf("[+") + 2;
/* 2225 */         int amEnd = l.indexOf("+]");
/*      */         
/* 2227 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 2228 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 2229 */         String name = l.substring(centerFrom, end);
/*      */         
/* 2231 */         ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 2232 */         ItemStack checkMain = player.getInventory().getItemInMainHand().clone();
/* 2233 */         if (item.getAmount() == 1 && 
/* 2234 */           checkMain != null) {
/* 2235 */           checkMain.setAmount(1);
/*      */         }
/*      */         
/* 2238 */         if (!name.equals("default")) {
/* 2239 */           ItemMeta itemMeta = item.getItemMeta();
/* 2240 */           itemMeta.setDisplayName(name);
/* 2241 */           item.setItemMeta(itemMeta);
/* 2242 */           if (checkMain != null && checkMain.equals(item)) {
/* 2243 */             return true;
/*      */           }
/*      */         }
/* 2246 */         else if (checkMain != null && checkMain.getType() == item.getType() && checkMain.getAmount() == item.getAmount()) {
/* 2247 */           return true;
/*      */         }
/*      */       
/* 2250 */       } else if (l.startsWith("apple")) {
/* 2251 */         Block block; int appleFrom = l.indexOf("[") + 1;
/* 2252 */         int appleTo = l.indexOf("]~");
/* 2253 */         String apple = l.substring(appleFrom, appleTo);
/* 2254 */         ItemStack item = null;
/*      */         
/* 2256 */         switch (apple) {
/*      */           case "look_block":
/* 2258 */             block = player.getTargetBlock(transparentBlock, 12);
/* 2259 */             item = block.getState().getData().toItemStack(1);
/*      */             break;
/*      */           case "main_hand_item":
/* 2262 */             return true;
/*      */           case "off_hand_item":
/* 2264 */             item = player.getInventory().getItemInOffHand();
/*      */             break;
/*      */           case "click_item":
/* 2267 */             if (event instanceof InventoryClickEvent) {
/* 2268 */               item = ((InventoryClickEvent)event).getCurrentItem();
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 2272 */             if (event instanceof BlockEvent) {
/* 2273 */               Block block1 = ((BlockEvent)event).getBlock();
/* 2274 */               item = block1.getState().getData().toItemStack(1);
/*      */             } 
/*      */             break;
/*      */         } 
/* 2278 */         ItemStack checkMain = player.getInventory().getItemInMainHand().clone();
/* 2279 */         assert item != null;
/* 2280 */         if (item.getAmount() == 1 && 
/* 2281 */           checkMain != null) {
/* 2282 */           checkMain.setAmount(1);
/*      */         }
/*      */         
/* 2285 */         if (item.hasItemMeta()) {
/* 2286 */           if (checkMain != null && checkMain.equals(item)) {
/* 2287 */             return true;
/*      */           }
/*      */         }
/* 2290 */         else if (checkMain != null && checkMain.getType() == item.getType() && checkMain.getAmount() == item.getAmount()) {
/* 2291 */           return true;
/*      */         } 
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/* 2297 */     return false;
/*      */   }
/*      */   
/*      */   public boolean playerHoldingItemMain_no(String condition, Player player, Event event) {
/* 2301 */     Set<Material> transparentBlock = new HashSet<>();
/* 2302 */     transparentBlock.add(Material.AIR);
/* 2303 */     int from = condition.indexOf("=") + 1;
/* 2304 */     String eq = condition.substring(from);
/* 2305 */     String[] eqList = eq.split("\\|");
/* 2306 */     for (String l : eqList) {
/* 2307 */       if (l.startsWith("item")) {
/* 2308 */         int start = l.indexOf("[-") + 2;
/* 2309 */         int end = l.indexOf("-]");
/* 2310 */         int centerTo = l.indexOf('*');
/* 2311 */         int centerFrom = l.indexOf('*') + 1;
/* 2312 */         int amStart = l.indexOf("[+") + 2;
/* 2313 */         int amEnd = l.indexOf("+]");
/*      */         
/* 2315 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 2316 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 2317 */         String name = l.substring(centerFrom, end);
/*      */         
/* 2319 */         ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 2320 */         ItemStack checkMain = player.getInventory().getItemInMainHand().clone();
/* 2321 */         if (item.getAmount() == 1 && 
/* 2322 */           checkMain != null) {
/* 2323 */           checkMain.setAmount(1);
/*      */         }
/*      */         
/* 2326 */         if (!name.equals("default")) {
/* 2327 */           ItemMeta itemMeta = item.getItemMeta();
/* 2328 */           itemMeta.setDisplayName(name);
/* 2329 */           item.setItemMeta(itemMeta);
/* 2330 */           if (checkMain != null && !checkMain.equals(item)) {
/* 2331 */             return true;
/*      */           }
/*      */         }
/* 2334 */         else if (checkMain != null && checkMain.getType() != item.getType() && checkMain.getAmount() != item.getAmount()) {
/* 2335 */           return true;
/*      */         }
/*      */       
/* 2338 */       } else if (l.startsWith("apple")) {
/* 2339 */         Block block; int appleFrom = l.indexOf("[") + 1;
/* 2340 */         int appleTo = l.indexOf("]~");
/* 2341 */         String apple = l.substring(appleFrom, appleTo);
/* 2342 */         ItemStack item = null;
/*      */         
/* 2344 */         switch (apple) {
/*      */           case "look_block":
/* 2346 */             block = player.getTargetBlock(transparentBlock, 12);
/* 2347 */             item = block.getState().getData().toItemStack(1);
/*      */             break;
/*      */           case "main_hand_item":
/* 2350 */             return false;
/*      */           case "off_hand_item":
/* 2352 */             item = player.getInventory().getItemInOffHand();
/*      */             break;
/*      */           case "click_item":
/* 2355 */             if (event instanceof InventoryClickEvent) {
/* 2356 */               item = ((InventoryClickEvent)event).getCurrentItem();
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 2360 */             if (event instanceof BlockEvent) {
/* 2361 */               Block block1 = ((BlockEvent)event).getBlock();
/* 2362 */               item = block1.getState().getData().toItemStack(1);
/*      */             } 
/*      */             break;
/*      */         } 
/* 2366 */         ItemStack checkMain = player.getInventory().getItemInMainHand().clone();
/* 2367 */         assert item != null;
/* 2368 */         if (item.getAmount() == 1 && 
/* 2369 */           checkMain != null) {
/* 2370 */           checkMain.setAmount(1);
/*      */         }
/*      */         
/* 2373 */         if (item.hasItemMeta()) {
/* 2374 */           if (checkMain != null && !checkMain.equals(item)) {
/* 2375 */             return true;
/*      */           }
/*      */         }
/* 2378 */         else if (checkMain != null && checkMain.getType() != item.getType() && checkMain.getAmount() != item.getAmount()) {
/* 2379 */           return true;
/*      */         } 
/*      */       } 
/*      */     } 
/*      */     
/* 2384 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean playerHoldingItemOff(String condition, Player player, Event event) {
/* 2389 */     Set<Material> transparentBlock = new HashSet<>();
/* 2390 */     transparentBlock.add(Material.AIR);
/* 2391 */     int from = condition.indexOf("=") + 1;
/* 2392 */     String eq = condition.substring(from);
/* 2393 */     String[] eqList = eq.split("\\|");
/* 2394 */     for (String l : eqList) {
/* 2395 */       if (l.startsWith("item")) {
/* 2396 */         int start = l.indexOf("[-") + 2;
/* 2397 */         int end = l.indexOf("-]");
/* 2398 */         int centerTo = l.indexOf('*');
/* 2399 */         int centerFrom = l.indexOf('*') + 1;
/* 2400 */         int amStart = l.indexOf("[+") + 2;
/* 2401 */         int amEnd = l.indexOf("+]");
/*      */         
/* 2403 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 2404 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 2405 */         String name = l.substring(centerFrom, end);
/*      */         
/* 2407 */         ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 2408 */         ItemStack checkOff = player.getInventory().getItemInOffHand().clone();
/* 2409 */         if (item.getAmount() == 1 && 
/* 2410 */           checkOff != null) {
/* 2411 */           checkOff.setAmount(1);
/*      */         }
/*      */         
/* 2414 */         if (!name.equals("default")) {
/* 2415 */           ItemMeta itemMeta = item.getItemMeta();
/* 2416 */           itemMeta.setDisplayName(name);
/* 2417 */           item.setItemMeta(itemMeta);
/* 2418 */           if (checkOff != null && checkOff.equals(item)) {
/* 2419 */             return true;
/*      */           }
/*      */         }
/* 2422 */         else if (checkOff != null && checkOff.getType() == item.getType() && checkOff.getAmount() == item.getAmount()) {
/* 2423 */           return true;
/*      */         }
/*      */       
/* 2426 */       } else if (l.startsWith("apple")) {
/* 2427 */         Block block; int appleFrom = l.indexOf("[") + 1;
/* 2428 */         int appleTo = l.indexOf("]~");
/* 2429 */         String apple = l.substring(appleFrom, appleTo);
/* 2430 */         ItemStack item = null;
/*      */         
/* 2432 */         switch (apple) {
/*      */           case "look_block":
/* 2434 */             block = player.getTargetBlock(transparentBlock, 12);
/* 2435 */             item = block.getState().getData().toItemStack(1);
/*      */             break;
/*      */           case "main_hand_item":
/* 2438 */             item = player.getInventory().getItemInMainHand();
/*      */             break;
/*      */           case "off_hand_item":
/* 2441 */             return true;
/*      */           case "click_item":
/* 2443 */             if (event instanceof InventoryClickEvent) {
/* 2444 */               item = ((InventoryClickEvent)event).getCurrentItem();
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 2448 */             if (event instanceof BlockEvent) {
/* 2449 */               Block block1 = ((BlockEvent)event).getBlock();
/* 2450 */               item = block1.getState().getData().toItemStack(1);
/*      */             } 
/*      */             break;
/*      */         } 
/* 2454 */         ItemStack checkOff = player.getInventory().getItemInOffHand().clone();
/* 2455 */         assert item != null;
/* 2456 */         if (item.getAmount() == 1 && 
/* 2457 */           checkOff != null) {
/* 2458 */           checkOff.setAmount(1);
/*      */         }
/*      */         
/* 2461 */         if (item.hasItemMeta()) {
/* 2462 */           if (checkOff != null && checkOff.equals(item)) {
/* 2463 */             return true;
/*      */           }
/*      */         }
/* 2466 */         else if (checkOff != null && checkOff.getType() == item.getType() && checkOff.getAmount() == item.getAmount()) {
/* 2467 */           return true;
/*      */         } 
/*      */       } 
/*      */     } 
/*      */     
/* 2472 */     return false;
/*      */   }
/*      */   
/*      */   public boolean playerHoldingItemOff_no(String condition, Player player, Event event) {
/* 2476 */     Set<Material> transparentBlock = new HashSet<>();
/* 2477 */     transparentBlock.add(Material.AIR);
/* 2478 */     int from = condition.indexOf("=") + 1;
/* 2479 */     String eq = condition.substring(from);
/* 2480 */     String[] eqList = eq.split("\\|");
/* 2481 */     for (String l : eqList) {
/* 2482 */       if (l.startsWith("item")) {
/* 2483 */         int start = l.indexOf("[-") + 2;
/* 2484 */         int end = l.indexOf("-]");
/* 2485 */         int centerTo = l.indexOf('*');
/* 2486 */         int centerFrom = l.indexOf('*') + 1;
/* 2487 */         int amStart = l.indexOf("[+") + 2;
/* 2488 */         int amEnd = l.indexOf("+]");
/*      */         
/* 2490 */         int amount = Integer.parseInt(l.substring(amStart, amEnd));
/* 2491 */         int itemID = Integer.parseInt(l.substring(start, centerTo));
/* 2492 */         String name = l.substring(centerFrom, end);
/*      */         
/* 2494 */         ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 2495 */         ItemStack checkOff = player.getInventory().getItemInOffHand().clone();
/* 2496 */         if (item.getAmount() == 1 && 
/* 2497 */           checkOff != null) {
/* 2498 */           checkOff.setAmount(1);
/*      */         }
/*      */         
/* 2501 */         if (!name.equals("default")) {
/* 2502 */           ItemMeta itemMeta = item.getItemMeta();
/* 2503 */           itemMeta.setDisplayName(name);
/* 2504 */           item.setItemMeta(itemMeta);
/* 2505 */           if (checkOff != null && !checkOff.equals(item)) {
/* 2506 */             return true;
/*      */           }
/*      */         }
/* 2509 */         else if (checkOff != null && checkOff.getType() != item.getType() && checkOff.getAmount() != item.getAmount()) {
/* 2510 */           return true;
/*      */         }
/*      */       
/* 2513 */       } else if (l.startsWith("apple")) {
/* 2514 */         Block block; int appleFrom = l.indexOf("[") + 1;
/* 2515 */         int appleTo = l.indexOf("]~");
/* 2516 */         String apple = l.substring(appleFrom, appleTo);
/* 2517 */         ItemStack item = null;
/*      */         
/* 2519 */         switch (apple) {
/*      */           case "look_block":
/* 2521 */             block = player.getTargetBlock(transparentBlock, 12);
/* 2522 */             item = block.getState().getData().toItemStack(1);
/*      */             break;
/*      */           case "main_hand_item":
/* 2525 */             item = player.getInventory().getItemInMainHand();
/*      */             break;
/*      */           case "off_hand_item":
/* 2528 */             return false;
/*      */           case "click_item":
/* 2530 */             if (event instanceof InventoryClickEvent) {
/* 2531 */               item = ((InventoryClickEvent)event).getCurrentItem();
/*      */             }
/*      */             break;
/*      */           case "event_block":
/* 2535 */             if (event instanceof BlockEvent) {
/* 2536 */               Block block1 = ((BlockEvent)event).getBlock();
/* 2537 */               item = block1.getState().getData().toItemStack(1);
/*      */             } 
/*      */             break;
/*      */         } 
/* 2541 */         ItemStack checkOff = player.getInventory().getItemInOffHand().clone();
/* 2542 */         assert item != null;
/* 2543 */         if (item.getAmount() == 1 && 
/* 2544 */           checkOff != null) {
/* 2545 */           checkOff.setAmount(1);
/*      */         }
/*      */         
/* 2548 */         if (item.hasItemMeta()) {
/* 2549 */           if (checkOff != null && !checkOff.equals(item)) {
/* 2550 */             return true;
/*      */           }
/*      */         }
/* 2553 */         else if (checkOff != null && checkOff.getType() != item.getType() && checkOff.getAmount() != item.getAmount()) {
/* 2554 */           return true;
/*      */         } 
/*      */       } 
/*      */     } 
/*      */     
/* 2559 */     return false;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean itemstack(String condition, Player player, Event event) {
/* 2565 */     Set<Material> transparentBlock = new HashSet<>();
/* 2566 */     transparentBlock.add(Material.AIR);
/* 2567 */     int from = condition.indexOf("=") + 1;
/* 2568 */     String eq = condition.substring(from);
/* 2569 */     if (eq.startsWith("item")) {
/* 2570 */       int start = eq.indexOf("[-") + 2;
/* 2571 */       int end = eq.indexOf("-]");
/* 2572 */       int centerTo = eq.indexOf('*');
/* 2573 */       int centerFrom = eq.indexOf('*') + 1;
/* 2574 */       int amStart = eq.indexOf("[+") + 2;
/* 2575 */       int amEnd = eq.indexOf("+]");
/*      */       
/* 2577 */       int amount = Integer.parseInt(eq.substring(amStart, amEnd));
/* 2578 */       int itemID = Integer.parseInt(eq.substring(start, centerTo));
/* 2579 */       String name = eq.substring(centerFrom, end);
/*      */       
/* 2581 */       ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 2582 */       if (!name.equals("default")) {
/* 2583 */         ItemMeta itemMeta = item.getItemMeta();
/* 2584 */         itemMeta.setDisplayName(name);
/* 2585 */         item.setItemMeta(itemMeta);
/* 2586 */         if (event instanceof PlayerPickupItemEvent)
/* 2587 */           return (((PlayerPickupItemEvent)event).getItem().getItemStack() == item); 
/* 2588 */         if (event instanceof PlayerDropItemEvent)
/* 2589 */           return (((PlayerDropItemEvent)event).getItemDrop().getItemStack() == item); 
/* 2590 */         if (event instanceof PlayerItemBreakEvent)
/* 2591 */           return (((PlayerItemBreakEvent)event).getBrokenItem() == item); 
/* 2592 */         if (event instanceof InventoryClickEvent) {
/* 2593 */           return (((InventoryClickEvent)event).getCurrentItem() == item);
/*      */         }
/*      */       } else {
/* 2596 */         if (event instanceof PlayerPickupItemEvent)
/* 2597 */           return 
/* 2598 */             (((PlayerPickupItemEvent)event).getItem().getItemStack().getType() == item.getType()); 
/* 2599 */         if (event instanceof PlayerDropItemEvent)
/* 2600 */           return 
/* 2601 */             (((PlayerDropItemEvent)event).getItemDrop().getItemStack().getType() == item.getType()); 
/* 2602 */         if (event instanceof PlayerItemBreakEvent)
/* 2603 */           return 
/* 2604 */             (((PlayerItemBreakEvent)event).getBrokenItem().getType() == item.getType()); 
/* 2605 */         if (event instanceof InventoryClickEvent) {
/* 2606 */           return 
/* 2607 */             (((InventoryClickEvent)event).getCurrentItem().getType() == item.getType());
/*      */         }
/*      */       } 
/* 2610 */     } else if (eq.startsWith("apple")) {
/* 2611 */       Block block; int appleFrom = eq.indexOf("[") + 1;
/* 2612 */       int appleTo = eq.indexOf("]~");
/* 2613 */       String apple = eq.substring(appleFrom, appleTo);
/* 2614 */       ItemStack item = null;
/*      */       
/* 2616 */       switch (apple) {
/*      */         case "look_block":
/* 2618 */           block = player.getTargetBlock(transparentBlock, 12);
/* 2619 */           item = block.getState().getData().toItemStack(1);
/*      */           break;
/*      */         case "main_hand_item":
/* 2622 */           item = player.getInventory().getItemInMainHand();
/*      */           break;
/*      */         case "off_hand_item":
/* 2625 */           item = player.getInventory().getItemInOffHand();
/*      */           break;
/*      */         case "click_item":
/* 2628 */           if (event instanceof InventoryClickEvent) {
/* 2629 */             item = ((InventoryClickEvent)event).getCurrentItem();
/*      */           }
/*      */           break;
/*      */         case "event_block":
/* 2633 */           if (event instanceof BlockEvent) {
/* 2634 */             Block block1 = ((BlockEvent)event).getBlock();
/* 2635 */             item = block1.getState().getData().toItemStack(1);
/*      */           } 
/*      */           break;
/*      */       } 
/* 2639 */       assert item != null;
/* 2640 */       if (item.hasItemMeta()) {
/* 2641 */         if (event instanceof PlayerPickupItemEvent)
/* 2642 */           return (((PlayerPickupItemEvent)event).getItem().getItemStack() == item); 
/* 2643 */         if (event instanceof PlayerDropItemEvent)
/* 2644 */           return (((PlayerDropItemEvent)event).getItemDrop().getItemStack() == item); 
/* 2645 */         if (event instanceof PlayerItemBreakEvent)
/* 2646 */           return (((PlayerItemBreakEvent)event).getBrokenItem() == item); 
/* 2647 */         if (event instanceof InventoryClickEvent) {
/* 2648 */           return (((InventoryClickEvent)event).getCurrentItem() == item);
/*      */         }
/*      */       } else {
/* 2651 */         if (event instanceof PlayerPickupItemEvent)
/* 2652 */           return 
/* 2653 */             (((PlayerPickupItemEvent)event).getItem().getItemStack().getType() == item.getType()); 
/* 2654 */         if (event instanceof PlayerDropItemEvent)
/* 2655 */           return 
/* 2656 */             (((PlayerDropItemEvent)event).getItemDrop().getItemStack().getType() == item.getType()); 
/* 2657 */         if (event instanceof PlayerItemBreakEvent)
/* 2658 */           return 
/* 2659 */             (((PlayerItemBreakEvent)event).getBrokenItem().getType() == item.getType()); 
/* 2660 */         if (event instanceof InventoryClickEvent) {
/* 2661 */           return 
/* 2662 */             (((InventoryClickEvent)event).getCurrentItem().getType() == item.getType());
/*      */         }
/*      */       } 
/*      */     } 
/* 2666 */     return false;
/*      */   }
/*      */   
/*      */   public boolean itemstack_no(String condition, Player player, Event event) {
/* 2670 */     Set<Material> transparentBlock = new HashSet<>();
/* 2671 */     transparentBlock.add(Material.AIR);
/* 2672 */     int from = condition.indexOf("=") + 1;
/* 2673 */     String eq = condition.substring(from);
/* 2674 */     if (eq.startsWith("item")) {
/* 2675 */       int start = eq.indexOf("[-") + 2;
/* 2676 */       int end = eq.indexOf("-]");
/* 2677 */       int centerTo = eq.indexOf('*');
/* 2678 */       int centerFrom = eq.indexOf('*') + 1;
/* 2679 */       int amStart = eq.indexOf("[+") + 2;
/* 2680 */       int amEnd = eq.indexOf("+]");
/*      */       
/* 2682 */       int amount = Integer.parseInt(eq.substring(amStart, amEnd));
/* 2683 */       int itemID = Integer.parseInt(eq.substring(start, centerTo));
/* 2684 */       String name = eq.substring(centerFrom, end);
/*      */       
/* 2686 */       ItemStack item = new ItemStack(Material.getMaterial(itemID), amount);
/* 2687 */       if (!name.equals("default")) {
/* 2688 */         ItemMeta itemMeta = item.getItemMeta();
/* 2689 */         itemMeta.setDisplayName(name);
/* 2690 */         item.setItemMeta(itemMeta);
/* 2691 */         if (event instanceof PlayerPickupItemEvent)
/* 2692 */           return (((PlayerPickupItemEvent)event).getItem().getItemStack() != item); 
/* 2693 */         if (event instanceof PlayerDropItemEvent)
/* 2694 */           return (((PlayerDropItemEvent)event).getItemDrop().getItemStack() != item); 
/* 2695 */         if (event instanceof PlayerItemBreakEvent)
/* 2696 */           return (((PlayerItemBreakEvent)event).getBrokenItem() != item); 
/* 2697 */         if (event instanceof InventoryClickEvent) {
/* 2698 */           return (((InventoryClickEvent)event).getCurrentItem() != item);
/*      */         }
/*      */       } else {
/* 2701 */         if (event instanceof PlayerPickupItemEvent)
/* 2702 */           return 
/* 2703 */             (((PlayerPickupItemEvent)event).getItem().getItemStack().getType() != item.getType()); 
/* 2704 */         if (event instanceof PlayerDropItemEvent)
/* 2705 */           return 
/* 2706 */             (((PlayerDropItemEvent)event).getItemDrop().getItemStack().getType() != item.getType()); 
/* 2707 */         if (event instanceof PlayerItemBreakEvent)
/* 2708 */           return 
/* 2709 */             (((PlayerItemBreakEvent)event).getBrokenItem().getType() != item.getType()); 
/* 2710 */         if (event instanceof InventoryClickEvent) {
/* 2711 */           return 
/* 2712 */             (((InventoryClickEvent)event).getCurrentItem().getType() != item.getType());
/*      */         }
/*      */       } 
/* 2715 */     } else if (eq.startsWith("apple")) {
/* 2716 */       Block block; int appleFrom = eq.indexOf("[") + 1;
/* 2717 */       int appleTo = eq.indexOf("]~");
/* 2718 */       String apple = eq.substring(appleFrom, appleTo);
/* 2719 */       ItemStack item = null;
/*      */       
/* 2721 */       switch (apple) {
/*      */         case "look_block":
/* 2723 */           block = player.getTargetBlock(transparentBlock, 12);
/* 2724 */           item = block.getState().getData().toItemStack(1);
/*      */           break;
/*      */         case "main_hand_item":
/* 2727 */           item = player.getInventory().getItemInMainHand();
/*      */           break;
/*      */         case "off_hand_item":
/* 2730 */           item = player.getInventory().getItemInOffHand();
/*      */           break;
/*      */         case "click_item":
/* 2733 */           if (event instanceof InventoryClickEvent) {
/* 2734 */             item = ((InventoryClickEvent)event).getCurrentItem();
/*      */           }
/*      */           break;
/*      */         case "event_block":
/* 2738 */           if (event instanceof BlockEvent) {
/* 2739 */             Block block1 = ((BlockEvent)event).getBlock();
/* 2740 */             item = block1.getState().getData().toItemStack(1);
/*      */           } 
/*      */           break;
/*      */       } 
/* 2744 */       assert item != null;
/* 2745 */       if (item.hasItemMeta()) {
/* 2746 */         if (event instanceof PlayerPickupItemEvent)
/* 2747 */           return (((PlayerPickupItemEvent)event).getItem().getItemStack() != item); 
/* 2748 */         if (event instanceof PlayerDropItemEvent)
/* 2749 */           return (((PlayerDropItemEvent)event).getItemDrop().getItemStack() != item); 
/* 2750 */         if (event instanceof PlayerItemBreakEvent)
/* 2751 */           return (((PlayerItemBreakEvent)event).getBrokenItem() != item); 
/* 2752 */         if (event instanceof InventoryClickEvent) {
/* 2753 */           return (((InventoryClickEvent)event).getCurrentItem() != item);
/*      */         }
/*      */       } else {
/* 2756 */         if (event instanceof PlayerPickupItemEvent)
/* 2757 */           return 
/* 2758 */             (((PlayerPickupItemEvent)event).getItem().getItemStack().getType() != item.getType()); 
/* 2759 */         if (event instanceof PlayerDropItemEvent)
/* 2760 */           return 
/* 2761 */             (((PlayerDropItemEvent)event).getItemDrop().getItemStack().getType() != item.getType()); 
/* 2762 */         if (event instanceof PlayerItemBreakEvent)
/* 2763 */           return 
/* 2764 */             (((PlayerItemBreakEvent)event).getBrokenItem().getType() != item.getType()); 
/* 2765 */         if (event instanceof InventoryClickEvent) {
/* 2766 */           return 
/* 2767 */             (((InventoryClickEvent)event).getCurrentItem().getType() != item.getType());
/*      */         }
/*      */       } 
/*      */     } 
/* 2771 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean lookingAt(String condition, Player player, Event event) {
/* 2776 */     Set<Material> transparentBlock = new HashSet<>();
/* 2777 */     transparentBlock.add(Material.AIR);
/* 2778 */     int radiusFrom = condition.indexOf(":") + 1;
/* 2779 */     int radiusTo = condition.indexOf("]");
/* 2780 */     int radius = Integer.parseInt(condition.substring(radiusFrom, radiusTo));
/*      */     
/* 2782 */     int from = condition.indexOf("=") + 1;
/* 2783 */     String eq = condition.substring(from);
/* 2784 */     String[] eqList = eq.split("\\|");
/* 2785 */     for (String l : eqList) {
/* 2786 */       if (l.startsWith("coord")) {
/* 2787 */         int coordsFrom = l.indexOf("[") + 1;
/* 2788 */         int coordsTo = l.indexOf("]");
/* 2789 */         String[] coords = l.substring(coordsFrom, coordsTo).split("~");
/*      */         
/* 2791 */         int x = Integer.parseInt(coords[0]);
/* 2792 */         int y = Integer.parseInt(coords[1]);
/* 2793 */         int z = Integer.parseInt(coords[2]);
/*      */         
/* 2795 */         Location blockLoc = player.getTargetBlock(transparentBlock, radius).getLocation();
/* 2796 */         if (blockLoc.getBlockX() == x && blockLoc
/* 2797 */           .getBlockY() == y && blockLoc
/* 2798 */           .getBlockZ() == z) {
/* 2799 */           return true;
/*      */         }
/* 2801 */       } else if (l.startsWith("block")) {
/* 2802 */         int idFrom = l.indexOf("[") + 1;
/* 2803 */         int idTo = l.indexOf("]");
/* 2804 */         int id = Integer.parseInt(l.substring(idFrom, idTo));
/*      */         
/* 2806 */         if (player.getTargetBlock(transparentBlock, radius).getType().getId() == id) {
/* 2807 */           return true;
/*      */         }
/* 2809 */       } else if (eq.startsWith("apple")) {
/* 2810 */         int appleFrom = eq.indexOf("[") + 1;
/* 2811 */         int appleTo = eq.indexOf("]~");
/* 2812 */         String apple = eq.substring(appleFrom, appleTo);
/* 2813 */         int id = 0;
/* 2814 */         Location coords = null;
/*      */         
/* 2816 */         switch (apple) {
/*      */           case "look_block":
/*      */           case "look_block_loc":
/* 2819 */             return true;
/*      */           case "main_hand_item":
/* 2821 */             if (player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 2822 */               id = player.getInventory().getItemInMainHand().getType().getId();
/*      */             }
/*      */             break;
/*      */           case "off_hand_item":
/* 2826 */             if (player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 2827 */               id = player.getInventory().getItemInOffHand().getType().getId();
/*      */             }
/*      */             break;
/*      */           case "click_item":
/* 2831 */             if (event instanceof InventoryClickEvent && 
/* 2832 */               player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 2833 */               id = ((InventoryClickEvent)event).getCurrentItem().getType().getId();
/*      */             }
/*      */             break;
/*      */           
/*      */           case "event_block":
/* 2838 */             if (event instanceof BlockEvent) {
/* 2839 */               Block block = ((BlockEvent)event).getBlock();
/* 2840 */               id = block.getState().getData().toItemStack(1).getType().getId();
/*      */             } 
/*      */             break;
/*      */           case "location":
/* 2844 */             coords = player.getLocation();
/*      */             break;
/*      */           case "block_loc":
/* 2847 */             if (event instanceof BlockEvent) {
/* 2848 */               coords = ((BlockEvent)event).getBlock().getLocation();
/*      */             }
/*      */             break;
/*      */         } 
/* 2852 */         if (coords != null) {
/* 2853 */           Location blockLoc = player.getTargetBlock(transparentBlock, radius).getLocation();
/* 2854 */           if (blockLoc.getBlockX() == coords.getBlockX() && blockLoc
/* 2855 */             .getBlockY() == coords.getBlockY() && blockLoc
/* 2856 */             .getBlockZ() == coords.getBlockZ()) {
/* 2857 */             return true;
/*      */           }
/* 2859 */         } else if (id != 0 && 
/* 2860 */           player.getTargetBlock(transparentBlock, radius).getType().getId() == id) {
/* 2861 */           return true;
/*      */         } 
/*      */       } 
/*      */     } 
/*      */     
/* 2866 */     return false;
/*      */   }
/*      */   
/*      */   public boolean lookingAt_no(String condition, Player player, Event event) {
/* 2870 */     Set<Material> transparentBlock = new HashSet<>();
/* 2871 */     transparentBlock.add(Material.AIR);
/* 2872 */     int radiusFrom = condition.indexOf(":") + 1;
/* 2873 */     int radiusTo = condition.indexOf("]");
/* 2874 */     int radius = Integer.parseInt(condition.substring(radiusFrom, radiusTo));
/*      */     
/* 2876 */     int from = condition.indexOf("=") + 1;
/* 2877 */     String eq = condition.substring(from);
/* 2878 */     String[] eqList = eq.split("\\|");
/* 2879 */     for (String l : eqList) {
/* 2880 */       if (l.startsWith("coord")) {
/* 2881 */         int coordsFrom = l.indexOf("[") + 1;
/* 2882 */         int coordsTo = l.indexOf("]");
/* 2883 */         String[] coords = l.substring(coordsFrom, coordsTo).split("~");
/*      */         
/* 2885 */         int x = Integer.parseInt(coords[0]);
/* 2886 */         int y = Integer.parseInt(coords[1]);
/* 2887 */         int z = Integer.parseInt(coords[2]);
/*      */         
/* 2889 */         Location blockLoc = player.getTargetBlock(transparentBlock, radius).getLocation();
/* 2890 */         if (blockLoc.getBlockX() != x && blockLoc
/* 2891 */           .getBlockY() != y && blockLoc
/* 2892 */           .getBlockZ() != z) {
/* 2893 */           return true;
/*      */         }
/* 2895 */       } else if (l.startsWith("block")) {
/* 2896 */         int idFrom = l.indexOf("[") + 1;
/* 2897 */         int idTo = l.indexOf("]");
/* 2898 */         int id = Integer.parseInt(l.substring(idFrom, idTo));
/*      */         
/* 2900 */         if (player.getTargetBlock(transparentBlock, radius).getType().getId() != id) {
/* 2901 */           return true;
/*      */         }
/* 2903 */       } else if (eq.startsWith("apple")) {
/* 2904 */         int appleFrom = eq.indexOf("[") + 1;
/* 2905 */         int appleTo = eq.indexOf("]~");
/* 2906 */         String apple = eq.substring(appleFrom, appleTo);
/* 2907 */         int id = 0;
/* 2908 */         Location coords = null;
/*      */         
/* 2910 */         switch (apple) {
/*      */           case "look_block":
/*      */           case "look_block_loc":
/* 2913 */             return false;
/*      */           case "main_hand_item":
/* 2915 */             if (player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 2916 */               id = player.getInventory().getItemInMainHand().getType().getId();
/*      */             }
/*      */             break;
/*      */           case "off_hand_item":
/* 2920 */             if (player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 2921 */               id = player.getInventory().getItemInOffHand().getType().getId();
/*      */             }
/*      */             break;
/*      */           case "click_item":
/* 2925 */             if (event instanceof InventoryClickEvent && 
/* 2926 */               player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 2927 */               id = ((InventoryClickEvent)event).getCurrentItem().getType().getId();
/*      */             }
/*      */             break;
/*      */           
/*      */           case "event_block":
/* 2932 */             if (event instanceof BlockEvent) {
/* 2933 */               Block block = ((BlockEvent)event).getBlock();
/* 2934 */               id = block.getState().getData().toItemStack(1).getType().getId();
/*      */             } 
/*      */             break;
/*      */           case "location":
/* 2938 */             coords = player.getLocation();
/*      */             break;
/*      */           case "block_loc":
/* 2941 */             if (event instanceof BlockEvent) {
/* 2942 */               coords = ((BlockEvent)event).getBlock().getLocation();
/*      */             }
/*      */             break;
/*      */         } 
/* 2946 */         if (coords != null) {
/* 2947 */           Location blockLoc = player.getTargetBlock(transparentBlock, radius).getLocation();
/* 2948 */           if (blockLoc.getBlockX() != coords.getBlockX() && blockLoc
/* 2949 */             .getBlockY() != coords.getBlockY() && blockLoc
/* 2950 */             .getBlockZ() != coords.getBlockZ()) {
/* 2951 */             return true;
/*      */           }
/* 2953 */         } else if (id != 0 && 
/* 2954 */           player.getTargetBlock(transparentBlock, radius).getType().getId() != id) {
/* 2955 */           return true;
/*      */         } 
/*      */       } 
/*      */     } 
/*      */     
/* 2960 */     return false;
/*      */   }
/*      */   
/*      */   public boolean playerNear(String condition, Player player, Event event) {
/* 2964 */     Set<Material> transparentBlock = new HashSet<>();
/* 2965 */     transparentBlock.add(Material.AIR);
/* 2966 */     int radiusFrom = condition.indexOf(":") + 1;
/* 2967 */     int radiusTo = condition.indexOf("]");
/* 2968 */     int radius = Integer.parseInt(condition.substring(radiusFrom, radiusTo));
/*      */     
/* 2970 */     int from = condition.indexOf("=") + 1;
/* 2971 */     String eq = condition.substring(from);
/* 2972 */     String[] eqList = eq.split("\\|");
/* 2973 */     for (String l : eqList) {
/* 2974 */       if (l.startsWith("coord")) {
/* 2975 */         int coordsFrom = l.indexOf("[") + 1;
/* 2976 */         int coordsTo = l.indexOf("]");
/* 2977 */         String[] coordsList = l.substring(coordsFrom, coordsTo).split("~");
/*      */         
/* 2979 */         double x = Double.parseDouble(coordsList[0]);
/* 2980 */         double y = Double.parseDouble(coordsList[1]);
/* 2981 */         double z = Double.parseDouble(coordsList[2]);
/*      */         
/* 2983 */         Location coords = new Location(player.getWorld(), x, y, z);
/* 2984 */         double distance = player.getLocation().distance(coords);
/*      */         
/* 2986 */         if (distance <= radius) {
/* 2987 */           return true;
/*      */         }
/* 2989 */       } else if (eq.startsWith("apple")) {
/* 2990 */         int appleFrom = eq.indexOf("[") + 1;
/* 2991 */         int appleTo = eq.indexOf("]~");
/* 2992 */         String apple = eq.substring(appleFrom, appleTo);
/* 2993 */         Location coords = null;
/*      */         
/* 2995 */         switch (apple) {
/*      */           case "location":
/* 2997 */             coords = player.getLocation();
/*      */             break;
/*      */           case "look_block_loc":
/* 3000 */             coords = player.getTargetBlock(transparentBlock, 12).getLocation();
/*      */             break;
/*      */           case "block_loc":
/* 3003 */             if (event instanceof BlockEvent) {
/* 3004 */               coords = ((BlockEvent)event).getBlock().getLocation();
/*      */             }
/*      */             break;
/*      */         } 
/* 3008 */         if (coords != null) {
/* 3009 */           double distance = player.getLocation().distance(coords);
/* 3010 */           if (distance <= radius) {
/* 3011 */             return true;
/*      */           }
/*      */         } 
/*      */       } 
/*      */     } 
/* 3016 */     return false;
/*      */   }
/*      */   public boolean playerNear_no(String condition, Player player, Event event) {
/* 3019 */     Set<Material> transparentBlock = new HashSet<>();
/* 3020 */     transparentBlock.add(Material.AIR);
/* 3021 */     int radiusFrom = condition.indexOf(":") + 1;
/* 3022 */     int radiusTo = condition.indexOf("]");
/* 3023 */     int radius = Integer.parseInt(condition.substring(radiusFrom, radiusTo));
/*      */     
/* 3025 */     int from = condition.indexOf("=") + 1;
/* 3026 */     String eq = condition.substring(from);
/* 3027 */     String[] eqList = eq.split("\\|");
/* 3028 */     for (String l : eqList) {
/* 3029 */       if (l.startsWith("coord")) {
/* 3030 */         int coordsFrom = l.indexOf("[") + 1;
/* 3031 */         int coordsTo = l.indexOf("]");
/* 3032 */         String[] coordsList = l.substring(coordsFrom, coordsTo).split("~");
/*      */         
/* 3034 */         double x = Double.parseDouble(coordsList[0]);
/* 3035 */         double y = Double.parseDouble(coordsList[1]);
/* 3036 */         double z = Double.parseDouble(coordsList[2]);
/*      */         
/* 3038 */         Location coords = new Location(player.getWorld(), x, y, z);
/* 3039 */         double distance = player.getLocation().distance(coords);
/*      */         
/* 3041 */         if (distance <= radius) {
/* 3042 */           return false;
/*      */         }
/* 3044 */       } else if (eq.startsWith("apple")) {
/* 3045 */         int appleFrom = eq.indexOf("[") + 1;
/* 3046 */         int appleTo = eq.indexOf("]~");
/* 3047 */         String apple = eq.substring(appleFrom, appleTo);
/* 3048 */         Location coords = null;
/*      */         
/* 3050 */         switch (apple) {
/*      */           case "location":
/* 3052 */             coords = player.getLocation();
/*      */             break;
/*      */           case "look_block_loc":
/* 3055 */             coords = player.getTargetBlock(transparentBlock, 12).getLocation();
/*      */             break;
/*      */           case "block_loc":
/* 3058 */             if (event instanceof BlockEvent) {
/* 3059 */               coords = ((BlockEvent)event).getBlock().getLocation();
/*      */             }
/*      */             break;
/*      */         } 
/* 3063 */         if (coords != null) {
/* 3064 */           double distance = player.getLocation().distance(coords);
/* 3065 */           if (distance <= radius) {
/* 3066 */             return false;
/*      */           }
/*      */         } 
/*      */       } 
/*      */     } 
/* 3071 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean standsAt(String condition, Player player, Event event) {
/* 3076 */     Set<Material> transparentBlock = new HashSet<>();
/* 3077 */     transparentBlock.add(Material.AIR);
/* 3078 */     int from = condition.indexOf("=") + 1;
/* 3079 */     String eq = condition.substring(from);
/* 3080 */     String[] eqList = eq.split("\\|");
/* 3081 */     for (String l : eqList) {
/* 3082 */       if (l.startsWith("coord")) {
/* 3083 */         int coordsFrom = l.indexOf("[") + 1;
/* 3084 */         int coordsTo = l.indexOf("]");
/* 3085 */         String[] coordsList = l.substring(coordsFrom, coordsTo).split("~");
/*      */         
/* 3087 */         int x = Integer.parseInt(coordsList[0]);
/* 3088 */         int y = Integer.parseInt(coordsList[1]);
/* 3089 */         int z = Integer.parseInt(coordsList[2]);
/*      */         
/* 3091 */         Location coords = new Location(player.getWorld(), x, y, z);
/* 3092 */         Location playercoords = player.getLocation();
/* 3093 */         if (coords.getBlockX() == playercoords.getBlockX() && coords
/* 3094 */           .getBlockY() == playercoords.getBlockY() && coords
/* 3095 */           .getBlockZ() == playercoords.getBlockZ()) {
/* 3096 */           return true;
/*      */         }
/* 3098 */       } else if (l.startsWith("block")) {
/* 3099 */         int idFrom = l.indexOf("[") + 1;
/* 3100 */         int idTo = l.indexOf("]");
/* 3101 */         int id = Integer.parseInt(l.substring(idFrom, idTo));
/* 3102 */         Location playercoords = player.getLocation();
/* 3103 */         Location blockLoc = playercoords.subtract(0.0D, 1.0D, 0.0D);
/* 3104 */         Block block = blockLoc.getBlock();
/* 3105 */         if (block.getType().getId() == id) {
/* 3106 */           return true;
/*      */         }
/* 3108 */       } else if (eq.startsWith("apple")) {
/* 3109 */         int appleFrom = eq.indexOf("[") + 1;
/* 3110 */         int appleTo = eq.indexOf("]~");
/* 3111 */         String apple = eq.substring(appleFrom, appleTo);
/* 3112 */         int id = 0;
/* 3113 */         Location coords = null;
/*      */         
/* 3115 */         switch (apple) {
/*      */           case "look_block":
/* 3117 */             id = player.getTargetBlock(transparentBlock, 12).getType().getId();
/*      */             break;
/*      */           case "look_block_loc":
/* 3120 */             coords = player.getTargetBlock(transparentBlock, 12).getLocation();
/*      */             break;
/*      */           case "main_hand_item":
/* 3123 */             if (player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 3124 */               id = player.getInventory().getItemInMainHand().getType().getId();
/*      */             }
/*      */             break;
/*      */           case "off_hand_item":
/* 3128 */             if (player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 3129 */               id = player.getInventory().getItemInOffHand().getType().getId();
/*      */             }
/*      */             break;
/*      */           case "click_item":
/* 3133 */             if (event instanceof InventoryClickEvent && 
/* 3134 */               player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 3135 */               id = ((InventoryClickEvent)event).getCurrentItem().getType().getId();
/*      */             }
/*      */             break;
/*      */           
/*      */           case "event_block":
/* 3140 */             if (event instanceof BlockEvent) {
/* 3141 */               Block block = ((BlockEvent)event).getBlock();
/* 3142 */               id = block.getState().getData().toItemStack(1).getType().getId();
/*      */             } 
/*      */             break;
/*      */           case "location":
/* 3146 */             coords = player.getLocation();
/*      */             break;
/*      */           case "block_loc":
/* 3149 */             if (event instanceof BlockEvent) {
/* 3150 */               coords = ((BlockEvent)event).getBlock().getLocation();
/*      */             }
/*      */             break;
/*      */         } 
/* 3154 */         if (coords != null) {
/* 3155 */           Location playercoords = player.getLocation();
/* 3156 */           if (coords.getBlockX() == playercoords.getBlockX() && coords
/* 3157 */             .getBlockY() == playercoords.getBlockY() && coords
/* 3158 */             .getBlockZ() == playercoords.getBlockZ()) {
/* 3159 */             return true;
/*      */           }
/* 3161 */         } else if (id != 0) {
/* 3162 */           Location playercoords = player.getLocation();
/* 3163 */           Location blockLoc = playercoords.subtract(0.0D, 1.0D, 0.0D);
/* 3164 */           Block block = blockLoc.getBlock();
/* 3165 */           if (block.getType().getId() == id) {
/* 3166 */             return true;
/*      */           }
/*      */         } 
/*      */       } 
/*      */     } 
/* 3171 */     return false;
/*      */   }
/*      */   
/*      */   public boolean standsAt_no(String condition, Player player, Event event) {
/* 3175 */     Set<Material> transparentBlock = new HashSet<>();
/* 3176 */     transparentBlock.add(Material.AIR);
/* 3177 */     int from = condition.indexOf("=") + 1;
/* 3178 */     String eq = condition.substring(from);
/* 3179 */     String[] eqList = eq.split("\\|");
/* 3180 */     for (String l : eqList) {
/* 3181 */       if (l.startsWith("coord")) {
/* 3182 */         int coordsFrom = l.indexOf("[") + 1;
/* 3183 */         int coordsTo = l.indexOf("]");
/* 3184 */         String[] coordsList = l.substring(coordsFrom, coordsTo).split("~");
/*      */         
/* 3186 */         int x = Integer.parseInt(coordsList[0]);
/* 3187 */         int y = Integer.parseInt(coordsList[1]);
/* 3188 */         int z = Integer.parseInt(coordsList[2]);
/*      */         
/* 3190 */         Location coords = new Location(player.getWorld(), x, y, z);
/* 3191 */         Location playercoords = player.getLocation();
/* 3192 */         if (coords.getBlockX() != playercoords.getBlockX() && coords
/* 3193 */           .getBlockY() != playercoords.getBlockY() && coords
/* 3194 */           .getBlockZ() != playercoords.getBlockZ()) {
/* 3195 */           return true;
/*      */         }
/* 3197 */       } else if (l.startsWith("block")) {
/* 3198 */         int idFrom = l.indexOf("[") + 1;
/* 3199 */         int idTo = l.indexOf("]");
/* 3200 */         int id = Integer.parseInt(l.substring(idFrom, idTo));
/* 3201 */         Location playercoords = player.getLocation();
/* 3202 */         Location blockLoc = playercoords.subtract(0.0D, 1.0D, 0.0D);
/* 3203 */         Block block = blockLoc.getBlock();
/* 3204 */         if (block.getType().getId() != id) {
/* 3205 */           return true;
/*      */         }
/* 3207 */       } else if (eq.startsWith("apple")) {
/* 3208 */         int appleFrom = eq.indexOf("[") + 1;
/* 3209 */         int appleTo = eq.indexOf("]~");
/* 3210 */         String apple = eq.substring(appleFrom, appleTo);
/* 3211 */         int id = 0;
/* 3212 */         Location coords = null;
/*      */         
/* 3214 */         switch (apple) {
/*      */           case "look_block":
/* 3216 */             id = player.getTargetBlock(transparentBlock, 12).getType().getId();
/*      */             break;
/*      */           case "look_block_loc":
/* 3219 */             coords = player.getTargetBlock(transparentBlock, 12).getLocation();
/*      */             break;
/*      */           case "main_hand_item":
/* 3222 */             if (player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 3223 */               id = player.getInventory().getItemInMainHand().getType().getId();
/*      */             }
/*      */             break;
/*      */           case "off_hand_item":
/* 3227 */             if (player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 3228 */               id = player.getInventory().getItemInOffHand().getType().getId();
/*      */             }
/*      */             break;
/*      */           case "click_item":
/* 3232 */             if (event instanceof InventoryClickEvent && 
/* 3233 */               player.getInventory().getItemInMainHand().getType().isBlock()) {
/* 3234 */               id = ((InventoryClickEvent)event).getCurrentItem().getType().getId();
/*      */             }
/*      */             break;
/*      */           
/*      */           case "event_block":
/* 3239 */             if (event instanceof BlockEvent) {
/* 3240 */               Block block = ((BlockEvent)event).getBlock();
/* 3241 */               id = block.getState().getData().toItemStack(1).getType().getId();
/*      */             } 
/*      */             break;
/*      */           case "location":
/* 3245 */             coords = player.getLocation();
/*      */             break;
/*      */           case "block_loc":
/* 3248 */             if (event instanceof BlockEvent) {
/* 3249 */               coords = ((BlockEvent)event).getBlock().getLocation();
/*      */             }
/*      */             break;
/*      */         } 
/* 3253 */         if (coords != null) {
/* 3254 */           Location playercoords = player.getLocation();
/* 3255 */           if (coords.getBlockX() != playercoords.getBlockX() && coords
/* 3256 */             .getBlockY() != playercoords.getBlockY() && coords
/* 3257 */             .getBlockZ() != playercoords.getBlockZ()) {
/* 3258 */             return true;
/*      */           }
/* 3260 */         } else if (id != 0) {
/* 3261 */           Location playercoords = player.getLocation();
/* 3262 */           Location blockLoc = playercoords.subtract(0.0D, 1.0D, 0.0D);
/* 3263 */           Block block = blockLoc.getBlock();
/* 3264 */           if (block.getType().getId() != id) {
/* 3265 */             return true;
/*      */           }
/*      */         } 
/*      */       } 
/*      */     } 
/* 3270 */     return false;
/*      */   }
/*      */   
/*      */   private boolean evaluateCondition(String condition, Entity entity, Event event) {
/*      */     Player player;
/* 3275 */     if (entity instanceof Player) {
/* 3276 */       player = ((Player)entity).getPlayer();
/*      */     } else {
/* 3278 */       player = null;
/*      */     } 
/* 3280 */     if (player != null)
/* 3281 */       if (condition.startsWith("!")) {
/* 3282 */         if (condition.startsWith("!message")) {
/* 3283 */           if (event instanceof AsyncPlayerChatEvent)
/* 3284 */             return message_no(condition, (AsyncPlayerChatEvent)event, player); 
/*      */         } else {
/* 3286 */           if (condition.startsWith("!playerName"))
/* 3287 */             return playerName_no(condition, player); 
/* 3288 */           if (condition.equals("!isFly"))
/* 3289 */             return !player.isFlying(); 
/* 3290 */           if (condition.equals("!isSneaking"))
/* 3291 */             return !player.isSneaking(); 
/* 3292 */           if (condition.equals("!isRunning"))
/* 3293 */             return !player.isSprinting(); 
/* 3294 */           if (condition.equals("!false"))
/* 3295 */             return true; 
/* 3296 */           if (condition.startsWith("!playerHaveItems"))
/* 3297 */             return playerHaveItems_no(condition, player, event); 
/* 3298 */           if (condition.startsWith("!inventoryHaveItems"))
/* 3299 */             return inventoryHaveItems_no(condition, player, event); 
/* 3300 */           if (condition.startsWith("!inventoryName")) {
/* 3301 */             if (event instanceof InventoryEvent)
/* 3302 */               return inventoryName_no(condition, (InventoryEvent)event, player); 
/*      */           } else {
/* 3304 */             if (condition.startsWith("!playerHoldingItemMain"))
/* 3305 */               return playerHoldingItemMain_no(condition, player, event); 
/* 3306 */             if (condition.startsWith("!playerHoldingItemOff"))
/* 3307 */               return playerHoldingItemOff_no(condition, player, event); 
/* 3308 */             if (condition.startsWith("!itemstack"))
/* 3309 */               return itemstack_no(condition, player, event); 
/* 3310 */             if (condition.startsWith("!lookingAt"))
/* 3311 */               return lookingAt_no(condition, player, event); 
/* 3312 */             if (condition.startsWith("!playerNear"))
/* 3313 */               return playerNear_no(condition, player, event); 
/* 3314 */             if (condition.startsWith("!standsAt"))
/* 3315 */               return standsAt_no(condition, player, event); 
/*      */           } 
/*      */         } 
/* 3318 */       } else if (condition.startsWith("message")) {
/* 3319 */         if (event instanceof AsyncPlayerChatEvent)
/* 3320 */           return message(condition, (AsyncPlayerChatEvent)event, player); 
/*      */       } else {
/* 3322 */         if (condition.startsWith("playerName"))
/* 3323 */           return playerName(condition, player); 
/* 3324 */         if (condition.equals("isFly"))
/* 3325 */           return player.isFlying(); 
/* 3326 */         if (condition.equals("isSneaking"))
/* 3327 */           return player.isSneaking(); 
/* 3328 */         if (condition.equals("isRunning"))
/* 3329 */           return player.isSprinting(); 
/* 3330 */         if (condition.equals("false"))
/* 3331 */           return false; 
/* 3332 */         if (condition.startsWith("playerHaveItems"))
/* 3333 */           return playerHaveItems(condition, player, event); 
/* 3334 */         if (condition.startsWith("inventoryHaveItems"))
/* 3335 */           return inventoryHaveItems(condition, player, event); 
/* 3336 */         if (condition.startsWith("inventoryName")) {
/* 3337 */           if (event instanceof InventoryEvent)
/* 3338 */             return inventoryName(condition, (InventoryEvent)event, player); 
/*      */         } else {
/* 3340 */           if (condition.startsWith("playerHoldingItemMain"))
/* 3341 */             return playerHoldingItemMain(condition, player, event); 
/* 3342 */           if (condition.startsWith("playerHoldingItemOff"))
/* 3343 */             return playerHoldingItemOff(condition, player, event); 
/* 3344 */           if (condition.startsWith("itemstack"))
/* 3345 */             return itemstack(condition, player, event); 
/* 3346 */           if (condition.startsWith("lookingAt"))
/* 3347 */             return lookingAt(condition, player, event); 
/* 3348 */           if (condition.startsWith("playerNear"))
/* 3349 */             return playerNear(condition, player, event); 
/* 3350 */           if (condition.startsWith("standsAt")) {
/* 3351 */             return standsAt(condition, player, event);
/*      */           }
/*      */         } 
/*      */       }  
/* 3355 */     return false;
/*      */   }
/*      */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\CodeCompiler\runCode.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */