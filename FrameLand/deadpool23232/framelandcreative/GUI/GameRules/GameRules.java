/*     */ package deadpool23232.framelandcreative.GUI.GameRules;
/*     */ 
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import deadpool23232.framelandcreative.Configs.RuleConfig;
/*     */ import deadpool23232.framelandcreative.Configs.WorldData;
/*     */ import deadpool23232.framelandcreative.FrameLandCreative;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.InventoryHolder;
/*     */ import org.bukkit.inventory.ItemFlag;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.meta.ItemMeta;
/*     */ 
/*     */ 
/*     */ public class GameRules
/*     */ {
/*  22 */   static FileConfiguration worldConfig = WorldData.get();
/*  23 */   static FileConfiguration dataConfig = DataConfig.get();
/*  24 */   static FileConfiguration ruleConfig = RuleConfig.get();
/*  25 */   static FileConfiguration config = FrameLandCreative.getInstance().getConfigFile();
/*     */   
/*     */   public static void main(Player player, String id) {
/*  28 */     Inventory gui = Bukkit.createInventory((InventoryHolder)player, 45, FrameLandCreative.Color("&2Правила игры"));
/*     */     
/*  30 */     ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
/*  31 */     ItemMeta glass1 = glass.getItemMeta();
/*  32 */     glass1.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE });
/*  33 */     glass1.setDisplayName(FrameLandCreative.Color("&7"));
/*  34 */     glass.setItemMeta(glass1);
/*     */     
/*  36 */     ItemStack rule1Item = new ItemStack(Material.EXP_BOTTLE);
/*  37 */     ItemMeta rule1Meta = rule1Item.getItemMeta();
/*  38 */     rule1Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.randomTickSpeed")));
/*     */     
/*  40 */     ItemStack rule2Item = new ItemStack(Material.DOUBLE_PLANT);
/*  41 */     ItemMeta rule2Meta = rule2Item.getItemMeta();
/*  42 */     rule2Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.doDaylightCycle")));
/*     */     
/*  44 */     ItemStack rule3Item = new ItemStack(Material.PORK);
/*  45 */     ItemMeta rule3Meta = rule3Item.getItemMeta();
/*  46 */     rule3Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.doEntityDrops")));
/*     */     
/*  48 */     ItemStack rule4Item = new ItemStack(Material.BONE);
/*  49 */     ItemMeta rule4Meta = rule4Item.getItemMeta();
/*  50 */     rule4Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.showDeathMessages")));
/*     */     
/*  52 */     ItemStack rule5Item = new ItemStack(Material.APPLE);
/*  53 */     ItemMeta rule5Meta = rule5Item.getItemMeta();
/*  54 */     rule5Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.naturalRegeneration")));
/*     */     
/*  56 */     ItemStack rule6Item = new ItemStack(Material.IRON_AXE);
/*  57 */     ItemMeta rule6Meta = rule6Item.getItemMeta();
/*  58 */     rule6Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.mobGriefing")));
/*     */     
/*  60 */     ItemStack rule7Item = new ItemStack(Material.GOLDEN_APPLE);
/*  61 */     ItemMeta rule7Meta = rule7Item.getItemMeta();
/*  62 */     rule7Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.keepInventory")));
/*     */     
/*  64 */     ItemStack rule8Item = new ItemStack(Material.WOOL);
/*  65 */     ItemMeta rule8Meta = rule8Item.getItemMeta();
/*  66 */     rule8Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.doWeatherCycle")));
/*     */     
/*  68 */     ItemStack rule9Item = new ItemStack(Material.ROTTEN_FLESH);
/*  69 */     ItemMeta rule9Meta = rule9Item.getItemMeta();
/*  70 */     rule9Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.doMobLoot")));
/*     */     
/*  72 */     ItemStack rule10Item = new ItemStack(Material.DIRT);
/*  73 */     ItemMeta rule10Meta = rule10Item.getItemMeta();
/*  74 */     rule10Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.doTileDrops")));
/*     */     
/*  76 */     ItemStack rule11Item = new ItemStack(Material.FLINT_AND_STEEL);
/*  77 */     ItemMeta rule11Meta = rule11Item.getItemMeta();
/*  78 */     rule11Meta.setDisplayName(FrameLandCreative.Color(config.getString("gameRules.doFireTick")));
/*     */     
/*  80 */     ItemStack back = new ItemStack(Material.ARROW);
/*  81 */     ItemMeta back1 = back.getItemMeta();
/*  82 */     back1.setDisplayName(FrameLandCreative.Color(config.getString("back.name")));
/*  83 */     List<String> back2 = new ArrayList<>();
/*  84 */     for (String line : config.getStringList("back.desc")) {
/*  85 */       back2.add(FrameLandCreative.Color(line));
/*     */     }
/*  87 */     back1.setLore(back2);
/*  88 */     back.setItemMeta(back1);
/*     */ 
/*     */     
/*  91 */     List<String> rule1Lore = new ArrayList<>();
/*  92 */     for (String line : config.getStringList("gameRules.rts-description")) {
/*  93 */       rule1Lore.add(FrameLandCreative.Color(line
/*  94 */             .replace("#value#", RuleConfig.get().getString(id + ".randomTickSpeed"))));
/*     */     }
/*     */     
/*  97 */     rule1Meta.setLore(rule1Lore);
/*     */ 
/*     */     
/* 100 */     List<String> rule2Lore = new ArrayList<>();
/* 101 */     for (String line : config.getStringList("gameRules.ddlc-description")) {
/* 102 */       rule2Lore.add(FrameLandCreative.Color(line
/* 103 */             .replace("#value#", RuleConfig.get().getString(id + ".doDaylightCycle"))));
/*     */     }
/*     */     
/* 106 */     rule2Meta.setLore(rule2Lore);
/*     */ 
/*     */     
/* 109 */     List<String> rule3Lore = new ArrayList<>();
/* 110 */     for (String line : config.getStringList("gameRules.ded-description")) {
/* 111 */       rule3Lore.add(FrameLandCreative.Color(line
/* 112 */             .replace("#value#", RuleConfig.get().getString(id + ".doEntityDrops"))));
/*     */     }
/*     */     
/* 115 */     rule3Meta.setLore(rule3Lore);
/*     */ 
/*     */     
/* 118 */     List<String> rule4Lore = new ArrayList<>();
/* 119 */     for (String line : config.getStringList("gameRules.sdm-description")) {
/* 120 */       rule4Lore.add(FrameLandCreative.Color(line
/* 121 */             .replace("#value#", RuleConfig.get().getString(id + ".showDeathMessages"))));
/*     */     }
/*     */     
/* 124 */     rule4Meta.setLore(rule4Lore);
/*     */ 
/*     */     
/* 127 */     List<String> rule5Lore = new ArrayList<>();
/* 128 */     for (String line : config.getStringList("gameRules.nr-description")) {
/* 129 */       rule5Lore.add(FrameLandCreative.Color(line
/* 130 */             .replace("#value#", RuleConfig.get().getString(id + ".naturalRegeneration"))));
/*     */     }
/*     */     
/* 133 */     rule5Meta.setLore(rule5Lore);
/*     */ 
/*     */     
/* 136 */     List<String> rule6Lore = new ArrayList<>();
/* 137 */     for (String line : config.getStringList("gameRules.mg-description")) {
/* 138 */       rule6Lore.add(FrameLandCreative.Color(line
/* 139 */             .replace("#value#", RuleConfig.get().getString(id + ".mobGriefing"))));
/*     */     }
/*     */     
/* 142 */     rule6Meta.setLore(rule6Lore);
/*     */ 
/*     */     
/* 145 */     List<String> rule7Lore = new ArrayList<>();
/* 146 */     for (String line : config.getStringList("gameRules.ki-description")) {
/* 147 */       rule7Lore.add(FrameLandCreative.Color(line
/* 148 */             .replace("#value#", RuleConfig.get().getString(id + ".keepInventory"))));
/*     */     }
/*     */     
/* 151 */     rule7Meta.setLore(rule7Lore);
/*     */ 
/*     */     
/* 154 */     List<String> rule8Lore = new ArrayList<>();
/* 155 */     for (String line : config.getStringList("gameRules.dwc-description")) {
/* 156 */       rule8Lore.add(FrameLandCreative.Color(line
/* 157 */             .replace("#value#", RuleConfig.get().getString(id + ".doMobLoot"))));
/*     */     }
/*     */     
/* 160 */     rule8Meta.setLore(rule8Lore);
/*     */ 
/*     */     
/* 163 */     List<String> rule9Lore = new ArrayList<>();
/* 164 */     for (String line : config.getStringList("gameRules.dml-description")) {
/* 165 */       rule9Lore.add(FrameLandCreative.Color(line
/* 166 */             .replace("#value#", RuleConfig.get().getString(id + ".doMobLoot"))));
/*     */     }
/*     */     
/* 169 */     rule9Meta.setLore(rule9Lore);
/*     */ 
/*     */     
/* 172 */     List<String> rule10Lore = new ArrayList<>();
/* 173 */     for (String line : config.getStringList("gameRules.dtd-description")) {
/* 174 */       rule10Lore.add(FrameLandCreative.Color(line
/* 175 */             .replace("#value#", RuleConfig.get().getString(id + ".doTileDrops"))));
/*     */     }
/*     */     
/* 178 */     rule10Meta.setLore(rule10Lore);
/*     */ 
/*     */     
/* 181 */     List<String> rule11Lore = new ArrayList<>();
/* 182 */     for (String line : config.getStringList("gameRules.dft-description")) {
/* 183 */       rule11Lore.add(FrameLandCreative.Color(line
/* 184 */             .replace("#value#", RuleConfig.get().getString(id + ".doFireTick"))));
/*     */     }
/*     */     
/* 187 */     rule11Meta.setLore(rule11Lore);
/*     */ 
/*     */ 
/*     */     
/* 191 */     rule1Item.setItemMeta(rule1Meta);
/* 192 */     rule2Item.setItemMeta(rule2Meta);
/* 193 */     rule3Item.setItemMeta(rule3Meta);
/* 194 */     rule4Item.setItemMeta(rule4Meta);
/* 195 */     rule5Item.setItemMeta(rule5Meta);
/* 196 */     rule6Item.setItemMeta(rule6Meta);
/* 197 */     rule7Item.setItemMeta(rule7Meta);
/* 198 */     rule8Item.setItemMeta(rule8Meta);
/* 199 */     rule9Item.setItemMeta(rule9Meta);
/* 200 */     rule10Item.setItemMeta(rule10Meta);
/* 201 */     rule11Item.setItemMeta(rule11Meta);
/*     */     
/* 203 */     gui.setItem(1, rule1Item);
/* 204 */     gui.setItem(19, rule2Item);
/* 205 */     gui.setItem(38, rule3Item);
/* 206 */     gui.setItem(3, rule4Item);
/* 207 */     gui.setItem(21, rule5Item);
/* 208 */     gui.setItem(40, rule6Item);
/* 209 */     gui.setItem(5, rule7Item);
/* 210 */     gui.setItem(23, rule8Item);
/* 211 */     gui.setItem(42, rule9Item);
/* 212 */     gui.setItem(7, rule10Item);
/* 213 */     gui.setItem(25, rule11Item);
/* 214 */     gui.setItem(36, back);
/*     */     
/* 216 */     gui.setItem(0, glass); gui.setItem(9, glass); gui.setItem(18, glass); gui.setItem(27, glass);
/* 217 */     gui.setItem(8, glass); gui.setItem(17, glass); gui.setItem(26, glass); gui.setItem(35, glass); gui.setItem(44, glass);
/*     */     
/* 219 */     player.openInventory(gui);
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\GameRules\GameRules.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */