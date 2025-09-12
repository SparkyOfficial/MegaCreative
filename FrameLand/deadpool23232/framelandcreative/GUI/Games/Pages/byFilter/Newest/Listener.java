/*     */ package deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest;
/*     */ 
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Functions.tpWorld;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.Latest.Last;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.Liked.Liked;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.Own.Own;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxLiked.Organizer;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.MaxPlayers.Organizer;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page1;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page10;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page2;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page3;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page4;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page5;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page6;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page7;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page8;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.Page9;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.byFilter.Newest.Pages.savedPages.Pages;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.inventory.InventoryClickEvent;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ public class Listener implements Listener {
/*  31 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*     */   
/*     */   @EventHandler
/*     */   public void onMenuClick(InventoryClickEvent event) {
/*  35 */     if (event.getView().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&eСписок игр - новейшее"))) {
/*  36 */       event.setCancelled(true);
/*  37 */       if (event.getCurrentItem() == null)
/*  38 */         return;  if (event.getCurrentItem().getType() == Material.AIR) {
/*     */         return;
/*     */       }
/*  41 */       Player player = (Player)event.getWhoClicked();
/*     */       
/*  43 */       ItemStack item = event.getCurrentItem();
/*  44 */       ItemMeta itemMeta = item.getItemMeta();
/*  45 */       String itemName = itemMeta.getDisplayName();
/*     */       
/*  47 */       int itemAmount = item.getAmount();
/*  48 */       if (itemName.equals(FrameLandCreative.Color(this.config.getString("gameList.back")))) {
/*  49 */         player.closeInventory();
/*  50 */         if (itemAmount == 1) {
/*  51 */           if (Page1.page1FirstTime) {
/*  52 */             Page1.open(player);
/*     */           } else {
/*  54 */             Pages.Page1(player);
/*     */           } 
/*  56 */         } else if (itemAmount == 2) {
/*  57 */           if (Page2.page2FirstTime) {
/*  58 */             Page2.open(player);
/*     */           } else {
/*  60 */             Pages.Page2(player);
/*     */           } 
/*  62 */         } else if (itemAmount == 3) {
/*  63 */           if (Page3.page3FirstTime) {
/*  64 */             Page3.open(player);
/*     */           } else {
/*  66 */             Pages.Page3(player);
/*     */           } 
/*  68 */         } else if (itemAmount == 4) {
/*  69 */           if (Page4.page4FirstTime) {
/*  70 */             Page4.open(player);
/*     */           } else {
/*  72 */             Pages.Page4(player);
/*     */           } 
/*  74 */         } else if (itemAmount == 5) {
/*  75 */           if (Page5.page5FirstTime) {
/*  76 */             Page5.open(player);
/*     */           } else {
/*  78 */             Pages.Page5(player);
/*     */           } 
/*  80 */         } else if (itemAmount == 6) {
/*  81 */           if (Page6.page6FirstTime) {
/*  82 */             Page6.open(player);
/*     */           } else {
/*  84 */             Pages.Page6(player);
/*     */           } 
/*  86 */         } else if (itemAmount == 7) {
/*  87 */           if (Page7.page7FirstTime) {
/*  88 */             Page7.open(player);
/*     */           } else {
/*  90 */             Pages.Page7(player);
/*     */           } 
/*  92 */         } else if (itemAmount == 8) {
/*  93 */           if (Page8.page8FirstTime) {
/*  94 */             Page8.open(player);
/*     */           } else {
/*  96 */             Pages.Page8(player);
/*     */           } 
/*  98 */         } else if (itemAmount == 9) {
/*  99 */           if (Page9.page9FirstTime) {
/* 100 */             Page9.open(player);
/*     */           } else {
/* 102 */             Pages.Page9(player);
/*     */           } 
/*     */         } 
/* 105 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("gameList.front")))) {
/* 106 */         player.closeInventory();
/* 107 */         if (itemAmount == 2) {
/* 108 */           if (Page2.page2FirstTime) {
/* 109 */             Page2.open(player);
/*     */           } else {
/* 111 */             Pages.Page2(player);
/*     */           } 
/* 113 */         } else if (itemAmount == 3) {
/* 114 */           if (Page3.page3FirstTime) {
/* 115 */             Page3.open(player);
/*     */           } else {
/* 117 */             Pages.Page3(player);
/*     */           } 
/* 119 */         } else if (itemAmount == 4) {
/* 120 */           if (Page4.page4FirstTime) {
/* 121 */             Page4.open(player);
/*     */           } else {
/* 123 */             Pages.Page4(player);
/*     */           } 
/* 125 */         } else if (itemAmount == 5) {
/* 126 */           if (Page5.page5FirstTime) {
/* 127 */             Page5.open(player);
/*     */           } else {
/* 129 */             Pages.Page5(player);
/*     */           } 
/* 131 */         } else if (itemAmount == 6) {
/* 132 */           if (Page6.page6FirstTime) {
/* 133 */             Page6.open(player);
/*     */           } else {
/* 135 */             Pages.Page6(player);
/*     */           } 
/* 137 */         } else if (itemAmount == 7) {
/* 138 */           if (Page7.page7FirstTime) {
/* 139 */             Page7.open(player);
/*     */           } else {
/* 141 */             Pages.Page7(player);
/*     */           } 
/* 143 */         } else if (itemAmount == 8) {
/* 144 */           if (Page8.page8FirstTime) {
/* 145 */             Page8.open(player);
/*     */           } else {
/* 147 */             Pages.Page8(player);
/*     */           } 
/* 149 */         } else if (itemAmount == 9) {
/* 150 */           if (Page9.page9FirstTime) {
/* 151 */             Page9.open(player);
/*     */           } else {
/* 153 */             Pages.Page9(player);
/*     */           } 
/* 155 */         } else if (itemAmount == 10) {
/* 156 */           if (Page10.page10FirstTime) {
/* 157 */             Page10.open(player);
/*     */           } else {
/* 159 */             Pages.Page10(player);
/*     */           } 
/*     */         } 
/* 162 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("maxUniq.name")))) {
/* 163 */         player.closeInventory();
/* 164 */         Organizer.use(player);
/* 165 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("maxLiked.name")))) {
/* 166 */         player.closeInventory();
/* 167 */         Organizer.use(player);
/* 168 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("own.name")))) {
/* 169 */         player.closeInventory();
/* 170 */         Own.open(player);
/* 171 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("latest.name")))) {
/* 172 */         player.closeInventory();
/* 173 */         Last.open(player);
/* 174 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("liked.name")))) {
/* 175 */         player.closeInventory();
/* 176 */         Liked.open(player);
/* 177 */       } else if (item.getType() == Material.SKULL_ITEM) {
/*     */         try {
/* 179 */           if (((String)itemMeta.getLore().get(0)).substring(0, 8).equals(FrameLandCreative.Color("&8ID: &7"))) {
/* 180 */             player.closeInventory();
/* 181 */             String id = FrameLandCreative.Color(itemMeta.getLore().get(0)).substring(8);
/* 182 */             tpWorld.to(player, id);
/*     */           } 
/* 184 */         } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {}
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Pages\byFilter\Newest\Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */