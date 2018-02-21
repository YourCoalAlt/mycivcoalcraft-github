package com.avrgaming.civcraft.mobs;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.mobs.components.MobComponentDefense;

import moblib.mob.ICustomMob;
import moblib.mob.MobBaseZombieGiant;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_12_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_12_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_12_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_12_R1.PathfinderGoalSelector;

public class YoboBoss extends CommonCustomMob implements ICustomMob {
	private String entityType = MobBaseZombieGiant.class.getName();

	
	public void onCreate() {
	    initLevelAndType();
		
	    MobBaseZombieGiant zombie = (MobBaseZombieGiant)this.entity;
	    zombie.length *= 6.0f;
	    
	    getGoalSelector().a(7, new PathfinderGoalRandomStroll((EntityCreature) entity, 100.0F));
	    getGoalSelector().a(8, new PathfinderGoalLookAtPlayer((EntityInsentient) entity, EntityHuman.class, 8.0F));
	    getGoalSelector().a(2, new PathfinderGoalMeleeAttack((EntityCreature) entity, 100.0F, false));
	    getTargetSelector().a(2, new PathfinderGoalNearestAttackableTarget<EntityHuman>((EntityCreature) entity, EntityHuman.class, true));

	    MobComponentDefense defense = new MobComponentDefense(9.0);
	    this.addComponent(defense);
	    
	    this.setName(this.getLevel().getName()+" "+this.getType().getName());
	}
	
	public void onCreateAttributes() {
		MobBaseZombieGiant zombie = (MobBaseZombieGiant)this.entity;
		zombie.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(200.0D);
		zombie.getAttributeInstance(GenericAttributes.maxHealth).setValue(5000.0D);
		zombie.getAttributeInstance(GenericAttributes.c).setValue(1.0D);
		zombie.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(200000.0D);
		//AttributeModifier mod = new AttributeModifier()
		zombie.setHealth(5000.0f);
	}
	
	@Override
	public String getBaseEntity() {
		return entityType;
	}
	
	@Override
	public void onDamage(EntityCreature e, DamageSource damagesource, PathfinderGoalSelector goalSelector, PathfinderGoalSelector targetSelector) {
		goalSelector.a(2, new PathfinderGoalMeleeAttack(e, 1.0D, false));
		for (int i = 0; i < 6; i++) {
			try {
				MobSpawner.spawnCustomMob(MobSpawner.CustomMobType.ANGRYYOBO, this.getLevel(), getLocation(e));
			} catch (CivException e1) {
				e1.printStackTrace();
			}
		}		
	}

	@Override
	public String getClassName() {
		return YoboBoss.class.getName();
	}
	
}
