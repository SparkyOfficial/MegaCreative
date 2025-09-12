/*      */ package deadpool23232.framelandcreative.CODE.CodeCompiler;
/*      */ 
/*      */ import deadpool23232.framelandcreative.FrameLandCreative;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.List;
/*      */ import java.util.Objects;
/*      */ import org.bukkit.Material;
/*      */ import org.bukkit.block.Block;
/*      */ import org.bukkit.block.Chest;
/*      */ import org.bukkit.block.Sign;
/*      */ import org.bukkit.configuration.file.FileConfiguration;
/*      */ import org.bukkit.inventory.Inventory;
/*      */ import org.bukkit.inventory.ItemStack;
/*      */ 
/*      */ 
/*      */ 
/*      */ public class GetFunc_new
/*      */ {
/*   20 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*      */   
/*      */   public static String diamond(String func) {
/*   23 */     String result = null;
/*   24 */     switch (func) {
/*      */       case "Вход игрока":
/*   26 */         result = "joinEvent";
/*      */         break;
/*      */       case "Выход игрока":
/*   29 */         result = "quitEvent";
/*      */         break;
/*      */       case "Разрушил блок":
/*   32 */         result = "breakEvent";
/*      */         break;
/*      */       case "Поставил блок":
/*   35 */         result = "placeEvent";
/*      */         break;
/*      */       case "Передвижение":
/*   38 */         result = "moveEvent";
/*      */         break;
/*      */       case "Левый клик":
/*   41 */         result = "LMBEvent";
/*      */         break;
/*      */       case "Правый клик":
/*   44 */         result = "RMBEvent";
/*      */         break;
/*      */       case "Событие чата":
/*   47 */         result = "messageEvent";
/*      */         break;
/*      */       case "Сущность умерла":
/*   50 */         result = "mobDeathEvent";
/*      */         break;
/*      */       case "Игрок умер":
/*   53 */         result = "playerDeathEvent";
/*      */         break;
/*      */       case "Игрок убил игрока":
/*   56 */         result = "plKillPlEvent";
/*      */         break;
/*      */       case "Игрок убил моба":
/*   59 */         result = "plKillMobEvent";
/*      */         break;
/*      */       case "Игрок урон игроку":
/*   62 */         result = "plDmgPlEvent";
/*      */         break;
/*      */       case "Моб урон игроку":
/*   65 */         result = "mobDmgPlEvent";
/*      */         break;
/*      */       case "Игрок урон мобу":
/*   68 */         result = "plDmgMobEvent";
/*      */         break;
/*      */       case "Откр. инвентарь":
/*   71 */         result = "invOpenEvent";
/*      */         break;
/*      */       case "Закр. инвентарь":
/*   74 */         result = "invCloseEvent";
/*      */         break;
/*      */       case "Клик инвентарь":
/*   77 */         result = "invClickEvent";
/*      */         break;
/*      */       case "Телепорт":
/*   80 */         result = "teleportEvent";
/*      */         break;
/*      */       case "Выбросил предмет":
/*   83 */         result = "itemDropEvent";
/*      */         break;
/*      */       case "Взял предмет":
/*   86 */         result = "itemPickupEvent";
/*      */         break;
/*      */       case "Сущ. урон сущ.":
/*   89 */         result = "mobDmgMobEvent";
/*      */         break;
/*      */       case "Смена слота":
/*   92 */         result = "slotChangeEvent";
/*      */         break;
/*      */     } 
/*   95 */     return result; } public static String cobblestone(String func, Inventory chestInventory, String switchPlayer) { List<String> list4, list3, list2, list;
/*      */     int i;
/*      */     String line;
/*      */     List<String> list1;
/*      */     int j;
/*  100 */     String line1, result = null;
/*  101 */     switch (func) {
/*      */       case "Рандом предмет":
/*  103 */         list4 = new ArrayList<>();
/*  104 */         if (chestInventory != null) {
/*  105 */           for (int k = 0; k < (chestInventory.getContents()).length; k++) {
/*  106 */             if (chestInventory.getItem(k) != null) {
/*  107 */               if (chestInventory.getItem(k).hasItemMeta() && chestInventory
/*  108 */                 .getItem(k).getItemMeta().hasDisplayName() && chestInventory
/*  109 */                 .getItem(k).getType() == Material.GOLDEN_APPLE) {
/*  110 */                 String name = chestInventory.getItem(k).getItemMeta().getDisplayName();
/*  111 */                 if (name.equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/*  112 */                   name = "apple[main_hand_item]~";
/*  113 */                   list4.add(name);
/*  114 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/*  115 */                   name = "apple[look_block]~";
/*  116 */                   list4.add(name);
/*  117 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/*  118 */                   name = "apple[off_hand_item]~";
/*  119 */                   list4.add(name);
/*  120 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/*  121 */                   name = "apple[click_item]~";
/*  122 */                   list4.add(name);
/*  123 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/*  124 */                   name = "apple[event_block]~";
/*  125 */                   list4.add(name);
/*      */                 } else {
/*  127 */                   name = "default";
/*  128 */                   String lore = "none";
/*  129 */                   if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasDisplayName()) {
/*  130 */                     name = chestInventory.getItem(k).getItemMeta().getDisplayName();
/*      */                   }
/*  132 */                   if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasLore()) {
/*  133 */                     List<String> loreList = chestInventory.getItem(k).getItemMeta().getLore();
/*  134 */                     lore = String.join("\\n", (Iterable)loreList);
/*      */                   } 
/*  136 */                   String id = Integer.toString(chestInventory.getItem(k).getType().getId());
/*  137 */                   String amount = Integer.toString(chestInventory.getItem(k).getAmount());
/*  138 */                   list4.add("item[-" + id + "*" + name + "-][=" + amount + "=][+" + lore + "+]");
/*      */                 } 
/*      */               } else {
/*  141 */                 String name = "default";
/*  142 */                 String lore = "none";
/*  143 */                 if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasDisplayName()) {
/*  144 */                   name = chestInventory.getItem(k).getItemMeta().getDisplayName();
/*      */                 }
/*  146 */                 if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasLore()) {
/*  147 */                   List<String> loreList = chestInventory.getItem(k).getItemMeta().getLore();
/*  148 */                   lore = String.join("\\n", (Iterable)loreList);
/*      */                 } 
/*  150 */                 String id = Integer.toString(chestInventory.getItem(k).getType().getId());
/*  151 */                 String amount = Integer.toString(chestInventory.getItem(k).getAmount());
/*  152 */                 list4.add("item[-" + id + "*" + name + "-][=" + amount + "=][+" + lore + "+]");
/*      */               } 
/*      */             }
/*      */           } 
/*      */         }
/*  157 */         if (!list4.isEmpty()) {
/*  158 */           String line4 = String.join("|", (Iterable)list4);
/*  159 */           result = "giveRandItem%_" + switchPlayer + "_%(" + line4 + ")";
/*      */         } 
/*      */         break;
/*      */       case "Удалить предм.":
/*  163 */         list3 = new ArrayList<>();
/*  164 */         if (chestInventory != null) {
/*  165 */           for (int k = 0; k < (chestInventory.getContents()).length; k++) {
/*  166 */             if (chestInventory.getItem(k) != null) {
/*  167 */               if (chestInventory.getItem(k).hasItemMeta() && chestInventory
/*  168 */                 .getItem(k).getItemMeta().hasDisplayName() && chestInventory
/*  169 */                 .getItem(k).getType() == Material.GOLDEN_APPLE) {
/*  170 */                 String name = chestInventory.getItem(k).getItemMeta().getDisplayName();
/*  171 */                 if (name.equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/*  172 */                   name = "apple[main_hand_item]~";
/*  173 */                   list3.add(name);
/*  174 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/*  175 */                   name = "apple[look_block]~";
/*  176 */                   list3.add(name);
/*  177 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/*  178 */                   name = "apple[off_hand_item]~";
/*  179 */                   list3.add(name);
/*  180 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/*  181 */                   name = "apple[click_item]~";
/*  182 */                   list3.add(name);
/*  183 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/*  184 */                   name = "apple[event_block]~";
/*  185 */                   list3.add(name);
/*      */                 } else {
/*  187 */                   name = "default";
/*  188 */                   String lore = "none";
/*  189 */                   if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasDisplayName()) {
/*  190 */                     name = chestInventory.getItem(k).getItemMeta().getDisplayName();
/*      */                   }
/*  192 */                   if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasLore()) {
/*  193 */                     List<String> loreList = chestInventory.getItem(k).getItemMeta().getLore();
/*  194 */                     lore = String.join("\\n", (Iterable)loreList);
/*      */                   } 
/*  196 */                   String id = Integer.toString(chestInventory.getItem(k).getType().getId());
/*  197 */                   String amount = Integer.toString(chestInventory.getItem(k).getAmount());
/*  198 */                   list3.add("item[-" + id + "*" + name + "-][=" + amount + "=][+" + lore + "+]");
/*      */                 } 
/*      */               } else {
/*  201 */                 String name = "default";
/*  202 */                 String lore = "none";
/*  203 */                 if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasDisplayName()) {
/*  204 */                   name = chestInventory.getItem(k).getItemMeta().getDisplayName();
/*      */                 }
/*  206 */                 if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasLore()) {
/*  207 */                   List<String> loreList = chestInventory.getItem(k).getItemMeta().getLore();
/*  208 */                   lore = String.join("\\n", (Iterable)loreList);
/*      */                 } 
/*  210 */                 String id = Integer.toString(chestInventory.getItem(k).getType().getId());
/*  211 */                 String amount = Integer.toString(chestInventory.getItem(k).getAmount());
/*  212 */                 list3.add("item[-" + id + "*" + name + "-][=" + amount + "=][+" + lore + "+]");
/*      */               } 
/*      */             }
/*      */           } 
/*      */         }
/*  217 */         if (!list3.isEmpty()) {
/*  218 */           String line3 = String.join("|", (Iterable)list3);
/*  219 */           result = "deleteItems%_" + switchPlayer + "_%(" + line3 + ")";
/*      */         } 
/*      */         break;
/*      */       case "Выдать предметы":
/*  223 */         list2 = new ArrayList<>();
/*  224 */         if (chestInventory != null) {
/*  225 */           for (int k = 0; k < (chestInventory.getContents()).length; k++) {
/*  226 */             if (chestInventory.getItem(k) != null) {
/*  227 */               if (chestInventory.getItem(k).hasItemMeta() && chestInventory
/*  228 */                 .getItem(k).getItemMeta().hasDisplayName() && chestInventory
/*  229 */                 .getItem(k).getType() == Material.GOLDEN_APPLE) {
/*  230 */                 String name = chestInventory.getItem(k).getItemMeta().getDisplayName();
/*  231 */                 if (name.equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/*  232 */                   name = "apple[main_hand_item]~";
/*  233 */                   list2.add(name);
/*  234 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/*  235 */                   name = "apple[look_block]~";
/*  236 */                   list2.add(name);
/*  237 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/*  238 */                   name = "apple[off_hand_item]~";
/*  239 */                   list2.add(name);
/*  240 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/*  241 */                   name = "apple[click_item]~";
/*  242 */                   list2.add(name);
/*  243 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/*  244 */                   name = "apple[event_block]~";
/*  245 */                   list2.add(name);
/*      */                 } else {
/*  247 */                   name = "default";
/*  248 */                   String lore = "none";
/*  249 */                   if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasDisplayName()) {
/*  250 */                     name = chestInventory.getItem(k).getItemMeta().getDisplayName();
/*      */                   }
/*  252 */                   if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasLore()) {
/*  253 */                     List<String> loreList = chestInventory.getItem(k).getItemMeta().getLore();
/*  254 */                     lore = String.join("\\n", (Iterable)loreList);
/*      */                   } 
/*  256 */                   String id = Integer.toString(chestInventory.getItem(k).getType().getId());
/*  257 */                   String amount = Integer.toString(chestInventory.getItem(k).getAmount());
/*  258 */                   list2.add("item[-" + id + "*" + name + "-][=" + amount + "=][+" + lore + "+]");
/*      */                 } 
/*      */               } else {
/*  261 */                 String name = "default";
/*  262 */                 String lore = "none";
/*  263 */                 if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasDisplayName()) {
/*  264 */                   name = chestInventory.getItem(k).getItemMeta().getDisplayName();
/*      */                 }
/*  266 */                 if (chestInventory.getItem(k).hasItemMeta() && chestInventory.getItem(k).getItemMeta().hasLore()) {
/*  267 */                   List<String> loreList = chestInventory.getItem(k).getItemMeta().getLore();
/*  268 */                   lore = String.join("\\n", (Iterable)loreList);
/*      */                 } 
/*  270 */                 String id = Integer.toString(chestInventory.getItem(k).getType().getId());
/*  271 */                 String amount = Integer.toString(chestInventory.getItem(k).getAmount());
/*  272 */                 list2.add("item[-" + id + "*" + name + "-][=" + amount + "=][+" + lore + "+]");
/*      */               } 
/*      */             }
/*      */           } 
/*      */         }
/*  277 */         if (!list2.isEmpty()) {
/*  278 */           String line2 = String.join("|", (Iterable)list2);
/*  279 */           result = "giveItems%_" + switchPlayer + "_%(" + line2 + ")";
/*      */         } 
/*      */         break;
/*      */       case "Очистить инв.":
/*  283 */         result = "clearInventory%_" + switchPlayer + "_%";
/*      */         break;
/*      */       case "Закр. инвентарь":
/*  286 */         result = "closeInventory%_" + switchPlayer + "_%";
/*      */         break;
/*      */       case "Откр. инвентарь":
/*  289 */         assert chestInventory != null;
/*  290 */         if (chestInventory.getItem(13) != null && 
/*  291 */           chestInventory.getItem(13).hasItemMeta()) {
/*  292 */           String coordsall = chestInventory.getItem(13).getItemMeta().getDisplayName();
/*  293 */           String type = chestInventory.getItem(22).getItemMeta().getDisplayName();
/*  294 */           List<String> coords5 = Arrays.asList(coordsall.split("\\|"));
/*  295 */           String chestCoords = "";
/*  296 */           if (chestInventory.getItem(13).getType() == Material.GOLDEN_APPLE) {
/*  297 */             if (coordsall.equals(FrameLandCreative.Color(config.getString("apple.n.name")))) {
/*  298 */               chestCoords = "apple[location]~";
/*  299 */             } else if (coordsall.equals(FrameLandCreative.Color(config.getString("apple.p.name")))) {
/*  300 */               chestCoords = "apple[look_block_loc]~";
/*  301 */             } else if (coordsall.equals(FrameLandCreative.Color(config.getString("apple.x.name")))) {
/*  302 */               chestCoords = "apple[block_loc]~";
/*      */             }
/*      */           
/*  305 */           } else if (coords5.size() >= 3) {
/*      */             try {
/*  307 */               String xStr = ((String)coords5.get(0)).substring(2).trim();
/*  308 */               String[] xList = xStr.split("\\.");
/*  309 */               int x = Integer.parseInt(xList[0]);
/*      */               
/*  311 */               String yStr = ((String)coords5.get(1)).trim();
/*  312 */               String[] yList = yStr.split("\\.");
/*  313 */               int y = Integer.parseInt(yList[0]);
/*      */               
/*  315 */               String zStr = ((String)coords5.get(2)).trim();
/*  316 */               String[] zList = zStr.split("\\.");
/*  317 */               int z = Integer.parseInt(zList[0]);
/*  318 */               chestCoords = x + "|" + y + "|" + z;
/*  319 */             } catch (Exception exception) {}
/*      */           } 
/*      */ 
/*      */ 
/*      */           
/*  324 */           if (type.contains("Копия")) {
/*  325 */             result = "openInventory%_" + switchPlayer + "_%[copy](" + chestCoords + ")"; break;
/*  326 */           }  if (type.contains("Оригинал")) {
/*  327 */             result = "openInventory%_" + switchPlayer + "_%[original](" + chestCoords + ")";
/*      */           }
/*      */         } 
/*      */         break;
/*      */       
/*      */       case "Сообщение игр.":
/*  333 */         list = new ArrayList<>();
/*  334 */         for (i = 0; i < (((Inventory)Objects.requireNonNull((T)chestInventory)).getContents()).length; i++) {
/*  335 */           if (chestInventory.getItem(i) != null && chestInventory.getItem(i).hasItemMeta() && chestInventory
/*  336 */             .getItem(i).getItemMeta().hasDisplayName()) {
/*  337 */             ItemStack item = chestInventory.getItem(i);
/*  338 */             String name = item.getItemMeta().getDisplayName();
/*  339 */             if (item.getType() == Material.GOLDEN_APPLE) {
/*  340 */               if (name.equals(FrameLandCreative.Color(config.getString("apple.d.name")))) {
/*  341 */                 name = "apple[health_now]~";
/*  342 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.e.name")))) {
/*  343 */                 name = "apple[health_max]~";
/*  344 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.f.name")))) {
/*  345 */                 name = "apple[hunger]~";
/*  346 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.g.name")))) {
/*  347 */                 name = "apple[satiety]~";
/*  348 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.h.name")))) {
/*  349 */                 name = "apple[xp]~";
/*  350 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.i.name")))) {
/*  351 */                 name = "apple[armor]~";
/*  352 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.j.name")))) {
/*  353 */                 name = "apple[air]~";
/*  354 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.k.name")))) {
/*  355 */                 name = "apple[slot_now]~";
/*  356 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.l.name")))) {
/*  357 */                 name = "apple[ping]~";
/*  358 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.n.name")))) {
/*  359 */                 name = "apple[location]~";
/*  360 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.o.name")))) {
/*  361 */                 name = "apple[inventory_name]~";
/*  362 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.p.name")))) {
/*  363 */                 name = "apple[look_block_loc]~";
/*  364 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.s.name")))) {
/*  365 */                 name = "apple[damage]~";
/*  366 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.t.name")))) {
/*  367 */                 name = "apple[click_slot]~";
/*  368 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.u.name")))) {
/*  369 */                 name = "apple[new_slot]~";
/*  370 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.v.name")))) {
/*  371 */                 name = "apple[old_slot]~";
/*  372 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.w.name")))) {
/*  373 */                 name = "apple[message]~";
/*  374 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.x.name")))) {
/*  375 */                 name = "apple[block_loc]~";
/*  376 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.aa.name")))) {
/*  377 */                 name = "apple[player_count]~";
/*  378 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ab.name")))) {
/*  379 */                 name = "apple[like_count]~";
/*  380 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ac.name")))) {
/*  381 */                 name = "apple[unique_count]~";
/*  382 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ad.name")))) {
/*  383 */                 name = "apple[world_id]~";
/*      */               } 
/*  385 */               list.add(name);
/*      */             } else {
/*  387 */               list.add(item.getItemMeta().getDisplayName());
/*      */             } 
/*      */           } 
/*      */         } 
/*  391 */         line = String.join(" ", (Iterable)list);
/*  392 */         if (chestInventory.getSize() > 0) {
/*  393 */           result = "playerMessage%_" + switchPlayer + "_%~(" + line + ")~";
/*      */         }
/*      */         break;
/*      */       case "Мир. сообщение":
/*  397 */         list1 = new ArrayList<>();
/*  398 */         for (j = 0; j < (((Inventory)Objects.requireNonNull((T)chestInventory)).getContents()).length; j++) {
/*  399 */           if (chestInventory.getItem(j) != null && chestInventory.getItem(j).hasItemMeta() && chestInventory
/*  400 */             .getItem(j).getItemMeta().hasDisplayName()) {
/*  401 */             ItemStack item = chestInventory.getItem(j);
/*  402 */             String name = item.getItemMeta().getDisplayName();
/*  403 */             if (item.getType() == Material.GOLDEN_APPLE) {
/*  404 */               if (name.equals(FrameLandCreative.Color(config.getString("apple.d.name")))) {
/*  405 */                 name = "apple[health_now]~";
/*  406 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.e.name")))) {
/*  407 */                 name = "apple[health_max]~";
/*  408 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.f.name")))) {
/*  409 */                 name = "apple[hunger]~";
/*  410 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.g.name")))) {
/*  411 */                 name = "apple[satiety]~";
/*  412 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.h.name")))) {
/*  413 */                 name = "apple[xp]~";
/*  414 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.i.name")))) {
/*  415 */                 name = "apple[armor]~";
/*  416 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.j.name")))) {
/*  417 */                 name = "apple[air]~";
/*  418 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.k.name")))) {
/*  419 */                 name = "apple[slot_now]~";
/*  420 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.l.name")))) {
/*  421 */                 name = "apple[ping]~";
/*  422 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.n.name")))) {
/*  423 */                 name = "apple[location]~";
/*  424 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.o.name")))) {
/*  425 */                 name = "apple[inventory_name]~";
/*  426 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.p.name")))) {
/*  427 */                 name = "apple[look_block_loc]~";
/*  428 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.s.name")))) {
/*  429 */                 name = "apple[damage]~";
/*  430 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.t.name")))) {
/*  431 */                 name = "apple[click_slot]~";
/*  432 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.u.name")))) {
/*  433 */                 name = "apple[new_slot]~";
/*  434 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.v.name")))) {
/*  435 */                 name = "apple[old_slot]~";
/*  436 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.w.name")))) {
/*  437 */                 name = "apple[message]~";
/*  438 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.x.name")))) {
/*  439 */                 name = "apple[block_loc]~";
/*  440 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.aa.name")))) {
/*  441 */                 name = "apple[player_count]~";
/*  442 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ab.name")))) {
/*  443 */                 name = "apple[like_count]~";
/*  444 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ac.name")))) {
/*  445 */                 name = "apple[unique_count]~";
/*  446 */               } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ad.name")))) {
/*  447 */                 name = "apple[world_id]~";
/*      */               } 
/*  449 */               list1.add(name);
/*      */             } else {
/*  451 */               list1.add(item.getItemMeta().getDisplayName());
/*      */             } 
/*      */           } 
/*      */         } 
/*  455 */         line1 = String.join(" ", (Iterable)list1);
/*  456 */         if (chestInventory.getSize() > 0) {
/*  457 */           result = "worldMessage~(" + line1 + ")~";
/*      */         }
/*      */         break;
/*      */       case "Режим игры":
/*  461 */         assert chestInventory != null;
/*  462 */         if (chestInventory.getItem(13) != null) {
/*  463 */           String coordsall = chestInventory.getItem(13).getItemMeta().getDisplayName();
/*  464 */           if (coordsall.contains("Креатив")) {
/*  465 */             result = "gameMode%_" + switchPlayer + "_%(creative)"; break;
/*  466 */           }  if (coordsall.contains("Выживание")) {
/*  467 */             result = "gameMode%_" + switchPlayer + "_%(survival)"; break;
/*  468 */           }  if (coordsall.contains("Приключение")) {
/*  469 */             result = "gameMode%_" + switchPlayer + "_%(adventure)"; break;
/*  470 */           }  if (coordsall.contains("Спектральный")) {
/*  471 */             result = "gameMode%_" + switchPlayer + "_%(spectator)";
/*      */           }
/*      */         } 
/*      */         break;
/*      */       case "Уст. здоровье":
/*  476 */         assert chestInventory != null;
/*  477 */         if (chestInventory.getItem(13) != null && 
/*  478 */           chestInventory.getItem(13).hasItemMeta() && 
/*  479 */           chestInventory.getItem(13).getItemMeta().hasDisplayName()) {
/*  480 */           String amountStr = chestInventory.getItem(13).getItemMeta().getDisplayName();
/*  481 */           int amount = 1400599779;
/*      */           try {
/*  483 */             amount = Integer.parseInt(amountStr.substring(2).trim());
/*  484 */           } catch (Exception exception) {}
/*      */ 
/*      */           
/*  487 */           if (chestInventory.getItem(13).getType() == Material.GOLDEN_APPLE) {
/*  488 */             if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.d.name")))) {
/*  489 */               amountStr = "apple[health_now]~";
/*  490 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  491 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.e.name")))) {
/*  492 */               amountStr = "apple[health_max]~";
/*  493 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  494 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.f.name")))) {
/*  495 */               amountStr = "apple[hunger]~";
/*  496 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  497 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.g.name")))) {
/*  498 */               amountStr = "apple[satiety]~";
/*  499 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  500 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.h.name")))) {
/*  501 */               amountStr = "apple[xp]~";
/*  502 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  503 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.i.name")))) {
/*  504 */               amountStr = "apple[armor]~";
/*  505 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  506 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.j.name")))) {
/*  507 */               amountStr = "apple[air]~";
/*  508 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  509 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.k.name")))) {
/*  510 */               amountStr = "apple[slot_now]~";
/*  511 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  512 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.l.name")))) {
/*  513 */               amountStr = "apple[ping]~";
/*  514 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  515 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.s.name")))) {
/*  516 */               amountStr = "apple[damage]~";
/*  517 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  518 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.t.name")))) {
/*  519 */               amountStr = "apple[click_slot]~";
/*  520 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  521 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.u.name")))) {
/*  522 */               amountStr = "apple[new_slot]~";
/*  523 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  524 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.v.name")))) {
/*  525 */               amountStr = "apple[old_slot]~";
/*  526 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  527 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.aa.name")))) {
/*  528 */               amountStr = "apple[player_count]~";
/*  529 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  530 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.ab.name")))) {
/*  531 */               amountStr = "apple[like_count]~";
/*  532 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  533 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.ac.name")))) {
/*  534 */               amountStr = "apple[unique_count]~";
/*  535 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*  536 */             }  if (amountStr.equals(FrameLandCreative.Color(config.getString("apple.ad.name")))) {
/*  537 */               amountStr = "apple[world_id]~";
/*  538 */               result = "setHealth%_" + switchPlayer + "_%(" + amountStr + ")"; break;
/*      */             } 
/*  540 */             if (amount != 1400599779) {
/*  541 */               result = "setHealth%_" + switchPlayer + "_%(" + amount + ")";
/*      */             }
/*      */             break;
/*      */           } 
/*  545 */           result = "setHealth%_" + switchPlayer + "_%(" + amount + ")";
/*      */         } 
/*      */         break;
/*      */ 
/*      */ 
/*      */       
/*      */       case "Телепортация":
/*  552 */         assert chestInventory != null;
/*  553 */         if (chestInventory.getItem(13) != null && 
/*  554 */           chestInventory.getItem(13).hasItemMeta()) {
/*  555 */           String coordsall = chestInventory.getItem(13).getItemMeta().getDisplayName();
/*  556 */           List<String> coords5 = Arrays.asList(coordsall.split("\\|"));
/*      */           
/*  558 */           if (chestInventory.getItem(13).getType() == Material.GOLDEN_APPLE) {
/*  559 */             if (coordsall.equals(FrameLandCreative.Color(config.getString("apple.n.name")))) {
/*  560 */               String coords = "apple[location]~";
/*  561 */               result = "teleport%_" + switchPlayer + "_%(" + coords + ")"; break;
/*  562 */             }  if (coordsall.equals(FrameLandCreative.Color(config.getString("apple.p.name")))) {
/*  563 */               String coords = "apple[look_block_loc]~";
/*  564 */               result = "teleport%_" + switchPlayer + "_%(" + coords + ")"; break;
/*  565 */             }  if (coordsall.equals(FrameLandCreative.Color(config.getString("apple.x.name")))) {
/*  566 */               String coords = "apple[block_loc]~";
/*  567 */               result = "teleport%_" + switchPlayer + "_%(" + coords + ")";
/*      */             }  break;
/*      */           } 
/*  570 */           if (coords5.size() >= 5) {
/*      */             try {
/*  572 */               double x = Double.parseDouble(((String)coords5.get(0)).substring(2).trim());
/*  573 */               double y = Double.parseDouble(((String)coords5.get(1)).trim());
/*  574 */               double z = Double.parseDouble(((String)coords5.get(2)).trim());
/*  575 */               double yaw = Double.parseDouble(((String)coords5.get(3)).trim());
/*  576 */               double patch = Double.parseDouble(((String)coords5.get(4)).trim());
/*  577 */               String coords = x + "|" + y + "|" + z + "|" + yaw + "|" + patch;
/*  578 */               result = "teleport%_" + switchPlayer + "_%(" + coords + ")";
/*  579 */             } catch (Exception exception) {}
/*      */           }
/*      */         } 
/*      */         break;
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  588 */     return result; }
/*      */ 
/*      */ 
/*      */   
/*      */   public static String nether_brick(String func, Inventory chestInventory, String textCheck) {
/*  593 */     String result = null;
/*  594 */     switch (func) {
/*      */       case "Исп. функцию":
/*  596 */         assert chestInventory != null;
/*  597 */         if (chestInventory.getItem(13) != null && 
/*  598 */           chestInventory.getItem(13).hasItemMeta()) {
/*  599 */           if (textCheck.equals(FrameLandCreative.Color("&a&lСинхронно"))) {
/*  600 */             result = "useFunc[sync](" + chestInventory.getItem(13).getItemMeta().getDisplayName().substring(2) + ")"; break;
/*  601 */           }  if (textCheck.equals(FrameLandCreative.Color("&c&lАсинхронно"))) {
/*  602 */             result = "useFunc[async](" + chestInventory.getItem(13).getItemMeta().getDisplayName().substring(2) + ")";
/*      */           }
/*      */         } 
/*      */         break;
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
/*      */       case "Установить блок":
/*  707 */         assert chestInventory != null;
/*  708 */         if (chestInventory.getItem(11) != null && 
/*  709 */           chestInventory.getItem(11).hasItemMeta() && 
/*  710 */           chestInventory.getItem(11).getItemMeta().hasDisplayName()) {
/*  711 */           String coordsall = chestInventory.getItem(11).getItemMeta().getDisplayName();
/*  712 */           ItemStack type = chestInventory.getItem(15);
/*  713 */           List<String> coords5 = Arrays.asList(coordsall.split("\\|"));
/*  714 */           String coords = "";
/*  715 */           String id = "0";
/*  716 */           if (type.getType().isBlock()) {
/*  717 */             id = Integer.toString(type.getType().getId());
/*      */           }
/*  719 */           if (chestInventory.getItem(11).getType() == Material.GOLDEN_APPLE) {
/*  720 */             if (coordsall.equals(FrameLandCreative.Color(config.getString("apple.n.name")))) {
/*  721 */               coords = "apple[location]~";
/*  722 */             } else if (coordsall.equals(FrameLandCreative.Color(config.getString("apple.p.name")))) {
/*  723 */               coords = "apple[look_block_loc]~";
/*  724 */             } else if (coordsall.equals(FrameLandCreative.Color(config.getString("apple.x.name")))) {
/*  725 */               coords = "apple[block_loc]~";
/*      */             }
/*      */           
/*  728 */           } else if (coords5.size() >= 3) {
/*      */             try {
/*  730 */               String xStr = ((String)coords5.get(0)).substring(2).trim();
/*  731 */               String[] xList = xStr.split("\\.");
/*  732 */               int x = Integer.parseInt(xList[0]);
/*      */               
/*  734 */               String yStr = ((String)coords5.get(1)).trim();
/*  735 */               String[] yList = yStr.split("\\.");
/*  736 */               int y = Integer.parseInt(yList[0]);
/*      */               
/*  738 */               String zStr = ((String)coords5.get(2)).trim();
/*  739 */               String[] zList = zStr.split("\\.");
/*  740 */               int z = Integer.parseInt(zList[0]);
/*  741 */               coords = x + "|" + y + "|" + z;
/*  742 */             } catch (Exception exception) {}
/*      */           } 
/*      */ 
/*      */ 
/*      */           
/*  747 */           if (type.hasItemMeta() && type.getItemMeta().hasDisplayName() && 
/*  748 */             type.getType() == Material.GOLDEN_APPLE) {
/*  749 */             if (type.getItemMeta().getDisplayName().equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/*  750 */               id = "apple[main_hand_item]~";
/*  751 */             } else if (type.getItemMeta().getDisplayName().equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/*  752 */               id = "apple[look_block]~";
/*  753 */             } else if (type.getItemMeta().getDisplayName().equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/*  754 */               id = "apple[off_hand_item]~";
/*  755 */             } else if (type.getItemMeta().getDisplayName().equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/*  756 */               id = "apple[click_item]~";
/*  757 */             } else if (type.getItemMeta().getDisplayName().equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/*  758 */               id = "apple[event_block]~";
/*      */             } 
/*      */           }
/*      */           
/*  762 */           result = "setBlock[" + id + "](" + coords + ")";
/*      */         } 
/*      */         break;
/*      */ 
/*      */       
/*      */       case "Задержка в тик.":
/*  768 */         assert chestInventory != null;
/*  769 */         if (chestInventory.getItem(13) != null && 
/*  770 */           chestInventory.getItem(13).hasItemMeta() && chestInventory
/*  771 */           .getItem(13).getItemMeta().hasDisplayName()) {
/*  772 */           String amount = chestInventory.getItem(13).getItemMeta().getDisplayName();
/*      */           try {
/*  774 */             int ticks = Integer.parseInt(amount.substring(2).trim());
/*  775 */             result = "timeWait(" + ticks + ")";
/*  776 */           } catch (Exception exception) {}
/*      */ 
/*      */           
/*  779 */           if (chestInventory.getItem(13).getType() == Material.GOLDEN_APPLE) {
/*  780 */             if (amount.equals(FrameLandCreative.Color(config.getString("apple.d.name")))) {
/*  781 */               String ticks = "apple[health_now]~";
/*  782 */               result = "timeWait(" + ticks + ")"; break;
/*  783 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.e.name")))) {
/*  784 */               String ticks = "apple[health_max]~";
/*  785 */               result = "timeWait(" + ticks + ")"; break;
/*  786 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.f.name")))) {
/*  787 */               String ticks = "apple[hunger]~";
/*  788 */               result = "timeWait(" + ticks + ")"; break;
/*  789 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.g.name")))) {
/*  790 */               String ticks = "apple[satiety]~";
/*  791 */               result = "timeWait(" + ticks + ")"; break;
/*  792 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.h.name")))) {
/*  793 */               String ticks = "apple[xp]~";
/*  794 */               result = "timeWait(" + ticks + ")"; break;
/*  795 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.i.name")))) {
/*  796 */               String ticks = "apple[armor]~";
/*  797 */               result = "timeWait(" + ticks + ")"; break;
/*  798 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.j.name")))) {
/*  799 */               String ticks = "apple[air]~";
/*  800 */               result = "timeWait(" + ticks + ")"; break;
/*  801 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.k.name")))) {
/*  802 */               String ticks = "apple[slot_now]~";
/*  803 */               result = "timeWait(" + ticks + ")"; break;
/*  804 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.l.name")))) {
/*  805 */               String ticks = "apple[ping]~";
/*  806 */               result = "timeWait(" + ticks + ")"; break;
/*  807 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.s.name")))) {
/*  808 */               String ticks = "apple[damage]~";
/*  809 */               result = "timeWait(" + ticks + ")"; break;
/*  810 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.t.name")))) {
/*  811 */               String ticks = "apple[click_slot]~";
/*  812 */               result = "timeWait(" + ticks + ")"; break;
/*  813 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.u.name")))) {
/*  814 */               String ticks = "apple[new_slot]~";
/*  815 */               result = "timeWait(" + ticks + ")"; break;
/*  816 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.v.name")))) {
/*  817 */               String ticks = "apple[old_slot]~";
/*  818 */               result = "timeWait(" + ticks + ")"; break;
/*  819 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.aa.name")))) {
/*  820 */               String ticks = "apple[player_count]~";
/*  821 */               result = "timeWait(" + ticks + ")"; break;
/*  822 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.ab.name")))) {
/*  823 */               String ticks = "apple[like_count]~";
/*  824 */               result = "timeWait(" + ticks + ")"; break;
/*  825 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.ac.name")))) {
/*  826 */               String ticks = "apple[unique_count]~";
/*  827 */               result = "timeWait(" + ticks + ")"; break;
/*  828 */             }  if (amount.equals(FrameLandCreative.Color(config.getString("apple.ad.name")))) {
/*  829 */               String ticks = "apple[world_id]~";
/*  830 */               result = "timeWait(" + ticks + ")";
/*      */             } 
/*      */           } 
/*      */           break;
/*      */         } 
/*      */       
/*      */       case "Отмена события":
/*  837 */         result = "cancelEvent";
/*      */         break;
/*      */     } 
/*  840 */     return result;
/*      */   }
/*      */   
/*      */   public static String wood(String func, Inventory chestInventory, String switchPlayer, String textCheck) {
/*      */     List<String> itemlist3, itemlist2, itemlist, list, list1, list2, list3, list4, list5, list6;
/*  845 */     String result = null;
/*  846 */     String noMark = "";
/*  847 */     if (textCheck.equals(FrameLandCreative.Color("&c&lНЕ"))) {
/*  848 */       noMark = "!";
/*      */     }
/*  850 */     String start = "if%_" + switchPlayer + "_%(" + noMark;
/*  851 */     switch (func) {
/*      */       case "Стоит на":
/*  853 */         itemlist3 = new ArrayList<>();
/*  854 */         if (chestInventory != null) {
/*  855 */           for (int i = 0; i < (chestInventory.getContents()).length; i++) {
/*  856 */             if (i == 7 || i == 16) i += 2; 
/*  857 */             if (i == 25)
/*  858 */               break;  if (chestInventory.getItem(i) != null) {
/*  859 */               ItemStack item = chestInventory.getItem(i);
/*  860 */               if (chestInventory.getItem(i).getType() == Material.PAPER) {
/*  861 */                 if (chestInventory.getItem(i).hasItemMeta() && chestInventory
/*  862 */                   .getItem(i).getItemMeta().hasDisplayName()) {
/*  863 */                   String s = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*  864 */                   List<String> coordsfull = Arrays.asList(s.split("\\|"));
/*      */                   
/*  866 */                   if (coordsfull.size() >= 3) {
/*      */                     try {
/*  868 */                       String xStr = ((String)coordsfull.get(0)).substring(2).trim();
/*  869 */                       String[] xList = xStr.split("\\.");
/*  870 */                       int x = Integer.parseInt(xList[0]);
/*      */                       
/*  872 */                       String yStr = ((String)coordsfull.get(1)).trim();
/*  873 */                       String[] yList = yStr.split("\\.");
/*  874 */                       int y = Integer.parseInt(yList[0]);
/*      */                       
/*  876 */                       String zStr = ((String)coordsfull.get(2)).trim();
/*  877 */                       String[] zList = zStr.split("\\.");
/*  878 */                       int z = Integer.parseInt(zList[0]);
/*  879 */                       String coords = "coord[" + x + "~" + y + "~" + z + "]";
/*  880 */                       itemlist3.add(coords);
/*  881 */                     } catch (Exception exception) {}
/*      */                   }
/*      */                 }
/*      */               
/*      */               }
/*  886 */               else if (item.getType() == Material.GOLDEN_APPLE) {
/*  887 */                 if (chestInventory.getItem(i).hasItemMeta() && chestInventory
/*  888 */                   .getItem(i).getItemMeta().hasDisplayName()) {
/*  889 */                   String name = item.getItemMeta().getDisplayName();
/*  890 */                   if (name.equals(FrameLandCreative.Color(config.getString("apple.n.name")))) {
/*  891 */                     itemlist3.add("apple[location]~");
/*  892 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/*  893 */                     itemlist3.add("apple[look_block]~");
/*  894 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.p.name")))) {
/*  895 */                     itemlist3.add("apple[look_block_loc]~");
/*  896 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/*  897 */                     itemlist3.add("apple[main_hand_item]~");
/*  898 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/*  899 */                     itemlist3.add("apple[off_hand_item]~");
/*  900 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.x.name")))) {
/*  901 */                     itemlist3.add("apple[block_loc]~");
/*  902 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/*  903 */                     itemlist3.add("apple[click_item]~");
/*  904 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/*  905 */                     itemlist3.add("apple[event_block]~");
/*      */                   }
/*      */                 
/*      */                 } 
/*  909 */               } else if (chestInventory.getItem(i).getType().isBlock()) {
/*  910 */                 itemlist3.add("block[" + chestInventory.getItem(i).getType().getId() + "]");
/*      */               } 
/*      */             } 
/*      */           } 
/*      */           
/*  915 */           if (!itemlist3.isEmpty()) {
/*  916 */             String coordsfull = String.join("|", (Iterable)itemlist3);
/*  917 */             result = start + "standsAt=" + coordsfull + ")";
/*      */           } 
/*      */         } 
/*      */         break;
/*      */       case "Рядом с":
/*  922 */         itemlist2 = new ArrayList<>();
/*  923 */         if (chestInventory != null) {
/*  924 */           for (int i = 0; i < (chestInventory.getContents()).length; i++) {
/*  925 */             if (i == 7 || i == 16) i += 2; 
/*  926 */             if (i == 25)
/*  927 */               break;  if (chestInventory.getItem(i) != null) {
/*  928 */               ItemStack item = chestInventory.getItem(i);
/*  929 */               if (chestInventory.getItem(i).getType() == Material.PAPER) {
/*  930 */                 if (chestInventory.getItem(i).hasItemMeta() && chestInventory
/*  931 */                   .getItem(i).getItemMeta().hasDisplayName()) {
/*  932 */                   String s = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*  933 */                   List<String> coordsfull = Arrays.asList(s.split("\\|"));
/*      */                   
/*  935 */                   if (coordsfull.size() >= 3) {
/*      */                     try {
/*  937 */                       double x = Double.parseDouble(((String)coordsfull.get(0)).substring(2).trim());
/*  938 */                       double y = Double.parseDouble(((String)coordsfull.get(1)).trim());
/*  939 */                       double z = Double.parseDouble(((String)coordsfull.get(2)).trim());
/*  940 */                       String coords = "coord[" + String.format("%.1f", new Object[] { Double.valueOf(x) }).replace(",", ".") + "~" + String.format("%.1f", new Object[] { Double.valueOf(y) }).replace(",", ".") + "~" + String.format("%.1f", new Object[] { Double.valueOf(z) }).replace(",", ".") + "]";
/*  941 */                       itemlist2.add(coords);
/*  942 */                     } catch (Exception exception) {}
/*      */                   }
/*      */                 }
/*      */               
/*      */               }
/*  947 */               else if (item.getType() == Material.GOLDEN_APPLE && 
/*  948 */                 chestInventory.getItem(i).hasItemMeta() && chestInventory
/*  949 */                 .getItem(i).getItemMeta().hasDisplayName()) {
/*  950 */                 String name = item.getItemMeta().getDisplayName();
/*  951 */                 if (name.equals(FrameLandCreative.Color(config.getString("apple.n.name")))) {
/*  952 */                   itemlist2.add("apple[location]~");
/*  953 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.p.name")))) {
/*  954 */                   itemlist2.add("apple[look_block_loc]~");
/*  955 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.x.name")))) {
/*  956 */                   itemlist2.add("apple[block_loc]~");
/*      */                 } 
/*      */               } 
/*      */             } 
/*      */           } 
/*      */           
/*  962 */           if (!itemlist2.isEmpty()) {
/*  963 */             String radius = "5";
/*  964 */             if (chestInventory.getItem(17) != null && 
/*  965 */               chestInventory.getItem(17).hasItemMeta() && 
/*  966 */               chestInventory.getItem(17).getItemMeta().hasDisplayName() && 
/*  967 */               isNumeric(chestInventory.getItem(17).getItemMeta().getDisplayName().substring(2))) {
/*  968 */               radius = chestInventory.getItem(17).getItemMeta().getDisplayName().substring(2);
/*      */             }
/*      */ 
/*      */ 
/*      */             
/*  973 */             String coordsfull = String.join("|", (Iterable)itemlist2);
/*  974 */             result = start + "playerNear[radius:" + radius + "]=" + coordsfull + ")";
/*      */           } 
/*      */         } 
/*      */         break;
/*      */       case "Смотрит на":
/*  979 */         itemlist = new ArrayList<>();
/*  980 */         if (chestInventory != null) {
/*  981 */           for (int i = 0; i < (chestInventory.getContents()).length; i++) {
/*  982 */             if (i == 7 || i == 16) i += 2; 
/*  983 */             if (i == 25)
/*  984 */               break;  if (chestInventory.getItem(i) != null) {
/*  985 */               ItemStack item = chestInventory.getItem(i);
/*  986 */               if (chestInventory.getItem(i).getType() == Material.PAPER) {
/*  987 */                 if (chestInventory.getItem(i).hasItemMeta() && chestInventory
/*  988 */                   .getItem(i).getItemMeta().hasDisplayName()) {
/*  989 */                   String s = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*  990 */                   List<String> coordsfull = Arrays.asList(s.split("\\|"));
/*      */                   
/*  992 */                   if (coordsfull.size() >= 3) {
/*      */                     try {
/*  994 */                       String xStr = ((String)coordsfull.get(0)).substring(2).trim();
/*  995 */                       String[] xList = xStr.split("\\.");
/*  996 */                       int x = Integer.parseInt(xList[0]);
/*      */                       
/*  998 */                       String yStr = ((String)coordsfull.get(1)).trim();
/*  999 */                       String[] yList = yStr.split("\\.");
/* 1000 */                       int y = Integer.parseInt(yList[0]);
/*      */                       
/* 1002 */                       String zStr = ((String)coordsfull.get(2)).trim();
/* 1003 */                       String[] zList = zStr.split("\\.");
/* 1004 */                       int z = Integer.parseInt(zList[0]);
/* 1005 */                       String coords = "coord[" + x + "~" + y + "~" + z + "]";
/* 1006 */                       itemlist.add(coords);
/* 1007 */                     } catch (Exception exception) {}
/*      */                   }
/*      */                 }
/*      */               
/*      */               }
/* 1012 */               else if (item.getType() == Material.GOLDEN_APPLE) {
/* 1013 */                 if (chestInventory.getItem(i).hasItemMeta() && chestInventory
/* 1014 */                   .getItem(i).getItemMeta().hasDisplayName()) {
/* 1015 */                   String name = item.getItemMeta().getDisplayName();
/* 1016 */                   if (name.equals(FrameLandCreative.Color(config.getString("apple.n.name")))) {
/* 1017 */                     itemlist.add("apple[location]~");
/* 1018 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/* 1019 */                     itemlist.add("apple[look_block]~");
/* 1020 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.p.name")))) {
/* 1021 */                     itemlist.add("apple[look_block_loc]~");
/* 1022 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/* 1023 */                     itemlist.add("apple[main_hand_item]~");
/* 1024 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/* 1025 */                     itemlist.add("apple[off_hand_item]~");
/* 1026 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.x.name")))) {
/* 1027 */                     itemlist.add("apple[block_loc]~");
/* 1028 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/* 1029 */                     itemlist.add("apple[click_item]~");
/* 1030 */                   } else if (name.equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/* 1031 */                     itemlist.add("apple[event_block]~");
/*      */                   }
/*      */                 
/*      */                 } 
/* 1035 */               } else if (chestInventory.getItem(i).getType().isBlock()) {
/* 1036 */                 itemlist.add("block[" + chestInventory.getItem(i).getType().getId() + "]");
/*      */               } 
/*      */             } 
/*      */           } 
/*      */           
/* 1041 */           if (!itemlist.isEmpty()) {
/* 1042 */             String radius = "5";
/* 1043 */             if (chestInventory.getItem(17) != null && 
/* 1044 */               chestInventory.getItem(17).hasItemMeta() && 
/* 1045 */               chestInventory.getItem(17).getItemMeta().hasDisplayName() && 
/* 1046 */               isNumeric(chestInventory.getItem(17).getItemMeta().getDisplayName().substring(2))) {
/* 1047 */               radius = chestInventory.getItem(17).getItemMeta().getDisplayName().substring(2);
/*      */             }
/*      */ 
/*      */ 
/*      */             
/* 1052 */             String coordsfull = String.join("|", (Iterable)itemlist);
/* 1053 */             result = start + "lookingAt[radius:" + radius + "]=" + coordsfull + ")";
/*      */           } 
/*      */         } 
/*      */         break;
/*      */       case "Имя игрока":
/* 1058 */         list = new ArrayList<>();
/* 1059 */         if (chestInventory != null) {
/* 1060 */           for (int i = 0; i < (chestInventory.getContents()).length; i++) {
/* 1061 */             if (chestInventory.getItem(i) != null && chestInventory.getItem(i).hasItemMeta() && chestInventory
/* 1062 */               .getItem(i).getItemMeta().hasDisplayName()) {
/* 1063 */               String s = chestInventory.getItem(i).getItemMeta().getDisplayName().replaceAll("§[0-9a-fbolmk]", "");
/* 1064 */               list.add(s);
/*      */             } 
/*      */           } 
/*      */         }
/* 1068 */         if (!list.isEmpty()) {
/* 1069 */           String line = String.join("|", (Iterable)list);
/* 1070 */           result = start + "playerName=" + line + ")";
/*      */         } 
/*      */         break;
/*      */       case "Сообщение":
/* 1074 */         list1 = new ArrayList<>();
/* 1075 */         if (chestInventory != null) {
/* 1076 */           for (int i = 0; i < (chestInventory.getContents()).length; i++) {
/* 1077 */             if (chestInventory.getItem(i) != null && chestInventory.getItem(i).hasItemMeta() && chestInventory
/* 1078 */               .getItem(i).getItemMeta().hasDisplayName()) {
/* 1079 */               ItemStack item = chestInventory.getItem(i);
/* 1080 */               String name = item.getItemMeta().getDisplayName();
/* 1081 */               if (item.getType() == Material.GOLDEN_APPLE) {
/* 1082 */                 if (name.equals(FrameLandCreative.Color(config.getString("apple.d.name")))) {
/* 1083 */                   list1.add("apple[health_now]~");
/* 1084 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.e.name")))) {
/* 1085 */                   list1.add("apple[health_max]~");
/* 1086 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.f.name")))) {
/* 1087 */                   list1.add("apple[hunger]~");
/* 1088 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.g.name")))) {
/* 1089 */                   list1.add("apple[satiety]~");
/* 1090 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.h.name")))) {
/* 1091 */                   list1.add("apple[xp]~");
/* 1092 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.i.name")))) {
/* 1093 */                   list1.add("apple[armor]~");
/* 1094 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.j.name")))) {
/* 1095 */                   list1.add("apple[air]~");
/* 1096 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.k.name")))) {
/* 1097 */                   list1.add("apple[slot_now]~");
/* 1098 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.l.name")))) {
/* 1099 */                   list1.add("apple[ping]~");
/* 1100 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.n.name")))) {
/* 1101 */                   list1.add("apple[location]~");
/* 1102 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.o.name")))) {
/* 1103 */                   list1.add("apple[inventory_name]~");
/* 1104 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.p.name")))) {
/* 1105 */                   list1.add("apple[look_block_loc]~");
/* 1106 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.s.name")))) {
/* 1107 */                   list1.add("apple[damage]~");
/* 1108 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.t.name")))) {
/* 1109 */                   list1.add("apple[click_slot]~");
/* 1110 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.u.name")))) {
/* 1111 */                   list1.add("apple[new_slot]~");
/* 1112 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.v.name")))) {
/* 1113 */                   list1.add("apple[old_slot]~");
/* 1114 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.w.name")))) {
/* 1115 */                   list1.add("apple[message]~");
/* 1116 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.x.name")))) {
/* 1117 */                   list1.add("apple[block_loc]~");
/* 1118 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.aa.name")))) {
/* 1119 */                   list1.add("apple[player_count]~");
/* 1120 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ab.name")))) {
/* 1121 */                   list1.add("apple[like_count]~");
/* 1122 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ac.name")))) {
/* 1123 */                   list1.add("apple[unique_count]~");
/* 1124 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ad.name")))) {
/* 1125 */                   list1.add("apple[world_id]~");
/*      */                 } 
/*      */               } else {
/* 1128 */                 String s = chestInventory.getItem(i).getItemMeta().getDisplayName().replaceAll("§[0-9a-fbolmk]", "");
/* 1129 */                 list1.add(s);
/*      */               } 
/*      */             } 
/*      */           } 
/*      */         }
/* 1134 */         if (!list1.isEmpty()) {
/* 1135 */           String line1 = String.join("|", (Iterable)list1);
/* 1136 */           result = start + "message=" + line1 + ")";
/*      */         } 
/*      */         break;
/*      */       case "Название инв.":
/* 1140 */         list2 = new ArrayList<>();
/* 1141 */         if (chestInventory != null) {
/* 1142 */           for (int i = 0; i < (chestInventory.getContents()).length; i++) {
/* 1143 */             if (chestInventory.getItem(i) != null && chestInventory.getItem(i).hasItemMeta() && chestInventory
/* 1144 */               .getItem(i).getItemMeta().hasDisplayName()) {
/* 1145 */               ItemStack item = chestInventory.getItem(i);
/* 1146 */               String name = item.getItemMeta().getDisplayName();
/* 1147 */               if (item.getType() == Material.GOLDEN_APPLE) {
/* 1148 */                 if (name.equals(FrameLandCreative.Color(config.getString("apple.d.name")))) {
/* 1149 */                   list2.add("apple[health_now]~");
/* 1150 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.e.name")))) {
/* 1151 */                   list2.add("apple[health_max]~");
/* 1152 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.f.name")))) {
/* 1153 */                   list2.add("apple[hunger]~");
/* 1154 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.g.name")))) {
/* 1155 */                   list2.add("apple[satiety]~");
/* 1156 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.h.name")))) {
/* 1157 */                   list2.add("apple[xp]~");
/* 1158 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.i.name")))) {
/* 1159 */                   list2.add("apple[armor]~");
/* 1160 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.j.name")))) {
/* 1161 */                   list2.add("apple[air]~");
/* 1162 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.k.name")))) {
/* 1163 */                   list2.add("apple[slot_now]~");
/* 1164 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.l.name")))) {
/* 1165 */                   list2.add("apple[ping]~");
/* 1166 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.n.name")))) {
/* 1167 */                   list2.add("apple[location]~");
/* 1168 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.o.name")))) {
/* 1169 */                   list2.add("apple[inventory_name]~");
/* 1170 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.p.name")))) {
/* 1171 */                   list2.add("apple[look_block_loc]~");
/* 1172 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.s.name")))) {
/* 1173 */                   list2.add("apple[damage]~");
/* 1174 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.t.name")))) {
/* 1175 */                   list2.add("apple[click_slot]~");
/* 1176 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.u.name")))) {
/* 1177 */                   list2.add("apple[new_slot]~");
/* 1178 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.v.name")))) {
/* 1179 */                   list2.add("apple[old_slot]~");
/* 1180 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.w.name")))) {
/* 1181 */                   list2.add("apple[message]~");
/* 1182 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.x.name")))) {
/* 1183 */                   list2.add("apple[block_loc]~");
/* 1184 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.aa.name")))) {
/* 1185 */                   list2.add("apple[player_count]~");
/* 1186 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ab.name")))) {
/* 1187 */                   list2.add("apple[like_count]~");
/* 1188 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ac.name")))) {
/* 1189 */                   list2.add("apple[unique_count]~");
/* 1190 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.ad.name")))) {
/* 1191 */                   list2.add("apple[world_id]~");
/*      */                 } 
/*      */               } else {
/* 1194 */                 String s = chestInventory.getItem(i).getItemMeta().getDisplayName().replaceAll("§[0-9a-fbolmk]", "");
/* 1195 */                 list2.add(s);
/*      */               } 
/*      */             } 
/*      */           } 
/*      */         }
/* 1200 */         if (!list2.isEmpty()) {
/* 1201 */           String line2 = String.join("|", (Iterable)list2);
/* 1202 */           result = start + "inventoryName=" + line2 + ")";
/*      */         } 
/*      */         break;
/*      */       case "Игрок имеет":
/* 1206 */         list3 = new ArrayList<>();
/* 1207 */         if (chestInventory != null) {
/* 1208 */           for (int i = 0; i < (chestInventory.getContents()).length; i++) {
/* 1209 */             if (chestInventory.getItem(i) != null) {
/* 1210 */               ItemStack item = chestInventory.getItem(i);
/* 1211 */               if (chestInventory.getItem(i).hasItemMeta() && chestInventory
/* 1212 */                 .getItem(i).getItemMeta().hasDisplayName() && item
/* 1213 */                 .getType() == Material.GOLDEN_APPLE) {
/* 1214 */                 String name = item.getItemMeta().getDisplayName();
/* 1215 */                 if (name.equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/* 1216 */                   list3.add("apple[main_hand_item]~");
/* 1217 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/* 1218 */                   list3.add("apple[look_block]~");
/* 1219 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/* 1220 */                   list3.add("apple[off_hand_item]~");
/* 1221 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/* 1222 */                   list3.add("apple[click_item]~");
/* 1223 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/* 1224 */                   list3.add("apple[event_block]~");
/*      */                 } else {
/* 1226 */                   name = "default";
/* 1227 */                   if (chestInventory.getItem(i).hasItemMeta() && chestInventory.getItem(i).getItemMeta().hasDisplayName()) {
/* 1228 */                     name = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*      */                   }
/* 1230 */                   String id = Integer.toString(chestInventory.getItem(i).getType().getId());
/* 1231 */                   String amount = Integer.toString(chestInventory.getItem(i).getAmount());
/* 1232 */                   list3.add("item[-" + id + "*" + name + "-][+" + amount + "+]");
/*      */                 } 
/*      */               } else {
/* 1235 */                 String name = "default";
/* 1236 */                 if (chestInventory.getItem(i).hasItemMeta() && chestInventory.getItem(i).getItemMeta().hasDisplayName()) {
/* 1237 */                   name = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*      */                 }
/* 1239 */                 String id = Integer.toString(chestInventory.getItem(i).getType().getId());
/* 1240 */                 String amount = Integer.toString(chestInventory.getItem(i).getAmount());
/* 1241 */                 list3.add("item[-" + id + "*" + name + "-][+" + amount + "+]");
/*      */               } 
/*      */             } 
/*      */           } 
/*      */         }
/* 1246 */         if (!list3.isEmpty()) {
/* 1247 */           String line3 = String.join("|", (Iterable)list3);
/* 1248 */           result = start + "playerHaveItems=" + line3 + ")";
/*      */         } 
/*      */         break;
/*      */       case "Инвентарь имеет":
/* 1252 */         list4 = new ArrayList<>();
/* 1253 */         if (chestInventory != null) {
/* 1254 */           for (int i = 0; i < (chestInventory.getContents()).length; i++) {
/* 1255 */             if (chestInventory.getItem(i) != null) {
/* 1256 */               ItemStack item = chestInventory.getItem(i);
/* 1257 */               if (chestInventory.getItem(i).hasItemMeta() && chestInventory
/* 1258 */                 .getItem(i).getItemMeta().hasDisplayName() && item
/* 1259 */                 .getType() == Material.GOLDEN_APPLE) {
/* 1260 */                 String name = item.getItemMeta().getDisplayName();
/* 1261 */                 if (name.equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/* 1262 */                   list4.add("apple[main_hand_item]~");
/* 1263 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/* 1264 */                   list4.add("apple[look_block]~");
/* 1265 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/* 1266 */                   list4.add("apple[off_hand_item]~");
/* 1267 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/* 1268 */                   list4.add("apple[click_item]~");
/* 1269 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/* 1270 */                   list4.add("apple[event_block]~");
/*      */                 } else {
/* 1272 */                   name = "default";
/* 1273 */                   if (chestInventory.getItem(i).hasItemMeta() && chestInventory.getItem(i).getItemMeta().hasDisplayName()) {
/* 1274 */                     name = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*      */                   }
/* 1276 */                   String id = Integer.toString(chestInventory.getItem(i).getType().getId());
/* 1277 */                   String amount = Integer.toString(chestInventory.getItem(i).getAmount());
/* 1278 */                   list4.add("item[-" + id + "*" + name + "-][+" + amount + "+]");
/*      */                 } 
/*      */               } else {
/* 1281 */                 String name = "default";
/* 1282 */                 if (chestInventory.getItem(i).hasItemMeta() && chestInventory.getItem(i).getItemMeta().hasDisplayName()) {
/* 1283 */                   name = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*      */                 }
/* 1285 */                 String id = Integer.toString(chestInventory.getItem(i).getType().getId());
/* 1286 */                 String amount = Integer.toString(chestInventory.getItem(i).getAmount());
/* 1287 */                 list4.add("item[-" + id + "*" + name + "-][+" + amount + "+]");
/*      */               } 
/*      */             } 
/*      */           } 
/*      */         }
/* 1292 */         if (!list4.isEmpty()) {
/* 1293 */           String line4 = String.join("|", (Iterable)list4);
/* 1294 */           result = "timeWait(1)&" + start + "inventoryHaveItems=" + line4 + ")";
/*      */         } 
/*      */         break;
/*      */       case "Предмет равен":
/* 1298 */         assert chestInventory != null;
/* 1299 */         if (chestInventory.getItem(13) != null) {
/* 1300 */           if (chestInventory.getItem(13).hasItemMeta() && chestInventory
/* 1301 */             .getItem(13).getItemMeta().hasDisplayName() && chestInventory
/* 1302 */             .getItem(13).getType() == Material.GOLDEN_APPLE) {
/* 1303 */             String str1 = chestInventory.getItem(13).getItemMeta().getDisplayName();
/* 1304 */             if (str1.equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/* 1305 */               result = start + "itemstack=apple[main_hand_item]~)"; break;
/* 1306 */             }  if (str1.equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/* 1307 */               result = start + "itemstack=apple[look_block]~)"; break;
/* 1308 */             }  if (str1.equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/* 1309 */               result = start + "itemstack=apple[off_hand_item]~)"; break;
/* 1310 */             }  if (str1.equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/* 1311 */               result = start + "itemstack=apple[click_item]~)"; break;
/* 1312 */             }  if (str1.equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/* 1313 */               result = start + "itemstack=apple[event_block]~)"; break;
/*      */             } 
/* 1315 */             str1 = "default";
/* 1316 */             if (chestInventory.getItem(13).hasItemMeta() && chestInventory.getItem(13).getItemMeta().hasDisplayName()) {
/* 1317 */               str1 = chestInventory.getItem(13).getItemMeta().getDisplayName();
/*      */             }
/* 1319 */             String str2 = Integer.toString(chestInventory.getItem(13).getType().getId());
/* 1320 */             String str3 = Integer.toString(chestInventory.getItem(13).getAmount());
/* 1321 */             result = start + "itemstack=item[-" + str2 + "*" + str1 + "-][+" + str3 + "+])";
/*      */             break;
/*      */           } 
/* 1324 */           String name = "default";
/* 1325 */           if (chestInventory.getItem(13).hasItemMeta() && chestInventory.getItem(13).getItemMeta().hasDisplayName()) {
/* 1326 */             name = chestInventory.getItem(13).getItemMeta().getDisplayName();
/*      */           }
/* 1328 */           String id = Integer.toString(chestInventory.getItem(13).getType().getId());
/* 1329 */           String amount = Integer.toString(chestInventory.getItem(13).getAmount());
/* 1330 */           result = start + "itemstack=item[-" + id + "*" + name + "-][+" + amount + "+])";
/*      */         } 
/*      */         break;
/*      */       
/*      */       case "Держит в осн.":
/* 1335 */         list5 = new ArrayList<>();
/* 1336 */         if (chestInventory != null) {
/* 1337 */           for (int i = 0; i < (chestInventory.getContents()).length; i++) {
/* 1338 */             if (chestInventory.getItem(i) != null) {
/* 1339 */               if (chestInventory.getItem(i).hasItemMeta() && chestInventory
/* 1340 */                 .getItem(i).getItemMeta().hasDisplayName() && chestInventory
/* 1341 */                 .getItem(i).getType() == Material.GOLDEN_APPLE) {
/* 1342 */                 String name = chestInventory.getItem(i).getItemMeta().getDisplayName();
/* 1343 */                 if (name.equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/* 1344 */                   list5.add("apple[main_hand_item]~");
/* 1345 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/* 1346 */                   list5.add("apple[look_block]~");
/* 1347 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/* 1348 */                   list5.add("apple[off_hand_item]~");
/* 1349 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/* 1350 */                   list5.add("apple[click_item]~");
/* 1351 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/* 1352 */                   list5.add("apple[event_block]~");
/*      */                 } else {
/* 1354 */                   name = "default";
/* 1355 */                   if (chestInventory.getItem(i).hasItemMeta() && chestInventory.getItem(i).getItemMeta().hasDisplayName()) {
/* 1356 */                     name = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*      */                   }
/* 1358 */                   String id = Integer.toString(chestInventory.getItem(i).getType().getId());
/* 1359 */                   String amount = Integer.toString(chestInventory.getItem(i).getAmount());
/* 1360 */                   list5.add("item[-" + id + "*" + name + "-][+" + amount + "+]");
/*      */                 } 
/*      */               } else {
/* 1363 */                 String name = "default";
/* 1364 */                 if (chestInventory.getItem(i).hasItemMeta() && chestInventory.getItem(i).getItemMeta().hasDisplayName()) {
/* 1365 */                   name = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*      */                 }
/* 1367 */                 String id = Integer.toString(chestInventory.getItem(i).getType().getId());
/* 1368 */                 String amount = Integer.toString(chestInventory.getItem(i).getAmount());
/* 1369 */                 list5.add("item[-" + id + "*" + name + "-][+" + amount + "+]");
/*      */               } 
/*      */             }
/*      */           } 
/*      */         }
/* 1374 */         if (!list5.isEmpty()) {
/* 1375 */           String line5 = String.join("|", (Iterable)list5);
/* 1376 */           result = start + "playerHoldingItemMain=" + line5 + ")";
/*      */         } 
/*      */         break;
/*      */       case "Держит в доп.":
/* 1380 */         list6 = new ArrayList<>();
/* 1381 */         if (chestInventory != null) {
/* 1382 */           for (int i = 0; i < (chestInventory.getContents()).length; i++) {
/* 1383 */             if (chestInventory.getItem(i) != null) {
/* 1384 */               if (chestInventory.getItem(i).hasItemMeta() && chestInventory
/* 1385 */                 .getItem(i).getItemMeta().hasDisplayName() && chestInventory
/* 1386 */                 .getItem(i).getType() == Material.GOLDEN_APPLE) {
/* 1387 */                 String name = chestInventory.getItem(i).getItemMeta().getDisplayName();
/* 1388 */                 if (name.equals(FrameLandCreative.Color(config.getString("apple.q.name")))) {
/* 1389 */                   list6.add("apple[main_hand_item]~");
/* 1390 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.m.name")))) {
/* 1391 */                   list6.add("apple[look_block]~");
/* 1392 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.r.name")))) {
/* 1393 */                   list6.add("apple[off_hand_item]~");
/* 1394 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.y.name")))) {
/* 1395 */                   list6.add("apple[click_item]~");
/* 1396 */                 } else if (name.equals(FrameLandCreative.Color(config.getString("apple.z.name")))) {
/* 1397 */                   list6.add("apple[event_block]~");
/*      */                 } else {
/* 1399 */                   name = "default";
/* 1400 */                   if (chestInventory.getItem(i).hasItemMeta() && chestInventory.getItem(i).getItemMeta().hasDisplayName()) {
/* 1401 */                     name = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*      */                   }
/* 1403 */                   String id = Integer.toString(chestInventory.getItem(i).getType().getId());
/* 1404 */                   String amount = Integer.toString(chestInventory.getItem(i).getAmount());
/* 1405 */                   list6.add("item[-" + id + "*" + name + "-][+" + amount + "+]");
/*      */                 } 
/*      */               } else {
/* 1408 */                 String name = "default";
/* 1409 */                 if (chestInventory.getItem(i).hasItemMeta() && chestInventory.getItem(i).getItemMeta().hasDisplayName()) {
/* 1410 */                   name = chestInventory.getItem(i).getItemMeta().getDisplayName();
/*      */                 }
/* 1412 */                 String id = Integer.toString(chestInventory.getItem(i).getType().getId());
/* 1413 */                 String amount = Integer.toString(chestInventory.getItem(i).getAmount());
/* 1414 */                 list6.add("item[-" + id + "*" + name + "-][+" + amount + "+]");
/*      */               } 
/*      */             }
/*      */           } 
/*      */         }
/* 1419 */         if (!list6.isEmpty()) {
/* 1420 */           String line6 = String.join("|", (Iterable)list6);
/* 1421 */           result = start + "playerHoldingItemOff=" + line6 + ")";
/*      */         } 
/*      */         break;
/*      */       case "Летает":
/* 1425 */         result = start + "isFly)";
/*      */         break;
/*      */       case "Крадётся":
/* 1428 */         result = start + "isSneaking)";
/*      */         break;
/*      */       case "":
/* 1431 */         result = start + "false)";
/*      */         break;
/*      */       case "Бежит":
/* 1434 */         result = start + "isRunning)";
/*      */         break;
/*      */     } 
/* 1437 */     return result;
/*      */   }
/*      */   
/*      */   public static String get(Block block) {
/* 1441 */     Block sign = block.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ() - 1);
/* 1442 */     if (sign.getState() instanceof Sign) {
/* 1443 */       Sign check = (Sign)sign.getState();
/* 1444 */       String func = check.getLine(1);
/* 1445 */       String textCheck = check.getLine(2);
/* 1446 */       String player = check.getLine(3);
/* 1447 */       String switchPlayer = "player";
/* 1448 */       switch (player) {
/*      */         case "Жертва":
/* 1450 */           switchPlayer = "victim";
/*      */           break;
/*      */         case "Атакующий":
/* 1453 */           switchPlayer = "damager";
/*      */           break;
/*      */         case "Случайный":
/* 1456 */           switchPlayer = "random";
/*      */           break;
/*      */       } 
/* 1459 */       String result = null;
/*      */       
/* 1461 */       Block chestBlock = block.getWorld().getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY() + 1, block.getLocation().getBlockZ());
/*      */       
/* 1463 */       Inventory chestInventory = null;
/* 1464 */       if (chestBlock.getState() instanceof Chest) {
/* 1465 */         Chest chest = (Chest)chestBlock.getState();
/* 1466 */         chestInventory = chest.getInventory();
/*      */       } 
/* 1468 */       if (block.getType() == Material.LAPIS_BLOCK) {
/* 1469 */         if (!func.equals("")) {
/* 1470 */           result = "function(" + func + ")";
/*      */         }
/* 1472 */       } else if (block.getType() == Material.DIAMOND_BLOCK) {
/* 1473 */         result = diamond(func);
/* 1474 */       } else if (block.getType() == Material.COBBLESTONE) {
/* 1475 */         result = cobblestone(func, chestInventory, switchPlayer);
/* 1476 */       } else if (block.getType() == Material.NETHER_BRICK) {
/* 1477 */         result = nether_brick(func, chestInventory, textCheck);
/* 1478 */       } else if (block.getType() == Material.WOOD) {
/* 1479 */         result = wood(func, chestInventory, switchPlayer, textCheck);
/*      */       } 
/* 1481 */       return result;
/*      */     } 
/* 1483 */     return null;
/*      */   }
/*      */   
/*      */   public static boolean isNumeric(String text) {
/*      */     try {
/* 1488 */       Integer.parseInt(text);
/* 1489 */       return true;
/* 1490 */     } catch (NumberFormatException e) {
/* 1491 */       return false;
/*      */     } 
/*      */   }
/*      */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\CodeCompiler\GetFunc_new.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */