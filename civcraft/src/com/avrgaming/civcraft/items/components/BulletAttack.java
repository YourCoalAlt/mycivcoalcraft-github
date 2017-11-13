package com.avrgaming.civcraft.items.components;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementUnitGainAttack;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class BulletAttack extends ItemComponent {
	
	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		attrs.addLore(" ");
		attrs.addLore(CivColor.LightGray+"When Shot:");
		attrs.addLore(CivColor.Navy+" +"+this.getDouble("value")+" Attack Damage");
	}
	
	@Override
	public void onHold(PlayerItemHeldEvent event) {	
		Resident resident = CivGlobal.getResident(event.getPlayer());
		if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {		
			CivMessage.send(resident, CivColor.RoseBold+"[Warning] "+CivColor.LightGray+"You do not have the required technology to use this item. Its attack output will be reduced in half.");
		}
	}
	
	@Override
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			Player p = event.getPlayer();
			Snowball sb = p.launchProjectile(Snowball.class);
			sb.setCustomName("bullet"); sb.setCustomNameVisible(false);
			sb.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE, 1, (float) 0.5);
			sb.getWorld().playEffect(p.getEyeLocation(), Effect.SMOKE, 20);
		}
	}
	
/*	@Override
	public void onInteract(PlayerInteractEvent event) {
		if (Unit.isWearingAnyLeather(event.getPlayer())) {
			event.setCancelled(true);
			CivMessage.sendError(event.getPlayer(), "Cannot use a gun while wearing leather armor.");
			return;
		}
	}*/
	
//	@EventHandler
//	public void onProjectileHit(ProjectileHitEvent event) {
//		event.getEntity().
//	}
	
	@Override
	public void onBulletAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
		double dmg = this.getDouble("value");
		
		if (event.getDamager() instanceof Snowball) {
			Snowball sb = (Snowball)event.getDamager();
			if (!(sb.getCustomName().equals("bullet"))) { return; }
			
			if (sb.getShooter() instanceof Player) {
				Player attacker = (Player)sb.getShooter();
/*				for (Entity ent : sb.getNearbyEntities(5, 30, 5)) { //we get the entities in a 5 block radius of where the snowball hit
					if (ent instanceof Player) {
						Player shot = (Player) ent;
						shot.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
						CivMessage.send(attacker, "You shot player "+shot.getName()+"!");
					}
				}*/
				
				ConfigUnit u = Unit.getPlayerUnit(attacker);
				ItemStack unit = Unit.getPlayerUnitStack(attacker);
				AttributeUtil a = new AttributeUtil(unit);
				
				double unitper = 1.0;
				if (u != null) { 
//					if (u.id.equals("u_warrior")) { dmg *= 0.9; }
//					else if (u != null && u.id.equals("u_archer")) { dmg *= 1.25; }
					// Additional attack dmg always gets added, reguardless of unit type.
					for (LoreEnhancement enh : a.getEnhancements()) {
						if (enh instanceof LoreEnhancementUnitGainAttack) {
							unitper += (enh.getLevel(a)*0.05);
						}
					}
				}
				
				dmg *= unitper;
				
				Resident resident = CivGlobal.getResident(((Player)sb.getShooter()));
				if (inHand.getType() == Material.BEDROCK) {
					CivMessage.send(resident, CivColor.RoseBold+"[Warning] "+CivColor.LightGray+"Your gun was not found, nerfing bullet damage by 50%.");
				} else {
					if (!resident.hasTechForItem(inHand)) {	dmg = dmg/2; }
				}
			}
			
			if (dmg < 0.2) { dmg = 0.2; }
			
			// Need final dmg set to send the info out.
			if (sb.getShooter() instanceof Player) {
				Player attacker = (Player)sb.getShooter();
				if (CivGlobal.getResident(((Player)sb.getShooter())).isCombatInfo()) {
					CivMessage.send(attacker, CivColor.LightGray+"    [Combat] Gave "+CivColor.LightGreen+dmg+CivColor.LightGray+" damage to "+CivColor.LightPurple+event.getEntity().getName());
				}
				if (event.getEntity() instanceof Player) {
					Player shot = (Player) event.getEntity();
					if (CivGlobal.getResident(shot).isCombatInfo()) {
						CivMessage.send(shot, CivColor.LightGray+"  [Combat] Took "+CivColor.Rose+dmg+" damage "+CivColor.LightGray+" from "+CivColor.LightPurple+attacker.getName());
					}
				}
			}
		}
		event.setDamage(dmg);
	}
}
