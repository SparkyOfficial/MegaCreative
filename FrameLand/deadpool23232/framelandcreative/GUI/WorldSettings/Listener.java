/*     */ package deadpool23232.framelandcreative.GUI.WorldSettings;
/*     */ 
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import deadpool23232.framelandcreative.GUI.BlackList.bList.LMB;
/*     */ import deadpool23232.framelandcreative.GUI.BlackList.bList.RMB;
/*     */ import deadpool23232.framelandcreative.GUI.GameRules.GameRules;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Organizer;
/*     */ import deadpool23232.framelandcreative.GUI.Kick.KickPlayers;
/*     */ import deadpool23232.framelandcreative.GUI.WhiteList.wList.LMB;
/*     */ import deadpool23232.framelandcreative.GUI.WhiteList.wList.RMB;
/*     */ import deadpool23232.framelandcreative.GUI.WorldSettings.Display.Description;
/*     */ import deadpool23232.framelandcreative.GUI.WorldSettings.Display.ID;
/*     */ import deadpool23232.framelandcreative.GUI.WorldSettings.Display.Name;
/*     */ import deadpool23232.framelandcreative.GUI.WorldSettings.Functions.Availability;
/*     */ import deadpool23232.framelandcreative.GUI.WorldSettings.Functions.SpawnPoint;
/*     */ import deadpool23232.framelandcreative.GUI.WorldSettings.Functions.ToCode;
/*     */ import deadpool23232.framelandcreative.GUI.WorldSettings.Functions.ToPlay;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.inventory.InventoryClickEvent;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ import org.bukkit.scheduler.BukkitRunnable;
/*     */ 
/*     */ public class Listener implements Listener {
/*  32 */   FileConfiguration config = FrameLandCreative.getInstance().getConfigFile();
/*  33 */   FileConfiguration dataConfig = DataConfig.get();
/*     */   @EventHandler
/*     */   public void onMenuClick(InventoryClickEvent event) {
/*  36 */     if (event.getView().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&2Настройки мира"))) {
/*  37 */       event.setCancelled(true);
/*  38 */       if (event.getCurrentItem() == null)
/*  39 */         return;  if (event.getCurrentItem().getType() == Material.AIR)
/*  40 */         return;  final Player player = (Player)event.getWhoClicked();
/*     */       
/*  42 */       ItemStack item = event.getCurrentItem();
/*  43 */       ItemMeta itemMeta = item.getItemMeta();
/*  44 */       String itemName = itemMeta.getDisplayName();
/*     */       
/*  46 */       String cID = player.getWorld().getName().replace("-world", "").replace("-code", "");
/*  47 */       if (itemName.equals(FrameLandCreative.Color(this.config.getString("nameName")))) {
/*  48 */         player.closeInventory();
/*  49 */         Name.main(player);
/*  50 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("descName")))) {
/*  51 */         player.closeInventory();
/*  52 */         Description.main(player);
/*  53 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("spawnName")))) {
/*  54 */         player.closeInventory();
/*  55 */         SpawnPoint.main(cID, player);
/*  56 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("idName")))) {
/*  57 */         player.closeInventory();
/*  58 */         ID.main(player);
/*  59 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("playName")))) {
/*  60 */         player.closeInventory();
/*  61 */         ToPlay.main(player);
/*  62 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("codeName")))) {
/*  63 */         player.closeInventory();
/*  64 */         ToCode.main(player);
/*  65 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("closeName")))) {
/*  66 */         player.closeInventory();
/*  67 */         Availability.main(player);
/*  68 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("ruleName")))) {
/*  69 */         player.closeInventory();
/*  70 */         GameRules.main(player, cID);
/*  71 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("kickName")))) {
/*  72 */         player.closeInventory();
/*  73 */         KickPlayers.open(player);
/*  74 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("whiteName")))) {
/*  75 */         if (event.isLeftClick()) {
/*  76 */           player.closeInventory();
/*  77 */           LMB.open(player);
/*  78 */         } else if (event.isRightClick()) {
/*  79 */           player.closeInventory();
/*  80 */           RMB.open(player);
/*     */         } 
/*  82 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("blackName")))) {
/*  83 */         if (event.isLeftClick()) {
/*  84 */           player.closeInventory();
/*  85 */           LMB.open(player);
/*  86 */         } else if (event.isRightClick()) {
/*  87 */           player.closeInventory();
/*  88 */           RMB.open(player);
/*     */         } 
/*  90 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("gamesName")))) {
/*  91 */         player.closeInventory();
/*  92 */         (new BukkitRunnable()
/*     */           {
/*     */             public void run() {
/*  95 */               Organizer.use(player);
/*     */             }
/*  97 */           }).runTaskLater(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), 15L);
/*  98 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("infoName")))) {
/*  99 */         String avail; player.closeInventory();
/* 100 */         List<String> list = this.config.getStringList("fullInformation");
/* 101 */         String name = this.dataConfig.getString("registered-worlds." + cID + ".name");
/* 102 */         String bID = this.dataConfig.getString("registered-worlds." + cID + ".id");
/*     */         
/* 104 */         if (this.dataConfig.getBoolean("registered-worlds." + cID + ".opened")) {
/* 105 */           avail = FrameLandCreative.Color("&fОткрыт");
/*     */         } else {
/* 107 */           avail = FrameLandCreative.Color("&fЗакрыт");
/*     */         } 
/* 109 */         for (String line : list) {
/* 110 */           player.sendMessage(FrameLandCreative.Color(line
/* 111 */                 .replace("#name#", name)
/* 112 */                 .replace("#cID#", cID)
/* 113 */                 .replace("#bID#", bID)
/* 114 */                 .replace("#avail#", avail)
/* 115 */                 .replace("#desc#", "\n")));
/*     */         }
/*     */         
/* 118 */         List<String> list1 = this.dataConfig.getStringList("registered-worlds." + cID + ".description");
/* 119 */         for (String line : list1)
/* 120 */           player.sendMessage(FrameLandCreative.Color("&8 - " + line)); 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\WorldSettings\Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */