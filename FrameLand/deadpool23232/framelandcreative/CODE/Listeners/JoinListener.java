/*     */ package deadpool23232.framelandcreative.CODE.Listeners;
/*     */ 
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.EventPriority;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.player.PlayerChangedWorldEvent;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.InventoryHolder;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ import org.bukkit.scheduler.BukkitRunnable;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class JoinListener
/*     */   implements Listener
/*     */ {
/*  25 */   public FileConfiguration config = FrameLandCreative.getInstance().getConfig();
/*     */   
/*     */   @EventHandler(priority = EventPriority.LOWEST)
/*     */   public void itemsOnJoin(PlayerChangedWorldEvent event) {
/*  29 */     if (event.getPlayer().getWorld().getName().contains("-code")) {
/*     */       
/*  31 */       final Player player = event.getPlayer();
/*  32 */       Inventory inventory = Bukkit.createInventory((InventoryHolder)player, 36, "code-inv");
/*     */       
/*  34 */       ItemStack diamond = new ItemStack(Material.DIAMOND_BLOCK);
/*  35 */       ItemStack cobble = new ItemStack(Material.COBBLESTONE);
/*  36 */       ItemStack wood = new ItemStack(Material.WOOD);
/*  37 */       ItemStack nether = new ItemStack(Material.NETHER_BRICK);
/*     */       
/*  39 */       ItemStack lapisBlock = new ItemStack(Material.LAPIS_BLOCK);
/*     */       
/*  41 */       ItemStack enderStone = new ItemStack(Material.ENDER_STONE);
/*  42 */       ItemStack piston = new ItemStack(Material.PISTON_BASE);
/*     */ 
/*     */ 
/*     */       
/*  46 */       ItemStack values = new ItemStack(Material.IRON_INGOT);
/*     */       
/*  48 */       ItemStack codeSwap = new ItemStack(Material.REDSTONE_COMPARATOR);
/*  49 */       ItemStack arrowNO = new ItemStack(Material.ARROW);
/*  50 */       ItemStack codeFloors = new ItemStack(Material.LADDER);
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  55 */       ItemMeta diamondMeta = diamond.getItemMeta();
/*  56 */       diamondMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.blocks.diamond.name")));
/*  57 */       List<String> diamondLore = new ArrayList<>();
/*  58 */       for (String line : this.config.getStringList("CODE.blocks.diamond.desc"))
/*  59 */         diamondLore.add(FrameLandCreative.Color(line)); 
/*  60 */       diamondMeta.setLore(diamondLore);
/*  61 */       diamond.setItemMeta(diamondMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  67 */       ItemMeta cobbleMeta = cobble.getItemMeta();
/*  68 */       cobbleMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.blocks.cobblestone.name")));
/*  69 */       List<String> cobbleLore = new ArrayList<>();
/*  70 */       for (String line : this.config.getStringList("CODE.blocks.cobblestone.desc"))
/*  71 */         cobbleLore.add(FrameLandCreative.Color(line)); 
/*  72 */       cobbleMeta.setLore(cobbleLore);
/*  73 */       cobble.setItemMeta(cobbleMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  79 */       ItemMeta woodMeta = wood.getItemMeta();
/*  80 */       woodMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.blocks.wood.name")));
/*  81 */       List<String> woodLore = new ArrayList<>();
/*  82 */       for (String line : this.config.getStringList("CODE.blocks.wood.desc"))
/*  83 */         woodLore.add(FrameLandCreative.Color(line)); 
/*  84 */       woodMeta.setLore(woodLore);
/*  85 */       wood.setItemMeta(woodMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  91 */       ItemMeta netherMeta = nether.getItemMeta();
/*  92 */       netherMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.blocks.nether-brick.name")));
/*  93 */       List<String> netherLore = new ArrayList<>();
/*  94 */       for (String line : this.config.getStringList("CODE.blocks.nether-brick.desc"))
/*  95 */         netherLore.add(FrameLandCreative.Color(line)); 
/*  96 */       netherMeta.setLore(netherLore);
/*  97 */       nether.setItemMeta(netherMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 103 */       ItemMeta lapisBlockMeta = lapisBlock.getItemMeta();
/* 104 */       lapisBlockMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.blocks.lapis.block.name")));
/* 105 */       List<String> lapisBlockLore = new ArrayList<>();
/* 106 */       for (String line : this.config.getStringList("CODE.blocks.lapis.block.desc"))
/* 107 */         lapisBlockLore.add(FrameLandCreative.Color(line)); 
/* 108 */       lapisBlockMeta.setLore(lapisBlockLore);
/* 109 */       lapisBlock.setItemMeta(lapisBlockMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 115 */       ItemMeta enderStoneMeta = enderStone.getItemMeta();
/* 116 */       enderStoneMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.blocks.ender-stone.name")));
/* 117 */       List<String> enderStoneLore = new ArrayList<>();
/* 118 */       for (String line : this.config.getStringList("CODE.blocks.ender-stone.desc"))
/* 119 */         enderStoneLore.add(FrameLandCreative.Color(line)); 
/* 120 */       enderStoneMeta.setLore(enderStoneLore);
/* 121 */       enderStone.setItemMeta(enderStoneMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 127 */       ItemMeta pistonMeta = piston.getItemMeta();
/* 128 */       pistonMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.blocks.piston.name")));
/* 129 */       List<String> pistonLore = new ArrayList<>();
/* 130 */       for (String line : this.config.getStringList("CODE.blocks.piston.desc"))
/* 131 */         pistonLore.add(FrameLandCreative.Color(line)); 
/* 132 */       pistonMeta.setLore(pistonLore);
/* 133 */       piston.setItemMeta(pistonMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 139 */       ItemMeta valuesMeta = values.getItemMeta();
/* 140 */       valuesMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.items.values.name")));
/* 141 */       List<String> valuesLore = new ArrayList<>();
/* 142 */       for (String line : this.config.getStringList("CODE.items.values.desc"))
/* 143 */         valuesLore.add(FrameLandCreative.Color(line)); 
/* 144 */       valuesMeta.setLore(valuesLore);
/* 145 */       values.setItemMeta(valuesMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 151 */       ItemMeta codeSwapMeta = codeSwap.getItemMeta();
/* 152 */       codeSwapMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.items.codeSwap.name")));
/* 153 */       List<String> codeSwapLore = new ArrayList<>();
/* 154 */       for (String line : this.config.getStringList("CODE.items.codeSwap.desc"))
/* 155 */         codeSwapLore.add(FrameLandCreative.Color(line)); 
/* 156 */       codeSwapMeta.setLore(codeSwapLore);
/* 157 */       codeSwap.setItemMeta(codeSwapMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 163 */       ItemMeta arrowNOMeta = arrowNO.getItemMeta();
/* 164 */       arrowNOMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.items.arrowNO.name")));
/* 165 */       List<String> arrowNOLore = new ArrayList<>();
/* 166 */       for (String line : this.config.getStringList("CODE.items.arrowNO.desc"))
/* 167 */         arrowNOLore.add(FrameLandCreative.Color(line)); 
/* 168 */       arrowNOMeta.setLore(arrowNOLore);
/* 169 */       arrowNO.setItemMeta(arrowNOMeta);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 175 */       ItemMeta codeFloorsMeta = codeFloors.getItemMeta();
/* 176 */       codeFloorsMeta.setDisplayName(FrameLandCreative.Color(this.config.getString("CODE.items.codeFloors.name")));
/* 177 */       List<String> codeFloorsLore = new ArrayList<>();
/* 178 */       for (String line : this.config.getStringList("CODE.items.codeFloors.desc"))
/* 179 */         codeFloorsLore.add(FrameLandCreative.Color(line)); 
/* 180 */       codeFloorsMeta.setLore(codeFloorsLore);
/* 181 */       codeFloors.setItemMeta(codeFloorsMeta);
/*     */ 
/*     */       
/* 184 */       inventory.setItem(0, diamond);
/* 185 */       inventory.setItem(1, cobble);
/* 186 */       inventory.setItem(2, wood);
/* 187 */       inventory.setItem(3, nether);
/* 188 */       inventory.setItem(8, values);
/*     */       
/* 190 */       inventory.setItem(35, codeSwap);
/* 191 */       inventory.setItem(34, arrowNO);
/* 192 */       inventory.setItem(17, codeFloors);
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 197 */       inventory.setItem(27, lapisBlock);
/* 198 */       inventory.setItem(29, enderStone);
/* 199 */       inventory.setItem(7, piston);
/*     */       
/* 201 */       final ItemStack[] itemStacks = inventory.getContents();
/* 202 */       if (!player.getInventory().contains(diamond) && 
/* 203 */         !player.getInventory().contains(cobble) && 
/* 204 */         !player.getInventory().contains(wood) && 
/* 205 */         !player.getInventory().contains(nether) && 
/* 206 */         !player.getInventory().contains(values) && 
/* 207 */         !player.getInventory().contains(codeSwap) && 
/* 208 */         !player.getInventory().contains(arrowNO) && 
/* 209 */         !player.getInventory().contains(codeFloors) && 
/* 210 */         !player.getInventory().contains(lapisBlock) && 
/* 211 */         !player.getInventory().contains(enderStone))
/* 212 */         (new BukkitRunnable()
/*     */           {
/*     */             public void run() {
/* 215 */               player.getInventory().setContents(itemStacks);
/*     */             }
/* 217 */           }).runTaskLater(Bukkit.getPluginManager().getPlugin("FrameLandCreative"), 4L); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Listeners\JoinListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */