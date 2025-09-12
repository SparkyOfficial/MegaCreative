/*     */ package deadpool23232.framelandcreative.CODE.Items;
/*     */ 
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.block.Action;
/*     */ import org.bukkit.event.inventory.InventoryClickEvent;
/*     */ import org.bukkit.event.player.PlayerInteractEvent;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.InventoryHolder;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ 
/*     */ public class APPLE_OWO
/*     */   implements Listener
/*     */ {
/*  24 */   FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*     */   
/*     */   @EventHandler
/*     */   public void a(PlayerInteractEvent event) {
/*  28 */     if (event.getPlayer().getWorld().getName().contains("-code") && (
/*  29 */       event.getAction() == Action.RIGHT_CLICK_BLOCK || event
/*  30 */       .getAction() == Action.RIGHT_CLICK_AIR)) {
/*  31 */       Player player = event.getPlayer();
/*  32 */       if (player.getInventory().getItemInMainHand().getType() == Material.GOLDEN_APPLE && 
/*  33 */         player.getInventory().getItemInMainHand().getItemMeta().hasLore() && 
/*  34 */         player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) {
/*  35 */         use(player);
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @EventHandler
/*     */   public void onMenuClick(InventoryClickEvent event) {
/*  45 */     if (event.getView().getTitle().equalsIgnoreCase(FrameLandCreative.Color(this.config.getString("apple.gui-name")))) {
/*  46 */       event.setCancelled(true);
/*  47 */       if (event.getCurrentItem() == null) {
/*     */         return;
/*     */       }
/*  50 */       if (event.getCurrentItem().getType() == Material.AIR) {
/*     */         return;
/*     */       }
/*  53 */       ItemStack item = event.getCurrentItem();
/*  54 */       ItemMeta itemMeta = item.getItemMeta();
/*  55 */       String itemName = itemMeta.getDisplayName();
/*  56 */       Player player = (Player)event.getWhoClicked();
/*  57 */       if (itemName == null) {
/*     */         return;
/*     */       }
/*  60 */       if (itemName.equals(FrameLandCreative.Color(this.config.getString("apple.a.name")))) {
/*  61 */         use_a(player);
/*  62 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("apple.b.name")))) {
/*  63 */         use_b(player);
/*  64 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("apple.c.name")))) {
/*  65 */         use_c(player);
/*  66 */       } else if (itemName.equals(FrameLandCreative.Color(this.config.getString("apple.d.name"))) || itemName
/*  67 */         .equals(FrameLandCreative.Color(this.config.getString("apple.e.name"))) || itemName
/*  68 */         .equals(FrameLandCreative.Color(this.config.getString("apple.f.name"))) || itemName
/*  69 */         .equals(FrameLandCreative.Color(this.config.getString("apple.g.name"))) || itemName
/*  70 */         .equals(FrameLandCreative.Color(this.config.getString("apple.h.name"))) || itemName
/*  71 */         .equals(FrameLandCreative.Color(this.config.getString("apple.i.name"))) || itemName
/*  72 */         .equals(FrameLandCreative.Color(this.config.getString("apple.j.name"))) || itemName
/*  73 */         .equals(FrameLandCreative.Color(this.config.getString("apple.k.name"))) || itemName
/*  74 */         .equals(FrameLandCreative.Color(this.config.getString("apple.l.name"))) || itemName
/*  75 */         .equals(FrameLandCreative.Color(this.config.getString("apple.m.name"))) || itemName
/*  76 */         .equals(FrameLandCreative.Color(this.config.getString("apple.n.name"))) || itemName
/*  77 */         .equals(FrameLandCreative.Color(this.config.getString("apple.o.name"))) || itemName
/*  78 */         .equals(FrameLandCreative.Color(this.config.getString("apple.p.name"))) || itemName
/*  79 */         .equals(FrameLandCreative.Color(this.config.getString("apple.q.name"))) || itemName
/*  80 */         .equals(FrameLandCreative.Color(this.config.getString("apple.r.name"))) || itemName
/*  81 */         .equals(FrameLandCreative.Color(this.config.getString("apple.s.name"))) || itemName
/*  82 */         .equals(FrameLandCreative.Color(this.config.getString("apple.t.name"))) || itemName
/*  83 */         .equals(FrameLandCreative.Color(this.config.getString("apple.u.name"))) || itemName
/*  84 */         .equals(FrameLandCreative.Color(this.config.getString("apple.v.name"))) || itemName
/*  85 */         .equals(FrameLandCreative.Color(this.config.getString("apple.w.name"))) || itemName
/*  86 */         .equals(FrameLandCreative.Color(this.config.getString("apple.x.name"))) || itemName
/*  87 */         .equals(FrameLandCreative.Color(this.config.getString("apple.y.name"))) || itemName
/*  88 */         .equals(FrameLandCreative.Color(this.config.getString("apple.z.name"))) || itemName
/*  89 */         .equals(FrameLandCreative.Color(this.config.getString("apple.aa.name"))) || itemName
/*  90 */         .equals(FrameLandCreative.Color(this.config.getString("apple.ab.name"))) || itemName
/*  91 */         .equals(FrameLandCreative.Color(this.config.getString("apple.ac.name"))) || itemName
/*  92 */         .equals(FrameLandCreative.Color(this.config.getString("apple.ad.name")))) {
/*  93 */         ItemStack newItem = new ItemStack(Material.GOLDEN_APPLE);
/*  94 */         ItemMeta newMeta = newItem.getItemMeta();
/*  95 */         newMeta.setDisplayName(itemName);
/*  96 */         newMeta.setLore(item.getItemMeta().getLore());
/*  97 */         newItem.setItemMeta(newMeta);
/*  98 */         player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
/*  99 */         player.getInventory().setItemInMainHand(newItem);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public ItemStack getItem(String name) {
/* 106 */     ItemStack itemStack = new ItemStack(Material.getMaterial(this.config.getInt("apple." + name + ".material-id")));
/* 107 */     ItemMeta itemMeta = itemStack.getItemMeta();
/* 108 */     itemMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("apple." + name + ".name")));
/* 109 */     List<String> itemLore = new ArrayList<>();
/* 110 */     for (String line : this.config.getStringList("apple." + name + ".lore")) {
/* 111 */       itemLore.add(FrameLandCreative.Color(line));
/*     */     }
/* 113 */     itemMeta.setLore(itemLore);
/* 114 */     itemStack.setItemMeta(itemMeta);
/* 115 */     return itemStack;
/*     */   }
/*     */   
/*     */   public void use(Player player) {
/* 119 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color(this.config.getString("apple.gui-name")));
/*     */     
/* 121 */     gui.setItem(11, getItem("a"));
/* 122 */     gui.setItem(13, getItem("b"));
/* 123 */     gui.setItem(15, getItem("c"));
/*     */     
/* 125 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public void use_a(Player player) {
/* 129 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color(this.config.getString("apple.gui-name")));
/*     */     
/* 131 */     gui.setItem(0, getItem("d"));
/* 132 */     gui.setItem(1, getItem("e"));
/* 133 */     gui.setItem(2, getItem("f"));
/* 134 */     gui.setItem(3, getItem("g"));
/* 135 */     gui.setItem(4, getItem("h"));
/* 136 */     gui.setItem(5, getItem("i"));
/* 137 */     gui.setItem(6, getItem("j"));
/* 138 */     gui.setItem(7, getItem("k"));
/* 139 */     gui.setItem(8, getItem("l"));
/* 140 */     gui.setItem(9, getItem("m"));
/* 141 */     gui.setItem(10, getItem("n"));
/* 142 */     gui.setItem(11, getItem("o"));
/* 143 */     gui.setItem(12, getItem("p"));
/* 144 */     gui.setItem(13, getItem("q"));
/* 145 */     gui.setItem(14, getItem("r"));
/*     */     
/* 147 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public void use_b(Player player) {
/* 151 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color(this.config.getString("apple.gui-name")));
/*     */     
/* 153 */     gui.setItem(0, getItem("s"));
/* 154 */     gui.setItem(1, getItem("t"));
/* 155 */     gui.setItem(2, getItem("u"));
/* 156 */     gui.setItem(3, getItem("v"));
/* 157 */     gui.setItem(4, getItem("w"));
/* 158 */     gui.setItem(5, getItem("x"));
/* 159 */     gui.setItem(6, getItem("y"));
/* 160 */     gui.setItem(7, getItem("z"));
/*     */     
/* 162 */     player.openInventory(gui);
/*     */   }
/*     */   
/*     */   public void use_c(Player player) {
/* 166 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 27, FrameLandCreative.Color(this.config.getString("apple.gui-name")));
/*     */     
/* 168 */     gui.setItem(0, getItem("aa"));
/* 169 */     gui.setItem(1, getItem("ab"));
/* 170 */     gui.setItem(2, getItem("ac"));
/* 171 */     gui.setItem(3, getItem("ad"));
/*     */     
/* 173 */     player.openInventory(gui);
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Items\APPLE_OWO.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */