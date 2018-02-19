/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.cache;

import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;

import com.avrgaming.civcraft.components.ProjectileCannonComponent;

public class CannonFiredCache {
	
	private ProjectileCannonComponent fromTower;
	private Location target;
	private Fireball fireball;
	private UUID uuid;
	private Calendar expired;
	private boolean hit = false;
	
	public CannonFiredCache(ProjectileCannonComponent fromTower, Location target, Fireball fireball) {
		this.setFromTower(fromTower);
		this.target = target;
		this.fireball = fireball;
		this.uuid = fireball.getUniqueId();
		expired = Calendar.getInstance();
		expired.set(Calendar.SECOND, 30);
	}
	
	public ProjectileCannonComponent getFromTower() {
		return fromTower;
	}
	
	public void setFromTower(ProjectileCannonComponent fromTower) {
		this.fromTower = fromTower;
	}
	
	public Location getTarget() {
		return target;
	}
	
	public void setTarget(Location target) {
		this.target = target;
	}
	
	public Fireball getFireball() {
		return fireball;
	}
	
	public void setFireball(Fireball fireball) {
		this.fireball = fireball;
	}
	
	public Object getUUID() {
		return uuid;
	}
	
	public Calendar getExpired() {
		return expired;
	}
	
	public void setExpired(Calendar expired) {
		this.expired = expired;
	}
	
	public boolean isHit() {
		return hit;
	}
	
	public void setHit(boolean hit) {
		this.hit = hit;
	}
	
	public void destroy(Fireball fireball) {
		fireball.remove();
		CivCache.cannonBallsFired.remove(this.getUUID());	
	}
	
	public void destroy(Entity damager) {
		if (damager instanceof Fireball) {
			this.destroy((Fireball)damager);
		}
	}
}
