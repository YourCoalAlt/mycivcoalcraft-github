package com.avrgaming.civcraft.mobs;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import com.avrgaming.civcraft.config.ConfigCustomMobs;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.mobs.components.MobComponent;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.war.War;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.GenericAttributes;

public abstract class CustomMobListener {

	public static HashMap<UUID, Entity> customMobs = new HashMap<UUID, Entity>();
	public static HashMap<UUID, ConfigCustomMobs> mobList = new HashMap<UUID, ConfigCustomMobs>();
	public EntityLiving entity;
	
	public static HashMap<String, MobComponent> components = new HashMap<String, MobComponent>();
	
	// Anti-trap stuff
	private String targetName;
	private Location lastLocation;
	
	public Location getLocation(EntityLiving entity) {
		World world = Bukkit.getWorld(entity.world.getWorld().getName());
		Location loc = new Location(world, entity.locX, entity.locY, entity.locZ);
		return loc;
	}
	
	private void checkForStuck() {
		if (this.targetName != null && this.lastLocation != null) {
			Location loc = getLocation(entity);
			if (loc.distance(this.lastLocation) < 0.5) {
				Player player;
				try {
					player = CivGlobal.getPlayer(this.targetName);
					entity.getBukkitEntity().teleport(player.getLocation());
				} catch (CivException e) {
					// This player is no longer online. Lose target.
					this.targetName = null;
					this.lastLocation = null;
				}			
			}
			this.lastLocation = loc;
		}
	}
	
	private void checkForTownBorders() {
		Location loc = getLocation(entity);
		TownChunk tc = CivGlobal.getTownChunk(loc);
		if (tc != null && !tc.perms.isMobs()) {
			entity.getBukkitEntity().remove();
		}
	}
	
	private void checkForisWarTime() {
		if (War.isWarTime()) {
			entity.getBukkitEntity().remove();
		}
	}
	
	// TODO Does not run
	private int tickCount = 0;
	public void onTick() {
		if (entity == null) {
			return;
		}
		
		tickCount++;
		if (tickCount > 90) {
			checkForStuck();
			checkForTownBorders();
			checkForisWarTime();
			tickCount = 0;
		}
	}
	
	public EntityLiving getEntity() {
		return this.entity;
	}
	
	public void setEntity(EntityLiving e) {
		this.entity = e;
	}
	
	public static Collection<MobComponent> getMobComponents() {
		return components.values();
	}
	
	public void addComponent(MobComponent comp) {
		CustomMobListener.components.put(comp.getClass().getName(), comp);
	}
	
	public double getFollowRange() {
		double value;
		try {
			value = entity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).getValue();
		} catch (NullPointerException e) {
			value = 32.0D;
		}
		return value;
	}
	
	protected void printAttributes() {
		try {
			if (entity == null) {
				CivLog.info("Entity was null!");
			}
			CivLog.info("Speed:"+entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue());
			CivLog.info("MaxHealth:"+entity.getAttributeInstance(GenericAttributes.maxHealth).getValue()+" Health:"+entity.getHealth());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onTarget(EntityTargetEvent event) {
		if (event.isCancelled()) return;
		
		if ((event.getReason().equals(TargetReason.CLOSEST_PLAYER) || event.getReason().equals(TargetReason.OWNER_ATTACKED_TARGET)) && (event.getTarget() instanceof Player)) {
			double followRange = this.getFollowRange();
			double distance = event.getEntity().getLocation().distance(event.getTarget().getLocation());
			if ((distance-0.5) <= followRange) {
				this.targetName = ((Player)event.getTarget()).getName();
				this.lastLocation = event.getEntity().getLocation();
			}
		} else {
			this.targetName = null;
			this.lastLocation = null;
		}
	}
	
}
