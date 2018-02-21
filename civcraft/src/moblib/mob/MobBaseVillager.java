package moblib.mob;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityVillager;
import net.minecraft.server.v1_12_R1.World;

public class MobBaseVillager extends EntityVillager {

	public MobBaseVillager(World arg0) {
		super(arg0);
	}
	
	
	public static Entity spawn(Location loc, String name) {
		CraftWorld world = (CraftWorld) loc.getWorld();
		World mcWorld = world.getHandle();
		MobBaseVillager villager = new MobBaseVillager(mcWorld);
		
		villager.setPosition(loc.getX(), loc.getY(), loc.getZ());
		mcWorld.addEntity(villager, SpawnReason.CUSTOM);
		return villager;
	}
}
