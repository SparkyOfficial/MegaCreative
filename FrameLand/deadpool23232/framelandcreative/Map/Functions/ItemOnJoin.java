/*     */ package deadpool23232.framelandcreative.Map.Functions;
/*     */ 
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Organizer;
/*     */ import deadpool23232.framelandcreative.GUI.WorldSettings.WorldSettings;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.World;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.block.Action;
/*     */ import org.bukkit.event.player.PlayerChangedWorldEvent;
/*     */ import org.bukkit.event.player.PlayerInteractEvent;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ import org.bukkit.scheduler.BukkitRunnable;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ItemOnJoin
/*     */   implements Listener
/*     */ {
/*  31 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*     */   
/*     */   @EventHandler
/*     */   public void onRightClickItem(PlayerInteractEvent e) {
/*  35 */     final Player player = e.getPlayer();
/*  36 */     World world = player.getWorld();
/*     */     
/*  38 */     String wID = world.getName().replace("-world", "").replace("-code", "");
/*     */     
/*  40 */     if (world.getName().contains("-world") && (
/*  41 */       e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && 
/*  42 */       player.getInventory().getItemInMainHand().getType() == Material.COMPASS && 
/*  43 */       player.getInventory().getItemInMainHand().getItemMeta().hasLore()) {
/*  44 */       String name = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
/*  45 */       if (name.equals(FrameLandCreative.Color(this.config.getString("compass.player.name")))) {
/*  46 */         (new BukkitRunnable()
/*     */           {
/*     */             public void run() {
/*  49 */               Organizer.use(player);
/*     */             }
/*  51 */           }).runTaskLater(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), 5L);
/*  52 */       } else if (name.equals(FrameLandCreative.Color(this.config.getString("compass.dev.name"))) && 
/*  53 */         DataConfig.get().getString("registered-worlds." + wID + ".author")
/*  54 */         .equals(player.getUniqueId().toString())) {
/*  55 */         WorldSettings.main(player, wID);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @EventHandler
/*     */   public void joinEvent(PlayerChangedWorldEvent event) {
/*  66 */     final Player player = event.getPlayer();
/*  67 */     World world = player.getWorld();
/*     */     
/*  69 */     if (world.getName().contains("-world")) {
/*  70 */       String wID = world.getName().replace("-world", "").replace("-code", "");
/*  71 */       final ItemStack item = new ItemStack(Material.COMPASS);
/*  72 */       ItemMeta meta = item.getItemMeta();
/*  73 */       List<String> lore = new ArrayList<>();
/*  74 */       if (DataConfig.get().getString("registered-worlds." + wID + ".author").equals(player.getUniqueId().toString())) {
/*  75 */         meta.setDisplayName(FrameLandCreative.Color(this.config.getString("compass.dev.name")));
/*  76 */         for (String line : this.config.getStringList("compass.dev.desc")) {
/*  77 */           lore.add(FrameLandCreative.Color(line));
/*     */         }
/*     */       } else {
/*  80 */         meta.setDisplayName(FrameLandCreative.Color(this.config.getString("compass.player.name")));
/*  81 */         for (String line : this.config.getStringList("compass.player.desc")) {
/*  82 */           lore.add(FrameLandCreative.Color(line));
/*     */         }
/*     */       } 
/*     */       
/*  86 */       meta.setLore(lore);
/*  87 */       item.setItemMeta(meta);
/*     */       
/*  89 */       if (!player.getInventory().contains(item))
/*  90 */         (new BukkitRunnable()
/*     */           {
/*     */             public void run() {
/*     */               try {
/*  94 */                 if (player.getInventory().getItem(8) == null || player.getInventory().getItem(8).getType() == Material.AIR) {
/*  95 */                   player.getInventory().setItem(8, item);
/*     */                 } else {
/*  97 */                   ItemStack oldItem = player.getInventory().getItem(8);
/*  98 */                   player.getInventory().setItem(8, item);
/*  99 */                   player.getInventory().addItem(new ItemStack[] { oldItem });
/*     */                 } 
/* 101 */               } catch (Exception exception) {}
/*     */             }
/* 105 */           }).runTaskLater(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), 10L); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\Map\Functions\ItemOnJoin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */