/*     */ package deadpool23232.framelandcreative.CODE.Items;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.Sound;
/*     */ import org.bukkit.World;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.EventPriority;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.block.Action;
/*     */ import org.bukkit.event.player.PlayerChangedWorldEvent;
/*     */ import org.bukkit.event.player.PlayerInteractEvent;
/*     */ import org.bukkit.event.player.PlayerTeleportEvent;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ import org.bukkit.scheduler.BukkitRunnable;
/*     */ 
/*     */ public class coordsSet implements Listener {
/*  23 */   public Map<Player, Location> codeLoc = new HashMap<>();
/*     */   
/*  25 */   private static final Map<Player, Boolean> isSession = new HashMap<>();
/*     */   
/*     */   public static void setSession(Player player, Boolean arg) {
/*  28 */     isSession.put(player, arg);
/*     */   }
/*     */   public static boolean getSession(Player player) {
/*  31 */     return ((Boolean)isSession.getOrDefault(player, Boolean.valueOf(false))).booleanValue();
/*     */   }
/*     */ 
/*     */   
/*     */   @EventHandler(priority = EventPriority.HIGHEST)
/*     */   public void a(PlayerChangedWorldEvent event) {
/*  37 */     final Player player = event.getPlayer();
/*  38 */     if (getSession(player) && 
/*  39 */       event.getFrom().getName().contains("-world")) {
/*  40 */       (new BukkitRunnable()
/*     */         {
/*     */           public void run() {
/*  43 */             coordsSet.setSession(player, Boolean.valueOf(false));
/*     */           }
/*  45 */         }).runTaskLater(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), 3L);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   @EventHandler(priority = EventPriority.HIGHEST)
/*     */   public void b(PlayerTeleportEvent event) {
/*  52 */     final Player player = event.getPlayer();
/*  53 */     if (event.getFrom() != event.getTo() && 
/*  54 */       getSession(player) && 
/*  55 */       event.getFrom().getWorld().getName().contains("-world")) {
/*  56 */       (new BukkitRunnable()
/*     */         {
/*     */           public void run() {
/*  59 */             coordsSet.setSession(player, Boolean.valueOf(false));
/*     */           }
/*  61 */         }).runTaskLater(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), 3L);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   @EventHandler(priority = EventPriority.LOWEST)
/*     */   public void onClickItem(PlayerInteractEvent event) {
/*  69 */     Player player = event.getPlayer();
/*  70 */     if (getSession(player)) event.setCancelled(true); 
/*  71 */     World world = player.getWorld();
/*  72 */     String id = world.getName().replace("-world", "").replace("-code", "");
/*     */     
/*  74 */     if ((world.getName().contains("-code") || world.getName().contains("-world")) && 
/*  75 */       player.getInventory().getItemInMainHand().getType() == Material.PAPER) {
/*  76 */       ItemStack item = player.getInventory().getItemInMainHand().clone();
/*  77 */       ItemMeta itemMeta = item.getItemMeta();
/*  78 */       Location pl = player.getLocation();
/*  79 */       if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
/*  80 */         if (player.getWorld().getName().contains("-world") && getSession(player)) {
/*  81 */           player.teleport(this.codeLoc.getOrDefault(player, Bukkit.getWorld(id + "-code").getSpawnLocation()));
/*  82 */         } else if (player.getWorld().getName().contains("-code") && !getSession(player)) {
/*  83 */           this.codeLoc.put(player, player.getLocation());
/*  84 */           setSession(player, Boolean.valueOf(true));
/*  85 */           player.teleport(Bukkit.getWorld(id + "-world").getSpawnLocation());
/*     */         } 
/*  87 */       } else if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && 
/*  88 */         player.getWorld().getName().contains("-world") && getSession(player)) {
/*  89 */         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
/*  90 */           if (event.getClickedBlock() != null) {
/*  91 */             if (event.getClickedBlock().getType() != Material.AIR) {
/*  92 */               Location blockLoc = event.getClickedBlock().getLocation();
/*  93 */               itemMeta.setDisplayName((FrameLandCreative.Color("&f" + String.format("%.2f", new Object[] { Double.valueOf(blockLoc.getX()) })) + " | " + String.format("%.2f", new Object[] { Double.valueOf(blockLoc.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(blockLoc.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(blockLoc.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(blockLoc.getPitch()) })).replace(",", "."));
/*     */             } else {
/*  95 */               itemMeta.setDisplayName((FrameLandCreative.Color("&f" + String.format("%.2f", new Object[] { Double.valueOf(pl.getX()) })) + " | " + String.format("%.2f", new Object[] { Double.valueOf(pl.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(pl.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(pl.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(pl.getPitch()) })).replace(",", "."));
/*     */             } 
/*  97 */             player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
/*     */           } 
/*  99 */         } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
/* 100 */           player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
/* 101 */           itemMeta.setDisplayName((FrameLandCreative.Color("&f" + String.format("%.2f", new Object[] { Double.valueOf(pl.getX()) })) + " | " + String.format("%.2f", new Object[] { Double.valueOf(pl.getY()) }) + " | " + String.format("%.2f", new Object[] { Double.valueOf(pl.getZ()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(pl.getYaw()) }) + " | " + String.format("%.2f", new Object[] { Float.valueOf(pl.getPitch()) })).replace(",", "."));
/*     */         } 
/* 103 */         item.setItemMeta(itemMeta);
/* 104 */         player.getInventory().setItemInMainHand(item);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Items\coordsSet.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */