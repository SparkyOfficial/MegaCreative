/*    */ package deadpool23232.framelandcreative.GUI.Games.Functions.Filter;
/*    */ import deadpool23232.framelandcreative.Configs.WorldList;
/*    */ import java.util.HashMap;
/*    */ import java.util.LinkedList;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class MaxPlayers {
/*  9 */   static FileConfiguration worldList = WorldList.get();
/*    */   public static List<String> getMax() {
/* 11 */     HashMap<String, Integer> hm = new HashMap<>();
/*    */ 
/*    */     
/* 14 */     worldList.getConfigurationSection("unique").getKeys(false).forEach(key -> hm.put(key, Integer.valueOf(worldList.getInt("unique." + key))));
/*    */ 
/*    */     
/* 17 */     Map<String, Integer> hm1 = sortByValue(hm);
/*    */     
/* 19 */     List<String> list = new ArrayList<>();
/* 20 */     for (Map.Entry<String, Integer> en : hm1.entrySet()) {
/* 21 */       list.add(en.getKey());
/*    */     }
/* 23 */     return list;
/*    */   }
/*    */ 
/*    */   
/*    */   public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
/* 28 */     List<Map.Entry<String, Integer>> list = new LinkedList<>(hm.entrySet());
/*    */ 
/*    */     
/* 31 */     list.sort((o1, o2) -> ((Integer)o2.getValue()).compareTo((Integer)o1.getValue()));
/*    */ 
/*    */     
/* 34 */     HashMap<String, Integer> temp = new LinkedHashMap<>();
/* 35 */     for (Map.Entry<String, Integer> aa : list) {
/* 36 */       temp.put(aa.getKey(), aa.getValue());
/*    */     }
/* 38 */     return temp;
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\GUI\Games\Functions\Filter\MaxPlayers.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */