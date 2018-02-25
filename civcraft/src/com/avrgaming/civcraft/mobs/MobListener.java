package com.avrgaming.civcraft.mobs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigCustomMobs;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.ResidentExperience;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

import net.minecraft.server.v1_12_R1.Entity;

public class MobListener implements Listener {
	
/*	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkLoad(ChunkLoadEvent event) {
		for (Entity e : event.getChunk().getEntities()) {
			if (e instanceof Monster) {
				e.remove();
				return;
			}
			
			if (e instanceof IronGolem) {
				e.remove();
				return;
			}
		}
	}*/
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityCatchFire(EntityCombustEvent event) {
		if (event.getEntityType() == EntityType.ZOMBIE || event.getEntityType() == EntityType.ZOMBIE_VILLAGER || event.getEntityType() == EntityType.SKELETON) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onDefense(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) return;
		Entity mob = CustomMobListener.customMobs.get(event.getEntity().getUniqueId());
		if (mob == null) {
			return;
		}
		
		ConfigCustomMobs cmob = CivSettings.customMobs.get(ChatColor.stripColor(mob.getCustomName()).toString().toUpperCase().replaceAll(" ", "_"));
		// Entity invalid? Remove.
		if (cmob == null) {
			CivLog.warning("Invalid mob found? "+mob.getCustomName()+"; removed.");
			event.getEntity().remove();
			return;
		}
		
		double damage = event.getDamage() - cmob.defense_dmg;
		if (damage < 0.5) {
			Player player = null;
			if (event.getDamager() instanceof Arrow) {
				if ((Arrow)event.getDamager() instanceof Player) {
					player = (Player)((Arrow) event.getDamager()).getShooter();
				}
			} else if (event.getDamager() instanceof Player) {
				player = (Player)event.getDamager();
			}
			
			Random rand = new Random();
			if (rand.nextInt(2) == 0) {
				if (player != null) {
					damage = (rand.nextInt(3) / 2) + 0.5;
					CivMessage.send(player, CivColor.LightGray+"Attack grazed by "+damage+" HP");
				}
			} else {
				if (player != null) {
					damage *= -1;
					CivMessage.send(player, CivColor.LightGray+"Attack ineffective by "+damage+" HP");
					damage = 0.0;
				}
			}
		}
		event.setDamage(damage);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;
		Entity mob = CustomMobListener.customMobs.get(event.getEntity().getUniqueId());
		if (mob == null) return;
		
		switch (event.getCause()) {
		case SUFFOCATION:
			Location loc = event.getEntity().getLocation();
			int y = loc.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ()).getY()+4;
			loc.setY(y);
			event.getEntity().teleport(loc);
		case CONTACT:
		case FALL:
		case FIRE:
		case FIRE_TICK:
		case LAVA:
		case MELTING:
		case DROWNING:
		case FALLING_BLOCK:
		case BLOCK_EXPLOSION:
		case ENTITY_EXPLOSION:
		case LIGHTNING:
		case MAGIC:
			event.setCancelled(true);
			break;
		default:
			break;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		if (CustomMobListener.mobList.get(event.getEntity().getUniqueId()) != null) {
			ConfigCustomMobs cmob = CustomMobListener.mobList.get(event.getEntity().getUniqueId());
			Player p = event.getEntity().getKiller();
			ResidentExperience resE = CivGlobal.getResidentE(p);
			double mod = ((resE.getWeapondryLevel() + 1) * cmob.rxp_mod) / 2;
			try {
				DecimalFormat df = new DecimalFormat("0.00");
				double genrf = cmob.res_exp*mod;
				double rfEXP = Double.valueOf(df.format(genrf));
				resE.addWeapondryEXP(rfEXP);
			} catch (CivException e1) {
			}
			
			Random rand = new Random();
			int coins = (rand.nextInt(cmob.exp_max - cmob.exp_min) + cmob.exp_min);
			coins = (int) ((coins * mod) / 2);
			event.setDroppedExp(coins);
			
			event.getDrops().clear();
			ArrayList<String> dropped = new ArrayList<String>();
			for (String values : cmob.drops) { // Specific Mob's Drops
				String[] drops = values.split(",");
				double dc = Double.valueOf(drops[4]);
				int chance = rand.nextInt(10000);
				if (chance < (dc*10000)) {
					dropped.add(values);
				}
			}
			
/*			try {
				for (String values : CivSettings.getString(CivSettings.mobConfig, "common_drops").split(";")) { // Common Mob's Drops
					String[] drops = values.split(",");
					double dc = Double.valueOf(drops[4]);
					int chance = rand.nextInt(10000);
					if (chance < (dc*10000)) {
						dropped.add(values);
					}
				}
			} catch (NumberFormatException | InvalidConfiguration e) {
				CivLog.error("Could not get common_drops from mob.yml!");
				CivLog.error(e.getMessage());
			}*/
			
			if (dropped.size() != 0) {
				for (String items : dropped) {
					if (items == null) continue;
					String[] drops = items.split(",");
					String mat = drops[0]; mat = mat.replace("[", "").replace("]", "");
					int dropAmt;
					int dropMin = Integer.valueOf(drops[2]);
					int dropMax = Integer.valueOf(drops[3]);
					int amtToRand = dropMax - dropMin;
					if (amtToRand < dropMin) {
						dropAmt = dropMin;
					} else {
						dropAmt = rand.nextInt(amtToRand+1) + dropMin; // + (Integer.valueOf(drops[7])
						if (dropAmt > dropMax) {
							dropAmt = dropMax;
						}
					}
					
					CivMessage.global("min "+dropMin+" max "+dropMax+" amt"+dropAmt);
					
					if (dropAmt > 0) {
						LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(mat);
						if (craftMat != null) {
							ItemStack item = LoreMaterial.spawn(LoreMaterial.materialMap.get(craftMat.getConfigId()), dropAmt);
							event.getDrops().add(item);
						} else {
							ItemStack item = ItemManager.createItemStack(Integer.valueOf(mat), dropAmt, Short.valueOf(drops[1]));
						//	CivMessage.global(item.getType().toString());
							event.getDrops().add(item);
						}
					}
				}
			}
			
		}
	}
	
/*	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLeash(PlayerLeashEntityEvent event) {
		if (event.getEntity() instanceof LivingEntity) {
			CustomMobListener mob = CustomMobListener.customMobs.get(event.getEntity().getUniqueId());
			if (mob != null) {
				CivMessage.sendError(event.getPlayer(), "This beast cannot be tamed.");
				event.setCancelled(true);
				return;
			}
		}
	}*/
	
}
