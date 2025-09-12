/*     */ package deadpool23232.framelandcreative.GUI.Games.Functions.Skull;
/*     */ 
/*     */ import com.mojang.authlib.GameProfile;
/*     */ import com.mojang.authlib.properties.Property;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Method;
/*     */ import java.net.URI;
/*     */ import java.net.URISyntaxException;
/*     */ import java.util.Base64;
/*     */ import java.util.UUID;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.SkullType;
/*     */ import org.bukkit.block.Block;
/*     */ import org.bukkit.block.Skull;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ import org.bukkit.inventory.meta.SkullMeta;
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
/*     */ public class SkullCreator
/*     */ {
/*     */   private static boolean warningPosted = false;
/*     */   private static Field blockProfileField;
/*     */   private static Method metaSetProfileMethod;
/*     */   private static Field metaProfileField;
/*     */   
/*     */   public static ItemStack createSkull() {
/*  44 */     checkLegacy();
/*     */     
/*     */     try {
/*  47 */       return new ItemStack(Material.valueOf("PLAYER_HEAD"));
/*  48 */     } catch (IllegalArgumentException e) {
/*  49 */       return new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short)3);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ItemStack itemFromName(String name) {
/*  61 */     return itemWithName(createSkull(), name);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ItemStack itemFromUuid(UUID id) {
/*  71 */     return itemWithUuid(createSkull(), id);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ItemStack itemFromUrl(String url) {
/*  81 */     return itemWithUrl(createSkull(), url);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ItemStack itemFromBase64(String base64) {
/*  91 */     return itemWithBase64(createSkull(), base64);
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
/*     */   @Deprecated
/*     */   public static ItemStack itemWithName(ItemStack item, String name) {
/* 104 */     notNull(item, "item");
/* 105 */     notNull(name, "name");
/*     */     
/* 107 */     SkullMeta meta = (SkullMeta)item.getItemMeta();
/* 108 */     meta.setOwner(name);
/* 109 */     item.setItemMeta((ItemMeta)meta);
/*     */     
/* 111 */     return item;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ItemStack itemWithUuid(ItemStack item, UUID id) {
/* 122 */     notNull(item, "item");
/* 123 */     notNull(id, "id");
/*     */     
/* 125 */     SkullMeta meta = (SkullMeta)item.getItemMeta();
/* 126 */     meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
/* 127 */     item.setItemMeta((ItemMeta)meta);
/*     */     
/* 129 */     return item;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ItemStack itemWithUrl(ItemStack item, String url) {
/* 140 */     notNull(item, "item");
/* 141 */     notNull(url, "url");
/*     */     
/* 143 */     return itemWithBase64(item, urlToBase64(url));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ItemStack itemWithBase64(ItemStack item, String base64) {
/* 154 */     notNull(item, "item");
/* 155 */     notNull(base64, "base64");
/*     */     
/* 157 */     if (!(item.getItemMeta() instanceof SkullMeta)) {
/* 158 */       return null;
/*     */     }
/* 160 */     SkullMeta meta = (SkullMeta)item.getItemMeta();
/* 161 */     mutateItemMeta(meta, base64);
/* 162 */     item.setItemMeta((ItemMeta)meta);
/*     */     
/* 164 */     return item;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Deprecated
/*     */   public static void blockWithName(Block block, String name) {
/* 176 */     notNull(block, "block");
/* 177 */     notNull(name, "name");
/*     */     
/* 179 */     Skull state = (Skull)block.getState();
/* 180 */     state.setOwningPlayer(Bukkit.getOfflinePlayer(name));
/* 181 */     state.update(false, false);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void blockWithUuid(Block block, UUID id) {
/* 191 */     notNull(block, "block");
/* 192 */     notNull(id, "id");
/*     */     
/* 194 */     setToSkull(block);
/* 195 */     Skull state = (Skull)block.getState();
/* 196 */     state.setOwningPlayer(Bukkit.getOfflinePlayer(id));
/* 197 */     state.update(false, false);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void blockWithUrl(Block block, String url) {
/* 207 */     notNull(block, "block");
/* 208 */     notNull(url, "url");
/*     */     
/* 210 */     blockWithBase64(block, urlToBase64(url));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void blockWithBase64(Block block, String base64) {
/* 220 */     notNull(block, "block");
/* 221 */     notNull(base64, "base64");
/*     */     
/* 223 */     setToSkull(block);
/* 224 */     Skull state = (Skull)block.getState();
/* 225 */     mutateBlockState(state, base64);
/* 226 */     state.update(false, false);
/*     */   }
/*     */   
/*     */   private static void setToSkull(Block block) {
/* 230 */     checkLegacy();
/*     */     
/*     */     try {
/* 233 */       block.setType(Material.valueOf("PLAYER_HEAD"), false);
/* 234 */     } catch (IllegalArgumentException e) {
/* 235 */       block.setType(Material.valueOf("SKULL"), false);
/* 236 */       Skull state = (Skull)block.getState();
/* 237 */       state.setSkullType(SkullType.PLAYER);
/* 238 */       state.update(false, false);
/*     */     } 
/*     */   }
/*     */   
/*     */   private static void notNull(Object o, String name) {
/* 243 */     if (o == null) {
/* 244 */       throw new NullPointerException(name + " should not be null!");
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   private static String urlToBase64(String url) {
/*     */     URI actualUrl;
/*     */     try {
/* 252 */       actualUrl = new URI(url);
/* 253 */     } catch (URISyntaxException e) {
/* 254 */       throw new RuntimeException(e);
/*     */     } 
/* 256 */     String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl.toString() + "\"}}}";
/* 257 */     return Base64.getEncoder().encodeToString(toEncode.getBytes());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static GameProfile makeProfile(String b64) {
/* 264 */     UUID id = new UUID(b64.substring(b64.length() - 20).hashCode(), b64.substring(b64.length() - 10).hashCode());
/*     */     
/* 266 */     GameProfile profile = new GameProfile(id, "Player");
/* 267 */     profile.getProperties().put("textures", new Property("textures", b64));
/* 268 */     return profile;
/*     */   }
/*     */   
/*     */   private static void mutateBlockState(Skull block, String b64) {
/*     */     try {
/* 273 */       if (blockProfileField == null) {
/* 274 */         blockProfileField = block.getClass().getDeclaredField("profile");
/* 275 */         blockProfileField.setAccessible(true);
/*     */       } 
/* 277 */       blockProfileField.set(block, makeProfile(b64));
/* 278 */     } catch (NoSuchFieldException|IllegalAccessException e) {
/* 279 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   private static void mutateItemMeta(SkullMeta meta, String b64) {
/*     */     try {
/* 285 */       if (metaSetProfileMethod == null) {
/* 286 */         metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", new Class[] { GameProfile.class });
/* 287 */         metaSetProfileMethod.setAccessible(true);
/*     */       } 
/* 289 */       metaSetProfileMethod.invoke(meta, new Object[] { makeProfile(b64) });
/* 290 */     } catch (NoSuchMethodException|IllegalAccessException|java.lang.reflect.InvocationTargetException ex) {
/*     */ 
/*     */       
/*     */       try {
/* 294 */         if (metaProfileField == null) {
/* 295 */           metaProfileField = meta.getClass().getDeclaredField("profile");
/* 296 */           metaProfileField.setAccessible(true);
/*     */         } 
/* 298 */         metaProfileField.set(meta, makeProfile(b64));
/*     */       }
/* 300 */       catch (NoSuchFieldException|IllegalAccessException ex2) {
/* 301 */         ex2.printStackTrace();
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void checkLegacy() {
/*     */     try {
/* 313 */       Material.class.getDeclaredField("PLAYER_HEAD");
/* 314 */       Material.valueOf("SKULL");
/*     */       
/* 316 */       if (!warningPosted) {
/* 317 */         Bukkit.getLogger().warning("SKULLCREATOR API - Using the legacy bukkit API with 1.13+ bukkit versions is not supported!");
/* 318 */         warningPosted = true;
/*     */       } 
/* 320 */     } catch (NoSuchFieldException|IllegalArgumentException noSuchFieldException) {}
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Functions\Skull\SkullCreator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */