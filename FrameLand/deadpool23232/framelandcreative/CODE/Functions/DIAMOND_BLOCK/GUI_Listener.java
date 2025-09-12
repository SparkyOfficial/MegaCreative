/*     */ package deadpool23232.framelandcreative.CODE.Functions.DIAMOND_BLOCK;
/*     */ 
/*     */ import deadpool23232.framelandcreative.CODE.Blocks.Sign;
/*     */ import deadpool23232.framelandcreative.CODE.Functions.OpenBySign;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.inventory.InventoryClickEvent;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ 
/*     */ public class GUI_Listener
/*     */   implements Listener
/*     */ {
/*     */   @EventHandler
/*     */   public void onMenuClick(InventoryClickEvent event) {
/*  23 */     if (event.getInventory().getTitle().equalsIgnoreCase(FrameLandCreative.Color("&bСобытие игрока"))) {
/*  24 */       event.setCancelled(true);
/*  25 */       if (event.getCurrentItem() == null) {
/*     */         return;
/*     */       }
/*  28 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*     */         return;
/*     */       }
/*  31 */       ItemStack item = event.getCurrentItem();
/*  32 */       ItemMeta itemMeta = item.getItemMeta();
/*  33 */       String itemName = itemMeta.getDisplayName();
/*  34 */       Player player = (Player)event.getWhoClicked();
/*  35 */       if (itemName == null) {
/*     */         return;
/*     */       }
/*     */       
/*  39 */       if (itemName.equals(FrameLandCreative.Color("&fВернутся"))) {
/*  40 */         GUI.firstGUI(player);
/*     */       }
/*     */       
/*  43 */       List<String> sign = new ArrayList<>();
/*     */ 
/*     */       
/*  46 */       if (itemName.equals(FrameLandCreative.Color("&aВзаимодействие с миром"))) {
/*  47 */         GUI.worldGUI(player);
/*     */       }
/*     */       
/*  50 */       if (itemName.equals(FrameLandCreative.Color("&bВход игрока"))) {
/*  51 */         sign.add("Событие игрока");
/*  52 */         sign.add("Вход игрока");
/*  53 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  54 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  55 */         player.closeInventory();
/*  56 */       } else if (itemName.equals(FrameLandCreative.Color("&bВыход игрока"))) {
/*  57 */         sign.add("Событие игрока");
/*  58 */         sign.add("Выход игрока");
/*  59 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  60 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  61 */         player.closeInventory();
/*  62 */       } else if (itemName.equals(FrameLandCreative.Color("&bИгрок написал в чат"))) {
/*  63 */         sign.add("Событие игрока");
/*  64 */         sign.add("Событие чата");
/*  65 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  66 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  67 */         player.closeInventory();
/*  68 */       } else if (itemName.equals(FrameLandCreative.Color("&bИгрок телепортировался"))) {
/*  69 */         sign.add("Событие игрока");
/*  70 */         sign.add("Телепорт");
/*  71 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  72 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  73 */         player.closeInventory();
/*  74 */       } else if (itemName.equals(FrameLandCreative.Color("&bИгрок взял предмет"))) {
/*  75 */         sign.add("Событие игрока");
/*  76 */         sign.add("Взял предмет");
/*  77 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  78 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  79 */         player.closeInventory();
/*  80 */       } else if (itemName.equals(FrameLandCreative.Color("&bИгрок выбросил предмет"))) {
/*  81 */         sign.add("Событие игрока");
/*  82 */         sign.add("Выбросил предмет");
/*  83 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  84 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  85 */         player.closeInventory();
/*     */       } 
/*     */ 
/*     */       
/*  89 */       if (itemName.equals(FrameLandCreative.Color("&aВзаимодействие с блоком"))) {
/*  90 */         GUI.blockGUI(player);
/*     */       }
/*     */       
/*  93 */       if (itemName.equals(FrameLandCreative.Color("&aИгрок разрушил блок"))) {
/*  94 */         sign.add("Событие игрока");
/*  95 */         sign.add("Разрушил блок");
/*  96 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/*  97 */         Sign.configSign(signLoc, player.getWorld(), sign);
/*  98 */         player.closeInventory();
/*  99 */       } else if (itemName.equals(FrameLandCreative.Color("&aИгрок поставил блок"))) {
/* 100 */         sign.add("Событие игрока");
/* 101 */         sign.add("Поставил блок");
/* 102 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 103 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 104 */         player.closeInventory();
/*     */       } 
/*     */ 
/*     */       
/* 108 */       if (itemName.equals(FrameLandCreative.Color("&aСостояние игрока"))) {
/* 109 */         GUI.playerGUI(player);
/*     */       }
/*     */       
/* 112 */       if (itemName.equals(FrameLandCreative.Color("&eИгрок передвигается"))) {
/* 113 */         sign.add("Событие игрока");
/* 114 */         sign.add("Передвижение");
/* 115 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 116 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 117 */         player.closeInventory();
/* 118 */       } else if (itemName.equals(FrameLandCreative.Color("&eЛевый клик мыши"))) {
/* 119 */         sign.add("Событие игрока");
/* 120 */         sign.add("Левый клик");
/* 121 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 122 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 123 */         player.closeInventory();
/* 124 */       } else if (itemName.equals(FrameLandCreative.Color("&eПравый клик мыши"))) {
/* 125 */         sign.add("Событие игрока");
/* 126 */         sign.add("Правый клик");
/* 127 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 128 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 129 */         player.closeInventory();
/* 130 */       } else if (itemName.equals(FrameLandCreative.Color("&eИгрок открыл инвентарь"))) {
/* 131 */         sign.add("Событие игрока");
/* 132 */         sign.add("Откр. инвентарь");
/* 133 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 134 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 135 */         player.closeInventory();
/* 136 */       } else if (itemName.equals(FrameLandCreative.Color("&eИгрок закрыл инвентарь"))) {
/* 137 */         sign.add("Событие игрока");
/* 138 */         sign.add("Закр. инвентарь");
/* 139 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 140 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 141 */         player.closeInventory();
/* 142 */       } else if (itemName.equals(FrameLandCreative.Color("&eИгрок кликнул по инвентарю"))) {
/* 143 */         sign.add("Событие игрока");
/* 144 */         sign.add("Клик инвентарь");
/* 145 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 146 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 147 */         player.closeInventory();
/* 148 */       } else if (itemName.equals(FrameLandCreative.Color("&eИгрок меняет слот"))) {
/* 149 */         sign.add("Событие игрока");
/* 150 */         sign.add("Смена слота");
/* 151 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 152 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 153 */         player.closeInventory();
/*     */       } 
/*     */ 
/*     */       
/* 157 */       if (itemName.equals(FrameLandCreative.Color("&aУрон сущности"))) {
/* 158 */         GUI.damageGUI(player);
/*     */       }
/*     */       
/* 161 */       if (itemName.equals(FrameLandCreative.Color("&cИгрок убил игрока"))) {
/* 162 */         sign.add("Событие игрока");
/* 163 */         sign.add("Игрок убил игрока");
/* 164 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 165 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 166 */         player.closeInventory();
/* 167 */       } else if (itemName.equals(FrameLandCreative.Color("&cИгрок убил моба"))) {
/* 168 */         sign.add("Событие игрока");
/* 169 */         sign.add("Игрок убил моба");
/* 170 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 171 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 172 */         player.closeInventory();
/* 173 */       } else if (itemName.equals(FrameLandCreative.Color("&cИгрок умер"))) {
/* 174 */         sign.add("Событие игрока");
/* 175 */         sign.add("Игрок умер");
/* 176 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 177 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 178 */         player.closeInventory();
/* 179 */       } else if (itemName.equals(FrameLandCreative.Color("&cСущность умерла"))) {
/* 180 */         sign.add("Событие игрока");
/* 181 */         sign.add("Сущность умерла");
/* 182 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 183 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 184 */         player.closeInventory();
/* 185 */       } else if (itemName.equals(FrameLandCreative.Color("&cИгрок ударил игрока"))) {
/* 186 */         sign.add("Событие игрока");
/* 187 */         sign.add("Игрок урон игроку");
/* 188 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 189 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 190 */         player.closeInventory();
/* 191 */       } else if (itemName.equals(FrameLandCreative.Color("&cИгрок ударил моба"))) {
/* 192 */         sign.add("Событие игрока");
/* 193 */         sign.add("Игрок урон мобу");
/* 194 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 195 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 196 */         player.closeInventory();
/* 197 */       } else if (itemName.equals(FrameLandCreative.Color("&cМоб ударил игрока"))) {
/* 198 */         sign.add("Событие игрока");
/* 199 */         sign.add("Моб урон игроку");
/* 200 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 201 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 202 */         player.closeInventory();
/* 203 */       } else if (itemName.equals(FrameLandCreative.Color("&cСущность ударила сущность"))) {
/* 204 */         sign.add("Событие игрока");
/* 205 */         sign.add("Сущ. урон сущ.");
/* 206 */         Location signLoc = (Location)OpenBySign.blockMap.get(player);
/* 207 */         Sign.configSign(signLoc, player.getWorld(), sign);
/* 208 */         player.closeInventory();
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Functions\DIAMOND_BLOCK\GUI_Listener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */