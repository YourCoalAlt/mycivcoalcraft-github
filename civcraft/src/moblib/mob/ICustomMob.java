package moblib.mob;

import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.PathfinderGoalSelector;

public interface ICustomMob {
	public void onCreate();
	public void onCreateAttributes();
	public void onTick();
	public String getBaseEntity();
	public void onDamage(EntityCreature e, DamageSource source,
			PathfinderGoalSelector goalSelector, PathfinderGoalSelector targetSelector);
	public void onDeath(EntityCreature e);
	public void onRangedAttack(Entity target);
	
	public void setData(String key, String value);
	public String getData(String key);
	public void setEntity(EntityLiving e);
	public String getSaveString();
	public void loadSaveString(String str);
	public String getClassName();
}
