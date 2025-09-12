/*     */ package deadpool23232.framelandcreative.CODE.Functions;
/*     */ 
/*     */ import deadpool23232.framelandcreative.CODE.Blocks.Sign;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.inventory.InventoryClickEvent;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ItemClick
/*     */   implements Listener
/*     */ {
/*     */   @EventHandler
/*     */   public void a(InventoryClickEvent event) {
/*  24 */     if (event.getWhoClicked().getWorld().getName().contains("-code")) {
/*  25 */       if (event.getCurrentItem() == null)
/*  26 */         return;  if (event.getCurrentItem().getType() == Material.AIR)
/*  27 */         return;  if (event.getClickedInventory() == null)
/*     */         return; 
/*  29 */       Player player = (Player)event.getWhoClicked();
/*  30 */       ItemStack item = event.getCurrentItem();
/*  31 */       Inventory inventory = event.getClickedInventory();
/*  32 */       if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
/*  33 */         ItemMeta itemMeta = item.getItemMeta();
/*  34 */         String name = itemMeta.getDisplayName();
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*  39 */         if (item.getType().equals(Material.STAINED_GLASS_PANE)) {
/*  40 */           if (name.equals(FrameLandCreative.Color("&7")) && ((String)item
/*  41 */             .getItemMeta().getLore().get(0)).equals(FrameLandCreative.Color("&7"))) {
/*  42 */             event.setCancelled(true);
/*  43 */           } else if (name.equals(FrameLandCreative.Color("&aКоординаты сундука"))) {
/*  44 */             event.setCancelled(true);
/*  45 */           } else if (name.equals(FrameLandCreative.Color("&3Число устанавливаемого здоровья"))) {
/*  46 */             event.setCancelled(true);
/*  47 */           } else if (name.equals(FrameLandCreative.Color("&aКоординаты телепортации"))) {
/*  48 */             event.setCancelled(true);
/*  49 */           } else if (name.equals(FrameLandCreative.Color("&3Предмет"))) {
/*  50 */             event.setCancelled(true);
/*  51 */           } else if (name.equals(FrameLandCreative.Color("&aКоординаты блока"))) {
/*  52 */             event.setCancelled(true);
/*  53 */           } else if (name.equals(FrameLandCreative.Color("&6Тип блока"))) {
/*  54 */             event.setCancelled(true);
/*  55 */           } else if (name.equals(FrameLandCreative.Color("&3Число задержки &o(20 = 1сек)"))) {
/*  56 */             event.setCancelled(true);
/*  57 */           } else if (name.equals(FrameLandCreative.Color("&bНазвание функции"))) {
/*  58 */             event.setCancelled(true);
/*     */           } 
/*     */         }
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*  65 */         if ((item.getType() == Material.ENDER_CHEST || item
/*  66 */           .getType() == Material.CHEST) && (
/*  67 */           name.contains("Копия") || name.contains("Оригинал"))) {
/*  68 */           event.setCancelled(true);
/*  69 */           ItemStack mode = new ItemStack(Material.ENDER_CHEST);
/*  70 */           ItemMeta modeMeta = mode.getItemMeta();
/*  71 */           List<String> lore = new ArrayList<>();
/*  72 */           lore.add(FrameLandCreative.Color("&7Установить тип сундука:"));
/*  73 */           lore.add(FrameLandCreative.Color(""));
/*  74 */           if (name.equals(FrameLandCreative.Color("&eКопия")))
/*  75 */           { mode.setType(Material.CHEST);
/*  76 */             modeMeta.setDisplayName(FrameLandCreative.Color("&eОригинал"));
/*  77 */             lore.add(FrameLandCreative.Color("&8Копия"));
/*  78 */             lore.add(FrameLandCreative.Color("&aОригинал")); }
/*  79 */           else if (name.equals(FrameLandCreative.Color("&eОригинал")))
/*  80 */           { mode.setType(Material.ENDER_CHEST);
/*  81 */             modeMeta.setDisplayName(FrameLandCreative.Color("&eКопия"));
/*  82 */             lore.add(FrameLandCreative.Color("&aКопия"));
/*  83 */             lore.add(FrameLandCreative.Color("&8Оригинал")); }
/*     */           else { return; }
/*  85 */            lore.add(FrameLandCreative.Color(""));
/*  86 */           modeMeta.setLore(lore);
/*  87 */           mode.setItemMeta(modeMeta);
/*  88 */           inventory.setItem(22, mode);
/*     */         } 
/*     */         
/*  91 */         if ((item.getType() == Material.WORKBENCH || item
/*  92 */           .getType() == Material.ENDER_PEARL || item
/*  93 */           .getType() == Material.GRASS || item
/*  94 */           .getType() == Material.STRUCTURE_VOID) && (
/*  95 */           name.contains("Креатив") || name.contains("Выживание") || name
/*  96 */           .contains("Приключение") || name.contains("Спектральный"))) {
/*  97 */           event.setCancelled(true);
/*  98 */           ItemStack mode = new ItemStack(Material.WORKBENCH);
/*  99 */           ItemMeta modeMeta = mode.getItemMeta();
/* 100 */           List<String> lore = new ArrayList<>();
/* 101 */           lore.add(FrameLandCreative.Color("&7Установить режим игры:"));
/* 102 */           lore.add(FrameLandCreative.Color(""));
/* 103 */           if (name.equals(FrameLandCreative.Color("&eКреатив")))
/* 104 */           { mode.setType(Material.GRASS);
/* 105 */             modeMeta.setDisplayName(FrameLandCreative.Color("&eВыживание"));
/* 106 */             lore.add(FrameLandCreative.Color("&8Креатив"));
/* 107 */             lore.add(FrameLandCreative.Color("&aВыживание"));
/* 108 */             lore.add(FrameLandCreative.Color("&8Спектральный"));
/* 109 */             lore.add(FrameLandCreative.Color("&8Приключение")); }
/* 110 */           else if (name.equals(FrameLandCreative.Color("&eВыживание")))
/* 111 */           { mode.setType(Material.STRUCTURE_VOID);
/* 112 */             modeMeta.setDisplayName(FrameLandCreative.Color("&eСпектральный"));
/* 113 */             lore.add(FrameLandCreative.Color("&8Креатив"));
/* 114 */             lore.add(FrameLandCreative.Color("&8Выживание"));
/* 115 */             lore.add(FrameLandCreative.Color("&aСпектральный"));
/* 116 */             lore.add(FrameLandCreative.Color("&8Приключение")); }
/* 117 */           else if (name.equals(FrameLandCreative.Color("&eСпектральный")))
/* 118 */           { mode.setType(Material.ENDER_PEARL);
/* 119 */             modeMeta.setDisplayName(FrameLandCreative.Color("&eПриключение"));
/* 120 */             lore.add(FrameLandCreative.Color("&8Креатив"));
/* 121 */             lore.add(FrameLandCreative.Color("&8Выживание"));
/* 122 */             lore.add(FrameLandCreative.Color("&8Спектральный"));
/* 123 */             lore.add(FrameLandCreative.Color("&aПриключение")); }
/* 124 */           else if (name.equals(FrameLandCreative.Color("&eПриключение")))
/* 125 */           { mode.setType(Material.WORKBENCH);
/* 126 */             modeMeta.setDisplayName(FrameLandCreative.Color("&eКреатив"));
/* 127 */             lore.add(FrameLandCreative.Color("&aКреатив"));
/* 128 */             lore.add(FrameLandCreative.Color("&8Выживание"));
/* 129 */             lore.add(FrameLandCreative.Color("&8Спектральный"));
/* 130 */             lore.add(FrameLandCreative.Color("&8Приключение")); }
/*     */           else { return; }
/* 132 */            lore.add(FrameLandCreative.Color(""));
/* 133 */           modeMeta.setLore(lore);
/* 134 */           mode.setItemMeta(modeMeta);
/* 135 */           inventory.setItem(13, mode);
/*     */         } 
/*     */         
/* 138 */         if ((item.getType() == Material.STRUCTURE_VOID || item
/* 139 */           .getType() == Material.BARRIER) && (
/* 140 */           name.contains("Синхронно") || name.contains("Асинхронно"))) {
/* 141 */           event.setCancelled(true);
/* 142 */           List<String> sign = new ArrayList<>();
/* 143 */           sign.add("Игровое действие");
/* 144 */           sign.add("Исп. функцию");
/* 145 */           ItemStack modeFunc = new ItemStack(Material.STRUCTURE_VOID);
/* 146 */           ItemMeta modeMeta = modeFunc.getItemMeta();
/* 147 */           List<String> modeLore = new ArrayList<>();
/* 148 */           modeLore.add(FrameLandCreative.Color("&7Нажмите, чтобы переключить функцию:"));
/* 149 */           modeLore.add(FrameLandCreative.Color(""));
/* 150 */           if (name.equals(FrameLandCreative.Color("&aСинхронно")))
/* 151 */           { sign.add(FrameLandCreative.Color("&c&lАсинхронно"));
/* 152 */             modeFunc.setType(Material.BARRIER);
/* 153 */             modeMeta.setDisplayName(FrameLandCreative.Color("&cАсинхронно"));
/* 154 */             modeLore.add(FrameLandCreative.Color("&8Синхронно"));
/* 155 */             modeLore.add(FrameLandCreative.Color("&aАсинхронно")); }
/* 156 */           else if (name.equals(FrameLandCreative.Color("&cАсинхронно")))
/* 157 */           { sign.add(FrameLandCreative.Color("&a&lСинхронно"));
/* 158 */             modeFunc.setType(Material.STRUCTURE_VOID);
/* 159 */             modeMeta.setDisplayName(FrameLandCreative.Color("&aСинхронно"));
/* 160 */             modeLore.add(FrameLandCreative.Color("&aСинхронно"));
/* 161 */             modeLore.add(FrameLandCreative.Color("&8Асинхронно")); }
/*     */           else { return; }
/* 163 */            Location signLoc = OnChestOpen.signLocation.get(player);
/* 164 */           modeLore.add(FrameLandCreative.Color(""));
/* 165 */           modeMeta.setLore(modeLore);
/* 166 */           modeFunc.setItemMeta(modeMeta);
/* 167 */           inventory.setItem(22, modeFunc);
/* 168 */           Sign.configSign(signLoc, player.getWorld(), sign);
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\ItemClick.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */