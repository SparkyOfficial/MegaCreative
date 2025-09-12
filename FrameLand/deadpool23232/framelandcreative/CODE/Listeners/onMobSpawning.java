/*    */ package deadpool23232.framelandcreative.CODE.Listeners;
/*    */ 
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.entity.Entity;
/*    */ import org.bukkit.entity.LivingEntity;
/*    */ import org.bukkit.entity.Vehicle;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.entity.CreatureSpawnEvent;
/*    */ import org.bukkit.event.entity.EntitySpawnEvent;
/*    */ import org.bukkit.event.vehicle.VehicleCreateEvent;
/*    */ 
/*    */ public class onMobSpawning
/*    */   implements Listener
/*    */ {
/*    */   public boolean canSpawnEntity(Entity entity) {
/* 17 */     World world = entity.getWorld();
/* 18 */     int limit = 60;
/* 19 */     int currentCount = world.getEntities().size();
/*    */     
/* 21 */     return (currentCount > limit);
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void onEntitySpawn1(CreatureSpawnEvent event) {
/* 26 */     LivingEntity livingEntity = event.getEntity();
/* 27 */     if (livingEntity.getWorld().getName().contains("-code")) {
/* 28 */       event.setCancelled(true);
/*    */       return;
/*    */     } 
/* 31 */     if (canSpawnEntity((Entity)livingEntity)) {
/* 32 */       event.setCancelled(true);
/*    */     }
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void onEntitySpawn2(EntitySpawnEvent event) {
/* 38 */     Entity entity = event.getEntity();
/* 39 */     if (entity.getWorld().getName().contains("-code")) {
/* 40 */       event.setCancelled(true);
/*    */       return;
/*    */     } 
/* 43 */     if (canSpawnEntity(entity)) {
/* 44 */       event.setCancelled(true);
/*    */     }
/*    */   }
/*    */   
/*    */   @EventHandler
/*    */   public void onEntitySpawn3(VehicleCreateEvent event) {
/* 50 */     Vehicle vehicle = event.getVehicle();
/* 51 */     if (vehicle.getWorld().getName().contains("-code")) {
/* 52 */       event.setCancelled(true);
/*    */       return;
/*    */     } 
/* 55 */     if (canSpawnEntity((Entity)vehicle))
/* 56 */       event.setCancelled(true); 
/*    */   }
/*    */ }


/* Location:              C:\Users\Богдан\Downloads\FrameLandCreative.jar!\deadpool23232\framelandcreative\CODE\Listeners\onMobSpawning.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */