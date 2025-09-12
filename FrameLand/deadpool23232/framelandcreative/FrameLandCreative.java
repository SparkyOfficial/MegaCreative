/*     */ package deadpool23232.framelandcreative;
/*     */ import deadpool23232.framelandcreative.CODE.Blocks.codeSwapper;
/*     */ import deadpool23232.framelandcreative.CODE.Blocks.onBreak;
/*     */ import deadpool23232.framelandcreative.CODE.CodeCompiler.runCode;
/*     */ import deadpool23232.framelandcreative.CODE.Functions.NETHER_BRICK.GUI_Listener;
/*     */ import deadpool23232.framelandcreative.CODE.Functions.OnChestOpen;
/*     */ import deadpool23232.framelandcreative.CODE.Functions.OpenBySign;
/*     */ import deadpool23232.framelandcreative.CODE.Functions.PlayerSelect;
/*     */ import deadpool23232.framelandcreative.CODE.Functions.WOOD.GUI_Listener;
/*     */ import deadpool23232.framelandcreative.CODE.Items.Variables;
/*     */ import deadpool23232.framelandcreative.CODE.Items.arrowNO;
/*     */ import deadpool23232.framelandcreative.CODE.Items.coordsSet;
/*     */ import deadpool23232.framelandcreative.CODE.Listeners.onMobSpawning;
/*     */ import deadpool23232.framelandcreative.Commands.Code;
/*     */ import deadpool23232.framelandcreative.Commands.Hub;
/*     */ import deadpool23232.framelandcreative.Commands.Like;
/*     */ import deadpool23232.framelandcreative.Commands.Settings;
/*     */ import deadpool23232.framelandcreative.Commands.WorldTp;
/*     */ import deadpool23232.framelandcreative.Configs.CodeFloors;
/*     */ import deadpool23232.framelandcreative.Configs.DataConfig;
/*     */ import deadpool23232.framelandcreative.Configs.Inventories;
/*     */ import deadpool23232.framelandcreative.Configs.PlayerData;
/*     */ import deadpool23232.framelandcreative.Configs.RuleConfig;
/*     */ import deadpool23232.framelandcreative.Configs.WorldCode;
/*     */ import deadpool23232.framelandcreative.Configs.WorldData;
/*     */ import deadpool23232.framelandcreative.Configs.WorldList;
/*     */ import deadpool23232.framelandcreative.Functions.onJoin;
/*     */ import deadpool23232.framelandcreative.GUI.BlackList.Listeners.LeftListener;
/*     */ import deadpool23232.framelandcreative.GUI.BlackList.Listeners.RightListener;
/*     */ import deadpool23232.framelandcreative.GUI.GameRules.randomTickSpeed;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.Liked.Listener;
/*     */ import deadpool23232.framelandcreative.GUI.Games.Pages.Own.Listener;
/*     */ import deadpool23232.framelandcreative.GUI.WhiteList.Listeners.LeftListener;
/*     */ import deadpool23232.framelandcreative.GUI.WorldSettings.Display.ID;
/*     */ import deadpool23232.framelandcreative.GUI.WorldSettings.Display.Name;
/*     */ import deadpool23232.framelandcreative.Map.UniquePlayer;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import org.bukkit.command.CommandExecutor;
/*     */ import org.bukkit.command.CommandSender;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.plugin.Plugin;
/*     */ import org.bukkit.plugin.PluginManager;
/*     */ 
/*     */ public final class FrameLandCreative extends JavaPlugin implements Listener, CommandExecutor {
/*     */   static {
/*  49 */     System.setProperty("file.encoding", "UTF-8");
/*     */   }
/*     */   
/*     */   private static FrameLandCreative instance;
/*  53 */   FileConfiguration config = getConfig();
/*     */ 
/*     */   
/*     */   public static File file;
/*     */ 
/*     */ 
/*     */   
/*     */   public void onEnable() {
/*  61 */     instance = this;
/*  62 */     this.config.options().copyDefaults();
/*  63 */     saveDefaultConfig();
/*     */     
/*  65 */     DataConfig.setup();
/*  66 */     DataConfig.get().options().copyDefaults(true);
/*  67 */     DataConfig.save();
/*  68 */     WorldData.setup();
/*  69 */     WorldData.get().options().copyDefaults(true);
/*  70 */     WorldData.save();
/*  71 */     RuleConfig.setup();
/*  72 */     RuleConfig.get().options().copyDefaults(true);
/*  73 */     RuleConfig.save();
/*  74 */     WorldCode.setup();
/*  75 */     WorldCode.get().options().copyDefaults(true);
/*  76 */     WorldCode.save();
/*  77 */     WorldList.setup();
/*  78 */     WorldList.get().options().copyDefaults(true);
/*  79 */     WorldList.save();
/*  80 */     PlayerData.setup();
/*  81 */     PlayerData.get().options().copyDefaults(true);
/*  82 */     PlayerData.save();
/*  83 */     CodeFloors.setup();
/*  84 */     CodeFloors.get().options().copyDefaults(true);
/*  85 */     CodeFloors.save();
/*  86 */     Inventories.setup();
/*  87 */     Inventories.get().options().copyDefaults(true);
/*  88 */     Inventories.save();
/*  89 */     file = new File(Bukkit.getServer().getPluginManager().getPlugin("FrameLandCreative").getDataFolder(), "config.yml");
/*     */     
/*  91 */     PluginManager pm = getServer().getPluginManager();
/*  92 */     pm.registerEvents(this, (Plugin)this);
/*  93 */     pm.registerEvents((Listener)new Listener(), (Plugin)this);
/*  94 */     pm.registerEvents((Listener)new Listener(), (Plugin)this);
/*  95 */     pm.registerEvents((Listener)new UniquePlayer(), (Plugin)this);
/*  96 */     pm.registerEvents((Listener)new Description(), (Plugin)this);
/*  97 */     pm.registerEvents((Listener)new Name(), (Plugin)this);
/*  98 */     pm.registerEvents((Listener)new ID(), (Plugin)this);
/*  99 */     pm.registerEvents((Listener)new randomTickSpeed(), (Plugin)this);
/* 100 */     pm.registerEvents((Listener)new Listener(), (Plugin)this);
/* 101 */     pm.registerEvents((Listener)new Listener(), (Plugin)this);
/* 102 */     pm.registerEvents((Listener)new Listener(), (Plugin)this);
/* 103 */     pm.registerEvents((Listener)new ClosedInventory(), (Plugin)this);
/* 104 */     pm.registerEvents((Listener)new ScoreBoard(), (Plugin)this);
/* 105 */     pm.registerEvents((Listener)new onJoin(), (Plugin)this);
/* 106 */     pm.registerEvents((Listener)new Listener(), (Plugin)this);
/* 107 */     pm.registerEvents((Listener)new Listener(), (Plugin)this);
/* 108 */     pm.registerEvents((Listener)new Listener(), (Plugin)this);
/* 109 */     pm.registerEvents((Listener)new Listener(), (Plugin)this);
/* 110 */     pm.registerEvents((Listener)new SpawnPoint(), (Plugin)this);
/* 111 */     pm.registerEvents((Listener)new RightListener(), (Plugin)this);
/* 112 */     pm.registerEvents((Listener)new LeftListener(), (Plugin)this);
/* 113 */     pm.registerEvents((Listener)new RightListener(), (Plugin)this);
/* 114 */     pm.registerEvents((Listener)new LeftListener(), (Plugin)this);
/* 115 */     pm.registerEvents((Listener)new ItemOnJoin(), (Plugin)this);
/* 116 */     pm.registerEvents((Listener)new onBreak(), (Plugin)this);
/* 117 */     pm.registerEvents((Listener)new JoinListener(), (Plugin)this);
/* 118 */     pm.registerEvents((Listener)new onPlace(), (Plugin)this);
/* 119 */     pm.registerEvents((Listener)new Variables(), (Plugin)this);
/* 120 */     pm.registerEvents((Listener)new Listeners(), (Plugin)this);
/* 121 */     pm.registerEvents((Listener)new setVar(), (Plugin)this);
/* 122 */     pm.registerEvents((Listener)new GUI_Listener(), (Plugin)this);
/* 123 */     pm.registerEvents((Listener)new OpenBySign(), (Plugin)this);
/* 124 */     pm.registerEvents((Listener)new GUI_Listener(), (Plugin)this);
/* 125 */     pm.registerEvents((Listener)new runCompileListener(), (Plugin)this);
/* 126 */     pm.registerEvents((Listener)new runCode(), (Plugin)this);
/* 127 */     pm.registerEvents((Listener)new GUI_Listener(), (Plugin)this);
/* 128 */     pm.registerEvents((Listener)new GUI_Listener(), (Plugin)this);
/* 129 */     pm.registerEvents((Listener)new codeSwapper(), (Plugin)this);
/* 130 */     pm.registerEvents((Listener)new PlayerSelect(), (Plugin)this);
/* 131 */     pm.registerEvents((Listener)new PlSel_GUILISTENER(), (Plugin)this);
/* 132 */     pm.registerEvents((Listener)new setName(), (Plugin)this);
/* 133 */     pm.registerEvents((Listener)new onMobSpawning(), (Plugin)this);
/* 134 */     pm.registerEvents((Listener)new arrowNO(), (Plugin)this);
/* 135 */     pm.registerEvents((Listener)new codeFloors(), (Plugin)this);
/* 136 */     pm.registerEvents((Listener)new ItemClick(), (Plugin)this);
/* 137 */     pm.registerEvents((Listener)new OnChestOpen(), (Plugin)this);
/* 138 */     pm.registerEvents((Listener)new coordsSet(), (Plugin)this);
/* 139 */     pm.registerEvents((Listener)new Hub(), (Plugin)this);
/* 140 */     pm.registerEvents((Listener)new InteractListener(), (Plugin)this);
/* 141 */     pm.registerEvents((Listener)new APPLE_OWO(), (Plugin)this);
/*     */     
/* 143 */     getCommand("code").setExecutor((CommandExecutor)new Code());
/* 144 */     getCommand("play").setExecutor((CommandExecutor)new World());
/* 145 */     getCommand("id").setExecutor((CommandExecutor)new WorldTp());
/* 146 */     getCommand("world").setExecutor((CommandExecutor)new Settings());
/* 147 */     getCommand("like").setExecutor((CommandExecutor)new Like());
/* 148 */     getCommand("games").setExecutor((CommandExecutor)new Games());
/* 149 */     getCommand("hub").setExecutor((CommandExecutor)new Hub());
/* 150 */     getCommand("delete").setExecutor((CommandExecutor)new Delete());
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
/* 155 */     Player player = (Player)sender;
/* 156 */     if (label.equalsIgnoreCase("create")) {
/* 157 */       if (player.hasPermission("framelandcreative.create")) {
/*     */         try {
/* 159 */           new onMapCreate(player);
/* 160 */         } catch (IOException e) {
/* 161 */           throw new RuntimeException(e);
/*     */         }
/*     */       
/*     */       }
/* 165 */     } else if (label.equalsIgnoreCase("test")) {
/*     */       
/* 167 */       player.sendMessage("tested ;P");
/*     */     } 
/*     */     
/* 170 */     return true;
/*     */   }
/*     */   
/*     */   public static String Color(String message) {
/* 174 */     return ChatColor.translateAlternateColorCodes('&', message);
/*     */   }
/* 176 */   public FileConfiguration getConfigFile() { return this.config; }
/* 177 */   public File getFile() { return file; } public static FrameLandCreative getInstance() {
/* 178 */     return instance;
/*     */   }
/*     */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\FrameLandCreative.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */