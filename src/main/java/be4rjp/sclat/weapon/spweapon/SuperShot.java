
package be4rjp.sclat.weapon.spweapon;

import be4rjp.sclat.Main;
import static be4rjp.sclat.Main.conf;
import be4rjp.sclat.Sphere;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.manager.ArmorStandMgr;
import be4rjp.sclat.manager.DamageMgr;
import be4rjp.sclat.manager.DeathMgr;
import be4rjp.sclat.manager.PaintMgr;
import be4rjp.sclat.manager.SPWeaponMgr;
import be4rjp.sclat.manager.WeaponClassMgr;
import be4rjp.sclat.weapon.Gear;
import java.util.List;
import java.util.Random;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_13_R1.PlayerConnection;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author Be4rJP
 */
public class SuperShot {
    
    public static void setSuperShot(Player player){
        DataMgr.getPlayerData(player).setIsUsingSP(true);
        SPWeaponMgr.setSPCoolTimeAnimation(player, 120);
        
        BukkitRunnable it = new BukkitRunnable() {
            Player p = player;
            @Override
            public void run() {
                player.getInventory().clear();
                player.updateInventory();

                ItemStack item = new ItemStack(Material.SUGAR_CANE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("右クリックで発射！");
                item.setItemMeta(meta);
                for (int count = 0; count < 9; count++){
                    player.getInventory().setItem(count, item);
                    if(count % 2 != 0)
                        player.getInventory().setItem(count, new ItemStack(Material.AIR));
                }
                player.updateInventory();
            }
        };
        it.runTaskLater(Main.getPlugin(), 2);
        
        BukkitRunnable task = new BukkitRunnable() {
            Player p = player;
            @Override
            public void run() {
                DataMgr.getPlayerData(p).setIsUsingSP(false);
                player.getInventory().clear();
                WeaponClassMgr.setWeaponClass(p);
            }
        };
        task.runTaskLater(Main.getPlugin(), 120);
    }
    
    
    public static void Shot(Player player){
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.2F, 1.2F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.3F, 2F);
        
        Location ploc = player.getEyeLocation().add(0, -1, 0);
        Vector pvec = player.getEyeLocation().getDirection();
        Vector vec = new Vector(pvec.getX(), 0, pvec.getZ());
        Vector vv1 = new Vector(pvec.getZ() * -1, 0, pvec.getX()).normalize().multiply(0.3);
        Vector vv2 = new Vector(pvec.getZ(), 0, pvec.getX() * -1).normalize().multiply(0.3);
        Vector vec1 = new Vector(pvec.getX(), 0, pvec.getZ()).normalize().multiply(1);
        Vector vec2 = new Vector(pvec.getX(), 0, pvec.getZ()).normalize().multiply(1.3);
        Vector vec3 = new Vector(pvec.getX(), 0, pvec.getZ()).normalize().multiply(1.6);
        Location loc1 = ploc.clone().add(vec1);
        Location loc2 = ploc.clone().add(vec2);
        Location loc3 = ploc.clone().add(vec3);
        Location loc4 = loc2.clone().add(vv1);
        Location loc5 = loc2.clone().add(vv2);
        
        player.setVelocity(vec.clone().multiply(-0.5));
        
        for(double y = 0; y <= 9; y+=0.5){
            ShootSnowball(player, loc1.clone().add(0, y, 0), vec);
            ShootSnowball(player, loc3.clone().add(0, y, 0), vec);
            ShootSnowball(player, loc4.clone().add(0, y, 0), vec);
            ShootSnowball(player, loc5.clone().add(0, y, 0), vec);
        }
        
        BukkitRunnable task = new BukkitRunnable() {
            Player p = player;
            @Override
            public void run() {
                DataMgr.getPlayerData(p).setCanUseSubWeapon(true);
            }
        };
        task.runTaskLater(Main.getPlugin(), 20);
    }
    
    public static void ShootSnowball(Player player, Location loc, Vector vec){
        BukkitRunnable task = new BukkitRunnable(){
            Player p = player;
            boolean block_check = false;
            int c = 0;
            Item drop;
            Snowball ball;
            @Override
            public void run(){
                try{
                    if(c == 0){
                        ItemStack i = new ItemStack(DataMgr.getPlayerData(p).getTeam().getTeamColor().getWool()).clone();
                        ItemMeta i_m = i.getItemMeta();
                        i_m.setLocalizedName(String.valueOf(Main.getNotDuplicateNumber()));
                        i.setItemMeta(i_m);
                        drop = p.getWorld().dropItem(loc, i);
                        drop.setVelocity(vec);
                        //雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                        ball = (Snowball)player.getWorld().spawnEntity(loc, EntityType.SNOWBALL);
                        ball.setVelocity(vec);
                        ball.setCustomName("SuperShot");
                        ball.setShooter(p);
                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                            PlayerConnection connection = ((CraftPlayer) o_player).getHandle().playerConnection;
                            connection.sendPacket(new PacketPlayOutEntityDestroy(ball.getEntityId()));
                        }
                    }
                    drop.setVelocity(ball.getVelocity());
                    
                    PaintMgr.PaintHightestBlock(ball.getLocation(), p, false, false);
                    
                    
                    if(new Random().nextInt(20) == 0){
                        org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(p).getTeam().getTeamColor().getWool().createBlockData();
                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if(DataMgr.getPlayerData(o_player).getSettings().ShowEffect_Shooter())
                                if(o_player.getWorld() == ball.getWorld())
                                    if(o_player.getLocation().distance(ball.getLocation()) < conf.getConfig().getInt("ParticlesRenderDistance"))
                                        o_player.spawnParticle(org.bukkit.Particle.BLOCK_DUST, ball.getLocation(), 1, 0, 0, 0, 1, bd);
                        }
                    }
                    
                    if(ball.isDead() || drop.isDead() || !p.isOnline() || !DataMgr.getPlayerData(p).isInMatch()){
                        ball.remove();
                        drop.remove();
                        cancel();
                    }
                    
                    c++;
                }catch(Exception e){
                    drop.remove();
                    cancel();
                    Main.getPlugin().getLogger().warning(e.getMessage());
                }
            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 1);
    }
}