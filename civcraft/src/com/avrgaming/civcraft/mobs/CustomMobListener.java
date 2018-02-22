package com.avrgaming.civcraft.mobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.ConfigCustomMobs;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.mobs.components.MobComponent;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.war.War;

import moblib.mob.ICustomMob;
import moblib.mob.ISpawnable;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.PathfinderGoalSelector;

public abstract class CustomMobListener implements ICustomMob {

	public static HashMap<UUID, CustomMobListener> customMobs = new HashMap<UUID, CustomMobListener>();
	public static HashSet<ConfigCustomMobs> disabledMobs = new HashSet<ConfigCustomMobs>();
	
	public EntityLiving entity;
	private String level;
	private String type;
	
	public HashMap<String, String> dataMap = new HashMap<String, String>();
	public HashMap<String, MobComponent> components = new HashMap<String, MobComponent>();
	public LinkedList<MobDrop> drops = new LinkedList<MobDrop>();
	
	/* Anti-trap stuff */
	//private static int 
	private String targetName;
	private Location lastLocation;
	
	private int coinMin = 0;
	private int coinMax = 0;
	
	public Location getLocation(EntityLiving entity) {
		World world = Bukkit.getWorld(entity.world.getWorld().getName());
		Location loc = new Location(world, entity.locX, entity.locY, entity.locZ);
		return loc;
	}
	
	@Override
	public String getBaseEntity() {
		return null;
	}

	public void onCreate() {
	}

	public void onCreateAttributes() {
	
	}

	public void onDamage(EntityCreature e, DamageSource damagesource, PathfinderGoalSelector goalSelector, PathfinderGoalSelector targetSelector) {

		
	}

	@Override
	public void onDeath(EntityCreature arg0) {
		dropItems();
	}

	public void onRangedAttack(Entity arg1) {
		
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
					/* This player is no longer online. Lose target. */
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
		if (tc != null) {
			entity.getBukkitEntity().remove();
		}
	}
	
	private void checkForisWarTime() {
		if (War.isWarTime()) {
			entity.getBukkitEntity().remove();
		}
	}

	private int tickCount = 0;
	@Override
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

	public void setData(String key, String value) {
		dataMap.put(key, value);
	}
	
	
	public String getData(String key) {
		return dataMap.get(key);
	}

	@Override
	public void setEntity(EntityLiving e) {
		this.entity = e;
	}

	public Collection<MobComponent> getMobComponents() {
		return this.components.values();
	}

	public void addComponent(MobComponent comp) {
		this.components.put(comp.getClass().getName(), comp);
	}
	
	public static CustomMobListener getCCM(Entity e) {
		if (!(e instanceof ISpawnable)) {
			return null;
		}
		
		ISpawnable spawn = (ISpawnable)e;
		return (CustomMobListener)spawn.getCustomMobInterface();
	}
	
	public static CustomMobListener getCCM(org.bukkit.entity.Entity entity) {
		Entity e = ((CraftEntity)entity).getHandle();
		return getCCM(e);
	}
	
	
	public void setAttack(double attack) {
		entity.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attack);
	}
	
	public void setMovementSpeed(double speed) {
		entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
	}
	
	public void setFollowRange(double range) {
		entity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(range);
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
	
	public void modifySpeed(double percent) {
		double speed = entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
		speed *= percent;
		setMovementSpeed(speed);
	}
	
	public void setMaxHealth(double health) {
		entity.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
		entity.setHealth((float) health);
	}
	
	public void setKnockbackResistance(double resist) {
		entity.getAttributeInstance(GenericAttributes.c).setValue(resist);
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

	public void addVanillaDrop(int type, short data, double chance) {
		MobDrop drop = new MobDrop();
		drop.isVanillaDrop = true;
		drop.vanillaType = type;
		drop.vanillaData = data;
		drop.chance = chance;
		
		this.drops.add(drop);
	}
	
	public void addDrop(String craftMatId, double chance) {
		MobDrop drop = new MobDrop();
		drop.isVanillaDrop = false;
		drop.craftMatId = craftMatId;
		drop.chance = chance;
		this.drops.add(drop);
	}
	
	public LinkedList<MobDrop> getRandomDrops() {
		Random rand = new Random();
		LinkedList<MobDrop> dropped = new LinkedList<MobDrop>();
		
		for (MobDrop d : drops) {
			int chance = rand.nextInt(1000);
			if (chance < (d.chance*1000)) {
				/* Dropping this item! */
				dropped.add(d);
			}
		}
		return dropped;
	}
	
	//TODO Fix this, It does not work at all.
	public void dropItems() {
		try {
			if (entity == null) {
				return;
			}
			
			LinkedList<MobDrop> dropped = getRandomDrops();
			World world = entity.getBukkitEntity().getWorld();
			Location loc = getLocation(entity);
			
			for (MobDrop d : dropped) {
				ArrayList<ItemStack> newItems = new ArrayList<ItemStack>();
				if (d.isVanillaDrop) {
					newItems.add(ItemManager.createItemStack(d.vanillaType, 1, d.vanillaData));
				} else {
					LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(d.craftMatId);
					newItems.add(LoreCraftableMaterial.spawn(craftMat));
				}
				for (ItemStack item : newItems) {
				world.dropItem(loc, item);
				}
			}
			
/*			for (MobDrop d : dropped) {
				ItemStack stack;
				if (d.isVanillaDrop) {
					stack = ItemManager.createItemStack(d.vanillaType, 1, d.vanillaData);
				} else {
					LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(d.craftMatId);
					stack = LoreCraftableMaterial.spawn(craftMat);
				}
				world.dropItem(loc, stack);
			}*/
			
			if (this.coinMax != 0 && this.coinMin != 0) {
				Random random = new Random();
				int coins = random.nextInt(this.coinMax - this.coinMin) + this.coinMin;
				ExperienceOrb orb = world.spawn(loc, ExperienceOrb.class);
				orb.setExperience(coins);

			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void coinDrop(int min, int max) {
		this.coinMin = min;
		this.coinMax = max;
	}
	
	public String getLevel() {
		return level;
	}
	
	public void setLevel(String level) {
		this.level = level;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
}
