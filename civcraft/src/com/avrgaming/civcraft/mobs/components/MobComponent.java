package com.avrgaming.civcraft.mobs.components;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.avrgaming.civcraft.mobs.CustomMobListener;

public class MobComponent {

	public void onDefense(EntityDamageByEntityEvent event) {}

	public static void onDefense(Entity entity, EntityDamageByEntityEvent event) {
		if (CustomMobListener.customMobs.get(entity.getUniqueId()) != null) {
			LivingEntity custom = (LivingEntity) entity;
			if (custom != null) {
				for (MobComponent comp : CustomMobListener.getMobComponents()) {
					comp.onDefense(event);
				}
			}
		}
	}
}
