package com.avrgaming.civcraft.items.components;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementUnitGainAttack;
import com.avrgaming.civcraft.main.CivCraft;
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
		if (Unit.isWearingAnyLeather(event.getPlayer())) {
			event.setCancelled(true);
			CivMessage.sendError(event.getPlayer(), "Cannot use a gun while wearing leather armor.");
			return;
		}
		
		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			Player p = event.getPlayer();
			Snowball sb = p.launchProjectile(Snowball.class);
			sb.setShooter(p);
			sb.setCustomName("bullet"); sb.setCustomNameVisible(false);
			sb.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE, 1, (float) 0.5);
			sb.getWorld().playEffect(p.getEyeLocation(), Effect.SMOKE, 20);
			
			double dmg = this.getDouble("value");
			ConfigUnit u = Unit.getPlayerUnit(p);
			ItemStack unit = Unit.getPlayerUnitStack(p);
			AttributeUtil a = new AttributeUtil(unit);
			
			double unitper = 1.0;
			if (u != null) { 
//				if (u.id.equals("u_musketman")) { dmg *= 0.9; }
				// Additional attack dmg always gets added, reguardless of unit type.
				for (LoreEnhancement enh : a.getEnhancements()) {
					if (enh instanceof LoreEnhancementUnitGainAttack) {
						unitper += (enh.getLevel(a)*0.05);
					}
				}
			}
			
			dmg *= unitper;
			
			Resident resident = CivGlobal.getResident(((Player)sb.getShooter()));
			if (p.getInventory().getItemInMainHand().getType() == Material.BEDROCK) {
				CivMessage.send(resident, CivColor.RoseBold+"[Warning] "+CivColor.LightGray+"Your gun was not found, nerfing bullet damage by 50%.");
			} else {
				if (!resident.hasTechForItem(p.getInventory().getItemInMainHand())) dmg = dmg/2;
			}
			
			if (dmg < 1.0) dmg = 1.0;
			sb.setMetadata("damage", new FixedMetadataValue(CivCraft.getPlugin(), dmg));
		}
	}
	
	@Override
	public void onBulletAttack(ProjectileHitEvent event, ItemStack inHand) {
		if (event.getEntity() instanceof Snowball) {
			Snowball sb = (Snowball) event.getEntity();
			double dmg = this.getDouble("value");
			if (sb.getMetadata("damage") != null) dmg = sb.getMetadata("damage").get(0).asDouble();
			if (sb.getShooter() instanceof Player) {
				Player p = (Player) sb.getShooter();
				@SuppressWarnings("deprecation")
				EntityDamageByEntityEvent newevent = new EntityDamageByEntityEvent(sb, (Entity)p, DamageCause.PROJECTILE, dmg);
				this.onProjectileHit(newevent, inHand);
			}
		}
	}
	
	public void onProjectileHit(EntityDamageByEntityEvent event, ItemStack inHand) {
		double dmg = 0.0;
		CivMessage.global(""+dmg);
		
		if (event.getDamager() instanceof Snowball) {
			Snowball sb = (Snowball)event.getDamager();
			if (!(sb.getCustomName().equals("bullet"))) return;
			
			if (sb.getMetadata("damage") != null) dmg = sb.getMetadata("damage").get(0).asDouble();
			event.setDamage(dmg);
			CivMessage.global(""+dmg);
			
			
			if (sb.getShooter() instanceof Player) {
				Player attacker = (Player)sb.getShooter();
				if (CivGlobal.getResident(((Player)sb.getShooter())).isCombatInfo()) {
					CivMessage.send(attacker, CivColor.LightGray+"    [Combat] Gave "+CivColor.LightGreen+dmg+CivColor.LightGray+" damage to "+CivColor.LightPurple+event.getEntity().getName()
									+CivColor.LightGray+" using Gun");
				}
			}
		}
	}
}
