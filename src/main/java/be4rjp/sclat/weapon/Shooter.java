
package be4rjp.sclat.weapon;

import be4rjp.sclat.Main;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.manager.PaintMgr;
import be4rjp.sclat.raytrace.RayTrace;
import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author Be4rJP
 */
public class Shooter {
    public static void ShooterRunnable(Player player){
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            Location loc = player.getLocation();
            int sl = 0;
            boolean check = true;
                            
            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(p);
                Location ploc = p.getLocation();
                boolean is = false;
                
                if(!data.isInMatch() || !p.isOnline()){
                    cancel();
                    return;
                }
                
                //マニューバー系
                if(data.getWeaponClass().getMainWeapon().getIsManeuver()){
                    if(data.getIsSneaking() && sl < 2){
                        Vector vec = p.getEyeLocation().getDirection();
                        Vector jvec = (new Vector(vec.getX(), 0, vec.getZ())).normalize().multiply(3);
                        p.setVelocity(jvec);
                        data.setIsSneaking(false);
                        sl++;
                        BukkitRunnable task = new BukkitRunnable(){
                            int i = 1;
                            @Override
                            public void run(){
                                if(i == 2)
                                    p.setVelocity(new Vector(0, 0, 0));
                                if(i == 4)
                                    loc = ploc;
                                if(i == 5)
                                    cancel();
                                i++;
                            }
                        };
                        task.runTaskTimer(Main.getPlugin(), 0, 1);
                        BukkitRunnable task1 = new BukkitRunnable(){
                            @Override
                            public void run(){
                                sl = 0;
                                check = true;
                            }
                        };
                        if(check)
                            task1.runTaskLater(Main.getPlugin(), 60);
                        check = false;
                    }
                }
                if(loc.getX() == ploc.getX() && loc.getZ() == ploc.getZ())
                    is = true;
                
                if(data.getTick() < 5 && data.isInMatch()){
                    Shooter.Shoot(p, is);
                    data.setTick(data.getTick() + DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getShootTick());
                }
                
                //loc = ploc;
            }
        };
        delay.runTaskTimer(Main.getPlugin(), 0, DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getShootTick());
    }
    
    public static void Shoot(Player player, boolean slided){
        PlayerData data = DataMgr.getPlayerData(player);
        if(player.getExp() <= data.getWeaponClass().getMainWeapon().getNeedInk()){
            player.sendTitle("", ChatColor.RED + "インクが足りません", 0, 5, 2);
            return;
        }
        player.setExp(player.getExp() - data.getWeaponClass().getMainWeapon().getNeedInk());
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(),player.getEyeLocation().getDirection());
        ArrayList<Vector> positions = rayTrace.traverse(data.getWeaponClass().getMainWeapon().getShootSpeed() * data.getWeaponClass().getMainWeapon().getDistanceTick(),0.7);
        boolean isLockOnPlayer = false;
        check : for(int i = 0; i < positions.size();i++){
            Location position = positions.get(i).toLocation(player.getLocation().getWorld());
            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                if(player != target && player.getWorld() == target.getWorld()){
                    if(target.getLocation().distance(position) < 2){
                        isLockOnPlayer = true;
                        break check;
                    }
                }
            }
        }
                    
        Snowball ball = player.launchProjectile(Snowball.class);
        player.playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3F, 1F);
                Vector vec = player.getLocation().getDirection().multiply(DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getShootSpeed());
                double random = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getRandom();
                if(isLockOnPlayer || slided)
                    random /= 2;
                int distick = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getDistanceTick();
                vec.add(new Vector(Math.random() * random - random/2, 0, Math.random() * random - random/2));
                ball.setVelocity(vec);
                ball.setShooter(player);
                BukkitRunnable task = new BukkitRunnable(){
                    int i = 0;
                    int tick = distick;
                    //Vector fallvec;
                    Vector origvec = vec;
                    Snowball inkball = ball;
                    Player p = player;
                    Vector fallvec = new Vector(inkball.getVelocity().getX(), inkball.getVelocity().getY()  , inkball.getVelocity().getZ()).multiply(DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getShootSpeed()/17);
                    @Override
                    public void run(){
                        org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(p).getTeam().getTeamColor().getWool().createBlockData();
                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if(DataMgr.getPlayerData(o_player).getSettings().ShowEffect_Shooter())
                                o_player.spawnParticle(org.bukkit.Particle.BLOCK_DUST, inkball.getLocation(), 1, 0, 0, 0, 1, bd);
                        }
                        
                        if(i == tick)
                            inkball.setVelocity(fallvec);
                        if(i >= tick)
                            inkball.setVelocity(inkball.getVelocity().add(new Vector(0, -0.1, 0)));
                        //if(i != tick)
                        PaintMgr.PaintHightestBlock(inkball.getLocation(), p, true, true);
                        if(inkball.isDead())
                            cancel();
                        
                        i++;
                    }
                };
                task.runTaskTimer(Main.getPlugin(), 0, 1);
    }
    
    
}
