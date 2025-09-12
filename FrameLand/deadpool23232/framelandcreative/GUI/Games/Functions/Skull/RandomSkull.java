/*    */ package deadpool23232.framelandcreative.GUI.Games.Functions.Skull;
/*    */ 
/*    */ import java.util.concurrent.ThreadLocalRandom;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ 
/*    */ 
/*    */ public class RandomSkull
/*    */ {
/*    */   public static ItemStack skull() {
/* 10 */     int random = ThreadLocalRandom.current().nextInt(1, 18);
/* 11 */     String wool = "http://textures.minecraft.net/texture/15ad93d56546f12d5356effcbc6ec4c87ba245d81e1662c4b830f7d298e9";
/* 12 */     switch (random) {
/*    */       case 1:
/* 14 */         wool = "http://textures.minecraft.net/texture/d0883650ea929db0eabdf5bc75599d8ef00d70340cd1ce5e04ad95ef8ed83b73";
/*    */         break;
/*    */       case 2:
/* 17 */         wool = "http://textures.minecraft.net/texture/f97012ed6a92b05ea0f194950748544e075baa28781ca373d1b27e28c26953c";
/*    */         break;
/*    */       case 3:
/* 20 */         wool = "http://textures.minecraft.net/texture/89ec5a30222d0659b0dbee844b8f53eae62fe95b4a3448a9ef790a7aedb296d9";
/*    */         break;
/*    */       case 4:
/* 23 */         wool = "http://textures.minecraft.net/texture/ea81fcb51be2a9f89b1adc9d87239ba429d635fbe01b37ec329164887bf665b";
/*    */         break;
/*    */       case 5:
/* 26 */         wool = "http://textures.minecraft.net/texture/163e6646f1c0d41fd3bf5584a1ce044f5c46d598258db46216117859f57af197";
/*    */         break;
/*    */       case 6:
/* 29 */         wool = "http://textures.minecraft.net/texture/53581c2f9cf358d7edc78dd6fd4b6257501bc4e6455e33fa0caae207cf0321a2";
/*    */         break;
/*    */       case 7:
/* 32 */         wool = "http://textures.minecraft.net/texture/77472d608821f45a8805376ec0c6ffcb78117829ea5f960041c2a09d10e04cb4";
/*    */         break;
/*    */       case 8:
/* 35 */         wool = "http://textures.minecraft.net/texture/4d905269accab24b11924eba8bd92991b8d85ce4276027a1636c931b6d06c89e";
/*    */         break;
/*    */       case 9:
/* 38 */         wool = "http://textures.minecraft.net/texture/adf2eb205a23c1196b3ecf21e68c076b696e76163ac8fc4fb9f5318c2a5e5b1a";
/*    */         break;
/*    */       case 10:
/* 41 */         wool = "http://textures.minecraft.net/texture/6953b12a0946b629b4c0889d41fd26ed26fb729d4d514b59727124c37bb70d8d";
/*    */         break;
/*    */       case 11:
/* 44 */         wool = "http://textures.minecraft.net/texture/22cbd9f43619ab5cb1b11f91cb03e955c6fc6c458abf89ab61031346a090612e";
/*    */         break;
/*    */       case 12:
/* 47 */         wool = "http://textures.minecraft.net/texture/cfa4dda6d19a1fe2d988d65dec53429505308166c9067b68a4770ca5c436cf94";
/*    */         break;
/*    */       case 13:
/* 50 */         wool = "http://textures.minecraft.net/texture/adf21f532122566af893da27880a1b6095c35712f29a378cfecc7fe2b1328ab4";
/*    */         break;
/*    */       case 14:
/* 53 */         wool = "http://textures.minecraft.net/texture/55288ddc911a75f77c3a5d336365a8f8b139fa53930b4b6ee139875c80ce366c";
/*    */         break;
/*    */       case 15:
/* 56 */         wool = "http://textures.minecraft.net/texture/d08df60c51074eef2544ff38cead9e16675ae4251916105180e1f8ce197ab3bc";
/*    */         break;
/*    */       case 16:
/* 59 */         wool = "http://textures.minecraft.net/texture/d83288620617bd5cedc8fdb7133cfad231ce25c13cb8726bbf76e5c72fe732ab";
/*    */         break;
/*    */       case 17:
/* 62 */         wool = "http://textures.minecraft.net/texture/e9127cb7bd3a989d72c2e5c426e1cc1446b5e6de7414d42783f2fe6badb177d4";
/*    */         break;
/*    */     } 
/*    */     
/* 66 */     return getSkull(wool);
/*    */   }
/*    */   public static ItemStack getSkull(String url) {
/* 69 */     return SkullCreator.itemFromUrl(url);
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Functions\Skull\RandomSkull.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */